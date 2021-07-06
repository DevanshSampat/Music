package com.devansh.music;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneIncomingCallListener extends PhoneStateListener {
    private Context context;
    public PhoneIncomingCallListener(Context context){this.context = context;}
    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        Log.println(Log.ASSERT,"call",state+"");
        if(state == TelephonyManager.CALL_STATE_IDLE) context.sendBroadcast(new Intent("PLAY"));
        else if(state==TelephonyManager.CALL_STATE_RINGING||state==TelephonyManager.CALL_STATE_OFFHOOK) context.sendBroadcast(new Intent("PAUSE"));
    }
}
