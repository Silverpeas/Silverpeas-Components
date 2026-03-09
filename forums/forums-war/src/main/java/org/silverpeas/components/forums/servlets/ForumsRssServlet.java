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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.servlets;

import jakarta.inject.Inject;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.service.ForumService;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.RssServlet;

import java.util.Collection;
import java.util.Date;

public class ForumsRssServlet extends RssServlet<Message> {

  private static final long serialVersionUID = -1153108746674900992L;

  @Inject
  private ForumService forumService;

  @Override
  public Collection<Message> getListElements(String instanceId, int nbReturned) {
    return forumService.getLastMessageRSS(instanceId, nbReturned);
  }

  @Override
  public String getElementTitle(Message message, String userId) {
    return message.getTitle();
  }

  @Override
  public String getElementLink(Message message, String userId) {
    return URLUtil.getApplicationURL() + "/ForumsMessage/"
        + message.getId() + "?ForumId="
        + message.getForumId();
  }

  @Override
  public String getElementDescription(Message message, String userId) {
    return message.getDescription();
  }

  @Override
  public Date getElementDate(Message message) {
    return message.getDate();
  }

  @Override
  public String getElementCreatorId(Message message) {
    return message.getAuthor();
  }
}