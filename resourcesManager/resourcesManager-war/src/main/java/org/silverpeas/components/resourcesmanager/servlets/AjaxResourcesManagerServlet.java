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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.servlets;

import org.silverpeas.components.resourcesmanager.control.ResourcesManagerSessionController;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.util.ResourceUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

public class AjaxResourcesManagerServlet extends HttpServlet {
  private static final long serialVersionUID = -8949667715718523202L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);
    String componentId = req.getParameter("ComponentId");
    ResourcesManagerSessionController sessionController =
        (ResourcesManagerSessionController) session.
            getAttribute("Silverpeas_" + "ResourcesManager" + "_" + componentId);

    if (sessionController != null) {
      HttpRequest requestWrapper = (HttpRequest) req;
      try {
        Long reservationId = requestWrapper.getParameterAsLong("reservationId");
        String beginDate = req.getParameter("beginDate");
        String beginHour = req.getParameter("beginHour");
        String endDate = req.getParameter("endDate");
        String endHour = req.getParameter("endHour");

        List<Resource> listResourceEverReserved =
            sessionController.getResourcesofReservation(reservationId);
        Date dateDebut = DateUtil.stringToDate(beginDate, beginHour, requestWrapper.
            getUserLanguage());
        Date dateFin = DateUtil.stringToDate(endDate, endHour, requestWrapper.getUserLanguage());
        List<Resource> listResources = sessionController
            .verifyUnavailableResources(ResourceUtil.toIdList(listResourceEverReserved), dateDebut,
                dateFin, reservationId);
        StringBuilder resourceNames = new StringBuilder();
        for (Resource resource : listResources) {
          if (resourceNames.length() > 0) {
            resourceNames.append(",");
          }
          resourceNames.append(resource.getName());
        }

        if (resourceNames.length() > 0) {
          resourceNames
              .insert(0, sessionController.getString("resourcesManager.resourceUnReservable"));
        }

        res.setHeader("charset", "UTF-8");

        Writer writer = res.getWriter();
        writer.write(resourceNames.toString());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
