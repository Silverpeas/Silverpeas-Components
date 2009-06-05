package com.silverpeas.mailinglist.service;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.mailinglist.service");
    //$JUnit-BEGIN$
    suite.addTest(com.silverpeas.mailinglist.service.model.AllTests.suite());
    suite.addTest(com.silverpeas.mailinglist.service.util.AllTests.suite());
    suite.addTest(com.silverpeas.mailinglist.service.job.AllTests.suite());
    suite.addTest(com.silverpeas.mailinglist.service.notification.AllTests.suite());
    suite.addTestSuite(TestServiceFactory.class);
    //$JUnit-END$
    return suite;
  }

}
