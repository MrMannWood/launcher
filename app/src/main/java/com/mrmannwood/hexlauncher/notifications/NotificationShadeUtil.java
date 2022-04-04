package com.mrmannwood.hexlauncher.notifications;

import android.content.Context;

import java.lang.reflect.Method;

import timber.log.Timber;

public class NotificationShadeUtil {
    public static void showNotificationShade(Context context) {
        try {
            Object statusBarService = context.getSystemService("statusbar");
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method showStatusBar = statusBarManager.getMethod("expandNotificationsPanel");
            showStatusBar.invoke(statusBarService);
        } catch (Exception e) {
            Timber.e(e, "Exception while opening notification shade");
        }
    }
}
