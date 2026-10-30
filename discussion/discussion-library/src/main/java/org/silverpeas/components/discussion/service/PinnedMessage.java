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

import org.silverpeas.components.discussion.model.DiscussionMessage;

/** Sidebar snapshot with readable mentions and a Unicode-safe excerpt. */
public record PinnedMessage(String id, String author, String excerpt) {
  // JSP EL resolves JavaBean properties rather than record component methods.
  public String getId() { return id; }
  public String getAuthor() { return author; }
  public String getExcerpt() { return excerpt; }

  public static PinnedMessage from(DiscussionMessage message) {
    String text = DiscussionMentionService.displayText(message.getBody());
    int length = text.codePointCount(0, text.length());
    String excerpt = length > 180 ? text.substring(0, text.offsetByCodePoints(0, 180)) + "…" : text;
    var creator = message.getCreator();
    return new PinnedMessage(message.getId(), creator == null ? "" : creator.getDisplayedName(), excerpt);
  }
}
