package com.droidorb;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Quickly set and retrieve app settings programatically
 * 
 * @author tobykurien
 * 
 */
public class Settings {
   private Context context;
   private SharedPreferences prefs;

   private Settings(Context context) {
      this.context = context;
      prefs = PreferenceManager.getDefaultSharedPreferences(context);
   }

   public static Settings getInstance(Context context) {
      return new Settings(context);
   }

   public boolean isEnabled() {
      return prefs.getBoolean("enabled", true);
   }

   public boolean isAntiTheft() {
      return prefs.getBoolean("anti_theft_enabled", true);
   }

}
