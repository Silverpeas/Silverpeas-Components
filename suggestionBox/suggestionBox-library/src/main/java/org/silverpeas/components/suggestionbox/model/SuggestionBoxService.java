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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Optional;

/**
 * A business service to provide a high level interface in the management of the suggestion boxes
 * and of the suggestions.
 * <p/>
 * This service isn't intended to be used as such but within the business objects SuggestionBox and
 * Suggestion. All the operations requiring interaction with other business services or persistence
 * repositories are delegated to this service by the SuggestionBox objects and by the Suggestion
 * objects.
 * <p/>
 * @author mmoquillon
 */
public interface SuggestionBoxService extends ApplicationService {

  /**
   * Gets an object of this interface. The default implementation is used.
   * @return a SuggestionBoxService object.
   */
  static SuggestionBoxService get() {
    return ServiceProvider.getService(SuggestionBoxService.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  Optional<Suggestion> getContributionById(final ContributionIdentifier contributionId);

  /**
   * @param componentInstanceId the identifier of a suggestion box instance.
   * @return the suggestion box belonging to the specified component instance.
   * @see org.silverpeas.components.suggestionbox.model.SuggestionBox#getByComponentInstanceId
   * (String)
   */
  SuggestionBox getByComponentInstanceId(String componentInstanceId);

  /**
   * Indexes all the validated suggestions of the specified suggestion box.
   * @param suggestionBox the suggestion box on which the indexation is performed.
   */
  void indexSuggestionBox(SuggestionBox suggestionBox);

  /**
   * Saves the specified suggestion box.
   * @param box the box to save in Silverpeas.
   */
  void saveSuggestionBox(final SuggestionBox box);

  /**
   * Deletes the specified suggestion box.
   * @param box the box to delete from Silverpeas.
   */
  void deleteSuggestionBox(final SuggestionBox box);
}
