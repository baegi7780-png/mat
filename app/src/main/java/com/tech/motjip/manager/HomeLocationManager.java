package com.tech.motjip.manager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.tech.motjip.Controller.TestController;
import com.tech.motjip.Thread.IThreadCallback;

public class HomeLocationManager {

    private final Fragment fragment;
    private final MapView mapView;
    private final TestController controller;
    private final IThreadCallback callback;

    private final FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String[]> permissionLauncher;

    public HomeLocationManager(
            Fragment fragment,
            MapView mapView,
            TestController controller,
            IThreadCallback callback
    ) {
        this.fragment = fragment;
        this.mapView = mapView;
        this.controller = controller;
        this.callback = callback;

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(
                        fragment.requireActivity()
                );

        permissionLauncher =
                fragment.registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {

                            Boolean fine =
                                    result.getOrDefault(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            false
                                    );

                            Boolean coarse =
                                    result.getOrDefault(
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            false
                                    );

                            if (fine || coarse) {
                                fetchLocationAndStartMap();
                            } else {
                                startDefaultMap();
                            }
                        }
                );
    }

    public void startMap() {

        if (ActivityCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        fragment.requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            permissionLauncher.launch(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }
            );

            return;
        }

        fetchLocationAndStartMap();
    }

    private void fetchLocationAndStartMap() {

        if (!fragment.isAdded()) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        fragment.requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            startDefaultMap();
            return;
        }

        try {
            fusedLocationClient
                    .getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            null
                    )
                    .addOnSuccessListener(location -> {

                        if (!fragment.isAdded()) {
                            return;
                        }

                        if (location != null) {
                            startMapWithLocation(location);
                        } else {
                            fetchLastLocationFallback();
                        }
                    })
                    .addOnFailureListener(e -> {

                        Log.e(
                                "HOME_LOCATION",
                                "getCurrentLocation 실패",
                                e
                        );

                        fetchLastLocationFallback();
                    });

        } catch (SecurityException e) {

            Log.e(
                    "HOME_LOCATION",
                    "위치 요청 SecurityException",
                    e
            );

            startDefaultMap();
        }
    }

    private void fetchLastLocationFallback() {

        if (!fragment.isAdded()) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        fragment.requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            startDefaultMap();
            return;
        }

        try {
            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (!fragment.isAdded()) {
                            return;
                        }

                        if (location != null) {
                            startMapWithLocation(location);
                        } else {
                            startDefaultMap();
                        }
                    })
                    .addOnFailureListener(e -> {

                        Log.e(
                                "HOME_LOCATION",
                                "lastLocation 실패",
                                e
                        );

                        startDefaultMap();
                    });

        } catch (SecurityException e) {

            Log.e(
                    "HOME_LOCATION",
                    "lastLocation SecurityException",
                    e
            );

            startDefaultMap();
        }
    }

    private void startMapWithLocation(Location location) {

        LatLng latLng =
                LatLng.from(
                        location.getLatitude(),
                        location.getLongitude()
                );

        controller.mapStart(
                mapView,
                callback,
                latLng
        );
    }

    private void startDefaultMap() {

        controller.mapStart(
                mapView,
                callback
        );
    }
}