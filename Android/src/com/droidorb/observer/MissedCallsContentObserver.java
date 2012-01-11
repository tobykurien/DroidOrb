package com.droidorb.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.util.Log;

import com.droidorb.Debug;
import com.droidorb.Main;

/**
 * Get notified of missed calls from the call log
 * @author toby
 */
public class MissedCallsContentObserver extends ContentObserver {
   private Context context;
   private int lastMissedCalls = 0;
   private OnMissedCallListener listner;
   
   public MissedCallsContentObserver(Context context, OnMissedCallListener listner) {
      super(null);
      this.context = context;
      this.listner = listner;
   }

   public void start() {
      context.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, this);      
   }

   public void stop() {
      context.getContentResolver().unregisterContentObserver(this);      
   }
   
   @Override
   public void onChange(boolean selfChange) {
      if (Debug.MISSED_CALLS)  Log.d(Main.LOG_TAG, "Got call notification");
      
      Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI,
               null,
               Calls.TYPE + " = ? AND " + Calls.NEW + " = ?",
               new String[] { Integer.toString(Calls.MISSED_TYPE), "1" },
               Calls.DATE + " DESC ");

      // check if the number of missed calls has changed
      int missedCalls = cursor.getCount();
      if (Debug.MISSED_CALLS) Log.d(Main.LOG_TAG, "Cursor count = " + missedCalls);
      if (missedCalls > lastMissedCalls) {
         lastMissedCalls = missedCalls;
         listner.onMissedCall(missedCalls);
      }
   }
}
