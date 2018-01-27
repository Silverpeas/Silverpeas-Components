/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.websites.servlets;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

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
    String node = "";
    List<String> array = new ArrayList<>();
    for(int i = 0; i < deb.length(); i++) {
      char car = deb.charAt(i);
      if (car == '/' || car == '\\') {
        array.add(node);
        node = "";
      } else {
        node += car;
      }
    }
    return array;
  }

  public static String getMachine(HttpServletRequest request) {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.webSites.settings.webSiteSettings");
    SettingBundle generalSettings = ResourceLocator.getGeneralSettingBundle();

    StringBuilder machine = new StringBuilder(settings.getString("Machine", "").trim());
    String context = (generalSettings.getString("ApplicationURL")).substring(1);
    if (machine.length() == 0) {
      List<String> a = buildTab(request.getRequestURL().toString());
      for (int i = 0; i < a.size() && !a.get(i).equals(context); i++) {
        if (machine.length() > 0) {
          machine.append("/");
        }
        machine.append(a.get(i));
      }
    }
    return machine.toString();
  }
}
  