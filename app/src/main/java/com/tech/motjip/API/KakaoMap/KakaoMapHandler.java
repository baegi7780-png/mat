package com.tech.motjip.API.KakaoMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.CompetitionType;
import com.kakao.vectormap.label.CompetitionUnit;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelLayerOptions;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;
import com.tech.motjip.API.KakaoMap.CallbackInterface.IViewDetailItemClickCallback;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.RecommendedPlace;
import com.tech.motjip.R;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import lombok.NonNull;

public class KakaoMapHandler {

    private static final String TAG =
            "KakaoMapHandlerDebug";

    private static final int RECOMMEND_MARKER_SIZE =
            130;

    private static final int RECOMMEND_RATING_TEXT_SIZE =
            34;

    private static final int RECOMMEND_RATING_TEXT_COLOR =
            Color.rgb(
                    255,
                    193,
                    7
            );

    private final KakaoMap kakaoMap;

    private final Context context;

    private LabelStyles labelStyle;

    private LabelStyles clusterStyle;

    private final ClusterManager clusterManager;

    private final Handler mainHandler =
            new Handler(
                    Looper.getMainLooper()
            );

    private final List<Label> recommendedLabels =
            new ArrayList<>();

    private int lastZoomLevel =
            -1;

    public KakaoMapHandler(
            KakaoMap kakaomap,
            Context context
    ) {

        this.kakaoMap =
                kakaomap;

        this.context =
                context;

        this.labelStyle =
                getDefaultLabelStyle();

        this.clusterStyle =
                getClusterLabelStyle();

        this.clusterManager =
                new ClusterManager(
                        kakaoMap,
                        labelStyle,
                        clusterStyle
                );

        setupCameraListener();
    }

    public KakaoMap getKakaoMap() {

        return kakaoMap;
    }

    public void moveCamera(
            @NonNull LatLng position
    ) {

        kakaoMap.moveCamera(
                CameraUpdateFactory.newCenterPosition(
                        position
                )
        );
    }

    public void setMarker(
            @Nonnull LatLng position,
            String labelText
    ) {

        setMarker(
                position,
                labelText,
                null
        );
    }

    public void setMarker(
            @Nonnull LatLng position,
            String labelText,
            KeywordMapVO vo
    ) {

        LabelLayer layer =
                kakaoMap
                        .getLabelManager()
                        .getLayer(
                                "MyClusterLayer"
                        );

        if (layer == null) {

            LabelLayerOptions layerOptions =
                    LabelLayerOptions.from(
                                    "MyClusterLayer"
                            )
                            .setCompetitionType(
                                    CompetitionType.SameLower
                            )
                            .setCompetitionUnit(
                                    CompetitionUnit.IconAndText
                            )
                            .setZOrder(
                                    10000
                            );

            layer =
                    kakaoMap
                            .getLabelManager()
                            .addLayer(
                                    layerOptions
                            );
        }

        LabelOptions options =
                LabelOptions.from(
                                position
                        )
                        .setStyles(
                                labelStyle
                        );

        Label label =
                layer.addLabel(
                        options
                );

        if (vo != null) {

            label.setTag(
                    vo
            );
        }

        label.changeText(
                new LabelTextBuilder()
                        .setTexts(
                                labelText
                        )
        );
    }

