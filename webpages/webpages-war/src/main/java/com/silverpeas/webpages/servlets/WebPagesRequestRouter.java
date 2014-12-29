/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.webpages.servlets;

import com.silverpeas.look.LookHelper;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.util.SubscriptionManagementContext;
import com.silverpeas.webpages.control.WebPagesSessionController;
import com.silverpeas.webpages.model.WebPagesException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.CharEncoding;
import org.owasp.encoder.Encode;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.util.ActionType;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
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
  @Override
  public String getSessionControlBeanName() {
    return "WebPages";
  }

  /**
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public WebPagesSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WebPagesSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param webPagesSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, WebPagesSessionController webPagesSC,
      HttpRequest request) {
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
          request.setAttribute("SpaceId", webPagesSC.getSpaceId());
          request.setAttribute("SpaceName", URLEncoder.encode(webPagesSC.getSpaceLabel(),
              CharEncoding.UTF_8));
          request.setAttribute("ComponentId", webPagesSC.getComponentId());
          request.setAttribute("ComponentName", URLEncoder.encode(webPagesSC.getComponentLabel(),
              CharEncoding.UTF_8));
          request.setAttribute("BrowseInfo", Encode.forHtml(webPagesSC.getComponentLabel()));
          request.setAttribute("ObjectId", webPagesSC.getComponentId());
          request.setAttribute("Language", webPagesSC.getLanguage());
          request.setAttribute("ReturnUrl",
              URLManager.getApplicationURL() + webPagesSC.getComponentUrl() + "Preview");
          request.setAttribute("UserId", webPagesSC.getUserId());
          request.setAttribute("IndexIt", "false");

          setupRequestForSubscriptionNotificationSending(request, webPagesSC.getComponentId());

          destination = "/wysiwyg/jsp/htmlEditor.jsp";
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

        setupRequestForSubscriptionNotificationSending(request, webPagesSC.getComponentId());

        destination = rootDestination + "editXMLContent.jsp";
      } else if ("UpdateXMLContent".equals(function)) {
        // user saves updated data
        List<FileItem> items = request.getFileItems();
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

  /**
   * Setup the request to manager some behaviors around subscription notification sending.
   * @param request the current request.
   * @param componentInstanceId the identifier of current component instance.
   */
  private void setupRequestForSubscriptionNotificationSending(HttpRequest request,
      String componentInstanceId) {
    ContributionStatus statusBeforeSave = ContributionStatus.VALIDATED;
    ContributionStatus statusAfterSave = ContributionStatus.VALIDATED;
    request.setAttribute("subscriptionManagementContext", SubscriptionManagementContext
        .on(ComponentSubscriptionResource.from(componentInstanceId), componentInstanceId)
        .forPersistenceAction(statusBeforeSave, ActionType.UPDATE, statusAfterSave));
  }

  private boolean processHaveGotContent(WebPagesSessionController webPagesSC,
      HttpServletRequest request) throws WebPagesException {
    boolean haveGotContent;
    if (webPagesSC.isXMLTemplateUsed()) {
      haveGotContent = webPagesSC.isXMLContentDefined();
    } else {
      haveGotContent = webPagesSC.haveGotWysiwygNotEmpty();
    }
    request.setAttribute("haveGotContent", haveGotContent);
    return haveGotContent;
  }

  private boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = LookHelper.getLookHelper(request.getSession());
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }

}
