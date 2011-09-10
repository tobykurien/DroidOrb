package com.droidorb;

/**
 * Toggle various debug streams
 * @author toby
 */
public class Debug {
   public static final boolean ON = true;
   public static final boolean COMMS = ON && false; // debug DroidOrb TCP/IP comms
   public static final boolean MISSED_CALLS = ON && false; // debug missed calls feature
   public static final boolean RECEIVER = ON && false; // debug broadcast receiver
   public static final boolean SERVICE = ON && false; // debug droid orb service
}
