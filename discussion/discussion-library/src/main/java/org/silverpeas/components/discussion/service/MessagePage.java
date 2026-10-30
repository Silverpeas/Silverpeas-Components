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
package org.silverpeas.components.discussion.service;

import java.util.List;
import org.silverpeas.components.discussion.model.DiscussionMessage;

/** Descending history page; nextBefore is exclusive and stable under concurrent appends. */
public record MessagePage(List<DiscussionMessage> messages, long nextBefore, boolean hasMore,
                          long lastSequence, long unreadCount) {
  public MessagePage {
    messages = List.copyOf(messages);
  }
  public List<DiscussionMessage> getMessages() { return messages; }
  public List<DiscussionMessage> getChronologicalMessages() { return messages.reversed(); }
  public long getNextBefore() { return nextBefore; }
  public boolean isHasMore() { return hasMore; }
  public long getLastSequence() { return lastSequence; }
  public long getUnreadCount() { return unreadCount; }
}
