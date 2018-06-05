package com.tf.forcestopper.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tf.forcestopper.R;
import com.tf.forcestopper.model.ApplicationItem;
import com.tf.forcestopper.util.ApplicationsHelper;
import com.tf.forcestopper.util.Preferences;
import com.tf.forcestopper.util.ShellScriptExecutor;
import com.tf.forcestopper.view.MainActivity;

import java.io.IOException;
import java.util.List;

import androidx.work.Worker;

public class ForceStopWorker extends Worker {

    private static final String TAG = ForceStopWorker.class.getSimpleName();
    private NotificationManager mNotificationManager;

    @NonNull
    @Override
    public WorkerResult doWork() {
        long startTime = System.currentTimeMillis();

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notifyProgress();

        List<ApplicationItem> installedApplications = new ApplicationsHelper(getApplicationContext()).getInstalledApplicationItems();
        List<String> ignoredList = new Preferences(getApplicationContext()).getIgnoreList();

        for (ApplicationItem applicationItem : installedApplications) {
            try {
                if (!ignoredList.contains(applicationItem.packageName)) {
                    ShellScriptExecutor.executeShell("am force-stop --user current " + applicationItem.packageName);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "force stopped " + installedApplications.size() + " in " + (System.currentTimeMillis() - startTime));

        notifySuccess();

        return WorkerResult.SUCCESS;
    }

    private void notifyProgress() {
        final Notification.Builder notificationBuilder = getNotificationBuilder();

        mNotificationManager.notify(0, notificationBuilder
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getApplicationContext().getString(R.string.app_name))
                        .setContentText(getApplicationContext().getString(R.string.stopping_apps))
                        .setOngoing(true)
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                        .build());
    }

    private void notifySuccess() {
        final Notification.Builder notificationBuilder = getNotificationBuilder();

        mNotificationManager.notify(0, notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(getApplicationContext().getString(R.string.apps_stopped))
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build());
    }

    @NonNull
    private Notification.Builder getNotificationBuilder() {
        final Notification.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelId = "status";
            notificationBuilder = new Notification.Builder(getApplicationContext(), channelId);

            NotificationChannel channel = new NotificationChannel(channelId, "Status", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        } else {
            notificationBuilder = new Notification.Builder(getApplicationContext());
        }

        return notificationBuilder;
    }
}
