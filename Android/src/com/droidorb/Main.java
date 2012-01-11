package com.droidorb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * This will become the main activity for this app once it is working properly. 
 * @author toby
 */
public class Main extends Activity {
   public static final String LOG_TAG = "DroidOrb";
   public static DroidOrbService mBoundService;
   public static boolean mIsBound;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      doBindService();
   }

   @Override
   protected void onStart() {
      super.onStart();
   }
   
   @Override
   protected void onRestart() {
      super.onRestart();
      finish();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.menu_test:
            Intent i = new Intent(this, DroidOrbActivity.class);
            startActivity(i);
            break;
      }
      return true;
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

  void doBindService() {
      // Establish a connection with the service.  We use an explicit
      // class name because we want a specific service implementation that
      // we know will be running in our own process (and thus won't be
      // supporting component replacement by other applications).
      bindService(new Intent(Main.this, DroidOrbService.class), mConnection, Context.BIND_AUTO_CREATE);
      mIsBound = true;
      
      Intent i = new Intent(this, Preferences.class);
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(i);
  }

  void doUnbindService() {
      if (mIsBound) {
          // Detach our existing connection.
          unbindService(mConnection);
          mIsBound = false;
      }
  }

  @Override
  protected void onDestroy() {
      super.onDestroy();
      doUnbindService();
  }   
   
}
