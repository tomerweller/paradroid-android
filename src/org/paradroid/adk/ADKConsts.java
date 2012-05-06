package org.paradroid.adk;

public class ADKConsts {
	
	//outgoing

	//leds
	public static final byte READY_TO_DROP_MSG = 0x10; 
	
	//servos
	public static final byte SERV_RIGHT_MSG = 0x22;
	public static final byte SERV_LEFT_MSG = 0x23;
	public static final byte PULL_RIGHT_MSG = 0x24;
	public static final byte PULL_LEFT_MSG = 0x25; 
	public static final byte FLARE_MSG = 0x26;
	public static final byte RESET_MSG = 0x27; 
	public static final byte PERM_FLARE_MSG = 0x28; 
	
	//incoming
	public static final byte RANGE_FINDER_MSG = 0x31;
	public static final byte PHOTO_SENSOR_MSG = 0x32; 
	

}
