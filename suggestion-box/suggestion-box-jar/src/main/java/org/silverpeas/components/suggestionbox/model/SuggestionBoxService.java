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

import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.upload.UploadedFile;

import java.util.Collection;
import java.util.List;

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
public interface SuggestionBoxService {

  /**
   * @param componentInstanceId the identifier of a suggestion box instance.
   * @return the suggestion box belonging to the specified component instance.
   * @see org.silverpeas.components.suggestionbox.model.SuggestionBox#getByComponentInstanceId
   * (String)
   */
  public SuggestionBox getByComponentInstanceId(String componentInstanceId);

  /**
   * Indexes all the validated suggestions of the specified suggestion box.
   * @param suggestionBox the suggestion box on which the indexation is performed.
   */
  public void indexSuggestionBox(SuggestionBox suggestionBox);

  /**
   * Saves the specified suggestion box.
   * @param box the box to save in Silverpeas.
   */
  public void saveSuggestionBox(final SuggestionBox box);

  /**
   * Deletes the specified suggestion box.
   * @param box the box to delete from Silverpeas.
   */
  public void deleteSuggestionBox(final SuggestionBox box);
}
