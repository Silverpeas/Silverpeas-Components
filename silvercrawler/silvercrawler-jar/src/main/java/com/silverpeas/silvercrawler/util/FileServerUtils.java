/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 *
 * @author NEY
 * @version
 */

public class FileServerUtils extends Object {

  private static String replaceSpecialChars(String toParse) {

    String newLogicalName = toParse.replace(' ', '_');
    newLogicalName = newLogicalName.replace('\'', '_'); // added on 06/09/2001
    newLogicalName = newLogicalName.replace('#', '_');
    newLogicalName = newLogicalName.replace('%', '_');
    FileServerUtils.replaceAccentChars(newLogicalName);

    return newLogicalName;
  }

  public static String replaceAccentChars(String toParse) {

    String newLogicalName = toParse.replace('é', 'e');
    newLogicalName = newLogicalName.replace('è', 'e');
    newLogicalName = newLogicalName.replace('ë', 'e');
    newLogicalName = newLogicalName.replace('ê', 'e');
    newLogicalName = newLogicalName.replace('ö', 'o');
    newLogicalName = newLogicalName.replace('ô', 'o');
    newLogicalName = newLogicalName.replace('õ', 'o');
    newLogicalName = newLogicalName.replace('ò', 'o');
    newLogicalName = newLogicalName.replace('ï', 'i');
    newLogicalName = newLogicalName.replace('î', 'i');
    newLogicalName = newLogicalName.replace('ì', 'i');
    newLogicalName = newLogicalName.replace('ñ', 'n');
    newLogicalName = newLogicalName.replace('ü', 'u');
    newLogicalName = newLogicalName.replace('û', 'u');
    newLogicalName = newLogicalName.replace('ù', 'u');
    newLogicalName = newLogicalName.replace('ç', 'c');
    newLogicalName = newLogicalName.replace('à', 'a');
    newLogicalName = newLogicalName.replace('ä', 'a');
    newLogicalName = newLogicalName.replace('ã', 'a');
    newLogicalName = newLogicalName.replace('â', 'a');

    return newLogicalName;
  }

  public static String getUrl(String logicalName, String physicalName,
      String mimeType, String userId, String componentId) {
    StringBuffer url = new StringBuffer();

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/SilverCrawlerFileServer/").append(
        newLogicalName).append("?SourceFile=").append(physicalName).append(
        "&TypeUpload=link&MimeType=").append(mimeType).append("&UserId=")
        .append(userId).append("&ComponentId=").append(componentId);

    return url.toString();
  }

  public static String getUrlToTempDir(String logicalName, String physicalName,
      String mimeType, String userId, String componentId, String path) {
    StringBuffer url = new StringBuffer();
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    String newLogicalName = replaceSpecialChars(logicalName);

    url.append(m_context).append("/SilverCrawlerFileServer/").append(
        newLogicalName).append("?SourceFile=").append(physicalName).append(
        "&TypeUpload=zip&MimeType=").append(mimeType).append("&UserId=")
        .append(userId).append("&ComponentId=").append(componentId).append(
            "&Path=").append(path);
    return url.toString();
  }

}