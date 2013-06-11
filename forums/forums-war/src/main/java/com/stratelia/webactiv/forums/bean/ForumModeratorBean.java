/*
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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.bean;

import com.silverpeas.util.comparator.AbstractComplexComparator;
import com.stratelia.webactiv.forums.models.Moderator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 17/06/13
 */
public class ForumModeratorBean {

  private int forumId;
  private Collection<Moderator> moderators;
  private Collection<String> specificModerators;
  private Collection<String> inheritedModerators;

  /**
   * Initializing a bean that handle moderators of a forum.
   * @param forumId
   * @param moderators
   * @return
   */
  public static ForumModeratorBean from(int forumId, List<Moderator> moderators) {
    Collection<String> specificModerators = new ArrayList<String>(moderators.size());
    Collection<String> inheritedModerators = new ArrayList<String>(moderators.size());
    Collections.sort(moderators, new AbstractComplexComparator<Moderator>() {
      @Override
      protected ValueBuffer getValuesToCompare(final Moderator moderator) {
        return new ValueBuffer().append(moderator.isByInheritance(), false)
            .append(moderator.getUser().getDisplayedName());
      }
    });
    for (Moderator moderator : moderators) {
      if (moderator.isByInheritance()) {
        inheritedModerators.add(moderator.getUserId());
      } else {
        specificModerators.add(moderator.getUserId());
      }
    }
    return new ForumModeratorBean(forumId, moderators, specificModerators, inheritedModerators);
  }

  /**
   * Default constructor.
   * @param forumId
   * @param moderators
   * @param specificModerators
   * @param inheritedModerators
   */
  private ForumModeratorBean(final int forumId, final Collection<Moderator> moderators,
      final Collection<String> specificModerators, final Collection<String> inheritedModerators) {
    this.forumId = forumId;
    this.moderators = moderators;
    this.specificModerators = specificModerators;
    this.inheritedModerators = inheritedModerators;
  }

  /**
   * Gets the identifier of handled forum.
   * @return
   */
  public int getForumId() {
    return forumId;
  }

  /**
   * Indicates id given user (represented by its identifier) is a moderator of the forum.
   * @param userId
   * @return
   */
  public boolean isModerator(String userId) {
    return specificModerators.contains(userId) || inheritedModerators.contains(userId);
  }

  /**
   * Gets the moderator of the forum (inherited and specifics)
   * @return
   */
  public Collection<Moderator> getModerators() {
    return moderators;
  }
}
