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

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.ForeignPK;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.components.suggestionbox.mock.AttachmentServiceMockWrapper;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxRepositoryMockWrapper;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.OperationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test on the SuggestionBoxService features. For doing, the test mocks all of the
 * service dependencies.
 * @author mmoquillon
 */
public class SuggestionBoxServiceTest {

  private static AbstractApplicationContext context;
  private SuggestionBoxService service = null;

  private static final String appInstanceId = "suggestion-box1";
  private static final String userId = "0";

  public SuggestionBoxServiceTest() {
  }

  @BeforeClass
  public static void bootstrapSpringContext() {
    context = new ClassPathXmlApplicationContext(
        "/spring-suggestion-box-mock.xml", "/spring-suggestion-box-embedded-datasource.xml");
  }

  @AfterClass
  public static void shutdownSpringContext() {
    context.close();
  }

  @Before
  public void setUp() {
    service = SuggestionBoxService.getInstance();
    assertThat(service, notNullValue());
  }

  @After
  public void tearDown() {
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
    verify(repository).save(any(OperationContext.class), eq(box));
  }

  /**
   * Test of deleteSuggestionBox method, of class SuggestionBoxService.
   */
  @Test
  public void deleteASuggestionBox() {
    SuggestionBox box = new SuggestionBox(appInstanceId);
    box.setCreatedBy(userId);
    ReflectionTestUtils
        .setField(box, "id", new UuidIdentifier().fromString("suggestionBoxIdToDelete"));
    AttachmentService attachmentService = getAttachmentService();
    ForeignPK foreignPK = new ForeignPK("suggestionBoxIdToDelete", appInstanceId);
    when(attachmentService.listAllDocumentsByForeignKey(foreignPK, null))
        .thenReturn(CollectionUtil.asList(new SimpleDocument(), new SimpleDocument()));

    service.deleteSuggestionBox(box);

    SuggestionBoxRepository repository = getSuggestionBoxRepository();
    verify(attachmentService, times(1)).listAllDocumentsByForeignKey(eq(foreignPK), anyString());
    verify(attachmentService, times(2)).deleteAttachment(any(SimpleDocument.class));
    verify(repository, times(1)).delete(eq(box));
  }

  private SuggestionBoxRepository getSuggestionBoxRepository() {
    SuggestionBoxRepositoryMockWrapper mockWrapper = context.
        getBean(SuggestionBoxRepositoryMockWrapper.class);
    return mockWrapper.getMock();
  }

  private AttachmentService getAttachmentService() {
    AttachmentServiceMockWrapper mockWrapper = context.
        getBean(AttachmentServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

}
