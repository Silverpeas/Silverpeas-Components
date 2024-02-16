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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.components.classifieds.servlets.handler;

import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.model.Subscribe;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SubscriptionCreateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    if (request.isContentInMultipart()) {
      // retrieves parameters from the multipart stream
      String field1 = request.getParameter(classifiedsSC.getSearchFields1());
      String field2 = request.getParameter(classifiedsSC.getSearchFields2());

      // subscribes user
      if (StringUtil.isDefined(field1) || StringUtil.isDefined(field2)) {
        Subscribe subscribe = new Subscribe(field1, field2);
        classifiedsSC.createSubscribe(subscribe);
      }
    }

    // go back to user's subscriptions visualization
    return HandlerProvider.getHandler("ViewMySubscriptions")
        .computeDestination(classifiedsSC, request);
  }
}
