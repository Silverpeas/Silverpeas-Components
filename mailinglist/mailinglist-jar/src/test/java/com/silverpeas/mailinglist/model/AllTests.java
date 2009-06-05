package com.silverpeas.mailinglist.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas.mailinglist.model");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestMailingListComponent.class);
    suite.addTestSuite(TestCheckSender.class);
    //$JUnit-END$
    return suite;
  }

}
