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
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AdService extends Service {
    private WindowManager mWindowManager;            // Reference to the window
    private WindowManager.LayoutParams mRootLayoutParams;        // Parameters of the root layout
    private RelativeLayout mRootLayout;            // Root layout

    private ImageView imageView;
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
                inflate(R.layout.service_player, null);

        imageView = (ImageView) mRootLayout.findViewById(R.id.cover_layout);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideView();
                Log.d("err", "clicked ");
            }
        });


        mRootLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,

                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,

                PixelFormat.TRANSLUCENT);


		/*mRootLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
						//

		 WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
						//| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);*/
        mRootLayoutParams.gravity = Gravity.CENTER;

        float dpHeight = displayMetrics.heightPixels;
        float dpWidth = displayMetrics.widthPixels;

        ViewGroup.LayoutParams imageViewLayoutParams = imageView.getLayoutParams();
        float width = (dpWidth - imageViewLayoutParams.width) / 2f;
        float height = (dpHeight - imageViewLayoutParams.height) / 2f + getResources().getDimensionPixelSize(R.dimen.vertical_offset);

        Log.d("pos", "lp: w" + width + " h:" + height + "from :" + dpHeight + " " + dpWidth);
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
                    "Advertisment",
                    "Tap to close the widget.",
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


    public void hideView() {
        if (mRootLayout != null) {
            mRootLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void showView() {
        if (mRootLayout != null) {
            mRootLayout.setVisibility(View.VISIBLE);
        }
    }
}