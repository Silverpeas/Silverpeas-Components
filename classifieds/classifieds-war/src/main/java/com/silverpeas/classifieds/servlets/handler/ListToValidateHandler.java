package com.silverpeas.classifieds.servlets.handler;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for moderator, show all classifieds waiting for validation
 *
 * @author Ludovic Bertin
 *
 */
public class ListToValidateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception{

    // Retrieve classifieds waiting for validation
    Collection<ClassifiedDetail> classifieds = classifiedsSC.getClassifiedsToValidate();

    // Stores objects in request
    request.setAttribute("Classifieds", classifieds);
    request.setAttribute("TitlePath", "classifieds.viewClassifiedToValidate");

    // Returns jsp to redirect to
    return "classifieds.jsp";
   }

}
