/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.silverpeas.components.community.repository.CommunityRepository;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests about the Community contributions.
 */
@EnableSilverTestEnv
class CommunityTest {

  private static final String USER_ID = "0";

  @BeforeEach
  @SuppressWarnings("JUnitMalformedDeclaration")
  public void mockRequiredResources(
      @TestManagedMock CommunityRepository repository,
      @TestManagedMock OrganizationController organizationController,
      @TestManagedMock UserProvider userProvider) {
    Answer<? extends User> userAnswer = a -> {
      String id = a.getArgument(0);
      UserDetail user = new UserDetail();
      user.setId(id);
      return user;
    };
    when(userProvider.getUser(anyString())).thenAnswer(userAnswer);
    when(organizationController.getUserDetail(anyString())).thenAnswer(userAnswer);

    when(repository.getById(anyString())).thenAnswer(i -> {
      String id = i.getArgument(0);
      return null;
    });

    OperationContext.fromUser(USER_ID);

  }

  @Test
  @DisplayName("An empty test to check the test bootstrapping is working fine")
  void emptyTest() {
    assertThat(true, is(true));
  }

}