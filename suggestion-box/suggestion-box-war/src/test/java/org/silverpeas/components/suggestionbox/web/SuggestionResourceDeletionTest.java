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

import com.silverpeas.web.ResourceDeletionTest;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxTestResources.*;


/**
 * Unit tests on the deletion of suggestions from the REST-based web service SuggestionBoxResource.
 * @author mmoquillon
 */
public class SuggestionResourceDeletionTest
    extends ResourceDeletionTest<SuggestionBoxTestResources> {

  private String sessionKey;
  private UserDetailWithProfiles authenticatedUser;

  public SuggestionResourceDeletionTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTest() {
    authenticatedUser = getTestResources().aUserCreator();
    sessionKey = authenticate(authenticatedUser);
    authenticatedUser.addProfile(COMPONENT_INSTANCE_ID, SilverpeasRole.writer);
  }

  @Test
  public void deletionOfAnExistingSuggestion() {
    when(getOrganizationControllerMock().getUserDetail(anyString())).thenReturn(authenticatedUser);

    deleteAt(aResourceURI());

    SuggestionBoxService service = getTestResources().getSuggestionBoxService();
    SuggestionBox box = getTestResources().aSuggestionBox();
    Suggestion suggestion = getTestResources().aSuggestion();
    verify(service, times(1)).removeSuggestion(eq(box), eq(suggestion));
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
