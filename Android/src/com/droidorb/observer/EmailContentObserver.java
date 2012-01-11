package com.droidorb.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.util.Log;

import com.droidorb.Debug;
import com.droidorb.Main;

public class EmailContentObserver extends ContentObserver {
   Uri GMAIL_URI = Uri.parse("content://gmail-ls/");

   private Context context;
   private int lastUnreadCount = 0;
   private OnMissedCallListener listner;
   
   public EmailContentObserver(Context context, OnMissedCallListener listner) {
      super(null);
      this.context = context;
      this.listner = listner;
   }

   public void start() {
      context.getContentResolver().registerContentObserver(GMAIL_URI, true, this);      
   }

   public void stop() {
      context.getContentResolver().unregisterContentObserver(this);      
   }
   
   @Override
   public void onChange(boolean selfChange) {
      if (Debug.MISSED_EMAIL)  Log.d(Main.LOG_TAG, "Got gmail notification");
      
//      Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI,
//               null,
//               Calls.TYPE + " = ? AND " + Calls.NEW + " = ?",
//               new String[] { Integer.toString(Calls.MISSED_TYPE), "1" },
//               Calls.DATE + " DESC ");
//
//      // check if the number of missed calls has changed
//      int unreadCount = cursor.getCount();
//      if (Debug.MISSED_EMAIL) Log.d(Main.LOG_TAG, "Cursor count = " + unreadCount);
//      if (unreadCount > lastUnreadCount) {
//         lastUnreadCount = unreadCount;
//         listner.onMissedCall(unreadCount);
//      }
   }
   
   
}
