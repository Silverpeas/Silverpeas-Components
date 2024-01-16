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

package org.silverpeas.components.quickinfo.model;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.ComponentInstanceContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * Contribution manager centralization about the kmelia contributions.
 * @author silveryocha
 */
@Service
@Named("quickinfo" + ComponentInstanceContributionManager.Constants.NAME_SUFFIX)
public class QuickInfoInstanceContributionManager implements ComponentInstanceContributionManager {

  @Override
  public Optional<Contribution> getById(final ContributionIdentifier contributionId) {
    if (News.CONTRIBUTION_TYPE.equals(contributionId.getType())) {
      final String localId = contributionId.getLocalId();
      return Optional.ofNullable(QuickInfoService.get().getNews(localId));
    }
    throw new IllegalStateException(
        MessageFormat.format("type {0} is not handled", contributionId.getType()));
  }
}
