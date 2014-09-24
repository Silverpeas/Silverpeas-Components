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
package com.silverpeas.silvercrawler.util;

import com.stratelia.silverpeas.peasCore.URLManager;

import javax.ws.rs.core.UriBuilder;


public class FileServerUtils extends org.silverpeas.util.FileServerUtils {

  public static String getSilverCrawlerUrl(String logicalName, String physicalName,
      String componentId) {
    UriBuilder uri = UriBuilder.fromPath(URLManager.getApplicationURL());
    uri.path("SilverCrawlerFileServer").path(logicalName);
    uri.queryParam("SourceFile", physicalName);
    uri.queryParam("TypeUpload", "link");
    uri.queryParam("ComponentId", componentId);
    return uri.build().toString();
  }

  public static String getSilverCrawlerUrl(String logicalName, String physicalName,
      String componentId, String path) {
    UriBuilder uri = UriBuilder.fromPath(URLManager.getApplicationURL());
    uri.path("SilverCrawlerFileServer").path(logicalName);
    uri.queryParam("SourceFile", physicalName);
    uri.queryParam("TypeUpload", "zip");
    uri.queryParam("ComponentId", componentId);
    uri.queryParam("Path", path);
    return uri.build().toString();
  }
}