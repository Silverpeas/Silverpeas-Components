/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.components.forums.servlets;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.RssServlet;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.components.forums.service.ForumsServiceProvider.getForumsService;

public class ForumsRssServlet extends RssServlet {

  private static final long serialVersionUID = -1153108746674900992L;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getListElements(java.lang.String, int)
   */
  @Override
  public Collection getListElements(String instanceId, int nbReturned) {
    Collection events = getForumsService().getLastMessageRSS(instanceId, nbReturned);
    return events;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
   */
  @Override
  public String getElementTitle(Object element, String userId) {
    List message = (List) element;
    return (String) message.get(1);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementLink(java.lang.Object, java.lang.String)
   */
  @Override
  public String getElementLink(Object element, String userId) {
    List message = (List) element;
    String messageUrl = URLUtil.getApplicationURL() + "/ForumsMessage/"
        + message.get(0) + "?ForumId="
        + message.get(4);
    return messageUrl;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getElementDescription(Object element, String userId) {
    List message = (List) element;
    return (String) message.get(1);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.web.util.servlet.RssServlet#getElementDate(java.lang.Object)
   */
  @Override
  public Date getElementDate(Object element) {
    List message = (List) element;
    // Date messageCreationDate = new Date(Long.parseLong((String) message.get(3)));
    Date messageCreationDate = (Date) message.get(3);
    return messageCreationDate;
  }

  @Override
  public String getElementCreatorId(Object element) {
    List message = (List) element;
    return (String) message.get(2);
  }
}