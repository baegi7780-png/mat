package com.tech.motjip.API.KakaoMap;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;
import com.tech.motjip.API.KakaoMap.Utils.MapHelper;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.MapPostionVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 마커 클러스터링 매니저 (Grid-based)
// 줌 레벨에 따라 격자 크기를 조정하여 가까운 마커들을 묶어 표시합니다.
public class ClusterManager {

    private final KakaoMap kakaoMap;
    private final LabelStyles markerStyle;   // 일반 마커 스타일
    private final LabelStyles clusterStyle;  // 클러스터 마커 스타일
    private final List<KeywordMapVO> allMarkers = new ArrayList<>();

    public ClusterManager(KakaoMap kakaoMap, LabelStyles markerStyle, LabelStyles clusterStyle) {
        this.kakaoMap = kakaoMap;
        this.markerStyle = markerStyle;
        this.clusterStyle = clusterStyle;
    }

    // 새 마커 목록을 설정 (기존 마커는 모두 교체)
    public void setMarkers(List<KeywordMapVO> markers) {
        allMarkers.clear();
        if (markers != null) allMarkers.addAll(markers);
        redraw();
    }

    // 모든 마커를 제거
    public void clear() {
        allMarkers.clear();
        LabelLayer layer = getOrCreateLayer();
        layer.removeAll();
    }

    // 현재 줌 레벨에 따라 클러스터 다시 계산하고 그림
    public void redraw() {
        LabelLayer layer = getOrCreateLayer();
        layer.removeAll();

        if (allMarkers.isEmpty()) return;

        int zoom = kakaoMap.getZoomLevel();
        double gridSize = getGridSizeByZoom(zoom);

        if (gridSize <= 0) {
            // 줌인 충분 → 클러스터링 안 함, 모든 마커 그대로
            for (KeywordMapVO vo : allMarkers) {
                drawSingleMarker(layer, vo);
            }
            return;
        }

        // 격자 기반 그룹핑
        Map<Long, List<KeywordMapVO>> groups = new HashMap<>();
        for (KeywordMapVO vo : allMarkers) {
            LatLng pos = MapHelper.getLatLng(vo.getX(), vo.getY());
            long key = computeGridKey(pos, gridSize);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(vo);
        }

        // 그룹별로 마커 그림 (1개면 일반 마커, 2개 이상이면 클러스터)
        for (List<KeywordMapVO> group : groups.values()) {
            if (group.size() == 1) {
                drawSingleMarker(layer, group.get(0));
            } else {
                drawCluster(layer, group);
            }
        }
    }

    // 일반 마커 1개를 그림
    private void drawSingleMarker(LabelLayer layer, KeywordMapVO vo) {
        LatLng pos = MapHelper.getLatLng(vo.getX(), vo.getY());
        Label label = layer.addLabel(LabelOptions.from(pos).setStyles(markerStyle));
        label.setTag(vo);
        label.changeText(new LabelTextBuilder().setTexts(vo.getPlace_name()));
    }

    // 클러스터 마커 1개를 그림 (중심 = 그룹 마커들의 평균 좌표)
    private void drawCluster(LabelLayer layer, List<KeywordMapVO> group) {
        double avgLat = 0;
        double avgLng = 0;
        for (KeywordMapVO vo : group) {
            avgLat += Double.parseDouble(vo.getY());
            avgLng += Double.parseDouble(vo.getX());
        }
        avgLat /= group.size();
        avgLng /= group.size();

        LatLng center = MapHelper.getLatLng(new MapPostionVO(avgLat, avgLng));
        Label label = layer.addLabel(LabelOptions.from(center).setStyles(clusterStyle));
        label.setTag(group); // 클릭 시 클러스터 식별용
        label.changeText(new LabelTextBuilder().setTexts(String.valueOf(group.size())));
    }

    // 줌 레벨별 격자 크기 (위경도 단위, 0 이하면 클러스터링 안 함)
    private double getGridSizeByZoom(int zoom) {
        if (zoom >= 18) return 0;       // 확대 충분 → 클러스터링 안 함
        if (zoom >= 16) return 0.001;   // 약 100m
        if (zoom >= 14) return 0.005;   // 약 500m
        return 0.01;                    // 약 1km
    }

    // 좌표를 격자 키로 변환 (row, col 을 long 하나로 인코딩)
    private long computeGridKey(LatLng pos, double gridSize) {
        long row = (long) (pos.getLatitude() / gridSize);
        long col = (long) (pos.getLongitude() / gridSize);
        return (row << 32) | (col & 0xFFFFFFFFL);
    }

    // 기본 레이어를 가져옵니다 (텍스트 위치가 기본 레이어 기준으로 정상 렌더링됨)
    private LabelLayer getOrCreateLayer() {
        return kakaoMap.getLabelManager().getLayer();
    }
}
