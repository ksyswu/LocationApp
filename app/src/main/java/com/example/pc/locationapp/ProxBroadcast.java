package com.example.pc.locationapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ProxBroadcast extends BroadcastReceiver {
    private String mActionName;
    private Intent mLasReceivedIntent;

    public ProxBroadcast(){}

    public  ProxBroadcast(String actionName){
        mActionName = actionName;
        mLasReceivedIntent = null;
    }

    public IntentFilter getFilter(){
        IntentFilter filter = new IntentFilter(mActionName);
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null){
            mLasReceivedIntent = intent;

            int id = intent.getIntExtra("id", 0);
            double lat = intent.getDoubleExtra("latitude", 0.0);
            double lng = intent.getDoubleExtra("longitude", 0.0);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("근접경보");
            builder.setMessage("지금 현재 등록된 위치 근처에 도달 하였습니다.");
            builder.create().show();

        }
    }
}
