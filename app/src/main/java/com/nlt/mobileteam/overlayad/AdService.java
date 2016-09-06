package com.nlt.mobileteam.overlayad;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RelativeLayout;

public class AdService extends Service {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mRootLayoutParams;
    private RelativeLayout mRootLayout;

    private WebView adWebView;
    private UserPresentReceiver userPresentReceiver;
    private IntentFilter intentFilter;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        userPresentReceiver = new UserPresentReceiver();
        userPresentReceiver.setAdService(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(userPresentReceiver, intentFilter);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mRootLayout = (RelativeLayout) LayoutInflater.from(this).
                inflate(R.layout.advertisment_layout, null);

        adWebView = (WebView) mRootLayout.findViewById(R.id.cover_layout);
        adWebView.loadUrl("https://shop.rammstein.de/img/products/1373/rst_flake_schallwandler_seitlich.png?w=360&fit=max&sharp=1");
        adWebView.getSettings().setLoadsImagesAutomatically(true);
        adWebView.getSettings().setJavaScriptEnabled(true);
        adWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mRootLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        mRootLayoutParams.gravity = Gravity.CENTER;
        float dpHeight = displayMetrics.heightPixels;
        float dpWidth = displayMetrics.widthPixels;

        ViewGroup.LayoutParams imageViewLayoutParams = adWebView.getLayoutParams();
        float width = (dpWidth - imageViewLayoutParams.width) / 2f;
        float height = (dpHeight - imageViewLayoutParams.height) / 2f + getResources().getDimensionPixelSize(R.dimen.vertical_offset);
        mRootLayout.setX(width);
        mRootLayout.setY(height);
        hideView();
        mWindowManager.addView(mRootLayout, mRootLayoutParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("stop_service", false)) {
            stopSelf();
        } else {
            Intent notificationIntent = new Intent(this, AdService.class);
            notificationIntent.putExtra("stop_service", true);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
            Notification notification = new Notification(
                    R.mipmap.ic_launcher,
                    "ad service",
                    System.currentTimeMillis());
            notification.setLatestEventInfo(
                    this,
                    getString(R.string.notif_title),
                    getString(R.string.notif_subtitle),
                    pendingIntent);
            startForeground(86, notification);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(userPresentReceiver);
        if (mRootLayout != null) {
            mWindowManager.removeView(mRootLayout);
        }
    }


    void hideView() {
        if (mRootLayout != null) {
            mRootLayout.setVisibility(View.INVISIBLE);
        }
    }

    void showView() {
        if (mRootLayout != null) {
            mRootLayout.setVisibility(View.VISIBLE);
        }
    }
}