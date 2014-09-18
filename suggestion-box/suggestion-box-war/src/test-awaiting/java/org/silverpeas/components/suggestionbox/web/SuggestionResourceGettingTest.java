/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.silverpeas.components.suggestionbox.web;

import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.web.ResourceGettingTest;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxTestResources.*;
import static org.silverpeas.components.suggestionbox.web.SuggestionMatcher.matches;

/**
 * Unit tests on the getting of suggestions from the REST-based web service SuggestionBoxResource.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({UserDetail.class})
public class SuggestionResourceGettingTest extends ResourceGettingTest<SuggestionBoxTestResources> {

  private String sessionKey;
  private UserDetailWithProfiles authenticatedUser;

  public SuggestionResourceGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTest() {
    authenticatedUser = aUser();
    sessionKey = authenticate(authenticatedUser);
    PowerMockito.mockStatic(UserDetail.class, new Answer() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        if (invocation.getMethod().getName().endsWith("getCurrentRequester")) {
          return authenticatedUser;
        }
        return invocation.callRealMethod();
      }
    });
  }

  @Test
  public void gettingAnExistingSuggestion() {
    SuggestionEntity entity = getAt(aResourceURI(), getWebEntityClass());
    Suggestion suggestion = getTestResources().aSuggestion();
    assertThat(entity, matches(suggestion));
  }

  @Test
  public void gettingInDraftSuggestionsByReaderUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.reader);
    try {
      gettingInDraftSuggestions();
      fail("User must be a writer to get a list of not published suggestions");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, Matchers.is(forbidden));
    }
  }

  @Test
  public void gettingInDraftSuggestionsByWriterUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
    SuggestionEntity[] entities = gettingInDraftSuggestions();
    assertThat(entities, notNullValue());
    assertThat(entities.length, is(3));
  }

  private SuggestionEntity[] gettingInDraftSuggestions() {
    List<Suggestion> inDraft = new ArrayList<Suggestion>();
    inDraft.add(getTestResources().aRandomSuggestion());
    inDraft.add(getTestResources().aRandomSuggestion());
    inDraft.add(getTestResources().aRandomSuggestion());
    when(getTestResources().aSuggestionBox().getSuggestions().findInDraftFor(authenticatedUser))
        .thenAnswer(new Returns(inDraft));
    return getAt(SUGGESTIONS_URI_BASE + "inDraft", SuggestionEntity[].class);
  }

  @Test
  public void gettingOutOfDraftSuggestionsByReaderUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.reader);
    try {
      gettingOutOfDraftSuggestions();
      fail("User must be a writer to get a list of not published suggestions");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, Matchers.is(forbidden));
    }
  }

  @Test
  public void gettingOutOfDraftSuggestionsByWriterUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
    SuggestionEntity[] entities = gettingOutOfDraftSuggestions();
    assertThat(entities, notNullValue());
    assertThat(entities.length, is(3));
    verify(getTestResources().aSuggestionBox().getSuggestions(), times(1)).
        findOutOfDraftFor(authenticatedUser);
  }

  private SuggestionEntity[] gettingOutOfDraftSuggestions() {
    List<Suggestion> outOfDraft = new ArrayList<Suggestion>();
    outOfDraft.add(getTestResources().aRandomSuggestion());
    outOfDraft.add(getTestResources().aRandomSuggestion());
    outOfDraft.add(getTestResources().aRandomSuggestion());
    when(getTestResources().aSuggestionBox().getSuggestions().findOutOfDraftFor(authenticatedUser))
        .thenAnswer(new Returns(outOfDraft));
    return getAt(SUGGESTIONS_URI_BASE + "outOfDraft", SuggestionEntity[].class);
  }

  @Test
  public void gettingPendingValidationSuggestionsByReaderUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.reader);
    try {
      gettingPendingValidationSuggestions();
      fail("User must be a publisher to get a list of pending validation suggestions");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, Matchers.is(forbidden));
    }
  }

  @Test
  public void gettingPendingValidationSuggestionsByWriterUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
    try {
      gettingPendingValidationSuggestions();
      fail("User must be a publisher to get a list of pending validation suggestions");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, Matchers.is(forbidden));
    }
  }

  @Test
  public void gettingPendingValidationSuggestionsByPublisherUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.publisher);
    SuggestionEntity[] entities = gettingPendingValidationSuggestions();
    assertThat(entities, notNullValue());
    assertThat(entities.length, is(2));
    verify(getTestResources().aSuggestionBox().getSuggestions(), times(1)).
        findPendingValidation();
  }

  private SuggestionEntity[] gettingPendingValidationSuggestions() {
    List<Suggestion> pendingValidation = new ArrayList<Suggestion>();
    pendingValidation.add(getTestResources().aRandomSuggestion());
    pendingValidation.add(getTestResources().aRandomSuggestion());
    when(getTestResources().aSuggestionBox().getSuggestions().findPendingValidation())
        .thenAnswer(new Returns(pendingValidation));
    return getAt(SUGGESTIONS_URI_BASE + "pendingValidation", SuggestionEntity[].class);
  }

  @Test
  public void gettingPublishedSuggestionsByReaderUser() {
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.user);
    SuggestionEntity[] entities = gettingPublishedSuggestions();
    assertThat(entities, notNullValue());
    assertThat(entities.length, is(1));
    verify(getTestResources().aSuggestionBox().getSuggestions(), times(1)).
        findPublished();
  }

  private SuggestionEntity[] gettingPublishedSuggestions() {
    SuggestionBoxService service = getTestResources().getSuggestionBoxService();
    List<Suggestion> pendingValidation = new ArrayList<Suggestion>();
    pendingValidation.add(getTestResources().aRandomSuggestion());
    when(getTestResources().aSuggestionBox().getSuggestions().findPublished())
        .thenAnswer(new Returns(pendingValidation));
    return getAt(SUGGESTIONS_URI_BASE + "published", SuggestionEntity[].class);
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }

  @Override
  public String aResourceURI() {
    return SUGGESTION_URI;
  }

  @Override
  public String anUnexistingResourceURI() {
    return SUGGESTION_BOX_URI + "/suggestions/toto";
  }

  @Override
  public <T> T aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<SuggestionEntity> getWebEntityClass() {
    return SuggestionEntity.class;
  }
}
