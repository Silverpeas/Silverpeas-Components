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
package org.silverpeas.components.suggestionbox.model;

import com.silverpeas.comment.service.CommentService;
import com.silverpeas.notation.control.RatingService;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.components.suggestionbox.mock.AttachmentServiceMockWrapper;
import org.silverpeas.components.suggestionbox.mock.CommentServiceMockWrapper;
import org.silverpeas.components.suggestionbox.mock.RatingServiceMockWrapper;
import org.silverpeas.components.suggestionbox.mock.SubscriptionServiceMockWrapper;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxRepositoryMockWrapper;
import org.silverpeas.components.suggestionbox.mock.SuggestionRepositoryMockWrapper;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


/**
 * Unit test on the SuggestionBoxService features. For doing, the test mocks all of the
 * service dependencies.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({IndexEngineProxy.class})
public class SuggestionBoxServiceTest {

  private static AbstractApplicationContext context;
  private SuggestionBoxService service = null;

  private static final String appInstanceId = "suggestionBox1";
  private static final String userId = "0";

  public SuggestionBoxServiceTest() {
  }

  @Before
  public void setUp() {
    context = new ClassPathXmlApplicationContext("/spring-suggestion-box-mock.xml",
        "/spring-suggestion-box-embedded-datasource.xml");
    SuggestionBoxServiceProvider serviceFactory = SuggestionBoxServiceProvider.getFactory();
    service = serviceFactory.getSuggestionBoxService();
    assertThat(service, notNullValue());
  }

  @After
  public void tearDown() {
    context.close();
  }

  /**
   * Test of saveSuggestionBox method, of class SuggestionBoxService.
   */
  @Test
  public void saveASuggestionBox() {
    SuggestionBox box = new SuggestionBox(appInstanceId);
    box.setCreatedBy(userId);

    service.saveSuggestionBox(box);

    SuggestionBoxRepository repository = getSuggestionBoxRepository();
    verify(repository, times(1)).save(any(OperationContext.class), eq(box));
  }

  /**
   * Test of deleteSuggestionBox method, of class SuggestionBoxService.
   */
  @Test
  public void deleteASuggestionBox() {
    SuggestionBox box = prepareASuggestionBox();
    service.deleteSuggestionBox(box);

    verify(getSuggestionBoxRepository(), times(1)).delete(box);
    verify(getAttachmentService(), times(1)).deleteAllAttachments(eq(box.getComponentInstanceId()));
    verify(getSubscriptionService(), times(1)).unsubscribeByResource(
        eq(ComponentSubscriptionResource.from(box.getComponentInstanceId())));
    verify(getCommentService(), times(1))
        .deleteAllCommentsByComponentInstanceId(eq(box.getComponentInstanceId()));
    verify(getRatingService(), times(1)).deleteComponentRatings(eq(box.getComponentInstanceId()));
  }

  /**
   * Test of indexSuggestionBox method, of class SuggestionBoxService.
   */
  @Test
  public void indexASuggestionBox() {
    SuggestionBox box = prepareASuggestionBox();
    List<Suggestion> suggestions = new ArrayList<Suggestion>();

    Suggestion currentSuggestion = prepareASuggestion();
    suggestions.add(currentSuggestion);
    currentSuggestion.getValidation().setStatus(ContributionStatus.VALIDATED);

    currentSuggestion = prepareASuggestion();
    suggestions.add(currentSuggestion);
    currentSuggestion.getValidation().setStatus(ContributionStatus.VALIDATED);

    when(getSuggestionRepository().findByCriteria(any(SuggestionCriteria.class)))
        .thenReturn(suggestions);

    service.indexSuggestionBox(box);

    verify(getSuggestionRepository(), times(1)).findByCriteria(any(SuggestionCriteria.class));
    verify(getSuggestionRepository(), times(suggestions.size())).index(any(Suggestion.class));
  }

  private SuggestionBoxRepository getSuggestionBoxRepository() {
    SuggestionBoxRepositoryMockWrapper mockWrapper = context.
        getBean(SuggestionBoxRepositoryMockWrapper.class);
    return mockWrapper.getMock();
  }

  private SuggestionRepository getSuggestionRepository() {
    SuggestionRepositoryMockWrapper mockWrapper = context.
        getBean(SuggestionRepositoryMockWrapper.class);
    return mockWrapper.getMock();
  }

  private SuggestionBox prepareASuggestionBox() {
    SuggestionBox box = new SuggestionBox(appInstanceId);
    box.setCreator(aUser());
    ReflectionTestUtils.setField(box, "id", new UuidIdentifier().fromString("suggestionBox2"));
    return box;
  }

  private Suggestion prepareASuggestion() {
    UserDetail author = aUser();
    SuggestionBox box = prepareASuggestionBox();
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(box);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(author);
    suggestion.setLastUpdater(author);
    ReflectionTestUtils.setField(suggestion, "id", new UuidIdentifier().fromString("suggestion1"));
    return suggestion;
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId(userId);
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    return user;
  }

  private AttachmentService getAttachmentService() {
    AttachmentServiceMockWrapper mockWrapper = context.
        getBean(AttachmentServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

  private SubscriptionService getSubscriptionService() {
    SubscriptionServiceMockWrapper mockWrapper = context.
        getBean(SubscriptionServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

  private CommentService getCommentService() {
    CommentServiceMockWrapper mockWrapper = context.
        getBean(CommentServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

  private RatingService getRatingService() {
    RatingServiceMockWrapper mockWrapper = context.
        getBean(RatingServiceMockWrapper.class);
    return mockWrapper.getMock();
  }
}
