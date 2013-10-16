/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.silvercrawler.servlets;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GoToCrawler extends GoTo {

  private static final long serialVersionUID = -295934990255957629L;

  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String componentId = objectId;
    String path = req.getParameter("Path");
    SilverTrace.info("formManager", "GoToCrawler.getDestination",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    String gotoURL = URLManager.getURL(null, componentId)
        + "SubDirectoryFromResult?DirectoryPath=" + path;
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  public GoToCrawler() {
  }
}