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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.community.control;

import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.SilverpeasList;

import java.time.Instant;
import java.util.Collections;
import java.util.function.Function;

import static org.silverpeas.components.community.model.MembershipStatus.REFUSED;
import static org.silverpeas.components.community.model.MembershipStatus.REMOVED;

/**
 * UI item for a {@link User} instance.
 */
public class HistoryItemUIEntity extends MemberUIEntity {

  HistoryItemUIEntity(final CommunityMembership data) {
    super(data, Collections.emptySet());
  }

  public Instant refusedOn() {
    return REFUSED.equals(getData().getStatus()) ? getData().getLastUpdateDate().toInstant() : null;
  }

  public Instant leftOn() {
    return REMOVED.equals(getData().getStatus()) ? getData().getLastUpdateDate().toInstant() : null;
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * User}.
   * @param values the list of {@link User}.
   * @return the {@link SilverpeasList} of {@link HistoryItemUIEntity}.
   */
  public static <U extends CommunityMembership> SilverpeasList<HistoryItemUIEntity> convertHistoryList(
      final SilverpeasList<U> values) {
    final Function<CommunityMembership, HistoryItemUIEntity> converter = HistoryItemUIEntity::new;
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }
}