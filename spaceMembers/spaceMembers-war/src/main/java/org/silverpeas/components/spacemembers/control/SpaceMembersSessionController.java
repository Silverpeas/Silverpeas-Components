/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.spacemembers.control;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import static java.util.Optional.ofNullable;

public class SpaceMembersSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = -8252699589828677539L;

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public SpaceMembersSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext);
  }

  /**
   * Should only logged-in members been displayed on homepage?
   * @return boolean true if it should, false otherwise.
   */
  public boolean isHomePageDisplayOnlyConnectedMembers() {
    return ofNullable(getComponentParameterValue("homePage"))
        .filter(StringUtil::isDefined)
        .map(StringUtil::getBooleanValue)
        .orElse(false);
  }

  /**
   * Should the component instances of space and its subspaces been lookup?
   * @return boolean true if it should, false otherwise.
   */
  public boolean isComponentInstanceRolesLookup() {
    return ofNullable(getComponentParameterValue("componentInstanceRolesLookup"))
        .filter(StringUtil::isDefined)
        .map(StringUtil::getBooleanValue)
        .orElse(true);
  }
}
