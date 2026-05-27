package com.tech.motjip;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.google.firebase.messaging.FirebaseMessaging;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Auth.TokenManager;
import com.tech.motjip.Controller.MainController;
import com.tech.motjip.Dto.RequestDto.FcmTokenRequestDto;
import com.tech.motjip.Dto.RequestDto.UpdateLocationRequestDto;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.Handler.BaseActivity;
import com.tech.motjip.Utils.DialogUtil;
import com.tech.motjip.Utils.LoginStateManager;
import com.tech.motjip.Utils.WebBrowserUtil;

import java.security.MessageDigest;

import dagger.hilt.android.AndroidEntryPoint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MainActivity extends BaseActivity
        implements MainController.MainControllerCallback {

    private static final String TAG =
            "MainActivityDebug";

    private static final int LOCATION_PERMISSION_REQUEST_CODE =
            3001;

    private MainController mainController;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(
                        this
                );

        getHashKey();

        Log.d(
                TAG,
                "onCreate() - MainActivity started"
        );

        mainController =
                new MainController(
                        this,
                        this
                );

        Intent intent =
                getIntent();

        Log.d(
                TAG,
                "받은 intent = "
                        + intent
        );

        if (intent != null
                && intent.getData() != null) {

            Log.d(
                    TAG,
                    "intent data = "
                            + intent.getData()
            );
        }

        String authExpiredMessage =
                intent != null
                        ? intent.getStringExtra(
                        RetrofitClient.AUTH_EXPIRED_MESSAGE
                )
                        : null;

        if (authExpiredMessage != null
                && !authExpiredMessage.isEmpty()) {

            Log.d(
                    TAG,
                    "토큰 만료 자동 로그아웃"
            );

            LoginStateManager.setLoginStatus(
                    this,
                    LoginStateManager.LOGOUT
            );

            initializeLoginScreen();

            DialogUtil.showMessageDialog(
                    this,
                    R.drawable.fail,
                    "자동 로그아웃",
                    authExpiredMessage,
                    null
            );

            return;
        }

        boolean isDeepLinkHandled =
                mainController.handleDeepLinkIfNeeded(
                        intent
                );

        Log.d(
                TAG,
                "딥링크 처리 결과 = "
                        + isDeepLinkHandled
        );

        if (isDeepLinkHandled) {

            Log.d(
                    TAG,
                    "딥링크 처리 후 종료"
            );

            return;
        }

        Log.d(
                TAG,
                "자동 로그인 체크 시작"
        );

        mainController.checkAutoLogin();
    }

    @Override
    protected void onResume() {

        super.onResume();

        Log.d(
                TAG,
                "onResume 호출됨"
        );
    }

    @Override
    protected void onNewIntent(
            Intent intent
    ) {

        super.onNewIntent(
                intent
        );

        setIntent(
                intent
        );

        Log.d(
                TAG,
                "onNewIntent 호출됨"
        );

        Log.d(
                TAG,
                "new intent = "
                        + intent
        );

        if (intent != null
                && intent.getData() != null) {

            Log.d(
                    TAG,
                    "new intent data = "
                            + intent.getData()
            );
        }

        boolean handled =
                mainController.handleDeepLinkIfNeeded(
                        intent
                );

        Log.d(
                TAG,
                "onNewIntent 딥링크 처리 결과 = "
                        + handled
        );
    }

    private void getHashKey() {

        try {

            PackageInfo info =
                    getPackageManager()
                            .getPackageInfo(
                                    getPackageName(),
                                    PackageManager.GET_SIGNING_CERTIFICATES
                            );

            for (android.content.pm.Signature signature
                    : info.signingInfo.getApkContentsSigners()) {

                MessageDigest md =
                        MessageDigest.getInstance(
                                "SHA"
                        );

                md.update(
                        signature.toByteArray()
                );

                String hashKey =
                        Base64.encodeToString(
                                md.digest(),
                                Base64.NO_WRAP
                        );

                Log.d(
                        TAG,
                        "카카오 HASH_KEY = "
                                + hashKey
                );
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "HASH_KEY 오류",
                    e
            );
        }
    }

    private void initializeLoginScreen() {

        setContentView(
                R.layout.activity_main
        );

        LinearLayoutCompat btnKakaoLogin =
                findViewById(
                        R.id.btnKakaoLogin
                );

        LinearLayoutCompat btnGoogleLogin =
                findViewById(
                        R.id.btnGoogleLogin
                );

        btnKakaoLogin.setOnClickListener(v -> {

            Log.d(
                    TAG,
                    "카카오 로그인 버튼 클릭"
            );

            mainController.loginWithKakaoSdk();
        });

        btnGoogleLogin.setOnClickListener(v -> {

            Log.d(
                    TAG,
                    "구글 로그인 버튼 클릭"
            );

            String url =
                    "https://accounts.google.com/o/oauth2/v2/auth?"
                            + "client_id=733059527774-sb6lg9a1nfiuicv713h62gr9kvjmfpul.apps.googleusercontent.com"
                            + "&redirect_uri=https://spout-distant-cost.ngrok-free.dev/login/oauth2/code/google"
                            + "&response_type=code"
                            + "&scope=openid%20email%20profile";

            Log.d(
                    TAG,
                    "구글 로그인 URL = "
                            + url
            );

            try {

                WebBrowserUtil.openWebBrowser(
                        this,
                        url
                );

                Log.d(
                        TAG,
                        "브라우저 실행 성공"
                );

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "브라우저 실행 실패",
                        e
                );
            }
        });
    }

    private void moveNextByUser(
            LoginResponseDto user
    ) {

        Log.d(
                TAG,
                "로그인 성공 user = "
                        + user
        );

        if (user == null) {

            Log.e(
                    TAG,
                    "moveNextByUser 중단 - user null"
            );

            LoginStateManager.setLoginStatus(
                    this,
                    LoginStateManager.LOGOUT
            );

            initializeLoginScreen();

            DialogUtil.showMessageDialog(
                    this,
                    R.drawable.fail,
                    "로그인 실패",
                    "사용자 정보를 받지 못했습니다.",
                    null
            );

            return;
        }

        Log.d(
                TAG,
                "accessToken exists = "
                        + (user.getAccessToken() != null
                        && !user.getAccessToken().trim().isEmpty())
        );

        Log.d(
                TAG,
                "refreshToken exists = "
                        + (user.getRefreshToken() != null
                        && !user.getRefreshToken().trim().isEmpty())
        );

        /*
         * 로그인 성공 직후 토큰 저장
         *
         * 닉네임이 이미 있는 사용자는 NicknameActivity를 거치지 않고
         * 바로 HomeActivity로 이동하기 때문에 여기서 반드시 토큰을 저장해야 한다.
         */
        if (user.getAccessToken() != null
                && !user.getAccessToken().trim().isEmpty()
                && user.getRefreshToken() != null
                && !user.getRefreshToken().trim().isEmpty()) {

            TokenManager tokenManager =
                    new TokenManager(
                            this
                    );

            tokenManager.saveTokens(
                    user.getAccessToken(),
                    user.getRefreshToken()
            );

            Log.d(
                    TAG,
                    "로그인 성공 후 토큰 저장 완료"
            );

        } else {

            Log.e(
                    TAG,
                    "로그인 응답에 토큰 없음"
            );
        }

        LoginStateManager.setLoginStatus(
                this,
                LoginStateManager.LOGIN
        );

        SharedPreferences authPrefs =
                getSharedPreferences(
                        "auth",
                        MODE_PRIVATE
                );

        authPrefs.edit()
                .putLong(
                        "memberId",
                        user.getMemberId()
                )
                .putString(
                        "nickname",
                        user.getNickname()
                )
                .apply();

        sendFcmTokenToServer();

        sendLocationToServer();

        if (user.getNickname() == null
                || user.getNickname().isEmpty()) {

            Log.d(
                    TAG,
                    "닉네임 없음 → NicknameActivity 이동"
            );

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            NicknameActivity.class
                    );

            intent.putExtra(
                    "member_id",
                    user.getMemberId()
            );

            intent.putExtra(
                    "nickname",
                    user.getNickname()
            );

            startActivity(
                    intent
            );

        } else {

            Log.d(
                    TAG,
                    "닉네임 있음 → HomeActivity 이동"
            );

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HomeActivity.class
                    );

            intent.putExtra(
                    "LOGIN_USER_INFO",
                    user
            );

            startActivity(
                    intent
            );
        }
    }

    private void sendFcmTokenToServer() {

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    if (token == null
                            || token.trim().isEmpty()) {

                        Log.d(
                                TAG,
                                "FCM Token 없음"
                        );

                        return;
                    }

                    Log.d(
                            TAG,
                            "현재 FCM Token = "
                                    + token
                    );

                    ApiService apiService =
                            RetrofitClient.getApiService(
                                    this
                            );

                    apiService.updateFcmToken(
                            new FcmTokenRequestDto(
                                    token
                            )
                    ).enqueue(new Callback<Void>() {

                        @Override
                        public void onResponse(
                                @NonNull Call<Void> call,
                                @NonNull Response<Void> response
                        ) {

                            if (response.isSuccessful()) {

                                Log.d(
                                        TAG,
                                        "FCM Token 서버 저장 성공"
                                );

                            } else {

                                Log.d(
                                        TAG,
                                        "FCM Token 서버 저장 실패 code = "
                                                + response.code()
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                @NonNull Call<Void> call,
                                @NonNull Throwable t
                        ) {

                            Log.d(
                                    TAG,
                                    "FCM Token 서버 저장 통신 실패 = "
                                            + t.getMessage()
                            );
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(
                                TAG,
                                "FCM Token 가져오기 실패",
                                e
                        )
                );
    }

    private void sendLocationToServer() {

        Log.d(
                TAG,
                "sendLocationToServer 시작"
        );

        if (fusedLocationClient == null) {

            fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(
                            this
                    );
        }

        if (!hasFineLocationPermission()) {

            Log.d(
                    TAG,
                    "위치 권한 없음 → 권한 요청"
            );

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            return;
        }

        requestCurrentLocation();
    }

    private boolean hasFineLocationPermission() {

        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCurrentLocation() {

        Log.d(
                TAG,
                "현재 위치 요청 시작"
        );

        if (!hasFineLocationPermission()) {

            Log.d(
                    TAG,
                    "requestCurrentLocation 중단 - 위치 권한 없음"
            );

            return;
        }

        try {

            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {

                if (location != null) {

                    Log.d(
                            TAG,
                            "getCurrentLocation 성공"
                    );

                    saveLocationToServer(
                            location
                    );

                } else {

                    Log.d(
                            TAG,
                            "getCurrentLocation 결과 null → lastLocation 재시도"
                    );

                    requestLastLocationFallback();
                }

            }).addOnFailureListener(e -> {

                Log.e(
                        TAG,
                        "getCurrentLocation 실패 → lastLocation 재시도",
                        e
                );

                requestLastLocationFallback();
            });

        } catch (SecurityException e) {

            Log.e(
                    TAG,
                    "현재 위치 요청 SecurityException",
                    e
            );
        }
    }

    private void requestLastLocationFallback() {

        Log.d(
                TAG,
                "lastLocation fallback 시작"
        );

        if (!hasFineLocationPermission()) {

            Log.d(
                    TAG,
                    "lastLocation 중단 - 위치 권한 없음"
            );

            return;
        }

        try {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (location != null) {

                            Log.d(
                                    TAG,
                                    "lastLocation 성공"
                            );

                            saveLocationToServer(
                                    location
                            );

                        } else {

                            Log.d(
                                    TAG,
                                    "lastLocation도 null - 위치 저장 실패"
                            );
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e(
                                    TAG,
                                    "lastLocation 실패",
                                    e
                            )
                    );

        } catch (SecurityException e) {

            Log.e(
                    TAG,
                    "lastLocation SecurityException",
                    e
            );
        }
    }

    private void saveLocationToServer(
            Location location
    ) {

        if (location == null) {

            Log.d(
                    TAG,
                    "saveLocationToServer 중단 - location null"
            );

            return;
        }

        double latitude =
                location.getLatitude();

        double longitude =
                location.getLongitude();

        Log.d(
                TAG,
                "저장할 현재 위치 = "
                        + latitude
                        + ", "
                        + longitude
                        + ", accuracy="
                        + location.getAccuracy()
        );

        UpdateLocationRequestDto requestDto =
                new UpdateLocationRequestDto();

        requestDto.setLatitude(
                latitude
        );

        requestDto.setLongitude(
                longitude
        );

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        apiService.updateMyLocation(
                requestDto
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    Log.d(
                            TAG,
                            "위치 서버 저장 성공"
                    );

                } else {

                    Log.d(
                            TAG,
                            "위치 서버 저장 실패 code = "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable t
            ) {

                Log.e(
                        TAG,
                        "위치 서버 저장 통신 실패",
                        t
                );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                Log.d(
                        TAG,
                        "위치 권한 허용됨 → 위치 저장 재시도"
                );

                sendLocationToServer();

            } else {

                Log.d(
                        TAG,
                        "위치 권한 거부됨"
                );
            }
        }
    }

    @Override
    public void onNeedLoginScreen() {

        Log.d(
                TAG,
                "로그인 화면 필요"
        );

        LoginStateManager.setLoginStatus(
                this,
                LoginStateManager.LOGOUT
        );

        initializeLoginScreen();
    }

    @Override
    public void onLoginSuccess(
            LoginResponseDto user
    ) {

        Log.d(
                TAG,
                "onLoginSuccess 호출"
        );

        moveNextByUser(
                user
        );
    }

    @Override
    public void onLoginFail(
            String message
    ) {

        Log.e(
                TAG,
                "로그인 실패 = "
                        + message
        );

        LoginStateManager.setLoginStatus(
                this,
                LoginStateManager.LOGOUT
        );

        DialogUtil.showMessageDialog(
                this,
                R.drawable.fail,
                "로그인 실패",
                message,
                null
        );
    }

    @Override
    public void onNeedFinish() {

        Log.d(
                TAG,
                "Activity 종료"
        );

        finish();
    }
}