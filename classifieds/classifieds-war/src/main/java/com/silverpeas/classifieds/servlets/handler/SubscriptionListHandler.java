package com.silverpeas.classifieds.servlets.handler;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SubscriptionListHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

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
      request.setAttribute("ErrorType", "labelErrorSubscriptions");
      return "error.jsp";
    }
  }

}