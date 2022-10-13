/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums.model;

import org.silverpeas.core.util.ContributionPath;
import org.silverpeas.core.util.Pair;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * List of {@link Message} which represents a path.
 * @author silveryocha
 */
public class MessagePath extends ContributionPath<Message> {
  private static final long serialVersionUID = 6399724102076584647L;

  private final ForumPath forumPath;

  public MessagePath(final ForumPath forumPath, final @Nonnull Collection<Message> c) {
    super(c);
    this.forumPath = forumPath;
  }

  @Override
  protected boolean isRoot(final Message message) {
    return message.isSubject();
  }

  @Override
  protected String getLabel(final Message message, final String language) {
    return message.getTitle();
  }

  @Override
  public String format(final String language, final boolean fullSpacePath) {
    return forumPath.format(language, fullSpacePath) + formatMessagePath(language);
  }

  private String formatMessagePath(final String language) {
    final String currentResourceIdPath =
        stream().map(Message::getIdAsString).collect(Collectors.joining(","));
    Pair<String, String> lastPath =
        lastPathByLanguage.computeIfAbsent(language, l -> Pair.of("", ""));
    if (!currentResourceIdPath.equals(lastPath.getFirst())) {
      final StringBuilder result = new StringBuilder();
      for (Message message : this) {
        result.insert(0, Constants.DEFAULT_SEPARATOR + getLabel(message, language));
      }
      lastPath = Pair.of(currentResourceIdPath, result.toString());
      lastPathByLanguage.put(language, lastPath);
    }
    return lastPath.getSecond();
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
