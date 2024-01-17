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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.servlets;

import org.silverpeas.components.formsonline.model.FormDetail;
import org.silverpeas.components.formsonline.model.FormPK;
import org.silverpeas.components.formsonline.model.FormsOnlineService;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.GoTo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToForm extends GoTo {

  private static final long serialVersionUID = -8077728285107343008L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {

    FormDetail form =
        FormsOnlineService.get().loadForm(new FormPK(objectId, req.getParameter("ComponentId")));

    String componentId = form.getPK().getInstanceId();

    String gotoURL = URLUtil.getURL(null, componentId) + "searchResult?Id=" + objectId;

    return "goto=" + URLEncoder.encode(gotoURL, Charsets.UTF_8.name());
  }

}