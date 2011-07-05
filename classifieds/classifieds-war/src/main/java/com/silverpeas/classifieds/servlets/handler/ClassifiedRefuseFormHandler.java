package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedRefuseFormHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // Retrieves parameters
    String classifiedId = request.getParameter("ClassifiedId");

    // Store classified object in request as attribute
    ClassifiedDetail classified = classifiedsSC.getClassified(classifiedId);
    request.setAttribute("ClassifiedToRefuse", classified);

    // Returns jsp to redirect to
    return "refusalMotive.jsp";
   }

}
