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

package com.silverpeas.webpages.servlets;

import com.silverpeas.look.LookHelper;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.webpages.control.WebPagesSessionController;
import com.silverpeas.webpages.model.WebPagesException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author sdevolder
 */
public class WebPagesRequestRouter extends ComponentRequestRouter<WebPagesSessionController> {

  private static final long serialVersionUID = -707071668797781762L;
  private final static String USER = "user";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
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
  public WebPagesSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WebPagesSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param webPagesSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,WebPagesSessionController webPagesSC, HttpServletRequest request) {
    String destination = "";
    String rootDestination = "/webPages/jsp/";
    SilverTrace.info("webPages", "WebPagesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + webPagesSC.getUserId()
            + " Function=" + function);

    try {
      if (function.startsWith("Main") || (function.equals("searchResult"))) {
        String profile = webPagesSC.getProfile();
        boolean haveGotContent = processHaveGotContent(webPagesSC, request);
        if (!profile.equals(USER) && !haveGotContent) {
          // Si le role est publieur, le composant s'ouvre en edition
          destination = getDestination("Edit", webPagesSC, request);
        } else {
          // affichage de la page wysiwyg si le role est lecteur ou si il y a un
          // contenu
          destination = getDestination("Preview", webPagesSC, request);
        }
      } else if (function.equals("Edit")) {
        if (webPagesSC.isXMLTemplateUsed()) {
          destination = getDestination("EditXMLContent", webPagesSC, request);
        } else {
          request.setAttribute("userId", webPagesSC.getUserId());
          destination = rootDestination + "edit.jsp";
        }
      } else if (function.equals("Preview")) {
        processHaveGotContent(webPagesSC, request);

        request.setAttribute("IsSubscriber", webPagesSC.isSubscriber());
        if (webPagesSC.isXMLTemplateUsed()) {
          request.setAttribute("Form", webPagesSC.getViewForm());
          request.setAttribute("Data", webPagesSC.getDataRecord());
        }
        
        if (!"Portlet".equals(request.getAttribute("Action"))) {
          if (!USER.equals(webPagesSC.getProfile())) {
            request.setAttribute("Action", "Preview");
          } else {
            request.setAttribute("Action", "Display");
          }
        }

        request.setAttribute("AnonymousAccess", isAnonymousAccess(request));
        destination = rootDestination + "display.jsp";
      } else if (function.startsWith("portlet")) {       
        request.setAttribute("Action", "Portlet");
        destination = getDestination("Preview", webPagesSC, request);
      } else if (function.startsWith("AddSubscription")) {
        webPagesSC.addSubscription();
        destination = getDestination("Main", webPagesSC, request);
      } else if (function.startsWith("RemoveSubscription")) {
        webPagesSC.removeSubscription();
        destination = getDestination("Main", webPagesSC, request);
      } else if ("EditXMLContent".equals(function)) {
        // user wants to edit data
        request.setAttribute("Form", webPagesSC.getUpdateForm());
        request.setAttribute("Data", webPagesSC.getDataRecord());

        destination = rootDestination + "editXMLContent.jsp";
      } else if ("UpdateXMLContent".equals(function)) {
        // user saves updated data
        List<FileItem> items = FileUploadUtil.parseRequest(request);
        webPagesSC.saveDataRecord(items);

        destination = getDestination("Main", webPagesSC, request);
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

  private boolean processHaveGotContent(WebPagesSessionController webPagesSC,
      HttpServletRequest request) throws WebPagesException {
    boolean haveGotContent = false;
    if (webPagesSC.isXMLTemplateUsed()) {
      haveGotContent = webPagesSC.isXMLContentDefined();
    } else {
      haveGotContent = webPagesSC.haveGotWysiwygNotEmpty();
    }
    request.setAttribute("haveGotContent", haveGotContent);
    return haveGotContent;
  }
  
  private boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = (LookHelper) request.getSession().getAttribute("Silverpeas_LookHelper");
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }

}