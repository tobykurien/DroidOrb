package com.droidorb.observer;

/**
 * Interface to implement for callback when phone rings
 * 
 * @author tobykurien
 * 
 */
public interface OnRingingListener {
   public void onRinging(String number);
   public void onStoppedRinging();
}
