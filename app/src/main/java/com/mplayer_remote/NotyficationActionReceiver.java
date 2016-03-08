package com.mplayer_remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotyficationActionReceiver extends BroadcastReceiver {
    public NotyficationActionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent sendCommandToConnectAndPlayServiceIntent = new Intent(context, ConnectAndPlayService.class);

        if (action.equals("previous")){
            sendCommandToConnectAndPlayServiceIntent.putExtra("command","previous");
            context.startService(sendCommandToConnectAndPlayServiceIntent);
        }else if (action.equals("pause")){
            sendCommandToConnectAndPlayServiceIntent.putExtra("command","pause");
            context.startService(sendCommandToConnectAndPlayServiceIntent);
        }else if (action.equals("next")){
            sendCommandToConnectAndPlayServiceIntent.putExtra("command","next");
            context.startService(sendCommandToConnectAndPlayServiceIntent);
        }
    }
}
