package com.tech.motjip.manager;

import android.content.Context;
import android.content.Intent;

import com.tech.motjip.MyFirebaseMessagingService;

public class NotificationBadgeManager {

    private NotificationBadgeManager() {
    }

    public static void sendBadgeUpdate(
            Context context
    ) {

        if (context == null) {

            return;
        }

        Intent intent =
                new Intent(
                        MyFirebaseMessagingService.ACTION_NOTIFICATION_BADGE_UPDATE
                );

        intent.setPackage(
                context.getPackageName()
        );

        context.sendBroadcast(
                intent
        );
    }

    public static String formatBadgeCount(
            int unreadCount
    ) {

        if (unreadCount <= 0) {

            return "";
        }

        if (unreadCount > 99) {

            return "99+";
        }

        return String.valueOf(
                unreadCount
        );
    }

    public static boolean shouldShowBadge(
            int unreadCount
    ) {

        return unreadCount > 0;
    }
}