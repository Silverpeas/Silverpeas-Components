/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.classifieds.servlets.handler;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.servlet.HttpRequest;

import java.net.URLEncoder;

/**
 * Use Case : for all users, show all adds of given category
 * @author Ludovic Bertin
 */
public class ToWysiwygHeaderHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC, HttpRequest request)
      throws Exception {

    String returnURL = URLEncoder.encode(URLManager.getApplicationURL() +
            URLManager.getURL(classifiedsSC.getSpaceId(), classifiedsSC.getComponentId()) +
            "FromTopicWysiwyg", "UTF-8");

    StringBuilder destination = new StringBuilder();
    destination.append("/wysiwyg/jsp/htmlEditor.jsp?");
    destination.append("SpaceId=").append(classifiedsSC.getSpaceId());
    destination.append("&SpaceName=")
        .append(URLEncoder.encode(classifiedsSC.getSpaceLabel(), "UTF-8"));
    destination.append("&ComponentId=").append(classifiedsSC.getComponentId());
    destination.append("&ComponentName=")
        .append(URLEncoder.encode(classifiedsSC.getComponentLabel(), "UTF-8"));
    destination.append("&BrowseInfo=").append(classifiedsSC.getString("HeaderWysiwyg"));
    destination.append("&ObjectId=Node_0");
    destination.append("&Language=fr");
    destination.append("&ReturnUrl=").append(returnURL);

    return destination.toString();
  }
}
