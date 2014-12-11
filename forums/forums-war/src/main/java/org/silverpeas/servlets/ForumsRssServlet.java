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

import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.stratelia.webactiv.forums.forumsManager.ejb.ForumsServiceProvider
    .getForumsService;

public class ForumsRssServlet extends RssServlet {

  private static final long serialVersionUID = -1153108746674900992L;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String, int)
   */
  @Override
  public Collection getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    // récupération de la liste des 15 derniers messages des forums
    Collection events = getForumsService().getLastMessageRSS(instanceId, nbReturned);

    SilverTrace.debug("forums", "ForumsRssServlet.getListElements()",
        "root.MSG_GEN_PARAM_VALUE", "events = " + events);

    return events;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
   */
  @Override
  public String getElementTitle(Object element, String userId) {
    List message = (List) element;

    SilverTrace.debug("forums", "ForumsRssServlet.getElementTitle()",
        "root.MSG_GEN_PARAM_VALUE", "message.get(1) = "
        + message.get(1));

    return (String) message.get(1);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object, java.lang.String)
   */
  @Override
  public String getElementLink(Object element, String userId) {
    List message = (List) element;
    String messageUrl = URLManager.getApplicationURL() + "/ForumsMessage/"
        + message.get(0) + "?ForumId="
        + message.get(4);

    SilverTrace.debug("forums", "ForumsRssServlet.getElementLink()",
        "root.MSG_GEN_PARAM_VALUE", "messageUrl = " + messageUrl);

    return messageUrl;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getElementDescription(Object element, String userId) {
    List message = (List) element;

    SilverTrace.debug("forums", "ForumsRssServlet.getElementDescription()",
        "root.MSG_GEN_PARAM_VALUE", "message.get(6) = "
        + message.get(1));

    return (String) message.get(1);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
  @Override
  public Date getElementDate(Object element) {
    List message = (List) element;
    // Date messageCreationDate = new Date(Long.parseLong((String) message.get(3)));
    Date messageCreationDate = (Date) message.get(3);
    SilverTrace.debug("forums", "ForumsRssServlet.getElementDate()",
        "root.MSG_GEN_PARAM_VALUE", "messageCreationDate = "
        + messageCreationDate);

    return messageCreationDate;
  }

  @Override
  public String getElementCreatorId(Object element) {
    List message = (List) element;
    return (String) message.get(2);
  }
}