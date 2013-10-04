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
package com.silverpeas.external.mailinglist.servlets;

import com.silverpeas.mailinglist.AbstractMailingListTest;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.MailingListActivity;
import com.silverpeas.mailinglist.service.util.OrderBy;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

public class TestActivitiesProcessor extends AbstractMailingListTest {

  @Test
  public void testProcessActivities() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/Rmailinglist/mailingList45/Main");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/Main");
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    MailingList list = servicesFactory.getMailingListService().findMailingList(
        "mailinglist45");
    assertNotNull(list);
    MailingListActivity mailingListActivity = servicesFactory.getMessageService().getActivity(list);
    assertNotNull(mailingListActivity);
    String url = ActivitiesProcessor.processActivities(rest, request, "7");
    MailingListActivity activity = (MailingListActivity) request.getAttribute(
        MailingListRoutage.ACTIVITY_LIST_ATT);
    assertNotNull(activity);
    assertFalse(activity.getMessages().isEmpty());
    Map activities = (Map) request.getAttribute(MailingListRoutage.ACTIVITY_MAP_ATT);
    assertNotNull(activities);
    assertFalse(activities.isEmpty());
    List years = (List) request.getAttribute(MailingListRoutage.ACTIVITY_YEARS_ATT);
    assertNotNull(years);
    assertFalse(years.isEmpty());
    assertEquals(2, years.size());
    assertTrue(years.contains("2007"));
    assertTrue(years.contains("2008"));
    Map months = (Map) activities.get("2007");
    assertNotNull(months);
    assertEquals("2", months.get("" + Calendar.NOVEMBER));
    assertEquals("2", months.get("" + Calendar.DECEMBER));
    months = (Map) activities.get("2008");
    assertNotNull(months);
    assertEquals("3", months.get("" + Calendar.MARCH));
    assertEquals("3", months.get("" + Calendar.FEBRUARY));
    assertEquals("1", months.get("" + Calendar.JANUARY));
    assertNotNull(url);
    assertEquals("/mailingList/jsp/activity.jsp", url);
  }

  @Test
  public void testProcessMailingList() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailinglist45/Main");
    request.setRequestURI("/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45");
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailingList45");
    MailingList list = servicesFactory.getMailingListService().findMailingList(
        "mailinglist45");
    assertNotNull(list);
    MailingListActivity mailingListActivity = servicesFactory.getMessageService().getActivity(list);
    assertNotNull(mailingListActivity);
    String url = MailingListProcessor.processMailingList(rest, request);
    assertEquals(10,
        servicesFactory.getMessageService().listDisplayableMessages(list, -1,
        -1, 0, new OrderBy("sentDate", true)).size());
    List messages = (List) request.getAttribute(MailingListRoutage.MESSAGES_LIST_ATT);
    assertFalse(messages.isEmpty());
    assertEquals(10, messages.size());
    Integer nbPages = (Integer) request.getAttribute(MailingListRoutage.NB_PAGE_ATT);
    assertNotNull(nbPages);
    int pages = servicesFactory.getMessageService().getNumberOfPagesForDisplayableMessages(list);
    assertEquals(2, pages);
    assertNotNull(url);
    assertEquals("/mailingList/jsp/list.jsp", url);
  }

  @Test
  public void testProcessModeration() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailinglist45/moderationList/mailinglist45");
    request.setRequestURI("/silverpeas/Rmailinglist/mailinglist45/moderationList/mailinglist45");
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailingList45");
    MailingList list = servicesFactory.getMailingListService().findMailingList(
        "mailinglist45");
    assertNotNull(list);
    MailingListActivity mailingListActivity = servicesFactory.getMessageService().getActivity(list);
    assertNotNull(mailingListActivity);
    String url = ModerationProcessor.processModeration(rest, request);
    assertEquals(2, servicesFactory.getMessageService().listUnmoderatedeMessages(list, 0,
        new OrderBy("sentDate", true)).size());
    List messages = (List) request.getAttribute(MailingListRoutage.MESSAGES_LIST_ATT);
    assertFalse(messages.isEmpty());
    assertEquals(2, messages.size());
    Integer nbPages = (Integer) request.getAttribute(MailingListRoutage.NB_PAGE_ATT);
    assertNotNull(nbPages);
    int pages = servicesFactory.getMessageService().getNumberOfPagesForUnmoderatedMessages(list);
    assertEquals(1, pages);
    assertEquals(1, nbPages.intValue());
    assertEquals("/mailingList/jsp/moderation.jsp", url);
  }

  @Test
  public void testProcessMessageDelete() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.FALSE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/delete");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/message/delete");
    request.addParameter("message", new String[]{"1", "2"});
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailingList45");
    assertEquals(RestRequest.DELETE, rest.getAction());
    MessageProcessor.processMessage(rest, request);
    assertNotNull(servicesFactory.getMessageService().getMessage("1"));
    assertNotNull(servicesFactory.getMessageService().getMessage("2"));

    request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setServletPath("/Rmailinglist");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.TRUE);
    request.setPathInfo("/Rmailinglist/mailinglist45/destination/list/message/delete");
    request.setRequestURI("/silverpeas/Rmailinglist/mailinglist45/destination/list/message/delete");
    request.addParameter("message", new String[]{"1", "2"});
    rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.DELETE, rest.getAction());
    String url = MessageProcessor.processMessage(rest, request);
    assertNull(servicesFactory.getMessageService().getMessage("1"));
    assertNull(servicesFactory.getMessageService().getMessage("2"));
    assertNotNull(url);
    assertEquals(
        "http://localhost:8000/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45",
        url);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/delete/");
    request.setRequestURI("/silverpeas/Rmailinglist/mailinglist45/message/delete/");
    request.addParameter("message", new String[]{"1", "2"});
    rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.DELETE, rest.getAction());
    url = MessageProcessor.processMessage(rest, request);
    assertNull(servicesFactory.getMessageService().getMessage("1"));
    assertNull(servicesFactory.getMessageService().getMessage("2"));
    assertNotNull(url);
    assertEquals(
        "http://localhost:8000/silverpeas/Rmailinglist/mailinglist45/list/mailinglist45",
        url);
    request.setPathInfo("/Rmailinglist/mailinglist45/destination/moderation/message/delete/");
    request.setRequestURI(
        "/silverpeas/Rmailinglist/mailinglist45/destination/moderation/message/delete/");
    request.addParameter("message", new String[]{"1", "2"});
    rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.DELETE, rest.getAction());
    url = MessageProcessor.processMessage(rest, request);
    assertNull(servicesFactory.getMessageService().getMessage("1"));
    assertNull(servicesFactory.getMessageService().getMessage("2"));
    assertNotNull(url);
    assertEquals(
        "http://localhost:8000/silverpeas/Rmailinglist/mailinglist45/moderationList/mailinglist45",
        url);
  }

  @Test
  public void testProcessMessageModerate() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.FALSE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/put");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/message/put");
    request.addParameter("message", new String[]{"12", "13"});
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailingList45");
    assertEquals(RestRequest.UPDATE, rest.getAction());
    MessageProcessor.processMessage(rest, request);
    assertFalse(servicesFactory.getMessageService().getMessage("12").isModerated());
    assertFalse(servicesFactory.getMessageService().getMessage("13").isModerated());

    request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setServletPath("/Rmailinglist");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.TRUE);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/put");
    request.setRequestURI("/silverpeas/Rmailinglist/mailinglist45/message/put");
    request.addParameter("message", new String[]{"12", "13"});
    rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.UPDATE, rest.getAction());
    String url = MessageProcessor.processMessage(rest, request);
    assertTrue(servicesFactory.getMessageService().getMessage("12").isModerated());
    assertTrue(servicesFactory.getMessageService().getMessage("13").isModerated());
    assertNotNull(url);
    assertEquals(
        "http://localhost:8000/silverpeas/Rmailinglist/mailinglist45/moderationList/mailinglist45",
        url);
  }

  @Test
  public void testProcessMessageSingleModerate() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.FALSE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/put");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/message/put");
    request.addParameter("message", new String[]{"12"});
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailingList45");
    assertEquals(RestRequest.UPDATE, rest.getAction());
    MessageProcessor.processMessage(rest, request);
    assertFalse(servicesFactory.getMessageService().getMessage("12").isModerated());
    assertFalse(servicesFactory.getMessageService().getMessage("13").isModerated());

    request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setServletPath("/Rmailinglist");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.TRUE);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/put");
    request.setRequestURI("/silverpeas/Rmailinglist/mailinglist45/message/put");
    request.addParameter("message", new String[]{"12"});
    rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.UPDATE, rest.getAction());
    String url = MessageProcessor.processMessage(rest, request);
    assertTrue(servicesFactory.getMessageService().getMessage("12").isModerated());
    assertFalse(servicesFactory.getMessageService().getMessage("13").isModerated());
    assertNotNull(url);
    /*
     * assertEquals(
     * "http://localhost:8000/silverpeas/Rmailinglist/mailinglist45/moderationList/mailinglist45",
     * url);
     */
    assertEquals(servicesFactory.getMessageService().getMessage("12"),
        request.getAttribute(MailingListRoutage.MESSAGE_ATT));
    assertEquals("/mailingList/jsp/message.jsp", url);
  }

  @Test
  public void testProcessMessage() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.FALSE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/message/1");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/message/1");
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.FIND, rest.getAction());
    String url = MessageProcessor.processMessage(rest, request);
    assertEquals(servicesFactory.getMessageService().getMessage("1"),
        request.getAttribute(MailingListRoutage.MESSAGE_ATT));
    assertEquals("/mailingList/jsp/message.jsp", url);
  }

  @Test
  public void testProcessUsers() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.TRUE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/users/");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/users/");
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.FIND, rest.getAction());
    String url = UsersProcessor.processUsers(rest, request);
    List users = (List) request.getAttribute(MailingListRoutage.USERS_LIST_ATT);
    assertNotNull(users);
    assertEquals(5, users.size());
    Set emails = new HashSet(5);
    Iterator iter = users.iterator();
    while (iter.hasNext()) {
      emails.add(((ExternalUser) iter.next()).getEmail());
    }
    assertTrue(emails.contains("barney.gumble@silverpeas.com"));
    assertTrue(emails.contains("julius.hibbert@silverpeas.com"));
    assertTrue(emails.contains("carl.carlson@silverpeas.com"));
    assertTrue(emails.contains("edna.krabappel@silverpeas.com"));
    assertTrue(emails.contains("nelson.muntz@silverpeas.com"));
    assertEquals("/mailingList/jsp/users.jsp", url);
  }

  @Test
  public void testProcessUsersDelete() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.TRUE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/users/delete");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/users/delete");
    MailingList mailingList = servicesFactory.getMailingListService().findMailingList(
        "mailinglist45");
    assertNotNull(mailingList);
    ExternalUser tempUser = new ExternalUser();
    tempUser.setComponentId("mailinglist45");
    tempUser.setEmail("nelson.muntz@silverpeas.com");
    assertTrue(mailingList.getExternalSubscribers().contains(tempUser));
    tempUser.setEmail("carl.carlson@silverpeas.com");
    assertTrue(mailingList.getExternalSubscribers().contains(tempUser));
    request.addParameter("users", new String[]{"carl.carlson@silverpeas.com",
      "toto@toto.fr", "nelson.muntz@silverpeas.com"});
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.DELETE, rest.getAction());
    String url = UsersProcessor.processUsers(rest, request);
    List users = (List) request.getAttribute(MailingListRoutage.USERS_LIST_ATT);
    assertNotNull(users);
    assertEquals(3, users.size());
    Set emails = new HashSet(3);
    Iterator iter = users.iterator();
    while (iter.hasNext()) {
      emails.add(((ExternalUser) iter.next()).getEmail());
    }
    assertTrue(emails.contains("barney.gumble@silverpeas.com"));
    assertTrue(emails.contains("julius.hibbert@silverpeas.com"));
    assertFalse(emails.contains("carl.carlson@silverpeas.com"));
    assertTrue(emails.contains("edna.krabappel@silverpeas.com"));
    assertFalse(emails.contains("nelson.muntz@silverpeas.com"));
    assertEquals("/mailingList/jsp/users.jsp", url);
  }

  @Test
  public void testProcessUsersUpdate() throws Exception {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setServerPort(8000);
    request.setScheme("http");
    request.setContextPath("/silverpeas");
    request.setAttribute(MailingListRoutage.IS_USER_ADMIN_ATT, Boolean.TRUE);
    request.setAttribute(MailingListRoutage.IS_USER_MODERATOR_ATT,
        Boolean.FALSE);
    request.setPathInfo("/Rmailinglist/mailinglist45/users");
    request.setRequestURI("/silverpeas/RmailingList/mailinglist45/users");
    MailingList mailingList = servicesFactory.getMailingListService().findMailingList(
        "mailinglist45");
    assertNotNull(mailingList);
    ExternalUser tempUser = new ExternalUser();
    tempUser.setComponentId("mailinglist45");
    tempUser.setEmail("krusty.theklown@silverpeas.com");
    assertFalse(mailingList.getExternalSubscribers().contains(tempUser));
    tempUser.setEmail("snowball@silverpeas.com");
    assertFalse(mailingList.getExternalSubscribers().contains(tempUser));
    request.addParameter("users", new String[]{"carl.carlson@silverpeas.com;"
      + "krusty.theklown@silverpeas.com;snowball@silverpeas.com"});
    RestRequest rest = new RestRequest(request);
    rest.setComponentId("mailinglist45");
    assertEquals(RestRequest.CREATE, rest.getAction());
    String url = UsersProcessor.processUsers(rest, request);
    List users = (List) request.getAttribute(MailingListRoutage.USERS_LIST_ATT);
    assertNotNull(users);
    assertEquals(7, users.size());
    Set emails = new HashSet(7);
    Iterator iter = users.iterator();
    while (iter.hasNext()) {
      emails.add(((ExternalUser) iter.next()).getEmail());
    }
    assertTrue(emails.contains("barney.gumble@silverpeas.com"));
    assertTrue(emails.contains("julius.hibbert@silverpeas.com"));
    assertTrue(emails.contains("carl.carlson@silverpeas.com"));
    assertTrue(emails.contains("edna.krabappel@silverpeas.com"));
    assertTrue(emails.contains("nelson.muntz@silverpeas.com"));
    assertTrue(emails.contains("snowball@silverpeas.com"));
    assertTrue(emails.contains("krusty.theklown@silverpeas.com"));
    assertEquals("/mailingList/jsp/users.jsp", url);
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet;
    dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        TestActivitiesProcessor.class.getResourceAsStream("test-mailinglist-messages-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @Override
  protected String[] getContextConfigurations() {
    return new String[]{"/spring-checker.xml", "/spring-notification.xml",
      "/spring-mailinglist-services-factory.xml", "/spring-mailinglist-personalization-dao.xml",
      "/spring-mailinglist-embbed-datasource.xml"};
  }
}
