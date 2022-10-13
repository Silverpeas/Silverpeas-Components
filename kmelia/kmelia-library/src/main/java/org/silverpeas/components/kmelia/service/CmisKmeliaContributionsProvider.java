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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.service;

import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisServiceUnavailableException;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.component.model.SilverpeasSharedComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.CoreContributionType;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.PublicationAccessControl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

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

  @Inject
  private PublicationAccessControl accessControl;

  @Override
  public List<I18nContribution> getAllowedRootContributions(final ResourceIdentifier appId,
      final User user) {
    String kmeliaId = appId.asString();
    checkKmeliaAccessible(kmeliaId, user);
    boolean treeEnabled = KmeliaPublicationHelper.isTreeEnabled(kmeliaId);
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID, kmeliaId);
    if (treeEnabled) {
      ContributionIdentifier rootId = ContributionIdentifier.from(root, NodeDetail.TYPE);
      return getAllowedContributionsInFolder(rootId, user);
    } else {
      String profile = kmeliaService.getUserTopicProfile(root, user.getId());
      return kmeliaService.getAuthorizedPublicationsOfFolder(root, profile, user.getId(), false)
          .stream()
          .map(KmeliaPublication::getDetail)
          .collect(Collectors.toList());
    }
  }

  @Override
  public List<I18nContribution> getAllowedContributionsInFolder(final ContributionIdentifier folder,
      final User user) {
    String kmeliaId = folder.getComponentInstanceId();
    checkKmeliaAccessible(kmeliaId, user);
    boolean treeEnabled = KmeliaPublicationHelper.isTreeEnabled(kmeliaId);
    NodePK folderPK = toNodePK(folder);
    try {
      String profile = kmeliaService.getUserTopicProfile(folderPK, user.getId());
      Stream<? extends I18nContribution> publications;
      if (!folderPK.isRoot() || KmeliaPublicationHelper.isPublicationsOnRootAllowed(kmeliaId)) {
        var pubsInFolder = kmeliaService.getAuthorizedPublicationsOfFolder(folderPK, profile, user.getId(), treeEnabled)
            .stream()
            .filter(not(KmeliaPublication::isAlias).and(KmeliaPublication::isVisible))
            .map(KmeliaPublication::getDetail)
            .collect(Collectors.toList());
        var drafts = accessControl.filterAuthorizedByUser(user.getId(),
                pubsInFolder.stream()
                    .filter(this::isInDraft)
                    .collect(Collectors.toList()), AccessControlContext.init()
                    .onOperationsOf(AccessControlOperation.MODIFICATION))
            .collect(Collectors.toMap(PublicationDetail::getId, p -> {
              PublicationPK pk = new PublicationPK(p.getCloneId(), p.getInstanceId());
              return kmeliaService.getPublicationDetail(pk);
            }));
        publications = pubsInFolder.stream()
            .map(p -> drafts.getOrDefault(p.getId(), p));
      } else {
        publications = Stream.empty();
      }

      var subFolders =
          kmeliaService.getFolderChildren(folderPK, user.getId())
              .stream()
              .filter(n -> !KmeliaHelper.isToValidateFolder(n.getNodePK()
                  .getId()))
              .filter(n -> !n.isUnclassified() && !n.isBin() && !n.getId()
                  .equals(KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS));

      return Stream.concat(publications, subFolders)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new CmisObjectNotFoundException(e.getMessage());
    }
  }

  @Override
  public I18nContribution getContribution(final ContributionIdentifier contributionId,
      final User user) {
    if (contributionId.getType().equals(NodeDetail.TYPE)) {
      return getNodeDetail(contributionId, user);
    } else if (contributionId.getType().equals(PublicationDetail.TYPE)) {
      return getPublicationDetail(contributionId, user);
    }
    throw new CmisNotSupportedException("Don't support such contribution types " +
        contributionId.getType());
  }

  @Override
  @Transactional
  public I18nContribution createContribution(final I18nContribution contribution,
      final ResourceIdentifier appId, final String language) {
    User user = User.getCurrentRequester();
    String kmeliaId = appId.asString();
    checkKmeliaAccessible(kmeliaId, user);

    ContributionIdentifier rootFolder =
        ContributionIdentifier.from(kmeliaId, NodePK.ROOT_NODE_ID, CoreContributionType.NODE);
    return createContributionInFolder(contribution, rootFolder, language);
  }

  @Override
  @Transactional
  public I18nContribution createContributionInFolder(final I18nContribution contribution,
      final ContributionIdentifier folder, final String language) {
    if (contribution.getIdentifier().getType().equals(PublicationDetail.TYPE)) {
      PublicationDetail publication = toPublicationDetail(contribution, language);
      String id = kmeliaService.createPublicationIntoTopic(publication, toNodePK(folder));
      PublicationDetail saved = publication.copy();
      saved.setPk(new PublicationPK(id, publication.getInstanceId()));
      return publication;
    }
    throw new CmisNotSupportedException(
        String.format("CMIS creation of %s isn't yet supported in Kmelia",
            contribution.getContributionType()));
  }

  private NodePK toNodePK(final ContributionIdentifier identifier) {
    return new NodePK(identifier.getLocalId(), identifier.getComponentInstanceId());
  }

  private PublicationPK toPublicationPK(final ContributionIdentifier identifier) {
    return new PublicationPK(identifier.getLocalId(), identifier.getComponentInstanceId());
  }

  private PublicationDetail toPublicationDetail(final I18nContribution contribution,
      final String language) {
    PublicationDetail publication;
    if (contribution instanceof PublicationDetail) {
      publication = (PublicationDetail) contribution;
    } else {
      PublicationPK pk = new PublicationPK(contribution.getIdentifier()
          .getLocalId(), contribution.getIdentifier()
          .getComponentInstanceId());
      publication = PublicationDetail.builder(language)
          .setPk(pk)
          .setNameAndDescription(contribution.getName(), contribution.getDescription())
          .setImportance(1)
          .created(contribution.getCreationDate(), contribution.getCreator()
              .getId())
          .updated(contribution.getLastUpdateDate(), contribution.getLastUpdater()
              .getId())
          .build();
    }
    return publication;
  }

  private void checkKmeliaAccessible(String kmeliaId, User user) {
    if (controller.getComponentInstance(kmeliaId)
        .filter(SilverpeasSharedComponentInstance.class::isInstance)
        .map(SilverpeasSharedComponentInstance.class::cast)
        .filter(i -> i.getName()
            .equals("kmelia") && i.canBeAccessedBy(user))
        .isEmpty()) {
      throw new CmisObjectNotFoundException(
          String.format("The application %s doesn't exist or is not accessible to user %s",
              kmeliaId, user.getId()));
    }
  }

  private boolean isInDraft(final PublicationDetail publication) {
    return publication.isValid() && publication.haveGotClone() && !publication.isClone();
  }

  private NodeDetail getNodeDetail(final ContributionIdentifier id, final User user) {
    NodeDetail node;
    try {
      node = kmeliaService.getNodeHeader(id.getLocalId(), id.getComponentInstanceId());
      if (node == null) {
        throw new KmeliaRuntimeException("");
      }
    } catch (Exception e) {
      throw new CmisObjectNotFoundException(String.format("Folder %s not found!", id.asString()));
    }

    if (node.isBin() || node.isUnclassified() || KmeliaHelper.isToValidateFolder(node.getId()) ||
        node.getId().equals(KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS)) {
      throw new CmisPermissionDeniedException("Forbidden access to special folders!");
    }

    if (!node.canBeAccessedBy(user)) {
      throw new CmisPermissionDeniedException("Forbidden access to folder " + id.asString());
    }

    return node;
  }

  private PublicationDetail getPublicationDetail(final ContributionIdentifier id, final User user) {
    try {
      PublicationDetail publication = kmeliaService.getPublicationDetail(toPublicationPK(id));
      if (publication == null) {
        throw new CmisObjectNotFoundException(String.format("Publication %s not found!",
            id.asString()));
      }
      if (!publication.canBeAccessedBy(user)) {
        throw new CmisPermissionDeniedException("Forbidden access to publication " + id.asString());
      }
      if (isInDraft(publication) &&
        accessControl.isUserAuthorized(user.getId(), publication,
            AccessControlContext.init().onOperationsOf(AccessControlOperation.MODIFICATION))) {
        PublicationPK pk = new PublicationPK(publication.getCloneId(), publication.getInstanceId());
        return kmeliaService.getPublicationDetail(pk);
      }
      return publication;
    } catch (Exception e) {
      throw new CmisServiceUnavailableException(e.getMessage());
    }
  }
}
  