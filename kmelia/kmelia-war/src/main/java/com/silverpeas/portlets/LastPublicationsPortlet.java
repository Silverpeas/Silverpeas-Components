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

package com.silverpeas.portlets;

import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.kmelia.KmeliaTransversal;
import com.stratelia.webactiv.publication.model.PublicationDetail;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class LastPublicationsPortlet extends GenericPortlet implements FormNames {

  @Override
  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController mainSessionController = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT, PortletSession.APPLICATION_SCOPE);

    String spaceId = (String) session.getAttribute("Silverpeas_Portlet_SpaceId",
        PortletSession.APPLICATION_SCOPE);

    PortletPreferences pref = request.getPreferences();
    int nbPublis = 5;
    if (StringUtil.isInteger(pref.getValue("nbPublis", "5"))) {
      nbPublis = Integer.parseInt(pref.getValue("nbPublis", "5"));
    }
    int maxAge = 0;
    if (StringUtil.isInteger(pref.getValue("maxAge", "0"))) {
      maxAge = Integer.parseInt(pref.getValue("maxAge", "0"));
    }
    KmeliaTransversal kmeliaTransversal = new KmeliaTransversal(mainSessionController);
    List<PublicationDetail> publications = kmeliaTransversal.getUpdatedPublications(spaceId, maxAge,
        nbPublis);
    if(StringUtil.isDefined(spaceId)){
       String rssUrl = getRSSUrl(mainSessionController, spaceId);
       request.setAttribute("rssUrl", rssUrl);
    }   
    request.setAttribute("Publications", publications);    
    include(request, response, "portlet.jsp");
  }

  @Override
  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /**
   * Include "help" JSP.
   */
  @Override
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }

  /**
   * Include a page.
   */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(
          "/portlets/jsp/lastPublications/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      log("Could not include a page", ioe);
      throw new PortletException(ioe);
    }
  }

  /*
   * Process Action.
   */
  @Override
  public void processAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    if (request.getParameter(SUBMIT_FINISHED) != null) {
      processEditFinishedAction(request, response);
    } else if (request.getParameter(SUBMIT_CANCEL) != null) {
      processEditCancelAction(request, response);
    }
  }

  /*
   * Process the "cancel" action for the edit page.
   */
  private void processEditCancelAction(ActionRequest request,
      ActionResponse response) throws PortletException {
    response.setPortletMode(PortletMode.VIEW);
  }

  /*
   * Process the "finished" action for the edit page. Set the "url" to the value specified in the
   * edit page.
   */
  private void processEditFinishedAction(ActionRequest request, ActionResponse response)
      throws PortletException {
    String nbPublis = request.getParameter(TEXTBOX_NB_ITEMS);
    String maxAge = request.getParameter(TEXTBOX_MAX_AGE);
    String displayDescription = request.getParameter("displayDescription");

    // Check if it is a number
    try {
      int nb = Integer.parseInt(nbPublis);
      Integer.parseInt(maxAge);
      if (nb < 0 || nb > 30) {
        throw new NumberFormatException();
      }
      // store preference
      PortletPreferences pref = request.getPreferences();
      try {
        pref.setValue("nbPublis", nbPublis);
        pref.setValue("maxAge", maxAge);
        pref.setValue("displayDescription", displayDescription);
        pref.store();
      } catch (ValidatorException ve) {
        log("could not set nbPublis", ve);
        throw new PortletException("IFramePortlet.processEditFinishedAction", ve);
      } catch (IOException ioe) {
        log("could not set nbPublis", ioe);
        throw new PortletException("IFramePortlet.prcoessEditFinishedAction", ioe);
      }
      response.setPortletMode(PortletMode.VIEW);

    } catch (NumberFormatException e) {
      response.setRenderParameter(ERROR_BAD_VALUE, "true");
      response.setPortletMode(PortletMode.EDIT);
    }
  }

  private void log(String message, Exception ex) {
    getPortletContext().log(message, ex);
  }

  public static String getRSSUrl(MainSessionController mainSessionController, String spaceId) {
    String userId = mainSessionController.getUserId();
    AdminController adminController = ServiceProvider.getService(AdminController.class);
    UserFull user = adminController.getUserFull(userId);
    StringBuilder builder = new StringBuilder();
    builder.append("/rsslastpublications/").append(spaceId);
    builder.append("?userId=").append(userId).append("&login=");
    try {
      builder.append(URLEncoder.encode(user.getLogin(), "UTF-8"));
      builder.append("&password=");
      builder.append(URLEncoder.encode(user.getPassword(), "UTF-8"));
      builder.append("&spaceId=");
      builder.append(URLEncoder.encode(spaceId, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      builder.append(user.getLogin());
      builder.append("&password=");
      builder.append(user.getPassword());
      builder.append("&spaceId=");
      builder.append(spaceId);
    }
    return builder.toString();
  }
}
