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

package org.silverpeas.components.forums.service;

import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.core.contribution.ContributionLocatorByLocalIdAndType;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import javax.inject.Named;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * This is an implementation of {@link ContributionLocatorByLocalIdAndType} which is able to locate
 * {@link Contribution} of following types:
 * <ul>
 * <li>{@link Forum}</li>
 * <li>{@link ForumDetail}</li>
 * </ul>
 * @author silveryocha
 */
@Named
public class ForumsContributionLocator implements ContributionLocatorByLocalIdAndType {

  private static final List<String> HANDLED_TYPES = singletonList(Forum.RESOURCE_TYPE);

  @Override
  public boolean isContributionLocatorOfType(final String type) {
    return HANDLED_TYPES.contains(type);
  }

  @Override
  public Optional<ContributionIdentifier> getContributionIdentifierFromLocalIdAndType(
      final String localId, final String type) {
    if (Forum.RESOURCE_TYPE.equals(type)) {
      final ForumService service = ForumService.get();
      final String instanceId = service.getForumInstanceId(Integer.parseInt(localId));
      return ofNullable(service.getForum(new ForumPK(instanceId, localId)))
          .map(Forum::getIdentifier);
    }
    return empty();
  }
}
