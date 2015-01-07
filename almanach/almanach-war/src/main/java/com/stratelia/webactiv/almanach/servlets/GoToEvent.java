/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.servlets;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import org.silverpeas.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToEvent extends GoTo {
  
  private static final long serialVersionUID = -3086487345543160152L;

  @Override
  public String getDestination(String eventId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    EventPK eventPK = new EventPK(eventId);
    EventDetail event = getAlmanachBm().getEventDetail(eventPK);
    String componentId = event.getPK().getInstanceId();

    SilverTrace.info("gallery", "GoToImage.doPost", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId);

    // Set GEF and look helper space identifier
    setGefSpaceId(req, componentId);

    String gotoURL = URLManager.getURL(null, componentId) + event.getURL();

    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  private AlmanachBm getAlmanachBm() {
    try {
      return ServiceProvider.getService(AlmanachBm.class);
    } catch (Exception e) {
      displayError(null);
      return null;
    }
  }
}