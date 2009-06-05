package com.silverpeas.mailinglist.service.model.dao;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.mailinglist.service.model.dao");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestMessageDao.class);
    suite.addTestSuite(TestMailingListDao.class);
    //$JUnit-END$
    return suite;
  }

}
