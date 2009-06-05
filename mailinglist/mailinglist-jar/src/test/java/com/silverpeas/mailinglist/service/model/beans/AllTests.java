package com.silverpeas.mailinglist.service.model.beans;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.mailinglist.service.model.beans");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestExternalUser.class);
    suite.addTestSuite(TestMessage.class);
    //$JUnit-END$
    return suite;
  }

}
