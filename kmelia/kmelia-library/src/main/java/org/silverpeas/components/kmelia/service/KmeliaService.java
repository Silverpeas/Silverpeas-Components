/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

/**
 * This is the Service interface controller of the MVC. It controls all the activities that happen
 * in a client session. It also provides mechanisms to access other services.
 * @author Nicolas Eysseric
 */
public interface KmeliaService extends ApplicationService<KmeliaPublication> {

  static KmeliaService get() {
    return ServiceProvider.getSingleton(KmeliaService.class);
  }

  /**
   * Return the detail of a topic.
   * @param nodePK
   * @param userId
   * @param isTreeStructureUsed
   * @param userProfile
   * @param isRightsOnTopicsUsed
   * @return the detail of a topic.
   * @
   */
  TopicDetail goTo(NodePK nodePK, String userId, boolean isTreeStructureUsed, String userProfile,
      boolean isRightsOnTopicsUsed);

  List<NodeDetail> getAllowedSubfolders(NodeDetail folder, String userId);

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   * @param fatherPK the topic Id of the future father
   * @param subtopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see NodeDetail
   * @see NodePK
   * @since 1.0
   */
  NodePK addToTopic(NodePK fatherPK, NodeDetail subtopic);

  /**
   * Add a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   * @param subtopic the NodeDetail of the new sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see NodeDetail
   * @see NodePK
   * @since 1.0
   */
  NodePK addSubTopic(NodePK fatherPK, NodeDetail subtopic, String alertType);

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
  NodePK updateTopic(NodeDetail topic, String alertType);

  /**
   * Return a subtopic to currentTopic
   * @param nodePK the id of the researched topic
   * @return the detail of the specified topic
   * @see NodeDetail
   * @since 1.0
   */
  NodeDetail getSubTopicDetail(NodePK nodePK);

  /**
   * Delete a topic and all descendants. Delete all links between descendants and publications.
   * This
   * publications will be visible in the Declassified zone. Delete All subscriptions and favorites
   * on this topics and all descendants
   * @param nodePK the id of the topic to delete
   * @since 1.0
   */
  void deleteTopic(NodePK nodePK);

  void changeSubTopicsOrder(String way, NodePK nodePK, NodePK fatherPK);

  void changeTopicStatus(String newStatus, NodePK nodePK, boolean recursiveChanges);

  void sortSubTopics(NodePK fatherPK);

  void sortSubTopics(NodePK fatherPK, boolean recursive, String[] criteria);

  List<NodeDetail> getTreeview(NodePK nodePK, String profile, boolean coWritingEnable,
      boolean draftVisibleWithCoWriting, String userId, boolean displayNb,
      boolean isRightsOnTopicsUsed);

  /**
   * ***********************************************************************************
   */
  /* Interface - Gestion des abonnements */
  /**
   * ***********************************************************************************
   */
  /**
   * Subscriptions - get the subscription list of the current user
   * @return a Path Collection - it's a Collection of NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  Collection<Collection<NodeDetail>> getSubscriptionList(String userId, String componentId);

  /**
   * Subscriptions - remove a subscription to the subscription list of the current user
   * @param topicPK the subscribe topic Id to remove
   * @since 1.0
   */
  void removeSubscriptionToCurrentUser(NodePK topicPK, String userId);

  /**
   * Subscriptions - add a subscription
   * @param topicPK the subscription topic Id to add
   * @since 1.0
   */
  void addSubscription(NodePK topicPK, String userId);

  boolean checkSubscription(NodePK topicPK, String userId);

