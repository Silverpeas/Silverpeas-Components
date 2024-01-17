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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.spacemembers.servlets;

import org.silverpeas.components.spacemembers.control.SpaceMembersSessionController;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

public class SpaceMembersRequestRouter
    extends ComponentRequestRouter<SpaceMembersSessionController> {
  private static final long serialVersionUID = -4293836461205680881L;

  @Override
  public SpaceMembersSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new SpaceMembersSessionController(mainSessionCtrl, context);
  }

  @Override
  public String getSessionControlBeanName() {
    return "spaceMembers";
  }

  @Override
  public String getDestination(String function, SpaceMembersSessionController spaceMembersSCC,
      HttpRequest request) {
    String destination = "";
    if (function.startsWith("Main") || function.startsWith("portlet")) {
      destination = "/Rdirectory/jsp/Main?SpaceId=" + spaceMembersSCC.getSpaceId();
      // only logged-in members
      if (spaceMembersSCC.isHomePageDisplayOnlyConnectedMembers()) {
        destination += "&View=connected";
      }
      // should component instance of space and its sub spaces been lookup?
      destination += "&InWholeSpaceTree=" + spaceMembersSCC.isComponentInstanceRolesLookup();
    } else {
      destination = "/Rdirectory/jsp/" + function;
    }
    return destination;
  }
}