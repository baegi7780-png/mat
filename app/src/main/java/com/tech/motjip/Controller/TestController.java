package com.tech.motjip.Controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.label.Label;
import com.tech.motjip.R;
import com.tech.motjip.API.HttpHelper.GetJsonAsync;
import com.tech.motjip.API.KakaoMap.CallbackInterface.IViewDetailItemClickCallback;
import com.tech.motjip.API.KakaoMap.CallbackInterface.IMapStartCallback;
import com.tech.motjip.API.KakaoMap.KakaoMapHandler;
import com.tech.motjip.API.KakaoMap.KakaoMapStarter;
import com.tech.motjip.API.KakaoMap.Utils.MapHelper;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.MapPostionVO;
import com.tech.motjip.Thread.IThreadCallback;
import com.tech.motjip.Thread.IThreadReturn1Callback;

import java.util.List;

public class TestController implements IMapStartCallback {

    private final Activity testActivity;

    private KakaoMapStarter mapStarter;

    private KakaoMapHandler mapHandler;

    // 비동기 멀티 쓰레드 작업이 완료되었음을 알립니다.
    IThreadCallback callback;

    // GPS 실패 시 기본 좌표 (목동)
    private static final LatLng DEFAULT_POSITION = LatLng.from(37.53660174890449, 126.8819899200535);

    // 현재 사용자 위치 (검색 기준 좌표)
    private LatLng currentPosition;

    // 현재 사용자 위치 마커
    private Label myLocationLabel;

    public TestController(Activity activity) {
        this.testActivity = activity;
    }

    // 콜백함수를 할당하고 맵뷰에 맵을 그립니다.
    public void mapStart(MapView mapView, IThreadCallback callback) {
        this.mapStarter = new KakaoMapStarter(mapView, this, testActivity);
        this.callback = callback;
        this.currentPosition = DEFAULT_POSITION;
        mapStarter.start();
    }

    // 콜백함수를 할당하고 맵뷰에 맵을 그리고 맵의 위치를 좌표에 따라 이동시킵니다.
    public void mapStart(MapView mapView, IThreadCallback callback, LatLng startPosition) {
        this.mapStarter = new KakaoMapStarter(mapView, this, testActivity);
        this.callback = callback;
        this.currentPosition = startPosition;
        mapStarter.start(startPosition);
    }

    // 현재 위치를 갱신하고 지도에 표시합니다.
    public void updateCurrentLocation(LatLng position) {

        if (position == null) {
            return;
        }

        currentPosition = position;

        if (mapHandler != null) {
            moveMapCamara(position);
            drawMyLocationMarker();
        }
    }

    // 권한이 확보된 상태에서 GPS 위치를 비동기로 가져와 콜백으로 반환합니다.
    // GPS 캐시 없으면 신호 기다리고, 5초 내 안 오면 onError 호출 후 GPS 요청 취소
    @SuppressLint("MissingPermission")
    public void fetchLocation(LocationManager locationManager, IThreadReturn1Callback<LatLng> callback) {

        Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (last != null) {

            if (!MapHelper.isInKorea(last.getLatitude(), last.getLongitude())) {
                callback.onError(new Exception("한국 범위 외 좌표"));
                return;
            }

            LatLng position =
                    MapHelper.getLatLng(
                            new MapPostionVO(
                                    last.getLatitude(),
                                    last.getLongitude()
                            )
                    );

            currentPosition = position;

            callback.ThreadEnds(position);
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());

        android.location.LocationListener[] listenerRef = {null};

        Runnable[] timeoutRef = {null};

        listenerRef[0] = location -> {

            handler.removeCallbacks(timeoutRef[0]);

            locationManager.removeUpdates(listenerRef[0]);

            if (!MapHelper.isInKorea(location.getLatitude(), location.getLongitude())) {
                callback.onError(new Exception("한국 범위 외 좌표"));
                return;
            }

            LatLng position =
                    MapHelper.getLatLng(
                            new MapPostionVO(
                                    location.getLatitude(),
                                    location.getLongitude()
                            )
                    );

            currentPosition = position;

            callback.ThreadEnds(position);
        };

        timeoutRef[0] = () -> {

            locationManager.removeUpdates(listenerRef[0]);

            callback.onError(new Exception("GPS 타임아웃"));
        };

        locationManager.requestSingleUpdate(
                LocationManager.GPS_PROVIDER,
                listenerRef[0],
                Looper.getMainLooper()
        );

        // 여기서 대기시간 설정
        handler.postDelayed(timeoutRef[0], 5000);
    }

    public void showAppKey() {
        Log.d("앱키 : ", mapStarter.getAppKey());
    }

    public void showAndroidHashKey() {
        Log.d("해시키 : ", mapStarter.getHashKey());
    }

    // 키워드에 따른 데이터를 찾습니다.
    public void searchMapData(String keyword, IThreadReturn1Callback<List<KeywordMapVO>> callback) {

        // GetJsonAsync.GetMapSearchDataAsync(keyword,callback);  // 좌표 없는 전체 검색 (보류)
        LatLng pos = currentPosition != null ? currentPosition : DEFAULT_POSITION;

        GetJsonAsync.GetMapSearchDataWithConditions(
                keyword,
                new MapPostionVO(pos.getLatitude(), pos.getLongitude()),
                "10000",
                callback
        );
    }

