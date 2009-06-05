package com.silverpeas;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas");
    //$JUnit-BEGIN$
    suite.addTest(com.silverpeas.mailinglist.AllTests.suite());
    suite.addTest(com.silverpeas.external.mailinglist.servlets.AllTests.suite());
    //$JUnit-END$
    return suite;
  }

}
