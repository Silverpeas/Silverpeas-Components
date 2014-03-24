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

import com.silverpeas.web.ResourceGettingTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxTestResources.*;
import static org.silverpeas.components.suggestionbox.web.SuggestionMatcher.matches;

/**
 * Unit tests on the getting of suggestions from the REST-based web service SuggestionBoxResource.
 * @author mmoquillon
 */
public class SuggestionResourceGettingTest extends ResourceGettingTest<SuggestionBoxTestResources> {

  private String sessionKey;

  public SuggestionResourceGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTest() {
    sessionKey = authenticate(aUser());
  }

  @Test
  public void gettingAnExistingSuggestion() {
    SuggestionEntity entity = getAt(aResourceURI(), getWebEntityClass());
    Suggestion suggestion = getTestResources().aSuggestion();
    assertThat(entity, matches(suggestion));
  }

  @Test
  public void gettingNotPublishedSuggestions() {
    SuggestionBoxService service = getTestResources().getSuggestionBoxService();
    List<Suggestion> notPublished = new ArrayList<Suggestion>();
    notPublished.add(getTestResources().aRandomSuggestion());
    notPublished.add(getTestResources().aRandomSuggestion());
    notPublished.add(getTestResources().aRandomSuggestion());
    when(service.findSuggestionsByCriteria(any(SuggestionCriteria.class)))
        .thenAnswer(new Returns(notPublished));
    SuggestionEntity entity = getAt(aResourceURI(), getWebEntityClass());
    Suggestion suggestion = getTestResources().aSuggestion();
    assertThat(entity, matches(suggestion));
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
