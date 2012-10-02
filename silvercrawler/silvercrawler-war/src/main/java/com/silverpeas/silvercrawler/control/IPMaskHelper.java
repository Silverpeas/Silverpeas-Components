package com.silverpeas.silvercrawler.control;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

public class IPMaskHelper {
	public static boolean isIPElligible(String ipAddress, String networkMasks) {
		String[] masks = networkMasks.split(",");
		
		for (String mask : masks) {
			if (isIPInMask(ipAddress, mask)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean isIPInMask(String ipAddress, String mask) {
		SubnetInfo subnet = (new SubnetUtils(mask)).getInfo();
		return subnet.isInRange(ipAddress);
	}
}
