package com.nlt.mobileteam.overlayad;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class AdService extends Service {

	// 
	private static final int TRAY_HIDDEN_FRACTION 			= 6; 	// Controls fraction of the tray hidden when open
	private static final int TRAY_MOVEMENT_REGION_FRACTION 	= 6;	// Controls fraction of y-axis on screen within which the tray stays.
	private static final int TRAY_CROP_FRACTION 			= 12;	// Controls fraction of the tray chipped at the right end.
	private static final int ANIMATION_FRAME_RATE 			= 30;	// Animation frame rate per second.
	private static final int TRAY_DIM_X_DP 					= 170;	// Width of the tray in dps
	private static final int TRAY_DIM_Y_DP 					= 160; 	// Height of the tray in dps
	private static final int BUTTONS_DIM_Y_DP 				= 27;	// Height of the buttons in dps
	
	// Layout containers for various widgets
	private WindowManager mWindowManager;			// Reference to the window
	private WindowManager.LayoutParams 	mRootLayoutParams;		// Parameters of the root layout
	private RelativeLayout mRootLayout;			// Root layout
	private LinearLayout mContentContainerLayout;// Contains everything other than buttons and song info
	private ImageView mLogoView;


	private boolean mIsTrayOpen = true;
	
	// Controls for animations
	private Timer mTrayAnimationTimer;
	private TrayAnimationTimerTask 	mTrayTimerTask;
	private Handler mAnimationHandler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		// Not used
		return null;
	}

	@Override
	public void onCreate() {

		// Get references to all the views and add them to root view as needed.
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		mRootLayout = (RelativeLayout) LayoutInflater.from(this).
				inflate(R.layout.service_ad, null);
		mContentContainerLayout = (LinearLayout) mRootLayout.findViewById(R.id.content_container);
		mContentContainerLayout.setOnTouchListener(new TrayTouchListener());

		mLogoView = (ImageView) mRootLayout.findViewById(R.id.view);

		/*mRootLayoutParams = new WindowManager.LayoutParams(
				Utils.dpToPixels(TRAY_DIM_X_DP, getResources()),
				Utils.dpToPixels(TRAY_DIM_Y_DP, getResources()),
				WindowManager.LayoutParams.TYPE_PHONE, 
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
*/
		mRootLayoutParams = new WindowManager.LayoutParams(
				Utils.dpToPixels(TRAY_DIM_X_DP, getResources()),
				Utils.dpToPixels(TRAY_DIM_Y_DP, getResources()),
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);

		mRootLayoutParams.gravity = Gravity.BOTTOM;
		mRootLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowManager.addView(mRootLayout, mRootLayoutParams);
		


		mRootLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				
				// Reusable variables
				RelativeLayout.LayoutParams params;
				InputStream is;
				Bitmap bmap;
				
				// Setup background spotify logo
				is = getResources().openRawResource(R.drawable.spot_bg);
				int containerNewWidth = (TRAY_CROP_FRACTION-1)* mLogoView.getHeight()/TRAY_CROP_FRACTION;
				bmap = Utils.loadMaskedBitmap(is, mLogoView.getHeight(), containerNewWidth);
				params = (RelativeLayout.LayoutParams) mLogoView.getLayoutParams();
				params.width = (bmap.getWidth() * mLogoView.getHeight()) / bmap.getHeight();
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
				mLogoView.setLayoutParams(params);
				mLogoView.requestLayout();
				mLogoView.setImageDrawable(new BitmapDrawable(getResources(), bmap));
				

			/*	params = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						Utils.dpToPixels(BUTTONS_DIM_Y_DP, getResources()));
				params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.tray_opener);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.leftMargin = mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION;
				mRootLayout.updateViewLayout(mPlayerButtonsLayout, params);
				
				// setup song info views
				params = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.tray_opener);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				int marg = Utils.dpToPixels(5, getResources());
				params.setMargins(
						marg/2 + mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION, 
						marg, 
						marg*3, 
						marg);
				*/
				// Setup the root layout
				mRootLayoutParams.x = -mLogoView.getLayoutParams().width;
				mRootLayoutParams.y = (getApplicationContext().getResources().getDisplayMetrics().heightPixels-mRootLayout.getHeight()) / 2;
				mWindowManager.updateViewLayout(mRootLayout, mRootLayoutParams);
				
				// Make everything visible
				mRootLayout.setVisibility(View.VISIBLE);
				
				// Animate the Tray
				mTrayTimerTask = new TrayAnimationTimerTask();
				mTrayAnimationTimer = new Timer();
				mTrayAnimationTimer.schedule(mTrayTimerTask, 0, ANIMATION_FRAME_RATE);
			}
		}, ANIMATION_FRAME_RATE);
	}

	// The phone orientation has changed. Update the widget's position.
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mIsTrayOpen)
			mRootLayoutParams.x = -mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION;
		else
			mRootLayoutParams.x = -mLogoView.getWidth();
		mRootLayoutParams.y = (getResources().getDisplayMetrics().heightPixels-mRootLayout.getHeight()) / 2;
		mWindowManager.updateViewLayout(mRootLayout, mRootLayoutParams);

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		if (intent.getBooleanExtra("stop_service", false)){
			// If it's a call from the notification, stop the service.
			stopSelf();
		}else{
			// Make the service run in foreground so that the system does not shut it down.
			Intent notificationIntent = new Intent(this, AdService.class);
			notificationIntent.putExtra("stop_service", true);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
			Notification notification = new Notification(
					R.mipmap.ic_launcher,
					"Spotify tray launched",
			        System.currentTimeMillis());
			 notification.setLatestEventInfo(
					this, 
					"Spotify tray",
			        "Tap to close the widget.", 
			        pendingIntent);
			startForeground(86, notification);
		}
		return START_STICKY;
	}

	// The app is closing.
	@Override
	public void onDestroy() {
		if (mRootLayout != null)
			mWindowManager.removeView(mRootLayout);
	}

	// Drags the tray as per touch info


	// Listens to the touch events on the tray.
	private class TrayTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			final int action = event.getActionMasked();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				// Filter and redirect the events to dragTray()

				break;
			default:
				return false;
			}
			return true;

		}
	}
	
	// Timer for animation/automatic movement of the tray.
	private class TrayAnimationTimerTask extends TimerTask {
		
		// Ultimate destination coordinates toward which the tray will move
		int mDestX;
		int mDestY;
		
		public TrayAnimationTimerTask(){
			
			// Setup destination coordinates based on the tray state. 
			super();
			if (!mIsTrayOpen){
				mDestX = -mLogoView.getWidth();
			}else{
				mDestX = -mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION;
			}
			
			// Keep upper edge of the widget within the upper limit of screen
			int screenHeight = getResources().getDisplayMetrics().heightPixels;
			mDestY = Math.max(
					screenHeight/TRAY_MOVEMENT_REGION_FRACTION, 
					mRootLayoutParams.y);
			
			// Keep lower edge of the widget within the lower limit of screen
			mDestY = Math.min(
					((TRAY_MOVEMENT_REGION_FRACTION-1)*screenHeight)/TRAY_MOVEMENT_REGION_FRACTION - mRootLayout.getWidth(), 
					mDestY);
		}
		
		// This function is called after every frame.
		@Override
		public void run() {
			
			// handler is used to run the function on main UI thread in order to
			// access the layouts and UI elements.
			mAnimationHandler.post(new Runnable() {
				@Override
				public void run() {
					
					// Update coordinates of the tray
					mRootLayoutParams.x = (2*(mRootLayoutParams.x-mDestX))/3 + mDestX;
					mRootLayoutParams.y = (2*(mRootLayoutParams.y-mDestY))/3 + mDestY;
					mWindowManager.updateViewLayout(mRootLayout, mRootLayoutParams);

					// Cancel animation when the destination is reached
					if (Math.abs(mRootLayoutParams.x-mDestX)<2 && Math.abs(mRootLayoutParams.y-mDestY)<2){
						TrayAnimationTimerTask.this.cancel();
						mTrayAnimationTimer.cancel();
					}
				}
			});
		}
	}
	

}
