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
package org.silverpeas.components.suggestionbox.mock;

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.contribution.model.ContributionValidation;

import javax.inject.Named;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author mmoquillon
 */
@Named("suggestionService")
public class SuggestionBoxServiceMockWrapper implements SuggestionBoxService {

  private final SuggestionBoxService mock = mock(SuggestionBoxService.class);

  public SuggestionBoxService getMock() {
    return mock;
  }

  @Override
  public List<Suggestion> findSuggestionsByCriteria(final SuggestionCriteria criteria) {
    return mock.findSuggestionsByCriteria(criteria);
  }

  @Override
  public void saveSuggestionBox(SuggestionBox box) {
    mock.saveSuggestionBox(box);
  }

  @Override
  public void deleteSuggestionBox(SuggestionBox box) {
    mock.deleteSuggestionBox(box);
  }

  @Override
  public SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    return mock.getByComponentInstanceId(componentInstanceId);
  }

  @Override
  public void addSuggestion(SuggestionBox box, Suggestion suggestion) {
    mock.addSuggestion(box, suggestion);
  }

  @Override
  public void updateSuggestion(Suggestion suggestion) {
    mock.updateSuggestion(suggestion);
  }

  @Override
  public Suggestion findSuggestionById(SuggestionBox box, String suggestionId) {
    return mock.findSuggestionById(box, suggestionId);
  }

  @Override
  public void removeSuggestion(SuggestionBox box, Suggestion suggestion) {
    mock.removeSuggestion(box, suggestion);
  }

  @Override
  public Suggestion publishSuggestion(final SuggestionBox box, final Suggestion suggestion) {
    return mock.publishSuggestion(box, suggestion);
  }

  @Override
  public Suggestion validateSuggestion(final SuggestionBox box, final Suggestion suggestion,
      final ContributionValidation validation) {
    return mock.validateSuggestion(box, suggestion, validation);
  }
}
