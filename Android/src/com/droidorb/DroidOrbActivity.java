package com.droidorb;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.droidorb.observer.MissedCallsContentObserver;
import com.droidorb.observer.OnMissedCallListener;
import com.droidorb.server.Client;
import com.droidorb.server.Server;
import com.droidorb.server.ServerListener;

public class DroidOrbActivity extends Activity implements OnMissedCallListener {
   public static final int SERVER_PORT = 4567;
   Server server = null;
   MissedCallsContentObserver mcco = new MissedCallsContentObserver(this, this);

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      // Create TCP server
      server = new Server(SERVER_PORT);
      
      // start missed calls listener
      mcco.start();

      setupSeekbarListeners();
   }

   @Override
   protected void onStart() {
      super.onStart();
      try {
         final TextView t = (TextView) findViewById(R.id.status);
         server.start();
         server.addListener(new ServerListener() {

            @Override
            public void onServerStopped(Server server) {
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     t.setText("stopped");
                  }
               });
            }

            @Override
            public void onServerStarted(Server server) {
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     t.setText("started");
                  }
               });
            }

            @Override
            public void onReceive(Client client, final byte[] data) {
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     t.setText("data received " + data[0] + data[1]);
                  }
               });
            }

            @Override
            public void onClientDisconnect(Server server, Client client) {
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     t.setText("accessory disconnected");
                  }
               });
            }

            @Override
            public void onClientConnect(Server server, Client client) {
               runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     t.setText("accessory connected");
                  }
               });
            }
         });
      } catch (IOException e) {
         Log.e("DroidOrb", "Error starting server", e);
      }
   }

   @Override
   protected void onStop() {
      server.stop();
      mcco.stop();
      
      super.onStop();
   }
   
   /**
    * Add a listener so that the LED's update as the skee bar is dragged
    */
   private void setupSeekbarListeners() {
      OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
         @Override
         public void onStopTrackingTouch(SeekBar arg0) {
         }
         
         @Override
         public void onStartTrackingTouch(SeekBar arg0) {
         }
         
         @Override
         public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            onColoursClick(null);
         }
      };      
      
      ((SeekBar) findViewById(R.id.red)).setOnSeekBarChangeListener(listener);
      ((SeekBar) findViewById(R.id.green)).setOnSeekBarChangeListener(listener);
      ((SeekBar) findViewById(R.id.blue)).setOnSeekBarChangeListener(listener);
   }   

   public void onCommandClick(View v) {
      try {
         server.send("test");
      } catch (IOException e) {
         Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
         Log.e("DroidOrb", "Error sending command to accessory", e);
      }
   }
   
   public void onColoursClick(View v) {
      try {
         byte[] data = new byte[3];
          data[0] = (byte) ((ProgressBar) findViewById(R.id.red)).getProgress();
          data[1] = (byte) ((ProgressBar) findViewById(R.id.green)).getProgress();
          data[2] = (byte) ((ProgressBar) findViewById(R.id.blue)).getProgress();
         
         server.send(data);
      } catch (IOException e) {
         Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
         Log.e("DroidOrb", "Error sending command to accessory", e);
      }
   }
  
   @Override
   public void onMissedCall(int missedCalls) {
      if (Debug.MISSED_CALLS) Log.d(Main.LOG_TAG, "Got missed calls " + missedCalls);
      try {
         // light up blue when there is a missed call
         byte[] data = new byte[3];
          data[0] = (byte) 0;
          data[1] = (byte) 0;
          data[2] = (byte) 255;
         
         server.send(data);
      } catch (IOException e) {
         Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
         Log.e("DroidOrb", "Error sending command to accessory", e);
      }
   }
   
}