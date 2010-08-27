/**
 * 
 */
package com.stratelia.webactiv.kmelia.control.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.silverpeas.pdc.ejb.PdcBm;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.webactiv.kmelia.model.FullPublication;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.kmelia.model.UserCompletePublication;
import com.stratelia.webactiv.kmelia.model.UserPublication;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * @author sfariello
 *
 */
public interface KmeliaBmBusinessSkeleton {
  /**************************************************************************************/
  /* Interface - Gestion des thèmes */
  /**************************************************************************************/
  /**
   * Return a the detail of a topic
   * @param id the id of the topic
   * @return a TopicDetail
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   * @since 1.0
   */
  public TopicDetail goTo(NodePK nodePK, String userId,
      boolean isTreeStructureUsed, String userProfile,
      boolean isRightsOnTopicsUsed) throws RemoteException;

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   * @param fatherId the topic Id of the future father
   * @param subTopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public NodePK addToTopic(NodePK fatherPK, NodeDetail subtopic)
      throws RemoteException;

  /**
   * Add a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   * @param subTopic the NodeDetail of the new sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public NodePK addSubTopic(NodePK fatherPK, NodeDetail subtopic,
      String alertType) throws RemoteException;

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   * @param topic the NodeDetail of the updated sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public NodePK updateTopic(NodeDetail topic, String alertType)
      throws RemoteException;

  /**
   * Return a subtopic to currentTopic
   * @param subTopicId the id of the researched topic
   * @return the detail of the specified topic
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public NodeDetail getSubTopicDetail(NodePK nodePK) throws RemoteException;

  /**
   * Delete a topic and all descendants. Delete all links between descendants and publications. This
   * publications will be visible in the Declassified zone. Delete All subscriptions and favorites
   * on this topics and all descendants
   * @param topicId the id of the topic to delete
   * @since 1.0
   */
  public void deleteTopic(NodePK nodePK) throws RemoteException;

  public void changeSubTopicsOrder(String way, NodePK nodePK, NodePK fatherPK)
      throws RemoteException;

  public void changeTopicStatus(String newStatus, NodePK nodePK,
      boolean recursiveChanges) throws RemoteException;

  public void sortSubTopics(NodePK fatherPK) throws RemoteException;

  public void sortSubTopics(NodePK fatherPK, boolean recursive,
      String[] criteria) throws RemoteException;

  public List<NodeDetail> getTreeview(NodePK nodePK, String profile,
      boolean coWritingEnable, boolean draftVisibleWithCoWriting,
      String userId, boolean displayNb, boolean isRightsOnTopicsUsed)
      throws RemoteException;

  /**************************************************************************************/
  /* Interface - Gestion des abonnements */
  /**************************************************************************************/
  /**
   * Subscriptions - get the subscription list of the current user
   * @return a Path Collection - it's a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection<Collection<NodeDetail>> getSubscriptionList(String userId, String componentId)
      throws RemoteException;

  /**
   * Subscriptions - remove a subscription to the subscription list of the current user
   * @param topicId the subscribe topic Id to remove
   * @since 1.0
   */
  public void removeSubscriptionToCurrentUser(NodePK topicPK, String userId)
      throws RemoteException;

  /**
   * Subscriptions - add a subscription
   * @param topicId the subscription topic Id to add
   * @since 1.0
   */
  public void addSubscription(NodePK topicPK, String userId)
      throws RemoteException;

  public boolean checkSubscription(NodePK topicPK, String userId) throws RemoteException;

  /**************************************************************************************/
  /* Interface - Gestion des publications */
  /**************************************************************************************/
  /**
   * Return the detail of a publication (only the Header)
   * @param pubId the id of the publication
   * @return a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail(PublicationPK pubPK)
      throws RemoteException;

  /**
   * Return list of all path to this publication - it's a Collection of NodeDetail collection
   * @param pubId the id of the publication
   * @return a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection<Collection<NodeDetail>> getPathList(PublicationPK pubPK) throws RemoteException;

  public Collection<NodePK> getPublicationFathers(PublicationPK pubPK)
      throws RemoteException;

  /**
   * Create a new Publication (only the header - parameters) to the current Topic
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public String createPublicationIntoTopic(PublicationDetail pubDetail,
      NodePK fatherPK) throws RemoteException;

  /**
   * Update a publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public void updatePublication(PublicationDetail detail)
      throws RemoteException;

  public void updatePublication(PublicationDetail detail, boolean forceUpdateDate)
      throws RemoteException;

  /**
   * Delete a publication If this publication is in the basket or in the DZ, it's deleted from the
   * database Else it only send to the basket
   * @param pubId the id of the publication to delete
   * @return a TopicDetail
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   * @since 1.0
   */
  public void deletePublication(PublicationPK pubPK) throws RemoteException;

