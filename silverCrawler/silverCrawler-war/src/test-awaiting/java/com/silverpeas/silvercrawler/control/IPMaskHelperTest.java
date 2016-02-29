package com.silverpeas.silvercrawler.control;

import org.junit.Assert;
import org.junit.Test;

public class IPMaskHelperTest {

  @Test
  public void testIsIPElligible() {
    String ipMask = "192.168.0.1/24,0:0:0:0:0:0:0:1/112";

    Assert.assertTrue(IPMaskHelper.isIPElligible("192.168.0.1", ipMask));
    Assert.assertTrue(IPMaskHelper.isIPElligible("192.168.0.48", ipMask));
    Assert.assertTrue(IPMaskHelper.isIPElligible("192.168.0.255", ipMask));
    Assert.assertFalse(IPMaskHelper.isIPElligible("192.168.1.255", ipMask));

    Assert.assertTrue(IPMaskHelper.isIPElligible("0:0:0:0:0:0:0:1", ipMask));
    Assert.assertTrue(IPMaskHelper.isIPElligible("0:0:0:0:0:0:0:E0", ipMask));
    Assert.assertFalse(IPMaskHelper.isIPElligible("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789", ipMask));
  }
}
