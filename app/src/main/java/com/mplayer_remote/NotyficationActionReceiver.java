package com.mplayer_remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;

public class NotyficationActionReceiver extends BroadcastReceiver {
    public NotyficationActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent sendBroadcastToConnectAndPlayServiceIntent = new Intent("ButtonActionInNotyficationClicked");

        Vibrator mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (action.equals("previous")){
            sendBroadcastToConnectAndPlayServiceIntent.putExtra("command", "previous");
            mVibrator.vibrate(50);
            LocalBroadcastManager.getInstance(context).sendBroadcast(sendBroadcastToConnectAndPlayServiceIntent);
        }else if (action.equals("pause")){
            sendBroadcastToConnectAndPlayServiceIntent.putExtra("command", "pause");
            mVibrator.vibrate(50);
            LocalBroadcastManager.getInstance(context).sendBroadcast(sendBroadcastToConnectAndPlayServiceIntent);
        }else if (action.equals("next")){
            sendBroadcastToConnectAndPlayServiceIntent.putExtra("command", "next");
            mVibrator.vibrate(50);
            LocalBroadcastManager.getInstance(context).sendBroadcast(sendBroadcastToConnectAndPlayServiceIntent);
        }
    }
}
