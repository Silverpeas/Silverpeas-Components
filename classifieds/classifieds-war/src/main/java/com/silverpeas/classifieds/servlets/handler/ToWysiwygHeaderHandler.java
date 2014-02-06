package com.silverpeas.classifieds.servlets.handler;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.servlet.HttpRequest;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ToWysiwygHeaderHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpRequest request) throws Exception {

    String returnURL = URLEncoder.encode(
          URLManager.getApplicationURL()
          + URLManager.getURL(classifiedsSC.getSpaceId(), classifiedsSC.getComponentId())
          + "FromTopicWysiwyg",
          "UTF-8");

    StringBuilder destination = new StringBuilder();
    destination.append("/wysiwyg/jsp/htmlEditor.jsp?");
    destination.append("SpaceId=").append(classifiedsSC.getSpaceId());
    destination.append("&SpaceName=").append(URLEncoder.encode(classifiedsSC.getSpaceLabel(), "UTF-8"));
    destination.append("&ComponentId=").append(classifiedsSC.getComponentId());
    destination.append("&ComponentName=").append(URLEncoder.encode(classifiedsSC.getComponentLabel(), "UTF-8"));
    destination.append("&BrowseInfo=").append(classifiedsSC.getString("HeaderWysiwyg"));
    destination.append("&ObjectId=Node_0");
    destination.append("&Language=fr");
    destination.append("&ReturnUrl=").append(returnURL);

    return destination.toString();
  }
}
