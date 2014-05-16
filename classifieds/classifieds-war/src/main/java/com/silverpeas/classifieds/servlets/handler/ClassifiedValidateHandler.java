package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedValidateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    // retrieves parameters
    String classifiedId = request.getParameter("ClassifiedId");

    // validates classified
    classifiedsSC.validateClassified(classifiedId);

    // go back to classified visualization
    return HandlerProvider.getHandler("ViewClassified").computeDestination(classifiedsSC, request);
  }
}
