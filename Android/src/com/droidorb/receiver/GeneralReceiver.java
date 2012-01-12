package com.droidorb.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
   private static final int TIMEOUT_ANTI_THEFT = 1000 * 5;
   private static final String INTENT_ACTION_ANTI_THEFT = "com.droidorb.intent.action.ANTI_THEFT_CHECK";

   private static boolean serviceRunning = false;
   private static boolean isTheftTimeout = false;
   private static boolean isUnlocked = false;
   private static MediaPlayer player;

   @Override
   public void onReceive(Context context, Intent intent) {
      if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "General Receiver got: " + intent.getAction());
      if (!Settings.getInstance(context).isEnabled()) return;

      if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Power connected, starting service");

         // start service
         Intent i = new Intent(context, DroidOrbService.class);
         context.startService(i);
         serviceRunning = true;
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
         if (player != null && player.isPlaying()) player.stop();
      } else if (intent.getAction().equals(INTENT_ACTION_ANTI_THEFT)) {
         if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Checking Anti-theft timeout");

         // anti=theft timeout check
         if (!isUnlocked) {
            isTheftTimeout = false;
            // sound alarm
            if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Anti theft alarm activated");
            soundAlarm(context);
         } else {
            // all good
            stopService(context);
         }
      }
   }

   /**
    * Stop the DroidOrb Service
    * 
    * @param context
    */
   private void stopService(Context context) {
      // stop service
      Intent i = new Intent(context, DroidOrbService.class);
      context.stopService(i);
      serviceRunning = false;
      if (Debug.RECEIVER) Log.d(Main.LOG_TAG, "Service stopped");
   }

   /**
    * Sound the anti-theft alarm
    * 
    * @param context
    */
   private void soundAlarm(Context context) {
      // turn up volume to full
      AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

      audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM),
               AudioManager.FLAG_PLAY_SOUND);
      audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
               AudioManager.FLAG_PLAY_SOUND);

      // play alarm sound file
      try {
         AssetFileDescriptor afd = context.getAssets().openFd("theft_alarm.mp3");
         player = new MediaPlayer();
         player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
         player.prepare();
         player.setLooping(true);
         player.start();
      } catch (Exception e) {
         // aaah crap, I guess this device gets stolen :-(
         Log.e(Main.LOG_TAG, "Error playing alarm", e);
      }
   }
}
