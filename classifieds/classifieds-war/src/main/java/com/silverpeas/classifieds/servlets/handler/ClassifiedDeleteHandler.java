package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedDeleteHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // Retrieves parameters
    String classifiedId = request.getParameter("ClassifiedId");

    // Delete classified
    classifiedsSC.deleteClassified(classifiedId);

    // Return to "MyClassifieds" view
    return HandlerProvider.getHandler("ViewMyClassifieds").computeDestination(classifiedsSC, request);
  }
}
