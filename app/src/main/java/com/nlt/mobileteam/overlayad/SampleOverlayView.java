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

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class SampleOverlayView extends OverlayView {

	private WebView webView;
	
	public SampleOverlayView(OverlayService service) {
		super(service, R.layout.advertisment_layout, 1);
	}

	public int getGravity() {
		return Gravity.TOP + Gravity.RIGHT;
	}
	
	@Override
	protected void onInflateView() {
		webView = (WebView) this.findViewById(R.id.cover_layout);
		webView.loadUrl("http://www.metrolyrics.com/top100.html");
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
	}
/*

	@Override
	protected void refreshViews() {
		webView.setText("WAITING\nWAITING");
	}
*/

/*	@Override
	protected void onTouchEvent_Up(MotionEvent event) {
		webView.setText("UP\nPOINTERS: " + event.getPointerCount());
	}

	@Override
	protected void onTouchEvent_Move(MotionEvent event) {
		webView.setText("MOVE\nPOINTERS: " + event.getPointerCount());
	}

	@Override
	protected void onTouchEvent_Press(MotionEvent event) {
		webView.setText("DOWN\nPOINTERS: " + event.getPointerCount());
	}

	@Override
	public boolean onTouchEvent_LongPress() {
		webView.setText("LONG\nPRESS");

		return true;
	}*/
	
	
}
