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

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.I18nContribution;
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
public class CmisKmeliaContributionsProvider implements CmisContributionsProvider {

  @Inject
  private KmeliaService kmeliaService;

  @Inject
  private OrganizationController controller;

  @Override
  public List<I18nContribution> getAllowedRootContributions(final ResourceIdentifier appId,
      final User user) {
    String kmeliaId = appId.asString();
    if (!controller.isComponentAvailableToUser(kmeliaId, user.getId())) {
      throw new CmisObjectNotFoundException(
          String.format("The application %s doesn't exist or is not accessible to user %s",
              kmeliaId, user.getId()));
    }
    boolean treeEnabled = KmeliaPublicationHelper.isTreeEnabled(kmeliaId);
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID, kmeliaId);
    if (treeEnabled) {
      ContributionIdentifier rootId = ContributionIdentifier.from(root, NodeDetail.TYPE);
      return getAllowedContributionsInFolder(rootId, user);
    } else {
      String profile = kmeliaService.getUserTopicProfile(root, user.getId());
      return kmeliaService.getPublicationsOfFolder(root, profile, user.getId(), treeEnabled)
          .stream()
          .map(KmeliaPublication::getDetail)
          .collect(Collectors.toList());
    }
  }

  @Override
  public List<I18nContribution> getAllowedContributionsInFolder(final ContributionIdentifier folder,
      final User user) {
    boolean treeEnabled = KmeliaPublicationHelper.isTreeEnabled(folder.getComponentInstanceId());
    NodePK folderPK = toNodePK(folder);
    try {
      String profile = kmeliaService.getUserTopicProfile(folderPK, user.getId());
      Stream<? extends I18nContribution> publications =
          kmeliaService.getPublicationsOfFolder(folderPK, profile, user.getId(), treeEnabled)
              .stream()
              .filter(k -> !k.isAlias())
              .map(KmeliaPublication::getDetail);
      Stream<? extends I18nContribution> subFolders =
          kmeliaService.getFolderChildren(folderPK, user.getId())
              .stream()
              .filter(n -> !KmeliaHelper.isToValidateFolder(n.getNodePK().getId()))
              .filter(n -> !n.isUnclassified() && !n.isBin());

      return Stream.concat(publications, subFolders).collect(Collectors.toList());
    } catch (Exception e) {
      throw new CmisObjectNotFoundException(e.getMessage());
    }
  }

  private NodePK toNodePK(final ContributionIdentifier identifier) {
    return new NodePK(identifier.getLocalId(), identifier.getComponentInstanceId());
  }
}
  