/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package com.silverpeas.classifieds.servlets.handler;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import org.silverpeas.servlet.HttpRequest;

import java.util.Collection;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SubscriptionListHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    try {
      // Retrieves user subscriptions
      Collection<Subscribe> subscribes = classifiedsSC.getSubscribesByUser();

      // Get form template and data
      Form formUpdate = null;
      DataRecord data = null;
      PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
      if (pubTemplate != null) {
        formUpdate = pubTemplate.getSearchForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        data = recordSet.getEmptyRecord();
      }

      // Stores objects in request
      request.setAttribute("Subscribes", subscribes);
      request.setAttribute("Form", formUpdate);
      request.setAttribute("Data", data);

      // Returns jsp to redirect to
      return "subscriptions.jsp";
    } catch (Exception e) {
      // form error for subscriptions
      request.setAttribute("ErrorType",
          classifiedsSC.getResources().getString("classifieds.labelErrorSubscriptions"));
      return "error.jsp";
    }
  }

}