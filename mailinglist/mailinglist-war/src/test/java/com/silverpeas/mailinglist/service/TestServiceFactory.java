package com.silverpeas.mailinglist.service;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.silverpeas.mailinglist.service.job.MessageChecker;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.MessageService;

public class TestServiceFactory extends
    AbstractDependencyInjectionSpringContextTests {
  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml",
        "spring-notification.xml", "spring-hibernate.xml",
        "spring-datasource.xml" };
  }

  public void testGetMessageService() {
    MessageService service = ServicesFactory.getMessageService();
    assertNotNull(service);
  }

  public void testGetMailingListService() {
    MailingListService service = ServicesFactory.getMailingListService();
    assertNotNull(service);
  }

  public void testGetMessageChecker() {
    MessageChecker checker = ServicesFactory.getMessageChecker();
    assertNotNull(checker);
  }
}
