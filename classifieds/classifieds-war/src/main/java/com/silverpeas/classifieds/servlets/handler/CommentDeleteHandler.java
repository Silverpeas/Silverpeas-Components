package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class CommentDeleteHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // retrieves parameters
    String id = request.getParameter("CommentId");

    // Removes comment
    classifiedsSC.deleteComment(id);

    // go back to classified visualization
    return HandlerProvider.getHandler("ViewClassified").computeDestination(classifiedsSC, request);
  }
}
