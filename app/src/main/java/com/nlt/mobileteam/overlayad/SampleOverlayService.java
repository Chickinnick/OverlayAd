package com.nlt.mobileteam.overlayad;

/*
Copyright 2011 jawsware international

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

public class SampleOverlayService extends OverlayService {

	public static SampleOverlayService instance;

	private SampleOverlayView overlayView;
	private UserPresentReceiver userPresentReceiver;
	private IntentFilter intentFilter;

	@Override
	public void onCreate() {
		super.onCreate();
		userPresentReceiver = new UserPresentReceiver();
		userPresentReceiver.setAdService(this);
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_USER_PRESENT);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(userPresentReceiver, intentFilter);

		instance = this;
		
		overlayView = new SampleOverlayView(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (overlayView != null) {
			overlayView.destory();
		}

	}
	
	static public void stop() {
		if (instance != null) {
			instance.stopSelf();
		}
	}
	
	@Override
	protected Notification foregroundNotification(int notificationId) {
		Notification notification;

		notification = new Notification(R.mipmap.ic_launcher, getString(R.string.app_name), System.currentTimeMillis());

		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;

		notification.setLatestEventInfo(this, getString(R.string.notif_title), getString(R.string.notif_subtitle), notificationIntent());

		return notification;
	}


	private PendingIntent notificationIntent() {
		Intent intent = new Intent(this,  MainActivity.class);

		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		return pending;
	}

	public void hideView() {
		overlayView.setVisibility(View.GONE);
	}

	public void showView() {
		overlayView.setVisibility(View.VISIBLE);

	}
}
