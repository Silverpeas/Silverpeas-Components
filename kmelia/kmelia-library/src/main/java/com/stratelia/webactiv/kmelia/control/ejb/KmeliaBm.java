/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.kmelia.control.ejb;

import com.silverpeas.ApplicationService;
import com.silverpeas.component.kmelia.KmeliaCopyDetail;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.pdc.model.PdcClassification;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.coordinates.model.Coordinate;
import com.stratelia.webactiv.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.Alias;
import com.stratelia.webactiv.publication.model.CompletePublication;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import com.stratelia.webactiv.statistic.model.HistoryObjectDetail;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.upload.UploadedFile;
import org.silverpeas.util.ForeignPK;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This is the Service interface controller of the MVC. It controls all the activities that happen
 * in a client session. It also provides mechanisms to access other services.
 * @author Nicolas Eysseric
 */
public interface KmeliaBm extends ApplicationService<KmeliaPublication> {

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
   * @see com.stratelia.webactiv.node.model.NodeDetail
   * @see com.stratelia.webactiv.node.model.NodePK
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
   * @see com.stratelia.webactiv.node.model.NodeDetail
   * @see com.stratelia.webactiv.node.model.NodePK
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
   * @see com.stratelia.webactiv.node.model.NodeDetail
   * @see com.stratelia.webactiv.node.model.NodePK
   * @since 1.0
   */
  NodePK updateTopic(NodeDetail topic, String alertType);

  /**
   * Return a subtopic to currentTopic
   * @param nodePK the id of the researched topic
   * @return the detail of the specified topic
   * @see com.stratelia.webactiv.node.model.NodeDetail
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
   * @see com.stratelia.webactiv.node.model.NodeDetail
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
   * @see com.stratelia.webactiv.publication.model.PublicationDetail
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
   * @see com.stratelia.webactiv.node.model.NodeDetail
   * @since 1.0
   */
  Collection<Collection<NodeDetail>> getPathList(PublicationPK pubPK);

  Collection<NodePK> getPublicationFathers(PublicationPK pubPK);

  /**
   * Create a new Publication (only the header - parameters) to the current Topic
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.publication.model.PublicationDetail
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
   * @see com.stratelia.webactiv.publication.model.PublicationDetail
   * @since 1.0
   */
  void updatePublication(PublicationDetail detail);

  void updatePublication(PublicationDetail detail, boolean forceUpdateDate);

  /**
   * Delete a publication If this publication is in the basket or in the DZ, it's deleted from the
   * database Else it only send to the basket
   * @param pubPK the id of the publication to delete
   * @return a TopicDetail
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
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
   * Delete a path of publication
   * @param pubPK the id of the publication
   * @since 1.0
   */
  void deletePublicationFromAllTopics(PublicationPK pubPK);

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  void addInfoLinks(PublicationPK pubPK, List<ForeignPK> links);

