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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxRepositoryMockWrapper;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.persistence.repository.OperationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

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
    service = SuggestionBoxServiceFactory.getServiceInstance();
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
    SuggestionBox box = new SuggestionBox(appInstanceId, null);
    box.setCreatedBy(userId);

    service.saveSuggestionBox(box);

    SuggestionBoxRepository repository = getSuggestionBoxRepository();
    verify(repository).save(any(OperationContext.class), eq(box));
  }

  private SuggestionBoxRepository getSuggestionBoxRepository() {
    SuggestionBoxRepositoryMockWrapper mockWrapper = (SuggestionBoxRepositoryMockWrapper) context.
        getBean(SuggestionBoxRepository.class);
    return mockWrapper.getMock();
  }

}
