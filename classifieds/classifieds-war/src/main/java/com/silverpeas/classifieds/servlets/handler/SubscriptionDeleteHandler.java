package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SubscriptionDeleteHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    // retrieves parameters
    String subscribeId = request.getParameter("SubscribeId");

    // Removes comment
    classifiedsSC.deleteSubscribe(subscribeId);

    // go back to user subscriptions visualization
    return HandlerProvider.getHandler("ViewMySubscriptions").computeDestination(classifiedsSC, request);
  }
}