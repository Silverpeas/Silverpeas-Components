package com.silverpeas.classifieds.servlets.handler;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import org.silverpeas.util.StringUtil;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SubscriptionCreateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    if (request.isContentInMultipart()) {
      // retrieves parameters from the multipart stream
      String field1 = request.getParameter(classifiedsSC.getSearchFields1());
      String field2 = request.getParameter(classifiedsSC.getSearchFields2());

      // subscribes user
      if (StringUtil.isDefined(field1) && StringUtil.isDefined(field2)) {
        Subscribe subscribe = new Subscribe(field1, field2);
        classifiedsSC.createSubscribe(subscribe);
      }
    }

    // go back to user's subscriptions visualization
    return HandlerProvider.getHandler("ViewMySubscriptions").computeDestination(classifiedsSC, request);
  }
}
