package sporemodder.files.formats.spui;

import sporemodder.files.formats.spui.SPUIResource.RESOURCE_TYPE;

public class SPUIChannel {
	//IMAGE_RESOURCE(0x01BE0001, DISPLAY_TYPE.DEFAULT);
	
	private static final SPUIChannel[] channels = {
		new SPUIChannel(0x01BE0001, DISPLAY_TYPE.DEFAULT, RESOURCE_TYPE.ATLAS),
		new SPUIChannel(0xEEC1B004, DISPLAY_TYPE.HEX), // tint
		};
	
	protected static enum DISPLAY_TYPE {DEFAULT, HEX, DECIMAL};
	
	protected int channel;
	protected DISPLAY_TYPE displayType = DISPLAY_TYPE.DEFAULT;
	protected RESOURCE_TYPE resourceType = null;
	
	private SPUIChannel(int channel, DISPLAY_TYPE displayType) {
		this.channel = channel;
		this.displayType = displayType;
	}
	
	private SPUIChannel(int channel, DISPLAY_TYPE displayType, RESOURCE_TYPE resourceType) {
		this.channel = channel;
		this.displayType = displayType;
		this.resourceType = resourceType;
	}
	
	public static SPUIChannel getChannel(int channel) {
		for (SPUIChannel c : channels) {
			if (c.channel == channel) {
				return c;
			}
		}
		return null;
	}
}
