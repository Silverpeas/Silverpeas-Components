package com.silverpeas.silvercrawler.control;

import com.silverpeas.silvercrawler.util.IpAddressMatcher;


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
    IpAddressMatcher matcher = new IpAddressMatcher(mask);
    return matcher.matches(ipAddress);
  }

}
