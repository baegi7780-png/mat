package com.tech.motjip.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Adapter.FriendAdapter;
import com.tech.motjip.Dto.ResponseDto.FriendResponseDto;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendFragment extends Fragment {

    private RecyclerView recyclerView;

    private FriendAdapter adapter;

    private final List<FriendResponseDto> friendList =
            new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view =
                inflater.inflate(
                        R.layout.fragment_friend,
                        container,
                        false
                );

        recyclerView =
                view.findViewById(
                        R.id.rv_friend_list
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        adapter =
                new FriendAdapter(
                        requireContext(),
                        new FriendAdapter.OnFriendClickListener() {

                            @Override
                            public void onChatClick(
                                    FriendResponseDto friend
                            ) {

                                Toast.makeText(
                                        getContext(),
                                        friend.getNickname(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onInviteClick(
                                    FriendResponseDto friend
                            ) {

                            }

                            @Override
                            public void onRecommendAddClick(
                                    FriendResponseDto friend
                            ) {

                            }

                            @Override
                            public void onDeleteFriendClick(
                                    FriendResponseDto friend
                            ) {

                            }
                        }
                );

        recyclerView.setAdapter(adapter);

        loadFriends();

        return view;
    }

    private void loadFriends() {

        ApiService apiService =
                RetrofitClient.getApiService(getContext());

        apiService.getCurrentUser()
                .enqueue(new Callback<LoginResponseDto>() {

                    @Override
                    public void onResponse(
                            Call<LoginResponseDto> call,
                            Response<LoginResponseDto> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            apiService.getMyFriends()
                                    .enqueue(
                                            new Callback<List<FriendResponseDto>>() {

                                                @Override
                                                public void onResponse(
                                                        Call<List<FriendResponseDto>> call,
                                                        Response<List<FriendResponseDto>> response
                                                ) {

                                                    if (response.isSuccessful()
                                                            && response.body() != null) {

                                                        friendList.clear();

                                                        friendList.addAll(
                                                                response.body()
                                                        );

                                                        adapter.setFriends(
                                                                friendList
                                                        );
                                                    }
                                                }

                                                @Override
                                                public void onFailure(
                                                        Call<List<FriendResponseDto>> call,
                                                        Throwable t
                                                ) {

                                                    Log.e(
                                                            "FRIEND",
                                                            "친구 목록 실패",
                                                            t
                                                    );
                                                }
                                            }
                                    );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<LoginResponseDto> call,
                            Throwable t
                    ) {

                        Log.e(
                                "FRIEND",
                                "유저 조회 실패",
                                t
                        );
                    }
                });
    }
}