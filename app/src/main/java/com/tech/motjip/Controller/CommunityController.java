package com.tech.motjip.Controller;

import android.content.Context;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.ResponseDto.CommunityPostPageResponse;
import com.tech.motjip.Model.CommunityMember;
import com.tech.motjip.Model.CommunityPost;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityController {

    public interface CommunityCallback {

        void onSuccess(
                List<CommunityPost> posts,
                boolean isLastPage
        );

        void onEmpty();

        void onError(String message);
    }

    public interface JoinCommunityCallback {

        void onSuccess();

        void onAlreadyJoined();

        void onError(String message);
    }

    public interface CancelJoinCallback {

        void onSuccess();

        void onError(String message);
    }

    public interface DeleteCommunityCallback {

        void onSuccess();

        void onError(String message);
    }

    public interface HideClosedCommunityCallback {

        void onSuccess();

        void onError(String message);
    }

    public interface CommunityMembersCallback {

        void onSuccess(
                List<CommunityMember> members
        );

        void onError(
                String message
        );
    }

    public interface KickMemberCallback {

        void onSuccess();

        void onError(String message);
    }

    private final ApiService apiService;

    private int currentPage = 0;

    private final int pageSize = 10;

    private boolean isLoading = false;

    private boolean isLastPage = false;

    public CommunityController(Context context) {

        apiService =
                RetrofitClient.getApiService(context);
    }

    public void resetPaging() {

        currentPage = 0;

        isLastPage = false;

        isLoading = false;
    }

    public void loadFirstPage(
            String title,
            String tag,
            String region,
            String sort,
            CommunityCallback callback
    ) {

        resetPaging();

        loadPosts(
                title,
                tag,
                region,
                sort,
                callback
        );
    }

    public void loadNextPage(
            String title,
            String tag,
            String region,
            String sort,
            CommunityCallback callback
    ) {

        if (isLoading || isLastPage) {
            return;
        }

        currentPage++;

        loadPosts(
                title,
                tag,
                region,
                sort,
                callback
        );
    }

    public void loadClosedFirstPage(
            String title,
            String tag,
            String region,
            String sort,
            CommunityCallback callback
    ) {

        resetPaging();

        loadClosedPosts(
                title,
                tag,
                region,
                sort,
                callback
        );
    }

    public void loadClosedNextPage(
            String title,
            String tag,
            String region,
            String sort,
            CommunityCallback callback
    ) {

        if (isLoading || isLastPage) {
            return;
        }

        currentPage++;

        loadClosedPosts(
                title,
                tag,
                region,
                sort,
                callback
        );
    }

    public void loadMyCommunities(
            String sort,
            CommunityCallback callback
    ) {

        resetPaging();

        loadMyPosts(
                sort,
                callback
        );
    }

    public void loadMyClosedCommunities(
            String sort,
            CommunityCallback callback
    ) {

        resetPaging();

        loadMyClosedPosts(
                sort,
                callback
        );
    }

    private void loadPosts(
            String title,
            String tag,
            String region,
            String sort,
            CommunityCallback callback
    ) {

        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

        apiService.getCommunityPosts(
                title,
                tag,
                region,
                sort,
                currentPage,
                pageSize
        ).enqueue(new Callback<CommunityPostPageResponse>() {

            @Override
            public void onResponse(
                    Call<CommunityPostPageResponse> call,
                    Response<CommunityPostPageResponse> response
            ) {

                isLoading = false;

                if (!response.isSuccessful()) {

                    callback.onError(
                            "게시글 조회 실패: "
                                    + response.code()
                    );

                    return;
                }

                CommunityPostPageResponse body =
                        response.body();

                if (body == null) {

                    callback.onError(
                            "게시글 응답 데이터가 없습니다."
                    );

                    return;
                }

                List<CommunityPost> posts =
                        body.getContent();

                isLastPage =
                        body.isLast();

                if (posts == null || posts.isEmpty()) {

                    if (currentPage == 0) {
                        callback.onEmpty();
                    }

                    return;
                }

                callback.onSuccess(
                        posts,
                        isLastPage
                );
            }

            @Override
            public void onFailure(
                    Call<CommunityPostPageResponse> call,
                    Throwable t
            ) {

                isLoading = false;

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    private void loadClosedPosts(
            String title,
            String tag,
            String region,
            String sort,
            CommunityCallback callback
    ) {

        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

        apiService.getClosedCommunityPosts(
                title,
                tag,
                region,
                sort,
                currentPage,
                pageSize
        ).enqueue(new Callback<CommunityPostPageResponse>() {

            @Override
            public void onResponse(
                    Call<CommunityPostPageResponse> call,
                    Response<CommunityPostPageResponse> response
            ) {

                isLoading = false;

                if (!response.isSuccessful()) {

                    callback.onError(
                            "종료된 모임 조회 실패: "
                                    + response.code()
                    );

                    return;
                }

                CommunityPostPageResponse body =
                        response.body();

                if (body == null) {

                    callback.onError(
                            "종료된 모임 응답 데이터가 없습니다."
                    );

                    return;
                }

                List<CommunityPost> posts =
                        body.getContent();

                isLastPage =
                        body.isLast();

                if (posts == null || posts.isEmpty()) {

                    if (currentPage == 0) {
                        callback.onEmpty();
                    }

                    return;
                }

                callback.onSuccess(
                        posts,
                        isLastPage
                );
            }

            @Override
            public void onFailure(
                    Call<CommunityPostPageResponse> call,
                    Throwable t
            ) {

                isLoading = false;

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    private void loadMyPosts(
            String sort,
            CommunityCallback callback
    ) {

        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

        apiService.getMyCommunityPosts(
                sort,
                currentPage,
                pageSize
        ).enqueue(new Callback<CommunityPostPageResponse>() {

            @Override
            public void onResponse(
                    Call<CommunityPostPageResponse> call,
                    Response<CommunityPostPageResponse> response
            ) {

                isLoading = false;

                if (!response.isSuccessful()) {

                    callback.onError(
                            "내 모임 조회 실패: "
                                    + response.code()
                    );

                    return;
                }

                CommunityPostPageResponse body =
                        response.body();

                if (body == null) {

                    callback.onError(
                            "내 모임 응답 데이터가 없습니다."
                    );

                    return;
                }

                List<CommunityPost> posts =
                        body.getContent();

                isLastPage =
                        body.isLast();

                if (posts == null || posts.isEmpty()) {

                    if (currentPage == 0) {
                        callback.onEmpty();
                    }

                    return;
                }

                callback.onSuccess(
                        posts,
                        isLastPage
                );
            }

            @Override
            public void onFailure(
                    Call<CommunityPostPageResponse> call,
                    Throwable t
            ) {

                isLoading = false;

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    private void loadMyClosedPosts(
            String sort,
            CommunityCallback callback
    ) {

        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;

        apiService.getMyClosedCommunityPosts(
                sort,
                currentPage,
                pageSize
        ).enqueue(new Callback<CommunityPostPageResponse>() {

            @Override
            public void onResponse(
                    Call<CommunityPostPageResponse> call,
                    Response<CommunityPostPageResponse> response
            ) {

                isLoading = false;

                if (!response.isSuccessful()) {

                    callback.onError(
                            "내 종료 모임 조회 실패: "
                                    + response.code()
                    );

                    return;
                }

                CommunityPostPageResponse body =
                        response.body();

                if (body == null) {

                    callback.onError(
                            "내 종료 모임 응답 데이터가 없습니다."
                    );

                    return;
                }

                List<CommunityPost> posts =
                        body.getContent();

                isLastPage =
                        body.isLast();

                if (posts == null || posts.isEmpty()) {

                    if (currentPage == 0) {
                        callback.onEmpty();
                    }

                    return;
                }

                callback.onSuccess(
                        posts,
                        isLastPage
                );
            }

            @Override
            public void onFailure(
                    Call<CommunityPostPageResponse> call,
                    Throwable t
            ) {

                isLoading = false;

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public void joinCommunity(
            Long comId,
            JoinCommunityCallback callback
    ) {

        if (comId == null) {

            callback.onError(
                    "게시글 정보가 올바르지 않습니다."
            );

            return;
        }

        apiService.joinCommunity(
                comId
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    callback.onSuccess();

                    return;
                }

                String errorMessage =
                        "모임 참여 실패: "
                                + response.code();

                try {

                    if (response.errorBody() != null) {

                        errorMessage =
                                response.errorBody().string();
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }

                callback.onError(
                        errorMessage
                );
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public void cancelJoinCommunity(
            Long comId,
            CancelJoinCallback callback
    ) {

        if (comId == null) {

            callback.onError(
                    "게시글 정보가 올바르지 않습니다."
            );

            return;
        }

        apiService.cancelJoinCommunity(
                comId
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    callback.onSuccess();

                    return;
                }

                callback.onError(
                        "모임 참여 취소 실패: "
                                + response.code()
                );
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public void hideMyClosedCommunity(
            Long comId,
            HideClosedCommunityCallback callback
    ) {

        if (comId == null) {

            callback.onError(
                    "게시글 정보가 올바르지 않습니다."
            );

            return;
        }

        apiService.hideMyClosedCommunity(
                comId
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    callback.onSuccess();

                    return;
                }

                callback.onError(
                        "종료 모임 숨기기 실패: "
                                + response.code()
                );
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public void deleteCommunityPost(
            Long comId,
            DeleteCommunityCallback callback
    ) {

        if (comId == null) {

            callback.onError(
                    "게시글 정보가 올바르지 않습니다."
            );

            return;
        }

        apiService.deleteCommunityPost(
                comId
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    callback.onSuccess();

                    return;
                }

                callback.onError(
                        "게시글 삭제 실패: "
                                + response.code()
                );
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public void getCommunityMembers(
            Long comId,
            CommunityMembersCallback callback
    ) {

        if (comId == null) {

            callback.onError(
                    "게시글 정보가 올바르지 않습니다."
            );

            return;
        }

        apiService.getCommunityMembers(
                comId
        ).enqueue(new Callback<List<CommunityMember>>() {

            @Override
            public void onResponse(
                    Call<List<CommunityMember>> call,
                    Response<List<CommunityMember>> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onSuccess(
                            response.body()
                    );

                    return;
                }

                String errorMessage =
                        "참여자 목록 조회 실패: "
                                + response.code();

                try {

                    if (response.errorBody() != null) {

                        errorMessage =
                                response.errorBody().string();
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }

                callback.onError(
                        errorMessage
                );
            }

            @Override
            public void onFailure(
                    Call<List<CommunityMember>> call,
                    Throwable t
            ) {

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public void kickCommunityMember(
            Long comId,
            Long targetMemberId,
            KickMemberCallback callback
    ) {

        if (comId == null || targetMemberId == null) {

            callback.onError(
                    "참여자 정보가 올바르지 않습니다."
            );

            return;
        }

        apiService.kickCommunityMember(
                comId,
                targetMemberId
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    callback.onSuccess();

                    return;
                }

                callback.onError(
                        "참여자 추방 실패: "
                                + response.code()
                );
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {

                callback.onError(
                        "서버 연결 실패: "
                                + t.getMessage()
                );
            }
        });
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isLastPage() {
        return isLastPage;
    }
}