    public void setRecommendedMarkers(
            List<RecommendedPlace> places
    ) {

        Log.d(
                TAG,
                "setRecommendedMarkers 호출 size = "
                        + (
                        places == null
                                ? 0
                                : places.size()
                )
        );

        clearRecommendedMarkers();

        if (places == null
                || places.isEmpty()) {

            return;
        }

        LabelLayer layer =
                getRecommendedLayer();

        for (RecommendedPlace place : places) {

            if (place == null
                    || place.getLatitude() == null
                    || place.getLongitude() == null) {

                Log.e(
                        TAG,
                        "추천 장소 좌표 없음"
                );

                continue;
            }

            LatLng position =
                    LatLng.from(
                            place.getLatitude(),
                            place.getLongitude()
                    );

            KeywordMapVO vo =
                    convertRecommendedPlaceToKeywordMapVO(
                            place
                    );

            String ratingText =
                    place.getFormattedAverageRating();

            if (ratingText == null
                    || ratingText.trim().isEmpty()) {

                ratingText =
                        "";
            }

            Bitmap defaultBitmap =
                    getDefaultRecommendedMarkerBitmap();

            LabelStyles defaultStyle =
                    createRecommendedLabelStyle(
                            defaultBitmap
                    );

            LabelOptions options =
                    LabelOptions.from(
                                    position
                            )
                            .setStyles(
                                    defaultStyle
                            );

            Label label =
                    layer.addLabel(
                            options
                    );

            label.setTag(
                    vo
            );

            label.changeText(
                    new LabelTextBuilder()
                            .setTexts(
                                    "★ " + ratingText
                            )
            );

            recommendedLabels.add(
                    label
            );

            Log.d(
                    TAG,
                    "추천 profileImageUrl = "
                            + place.getProfileImageUrl()
            );

            loadProfileImageAndApplyToLabel(
                    label,
                    place.getProfileImageUrl()
            );
        }
    }

    public void clearRecommendedMarkers() {

        for (Label label : recommendedLabels) {

            if (label == null) {
                continue;
            }

            try {

                label.remove();

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "추천 마커 제거 실패",
                        e
                );
            }
        }

