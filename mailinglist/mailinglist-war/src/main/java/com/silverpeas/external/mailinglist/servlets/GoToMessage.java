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

package com.silverpeas.external.mailinglist.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;

public class GoToMessage extends GoTo {

  public String getDestination(String id, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Message message = ServicesFactory.getMessageService().getMessage(id);
    String baseUrl = URLManager.getURL(null, message.getComponentId());
    if (!baseUrl.endsWith("/")) {
      baseUrl = baseUrl + '/';
    }
    String gotoURL = baseUrl + "message/" + id;
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

}
