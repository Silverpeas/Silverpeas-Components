/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.components.kmelia.KmeliaContentManager;
import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.PublicationImport;
import org.silverpeas.components.kmelia.model.KmaxRuntimeException;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.model.TopicComparator;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.components.kmelia.model.ValidatorsList;
import org.silverpeas.components.kmelia.notification.*;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.RemovedSpaceAndComponentInstanceChecker;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.notification.AttachmentRef;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.dao.DistributionTreeCriteria;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationLink;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.template.form.dao.ModelDAO;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailException;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailServiceProvider;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.coordinates.service.CoordinatesService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionManager;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.TodoDetail;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.process.annotation.SimulationActionProcess;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.components.kmelia.model.KmeliaPublication.fromDetail;
import static org.silverpeas.components.kmelia.notification.KmeliaDelayedVisibilityUserNotificationReminder.KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION;
import static org.silverpeas.components.kmelia.service.KmeliaHelper.isToolbox;
import static org.silverpeas.components.kmelia.service.KmeliaOperationContext.OperationType.*;
import static org.silverpeas.components.kmelia.service.KmeliaServiceContext.*;
import static org.silverpeas.core.admin.component.model.ComponentInst.getComponentLocalId;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;
import static org.silverpeas.core.cache.service.CacheAccessorProvider.getThreadCacheAccessor;
import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;
import static org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController.deleteWysiwygAttachmentsOnly;
import static org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController.haveGotWysiwyg;
import static org.silverpeas.core.node.model.NodePK.UNDEFINED_NODE_ID;
import static org.silverpeas.core.notification.system.ResourceEvent.Type.MOVE;
import static org.silverpeas.core.persistence.Transaction.getTransaction;
import static org.silverpeas.core.security.authorization.AccessControlOperation.MODIFICATION;
import static org.silverpeas.kernel.util.StringUtil.*;

/**
 * This is the Kmelia Service controller of the MVC. It controls all the activities that happen in a
 * client session. It also provides mechanisms to access other services. Service which manage kmelia
 * and kmax application.
 * @author Nicolas Eysseric
 */
