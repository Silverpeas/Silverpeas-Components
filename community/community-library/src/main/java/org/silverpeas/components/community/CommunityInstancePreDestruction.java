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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community;

import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.model.WysiwygContent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.Optional;

/**
 * Wipe out for the spawned community instance the resources that were allocated to him.
 */
@Bean
@Named
public class CommunityInstancePreDestruction implements ComponentInstancePreDestruction {

  @Inject
  private CommunityOfUsersRepository communitiesRepository;
  @Inject
  private CommunityMembershipRepository membersRepository;

  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    communitiesRepository.getByComponentInstanceId(componentInstanceId).ifPresent(c -> {
      Optional.ofNullable(c.getSpacePresentationContent())
          .map(WysiwygContent::getContribution)
          .ifPresent(WysiwygContent::deleteAllContents);
      membersRepository.getMembershipsTable(c).deleteAll();
      communitiesRepository.delete(c);
    });
  }
}