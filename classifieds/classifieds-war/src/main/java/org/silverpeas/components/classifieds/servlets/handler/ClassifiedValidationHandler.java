/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.classifieds.servlets.handler;

import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

/**
 * Handler takin in charge the validation of a classified.
 *
 * @author mmoquillon
 */
public abstract class ClassifiedValidationHandler extends FunctionHandler {

  /**
   * Once the validation (reject or accept) done, compute the next destination of the user web
   * navigation.
   * @param classifiedsSC the controller
   * @param request the HTTP request
   * @param message the message to print out to the user
   * @return the URL of the web page to go to
   */
  protected final String nextDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request, String message) {
    MultiSilverpeasBundle resources = classifiedsSC.getResources();
    if (!classifiedsSC.getSessionClassifieds().isEmpty()) {
      WebMessager.getInstance()
          .addSuccess(message + resources.getString("classifieds.redirect.next"));
      // More classifieds to validate, go to the next one
      return HandlerProvider.getHandler("Next").computeDestination(classifiedsSC, request);
    }

    // go back to classifieds to validate
    WebMessager.getInstance()
        .addSuccess(message + resources.getString("classifieds.toValidate.nomore"));
    return HandlerProvider.getHandler("ViewClassifiedToValidate")
        .computeDestination(classifiedsSC, request);
  }
}
  