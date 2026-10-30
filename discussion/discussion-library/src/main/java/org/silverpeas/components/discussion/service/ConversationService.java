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
import java.util.Map;

/** Dedicated conversation engine, independent of comments and instant messaging. */
public interface ConversationService {
  List<String> participantIds(String instanceId);
  Map<String, LikeState> likeStates(String instanceId, List<String> messageIds);
  LikeState setLike(String instanceId, String messageId, boolean liked);
  MessagePage historyAround(String instanceId, String messageId);
  MessagePage history(String instanceId, long before, int limit);
  void publish(String instanceId, String body);
  void edit(String instanceId, String messageId, long version, String body);
  void delete(String instanceId, String messageId, long version);
  void markRead(String instanceId, long sequence);
}
