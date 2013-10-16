package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.util.ResourcesWrapper;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ViewClassifiedHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // Retrieves parameters either as parameter or as attribute
    String classifiedId = request.getParameter("ClassifiedId");
    if (!StringUtil.isDefined(classifiedId)) {
      classifiedId = (String) request.getAttribute("ClassifiedId");
    }

    // Get creationDate
    ResourcesWrapper resources = classifiedsSC.getResources();
    ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
    request.setAttribute("Classified", classifiedsSC.getClassifiedWithImages(classifiedId));
    String creationDate = null;
    if (classified.getCreationDate() != null) {
      creationDate = resources.getOutputDateAndHour(classified.getCreationDate());
    } else {
      creationDate = "";
    }
    
    // Get updateDate
    String updateDate = null;
    if (classified.getUpdateDate() != null) {
      updateDate = resources.getOutputDateAndHour(classified.getUpdateDate());
    } else {
      updateDate = "";
    }

    // Get validation date
    String validateDate = null;
    if (classified.getValidateDate() != null) {
      validateDate = resources.getOutputDateAndHour(classified.getValidateDate());
    } else {
      validateDate = "";
    }
    
    // Get form template and data
    Form formView = null;
    DataRecord data = null;
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
