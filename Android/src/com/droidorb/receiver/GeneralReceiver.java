package com.droidorb.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.util.Log;

import com.droidorb.Debug;
import com.droidorb.DroidOrbService;
import com.droidorb.Main;
import com.droidorb.Settings;

/**
 * General broadcast receiver, which also starts the DroidOrb service when power
 * is connected
 * 
 * @author tobykurien
 */
public class GeneralReceiver extends BroadcastReceiver {
   private static final int REQUEST_ANTI_THEFT = 1234;
   private static final int TIMEOUT_ANTI_THEFT = 1000*5;
   private static final String INTENT_ACTION_ANTI_THEFT = "com.droidorb.intent.action.ANTI_THEFT_CHECK";
   
   private static boolean serviceRunning = false;
   private static boolean isTheftTimeout = false;
   private static boolean isUnlocked = false;

   @Override
   public void onReceive(Context context, Intent intent) {
      if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "General Receiver got: " + intent.getAction());
      if (!Settings.getInstance(context).isEnabled()) return;

      if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Power connected, starting activity");

         // start service
         Intent i = new Intent(context, DroidOrbService.class);
         context.startService(i);
         serviceRunning = true;
         
         // testing
         // activate anti-theft from power disconnect
         // TODO - check that DroidOrb accessory is connected first
         isTheftTimeout = true;
         isUnlocked = false;
      } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Power disconnected");
         if (serviceRunning && Settings.getInstance(context).isAntiTheft()) {
            // activate anti-theft from power disconnect
            // TODO - check that DroidOrb accessory is connected first
            isTheftTimeout = true;
            isUnlocked = false;

            // start timer for anti-theft timeout
            Intent i = new Intent(context, GeneralReceiver.class);
            i.setAction(INTENT_ACTION_ANTI_THEFT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_ANTI_THEFT, i, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIMEOUT_ANTI_THEFT, pendingIntent);
         } else {
            stopService(context);            
         }
      } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Screen unlocked");
         // screen unlocked
         isUnlocked = true;
      } else if (intent.getAction().equals(INTENT_ACTION_ANTI_THEFT)) {
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Checking Anti-theft timeout");
         stopService(context);
         
         // anti=theft timeout check
         if (!isUnlocked) {
            isTheftTimeout = false;
            // sound alarm
            if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Anti theft alarm activated");
            // TODO - sound alarm
         }
      }
   }

   private void stopService(Context context) {
      // stop service
      Intent i = new Intent(context, DroidOrbService.class);
      context.stopService(i);
      serviceRunning = false;
   }

}
