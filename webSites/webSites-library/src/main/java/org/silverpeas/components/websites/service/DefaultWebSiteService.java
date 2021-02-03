/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.service;

/**
 * This is the WebSite manager service controller of the MVC. It is implemented as a CDI Bean.
 * It controls all the activities that happen in a client session. It also provides mechanisms to
 * access other service layer.
 * @author Cecile BONIN
 */

import org.silverpeas.components.websites.WebSitesContentManager;
import org.silverpeas.components.websites.siteManage.dao.SiteDAO;
import org.silverpeas.components.websites.siteManage.model.FolderDetail;
import org.silverpeas.components.websites.siteManage.model.IconDetail;
import org.silverpeas.components.websites.siteManage.model.SiteDetail;
import org.silverpeas.components.websites.siteManage.model.SitePK;
import org.silverpeas.components.websites.siteManage.model.WebSitesRuntimeException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.core.contribution.publication.dao.PublicationCriteria.onComponentInstanceIds;

@Service
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultWebSiteService implements WebSiteService {

  private static final String NO_ID = "useless";

  @Inject
  private NodeService nodeService;
  @Inject
  private PublicationService publicationService;
  /**
   * use for the PDC utilization
   */
  @Inject
  private WebSitesContentManager webSitesContentManager = null;

  DefaultWebSiteService() {
  }

  @Override
  public FolderDetail goTo(NodePK pk) {
    Collection<NodeDetail> newPath = new ArrayList<>();
    int nbPub;
    NodeDetail nodeDetail;
    // get the basic information (Header) of this folder
    try {
      nodeDetail = nodeService.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }

    Collection<PublicationDetail> pubDetails;
    // get the publications associated to this topic
    try {
      // get the publication details linked to this topic
      pubDetails = publicationService.getDetailsByFatherPK(nodeDetail.getNodePK());
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }

    // get the path to this topic

    if (nodeDetail.getNodePK().isRoot()) {
      newPath.add(nodeDetail);
    } else {
      newPath = getPathFromAToZ(nodeDetail);
    }


    // Get the publication number associated to each subTopics
    // First, get the childrenPKs of current topic
    Collection<NodeDetail> childrenPKs = nodeDetail.getChildrenDetails();

    List<Integer> nbPubByTopic = new ArrayList<>();
    // For each child, get the publication number associated to it
    for (final NodeDetail child : childrenPKs) {
      NodePK childPK = child.getNodePK();
      String childPath = child.getPath();
      try {
        // get the total number of publication associated to this descendant
        // topics
        nbPub = publicationService.getNbPubByFatherPath(childPK, childPath);
      } catch (Exception re) {
        throw new WebSitesRuntimeException(re);
      }
      // add this total to the collection
      nbPubByTopic.add(nbPub);
    }
    // set the currentTopic and return it
    return new FolderDetail(newPath, nodeDetail, pubDetails, nbPubByTopic);
  }

  /**
   * @param nd a NodeDetail
   * @return collection of NodeDetail
   */
  private Collection<NodeDetail> getPathFromAToZ(NodeDetail nd) {
    Collection<NodeDetail> newPath = new ArrayList<>();

    try {
      List<NodeDetail> pathInReverse = nodeService.getPath(nd.getNodePK());
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
    return newPath;
  }

  public NodePK addToFolder(NodePK fatherId, NodeDetail subTopic) {

    try {
      NodeDetail father = nodeService.getDetail(fatherId);
      return nodeService.createNode(subTopic, father);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param subFolder
   * @param fatherId
   * @param currentUser
   * @return
   */
  @Override
  public NodePK addFolder(NodeDetail subFolder, NodePK fatherId, UserDetail currentUser) {
    if (subFolder == null) {
      throw new WebSitesRuntimeException("Non existing subfolder node detail to add");
    }
    // add current space and component to subTopic detail
    subFolder.getNodePK().setComponentName(fatherId.getInstanceId());

    // Construction de la date de creation (date courante)
    String creationDate = DateUtil.today2SQLDate();
    subFolder.setCreationDate(creationDate);
    subFolder.setCreatorId(currentUser.getId());
    // add new topic to current topic
    return addToFolder(fatherId, subFolder);
  }

  /**
   * @param topic
   * @param fatherPK
   * @return a NodePK
   */
  @Override
  public NodePK updateFolder(NodeDetail topic, NodePK fatherPK) {
    try {
      NodeDetail father = nodeService.getDetail(fatherPK);
      topic.setLevel(father.getLevel());
      topic.setFatherPK(fatherPK);
      topic.getNodePK().setComponentName(fatherPK.getComponentName());
      nodeService.setDetail(topic);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
    return topic.getNodePK();
  }

  /**
   * @param pk
   * @return a NodeDetail
   */
  @Override
  public NodeDetail getFolderDetail(NodePK pk) {
    // get the basic information (Header) of this topic
    try {
      return nodeService.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param pkToDelete the topic identifier to delete
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deleteFolder(NodePK pkToDelete) {

    try {
      // get all nodes which will be deleted
      Collection<NodePK> nodesToDelete = nodeService.getDescendantPKs(pkToDelete);
      nodesToDelete.add(pkToDelete);
      for (NodePK oneNodeToDelete : nodesToDelete) {
        // get pubs linked to current node
        Collection<PublicationPK> pubsToCheck =
            publicationService.getPubPKsInFatherPK(oneNodeToDelete);
        // check each pub contained in current node
        for (PublicationPK onePubToCheck : pubsToCheck) {
          publicationService.removeFather(onePubToCheck, oneNodeToDelete);
        }
      }
      // Delete the topic
      nodeService.removeNode(pkToDelete);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }

  }

  /**
   * @param nodeId
   * @param nodes
   * @return
   */
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

  /**
   * @param way
   * @param topicPK
   * @param fatherPK
   */
  @Override
  public void changeTopicsOrder(String way, NodePK topicPK, NodePK fatherPK) {


    List<NodeDetail> subTopics = (List<NodeDetail>) nodeService.getChildrenDetails(fatherPK);

    if (subTopics != null && !subTopics.isEmpty()) {
      // search the place of the topic we want to move
      int indexOfTopic = getIndexOfNode(topicPK.getId(), subTopics);

      // get the node to move
      NodeDetail node2move = subTopics.get(indexOfTopic);

      // remove the node to move
      subTopics.remove(indexOfTopic);

      if ("up".equals(way)) {
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
          throw new WebSitesRuntimeException(e);
        }
      }
    }
  }

  /**
   * @param pk
   * @return
   */
  @Override
  public PublicationDetail getPublicationDetail(PublicationPK pk) {
    try {
      return publicationService.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param componentId
   * @param pubDetail
   * @return
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public String createPublication(String componentId, PublicationDetail pubDetail) {

    pubDetail.getPK().setComponentName(componentId);
    pubDetail.setStatus(PublicationDetail.VALID_STATUS);
    try {
      // create the publication
      PublicationPK pubPK = publicationService.createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());
      return pubPK.getId();
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param pubDetail
   */
  @Override
  public void updatePublication(PublicationDetail pubDetail, String componentId) {

    pubDetail.getPK().setComponentName(componentId);
    try {
      publicationService.setDetail(pubDetail);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param pubPK
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deletePublication(PublicationPK pubPK) {

    try {
      publicationService.removeAllFathers(pubPK);
      publicationService.removePublication(pubPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param pubPK
   * @param fatherPK
   */
  @Override
  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK) {
    try {
      publicationService.addFather(pubPK, fatherPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  @Override
  public void removePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK) {
    try {
      publicationService.removeFather(pubPK, fatherPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * @param pubPK
   * @return
   */
  @Override
  public Collection<NodePK> getAllFatherPK(PublicationPK pubPK) {

    try {
      return publicationService.getAllFatherPKInSamePublicationComponentInstance(pubPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(re);
    }
  }

  /**
   * getIdPublication
   */
  @Override
  public String getIdPublication(String componentId, String idSite) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getIdPublication(idSite);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  @Override
  public void updateClassification(PublicationPK pubPK, List<String> arrayTopic) {
    Collection<NodePK> oldFathersColl = publicationService.getAllFatherPKInSamePublicationComponentInstance(pubPK);

    List<NodePK> oldFathers = new ArrayList<>();
    List<NodePK> newFathers = new ArrayList<>();
    List<NodePK> remFathers = new ArrayList<>();

    // Compute the remove list
    for (NodePK nodePK : oldFathersColl) {
      if (arrayTopic.indexOf(nodePK.getId()) == -1) {
        remFathers.add(new NodePK(nodePK.getId(), pubPK));
      }
      oldFathers.add(nodePK);
    }

    // Compute the add and stay list
    for (String topicId : arrayTopic) {
      NodePK nodePK = new NodePK(topicId, pubPK);
      if (oldFathers.indexOf(nodePK) == -1) {
        newFathers.add(nodePK);
      }
    }

    for (NodePK newFather : newFathers) {
      publicationService.addFather(pubPK, newFather);
    }
    for (NodePK remFather : remFathers) {
      publicationService.removeFather(pubPK, remFather);
    }
  }

  /**
   * @param pubPK
   * @param nodePK
   * @param direction
   */
  @Override
  public void changePubsOrder(PublicationPK pubPK, NodePK nodePK, int direction) {

    publicationService.changePublicationOrder(pubPK, nodePK, direction);
  }

  /**
   * getAllWebSite
   */
  @Override
  public Collection<SiteDetail> getAllWebSite(String componentId) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getAllWebSite();
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * getWebSite
   * @param id
   * @return
   */
  public SiteDetail getWebSite(String componentId, String id) {

    SitePK pk = new SitePK(id, componentId);
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getWebSite(pk);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * @param ids
   * @return
   */
  public List<SiteDetail> getWebSites(String componentId, List<String> ids) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getWebSites(ids);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * getIcons
   */
  public Collection<IconDetail> getIcons(String componentId, String id) {

    SitePK pk = new SitePK(id, componentId);
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getIcons(pk);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  @Override
  public String getNextId(String componentId) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getNextId();
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  @Override
  public Collection<IconDetail> getAllIcons(String componentId) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getAllIcons();
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String createWebSite(String componentId, SiteDetail description, UserDetail currentUser) {

    try (Connection con = getConnection()) {
      SiteDAO dao = new SiteDAO(componentId);
      dao.createWebSite(description);
      String pubPk = createPublication(componentId, description);
      // register the new publication as a new content to content manager
      // connection usefull for content service
      createSilverContent(con, description, currentUser.getId());
      return pubPk;
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  @Override
  public void associateIcons(String componentId, String id, Collection<String> liste) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.associateIcons(id, liste);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  @Override
  public void publish(String componentId, Collection<String> liste) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.publish(liste);
      // register the new publication as a new content to content manager
      for (String siteId : liste) {
        SiteDetail siteDetail = getWebSite(componentId, siteId);
        updateSilverContentVisibility(siteDetail);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * dePublish
   */
  public void dePublish(String componentId, Collection<String> liste) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.dePublish(liste);
      // register the new publication as a new content to content manager
      for (String siteId : liste) {
        SiteDetail siteDetail = getWebSite(componentId, siteId);
        updateSilverContentVisibility(siteDetail);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * deleteWebSites
   */
  public void deleteWebSites(String componentId, Collection<String> liste) {

    try (Connection con = getConnection()) {
      SiteDAO dao = new SiteDAO(componentId);
      dao.deleteWebSites(liste);
      // register the new publication as a new content to content manager
      for (String siteId : liste) {
        SitePK sitePK = new SitePK(siteId, componentId);


        deleteSilverContent(con, sitePK);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException( e);
    }
  }

  public void index(String componentId) {
    try {
      // index all topics
      NodePK rootPK = new NodePK("0", NO_ID, componentId);
      List<NodeDetail> tree = nodeService.getSubTree(rootPK);
      for (NodeDetail node : tree) {
        nodeService.createIndex(node);
      }
      // index all publications
      Collection<PublicationDetail> publications =
          publicationService.getPublicationsByCriteria(onComponentInstanceIds(componentId));
      for (final PublicationDetail pub : publications) {
        publicationService.createIndex(pub);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * updateWebSite
   */
  public void updateWebSite(String componentId, SiteDetail description) {

    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.updateWebSite(description);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * ContentManager utilization to use PDC *
   */
  public int getSilverObjectId(String componentId, String id) {

    int silverObjectId;
    try {
      silverObjectId = getWebSitesContentManager().getSilverContentId(id, componentId);
      if (silverObjectId == -1) {
        SiteDetail siteDetail = getWebSite(componentId, id);
        silverObjectId = createSilverContent(null, siteDetail, "-1");
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
    return silverObjectId;
  }

  private int createSilverContent(Connection con, SiteDetail siteDetail, String creator) {

    try {
      return getWebSitesContentManager().createSilverContent(con, siteDetail, creator);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * @param con
   * @param sitePK
   */
  private void deleteSilverContent(Connection con, SitePK sitePK) {
    try {
      getWebSitesContentManager().deleteSilverContent(con, sitePK);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * @param siteDetail
   *
   */
  private void updateSilverContentVisibility(SiteDetail siteDetail) {
    try {
      getWebSitesContentManager().updateSilverContentVisibility(siteDetail);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }

  /**
   * @return a "singleton" instance of WebSitesContentManager
   */
  private WebSitesContentManager getWebSitesContentManager() {
    return webSitesContentManager;
  }

  /**
   * Connection management methods used for the content service *
   */
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new WebSitesRuntimeException(e);
    }
  }
}