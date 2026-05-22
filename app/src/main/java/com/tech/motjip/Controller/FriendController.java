package com.tech.motjip.Controller;

import android.content.Context;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.ResponseDto.FriendStatusResponseDto;

import retrofit2.Callback;

public class FriendController {

    private final ApiService apiService;

    public FriendController(Context context) {

        apiService =
                RetrofitClient.getApiService(context);
    }

    public void sendFriendRequest(
            Long receiverId,
            Callback<Void> callback
    ) {

        apiService.sendFriendRequest(
                receiverId
        ).enqueue(callback);
    }

    public void respondFriendRequest(
            Long friendRequestId,
            String status,
            Callback<Void> callback
    ) {

        apiService.respondFriendRequest(
                friendRequestId,
                status
        ).enqueue(callback);
    }

    public void cancelFriendRequest(
            Long friendRequestId,
            Callback<Void> callback
    ) {

        apiService.cancelFriendRequest(
                friendRequestId
        ).enqueue(callback);
    }

    public void getFriendStatus(
            Long targetMemberId,
            Callback<FriendStatusResponseDto> callback
    ) {

        apiService.getFriendStatus(
                targetMemberId
        ).enqueue(callback);
    }

    public void deleteFriend(
            Long friendMemberId,
            Callback<Void> callback
    ) {

        apiService.deleteFriend(
                friendMemberId
        ).enqueue(callback);
    }
}