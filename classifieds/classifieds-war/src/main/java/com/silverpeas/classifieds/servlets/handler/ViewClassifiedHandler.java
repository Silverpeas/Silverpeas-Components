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
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.MultiSilverpeasBundle;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ViewClassifiedHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    // Retrieves parameters either as parameter or as attribute
    String classifiedId = request.getParameter("ClassifiedId");
    if (!StringUtil.isDefined(classifiedId)) {
      classifiedId = (String) request.getAttribute("ClassifiedId");
    }

    // Get creationDate
    MultiSilverpeasBundle resources = classifiedsSC.getResources();
    ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
    request.setAttribute("Classified", classifiedsSC.getClassifiedWithImages(classifiedId));
    String creationDate;
    if (classified.getCreationDate() != null) {
      creationDate = resources.getOutputDateAndHour(classified.getCreationDate());
    } else {
      creationDate = "";
    }
    
    // Get updateDate
    String updateDate;
    if (classified.getUpdateDate() != null) {
      updateDate = resources.getOutputDateAndHour(classified.getUpdateDate());
    } else {
      updateDate = "";
    }

    // Get validation date
    String validateDate;
    if (classified.getValidateDate() != null) {
      validateDate = resources.getOutputDateAndHour(classified.getValidateDate());
    } else {
      validateDate = "";
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
        request.setAttribute("Form", formView);
        request.setAttribute("Data", data);
        request.setAttribute("Context", xmlContext);
      }
    }

    // Stores objects in request
    request.setAttribute("IsDraftEnabled", classifiedsSC.isDraftEnabled());
    request.setAttribute("IsCommentsEnabled", classifiedsSC.isCommentsEnabled());
    request.setAttribute("CreationDate", creationDate);
    request.setAttribute("UpdateDate", updateDate);
    request.setAttribute("ValidateDate", validateDate);

    // Returns jsp to redirect to
    return "viewClassified.jsp";
  }

}
