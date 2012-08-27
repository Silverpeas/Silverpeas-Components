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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.forums.url.ActionUrl;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class GoToMessage extends GoTo {

  private static final long serialVersionUID = -2368464620933821332L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    int forumId = Integer.parseInt(req.getParameter("ForumId"));
    String componentName = getForumsBM().getForumInstanceId(forumId);
    String messageUrl = ActionUrl.getUrl("viewMessage", "viewForum", 1, Integer
        .parseInt(objectId), forumId);
    String gotoURL = URLManager.getURL(null, componentName) + messageUrl;
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  private ForumsBM getForumsBM() {
    ForumsBM forumsBM = null;
    try {
      ForumsBMHome forumsBMHome = EJBUtilitaire.getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME,
          ForumsBMHome.class);
      forumsBM = forumsBMHome.create();
    } catch (Exception e) {
      throw new ForumsRuntimeException("RssServlet.getForumsBM()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return forumsBM;
  }
}