  public void deletePublicationImage(PublicationPK pubPK)
      throws RemoteException;

  public void sendPublicationToBasket(PublicationPK pubPK)
      throws RemoteException;

  public void sendPublicationToBasket(PublicationPK pubPK, boolean kmaxMode)
      throws RemoteException;

  /**
   * Add a publication to a topic and send email alerts to topic subscribers
   * @param pubId the id of the publication
   * @param fatherId the id of the topic
   * @since 1.0
   */
  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK,
      boolean isACreation) throws RemoteException;

  /**
   * Delete a path between publication and topic
   * @param pubId the id of the publication
   * @param fatherId the id of the topic
   * @since 1.0
   */
  public void deletePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException;

  /**
   * Delete a path of publication
   * @param pubId the id of the publication
   * @since 1.0
   */
  public void deletePublicationFromAllTopics(PublicationPK pubPK)
      throws RemoteException;

  /**
   * get all available models
   * @return a Collection of ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @since 1.0
   */
  public Collection<ModelDetail> getAllModels() throws RemoteException;

  /**
   * Return the detail of a model
   * @param modelId the id of the model
   * @return a ModelDetail
   * @see com.stratelia.webactiv.util.publication.Info.model.ModelDetail
   * @since 1.0
   */
  public ModelDetail getModelDetail(String modelId) throws RemoteException;

  /**
   * Create info attached to a publication
   * @param pubId the id of the publication
   * @param modelId the id of the selected model
   * @param infos an InfoDetail containing info
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public void createInfoDetail(PublicationPK pubPK, String modelId,
      InfoDetail infos) throws RemoteException;

  /**
   * Create model info attached to a publication
   * @param pubId the id of the publication
   * @param modelId the id of the selected model
   * @param infos an InfoDetail containing info
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public void createInfoModelDetail(PublicationPK pubPK, String modelId,
      InfoDetail infos) throws RemoteException;

  /**
   * get info attached to a publication
   * @param pubId the id of the publication
   * @return an InfoDetail
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public InfoDetail getInfoDetail(PublicationPK pubPK) throws RemoteException;

  /**
   * Update info attached to a publication
   * @param pubId the id of the publication
   * @param infos an InfoDetail containing info to updated
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public void updateInfoDetail(PublicationPK pubPK, InfoDetail infos)
      throws RemoteException;

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  public void addInfoLinks(PublicationPK pubPK, List<ForeignPK> links) throws RemoteException;

  /**
   * Removes links between publications and the specified publication
   * @param pubPK
   * @param links list of links to remove
   * @throws RemoteException
   */
  public void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links)
      throws RemoteException;

  /**
   * Return all info of a publication and add a reading statistic
   * @param pubId the id of a publication
   * @return a CompletePublication
   * @see com.stratelia.webactiv.util.publication.model.CompletePublication
   * @since 1.0
   */
  public UserCompletePublication getUserCompletePublication(
      PublicationPK pubPK, String userId) throws RemoteException;

  public CompletePublication getCompletePublication(PublicationPK pubPK)
      throws RemoteException;

  public FullPublication getFullPublication(PublicationPK pubPK)
      throws RemoteException;

  public TopicDetail getPublicationFather(PublicationPK pubPK,
      boolean isTreeStructureUsed, String userId, boolean isRightsOnTopicsUsed)
      throws RemoteException;

  public Collection<PublicationDetail> getPublicationDetails(List<ForeignPK> links)
      throws RemoteException;

  /**
   * gets a list of authorized publications
   * @param links list of publication defined by his id and component id
   * @param userId identifier User. allow to check if the publication is accessible for current user
   * @param isRightsOnTopicsUsed indicates if the right must be checked
   * @return a collection of UserPublication
   * @throws RemoteException
   * @since 1.0
   */
  public Collection<UserPublication> getPublications(List<ForeignPK> links,
      String userId,
      boolean isRightsOnTopicsUsed) throws RemoteException;

  public List<UserPublication> getPublicationsToValidate(String componentId)
      throws RemoteException;

  public List<String> getAllValidators(PublicationPK pubPK, int validationType)
      throws RemoteException;

  public boolean validatePublication(PublicationPK pubPK, String userId,
      int validationType, boolean force) throws RemoteException;

  public void unvalidatePublication(PublicationPK pubPK, String userId,
      String refusalMotive, int validationType) throws RemoteException;

  public void suspendPublication(PublicationPK pubPK, String refusalMotive,
      String userId) throws RemoteException;

  /**
   * Change publication status from draft to valid (for publisher) or toValidate (for redactor)
   * @param publicationId the id of the publication
   * @since 3.0
   */
  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile) throws RemoteException;

  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile, boolean forceUpdateDate) throws RemoteException;

  /**
   * Change publication status from any state to draft
   * @param publicationId the id of the publication
   * @since 3.0
   */
  public void draftInPublication(PublicationPK pubPK) throws RemoteException;

  /**
   * alert that an external elements of publication (wysiwyg, attachment, versioning) has been
   * created, updated or removed
   * @param pubId - id of the publication which contains this external elements
   * @throws RemoteException
   */
  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK)
      throws RemoteException;

  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK,
      String userId) throws RemoteException;

  public PublicationBm getPublicationBm() throws RemoteException;

  public NodeBm getNodeBm() throws RemoteException;

  public PdcBm getPdcBm() throws RemoteException;

  public void sendModificationAlert(int modificationScope, PublicationPK pubPK)
      throws RemoteException;

  /*************************************************************/
  /** SCO - 26/12/2002 Integration AlertUser et AlertUserPeas **/
  /*************************************************************/
  /**
   * Send an email to alert users of a publication creation
   * @param pubId the publication Id
   */
  public NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK,
      NodePK topicPK, String senderName) throws RemoteException;

  /**************************************************************************************/
  /* Interface - Controle de lecture */
  /**************************************************************************************/
  /**
   * get reading control states to a publication
   * @param pubId the id of a publication
   * @return a Collection (Actor, reading date, nb)
   * @since 1.0
   */
  // public Collection getReadingStates(PublicationPK pubPK) throws
  // RemoteException;

  /**
   * delete reading controls to a publication
   * @param pubId the id of a publication
   * @since 1.0
   */
  public void deleteAllReadingControlsByPublication(PublicationPK pubPK)
      throws RemoteException;

  public void indexKmelia(String componentId) throws RemoteException;

  public int getSilverObjectId(PublicationPK pubPK) throws RemoteException;

  public void deleteSilverContent(PublicationPK pubPK) throws RemoteException;

  /**************************************************************************************/
  /* Interface - Fichiers joints */
  /**************************************************************************************/

  public Collection<AttachmentDetail> getAttachments(PublicationPK pubPK) throws RemoteException;

  public String getWysiwyg(PublicationPK pubPK) throws RemoteException;

  public void addModelUsed(String[] models, String instanceId, String nodeId)
      throws RemoteException;

  public Collection<String> getModelUsed(String instanceId, String nodeId) throws RemoteException;

  /**************************************************************************************/
  /**************************************************************************************/
  /**************************************************************************************/
  /* Specific Kmax methods */
  /**************************************************************************************/
  /**************************************************************************************/
  /* Interface - Axis */
  /**************************************************************************************/
  /**
   * Get list of Axis
   * @param componentId
   * @return List of Axis
   * @throws RemoteException
   */
  public List<NodeDetail> getAxis(String componentId) throws RemoteException;

  /**
   * Get list of Axis Headers
   * @param componentId
   * @return List of Axis Headers
   * @throws RemoteException
   */
  public List<NodeDetail> getAxisHeaders(String componentId) throws RemoteException;

  /**
   * Add an axis
   * @param axis
   * @param componentId
   * @return
   * @throws RemoteException
   */
  public NodePK addAxis(NodeDetail axis, String componentId)
      throws RemoteException;

  /**
   * Update an axis
   * @param axis
   * @param componentId
   * @throws RemoteException
   */
  public void updateAxis(NodeDetail axis, String componentId)
      throws RemoteException;

  /**
   * Delete axis
   * @param axisId
   * @param componentId
   * @throws RemoteException
   */
  public void deleteAxis(String axisId, String componentId)
      throws RemoteException;

  /**
   * Get Node Header
   * @param id
   * @param componentId
   * @return NodeDetail
   * @throws RemoteException
   */
  public NodeDetail getNodeHeader(String id, String componentId)
      throws RemoteException;

  /**
   * Add position to a axis
   * @param fatherId
   * @param position
   * @param componentId
   * @param userId
   * @return NodePK
   * @throws RemoteException
   */
  public NodePK addPosition(String fatherId, NodeDetail position,
      String componentId, String userId) throws RemoteException;

  /**
   * Update a position in an axis
   * @param position
   * @param componentId
   * @throws RemoteException
   */
  public void updatePosition(NodeDetail position, String componentId)
      throws RemoteException;

  /**
   * Delete a position in an axis
   * @param positionId
   * @param componentId
   * @throws RemoteException
   */
  public void deletePosition(String positionId, String componentId)
      throws RemoteException;

  /**
   * Get path from a position
   * @param positionId
   * @param componentId
   * @return
   * @throws RemoteException
   */
  public Collection<NodeDetail> getPath(String positionId, String componentId)
      throws RemoteException;

  /**************************************************************************************/
  /* Interface - Recherche */
  /**************************************************************************************/

  /**
   * Get publications in a combination
   * @param combination
   * @param componentId
   * @return Collection of publication
   * @throws RemoteException
   */
  public Collection<UserPublication> search(List<String> combination, String componentId)
      throws RemoteException;

  /**
   * Get publications in a combination with time criteria
   * @param combination
   * @param componentId
   * @return Collection of publication
   * @throws RemoteException
   */
  public Collection<UserPublication> search(List<String> combination, int nbDays, String componentId)
      throws RemoteException;

  /**
   * Get publications with no classement
   * @param componentId
   * @return Collection of publication
   * @throws RemoteException
   */
  public Collection<UserPublication> getUnbalancedPublications(String componentId)
      throws RemoteException;

  /**************************************************************************************/
  /* Interface - Indexation */
  /**************************************************************************************/
  public void indexKmax(String componentId) throws RemoteException;

  /**************************************************************************************/
  /* Interface - Publications */
  /**************************************************************************************/

  /**
   * Get complete publication of a user
   * @param componentId , pubId
   * @return UserCompletePublication
   * @throws RemoteException
   */
  public UserCompletePublication getKmaxCompletePublication(String pubId,
      String currentUserId) throws RemoteException;

  /**
   * Get Collection of coordinates for a publication
   * @param pubId , componentId
   * @return UserCompletePublication
   * @throws RemoteException
   */
  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId)
      throws RemoteException;

  /**
   * Add a combination for this publication
   * @param pubId , combination, componentId
   * @return
   * @throws RemoteException
   */
  public void addPublicationToCombination(String pubId, List<String> combination,
      String componentId) throws RemoteException;

  /**
   * Remove a combination for this publication
   * @param pubId , combinationId, componentId
   * @return
   * @throws RemoteException
   */
  public void deletePublicationFromCombination(String pubId,
      String combinationId, String componentId) throws RemoteException;

  /**
   * Create a new Publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public String createKmaxPublication(PublicationDetail pubDetail)
      throws RemoteException;

  /**
   * Delete coordinates of a publication (ie: when publication is deleted)
   * @param coordinatePK
   * @param coordinates
   */
  public void deleteCoordinates(CoordinatePK coordinatePK, ArrayList coordinates)
      throws RemoteException;

  public Collection<Alias> getAlias(PublicationPK pubPK) throws RemoteException;

  public void setAlias(PublicationPK pubPK, List<Alias> alias) throws RemoteException;

  public void addAttachmentToPublication(PublicationPK pubPK, String userId,
      String filename, String description, byte[] contents)
      throws RemoteException;

  public boolean importPublication(String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams,
      String language, String xmlFormName, String discrimatingParameterName,
      String userProfile) throws RemoteException;

  public void importPublications(String componentId, String topicId,
      String spaceId, String userId, List<Map<String, String>> publiParamsList,
      List<Map<String, String>> formParamsList, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile)
      throws RemoteException;

  public List getPublicationXmlFields(String publicationId, String componentId,
      String spaceId, String userId) throws RemoteException;

  public String createTopic(String componentId, String topicId, String spaceId,
      String userId, String name, String description) throws RemoteException;

  public void importAttachment(String publicationId, String componentId,
      String userId, String filePath, String title, String info,
      Date creationDate) throws RemoteException;

  public void importAttachment(String publicationId, String componentId,
      String userId, String filePath, String title, String info,
      Date creationDate, String logicalName) throws RemoteException;

  public void deleteAttachment(AttachmentDetail attachmentDetail)
      throws RemoteException;

  public Collection<String> getPublicationsSpecificValues(String componentId,
      String xmlFormName, String fieldName) throws RemoteException;

  public void draftInPublication(String componentId, String xmlFormName,
      String fieldName, String fieldValue) throws RemoteException;

  public void updatePublicationEndDate(String componentId, String spaceId,
      String userId, String xmlFormName, String fieldName, String fieldValue,
      Date endDate) throws RemoteException;

  public String findPublicationIdBySpecificValue(String componentId, String xmlFormName,
      String fieldName, String fieldValue, String topicId, String spaceId, String userId)
      throws RemoteException;

}
