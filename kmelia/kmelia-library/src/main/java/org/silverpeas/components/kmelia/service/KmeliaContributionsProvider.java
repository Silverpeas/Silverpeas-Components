/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.components.kmelia.service;

import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Providers of the user contributions managed in a Kmelia instance and to expose in the CMIS
 * objects tree of Silverpeas.
 * @author mmoquillon
 */
@Service
@Named("kmelia" + CmisContributionsProvider.Constants.NAME_SUFFIX)
public class KmeliaContributionsProvider implements CmisContributionsProvider {

  @Inject
  private KmeliaService kmeliaService;

  /**
   * The root contributions are either the publications for tree disabled kmelia instances or
   * the root folder.
   * @param appId the unique identifier of a Kmelia application.
   * @param user a user in Silverpeas.
   * @return either a list with the root {@link org.silverpeas.core.node.model.NodeDetail} instance
   * or a list of all publications.
   */
  @Override
  public List<ContributionIdentifier> getAllowedRootContributions(final String appId,
      final User user) {
    boolean treeEnabled = KmeliaPublicationHelper.isTreeEnabled(appId);
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID, appId);
    if (treeEnabled) {
      ContributionIdentifier rootId = ContributionIdentifier.from(root, NodeDetail.TYPE);
      return getAllowedContributionsInFolder(rootId, user);
    } else {
      String profile = kmeliaService.getUserTopicProfile(root, user.getId());
      List<KmeliaPublication> publications =
          kmeliaService.getPublicationsOfFolder(root, profile, user.getId(), treeEnabled);
      return publications.stream()
          .map(p -> p.getDetail().getContributionId())
          .collect(Collectors.toList());
    }
  }

  @Override
  public List<ContributionIdentifier> getAllowedContributionsInFolder(
      final ContributionIdentifier folder, final User user) {
    boolean treeEnabled = KmeliaPublicationHelper.isTreeEnabled(folder.getComponentInstanceId());
    NodePK folderPK = toNodePK(folder);
    String profile = kmeliaService.getUserTopicProfile(folderPK, user.getId());
    Stream<ContributionIdentifier> publications =
        kmeliaService.getPublicationsOfFolder(folderPK, profile, user.getId(), treeEnabled)
            .stream()
            .map(p -> p.getDetail().getContributionId());
    Stream<ContributionIdentifier> subFolders =
        kmeliaService.getFolderChildren(folderPK, user.getId())
            .stream()
            .filter(n -> !KmeliaHelper.isToValidateFolder(n.getNodePK().getId()))
            .filter(n -> !n.isUnclassified() && !n.isBin())
            .map(NodeDetail::getContributionId);

    return Stream.concat(publications, subFolders).collect(Collectors.toList());
  }

  private NodePK toNodePK(final ContributionIdentifier identifier) {
    return new NodePK(identifier.getLocalId(), identifier.getComponentInstanceId());
  }
}
  