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

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.components.saasmanager.service.SaasAccessService;
import com.silverpeas.components.saasmanager.service.SaasMailService;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Handler which manages actions linked to a SAAS access request.
 * @author ahedin
 */
public class SaasAccessRequestHandler extends Handler {

  private SaasAccessService accessService;
  private SaasMailService mailService;

  public SaasAccessRequestHandler() {
    accessService = new SaasAccessService();
    mailService = new SaasMailService();
  }

  @Override
  public String getPage(HttpServletRequest request) {
    SaasAccess access = getSaasAccess(request);
    boolean accessAllowed = accessService.isAccessAllowed(access);
    if (accessAllowed && access.isConditionsAgreement()) {
      try {
        int id = accessService.addAccessRequest(access);
        access.setId(id);
        mailService.sendActivationMail(access);
      } catch (SaasManagerException e) {
        SilverTrace.error("saasmanager", "SaasAccessRequestHandler.getPage()",
          "saasmanager.EX_REQUEST_ACCESS", "uid=" + access.getUid(), e);
        return "Error";
      }
    } else {
      return "Error";
    }

    return "Success";
  }

  /**
   * @param request The HTTP request.
   * @return A SAAS access initialized with data coming from the request.
   */
  private SaasAccess getSaasAccess(HttpServletRequest request) {
    SaasAccess access = new SaasAccess();

    access.setLastName(request.getParameter("lastName"));
    access.setFirstName(request.getParameter("firstName"));
    access.setEmail(request.getParameter("email"));
    access.setPhone(request.getParameter("phone"));
    access.setCompany(request.getParameter("company"));
    access.setCompanyWebSite(request.getParameter("companyWebSite"));
    access.setLang(request.getParameter("lang"));
    int usersCount;
    try {
      usersCount = Integer.parseInt(request.getParameter("usersCount"));
    } catch (NumberFormatException e) {
      usersCount = 0;
    }
    access.setUsersCount(usersCount);
    access.setConditionsAgreement("true".equals(request.getParameter("conditionsAgreement")));
    access.setRemarks(request.getParameter("remarks"));

    String services = request.getParameter("services");
    access.setServices(services);

    access.setUid(UUID.randomUUID().toString());
    access.setRemoteAddress(request.getRemoteAddr());
    access.setRequestDate(new Date());

    return access;
  }

}
