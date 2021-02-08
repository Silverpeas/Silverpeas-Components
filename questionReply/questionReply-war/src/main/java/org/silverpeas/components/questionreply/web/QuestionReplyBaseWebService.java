/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.web;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;

/**
 *
 */
public abstract class QuestionReplyBaseWebService extends RESTWebService {

  static final String PATH = "questionreply";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  SilverpeasRole getUserProfile() {
    String[] roles =
        getOrganisationController().getUserProfiles(getUser().getId(), getComponentId());
    SilverpeasRole profile = USER;
    for (String currentRole : roles) {
      SilverpeasRole role = SilverpeasRole.fromString(currentRole);
      switch (role) {
        case ADMIN:
          return ADMIN;
        case WRITER:
          profile = WRITER;
          break;
        case PUBLISHER:
          if (profile != WRITER) {
            profile = PUBLISHER;
          }
          break;
        default:
          if (profile != PUBLISHER && profile != WRITER) {
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
