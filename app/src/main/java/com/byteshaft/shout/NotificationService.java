package com.byteshaft.shout;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class NotificationService extends Service {

    private Notification status;
    private final String LOG_TAG = "NotificationService";
    private RemoteViews views;
    private RemoteViews bigViews;
    public static NotificationService sInstance;
    private NotificationManager notificationManager;

    public static NotificationService getsInstance() {
        return sInstance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sInstance = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
//            showNotification();
//            AppGlobals.setNotificationVisibility(true);
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            try {
                Player.getInstance().togglePlayPause();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            showNotification();
            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
            AppGlobals.setNotificationVisibility(false);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppGlobals.setNotificationVisibility(false);
        stopForeground(true);
        stopSelf();
    }

    public void showNotification() {
        // Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);
        // showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
//        bigViews.setImageViewBitmap(R.id.status_bar_album_art, AppGlobals.getCurrentPlayingSongBitMap());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(this, NotificationService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);

        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        if (AppGlobals.getSongStatus()) {
            views.setImageViewBitmap(R.id.status_bar_play,
                    AppGlobals.pauseButton);
            bigViews.setImageViewBitmap(R.id.status_bar_play,
                    AppGlobals.pauseButton);
        } else {
            views.setImageViewBitmap(R.id.status_bar_play,
                    AppGlobals.playButton);
            bigViews.setImageViewBitmap(R.id.status_bar_play,
                    AppGlobals.playButton);
        }

        views.setImageViewBitmap(R.id.status_bar_icon, AppGlobals.notificationAlbumArt);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art, AppGlobals.notificationAlbumArt);

        bigViews.setTextViewText(R.id.status_bar_album_name, "8CCC FM");

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.icon;
        status.contentIntent = pendingIntent;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }
}
