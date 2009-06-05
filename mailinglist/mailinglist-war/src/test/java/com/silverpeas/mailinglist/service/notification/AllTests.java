package com.silverpeas.mailinglist.service.notification;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas.mailinglist.model");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestNotificationHelper.class);
    suite.addTestSuite(TestNotificationFormatter.class);
    suite.addTestSuite(TestCheckNotification.class);
    //$JUnit-END$
    return suite;
  }

}
