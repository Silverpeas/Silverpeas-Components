/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.silvercrawler.util;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Matches a request based on IP Address or subnet mask matching against the remote address.
 * <p>
 * Both IPv6 and IPv4 addresses are supported, but a matcher which is configured with an IPv4
 * address will never match a request which returns an IPv6 address, and vice-versa.
 * <p>
 * Implementation based on the org.springframework.security.web.util.matcher.IpAddressMatcher of
 * Spring Security.
 * @author Luke Taylor
 */
public class IpAddressMatcher {
  private final int nMaskBits;
  private final InetAddress requiredAddress;

  /**
   * Takes a specific IP address or a range specified using the
   * IP/Netmask (e.g. 192.168.1.0/24 or 202.24.0.0/14).
   * @param ipAddress the address or range of addresses from which the request must come.
   */
  public IpAddressMatcher(String ipAddress) {

    if (ipAddress.indexOf('/') > 0) {
      String[] addressAndMask = StringUtils.split(ipAddress, "/");
      ipAddress = addressAndMask[0];
      nMaskBits = Integer.parseInt(addressAndMask[1]);
    } else {
      nMaskBits = -1;
    }
    requiredAddress = parseAddress(ipAddress);
  }

  public boolean matches(String address) {
    InetAddress remoteAddress = parseAddress(address);

    if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
      return false;
    }

    if (nMaskBits < 0) {
      return remoteAddress.equals(requiredAddress);
    }

    byte[] remAddr = remoteAddress.getAddress();
    byte[] reqAddr = requiredAddress.getAddress();

    int oddBits = nMaskBits % 8;
    int nMaskBytes = nMaskBits / 8 + (oddBits == 0 ? 0 : 1);
    byte[] mask = new byte[nMaskBytes];

    Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte) 0xFF);

    if (oddBits != 0) {
      int finalByte = (1 << oddBits) - 1;
      finalByte <<= 8 - oddBits;
      mask[mask.length - 1] = (byte) finalByte;
    }

    for (int i = 0; i < mask.length; i++) {
      if ((remAddr[i] & mask[i]) != (reqAddr[i] & mask[i])) {
        return false;
      }
    }

    return true;
  }

  private InetAddress parseAddress(String address) {
    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Failed to parse address" + address, e);
    }
  }
}
