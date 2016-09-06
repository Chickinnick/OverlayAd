package com.nlt.mobileteam.overlayad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UserPresentReceiver extends BroadcastReceiver {


    private AdService adService;

    public UserPresentReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            if (null != adService) {
                adService.hideView();
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (null != adService) {
                adService.showView();
            }
        }
    }

    void setAdService(AdService adService) {
        this.adService = adService;
    }
}
