/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.silverpeas.mailinglist.service.job.MessageChecker;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.MessageService;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestServiceFactory {

  private static ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void loadSpringContext() {
    context = new ClassPathXmlApplicationContext(new String[]{"/spring-checker.xml",
      "/spring-notification.xml", "/spring-hibernate.xml", "/spring-datasource.xml"});
  }

  @AfterClass
  public static void unloadSpringContext() {
    context.close();
  }

  @Test
  public void testGetMessageService() {
    MessageService service = ServicesFactory.getMessageService();
    assertThat(service, is(notNullValue()));
  }

  @Test
  public void testGetMailingListService() {
    MailingListService service = ServicesFactory.getMailingListService();
    assertThat(service, is(notNullValue()));
  }

  @Test
  public void testGetMessageChecker() {
    MessageChecker checker = ServicesFactory.getMessageChecker();
    assertThat(checker, is(notNullValue()));
  }
}