  /**
   * Removes links between publications and the specified publication
   * @param pubPK
   * @param links list of links to remove
   * @
   */
  void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links);

  CompletePublication getCompletePublication(PublicationPK pubPK);

  KmeliaPublication getPublication(PublicationPK pubPK);

  TopicDetail getPublicationFather(PublicationPK pubPK, boolean isTreeStructureUsed, String userId,
      boolean isRightsOnTopicsUsed);

  NodePK getPublicationFatherPK(PublicationPK pubPK, boolean isTreeStructureUsed, String userId,
      boolean isRightsOnTopicsUsed);

  Collection<PublicationDetail> getPublicationDetails(List<ForeignPK> links);

  /**
   * gets a list of authorized publications
   * @param links list of publication defined by his id and component id
   * @param userId identifier User. allow to check if the publication is accessible for current
   * user
   * @param isRightsOnTopicsUsed indicates if the right must be checked
   * @return a collection of Kmelia publications
   * @
   * @since 1.0
   */
  Collection<KmeliaPublication> getPublications(List<ForeignPK> links, String userId,
      boolean isRightsOnTopicsUsed);

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

  /**
   * Gets all the publications linked with the specified one.
   * @param publication the publication from which linked publications are get.
   * @return a list of Kmelia publications.
   * @ if an error occurs while communicating with the remote business service.
   */
  List<KmeliaPublication> getLinkedPublications(KmeliaPublication publication);

  List<KmeliaPublication> getPublicationsToValidate(String componentId, String userId);

  boolean isUserCanValidatePublication(PublicationPK pubPK, String userId);

  List<String> getAllValidators(PublicationPK pubPK);

  /**
   * @param pubPK id of the publication to validate. If publication is always visible, clone is
   * processed.
   * @param userId id of the user who validate
   * @param force if true, force to validate publication (bypass pending validations)
   * @return true if the validation process is complete (ie all validators have validate)
   * @
   */
  boolean validatePublication(PublicationPK pubPK, String userId, boolean force);

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

  void movePublication(PublicationPK pubPK, NodePK to, String userId);

  void movePublicationInSameApplication(PublicationPK pubPK, NodePK from, NodePK to, String userId);

  void movePublicationInAnotherApplication(PublicationDetail pub, NodePK to, String userId);

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
  NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK, NodePK topicPK,
      String senderName);

  /**
   * Send an email to alert users of a attachment
   * @param pubPK the publication Id
   */
  NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK,
      SimpleDocumentPK documentPk, NodePK topicPK, String senderName);

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
   * @see com.stratelia.webactiv.publication.model.PublicationDetail
   * @since 1.0
   */
  String createKmaxPublication(PublicationDetail pubDetail);

  /**
   * Delete coordinates of a publication (ie: when publication is deleted)
   * @param coordinatePK
   * @param coordinates
   */
  void deleteCoordinates(CoordinatePK coordinatePK, List<String> coordinates);

  Collection<Alias> getAlias(PublicationPK pubPK);

  void setAlias(PublicationPK pubPK, List<Alias> alias);

  void addUploadedFilesToPublication(PublicationDetail pubDetail,
      Collection<UploadedFile> uploadedFiles);

  void addAttachmentToPublication(PublicationPK pubPK, String userId, String filename,
      String description, byte[] contents);

  boolean importPublication(String componentId, String topicId, String spaceId, String userId,
      Map<String, String> publiParams, Map<String, String> formParams, String language,
      String xmlFormName, String discrimatingParameterName, String userProfile);

  boolean importPublication(String componentId, String topicId, String userId,
      Map<String, String> publiParams, Map<String, String> formParams, String language,
      String xmlFormName, String discriminantParameterName, String userProfile,
      boolean ignoreMissingFormFields);

  boolean importPublication(String publicationId, String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName, String userProfile);

  void importPublications(String componentId, String topicId, String spaceId, String userId,
      List<Map<String, String>> publiParamsList, List<Map<String, String>> formParamsList,
      String language, String xmlFormName, String discrimatingParameterName, String userProfile);

  List<XMLField> getPublicationXmlFields(String publicationId, String componentId, String spaceId,
      String userId);

  List<XMLField> getPublicationXmlFields(String publicationId, String componentId, String spaceId,
      String userId, String language);

  String createTopic(String componentId, String topicId, String spaceId, String userId, String name,
      String description);

  Collection<String> getPublicationsSpecificValues(String componentId, String xmlFormName,
      String fieldName);

  void draftInPublication(String componentId, String xmlFormName, String fieldName,
      String fieldValue);

  void updatePublicationEndDate(String componentId, String spaceId, String userId,
      String xmlFormName, String fieldName, String fieldValue, Date endDate);

  String findPublicationIdBySpecificValue(String componentId, String xmlFormName, String fieldName,
      String fieldValue, String topicId, String spaceId, String userId);

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

  NodeDetail getExpandedPathToNode(NodePK pk, String userId);

  boolean isUserCanWrite(String componentId, String userId);

  boolean isUserCanValidate(String componentId, String userId);

  boolean isUserCanPublish(String componentId, String userId);

  String getUserTopicProfile(NodePK pk, String userId);

  List<String> deletePublications(List<String> ids, NodePK nodePK, String userId);

  List<String> getUserIdsOfFolder(NodePK pk);

  List<HistoryObjectDetail> getLastAccess(PublicationPK pk, NodePK nodePK, String excludedUserId);

  NodeDetail copyNode(KmeliaCopyDetail copyDetail);

  void copyPublications(KmeliaCopyDetail copyDetail);

  PublicationPK copyPublication(PublicationDetail publi, KmeliaCopyDetail copyDetail);

  NodeDetail moveNode(NodePK nodePK, NodePK to, String userId);

  List<KmeliaPublication> filterPublications(List<KmeliaPublication> publications,
      String instanceId, SilverpeasRole profile, String userId);

  boolean isPublicationVisible(PublicationDetail detail, SilverpeasRole profile, String userId);

}