package com.tech.motjip;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;

import com.tech.motjip.Controller.CommunityDetailController;
import com.tech.motjip.Model.CommunityPost;

public class CommunityDetailActivity
        extends AppCompatActivity {

    private ImageView ivPostImage;

    private ImageView ivFavorite;
    private ImageView ivMore;

    private TextView tvTitle;
    private TextView tvTagRegion;
    private TextView tvPlace;
    private TextView tvDate;
    private TextView tvContent;
    private TextView tvMemberCount;

    private Button btnJoin;

    private Button btnOpenChatLink;

    private MaterialButton btnMemberList;

    private CommunityDetailController detailController;

    private CommunityPost post;

    private Long comId;

    private ActivityResultLauncher<Intent> editPostLauncher;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_community_detail
        );

        editPostLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK) {

                                setResult(RESULT_OK);

                                finish();
                            }
                        }
                );

        detailController =
                new CommunityDetailController(this);

        ivPostImage =
                findViewById(R.id.ivPostImage);

        ivFavorite =
                findViewById(R.id.ivFavorite);

        ivMore =
                findViewById(R.id.ivMore);

        tvTitle =
                findViewById(R.id.tvTitle);

        tvTagRegion =
                findViewById(R.id.tvTagRegion);

        tvPlace =
                findViewById(R.id.tvPlace);

        tvDate =
                findViewById(R.id.tvDate);

        tvContent =
                findViewById(R.id.tvContent);

        tvMemberCount =
                findViewById(R.id.tvMemberCount);

        btnJoin =
                findViewById(R.id.btnJoin);

        btnOpenChatLink =
                findViewById(R.id.btnOpenChatLink);

        btnMemberList =
                findViewById(R.id.btnMemberList);

        post =
                (CommunityPost) getIntent()
                        .getSerializableExtra(
                                "communityPost"
                        );

        if (post == null || post.getComId() == null) {

            Toast.makeText(
                    this,
                    "게시글 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        comId =
                post.getComId();

        setupPostData();

        setupTopButtons();

        setupJoinedState();

        setupJoinButton();

        setupChatLinkButton();

        setupMemberListButton();

        ivPostImage.setOnClickListener(v -> {
            showImagePreviewDialog();
        });
    }

    private void setupTopButtons() {

        if (post.isMine()) {

            ivFavorite.setVisibility(
                    View.GONE
            );

            ivMore.setVisibility(
                    View.VISIBLE
            );

        } else {

            ivFavorite.setVisibility(
                    View.VISIBLE
            );

            ivMore.setVisibility(
                    View.GONE
            );
        }

        ivFavorite.setColorFilter(
                detailController.getFavoriteColor(
                        post.isFavorite()
                )
        );

        ivMore.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            CommunityDetailActivity.this,
                            ivMore
                    );

            popupMenu.getMenu().add(
                    "수정하기"
            );

            popupMenu.getMenu().add(
                    "삭제하기"
            );

            popupMenu.setOnMenuItemClickListener(item -> {

                String title =
                        item.getTitle().toString();

                if (title.equals("수정하기")) {

                    Intent intent =
                            new Intent(
                                    CommunityDetailActivity.this,
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

                    editPostLauncher.launch(intent);

                } else if (title.equals("삭제하기")) {

                    showDeleteConfirmDialog();
                }

                return true;
            });

            popupMenu.show();
        });

        ivFavorite.setOnClickListener(v -> {

            detailController.toggleFavorite(
                    post.getComId(),
                    new CommunityDetailController.FavoriteCallback() {

                        @Override
                        public void onSuccess(
                                boolean isFavorite
                        ) {

                            post.setFavorite(
                                    isFavorite
                            );

                            ivFavorite.setColorFilter(
                                    detailController.getFavoriteColor(
                                            isFavorite
                                    )
                            );

                            if (isFavorite) {

                                Toast.makeText(
                                        CommunityDetailActivity.this,
                                        "즐겨찾기에 추가되었습니다.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        CommunityDetailActivity.this,
                                        "즐겨찾기가 취소되었습니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            setResult(RESULT_OK);
                        }

                        @Override
                        public void onError(
                                String message
                        ) {

                            Toast.makeText(
                                    CommunityDetailActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });
    }

    private void showDeleteConfirmDialog() {

        new AlertDialog.Builder(this)
                .setTitle("모임 삭제")
                .setMessage("정말 이 모임을 삭제하시겠습니까?")
                .setNegativeButton(
                        "취소",
                        null
                )
                .setPositiveButton(
                        "삭제",
                        (dialog, which) -> deleteCommunity()
                )
                .show();
    }

    private void setupPostData() {

        tvTitle.setText(
                post.getTitle()
        );

        tvTagRegion.setText(
                post.getTag()
                        + " · "
                        + post.getRegion()
        );

        tvPlace.setText(
                post.getPlaceName()
        );

        tvDate.setText(
                detailController.formatMeetingAt(
                        post.getMeetingAt()
                )
        );

        tvContent.setText(
                post.getContent()
        );

        tvMemberCount.setText(
                post.getMemberCount()
                        + "명 참여중"
        );

        detailController.loadPostImage(
                ivPostImage,
                post.getImageUrl()
        );
    }

    private void showImagePreviewDialog() {

        if (post == null
                || post.getImageUrl() == null
                || post.getImageUrl().trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "확대할 이미지가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        PhotoView previewImageView =
                new PhotoView(this);

        int width =
                (int) (getResources()
                        .getDisplayMetrics()
                        .widthPixels * 0.92);

        int height =
                (int) (getResources()
                        .getDisplayMetrics()
                        .heightPixels * 0.65);

        previewImageView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        width,
                        height
                )
        );

        previewImageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        previewImageView.setBackgroundColor(
                Color.BLACK
        );

        previewImageView.setPadding(
                12,
                12,
                12,
                12
        );

        detailController.loadPostImage(
                previewImageView,
                post.getImageUrl()
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(previewImageView)
                        .create();

        previewImageView.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(
                            Color.parseColor("#DD000000")
                    )
            );
        }
    }

    private void setupJoinedState() {

        boolean closed =
                detailController.isMeetingClosed(
                        post.getMeetingAt()
                );

        if (closed) {

            btnJoin.setVisibility(
                    View.VISIBLE
            );

            btnJoin.setEnabled(true);

            btnJoin.setAlpha(1f);

            if (post.isMine()) {

                btnJoin.setText(
                        "모임 삭제"
                );

            } else {

                btnJoin.setText(
                        "목록 숨기기"
                );
            }

            return;
        }

        if (post.isMine()) {

            btnJoin.setVisibility(
                    View.GONE
            );

            return;
        }

        if (post.isJoined()) {

            btnJoin.setText(
                    "참여 취소"
            );

        } else {

            btnJoin.setText(
                    "모임 참여하기"
            );
        }

        btnJoin.setEnabled(true);

        btnJoin.setAlpha(1f);
    }

    private void setupJoinButton() {

        btnJoin.setOnClickListener(v -> {

            if (detailController.isMeetingClosed(
                    post.getMeetingAt()
            )) {

                btnJoin.setEnabled(false);

                if (post.isMine()) {

                    showDeleteConfirmDialog();

                } else {

                    hideClosedCommunity();
                }

                return;
            }

            btnJoin.setEnabled(false);

            if (post.isJoined()) {

                cancelJoin();

            } else {

                joinCommunity();
            }
        });
    }

    private void setupChatLinkButton() {

        if (!post.isJoined() && !post.isMine()) {

            btnOpenChatLink.setVisibility(
                    View.GONE
            );

            return;
        }

        String chatLink =
                post.getChatLink();

        if (chatLink == null
                || chatLink.trim().isEmpty()) {

            btnOpenChatLink.setVisibility(
                    View.GONE
            );

            return;
        }

        String finalChatLink =
                chatLink.trim();

        String lowerLink =
                finalChatLink.toLowerCase();

        btnOpenChatLink.setVisibility(
                View.VISIBLE
        );

        if (lowerLink.contains("open.kakao.com")) {

            btnOpenChatLink.setText(
                    "오픈채팅 참여하기"
            );

        } else {

            btnOpenChatLink.setText(
                    "채팅방 링크 열기"
            );
        }

        btnOpenChatLink.setOnClickListener(v -> {

            try {

                String openLink =
                        finalChatLink;

                if (!openLink.startsWith("http://")
                        && !openLink.startsWith("https://")) {

                    openLink =
                            "https://" + openLink;
                }

                Intent intent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(openLink)
                        );

                if (intent.resolveActivity(getPackageManager()) != null) {

                    startActivity(intent);

                } else {

                    Toast.makeText(
                            CommunityDetailActivity.this,
                            "링크를 열 수 있는 앱이 없습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } catch (Exception e) {

                Toast.makeText(
                        CommunityDetailActivity.this,
                        "채팅방 링크를 열 수 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnOpenChatLink.setOnLongClickListener(v -> {

            try {

                ClipboardManager clipboardManager =
                        (ClipboardManager) getSystemService(
                                CLIPBOARD_SERVICE
                        );

                ClipData clipData =
                        ClipData.newPlainText(
                                "chat_link",
                                finalChatLink
                        );

                if (clipboardManager != null) {

                    clipboardManager.setPrimaryClip(
                            clipData
                    );

                    Toast.makeText(
                            CommunityDetailActivity.this,
                            "링크가 복사되었습니다.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return true;
                }

            } catch (Exception e) {

                Toast.makeText(
                        CommunityDetailActivity.this,
                        "링크 복사에 실패했습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return false;
        });
    }

    private void setupMemberListButton() {

        btnMemberList.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            CommunityDetailActivity.this,
                            CommunityMemberListActivity.class
                    );

            intent.putExtra(
                    "comId",
                    comId
            );

            startActivity(intent);
        });
    }

    private void joinCommunity() {

        detailController.joinCommunity(
                comId,
                new CommunityDetailController.JoinCallback() {

                    @Override
                    public void onSuccess() {

                        post.setJoined(true);

                        post.setMemberCount(
                                post.getMemberCount() + 1
                        );

                        tvMemberCount.setText(
                                post.getMemberCount()
                                        + "명 참여중"
                        );

                        setupChatLinkButton();

                        setResult(RESULT_OK);

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                "모임 참여 완료",
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setText(
                                "참여 취소"
                        );

                        btnJoin.setEnabled(true);
                    }

                    @Override
                    public void onAlreadyJoined() {

                        post.setJoined(true);

                        setupChatLinkButton();

                        setResult(RESULT_OK);

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                "이미 참여한 모임입니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setText(
                                "참여 취소"
                        );

                        btnJoin.setEnabled(true);
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setEnabled(true);
                    }
                }
        );
    }

    private void cancelJoin() {

        detailController.cancelJoinCommunity(
                comId,
                new CommunityDetailController.BasicCallback() {

                    @Override
                    public void onSuccess() {

                        post.setJoined(false);

                        if (post.getMemberCount() > 0) {

                            post.setMemberCount(
                                    post.getMemberCount() - 1
                            );
                        }

                        tvMemberCount.setText(
                                post.getMemberCount()
                                        + "명 참여중"
                        );

                        setupChatLinkButton();

                        setResult(RESULT_OK);

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                "모임 참여가 취소되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setText(
                                "모임 참여하기"
                        );

                        btnJoin.setEnabled(true);
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setEnabled(true);
                    }
                }
        );
    }

    private void hideClosedCommunity() {

        detailController.hideClosedCommunity(
                comId,
                new CommunityDetailController.BasicCallback() {

                    @Override
                    public void onSuccess() {

                        setResult(RESULT_OK);

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                "목록에서 숨김 처리되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setEnabled(true);
                    }
                }
        );
    }

    private void deleteCommunity() {

        detailController.deleteCommunity(
                comId,
                new CommunityDetailController.BasicCallback() {

                    @Override
                    public void onSuccess() {

                        setResult(RESULT_OK);

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                "모임이 삭제되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                CommunityDetailActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        btnJoin.setEnabled(true);
                    }
                }
        );
    }
}