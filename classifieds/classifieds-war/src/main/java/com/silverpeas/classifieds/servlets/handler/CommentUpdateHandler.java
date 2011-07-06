package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class CommentUpdateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // retrieves parameters
    String classifiedId = request.getParameter("ClassifiedId");

    // Stores objects in request
    request.setAttribute("ClassifiedId", classifiedId);

    // go back to classified visualization
    return HandlerProvider.getHandler("ViewClassified").computeDestination(classifiedsSC, request);
  }
}
