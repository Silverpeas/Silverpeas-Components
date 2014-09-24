/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import com.silverpeas.peasUtil.RssServlet;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

public class ForumsRssServlet extends RssServlet {

  private static final long serialVersionUID = -1153108746674900992L;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String, int)
   */
  public Collection getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    // récupération de la liste des 15 derniers messages des forums
    Collection events = getForumsBM().getLastMessageRSS(instanceId, nbReturned);

    SilverTrace.debug("forums", "ForumsRssServlet.getListElements()",
        "root.MSG_GEN_PARAM_VALUE", "events = " + events);

    return events;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
   */
  public String getElementTitle(Object element, String userId) {
    Vector message = (Vector) element;

    SilverTrace.debug("forums", "ForumsRssServlet.getElementTitle()",
        "root.MSG_GEN_PARAM_VALUE", "message.elementAt(1) = "
        + message.elementAt(1));

    return (String) message.elementAt(1);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object, java.lang.String)
   */
  public String getElementLink(Object element, String userId) {
    Vector message = (Vector) element;
    String messageUrl = URLManager.getApplicationURL() + "/ForumsMessage/"
        + (String) message.elementAt(0) + "?ForumId="
        + (String) message.elementAt(4);

    SilverTrace.debug("forums", "ForumsRssServlet.getElementLink()",
        "root.MSG_GEN_PARAM_VALUE", "messageUrl = " + messageUrl);

    return messageUrl;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  public String getElementDescription(Object element, String userId) {
    Vector message = (Vector) element;

    SilverTrace.debug("forums", "ForumsRssServlet.getElementDescription()",
        "root.MSG_GEN_PARAM_VALUE", "message.elementAt(6) = "
        + message.elementAt(1));

    return (String) message.elementAt(1);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
  public Date getElementDate(Object element) {
    Vector message = (Vector) element;
    // Date messageCreationDate = new Date(Long.parseLong((String) message.elementAt(3)));
    Date messageCreationDate = (Date) message.elementAt(3);
    SilverTrace.debug("forums", "ForumsRssServlet.getElementDate()",
        "root.MSG_GEN_PARAM_VALUE", "messageCreationDate = "
        + messageCreationDate);

    return messageCreationDate;
  }

  public String getElementCreatorId(Object element) {
    Vector message = (Vector) element;
    return (String) message.elementAt(2);
  }

  private ForumsBM getForumsBM() {
    ForumsBM forumsBM = null;
    try {
      forumsBM = EJBUtilitaire.getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME, ForumsBM.class);
    } catch (Exception e) {
      throw new ForumsRuntimeException("RssServlet.getForumsBM()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return forumsBM;
  }
}