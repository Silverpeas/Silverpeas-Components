/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.spaceMembers.servlets;


import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import org.silverpeas.components.spaceMembers.control.SpaceMembersSessionController;
import org.silverpeas.servlet.HttpRequest;

public class SpaceMembersRequestRouter extends ComponentRequestRouter<SpaceMembersSessionController> {

  private static final long serialVersionUID = -4334545961842905383L;

  @Override
  public SpaceMembersSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new SpaceMembersSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return "spaceMembers";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param hyperlinkSCC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, SpaceMembersSessionController spaceMembersSCC,
      HttpRequest request) {

    SilverTrace.info("spaceMembers", "SpaceMembersRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function = " + function);
    
    String destination = "";

    if (function.startsWith("Main") || function.startsWith("portlet")) {
      
      destination = "/Rdirectory/jsp/Main?SpaceId="+spaceMembersSCC.getSpaceId(); 
      
      //Affichage page d'accueil : Seulement les membres connectés
      if(spaceMembersSCC.isHomePageDisplayOnlyConnectedMembers()) {
        
        destination += "&View=connected";
        
      } 
    } else {
      destination = "/Rdirectory/jsp/"+function;
    }
    
    SilverTrace.info("spaceMembers", "SpaceMembersRequestRooter.getDestination()",
        "root.MSG_GEN_RETURN_VALUE", "destination = " + destination);
    return destination;
  }
}