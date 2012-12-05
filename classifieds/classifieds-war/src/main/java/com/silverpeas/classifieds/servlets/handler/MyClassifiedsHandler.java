package com.silverpeas.classifieds.servlets.handler;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.servlets.FunctionHandler;

/**
 * Use Case : for publisher, show own classifieds
 *
 * @author Ludovic Bertin
 *
 */
public class MyClassifiedsHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception{

    // Retrieve user own classifieds
    Collection<ClassifiedDetail> classifieds = classifiedsSC.getClassifiedsByUser();

    // Stores objects in request
    request.setAttribute("CreatorName", classifiedsSC.getUserDetail().getDisplayedName());
    request.setAttribute("Classifieds", classifieds);
    request.setAttribute("TitlePath", "classifieds.myClassifieds");

    // Returns jsp to redirect to
    return "classifieds.jsp";
   }

}