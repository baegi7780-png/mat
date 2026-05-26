package com.tech.motjip.API.KakaoMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

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
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.R;

import java.util.List;

import javax.annotation.Nonnull;

import lombok.NonNull;

public class KakaoMapHandler {

    private final KakaoMap kakaoMap;

    private final Context context;

    private LabelStyles labelStyle;

    private LabelStyles clusterStyle;

    private final ClusterManager clusterManager;

    private int lastZoomLevel = -1;  // 줌 변경 감지용

    public KakaoMapHandler(KakaoMap kakaomap, Context context) {

        this.kakaoMap = kakaomap;
        this.context = context;
        this.labelStyle = getDefaultLabelStyle();
        this.clusterStyle = getClusterLabelStyle();
        this.clusterManager = new ClusterManager(kakaoMap, labelStyle, clusterStyle);

        setupCameraListener();
    }

    // 카카오맵 객체를 반환합니다.
    public KakaoMap getKakaoMap() {

        return kakaoMap;
    }

    // 카메라를 이동시킵니다.
    // https://apis.map.kakao.com/android_v2/docs/getting-started/precautions/#2-api-의-비동기-처리
    public void moveCamera(@NonNull LatLng position) {

        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(position));
    }

    // 맵에 마커를 찍습니다. (테스트용 - vo 없는 버전)
    // https://apis.map.kakao.com/android_v2/docs/api-guide/label/label/#1-label-생성하기
    public void setMarker(@Nonnull LatLng position, String labelText) {

        setMarker(position, labelText, null);
    }

    // @오버로드 맵에 마커를 찍고 KeywordMapVO를 라벨에 tag로 보관합니다 (클릭 추적용).
    public void setMarker(@Nonnull LatLng position, String labelText, KeywordMapVO vo) {

        // 클러스터 구현 필요
        LabelLayer layer = kakaoMap.getLabelManager().getLayer("MyClusterLayer");

        if (layer == null) {

            LabelLayerOptions layerOptions =
                    LabelLayerOptions.from("MyClusterLayer")
                            .setCompetitionType(CompetitionType.SameLower) // 같은 레이어 안의 마커끼리 경쟁
                            .setCompetitionUnit(CompetitionUnit.IconAndText) // 아이콘이나 글씨가 겹치면 숨김
                            .setZOrder(10000); // 기본 레이어보다 위에 그려지도록 설정

            layer = kakaoMap.getLabelManager().addLayer(layerOptions);
        }

        LabelOptions options =
                LabelOptions.from(position)
                        .setStyles(labelStyle);

        Label label = layer.addLabel(options);

        // 클릭 시 어떤 가게인지 알 수 있도록 vo를 tag로 보관
        if (vo != null) {

            label.setTag(vo);
        }

        // https://apis.map.kakao.com/android_v2/docs/api-guide/label/label/#스타일-및-텍스트-변경
        // 이부분 api문서에없음...LabelTextBuilder 클래스에 대한 확인 필요
        label.changeText(new LabelTextBuilder().setTexts(labelText));

        // 주변 마커들 숨김 마커가 주변의 지도 그림위에 올라가있음 마커 주변으로 폴리곤을 그려서 지도를 덮어쓰든지 아예 다없애든지...
        // kakaoMap.setPoiVisible(false);
    }

    // 현재 위치 마커를 찍습니다.
    public Label setMyLocationMarker(@Nonnull LatLng position) {

        LabelLayer layer = kakaoMap.getLabelManager().getLayer("MyLocationLayer");

        if (layer == null) {

            LabelLayerOptions layerOptions =
                    LabelLayerOptions.from("MyLocationLayer")
                            .setCompetitionType(CompetitionType.None) // 현재 위치 마커는 다른 마커와 경쟁하지 않음
                            .setZOrder(20000); // 검색 마커보다 위에 그려지도록 설정

            layer = kakaoMap.getLabelManager().addLayer(layerOptions);
        }

        // 현재 위치 마커 이미지를 실제 64x64 크기로 줄입니다.
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
                kakaoMap.getLabelManager()
                        .addLabelStyles(
                                LabelStyles.from(
                                        LabelStyle.from(
                                                resizedBitmap
                                        )
                                )
                        );

        LabelOptions options =
                LabelOptions.from(position)
                        .setStyles(myLocationStyle);

        return layer.addLabel(options);
    }

    // 검색 결과 마커 목록을 통째로 설정합니다 (클러스터링 적용).
    public void setMarkers(List<KeywordMapVO> markers) {

        clusterManager.setMarkers(markers);

        lastZoomLevel = kakaoMap.getZoomLevel();
    }

    // 마커 클릭 이벤트 리스너를 등록합니다.
    public void setMarkerClickListener(IViewDetailItemClickCallback callback) {

        kakaoMap.setOnLabelClickListener(new KakaoMap.OnLabelClickListener() {

            @Override
            public boolean onLabelClicked(KakaoMap kakaoMap, LabelLayer labelLayer, Label label) {

                Object tag = label.getTag();

                if (tag instanceof KeywordMapVO && callback != null) {

                    // 일반 마커 클릭 — 상세페이지 열기
                    callback.onItemClick((KeywordMapVO) tag);

                } else if (tag instanceof List) {

                    // 클러스터 마커 클릭 — 줌 인 (자동으로 카메라 이벤트 발생 → 재계산)
                    zoomInOnCluster();
                }

                return true;
            }
        });
    }

    // 지도 위의 모든 마커를 초기화합니다.
    public void clearMarkers() {

        clusterManager.clear();
    }

    // 줌 변경될 때만 클러스터 재계산 (단순 이동은 무시)
    private void setupCameraListener() {

        kakaoMap.setOnCameraMoveEndListener((map, cameraPosition, gestureType) -> {

            int currentZoom =
                    kakaoMap.getZoomLevel();

            if (currentZoom != lastZoomLevel) {

                lastZoomLevel =
                        currentZoom;

                clusterManager.redraw();
            }
        });
    }

    // 클러스터 마커 클릭 시 두 단계 줌 인 → 카메라 이벤트 트리거되어 자동 재계산됨
    private void zoomInOnCluster() {

        int currentZoom =
                kakaoMap.getZoomLevel();

        kakaoMap.moveCamera(
                CameraUpdateFactory.zoomTo(
                        currentZoom + 2
                )
        );
    }

    // 기본 마커 스타일을 가져옵니다.
    private LabelStyles getDefaultLabelStyle() {

        // 마커 이미지 할당 및 텍스트 크기 및 글자 색 적용 이미지 크기 나중에 조정필요
        return kakaoMap.getLabelManager()
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

    // 클러스터 마커 스타일 (큰 빨간 글자로 일반 마커와 구분)
    private LabelStyles getClusterLabelStyle() {

        return kakaoMap.getLabelManager()
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