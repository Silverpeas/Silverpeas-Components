/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.components.kmelia.model.ValidatorsList;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
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
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This is the Service interface controller of the MVC. It controls all the activities that happen
 * in a client session. It also provides mechanisms to access other services.
 * @author Nicolas Eysseric
 */
public interface KmeliaService extends ApplicationService {

  static KmeliaService get() {
    return ServiceProvider.getSingleton(KmeliaService.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  Optional<KmeliaPublication> getContributionById(ContributionIdentifier contributionId);

  TopicDetail goTo(NodePK nodePK, String userId, boolean isTreeStructureUsed, String userProfile,
      boolean mustUserRightsBeChecked);

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
   * Delete a topic and all descendants. Delete all links between descendants and publications. Its
   * publications will be visible in the Declassified zone. Delete All subscriptions and favorites
   * on its topics and all descendants
   * @param nodePK the id of the topic to delete
   * @since 1.0
   */
  void deleteTopic(NodePK nodePK);

  void changeTopicStatus(String newStatus, NodePK nodePK, boolean recursiveChanges);

  void sortSubTopics(NodePK fatherPK, boolean recursive, String[] criteria);

  List<NodeDetail> getTreeview(NodePK nodePK, String profile, boolean coWritingEnable,
      boolean draftVisibleWithCoWriting, String userId, boolean displayNb,
      boolean isRightsOnTopicsUsed);

  /**
   * Return the detail of a publication (only the Header)
   * @param pubPK the id of the publication
   * @return a PublicationDetail
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  PublicationDetail getPublicationDetail(PublicationPK pubPK);

  /**
   * Gets the publications linked to the folder represented by given {@link NodePK} reference.
   * <p>
   * In any case, user right access to a publication is verified so that only accessible ones are
   * included into returned list.
   * </p>
   * @param pk the reference to a folder.
   * @param userProfile a user profile
   * @param userId the identifier of the user for which access controls MUST be verified.
   * @param isTreeStructureUsed true if publications are represented into tree structure, false
   * otherwise.
   * @return a list of {@link KmeliaPublication} instances.
   */
  List<KmeliaPublication> getAuthorizedPublicationsOfFolder(NodePK pk, String userProfile,
      String userId, boolean isTreeStructureUsed);

  /**
   * Checks rights on publications order by descending begin visibility date of publication.
   * <p>
   * Due to the nature of this service, it is not designed to by used by update processes and the
   * result is so cached at request scope in order to avoid to perform several times the same
   * request.
   * </p>
   * @param instanceId the identifier of the instance.
   * @param userId the identifier of the user for which access controls MUST be verified.
   * @param limit the maximum number of publications to return (0 = no limit).
   * @return a list of {@link KmeliaPublication} instances.
   */
  List<KmeliaPublication> getLatestAuthorizedPublications(String instanceId, String userId,
      int limit);

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
   * Creates a new publication into the specified topic and with the specified classification on the
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

  /**
   * Update a publication (only the header - parameters)
   * @param detail a PublicationDetail
   * @param classification the classification on the PdC of the publication content.
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  void updatePublication(PublicationDetail detail, PdcClassification classification);

  void updatePublication(PublicationDetail detail, boolean forceUpdateDate);

  /**
   * Delete a publication If this publication is in the basket or in the DZ, it's deleted from the
   * database Else it only send to the basket
   * @param pubPK the id of the publication to delete
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
   * <p>
   * The component instance set to given {@link PublicationPK} gives the priority of the resulting
   * {@link TopicDetail}. For example, into case of a main publication on instance A with aliases on
   * instance B, if component instance id set to given {@link PublicationPK} is the B one, then
   * {@link TopicDetail} result is about the best father PK (the best location) on instance B (so an
   * alias in that case).
   * </p>
   * @param pubPK the unique identifier of the publication.
   * @param isTreeStructureUsed is the tree view of the topics enabled?
   * @param userId the unique identifier of a user.
   * @return the details of the topic in which the publication is accessible by the given user.
   */
  TopicDetail getBestTopicDetailOfPublicationForUser(PublicationPK pubPK,
      boolean isTreeStructureUsed, String userId);

  /**
   * Gets the father of the specified publication according to the rights of the user. If the main
   * location of the publication isn't accessible by the user, then the first accessible alias of
   * the publication is returned. If no aliases are accessible or defined, the the root topic is
   * returned.
   * <p>
   * The component instance set to given {@link PublicationPK} gives the priority of the resulting
   * {@link NodePK}. For example, into case of a main publication on instance A with aliases on
   * instance B, if component instance id set to given {@link PublicationPK} is the B one, then the
   * best father PK (the best location) on instance B is returned (so an alias in that case).
   * </p>
   * @param pubPK the unique identifier of the publication
   * @param userId the unique identifier of a user.
   * @return a topic in which the publication is accessible by the given user.
   */
  NodePK getBestLocationOfPublicationForUser(PublicationPK pubPK, String userId);

  /**
   * gets a list of PublicationDetail corresponding to the links parameter
   * @param references list of publication (componentID + publicationId)
   * @return a list of PublicationDetail
   */
  <T extends ResourceReference> List<PublicationDetail> getPublicationDetails(List<T> references);

  /**
   * Gets a list of publications with optional control access filtering.
   * <p>
   * When a folder is given as context, then the ALIAS information is computed on each publication.
   * </p>
   * @param references list of publication represented as {@link ResourceReference} instances.
   * @param userId identifier User. allow to check if the publication is accessible for current
   * user
   * @param contextFolder optional folder that represents if specified the folder into which the
   * given references are retrieved. It is MANDATORY to determinate alias status.
   * @param accessControlFiltering true to filter the publication according user rights.
   * @return a collection of Kmelia publications
   */
  <T extends ResourceReference> List<KmeliaPublication> getPublications(List<T> references,
      String userId, final NodePK contextFolder, boolean accessControlFiltering);

  /**
   * Gets a list of {@link Pair} of {@link KmeliaPublication} instances into context of modification
   * by a user represented by the given user id. On the left of a {@link Pair} instance, there is a
   * publication that can not be a null value. On the right, there is the clone of the publication
   * if any, and so, it can be null if the publication has got no clone.
   * <p>
   * The main location is computed for each publication (and clone) by taking care about
   * performances.
   * </p>
   * <p>
   * This service guarantees that the returned {@link KmeliaPublication} instances each aims the
   * main location.
   * </p>
   * @param references list of publication represented as {@link ResourceReference} instances.
   * @param userId identifier User. allow to check if the publication is accessible for current
   * user
   * @return a list of {@link Pair} of {@link KmeliaPublication} instances. A pair represents on the
   * left the publication and the eventual corresponding clone on the right if it exists.
   */
  <T extends ResourceReference> List<Pair<KmeliaPublication, KmeliaPublication>> getPublicationsForModification(
      List<T> references, String userId);

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

  ValidatorsList getAllValidators(PublicationPK pubPK);

  void setValidators(PublicationPK pubPK, String userIds);

  /**
   * @param pubPK id of the publication to validate. If publication is always visible, clone is
   * processed.
   * @param userId id of the user who validate
   * @param force if true, force to validate publication (bypass pending validations)
   * @param hasUserNoMoreValidationRight true if the given id represents a user which has no more
   * validation right (deleted user for example)
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

  void draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile,
      boolean forceUpdateDate);

  /**
   * Change publication status from any state to draft
   * @param pubPK the id of the publication
   */
  void draftInPublication(PublicationPK pubPK);

  void draftInPublication(PublicationPK pubPK, String userId);

  void movePublication(PublicationPK pubPK, NodePK to, KmeliaPasteDetail pasteContext);

  void movePublicationInSameApplication(PublicationPK pubPK, NodePK from,
      KmeliaPasteDetail pasteContext);

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
  UserNotification getUserNotification(PublicationPK pubPK, SimpleDocumentPK documentPk,
      NodePK topicPK);

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

  void setModelUsed(String[] models, String instanceId, String nodeId);

  Collection<String> getModelUsed(String instanceId, String nodeId);

  /**
   * Get the axis on which publications are categorized in the specified component instance.
   * @param componentId the unique identifier of a component instance.
   * @return a list of axis as {@link NodeDetail} instances.
   */
  List<NodeDetail> getAxis(String componentId);

  /**
   * Get the header of the axis on which publications are categorized in the specified component
   * instance.
   * @param componentId the unique identifier of a component instance.
   * @return a list of axis headers as {@link NodeDetail} instances.
   */
  List<NodeDetail> getAxisHeaders(String componentId);

  /**
   * Add the given axis into the specified component instance.
   * @param axis the axis to add.
   * @param componentId the unique identifier of the component instance.
   * @return the identifier of the new added axis.
   */
  @SuppressWarnings("UnusedReturnValue")
  NodePK addAxis(NodeDetail axis, String componentId);

  /**
   * Update the given axis in the specified component instance.
   * @param axis the axis from which its counterpart in the data source will be updated.
   * @param componentId the unique identifier of a component instance.
   */
  void updateAxis(NodeDetail axis, String componentId);

  /**
   * Delete the given axis in the specified component instance.
   * @param axisId the unique identifier of the axis to delete.
   * @param componentId the unique identifier of a component instance.
   */
  void deleteAxis(String axisId, String componentId);

  /**
   * Get the header of the specified node.
   * @param id the unique identifier of a node
   * @param componentId the unique identifier of the component instance in which the node is.
   * @return a {@link NodeDetail} instance.
   * @
   */
  NodeDetail getNodeHeader(String id, String componentId);

  /**
   * Add position to an axis in the given component instance and for the specified user.
   * @param fatherId the identifier of the position that will be the father of the new one.
   * @param position the position to add.
   * @param componentId the unique identifier of the component instance.
   * @param userId the unique identifier of a user.
   * @return the identifier of the new position.
   */
  @SuppressWarnings("UnusedReturnValue")
  NodePK addPosition(String fatherId, NodeDetail position, String componentId, String userId);

  /**
   * Update a position in an axis
   * @param position the position from which its counterpart in the data source will be updated.
   * @param componentId the unique identifier of the component instance in which belongs the axis.
   */
  void updatePosition(NodeDetail position, String componentId);

  /**
   * Delete a position in an axis
   * @param positionId the unique identifier of the position to delete.
   * @param componentId the unique identifier of the component instance in which belongs the axis.
   */
  void deletePosition(String positionId, String componentId);

  /**
   * Get path of a position in an axis.
   * @param positionId the unique identifier of a position.
   * @param componentId the unique identifier of the component instance in which belongs the axis.
   * @return the path of the position with a {@link NodeDetail} instance for each path's node.
   */
  Collection<NodeDetail> getPath(String positionId, String componentId);

  /**
   * Get publications categorized in a combination of positions.
   * @param combination a list of positions composing the combination.
   * @param componentId the unique identifier of the component instance in which belongs the
   * combination.
   * @return the publications that satisfy the combination of positions.
   */
  List<KmeliaPublication> search(List<String> combination, String componentId);

  /**
   * Get publications categorized in a combination of positions and that are visible or created the
   * given number of days ago.
   * @param combination a list of positions composing the combination.
   * @param nbDays the number of days before today.
   * @param componentId the unique identifier of the component instance in which belongs the
   * combination.
   * @return the publications that satisfy the combination of positions and the time criteria.
   */
  List<KmeliaPublication> search(List<String> combination, int nbDays, String componentId);

  /**
   * Get publications that aren't categorized on any axis.
   * @param componentId the unique identifier of a component instance.
   * @return the uncategorized publications.
   */
  Collection<KmeliaPublication> getUnbalancedPublications(String componentId);

  void indexKmax(String componentId);

  /**
   * Get the given publication for the given user.
   * @param pubId the unique identifier of a publication.
   * @param currentUserId the unique identifier of the user for whom the publications are asked.
   * @return the publication as a {@link KmeliaPublication} instance.
   */
  @SuppressWarnings("unused")
  KmeliaPublication getKmaxPublication(String pubId, String currentUserId);

  /**
   * Get the coordinates for the given publication on the axis of the specified component instance.
   * @param pubId the unique identifier of a publication.
   * @param componentId the unique identifier of the component instance.
   * @return a collection of coordinates
   */
  Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId);

  /**
   * Add for the given publication a combination of position on the axis of the component instance.
   * @param pubId the unique identifier of a publication.
   * @param combination a list of coordinate identifiers.
   * @param componentId the unique identifier of the component instance.
   */
  void addPublicationToCombination(String pubId, List<String> combination, String componentId);

  /**
   * Remove for the given publication the specified combination of positions on the axis of the
   * component instance.
   * @param pubId the unique identifier of a publication.
   * @param combinationId the unique identifier of a combination of positions.
   * @param componentId the unique identifier of the component instance.
   */
  void deletePublicationFromCombination(String pubId, String combinationId, String componentId);

  /**
   * Create a new publication (only the header, no content) in a Kmax instance.
   * @param pubDetail the publication to create.
   * @return the id of the new publication
   * @see org.silverpeas.core.contribution.publication.model.PublicationDetail
   * @since 1.0
   */
  String createKmaxPublication(PublicationDetail pubDetail);

  /**
   * Gets all the locations of the specified publication; whatever the component instance. If the
   * given publication is a clone, then gets all the locations of the main publication.
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

  @SuppressWarnings("unused")
  void setAliases(PublicationPK pubPK, List<Location> locations);

  void addAttachmentToPublication(PublicationPK pubPK, String userId, String filename,
      String description, byte[] contents);

  String createTopic(String componentId, String topicId, String spaceId, String userId, String name,
      String description);

  /**
   * Clone the given publication. Create new publication based on pubDetail object if not null or
   * CompletePublication otherwise. Original publication will not be modified (except references to
   * clone: cloneId and cloneStatus).
   * @param pubDetail If not null, attribute values are set to the clone
   * @param nextStatus Draft or Clone
   * @return the identifier of the cloned publication.
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

  /**
   * Copies the node according to the information provided by the specified copy descriptor.
   * @param copyDetail a descriptor giving details about the copy to perform like the node to copy
   * and the destination.
   * @return the copy of the node.
   */
  @SuppressWarnings("UnusedReturnValue")
  NodeDetail copyNode(KmeliaCopyDetail copyDetail);

  /**
   * Copies all the publications defined in the specified copy descriptor.
   * @param copyDetail a descriptor providing information about the publications to copy and the
   * destination of the copy.
   */
  void copyPublications(KmeliaCopyDetail copyDetail);

  /**
   * Copies the specified publication according to the given copy descriptor. In the case the
   * publication is an alias, then the copy adds a new location to the original copy. Otherwise the
   * publication is well copied.
   * @param publication the publication to copy.
   * @param copyDetail a descriptor providing details about the copy like the destination.
   * @return the copy of the publication or the publication itself in the case of a new location.
   */
  @SuppressWarnings("UnusedReturnValue")
  PublicationDetail copyPublication(PublicationDetail publication, KmeliaCopyDetail copyDetail);

  void moveNode(NodePK nodePK, NodePK to, KmeliaPasteDetail pasteContext);

  List<KmeliaPublication> filterPublications(List<KmeliaPublication> publications,
      String instanceId, SilverpeasRole profile, String userId);

  void userHaveBeenDeleted(String userId);

  List<String> getActiveValidatorIds(PublicationPK pk);

  /**
   * Performs processes about kmelia linked to given reminder.<br/> If kmelia is not concerned,
   * nothing is performed.
   * @param reminder a {@link Reminder} instance.
   */
  void performReminder(final Reminder reminder);

  void deleteClone(PublicationPK pk);

  List<KmeliaPublication> getNonVisiblePublications(String componentId, String userId);
}