package com.tech.motjip.API.HttpHelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tech.motjip.API.CustomException.KakaoRestException;
import com.tech.motjip.Model.KakaoRestVO;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.MapPostionVO;
import com.tech.motjip.Model.PlaceDetailModel.PlaceDetailVO;
import com.tech.motjip.Utils.DetailJsonParse;
import com.tech.motjip.Utils.JsonHelper;
import com.tech.motjip.Utils.UrlChecker;

import org.jsoup.Connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Http통신을 통해 특정 Json을 가져옵니다
public class GetJson {

    // RestAPI 요청 주소를 담고있는 데이터 모델입니다.
    private final static KakaoRestVO vo = new KakaoRestVO();

    // @오버로드 검색어에 해당하는 맵 정보 데이터를 반환합니다.
    public static List<KeywordMapVO> GetMapSearchData(String query) throws KakaoRestException{
        Map<String, String> hashQuery = new HashMap<String, String>();
        hashQuery.put("query", query);
        hashQuery.put("category_group_code", "FD6"); // 음식점만 검색가능 코드

        return GetMapSearchData(hashQuery);
    }

    // @오버로드, 검색어, 경도, 위도, 범위에 해당하는 음식점 맵 정보 데이터를 반환합니다.
    public static List<KeywordMapVO> GetMapSearchDataWithConditions(String query, String x, String y, String radius) throws KakaoRestException{
        Map<String, String> hashQuery = new HashMap<String, String>();
        hashQuery.put("query", query);
        //음식점 Only
        hashQuery.put("category_group_code", "FD6");
        // 경도
        hashQuery.put("x", x);
        // 위도
        hashQuery.put("y", y);
        // 좌표값을 기준으로 해당하는 주변 범위 Ex 2000 = 2km
        hashQuery.put("radius", radius);
        // 가까운 순으로 정렬
        hashQuery.put("sort", "distance");

        return GetMapSearchData(hashQuery);
    }

    // @오버로드, 검색어, 경도, 위도, 범위에 해당하는 음식점 맵 정보 데이터를 반환합니다.
    public static List<KeywordMapVO> GetMapSearchDataWithConditions(String query, MapPostionVO position, String radius) throws KakaoRestException{
        return GetMapSearchDataWithConditions(query, Double.toString(position.getLngX()), Double.toString(position.getLatY()), radius);
    }

    // 쿼리에 해당하는 맵 정보 데이터를 반환합니다.
    private static List<KeywordMapVO> GetMapSearchData(Map<String, String> data) throws KakaoRestException{
        try {
            Connection.Response result = DoConnect.get(vo.getMapSearch(), KakaoRestHeader.getHeader(), data);
            if (result.statusCode() != 200) {
                // 통신은 성공헀으나 오류가 발생했다면 예외를 발생시킵니다.
                throw new KakaoRestException(result);
            }
            // 검색결과 배열 데이터를 반환합니다.
            return JsonHelper.getJsonFromString(result.body(), "documents", KeywordMapVO[].class);
        } catch (Exception e) {
            // 자체 통신에서 오류가 발생했다면 예외를 발생시킵니다.
            throw new KakaoRestException(e);
        }
    }

    // place_url에 해당하는 장소 상세 데이터를 반환합니다.
    public static PlaceDetailVO GetPlaceDetail(String placeUrl) throws KakaoRestException{
        // URL 형식 검증 (실수방지)
        if (!UrlChecker.isPlaceUrl(placeUrl)) {
            throw new KakaoRestException("잘못된 place_url 형식: " + placeUrl, new IllegalArgumentException());
        }
        try {
            // URL에서 placeId 추출 (마지막 / 뒷부분의 숫자)
            String afterSlash = placeUrl.substring(placeUrl.lastIndexOf('/') + 1);
            // 쿼리스트링 등이 붙어있으면 제거 (?나 # 앞까지만)
            String placeId = afterSlash.split("[?#]")[0];

            Connection.Response result = DoConnect.get(vo.getPlaceDetail() + placeId, KakaoRestHeader.getPlaceApiHeader());
            if (result.statusCode() != 200) {
                // 통신은 성공헀으나 오류가 발생했다면 예외를 발생시킵니다.
                throw new KakaoRestException(result);
            }
            // 응답 JSON을 DetailJsonParse로 단순화된 PlaceDetailVO로 변환합니다.
            JsonObject json = JsonHelper.getJsonFromString(result.body());
            return new DetailJsonParse().parse(json);
        } catch (KakaoRestException e) {
            // 위에서 던진 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            // 자체 통신에서 오류가 발생했다면 예외를 발생시킵니다.
            throw new KakaoRestException(e);
        }
    }
}

