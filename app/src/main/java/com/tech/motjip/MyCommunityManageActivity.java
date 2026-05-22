package com.tech.motjip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.Adapter.CommunityPostAdapter;
import com.tech.motjip.Controller.CommunityController;
import com.tech.motjip.Model.CommunityPost;

import java.util.List;

public class MyCommunityManageActivity
        extends AppCompatActivity {

    private static final int REQUEST_DETAIL = 1001;

    private TextView tvOngoing;
    private TextView tvClosed;

    private RecyclerView recyclerView;

    private CommunityPostAdapter adapter;

    private CommunityController communityController;

    private boolean showingClosed = false;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_my_community_manage
        );

        editLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK) {

                                reloadCurrentList();
                            }
                        }
                );

        tvOngoing =
                findViewById(R.id.tvOngoing);

        tvClosed =
                findViewById(R.id.tvClosed);

        recyclerView =
                findViewById(R.id.recyclerViewMyCommunity);

        communityController =
                new CommunityController(this);

        adapter =
                new CommunityPostAdapter(
                        this,
                        post -> {

                            Intent intent =
                                    new Intent(
                                            MyCommunityManageActivity.this,
                                            CommunityDetailActivity.class
                                    );

                            intent.putExtra(
                                    "communityPost",
                                    post
                            );

                            startActivityForResult(
                                    intent,
                                    REQUEST_DETAIL
                            );
                        },
                        new CommunityPostAdapter.OnPostMenuClickListener() {

                            @Override
                            public void onEditClick(
                                    CommunityPost post
                            ) {

                                Intent intent =
                                        new Intent(
                                                MyCommunityManageActivity.this,
                                                WriteActivity.class
                                        );

                                intent.putExtra(
                                        "isEditMode",
                                        true
                                );

                                intent.putExtra(
                                        "communityPost",
                                        post
                                );

                                editLauncher.launch(intent);
                            }

                            @Override
                            public void onDeleteClick(
                                    CommunityPost post,
                                    int position
                            ) {

                                if (post.isMine()) {

                                    communityController.deleteCommunityPost(
                                            post.getComId(),
                                            new CommunityController.DeleteCommunityCallback() {

                                                @Override
                                                public void onSuccess() {

                                                    adapter.removePost(
                                                            position
                                                    );

                                                    Toast.makeText(
                                                            MyCommunityManageActivity.this,
                                                            "게시글이 삭제되었습니다.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }

                                                @Override
                                                public void onError(
                                                        String message
                                                ) {

                                                    Toast.makeText(
                                                            MyCommunityManageActivity.this,
                                                            message,
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            }
                                    );

                                } else {

                                    communityController.hideMyClosedCommunity(
                                            post.getComId(),
                                            new CommunityController.HideClosedCommunityCallback() {

                                                @Override
                                                public void onSuccess() {

                                                    adapter.removePost(
                                                            position
                                                    );

                                                    Toast.makeText(
                                                            MyCommunityManageActivity.this,
                                                            "목록에서 삭제되었습니다.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }

                                                @Override
                                                public void onError(
                                                        String message
                                                ) {

                                                    Toast.makeText(
                                                            MyCommunityManageActivity.this,
                                                            message,
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            }
                                    );
                                }
                            }
                        }
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        loadOngoingCommunities();

        tvOngoing.setOnClickListener(v -> {

            showingClosed = false;

            updateTabUi();

            loadOngoingCommunities();
        });

        tvClosed.setOnClickListener(v -> {

            showingClosed = true;

            updateTabUi();

            loadClosedCommunities();
        });

        updateTabUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadCurrentList();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == REQUEST_DETAIL
                && resultCode == RESULT_OK) {

            reloadCurrentList();
        }
    }

    private void reloadCurrentList() {

        if (showingClosed) {

            loadClosedCommunities();

        } else {

            loadOngoingCommunities();
        }
    }

    private void updateTabUi() {

        if (showingClosed) {

            tvClosed.setBackgroundResource(
                    R.drawable.bg_orange_fill_round
            );

            tvClosed.setTextColor(
                    getColor(android.R.color.white)
            );

            tvOngoing.setBackgroundResource(
                    R.drawable.bg_gray_round
            );

            tvOngoing.setTextColor(
                    0xFF444444
            );

        } else {

            tvOngoing.setBackgroundResource(
                    R.drawable.bg_orange_fill_round
            );

            tvOngoing.setTextColor(
                    getColor(android.R.color.white)
            );

            tvClosed.setBackgroundResource(
                    R.drawable.bg_gray_round
            );

            tvClosed.setTextColor(
                    0xFF444444
            );
        }
    }

    private void loadOngoingCommunities() {

        communityController.loadMyCommunities(
                "new",
                new CommunityController.CommunityCallback() {

                    @Override
                    public void onSuccess(
                            List<CommunityPost> posts,
                            boolean isLastPage
                    ) {

                        adapter.setPosts(posts);
                    }

                    @Override
                    public void onEmpty() {

                        adapter.setPosts(null);
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                MyCommunityManageActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void loadClosedCommunities() {

        communityController.loadMyClosedCommunities(
                "new",
                new CommunityController.CommunityCallback() {

                    @Override
                    public void onSuccess(
                            List<CommunityPost> posts,
                            boolean isLastPage
                    ) {

                        adapter.setPosts(posts);
                    }

                    @Override
                    public void onEmpty() {

                        adapter.setPosts(null);
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                MyCommunityManageActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}