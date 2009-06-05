package com.silverpeas.mailinglist.service.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.mailinglist.service.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestCryptMd5.class);
    //$JUnit-END$
    return suite;
  }

}
