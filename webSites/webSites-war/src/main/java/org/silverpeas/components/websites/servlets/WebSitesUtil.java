/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.servlets;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility classes providing common functions.
 * @author mmoquillon
 */
public class WebSitesUtil {

  private WebSitesUtil() {
  }

  public static List<String> buildTab(String deb) {
    /* deb = id/rep/ ou id\rep/ */
    /* res = [id | rep] */
    StringBuilder node = new StringBuilder();
    List<String> array = new ArrayList<>();
    for(int i = 0; i < deb.length(); i++) {
      char car = deb.charAt(i);
      if (car == '/' || car == '\\') {
        array.add(node.toString());
        node = new StringBuilder();
      } else {
        node.append(car);
      }
    }
    return array;
  }

  static String getSiteURL(HttpServletRequest request, String componentInstanceId, String siteId) {
    return getComponentURL(request, componentInstanceId) + "/" + siteId;
  }

  public static String getComponentURL(HttpServletRequest request, String componentInstanceId) {
    return getServerURL(request) + "/" + componentInstanceId;
  }

  private static String getServerURL(HttpServletRequest request) {
    SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
    String machine = settings.getString("Machine");
    if (StringUtil.isNotDefined(machine)) {
      machine = URLUtil.getServerURL(request);
    } else if (!machine.startsWith("http")) {
      machine = request.getScheme() + "://" + machine.replaceFirst("^[/]+", "");
    }
    return machine + "/" + settings.getString("Context");
  }
}
  