@Service
@Singleton
@Named("kmeliaService")
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultKmeliaService implements KmeliaService {

  private static final String MESSAGES_PATH = "org.silverpeas.kmelia.multilang.kmeliaBundle";
  private static final String SETTINGS_PATH = "org.silverpeas.kmelia.settings.kmeliaSettings";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);
  private static final String UNKNOWN = "unknown";
  private static final String PUBLICATION = "Publication";
  private static final String USELESS = "useless";
  private static final String NODE_PREFIX = "Node_";
  private static final String ADMIN_ROLE = "admin";
  private static final String ALIASES_CACHE_KEY = "NEW_PUB_ALIASES";
  private static final Predicate<PublicationDetail> HAS_CLONE = k -> k.isValid() &&
      k.haveGotClone() && !k.isClone();
  @Inject
  private NodeService nodeService;
  @Inject
  private PublicationService publicationService;
  @Inject
  private StatisticService statisticService;
  @Inject
  private PdcManager pdcManager;
  @Inject
  private CoordinatesService coordinatesService;
  @Inject
  private CommentService commentService;
  @Inject
  private AdminController adminController;
  @Inject
  private SilverpeasCalendar calendar;
  @Inject
  private PdcClassificationService pdcClassificationService;
  @Inject
  private PdcSubscriptionManager pdcSubscriptionManager;
  @Inject
  private KmeliaContentManager kmeliaContentManager;

  private int getNbPublicationsOnRoot(String componentId) {
    String parameterValue =
        getOrganisationController().getComponentParameterValue(componentId, "nbPubliOnRoot");
    if (isDefined(parameterValue)) {
      return Integer.parseInt(parameterValue);
    } else {
      if (isToolbox(componentId)) {

        return 0;
      }
      // lecture du properties
      SettingBundle theSettings = getComponentSettings();
      return theSettings.getInteger("HomeNbPublications");
    }
  }

  private boolean isDraftModeUsed(String componentId) {
    return StringUtil.getBooleanValue(
        getOrganisationController().getComponentParameterValue(componentId, "draft"));
  }

  @Override
  public TopicDetail goTo(NodePK pk, String userId, boolean isTreeStructureUsed, String userProfile,
      boolean mustUserRightsBeChecked) {
    Collection<NodeDetail> newPath = new ArrayList<>();
    NodeDetail nodeDetail;

    // get the basic information (Header) of this topic
    try {
      nodeDetail = nodeService.getDetail(pk);
      if (mustUserRightsBeChecked) {
        if (!NodeAccessControl.get().isUserAuthorized(userId, nodeDetail)) {
          nodeDetail.setUserRole("noRights");
        }
        List<NodeDetail> availableChildren = getAllowedSubfolders(nodeDetail, userId);
        nodeDetail.setChildrenDetails(availableChildren);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    // get publications
    List<KmeliaPublication> pubDetails =
        getAuthorizedPublicationsOfFolder(pk, userProfile, userId, isTreeStructureUsed);

    // get the path to this topic
    if (pk.isRoot()) {
      newPath.add(nodeDetail);
    } else {
      newPath = getPathFromAToZ(nodeDetail);
    }

    // set the currentTopic and return it
    return new TopicDetail(newPath, nodeDetail, pubDetails);
  }

  @Override
  public List<KmeliaPublication> getAuthorizedPublicationsOfFolder(NodePK pk, String userProfile,
      String userId, boolean isTreeStructureUsed) {
    final long start = System.currentTimeMillis();
    final Collection<PublicationDetail> pubDetails;
    // get the publications associated to this topic
    if (pk.isTrash()) {
      // Topic = Basket
      pubDetails = getPublicationsInBasket(pk, userProfile, userId);
    } else if (pk.isRoot()) {
      try {
        int nbPublisOnRoot = getNbPublicationsOnRoot(pk.getInstanceId());
        if (nbPublisOnRoot == 0 || !isTreeStructureUsed || isToolbox(pk.getInstanceId())) {
          pubDetails = publicationService.getDetailsByFatherPK(pk, "P.pubUpdateDate desc", false);
        } else {
          return getLatestAuthorizedPublications(pk.getInstanceId(), userId, nbPublisOnRoot);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException(e);
      }
    } else {
      try {
        // get the publication details linked to this topic
        pubDetails =
            publicationService.getDetailsByFatherPK(pk, "P.pubUpdateDate DESC, P.pubId DESC",
                false);
      } catch (Exception e) {
        throw new KmeliaRuntimeException(e);
      }
    }
    final List<KmeliaPublication> result = filterPublications(
        asLocatedKmeliaPublication(pk, pubDetails), pk.getInstanceId(),
        SilverpeasRole.fromString(userProfile), userId);
    SilverLogger.getLogger(this)
        .debug(() -> format("getting {0} publications of folder {1} in {2}",
            result.size(), pk, formatDurationHMS(System.currentTimeMillis() - start)));
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<KmeliaPublication> getLatestAuthorizedPublications(String instanceId, String userId,
      int limit) {
    final long start = System.currentTimeMillis();
    final List<KmeliaPublication> result = getThreadCacheAccessor().getCache()
        .computeIfAbsent(
            "KmeliaService:getLatestAuthorizedPublications:" + instanceId + ":" + userId + ":" +
                limit, List.class, () -> {
              final List<PublicationDetail> pubDetails =
                  publicationService.getAuthorizedPublicationsForUserByCriteria(userId,
                      PublicationCriteria.excludingTrashNodeOnComponentInstanceIds(instanceId)
                          .excludingNodes(NodePK.ROOT_NODE_ID)
                          .ofStatus(PublicationDetail.VALID_STATUS)
                          .visibleAt(OffsetDateTime.now())
                          .takingAliasesIntoAccount()
                          .orderByDescendingBeginDate()
                          .limitTo(limit));
              return asKmeliaPublication(pubDetails);
            });
    String count = result == null ? "" : String.valueOf(result.size());
    SilverLogger.getLogger(this)
        .debug(() -> format("getting {0} latest authorized publications of instance {1} in {2}",
            count, instanceId, formatDurationHMS(System.currentTimeMillis() - start)));
    return result;
  }

  @Override
  public List<NodeDetail> getAllowedSubfolders(NodeDetail folder, String userId) {
    final List<NodeDetail> children = (List<NodeDetail>) folder.getChildrenDetails();
    final List<NodeDetail> availableChildren = new ArrayList<>();
    for (final NodeDetail child : children) {
      final NodePK childId = child.getNodePK();
      if (childId.isTrash() || childId.isUnclassed() || !child.haveRights()) {
        availableChildren.add(child);
      } else {
        addAccordingToRights(userId, availableChildren, child);
      }
    }
    return availableChildren;
  }

  private void addAccordingToRights(final String userId, final List<NodeDetail> availableChildren,
      final NodeDetail child) {
    final String rightsDependsOn = child.getRightsDependsOn();
    final boolean nodeAvailable = getOrganisationController().isObjectAvailableToUser(
        ProfiledObjectId.fromNode(rightsDependsOn), child.getNodePK().getInstanceId(), userId);
    if (nodeAvailable) {
      availableChildren.add(child);
    } else { // check if at least one descendant is available
      Iterator<NodeDetail> descendants = nodeService.getDescendantDetails(child).iterator();
      addDescendantIfAvailable(userId, availableChildren, child, rightsDependsOn, descendants);
    }
  }

  private void addDescendantIfAvailable(final String userId,
      final List<NodeDetail> availableChildren, final NodeDetail child,
      final String rightsDependsOn, final Iterator<NodeDetail> descendants) {
    boolean childAllowed = false;
    while (!childAllowed && descendants.hasNext()) {
      NodeDetail descendant = descendants.next();
      if (!descendant.getRightsDependsOn().equals(rightsDependsOn) &&
          getOrganisationController().isObjectAvailableToUser(
              ProfiledObjectId.fromNode(descendant.getRightsDependsOn()),
              descendant.getNodePK().getInstanceId(), userId)) {
        // different rights of father check if it is available
        childAllowed = true;
        if (!availableChildren.contains(child)) {
          availableChildren.add(child);
        }
      }
    }
  }

  private Collection<NodeDetail> getPathFromAToZ(NodeDetail nd) {
    Collection<NodeDetail> newPath = new ArrayList<>();
    try {
      List<NodeDetail> pathInReverse = nodeService.getPath(nd.getNodePK());
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return newPath;
  }

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   * @param fatherPK the topic Id of the future father
   * @param subTopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see NodeDetail
   * @see NodePK
   */
  @Override
  public NodePK addToTopic(NodePK fatherPK, NodeDetail subTopic) {
    NodePK theNodePK;
    try {
      NodeDetail fatherDetail = nodeService.getHeader(fatherPK);
      theNodePK = nodeService.createNode(subTopic, fatherDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return theNodePK;
  }

  @Override
  public NodePK addSubTopic(NodePK fatherPK, NodeDetail subTopic, String alertType) {
    // Construction de la date de création (date courante)
    subTopic.setCreationDate(new Date());
    // Web visibility parameter. The topic is by default invisible.
    subTopic.setStatus("Invisible");
    // add new topic to current topic
    NodePK pk = addToTopic(fatherPK, subTopic);
    // Creation alert
    if (!UNDEFINED_NODE_ID.equals(pk.getId())) {
      subTopic.setNodePK(pk);
      subTopic.setFatherPK(fatherPK);
      topicCreationAlert(subTopic, NotifAction.CREATE, alertType);
    }
    return pk;
  }

  /**
   * Alert all users, only publishers or nobody of the topic creation or update
   * @param alertType alertType = "All"|"Publisher"|"None"
   * @see NodePK
   * @since 1.0
   */
  private void topicCreationAlert(final NodeDetail node, NotifAction action,
      final String alertType) {
    UserNotificationHelper.buildAndSend(new KmeliaTopicUserNotification(node, action, alertType));
  }

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   * @param topic the NodeDetail of the updated sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see NodeDetail
   * @see NodePK
   * @since 1.0
   */
  @Override
  public NodePK updateTopic(NodeDetail topic, String alertType) {
    // Order of the node must be unchanged
    NodeDetail oldNode = nodeService.getHeader(topic.getNodePK());
    int order = oldNode.getOrder();
    topic.setOrder(order);
    nodeService.setDetail(topic);

    // manage operations relative to folder rights
    if (isRightsOnTopicsEnabled(topic.getNodePK().getInstanceId())) {
      updateNode(topic, oldNode);
    }

    // Update Alert
    topic.setFatherPK(oldNode.getFatherPK());
    topicCreationAlert(topic, NotifAction.UPDATE, alertType);
    return topic.getNodePK();
  }

  private void updateNode(final NodeDetail newNode, final NodeDetail oldNode) {
    if (!oldNode.getRightsDependsOn().equals(newNode.getRightsDependsOn())) {
      // rights dependency have changed
      if (!newNode.haveRights()) {

        NodeDetail father = nodeService.getHeader(oldNode.getFatherPK());
        newNode.setRightsDependsOn(father.getRightsDependsOn());

        // Topic profiles must be removed
        List<ProfileInst> profiles = adminController.getProfilesByObject(
            ProfiledObjectId.fromNode(newNode.getNodePK().getId()),
            newNode.getNodePK().getInstanceId());
        deleteProfiles(profiles);
      } else {
        newNode.setRightsDependsOnMe();
      }
      nodeService.updateRightsDependency(newNode);
    }
  }

  private void deleteProfiles(final List<ProfileInst> profiles) {
    for (ProfileInst profile : profiles) {
      if (profile != null) {
        adminController.deleteProfileInst(profile.getId());
      }
    }
  }

  @Override
  public NodeDetail getSubTopicDetail(NodePK pk) {
    NodeDetail subTopic;
    // get the basic information (Header) of this topic
    try {
      subTopic = nodeService.getDetail(pk);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return subTopic;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteTopic(NodePK pkToDelete) {
    try {
      // get all nodes which will be deleted
      final Collection<NodePK> nodesToDelete = nodeService.getDescendantPKs(pkToDelete);
      nodesToDelete.add(pkToDelete);
      for (final NodePK oneNodeToDelete : nodesToDelete) {
        // get pubs linked to current node (includes alias)
        final Collection<PublicationDetail> pubsToCheck =
            publicationService.getDetailsByFatherPK(oneNodeToDelete);
        // check each pub contained in current node
        for (PublicationDetail onePubToCheck : pubsToCheck) {
          final KmeliaPublication kmeliaPub = fromDetail(onePubToCheck, oneNodeToDelete);
          if (!kmeliaPub.isAlias()) {
            // delete definitively the publication
            deletePublication(kmeliaPub.getPk());
          } else {
            // remove only the alias
            final Collection<Location> aliases = singletonList(kmeliaPub.getLocation());
            publicationService.removeAliases(kmeliaPub.getPk(), aliases);
          }
        }
      }

      // Delete the topic
      nodeService.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public void changeTopicStatus(String newStatus, NodePK nodePK, boolean recursiveChanges) {
    try {
      if (!recursiveChanges) {
        NodeDetail nodeDetail = nodeService.getHeader(nodePK);
        changeTopicStatus(newStatus, nodeDetail);
      } else {
        List<NodeDetail> subTree = nodeService.getSubTree(nodePK);
        for (NodeDetail aSubTree : subTree) {
          changeTopicStatus(newStatus, aSubTree);
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public void sortSubTopics(NodePK fatherPK, boolean recursive, String[] criteria) {
    List<NodeDetail> subTopics;
    try {
      subTopics = (List<NodeDetail>) nodeService.getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }

    if (subTopics != null && !subTopics.isEmpty()) {
      subTopics.sort(new TopicComparator(criteria));
      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        NodeDetail nodeDetail = subTopics.get(i);
        try {
          nodeDetail.setOrder(i);
          nodeService.setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException(e);
        }
        if (recursive) {
          sortSubTopics(nodeDetail.getNodePK(), true, criteria);
        }
      }
    }
  }

  private void changeTopicStatus(String newStatus, NodeDetail topic) {
    try {
      topic.setStatus(newStatus);
      nodeService.setDetail(topic);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public List<NodeDetail> getTreeview(NodePK nodePK, String profile, boolean coWritingEnable,
      boolean draftVisibleWithCoWriting, String userId, boolean displayNb,
      boolean isRightsOnTopicsUsed) {
    final long start = System.currentTimeMillis();

    String instanceId = nodePK.getInstanceId();
    List<NodeDetail> allowedTree = nodeService.getSubTree(nodePK);

    if (profile == null) {
      profile = getProfile(userId, nodePK);
    }

    KmeliaUserTreeViewFilter.from(userId, instanceId, nodePK, profile, isRightsOnTopicsUsed)
        .setBestUserRoleAndFilter(allowedTree);

    if (displayNb) {
      buildTreeView(nodePK, profile, coWritingEnable, draftVisibleWithCoWriting, userId,
          allowedTree);
    }

    SilverLogger.getLogger(this)
        .debug(() -> format("getting {0} nodes from folder {1} in {2}",
            allowedTree.size(), nodePK, formatDurationHMS(System.currentTimeMillis() - start)));

    return allowedTree;
  }

  private void buildTreeView(final NodePK nodePK, final String profile,
      final boolean coWritingEnable, final boolean draftVisibleWithCoWriting, final String userId,
      final List<NodeDetail> allowedTree) {
    boolean checkVisibility = false;
    StringBuilder statusSubQuery = new StringBuilder();
    if (profile == null || profile.equals("user")) {
      checkVisibility = true;
      statusSubQuery.append(" AND sb_publication_publi.pubStatus = 'Valid' ");
    } else if (profile.equals("writer")) {
      statusSubQuery.append(" AND (");
      if (coWritingEnable && draftVisibleWithCoWriting) {
        statusSubQuery.append("sb_publication_publi.pubStatus = 'Valid' OR ")
            .append("sb_publication_publi.pubStatus = 'Draft' OR ")
            .append("sb_publication_publi.pubStatus = 'Unvalidate' ");
      } else {
        checkVisibility = true;
        statusSubQuery.append("sb_publication_publi.pubStatus = 'Valid' OR ")
            .append("(sb_publication_publi.pubStatus = 'Draft' AND ")
            .append("sb_publication_publi.pubUpdaterId = '")
            .append(userId)
            .append("') OR (sb_publication_publi.pubStatus = 'Unvalidate' AND ")
            .append("sb_publication_publi.pubUpdaterId = '")
            .append(userId)
            .append("') ");
      }
      statusSubQuery.append("OR (sb_publication_publi.pubStatus = 'ToValidate' ")
          .append("AND sb_publication_publi.pubUpdaterId = '")
          .append(userId)
          .append("') ");
      statusSubQuery.append("OR sb_publication_publi.pubUpdaterId = '").append(userId).append("')");
    } else {
      statusSubQuery.append(" AND (");
      if (coWritingEnable && draftVisibleWithCoWriting) {
        statusSubQuery.append("sb_publication_publi.pubStatus IN ('Valid','ToValidate','Draft') ");
      } else {
        if (profile.equals("publisher")) {
          checkVisibility = true;
        }
        statusSubQuery.append("sb_publication_publi.pubStatus IN ('Valid','ToValidate') OR " +
                "(sb_publication_publi" +
                ".pubStatus = 'Draft' AND sb_publication_publi.pubUpdaterId = '")
            .append(userId)
            .append("') ");
      }
      statusSubQuery.append("OR sb_publication_publi.pubUpdaterId = '").append(userId).append("')");
    }
    final DistributionTreeCriteria criteria = DistributionTreeCriteria
        .onInstanceId(nodePK.getInstanceId())
        .withVisibilityCheck(checkVisibility)
        .withManualStatusFilter(statusSubQuery.toString());
    final Map<String, Integer> numbers = publicationService.getDistributionTree(criteria);

    // set right number of publications in basket
    NodePK trashPk = new NodePK(NodePK.BIN_NODE_ID, nodePK.getInstanceId());
    int nbPubsInTrash = getPublicationsInBasket(trashPk, profile, userId).size();
    numbers.put(NodePK.BIN_NODE_ID, nbPubsInTrash);

    decorateWithNumberOfPublications(allowedTree, numbers);
  }

  private void decorateWithNumberOfPublications(List<NodeDetail> nodes,
      Map<String, Integer> numbers) {
    for (NodeDetail node : nodes) {
      decorateWithNumberOfPublications(node, numbers);
    }
  }

  private int decorateWithNumberOfPublications(NodeDetail node, Map<String, Integer> numbers) {
    Integer nb = numbers.get(node.getNodePK().getId());
    for (NodeDetail child : node.getChildrenDetails()) {
      nb += decorateWithNumberOfPublications(child, numbers);
    }
    node.setNbObjects(nb);
    return nb;
  }

  private Collection<PublicationDetail> getPublicationsInBasket(NodePK pk, String userProfile,
      String userId) {
    String currentUserId = userId;
    try {
      // Give the publications associated to basket topic and visibility period expired
      if (SilverpeasRole.ADMIN.isInRole(userProfile)) {// Admin can see all Publis in the basket.
        currentUserId = null;
      }
      return publicationService.getDetailsByFatherPK(pk, null, false, currentUserId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private List<KmeliaPublication> asLocatedKmeliaPublication(NodePK fatherPK,
      Collection<PublicationDetail> pubDetails) {
    final Collection<String> pubIds = pubDetails.stream()
        .map(PublicationDetail::getId)
        .collect(Collectors.toSet());
    final Map<String, List<Location>> locationsByPublication =
        publicationService.getAllLocationsByPublicationIds(pubIds);
    return pubDetails.stream()
        .map(p -> KmeliaPublication.fromDetail(p, fatherPK, locationsByPublication))
        .collect(Collectors.toList());
  }

  private List<KmeliaPublication> asKmeliaPublication(Collection<PublicationDetail> pubDetails) {
    return pubDetails.stream().map(KmeliaPublication::fromDetail).collect(Collectors.toList());
  }

  /**
   * Return the detail of a publication (only the Header)
   * @param pubPK the id of the publication
   * @return a PublicationDetail
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  public PublicationDetail getPublicationDetail(PublicationPK pubPK) {
    try {
      return publicationService.getDetail(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  /**
   * Return list of all path to this publication - it's a Collection of NodeDetail collection
   * @param pubPK the id of the publication
   * @return a Collection of NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<Collection<NodeDetail>> getPathList(PublicationPK pubPK) {
    Collection<NodePK> fatherPKs;
    try {
      // get PK of all nodes which contain this publication
      fatherPKs = publicationService.getAllFatherPKInSamePublicationComponentInstance(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    try {
      List<Collection<NodeDetail>> pathList = new ArrayList<>();
      if (fatherPKs != null) {
        // For each topic, get the path to it
        for (NodePK pk : fatherPKs) {
          Collection<NodeDetail> path = nodeService.getPath(pk);
          // add this path
          pathList.add(path);
        }
      }
      return pathList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  /**
   * Create a new Publication (only the header - parameters) to the current Topic
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK) {
    PdcClassification predefinedClassification =
        pdcClassificationService.findAPreDefinedClassification(fatherPK.getId(),
            fatherPK.getInstanceId());
    return createPublicationIntoTopic(pubDetail, fatherPK, predefinedClassification);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK,
      PdcClassification classification) {
    final String pubId;
    KmeliaOperationContext.about(CREATION);
    try {
      pubId = createPublicationIntoTopicWithoutNotifications(pubDetail, fatherPK, classification);

      // creates todos for publishers
      createTodosForPublication(pubDetail, true);

      // alert supervisors
      sendAlertToSupervisors(fatherPK, pubDetail);

      // alert subscribers
      sendSubscriptionsNotification(pubDetail, NotifAction.CREATE, false);

    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return pubId;
  }

  private String createPublicationIntoTopicWithoutNotifications(PublicationDetail pubDetail,
      NodePK fatherPK, PdcClassification classification) {
    PublicationPK pubPK;
    try {
      PublicationDetail detail = changePublicationStatusOnCreation(pubDetail, fatherPK);
      // create the publication
      pubPK = publicationService.createPublication(detail);
      detail.getPK().setId(pubPK.getId());
      // register the new publication as a new content to content manager
      createSilverContent(detail, detail.getCreatorId());
      // add this publication to the current topic
      addPublicationToTopicWithoutNotifications(pubPK, fatherPK, true);
      // classify the publication on the PdC if its classification is defined
      // subscribers are notified later (only if publication is valid)
      classification.classifyContent(detail, false);

      createdIntoRequestContext(detail);

    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return pubPK.getId();
  }

  private String getProfile(String userId, NodePK nodePK) {
    OrganizationController orgCtrl = getOrganisationController();
    if (isRightsOnTopicsEnabled(nodePK.getInstanceId())) {
      NodeDetail topic = nodeService.getHeader(nodePK);
      if (topic.haveRights()) {
        ProfiledObjectId nodeId = ProfiledObjectId.fromNode(topic.getRightsDependsOn());
        return KmeliaHelper.getProfile(
            orgCtrl.getUserProfiles(userId, nodePK.getInstanceId(), nodeId));
      }
    }
    return KmeliaHelper.getProfile(getUserRoles(nodePK.getInstanceId(), userId));
  }

  private String getProfileOnPublication(String userId, PublicationPK pubPK) {
    final NodePK fatherPK = getPublicationFatherPK(pubPK);
    return getProfileForDirectNodeOfPublication(userId, pubPK, fatherPK);
  }

  private String getProfileForDirectNodeOfPublication(final String userId,
      final PublicationPK pubPK, final NodePK fatherPK) {
    final String profile;
    if (fatherPK != null) {
      profile = getProfile(userId, fatherPK);
    } else {
      // peculiar case in which the publication isn't in any node: this situation occurs if the
      // publication is definitely deleted or orphaned. In that case, we take the profile of the
      // user in the concerned kmelia instance. This shouldn't occur!
      SilverLogger.getLogger(this).warn("The publication {0} is orphaned!", pubPK);
      //noinspection ConstantConditions
      profile = SilverpeasRole.getHighestFrom(SilverpeasRole.fromStrings(
          getOrganisationController().getUserProfiles(userId, pubPK.getInstanceId()))).getName();

    }
    return profile;
  }

  private PublicationDetail changePublicationStatusOnCreation(PublicationDetail pubDetail,
      NodePK nodePK) {
    String status = pubDetail.getStatus();
    if (!isDefined(status)) {
      status = PublicationDetail.TO_VALIDATE_STATUS;

      boolean draftModeUsed = isDraftModeUsed(pubDetail.getPK().getInstanceId());

      if (draftModeUsed) {
        status = PublicationDetail.DRAFT_STATUS;
      } else {
        String profile = getProfile(pubDetail.getCreatorId(), nodePK);
        if (SilverpeasRole.PUBLISHER.isInRole(profile) || SilverpeasRole.ADMIN.isInRole(profile)) {
          status = PublicationDetail.VALID_STATUS;
        }
      }
    }
    pubDetail.setStatus(status);
    KmeliaHelper.checkIndex(pubDetail);
    return pubDetail;
  }

  private Pair<Boolean, String> changePublicationStatusOnMove(PublicationDetail pub, NodePK to,
      final String currentUserId) {
    String oldStatus = pub.getStatus();
    String status = pub.getStatus();
    if (!status.equals(PublicationDetail.DRAFT_STATUS)) {
      status = PublicationDetail.TO_VALIDATE_STATUS;
      String profile = getProfile(currentUserId, to);
      if (SilverpeasRole.PUBLISHER.isInRole(profile) || SilverpeasRole.ADMIN.isInRole(profile)) {
        status = PublicationDetail.VALID_STATUS;
      }
    }
    pub.setStatus(status);
    KmeliaHelper.checkIndex(pub);
    return Pair.of(!oldStatus.equals(status), status);
  }

  private boolean changePublicationStatusOnUpdate(PublicationDetail pubDetail) {
    final String previousStatus = pubDetail.getStatus();
    final String newStatus;
    final NodePK father = getPublicationFatherPK(pubDetail.getPK());
    if (pubDetail.isStatusMustBeChecked() && !pubDetail.isDraft() && !pubDetail.isClone()) {
      newStatus = setPublicationStatus(pubDetail, previousStatus, father);
    } else {
      newStatus = previousStatus;
    }

    KmeliaHelper.checkIndex(pubDetail);

    if (father == null || father.isTrash()) {
      // the publication is in the trash
      pubDetail.setIndexOperation(IndexManager.NONE);
    }
    return !previousStatus.equalsIgnoreCase(newStatus);
  }

  private String setPublicationStatus(final PublicationDetail pubDetail, final String newStatus,
      final NodePK father) {
    final String profile =
        getProfileForDirectNodeOfPublication(pubDetail.getUpdaterId(), pubDetail.getPK(), father);
    final String status;
    if (SilverpeasRole.WRITER.isInRole(profile)) {
      status = PublicationDetail.TO_VALIDATE_STATUS;
    } else if (pubDetail.isRefused() &&
        (SilverpeasRole.ADMIN.isInRole(profile) || SilverpeasRole.PUBLISHER.isInRole(profile))) {
      status = PublicationDetail.VALID_STATUS;
    } else {
      status = newStatus;
    }
    pubDetail.setStatus(status);
    return status;
  }

  /**
   * Update a publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  public void updatePublication(PublicationDetail pubDetail) {
    updatePublication(pubDetail, KmeliaHelper.PUBLICATION_HEADER, false);
  }

  @Override
  public void updatePublication(final PublicationDetail pubDetail,
      final PdcClassification classification) {
    updatePublication(pubDetail, classification, KmeliaHelper.PUBLICATION_HEADER, false);
  }

  @Override
  public void updatePublication(PublicationDetail pubDetail, boolean forceUpdateDate) {
    updatePublication(pubDetail, KmeliaHelper.PUBLICATION_HEADER, forceUpdateDate);
  }

  private void updatePublication(PublicationDetail pubDetail, int updateScope,
      boolean forceUpdateDate) {
    updatePublication(pubDetail, null, updateScope, forceUpdateDate);
  }

  private void updatePublication(PublicationDetail pubDetail, PdcClassification classification,
      int updateScope, boolean forceUpdateDate) {
    KmeliaOperationContext.about(UPDATE);
    try {
      // if pubDetail is a clone
      boolean isClone = pubDetail.isClone();
      PublicationDetail old = getPublicationDetail(pubDetail.getPK());

      // prevents to lose some data
      if (StringUtil.isDefined(old.getTargetValidatorId()) &&
          !StringUtil.isDefined(pubDetail.getTargetValidatorId())) {
        pubDetail.setTargetValidatorId(old.getTargetValidatorId());
      }
      final boolean isPublicationInBasket = isPublicationInBasket(pubDetail.getPK());
      if (isClone) {
        // update only updateDate
        publicationService.setDetail(pubDetail, forceUpdateDate);
        performValidatorChanges(old, pubDetail);
      } else {
        boolean statusChanged = changePublicationStatusOnUpdate(pubDetail);
        publicationService.setDetail(pubDetail, forceUpdateDate);

        if (!isPublicationInBasket) {
          updatePublicationContent(pubDetail, updateScope, old, statusChanged);
        }
      }

      if (classification != null) {
        // classify the publication on the PdC if any
        // subscribers are notified later (only if publication is valid)
        classification.classifyContentOrClearClassificationIfEmpty(pubDetail, false);
      }

      // Sending a subscription notification if the publication updated comes not from the
      // basket, has not been created or already updated from the same request
      if (!isPublicationInBasket && !hasPublicationBeenCreatedFromRequestContext(pubDetail) &&
          !hasPublicationBeenUpdatedFromRequestContext(pubDetail)) {
        sendSubscriptionsNotification(pubDetail, NotifAction.UPDATE, false);
      }


      updatedIntoRequestContext(pubDetail);

    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void updatePublicationContent(final PublicationDetail pubDetail, final int updateScope,
      final PublicationDetail old, final boolean statusChanged) {
    if (statusChanged) {
      // creates todos for publishers
      this.createTodosForPublication(pubDetail, false);
    } else {
      performValidatorChanges(old, pubDetail);
    }

    updateSilverContentVisibility(pubDetail);

    // la publication a été modifié par un superviseur
    // le créateur de la publi doit être averti
    String profile = KmeliaHelper.getProfile(
        getUserRoles(pubDetail.getPK().getInstanceId(), pubDetail.getUpdaterId()));
    if ("supervisor".equals(profile)) {
      sendModificationAlert(updateScope, pubDetail.getPK());
    }

    boolean visibilityPeriodUpdated = isVisibilityPeriodUpdated(pubDetail, old);

    if (statusChanged || visibilityPeriodUpdated) {
      if (KmeliaHelper.isIndexable(pubDetail)) {
        indexExternalElementsOfPublication(pubDetail);
      } else {
        unIndexExternalElementsOfPublication(pubDetail.getPK());
      }
    }
  }

  /**
   * Performs the treatments associated to changes about set of validators linked to a publication.
   * @param previousPublication the publication data (or clone data) before changes.
   * @param currentPublication the publication data (or clone data if previousPublication is a
   * clone) containing the changes.
   */
  private void performValidatorChanges(final PublicationDetail previousPublication,
      final PublicationDetail currentPublication) {

    // The publication (or the clone) must be into "ToValidate" state, and validator identifiers
    // must exist.
    if (currentPublication.isValidationRequired() &&
        currentPublication.getTargetValidatorIds() != null) {

      // Getting validator identifiers from previous and current data.
      List<String> oldValidatorIds = Arrays.asList(previousPublication.getTargetValidatorIds());
      List<String> newValidatorIds = Arrays.asList(currentPublication.getTargetValidatorIds());

      // Computing identifiers of removed validators, and the ones of added validators.
      List<String> toRemoveToDo = new ArrayList<>(oldValidatorIds);
      toRemoveToDo.removeAll(newValidatorIds);
      ValidatorsList toAlert = new ValidatorsList(getActiveValidatorIds(currentPublication));
      toAlert.removeAll(oldValidatorIds);

      // Performing the actions.
      removeTodoForPublication(currentPublication.getPK(), toRemoveToDo);
      addTodoAndSendNotificationToValidators(currentPublication, toAlert);
    }
  }

  private boolean isVisibilityPeriodUpdated(PublicationDetail pubDetail, PublicationDetail old) {
    boolean beginVisibilityPeriodUpdated = (
        (pubDetail.getBeginDate() != null && old.getBeginDate() == null) ||
            (pubDetail.getBeginDate() == null && old.getBeginDate() != null) ||
            (pubDetail.getBeginDate() != null && old.getBeginDate() != null &&
                !pubDetail.getBeginDate().equals(old.getBeginDate())));
    boolean endVisibilityPeriodUpdated = (
        (pubDetail.getEndDate() != null && old.getEndDate() == null) ||
            (pubDetail.getEndDate() == null && old.getEndDate() != null) ||
            (pubDetail.getEndDate() != null && old.getEndDate() != null &&
                !pubDetail.getEndDate().equals(old.getEndDate())));
    return beginVisibilityPeriodUpdated || endVisibilityPeriodUpdated;
  }

  @Transactional
  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void movePublication(@SourcePK PublicationPK pubPK, @TargetPK NodePK to,
      KmeliaPasteDetail pasteContext) {
    final NodePK fromNode = pasteContext.getFromPK();
    final KmeliaPublication publication = KmeliaPublication.withPK(pubPK, fromNode);
    if (publication.isAlias()) {
      final Location newLocation = new Location(to.getId(), to.getInstanceId());
      newLocation.setAsAlias(pasteContext.getUserId());
      publicationService.removeAliases(pubPK, singletonList(publication.getLocation()));
      publicationService.addAliases(pubPK, singletonList(newLocation));
    } else if (fromNode.getInstanceId().equals(to.getInstanceId())) {
      movePublicationInSameApplication(publication.getDetail(), to, pasteContext);
    } else {
      movePublicationInAnotherApplication(publication.getDetail(), to, pasteContext);
    }
  }

  @Transactional
  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void movePublicationInSameApplication(@SourcePK PublicationPK pubPK, @TargetPK NodePK from,
      KmeliaPasteDetail pasteContext) {
    PublicationDetail pub = getPublicationDetail(pubPK);
    String userId = pasteContext.getUserId();
    NodePK to = pasteContext.getToPK();

    // check if user can cut publication from source folder
    String profile = getUserTopicProfile(from, userId);
    @SuppressWarnings("removal") boolean cutAllowed = KmeliaPublicationHelper.isCanBeCut(
        from.getComponentName(), userId, profile, pub.getCreator());

    // check if user can paste publication into target folder
    String profileInTarget = getUserTopicProfile(to, userId);
    boolean pasteAllowed = KmeliaPublicationHelper.isCreationAllowed(to, profileInTarget);

    if (cutAllowed && pasteAllowed) {
      movePublicationInSameApplication(pub, to, pasteContext);
    }
  }

  private void movePublicationInSameApplication(PublicationDetail pub, NodePK to,
      KmeliaPasteDetail pasteContext) {
    if (to.isTrash()) {
      sendPublicationInBasket(pub.getPK());
    } else {
      // update parent
      publicationService.movePublication(pub.getPK(), to, false);
      pub.setTargetValidatorId(pasteContext.getTargetValidatorIds());
      processPublicationAfterMove(pub, to, pasteContext.getUserId());
    }
  }

  /**
   * Move a publication to another component. Moving in this order : <ul>
   * <li>moving the metadata</li>
   * <li>moving the thumbnail</li>
   * <li>moving the content</li>
   * <li>moving the wysiwyg</li>
   * <li>moving the images linked to the wysiwyg</li>
   * <li>moving the xml form content (files and images)</li>
   * <li>moving attachments</li>
   * <li>moving the pdc poistion</li>
   * <li>moving the statistics</li>
   * </ul>
   */
  private void movePublicationInAnotherApplication(PublicationDetail pub, NodePK to,
      KmeliaPasteDetail pasteContext) {
    try {
      ResourceReference fromResourceReference = new ResourceReference(pub.getPK());
      String fromComponentId = pub.getInstanceId();
      ResourceReference toPubliResourceReference =
          new ResourceReference(pub.getId(), to.getInstanceId());

      // remove index relative to publication
      unIndexExternalElementsOfPublication(pub.getPK());

      // move thumbnail
      ThumbnailController.moveThumbnail(fromResourceReference, toPubliResourceReference);

      moveAdditionalFiles(pub, fromResourceReference, toPubliResourceReference);

      // change images path in wysiwyg
      WysiwygController.wysiwygPlaceHaveChanged(fromResourceReference.getInstanceId(),
          pub.getPK().getId(), to.getInstanceId(), pub.getPK().getId());

      // move regular files
      List<SimpleDocument> docs = getAttachmentService().listDocumentsByForeignKeyAndType(
          fromResourceReference, DocumentType.attachment, null);
      for (SimpleDocument doc : docs) {
        getAttachmentService().moveDocument(doc, toPubliResourceReference);
      }

      // move form content
      String infoId = pub.getInfoId();
      if (infoId != null && !"0".equals(infoId)) {
        // register content to component
        PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
        GenericRecordSet toRecordSet = templateManager.addDynamicPublicationTemplate(
            to.getInstanceId() + ":" + pub.getInfoId(), pub.getInfoId() + ".xml");
        RecordTemplate toRecordTemplate = toRecordSet.getRecordTemplate();

        // get xmlContent to move
        PublicationTemplate pubTemplateFrom =
            templateManager.getPublicationTemplate(fromComponentId + ":" + pub.getInfoId());

        RecordSet set = pubTemplateFrom.getRecordSet();
        set.move(fromResourceReference, toPubliResourceReference, toRecordTemplate);
      }

      // move comments
      getCommentService().moveComments(PublicationDetail.getResourceType(), fromResourceReference,
          toPubliResourceReference);

      // move pdc positions
      // Careful! positions must be moved according to taxonomy restrictions of target application
      int fromSilverObjectId = getSilverObjectId(pub.getPK());
      // get positions of cutted publication
      List<ClassifyPosition> positions = pdcManager.getPositions(fromSilverObjectId,
          fromComponentId);

      // delete taxonomy data relative to moved publication
      deleteSilverContent(pub.getPK());

      // move statistics
      statisticService.moveStat(toPubliResourceReference, 1, PUBLICATION);

      // move publication itself
      publicationService.movePublication(pub.getPK(), to, false);
      pub.getPK().setComponentName(to.getInstanceId());

      pub.setTargetValidatorId(pasteContext.getTargetValidatorIds());

      processPublicationAfterMove(pub, to, pasteContext.getUserId());

      // index moved publication
      if (KmeliaHelper.isIndexable(pub)) {
        indexPublication(pub);
      }

      // reference pasted publication on taxonomy service
      int toSilverObjectId = getSilverObjectId(pub.getPK());
      // add original positions to pasted publication
      pdcManager.addPositions(positions, toSilverObjectId, to.getInstanceId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void moveAdditionalFiles(final @SourcePK PublicationDetail pub,
      final ResourceReference fromResourceReference,
      final ResourceReference toPubliResourceReference) {
    try {
      // move additional files
      List<SimpleDocument> documents =
          getAttachmentService().listDocumentsByForeignKeyAndType(fromResourceReference,
              DocumentType.image, null);
      documents.addAll(
          getAttachmentService().listDocumentsByForeignKeyAndType(fromResourceReference,
              DocumentType.wysiwyg, null));
      for (SimpleDocument doc : documents) {
        getAttachmentService().moveDocument(doc, toPubliResourceReference);
      }
    } catch (AttachmentException e) {
      SilverLogger.getLogger(this)
          .error("Cannot move attachments of publication {0}", new String[]{pub.getPK().getId()},
              e);
    }
  }

  private void processPublicationAfterMove(PublicationDetail pub, NodePK to, String currentUserId) {
    // status must be checked according to topic rights and last modifier (current user)
    final Pair<Boolean, String> statusChanges = changePublicationStatusOnMove(pub, to,
        currentUserId);
    final boolean statusChanged = statusChanges.getFirst();
    // update publication
    if (!statusChanged) {
      pub.setUpdateDataMustBeSet(false);
    } else if (!PublicationDetail.VALID_STATUS.equals(statusChanges.getSecond())) {
      // update last modifier
      pub.setUpdaterId(currentUserId);
    }
    publicationService.setDetail(pub, false, MOVE);

    // check visibility on taxonomy
    updateSilverContentVisibility(pub);

    if (statusChanged) {
      // creates todos for publishers
      createTodosForPublication(pub, false);
      // index or unindex external elements
      if (KmeliaHelper.isIndexable(pub)) {
        indexExternalElementsOfPublication(pub);
      } else {
        unIndexExternalElementsOfPublication(pub.getPK());
      }
    }
    // send notifications like a publish action
    sendSubscriptionsNotification(pub, NotifAction.PUBLISHED, false);
  }

  @Override
  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId) {
    externalElementsOfPublicationHaveChanged(pubPK, userId, true);
  }

  private void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId,
      boolean indexExternalElements) {
    // check if related contribution is managed by kmelia
    if (pubPK == null || StringUtil.isNotDefined(pubPK.getInstanceId()) ||
        (!pubPK.getInstanceId().startsWith("kmelia") &&
            !pubPK.getInstanceId().startsWith("toolbox") &&
            !pubPK.getInstanceId().startsWith("kmax"))) {
      return;
    }

    PublicationConcernedByUpdate publicationConcernedByUpdate = new PublicationConcernedByUpdate(
        pubPK).invoke();
    if (publicationConcernedByUpdate.isPublicationNotDefined()) {
      return;
    }

    PublicationDetail pubDetail = publicationConcernedByUpdate.getPubDetail();
    boolean isPublicationInBasketBeforeUpdate =
        publicationConcernedByUpdate.isPublicationInBasketBeforeUpdate();


    if (pubDetail.isClone()) {
      pubDetail.setIndexOperation(IndexManager.NONE);
    }

    if (isDefined(userId)) {
      pubDetail.setUpdaterId(userId);
    }

    // update publication header to store last modifier and update date
    if (!isDefined(userId)) {
      updatePublication(pubDetail, KmeliaHelper.PUBLICATION_CONTENT, false);
    } else {
      updatePublicationAccordingToProfile(userId, pubDetail);
    }

    if (KmeliaHelper.isIndexable(pubDetail) && !isPublicationInBasketBeforeUpdate) {
      publicationService.createIndex(pubDetail);
    }

    if (indexExternalElements) {
      // index all attached files to taking into account visibility period
      indexExternalElementsOfPublication(pubDetail);
    }
  }

  private void updatePublicationAccordingToProfile(final String userId,
      final PublicationDetail pubDetail) {
    // check if user have sufficient rights to update a publication
    String profile = getProfileOnPublication(userId, pubDetail.getPK());
    if ("supervisor".equals(profile) || SilverpeasRole.PUBLISHER.isInRole(profile) ||
        SilverpeasRole.ADMIN.isInRole(profile) || SilverpeasRole.WRITER.isInRole(profile)) {
      updatePublication(pubDetail, KmeliaHelper.PUBLICATION_CONTENT, false);
    } else {
      SilverLogger.getLogger(this)
          .warn("User {0} not allowed to update publication {1}", userId,
              pubDetail.getPK().getId());
    }
  }

  private boolean isClone(PublicationDetail publication) {
    return isDefined(publication.getCloneId()) &&
        !UNDEFINED_NODE_ID.equals(publication.getCloneId()) &&
        !isDefined(publication.getCloneStatus());
  }

  /**
   * Deletes definitively the specified publication.
   *
   * @param pubPK the unique identifier of the publication to delete.
   */
  private void deletePublication(PublicationPK pubPK) {
    KmeliaOperationContext.about(DELETION);
    try {
      // remove form content
      removeXMLContentOfPublication(pubPK);
      // delete all reading controls associated to this publication
      deleteAllReadingControlsByPublication(pubPK);
      // delete all links
      publicationService.removeAllFathers(pubPK);
      // delete the publication
      publicationService.removePublication(pubPK);
      // delete reference to contentManager
      deleteSilverContent(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }

  }

  private void sendPublicationInBasket(PublicationPK pubPK, boolean kmaxMode) {
    KmeliaOperationContext.about(REMOVING);
    try {
      // remove coordinates for Kmax
      if (kmaxMode) {
        CoordinatePK coordinatePK =
            new CoordinatePK(UNKNOWN, pubPK.getSpaceId(), pubPK.getComponentName());

        Collection<NodePK> fatherPKs =
            publicationService.getAllFatherPKInSamePublicationComponentInstance(pubPK);
        // delete publication coordinates
        Iterator<NodePK> it = fatherPKs.iterator();
        List<String> coordinates = new ArrayList<>();
        while (it.hasNext()) {
          String coordinateId = (it.next()).getId();
          coordinates.add(coordinateId);
        }
        if (!coordinates.isEmpty()) {
          coordinatesService.deleteCoordinates(coordinatePK, coordinates);
        }
      }

      // remove all links between this publication and topics
      publicationService.removeAllFathers(pubPK);
      // add link between this publication and the basket topic
      publicationService.addFather(pubPK, new NodePK(NodePK.BIN_NODE_ID, pubPK));

      cleanUpPublicationsInBasket(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void cleanUpPublicationsInBasket(PublicationPK pubPK) {
    // remove all the todos attached to the publication
    removeAllTodosForPublication(pubPK);

    // publication is no more accessible
    updateSilverContentVisibility(pubPK);

    unIndexExternalElementsOfPublication(pubPK);
  }

  private void sendPublicationInBasket(PublicationPK pubPK) {
    sendPublicationInBasket(pubPK, KmeliaHelper.isKmax(pubPK.getInstanceId()));
  }

  private void sendTopicInBasket(NodeDetail topic) {
    NodePK trash = new NodePK(NodePK.BIN_NODE_ID,
        topic.getIdentifier().getComponentInstanceId());

    // get all the tree of folders, rooted to the topic, to be sent in the basket with the topic
    final Collection<NodeDetail> children = nodeService.getDescendantDetails(topic.getNodePK());
    for (final NodeDetail childTopic : children) {
      // get all the direct publications in the topic child (including aliases)
      final Collection<PublicationDetail> publications =
          publicationService.getDetailsByFatherPK(childTopic.getNodePK());
      // check each publication: if it is an alias, remove it, otherwise it is moved into the trash
      // with its topic
      for (PublicationDetail publication : publications) {
        final KmeliaPublication kmeliaPublication = fromDetail(publication, childTopic.getNodePK());
        if (kmeliaPublication.isAlias()) {
          // remove only the alias
          final Collection<Location> aliases = singletonList(kmeliaPublication.getLocation());
          publicationService.removeAliases(kmeliaPublication.getPk(), aliases);
        } else {
          cleanUpPublicationsInBasket(publication.getPK());
        }
      }
    }

    nodeService.moveNode(topic.getNodePK(), trash);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK, boolean isACreation) {
    addPublicationToTopicWithoutNotifications(pubPK, fatherPK, isACreation);
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    sendSubscriptionsNotification(pubDetail, NotifAction.CREATE, false);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addPublicationToTopicWithoutNotifications(PublicationPK pubPK, NodePK fatherPK,
      boolean isACreation) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    Optional<Location> mainPubLocation = publicationService.getMainLocation(pubPK);
    if (!isACreation) {
      try {
        if (mainPubLocation.filter(NodePK::isTrash).isPresent()) {
          publicationService.removeFather(pubPK, mainPubLocation.get());
          mainPubLocation = Optional.empty();
          if (PublicationDetail.VALID_STATUS.equalsIgnoreCase(pubDetail.getStatus())) {
            // index publication
            publicationService.createIndex(pubPK);
            // index external elements
            indexExternalElementsOfPublication(pubDetail);
            // publication is accessible again
            updateSilverContentVisibility(pubDetail);
          } else if (PublicationDetail.TO_VALIDATE_STATUS.equalsIgnoreCase(pubDetail.getStatus())) {
            // create validation todos for publishers
            createTodosForPublication(pubDetail, true);
          }
        } else if (mainPubLocation.isEmpty()) {
          // The publi have got no father
          // change the end date to make this publi visible
          pubDetail.setEndDate(null);
          publicationService.setDetail(pubDetail);
          // publication is accessible again
          updateSilverContentVisibility(pubDetail);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException(e);
      }
    }

    try {
      if (mainPubLocation.isPresent()) {
        final Location alias = new Location(fatherPK.getId(), fatherPK.getInstanceId());
        alias.setAsAlias(User.getCurrentRequester().getId());
        publicationService.addAliases(pubPK, singletonList(alias));
      } else {
        publicationService.addFather(pubPK, fatherPK);
      }
      // index publication to index path
      if (pubDetail.isIndexable()) {
        publicationService.createIndex(pubPK);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private boolean isPublicationInBasket(PublicationPK pubPK) {
    return publicationService.getAllLocations(pubPK).stream().anyMatch(NodePK::isTrash);
  }

  @SuppressWarnings("unchecked")
  private NodePK sendSubscriptionsNotification(PublicationDetail pubDetail, NotifAction action,
      final boolean sendOnlyToAliases) {
    NodePK father = null;
    // We alert subscribers only if publication is Valid
    if (!pubDetail.haveGotClone() && pubDetail.isValid() && pubDetail.isVisible()) {
      // Subscription to the main topic
      father = getPublicationFatherPK(pubDetail.getPK());
      if (!sendOnlyToAliases && father != null) {
        sendSubscriptionsNotification(father, pubDetail, action);
      }

      // Subscriptions related to aliases
      Collection<Location> locations = (Collection<Location>) getThreadCacheAccessor().getCache()
          .get(ALIASES_CACHE_KEY);
      if (locations == null) {
        locations = getAliases(pubDetail.getPK());
      }
      sendSubscriptionNotificationForAliases(pubDetail, action, locations);

      // PDC subscriptions
      try {
        int silverObjectId = getSilverObjectId(pubDetail.getPK());
        List<ClassifyPosition> positions = pdcManager.getPositions(silverObjectId,
            pubDetail.getPK().getInstanceId());
        if (positions != null) {
          for (ClassifyPosition position : positions) {
            pdcSubscriptionManager.checkSubscriptions(position.getValues(),
                pubDetail.getPK().getInstanceId(), silverObjectId);
          }
        }
      } catch (PdcException e) {
        SilverLogger.getLogger(this)
            .error("PdC subscriptions notification failure for publication {0}",
                new String[]{pubDetail.getPK().getId()}, e);
      }
    } else {
      KmeliaDelayedVisibilityUserNotificationReminder.get().setAbout(pubDetail);
    }
    return father;
  }

  private void sendSubscriptionNotificationForAliases(final PublicationDetail pubDetail,
      final NotifAction action, final Collection<Location> locations) {
    // Transform the current alias to a NodePK (even if Alias is extending NodePK) in the aim
    // to execute the equals method of NodePK
    locations.stream().filter(Location::isAlias).forEach(a -> {
      // we copy the publication detail to avoid overriding attributes of the original one
      final PublicationDetail copy = pubDetail.copy();
      copy.setAlias(true);
      sendSubscriptionsNotification(a, copy, action);
    });
  }

  private void sendSubscriptionsNotification(NodePK fatherPK, PublicationDetail pubDetail,
      NotifAction action) {
    try {
      // Building and sending the notification
      UserNotificationHelper.buildAndSend(
          new KmeliaSubscriptionPublicationUserNotification(fatherPK, pubDetail, action));

    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Subscriber notification failure about publication {0}",
              new String[]{pubDetail.getPK().getId()}, e);
    }
  }

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addInfoLinks(PublicationPK pubPK, List<ResourceReference> links) {
    try {
      publicationService.addLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    try {
      return publicationService.getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private KmeliaPublication getPublication(PublicationPK pubPK) {
    return KmeliaPublication.withPK(pubPK);
  }

  @Override
  public KmeliaPublication getPublication(final PublicationPK pubPK, final NodePK topicPK) {
    return KmeliaPublication.withPK(pubPK, topicPK);
  }

  @Override
  public TopicDetail getBestTopicDetailOfPublicationForUser(PublicationPK pubPK,
      boolean isTreeStructureUsed, String userId) {
    // fetch one of the publication fathers
    final NodePK fatherPK = getBestLocationOfPublicationForUser(pubPK, userId);
    final String profile = KmeliaHelper.getProfile(getUserRoles(pubPK.getInstanceId(), userId));
    return goTo(fatherPK, userId, isTreeStructureUsed, profile, false);
  }

  @Override
  public NodePK getBestLocationOfPublicationForUser(PublicationPK pubPK, String userId) {
    final Location root = new Location(NodePK.ROOT_NODE_ID, pubPK.getInstanceId());
    return getPublicationLocations(pubPK, false).stream()
        .sorted(comparing(
            (Location l) -> !l.getInstanceId().equals(pubPK.getInstanceId())).thenComparing(
                Location::isAlias)
            .thenComparing(Location::getInstanceId)
            .thenComparing(Location::getId))
        .filter(l -> NodeAccessControl.get().isUserAuthorized(userId, l))
        .findFirst()
        .orElse(root);
  }

  /**
   * Gets all the locations of the publication in the original Kmelia instance.
   * @param pubPK the identifying key of the publication
   * @param inComponentInstance true o get location in component instance only, false to get all
   * @return a collection of {@link Location} objects.
   */
  private List<Location> getPublicationLocations(PublicationPK pubPK,
      final boolean inComponentInstance) {
    final Function<PublicationPK, List<Location>> locationSupplier = pk -> {
      if (inComponentInstance) {
        return publicationService.getLocationsInComponentInstance(pk, pk.getInstanceId());
      } else {
        return publicationService.getAllLocations(pk);
      }
    };
    List<Location> locations = locationSupplier.apply(pubPK);
    if (locations.isEmpty()) {
      // This publication have got no father!
      // Check if it's a clone (a clone have got no father ever)
      final boolean alwaysVisibleModeActivated = getBooleanValue(
          getOrganisationController().getComponentParameterValue(pubPK.getInstanceId(),
              "publicationAlwaysVisible"));
      if (alwaysVisibleModeActivated) {
        final PublicationDetail publi = publicationService.getDetail(pubPK);
        if (publi != null && isClone(publi)) {
          // This publication is a clone
          // Get fathers from main publication
          locations = locationSupplier.apply(publi.getClonePK());
        }
      }
    }
    return locations;
  }

  @Override
  public NodePK getPublicationFatherPK(PublicationPK pubPK) {
    Collection<Location> locations = getPublicationLocations(pubPK, true);
    return locations.stream().filter(l -> !l.isAlias()).findFirst().orElse(null);
  }

  @Override
  public <T extends ResourceReference> List<PublicationDetail> getPublicationDetails(
      List<T> references) {
    try {
      return publicationService.getPublications(references.stream()
          .map(l -> new PublicationPK(l.getId(), l.getInstanceId()))
          .collect(Collectors.toSet()));
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public <T extends ResourceReference> List<KmeliaPublication> getPublications(
      final List<T> references, String userId, final NodePK contextFolder,
      boolean accessControlFiltering) {
    // initialization of the publications list
    final List<PublicationDetail> publications;
    if (accessControlFiltering) {
      final Map<PublicationPK, ResourceReference> indexedReferences = references.stream()
          .collect(toMap(r -> new PublicationPK(r.getId(), r.getInstanceId()), r -> r));
      final List<ResourceReference> authorizedReferences = PublicationAccessControl.get()
          .filterAuthorizedByUser(indexedReferences.keySet(), userId)
          .map(indexedReferences::get)
          .collect(Collectors.toList());
      publications = getPublicationDetails(authorizedReferences);
    } else {
      publications = getPublicationDetails(references);
    }
    return contextFolder != null ?
        asLocatedKmeliaPublication(contextFolder, publications) :
        asKmeliaPublication(publications);
  }

  @Override
  public <T extends ResourceReference> List<Pair<KmeliaPublication, KmeliaPublication>> getPublicationsForModification(
      final List<T> references, final String userId) {
    // initialization of the publications list
    final Map<PublicationPK, ResourceReference> indexedReferences = references.stream()
        .collect(toMap(r -> new PublicationPK(r.getId(), r.getInstanceId()), r -> r));
    final AccessControlContext modificationContext = AccessControlContext.init()
        .onOperationsOf(MODIFICATION);
    final List<ResourceReference> authorizedReferences = PublicationAccessControl.get()
        .filterAuthorizedByUser(indexedReferences.keySet(), userId, modificationContext)
        .map(indexedReferences::get)
        .collect(Collectors.toList());
    final List<PublicationDetail> publications = getPublicationDetails(authorizedReferences);
    final List<String> pubIds = authorizedReferences.stream()
        .map(ResourceReference::getId)
        .collect(Collectors.toList());
    final Map<String, List<Location>> locationsByPublication =
        publicationService.getAllLocationsByPublicationIds(pubIds);
    final Map<PublicationPK, PublicationDetail> clones =
        getPublicationDetails(publications.stream()
            .filter(HAS_CLONE)
            .map(PublicationDetail::getClonePK)
            .collect(Collectors.toList()))
        .stream()
        .collect(toMap(PublicationDetail::getPK, k -> k));
    return publications.stream()
        .map(p -> KmeliaPublication.fromDetail(p, null, locationsByPublication))
        .map(k -> ofNullable(HAS_CLONE.test(k.getDetail()) ? clones.getOrDefault(k.getDetail().getClonePK(), null) : null)
            .map(c -> Pair.of(k, KmeliaPublication.fromDetail(c, k.getLocation(), locationsByPublication)))
            .orElseGet(() -> Pair.of(k, null)))
        .collect(Collectors.toList());
  }

  /**
   * Gets the publications linked with the specified one and for which the specified user is
   * authorized to access.
   * @param publication the publication from which linked publications are get.
   * @param userId the unique identifier of a user. It allows to check if a linked publication is
   * accessible for the specified user.
   * @return a list of Kmelia publications.
   * @ if an error occurs while communicating with the remote business service.
   */
  @Override
  public List<KmeliaPublication> getLinkedPublications(KmeliaPublication publication,
      String userId) {
    List<PublicationLink> allLinks = publication.getCompleteDetail().getLinkedPublications(userId);
    List<KmeliaPublication> authorizedLinks = new ArrayList<>();
    for (PublicationLink link : allLinks) {
      authorizedLinks.add(KmeliaPublication.withPK(link.getPubPK()));
    }
    return authorizedLinks;
  }

  @Override
  public List<KmeliaPublication> getPublicationsToValidate(String componentId, String userId) {
    Collection<PublicationDetail> publications = new ArrayList<>();
    try {
      Collection<PublicationDetail> temp = publicationService.getPublicationsByCriteria(
          PublicationCriteria.onComponentInstanceIds(componentId)
              .ofStatus(PublicationDetail.TO_VALIDATE_STATUS)
              .orderByDescendingLastUpdateDate());
      // only publications which must be validated by current user must be returned
      for (PublicationDetail publi : temp) {
        addPublicationsToValidate(userId, publications, publi);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return asKmeliaPublication(publications);
  }

  private void addPublicationsToValidate(final String userId,
      final Collection<PublicationDetail> publications, final PublicationDetail publi) {
    boolean isClone = publi.isValidationRequired() && !UNDEFINED_NODE_ID.equals(publi.getCloneId());
    if (isClone) {
      if (isUserCanValidatePublication(publi.getPK(), userId)) {
        // publication to validate is a clone, get original one
        try {
          PublicationDetail original =
              getPublicationDetail(new PublicationPK(publi.getCloneId(), publi.getPK()));
          publications.add(original);
        } catch (Exception e) {
          // inconsistency in database! Original publication does not exist
          SilverLogger.getLogger(this)
              .warn("Original publication {0} of clone {1} not found", publi.getId(),
                  publi.getCloneId());
        }
      }
    } else {
      if (isUserCanValidatePublication(publi.getPK(), userId)) {
        publications.add(publi);
      }
    }
  }

  private void sendValidationNotification(final NodePK fatherPK, final PublicationDetail pubDetail,
      final String refusalMotive, final String userIdWhoRefuse) {

    try {

      UserNotificationHelper.buildAndSend(
          new KmeliaValidationPublicationUserNotification(fatherPK, pubDetail, refusalMotive,
              userIdWhoRefuse));

    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("User notification failure about publication {0}",
              new String[]{pubDetail.getPK().getId()}, e);
    }
  }

  private void sendAlertToSupervisors(final NodePK fatherPK, final PublicationDetail pubDetail) {
    if (pubDetail.isValid()) {
      try {
        UserNotificationHelper.buildAndSend(
            new KmeliaSupervisorPublicationUserNotification(fatherPK, pubDetail));
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Supervisors notification failure about publication {0}",
                new String[]{pubDetail.getPK().getId()}, e);
      }
    }
  }

  private int getValidationType(String instanceId) {
    String sParam = getOrganisationController().getComponentParameterValue(instanceId,
        InstanceParameters.validation);
    if (isDefined(sParam)) {
      return Integer.parseInt(sParam);
    }
    return KmeliaHelper.VALIDATION_CLASSIC;
  }

  private boolean isTargetedValidationEnabled(String componentId) {
    int value = getValidationType(componentId);
    return value == KmeliaHelper.VALIDATION_TARGET_N || value == KmeliaHelper.VALIDATION_TARGET_1;
  }

  @Override
  public ValidatorsList getAllValidators(PublicationPK pubPK) {
    // get all users who have to validate
    ValidatorsList allValidators = new ValidatorsList(getValidationType(pubPK.getInstanceId()));
    if (isTargetedValidationEnabled(pubPK.getInstanceId())) {
      allValidators.addAll(getActiveValidatorIds(pubPK));
    } else {
      // It's not a targeted validation
      List<String> roles = new ArrayList<>(2);
      roles.add(SilverpeasRole.ADMIN.getName());
      roles.add(SilverpeasRole.PUBLISHER.getName());

      if (KmeliaHelper.isKmax(pubPK.getInstanceId())) {
        allValidators.addAll(Arrays.asList(
            getOrganisationController().getUsersIdsByRoleNames(pubPK.getInstanceId(), roles)));
      } else {
        // get admin and publishers of all nodes where publication is
        addAdminAndPublishers(pubPK, allValidators, roles);
      }
    }
    return allValidators;
  }

  private void addAdminAndPublishers(final PublicationPK pubPK, final List<String> allValidators,
      final List<String> roles) {
    NodePK father = getPublicationFatherPK(pubPK);
    if (father != null) {
      NodeDetail topic = getNodeHeader(father);
      if (!topic.haveRights()) {
        allValidators.addAll(Arrays.asList(
            getOrganisationController().getUsersIdsByRoleNames(pubPK.getInstanceId(), roles)));
      } else {
        allValidators.addAll(Arrays.asList(
            getOrganisationController().getUsersIdsByRoleNames(pubPK.getInstanceId(),
                ProfiledObjectId.fromNode(topic.getRightsDependsOn()), roles)));
      }
    }
  }

  public void setValidators(PublicationPK pubOrClonePK, String userIds) {
    PublicationDetail publication = getPublicationDetail(pubOrClonePK);

    String[] validatorIds = StringUtil.split(userIds, ',');

    if (!ArrayUtil.isEmpty(validatorIds)) {
      // set new validators in database
      publication.setTargetValidatorId(userIds);
      publication.setStatusMustBeChecked(false);
      publication.setIndexOperation(IndexManager.NONE);
      publicationService.setDetail(publication);

      //notify them if the publication (or the clone) is in validation required state...
      if (publication.isValidationRequired()) {
        sendValidationAlert(publication, validatorIds);
      }
    }
  }

  private boolean isValidationComplete(PublicationPK pubPK, List<String> allValidators) {
    List<ValidationStep> steps = publicationService.getValidationSteps(pubPK);

    // get users who have already validate
    List<String> stepUserIds = new ArrayList<>();
    for (ValidationStep step : steps) {
      stepUserIds.add(step.getUserId());
    }

    // check if all users have validate
    boolean validationOK = true;
    for (int i = 0; validationOK && i < allValidators.size(); i++) {
      String validatorId = allValidators.get(i);
      validationOK = stepUserIds.contains(validatorId);
    }

    return validationOK;
  }

  @Override
  public boolean validatePublication(PublicationPK pubPK, String userId, boolean force,
      final boolean hasUserNoMoreValidationRight) {
    boolean validationComplete;
    try {
      CompletePublication currentPub = publicationService.getCompletePublication(pubPK);
      PublicationDetail currentPubDetail = currentPub.getPublicationDetail();
      PublicationDetail currentPubOrCloneDetail = currentPubDetail;
      boolean validationOnClone = currentPubDetail.haveGotClone();
      PublicationPK validatedPK = pubPK;
      if (validationOnClone) {
        validatedPK = currentPubDetail.getClonePK();
        currentPubOrCloneDetail = getPublicationDetail(validatedPK);
      }
      if (!hasUserNoMoreValidationRight && !isUserCanValidatePublication(validatedPK, userId)) {
        SilverLogger.getLogger(this)
            .debug("user ''{0}'' is not allowed to validate publication {1}", userId,
                pubPK.toString());
        return false;
      }

      String validatorUserId = userId;
      Date validationDate = new Date();
      if (force) {
        validationComplete = true;
      } else if (!hasUserNoMoreValidationRight) {
        validationComplete = completeValidation(pubPK, validatedPK, userId, validationOnClone);
      } else {
        ValidationChecker validationChecker = new ValidationChecker().setPubPK(pubPK)
            .setUserId(userId)
            .setCurrentPubDetail(currentPubDetail)
            .setCurrentPubOrCloneDetail(currentPubOrCloneDetail)
            .setValidatedPK(validatedPK)
            .setValidationDate(validationDate)
            .setValidatorUserId(validatorUserId)
            .check();
        validationComplete = validationChecker.isValidationComplete();
        validatorUserId = validationChecker.getValidatorUserId();
        validationDate = validationChecker.getValidationDate();
      }

      if (validationComplete) {
        applyValidation(pubPK, currentPub, currentPubDetail, validatedPK, validatorUserId,
            validationDate, validationOnClone);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return validationComplete;
  }

  private void applyValidation(final PublicationPK pubPK, final CompletePublication currentPub,
      PublicationDetail currentPubDetail, final PublicationPK validatedPK,
      final String validatorUserId, final Date validationDate, final boolean validationOnClone) {
    removeAllTodosForPublication(validatedPK);
    if (validationOnClone) {
      removeAllTodosForPublication(pubPK);
    }
    if (currentPubDetail.haveGotClone()) {
      currentPubDetail = mergeClone(currentPub, validatorUserId, validationDate);
    } else if (currentPubDetail.isValidationRequired()) {
      currentPubDetail.setValidatorId(validatorUserId);
      currentPubDetail.setValidateDate(validationDate);
      currentPubDetail.setStatus(PublicationDetail.VALID_STATUS);
    }
    KmeliaHelper.checkIndex(currentPubDetail);
    publicationService.setDetail(currentPubDetail);
    updateSilverContentVisibility(currentPubDetail);
    // index all publication's elements
    indexExternalElementsOfPublication(currentPubDetail);

    // the publication has been validated
    // all subscribers of the different topics must be alerted
    NodePK oneFather =
        sendSubscriptionsNotification(currentPubDetail, NotifAction.PUBLISHED, false);

    // publication's creator must be alerted
    sendValidationNotification(oneFather, currentPubDetail, null, validatorUserId);

    // alert supervisors
    sendAlertToSupervisors(oneFather, currentPubDetail);
  }

  private boolean completeValidation(final PublicationPK pubPK, final PublicationPK validatedPK,
      final String userId, final boolean validationOnClone) {
    boolean validationComplete = false;
    int validationType = getValidationType(pubPK.getInstanceId());
    if (validationType == KmeliaHelper.VALIDATION_CLASSIC ||
        validationType == KmeliaHelper.VALIDATION_TARGET_1) {
      validationComplete = true;
    } else {
      if (validationType == KmeliaHelper.VALIDATION_TARGET_N) {
        // check that validators are well defined
        // If not, considering validation as classic one
        PublicationDetail publi = publicationService.getDetail(validatedPK);
        validationComplete = !isDefined(publi.getTargetValidatorId());
      }
      if (!validationComplete) {
        // get all users who have to validate
        List<String> allValidators = getAllValidators(validatedPK);
        if (allValidators.size() == 1) {
          // special case : only once user is concerned by validation
          validationComplete = true;
        } else if (allValidators.size() > 1) {
          // remove it for this user. His job is done !
          removeTodoForPublication(validatedPK, userId);
          if (validationOnClone) {
            removeTodoForPublication(pubPK, userId);
          }
          // save his decision

          ValidationStep validation =
              new ValidationStep(validatedPK, userId, PublicationDetail.VALID_STATUS);
          publicationService.addValidationStep(validation);
          // check if all validators have give their decision
          validationComplete = isValidationComplete(validatedPK, allValidators);
        }
      }
    }
    return validationComplete;
  }

  private PublicationDetail getClone(PublicationDetail refPub) {
    PublicationDetail copy = PublicationDetail.builder(refPub.getLanguage())
        .setPk(new PublicationPK(refPub.getPK().getId(), refPub.getPK().getInstanceId()))
        .setNameAndDescription(refPub.getName(), refPub.getDescription())
        .build();
    copy.setAuthor(refPub.getAuthor());
    if (refPub.getBeginDate() != null) {
      copy.setBeginDate(new Date(refPub.getBeginDate().getTime()));
    }
    copy.setBeginHour(refPub.getBeginHour());
    copy.setContentPagePath(refPub.getContentPagePath());
    copy.setCreationDate(new Date(refPub.getCreationDate().getTime()));
    copy.setCreatorId(refPub.getCreatorId());
    if (refPub.getEndDate() != null) {
      copy.setEndDate(new Date(refPub.getEndDate().getTime()));
    }
    copy.setEndHour(refPub.getEndHour());
    copy.setImportance(refPub.getImportance());
    copy.setInfoId(refPub.getInfoId());
    copy.setKeywords(refPub.getKeywords());
    copy.setStatus(refPub.getStatus());
    copy.setTargetValidatorId(refPub.getTargetValidatorId());
    copy.setCloneId(refPub.getCloneId());
    if (refPub.getLastUpdateDate() != null) {
      copy.setUpdateDate(new Date(refPub.getLastUpdateDate().getTime()));
    }
    copy.setUpdaterId(refPub.getUpdaterId());
    if (refPub.getValidateDate() != null) {
      copy.setValidateDate(new Date(refPub.getValidateDate().getTime()));
    }
    copy.setValidatorId(refPub.getValidatorId());
    copy.setVersion(refPub.getVersion());

    return copy;
  }

  /**
   * In charge of merging data from the clone with the stable one.
   * @param currentPub all the necessary data about a publication as {@link CompletePublication}.
   * @param validatorUserId the identifier of the last user validating the given publication.
   * @param validationDate the date of validation to register. Date of day is taken if null is
   * given.
   * @return the merged publication as {@link PublicationDetail}.
   */
  private PublicationDetail mergeClone(CompletePublication currentPub, String validatorUserId,
      final Date validationDate) {
    PublicationDetail currentPubDetail = currentPub.getPublicationDetail();
    String memInfoId = currentPubDetail.getInfoId();
    PublicationPK pubPK = currentPubDetail.getPK();
    // merging clone data on the original ones
    String cloneId = currentPubDetail.getCloneId();
    if (!UNDEFINED_NODE_ID.equals(cloneId)) {
      final PublicationDetail currentClonedPubDetail =
          clonePublication(cloneId, pubPK, validatorUserId, validationDate);
      currentPubDetail = currentClonedPubDetail;
      // merging attachments in a new transaction in order to ensure getting right committed data
      // into JCR
      getTransaction().performNew(() -> {
        final String instanceId = pubPK.getInstanceId();
        final ResourceReference pkFrom = new ResourceReference(pubPK.getId(), instanceId);
        final ResourceReference pkTo = new ResourceReference(cloneId, instanceId);
        final Map<String, String> attachmentIds =
            getAttachmentService().mergeDocuments(pkFrom, pkTo, DocumentType.attachment);
        // merging XMLModel content
        mergeXmlModelClone(pubPK, memInfoId, cloneId, currentClonedPubDetail, attachmentIds);
        // merging Wysiwyg content
        mergeWysiwygClone(pubPK, cloneId, currentClonedPubDetail);
        return null;
      });
      // deleting the clone
      deletePublication(new PublicationPK(cloneId, pubPK));
    }
    return currentPubDetail;
  }

  private void mergeXmlModelClone(final PublicationPK originalPubPK, final String originalInfoId,
      final String cloneId, final PublicationDetail currentClonedPubDetail,
      final Map<String, String> attachmentIds) throws PublicationTemplateException, FormException {
    final String infoId = currentClonedPubDetail.getInfoId();
    if (infoId != null && !"0".equals(infoId) && !isInteger(infoId)) {
      final RecordSet set = getXMLFormFrom(infoId, originalPubPK);
      if (originalInfoId != null && !"0".equals(originalInfoId)) {
        // content already exists
        set.merge(cloneId, originalPubPK.getInstanceId(), originalPubPK.getId(),
            originalPubPK.getInstanceId(), attachmentIds);
      } else {
        // no content exists
        final PublicationTemplateManager publicationTemplateManager =
            PublicationTemplateManager.getInstance();
        publicationTemplateManager.addDynamicPublicationTemplate(
            originalPubPK.getInstanceId() + ":" + infoId, infoId + ".xml");
        set.clone(cloneId, originalPubPK.getInstanceId(), originalPubPK.getId(),
            originalPubPK.getInstanceId(), attachmentIds);
      }
    }
  }

  private void mergeWysiwygClone(final PublicationPK originalPubPK, final String cloneId,
      final PublicationDetail currentClonedPubDetail) {
    final String language = currentClonedPubDetail.getLanguage();
    final boolean cloneWysiwyg = haveGotWysiwyg(originalPubPK.getInstanceId(), cloneId, language);
    if (cloneWysiwyg) {
      try {
        // delete wysiwyg contents of public version
        deleteWysiwygAttachmentsOnly(originalPubPK.getInstanceId(), originalPubPK.getId());
      } catch (WysiwygException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
      // wysiwyg contents of work version become public version ones
      WysiwygController.copy(originalPubPK.getInstanceId(), cloneId, originalPubPK.getInstanceId(),
          originalPubPK.getId(), currentClonedPubDetail.getUpdaterId());
    }
  }

  @Override
  public void unvalidatePublication(PublicationPK pubPK, String userId, String refusalMotive,
      int validationType) {
    try {
      switch (validationType) {
        case KmeliaHelper.VALIDATION_COLLEGIATE:
        case KmeliaHelper.VALIDATION_TARGET_N:
          // reset other decisions
          publicationService.removeValidationSteps(pubPK);
          break;
        case KmeliaHelper.VALIDATION_CLASSIC:
        case KmeliaHelper.VALIDATION_TARGET_1:
        default:
          break;// do nothing
      }

      PublicationDetail currentPubDetail = publicationService.getDetail(pubPK);

      if (currentPubDetail.haveGotClone()) {
        String cloneId = currentPubDetail.getCloneId();
        PublicationPK tempPK = new PublicationPK(cloneId, pubPK);
        PublicationDetail clone = publicationService.getDetail(tempPK);

        // change clone's status
        clone.setStatus("UnValidate");
        clone.setIndexOperation(IndexManager.NONE);
        publicationService.setDetail(clone);

        // Modification de la publication de reference
        currentPubDetail.setCloneStatus(PublicationDetail.REFUSED_STATUS);
        currentPubDetail.setUpdateDataMustBeSet(false);
        publicationService.setDetail(currentPubDetail);

        // we have to alert publication's last updater
        NodePK fatherPK = getPublicationFatherPK(pubPK);
        sendValidationNotification(fatherPK, clone, refusalMotive, userId);

        // remove tasks
        removeAllTodosForPublication(clone.getPK());
      } else {
        // change publication's status
        currentPubDetail.setStatus("UnValidate");

        KmeliaHelper.checkIndex(currentPubDetail);

        publicationService.setDetail(currentPubDetail);

        // change visibility over PDC
        updateSilverContentVisibility(currentPubDetail);

        // we have to alert publication's creator
        NodePK fatherPK = getPublicationFatherPK(pubPK);
        sendValidationNotification(fatherPK, currentPubDetail, refusalMotive, userId);

        //remove tasks
        removeAllTodosForPublication(currentPubDetail.getPK());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public void suspendPublication(PublicationPK pubPK, String defermentMotive, String userId) {
    try {
      PublicationDetail currentPubDetail = publicationService.getDetail(pubPK);

      // change publication's status
      currentPubDetail.setStatus(PublicationDetail.TO_VALIDATE_STATUS);

      KmeliaHelper.checkIndex(currentPubDetail);

      publicationService.setDetail(currentPubDetail);

      // change visibility over PDC
      updateSilverContentVisibility(currentPubDetail);

      unIndexExternalElementsOfPublication(currentPubDetail.getPK());

      // we have to alert publication's creator
      sendDefermentNotification(currentPubDetail, defermentMotive);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void sendDefermentNotification(final PublicationDetail pubDetail,
      final String defermentMotive) {
    try {
      UserNotificationHelper.buildAndSend(
          new KmeliaDefermentPublicationUserNotification(pubDetail, defermentMotive));
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("User notification failure about publication ''{0}'' (id={1})",
              pubDetail.getTitle(), pubDetail.getPK().getId());
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile) {
    PublicationDetail pubDetail =
        draftOutPublicationWithoutNotifications(pubPK, topicPK, userProfile);
    indexExternalElementsOfPublication(pubDetail);
    sendTodosAndNotificationsOnDraftOut(pubDetail, topicPK, userProfile);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public PublicationDetail draftOutPublicationWithoutNotifications(PublicationPK pubPK,
      NodePK topicPK, String userProfile) {
    return draftOutPublication(pubPK, topicPK, userProfile, false, true);
  }

  @Override
  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile,
      boolean forceUpdateDate) {
    draftOutPublication(pubPK, topicPK, userProfile, forceUpdateDate, false);
  }

  private PublicationDetail draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile, boolean forceUpdateDate, boolean inTransaction) {
    try {
      PublicationDetail changedPublication;
      CompletePublication currentPub = publicationService.getCompletePublication(pubPK);
      PublicationDetail pubDetail = currentPub.getPublicationDetail();
      if (userProfile.equals("publisher") || userProfile.equals(ADMIN_ROLE)) {
        if (pubDetail.haveGotClone()) {
          pubDetail = mergeClone(currentPub, null, null);
        }
        pubDetail.setStatus(PublicationDetail.VALID_STATUS);
        changedPublication = pubDetail;
      } else {
        if (pubDetail.haveGotClone()) {
          // changement du statut du clone
          PublicationDetail clone = publicationService.getDetail(pubDetail.getClonePK());
          clone.setStatus(PublicationDetail.TO_VALIDATE_STATUS);
          clone.setIndexOperation(IndexManager.NONE);
          clone.setUpdateDataMustBeSet(false);
          publicationService.setDetail(clone);
          changedPublication = clone;
          pubDetail.setUpdateDataMustBeSet(false);
          pubDetail.setCloneStatus(PublicationDetail.TO_VALIDATE_STATUS);
        } else {
          pubDetail.setStatus(PublicationDetail.TO_VALIDATE_STATUS);
          changedPublication = pubDetail;
        }
      }
      KmeliaHelper.checkIndex(pubDetail);
      publicationService.setDetail(pubDetail, forceUpdateDate);
      if (!KmeliaHelper.isKmax(pubDetail.getInstanceId())) {
        // update visibility attribute on PDC
        updateSilverContentVisibility(pubDetail);
      }
      if (!inTransaction) {
        // index all publication's elements
        indexExternalElementsOfPublication(changedPublication);
        sendTodosAndNotificationsOnDraftOut(changedPublication, topicPK, userProfile);
      }

      return changedPublication;
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void sendTodosAndNotificationsOnDraftOut(PublicationDetail pubDetail, NodePK topicPK,
      String userProfile) {
    if (SilverpeasRole.WRITER.isInRole(userProfile)) {
      createTodosForPublication(pubDetail, true);
    }
    // Subscriptions and supervisors are supported by kmelia and filebox only
    if (!KmeliaHelper.isKmax(pubDetail.getInstanceId())) {
      // alert subscribers
      sendSubscriptionsNotification(pubDetail, NotifAction.PUBLISHED, false);

      // alert supervisors
      if (topicPK != null) {
        sendAlertToSupervisors(topicPK, pubDetail);
      }
    }
  }

  @Override
  public void draftInPublication(PublicationPK pubPK) {
    draftInPublication(pubPK, null);
  }

  @Override
  public void draftInPublication(PublicationPK pubPK, String userId) {
    try {
      PublicationDetail pubDetail = publicationService.getDetail(pubPK);
      if (pubDetail.isRefused() || pubDetail.isValid()) {
        pubDetail.setStatus(PublicationDetail.DRAFT_STATUS);
        pubDetail.setUpdaterId(userId);
        KmeliaHelper.checkIndex(pubDetail);
        publicationService.setDetail(pubDetail);
        updateSilverContentVisibility(pubDetail);
        unIndexExternalElementsOfPublication(pubDetail.getPK());
        removeAllTodosForPublication(pubPK);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public UserNotification getUserNotification(PublicationPK pubPK, NodePK topicPK) {
    final KmeliaPublication publication = KmeliaPublication.withPK(pubPK, topicPK);
    return new KmeliaNotifyPublicationUserNotification(topicPK, publication.getDetail()).build();
  }

  @Override
  public UserNotification getUserNotification(PublicationPK pubPK, SimpleDocumentPK documentPk,
      NodePK topicPK) {
    final PublicationDetail pubDetail = getPublicationDetail(pubPK);
    // componentId of document is always the same than its publication (case of alias)
    documentPk.setComponentName(pubDetail.getInstanceId());
    final SimpleDocument document = getAttachmentService().searchDocumentById(documentPk, null);
    SimpleDocument version = document.getLastPublicVersion();
    if (version == null) {
      version = document.getVersionMaster();
    }
    return new KmeliaNotifyPublicationDocumentUserNotification(topicPK, pubDetail, version).build();
  }

  /**
   * delete reading controls to a publication
   * @param pubPK the id of a publication
   * @since 1.0
   */
  @Override
  public void deleteAllReadingControlsByPublication(PublicationPK pubPK) {
    try {
      statisticService.deleteStats(new ResourceReference(pubPK.getId(), pubPK.getInstanceId()),
          PUBLICATION);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public List<HistoryObjectDetail> getLastAccess(PublicationPK pk, NodePK nodePK,
      String excludedUserId, final int maxResult) {
    final Set<String> userIds = new HashSet<>(getUserIdsOfFolder(nodePK));
    final Set<String> readerIds = new HashSet<>();
    readerIds.add(excludedUserId);
    final Pagination<HistoryObjectDetail> pagination = new Pagination<HistoryObjectDetail>(
        new PaginationPage(1, maxResult)).limitDataSourceCallsTo(10)
        .withMinPerPage(200)
        .paginatedDataSource(
            p -> statisticService.getHistoryByAction(new ResourceReference(pk), 1, PUBLICATION,
                readerIds, p.originalSizeIsNotRequired()))
        .filter(r -> {
          final SilverpeasList<HistoryObjectDetail> currentLastAccess = new SilverpeasArrayList<>();
          for (HistoryObjectDetail access : r) {
            final String readerId = access.getUserId();
            if ((CollectionUtil.isEmpty(userIds) || userIds.contains(readerId)) &&
                !readerIds.contains(readerId)) {
              readerIds.add(readerId);
              if (!User.getById(readerId).isAnonymous()) {
                currentLastAccess.add(access);
              }
            }
          }
          return currentLastAccess;
        });
    try {
      return pagination.execute();
    } finally {
      if (pagination.isNbMaxDataSourceCallLimitReached()) {
        SilverLogger.getLogger(this)
            .warn(
                "Performing too much paginated sql queries to retrieve last accesses from pub {0}" +
                    " on node {1}", pk, nodePK);
      }
    }
  }

  @Override
  public List<String> getUserIdsOfFolder(NodePK pk) {
    if (!isRightsOnTopicsEnabled(pk.getInstanceId())) {
      return Collections.emptyList();
    }

    NodeDetail node = getNodeHeader(pk);

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      String rightsDependsOn = node.getRightsDependsOn();
      List<String> profileNames = new ArrayList<>(4);
      profileNames.add(KmeliaHelper.ROLE_ADMIN);
      profileNames.add(KmeliaHelper.ROLE_PUBLISHER);
      profileNames.add(KmeliaHelper.ROLE_WRITER);
      profileNames.add(KmeliaHelper.ROLE_READER);
      String[] userIds = getOrganisationController().getUsersIdsByRoleNames(pk.getInstanceId(),
          ProfiledObjectId.fromNode(rightsDependsOn), profileNames);
      return Arrays.asList(userIds);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void indexKmelia(String componentId) {
    indexTopics(new NodePK(USELESS, componentId));
    indexPublications(componentId);
  }

  private void indexPublications(String componentId) {
    final Collection<PublicationDetail> pubs;
    try {
      pubs = publicationService.getAllPublications(componentId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }

    if (pubs != null) {
      for (PublicationDetail pub : pubs) {
        processPublicationIndexation(pub);
      }
    }
  }

  private void processPublicationIndexation(final PublicationDetail pub) {
    final PublicationPK pk = pub.getPK();
    try {
      // index only valid publications which are not an alias and not in trash
      if (pub.isValid() && !isPublicationInBasket(pub.getPK())) {
        indexPublication(pub);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Error during indexation of publication {0}", pk.getId(), e);
    }
  }

  private void indexPublication(PublicationDetail pub) {
    // index publication itself
    publicationService.createIndex(pub.getPK());

    // index external elements
    indexExternalElementsOfPublication(pub);
  }

  private void indexTopics(NodePK nodePK) {
    try {
      Collection<NodeDetail> nodes = nodeService.getAllNodes(nodePK);
      if (nodes != null) {
        for (NodeDetail node : nodes) {
          if (!node.getNodePK().isRoot() && !node.getNodePK().isTrash() &&
              !node.getNodePK().getId().equals("2")) {
            nodeService.createIndex(node);
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  /*
   * Creates todos for all publishers of this kmelia instance
   * @param pubDetail publication to be validated
   * @param creation true if it's the creation of the publi
   */
  private void createTodosForPublication(PublicationDetail pubDetail, boolean creation) {
    if (!creation) {
      /* remove all todos attached to that publication */
      removeAllTodosForPublication(pubDetail.getPK());
    }
    if (pubDetail.isValidationRequired() ||
        PublicationDetail.TO_VALIDATE_STATUS.equalsIgnoreCase(pubDetail.getCloneStatus())) {
      int validationType = getValidationType(pubDetail.getPK().getInstanceId());
      if (validationType == KmeliaHelper.VALIDATION_TARGET_N ||
          validationType == KmeliaHelper.VALIDATION_COLLEGIATE) {
        // removing potential older validation decision
        publicationService.removeValidationSteps(pubDetail.getPK());
      }
      ValidatorsList validators = getAllValidators(pubDetail.getPK());
      addTodoAndSendNotificationToValidators(pubDetail, validators);
    }
  }

  private void addTodoAndSendNotificationToValidators(PublicationDetail pub,
      ValidatorsList validators) {
    if (CollectionUtil.isNotEmpty(validators)) {
      String[] users = validators.getUserIds();
      addTodo(pub, users);
      // Send a notification to alert validators
      sendValidationAlert(pub, users);
    } else if (validators.isTargetedValidation()) {
      // targeted validation is enabled but there is no validators to notify
      UserNotificationBuilder notification;
      // tries to notify updater
      String profile = getProfileOnPublication(pub.getMostRecentUpdater(), pub.getPK());
      //noinspection ConstantConditions
      if (profile != null &&
          SilverpeasRole.fromString(profile).isGreaterThanOrEquals(SilverpeasRole.WRITER)) {
        notification = new KmeliaNoMoreValidatorPublicationUserNotification(null, pub);
      } else {
        // notify current user
        notification = new KmeliaNoMoreValidatorPublicationUserNotification(null, pub,
            User.getCurrentRequester().getId());
      }
      UserNotificationHelper.buildAndSend(notification);
    }
  }

  private void addTodo(PublicationDetail pubDetail, String[] users) {
    LocalizationBundle message = ResourceLocator.getLocalizationBundle(MESSAGES_PATH);

    TodoDetail todo = new TodoDetail();

    todo.setId(pubDetail.getPK().getId());
    todo.setSpaceId(pubDetail.getPK().getSpace());
    todo.setComponentId(pubDetail.getPK().getComponentName());
    todo.setName(message.getString("ToValidateShort") + " : " + pubDetail.getName());

    List<Attendee> attendees = new ArrayList<>();
    for (String user : users) {
      if (user != null) {
        attendees.add(new Attendee(user));
      }
    }
    todo.setAttendees(new ArrayList<>(attendees));
    todo.setDelegatorId(pubDetail.getMostRecentUpdater());
    todo.setExternalId(pubDetail.getPK().getId());

    calendar.addToDo(todo);
  }

  /*
   * Remove todos for all pubishers of this kmelia instance
   * @param pubDetail corresponding publication
   */
  private void removeAllTodosForPublication(PublicationPK pubPK) {
    calendar.removeToDoFromExternal(USELESS, pubPK.getInstanceId(), pubPK.getId());
  }

  private void removeTodoForPublication(PublicationPK pubPK, List<String> userIds) {
    if (userIds != null) {
      for (String userId : userIds) {
        removeTodoForPublication(pubPK, userId);
      }
    }
  }

  private void removeTodoForPublication(PublicationPK pubPK, String userId) {
    calendar.removeAttendeeInToDoFromExternal(pubPK.getInstanceId(), pubPK.getId(), userId);
  }

  private void sendValidationAlert(final PublicationDetail pubDetail, final String[] users) {
    UserNotificationHelper.buildAndSend(
        new KmeliaPendingValidationPublicationUserNotification(pubDetail, users));
  }

  private void sendModificationAlert(final int modificationScope,
      final PublicationDetail pubDetail) {
    UserNotificationHelper.buildAndSend(
        new KmeliaModificationPublicationUserNotification(pubDetail, modificationScope));
  }

  @Override
  public void sendModificationAlert(int modificationScope, PublicationPK pubPK) {
    sendModificationAlert(modificationScope, getPublicationDetail(pubPK));
  }

  @Override
  public int getSilverObjectId(PublicationPK pubPK) {
    int silverObjectId;
    PublicationDetail pubDetail;
    try {
      silverObjectId =
          kmeliaContentManager.getSilverContentId(pubPK.getId(), pubPK.getInstanceId());
      if (silverObjectId == -1) {
        pubDetail = getPublicationDetail(pubPK);
        silverObjectId = createSilverContent(pubDetail, pubDetail.getCreatorId());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return silverObjectId;
  }

  private int createSilverContent(PublicationDetail pubDetail, String creatorId) {
    Connection con = null;
    try {
      con = getConnection();
      return kmeliaContentManager.createSilverContent(con, pubDetail, creatorId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void deleteSilverContent(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      kmeliaContentManager.deleteSilverContent(con, pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      freeConnection(con);
    }
  }

  private void updateSilverContentVisibility(PublicationDetail pubDetail) {
    try {
      kmeliaContentManager.updateSilverContentVisibility(pubDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void updateSilverContentVisibility(PublicationPK pubPK) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    try {
      kmeliaContentManager.updateSilverContentVisibility(pubDetail, false);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void indexExternalElementsOfPublication(PublicationDetail pubDetail) {
    if (KmeliaHelper.isIndexable(pubDetail)) {
      try {
        // index all files except Wysiwyg which are already indexed as publication content
        List<SimpleDocument> documents = getAttachmentService().listAllDocumentsByForeignKey(
            pubDetail.getPK().toResourceReference(), null);
        for (SimpleDocument doc : documents) {
          if (doc.getDocumentType() != DocumentType.wysiwyg) {
            getAttachmentService().createIndex(doc, pubDetail.getBeginDate(),
                pubDetail.getEndDate());
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Indexing versioning documents failed for publication {0}",
                new String[]{pubDetail.getPK().getId()}, e);
      }
      try {
        // index comments
        getCommentService().indexAllCommentsOnPublication(pubDetail.getContributionType(),
            new ResourceReference(pubDetail.getPK()));
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Indexing comments failed for publication {0}",
                new String[]{pubDetail.getPK().getId()}, e);
      }
    }
  }

  private void unIndexExternalElementsOfPublication(PublicationPK pubPK) {
    ResourceReference ref = new ResourceReference(pubPK);
    try {
      getAttachmentService().unindexAttachmentsOfExternalObject(ref);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Unindexing versioning documents failed for publication {0}",
              new String[]{pubPK.getId()}, e);
    }
    try {
      // index comments
      getCommentService().unindexAllCommentsOnPublication(PublicationDetail.getResourceType(), ref);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Unindexing versioning documents failed for publication {0}",
              new String[]{pubPK.getId()}, e);
    }
  }

  @Override
  public void removeContentOfPublication(PublicationPK pubPK) {
    // remove XML content
    PublicationDetail publication = getPublicationDetail(pubPK);
    if (!StringUtil.isInteger(publication.getInfoId())) {
      removeXMLContentOfPublication(pubPK);
    }
    // reset reference to content
    publication.setInfoId("0");
    updatePublication(publication, KmeliaHelper.PUBLICATION_CONTENT, true);
  }

  private void removeXMLContentOfPublication(PublicationPK pubPK) {
    try {
      PublicationDetail pubDetail = getPublicationDetail(pubPK);
      String infoId = pubDetail.getInfoId();
      if (!isInteger(infoId)) {
        PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance()
            .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":" + infoId);

        RecordSet set = pubTemplate.getRecordSet();
        set.delete(pubDetail.getPK().getId());
      }
    } catch (PublicationTemplateException | FormException e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Connection freed failure", e);
      }
    }
  }

  @Override
  public void setModelUsed(String[] models, String instanceId, String nodeId) {
    Connection con = getConnection();
    try {
      ModelDAO.deleteModel(con, instanceId, nodeId);
      if (models != null) {
        for (String modelId : models) {
          ModelDAO.addModel(con, instanceId, modelId, nodeId);
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }
  }

  @Override
  public Collection<String> getModelUsed(String instanceId, String nodeId) {
    Connection con = getConnection();
    try {
      // get templates defined for the given node
      Collection<String> result = ModelDAO.getModelUsed(con, instanceId, nodeId);
      if (isDefined(nodeId) && result.isEmpty()) {
        // there is no templates defined for the given node, check the parent nodes
        Collection<NodeDetail> parents = nodeService.getPath(new NodePK(nodeId, instanceId));
        Iterator<NodeDetail> iter = parents.iterator();
        while (iter.hasNext() && result.isEmpty()) {
          NodeDetail parent = iter.next();
          //Bin, unclassifieds, unvisibles and to_validate nodes can't have models
          if (!parent.isBin() && !parent.isUnclassified() && !NodePK.UNDEFINED_NODE_ID.equals(parent.getId()) && !KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS.equals(parent.getId()) && !KmeliaHelper.SPECIALFOLDER_TOVALIDATE.equals(parent.getId())) {
            result = ModelDAO.getModelUsed(con, instanceId, parent.getNodePK().getId());
          }
        }
      }
      return result;
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }
  }

  /**
   * Copy model used from a node to an other one.
   * @param from the node the models used are linked to.
   * @param to the node the models must be copied.
   */
  private void copyUsedModel(NodePK from, NodePK to) {
    Connection con = getConnection();
    try {
      // get templates defined for the 'from' node
      Collection<String> modelIds = ModelDAO.getModelUsed(con, from.getInstanceId(), from.getId());
      for (String modelId : modelIds) {
        // set template to 'to' node
        ModelDAO.addModel(con, to.getInstanceId(), modelId, to.getId());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public List<NodeDetail> getAxis(String componentId) {
    SettingBundle nodeSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.node.nodeSettings");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List<NodeDetail> axis = new ArrayList<>();
    try {
      List<NodeDetail> headers = getAxisHeaders(componentId);
      for (NodeDetail header : headers) {
        // Do not get hidden nodes (Basket and unclassified)
        if (!NodeDetail.STATUS_INVISIBLE.equals(header.getStatus())) {
          // get content  of  this axis
          axis.addAll(nodeService.getSubTree(header.getNodePK(), sortField + " " + sortOrder));
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return axis;
  }

  @Override
  public List<NodeDetail> getAxisHeaders(String componentId) {
    List<NodeDetail> axisHeaders;
    try {
      axisHeaders = nodeService.getHeadersByLevel(new NodePK(USELESS, componentId), 2);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return axisHeaders;
  }

  @Override
  public NodePK addAxis(NodeDetail axis, String componentId) {
    NodePK axisPK = new NodePK("toDefine", componentId);
    NodeDetail rootDetail = new NodeDetail("0", "Root", "desc", 1, UNDEFINED_NODE_ID);
    rootDetail.getNodePK().setComponentName(componentId);
    rootDetail.setCreationDate(null);
    rootDetail.setCreatorId(UNKNOWN);
    rootDetail.setPath("/0");
    rootDetail.setStatus(NodeDetail.STATUS_VISIBLE);
    axis.setNodePK(axisPK);
    CoordinatePK coordinatePK = new CoordinatePK(USELESS, axisPK);
    try {
      // axis creation
      axisPK = nodeService.createNode(axis, rootDetail);
      // add this new axis to existing coordinates
      CoordinatePoint point = new CoordinatePoint(-1, Integer.parseInt(axisPK.getId()), true);
      coordinatesService.addPointToAllCoordinates(coordinatePK, point);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
    return axisPK;
  }

  @Override
  public void updateAxis(NodeDetail axis, String componentId) {
    axis.getNodePK().setComponentName(componentId);
    final NodeDetail previousNode = nodeService.getDetail(axis.getNodePK());
    axis.setOrder(previousNode.getOrder());
    try {
      nodeService.setDetail(axis);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public void deleteAxis(String axisId, String componentId) {
    NodePK pkToDelete = new NodePK(axisId, componentId);

    CoordinatePK coordinatePK = new CoordinatePK(USELESS, pkToDelete);
    // Delete the axis
    try {
      // delete publicationFathers
      final long nbAxisHeaders = getAxisHeaders(componentId).stream()
          .map(NodeDetail::getNodePK)
          .filter(n -> !n.isUnclassed() && !n.isTrash())
          .count();
      if (nbAxisHeaders == 1) {
        final PublicationPK pubPK = new PublicationPK(USELESS, componentId);
        final List<String> fatherIds =
            coordinatesService.getCoordinateIdsByNodeId(coordinatePK, axisId)
                .stream()
                .filter(f -> !NodePK.UNCLASSED_NODE_ID.equals(f) && !NodePK.BIN_NODE_ID.equals(f))
                .collect(Collectors.toList());
        if (!fatherIds.isEmpty()) {
          publicationService.removeFathers(pubPK, fatherIds);
        }
      }
      // delete coordinate which contains subComponents of this component
      Collection<NodeDetail> subComponents = nodeService.getDescendantDetails(pkToDelete);
      Iterator<NodeDetail> it = subComponents.iterator();
      List<NodePK> points = new ArrayList<>();
      points.add(pkToDelete);
      while (it.hasNext()) {
        points.add((it.next()).getNodePK());
      }
      removeCoordinatesByPoints(points, componentId);
      // delete axis
      nodeService.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  private void removeCoordinatesByPoints(List<NodePK> nodePKs, String componentId) {
    Iterator<NodePK> it = nodePKs.iterator();
    List<String> coordinatePoints = new ArrayList<>();
    String nodeId;
    while (it.hasNext()) {
      nodeId = (it.next()).getId();
      coordinatePoints.add(nodeId);
    }
    CoordinatePK coordinatePK = new CoordinatePK(USELESS, USELESS, componentId);
    try {
      coordinatesService.deleteCoordinatesByPoints(coordinatePK, coordinatePoints);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public NodeDetail getNodeHeader(String id, String componentId) {
    NodePK pk = new NodePK(id, componentId);
    return getNodeHeader(pk);
  }

  private NodeDetail getNodeHeader(NodePK pk) {
    NodeDetail nodeDetail;
    try {
      nodeDetail = nodeService.getHeader(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return nodeDetail;
  }

  @Override
  public NodePK addPosition(String fatherId, NodeDetail position, String componentId,
      String userId) {
    position.getNodePK().setComponentName(componentId);
    position.setCreationDate(new Date());
    position.setCreatorId(userId);
    NodeDetail fatherDetail = getNodeHeader(fatherId, componentId);
    try {
      return nodeService.createNode(position, fatherDetail);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public void updatePosition(NodeDetail position, String componentId) {
    position.getNodePK().setComponentName(componentId);
    try {
      nodeService.setDetail(position);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public void deletePosition(String positionId, String componentId) {
    NodePK pkToDelete = new NodePK(positionId, componentId);
    // Delete the axis
    try {
      // delete coordinate which contains subPositions of this position
      Collection<NodeDetail> subComponents = nodeService.getDescendantDetails(pkToDelete);
      Iterator<NodeDetail> it = subComponents.iterator();
      List<NodePK> points = new ArrayList<>();
      points.add(pkToDelete);
      while (it.hasNext()) {
        points.add((it.next()).getNodePK());
      }
      removeCoordinatesByPoints(points, componentId);
      // delete component
      nodeService.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public Collection<NodeDetail> getPath(String id, String componentId) {
    Collection<NodeDetail> newPath = new ArrayList<>();
    NodePK nodePK = new NodePK(id, componentId);
    // compute path from a to z

    try {
      List<NodeDetail> pathInReverse = nodeService.getPath(nodePK);
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return newPath;
  }

  @Override
  public List<KmeliaPublication> search(List<String> combination, String componentId) {
    Collection<PublicationDetail> publications = searchPublications(combination, componentId);
    if (publications == null) {
      return new ArrayList<>();
    }
    return asKmeliaPublication(publications);
  }

  @Override
  public List<KmeliaPublication> search(List<String> combination, int nbDays, String componentId) {
    Collection<PublicationDetail> publications = searchPublications(combination, componentId);
    return asKmeliaPublication(filterPublicationsByBeginDate(publications, nbDays));
  }

  private Collection<PublicationDetail> searchPublications(List<String> combination,
      String componentId) {
    PublicationPK pk = new PublicationPK(USELESS, componentId);
    CoordinatePK coordinatePK = new CoordinatePK(UNKNOWN, pk);
    Collection<PublicationDetail> publications = null;
    Collection<String> coordinates;
    try {
      // Remove node "Toutes catégories" (level == 2) from combination
      int nodeLevel;
      String axisValue;
      int i = 0;
      while (i < combination.size()) {
        axisValue = combination.get(i);
        StringTokenizer st = new StringTokenizer(axisValue, "/");
        nodeLevel = st.countTokens();
        // if node is level 2, it represents "Toutes Catégories"
        // this axis is not used by the search
        if (nodeLevel == 2) {
          combination.remove(i);
          i--;
        }
        i++;
      }
      if (combination.isEmpty()) {
        // all criterias is "Toutes Catégories"
        // get all publications classified
        NodePK basketPK = new NodePK("1", componentId);
        publications = publicationService.getDetailsNotInFatherPK(basketPK);
      } else {
        coordinates = coordinatesService.getCoordinatesByFatherPaths(combination, coordinatePK);
        if (coordinates != null && !coordinates.isEmpty()) {
          publications =
              publicationService.getDetailsByFatherIds((ArrayList<String>) coordinates, componentId,
                  false);
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return publications;
  }

  @Override
  public Collection<KmeliaPublication> getUnbalancedPublications(String componentId) {
    Collection<PublicationDetail> publications;
    try {
      publications = publicationService.getOrphanPublications(componentId);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return asKmeliaPublication(publications);
  }

  private Collection<PublicationDetail> filterPublicationsByBeginDate(
      Collection<PublicationDetail> publications, int nbDays) {
    List<PublicationDetail> pubOK = new ArrayList<>();
    if (publications != null) {
      Calendar rightNow = Calendar.getInstance();
      if (nbDays == 0) {
        nbDays = 1;
      }
      rightNow.add(Calendar.DATE, -nbDays);
      Date day = rightNow.getTime();
      Iterator<PublicationDetail> it = publications.iterator();
      PublicationDetail pub;
      Date dateToCompare;
      while (it.hasNext()) {
        pub = it.next();
        if (pub.getBeginDate() != null) {
          dateToCompare = pub.getBeginDate();
        } else {
          dateToCompare = pub.getCreationDate();
        }

        if (dateToCompare.compareTo(day) >= 0) {
          pubOK.add(pub);
        }
      }
    }
    return pubOK;
  }

  @Override
  public void indexKmax(String componentId) {
    indexAxis(componentId);
    indexPublications(componentId);
  }

  private void indexAxis(String componentId) {
    NodePK nodePK = new NodePK(USELESS, componentId);
    try {
      Collection<NodeDetail> nodes = nodeService.getAllNodes(nodePK);
      if (nodes != null) {
        for (NodeDetail nodeDetail : nodes) {
          if (!"corbeille".equalsIgnoreCase(nodeDetail.getName()) ||
              !nodeDetail.getNodePK().isTrash()) {
            nodeService.createIndex(nodeDetail);
          }
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public KmeliaPublication getKmaxPublication(String pubId, String currentUserId) {
    PublicationPK pubPK;
    CompletePublication completePublication;

    try {
      pubPK = new PublicationPK(pubId);
      completePublication = publicationService.getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
    return KmeliaPublication.aKmeliaPublicationFromCompleteDetail(completePublication);
  }

  @Override
  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId) {
    try {
      return publicationService.getCoordinates(pubId, componentId);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @Override
  public void addPublicationToCombination(String pubId, List<String> combination,
      String componentId) {
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    CoordinatePK coordinatePK = new CoordinatePK(UNKNOWN, pubPK);
    try {

      Collection<Coordinate> coordinates = getPublicationCoordinates(pubId, componentId);

      if (!checkCombination(coordinates, combination)) {
        return;
      }

      NodeDetail nodeDetail;
      // enrich combination by get ancestors
      Iterator<String> it = combination.iterator();
      List<CoordinatePoint> allnodes = new ArrayList<>();
      int i = 1;
      while (it.hasNext()) {
        String nodeId = it.next();
        NodePK nodePK = new NodePK(nodeId, componentId);
        Collection<NodeDetail> path = nodeService.getPath(nodePK);
        for (NodeDetail aPath : path) {
          nodeDetail = aPath;
          String anscestorId = nodeDetail.getNodePK().getId();
          int nodeLevel = nodeDetail.getLevel();
          if (!nodeDetail.getNodePK().isRoot()) {
            CoordinatePoint point;
            if (anscestorId.equals(nodeId)) {
              point = new CoordinatePoint(-1, Integer.parseInt(anscestorId), true, nodeLevel, i);
            } else {
              point = new CoordinatePoint(-1, Integer.parseInt(anscestorId), false, nodeLevel, i);
            }
            allnodes.add(point);
          }
        }
        i++;
      }
      int coordinateId = coordinatesService.addCoordinate(coordinatePK, allnodes);
      publicationService.addFather(pubPK, new NodePK(String.valueOf(coordinateId), pubPK));
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected boolean checkCombination(Collection<Coordinate> coordinates, List<String> combination) {
    for (Coordinate coordinate : coordinates) {
      Collection<CoordinatePoint> points = coordinate.getCoordinatePoints();
      if (points.isEmpty()) {
        continue;
      }

      boolean matchFound = false;

      for (CoordinatePoint point : points) {
        if (!checkPoint(point, combination)) {
          matchFound = false;
          break;
        }
        matchFound = true;
      }

      if (matchFound) {
        return false;
      }
    }
    return true;
  }

  protected boolean checkPoint(CoordinatePoint point, List<String> combination) {
    for (String intVal : combination) {
      if (Integer.parseInt(intVal) == point.getNodeId()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void deletePublicationFromCombination(String pubId, String combinationId,
      String componentId) {
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    NodePK fatherPK = new NodePK(combinationId, componentId);
    CoordinatePK coordinatePK = new CoordinatePK(combinationId, pubPK);
    try {
      // remove publication fathers
      publicationService.removeFather(pubPK, fatherPK);
      // remove coordinate
      List<String> coordinateIds = new ArrayList<>(1);
      coordinateIds.add(combinationId);
      coordinatesService.deleteCoordinates(coordinatePK, coordinateIds);
    } catch (Exception e) {
      throw new KmaxRuntimeException(e);
    }
  }

  /**
   * Create a new Publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  public String createKmaxPublication(PublicationDetail pubDetail) {
    PublicationPK pubPK;
    Connection con = getConnection();
    try {
      // create the publication
      changePublicationStatusOnCreation(pubDetail, new NodePK(USELESS, pubDetail.getPK()));
      pubPK = publicationService.createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());

      // creates todos for publishers
      this.createTodosForPublication(pubDetail, true);

      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    } finally {
      freeConnection(con);
    }
    return pubPK.getId();
  }

  @Override
  public Collection<Location> getAliases(final PublicationPK pubPK) {
    try {
      return publicationService.getAllAliases(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public Collection<Location> getLocations(PublicationPK pubPK) {
    try {
      Collection<Location> locations = publicationService.getAllLocations(pubPK);
      if (locations.isEmpty()) {
        // This publication doesn't yet have any location!
        // Check if it's a clone (a clone doesn't have a location. It is orphaned)
        boolean alwaysVisibleModeActivated = getBooleanValue(
            getOrganisationController().getComponentParameterValue(pubPK.getInstanceId(),
                "publicationAlwaysVisible"));
        if (alwaysVisibleModeActivated) {
          PublicationDetail publication = publicationService.getDetail(pubPK);
          if (publication != null && isClone(publication)) {
            // This publication is a clone
            // Get locations from the main publication
            locations = publicationService.getAllLocations(publication.getClonePK());
          }
        }
      }
      return locations;
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  @Override
  public void setAliases(PublicationPK pubPK, List<Location> locations) {

    Pair<Collection<Location>, Collection<Location>> result = publicationService.setAliases(pubPK,
        locations);

    // Send subscriptions to aliases subscribers
    getThreadCacheAccessor().getCache().put(ALIASES_CACHE_KEY, result.getFirst());
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    sendSubscriptionsNotification(pubDetail, NotifAction.PUBLISHED, true);
  }

  @Override
  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public void addAttachmentToPublication(PublicationPK pubPK, String userId, String filename,
      String description, byte[] contents) {
    try {
      Date creationDate = new Date();
      SimpleAttachment file = SimpleAttachment.builder(I18NHelper.DEFAULT_LANGUAGE)
          .setFilename(FileUtil.getFilename(filename))
          .setTitle(filename)
          .setDescription("")
          .setSize(contents.length)
          .setContentType(FileUtil.getMimeType(filename))
          .setCreationData(userId, creationDate)
          .build();
      boolean versioningActive = getBooleanValue(
          getOrganisationController().getComponentParameterValue(pubPK.getComponentName(),
              VERSION_MODE));
      SimpleDocument document;
      if (versioningActive) {
        document = new HistorisedDocument(new SimpleDocumentPK(null, pubPK.getComponentName()),
            pubPK.getId(), 0, file);
        document.setPublicDocument(true);
        document.setDocumentType(DocumentType.attachment);
      } else {
        document =
            new SimpleDocument(new SimpleDocumentPK(null, pubPK.getComponentName()), pubPK.getId(),
                0, false, file);
      }
      getAttachmentService().createAttachment(document, new ByteArrayInputStream(contents));
    } catch (org.silverpeas.core.contribution.attachment.AttachmentException fnfe) {
      throw new KmeliaRuntimeException(fnfe);
    }
  }

  @Override
  public String createTopic(String componentId, String topicId, String spaceId, String userId,
      String name, String description) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport.createTopic(name, description);
  }

  @Override
  public String clonePublication(CompletePublication refPubComplete, PublicationDetail pubDetail,
      String nextStatus) {
    String cloneId;
    try {
      // récupération de la publi de référence
      PublicationDetail refPub = refPubComplete.getPublicationDetail();

      String fromId = refPub.getPK().getId();
      String fromComponentId = refPub.getPK().getInstanceId();

      PublicationDetail clone = getClone(refPub);

      SettingBundle publicationSettings = ResourceLocator.getSettingBundle(
          "org.silverpeas.publication.publicationSettings");
      String absolutePath = FileRepositoryManager.getAbsolutePath(fromComponentId);

      if (pubDetail != null) {
        clone.setAuthor(pubDetail.getAuthor());
        clone.setBeginDate(pubDetail.getBeginDate());
        clone.setBeginHour(pubDetail.getBeginHour());
        clone.setDescription(pubDetail.getDescription());
        clone.setEndDate(pubDetail.getEndDate());
        clone.setEndHour(pubDetail.getEndHour());
        clone.setImportance(pubDetail.getImportance());
        clone.setKeywords(pubDetail.getKeywords());
        clone.setName(pubDetail.getName());
        clone.setTargetValidatorId(pubDetail.getTargetValidatorId());
      }
      if (isInteger(refPub.getInfoId())) {
        // Case content = DB
        clone.setInfoId(null);
      }
      clone.setStatus(nextStatus);
      clone.setCloneId(fromId);
      clone.setIndexOperation(IndexManager.NONE);

      PublicationPK clonePK = publicationService.createPublication(clone);
      clonePK.setComponentName(fromComponentId);
      cloneId = clonePK.getId();

      // clone attachments
      List<SimpleDocument> documents = getAttachmentService().listDocumentsByForeignKey(
          new ResourceReference(fromId, fromComponentId), null);
      Map<String, String> attachmentIds = new HashMap<>(documents.size());
      Collections.reverse(documents);
      for (SimpleDocument document : documents) {
        getAttachmentService().cloneDocument(document, cloneId);
      }

      // eventually, paste the form content
      String xmlFormShortName = refPub.getInfoId();
      if (xmlFormShortName != null && !"0".equals(xmlFormShortName) &&
          !isInteger(xmlFormShortName)) {
        PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
        // Content = XMLForm
        // register xmlForm to publication
        templateManager.addDynamicPublicationTemplate(fromComponentId + ":" + xmlFormShortName,
            xmlFormShortName + ".xml");

        PublicationTemplate pubTemplate =
            templateManager.getPublicationTemplate(fromComponentId + ":" + xmlFormShortName);

        RecordSet set = pubTemplate.getRecordSet();

        // clone dataRecord
        set.clone(fromId, fromComponentId, cloneId, fromComponentId, attachmentIds);
      }

      // paste wysiwyg
      WysiwygController.copy(fromComponentId, fromId, fromComponentId, cloneId,
          clone.getCreatorId());

      // affectation de l'id du clone à la publication de référence
      refPub.setCloneId(cloneId);
      refPub.setCloneStatus(nextStatus);
      refPub.setStatusMustBeChecked(false);
      refPub.setUpdateDataMustBeSet(false);
      updatePublication(refPub);

      // paste vignette
      String vignette = refPub.getImage();
      if (vignette != null) {
        ThumbnailDetail thumbDetail = new ThumbnailDetail(clone.getPK().getInstanceId(),
            Integer.parseInt(clone.getPK().getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
        thumbDetail.setMimeType(refPub.getImageMimeType());
        if (vignette.startsWith("/")) {
          thumbDetail.setOriginalFileName(vignette);
        } else {
          String thumbnailsSubDirectory = publicationSettings.getString("imagesSubDirectory");
          String from = absolutePath + thumbnailsSubDirectory + File.separator + vignette;
          String type = FilenameUtils.getExtension(vignette);
          String newVignette = System.currentTimeMillis() + "." + type;
          String to = absolutePath + thumbnailsSubDirectory + File.separator + newVignette;
          FileRepositoryManager.copyFile(from, to);
          thumbDetail.setOriginalFileName(newVignette);
        }
        ThumbnailServiceProvider.getThumbnailService().createThumbnail(thumbDetail);
      }
    } catch (IOException | ThumbnailException | FormException | PublicationTemplateException e) {
      throw new KmeliaRuntimeException(e);
    }
    return cloneId;
  }

  /**
   * Gets a service object on the comments.
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    return commentService;
  }

  private LocalizationBundle getMultilang() {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH);
  }

  @Override
  public NodeDetail getRoot(String componentId, String userId) {
    return getRoot(componentId, userId, null);
  }

  private NodeDetail getRoot(String componentId, String userId, List<NodeDetail> treeview) {
    NodePK rootPK = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail root = nodeService.getDetail(rootPK);
    setRole(root, userId);
    root.setChildrenDetails(getRootChildren(root, userId, treeview));
    return root;
  }

  private List<NodeDetail> getRootChildren(NodeDetail root, String userId,
      List<NodeDetail> treeview) {
    String instanceId = root.getNodePK().getInstanceId();
    List<NodeDetail> children = new ArrayList<>();
    try {
      setAllowedSubfolders(root, userId);
      List<NodeDetail> nodes = (List<NodeDetail>) root.getChildrenDetails();

      // set nb objects in nodes
      setNbItemsOfSubfolders(root, treeview, userId);

      NodeDetail trash = null;
      for (NodeDetail node : nodes) {
        if (node.getNodePK().isTrash()) {
          trash = node;
        } else if (!node.getNodePK().isUnclassed()) {
          children.add(node);
        }
      }

      // adding special folder "to validate"
      if (isUserCanValidate(instanceId, userId)) {
        NodeDetail temp = new NodeDetail();
        temp.getNodePK().setId(KmeliaHelper.SPECIALFOLDER_TOVALIDATE);
        temp.setName(getMultilang().getString("ToValidateShort"));
        if (isNbItemsDisplayed(instanceId)) {
          int nbPublisToValidate = getPublicationsToValidate(instanceId, userId).size();
          temp.setNbObjects(nbPublisToValidate);
        }
        children.add(temp);
      }

      // adding special folders "non visible publications"
      if (isUserCanWrite(instanceId, userId)) {
        NodeDetail temp = new NodeDetail();
        temp.getNodePK().setId(KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS);
        temp.setName(getMultilang().getString("kmelia.folder.nonvisiblepubs"));
        if (isNbItemsDisplayed(instanceId)) {
          int nbPublis = getNonVisiblePublications(instanceId, userId).size();
          temp.setNbObjects(nbPublis);
        }
        children.add(temp);

        if (trash != null) {
          children.add(trash);
        }
      }

      root.setChildrenDetails(children);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return children;
  }

  @Override
  public NodeDetail getFolder(final NodePK nodePK, final String userId) {
    NodeDetail node = nodeService.getDetail(nodePK);
    if (node.getNodePK().isRoot()) {
      node.setChildrenDetails(getRootChildren(node, userId, null));
    } else {
      setAllowedSubfolders(node, userId);
    }

    // set nb objects in nodes
    final List<NodeDetail> treeView = getTreeview(nodePK, userId);
    setNbItemsOfFolders(node.getNodePK().getInstanceId(), singletonList(node), treeView);
    setNbItemsOfSubfolders(node, treeView, userId);
    return node;
  }

  @Override
  public Collection<NodeDetail> getFolderChildren(NodePK nodePK, String userId) {
    NodeDetail node = getFolder(nodePK, userId);
    return node.getChildrenDetails();
  }

  private void setNbItemsOfSubfolders(NodeDetail node, List<NodeDetail> treeview, String userId) {
    String instanceId = node.getNodePK().getInstanceId();
    if (isNbItemsDisplayed(instanceId)) {
      if (treeview == null) {
        treeview = getTreeview(node.getNodePK(), userId);
      }
      // set nb objects in each nodes
      setNbItemsOfFolders(instanceId, node.getChildrenDetails(), treeview);
    }
  }

  private List<NodeDetail> getTreeview(NodePK pk, String userId) {
    String instanceId = pk.getInstanceId();
    if (isUserComponentAdmin(instanceId, userId)) {
      return getTreeview(pk, ADMIN_ROLE, isCoWritingEnable(instanceId),
          isDraftVisibleWithCoWriting(), userId, isNbItemsDisplayed(instanceId), false);
    } else {
      return getTreeview(pk, getUserTopicProfile(pk, userId), isCoWritingEnable(instanceId),
          isDraftVisibleWithCoWriting(), userId, isNbItemsDisplayed(instanceId),
          isRightsOnTopicsEnabled(instanceId));
    }
  }

  private void setNbItemsOfFolders(String componentId, Collection<NodeDetail> nodes,
      List<NodeDetail> treeview) {
    if (isNbItemsDisplayed(componentId)) {
      for (NodeDetail child : nodes) {
        int index = treeview.indexOf(child);
        if (index != -1) {
          child.setNbObjects(treeview.get(index).getNbObjects());
        }
      }
    }
  }

  private void setAllowedSubfolders(NodeDetail node, String userId) {
    String instanceId = node.getNodePK().getInstanceId();
    if (isRightsOnTopicsEnabled(instanceId)) {
      if (isUserComponentAdmin(instanceId, userId) ||
          SilverpeasRole.ADMIN.getName().equals(getUserTopicProfile(node.getNodePK(), userId))) {
        // user is admin of application or admin of folder, all subfolders must be shown
        setRole(node.getChildrenDetails(), userId);
      } else {
        Collection<NodeDetail> allowedChildren = getAllowedSubfolders(node, userId);
        setRole(allowedChildren, userId);
        node.setChildrenDetails(allowedChildren);
      }
    }
  }

  private boolean isUserComponentAdmin(String componentId, String userId) {
    return ADMIN_ROLE.equalsIgnoreCase(KmeliaHelper.getProfile(getUserRoles(componentId, userId)));
  }

  private void setRole(NodeDetail node, String userId) {
    if (isRightsOnTopicsEnabled(node.getNodePK().getInstanceId())) {
      node.setUserRole(getUserTopicProfile(node.getNodePK(), userId));
    }
  }

  private void setRole(Collection<NodeDetail> nodes, String userId) {
    for (NodeDetail node : nodes) {
      setRole(node, userId);
    }
  }

  private boolean isRightsOnTopicsEnabled(String componentId) {
    return getBooleanValue(getOrganisationController().getComponentParameterValue(componentId,
        InstanceParameters.rightsOnFolders));
  }

  private boolean isNbItemsDisplayed(String componentId) {
    return getBooleanValue(getOrganisationController().getComponentParameterValue(componentId,
        InstanceParameters.displayNbItemsOnFolders));
  }

  private boolean isCoWritingEnable(String componentId) {
    return getBooleanValue(getOrganisationController().getComponentParameterValue(componentId,
        InstanceParameters.coWriting));
  }

  private boolean isDraftVisibleWithCoWriting() {
    return getComponentSettings().getBoolean("draftVisibleWithCoWriting", false);
  }

  @Override
  public String getUserTopicProfile(NodePK pk, String userId) {
    if (!isRightsOnTopicsEnabled(pk.getInstanceId()) || KmeliaHelper.isSpecialFolder(pk.getId())) {
      return KmeliaHelper.getProfile(getUserRoles(pk.getInstanceId(), userId));
    }

    NodeDetail node = getNodeHeader(pk.getId(), pk.getInstanceId());

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      String rightsDependsOn = node.getRightsDependsOn();
      return KmeliaHelper.getProfile(
          getOrganisationController().getUserProfiles(userId, pk.getInstanceId(),
              ProfiledObjectId.fromNode(rightsDependsOn)));
    } else {
      return KmeliaHelper.getProfile(getUserRoles(pk.getInstanceId(), userId));
    }
  }

  private String[] getUserRoles(String componentId, String userId) {
    final OrganizationController oc = getOrganisationController();
    String[] profiles = oc.getUserProfiles(userId, componentId);
    if (ArrayUtil.isEmpty(profiles) && oc.getComponentInstLight(componentId).isPublic()) {
      profiles = new String[]{KmeliaHelper.ROLE_READER};
    }
    return profiles;
  }

  private NodePK getRootPK(String componentId) {
    return new NodePK(NodePK.ROOT_NODE_ID, componentId);
  }

  /**
   * This method verifies if the user behind the given user identifier can validate the publication
   * represented by the given primary key. The verification is strictly applied on the given primary
   * key, that is to say that no publication clone information are retrieved. To perform a
   * verification on a publication clone, the primary key of the clone must be given.
   * @param pubPK the primary key of the publication or of the clone of a publication.
   * @param userId the identifier of the user fo which rights must be verified.
   * @return true if the user can validate, false otherwise.
   */
  @Override
  public boolean isUserCanValidatePublication(PublicationPK pubPK, String userId) {
    PublicationDetail publi = getPublicationDetail(pubPK);
    if (!publi.isValidationRequired()) {
      // publication is not in a state which allow a validation
      return false;
    }
    final ValidatorsList validators = getAllValidators(pubPK);
    if (!validators.contains(userId)) {
      // current user is not part of users who are able to validate this publication
      return false;
    }

    if (validators.getValidationType() == KmeliaHelper.VALIDATION_TARGET_N) {
      ValidationStep validationStep = publicationService.getValidationStepByUser(pubPK, userId);
      // user has not yet validated publication, so validation is allowed
      return validationStep == null;
    }
    return true;
  }

  @Override
  public boolean isUserCanValidate(String componentId, String userId) {
    if (isToolbox(componentId)) {
      return false;
    }
    return isUserCanPublish(componentId, userId);
  }

  @Override
  public boolean isUserCanWrite(String componentId, String userId) {
    String[] grantedRoles =
        new String[]{SilverpeasRole.ADMIN.getName(), SilverpeasRole.PUBLISHER.getName(),
            SilverpeasRole.WRITER.getName()};
    return checkUserRoles(componentId, userId, grantedRoles);
  }

  @Override
  public boolean isUserCanPublish(String componentId, String userId) {
    String[] grantedRoles =
        new String[]{SilverpeasRole.ADMIN.getName(), SilverpeasRole.PUBLISHER.getName()};
    return checkUserRoles(componentId, userId, grantedRoles);
  }

  private boolean checkUserRoles(String componentId, String userId, String... roles) {
    SilverpeasRole userProfile = SilverpeasRole.fromString(
        KmeliaHelper.getProfile(getUserRoles(componentId, userId)));
    boolean checked = Objects.requireNonNull(userProfile).isInRole(roles);

    if (!checked && isRightsOnTopicsEnabled(componentId)) {
      // check if current user is publisher or admin on at least one descendant
      Iterator<NodeDetail> descendants = nodeService.getDescendantDetails(getRootPK(componentId))
          .iterator();
      while (!checked && descendants.hasNext()) {
        NodeDetail descendant = descendants.next();
        if (descendant.haveLocalRights()) {
          // check if user is admin, publisher or writer on this topic
          String[] profiles = adminController.getProfilesByObjectAndUserId(
              ProfiledObjectId.fromNode(descendant.getNodePK().getId()), componentId, userId);
          if (profiles != null && profiles.length > 0) {
            userProfile = SilverpeasRole.fromString(KmeliaHelper.getProfile(profiles));
            checked = Objects.requireNonNull(userProfile).isInRole(roles);
          }
        }
      }
    }
    return checked;
  }

  @Override
  public NodeDetail getExpandedPathToNode(NodePK pk, String userId) {
    String instanceId = pk.getInstanceId();
    List<NodeDetail> nodes = new ArrayList<>(nodeService.getPath(pk));
    Collections.reverse(nodes);
    nodes.remove(0);

    List<NodeDetail> treeview = null;
    if (isNbItemsDisplayed(instanceId)) {
      treeview = getTreeview(getRootPK(instanceId), userId);
    }

    NodeDetail root = getRoot(instanceId, userId, treeview);

    // set nb objects in nodes
    if (treeview != null) {
      // set nb objects on root
      root.setNbObjects(treeview.get(0).getNbObjects());
      // set nb objects in each allowed nodes
      setNbItemsOfFolders(instanceId, nodes, treeview);
    }

    NodeDetail currentNode = root;
    for (NodeDetail node : nodes) {
      // get children of each node on path to target node
      Collection<NodeDetail> children = nodeService.getChildrenDetails(node.getNodePK());
      node.setChildrenDetails(children);
      setAllowedSubfolders(node, userId);
      if (treeview != null) {
        setNbItemsOfSubfolders(node, treeview, userId);
      }
      if (currentNode != null) {
        currentNode = find(currentNode.getChildrenDetails(), node);
      }
      if (currentNode != null) {
        currentNode.setChildrenDetails(node.getChildrenDetails());
      }
    }
    return root;

  }

  private NodeDetail find(Collection<NodeDetail> nodes, NodeDetail toFind) {
    for (NodeDetail node : nodes) {
      if (node.getNodePK().getId().equals(toFind.getNodePK().getId())) {
        return node;
      }
    }
    return null;
  }

  /**
   * Removes publications according to given ids. Before a publication is removed, user privileges
   * are controlled. If node defines the trash, publications are definitively deleted. Otherwise,
   * publications move into trash.
   * @param publiIds the ids of publications to delete
   * @param topicId the node where the publications are
   * @param userId the user who wants to perform deletion
   * @return the list of publication ids which has been really deleted
   * @
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public List<String> deletePublications(List<String> publiIds, NodePK topicId, String userId) {
    List<String> removedIds = new ArrayList<>();
    String profile = getProfile(userId, topicId);
    for (String id : publiIds) {
      PublicationPK pk = new PublicationPK(id, topicId);
      if (isUserCanDeletePublication(new PublicationPK(id, topicId), profile, userId)) {
        try {
          if (topicId.isTrash() && isPublicationInBasket(pk)) {
            deletePublication(pk);
          } else {
            sendPublicationInBasket(pk);
          }
          removedIds.add(id);
        } catch (Exception e) {
          SilverLogger.getLogger(this)
              .error("Deletion of publication {0} failed", new String[]{pk.getId()}, e);
        }
      }
    }
    return removedIds;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deletePublication(PublicationPK pubPK, String userId) {
    NodePK topicId = publicationService.getMainLocation(pubPK)
        .orElse(null);
    String profile = topicId != null ? getProfile(userId, topicId) : null;
    if (profile != null && isUserCanDeletePublication(pubPK, profile, userId)) {
      try {
        if (topicId.isTrash()) {
          deletePublication(pubPK);
        } else {
          sendPublicationInBasket(pubPK);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .error("Deletion of publication {0} failed", new String[]{pubPK.getId()}, e);
      }
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deleteTopic(@NonNull NodePK topic, String userId) {
    Objects.requireNonNull(topic);
    if (topic.isTrash() || topic.isRoot()) {
      return;
    }
    NodeDetail folder = getNodeHeader(topic);
    boolean isInTrash = nodeService.getPath(topic).get(1).getId().equals(NodePK.BIN_NODE_ID);
    // check if user is allowed to delete this topic
    NodePK root = new NodePK(NodePK.ROOT_NODE_ID, topic.getInstanceId());
    if (SilverpeasRole.ADMIN.isInRole(getUserTopicProfile(topic, userId)) ||
        SilverpeasRole.ADMIN.isInRole(getUserTopicProfile(root, userId)) ||
        SilverpeasRole.ADMIN.isInRole(getUserTopicProfile(folder.getFatherPK(), userId))) {
      if (isInTrash) {
        deleteTopic(topic);
      } else {
        sendTopicInBasket(folder);
      }
    }
  }

  private boolean isUserCanDeletePublication(PublicationPK pubPK, String profile, String userId) {
    User owner = getPublication(pubPK).getCreator();
    return KmeliaPublicationHelper.isRemovable(pubPK.getInstanceId(), userId, profile, owner);
  }

  @Override
  public Optional<KmeliaPublication> getContributionById(ContributionIdentifier contributionId) {
    return Optional.of(getPublication(
        new PublicationPK(contributionId.getLocalId(), contributionId.getComponentInstanceId())));
  }

  @Override
  public SettingBundle getComponentSettings() {
    return settings;
  }

  @Override
  public LocalizationBundle getComponentMessages(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("kmelia") || instanceId.startsWith("kmax") ||
        instanceId.startsWith("toolbox");
  }

  @SimulationActionProcess(elementLister = KmeliaNodeSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void moveNode(@SourcePK NodePK nodePK, @TargetPK NodePK to,
      KmeliaPasteDetail pasteContext) {
    List<NodeDetail> treeToPaste = nodeService.getSubTree(nodePK);

    boolean rightsOnTopicsEnabled = isRightsOnTopicsEnabled(to.getInstanceId());

    // move node and subtree
    nodeService.moveNode(nodePK, to, rightsOnTopicsEnabled);

    for (NodeDetail fromNode : treeToPaste) {
      if (fromNode != null) {
        NodePK toNodePK = new NodePK(fromNode.getNodePK().getId(), to);
        boolean movedToAnotherApp = !nodePK.getInstanceId().equals(to.getInstanceId());

        if (movedToAnotherApp) {
          NodeDetail toNode = nodeService.getDetail(toNodePK);

          checkNodeRights(fromNode, toNode, rightsOnTopicsEnabled);
          // move rich description of node
          if (!nodePK.getInstanceId().equals(to.getInstanceId())) {
            WysiwygController.move(fromNode.getNodePK().getInstanceId(),
                NODE_PREFIX + fromNode.getId(), to.getInstanceId(), NODE_PREFIX + toNodePK.getId());
          }
        }

        // move publications of node
        movePublicationsOfTopic(fromNode.getNodePK(), toNodePK, pasteContext);
      }
    }

    nodePK.setComponentName(to.getInstanceId());
  }

  private void checkNodeRights(final NodeDetail fromNode, final NodeDetail node,
      final boolean rightsOnTopicsEnabled) {
    if (fromNode.haveLocalRights()) {
      List<ProfileInst> profiles = adminController.getProfilesByObject(
          ProfiledObjectId.fromNode(fromNode.getNodePK().getId()),
          fromNode.getNodePK().getInstanceId());
      if (rightsOnTopicsEnabled) {
        // adjusting previous rights according to target component
        for (ProfileInst profile : profiles) {
          if (profile != null && StringUtil.isDefined(profile.getId())) {

            // removing previous rights (can't be reused cause componentId have changed)
            adminController.deleteProfileInst(profile.getId());

            // checking rights and add new profile
            checkNodeProfile(profile, node);
            adminController.addProfileInst(profile);
          }
        }
      } else {
        // target component does not use specific rights, so removing rights
        for (ProfileInst profile: profiles) {
          adminController.deleteProfileInst(profile.getId());
        }
      }
    }
  }

  private void movePublicationsOfTopic(NodePK fromPK, NodePK toPK, KmeliaPasteDetail pasteContext) {
    Collection<PublicationDetail> publications = publicationService.getDetailsByFatherPK(fromPK);
    pasteContext.setFromPK(fromPK);
    for (PublicationDetail publi : publications) {
      movePublication(publi.getPK(), toPK, pasteContext);
    }
  }

  @SimulationActionProcess(elementLister = KmeliaNodeSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  public NodeDetail copyNode(@SourcePK @TargetPK KmeliaCopyDetail copyDetail) {
    HashMap<String, String> oldAndNewIds = new HashMap<>();
    return copyNode(copyDetail, oldAndNewIds);
  }

  private NodeDetail copyNode(KmeliaCopyDetail copyDetail, HashMap<String, String> oldAndNewIds) {
    NodePK nodePKToCopy = copyDetail.getFromNodePK();
    NodePK targetPK = copyDetail.getToNodePK();
    String userId = copyDetail.getUserId();
    NodeDetail nodeToCopy = nodeService.getDetail(nodePKToCopy);
    NodeDetail father = getNodeHeader(targetPK);
    boolean rightsOnTopicsEnabled = isRightsOnTopicsEnabled(targetPK.getInstanceId());

    // paste topic
    NodePK nodePK = new NodePK(UNKNOWN, targetPK);
    NodeDetail node = new NodeDetail(nodeToCopy);
    node.setNodePK(nodePK);
    node.setCreatorId(userId);
    node.setRightsDependsOn(NodeDetail.NO_RIGHTS_DEPENDENCY);
    node.setCreationDate(new Date());
    nodePK = nodeService.createNode(node, father);

    // duplicate the predefined classification on the PdC if any
    copyNodePredefinedClassification(nodeToCopy.getNodePK(), nodePK);

    // duplicate rights
    if (rightsOnTopicsEnabled && copyDetail.isNodeRightsMustBeCopied()) {
      oldAndNewIds.put(nodePKToCopy.getId(), nodePK.getId());
      setNodeRightsDependency(nodeToCopy, father, node);
      // Set topic rights if any
      copyNodeRights(userId, nodeToCopy, nodePK);
    }

    // paste wysiwyg attached to node
    WysiwygController.copy(nodePKToCopy.getInstanceId(), NODE_PREFIX + nodePKToCopy.getId(),
        nodePK.getInstanceId(), NODE_PREFIX + nodePK.getId(), userId);

    // associate model used by copied folder to new folder
    copyUsedModel(nodePKToCopy, nodePK);

    // paste publications of topics
    KmeliaCopyDetail folderContentCopy = new KmeliaCopyDetail(copyDetail);
    folderContentCopy.setFromNodePK(nodePKToCopy);
    folderContentCopy.setToNodePK(nodePK);

    if (copyDetail.isPublicationHeaderMustBeCopied()) {
      copyPublications(folderContentCopy);
    }

    // paste subtopics
    Collection<NodeDetail> subtopics = nodeToCopy.getChildrenDetails();
    for (NodeDetail subTopic : subtopics) {
      if (subTopic != null) {
        folderContentCopy.setFromNodePK(subTopic.getNodePK());
        copyNode(folderContentCopy, oldAndNewIds);
      }
    }

    return node;
  }

  private void copyNodeRights(final String userId, final NodeDetail nodeToCopy,
      final NodePK nodePK) {
    if (nodeToCopy.haveLocalRights()) {
      NodeDetail node = nodeService.getDetail(nodePK);
      List<ProfileInst> topicProfiles = adminController.getProfilesByObject(
          ProfiledObjectId.fromNode(nodeToCopy.getNodePK().getId()),
          nodeToCopy.getNodePK().getInstanceId());
      for (ProfileInst nodeToPasteProfile : topicProfiles) {
        if (nodeToPasteProfile != null) {
          ProfileInst nodeProfileInst = new ProfileInst(nodeToPasteProfile);
          checkNodeProfile(nodeProfileInst, node);
          // Add the profile
          adminController.addProfileInst(nodeProfileInst, userId);
        }
      }
    }
  }

  private void copyNodePredefinedClassification(final NodePK nodeToCopy, final NodePK nodePK) {
    PdcClassification nodeClassification =
        pdcClassificationService.getPreDefinedClassification(nodeToCopy.getId(),
            nodeToCopy.getComponentInstanceId());
    if (!nodeClassification.isEmpty() && nodeClassification.isPredefinedForANode()) {
      PdcClassification copy = nodeClassification.copy()
          .forNode(nodePK.getId())
          .inComponentInstance(nodePK.getComponentInstanceId());
      pdcClassificationService.savePreDefinedClassification(copy);
    }
  }

  /*
   * Checks given profile according to component instance.
   * Removes all groups and users which are not authorized to access to component instance.
   *
   */
  private void checkNodeProfile(ProfileInst profile, NodeDetail node) {
    List<String> verifiedUserIds = new ArrayList<>();
    List<String> verifiedGroupIds = new ArrayList<>();
    String instanceId = node.getNodePK().getInstanceId();

    // check users and groups according to component instance rights
    List<String> userIdsToCheck = profile.getAllUsers();
    for (String userIdToCheck : userIdsToCheck) {
      if (ComponentAccessControl.get().isUserAuthorized(userIdToCheck, instanceId)) {
        verifiedUserIds.add(userIdToCheck);
      }
    }

    List<String> groupIdsToCheck = profile.getAllGroups();
    for (String groupIdToCheck : groupIdsToCheck) {
      if (ComponentAccessControl.get().isGroupAuthorized(groupIdToCheck, instanceId)) {
        verifiedGroupIds.add(groupIdToCheck);
      }
    }

    profile.setId("-1");
    profile.setUsers(verifiedUserIds);
    profile.setGroups(verifiedGroupIds);
    profile.setComponentFatherId(getComponentLocalId(instanceId));
    profile.setObjectId(ProfiledObjectId.fromNode(node.getId()));
    profile.setParentObjectId(ProfiledObjectId.fromNode(node.getFatherPK().getId()));
  }

  private void setNodeRightsDependency(final NodeDetail nodeToCopy, final NodeDetail father,
      final NodeDetail node) {
    if (nodeToCopy.haveRights()) {
      if (nodeToCopy.haveLocalRights()) {
        node.setRightsDependsOnMe();
      } else {
        node.setRightsDependsOn(father.getRightsDependsOn());
      }
      nodeService.updateRightsDependency(node);
    }
  }

  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void copyPublications(@SourcePK @TargetPK KmeliaCopyDetail copyDetail) {
    Collection<PublicationDetail> publications =
        publicationService.getDetailsByFatherPK(copyDetail.getFromNodePK());
    for (PublicationDetail publi : publications) {
      copyPublication(publi, copyDetail);
    }
  }

  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public PublicationDetail copyPublication(@SourcePK PublicationDetail publiToCopy,
      @TargetPK KmeliaCopyDetail copyDetail) {
    NodePK toNodePK = copyDetail.getToNodePK();
    String userId = copyDetail.getUserId();
    final String toComponentId = toNodePK.getInstanceId();
    final NodePK fromNodePK = copyDetail.getFromNodePK();
    final KmeliaPublication publication = fromDetail(publiToCopy, fromNodePK);
    try {
      if (publication.isAlias()) {
        Location location = new Location(copyDetail.getToNodePK().getId(), toComponentId);
        location.setAsAlias(userId);
        publicationService.addAliases(publiToCopy.getPK(), singletonList(location));
        return publiToCopy;
      } else {
        return copyPublication(publiToCopy, toNodePK, toComponentId, copyDetail, userId);
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error("Publication copy failure", ex);
    }
    return null;
  }

  private PublicationDetail copyPublication(final PublicationDetail publiToCopy, final NodePK toNodePK,
      final String toComponentId, final KmeliaCopyDetail copyDetail, final String userId)
      throws PdcException, PublicationTemplateException, FormException {
    ResourceReference toResourceReference = new ResourceReference(ResourceReference.UNKNOWN_ID,
        toNodePK.getInstanceId());
    PublicationPK toPubPK = new PublicationPK(UNKNOWN, toNodePK);

    // Handle duplication as a creation, ignore initial parameters
    PublicationDetail newPubli = PublicationDetail.builder(publiToCopy.getLanguage())
        .setPk(toPubPK)
        .setNameAndDescription(publiToCopy.getName(), publiToCopy.getDescription())
        .setVersion(publiToCopy.getVersion())
        .setKeywords(publiToCopy.getKeywords())
        .setBeginDateTime(publiToCopy.getBeginDate(), publiToCopy.getBeginHour())
        .setEndDateTime(publiToCopy.getEndDate(), publiToCopy.getEndHour())
        .setImportance(publiToCopy.getImportance())
        .build();

    newPubli.setTranslations(publiToCopy.getClonedTranslations());
    newPubli.setAuthor(publiToCopy.getAuthor());
    newPubli.setCreatorId(userId);
    if (copyDetail.isPublicationContentMustBeCopied()) {
      newPubli.setInfoId(publiToCopy.getInfoId());
    }

    if (copyDetail.isAdministrativeOperation()) {
      newPubli.setCreatorId(publiToCopy.getCreatorId());
      newPubli.setCreationDate(publiToCopy.getCreationDate());
      newPubli.setUpdaterId(publiToCopy.getUpdaterId());
      newPubli.setUpdateDate(publiToCopy.getLastUpdateDate());
      newPubli.setStatus(publiToCopy.getStatus());
      newPubli.setTargetValidatorId(publiToCopy.getTargetValidatorId());
    } else {
      // use validators selected via UI
      newPubli.setTargetValidatorId(copyDetail.getPublicationValidatorIds());

      // manage status explicitly to bypass Draft mode
      setToByPassDraftMode(copyDetail, toNodePK, newPubli, userId);
    }

    String fromId = publiToCopy.getPK().getId();
    String fromComponentId = publiToCopy.getPK().getInstanceId();
    ResourceReference fromResourceReference = new ResourceReference(publiToCopy.getPK().getId(),
        fromComponentId);
    PublicationPK fromPubPK = new PublicationPK(publiToCopy.getPK().getId(), fromComponentId);

    String id = createPublicationIntoTopic(newPubli, toNodePK);
    // update id cause new publication is created
    toPubPK.setId(id);
    toResourceReference.setId(id);

    // Copy vignette
    ThumbnailController.copyThumbnail(fromResourceReference, toResourceReference);

    // Copy positions on Pdc
    if (copyDetail.isPublicationPositionsMustBeCopied()) {
      copyPdcPositions(fromPubPK, toPubPK);
    }

    // Copy files
    Map<String, String> fileIds = new HashMap<>();
    if (copyDetail.isPublicationFilesMustBeCopied()) {
      fileIds.putAll(copyFiles(fromPubPK, toPubPK));
    }

    // Copy content
    if (copyDetail.isPublicationContentMustBeCopied()) {
      String xmlFormShortName = newPubli.getInfoId();
      if (xmlFormShortName != null && !"0".equals(xmlFormShortName)) {
        registerXmlForm(fromComponentId, fromResourceReference, toComponentId, toResourceReference,
            xmlFormShortName, fileIds);
      } else {
        // paste wysiwyg
        WysiwygController.copy(fromComponentId, fromId, toPubPK.getInstanceId(), id, userId);
      }
    }

    // Index publication to index its files and content
    publicationService.createIndex(toPubPK);

    return newPubli;
  }

  private void setToByPassDraftMode(@TargetPK final KmeliaCopyDetail copyDetail,
      final NodePK nodePK, final PublicationDetail newPubli, final String userId) {
    if (StringUtil.isDefined(copyDetail.getPublicationStatus())) {
      String profile = getProfile(userId, nodePK);
      if (!copyDetail.getPublicationStatus().equals(PublicationDetail.DRAFT_STATUS)) {
        //noinspection ConstantConditions
        if (SilverpeasRole.fromString(profile).isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
          newPubli.setStatus(PublicationDetail.VALID_STATUS);
        } else {
          // case of writer
          newPubli.setStatus(PublicationDetail.TO_VALIDATE_STATUS);
        }
      }
    }
  }

  private void registerXmlForm(final String fromComponentId,
      final ResourceReference fromResourceReference, final String toComponentId,
      final ResourceReference toResourceReference, final String xmlFormShortName,
      final Map<String, String> fileIds) throws PublicationTemplateException, FormException {
    // Content = XMLForm
    // register xmlForm to publication
    PublicationTemplateManager publicationTemplateManager =
        PublicationTemplateManager.getInstance();
    GenericRecordSet toRecordset = publicationTemplateManager.addDynamicPublicationTemplate(
        toComponentId + ":" + xmlFormShortName, xmlFormShortName + ".xml");

    PublicationTemplate pubTemplate =
        publicationTemplateManager.getPublicationTemplate(fromComponentId + ":" + xmlFormShortName);
    RecordSet set = pubTemplate.getRecordSet();

    set.copy(fromResourceReference, toResourceReference, toRecordset.getRecordTemplate(), fileIds);
  }

  private Map<String, String> copyFiles(PublicationPK fromPK, PublicationPK toPK) {
    Map<String, String> fileIds = new HashMap<>();
    List<SimpleDocument> origins =
        getAttachmentService().listDocumentsByForeignKeyAndType(fromPK.toResourceReference(),
            DocumentType.attachment, null);
    for (SimpleDocument origin : origins) {
      SimpleDocumentPK copyPk =
          getAttachmentService().copyDocument(origin, new ResourceReference(toPK));
      fileIds.put(origin.getId(), copyPk.getId());
    }
    return fileIds;
  }

  private void copyPdcPositions(PublicationPK fromPK, PublicationPK toPK) throws PdcException {
    int fromSilverObjectId = getSilverObjectId(fromPK);
    int toSilverObjectId = getSilverObjectId(toPK);

    pdcManager.copyPositions(fromSilverObjectId, fromPK.getInstanceId(), toSilverObjectId,
        toPK.getInstanceId());
  }

  @Override
  public List<KmeliaPublication> filterPublications(List<KmeliaPublication> publications,
      String instanceId, SilverpeasRole profile, String userId) {
    final boolean coWriting = isCoWritingEnable(instanceId);
    final RemovedSpaceAndComponentInstanceChecker checker = RemovedSpaceAndComponentInstanceChecker.create();
    final Predicate<KmeliaPublication> removedComponentInstance =
        k -> checker.isRemovedComponentInstanceById(k.getComponentInstanceId());
    final Predicate<KmeliaPublication> visiblePublication =
        k -> isPublicationVisible(k.getDetail(), profile, userId, coWriting);
    return publications.stream()
        .filter(not(removedComponentInstance).and(visiblePublication))
        .collect(Collectors.toList());
  }

  @Override
  public void userHaveBeenDeleted(String userId) {
    List<PublicationDetail> publications = publicationService.removeUserFromTargetValidators(
        userId);
    SilverLogger.getLogger(this)
        .debug("User ''{0}'' have been removed from {1} publications as target validator", userId,
            publications.size());

    // Validation process is performed, maybe some must be validated.
    KmeliaValidation.by(userId).validatorHasNoMoreRight().validate(publications);
  }

  private boolean isPublicationVisible(PublicationDetail detail, SilverpeasRole profile,
      String userId, boolean coWriting) {
    if (detail.getStatus() != null) {
      if (detail.isValid()) {
        return isVisible(detail, profile, userId, coWriting);
      } else {
        // si le thème est en co-rédaction, toutes les publications sont visibles par tous,
        // sauf les lecteurs
        if (detail.isDraft()) {
          // si le theme est en co-rédaction et si on autorise le mode brouillon visible par tous
          // toutes les publications en mode brouillon sont visibles par tous, sauf les lecteurs
          // sinon, seules les publications brouillon sont visibles à l'utilisateur
          return isVisibleInDraft(detail, profile, userId, coWriting);
        } else {
          return isVisibleInPublished(detail, profile, userId, coWriting);
        }
      }
    }
    return false;
  }

  private boolean isVisibleInDraft(final PublicationDetail detail, final SilverpeasRole profile,
      final String userId, final boolean coWriting) {
    return userId.equals(detail.getCreatorId()) || userId.equals(detail.getUpdaterId()) ||
        (coWriting && isDraftVisibleWithCoWriting() && profile != SilverpeasRole.USER) ||
        (detail.haveGotClone() && profile.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER));
  }

  private boolean isVisibleInPublished(final PublicationDetail detail, final SilverpeasRole profile,
      final String userId, final boolean coWriting) {
    return profile == SilverpeasRole.ADMIN || profile == SilverpeasRole.PUBLISHER ||
        userId.equals(detail.getCreatorId()) || userId.equals(detail.getUpdaterId()) ||
        (profile != SilverpeasRole.USER && coWriting);
  }

  private boolean isVisible(final PublicationDetail detail, final SilverpeasRole profile,
      final String userId, final boolean coWriting) {
    if (detail.isVisible()) {
      return true;
    } else {
      return profile == SilverpeasRole.ADMIN || userId.equals(detail.getUpdaterId()) ||
          (profile != SilverpeasRole.USER && coWriting);
    }
  }

  private PublicationDetail clonePublication(String cloneId, PublicationPK pubPK,
      String validatorUserId, Date validationDate) {
    PublicationPK tempPK = new PublicationPK(cloneId, pubPK);
    CompletePublication publication = publicationService.getCompletePublication(tempPK);
    PublicationDetail clone = getClone(publication.getPublicationDetail());
    clone.setPk(pubPK);
    if (validatorUserId != null) {
      clone.setValidatorId(validatorUserId);
      clone.setValidateDate(validationDate != null ? validationDate : new Date());
    }
    clone.setStatus(PublicationDetail.VALID_STATUS);
    clone.setCloneId(UNDEFINED_NODE_ID);
    clone.setCloneStatus(null);
    return clone;
  }

  private RecordSet getXMLFormFrom(String infoId, PublicationPK pubPK)
      throws PublicationTemplateException {
    PublicationTemplateManager publicationTemplateManager = PublicationTemplateManager.getInstance();
    PublicationTemplate pubTemplate = publicationTemplateManager.getPublicationTemplate(
        pubPK.getInstanceId() + ":" + infoId);

    return pubTemplate.getRecordSet();
  }

  protected void onDocumentDeletion(AttachmentRef attachment) {
    Optional<KmeliaOperationContext> context = KmeliaOperationContext.current();
    if (context.isEmpty() || !context.get().isAbout(DELETION)) {
      PublicationPK pubPK = new PublicationPK(attachment.getForeignId(),
          attachment.getInstanceId());
      externalElementsOfPublicationHaveChanged(pubPK, attachment.getUserId(), false);
    }
  }

  @Override
  public UserNotification getUserNotification(NodePK pk) {
    NodeDetail node = getNodeHeader(pk);
    return new KmeliaNotifyTopicUserNotification(node).build();
  }

  @Override
  public List<String> getActiveValidatorIds(PublicationPK pk) {
    PublicationDetail pub = getPublicationDetail(pk);
    return getActiveValidatorIds(pub);
  }

  private List<String> getActiveValidatorIds(PublicationDetail publication) {
    List<String> activeValidatorIds = new ArrayList<>();
    String[] validatorIds = publication.getTargetValidatorIds();
    if (validatorIds == null) {
      return activeValidatorIds;
    }
    for (String userId : validatorIds) {
      String profile = getProfileOnPublication(userId, publication.getPK());
      //noinspection ConstantConditions
      if (profile != null &&
          SilverpeasRole.fromString(profile).isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
        activeValidatorIds.add(userId);
      }
    }
    return activeValidatorIds;
  }

  @Override
  public void performReminder(final Reminder reminder) {
    if (KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION.asString().equals(reminder.getProcessName())) {
      getContributionById(reminder.getContributionId()).filter(
              p -> !isPublicationInBasket(p.getPk()))
          .map(KmeliaPublication::getDetail)
          .ifPresent(p -> sendSubscriptionsNotification(p, NotifAction.PUBLISHED, false));
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteClone(PublicationPK pk) {
    PublicationDetail clone = getPublicationDetail(pk);
    PublicationDetail original = getPublicationDetail(clone.getClonePK());

    //delete clone itself
    deletePublication(pk);

    //remove reference to clone from original publication
    original.setCloneId(null);
    original.setCloneStatus(null);
    original.setUpdateDataMustBeSet(false);
    original.setIndexOperation(IndexManager.NONE);
    publicationService.setDetail(original);
  }

  @Override
  public List<KmeliaPublication> getNonVisiblePublications(String componentId, String userId) {
    try {
      Collection<PublicationDetail> temp = publicationService.getPublicationsByCriteria(
          PublicationCriteria.onComponentInstanceIds(componentId)
              .ofStatus(PublicationDetail.VALID_STATUS)
              .nonVisibleAt(OffsetDateTime.now())
              .excludingNodes(NodePK.BIN_NODE_ID)
              .orderByDescendingLastUpdateDate());
      // only publications allowed by current user must be returned
      List<PublicationDetail> publications = PublicationAccessControl.get()
          .filterAuthorizedByUser(userId, temp)
          .collect(Collectors.toList());
      return asKmeliaPublication(publications);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private class PublicationConcernedByUpdate {
    private final PublicationPK pubPK;
    private PublicationDetail pubDetail;
    private boolean isPublicationInBasketBeforeUpdate;

    public PublicationConcernedByUpdate(final PublicationPK pubPK) {
      this.pubPK = pubPK;
    }

    public PublicationDetail getPubDetail() {
      return pubDetail;
    }

    public boolean isPublicationInBasketBeforeUpdate() {
      return isPublicationInBasketBeforeUpdate;
    }

    public PublicationConcernedByUpdate invoke() {
      pubDetail = null;
      isPublicationInBasketBeforeUpdate = false;
      try {
        isPublicationInBasketBeforeUpdate = isPublicationInBasket(pubPK);
        pubDetail = getPublicationDetail(pubPK);
      } catch (Exception e) {
        // publication no longer exists do not throw exception because this method is called by JMS
        // layer
        // if exception is throw, JMS will attempt to execute it again and again...
        SilverLogger.getLogger(DefaultKmeliaService.this)
            .error("Impossible to get the publication {0}", pubPK.getId());
      }
      return this;
    }

    public boolean isPublicationNotDefined() {
      return pubDetail == null || (StringUtil.isDefined(pubPK.getInstanceId()) &&
          !pubDetail.getInstanceId().equals(pubPK.getInstanceId()));
    }
  }

  private class ValidationChecker {
    private PublicationPK pubPK;
    private String userId;
    private PublicationDetail currentPubDetail;
    private PublicationDetail currentPubOrCloneDetail;
    private PublicationPK validatedPK;
    private boolean validationComplete = false;
    private String validatorUserId;
    private Date validationDate;

    public ValidationChecker setPubPK(final PublicationPK pubPK) {
      this.pubPK = pubPK;
      return this;
    }

    public ValidationChecker setUserId(final String userId) {
      this.userId = userId;
      return this;
    }

    public ValidationChecker setCurrentPubDetail(final PublicationDetail currentPubDetail) {
      this.currentPubDetail = currentPubDetail;
      return this;
    }

    public ValidationChecker setCurrentPubOrCloneDetail(
        final PublicationDetail currentPubOrCloneDetail) {
      this.currentPubOrCloneDetail = currentPubOrCloneDetail;
      return this;
    }

    public ValidationChecker setValidatedPK(final PublicationPK validatedPK) {
      this.validatedPK = validatedPK;
      return this;
    }

    public ValidationChecker setValidatorUserId(final String validatorUserId) {
      this.validatorUserId = validatorUserId;
      return this;
    }

    public ValidationChecker setValidationDate(final Date validationDate) {
      this.validationDate = validationDate;
      return this;
    }

    public boolean isValidationComplete() {
      return validationComplete;
    }

    public String getValidatorUserId() {
      return validatorUserId;
    }

    public Date getValidationDate() {
      return validationDate;
    }

    public ValidationChecker check() {
      // User has no more validation right
      final int validationType = getValidationType(pubPK.getInstanceId());
      boolean alertPublicationOwnerThereIsNoMoreValidator = false;
      switch (validationType) {
        case KmeliaHelper.VALIDATION_CLASSIC:
        case KmeliaHelper.VALIDATION_TARGET_1:
          alertPublicationOwnerThereIsNoMoreValidator = true;
          break;
        default:
          // get all users who have to validate
          List<String> allValidators = getAllValidators(validatedPK);
          if (allValidators.isEmpty()) {
            alertPublicationOwnerThereIsNoMoreValidator = true;
          } else {
            // check if all validators have give their decision
            validationComplete =
                DefaultKmeliaService.this.isValidationComplete(validatedPK, allValidators);
            if (validationComplete) {
              findLastValidation();
            } else if (validationType == KmeliaHelper.VALIDATION_TARGET_N &&
                StringUtil.isNotDefined(currentPubOrCloneDetail.getTargetValidatorId())) {
              // Case of fallback solution when no more validator is defined, all publishers
              // must validate (as collegiate method)
              alertPublicationOwnerThereIsNoMoreValidator = true;
            }
          }
      }

      if (alertPublicationOwnerThereIsNoMoreValidator) {
        NodePK fatherPK = getPublicationFatherPK(currentPubDetail.getPK());
        if (fatherPK != null) {
          sendNoMoreValidatorNotification(fatherPK, currentPubDetail);
        }
      }
      return this;
    }

    private void findLastValidation() {
      // taking the last effective validator for the state change.
      validationDate = new Date(0);
      for (ValidationStep validationStep : publicationService.getValidationSteps(validatedPK)) {
        final String validationStepUserId = validationStep.getUserId();
        if (!validationStepUserId.equals(userId) &&
            validationStep.getValidationDate().compareTo(validationDate) > 0) {
          validationDate = validationStep.getValidationDate();
          validatorUserId = validationStepUserId;
        }
      }
    }

    private void sendNoMoreValidatorNotification(final NodePK fatherPK,
        final PublicationDetail pubDetail) {
      if (pubDetail.isValidationRequired() || pubDetail.isValid()) {
        try {
          UserNotificationHelper.buildAndSend(
              new KmeliaNoMoreValidatorPublicationUserNotification(fatherPK, pubDetail));
        } catch (Exception e) {
          SilverLogger.getLogger(this)
              .error("fatherId = {0}, pubPK = {1}",
                  new String[]{fatherPK.getId(), pubDetail.getPK().toString()}, e);
        }
      }
    }
  }
}
