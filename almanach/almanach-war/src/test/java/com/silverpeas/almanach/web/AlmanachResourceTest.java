/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.almanach.web;

import javax.inject.Inject;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.WebResource;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.silverpeas.rest.RESTWebServiceTest;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.almanach.web.AlmanachTestResources.*;
import static com.silverpeas.rest.RESTWebService.*;

/**
 * Unit tests on the Almanach REST-based web service.
 */
public class AlmanachResourceTest extends RESTWebServiceTest {
  
  @Inject
  private AlmanachTestResources testResources;
  private UserDetail user;
  private String sessionKey;
  
  public AlmanachResourceTest() {
    super("com.silverpeas.almanach.web", "spring-almanach.xml");
  }
  
  @Before
  public void createAndAuthenticateAUser() {
    assertNotNull(testResources);
    testResources.init();
    user = aUser();
    sessionKey = authenticate(user);
  }
  
  @Test
  public void getNextEventsByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(ALMANACH_PATH).accept(MediaType.APPLICATION_JSON).get(String.class);
      fail("A non authenticated user shouldn't access the almanach");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void getNextEventsWithADeprecatedSession() {
    WebResource resource = resource();
    try {
      resource.path(ALMANACH_PATH).header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
          accept(MediaType.APPLICATION_JSON).get(String.class);
      fail("A user shouldn't access the almanach through an expired session");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void getNextEventsFromANonAuthorizedAlmanach() {
    denieAuthorizationToUsers();

    WebResource resource = resource();
    try {
      resource.path(ALMANACH_PATH).
          header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          get(String.class);
      fail("A user shouldn't access a non authorized almanach");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(forbidden));
    }
  }

  @Test
  public void getNextEventsFromAnUnexistingAlmanach() {
    WebResource resource = resource();
    try {
      resource.path(INVALID_ALMANACH_PATH).
          header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          get(String.class);
      fail("A user shouldn't get an unexisting almanach");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(recievedStatus, is(notFound));
    }
  }
  
  @Test
  public void getNextEventsFromAnEmptyAlmanach() {
    String eventsInJSON = resource().path(ALMANACH_PATH).
          header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          get(String.class);
    assertThat(eventsInJSON, is("[]"));
  }
  
  @Test
  public void getNextEventsInAnAlmanach() throws Exception {
    testResources.saveSomeEventsInTheFuture(5);
    String eventsInJSON = resource().path(ALMANACH_PATH).
          header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          get(String.class);
    JSONArray arrayOfEvents = new JSONArray(eventsInJSON);
    assertThat(arrayOfEvents.length(), is(5));
  }
  
  public void getNextEventsInAnAgregatedAlmanach() throws Exception {
    
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{ COMPONENT_INSTANCE_ID };
  }

}
