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
import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.servlets.FunctionHandler;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ViewClassifiedHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    String classifiedId;
    ClassifiedDetail classified = (ClassifiedDetail) request.getAttribute("Classified");
    if (classified == null) {
      // Retrieves parameters either as parameter or as attribute
      classifiedId = request.getParameter("ClassifiedId");
      if (!StringUtil.isDefined(classifiedId)) {
        classifiedId = (String) request.getAttribute("ClassifiedId");
      }
    } else {
      classifiedId = classified.getId();
    }
    classified = classifiedsSC.getClassifiedWithImages(classifiedId);
    request.setAttribute("Classified", classified);

    // Get creationDate
    MultiSilverpeasBundle resources = classifiedsSC.getResources();
    String creationDate = "";
    if (classified.getCreationDate() != null) {
      creationDate = resources.getOutputDateAndHour(classified.getCreationDate());
    }

    // Get updateDate
    String updateDate = "";
    if (classified.getUpdateDate() != null) {
      updateDate = resources.getOutputDateAndHour(classified.getUpdateDate());
    }

    // Get validation date
    String validateDate = "";
    if (classified.getValidateDate() != null) {
      validateDate = resources.getOutputDateAndHour(classified.getValidateDate());
    }

    // Get form template and data
    Form formView;
    DataRecord data;
    PublicationTemplate pubTemplate = getPublicationTemplate(classifiedsSC);
    if (pubTemplate != null) {
      formView = pubTemplate.getViewForm();
      RecordSet recordSet = pubTemplate.getRecordSet();
      data = recordSet.getRecord(classifiedId);
      if (data != null) {
        PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(),
            false, classified.getInstanceId(), null);
        xmlContext.setBorderPrinted(false);
        xmlContext.setIgnoreDefaultValues(true);
        xmlContext.setObjectId(classifiedId);
        request.setAttribute("Form", formView);
        request.setAttribute("Data", data);
        request.setAttribute("Context", xmlContext);
      }
    }

    // verify status to set right scope
    classifiedsSC.checkScope(classified);

    // Stores objects in request
    request.setAttribute("IsDraftEnabled", classifiedsSC.isDraftEnabled());
    request.setAttribute("InstanceSettings", classifiedsSC.getInstanceSettings());
    request.setAttribute("CreationDate", creationDate);
    request.setAttribute("UpdateDate", updateDate);
    request.setAttribute("ValidateDate", validateDate);
    request.setAttribute("User", classifiedsSC.getUserDetail());
    request.setAttribute("Index", classifiedsSC.getIndex());
    request.setAttribute("CurrentScope", classifiedsSC.getCurrentScope());

    // Returns jsp to redirect to
    return "viewClassified.jsp";
  }

}