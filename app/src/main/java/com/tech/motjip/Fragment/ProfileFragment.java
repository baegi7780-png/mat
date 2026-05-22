package com.tech.motjip.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.RequestDto.LogoutRequestDto;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.FavoriteActivity;
import com.tech.motjip.FriendActivity;
import com.tech.motjip.MainActivity;
import com.tech.motjip.MyCommunityManageActivity;
import com.tech.motjip.MyInfoActivity;
import com.tech.motjip.NotificationActivity;
import com.tech.motjip.Handler.PreferenceManager;
import com.tech.motjip.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvEmail;
    private TextView tvNickname;

    private ImageView ivProfileImage;

    private Button btnLogout;

    private LinearLayout btnFavorite;
    private LinearLayout btnFriend;
    private LinearLayout btnMyCommunity;
    private LinearLayout btnNotification;

    private ImageButton btnSettings;

    private Call<LoginResponseDto> currentUserCall;

    private boolean isLoadingMyInfo = false;

    private static final String PREF_NAME = "AppPrefs";

    private static final String ACCESS_TOKEN =
            "ACCESS_TOKEN";

    private static final String REFRESH_TOKEN =
            "REFRESH_TOKEN";

    private static final String LOGIN_STATUS =
            "LOGIN_STATUS";

    private static final String CACHED_EMAIL =
            "CACHED_EMAIL";

    private static final String CACHED_NICKNAME =
            "CACHED_NICKNAME";

    private static final String CACHED_PROFILE_IMG_URL =
            "CACHED_PROFILE_IMG_URL";

    private static final String BASE_IMAGE_URL =
            "https://spout-distant-cost.ngrok-free.dev";

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(view, savedInstanceState);

        tvEmail =
                view.findViewById(R.id.tvEmail);

        tvNickname =
                view.findViewById(R.id.tvNickname);

        ivProfileImage =
                view.findViewById(R.id.ivProfileImage);

        btnLogout =
                view.findViewById(R.id.btnLogout);

        btnFavorite =
                view.findViewById(R.id.btnFavorite);

        btnFriend =
                view.findViewById(R.id.btnFriend);

        btnMyCommunity =
                view.findViewById(R.id.btnMyCommunity);

        btnNotification =
                view.findViewById(R.id.btnNotification);

        btnSettings =
                view.findViewById(R.id.btnSettings);

        loadCachedMyInfo();

        btnLogout.setOnClickListener(v -> logout());

        btnSettings.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            MyInfoActivity.class
                    );

            startActivity(intent);
        });

        btnFavorite.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            FavoriteActivity.class
                    );

            startActivity(intent);
        });

        btnFriend.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            FriendActivity.class
                    );

            startActivity(intent);
        });

        btnMyCommunity.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            MyCommunityManageActivity.class
                    );

            startActivity(intent);
        });

        btnNotification.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            NotificationActivity.class
                    );

            startActivity(intent);
        });
    }

    @Override
    public void onResume() {

        super.onResume();

        loadMyInfo();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (currentUserCall != null) {

            currentUserCall.cancel();

            currentUserCall =
                    null;
        }

        isLoadingMyInfo =
                false;
    }

    private void loadCachedMyInfo() {

        if (!isAdded()
                || getContext() == null) {

            return;
        }

        SharedPreferences preferences =
                requireActivity().getSharedPreferences(
                        PREF_NAME,
                        requireActivity().MODE_PRIVATE
                );

        String cachedEmail =
                preferences.getString(
                        CACHED_EMAIL,
                        null
                );

        String cachedNickname =
                preferences.getString(
                        CACHED_NICKNAME,
                        null
                );

        String cachedProfileImgUrl =
                preferences.getString(
                        CACHED_PROFILE_IMG_URL,
                        null
                );

        tvEmail.setText(
                cachedEmail != null
                        && !cachedEmail.isEmpty()
                        ? cachedEmail
                        : "조회된 계정"
        );

        tvNickname.setText(
                cachedNickname != null
                        && !cachedNickname.isEmpty()
                        ? cachedNickname
                        : "미설정"
        );

        loadProfileImage(
                cachedProfileImgUrl
        );
    }

    private void loadMyInfo() {

        if (isLoadingMyInfo) {

            return;
        }

        isLoadingMyInfo =
                true;

        currentUserCall =
                RetrofitClient.getApiService(requireContext())
                        .getCurrentUser();

        currentUserCall.enqueue(new Callback<LoginResponseDto>() {

            @Override
            public void onResponse(
                    Call<LoginResponseDto> call,
                    Response<LoginResponseDto> response
            ) {

                isLoadingMyInfo =
                        false;

                if (!isAdded()
                        || getContext() == null
                        || getView() == null) {

                    return;
                }

                if (response.isSuccessful()
                        && response.body() != null) {

                    LoginResponseDto user =
                            response.body();

                    String email =
                            user.getEmail();

                    String nickname =
                            user.getNickname();

                    String profileImgUrl =
                            user.getProfileImgUrl();

                    saveMyInfoCache(
                            email,
                            nickname,
                            profileImgUrl
                    );

                    if (email != null) {

                        PreferenceManager.saveUserEmail(
                                requireContext(),
                                email
                        );
                    }

                    if (nickname != null) {

                        PreferenceManager.saveNickname(
                                requireContext(),
                                nickname
                        );
                    }

                    tvEmail.setText(
                            email != null
                                    && !email.isEmpty()
                                    ? email
                                    : "조회된 계정"
                    );

                    tvNickname.setText(
                            nickname != null
                                    && !nickname.isEmpty()
                                    ? nickname
                                    : "미설정"
                    );

                    loadProfileImage(
                            profileImgUrl
                    );

                } else {

                    loadCachedMyInfo();
                }
            }

            @Override
            public void onFailure(
                    Call<LoginResponseDto> call,
                    Throwable t
            ) {

                isLoadingMyInfo =
                        false;

                if (call.isCanceled()) {

                    return;
                }

                if (!isAdded()
                        || getContext() == null
                        || getView() == null) {

                    return;
                }

                loadCachedMyInfo();
            }
        });
    }

    private void saveMyInfoCache(
            String email,
            String nickname,
            String profileImgUrl
    ) {

        SharedPreferences preferences =
                requireActivity().getSharedPreferences(
                        PREF_NAME,
                        requireActivity().MODE_PRIVATE
                );

        SharedPreferences.Editor editor =
                preferences.edit();

        if (email != null) {

            editor.putString(
                    CACHED_EMAIL,
                    email
            );
        }

        if (nickname != null) {

            editor.putString(
                    CACHED_NICKNAME,
                    nickname
            );
        }

        if (profileImgUrl != null) {

            editor.putString(
                    CACHED_PROFILE_IMG_URL,
                    profileImgUrl
            );
        }

        editor.apply();
    }

    private void loadProfileImage(
            String profileImgUrl
    ) {

        if (!isAdded()
                || getContext() == null) {

            return;
        }

        if (profileImgUrl != null
                && !profileImgUrl.isEmpty()) {

            String imageUrl =
                    BASE_IMAGE_URL
                            + profileImgUrl;

            Glide.with(ProfileFragment.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(ivProfileImage);

        } else {

            ivProfileImage.setImageResource(
                    R.drawable.default_profile
            );
        }
    }

    private void logout() {

        SharedPreferences preferences =
                requireActivity().getSharedPreferences(
                        PREF_NAME,
                        requireActivity().MODE_PRIVATE
                );

        String refreshToken =
                preferences.getString(
                        REFRESH_TOKEN,
                        null
                );

        if (refreshToken == null
                || refreshToken.trim().isEmpty()) {

            clearTokensAndMoveToMain();

            return;
        }

        LogoutRequestDto requestDto =
                new LogoutRequestDto(refreshToken);

        RetrofitClient.getApiService(requireContext())
                .logout(requestDto)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        clearTokensAndMoveToMain();
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        clearTokensAndMoveToMain();
                    }
                });
    }

    private void clearTokensAndMoveToMain() {

        SharedPreferences preferences =
                requireActivity().getSharedPreferences(
                        PREF_NAME,
                        requireActivity().MODE_PRIVATE
                );

        preferences.edit()
                .remove(ACCESS_TOKEN)
                .remove(REFRESH_TOKEN)
                .remove(CACHED_EMAIL)
                .remove(CACHED_NICKNAME)
                .remove(CACHED_PROFILE_IMG_URL)
                .putInt(LOGIN_STATUS, 0)
                .apply();

        Intent intent =
                new Intent(
                        requireContext(),
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }
}