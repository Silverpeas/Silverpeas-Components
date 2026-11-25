/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.yellowpages.control;

import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.kernel.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * The XML form models with which the data of the contacts are exported. The model of each folder
 * is fetched only once.
 * @author mmoquillon
 */
class ContactFormModels {

  private final UnaryOperator<String> folderModels;
  private final Map<String, String> modelsByFolder = new HashMap<>();

  /**
   * @param folderModels gives the identifier of the model of the folder with the specified
   * identifier.
   */
  ContactFormModels(UnaryOperator<String> folderModels) {
    this.folderModels = folderModels;
  }

  /**
   * Gets the XML form model of the specified contact, that is the one of the folder in which
   * it is. The users coming from a group of Silverpeas aren't in a folder and then have no such
   * model.
   * @param contact a contact in a folder or a user of a group of Silverpeas.
   * @return the identifier of the XML form model of the contact or nothing if it has no such
   * model.
   */
  Optional<String> getFormModelOf(ContactFatherDetail contact) {
    String nodeId = contact.getNodeId();
    if (nodeId == null || nodeId.startsWith(YellowpagesSessionController.GROUP_REFERENTIEL_PREFIX)) {
      return Optional.empty();
    }
    String modelId = modelsByFolder.computeIfAbsent(nodeId,
        id -> Objects.toString(folderModels.apply(id), ""));
    return StringUtil.isDefined(modelId) && modelId.endsWith(".xml") ? Optional.of(modelId) :
        Optional.empty();
  }
}
