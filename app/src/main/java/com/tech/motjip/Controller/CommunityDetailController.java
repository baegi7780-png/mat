package com.tech.motjip.Controller;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityDetailController {

    private final Context context;

    private final CommunityController communityController;
    private final FavoriteController favoriteController;

    public CommunityDetailController(Context context) {

        this.context = context;

        this.communityController =
                new CommunityController(context);

        this.favoriteController =
                new FavoriteController(context);
    }

    public void loadPostImage(
            ImageView imageView,
            String imageUrl
    ) {

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {

            Glide.with(context)
                    .load(createImageGlideUrl(imageUrl))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .error(R.drawable.chef_illustration)
                    .into(imageView);

        } else {

            Glide.with(context)
                    .load(R.drawable.chef_illustration)
                    .into(imageView);
        }
    }

    private GlideUrl createImageGlideUrl(
            String imageUrl
    ) {

        String baseUrl =
                RetrofitClient.BASE_URL
                        .replace("/api/", "/")
                        .replace("/api", "");

        if (baseUrl.endsWith("/")) {

            baseUrl =
                    baseUrl.substring(
                            0,
                            baseUrl.length() - 1
                    );
        }

        if (!imageUrl.startsWith("/")) {

            imageUrl =
                    "/" + imageUrl;
        }

        String fullImageUrl =
                baseUrl + imageUrl;

        return new GlideUrl(
                fullImageUrl,
                new LazyHeaders.Builder()
                        .addHeader(
                                "ngrok-skip-browser-warning",
                                "true"
                        )
                        .build()
        );
    }

    public String formatMeetingAt(
            String meetingAt
    ) {

        if (meetingAt == null) {
            return "";
        }

        if (meetingAt.contains("T")) {

            return meetingAt.replaceFirst(
                    "T",
                    "\n"
            );
        }

        return meetingAt.replaceFirst(
                " ",
                "\n"
        );
    }

    public boolean isMeetingClosed(
            String meetingAt
    ) {

        try {

            if (meetingAt == null
                    || meetingAt.trim().isEmpty()) {

                return false;
            }

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm"
                    );

            LocalDateTime meetingDateTime =
                    LocalDateTime.parse(
                            meetingAt,
                            formatter
                    );

            return meetingDateTime.isBefore(
                    LocalDateTime.now()
            );

        } catch (Exception e) {

            return false;
        }
    }

    public int getFavoriteColor(
            boolean isFavorite
    ) {

        if (isFavorite) {
            return Color.parseColor("#FF9800");
        }

        return Color.parseColor("#BBBBBB");
    }

    public void toggleFavorite(
            Long comId,
            FavoriteCallback callback
    ) {

        favoriteController.toggleFavoriteCommunityPost(
                comId,
                new Callback<Boolean>() {

                    @Override
                    public void onResponse(
                            Call<Boolean> call,
                            Response<Boolean> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "즐겨찾기 처리 실패"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Boolean> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                }
        );
    }

    public void joinCommunity(
            Long comId,
            JoinCallback callback
    ) {

        communityController.joinCommunity(
                comId,
                new CommunityController.JoinCommunityCallback() {

                    @Override
                    public void onSuccess() {

                        callback.onSuccess();
                    }

                    @Override
                    public void onAlreadyJoined() {

                        callback.onAlreadyJoined();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(message);
                    }
                }
        );
    }

    public void cancelJoinCommunity(
            Long comId,
            BasicCallback callback
    ) {

        communityController.cancelJoinCommunity(
                comId,
                new CommunityController.CancelJoinCallback() {

                    @Override
                    public void onSuccess() {

                        callback.onSuccess();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(message);
                    }
                }
        );
    }

    public void hideClosedCommunity(
            Long comId,
            BasicCallback callback
    ) {

        communityController.hideMyClosedCommunity(
                comId,
                new CommunityController.HideClosedCommunityCallback() {

                    @Override
                    public void onSuccess() {

                        callback.onSuccess();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(message);
                    }
                }
        );
    }

    public void deleteCommunity(
            Long comId,
            BasicCallback callback
    ) {

        communityController.deleteCommunityPost(
                comId,
                new CommunityController.DeleteCommunityCallback() {

                    @Override
                    public void onSuccess() {

                        callback.onSuccess();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(message);
                    }
                }
        );
    }

    public interface FavoriteCallback {

        void onSuccess(
                boolean isFavorite
        );

        void onError(
                String message
        );
    }

    public interface JoinCallback {

        void onSuccess();

        void onAlreadyJoined();

        void onError(
                String message
        );
    }

    public interface BasicCallback {

        void onSuccess();

        void onError(
                String message
        );
    }
}