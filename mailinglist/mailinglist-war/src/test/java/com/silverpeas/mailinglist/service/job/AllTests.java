package com.silverpeas.mailinglist.service.job;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.mailinglist.service.job");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestMailProcessor.class);
    suite.addTestSuite(TestMessageCheckerWithStubs.class);
    suite.addTestSuite(TestMessageChecker.class);
    suite.addTestSuite(TestZimbraConnection.class);
    //$JUnit-END$
    return suite;
  }

}
