/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.stratelia.webactiv.webSites.control.ejb;

/**
 * This is the WebSite manager service controller of the MVC. It is implemented as a CDI Bean.
 * It controls all the activities that happen in a client session. It also provides mechanisms to
 * access other service layer.
 * @author Cecile BONIN
 */

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import com.stratelia.webactiv.webSites.WebSitesContentManager;
import com.stratelia.webactiv.webSites.siteManage.dao.SiteDAO;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SitePK;
import com.stratelia.webactiv.webSites.siteManage.model.WebSitesRuntimeException;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class WebSiteBmEJB implements WebSiteBm {

  @Inject
  private NodeService nodeService;
  @Inject
  private PublicationService publicationService;
  /**
   * use for the PDC utilization
   */
  private WebSitesContentManager webSitesContentManager = null;

  public WebSiteBmEJB() {
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
      throw new WebSitesRuntimeException("WebSiteBmEJB.goTo()", SilverpeasRuntimeException.ERROR,
          "webSites.EX_NODEBM_DETAIL_FAILED", " pk = " + pk.toString(), re);
    }

    Collection<PublicationDetail> pubDetails;
    // get the publications associated to this topic
    try {
      // get the publication details linked to this topic
      pubDetails = publicationService.getDetailsByFatherPK(nodeDetail.getNodePK());
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.goTo()", SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATIONBM_DETAIL_FAILED", " pk = " + pk.toString(), re);
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
        throw new WebSitesRuntimeException("WebSiteBmEJB.goTo()", SilverpeasRuntimeException.ERROR,
            "webSites.EX_GET_NB_PUBLICATIONS_FAILED", re);
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
      List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeService.getPath(nd.getNodePK());
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getPathFromAToZ()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_GETPATH_FAILED",
          " pk = " + nd.getNodePK().toString(), re);
    }
    return newPath;
  }

  public NodePK addToFolder(NodePK fatherId, NodeDetail subTopic) {
    SilverTrace.info("webSites", "WebSiteBmEJB.addToFolder()", "root.MSG_GEN_ENTER_METHOD");
    try {
      NodeDetail father = nodeService.getDetail(fatherId);
      return nodeService.createNode(subTopic, father);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.addToFolder()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_CREATE_FAILED", re);
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
      SilverTrace.error("webSites", "WebSiteBmEJB.addFolder()", "root.MSG_GEN_PARAM_VALUE",
          "subFolder to add is null");
      throw new WebSitesRuntimeException("WebSiteBmEJB.addFolder()", SilverpeasException.ERROR,
          "unexisting subfolder node detail to add");
    }
    // add current space and component to subTopic detail
    subFolder.getNodePK().setComponentName(fatherId.getInstanceId());

    // Construction de la date de creation (date courante)
    String creationDate = DateUtil.today2SQLDate();
    subFolder.setCreationDate(creationDate);
    subFolder.setCreatorId(currentUser.getId());
    // add new topic to current topic
    NodePK pk = addToFolder(fatherId, subFolder);
    SilverTrace.info("webSites", "WebSiteBmEJB.addFolder()", "root.MSG_GEN_EXIT_METHOD");
    return pk;
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
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateFolder()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_UPDATE_FAILED", "topic = " + topic,
          re);
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
      throw new WebSitesRuntimeException("WebSiteBmEJB.getFolderDetail()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_NODE_DETAIL_FAILED", "pk = " + pk, re);
    }
  }

  /**
   * @param pkToDelete the topic identifier to delete
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deleteFolder(NodePK pkToDelete) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deleteFolder()", "root.MSG_GEN_ENTER_METHOD");
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
      throw new WebSitesRuntimeException("WebSiteBmEJB.deleteFolder()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_DELETE_FAILED", "pk = " + pkToDelete,
          re);
    }

  }

  /**
   * @param nodeId
   * @param nodes
   * @return
   */
  private int getIndexOfNode(String nodeId, List<NodeDetail> nodes) {
    SilverTrace.debug("webSites", "WebSiteBmEJB.getIndexOfNode()", "root.MSG_GEN_ENTER_METHOD",
        "nodeId = " + nodeId);
    int index = 0;
    if (nodes != null) {
      for (NodeDetail node : nodes) {
        if (nodeId.equals(node.getNodePK().getId())) {
          SilverTrace.debug("webSites", "WebSiteBmEJB.getIndexOfNode()", "root.MSG_GEN_EXIT_METHOD",
              "index = " + index);
          return index;
        }
        index++;
      }
    }
    SilverTrace.debug("webSites", "WebSiteBmEJB.getIndexOfNode()", "root.MSG_GEN_EXIT_METHOD",
        "index = " + index);
    return index;
  }

  /**
   * @param way
   * @param topicPK
   * @param fatherPK
   */
  @Override
  public void changeTopicsOrder(String way, NodePK topicPK, NodePK fatherPK) {
    SilverTrace.info("webSites", "WebSiteBmEJB.changeTopicsOrder()", "root.MSG_GEN_ENTER_METHOD",
        "way = " + way + ", topicPK = " + topicPK.toString());

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

        SilverTrace.info("webSites", "WebSiteBmEJB.changeTopicsOrder()", "root.MSG_GEN_PARAM_VALUE",
            "updating Node : nodeId = " + nodeDetail.getNodePK().getId() + ", order = " + i);
        try {
          nodeDetail.setOrder(i);
          nodeService.setDetail(nodeDetail);
        } catch (Exception e) {
          throw new WebSitesRuntimeException("WebSiteBmEJB.changeTopicsOrder()",
              SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_UPDATE_FAILED", e);
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
    SilverTrace
        .info("webSites", "WebSiteBmEJB.getPublicationDetail()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return publicationService.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getPublicationDetail()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_PUBLICATION_DETAIL_FAILED",
          "pubId = " + pk, re);
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
    SilverTrace.info("webSites", "WebSiteBmEJB.createPublication()", "root.MSG_GEN_PARAM_VALUE",
        "pubDetail = " + pubDetail);
    pubDetail.getPK().setComponentName(componentId);
    pubDetail.setStatus(PublicationDetail.VALID);
    try {
      // create the publication
      PublicationPK pubPK = publicationService.createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());
      return pubPK.getId();
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.createPublication()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_PUBLICATION_CREATE_FAILED",
          "pubDetail = " + pubDetail, re);
    }
  }

  /**
   * @param pubDetail
   */
  @Override
  public void updatePublication(PublicationDetail pubDetail, String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.updatePublication()", "root.MSG_GEN_ENTER_METHOD");
    pubDetail.getPK().setComponentName(componentId);
    try {
      publicationService.setDetail(pubDetail);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updatePublication()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_PUBLICATION_UPDATE_FAILED",
          "pubDetail = " + pubDetail, re);
    }
  }

  /**
   * @param pubPK
   */
  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deletePublication(PublicationPK pubPK) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deletePublication()", "root.MSG_GEN_PARAM_VALUE",
        "pubId = " + pubPK);
    try {
      publicationService.removeAllFather(pubPK);
      publicationService.removePublication(pubPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deletePublication()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_PUBLICATION_DELETE_FAILED",
          "pubPK = " + pubPK, re);
    }
  }

  /**
   * @param pubPK
   * @param fatherPK
   */
  @Override
  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK) {
    SilverTrace
        .info("webSites", "WebSiteBmEJB.addPublicationToTopic()", "root.MSG_GEN_ENTER_METHOD");
    try {
      publicationService.addFather(pubPK, fatherPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.addPublicationToTopic()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_PUBLICATION_ADD_TO_NODE_FAILED",
          "pubPK = " + pubPK + " - fatherPK = " + fatherPK, re);
    }
  }

  @Override
  public void removePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK) {
    SilverTrace
        .info("webSites", "WebSiteBmEJB.removePublicationToTopic()", "root.MSG_GEN_ENTER_METHOD");
    try {
      publicationService.removeFather(pubPK, fatherPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.removePublicationToTopic()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_PUBLICATION_DELETE_TO_NODE_FAILED",
          "pubPK = " + pubPK + " - fatherPK = " + fatherPK, re);
    }
  }

  /**
   * @param pubPK
   * @return
   */
  @Override
  public Collection<NodePK> getAllFatherPK(PublicationPK pubPK) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getAllFatherPK()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return publicationService.getAllFatherPK(pubPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getAllFatherPK()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_PUBLICATION_FATHER_FAILED",
          "pubId = " + pubPK, re);
    }
  }

  /**
   * getIdPublication
   */
  @Override
  public String getIdPublication(String componentId, String idSite) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getIdPublication()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getIdPublication(idSite);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getIdPublication()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_PUBLICATION_FAILED",
          "idSite = " + idSite, e);
    }
  }

  @Override
  public void updateClassification(PublicationPK pubPK, List<String> arrayTopic) {
    SilverTrace
        .info("webSites", "WebSiteBmEJB.updateClassification()", "root.MSG_GEN_ENTER_METHOD");
    Collection<NodePK> oldFathersColl = publicationService.getAllFatherPK(pubPK);

    List<NodePK> oldFathers = new ArrayList<>();
    List<NodePK> newFathers = new ArrayList<>();
    Collection<String> remFathers = new ArrayList<>();

    // Compute the remove list
    for (NodePK nodePK : oldFathersColl) {
      if (arrayTopic.indexOf(nodePK.getId()) == -1) {
        remFathers.add(nodePK.getId());
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
    publicationService.removeFathers(pubPK, remFathers);
  }

  /**
   * @param pubPK
   * @param nodePK
   * @param direction
   */
  @Override
  public void changePubsOrder(PublicationPK pubPK, NodePK nodePK, int direction) {
    SilverTrace.info("webSites", "WebSiteBmEJB.changePubsOrder()", "root.MSG_GEN_ENTER_METHOD",
        "pubId = " + pubPK + ", nodePK = " + nodePK.toString() + ", direction = " + direction);
    publicationService.changePublicationOrder(pubPK, nodePK, direction);
  }

  /**
   * getAllWebSite
   */
  @Override
  public Collection<SiteDetail> getAllWebSite(String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getAllWebSite()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getAllWebSite();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getAllWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITES_FAILED", e);
    }
  }

  /**
   * getWebSite
   * @param id
   * @return
   */
  public SiteDetail getWebSite(String componentId, String id) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getWebSite()", "root.MSG_GEN_ENTER_METHOD");
    SitePK pk = new SitePK(id, componentId);
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getWebSite(pk);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITE_FAILED", "id = " + id, e);
    }
  }

  /**
   * @param ids
   * @return
   */
  public List<SiteDetail> getWebSites(String componentId, List<String> ids) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getWebSites()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getWebSites(ids);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITE_FAILED", "ids = " + ids, e);
    }
  }

  /**
   * getIcons
   */
  public Collection<IconDetail> getIcons(String componentId, String id) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getIcons()", "root.MSG_GEN_ENTER_METHOD");
    SitePK pk = new SitePK(id, componentId);
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getIcons(pk);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getIcons()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_ICONS_FAILED", "id = " + id, e);
    }
  }

  @Override
  public String getNextId(String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getNextId()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getNextId();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getNextId()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }
  }

  @Override
  public Collection<IconDetail> getAllIcons(String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getAllIcons()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      return dao.getAllIcons();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getAllIcons()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_ALL_ICONS_FAILED", e);
    }
  }

  @Override
  public String createWebSite(String componentId, SiteDetail description, UserDetail currentUser) {
    SilverTrace.info("webSites", "WebSiteBmEJB.createWebSite()", "root.MSG_GEN_ENTER_METHOD");
    try (Connection con = getConnection()) {
      SiteDAO dao = new SiteDAO(componentId);
      dao.createWebSite(description);
      String pubPk = createPublication(componentId, description);
      // register the new publication as a new content to content manager
      // connection usefull for content service
      createSilverContent(con, description, currentUser.getId(), null, componentId);
      return pubPk;
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.createWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_CREATE_WEBSITE_FAILED",
          " SiteDetail = " + description.toString(), e);
    }
  }

  @Override
  public void associateIcons(String componentId, String id, Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.associateIcons()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.associateIcons(id, liste);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.associateIcons()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_ASSOCIATE_ICONS_FAILED", " id = " + id, e);
    }
  }

  @Override
  public void publish(String componentId, Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.publish()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.publish(liste);
      // register the new publication as a new content to content manager
      for (String siteId : liste) {
        SiteDetail siteDetail = getWebSite(componentId, siteId);
        updateSilverContentVisibility(siteDetail, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.publish()", SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLISH_FAILED", e);
    }
  }

  /**
   * dePublish
   */
  public void dePublish(String componentId, Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.dePublish()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.dePublish(liste);
      // register the new publication as a new content to content manager
      for (String siteId : liste) {
        SiteDetail siteDetail = getWebSite(componentId, siteId);
        updateSilverContentVisibility(siteDetail, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.dePublish()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_DEPUBLISH_FAILED", e);
    }
  }

  /**
   * deleteWebSites
   */
  public void deleteWebSites(String componentId, Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()", "root.MSG_GEN_ENTER_METHOD");
    try (Connection con = getConnection()) {
      SiteDAO dao = new SiteDAO(componentId);
      dao.deleteWebSites(liste);
      // register the new publication as a new content to content manager
      for (String siteId : liste) {
        SitePK sitePK = new SitePK(siteId, componentId);
        SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()", "root.MSG_GEN_PARAM_VALUE",
            "siteId =" + siteId);
        SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()", "root.MSG_GEN_PARAM_VALUE",
            "componentId =" + componentId);
        deleteSilverContent(con, sitePK, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deleteWebSites()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_DELETE_WEBSITES_FAILED", e);
    }
  }

  public void index(String componentId) {
    try {
      // index all topics
      NodePK rootPK = new NodePK("0", "useless", componentId);
      List<NodeDetail> tree = nodeService.getSubTree(rootPK);
      for (NodeDetail node : tree) {
        nodeService.createIndex(node);
      }
      // index all publications
      PublicationPK pubPK = new PublicationPK("useless", "useless", componentId);
      Collection<PublicationDetail> publications = publicationService.getAllPublications(pubPK);
      for (final PublicationDetail pub : publications) {
        publicationService.createIndex(pub);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.index(" + componentId + ")",
          SilverpeasRuntimeException.ERROR, "webSites.EX_INDEXING_COMPONENT_FAILED", e);
    }
  }

  /**
   * updateWebSite
   */
  public void updateWebSite(String componentId, SiteDetail description) {
    SilverTrace.info("webSites", "WebSiteBmEJB.updateWebSite()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(componentId);
      dao.updateWebSite(description);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_UPDATE_WEBSITE_FAILED",
          " SiteDetail = " + description, e);
    }
  }

  /**
   * ContentManager utilization to use PDC *
   */
  public int getSilverObjectId(String componentId, String id) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD",
        "id = " + id);
    int silverObjectId;
    try {
      silverObjectId = getWebSitesContentManager().getSilverObjectId(id, componentId);
      if (silverObjectId == -1) {
        SiteDetail siteDetail = getWebSite(componentId, id);
        silverObjectId = createSilverContent(null, siteDetail, "-1", null, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(Connection con, SiteDetail siteDetail, String creator,
      String prefixTableName, String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.createSilverContent()", "root.MSG_GEN_ENTER_METHOD",
        "siteId = " + siteDetail.getSitePK().getId());
    try {
      return getWebSitesContentManager()
          .createSilverContent(con, siteDetail, creator, prefixTableName, componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param con
   * @param sitePK
   * @param componentId
   */
  private void deleteSilverContent(Connection con, SitePK sitePK, String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deleteSilverContent()", "root.MSG_GEN_ENTER_METHOD",
        "siteId = " + sitePK.getId());
    try {
      getWebSitesContentManager().deleteSilverContent(con, sitePK, null, componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param siteDetail
   * @param componentId
   */
  private void updateSilverContentVisibility(SiteDetail siteDetail, String componentId) {
    try {
      getWebSitesContentManager().updateSilverContentVisibility(siteDetail, null, componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateSilverContent()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @return a "singleton" instance of WebSitesContentManager
   */
  private WebSitesContentManager getWebSitesContentManager() {
    if (webSitesContentManager == null) {
      webSitesContentManager = new WebSitesContentManager();
    }
    return webSitesContentManager;
  }

  /**
   * Connection management methods used for the content service *
   */
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}