/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcesmanager.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcesmanager.control.ResourcesManagerSessionController;

public class AjaxResourcesManagerServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
          IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
          IOException {
    HttpSession session = req.getSession(true);
    String componentId = req.getParameter("ComponentId");
    ResourcesManagerSessionController sessionController = (ResourcesManagerSessionController) session.
            getAttribute("Silverpeas_" + "ResourcesManager" + "_" + componentId);

    if (sessionController != null) {
      try {
        String reservationId = req.getParameter("reservationId");
        SilverTrace.info("resourcesManager", "AjaxResourcesManagerServlet",
                "root.MSG_GEN_PARAM_VALUE", "reservationId=" + reservationId);
        String beginDate = req.getParameter("beginDate");
        String beginHour = req.getParameter("beginHour");
        String endDate = req.getParameter("endDate");
        String endHour = req.getParameter("endHour");

        List<Resource> listResourceEverReserved = sessionController.getResourcesofReservation(
                reservationId);
        String listResource = "";
        for (int i = 0; i < listResourceEverReserved.size(); i++) {
          Resource myResource = listResourceEverReserved.get(i);
          String idResource = myResource.getId();
          if (i == 0) {
            listResource = idResource;
          } else {
            listResource = listResource + "," + idResource;
          }
        }
        String currentLang = sessionController.getLanguage();
        Date dateDebut = DateUtil.stringToDate(beginDate, beginHour, currentLang);
        Date dateFin = DateUtil.stringToDate(endDate, endHour, currentLang);
        List<Resource> listResources = sessionController.getResourcesProblemDate(listResource,
                dateDebut, dateFin, reservationId);
        String listResourceName = "";
        for (int i = 0; i < listResources.size(); i++) {
          Resource myResource = listResources.get(i);
          String resourceName = myResource.getName();
          if (i == 0) {
            listResourceName = resourceName;
          } else {
            listResourceName = listResourceName + "," + resourceName;
          }
        }

        SilverTrace.info("resourcesManager", "AjaxResourcesManagerServlet",
                "root.MSG_GEN_PARAM_VALUE",
                " avant concaténation listResourceName= " + listResourceName);
        if (StringUtil.isDefined(listResourceName)) {
          listResourceName = sessionController.getString("resourcesManager.resourceUnReservable")
                  + listResourceName;
        }

        SilverTrace.info("resourcesManager", "AjaxResourcesManagerServlet",
                "root.MSG_GEN_PARAM_VALUE", "listResourceName= " + listResourceName);
        res.setHeader("charset", "UTF-8");

        Writer writer = res.getWriter();
        writer.write(listResourceName);
      } catch (Exception e) {
        // TODO: handle exception
      }
    }
  }
}