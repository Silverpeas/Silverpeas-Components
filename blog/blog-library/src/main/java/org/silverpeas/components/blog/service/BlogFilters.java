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

package org.silverpeas.components.blog.service;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class allowing to define several filters for blog post fetching.
 * @author silveryocha
 */
public class BlogFilters {
  private final boolean getDraftIfNotCreator;
  private int maxResult = 0;
  private String creatorId = null;

  /**
   * Mandatory constructor.
   * @param getDraftIfNotCreator true to get
   */
  public BlogFilters(boolean getDraftIfNotCreator) {
    this.getDraftIfNotCreator = getDraftIfNotCreator;
  }

  /**
   * Sets the identifier of the creator.
   * <p>
   *   The creator is a registered Silverpeas user.
   * </p>
   * @param creatorId identifier of a user.
   * @return itself.
   */
  public BlogFilters withCreatorId(final String creatorId) {
    this.creatorId = creatorId;
    return this;
  }

  /**
   * Sets the maximum result if result must be limited.
   * @param maxResult positive integer to set a maximum result, 0 or negative means no limit.
   * @return itself.
   */
  public BlogFilters withMaxResult(final int maxResult) {
    this.maxResult = maxResult;
    return this;
  }

  /**
   * Gets the predicate to apply on publication list.
   * @return the {@link Predicate} instance.
   */
  public Predicate<PublicationDetail> toPredicate() {
    return p -> !p.isDraft() || getDraftIfNotCreator || p.getCreatorId().equals(creatorId);
  }

  /**
   * Gets the maximum of results if any.
   * @return an optional positive integer.
   */
  public Optional<Integer> getMaxResult() {
    return Optional.of(maxResult).filter(l -> l > 0);
  }
}
