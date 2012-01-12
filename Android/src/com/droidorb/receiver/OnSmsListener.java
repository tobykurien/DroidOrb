package com.droidorb.receiver;

/**
 * Callback interface for unread Sms notification
 * @author tobykurien
 *
 */
public interface OnSmsListener {

   public void onUnreadSms(int count);
   
}
