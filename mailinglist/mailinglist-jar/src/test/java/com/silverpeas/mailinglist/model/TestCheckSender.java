/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.mailinglist.model;

import com.silverpeas.mailinglist.AbstractMailingListTest;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import java.io.IOException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;

import static org.junit.Assert.*;

public class TestCheckSender extends AbstractMailingListTest {

  private static final String ArchivageNotModeratedOpen_ID = "101";
  private static final String ArchivageNotModeratedClosed_ID = "102";

  @After
  public void onTearDown() throws Exception {
    Mailbox.clearAll();
  }

  @Before
  public void onSetUp() {
    Mailbox.clearAll();
  }

  @Override
  protected IDataSet getDataSet() throws DataSetException, IOException {
    FlatXmlDataSet dataSet = new FlatXmlDataSetBuilder().build(TestCheckSender.class.
        getResourceAsStream("test-check-sender-dataset.xml"));;
    return dataSet;
  }

  @Test
  public void testArchivageNotModeratedOpen() {
    String email = "maggie.simpson@silverpeas.com";
    MailingList list = ServicesFactory.getFactory().getMailingListService().findMailingList(
        ArchivageNotModeratedOpen_ID);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertTrue(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non modérée et ouverte avec un lecteur abonné", list.getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = list.getReaders().iterator().next();
    assertEquals(email, reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(0, list.getModerators().size());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    MailingListComponent component = new MailingListComponent(
        ArchivageNotModeratedOpen_ID);
    assertTrue(component.checkSender(email));
    assertTrue(list.isEmailAuthorized(email));
  }

  @Test
  public void testArchivageNotModeratedClosed() {
    String email = "lisa.simpson@silverpeas.com";
    String spammer = "joe.theplumber@spam.com";
    MailingList list = ServicesFactory.getFactory().getMailingListService().findMailingList(
        ArchivageNotModeratedClosed_ID);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertFalse(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non modérée et fermée avec un lecteur abonné", list.getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    assertNotNull(list.getModerators());
    assertEquals(0, list.getModerators().size());
    InternalUser reader = list.getReaders().iterator().next();
    assertEquals(email, reader.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    MailingListComponent component = new MailingListComponent(
        ArchivageNotModeratedClosed_ID);
    assertTrue(component.checkSender(email));
    assertTrue(list.isEmailAuthorized(email));
    assertFalse(component.checkSender(spammer));
    assertFalse(list.isEmailAuthorized(spammer));
  }

  @Override
  protected String[] getContextConfigurations() {
    return new String[]{"/spring-checker.xml", "/spring-notification.xml",
      "/spring-mailinglist-services-factory.xml", "/spring-mailinglist-dao.xml",
      "/spring-mailinglist-embbed-datasource.xml"};
  }
}
