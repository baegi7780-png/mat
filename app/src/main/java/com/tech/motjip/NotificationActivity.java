package com.tech.motjip;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.Adapter.NotificationAdapter;
import com.tech.motjip.Controller.NotificationController;
import com.tech.motjip.Model.NotificationItem;
import com.tech.motjip.Utils.DialogUtil;

import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class NotificationActivity
        extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;

    private TextView tvEmpty;

    private Button btnDeleteSelected;

    private NotificationAdapter adapter;

    private NotificationController notificationController;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_notification
        );

        recyclerViewNotifications =
                findViewById(R.id.recyclerViewNotifications);

        tvEmpty =
                findViewById(R.id.tvEmpty);

        btnDeleteSelected =
                findViewById(R.id.btnDeleteSelected);

        notificationController =
                new NotificationController(this);

        btnDeleteSelected.setVisibility(
                View.GONE
        );

        adapter =
                new NotificationAdapter(
                        this,
                        new NotificationAdapter.OnNotificationActionListener() {

                            @Override
                            public void onAcceptClick(
                                    NotificationItem item
                            ) {

                                if (item == null
                                        || item.getType() == null) {

                                    Toast.makeText(
                                            NotificationActivity.this,
                                            "알림 정보가 올바르지 않습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if ("FRIEND_INVITE".equals(
                                        item.getType()
                                )) {

                                    respondFriendRequest(
                                            item,
                                            "ACCEPTED"
                                    );

                                } else if ("COMMUNITY_INVITE".equals(
                                        item.getType()
                                )) {

                                    respondCommunityInvite(
                                            item,
                                            "ACCEPTED"
                                    );

                                } else {

                                    Toast.makeText(
                                            NotificationActivity.this,
                                            "처리할 수 없는 알림입니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onRejectClick(
                                    NotificationItem item
                            ) {

                                if (item == null
                                        || item.getType() == null) {

                                    Toast.makeText(
                                            NotificationActivity.this,
                                            "알림 정보가 올바르지 않습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if ("FRIEND_INVITE".equals(
                                        item.getType()
                                )) {

                                    respondFriendRequest(
                                            item,
                                            "REJECTED"
                                    );

                                } else if ("COMMUNITY_INVITE".equals(
                                        item.getType()
                                )) {

                                    respondCommunityInvite(
                                            item,
                                            "REJECTED"
                                    );

                                } else {

                                    Toast.makeText(
                                            NotificationActivity.this,
                                            "처리할 수 없는 알림입니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onReadClick(
                                    NotificationItem item,
                                    int position
                            ) {

                                if (item == null
                                        || item.isRead()) {

                                    return;
                                }

                                notificationController.markAsRead(
                                        item.getNotificationId(),
                                        new NotificationController.NotificationActionCallback() {

                                            @Override
                                            public void onSuccess() {

                                                item.setRead(true);

                                                adapter.notifyItemChanged(
                                                        position
                                                );
                                            }

                                            @Override
                                            public void onError(
                                                    String message
                                            ) {

                                                Toast.makeText(
                                                        NotificationActivity.this,
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onSelectionModeChange(
                                    boolean isSelectionMode
                            ) {

                                if (isSelectionMode) {

                                    btnDeleteSelected.setVisibility(
                                            View.VISIBLE
                                    );

                                } else {

                                    btnDeleteSelected.setVisibility(
                                            View.GONE
                                    );
                                }
                            }
                        }
                );

        recyclerViewNotifications.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewNotifications.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT
                ) {

                    @Override
                    public boolean onMove(
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target
                    ) {

                        return false;
                    }

                    @Override
                    public void onSwiped(
                            RecyclerView.ViewHolder viewHolder,
                            int direction
                    ) {

                        if (adapter.isSelectionMode()) {

                            adapter.notifyItemChanged(
                                    viewHolder.getAdapterPosition()
                            );

                            return;
                        }

                        int position =
                                viewHolder.getAdapterPosition();

                        NotificationItem item =
                                adapter.getNotification(position);

                        new AlertDialog.Builder(NotificationActivity.this)
                                .setTitle("알림 삭제")
                                .setMessage("이 알림을 삭제하시겠습니까?")
                                .setPositiveButton(
                                        "삭제",
                                        (dialog, which) -> {

                                            notificationController.deleteNotification(
                                                    item.getNotificationId(),
                                                    new NotificationController.NotificationActionCallback() {

                                                        @Override
                                                        public void onSuccess() {

                                                            adapter.removeNotification(
                                                                    position
                                                            );

                                                            Toast.makeText(
                                                                    NotificationActivity.this,
                                                                    "알림이 삭제되었습니다.",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();

                                                            if (adapter.getItemCount() == 0) {

                                                                tvEmpty.setVisibility(
                                                                        View.VISIBLE
                                                                );

                                                                recyclerViewNotifications.setVisibility(
                                                                        View.GONE
                                                                );
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(
                                                                String message
                                                        ) {

                                                            adapter.notifyItemChanged(
                                                                    position
                                                            );

                                                            Toast.makeText(
                                                                    NotificationActivity.this,
                                                                    message,
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        }
                                                    }
                                            );
                                        }
                                )
                                .setNegativeButton(
                                        "취소",
                                        (dialog, which) ->
                                                adapter.notifyItemChanged(position)
                                )
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void onChildDraw(
                            Canvas c,
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive
                    ) {

                        new RecyclerViewSwipeDecorator.Builder(
                                c,
                                recyclerView,
                                viewHolder,
                                dX,
                                dY,
                                actionState,
                                isCurrentlyActive
                        )
                                .addSwipeLeftBackgroundColor(
                                        ContextCompat.getColor(
                                                NotificationActivity.this,
                                                android.R.color.holo_red_dark
                                        )
                                )
                                .addSwipeLeftActionIcon(
                                        android.R.drawable.ic_menu_delete
                                )
                                .addSwipeLeftLabel("삭제")
                                .setSwipeLeftLabelColor(
                                        ContextCompat.getColor(
                                                NotificationActivity.this,
                                                android.R.color.white
                                        )
                                )
                                .create()
                                .decorate();

                        super.onChildDraw(
                                c,
                                recyclerView,
                                viewHolder,
                                dX,
                                dY,
                                actionState,
                                isCurrentlyActive
                        );
                    }
                };

        new ItemTouchHelper(simpleCallback)
                .attachToRecyclerView(
                        recyclerViewNotifications
                );

        btnDeleteSelected.setOnClickListener(v -> {

            List<Long> selectedIds =
                    adapter.getSelectedNotificationIds();

            if (selectedIds.isEmpty()) {

                Toast.makeText(
                        NotificationActivity.this,
                        "선택된 알림이 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            new AlertDialog.Builder(NotificationActivity.this)
                    .setTitle("알림 삭제")
                    .setMessage("선택한 알림을 삭제하시겠습니까?")
                    .setPositiveButton(
                            "삭제",
                            (dialog, which) -> {

                                notificationController.deleteNotifications(
                                        selectedIds,
                                        new NotificationController.NotificationActionCallback() {

                                            @Override
                                            public void onSuccess() {

                                                adapter.removeSelectedNotifications();

                                                adapter.setSelectionMode(
                                                        false
                                                );

                                                Toast.makeText(
                                                        NotificationActivity.this,
                                                        "선택한 알림을 삭제했습니다.",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                if (adapter.getItemCount() == 0) {

                                                    tvEmpty.setVisibility(
                                                            View.VISIBLE
                                                    );

                                                    recyclerViewNotifications.setVisibility(
                                                            View.GONE
                                                    );
                                                }
                                            }

                                            @Override
                                            public void onError(
                                                    String message
                                            ) {

                                                Toast.makeText(
                                                        NotificationActivity.this,
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }
                                );
                            }
                    )
                    .setNegativeButton(
                            "취소",
                            null
                    )
                    .show();

        });

        loadNotifications();

        showNotificationGuideIfNeeded();
    }

    @Override
    public void onBackPressed() {

        if (adapter != null
                && adapter.isSelectionMode()) {

            adapter.setSelectionMode(false);

            return;
        }

        super.onBackPressed();
    }

    private void showNotificationGuideIfNeeded() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "notification_guide",
                        MODE_PRIVATE
                );

        boolean isGuideShown =
                preferences.getBoolean(
                        "isGuideShown",
                        false
                );

        if (isGuideShown) {

            return;
        }

        DialogUtil.showMessageDialog(
                this,
                R.drawable.ic_launcher_foreground,
                "알림 관리 안내",
                "• 알림을 클릭하면 읽음 처리됩니다.\n\n"
                        + "• 알림을 왼쪽으로 밀면 삭제할 수 있습니다.\n\n"
                        + "• 알림을 길게 누르면 여러 개를 선택해서 삭제할 수 있습니다.",
                () -> preferences.edit()
                        .putBoolean(
                                "isGuideShown",
                                true
                        )
                        .apply()
        );
    }

    private void loadNotifications() {

        notificationController.getNotifications(
                new NotificationController.NotificationListCallback() {

                    @Override
                    public void onSuccess(
                            List<NotificationItem> notifications
                    ) {

                        if (notifications == null
                                || notifications.isEmpty()) {

                            tvEmpty.setVisibility(View.VISIBLE);

                            recyclerViewNotifications.setVisibility(View.GONE);

                            return;
                        }

                        tvEmpty.setVisibility(View.GONE);

                        recyclerViewNotifications.setVisibility(View.VISIBLE);

                        adapter.setNotifications(
                                notifications
                        );

                        markUnreadNotificationsAsRead(
                                notifications
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                NotificationActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void markUnreadNotificationsAsRead(
            List<NotificationItem> notifications
    ) {

        if (notifications == null
                || notifications.isEmpty()) {

            return;
        }

        for (NotificationItem item : notifications) {

            if (item == null
                    || item.isRead()
                    || item.getNotificationId() == null) {

                continue;
            }

            notificationController.markAsRead(
                    item.getNotificationId(),
                    new NotificationController.NotificationActionCallback() {

                        @Override
                        public void onSuccess() {

                            item.setRead(true);
                        }

                        @Override
                        public void onError(
                                String message
                        ) {
                        }
                    }
            );
        }
    }

    private void respondFriendRequest(
            NotificationItem item,
            String status
    ) {

        if (item == null
                || item.getTargetId() == null) {

            Toast.makeText(
                    this,
                    "알림 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        notificationController.respondFriendRequest(
                item.getTargetId(),
                status,
                new NotificationController.NotificationActionCallback() {

                    @Override
                    public void onSuccess() {

                        if ("ACCEPTED".equals(status)) {

                            Toast.makeText(
                                    NotificationActivity.this,
                                    "친구 요청을 수락했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    NotificationActivity.this,
                                    "친구 요청을 거절했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        loadNotifications();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                NotificationActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void respondCommunityInvite(
            NotificationItem item,
            String status
    ) {

        if (item == null
                || item.getTargetId() == null) {

            Toast.makeText(
                    this,
                    "알림 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        notificationController.respondCommunityInvite(
                item.getTargetId(),
                status,
                new NotificationController.NotificationActionCallback() {

                    @Override
                    public void onSuccess() {

                        if ("ACCEPTED".equals(status)) {

                            Toast.makeText(
                                    NotificationActivity.this,
                                    "모임 초대를 수락했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    NotificationActivity.this,
                                    "모임 초대를 거절했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        loadNotifications();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                NotificationActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}