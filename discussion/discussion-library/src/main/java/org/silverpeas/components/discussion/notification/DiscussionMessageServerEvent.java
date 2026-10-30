/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.notification;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.components.discussion.service.DiscussionMentionService;

/** Live desktop signal only: never stored or replayed to disconnected users. */
public class DiscussionMessageServerEvent extends AuthorizedDiscussionEvent implements IgnoreStoring {
  private final DiscussionNewMessage message;
  private final ComponentInstLight component;

  public DiscussionMessageServerEvent(DiscussionNewMessage message, ComponentInstLight component,
      ComponentAccessController access) {
    super(message.instanceId(), access);
    this.message = message;
    this.component = component;
  }

  @Override
  public ServerEventName getName() { return () -> "DISCUSSION_NEW_MESSAGE"; }

  @Override
  public boolean isConcerned(String sessionId, User receiver) {
    return receiver != null && !message.authorId().equals(receiver.getId())
        && super.isConcerned(sessionId, receiver);
  }

  @Override
  public String getData(String sessionId, User receiver) {
    String language = receiver.getUserPreferences().getLanguage();
    return JSONCodec.encodeObject(json -> json.put("instanceId", message.instanceId())
        .put("messageId", message.messageId()).put("userId", receiver.getId())
        .put("title", component.getLabel(language))
        .put("text", message.authorName() + ": " + DiscussionMentionService.displayText(message.body()))
        .put("url", URLUtil.getApplicationURL() + "/Rdiscussion/" + message.instanceId() + "/messages/" + message.messageId()));
  }
}
