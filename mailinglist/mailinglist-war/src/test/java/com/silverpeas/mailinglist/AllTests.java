package com.silverpeas.mailinglist;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas.mailinglist");
    //$JUnit-BEGIN$
    suite.addTest(com.silverpeas.mailinglist.model.AllTests.suite());
    suite.addTest(com.silverpeas.mailinglist.service.AllTests.suite());
    //$JUnit-END$
    return suite;
  }

}