        recommendedLabels.clear();
    }

    public Label setMyLocationMarker(
            @Nonnull LatLng position
    ) {

        LabelLayer layer =
                kakaoMap
                        .getLabelManager()
                        .getLayer(
                                "MyLocationLayer"
                        );

        if (layer == null) {

            LabelLayerOptions layerOptions =
                    LabelLayerOptions.from(
                                    "MyLocationLayer"
                            )
                            .setCompetitionType(
                                    CompetitionType.None
                            )
                            .setZOrder(
                                    20000
                            );

            layer =
                    kakaoMap
                            .getLabelManager()
                            .addLayer(
                                    layerOptions
                            );
        }

        Bitmap originalBitmap =
                BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.ic_my_location_person
                );

        Bitmap resizedBitmap =
                Bitmap.createScaledBitmap(
                        originalBitmap,
                        164,
                        164,
                        true
                );

        LabelStyles myLocationStyle =
                kakaoMap
                        .getLabelManager()
                        .addLabelStyles(
                                LabelStyles.from(
                                        LabelStyle.from(
                                                resizedBitmap
                                        )
                                )
                        );

        LabelOptions options =
                LabelOptions.from(
                                position
                        )
                        .setStyles(
                                myLocationStyle
                        );

        return layer.addLabel(
                options
        );
    }

    public void setMarkers(
            List<KeywordMapVO> markers
    ) {

        clusterManager.setMarkers(
                markers
        );

        lastZoomLevel =
                kakaoMap.getZoomLevel();
    }

    public void setMarkerClickListener(
            IViewDetailItemClickCallback callback
    ) {

        kakaoMap.setOnLabelClickListener(
                new KakaoMap.OnLabelClickListener() {

                    @Override
                    public boolean onLabelClicked(
                            KakaoMap kakaoMap,
                            LabelLayer labelLayer,
                            Label label
                    ) {

                        Object tag =
                                label.getTag();

                        if (tag instanceof KeywordMapVO
                                && callback != null) {

                            callback.onItemClick(
                                    (KeywordMapVO) tag
                            );

                        } else if (tag instanceof List) {

                            zoomInOnCluster();
                        }

                        return true;
                    }
                }
        );
    }

    public void clearMarkers() {

        clusterManager.clear();
    }

    private void setupCameraListener() {

        kakaoMap.setOnCameraMoveEndListener(
                (map, cameraPosition, gestureType) -> {

                    int currentZoom =
                            kakaoMap.getZoomLevel();

                    if (currentZoom != lastZoomLevel) {

                        lastZoomLevel =
                                currentZoom;

                        clusterManager.redraw();
                    }
                }
        );
    }

    private void zoomInOnCluster() {

        int currentZoom =
                kakaoMap.getZoomLevel();

        kakaoMap.moveCamera(
                CameraUpdateFactory.zoomTo(
                        currentZoom + 2
                )
        );
    }

    private LabelLayer getRecommendedLayer() {

        LabelLayer layer =
                kakaoMap
                        .getLabelManager()
                        .getLayer(
                                "RecommendedLayer"
                        );

        if (layer == null) {

            LabelLayerOptions layerOptions =
                    LabelLayerOptions.from(
                                    "RecommendedLayer"
                            )
                            .setCompetitionType(
                                    CompetitionType.None
                            )
                            .setZOrder(
                                    15000
                            );

            layer =
                    kakaoMap
                            .getLabelManager()
                            .addLayer(
                                    layerOptions
                            );
        }

        return layer;
    }

    private void loadProfileImageAndApplyToLabel(
            Label label,
            String profileImageUrl
    ) {

        if (label == null) {
            return;
        }

        if (profileImageUrl == null
                || profileImageUrl.trim().isEmpty()) {

            return;
        }

        new Thread(() -> {

            try {

                String fullUrl =
                        makeFullImageUrl(
                                profileImageUrl
                        );

                Log.d(
                        TAG,
                        "추천 프로필 fullUrl = "
                                + fullUrl
                );

                Bitmap bitmap =
                        downloadBitmap(
                                fullUrl
                        );

                if (bitmap == null) {
                    return;
                }

                Bitmap markerBitmap =
                        createCircleMarkerBitmap(
                                bitmap,
                                RECOMMEND_MARKER_SIZE
                        );

                mainHandler.post(() -> {

                    try {

                        LabelStyles profileStyle =
                                createRecommendedLabelStyle(
                                        markerBitmap
                                );

                        label.changeStyles(
                                profileStyle
                        );

                    } catch (Exception e) {

                        Log.e(
                                TAG,
                                "프로필 마커 스타일 적용 실패",
                                e
                        );
                    }
                });

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "프로필 이미지 로드 실패",
                        e
                );
            }
        }).start();
    }

    private String makeFullImageUrl(
            String imageUrl
    ) {

        if (imageUrl == null) {
            return "";
        }

        if (imageUrl.startsWith("http://")
                || imageUrl.startsWith("https://")) {

            return imageUrl;
        }

        String baseUrl =
                RetrofitClient.BASE_URL;

        if (baseUrl.endsWith("/")
                && imageUrl.startsWith("/")) {

            return baseUrl.substring(
                    0,
                    baseUrl.length() - 1
            ) + imageUrl;
        }

        if (!baseUrl.endsWith("/")
                && !imageUrl.startsWith("/")) {

            return baseUrl + "/" + imageUrl;
        }

        return baseUrl + imageUrl;
    }

    private Bitmap downloadBitmap(
            String imageUrl
    ) {

        HttpURLConnection connection =
                null;

        InputStream inputStream =
                null;

        try {

            URL url =
                    new URL(
                            imageUrl
                    );

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(
                    5000
            );

            connection.setReadTimeout(
                    5000
            );

            connection.setDoInput(
                    true
            );

            connection.connect();

            int responseCode =
                    connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {

                Log.e(
                        TAG,
                        "이미지 다운로드 실패 code = "
                                + responseCode
                );

                return null;
            }

            inputStream =
                    connection.getInputStream();

            return BitmapFactory.decodeStream(
                    inputStream
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "downloadBitmap 실패",
                    e
            );

            return null;

        } finally {

            try {

                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (Exception ignore) {
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Bitmap getDefaultRecommendedMarkerBitmap() {

        Bitmap originalBitmap =
                BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.ic_my_location_person
                );

        return createCircleMarkerBitmap(
                originalBitmap,
                RECOMMEND_MARKER_SIZE
        );
    }

    private LabelStyles createRecommendedLabelStyle(
            Bitmap bitmap
    ) {

        return kakaoMap
                .getLabelManager()
                .addLabelStyles(
                        LabelStyles.from(
                                LabelStyle.from(
                                        bitmap
                                ).setTextStyles(
                                        RECOMMEND_RATING_TEXT_SIZE,
                                        RECOMMEND_RATING_TEXT_COLOR
                                )
                        )
                );
    }

    private Bitmap createCircleMarkerBitmap(
            Bitmap source,
            int size
    ) {

        if (source == null) {

            source =
                    BitmapFactory.decodeResource(
                            context.getResources(),
                            R.drawable.ic_my_location_person
                    );
        }

        Bitmap scaledBitmap =
                Bitmap.createScaledBitmap(
                        source,
                        size,
                        size,
                        true
                );

        Bitmap outputBitmap =
                Bitmap.createBitmap(
                        size,
                        size,
                        Bitmap.Config.ARGB_8888
                );

        Canvas canvas =
                new Canvas(
                        outputBitmap
                );

        Paint paint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        RectF rect =
                new RectF(
                        0,
                        0,
                        size,
                        size
                );

        canvas.drawOval(
                rect,
                paint
        );

        paint.setXfermode(
                new android.graphics.PorterDuffXfermode(
                        android.graphics.PorterDuff.Mode.SRC_IN
                )
        );

        canvas.drawBitmap(
                scaledBitmap,
                0,
                0,
                paint
        );

        paint.setXfermode(
                null
        );

        Paint borderPaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        borderPaint.setStyle(
                Paint.Style.STROKE
        );

        borderPaint.setStrokeWidth(
                8
        );

        borderPaint.setColor(
                Color.WHITE
        );

        canvas.drawOval(
                new RectF(
                        4,
                        4,
                        size - 4,
                        size - 4
                ),
                borderPaint
        );

        return outputBitmap;
    }

    private KeywordMapVO convertRecommendedPlaceToKeywordMapVO(
            RecommendedPlace place
    ) {

        KeywordMapVO vo =
                new KeywordMapVO();

        if (place == null) {
            return vo;
        }

        String id =
                place.getPlaceId() == null
                        ? ""
                        : String.valueOf(
                        place.getPlaceId()
                );

        String placeName =
                place.getPlaceName() == null
                        ? ""
                        : place.getPlaceName();

        String latitude =
                place.getLatitude() == null
                        ? ""
                        : String.valueOf(
                        place.getLatitude()
                );

        String longitude =
                place.getLongitude() == null
                        ? ""
                        : String.valueOf(
                        place.getLongitude()
                );

        setKeywordValue(
                vo,
                "id",
                "setId",
                id
        );

        setKeywordValue(
                vo,
                "place_name",
                "setPlace_name",
                placeName
        );

        setKeywordValue(
                vo,
                "placeName",
                "setPlaceName",
                placeName
        );

        setKeywordValue(
                vo,
                "x",
                "setX",
                longitude
        );

        setKeywordValue(
                vo,
                "y",
                "setY",
                latitude
        );

        setKeywordValue(
                vo,
                "longitude",
                "setLongitude",
                longitude
        );

        setKeywordValue(
                vo,
                "latitude",
                "setLatitude",
                latitude
        );

        return vo;
    }

    private void setKeywordValue(
            KeywordMapVO vo,
            String fieldName,
            String setterName,
            String value
    ) {

        if (vo == null) {
            return;
        }

        try {

            Method method =
                    vo.getClass()
                            .getMethod(
                                    setterName,
                                    String.class
                            );

            method.invoke(
                    vo,
                    value
            );

            return;

        } catch (Exception ignore) {
        }

        try {

            Field field =
                    vo.getClass()
                            .getDeclaredField(
                                    fieldName
                            );

            field.setAccessible(
                    true
            );

            field.set(
                    vo,
                    value
            );

        } catch (Exception ignore) {
        }
    }

    private LabelStyles getDefaultLabelStyle() {

        return kakaoMap
                .getLabelManager()
                .addLabelStyles(
                        LabelStyles.from(
                                LabelStyle.from(
                                        R.drawable.loca_icon
                                ).setTextStyles(
                                        20,
                                        Color.BLACK
                                )
                        )
                );
    }

    private LabelStyles getClusterLabelStyle() {

        return kakaoMap
                .getLabelManager()
                .addLabelStyles(
                        LabelStyles.from(
                                LabelStyle.from(
                                        R.drawable.loca_icon
                                ).setTextStyles(
                                        32,
                                        Color.RED
                                )
                        )
                );
    }
}