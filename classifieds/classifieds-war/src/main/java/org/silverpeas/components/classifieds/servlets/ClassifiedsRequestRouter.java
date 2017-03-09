/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.components.classifieds.servlets;

import org.silverpeas.components.classifieds.control.ClassifiedsRole;
import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.servlets.handler.HandlerProvider;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;

public class ClassifiedsRequestRouter extends ComponentRequestRouter<ClassifiedsSessionController> {

  private static final long serialVersionUID = -4872776979680116068L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "classifieds";
  }

  @Override
  public ClassifiedsSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ClassifiedsSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param classifiedsSC The component Session Control, build and initialised.
   * @param request the HTTP request
   * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach
   * .jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ClassifiedsSessionController classifiedsSC,
      HttpRequest request) {
    String destination;
    String rootDest = "/classifieds/jsp/";

    // Common parameters
    ClassifiedsRole highestRole = isAnonymousAccess(request) ? ClassifiedsRole.ANONYMOUS :
        ClassifiedsRole.getRole(classifiedsSC.getHighestSilverpeasUserRole().getName());

    // Store them in request as attributes
    request.setAttribute("Profile", highestRole);
    request.setAttribute("InstanceId", classifiedsSC.getComponentId());
    request.setAttribute("Language", classifiedsSC.getLanguage());
    request.setAttribute("isWysiwygHeaderEnabled", classifiedsSC.isWysiwygHeaderEnabled());

    // manage pagination
    if ("SearchClassifieds".equals(function)) {
      classifiedsSC.setCurrentFirstItemIndex("0");
    } else {
      classifiedsSC.setCurrentFirstItemIndex(request.getParameter("ItemIndex"));
    }

    classifiedsSC.setNbItemsPerPage(request.getParameter("ItemsPerPage"));
    request.setAttribute("NbPerPage", classifiedsSC.getNbPerPage());
    request.setAttribute("CurrentFirstItemIndex", classifiedsSC.getCurrentFirstItemIndex());

    try {
      // Delegate to specific Handler
      FunctionHandler handler = HandlerProvider.getHandler(function);
      if (handler != null) {
        destination = handler.computeDestination(classifiedsSC, request);
      } else {
        destination = rootDest + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }


    return destination;
  }


  private boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = LookHelper.getLookHelper(request.getSession());
    return lookHelper != null && lookHelper.isAnonymousAccess();
  }

}