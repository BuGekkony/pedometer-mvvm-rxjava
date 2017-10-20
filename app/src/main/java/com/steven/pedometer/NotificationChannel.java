package com.steven.pedometer;

import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.RequiresApi;

/**
 * Created by Steven on 2017-10-03.
 */

public class NotificationChannel {

    public static final String GROUP_ID = "20171003";
    public static final String GROUP_NAME = "service foreground";

    @RequiresApi(26)
    public static void createNotificationChannelIfNeeded(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(GROUP_ID, GROUP_NAME));
    }
}
