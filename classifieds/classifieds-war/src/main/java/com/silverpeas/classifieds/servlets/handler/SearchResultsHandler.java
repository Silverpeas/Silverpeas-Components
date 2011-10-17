package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SearchResultsHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    // retrieves parameters
    String id = request.getParameter("Id");
    String type = request.getParameter("Type");

    try {
      // Classified
      if (type.equals("Classified")) {
        request.setAttribute("ClassifiedId", id);
        return HandlerProvider.getHandler("ViewClassified").computeDestination(classifiedsSC,
            request);
      }

      // Comment
      else if (type.startsWith("Comment")) {
        request.setAttribute("ClassifiedId", id);
        return HandlerProvider.getHandler("Comments").computeDestination(classifiedsSC, request);
      }

      // Default : Main page
      else {
        return HandlerProvider.getHandler("Main").computeDestination(classifiedsSC, request);
      }
    } catch (Exception e) {
      request.setAttribute("ComponentId", classifiedsSC.getComponentId());
      return "/admin/jsp/documentNotFound.jsp";
    }
  }

}
