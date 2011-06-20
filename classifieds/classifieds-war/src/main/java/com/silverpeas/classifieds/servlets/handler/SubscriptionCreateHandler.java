package com.silverpeas.classifieds.servlets.handler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.model.Subscribe;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class SubscriptionCreateHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    if (FileUploadUtil.isRequestMultipart(request)) {
      // retrieves parameters
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      String field1 = FileUploadUtil.getParameter(items, classifiedsSC.getSearchFields1());
      String field2 = FileUploadUtil.getParameter(items, classifiedsSC.getSearchFields2());

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
