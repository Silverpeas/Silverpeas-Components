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
package org.silverpeas.components.infoletter.servlets;

import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.service.InfoLetterServiceProvider;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.GoTo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToNewsletter extends GoTo {

  private static final long serialVersionUID = 4824194822323955033L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    IdPK pk = new IdPK(objectId);
    InfoLetterPublicationPdC infoLetter = InfoLetterServiceProvider.getInfoLetterData().getInfoLetterPublication(pk);
    if (infoLetter != null) {
      String componentId = infoLetter.getComponentInstanceId();
      String gotoURL = URLUtil.getComponentInstanceURL(componentId) + infoLetter.getURL();

      // force context of GraphicElementFactory
      setGefSpaceId(req, componentId);

      return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
    }

    return null;
  }
}