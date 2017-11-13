/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.components.kmelia.InstanceParameters;
import org.silverpeas.components.kmelia.KmeliaAuthorization;
import org.silverpeas.components.kmelia.KmeliaContentManager;
import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.PublicationImport;
import org.silverpeas.components.kmelia.model.KmaxRuntimeException;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.model.TopicComparator;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.components.kmelia.notification.*;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.publication.datereminder.PublicationNoteReference;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.notification.PublicationEventNotifier;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.template.form.dao.ModelDAO;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.datereminder.persistence.service.PersistentDateReminderService;
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
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
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
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

import static org.silverpeas.components.kmelia.service.KmeliaServiceContext.*;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider
    .getOrganisationController;
import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;
import static org.silverpeas.core.exception.SilverpeasRuntimeException.ERROR;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * This is the KMelia Service controller of the MVC. It controls all the activities that happen in a
 * client session. It also provides mechanisms to access other services.
 * Service which manage kmelia and kmax application.
 * @author Nicolas Eysseric
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultKmeliaService implements KmeliaService {

  private static final String MESSAGES_PATH = "org.silverpeas.kmelia.multilang.kmeliaBundle";
  private static final String SETTINGS_PATH = "org.silverpeas.kmelia.settings.kmeliaSettings";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);
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
  private PublicationEventNotifier notifier;
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
  private PersistentDateReminderService dateReminderService;
  @Inject
  private KmeliaContentManager kmeliaContentManager;

  public DefaultKmeliaService() {
  }

  private int getNbPublicationsOnRoot(String componentId) {
    String parameterValue =
        getOrganisationController().getComponentParameterValue(componentId, "nbPubliOnRoot");
    if (isDefined(parameterValue)) {
      return Integer.parseInt(parameterValue);
    } else {
      if (KmeliaHelper.isToolbox(componentId)) {

        return 0;
      }
      // lecture du properties
      SettingBundle theSettings = getComponentSettings();
      return theSettings.getInteger("HomeNbPublications");
    }
  }

  private boolean isDraftModeUsed(String componentId) {
    return "yes".
        equals(getOrganisationController().getComponentParameterValue(componentId, "draft"));
  }

  public SubscriptionService getSubscribeService() {
    return SubscriptionServiceProvider.getSubscribeService();
  }

  /**
   * Return a the detail of a topic
   * @param pk the id of the topic
   * @param userId
   * @param isTreeStructureUsed
   * @param userProfile
   * @param isRightsOnTopicsUsed
   * @return
   */
  @Override
  public TopicDetail goTo(NodePK pk, String userId, boolean isTreeStructureUsed, String userProfile,
      boolean isRightsOnTopicsUsed) {
    Collection<NodeDetail> newPath = new ArrayList<>();
    NodeDetail nodeDetail = null;

    // get the basic information (Header) of this topic
    try {
      nodeDetail = nodeService.getDetail(pk);
      if (isRightsOnTopicsUsed) {
        OrganizationController orga = getOrganisationController();
        if (nodeDetail.haveRights() && !orga
            .isObjectAvailable(nodeDetail.getRightsDependsOn(), ObjectType.NODE, pk.getInstanceId(),
                userId)) {
          nodeDetail.setUserRole("noRights");
        }
        List<NodeDetail> availableChildren = getAllowedSubfolders(nodeDetail, userId);
        nodeDetail.setChildrenDetails(availableChildren);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.goTo()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e);
    }
    // get publications
    List<KmeliaPublication> pubDetails =
        getPublicationsOfFolder(pk, userProfile, userId, isTreeStructureUsed, isRightsOnTopicsUsed);

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
  public List<KmeliaPublication> getPublicationsOfFolder(NodePK pk, String userProfile,
      String userId, boolean isTreeStructureUsed, boolean isRightsOnTopicsUsed) {
    Collection<PublicationDetail> pubDetails = null;

    // get the publications associated to this topic
    if (pk.isTrash()) {
      // Topic = Basket
      pubDetails = getPublicationsInBasket(pk, userProfile, userId);
    } else if (pk.isRoot()) {
      try {
        int nbPublisOnRoot = getNbPublicationsOnRoot(pk.getInstanceId());
        if (nbPublisOnRoot == 0 || !isTreeStructureUsed ||
            KmeliaHelper.isToolbox(pk.getInstanceId())) {
          pubDetails = publicationService.getDetailsByFatherPK(pk, "P.pubUpdateDate desc", false);
        } else {
          return getLatestPublications(pk.getInstanceId(), nbPublisOnRoot, isRightsOnTopicsUsed,
              userId);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationsOfFolder()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DAVOIR_LES_DERNIERES_PUBLICATIONS", e);
      }
    } else {
      try {
        // get the publication details linked to this topic
        pubDetails = publicationService
            .getDetailsByFatherPK(pk, "P.pubUpdateDate DESC, P.pubId DESC", false);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationsOfFolder()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DAVOIR_LA_LISTE_DES_PUBLICATIONS", e);
      }
    }
    return pubDetails2userPubs(pubDetails);
  }

  @Override
  public List<KmeliaPublication> getLatestPublications(String instanceId, int nbPublisOnRoot,
      boolean isRightsOnTopicsUsed, String userId) {
    PublicationPK pubPK = new PublicationPK("unknown", instanceId);
    Collection<PublicationDetail> pubDetails = publicationService.
        getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(pubPK, PublicationDetail.VALID,
            nbPublisOnRoot, NodePK.BIN_NODE_ID);
    if (isRightsOnTopicsUsed) {// The list of publications must be filtered
      List<PublicationDetail> filteredList = new ArrayList<>();
      KmeliaAuthorization security = new KmeliaAuthorization();
      for (PublicationDetail pubDetail : pubDetails) {
        if (security
            .isObjectAvailable(instanceId, userId, pubDetail.getPK().getId(), "Publication")) {
          filteredList.add(pubDetail);
        }
      }
      pubDetails.clear();
      pubDetails.addAll(filteredList);
    }
    return pubDetails2userPubs(pubDetails);
  }

  @Override
  public List<NodeDetail> getAllowedSubfolders(NodeDetail folder, String userId) {
    OrganizationController orga = getOrganisationController();
    NodePK pk = folder.getNodePK();
    List<NodeDetail> children = (List<NodeDetail>) folder.getChildrenDetails();
    List<NodeDetail> availableChildren = new ArrayList<>();
    for (NodeDetail child : children) {
      NodePK childId = child.getNodePK();
      if (childId.isTrash() || childId.isUnclassed() || !child.haveRights()) {
        availableChildren.add(child);
      } else {
        int rightsDependsOn = child.getRightsDependsOn();
        boolean nodeAvailable = orga.isObjectAvailable(rightsDependsOn, ObjectType.NODE, pk.
            getInstanceId(), userId);
        if (nodeAvailable) {
          availableChildren.add(child);
        } else { // check if at least one descendant is available
          Iterator<NodeDetail> descendants = nodeService.getDescendantDetails(child).iterator();
          boolean childAllowed = false;
          while (!childAllowed && descendants.hasNext()) {
            NodeDetail descendant = descendants.next();
            if (descendant.getRightsDependsOn() != rightsDependsOn) {
              // different rights of father check if it is available
              if (orga.isObjectAvailable(descendant.getRightsDependsOn(), ObjectType.NODE, pk.
                  getInstanceId(), userId)) {
                childAllowed = true;
                if (!availableChildren.contains(child)) {
                  availableChildren.add(child);
                }
              }
            }
          }
        }
      }
    }
    return availableChildren;
  }

  private Collection<NodeDetail> getPathFromAToZ(NodeDetail nd) {
    Collection<NodeDetail> newPath = new ArrayList<>();
    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeService.getPath(nd.getNodePK());
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPathFromAToZ()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CHEMIN_COURANT", e);
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.addToTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LE_THEME", e);
    }
    return theNodePK;
  }

  /**
   * Add a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   * @param fatherPK
   * @param subTopic the NodeDetail of the new sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   */
  @Override
  public NodePK addSubTopic(NodePK fatherPK, NodeDetail subTopic, String alertType) {
    // Construction de la date de création (date courante)
    String creationDate = DateUtil.today2SQLDate();
    subTopic.setCreationDate(creationDate);
    // Web visibility parameter. The topic is by default invisible.
    subTopic.setStatus("Invisible");
    // add new topic to current topic
    NodePK pk = addToTopic(fatherPK, subTopic);
    // Creation alert
    if (!"-1".equals(pk.getId())) {
      topicCreationAlert(pk, fatherPK, alertType);
    }
    return pk;
  }

  /**
   * Alert all users, only publishers or nobody of the topic creation or update
   * @param nodePK the NodePK of the new sub topic
   * @param fatherPK the NodePK of the parent topic
   * @param alertType alertType = "All"|"Publisher"|"None"
   * @see NodePK
   * @since 1.0
   */
  private void topicCreationAlert(final NodePK nodePK, final NodePK fatherPK,
      final String alertType) {
    UserNotificationHelper
        .buildAndSend(new KmeliaTopicUserNotification(nodePK, fatherPK, alertType));
  }

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists
   * a
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
    try {
      // Order of the node must be unchanged
      NodeDetail oldNode = nodeService.getHeader(topic.getNodePK());
      int order = oldNode.getOrder();
      topic.setOrder(order);
      nodeService.setDetail(topic);

      // manage operations relative to folder rights
      if (isRightsOnTopicsEnabled(topic.getNodePK().getInstanceId())) {
        if (oldNode.getRightsDependsOn() != topic.getRightsDependsOn()) {
          // rights dependency have changed
          if (!topic.haveRights()) {

            NodeDetail father = nodeService.getHeader(oldNode.getFatherPK());
            topic.setRightsDependsOn(father.getRightsDependsOn());

            // Topic profiles must be removed
            List<ProfileInst> profiles = adminController
                .getProfilesByObject(topic.getNodePK().getId(), ObjectType.NODE.getCode(),
                    topic.getNodePK().getInstanceId());
            if (profiles != null) {
              for (ProfileInst profile : profiles) {
                if (profile != null) {
                  adminController.deleteProfileInst(profile.getId());
                }
              }
            }
          } else {
            topic.setRightsDependsOnMe();
          }
          nodeService.updateRightsDependency(topic);
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.updateTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
    // Update Alert
    topicCreationAlert(topic.getNodePK(), null, alertType);
    return topic.getNodePK();
  }

  @Override
  public NodeDetail getSubTopicDetail(NodePK pk) {
    NodeDetail subTopic;
    // get the basic information (Header) of this topic
    try {
      subTopic = nodeService.getDetail(pk);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getSubTopicDetail()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e);
    }
    return subTopic;
  }

  /**
   * Delete a topic and all descendants. Delete all links between descendants and publications.
   * This
   * publications will be visible in the Declassified zone. Delete All subscriptions and favorites
   * on this topics and all descendants
   * @param pkToDelete the id of the topic to delete
   * @since 1.0
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteTopic(NodePK pkToDelete) {
    try {
      // get all nodes which will be deleted
      Collection<NodePK> nodesToDelete = nodeService.getDescendantPKs(pkToDelete);
      nodesToDelete.add(pkToDelete);
      Iterator<PublicationPK> itPub;
      Collection<PublicationPK> pubsToCheck; // contains all PubPKs concerned by
      // the delete
      NodePK oneNodeToDelete; // current node to delete
      Collection<NodePK> pubFathers; // contains all fatherPKs to a given
      // publication
      PublicationPK onePubToCheck; // current pub to check
      Iterator<NodePK> itNode = nodesToDelete.iterator();
      List<Alias> aliases = new ArrayList<>();
      while (itNode.hasNext()) {
        oneNodeToDelete = itNode.next();
        // get pubs linked to current node (includes alias)
        pubsToCheck = publicationService.getPubPKsInFatherPK(oneNodeToDelete);
        itPub = pubsToCheck.iterator();
        // check each pub contained in current node
        while (itPub.hasNext()) {
          onePubToCheck = itPub.next();
          if (onePubToCheck.getInstanceId().equals(oneNodeToDelete.getInstanceId())) {
            // get fathers of the pub
            pubFathers = publicationService.getAllFatherPK(onePubToCheck);
            if (pubFathers.size() >= 2) {
              // the pub have got many fathers
              // delete only the link between pub and current node
              publicationService.removeFather(onePubToCheck, oneNodeToDelete);
            } else {
              sendPublicationToBasket(onePubToCheck);
            }
          } else {
            // remove alias
            aliases.clear();
            aliases.add(new Alias(oneNodeToDelete.getId(), oneNodeToDelete.getInstanceId()));
            publicationService.removeAlias(onePubToCheck, aliases);
          }
        }
      }

      // Delete all subscriptions on this topic and on its descendants
      removeSubscriptionsByTopic(nodesToDelete);

      // Delete the topic
      nodeService.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.deleteTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_THEME", e);
    }
  }

  @Override
  public void changeSubTopicsOrder(String way, NodePK subTopicPK, NodePK fatherPK) {
    List<NodeDetail> subTopics;
    try {
      subTopics = (List<NodeDetail>) nodeService.getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.changeSubTopicsOrder()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_LISTER_THEMES", e);
    }

    if (subTopics != null && !subTopics.isEmpty()) {
      int indexOfTopic;

      if (fatherPK.isRoot() && !KmeliaHelper.isToolbox(subTopicPK.getInstanceId())) {
        // search the place of the basket
        indexOfTopic = getIndexOfNode("1", subTopics);
        // remove the node
        subTopics.remove(indexOfTopic);
        // search the place of the declassified
        indexOfTopic = getIndexOfNode("2", subTopics);
        // remove the node
        subTopics.remove(indexOfTopic);
      }

      // search the place of the topic we want to move
      indexOfTopic = getIndexOfNode(subTopicPK.getId(), subTopics);
      // get the node to move
      NodeDetail node2move = subTopics.get(indexOfTopic);
      // remove the node to move
      subTopics.remove(indexOfTopic);

      if (way.equals("up")) {
        subTopics.add(indexOfTopic - 1, node2move);
      } else {
        subTopics.add(indexOfTopic + 1, node2move);
      }

      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        NodeDetail nodeDetail = subTopics.get(i);
        try {
          nodeDetail.setOrder(i);
          nodeService.setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException("DefaultKmeliaService.changeSubTopicsOrder()", ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
        }
      }
    }
  }

  private int getIndexOfNode(String nodeId, List<NodeDetail> nodes) {
    int index = 0;
    if (nodes != null) {
      for (NodeDetail node : nodes) {
        if (nodeId.equals(node.getNodePK().getId())) {
          return index;
        }
        index++;
      }
    }
    return index;
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
          NodeDetail nodeDetail = aSubTree;
          changeTopicStatus(newStatus, nodeDetail);
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.changeTopicStatus()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
  }

  @Override
  public void sortSubTopics(NodePK fatherPK) {
    sortSubTopics(fatherPK, false, null);
  }

  @Override
  public void sortSubTopics(NodePK fatherPK, boolean recursive, String[] criteria) {
    List<NodeDetail> subTopics = null;
    try {
      subTopics = (List<NodeDetail>) nodeService.getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.sortSubTopics()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_LISTER_THEMES", e);
    }

    if (subTopics != null && subTopics.size() > 0) {
      Collections.sort(subTopics, new TopicComparator(criteria));
      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        NodeDetail nodeDetail = subTopics.get(i);
        try {
          nodeDetail.setOrder(i);
          nodeService.setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException("DefaultKmeliaService.sortSubTopics()", ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.changeTopicStatus()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
  }

  @Override
  public List<NodeDetail> getTreeview(NodePK nodePK, String profile, boolean coWritingEnable,
      boolean draftVisibleWithCoWriting, String userId, boolean displayNb,
      boolean isRightsOnTopicsUsed) {

    String instanceId = nodePK.getInstanceId();
    List<NodeDetail> allowedTree = nodeService.getSubTree(nodePK);

    if (profile == null) {
      profile = getProfile(userId, nodePK);
    }

    KmeliaUserTreeViewFilter
        .from(userId, instanceId, nodePK, profile, isRightsOnTopicsUsed)
        .setBestUserRoleAndFilter(allowedTree);

    if (displayNb) {
      boolean checkVisibility = false;
      StringBuilder statusSubQuery = new StringBuilder();
      if (profile.equals("user")) {
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
              .append("sb_publication_publi.pubUpdaterId = '").
              append(userId).append("') OR (sb_publication_publi.pubStatus = 'Unvalidate' AND ").
              append("sb_publication_publi.pubUpdaterId = '").append(userId).append("') ");
        }
        statusSubQuery.append("OR (sb_publication_publi.pubStatus = 'ToValidate' ")
            .append("AND sb_publication_publi.pubUpdaterId = '").append(userId).append("') ");
        statusSubQuery.append("OR sb_publication_publi.pubUpdaterId = '").append(userId)
            .append("')");
      } else {
        statusSubQuery.append(" AND (");
        if (coWritingEnable && draftVisibleWithCoWriting) {
          statusSubQuery
              .append("sb_publication_publi.pubStatus IN ('Valid','ToValidate','Draft') ");
        } else {
          if (profile.equals("publisher")) {
            checkVisibility = true;
          }
          statusSubQuery.append(
              "sb_publication_publi.pubStatus IN ('Valid','ToValidate') OR (sb_publication_publi" +
                  ".pubStatus = 'Draft' AND sb_publication_publi.pubUpdaterId = '").
              append(userId).append("') ");
        }
        statusSubQuery.append("OR sb_publication_publi.pubUpdaterId = '").append(userId)
            .append("')");
      }

      Map<String, Integer> numbers = publicationService
          .getDistributionTree(nodePK.getInstanceId(), statusSubQuery.toString(), checkVisibility);

      // set right number of publications in basket
      NodePK trashPk = new NodePK(NodePK.BIN_NODE_ID, nodePK.getInstanceId());
      int nbPubsInTrash = getPublicationsInBasket(trashPk, profile, userId).size();
      numbers.put(NodePK.BIN_NODE_ID, nbPubsInTrash);

      decorateWithNumberOfPublications(allowedTree, numbers);
    }

    return allowedTree;
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
      if (SilverpeasRole.admin.isInRole(userProfile)) {// Admin can see all Publis in the basket.
        currentUserId = null;
      }
      Collection<PublicationDetail> pubDetails =
          publicationService.getDetailsByFatherPK(pk, null, false, currentUserId);
      return pubDetails;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationsInBasket()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CONTENU_DE_LA_CORBEILLE", e);
    }
  }

  /**
   * Subscriptions - get the subscription list of the current user
   * @param userId
   * @param componentId
   * @return a Path Collection - it's a Collection of NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<Collection<NodeDetail>> getSubscriptionList(String userId, String componentId) {
    try {
      Collection<Subscription> list = getSubscribeService()
          .getBySubscriberAndComponent(UserSubscriptionSubscriber.from(userId), componentId);
      Collection<Collection<NodeDetail>> detailedList = new ArrayList<Collection<NodeDetail>>();
      // For each favorite, get the path from root to favorite
      for (Subscription subscription : list) {
        Collection<NodeDetail> path =
            nodeService.getPath((NodePK) subscription.getResource().getPK());
        detailedList.add(path);
      }
      return detailedList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getSubscriptionList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
    }
  }

  /**
   * Subscriptions - remove a subscription to the subscription list of the current user
   * @param topicPK the subscribe topic Id to remove
   * @param userId
   * @since 1.0
   */
  @Override
  public void removeSubscriptionToCurrentUser(NodePK topicPK, String userId) {
    try {
      getSubscribeService().unsubscribe(new NodeSubscription(userId, topicPK));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.removeSubscriptionToCurrentUser()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_ABONNEMENT", e);
    }
  }

  /**
   * Subscriptions - remove all subscriptions from topic
   * @param topicPKsToDelete the subscription topic Ids to remove
   * @since 1.0
   */
  private void removeSubscriptionsByTopic(Collection<NodePK> topicPKsToDelete) {
    try {
      Collection<SubscriptionResource> subscriptionResourcesToDelete =
          new ArrayList<SubscriptionResource>();
      for (NodePK topicPK : topicPKsToDelete) {
        subscriptionResourcesToDelete.add(NodeSubscriptionResource.from(topicPK));
      }
      getSubscribeService().unsubscribeByResources(subscriptionResourcesToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.removeSubscriptionsByTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_ABONNEMENTS", e);
    }
  }

  /**
   * Subscriptions - add a subscription
   * @param topicPK the subscription topic Id to add
   * @param userId the subscription userId
   * @since 1.0
   */
  @Override
  public void addSubscription(NodePK topicPK, String userId) {
    getSubscribeService().subscribe(new NodeSubscription(userId, topicPK));
  }

  /**
   * @param topicPK
   * @param userId
   * @return true if this topic does not exists in user subscriptions and can be added to them.
   */
  @Override
  public boolean checkSubscription(NodePK topicPK, String userId) {
    return !getSubscribeService().existsSubscription(new NodeSubscription(userId, topicPK));
  }

  /**
   * ***********************************************************************************
   * Interface - Gestion des publications
   * **********************************************************************************
   */
  private List<KmeliaPublication> pubDetails2userPubs(Collection<PublicationDetail> pubDetails) {
    List<KmeliaPublication> publications = new ArrayList<KmeliaPublication>();
    int i = -1;
    for (PublicationDetail publicationDetail : pubDetails) {
      publications.add(KmeliaPublication.aKmeliaPublicationFromDetail(publicationDetail, i++));
    }
    return publications;
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationDetail()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
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
    Collection<NodePK> fatherPKs = null;
    try {
      // get all nodePK whick contains this publication
      fatherPKs = publicationService.getAllFatherPK(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPathList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    try {
      List<Collection<NodeDetail>> pathList = new ArrayList<Collection<NodeDetail>>();
      if (fatherPKs != null) {
        // For each topic, get the path to it
        for (NodePK pk : fatherPKs) {
          Collection<NodeDetail> path = nodeService.getAnotherPath(pk);
          // add this path
          pathList.add(path);
        }
      }
      return pathList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPathList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
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
    PdcClassification predefinedClassification = pdcClassificationService
        .findAPreDefinedClassification(fatherPK.getId(), fatherPK.getInstanceId());
    return createPublicationIntoTopic(pubDetail, fatherPK, predefinedClassification);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK,
      PdcClassification classification) {
    String pubId = null;
    try {
      pubId = createPublicationIntoTopicWithoutNotifications(pubDetail, fatherPK, classification);

      // creates todos for publishers
      createTodosForPublication(pubDetail, true);

      // alert supervisors
      sendAlertToSupervisors(fatherPK, pubDetail);

      // alert subscribers
      sendSubscriptionsNotification(pubDetail, NotifAction.CREATE, false);

    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.createPublicationIntoTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    }
    return pubId;
  }

  private String createPublicationIntoTopicWithoutNotifications(PublicationDetail pubDetail,
      NodePK fatherPK, PdcClassification classification) {
    PublicationPK pubPK = null;
    try {
      pubDetail = changePublicationStatusOnCreation(pubDetail, fatherPK);
      // create the publication
      pubPK = publicationService.createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());
      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
      // add this publication to the current topic
      addPublicationToTopicWithoutNotifications(pubPK, fatherPK, true);
      // classify the publication on the PdC if its classification is defined
      // subscribers are notified later (only if publication is valid)
      classification.classifyContent(pubDetail, false);

      createdIntoRequestContext(pubDetail);

    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.createPublicationIntoTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    }
    return pubPK.getId();
  }

  private String getProfile(String userId, NodePK nodePK) {
    String profile;
    OrganizationController orgCtrl = getOrganisationController();
    if (isRightsOnTopicsEnabled(nodePK.getInstanceId())) {
      NodeDetail topic = nodeService.getHeader(nodePK);
      if (topic.haveRights()) {
        profile = KmeliaHelper.getProfile(orgCtrl
            .getUserProfiles(userId, nodePK.getInstanceId(), topic.getRightsDependsOn(),
                ObjectType.NODE));
      } else {
        profile = KmeliaHelper.getProfile(orgCtrl.getUserProfiles(userId, nodePK.getInstanceId()));
      }
    } else {
      profile = KmeliaHelper.getProfile(orgCtrl.getUserProfiles(userId, nodePK.getInstanceId()));
    }
    return profile;
  }

  private String getProfileOnPublication(String userId, PublicationPK pubPK) {
    List<NodePK> fathers = (List<NodePK>) getPublicationFathers(pubPK);
    NodePK nodePK = new NodePK("unknown", pubPK.getInstanceId());
    if (fathers != null && !fathers.isEmpty()) {
      nodePK = fathers.get(0);
    }
    return getProfile(userId, nodePK);
  }

  private PublicationDetail changePublicationStatusOnCreation(PublicationDetail pubDetail,
      NodePK nodePK) {
    String status = pubDetail.getStatus();
    if (!isDefined(status)) {
      status = PublicationDetail.TO_VALIDATE;

      boolean draftModeUsed = isDraftModeUsed(pubDetail.getPK().getInstanceId());

      if (draftModeUsed) {
        status = PublicationDetail.DRAFT;
      } else {
        String profile = getProfile(pubDetail.getCreatorId(), nodePK);
        if (SilverpeasRole.publisher.isInRole(profile) || SilverpeasRole.admin.isInRole(profile)) {
          status = PublicationDetail.VALID;
        }
      }
    }
    pubDetail.setStatus(status);
    KmeliaHelper.checkIndex(pubDetail);
    return pubDetail;
  }

  private boolean changePublicationStatusOnMove(PublicationDetail pub, NodePK to) {
    String oldStatus = pub.getStatus();
    String status = pub.getStatus();
    if (!status.equals(PublicationDetail.DRAFT)) {
      status = PublicationDetail.TO_VALIDATE;
      String profile = getProfile(pub.getUpdaterId(), to);
      if (SilverpeasRole.publisher.isInRole(profile) || SilverpeasRole.admin.isInRole(profile)) {
        status = PublicationDetail.VALID;
      }
    }
    pub.setStatus(status);
    KmeliaHelper.checkIndex(pub);
    return !oldStatus.equals(status);
  }

  /**
   * determine new publication's status according to actual status and current user's profile
   * @param pubDetail
   * @return true if status has changed, false otherwise
   */
  private boolean changePublicationStatusOnUpdate(PublicationDetail pubDetail) {
    String oldStatus = pubDetail.getStatus();
    String newStatus = oldStatus;

    List<NodePK> fathers = (List<NodePK>) getPublicationFathers(pubDetail.getPK());

    if (pubDetail.isStatusMustBeChecked()) {
      if (!pubDetail.isDraft() && !pubDetail.isClone()) {
        NodePK nodePK = new NodePK("unknown", pubDetail.getPK().getInstanceId());
        if (fathers != null && !fathers.isEmpty()) {
          nodePK = fathers.get(0);
        }
        String profile = getProfile(pubDetail.getUpdaterId(), nodePK);
        if (SilverpeasRole.writer.isInRole(profile)) {
          newStatus = PublicationDetail.TO_VALIDATE;
        } else if (pubDetail.isRefused() && (SilverpeasRole.admin.isInRole(profile) ||
            SilverpeasRole.publisher.isInRole(profile))) {
          newStatus = PublicationDetail.VALID;
        }
        pubDetail.setStatus(newStatus);
      }
    }

    KmeliaHelper.checkIndex(pubDetail);

    if (fathers == null || fathers.isEmpty() || (fathers.size() == 1 && fathers.get(0).isTrash())) {
      // la publication est dans la corbeille
      pubDetail.setIndexOperation(IndexManager.NONE);
    }
    return !oldStatus.equalsIgnoreCase(newStatus);
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
  public void updatePublication(PublicationDetail pubDetail, boolean forceUpdateDate) {
    updatePublication(pubDetail, KmeliaHelper.PUBLICATION_HEADER, forceUpdateDate);
  }

  private void updatePublication(PublicationDetail pubDetail, int updateScope,
      boolean forceUpdateDate) {
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
          if (statusChanged) {
            // creates todos for publishers
            this.createTodosForPublication(pubDetail, false);
          } else {
            performValidatorChanges(old, pubDetail);
          }

          updateSilverContentVisibility(pubDetail);

          // la publication a été modifié par un superviseur
          // le créateur de la publi doit être averti
          String profile = KmeliaHelper.getProfile(getOrganisationController()
              .getUserProfiles(pubDetail.getUpdaterId(), pubDetail.getPK().getInstanceId()));
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
      }

      // Sending a subscription notification if the publication updated comes not from the
      // basket, has not been created or already updated from the same request
      if (!isPublicationInBasket &&
          !hasPublicationBeenCreatedFromRequestContext(pubDetail) &&
          !hasPublicationBeenUpdatedFromRequestContext(pubDetail)) {
        sendSubscriptionsNotification(pubDetail, NotifAction.UPDATE, false);
      }


      updatedIntoRequestContext(pubDetail);

    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.updatePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
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
      List<String> toRemoveToDo = new ArrayList<String>(oldValidatorIds);
      List<String> toAlert = new ArrayList<String>(newValidatorIds);
      toRemoveToDo.removeAll(newValidatorIds);
      toAlert.removeAll(oldValidatorIds);

      // Performing the actions.
      removeTodoForPublication(currentPublication.getPK(), toRemoveToDo);
      addTodoAndSendNotificationToValidators(currentPublication, toAlert);
    }
  }

  private boolean isVisibilityPeriodUpdated(PublicationDetail pubDetail, PublicationDetail old) {
    boolean beginVisibilityPeriodUpdated =
        ((pubDetail.getBeginDate() != null && old.getBeginDate() == null) || (pubDetail.
            getBeginDate() == null && old.getBeginDate() != null) ||
            (pubDetail.getBeginDate() != null && old.getBeginDate() != null &&
                !pubDetail.getBeginDate().equals(old.
                    getBeginDate())));
    boolean endVisibilityPeriodUpdated =
        ((pubDetail.getEndDate() != null && old.getEndDate() == null) ||
            (pubDetail.getEndDate() == null && old.getEndDate() != null) ||
            (pubDetail.getEndDate() != null && old.
                getEndDate() != null && !pubDetail.getEndDate().equals(old.getEndDate())));
    return beginVisibilityPeriodUpdated || endVisibilityPeriodUpdated;
  }

  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void movePublication(@SourcePK PublicationPK pubPK, @TargetPK NodePK to, String userId) {
    PublicationDetail pub = getPublicationDetail(pubPK);
    if (pub != null) {
      if (pubPK.getInstanceId().equals(to.getInstanceId())) {
        movePublicationInSameApplication(pub, to, userId);
      } else {
        movePublicationInAnotherApplication(pub, to, userId);
      }
    }
  }

  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void movePublicationInSameApplication(@SourcePK PublicationPK pubPK, @TargetPK NodePK from,
      NodePK to, String userId) {
    PublicationDetail pub = getPublicationDetail(pubPK);

    // check if user can cut publication from source folder
    String profile = getUserTopicProfile(from, userId);
    boolean cutAllowed = KmeliaPublicationHelper
        .isCanBeCut(from.getComponentName(), userId, profile, pub.getCreator());

    // check if user can paste publication into target folder
    String profileInTarget = getUserTopicProfile(to, userId);
    boolean pasteAllowed = KmeliaPublicationHelper.isCreationAllowed(to, profileInTarget);

    if (cutAllowed && pasteAllowed) {
      movePublicationInSameApplication(pub, to, userId);
    }
  }

  private void movePublicationInSameApplication(PublicationDetail pub, NodePK to, String userId) {
    if (to.isTrash()) {
      sendPublicationToBasket(pub.getPK());
    } else {
      // update parent
      publicationService.removeAllFather(pub.getPK());
      publicationService.addFather(pub.getPK(), to);
      processPublicationAfterMove(pub, to, userId);
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

  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void movePublicationInAnotherApplication(@SourcePK PublicationDetail pub,
      @TargetPK NodePK to, String userId) {

    try {
      ForeignPK fromForeignPK = new ForeignPK(pub.getPK());
      String fromComponentId = pub.getInstanceId();
      ForeignPK toPubliForeignPK = new ForeignPK(pub.getId(), to);

      // remove index relative to publication
      unIndexExternalElementsOfPublication(pub.getPK());

      // move thumbnail
      ThumbnailController.moveThumbnail(fromForeignPK, toPubliForeignPK);

      try {
        // move additional files
        List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
            listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.image, null);
        documents.addAll(AttachmentServiceProvider.getAttachmentService().
            listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.wysiwyg, null));
        for (SimpleDocument doc : documents) {
          AttachmentServiceProvider.getAttachmentService().moveDocument(doc, toPubliForeignPK);
        }
      } catch (org.silverpeas.core.contribution.attachment.AttachmentException e) {
        SilverLogger.getLogger(this).error("Cannot move attachments of publication {0}",
            new String[] {pub.getPK().getId()}, e);
      }

      // change images path in wysiwyg
      WysiwygController.wysiwygPlaceHaveChanged(fromForeignPK.getInstanceId(), pub.getPK().getId(),
          to.getInstanceId(), pub.getPK().getId());

      // move regular files
      List<SimpleDocument> docs = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKeyAndType(fromForeignPK, DocumentType.attachment, null);
      for (SimpleDocument doc : docs) {
        AttachmentServiceProvider.getAttachmentService().moveDocument(doc, toPubliForeignPK);
      }

      // move form content
      String infoId = pub.getInfoId();
      if (infoId != null && !"0".equals(infoId)) {
        // register content to component
        PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
        GenericRecordSet toRecordSet = templateManager
            .addDynamicPublicationTemplate(to.getInstanceId() + ":" + pub.getInfoId(),
                pub.getInfoId() + ".xml");
        RecordTemplate toRecordTemplate = toRecordSet.getRecordTemplate();

        // get xmlContent to move
        PublicationTemplate pubTemplateFrom = templateManager.
            getPublicationTemplate(fromComponentId + ":" + pub.getInfoId());

        RecordSet set = pubTemplateFrom.getRecordSet();
        set.move(fromForeignPK, toPubliForeignPK, toRecordTemplate);
      }

      // move comments
      getCommentService()
          .moveComments(PublicationDetail.getResourceType(), fromForeignPK, toPubliForeignPK);

      // move pdc positions
      // Careful! positions must be moved according to taxonomy restrictions of target application
      int fromSilverObjectId = getSilverObjectId(pub.getPK());
      // get positions of cutted publication
      List<ClassifyPosition> positions = pdcManager.
          getPositions(fromSilverObjectId, fromComponentId);

      // delete taxonomy data relative to moved publication
      deleteSilverContent(pub.getPK());

      // move statistics
      statisticService.moveStat(toPubliForeignPK, 1, "Publication");

      // move publication itself
      publicationService.movePublication(pub.getPK(), to, false);
      pub.getPK().setComponentName(to.getInstanceId());

      processPublicationAfterMove(pub, to, userId);

      // index moved publication
      if (KmeliaHelper.isIndexable(pub)) {
        indexPublication(pub);
      }

      // reference pasted publication on taxonomy service
      int toSilverObjectId = getSilverObjectId(pub.getPK());
      // add original positions to pasted publication
      pdcManager.addPositions(positions, toSilverObjectId, to.getInstanceId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.movePublication()", ERROR,
          "kmelia.EX_CANT_MOVE_PUBLICATION_INTO_ANOTHER_APP", e);
    }
  }

  private void processPublicationAfterMove(PublicationDetail pub, NodePK to, String userId) {
    // update last modifier
    pub.setUpdaterId(userId);
    // status must be checked according to topic rights and last modifier (current user)
    boolean statusChanged = changePublicationStatusOnMove(pub, to);
    // update publication
    publicationService.setDetail(pub, statusChanged);

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
    // check if related contribution is managed by kmelia
    if (pubPK != null && StringUtil.isDefined(pubPK.getInstanceId()) && (pubPK.getInstanceId().
        startsWith("kmelia") || pubPK.getInstanceId().startsWith("toolbox") ||
        pubPK.getInstanceId().startsWith("kmax"))) {

      PublicationDetail pubDetail = null;
      boolean isPublicationInBasketBeforeUpdate = false;
      try {
        isPublicationInBasketBeforeUpdate = isPublicationInBasket(pubPK);
        pubDetail = getPublicationDetail(pubPK);
      } catch (Exception e) {
        // publication no longer exists do not throw exception because this method is called by JMS
        // layer
        // if exception is throw, JMS will attempt to execute it again and again...
        SilverLogger.getLogger(this).error("Impossible to get the publication {0}", pubPK.getId());
      }

      // The treatment is stopped if publication is not found or if publication doesn't correspond
      // with parameter of given publication pk. The second condition could happen, for now,
      // with applications dealing with wysiwyg without using publication for their storage
      // (infoletter for example).
      if (pubDetail == null || (StringUtil.isDefined(pubPK.getInstanceId()) && !pubDetail.
          getInstanceId().equals(pubPK.getInstanceId()))) {
        return;
      }

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
        // check if user have sufficient rights to update a publication
        String profile = getProfileOnPublication(userId, pubDetail.getPK());
        if ("supervisor".equals(profile) || SilverpeasRole.publisher.isInRole(profile) ||
            SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.writer.isInRole(profile)) {
          updatePublication(pubDetail, KmeliaHelper.PUBLICATION_CONTENT, false);
        } else {
          SilverLogger.getLogger(this).warn("User {0} not allowed to update publication {1}",
              userId, pubDetail.getPK().getId());
        }
      }

      if (KmeliaHelper.isIndexable(pubDetail) && !isPublicationInBasketBeforeUpdate) {
        publicationService.createIndex(pubDetail);
      }

      // index all attached files to taking into account visibility period
      indexExternalElementsOfPublication(pubDetail);
    }
  }

  private boolean isClone(PublicationDetail publication) {
    return isDefined(publication.getCloneId()) && !"-1".equals(publication.getCloneId()) &&
        !isDefined(publication.getCloneStatus());
  }

  /**
   * HEAD Delete a publication If this publication is in the basket or in the DZ, it's deleted from
   * the database Else it only send to the basket.
   * @param pubPK the id of the publication to delete
   * @see TopicDetail
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deletePublication(PublicationPK pubPK) {
    // if the publication is in the basket or in the DZ
    // this publication is deleted from the database
    try {
      // remove form content
      removeXMLContentOfPublication(pubPK);
      // delete all reading controls associated to this publication
      deleteAllReadingControlsByPublication(pubPK);
      // delete all links
      publicationService.removeAllFather(pubPK);
      // delete the publication
      publicationService.removePublication(pubPK);
      // delete reference to contentManager
      deleteSilverContent(pubPK);

      removeExternalElementsOfPublications(pubPK);

    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.deletePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION", e);
    }

  }

  /**
   * Send the publication in the basket topic
   * @param pubPK the id of the publication
   * @param kmaxMode
   * @see TopicDetail
   * @since 1.0
   */
  @Override
  public void sendPublicationToBasket(PublicationPK pubPK, boolean kmaxMode) {
    try {
      PublicationDetail pubDetail = publicationService.getDetail(pubPK);
      // remove coordinates for Kmax
      if (kmaxMode) {
        CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK.getSpaceId(), pubPK.
            getComponentName());

        Collection<NodePK> fatherPKs = publicationService.getAllFatherPK(pubPK);
        // delete publication coordinates
        Iterator<NodePK> it = fatherPKs.iterator();
        List<String> coordinates = new ArrayList<String>();
        while (it.hasNext()) {
          String coordinateId = (it.next()).getId();
          coordinates.add(coordinateId);
        }
        if (coordinates.size() > 0) {
          coordinatesService.deleteCoordinates(coordinatePK, (ArrayList<String>) coordinates);
        }
      }

      // remove all links between this publication and topics
      publicationService.removeAllFather(pubPK);
      // add link between this publication and the basket topic
      publicationService.addFather(pubPK, new NodePK("1", pubPK));

      // remove all the todos attached to the publication
      removeAllTodosForPublication(pubPK);

      // publication is no more accessible
      updateSilverContentVisibility(pubPK, false);

      unIndexExternalElementsOfPublication(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.sendPublicationToBasket()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DENVOYER_LA_PUBLICATION_A_LA_CORBEILLE", e);
    }
  }

  @Override
  public void sendPublicationToBasket(PublicationPK pubPK) {
    sendPublicationToBasket(pubPK, KmeliaHelper.isKmax(pubPK.getInstanceId()));
  }

  /**
   * Add a publication to a topic and send email alerts to topic subscribers
   * @param pubPK the id of the publication
   * @param fatherPK the id of the topic
   * @param isACreation
   */
  @Override
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
    if (!isACreation) {
      try {
        Collection<NodePK> fathers = publicationService.getAllFatherPK(pubPK);
        if (isPublicationInBasket(pubPK, fathers)) {
          publicationService.removeFather(pubPK, new NodePK(NodePK.BIN_NODE_ID, fatherPK));
          if (PublicationDetail.VALID.equalsIgnoreCase(pubDetail.getStatus())) {
            // index publication
            publicationService.createIndex(pubPK);
            // index external elements
            indexExternalElementsOfPublication(pubDetail);
            // publication is accessible again
            updateSilverContentVisibility(pubDetail);
          } else if (PublicationDetail.TO_VALIDATE.equalsIgnoreCase(pubDetail.getStatus())) {
            // create validation todos for publishers
            createTodosForPublication(pubDetail, true);
          }
        } else if (fathers.isEmpty()) {
          // The publi have got no father
          // change the end date to make this publi visible
          pubDetail.setEndDate(null);
          publicationService.setDetail(pubDetail);
          // publication is accessible again
          updateSilverContentVisibility(pubDetail);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("DefaultKmeliaService.addPublicationToTopic()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", e);
      }
    }

    try {
      publicationService.addFather(pubPK, fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.addPublicationToTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", e);
    }
  }

  private boolean isPublicationInBasket(PublicationPK pubPK) {
    return isPublicationInBasket(pubPK, null);
  }

  private boolean isPublicationInBasket(PublicationPK pubPK, Collection<NodePK> fathers) {
    if (fathers == null) {
      fathers = publicationService.getAllFatherPK(pubPK);
    }
    if (fathers.size() == 1) {
      Iterator<NodePK> iterator = fathers.iterator();
      if (iterator.hasNext()) {
        NodePK pk = iterator.next();
        if (pk.isTrash()) {
          return true;
        }
      }
    }
    return false;
  }

  private NodePK sendSubscriptionsNotification(PublicationDetail pubDetail, NotifAction action,
      final boolean sendOnlyToAliases) {
    NodePK oneFather = null;
    // We alert subscribers only if publication is Valid
    if (!pubDetail.haveGotClone() && pubDetail.isValid() && pubDetail.isVisible()) {
      // Topic subscriptions
      Collection<NodePK> fathers = getPublicationFathers(pubDetail.getPK());
      if (!sendOnlyToAliases) {
        for (NodePK father : fathers) {
          oneFather = father;
          sendSubscriptionsNotification(father, pubDetail, action);
        }
      }

      // Subscriptions related to aliases
      List<Alias> aliases = (List<Alias>) getAlias(pubDetail.getPK());
      for (Alias alias : aliases) {
        // Transform the current alias to a NodePK (even if Alias is extending NodePK) in the aim
        // to execute the equals method of NodePK
        NodePK aliasNodePk = new NodePK(alias.getId(), alias.getInstanceId());
        if ((sendOnlyToAliases && alias.getDate() != null) || !fathers.contains(aliasNodePk)) {
          // Perform subscription notification sendings when the alias is not the one of the
          // original publication
          pubDetail.setAlias(true);
          sendSubscriptionsNotification(alias, pubDetail, action);
        }
      }

      // PDC subscriptions
      try {
        int silverObjectId = getSilverObjectId(pubDetail.getPK());
        List<ClassifyPosition> positions =
            pdcManager.getPositions(silverObjectId, pubDetail.getPK().
                getInstanceId());
        if (positions != null) {
          for (ClassifyPosition position : positions) {
            pdcSubscriptionManager
                .checkSubscriptions(position.getValues(), pubDetail.getPK().getInstanceId(),
                    silverObjectId);
          }
        }
      } catch (PdcException e) {
        SilverLogger.getLogger(this).error("PdC subscriptions notification failure for publication {0}",
            new String[] {pubDetail.getPK().getId()}, e);
      }
    }
    return oneFather;
  }

  private void sendSubscriptionsNotification(NodePK fatherPK, PublicationDetail pubDetail,
      NotifAction action) {

    // Send email alerts
    try {

      // Building and sending the notification
      UserNotificationHelper.buildAndSend(
          new KmeliaSubscriptionPublicationUserNotification(fatherPK, pubDetail, action));

    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Subscriber notification failure about publication {0}",
          new String[]{pubDetail.getPK().getId()}, e);
    }
  }

  /**
   * Delete a path between publication and topic
   * @param pubPK
   * @param fatherPK
   */
  @Override
  public void deletePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK) {
    try {
      Collection<NodePK> pubFathers = publicationService.getAllFatherPK(pubPK);
      if (pubFathers.size() >= 2) {
        publicationService.removeFather(pubPK, fatherPK);
      } else {
        // la publication n'a qu'un seul emplacement
        // elle est donc placée dans la corbeille du créateur
        sendPublicationToBasket(pubPK);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.deletePublicationFromTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION_DE_CE_THEME", e);
    }
  }

  @Override
  public void deletePublicationFromAllTopics(PublicationPK pubPK) {
    try {
      publicationService.removeAllFather(pubPK);

      // la publication n'a qu'un seul emplacement
      // elle est donc placée dans la corbeille du créateur
      sendPublicationToBasket(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.deletePublicationFromAllTopics()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION_DE_CE_THEME", e);
    }
  }

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    try {
      publicationService.addLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.addInfoLinks()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    try {
      publicationService.deleteInfoLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.deleteInfoLinks()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
  }

  @Override
  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    CompletePublication completePublication = null;

    try {
      completePublication = publicationService.getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getCompletePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
    return completePublication;
  }

  @Override
  public KmeliaPublication getPublication(PublicationPK pubPK) {
    return KmeliaPublication.aKmeliaPublicationWithPk(pubPK);
  }

  @Override
  public TopicDetail getPublicationFather(PublicationPK pubPK, boolean isTreeStructureUsed,
      String userId, boolean isRightsOnTopicsUsed) {
    // fetch one of the publication fathers
    NodePK fatherPK =
        getPublicationFatherPK(pubPK, isTreeStructureUsed, userId, isRightsOnTopicsUsed);
    String profile = KmeliaHelper
        .getProfile(getOrganisationController().getUserProfiles(userId, pubPK.getInstanceId()));
    TopicDetail fatherDetail =
        goTo(fatherPK, userId, isTreeStructureUsed, profile, isRightsOnTopicsUsed);
    return fatherDetail;
  }

  @Override
  public NodePK getPublicationFatherPK(PublicationPK pubPK, boolean isTreeStructureUsed,
      String userId, boolean isRightsOnTopicsUsed) {
    // fetch one of the publication fathers
    Collection<NodePK> fathers = getPublicationFathers(pubPK);
    NodePK fatherPK = new NodePK("0", pubPK); // By default --> Root
    if (fathers != null) {
      Iterator<NodePK> it = fathers.iterator();
      if (!isRightsOnTopicsUsed) {
        if (it.hasNext()) {
          fatherPK = it.next();
        }
      } else {
        NodeDetail allowedFather = null;
        while (allowedFather == null && it.hasNext()) {
          fatherPK = it.next();
          NodeDetail father = getNodeHeader(fatherPK);
          if (!father.haveRights() || getOrganisationController()
              .isObjectAvailable(father.getRightsDependsOn(), ObjectType.NODE,
                  fatherPK.getInstanceId(), userId)) {
            allowedFather = father;
          }
        }
        if (allowedFather != null) {
          fatherPK = allowedFather.getNodePK();
        }
      }
    }
    return fatherPK;
  }

  @Override
  public Collection<NodePK> getPublicationFathers(PublicationPK pubPK) {
    try {
      Collection<NodePK> fathers = publicationService.getAllFatherPK(pubPK);
      if (CollectionUtil.isEmpty(fathers)) {
        // This publication have got no father !
        // Check if it's a clone (a clone have got no father ever)
        boolean alwaysVisibleModeActivated = StringUtil.getBooleanValue(getOrganisationController().
            getComponentParameterValue(pubPK.getInstanceId(), "publicationAlwaysVisible"));
        if (alwaysVisibleModeActivated) {
          PublicationDetail publi = publicationService.getDetail(pubPK);
          if (publi != null) {
            boolean isClone = isClone(publi);
            if (isClone) {
              // This publication is a clone
              // Get fathers from main publication
              fathers = publicationService.getAllFatherPK(publi.getClonePK());
            }
          }
        }
      }
      return fathers;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationFathers()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_UN_PERE_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * gets a list of PublicationDetail corresponding to the links parameter
   * @param links list of publication (componentID + publicationId)
   * @return a list of PublicationDetail
   */
  @Override
  public Collection<PublicationDetail> getPublicationDetails(List<ForeignPK> links) {
    Collection<PublicationDetail> publications = null;
    List<PublicationPK> publicationPKs = new ArrayList<PublicationPK>();

    for (ForeignPK link : links) {
      PublicationPK pubPK = new PublicationPK(link.getId(), link.getInstanceId());
      publicationPKs.add(pubPK);
    }
    try {
      publications = publicationService.getPublications(publicationPKs);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationDetails()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_PUBLICATIONS", e);
    }
    return publications;
  }

  /**
   * gets a list of authorized publications
   * @param links list of publication defined by his id and component id
   * @param userId identifier User. allow to check if the publication is accessible for current
   * user
   * @param isRightsOnTopicsUsed indicates if the right must be checked
   * @return a collection of Kmelia publication
   * @since 1.0
   */
  @Override
  public Collection<KmeliaPublication> getPublications(List<ForeignPK> links, String userId,
      boolean isRightsOnTopicsUsed) {
    // initialization of the publications list
    List<ForeignPK> allowedPublicationIds = new ArrayList<ForeignPK>(links);
    if (isRightsOnTopicsUsed) {
      KmeliaAuthorization security = new KmeliaAuthorization();
      allowedPublicationIds.clear();

      // check if the publication is authorized for current user
      for (ForeignPK link : links) {
        if (security.isObjectAvailable(link.getInstanceId(), userId, link.getId(),
            KmeliaAuthorization.PUBLICATION_TYPE)) {
          allowedPublicationIds.add(link);
        }
      }
    }
    Collection<PublicationDetail> publications = getPublicationDetails(allowedPublicationIds);
    return pubDetails2userPubs(publications);
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
    KmeliaAuthorization security = new KmeliaAuthorization();
    List<ForeignPK> allLinkIds = publication.getCompleteDetail().getLinkList();
    List<KmeliaPublication> authorizedLinks = new ArrayList<KmeliaPublication>(allLinkIds.size());
    for (ForeignPK linkId : allLinkIds) {
      if (security.isAccessAuthorized(linkId.getInstanceId(), userId, linkId.getId())) {
        PublicationPK pubPk = new PublicationPK(linkId.getId(), linkId.getInstanceId());
        authorizedLinks.add(KmeliaPublication.aKmeliaPublicationWithPk(pubPk));
      }
    }
    return authorizedLinks;
  }

  /**
   * Gets all the publications linked with the specified one.
   * @param publication the publication from which linked publications are get.
   * @return a list of Kmelia publications.
   * @ if an error occurs while communicating with the remote business service.
   */
  @Override
  public List<KmeliaPublication> getLinkedPublications(KmeliaPublication publication) {
    List<ForeignPK> allLinkIds = publication.getCompleteDetail().getLinkList();
    List<KmeliaPublication> linkedPublications =
        new ArrayList<KmeliaPublication>(allLinkIds.size());
    for (ForeignPK linkId : allLinkIds) {
      PublicationPK pubPk = new PublicationPK(linkId.getId(), linkId.getInstanceId());
      linkedPublications.add(KmeliaPublication.aKmeliaPublicationWithPk(pubPk));
    }
    return linkedPublications;
  }

  @Override
  public List<KmeliaPublication> getPublicationsToValidate(String componentId, String userId) {
    Collection<PublicationDetail> publications = new ArrayList<PublicationDetail>();
    PublicationPK pubPK = new PublicationPK("useless", componentId);
    try {
      Collection<PublicationDetail> temp =
          publicationService.getPublicationsByStatus(PublicationDetail.TO_VALIDATE, pubPK);
      // only publications which must be validated by current user must be returned
      for (PublicationDetail publi : temp) {
        boolean isClone = publi.isValidationRequired() && !"-1".equals(publi.getCloneId());
        if (isClone) {
          if (isUserCanValidatePublication(publi.getPK(), userId)) {
            // publication to validate is a clone, get original one
            try {
              PublicationDetail original = getPublicationDetail(new PublicationPK(publi.
                  getCloneId(), publi.getPK()));
              publications.add(original);
            } catch (Exception e) {
              // inconsistency in database! Original publication does not exist
              SilverLogger.getLogger(this).warn("Original publication {0} of clone {1} not found",
                  publi.getId(), publi.getCloneId());
            }
          }
        } else {
          if (isUserCanValidatePublication(publi.getPK(), userId)) {
            publications.add(publi);
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getPublicationsToValidate()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_PUBLICATIONS_A_VALIDER", e);
    }
    return pubDetails2userPubs(publications);
  }

  private void sendValidationNotification(final NodePK fatherPK, final PublicationDetail pubDetail,
      final String refusalMotive, final String userIdWhoRefuse) {

    try {

      UserNotificationHelper.buildAndSend(
          new KmeliaValidationPublicationUserNotification(fatherPK, pubDetail, refusalMotive,
              userIdWhoRefuse));

    } catch (Exception e) {
      SilverLogger.getLogger(this).error("User notification failure about publication {0}",
          new String[]{pubDetail.getPK().getId()}, e);
    }
  }

  private void sendAlertToSupervisors(final NodePK fatherPK, final PublicationDetail pubDetail) {
    if (pubDetail.isValid()) {
      try {
        UserNotificationHelper
            .buildAndSend(new KmeliaSupervisorPublicationUserNotification(fatherPK, pubDetail));
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Supervisors notification failure about publication {0}",
            new String[]{pubDetail.getPK().getId()}, e);
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
        SilverLogger.getLogger(this).error("fatherId = {0}, pubPK = {1}",
            new String[]{fatherPK.getId(), pubDetail.getPK().toString()}, e);
      }
    }
  }

  private int getValidationType(String instanceId) {
    String sParam = getOrganisationController()
        .getComponentParameterValue(instanceId, InstanceParameters.validation);
    if (isDefined(sParam)) {
      return Integer.parseInt(sParam);
    }
    return KmeliaHelper.VALIDATION_CLASSIC;
  }

  private boolean isTargetedValidationEnabled(String componentId) {
    int value = getValidationType(componentId);
    return value == KmeliaHelper.VALIDATION_TARGET_N || value == KmeliaHelper.VALIDATION_TARGET_1;
  }

  private List<String> getValidatorIds(PublicationDetail publi) {
    List<String> allValidators = new ArrayList<String>();
    if (isDefined(publi.getTargetValidatorId())) {
      StringTokenizer tokenizer = new StringTokenizer(publi.getTargetValidatorId(), ",");
      while (tokenizer.hasMoreTokens()) {
        allValidators.add(tokenizer.nextToken());
      }
    }
    return allValidators;
  }

  @Override
  public List<String> getAllValidators(PublicationPK pubPK) {
    // get all users who have to validate
    List<String> allValidators = new ArrayList<String>();
    if (isTargetedValidationEnabled(pubPK.getInstanceId())) {
      PublicationDetail publi = publicationService.getDetail(pubPK);
      allValidators = getValidatorIds(publi);
    }
    if (allValidators.isEmpty()) {
      // It's not a targeted validation or it is but no validators has
      // been selected !
      List<String> roles = new ArrayList<String>(2);
      roles.add(SilverpeasRole.admin.name());
      roles.add(SilverpeasRole.publisher.name());

      if (KmeliaHelper.isKmax(pubPK.getInstanceId())) {
        allValidators.addAll(Arrays.asList(
            getOrganisationController().getUsersIdsByRoleNames(pubPK.getInstanceId(), roles)));
      } else {
        // get admin and publishers of all nodes where publication is
        List<NodePK> nodePKs = (List<NodePK>) getPublicationFathers(pubPK);
        NodePK nodePK;
        NodeDetail node;
        boolean oneNodeIsPublic = false;
        for (int n = 0; !oneNodeIsPublic && nodePKs != null && n < nodePKs.size(); n++) {
          nodePK = nodePKs.get(n);
          node = getNodeHeader(nodePK);
          if (node != null) {
            if (!node.haveRights()) {
              allValidators.addAll(Arrays.asList(getOrganisationController()
                  .getUsersIdsByRoleNames(pubPK.getInstanceId(), roles)));
              oneNodeIsPublic = true;
            } else {
              allValidators.addAll(Arrays.asList(getOrganisationController()
                  .getUsersIdsByRoleNames(pubPK.getInstanceId(),
                      Integer.toString(node.getRightsDependsOn()), ObjectType.NODE, roles)));
            }
          }
        }
      }
    }
    return allValidators;
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

  /**
   * @param pubPK
   * @param allValidators
   * @return
   * @
   */
  private boolean isValidationComplete(PublicationPK pubPK, List<String> allValidators) {
    List<ValidationStep> steps = publicationService.getValidationSteps(pubPK);

    // get users who have already validate
    List<String> stepUserIds = new ArrayList<String>();
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
    boolean validationComplete = false;
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
        int validationType = getValidationType(pubPK.getInstanceId());
        if (validationType == KmeliaHelper.VALIDATION_CLASSIC ||
            validationType == KmeliaHelper.VALIDATION_TARGET_1) {
          validationComplete = true;
        } else {
          if (validationType == KmeliaHelper.VALIDATION_TARGET_N) {
            // check that validators are well defined
            // If not, considering validation as classic one
            PublicationDetail publi = publicationService.getDetail(validatedPK);
            if (!isDefined(publi.getTargetValidatorId())) {
              validationComplete = true;
            }
          }
          if (!validationComplete) {
            // get all users who have to validate
            List<String> allValidators = getAllValidators(validatedPK);
            if (allValidators.size() == 1) {
              // special case : only once user is concerned by validation
              validationComplete = true;
            } else if (allValidators.size() > 1) {
              // remove todo for this user. His job is done !
              removeTodoForPublication(validatedPK, userId);
              if (validationOnClone) {
                removeTodoForPublication(pubPK, userId);
              }
              // save his decision

              ValidationStep validation =
                  new ValidationStep(validatedPK, userId, PublicationDetail.VALID);
              publicationService.addValidationStep(validation);
              // check if all validators have give their decision
              validationComplete = isValidationComplete(validatedPK, allValidators);
            }
          }
        }

      } else {

        // User has no more validation right
        int validationType = getValidationType(pubPK.getInstanceId());

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
              validationComplete = isValidationComplete(validatedPK, allValidators);
              if (validationComplete) {

                // taking the last effective validator for the state change.
                validationDate = new Date(0);
                for (ValidationStep validationStep : publicationService
                    .getValidationSteps(validatedPK)) {
                  final String validationStepUserId = validationStep.getUserId();
                  if (!validationStepUserId.equals(userId) &&
                      validationStep.getValidationDate().compareTo(validationDate) > 0) {
                    validationDate = validationStep.getValidationDate();
                    validatorUserId = validationStepUserId;
                  }
                }
              } else if (validationType == KmeliaHelper.VALIDATION_TARGET_N &&
                  StringUtil.isNotDefined(currentPubOrCloneDetail.getTargetValidatorId())) {
                // Case of fallback solution when no more validator is defined, all publishers
                // must validate (as collegiate method)
                alertPublicationOwnerThereIsNoMoreValidator = true;
              }
            }
        }

        if (alertPublicationOwnerThereIsNoMoreValidator) {
          Collection<NodePK> fatherPks = getPublicationFathers(currentPubDetail.getPK());
          if (!fatherPks.isEmpty()) {
            sendNoMoreValidatorNotification(fatherPks.iterator().next(), currentPubDetail);
          }
        }
      }

      if (validationComplete) {
        removeAllTodosForPublication(validatedPK);
        if (validationOnClone) {
          removeAllTodosForPublication(pubPK);
        }
        if (currentPubDetail.haveGotClone()) {
          currentPubDetail = mergeClone(currentPub, validatorUserId, validationDate);
        } else if (currentPubDetail.isValidationRequired()) {
          currentPubDetail.setValidatorId(validatorUserId);
          currentPubDetail.setValidateDate(validationDate);
          currentPubDetail.setStatus(PublicationDetail.VALID);
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
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.validatePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_VALIDER_LA_PUBLICATION", e);
    }
    return validationComplete;
  }

  private PublicationDetail getClone(PublicationDetail refPub) {
    PublicationDetail clone = new PublicationDetail();
    if (refPub.getAuthor() != null) {
      clone.setAuthor(refPub.getAuthor());
    }
    if (refPub.getBeginDate() != null) {
      clone.setBeginDate(new Date(refPub.getBeginDate().getTime()));
    }
    if (refPub.getBeginHour() != null) {
      clone.setBeginHour(refPub.getBeginHour());
    }
    if (refPub.getContent() != null) {
      clone.setContent(refPub.getContent());
    }
    clone.setCreationDate(new Date(refPub.getCreationDate().getTime()));
    clone.setCreatorId(refPub.getCreatorId());
    if (refPub.getDescription() != null) {
      clone.setDescription(refPub.getDescription());
    }
    if (refPub.getEndDate() != null) {
      clone.setEndDate(new Date(refPub.getEndDate().getTime()));
    }
    if (refPub.getEndHour() != null) {
      clone.setEndHour(refPub.getEndHour());
    }
    clone.setImportance(refPub.getImportance());
    if (refPub.getInfoId() != null) {
      clone.setInfoId(refPub.getInfoId());
    }
    if (refPub.getKeywords() != null) {
      clone.setKeywords(refPub.getKeywords());
    }
    if (refPub.getName() != null) {
      clone.setName(refPub.getName());
    }
    clone.setPk(new PublicationPK(refPub.getPK().getId(), refPub.getPK().getInstanceId()));
    if (refPub.getStatus() != null) {
      clone.setStatus(refPub.getStatus());
    }
    if (refPub.getTargetValidatorId() != null) {
      clone.setTargetValidatorId(refPub.getTargetValidatorId());
    }
    if (refPub.getCloneId() != null) {
      clone.setCloneId(refPub.getCloneId());
    }
    if (refPub.getUpdateDate() != null) {
      clone.setUpdateDate(new Date(refPub.getUpdateDate().getTime()));
    }
    if (refPub.getUpdaterId() != null) {
      clone.setUpdaterId(refPub.getUpdaterId());
    }
    if (refPub.getValidateDate() != null) {
      clone.setValidateDate(new Date(refPub.getValidateDate().getTime()));
    }
    if (refPub.getValidatorId() != null) {
      clone.setValidatorId(refPub.getValidatorId());
    }
    if (refPub.getVersion() != null) {
      clone.setVersion(refPub.getVersion());
    }
    if (refPub.getLanguage() != null) {
      clone.setLanguage(refPub.getLanguage());
    }

    return clone;
  }

  /**
   * In charge of merging data from the clone with the stable one.
   * @param currentPub all the necessary data about a publication as {@link CompletePublication}.
   * @param validatorUserId the identifier of the last user validating the given publication.
   * @param validationDate the date of validation to register. Date of day is taken if null is
   * given.
   * @return the merged publication as {@link PublicationDetail}.
   * @throws FormException
   * @throws PublicationTemplateException
   * @throws AttachmentException
   */
  private PublicationDetail mergeClone(CompletePublication currentPub, String validatorUserId,
      final Date validationDate) throws
      FormException, PublicationTemplateException, AttachmentException {
    PublicationDetail currentPubDetail = currentPub.getPublicationDetail();
    String memInfoId = currentPubDetail.getInfoId();
    PublicationPK pubPK = currentPubDetail.getPK();
    // merge du clone sur la publi de référence
    String cloneId = currentPubDetail.getCloneId();
    if (!"-1".equals(cloneId)) {
      currentPubDetail = clonePublication(cloneId, pubPK, validatorUserId, validationDate);
      // merge des fichiers joints
      ForeignPK pkFrom = new ForeignPK(pubPK.getId(), pubPK.getInstanceId());
      ForeignPK pkTo = new ForeignPK(cloneId, pubPK.getInstanceId());
      Map<String, String> attachmentIds = AttachmentServiceProvider.getAttachmentService().
          mergeDocuments(pkFrom, pkTo, DocumentType.attachment);
      // merge du contenu XMLModel
      String infoId = currentPubDetail.getInfoId();
      if (infoId != null && !"0".equals(infoId) && !isInteger(infoId)) {
        RecordSet set = getXMLFormFrom(infoId, pubPK);
        if (memInfoId != null && !"0".equals(memInfoId)) {
          // il existait déjà un contenu
          set.merge(cloneId, pubPK.getInstanceId(), pubPK.getId(), pubPK.getInstanceId(),
              attachmentIds);
        } else {
          // il n'y avait pas encore de contenu
          PublicationTemplateManager publicationTemplateManager = PublicationTemplateManager.
              getInstance();
          publicationTemplateManager
              .addDynamicPublicationTemplate(pubPK.getInstanceId() + ":" + infoId,
                  infoId + ".xml");

          set.clone(cloneId, pubPK.getInstanceId(), pubPK.getId(), pubPK.getInstanceId(),
              attachmentIds);
        }
      }
      // merge du contenu Wysiwyg
      boolean cloneWysiwyg = WysiwygController.haveGotWysiwyg(pubPK.getInstanceId(), cloneId,
          currentPubDetail.getLanguage());
      if (cloneWysiwyg) {
        try {
          // delete wysiwyg contents of public version
          WysiwygController.deleteWysiwygAttachmentsOnly(pubPK.getInstanceId(), pubPK.getId());
        } catch (WysiwygException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
        // wysiwyg contents of work version become public version ones
        WysiwygController
            .copy(pubPK.getInstanceId(), cloneId, pubPK.getInstanceId(), pubPK.getId(),
                currentPubDetail.getUpdaterId());
      }

      // suppression du clone
      deletePublication(new PublicationPK(cloneId, pubPK));
    }
    return currentPubDetail;
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
        currentPubDetail.setCloneStatus(PublicationDetail.REFUSED);
        currentPubDetail.setUpdateDateMustBeSet(false);
        publicationService.setDetail(currentPubDetail);

        // we have to alert publication's last updater
        List<NodePK> fathers = (List<NodePK>) getPublicationFathers(pubPK);
        NodePK oneFather = null;
        if (fathers != null && fathers.size() > 0) {
          oneFather = fathers.get(0);
        }
        sendValidationNotification(oneFather, clone, refusalMotive, userId);

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
        List<NodePK> fathers = (List<NodePK>) getPublicationFathers(pubPK);
        NodePK oneFather = null;
        if (fathers != null && !fathers.isEmpty()) {
          oneFather = fathers.get(0);
        }
        sendValidationNotification(oneFather, currentPubDetail, refusalMotive, userId);

        //remove tasks
        removeAllTodosForPublication(currentPubDetail.getPK());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.unvalidatePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_REFUSER_LA_PUBLICATION", e);
    }
  }

  @Override
  public void suspendPublication(PublicationPK pubPK, String defermentMotive, String userId) {
    try {
      PublicationDetail currentPubDetail = publicationService.getDetail(pubPK);

      // change publication's status
      currentPubDetail.setStatus(PublicationDetail.TO_VALIDATE);

      KmeliaHelper.checkIndex(currentPubDetail);

      publicationService.setDetail(currentPubDetail);

      // change visibility over PDC
      updateSilverContentVisibility(currentPubDetail);

      unIndexExternalElementsOfPublication(currentPubDetail.getPK());

      // we have to alert publication's creator
      sendDefermentNotification(currentPubDetail, defermentMotive, userId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.unvalidatePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_REFUSER_LA_PUBLICATION", e);
    }
  }

  private void sendDefermentNotification(final PublicationDetail pubDetail,
      final String defermentMotive, final String senderId) {
    try {
      UserNotificationHelper
          .buildAndSend(new KmeliaDefermentPublicationUserNotification(pubDetail, defermentMotive));
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn("User notification failure about publication ''{0}'' (id={1})",
          pubDetail.getTitle(), pubDetail.getPK().getId());
    }
  }

  /**
   * Change publication status from draft to valid (for publisher) or toValidate (for redactor).
   * @param pubPK
   * @param topicPK
   * @param userProfile
   */
  @Override
  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile) {
    PublicationDetail pubDetail =
        draftOutPublicationWithoutNotifications(pubPK, topicPK, userProfile);
    indexExternalElementsOfPublication(pubDetail);
    sendTodosAndNotificationsOnDraftOut(pubDetail, topicPK, userProfile);
  }

  /**
   * This method is here to manage correctly transactional scope of EJB (conflict between EJB and
   * UserPreferences service)
   * @param pubPK
   * @return
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public PublicationDetail draftOutPublicationWithoutNotifications(PublicationPK pubPK,
      NodePK topicPK, String userProfile) {
    return draftOutPublication(pubPK, topicPK, userProfile, false, true);
  }

  @Override
  public PublicationDetail draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile, boolean forceUpdateDate) {
    return draftOutPublication(pubPK, topicPK, userProfile, forceUpdateDate, false);
  }

  private PublicationDetail draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile, boolean forceUpdateDate, boolean inTransaction) {
    try {
      PublicationDetail changedPublication = null;
      CompletePublication currentPub = publicationService.getCompletePublication(pubPK);
      PublicationDetail pubDetail = currentPub.getPublicationDetail();
      if (userProfile.equals("publisher") || userProfile.equals("admin")) {
        if (pubDetail.haveGotClone()) {
          pubDetail = mergeClone(currentPub, null, null);
        }
        pubDetail.setStatus(PublicationDetail.VALID);
        changedPublication = pubDetail;
      } else {
        if (pubDetail.haveGotClone()) {
          // changement du statut du clone
          PublicationDetail clone = publicationService.getDetail(pubDetail.getClonePK());
          clone.setStatus(PublicationDetail.TO_VALIDATE);
          clone.setIndexOperation(IndexManager.NONE);
          clone.setUpdateDateMustBeSet(false);
          publicationService.setDetail(clone);
          changedPublication = clone;
          pubDetail.setCloneStatus(PublicationDetail.TO_VALIDATE);
        } else {
          pubDetail.setStatus(PublicationDetail.TO_VALIDATE);
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.draftOutPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
  }

  private void sendTodosAndNotificationsOnDraftOut(PublicationDetail pubDetail, NodePK topicPK,
      String userProfile) {
    if (SilverpeasRole.writer.isInRole(userProfile)) {
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

  /**
   * Change publication status from any state to draft.
   * @param pubPK
   */
  @Override
  public void draftInPublication(PublicationPK pubPK) {
    draftInPublication(pubPK, null);
  }

  @Override
  public void draftInPublication(PublicationPK pubPK, String userId) {
    try {
      PublicationDetail pubDetail = publicationService.getDetail(pubPK);
      if (pubDetail.isRefused() || pubDetail.isValid()) {
        pubDetail.setStatus(PublicationDetail.DRAFT);
        pubDetail.setUpdaterId(userId);
        KmeliaHelper.checkIndex(pubDetail);
        publicationService.setDetail(pubDetail);
        updateSilverContentVisibility(pubDetail);
        unIndexExternalElementsOfPublication(pubDetail.getPK());
        removeAllTodosForPublication(pubPK);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.draftInPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
  }

  @Override
  public NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK, NodePK topicPK,
      String senderName) {
    final PublicationDetail pubDetail = getPublicationDetail(pubPK);
    pubDetail.setAlias(isAlias(pubDetail, topicPK));

    final NotificationMetaData notifMetaData = UserNotificationHelper
        .build(new KmeliaNotifyPublicationUserNotification(topicPK, pubDetail, senderName));

    return notifMetaData;
  }

  public boolean isAlias(PublicationDetail pubDetail, NodePK nodePK) {
    boolean result = false;
    Collection<Alias> aliases = getAlias(pubDetail.getPK());
    for (Alias alias : aliases) {
      if (!alias.getInstanceId().equals(pubDetail.getInstanceId()) && nodePK.equals(alias)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * @param pubPK
   * @param documentPk
   * @param topicPK
   * @param senderName
   * @return
   * @
   */
  @Override
  public NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK,
      SimpleDocumentPK documentPk, NodePK topicPK, String senderName) {
    final PublicationDetail pubDetail = getPublicationDetail(pubPK);
    final SimpleDocument document = AttachmentServiceProvider.getAttachmentService().
        searchDocumentById(documentPk, null);
    SimpleDocument version = document.getLastPublicVersion();
    if (version == null) {
      version = document.getVersionMaster();
    }
    final NotificationMetaData notifMetaData = UserNotificationHelper.build(
        new KmeliaDocumentSubscriptionPublicationUserNotification(topicPK, pubDetail, version,
            senderName));
    return notifMetaData;
  }

  /**
   * delete reading controls to a publication
   * @param pubPK the id of a publication
   * @since 1.0
   */
  @Override
  public void deleteAllReadingControlsByPublication(PublicationPK pubPK) {
    try {
      statisticService
          .deleteStats(new ForeignPK(pubPK.getId(), pubPK.getInstanceId()), "Publication");
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.deleteAllReadingControlsByPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_CONTROLES_DE_LECTURE", e);
    }
  }

  @Override
  public List<HistoryObjectDetail> getLastAccess(PublicationPK pk, NodePK nodePK,
      String excludedUserId) {

    Collection<HistoryObjectDetail> allAccess =
        statisticService.getHistoryByAction(new ForeignPK(pk), 1, "Publication");
    List<String> userIds = getUserIdsOfFolder(nodePK);
    List<String> readerIds = new ArrayList<String>();

    List<HistoryObjectDetail> lastAccess = new ArrayList<HistoryObjectDetail>();

    for (HistoryObjectDetail access : allAccess) {
      String readerId = access.getUserId();
      if ((!StringUtil.isDefined(excludedUserId) || !excludedUserId.equals(readerId)) &&
          (userIds == null || userIds.contains(readerId)) && !readerIds.contains(readerId)) {
        readerIds.add(readerId);
        if (!User.getById(readerId).isAnonymous()) {
          lastAccess.add(access);
        }
      }
    }

    return lastAccess;
  }

  @Override
  public List<String> getUserIdsOfFolder(NodePK pk) {
    if (!isRightsOnTopicsEnabled(pk.getInstanceId())) {
      return null;
    }

    NodeDetail node = getNodeHeader(pk);

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      List<String> profileNames = new ArrayList<String>(4);
      profileNames.add(KmeliaHelper.ROLE_ADMIN);
      profileNames.add(KmeliaHelper.ROLE_PUBLISHER);
      profileNames.add(KmeliaHelper.ROLE_WRITER);
      profileNames.add(KmeliaHelper.ROLE_READER);
      String[] userIds = getOrganisationController()
          .getUsersIdsByRoleNames(pk.getInstanceId(), Integer.toString(rightsDependsOn),
              ObjectType.NODE, profileNames);
      return Arrays.asList(userIds);
    } else {
      return null;
    }
  }

  @Override
  public void indexKmelia(String componentId) {
    indexTopics(new NodePK("useless", componentId));
    indexPublications(new PublicationPK("useless", componentId));
  }

  private void indexPublications(PublicationPK pubPK) {
    Collection<PublicationDetail> pubs = null;
    try {
      pubs = publicationService.getAllPublications(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.indexPublications()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_PUBLICATIONS", e);
    }

    if (pubs != null) {
      for (PublicationDetail pub : pubs) {
        try {
          pubPK = pub.getPK();
          // index only valid publications
          if (pub.getStatus() != null && pub.isValid()) {
            List<NodePK> pubFathers = (List<NodePK>) publicationService.getAllFatherPK(pubPK);
            // index only valid publications which are not only in
            // dz or basket
            if (pubFathers.size() >= 2) {
              indexPublication(pub);
            } else if (pubFathers.size() == 1) {
              NodePK nodePK = pubFathers.get(0);
              // index the valid publication if it is not in the
              // basket
              if (!nodePK.isTrash()) {
                indexPublication(pub);
              }
            } else {
              // don't index publications in the dz
            }
          }
        } catch (Exception e) {
          /*throw new KmeliaRuntimeException("DefaultKmeliaService.indexPublications()", ERROR,
              "kmelia.EX_IMPOSSIBLE_DINDEXER_LA_PUBLICATION", "pubPK = " + pubPK.toString(), e);*/
          SilverLogger.getLogger(this)
              .error("Error during indexation of publication {0}", pubPK.getId(), e);
        }
      }
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.indexTopics()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_THEMES", e);
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
        PublicationDetail.TO_VALIDATE.equalsIgnoreCase(pubDetail.getCloneStatus())) {
      int validationType = getValidationType(pubDetail.getPK().getInstanceId());
      if (validationType == KmeliaHelper.VALIDATION_TARGET_N ||
          validationType == KmeliaHelper.VALIDATION_COLLEGIATE) {
        // removing potential older validation decision
        publicationService.removeValidationSteps(pubDetail.getPK());
      }
      List<String> validators = getAllValidators(pubDetail.getPK());
      addTodoAndSendNotificationToValidators(pubDetail, validators);
    }
  }

  private void addTodoAndSendNotificationToValidators(PublicationDetail pub,
      List<String> validators) {
    if (validators != null && !validators.isEmpty()) {
      String[] users = validators.toArray(new String[validators.size()]);
      // For each publisher create a todo
      addTodo(pub, users);
      // Send a notification to alert admins and publishers
      sendValidationAlert(pub, users);
    }
  }

  private String addTodo(PublicationDetail pubDetail, String[] users) {
    LocalizationBundle message =
        ResourceLocator.getLocalizationBundle("org.silverpeas.kmelia.multilang.kmeliaBundle");

    TodoDetail todo = new TodoDetail();

    todo.setId(pubDetail.getPK().getId());
    todo.setSpaceId(pubDetail.getPK().getSpace());
    todo.setComponentId(pubDetail.getPK().getComponentName());
    todo.setName(message.getString("ToValidateShort") + " : " + pubDetail.getName());

    List<Attendee> attendees = new ArrayList<Attendee>();
    for (String user : users) {
      if (user != null) {
        attendees.add(new Attendee(user));
      }
    }
    todo.setAttendees(new ArrayList<Attendee>(attendees));
    if (isDefined(pubDetail.getUpdaterId())) {
      todo.setDelegatorId(pubDetail.getUpdaterId());
    } else {
      todo.setDelegatorId(pubDetail.getCreatorId());
    }
    todo.setExternalId(pubDetail.getPK().getId());

    return calendar.addToDo(todo);
  }

  /*
   * Remove todos for all pubishers of this kmelia instance
   * @param pubDetail corresponding publication
   */
  private void removeAllTodosForPublication(PublicationPK pubPK) {
    calendar.removeToDoFromExternal("useless", pubPK.getInstanceId(), pubPK.getId());
  }

  private void removeTodoForPublication(PublicationPK pubPK, List<String> userIds) {
    if (userIds != null) {
      for(String userId : userIds) {
        removeTodoForPublication(pubPK, userId);
      }
    }
  }

  private void removeTodoForPublication(PublicationPK pubPK, String userId) {
    calendar.removeAttendeeInToDoFromExternal(pubPK.getInstanceId(), pubPK.getId(), userId);
  }

  private void sendValidationAlert(final PublicationDetail pubDetail, final String[] users) {
    UserNotificationHelper
        .buildAndSend(new KmeliaPendingValidationPublicationUserNotification(pubDetail, users));
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
    int silverObjectId = -1;
    PublicationDetail pubDetail;
    try {
      silverObjectId =
          kmeliaContentManager.getSilverContentId(pubPK.getId(), pubPK.getInstanceId());
      if (silverObjectId == -1) {
        pubDetail = getPublicationDetail(pubPK);
        silverObjectId = createSilverContent(pubDetail, pubDetail.getCreatorId());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getSilverObjectId()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(PublicationDetail pubDetail, String creatorId) {
    Connection con = null;
    try {
      con = getConnection();
      return kmeliaContentManager.createSilverContent(con, pubDetail, creatorId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.createSilverContent()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.deleteSilverContent()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    } finally {
      freeConnection(con);
    }
  }

  private void updateSilverContentVisibility(PublicationDetail pubDetail) {
    try {
      kmeliaContentManager.updateSilverContentVisibility(pubDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.updateSilverContentVisibility()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  private void updateSilverContentVisibility(PublicationPK pubPK, boolean isVisible) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    try {
      kmeliaContentManager.updateSilverContentVisibility(pubDetail, isVisible);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.updateSilverContentVisibility()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  private void indexExternalElementsOfPublication(PublicationDetail pubDetail) {
    if (KmeliaHelper.isIndexable(pubDetail)) {
      try {
        // index all files except Wysiwyg which are already indexed as publication content
        List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
            listAllDocumentsByForeignKey(pubDetail.getPK(), null);
        for (SimpleDocument doc : documents) {
          if (doc.getDocumentType() != DocumentType.wysiwyg) {
            AttachmentServiceProvider.getAttachmentService().createIndex(doc, pubDetail.getBeginDate(), pubDetail.getEndDate());
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Indexing versioning documents failed for publication {0}",
            new String[] {pubDetail.getPK().getId()}, e);
      }
      try {
        // index comments
        getCommentService()
            .indexAllCommentsOnPublication(pubDetail.getContributionType(), pubDetail.getPK());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Indexing comments failed for publication {0}",
            new String[]{pubDetail.getPK().getId()}, e);
      }
    }
  }

  private void unIndexExternalElementsOfPublication(PublicationPK pubPK) {
    try {
      AttachmentServiceProvider.getAttachmentService().unindexAttachmentsOfExternalObject(pubPK);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Unindexing versioning documents failed for publication {0}",
          new String[] {pubPK.getId()}, e);
    }
    try {
      // index comments
      getCommentService()
          .unindexAllCommentsOnPublication(PublicationDetail.getResourceType(), pubPK);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Unindexing versioning documents failed for publication {0}",
          new String[] {pubPK.getId()}, e);
    }
  }

  private void removeExternalElementsOfPublications(PublicationPK pubPK) {
    // remove attachments and WYSIWYG
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
        listAllDocumentsByForeignKey(pubPK, null);
    for (SimpleDocument doc : documents) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(doc, false);
    }
    // remove comments
    try {
      getCommentService()
          .deleteAllCommentsOnPublication(PublicationDetail.getResourceType(), pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.removeExternalElementsOfPublications()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COMMENTAIRES", e);
    }

    // remove Thumbnail content
    try {
      ThumbnailDetail thumbToDelete =
          new ThumbnailDetail(pubPK.getInstanceId(), Integer.parseInt(pubPK.getId()),
              ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      ThumbnailController.deleteThumbnail(thumbToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.removeExternalElementsOfPublications", ERROR,
          "root.EX_DELETE_THUMBNAIL_FAILED", e);
    }

    // remove date reminder
    PublicationNoteReference publicationNoteReference = new PublicationNoteReference(pubPK.getId());
    getDateReminderService().remove(publicationNoteReference);
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
        String xmlFormShortName = infoId;

        PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance().
            getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":" + xmlFormShortName);

        RecordSet set = pubTemplate.getRecordSet();
        DataRecord data = set.getRecord(pubDetail.getPK().getId());
        set.delete(data);
      }
    } catch (PublicationTemplateException e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.removeXMLContentOfPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (FormException e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.removeXMLContentOfPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.openConnection();
      return con;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getConnection()", ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
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
      throw new KmeliaRuntimeException("DefaultKmeliaService.addModelUsed()", ERROR,
          "kmelia.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
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
      Collection<String> result =
          ModelDAO.getModelUsed(con, instanceId, nodeId);
      if (isDefined(nodeId) && result.isEmpty()) {
        // there is no templates defined for the given node, check the parent nodes
        Collection<NodeDetail> parents = nodeService.getPath(new NodePK(nodeId, instanceId));
        Iterator<NodeDetail> iter = parents.iterator();
        while (iter.hasNext() && result.isEmpty()) {
          NodeDetail parent = iter.next();
          result = ModelDAO.getModelUsed(con, instanceId, parent.
              getNodePK().getId());
        }
      }
      return result;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getModelUsed()", ERROR,
          "kmelia.IMPOSSIBLE_DE_RECUPERER_LES_MODELES", e);
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
      throw new KmeliaRuntimeException("KmeliaBmEJB.setModelUsed()", ERROR,
          "kmelia.IMPOSSIBLE_DE_RECUPERER_LES_MODELES", e);
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
    List<NodeDetail> axis = new ArrayList<NodeDetail>();
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
      throw new KmaxRuntimeException("DefaultKmeliaService.getAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_AXES", e);
    }
    return axis;
  }

  @Override
  public List<NodeDetail> getAxisHeaders(String componentId) {
    List<NodeDetail> axisHeaders = null;
    try {
      axisHeaders = nodeService.getHeadersByLevel(new NodePK("useless", componentId), 2);
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.getAxisHeaders()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_ENTETES_DES_AXES", e);
    }
    return axisHeaders;
  }

  @Override
  public NodePK addAxis(NodeDetail axis, String componentId) {
    NodePK axisPK = new NodePK("toDefine", componentId);
    NodeDetail rootDetail =
        new NodeDetail(new NodePK("0"), "Root", "desc", "unknown", "unknown", "/0", 1,
            new NodePK("-1"), null);
    rootDetail.setStatus(NodeDetail.STATUS_VISIBLE);
    axis.setNodePK(axisPK);
    CoordinatePK coordinatePK = new CoordinatePK("useless", axisPK);
    try {
      // axis creation
      axisPK = nodeService.createNode(axis, rootDetail);
      // add this new axis to existing coordinates
      CoordinatePoint point = new CoordinatePoint(-1, Integer.parseInt(axisPK.getId()), true);
      coordinatesService.addPointToAllCoordinates(coordinatePK, point);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.addAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_CREER_L_AXE", e);
    }
    return axisPK;
  }

  @Override
  public void updateAxis(NodeDetail axis, String componentId) {
    axis.getNodePK().setComponentName(componentId);
    try {
      nodeService.setDetail(axis);
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.updateAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_MODIFIER_L_AXE", e);
    }
  }

  @Override
  public void deleteAxis(String axisId, String componentId) {
    NodePK pkToDelete = new NodePK(axisId, componentId);
    PublicationPK pubPK = new PublicationPK("useless");

    CoordinatePK coordinatePK = new CoordinatePK("useless", pkToDelete);
    List<String> fatherIds = new ArrayList<String>();
    Collection<String> coordinateIds;
    // Delete the axis
    try {
      // delete publicationFathers
      if (getAxisHeaders(componentId).size() == 1) {
        coordinateIds = coordinatesService.getCoordinateIdsByNodeId(coordinatePK, axisId);
        Iterator<String> coordinateIdsIt = coordinateIds.iterator();
        String coordinateId;
        while (coordinateIdsIt.hasNext()) {
          coordinateId = coordinateIdsIt.next();
          fatherIds.add(coordinateId);
        }
        if (fatherIds.size() > 0) {
          publicationService.removeFathers(pubPK, fatherIds);
        }
      }
      // delete coordinate which contains subComponents of this component
      Collection<NodeDetail> subComponents = nodeService.getDescendantDetails(pkToDelete);
      Iterator<NodeDetail> it = subComponents.iterator();
      List<NodePK> points = new ArrayList<NodePK>();
      points.add(pkToDelete);
      while (it.hasNext()) {
        points.add((it.next()).getNodePK());
      }
      removeCoordinatesByPoints(points, componentId);
      // delete axis
      nodeService.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.deleteAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_L_AXE", e);
    }
  }

  private void removeCoordinatesByPoints(List<NodePK> nodePKs, String componentId) {
    Iterator<NodePK> it = nodePKs.iterator();
    List<String> coordinatePoints = new ArrayList<String>();
    String nodeId;
    while (it.hasNext()) {
      nodeId = (it.next()).getId();
      coordinatePoints.add(nodeId);
    }
    CoordinatePK coordinatePK = new CoordinatePK("useless", "useless", componentId);
    try {
      coordinatesService
          .deleteCoordinatesByPoints(coordinatePK, (ArrayList<String>) coordinatePoints);
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.removeCoordinatesByPoints()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COORDONNEES_PAR_UN_POINT", e);
    }
  }

  @Override
  public NodeDetail getNodeHeader(String id, String componentId) {
    NodePK pk = new NodePK(id, componentId);
    return getNodeHeader(pk);
  }

  private NodeDetail getNodeHeader(NodePK pk) {
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = nodeService.getHeader(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.getNodeHeader()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
    }
    return nodeDetail;
  }

  @Override
  public NodePK addPosition(String fatherId, NodeDetail position, String componentId,
      String userId) {
    position.getNodePK().setComponentName(componentId);
    position.setCreationDate(DateUtil.today2SQLDate());
    position.setCreatorId(userId);
    NodeDetail fatherDetail;
    NodePK componentPK = null;

    fatherDetail = getNodeHeader(fatherId, componentId);
    try {
      componentPK = nodeService.createNode(position, fatherDetail);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.addPosition()", ERROR,
          "kmax.EX_IMPOSSIBLE_DAJOUTER_UNE_COMPOSANTE_A_L_AXE", e);
    }
    return componentPK;
  }

  @Override
  public void updatePosition(NodeDetail position, String componentId) {
    position.getNodePK().setComponentName(componentId);
    try {
      nodeService.setDetail(position);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.updatePosition()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_MODIFIER_LA_COMPOSANTE_DE_L_AXE", e);
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
      List<NodePK> points = new ArrayList<NodePK>();
      points.add(pkToDelete);
      while (it.hasNext()) {
        points.add((it.next()).getNodePK());
      }
      removeCoordinatesByPoints(points, componentId);
      // delete component
      nodeService.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.deletePosition()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_COMPOSANTE_DE_L_AXE", e);
    }
  }

  @Override
  public Collection<NodeDetail> getPath(String id, String componentId) {
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    NodePK nodePK = new NodePK(id, componentId);
    // compute path from a to z

    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeService.getPath(nodePK);
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.getPath()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LE_CHEMIN", e);
    }
    return newPath;
  }

  public Collection<Coordinate> getKmaxPathList(PublicationPK pubPK) {
    Collection<Coordinate> coordinates = null;
    try {
      coordinates = getPublicationCoordinates(pubPK.getId(), pubPK.getInstanceId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getKmaxPathList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    return coordinates;
  }

  @Override
  public List<KmeliaPublication> search(List<String> combination, String componentId) {
    Collection<PublicationDetail> publications = searchPublications(combination, componentId);
    if (publications == null) {
      return new ArrayList<KmeliaPublication>();
    }
    return pubDetails2userPubs(publications);
  }

  @Override
  public List<KmeliaPublication> search(List<String> combination, int nbDays, String componentId) {
    Collection<PublicationDetail> publications = searchPublications(combination, componentId);
    return pubDetails2userPubs(filterPublicationsByBeginDate(publications, nbDays));
  }

  private Collection<PublicationDetail> searchPublications(List<String> combination,
      String componentId) {
    PublicationPK pk = new PublicationPK("useless", componentId);
    CoordinatePK coordinatePK = new CoordinatePK("unknown", pk);
    Collection<PublicationDetail> publications = null;
    Collection<String> coordinates = null;
    try {
      // Remove node "Toutes catégories" (level == 2) from combination
      int nodeLevel;
      String axisValue;
      for (int i = 0; i < combination.size(); i++) {
        axisValue = combination.get(i);
        StringTokenizer st = new StringTokenizer(axisValue, "/");
        nodeLevel = st.countTokens();
        // if node is level 2, it represents "Toutes Catégories"
        // this axis is not used by the search
        if (nodeLevel == 2) {
          combination.remove(i);
          i--;
        }
      }
      if (combination.isEmpty()) {
        // all criterias is "Toutes Catégories"
        // get all publications classified
        NodePK basketPK = new NodePK("1", componentId);
        publications = publicationService.getDetailsNotInFatherPK(basketPK);
      } else {
        if (combination != null && combination.size() > 0) {
          coordinates = coordinatesService
              .getCoordinatesByFatherPaths((ArrayList<String>) combination, coordinatePK);
        }
        if (!coordinates.isEmpty()) {
          publications =
              publicationService.getDetailsByFatherIds((ArrayList<String>) coordinates, pk, false);
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.search()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_RESULTATS", e);
    }
    return publications;
  }

  @Override
  public Collection<KmeliaPublication> getUnbalancedPublications(String componentId) {
    PublicationPK pk = new PublicationPK("useless", componentId);
    Collection<PublicationDetail> publications = null;
    try {
      publications = publicationService.getOrphanPublications(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException("DefaultKmeliaService.getUnbalancedPublications()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_PUBLICATIONS_NON_CLASSEES", e);
    }
    return pubDetails2userPubs(publications);
  }

  private Collection<PublicationDetail> filterPublicationsByBeginDate(
      Collection<PublicationDetail> publications, int nbDays) {
    List<PublicationDetail> pubOK = new ArrayList<PublicationDetail>();
    if (publications != null) {
      Calendar rightNow = Calendar.getInstance();
      if (nbDays == 0) {
        nbDays = 1;
      }
      rightNow.add(Calendar.DATE, 0 - nbDays);
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
    indexPublications(new PublicationPK("useless", componentId));
  }

  private void indexAxis(String componentId) {
    NodePK nodePK = new NodePK("useless", componentId);
    try {
      Collection<NodeDetail> nodes = nodeService.getAllNodes(nodePK);
      if (nodes != null) {
        for (NodeDetail nodeDetail : nodes) {
          if ("corbeille".equalsIgnoreCase(nodeDetail.getName()) &&
              nodeDetail.getNodePK().isTrash()) {
            // do not index the bin
          } else {
            nodeService.createIndex(nodeDetail);
          }
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.indexAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DINDEXER_LES_AXES", e);
    }
  }

  @Override
  public KmeliaPublication getKmaxPublication(String pubId, String currentUserId) {
    PublicationPK pubPK;
    CompletePublication completePublication = null;

    try {
      pubPK = new PublicationPK(pubId);
      completePublication = publicationService.getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.getKmaxCompletePublication()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_INFORMATIONS_DE_LA_PUBLICATION", e);
    }
    KmeliaPublication publication =
        KmeliaPublication.aKmeliaPublicationFromCompleteDetail(completePublication);
    return publication;
  }

  @Override
  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId) {
    try {
      return publicationService.getCoordinates(pubId, componentId);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.getPublicationCoordinates()", ERROR,
          "root.MSG_GEN_PARAM_VALUE", e);
    }
  }

  @Override
  public void addPublicationToCombination(String pubId, List<String> combination,
      String componentId) {
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK);
    try {

      Collection<Coordinate> coordinates = getPublicationCoordinates(pubId, componentId);

      if (!checkCombination(coordinates, combination)) {
        return;
      }

      NodeDetail nodeDetail;
      // enrich combination by get ancestors
      Iterator<String> it = combination.iterator();
      List<CoordinatePoint> allnodes = new ArrayList<CoordinatePoint>();
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
      throw new KmaxRuntimeException("KmeliaBmEjb.addPublicationToCombination()", ERROR,
          "kmax.EX_IMPOSSIBLE_DAJOUTER_LA_PUBLICATION_A_CETTE_COMBINAISON", e);
    }
  }

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
  public void deleteCoordinates(CoordinatePK coordinatePK, List<String> coordinates) {
    coordinatesService.deleteCoordinates(coordinatePK, coordinates);
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
      List<String> coordinateIds = new ArrayList<String>(1);
      coordinateIds.add(combinationId);
      coordinatesService.deleteCoordinates(coordinatePK, coordinateIds);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.deletePublicationFromCombination()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_COMBINAISON_DE_LA_PUBLICATION", e);
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
    PublicationPK pubPK = null;
    Connection con = getConnection();
    try {
      // create the publication
      pubDetail =
          changePublicationStatusOnCreation(pubDetail, new NodePK("useless", pubDetail.getPK()));
      pubPK = publicationService.createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());

      // creates todos for publishers
      this.createTodosForPublication(pubDetail, true);

      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.createKmaxPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
    return pubPK.getId();
  }

  @Override
  public Collection<Alias> getAlias(PublicationPK pubPK) {
    try {
      return publicationService.getAlias(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getAlias()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LES_ALIAS_DE_PUBLICATION", e);
    }
  }

  @Override
  public void setAlias(PublicationPK pubPK, List<Alias> alias) {

    publicationService.setAlias(pubPK, alias);

    // Send subscriptions to aliases subscribers
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    sendSubscriptionsNotification(pubDetail, NotifAction.PUBLISHED, true);
  }

  @Override
  public void addAttachmentToPublication(PublicationPK pubPK, String userId, String filename,
      String description, byte[] contents) {
    try {
      Date creationDate = new Date();
      SimpleAttachment file =
          new SimpleAttachment(FileUtil.getFilename(filename), I18NHelper.defaultLanguage, filename,
              "", contents.length, FileUtil.getMimeType(filename), userId, creationDate, null);
      boolean versioningActive = getBooleanValue(getOrganisationController().
          getComponentParameterValue(pubPK.getComponentName(), VERSION_MODE));
      SimpleDocument document;
      if (versioningActive) {
        document = new HistorisedDocument(new SimpleDocumentPK(null, pubPK.getComponentName()),
            pubPK.getId(), 0, file);
        document.setPublicDocument(true);
      } else {
        document = new SimpleDocument(new SimpleDocumentPK(null, pubPK.getComponentName()), pubPK.
            getId(), 0, false, file);
      }
      AttachmentServiceProvider.getAttachmentService()
          .createAttachment(document, new ByteArrayInputStream(contents));
    } catch (org.silverpeas.core.contribution.attachment.AttachmentException fnfe) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.addAttachmentToPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAJOUTER_ATTACHEMENT", fnfe);
    }
  }

  /**
   * Creates or updates a publication.
   * @param componentId The id of the component containing the publication.
   * @param topicId The id of the topic containing the publication.
   * @param spaceId The id of the space containing the publication.
   * @param userId The id of the user creating or updating the publication.
   * @param publiParams The publication's parameters.
   * @param formParams The parameters of the publication's form.
   * @param language The language of the publication.
   * @param xmlFormName The name of the publication's form.
   * @param discrimatingParameterName The name of the field included in the form which allowes to
   * retrieve the eventually existing publication to update.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   * @
   */
  @Override
  public boolean importPublication(String componentId, String topicId, String spaceId,
      String userId, Map<String, String> publiParams, Map<String, String> formParams,
      String language, String xmlFormName, String discrimatingParameterName, String userProfile) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport.importPublication(publiParams, formParams, language, xmlFormName,
        discrimatingParameterName, userProfile);
  }

  @Override
  public boolean importPublication(String componentId, String topicId, String userId,
      Map<String, String> publiParams, Map<String, String> formParams, String language,
      String xmlFormName, String discriminantParameterName, String userProfile,
      boolean ignoreMissingFormFields) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, null, userId);
    publicationImport.setIgnoreMissingFormFields(ignoreMissingFormFields);
    return publicationImport.importPublication(publiParams, formParams, language, xmlFormName,
        discriminantParameterName, userProfile);
  }

  /**
   * Creates or updates a publication.
   * @param publicationId The id of the publication to update.
   * @param componentId The id of the component containing the publication.
   * @param topicId The id of the topic containing the publication.
   * @param spaceId The id of the space containing the publication.
   * @param userId The id of the user creating or updating the publication.
   * @param publiParams The publication's parameters.
   * @param formParams The parameters of the publication's form.
   * @param language The language of the publication.
   * @param xmlFormName The name of the publication's form.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   * @
   */
  @Override
  public boolean importPublication(String publicationId, String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName, String userProfile) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport
        .importPublication(publicationId, publiParams, formParams, language, xmlFormName,
            userProfile);
  }

  @Override
  public void importPublications(String componentId, String topicId, String spaceId, String userId,
      List<Map<String, String>> publiParamsList, List<Map<String, String>> formParamsList,
      String language, String xmlFormName, String discrimatingParameterName, String userProfile) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    publicationImport.importPublications(publiParamsList, formParamsList, language, xmlFormName,
        discrimatingParameterName, userProfile);
  }

  @Override
  public List<XMLField> getPublicationXmlFields(String publicationId, String componentId,
      String spaceId, String userId) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, null, spaceId, userId);
    return publicationImport.getPublicationXmlFields(publicationId);
  }

  @Override
  public List<XMLField> getPublicationXmlFields(String publicationId, String componentId,
      String spaceId, String userId, String language) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, null, spaceId, userId);
    return publicationImport.getPublicationXmlFields(publicationId, language);
  }

  @Override
  public String createTopic(String componentId, String topicId, String spaceId, String userId,
      String name, String description) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport.createTopic(name, description);
  }

  @Override
  public Collection<String> getPublicationsSpecificValues(String componentId, String xmlFormName,
      String fieldName) {
    PublicationImport publicationImport = new PublicationImport(this, componentId);
    return publicationImport.getPublicationsSpecificValues(componentId, xmlFormName, fieldName);
  }

  @Override
  public void draftInPublication(String componentId, String xmlFormName, String fieldName,
      String fieldValue) {
    PublicationImport publicationImport = new PublicationImport(this, componentId);
    publicationImport.draftInPublication(xmlFormName, fieldName, fieldValue);
  }

  @Override
  public void updatePublicationEndDate(String componentId, String spaceId, String userId,
      String xmlFormName, String fieldName, String fieldValue, Date endDate) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, null, spaceId, userId);
    publicationImport.updatePublicationEndDate(xmlFormName, fieldName, fieldValue, endDate);
  }

  /**
   * Find a publication imported only by a xml field (old id for example)
   * @param componentId
   * @param xmlFormName
   * @param fieldName
   * @param fieldValue
   * @param topicId
   * @param spaceId
   * @param userId
   * @return pubId
   * @
   */
  @Override
  public String findPublicationIdBySpecificValue(String componentId, String xmlFormName,
      String fieldName, String fieldValue, String topicId, String spaceId, String userId) {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport.getPublicationId(xmlFormName, fieldName, fieldValue);
  }

  @Override
  public void doAutomaticDraftOut() {
    // get all clones with draftoutdate <= current date
    // pubCloneId <> -1 AND pubCloneStatus == 'Draft'
    Collection<PublicationDetail> pubs = publicationService.getPublicationsToDraftOut(true);
    // for each clone, call draftOutPublication method
    for (PublicationDetail pub : pubs) {
      draftOutPublication(pub.getClonePK(), null, "admin", true);
    }
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

      SettingBundle publicationSettings =
          ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");
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
      List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService()
          .listDocumentsByForeignKey(new ForeignPK(fromId, fromComponentId), null);
      Map<String, String> attachmentIds = new HashMap<String, String>(documents.size());
      for (SimpleDocument document : documents) {
        AttachmentServiceProvider.getAttachmentService().cloneDocument(document, cloneId);
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
      // paste only links, reverseLinks can't be cloned because it'is a new content not referenced
      // by any publication
      if (refPubComplete.getLinkList() != null && refPubComplete.getLinkList().size() > 0) {
        addInfoLinks(clonePK, refPubComplete.getLinkList());
      }

      // paste wysiwyg
      WysiwygController.copy(fromComponentId, fromId, fromComponentId, cloneId, clone.
          getCreatorId());

      // affectation de l'id du clone à la publication de référence
      refPub.setCloneId(cloneId);
      refPub.setCloneStatus(nextStatus);
      refPub.setStatusMustBeChecked(false);
      refPub.setUpdateDateMustBeSet(false);
      updatePublication(refPub);

      // paste vignette
      String vignette = refPub.getImage();
      if (vignette != null) {
        ThumbnailDetail thumbDetail = new ThumbnailDetail(clone.getPK().getInstanceId(),
            Integer.valueOf(clone.getPK().getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
        thumbDetail.setMimeType(refPub.getImageMimeType());
        if (vignette.startsWith("/")) {
          thumbDetail.setOriginalFileName(vignette);
        } else {
          String thumbnailsSubDirectory = publicationSettings.getString("imagesSubDirectory");
          String from = absolutePath + thumbnailsSubDirectory + File.separator + vignette;
          String type = FilenameUtils.getExtension(vignette);
          String newVignette = Long.toString(System.currentTimeMillis()) + "." + type;
          String to = absolutePath + thumbnailsSubDirectory + File.separator + newVignette;
          FileRepositoryManager.copyFile(from, to);
          thumbDetail.setOriginalFileName(newVignette);
        }
        ThumbnailServiceProvider.getThumbnailService().createThumbnail(thumbDetail);
      }
    } catch (IOException e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION", e);
    } catch (FormException fe) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", fe);
    } catch (PublicationTemplateException pe) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", pe);
    } catch (ThumbnailException e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION", e);
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
    return ResourceLocator.getLocalizationBundle("org.silverpeas.kmelia.multilang.kmeliaBundle");
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
    List<NodeDetail> children = new ArrayList<NodeDetail>();
    try {
      setAllowedSubfolders(root, userId);
      List<NodeDetail> nodes = (List<NodeDetail>) root.getChildrenDetails();

      // set nb objects in nodes
      setNbItemsOfSubfolders(root, treeview, userId);

      NodeDetail trash = null;
      for (NodeDetail node : nodes) {
        if (node.getNodePK().isTrash()) {
          trash = node;
        } else if (node.getNodePK().isUnclassed()) {
          // do not return it cause it is useless
        } else {
          children.add(node);
        }
      }

      // adding special folder "to validate"
      if (isUserCanValidate(instanceId, userId)) {
        NodeDetail temp = new NodeDetail();
        temp.getNodePK().setId("tovalidate");
        temp.setName(getMultilang().getString("ToValidateShort"));
        if (isNbItemsDisplayed(instanceId)) {
          int nbPublisToValidate = getPublicationsToValidate(instanceId, userId).size();
          temp.setNbObjects(nbPublisToValidate);
        }
        children.add(temp);
      }

      // adding special folder "trash"
      if (isUserCanWrite(instanceId, userId) && trash != null) {
        children.add(trash);
      }

      root.setChildrenDetails(children);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return children;
  }

  @Override
  public Collection<NodeDetail> getFolderChildren(NodePK nodePK, String userId) {
    NodeDetail node = nodeService.getDetail(nodePK);
    if (node.getNodePK().isRoot()) {
      node.setChildrenDetails(getRootChildren(node, userId, null));
    } else {
      setAllowedSubfolders(node, userId);
    }

    // set nb objects in nodes
    setNbItemsOfSubfolders(node, null, userId);

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
      return getTreeview(pk, "admin", isCoWritingEnable(instanceId), isDraftVisibleWithCoWriting(),
          userId, isNbItemsDisplayed(instanceId), false);
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
      if (isUserComponentAdmin(instanceId, userId)) {
        // user is admin of application, all folders must be shown
        setRole(node.getChildrenDetails(), userId);
      } else {
        Collection<NodeDetail> allowedChildren = getAllowedSubfolders(node, userId);
        setRole(allowedChildren, userId);
        node.setChildrenDetails(allowedChildren);
      }
    } else {
      // no rights are used
      // keep children as they are
    }
  }

  private boolean isUserComponentAdmin(String componentId, String userId) {
    return "admin".equalsIgnoreCase(KmeliaHelper.getProfile(getUserRoles(componentId, userId)));
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
    return StringUtil.getBooleanValue(getOrganisationController()
        .getComponentParameterValue(componentId, InstanceParameters.rightsOnFolders));
  }

  private boolean isNbItemsDisplayed(String componentId) {
    return StringUtil.getBooleanValue(getOrganisationController()
        .getComponentParameterValue(componentId, InstanceParameters.displayNbItemsOnFolders));
  }

  private boolean isCoWritingEnable(String componentId) {
    return StringUtil.getBooleanValue(getOrganisationController()
        .getComponentParameterValue(componentId, InstanceParameters.coWriting));
  }

  private boolean isDraftVisibleWithCoWriting() {
    return getComponentSettings().getBoolean("draftVisibleWithCoWriting", false);
  }

  @Override
  public String getUserTopicProfile(NodePK pk, String userId) {
    if (!isRightsOnTopicsEnabled(pk.getInstanceId()) ||
        KmeliaHelper.isToValidateFolder(pk.getId())) {
      return KmeliaHelper.getProfile(getUserRoles(pk.getInstanceId(), userId));
    }

    NodeDetail node = getNodeHeader(pk.getId(), pk.getInstanceId());

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      return KmeliaHelper.getProfile(getOrganisationController()
          .getUserProfiles(userId, pk.getInstanceId(), rightsDependsOn, ObjectType.NODE));
    } else {
      return KmeliaHelper.getProfile(getUserRoles(pk.getInstanceId(), userId));
    }
  }

  private String[] getUserRoles(String componentId, String userId) {
    return getOrganisationController().getUserProfiles(userId, componentId);
  }

  private NodePK getRootPK(String componentId) {
    return new NodePK(NodePK.ROOT_NODE_ID, componentId);
  }

  /**
   * This method verifies if the user behind the given user identifier can validate the publication
   * represented by the given primary key.
   * The verification is strictly applied on the given primary key, that is to say that no
   * publication clone information are retrieved.
   * To perform a verification on a publication clone, the primary key of the clone must be given.
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
    List<String> validatorIds = getAllValidators(pubPK);
    if (!validatorIds.contains(userId)) {
      // current user is not part of users who are able to validate this publication
      return false;
    }

    if (getValidationType(pubPK.getInstanceId()) == KmeliaHelper.VALIDATION_TARGET_N) {
      ValidationStep validationStep = publicationService.getValidationStepByUser(pubPK, userId);
      // user has not yet validated publication, so validation is allowed
      return validationStep == null;
    }
    return true;
  }

  @Override
  public boolean isUserCanValidate(String componentId, String userId) {
    if (KmeliaHelper.isToolbox(componentId)) {
      return false;
    }
    return isUserCanPublish(componentId, userId);
  }

  @Override
  public boolean isUserCanWrite(String componentId, String userId) {
    String[] grantedRoles =
        new String[]{SilverpeasRole.admin.name(), SilverpeasRole.publisher.name(),
            SilverpeasRole.writer.name()};
    return checkUserRoles(componentId, userId, grantedRoles);
  }

  @Override
  public boolean isUserCanPublish(String componentId, String userId) {
    String[] grantedRoles =
        new String[]{SilverpeasRole.admin.name(), SilverpeasRole.publisher.name()};
    return checkUserRoles(componentId, userId, grantedRoles);
  }

  private boolean checkUserRoles(String componentId, String userId, String... roles) {
    SilverpeasRole userProfile =
        SilverpeasRole.from(KmeliaHelper.getProfile(getUserRoles(componentId, userId)));
    boolean checked = userProfile.isInRole(roles);

    if (!checked && isRightsOnTopicsEnabled(componentId)) {
      // check if current user is publisher or admin on at least one descendant
      Iterator<NodeDetail> descendants =
          nodeService.getDescendantDetails(getRootPK(componentId)).iterator();
      while (!checked && descendants.hasNext()) {
        NodeDetail descendant = descendants.next();
        if (descendant.haveLocalRights()) {
          // check if user is admin, publisher or writer on this topic
          String[] profiles = adminController
              .getProfilesByObjectAndUserId(descendant.getId(), ObjectType.NODE.getCode(),
                  componentId, userId);
          if (profiles != null && profiles.length > 0) {
            userProfile = SilverpeasRole.from(KmeliaHelper.getProfile(profiles));
            checked = userProfile.isInRole(roles);
          }
        }
      }
    }
    return checked;
  }

  @Override
  public NodeDetail getExpandedPathToNode(NodePK pk, String userId) {
    String instanceId = pk.getInstanceId();
    List<NodeDetail> nodes = new ArrayList<NodeDetail>(nodeService.getPath(pk));
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
      currentNode = find(currentNode.getChildrenDetails(), node);
      // get children of each node on path to target node
      Collection<NodeDetail> children = nodeService.getChildrenDetails(node.getNodePK());
      node.setChildrenDetails(children);
      setAllowedSubfolders(node, userId);
      if (treeview != null) {
        setNbItemsOfSubfolders(node, treeview, userId);
      }
      currentNode.setChildrenDetails(node.getChildrenDetails());
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
   * Removes publications according to given ids. Before a publication is removed, user priviledges
   * are controlled. If node defines the trash, publications are definitively deleted. Otherwise,
   * publications move into trash.
   * @param ids the ids of publications to delete
   * @param nodePK the node where the publications are
   * @param userId the user who wants to perform deletion
   * @return the list of publication ids which has been really deleted
   * @
   */
  @Override
  public List<String> deletePublications(List<String> ids, NodePK nodePK, String userId) {
    List<String> removedIds = new ArrayList<String>();
    String profile = getProfile(userId, nodePK);
    for (String id : ids) {
      PublicationPK pk = new PublicationPK(id, nodePK);
      if (isUserCanDeletePublication(new PublicationPK(id, nodePK), profile, userId)) {
        try {
          if (nodePK.isTrash()) {
            deletePublication(pk);
          } else {
            sendPublicationToBasket(pk);
          }
          removedIds.add(id);
        } catch (Exception e) {
          SilverLogger.getLogger(this).error("Deletion of publication {0} failed",
              new String[] {pk.getId()}, e);
        }
      }
    }
    return removedIds;
  }

  private boolean isUserCanDeletePublication(PublicationPK pubPK, String profile, String userId) {
    User owner = getPublication(pubPK).getCreator();
    return KmeliaPublicationHelper.isRemovable(pubPK.getInstanceId(), userId, profile, owner);
  }

  @Override
  public String getWysiwyg(PublicationPK pubPK, String language) {
    try {
      return WysiwygController
          .load(pubPK.getInstanceId(), pubPK.getId(), I18NHelper.checkLanguage(language));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.getAttachments()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_WYSIWYG", e);
    }
  }

  @Override
  public KmeliaPublication getContentById(String contentId) {
    return getPublication(new PublicationPK(contentId));
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
  public NodeDetail moveNode(@SourcePK NodePK nodePK, @TargetPK NodePK to, String userId) {
    List<NodeDetail> treeToPaste = nodeService.getSubTree(nodePK);

    // move node and subtree
    nodeService.moveNode(nodePK, to);

    for (NodeDetail fromNode : treeToPaste) {
      if (fromNode != null) {
        NodePK toNodePK = new NodePK(fromNode.getNodePK().getId(), to);

        // remove rights
        if (fromNode.haveLocalRights()) {
          List<ProfileInst> profiles = adminController
              .getProfilesByObject(fromNode.getNodePK().getId(), ObjectType.NODE.getCode(),
                  fromNode.getNodePK().getInstanceId());
          if (profiles != null) {
            for (ProfileInst profile : profiles) {
              if (profile != null && StringUtil.isDefined(profile.getId())) {
                adminController.deleteProfileInst(profile.getId());
              }
            }
          }
        }

        // move rich description of node
        if (!nodePK.getInstanceId().equals(to.getInstanceId())) {
          WysiwygController.move(fromNode.getNodePK().getInstanceId(), "Node_" + fromNode.getId(),
              to.getInstanceId(), "Node_" + toNodePK.getId());
        }

        // move publications of node
        movePublicationsOfTopic(fromNode.getNodePK(), toNodePK, userId);
      }
    }

    nodePK.setComponentName(to.getInstanceId());
    return getNodeHeader(nodePK);
  }

  private void movePublicationsOfTopic(NodePK fromPK, NodePK toPK, String userId) {
    Collection<PublicationDetail> publications = publicationService.getDetailsByFatherPK(fromPK);
    for (PublicationDetail publi : publications) {
      movePublication(publi.getPK(), toPK, userId);
    }
  }

  @SimulationActionProcess(elementLister = KmeliaNodeSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
  public NodeDetail copyNode(@SourcePK @TargetPK KmeliaCopyDetail copyDetail) {
    HashMap<Integer, Integer> oldAndNewIds = new HashMap<Integer, Integer>();
    return copyNode(copyDetail, oldAndNewIds);
  }

  private NodeDetail copyNode(KmeliaCopyDetail copyDetail, HashMap<Integer, Integer> oldAndNewIds) {
    NodePK nodePKToCopy = copyDetail.getFromNodePK();
    NodePK targetPK = copyDetail.getToNodePK();
    String userId = copyDetail.getUserId();
    NodeDetail nodeToCopy = nodeService.getDetail(nodePKToCopy);
    NodeDetail father = getNodeHeader(targetPK);

    // paste topic
    NodePK nodePK = new NodePK("unknown", targetPK);
    NodeDetail node = nodeToCopy.clone();
    node.setNodePK(nodePK);
    node.setCreatorId(userId);
    node.setRightsDependsOn(father.getRightsDependsOn());
    node.setCreationDate(DateUtil.today2SQLDate());
    nodePK = nodeService.createNode(node, father);

    // duplicate rights
    if (copyDetail.isNodeRightsMustBeCopied()) {
      oldAndNewIds.put(Integer.parseInt(nodePKToCopy.getId()), Integer.parseInt(nodePK.getId()));
      if (nodeToCopy.haveRights()) {
        if (nodeToCopy.haveLocalRights()) {
          node.setRightsDependsOn(Integer.parseInt(nodePK.getId()));
        } else {
          int oldRightsDependsOn = nodeToCopy.getRightsDependsOn();
          Integer newRightsDependsOn = oldAndNewIds.get(Integer.valueOf(oldRightsDependsOn));
          node.setRightsDependsOn(newRightsDependsOn);
        }
        nodeService.updateRightsDependency(node);
      }
      // Set topic rights if necessary
      if (nodeToCopy.haveLocalRights()) {
        List<ProfileInst> topicProfiles = adminController
            .getProfilesByObject(nodeToCopy.getNodePK().getId(), ObjectType.NODE.getCode(),
                nodeToCopy.getNodePK().getInstanceId());
        for (ProfileInst nodeToPasteProfile : topicProfiles) {
          if (nodeToPasteProfile != null) {
            ProfileInst nodeProfileInst = (ProfileInst) nodeToPasteProfile.clone();
            nodeProfileInst.setId("-1");
            nodeProfileInst.setComponentFatherId(nodePK.getInstanceId());
            nodeProfileInst.setObjectId(Integer.parseInt(nodePK.getId()));
            nodeProfileInst.setObjectFatherId(father.getId());
            // Add the profile
            adminController.addProfileInst(nodeProfileInst, userId);
          }
        }
      }
    }

    // paste wysiwyg attached to node
    WysiwygController
        .copy(nodePKToCopy.getInstanceId(), "Node_" + nodePKToCopy.getId(), nodePK.getInstanceId(),
            "Node_" + nodePK.getId(), userId);

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

  @SimulationActionProcess(elementLister = KmeliaPublicationSimulationElementLister.class)
  @Action(ActionType.COPY)
  @Override
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
  public PublicationPK copyPublication(@SourcePK PublicationDetail publiToCopy, @TargetPK
      KmeliaCopyDetail copyDetail) {
    NodePK nodePK = copyDetail.getToNodePK();
    String userId = copyDetail.getUserId();
    try {
      ForeignPK toForeignPK = new ForeignPK("unknown", nodePK);
      PublicationPK toPubPK = new PublicationPK("unknown", nodePK);
      String toComponentId = nodePK.getInstanceId();

      // Handle duplication as a creation, ignore initial parameters
      PublicationDetail newPubli = new PublicationDetail();
      newPubli.setPk(toPubPK);
      newPubli.setLanguage(publiToCopy.getLanguage());
      newPubli.setName(publiToCopy.getName());
      newPubli.setDescription(publiToCopy.getDescription());
      newPubli.setKeywords(publiToCopy.getKeywords());
      newPubli.setTranslations(publiToCopy.getClonedTranslations());
      newPubli.setAuthor(publiToCopy.getAuthor());
      newPubli.setCreatorId(userId);
      newPubli.setBeginDate(publiToCopy.getBeginDate());
      newPubli.setBeginHour(publiToCopy.getBeginHour());
      newPubli.setEndDate(publiToCopy.getEndDate());
      newPubli.setEndHour(publiToCopy.getEndHour());
      newPubli.setImportance(publiToCopy.getImportance());
      if (copyDetail.isPublicationContentMustBeCopied()) {
        newPubli.setInfoId(publiToCopy.getInfoId());
      }
      // use validators selected via UI
      newPubli.setTargetValidatorId(copyDetail.getPublicationValidatorIds());

      // manage status explicitly to bypass Draft mode
      if (StringUtil.isDefined(copyDetail.getPublicationStatus())) {
        String profile = getProfile(userId, nodePK);
        if (!copyDetail.getPublicationStatus().equals(PublicationDetail.DRAFT)) {
          if (SilverpeasRole.from(profile).isGreaterThanOrEquals(SilverpeasRole.publisher)) {
            newPubli.setStatus(PublicationDetail.VALID);
          } else {
            // case of writer
            newPubli.setStatus(PublicationDetail.TO_VALIDATE);
          }
        }
      }

      String fromId = publiToCopy.getPK().getId();
      String fromComponentId = publiToCopy.getPK().getInstanceId();
      ForeignPK fromForeignPK = new ForeignPK(publiToCopy.getPK().getId(), fromComponentId);
      PublicationPK fromPubPK = new PublicationPK(publiToCopy.getPK().getId(), fromComponentId);

      if (copyDetail.isAdministrativeOperation()) {
        newPubli.setCreatorId(publiToCopy.getCreatorId());
        newPubli.setCreationDate(publiToCopy.getCreationDate());
        newPubli.setUpdaterId(publiToCopy.getUpdaterId());
        newPubli.setUpdateDate(publiToCopy.getUpdateDate());
        newPubli.setStatus(publiToCopy.getStatus());
      }

      String id = createPublicationIntoTopic(newPubli, nodePK);
      // update id cause new publication is created
      toPubPK.setId(id);
      toForeignPK.setId(id);

      // Copy vignette
      ThumbnailController.copyThumbnail(fromForeignPK, toForeignPK);

      // Copy positions on Pdc
      if (copyDetail.isPublicationPositionsMustBeCopied()) {
        copyPdcPositions(fromPubPK, toPubPK);
      }

      // Copy files
      Map<String, String> fileIds = new HashMap<String, String>();
      if (copyDetail.isPublicationFilesMustBeCopied()) {
        fileIds.putAll(copyFiles(fromPubPK, toPubPK));
      }

      // Copy content
      if (copyDetail.isPublicationContentMustBeCopied()) {
        String xmlFormShortName = newPubli.getInfoId();
        if (xmlFormShortName != null && !"0".equals(xmlFormShortName)) {
          // Content = XMLForm
          // register xmlForm to publication
          PublicationTemplateManager publicationTemplateManager =
              PublicationTemplateManager.getInstance();
          GenericRecordSet toRecordset = publicationTemplateManager
              .addDynamicPublicationTemplate(toComponentId + ":" + xmlFormShortName,
                  xmlFormShortName + ".xml");

          PublicationTemplate pubTemplate = publicationTemplateManager
              .getPublicationTemplate(fromComponentId + ":" + xmlFormShortName);
          RecordSet set = pubTemplate.getRecordSet();

          set.copy(fromForeignPK, toForeignPK, toRecordset.getRecordTemplate(), fileIds);
        } else {
          // paste wysiwyg
          WysiwygController.copy(fromComponentId, fromId, toPubPK.getInstanceId(), id, userId);
        }
      }

      // Index publication to index its files and content
      publicationService.createIndex(toPubPK);

      return newPubli.getPK();
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error("Publication copy failure", ex);
    }
    return null;
  }

  private Map<String, String> copyFiles(PublicationPK fromPK, PublicationPK toPK)
      throws IOException {
    Map<String, String> fileIds = new HashMap<String, String>();
    List<SimpleDocument> origins = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKeyAndType(fromPK, DocumentType.attachment, null);
    for (SimpleDocument origin : origins) {
      SimpleDocumentPK copyPk = AttachmentServiceProvider.getAttachmentService()
          .copyDocument(origin, new ForeignPK(toPK));
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

  public List<KmeliaPublication> filterPublications(List<KmeliaPublication> publications,
      String instanceId, SilverpeasRole profile, String userId) {
    boolean coWriting = isCoWritingEnable(instanceId);
    List<KmeliaPublication> filteredPublications = new ArrayList<KmeliaPublication>();
    for (KmeliaPublication userPub : publications) {
      if (isPublicationVisible(userPub.getDetail(), profile, userId, coWriting)) {
        filteredPublications.add(userPub);
      }
    }
    return filteredPublications;
  }

  public boolean isPublicationVisible(PublicationDetail detail, SilverpeasRole profile,
      String userId) {
    boolean coWriting = isCoWritingEnable(detail.getInstanceId());
    return isPublicationVisible(detail, profile, userId, coWriting);
  }

  @Override
  public void userHaveBeenDeleted(String userId) {
    List<PublicationDetail> publications =
        publicationService.removeUserFromTargetValidators(userId);
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
        if (detail.isVisible()) {
          return true;
        } else {
          if (profile == SilverpeasRole.admin || userId.equals(detail.getUpdaterId()) ||
              (profile != SilverpeasRole.user && coWriting)) {
            return true;
          }
        }
      } else {
        if (detail.isDraft()) {
          // si le theme est en co-rédaction et si on autorise le mode brouillon visible par tous
          // toutes les publications en mode brouillon sont visibles par tous, sauf les lecteurs
          // sinon, seule les publications brouillon de l'utilisateur sont visibles
          if (userId.equals(detail.getCreatorId()) || userId.equals(detail.getUpdaterId()) ||
              (coWriting && isDraftVisibleWithCoWriting() && profile != SilverpeasRole.user)) {
            return true;
          }
        } else {
          // si le thème est en co-rédaction, toutes les publications sont visibles par tous,
          // sauf les lecteurs
          if (profile == SilverpeasRole.admin || profile == SilverpeasRole.publisher ||
              userId.equals(detail.getCreatorId()) || userId.equals(detail.getUpdaterId()) ||
              (profile != SilverpeasRole.user && coWriting)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Gets a business service of dateReminder.
   *
   * @return a DefaultDateReminderService instance.
   */
  private PersistentDateReminderService getDateReminderService() {
    return dateReminderService;
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
    clone.setStatus(PublicationDetail.VALID);
    clone.setCloneId("-1");
    clone.setCloneStatus(null);
    return clone;
  }

  private RecordSet getXMLFormFrom(String infoId, PublicationPK pubPK)
      throws PublicationTemplateException {
    // register xmlForm to publication
    String xmlFormShortName = infoId;

    // get xmlContent to paste
    PublicationTemplateManager publicationTemplateManager = PublicationTemplateManager.
        getInstance();
    PublicationTemplate pubTemplate = publicationTemplateManager.
        getPublicationTemplate(pubPK.getInstanceId() + ":" + xmlFormShortName);

    RecordSet set = pubTemplate.getRecordSet();
    // DataRecord data = set.getRecord(fromId);

    return set;
  }
}