    // 마커를 그립니다(테스트용)
    public void drawMarker() {
        mapHandler.setMarker(
                MapHelper.getLatLng(
                        new MapPostionVO(
                                37.53660174890449,
                                126.8819899200535
                        )
                ),
                "테스트 입니다"
        );
    }

    // 현재 내 위치를 사람 아이콘으로 표시합니다.
    public void drawMyLocationMarker() {

        // 맵이 아직 준비되지 않았거나 현재 위치가 없다면 종료
        if (mapHandler == null || currentPosition == null) {
            return;
        }

        try {

            // 기존 현재 위치 마커가 있다면 제거합니다.
            if (myLocationLabel != null) {
                myLocationLabel.remove();
                myLocationLabel = null;
            }

            // 검색 마커와 같은 방식으로 현재 위치 마커를 생성합니다.
            myLocationLabel =
                    mapHandler.setMyLocationMarker(
                            currentPosition
                    );

            Log.d(
                    "HOME_LOCATION",
                    "현재 위치 사람 마커 생성 완료 : "
                            + currentPosition.getLatitude()
                            + ", "
                            + currentPosition.getLongitude()
            );

        } catch (Exception e) {

            Log.e(
                    "HOME_LOCATION",
                    "현재 위치 마커 생성 실패",
                    e
            );
        }
    }

    // 검색 결과값에 따른 마커를 그립니다 (클러스터링 적용).
    public void drawMarker(List<KeywordMapVO> vo) {

        // 마커 목록 통째로 전달 — 클러스터링은 ClusterManager에서 자동 처리
        mapHandler.setMarkers(vo);
    }

    // 마커 클릭 이벤트 리스너를 등록합니다 (리스트 클릭과 동일하게 카메라 이동까지 처리).
    public void setMarkerClickListener(IViewDetailItemClickCallback callback) {

        if (mapHandler != null) {

            mapHandler.setMarkerClickListener(vo -> {

                callback.onItemClick(vo);

                focusOnPlace(vo);
            });
        }
    }

    // 타겟 포지션으로 카메라를 이동시킵니다.
    public void moveMapCamara(LatLng targetPosition) {

        mapHandler.moveCamera(targetPosition);
    }

    // 맵의 마커를 초기화합니다.
    public void clearMarkers() {

        mapHandler.clearMarkers();

        drawMyLocationMarker();
    }

    // 검색 결과 전체를 지도에 표시합니다.
    public void showSearchResults(List<KeywordMapVO> result) {

        clearMarkers();

        drawMarker(result);

        moveMapCamara(
                MapHelper.getLatLng(
                        result.get(0).getX(),
                        result.get(0).getY()
                )
        );
    }

    // 선택한 장소로 카메라를 이동합니다.
    public void focusOnPlace(KeywordMapVO vo) {

        moveMapCamara(
                MapHelper.getLatLng(
                        vo.getX(),
                        vo.getY()
                )
        );
    }

    // 검색 결과를 지도와 바텀시트에 그리는 공통 콜백을 생성합니다.
    public IThreadReturn1Callback<List<KeywordMapVO>> createSearchCallback(
            LinearLayout resultContainer,
            LinearLayout suggestionContainer,
            View overlaySuggestion,
            LayoutInflater inflater,
            IViewDetailItemClickCallback onItemClick
    ) {

        return new IThreadReturn1Callback<List<KeywordMapVO>>() {

            @Override
            public void ThreadEnds(List<KeywordMapVO> result) {

                if (result.isEmpty()) {

                    Log.d("TestLog", "결과없음");

                    return;
                }

                new Handler(Looper.getMainLooper()).post(() -> {

                    // 검색쪽 뷰 꺼주고
                    overlaySuggestion.setVisibility(View.GONE);

                    // 검색 안에 데이터 날려주고
                    suggestionContainer.removeAllViews();

                    // 바텀시트 데이터 한번지워줌
                    resultContainer.removeAllViews();

                    // 그뒤에 그림 다시그림
                    for (KeywordMapVO vo : result) {

                        View itemView =
                                inflater.inflate(
                                        R.layout.item_search_result,
                                        resultContainer,
                                        false
                                );

                        ((TextView) itemView.findViewById(R.id.tv_category))
                                .setText(vo.getCategory_name());

                        ((TextView) itemView.findViewById(R.id.tv_place_name))
                                .setText(vo.getPlace_name());

                        ((TextView) itemView.findViewById(R.id.tv_address))
                                .setText(vo.getRoad_address_name());

                        // 리스트에서 클릭이벤트 발생시 상세페이지 실행
                        itemView.setOnClickListener(v -> {

                            // 클릭 이벤트를 콜백으로 위임 (UI 처리는 HomeFragment 책임)
                            // 상세페이지를 실행함
                            onItemClick.onItemClick(vo);

                            focusOnPlace(vo);
                        });

                        resultContainer.addView(itemView);
                    }

                    showSearchResults(result);
                });
            }

            @Override
            public void onError(Exception e) {

                Log.e("MapDebug", "원인: ", e);
            }
        };
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onReady(KakaoMap kakaoMap) {

        this.mapHandler =
                new KakaoMapHandler(
                        kakaoMap,
                        testActivity
                );

        Log.e(
                "KakaoMap",
                "맵 로드 성공"
        );

        // 현재 위치 사람 마커를 생성합니다.
        drawMyLocationMarker();

        //drawMarker();

        callback.ThreadEnds();
    }
}