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

package org.silverpeas.components.classifieds.servlets;

import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * A functio handler is associated to a peas function and is called by the request router when this
 * function has to be processed.
 */
public abstract class FunctionHandler {

  protected static final String ROOT_DESTINATION = "/classifieds/jsp/";

  public String computeDestination(ClassifiedsSessionController session, HttpRequest request) {
    try {
      String destination = getDestination(session, request);
      if (destination.startsWith("/")) {
        return destination;
      } else {
        return ROOT_DESTINATION + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

  /**
   * Process the request and returns the response url.
   * @param request the user request params
   * @param session the user request context
   */
  public abstract String getDestination(ClassifiedsSessionController session, HttpRequest request)
      throws Exception;

  /**
   * Gets the template of the publication based on the classified XML form.
   * @param classifiedsSC the session controller.
   * @return the publication template for classifieds.
   * @throws PublicationTemplateException if an error occurs while getting the publication
   * template.
   */
  protected PublicationTemplate getPublicationTemplate(
      final ClassifiedsSessionController classifiedsSC) throws PublicationTemplateException {
    PublicationTemplateImpl pubTemplate = null;
    String xmlFormName = classifiedsSC.getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      pubTemplate = (PublicationTemplateImpl) getPublicationTemplateManager()
          .getPublicationTemplate(classifiedsSC.getComponentId() + ":" + xmlFormShortName,
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

  protected boolean isAnonymousAccess(HttpRequest request) {
    LookHelper lookHelper = LookHelper.getLookHelper(request.getSession());
    if (lookHelper != null) {
      return lookHelper.isAnonymousAccess();
    }
    return false;
  }

}
