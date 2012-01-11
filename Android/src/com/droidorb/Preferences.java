package com.droidorb;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Preferences extends PreferenceActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
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
   
}
