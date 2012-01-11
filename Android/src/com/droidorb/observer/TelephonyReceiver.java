package com.droidorb.observer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.droidorb.DroidOrbActivity;
import com.droidorb.DroidOrbService;
import com.droidorb.Main;

/**
 * BroadcastReceiver to check when there is an incoming call or outgoing call
 * @author tobykurien
 *
 */
public class TelephonyReceiver extends BroadcastReceiver {
   public static DroidOrbService mBoundService;
   public static boolean mIsBound;
   
   private OnRingingListener listener;
   private boolean isRinging = false;

   @Override
   public void onReceive(Context context, Intent intent) {
      doBindService(context);
      listener = mBoundService.ringListener;

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
      
      doUnbindService(context);
   }
   
   private ServiceConnection mConnection = new ServiceConnection() {
      public void onServiceConnected(ComponentName className, IBinder service) {
         DroidOrbService.LocalBinder binder = (DroidOrbService.LocalBinder)service;
         mBoundService = binder.getService();
     }

      public void onServiceDisconnected(ComponentName className) {
          mBoundService = null;
      }
  };

  void doBindService(Context context) {
      // Establish a connection with the service.  We use an explicit
      // class name because we want a specific service implementation that
      // we know will be running in our own process (and thus won't be
      // supporting component replacement by other applications).
      context.bindService(new Intent(context, DroidOrbService.class), mConnection, Context.BIND_AUTO_CREATE);
      mIsBound = true;
  }

  void doUnbindService(Context context) {
      if (mIsBound) {
          // Detach our existing connection.
          context.unbindService(mConnection);
          mIsBound = false;
      }
  }
   
}
