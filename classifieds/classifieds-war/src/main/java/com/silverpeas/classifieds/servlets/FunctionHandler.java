/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.look.LookHelper;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;

/**
 * A functio handler is associated to a peas function and is called by the request router when this
 * function has to be processed.
 */
public abstract class FunctionHandler {

  protected static final String ROOT_DESTINATION = "/classifieds/jsp/";

  public String computeDestination(ClassifiedsSessionController session, HttpServletRequest request) {
    try {
      String destination = getDestination(session, request);
      if (destination.startsWith("/")) {
        return destination;
      }
      else {
        return ROOT_DESTINATION + destination;
      }
    }
    catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

  /**
   * Process the request and returns the response url.
   * @param function the user request name
   * @param request the user request params
   * @param session the user request context
   */
  public abstract String getDestination(ClassifiedsSessionController session, HttpServletRequest request) throws Exception;

  /**
   * Gets the template of the publication based on the classified XML form.
   * @param classifiedsSC the session controller.
   * @return the publication template for classifieds.
   * @throws PublicationTemplateException if an error occurs while getting the publication template.
   */
  protected PublicationTemplate getPublicationTemplate(
      final ClassifiedsSessionController classifiedsSC) throws PublicationTemplateException {
    PublicationTemplateImpl pubTemplate = null;
    String xmlFormName = classifiedsSC.getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
          xmlFormName.indexOf("."));
      pubTemplate =
          (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
          classifiedsSC.getComponentId() + ":" + xmlFormShortName,
          xmlFormName);
    }
    return pubTemplate;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  protected boolean isAnonymousAccess(HttpServletRequest request) {
    LookHelper lookHelper = (LookHelper) request.getSession().getAttribute(LookHelper.SESSION_ATT);
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }

}
