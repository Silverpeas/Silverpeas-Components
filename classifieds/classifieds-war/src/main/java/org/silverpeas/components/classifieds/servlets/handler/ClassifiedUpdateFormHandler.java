/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.classifieds.servlets.handler;

import org.silverpeas.components.classifieds.control.ClassifiedsSessionController;
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedUpdateFormHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    // Retrieves parameters
    String classifiedId = request.getParameter("ClassifiedId");
    ClassifiedDetail classified = classifiedsSC.getClassifiedWithImages(classifiedId);

    // Get form template and data
    Form formView = null;
    DataRecord data = null;
    PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
    if (pubTemplate != null) {
      formView = pubTemplate.getUpdateForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      data = recordSet.getRecord(classifiedId);
      if (data != null) {
        request.setAttribute("Form", formView);
        request.setAttribute("Data", data);
      }
    }

    // Stores objects in request
    request.setAttribute("Classified", classified);
    request.setAttribute("DraftEnabled", classifiedsSC.isDraftEnabled() && classified.isDraft());
    request.setAttribute("InstanceSettings", classifiedsSC.getInstanceSettings());

    // Returns jsp to redirect to
    return "classifiedManager.jsp";
  }
}
