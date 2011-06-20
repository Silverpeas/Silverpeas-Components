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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsRole;
import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.handler.HandlerProvider;
import com.silverpeas.look.LookHelper;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ClassifiedsRequestRouter
    extends ComponentRequestRouter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "classifieds";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    return new ClassifiedsSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function,
      ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    String rootDest = "/classifieds/jsp/";

    ClassifiedsSessionController classifiedsSC = (ClassifiedsSessionController) componentSC;
    SilverTrace.info("classifieds", "classifiedsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

    // Common parameters
    ClassifiedsRole highestRole = (isAnonymousAccess(request)) ? ClassifiedsRole.ANONYMOUS : ClassifiedsRole.getRole(classifiedsSC.getUserRoles());
    String userId = classifiedsSC.getUserId();

    // Store them in request as attributes
    request.setAttribute("Profile", highestRole);
    request.setAttribute("UserId", userId);
    request.setAttribute("InstanceId", classifiedsSC.getComponentId());
    request.setAttribute("Language", classifiedsSC.getLanguage());

    SilverTrace.debug("classifieds", "classifiedsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Profile=" + highestRole);

    // Delegate to specific Handler
    FunctionHandler handler = HandlerProvider.getHandler(function);
    if (handler != null) {
      destination = handler.computeDestination(classifiedsSC, request);
    }
    else {
      destination = rootDest + function;
    }

    SilverTrace.info("classifieds", "classifiedsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }



  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  private boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = (LookHelper) request.getSession().getAttribute(LookHelper.SESSION_ATT);
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }

}