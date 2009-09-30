/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.webpages.servlets;

import javax.servlet.http.HttpServletRequest;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;

import com.silverpeas.webpages.control.*;

/**
 * @author sdevolder
 * 
 */
public class WebPagesRequestRouter extends ComponentRequestRouter {
  private final static String USER = "user";

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "WebPages";
  }

  /**
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WebPagesSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = "";
    String rootDestination = "/webPages/jsp/";

    WebPagesSessionController webPagesSC = (WebPagesSessionController) componentSC;
    SilverTrace.info("webPages", "WebPagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId()
            + " Function=" + function);

    try {
      if (function.startsWith("Main") || (function.equals("searchResult"))) {
        String profile = webPagesSC.getProfile();
        boolean haveGotWysiwyg = processHaveGotWysiwygNotEmpty(webPagesSC,
            request);
        if (!profile.equals(USER) && !haveGotWysiwyg) {
          // Si le role est publieur, le composant s'ouvre en édition
          destination = getDestination("Edit", componentSC, request);
        } else {
          // affichage de la page wysiwyg si le role est lecteur ou si il y a un
          // contenu
          destination = getDestination("Preview", componentSC, request);
        }
      } else if (function.equals("Edit")) {
        request.setAttribute("userId", webPagesSC.getUserId());
        destination = rootDestination + "edit.jsp";
      } else if (function.equals("Preview")) {
        processHaveGotWysiwygNotEmpty(webPagesSC, request);

        String profile = webPagesSC.getProfile();
        if (!profile.equals(USER))
          request.setAttribute("Action", "Preview");
        else
          request.setAttribute("Action", "Display");

        destination = rootDestination + "display.jsp";
      } else if (function.startsWith("portlet")) {
        processHaveGotWysiwygNotEmpty(webPagesSC, request);

        request.setAttribute("Action", "Portlet");

        destination = rootDestination + "display.jsp";
      } else if (function.startsWith("AddSubscription")) {
        webPagesSC.addSubscription("0");
        destination = getDestination("Main", componentSC, request);
      } else if (function.startsWith("RemoveSubscription")) {
        webPagesSC.removeSubscription("0");
        destination = getDestination("Main", componentSC, request);
      } else {
        destination = rootDestination + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("webPages", "WebPagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  private boolean processHaveGotWysiwygNotEmpty(
      WebPagesSessionController webPagesSC, HttpServletRequest request) {
    boolean haveGotWysiwyg = webPagesSC.haveGotWysiwygNotEmpty();
    request.setAttribute("haveGotWysiwyg", new Boolean(haveGotWysiwyg));
    return haveGotWysiwyg;
  }

}