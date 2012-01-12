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
   public static Uri GMAIL_URI = Uri.parse("content://gmail-ls/");
   public static Uri EMAIL_URI = Uri.parse("com.android.email.provider");

   private Context context;
   private int lastUnreadCount = 0;
   private OnUnreadEmailListener listner;
   
   public EmailContentObserver(Context context, OnUnreadEmailListener listner) {
      super(null);
      this.context = context;
      this.listner = listner;
   }

   public void start() {
      context.getContentResolver().registerContentObserver(GMAIL_URI, true, this);      
      context.getContentResolver().registerContentObserver(EMAIL_URI, true, this);      
      if (Debug.MISSED_EMAIL)  Log.d(Main.LOG_TAG, "Email content observers registered");
   }

   public void stop() {
      context.getContentResolver().unregisterContentObserver(this);      
   }
   
   @Override
   public void onChange(boolean selfChange) {
      if (Debug.MISSED_EMAIL)  Log.d(Main.LOG_TAG, "Got mail notification");
      
      // TODO - how do we get the e-mail counts for the GMail and EMail clients?
//      Cursor cursor = context.getContentResolver().query(GMAIL_URI,
//               null,
//               null,
//               null,
//               null);
//
//      // check if the number of missed calls has changed
//      int unreadCount = cursor.getCount();
//      if (Debug.MISSED_EMAIL) Log.d(Main.LOG_TAG, "Cursor count = " + unreadCount);
//      if (unreadCount > lastUnreadCount) {
//         lastUnreadCount = unreadCount;
//         listner.onUnreadEmail(unreadCount);
//      }
   }
   
   
}
