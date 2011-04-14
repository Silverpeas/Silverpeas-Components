/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.external.mailinglist.servlets;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

public class TestRestRequest extends TestCase implements MailingListRoutage{

  public void testNotRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/Main");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/Main");
    RestRequest rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertNull(rest.getElements().get("Main"));
    assertEquals("mailingList45", rest.getComponentId());
  }

  public void testSearchResultRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/searchResult?Type=message&Id=6");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/searchResult?Type=message&Id=6");
    RestRequest rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertNull(rest.getElements().get("searchResult"));
    assertEquals("mailingList45", rest.getComponentId());
  }

  public void testRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/list/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/list/45");
    RestRequest rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("45", rest.getElements().get(DESTINATION_LIST));
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/message/45/mailingListAttachment/18/");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/message/45/mailingListAttachment/18/");
    rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("mailingList45", rest.getComponentId());
    assertEquals("45", rest.getElements().get(DESTINATION_MESSAGE));
    assertEquals("18", rest.getElements().get(DESTINATION_ATTACHMENT));
  }


  public void testDoubleRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45//list/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45//list/45");
    RestRequest rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("45", rest.getElements().get(DESTINATION_LIST));
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45//message/45/mailingListAttachment/18/");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45//message/45/mailingListAttachment/18/");
    rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("mailingList45", rest.getComponentId());
    assertEquals("45", rest.getElements().get(DESTINATION_MESSAGE));
    assertEquals("18", rest.getElements().get(DESTINATION_ATTACHMENT));
  }

  public void testActionRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("PUT");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/message/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/message/45");
    RestRequest rest = new RestRequest(request);
    assertEquals(RestRequest.UPDATE, rest.getAction());
    assertEquals("mailingList45", rest.getComponentId());
    assertEquals("45", rest.getElements().get(DESTINATION_MESSAGE));
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/mailingList/45");
    request.setRequestURI("/silverpeas/mailingList/45");
    rest = new RestRequest(request);
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("45", rest.getElements().get("mailingList"));
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/mailingList/45");
    request.setRequestURI("/silverpeas/mailingList/45");
    rest = new RestRequest(request);
    assertEquals(RestRequest.CREATE, rest.getAction());
    assertEquals("45", rest.getElements().get("mailingList"));
    request.setRemoteHost("localhost");
    request.setMethod("DELETE");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/mailingList/45");
    request.setRequestURI("/silverpeas/mailingList/45");
    rest = new RestRequest(request);
    assertEquals(RestRequest.DELETE, rest.getAction());
    assertEquals("45", rest.getElements().get("mailingList"));
  }


}
