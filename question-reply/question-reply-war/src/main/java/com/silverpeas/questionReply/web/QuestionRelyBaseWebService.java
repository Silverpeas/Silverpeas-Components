/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import static com.stratelia.webactiv.SilverpeasRole.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 *
 */
public abstract class QuestionRelyBaseWebService extends RESTWebService {

  SilverpeasRole getUserProfile() {
    String[] roles = getOrganizationController().getUserProfiles(getUserDetail().getId(),
            getComponentId());
    SilverpeasRole profile = user;
    for (String currentRole : roles) {
      SilverpeasRole role = SilverpeasRole.valueOf(currentRole);
      switch (role) {
        case admin:
          return admin;
        case writer:
          profile = writer;
          break;
        case publisher:
          if (profile != writer) {
            profile = publisher;
          }
          break;
        default:
          if (profile != publisher && profile != writer) {
            profile = role;
          }
          break;
      }
    }
    return profile;
  }

  WebApplicationException encapsulateException(Exception ex) {
    if (ex instanceof WebApplicationException) {
      return (WebApplicationException) ex;
    }
    return new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
  }
}
