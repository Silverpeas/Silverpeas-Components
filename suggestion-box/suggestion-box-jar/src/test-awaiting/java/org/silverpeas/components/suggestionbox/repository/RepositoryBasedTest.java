/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.repository;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.components.suggestionbox.mock.OrganisationControllerMockWrapper;
import org.silverpeas.components.suggestionbox.model.PersistenceService;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Abstract class for tests that are based on the behavior of a JPA repository. These tests are not
 * about the repository itself but on the persistence characteristics of a business object using a
 * JPA repository.
 */
public abstract class RepositoryBasedTest
    extends org.silverpeas.persistence.jpa.RepositoryBasedTest {

  private PersistenceService persistanceService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    persistanceService = getApplicationContext().getBean(PersistenceService.class);

    // Getting a user by its id
    when(getOrganisationController().getUserDetail(anyString())).then(new Answer<UserDetail>() {

      @Override
      public UserDetail answer(final InvocationOnMock invocation) throws Throwable {
        UserDetail user = new UserDetail();
        user.setId((String) invocation.getArguments()[0]);
        return user;
      }
    });
  }

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{getSuggestionBoxContextPath(),
      "spring-suggestion-box-embedded-datasource.xml"};
  }

  /**
   * Gets the XML Spring configuration file from which the context will be bootstrapped for the
   * test. By default, the context is loaded from the XML file spring-jpa.xml.
   * Overrides this method to specify another XML configuration file.
   * @return the location of the Spring XML configuration file.
   */
  public String getSuggestionBoxContextPath() {
    return "spring-suggestion-box-jpa.xml";
  }

  public OrganizationController getOrganisationController() {
    OrganizationController organisationController = OrganizationControllerProvider.
        getOrganisationController();
    return ((OrganisationControllerMockWrapper) organisationController).getMock();
  }

  public UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId("1");
    OrganizationController organisationController = getOrganisationController();
    when(organisationController.getUserDetail("1")).thenReturn(user);

    return user;
  }

  public PersistenceService getPersistenceService() {
    return this.persistanceService;
  }
}
