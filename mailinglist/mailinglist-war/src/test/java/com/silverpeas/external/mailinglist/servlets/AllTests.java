package com.silverpeas.external.mailinglist.servlets;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.external.mailinglist.servlets");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestActivitiesProcessor.class);
    suite.addTestSuite(TestRestRequest.class);
    //$JUnit-END$
    return suite;
  }

}
