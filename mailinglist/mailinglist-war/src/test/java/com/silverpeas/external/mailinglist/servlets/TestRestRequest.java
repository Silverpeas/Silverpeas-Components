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
