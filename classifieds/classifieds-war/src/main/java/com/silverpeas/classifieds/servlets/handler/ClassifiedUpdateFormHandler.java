package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedUpdateFormHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // Retrieves parameters
    String classifiedId = request.getParameter("ClassifiedId");
    ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);

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
    request.setAttribute("UserName", classifiedsSC.getUserDetail().getDisplayedName());
    request.setAttribute("UserEmail", classifiedsSC.getUserDetail().geteMail());

    // Returns jsp to redirect to
    return "classifiedManager.jsp";
  }
}
