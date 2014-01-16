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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.components.saasmanager.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.components.saasmanager.service.SaasComponentService;

/**
 * Handler which proposes available components to add to a spaces created in a SAAS context.
 * @author ahedin
 */
public class SaasServicesListHandler extends Handler {

  private SaasComponentService componentService;

  public SaasServicesListHandler() {
    componentService = new SaasComponentService();
  }

  @Override
  public String getPage(HttpServletRequest request) {
    String language = request.getParameter("language");
    request.setAttribute("componentLists", componentService.getComponentLists(language));
    return "services.jsp";
  }

}
