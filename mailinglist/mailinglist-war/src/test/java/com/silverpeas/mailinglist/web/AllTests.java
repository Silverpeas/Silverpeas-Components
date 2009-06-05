package com.silverpeas.mailinglist.web;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas.mailinglist.web");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestMailingListModeration.class);
    suite.addTestSuite(TestMailingListActivity.class);
    suite.addTestSuite(TestMailingListMessages.class);
    suite.addTestSuite(TestMailingListSimpleMessage.class);
    suite.addTestSuite(TestMailingListExternalUsers.class);
    //$JUnit-END$
    return suite;
  }

}
