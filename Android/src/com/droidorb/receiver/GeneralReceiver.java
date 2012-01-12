package com.droidorb.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidorb.Debug;
import com.droidorb.DroidOrbService;
import com.droidorb.Main;

public class GeneralReceiver extends BroadcastReceiver {
   

   @Override
   public void onReceive(Context context, Intent intent) {
      if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "General Receiver got: " + intent.getAction());
      
      if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {         
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Power connected, starting activity");
         // start service
         Intent i = new Intent(context, DroidOrbService.class);
         //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startService(i);
      }
   }

}
