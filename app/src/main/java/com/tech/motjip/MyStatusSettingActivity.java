package com.tech.motjip;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.RequestDto.UpdateMyStatusSettingRequestDto;
import com.tech.motjip.Dto.ResponseDto.MyStatusSettingResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyStatusSettingActivity extends AppCompatActivity {

    private Switch switchRejectFriendRequest;
    private Switch switchRejectChat;
    private Switch switchRejectFriendRecommend;
    private Switch switchRejectCommunityInvite;

    private Button btnSaveStatusSetting;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_status_setting);

        apiService = RetrofitClient.getApiService(
                MyStatusSettingActivity.this
        );

        switchRejectFriendRequest = findViewById(R.id.switchRejectFriendRequest);
        switchRejectChat = findViewById(R.id.switchRejectChat);
        switchRejectFriendRecommend = findViewById(R.id.switchRejectFriendRecommend);
        switchRejectCommunityInvite = findViewById(R.id.switchRejectCommunityInvite);

        btnSaveStatusSetting = findViewById(R.id.btnSaveStatusSetting);

        loadMyStatusSetting();

        btnSaveStatusSetting.setOnClickListener(v ->
                saveMyStatusSetting()
        );
    }

    private void loadMyStatusSetting() {

        apiService.getMyStatusSetting()
                .enqueue(new Callback<MyStatusSettingResponseDto>() {
                    @Override
                    public void onResponse(
                            Call<MyStatusSettingResponseDto> call,
                            Response<MyStatusSettingResponseDto> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    MyStatusSettingActivity.this,
                                    "상태 설정을 불러오지 못했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        MyStatusSettingResponseDto status =
                                response.body();

                        switchRejectFriendRequest.setChecked(
                                Boolean.TRUE.equals(
                                        status.getRejectFriendRequest()
                                )
                        );

                        switchRejectChat.setChecked(
                                Boolean.TRUE.equals(
                                        status.getRejectChat()
                                )
                        );

                        switchRejectFriendRecommend.setChecked(
                                Boolean.TRUE.equals(
                                        status.getRejectFriendRecommend()
                                )
                        );

                        switchRejectCommunityInvite.setChecked(
                                Boolean.TRUE.equals(
                                        status.getRejectCommunityInvite()
                                )
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<MyStatusSettingResponseDto> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                MyStatusSettingActivity.this,
                                "서버 연결에 실패했습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void saveMyStatusSetting() {

        UpdateMyStatusSettingRequestDto requestDto =
                new UpdateMyStatusSettingRequestDto(
                        switchRejectFriendRequest.isChecked(),
                        switchRejectChat.isChecked(),
                        switchRejectFriendRecommend.isChecked(),
                        switchRejectCommunityInvite.isChecked()
                );

        apiService.updateMyStatusSetting(requestDto)
                .enqueue(new Callback<MyStatusSettingResponseDto>() {
                    @Override
                    public void onResponse(
                            Call<MyStatusSettingResponseDto> call,
                            Response<MyStatusSettingResponseDto> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    MyStatusSettingActivity.this,
                                    "상태 설정 저장에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        Toast.makeText(
                                MyStatusSettingActivity.this,
                                "상태 설정이 저장되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<MyStatusSettingResponseDto> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                MyStatusSettingActivity.this,
                                "서버 연결에 실패했습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}