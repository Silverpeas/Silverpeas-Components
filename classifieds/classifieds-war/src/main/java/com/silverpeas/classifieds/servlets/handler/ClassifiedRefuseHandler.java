package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ClassifiedRefuseHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    // retrieves parameters
    String motive = request.getParameter("Motive");
    String classifiedId = request.getParameter("ClassifiedId");

    // validates classified
    classifiedsSC.refusedClassified(classifiedId, motive);

    // go back to classifieds list
    return HandlerProvider.getHandler("Main").computeDestination(classifiedsSC, request);
  }
}
