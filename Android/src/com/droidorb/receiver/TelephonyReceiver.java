package com.droidorb.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.droidorb.DroidOrbService;
import com.droidorb.observer.OnRingingListener;

/**
 * BroadcastReceiver to check when there is an incoming call or outgoing call
 * @author tobykurien
 *
 */
public class TelephonyReceiver extends BroadcastReceiver {
   private OnRingingListener listener;
   private boolean isRinging = false;

   @Override
   public void onReceive(Context context, Intent intent) {
      listener = DroidOrbService.ringListener;

      if (intent.getAction() == Intent.ACTION_NEW_OUTGOING_CALL) {
         // outgoing call         
      } else if (intent.getAction() == TelephonyManager.ACTION_PHONE_STATE_CHANGED
               && intent.getExtras().get(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
         // incoming call
         String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
         isRinging = true;
         if (listener != null) listener.onRinging(number);
      } else if (isRinging 
               && intent.getAction() == TelephonyManager.ACTION_PHONE_STATE_CHANGED
               && intent.getExtras().get(TelephonyManager.EXTRA_STATE) != TelephonyManager.EXTRA_STATE_RINGING) {
         isRinging = false;
         if (listener != null) listener.onStoppedRinging();
      }
   }   
}
