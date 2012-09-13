/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.stratelia.silverpeas.peasCore.URLManager;


public class FileServerUtils extends com.stratelia.webactiv.util.FileServerUtils {


  public static String getUrl(String logicalName, String physicalName, String mimeType,
      String userId, String componentId) {
    StringBuilder url = new StringBuilder(256);
    url.append(URLManager.getApplicationURL());
    url.append("/SilverCrawlerFileServer/");
    url.append(replaceSpecialChars(logicalName));
    url.append("?SourceFile=").append(encode(physicalName));
    url.append("&TypeUpload=link&MimeType=").append(mimeType);
    url.append("&UserId=").append(userId);
    url.append("&ComponentId=").append(componentId);
    return url.toString();
  }

  public static String getUrlToTempDir(String logicalName, String physicalName, String mimeType,
      String userId, String componentId, String path) {
    StringBuilder url = new StringBuilder(256);
    url.append(URLManager.getApplicationURL());
    url.append("/SilverCrawlerFileServer/");
    url.append(replaceSpecialChars(logicalName));
    url.append("?SourceFile=").append(encode(physicalName));
    url.append("&TypeUpload=zip&MimeType=").append(mimeType);
    url.append("&UserId=").append(userId);
    url.append("&ComponentId=").append(componentId);
    url.append("&Path=").append(path);
    return url.toString();
  }
  
  private static String encode(String path) {
    try {
      return URLEncoder.encode(path, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return path;
    }
  }

}