/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;

import java.sql.Connection;


public class SuggestionBoxInstanciator implements ComponentsInstanciatorIntf {

  private static final String COMPONENT_NAME = SuggestionBoxComponentSettings.COMPONENT_NAME;

  public SuggestionBoxInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace
        .info(COMPONENT_NAME, "SuggestionBoxInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
            "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);

    // The title of the suggestion box is the one of the spawned component instance
    SuggestionBox newBox = new SuggestionBox(componentId);
    newBox.setCreatedBy(userId);
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    suggestionBoxService.saveSuggestionBox(newBox);

    SilverTrace
        .info(COMPONENT_NAME, "SuggestionBoxInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace
        .info(COMPONENT_NAME, "SuggestionBoxInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
            "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();

    // Getting the suggestion box entity
    SuggestionBox suggestionBoxToDelete = suggestionBoxService.getByComponentInstanceId(componentId);

    // Deleting the suggestion box
    suggestionBoxService.deleteSuggestionBox(suggestionBoxToDelete);

    SilverTrace
        .info(COMPONENT_NAME, "SuggestionBoxInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private SuggestionBoxService getSuggestionBoxService() {
    return SuggestionBoxService.get();
  }
}
