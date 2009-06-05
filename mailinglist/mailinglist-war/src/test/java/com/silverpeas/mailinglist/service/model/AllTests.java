package com.silverpeas.mailinglist.service.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.mailinglist.service.model");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestMessageService.class);
    suite.addTestSuite(TestMailingListService.class);
    suite.addTest(com.silverpeas.mailinglist.service.model.beans.AllTests.suite());
    suite.addTest(com.silverpeas.mailinglist.service.model.dao.AllTests.suite());
    //$JUnit-END$
    return suite;
  }

}
