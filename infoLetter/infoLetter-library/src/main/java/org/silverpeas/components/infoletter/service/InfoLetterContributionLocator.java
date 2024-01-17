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

package org.silverpeas.components.infoletter.service;

import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.core.contribution.ContributionLocatorByLocalIdAndType;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;

import javax.inject.Named;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;

/**
 * This is an implementation of {@link ContributionLocatorByLocalIdAndType} which is able to locate
 * {@link Contribution} of following types:
 * <ul>
 * <li>{@link InfoLetterPublicationPdC}</li>
 * </ul>
 * @author silveryocha
 */
@Named
public class InfoLetterContributionLocator implements ContributionLocatorByLocalIdAndType {

  private static final List<String> HANDLED_TYPES = singletonList(InfoLetterPublicationPdC.TYPE);

  @Override
  public boolean isContributionLocatorOfType(final String type) {
    return HANDLED_TYPES.contains(type);
  }

  @Override
  public Optional<ContributionIdentifier> getContributionIdentifierFromLocalIdAndType(
      final String localId, final String type) {
    if (InfoLetterPublicationPdC.TYPE.equals(type)) {
      final InfoLetterService service = InfoLetterService.get();
      final IdPK pk = new IdPK(localId);
      return Optional.ofNullable(service.getInfoLetterPublication(pk))
          .map(InfoLetterPublicationPdC::getIdentifier);
    }
    return empty();
  }
}