  /**
   * Return the detail of a publication (only the Header)
   * @param pubPK the id of the publication
   * @return a PublicationDetail
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  PublicationDetail getPublicationDetail(PublicationPK pubPK);

  List<KmeliaPublication> getPublicationsOfFolder(NodePK pk, String userProfile, String userId,
      boolean isTreeStructureUsed, boolean isRightsOnTopicsUsed);

  List<KmeliaPublication> getLatestPublications(String instanceId, int nbPublisOnRoot,
      boolean isRightsOnTopicsUsed, String userId);

  /**
   * Return list of all path to this publication - it's a Collection of NodeDetail collection
   * @param pubPK the id of the publication
   * @return a Collection of NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  Collection<Collection<NodeDetail>> getPathList(PublicationPK pubPK);

  /**
   * Gets the father of the specified publication. If the publication is a clone of a main one, then
   * gets the father of the cloned publication. The father returned should be the main location of
   * the publication. It the publication is an orphaned one, null is returned.
   * @param pubPK the identifying key of the publication.
   * @return the father of the publication or null if the publication is an orphaned one.
   */
  NodePK getPublicationFatherPK(PublicationPK pubPK);

  /**
   * Create a new Publication (only the header - parameters) to the current Topic
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK);

  /**
   * Creates a new publication into the specified topic and with the specified classification on
   * the
   * PdC.
   * @param pubDetail the detail about the publication to create.
   * @param fatherPK the unique identifier of the topic into which the publication is published.
   * @param classification the classification on the PdC of the publication content.
   * @return the unique identifier of the created publication.
   * @ if an error occurs while communicating with the remote business logic.
   */
  String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK,
      PdcClassification classification);

  /**
   * Update a publication (only the header - parameters)
   * @param detail a PublicationDetail
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  void updatePublication(PublicationDetail detail);

  void updatePublication(PublicationDetail detail, boolean forceUpdateDate);

  /**
   * Delete a publication If this publication is in the basket or in the DZ, it's deleted from the
   * database Else it only send to the basket
   * @param pubPK the id of the publication to delete
   * @return a TopicDetail
   * @see TopicDetail
   * @since 1.0
   */
  void deletePublication(PublicationPK pubPK);

  void sendPublicationToBasket(PublicationPK pubPK);

  void sendPublicationToBasket(PublicationPK pubPK, boolean kmaxMode);

  /**
   * Add a publication to a topic and send email alerts to topic subscribers
   * @param pubPK the id of the publication
   * @param fatherPK the id of the topic
   * @since 1.0
   */
  void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK, boolean isACreation);

  void addPublicationToTopicWithoutNotifications(PublicationPK pubPK, NodePK fatherPK,
      boolean isACreation);

  /**
   * Delete a path between publication and topic
   * @param pubPK the id of the publication
   * @param fatherPK the id of the topic
   * @since 1.0
   */
  void deletePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK);

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  void addInfoLinks(PublicationPK pubPK, List<ResourceReference> links);

  /**
   * Gets the complete details about the publication referred by the specified unique identifier.
   * @param pubPK the unique identifier of a Kmelia publication.
   * @return a {@link CompletePublication} object.
   */
  CompletePublication getCompletePublication(PublicationPK pubPK);

  /**
   * Gets the Kmelia publication identified by the specified identifying key and that is located
   * into the specified topic. As a Kmelia publication can be in different locations, all
   * publications other than the original father of the publication are considered as an alias of
   * that original publication. This is why it is required to know the father of the asked
   * publication.
   * @param pubPK identifier of the publication to get.
   * @param topicPK identifier of the topic in which the publication is located.
   * @return the asked {@link KmeliaPublication} instance.
   */
  KmeliaPublication getPublication(PublicationPK pubPK, NodePK topicPK);

  /**
   * Gets the details about the father from which the specified publication is accessible to the
   * given user. If the main location of the publication isn't accessible by the user, then the
   * first accessible alias of the publication is returned. If no aliases are accessible or defined,
   * the the details of the root topic is returned.
   * @param pubPK the unique identifier of the publication.
   * @param isTreeStructureUsed is the tree view of the topics enabled?
   * @param userId the unique identifier of a user.
   * @param isRightsOnTopicsUsed is the rights on the topics enabled in the component instance
   * in which is defined the publication?
   * @return the details of the topic in which the publication is accessible by the given user.
   */
  TopicDetail getPublicationFather(PublicationPK pubPK, boolean isTreeStructureUsed, String userId,
      boolean isRightsOnTopicsUsed);

  /**
   * Gets the father of the specified publication according to the rights of the user. If the main
   * location of the publication isn't accessible by the user, then the first accessible alias of
   * the publication is returned. If no aliases are accessible or defined, the the root topic is
   * returned.
   * @param pubPK the unique identifier of the publication
   * @param isTreeStructureUsed is the tree view of the topics is used?
   * @param userId the unique identifier of a user.
   * @param isRightsOnTopicsUsed is the rights on the topics enabled in the component instance
   * in which is defined the publication?
   * @return a topic in which the publication is accessible by the given user.
   */
  NodePK getPublicationFatherPK(PublicationPK pubPK, boolean isTreeStructureUsed, String userId,
      boolean isRightsOnTopicsUsed);

  /**
   * gets a list of PublicationDetail corresponding to the links parameter
   * @param links list of publication (componentID + publicationId)
   * @return a list of PublicationDetail
   */
  List<PublicationDetail> getPublicationDetails(List<ResourceReference> links);

  /**
   * Gets a list of publications with optional control access filtering
   * @param links list of publication defined by his id and component id
   * @param userId identifier User. allow to check if the publication is accessible for current
   * user
   * @param accessControlFiltering true to filter the publication according user rights.
   * @return a collection of Kmelia publications
   */
  List<KmeliaPublication> getPublications(List<ResourceReference> links, String userId,
      boolean accessControlFiltering);

  /**
   * Gets the publications linked with the specified one and for which the specified user is
   * authorized to access.
   * @param publication the publication from which linked publications are get.
   * @param userId the unique identifier of a user. It allows to check if a linked publication is
   * accessible for the specified user.
   * @return a list of Kmelia publications.
   * @ if an error occurs while communicating with the remote business service.
   */
  List<KmeliaPublication> getLinkedPublications(KmeliaPublication publication, String userId);

  List<KmeliaPublication> getPublicationsToValidate(String componentId, String userId);

  boolean isUserCanValidatePublication(PublicationPK pubPK, String userId);

  List<String> getAllValidators(PublicationPK pubPK);

  void setValidators(PublicationPK pubPK, String userIds);

  /**
   * @param pubPK id of the publication to validate. If publication is always visible, clone is
   * processed.
   * @param userId id of the user who validate
   * @param force if true, force to validate publication (bypass pending validations)
   * @param hasUserNoMoreValidationRight true if the given id represents a user which has no more
   *        validation right (deleted user for example)
   * @return true if the validation process is complete (ie all validators have validate)
   * @
   */
  boolean validatePublication(PublicationPK pubPK, String userId, boolean force,
      final boolean hasUserNoMoreValidationRight);

  void unvalidatePublication(PublicationPK pubPK, String userId, String refusalMotive,
      int validationType);

  void suspendPublication(PublicationPK pubPK, String refusalMotive, String userId);

  /**
   * Change publication status from draft to valid (for publisher) or toValidate (for redactor)
   * @param pubPK the id of the publication
   */
  void draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile);

  PublicationDetail draftOutPublicationWithoutNotifications(PublicationPK pubPK, NodePK topicPK,
      String userProfile);

  PublicationDetail draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile,
      boolean forceUpdateDate);

  /**
   * Change publication status from any state to draft
   * @param pubPK the id of the publication
   */
  void draftInPublication(PublicationPK pubPK);

  void draftInPublication(PublicationPK pubPK, String userId);

  void movePublication(PublicationPK pubPK, NodePK to, KmeliaPasteDetail pasteContext);

  void movePublicationInSameApplication(PublicationPK pubPK, NodePK from, KmeliaPasteDetail pasteContext);

  /**
   * alert that an external elements of publication (wysiwyg, attachment, versioning) has been
   * created, updated or removed
   * @param pubPK - id of the publication which contains this external elements
   * @
   */
  void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId);

  void sendModificationAlert(int modificationScope, PublicationPK pubPK);

  /**
   * Send an email to alert users of a publication creation
   * @param pubPK the publication Id
   */
  UserNotification getUserNotification(PublicationPK pubPK, NodePK topicPK);

  /**
   * Send an email to alert users of a attachment
   * @param pubPK the publication Id
   */
  UserNotification getUserNotification(PublicationPK pubPK,
      SimpleDocumentPK documentPk, NodePK topicPK);

  /**
   * Send a notification to alert users about a folder
   * @param pk the folder id
   */
  UserNotification getUserNotification(NodePK pk);

  /**
   * delete reading controls to a publication
   * @param pubPK the id of a publication
   * @since 1.0
   */
  void deleteAllReadingControlsByPublication(PublicationPK pubPK);

  void indexKmelia(String componentId);

  int getSilverObjectId(PublicationPK pubPK);

  void deleteSilverContent(PublicationPK pubPK);

  String getWysiwyg(PublicationPK pubPK, String language);

  void setModelUsed(String[] models, String instanceId, String nodeId);

  Collection<String> getModelUsed(String instanceId, String nodeId);

  /**
   * Get list of Axis
   * @param componentId
   * @return List of Axis
   * @
   */
  List<NodeDetail> getAxis(String componentId);

  /**
   * Get list of Axis Headers
   * @param componentId
   * @return List of Axis Headers
   * @
   */
  List<NodeDetail> getAxisHeaders(String componentId);

  /**
   * Add an axis
   * @param axis
   * @param componentId
   * @return
   * @
   */
  NodePK addAxis(NodeDetail axis, String componentId);

  /**
   * Update an axis
   * @param axis
   * @param componentId
   * @
   */
  void updateAxis(NodeDetail axis, String componentId);

  /**
   * Delete axis
   * @param axisId
   * @param componentId
   * @
   */
  void deleteAxis(String axisId, String componentId);

  /**
   * Get Node Header
   * @param id
   * @param componentId
   * @return NodeDetail
   * @
   */
  NodeDetail getNodeHeader(String id, String componentId);

  /**
   * Add position to a axis
   * @param fatherId
   * @param position
   * @param componentId
   * @param userId
   * @return NodePK
   * @
   */
  NodePK addPosition(String fatherId, NodeDetail position, String componentId, String userId);

  /**
   * Update a position in an axis
   * @param position
   * @param componentId
   * @
   */
  void updatePosition(NodeDetail position, String componentId);

  /**
   * Delete a position in an axis
   * @param positionId
   * @param componentId
   * @
   */
  void deletePosition(String positionId, String componentId);

  /**
   * Get path from a position
   * @param positionId
   * @param componentId
   * @return
   * @
   */
  Collection<NodeDetail> getPath(String positionId, String componentId);

  /**
   * Get publications in a combination
   * @param combination
   * @param componentId
   * @return Collection of publication
   * @
   */
  List<KmeliaPublication> search(List<String> combination, String componentId);

  /**
   * Get publications in a combination with time criteria
   * @param combination
   * @param componentId
   * @return Collection of publication
   * @
   */
  List<KmeliaPublication> search(List<String> combination, int nbDays, String componentId);

  /**
   * Get publications with no classement
   * @param componentId
   * @return Collection of publication
   * @
   */
  Collection<KmeliaPublication> getUnbalancedPublications(String componentId);

  void indexKmax(String componentId);

  /**
   * Get a publication of a user
   * @param pubId , pubId
   * @return a Kmelia publication
   * @
   */
  KmeliaPublication getKmaxPublication(String pubId, String currentUserId);

  /**
   * Get Collection of coordinates for a publication
   * @param pubId , componentId
   * @return a collection of coordinates
   * @
   */
  Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId);

  /**
   * Add a combination for this publication
   * @param pubId , combination, componentId
   * @return
   * @
   */
  void addPublicationToCombination(String pubId, List<String> combination, String componentId);

  /**
   * Remove a combination for this publication
   * @param pubId , combinationId, componentId
   * @return
   * @
   */
  void deletePublicationFromCombination(String pubId, String combinationId, String componentId);

  /**
   * Create a new Publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  String createKmaxPublication(PublicationDetail pubDetail);

  /**
   * Gets all the locations of the specified publication; whatever the component instance.
   * If the given publication is a clone, then gets all the locations of the main
   * publication.
   * @param pubPK the unique identifier of the publication.
   * @return a collection of the locations of the given publication.
   */
  Collection<Location> getLocations(PublicationPK pubPK);

  /**
   * Gets all the aliases of the specified publication, whatever the component instance and without
   * taking into account the publication is a clone or not. If the publication is a clone, then
   * nothing will be returned.
   * @param pubPK the unique identifier of the publication.
   * @return a collection of locations that are all the aliases for the given publication.
   */
  Collection<Location> getAliases(PublicationPK pubPK);

  void setAliases(PublicationPK pubPK, List<Location> locations);

  void addAttachmentToPublication(PublicationPK pubPK, String userId, String filename,
      String description, byte[] contents);

  String createTopic(String componentId, String topicId, String spaceId, String userId, String name,
      String description);

  void doAutomaticDraftOut();

  /**
   * Clone CompletePublication. Create new publication based on pubDetail object if not null or
   * CompletePublication otherwise. Original publication will not be modified (except references to
   * clone : cloneId and cloneStatus).
   * @param pubDetail If not null, attribute values are set to the clone
   * @param nextStatus Draft or Clone
   * @return
   */
  String clonePublication(CompletePublication refPubComplete, PublicationDetail pubDetail,
      String nextStatus);

  void removeContentOfPublication(PublicationPK pubPK);

  NodeDetail getRoot(String componentId, String userId);

  Collection<NodeDetail> getFolderChildren(NodePK nodePK, String userId);

  /**
   * Gets the details about the specified folder. The difference with
   * {@link KmeliaService#getNodeHeader(String, String)} is that the children are also set as well
   * as other information like the number of publications.
   * @param nodePK the unique identifier of the folder.
   * @param userId the unique identifier of the user for which the folder is asked.
   * @return the {@link NodeDetail} instance corresponding to the folder.
   */
  NodeDetail getFolder(NodePK nodePK, String userId);

  NodeDetail getExpandedPathToNode(NodePK pk, String userId);

  boolean isUserCanWrite(String componentId, String userId);

  boolean isUserCanValidate(String componentId, String userId);

  boolean isUserCanPublish(String componentId, String userId);

  String getUserTopicProfile(NodePK pk, String userId);

  List<String> deletePublications(List<String> ids, NodePK nodePK, String userId);

  List<String> getUserIdsOfFolder(NodePK pk);

  List<HistoryObjectDetail> getLastAccess(PublicationPK pk, NodePK nodePK, String excludedUserId,
      final int maxResult);

  NodeDetail copyNode(KmeliaCopyDetail copyDetail);

  void copyPublications(KmeliaCopyDetail copyDetail);

  PublicationPK copyPublication(PublicationDetail publi, KmeliaCopyDetail copyDetail);

  NodeDetail moveNode(NodePK nodePK, NodePK to, KmeliaPasteDetail pasteContext);

  List<KmeliaPublication> filterPublications(List<KmeliaPublication> publications,
      String instanceId, SilverpeasRole profile, String userId);

  boolean isPublicationVisible(PublicationDetail detail, SilverpeasRole profile, String userId);

  void userHaveBeenDeleted(String userId);

  List<String> getActiveValidatorIds(PublicationPK pk);

  /**
   * Performs processes about kmelia linked to given reminder.<br/>
   * If kmelia is not concerned, nothing is performed.
   * @param reminder a {@link Reminder} instance.
   */
  void performReminder(final Reminder reminder);
}