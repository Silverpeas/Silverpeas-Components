/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.suggestionbox;

import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.web.index.components.ComponentIndexation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("suggestionBox" + ComponentIndexation.QUALIFIER_SUFFIX)
public class SuggestionBoxIndexer implements ComponentIndexation {

  @Inject
  private SuggestionBoxService suggestionBoxService;

  @Override
  public void index(SilverpeasComponentInstance componentInst) {
    // Getting the suggestion box entity
    SuggestionBox suggestionBoxToIndex =
        suggestionBoxService.getByComponentInstanceId(componentInst.getId());
    // Indexing the suggestion box
    suggestionBoxService.indexSuggestionBox(suggestionBoxToIndex);
  }
}