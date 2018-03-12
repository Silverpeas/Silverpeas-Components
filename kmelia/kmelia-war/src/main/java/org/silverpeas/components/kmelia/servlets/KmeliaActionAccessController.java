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
 * Open Source Software ("FLOSS", SilverpeasRole.reader) applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.servlets;

import org.silverpeas.core.admin.user.model.SilverpeasRole;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ebonnet
 */
@Singleton
public class KmeliaActionAccessController {

  private Map<String, SilverpeasRole> actionRole = new HashMap<>();

  public KmeliaActionAccessController() {
    actionRole.put("Main", SilverpeasRole.reader);
    actionRole.put("DeletePublication", SilverpeasRole.writer);
    actionRole.put("NewPublication", SilverpeasRole.writer);
    actionRole.put("ToUpdatePublicationHeader", SilverpeasRole.writer);
    actionRole.put("ToPubliContent", SilverpeasRole.writer);
    actionRole.put("AddLinksToPublication", SilverpeasRole.writer);
    actionRole.put("DeleteSeeAlso", SilverpeasRole.writer);
  }


  /**
   * Check if user role has right access to the given action
   * @param action the checked action
   * @param role the highest user role
   * @return true if given role has right access to the action
   */
  public boolean hasRightAccess(String action, SilverpeasRole role) {
    boolean actionExist = actionRole.containsKey(action);
    return (actionExist && role.isGreaterThanOrEquals(actionRole.get(action))) || !actionExist;
  }
}
