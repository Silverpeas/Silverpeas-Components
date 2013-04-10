/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.formTemplate.dao.ModelDAO;
import com.silverpeas.kmelia.notification.KmeliaDefermentPublicationUserNotification;
import com.silverpeas.kmelia.notification.KmeliaDocumentSubscriptionPublicationUserNotification;
import com.silverpeas.kmelia.notification.KmeliaModificationPublicationUserNotification;
import com.silverpeas.kmelia.notification.KmeliaPendingValidationPublicationUserNotification;
import com.silverpeas.kmelia.notification.KmeliaSubscriptionPublicationUserNotification;
import com.silverpeas.kmelia.notification.KmeliaSupervisorPublicationUserNotification;
import com.silverpeas.kmelia.notification.KmeliaTopicUserNotification;
import com.silverpeas.kmelia.notification.KmeliaValidationPublicationUserNotification;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.ejb.PdcBm;
import com.silverpeas.pdc.ejb.PdcBmHome;
import com.silverpeas.pdc.ejb.PdcBmRuntimeException;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdcSubscription.util.PdcSubscriptionUtil;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.NodeSubscription;
import com.silverpeas.subscribe.service.NodeSubscriptionResource;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.kmelia.KmeliaContentManager;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.kmelia.PublicationImport;
import com.stratelia.webactiv.kmelia.model.KmaxRuntimeException;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.TopicComparator;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBm;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBmHome;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.NodeTree;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.ValidationStep;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.component.kmelia.InstanceParameters;
import org.silverpeas.component.kmelia.KmeliaPublicationHelper;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.*;

import static com.silverpeas.util.StringUtil.*;
import static com.stratelia.webactiv.util.JNDINames.*;
import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

/**
 * This is the KMelia EJB-tier controller of the MVC. It is implemented as a session EJB. It
 * controls all the activities that happen in a client session. It also provides mechanisms to
 * access other session EJBs.
 *
 * @author Nicolas Eysseric
 */
public class KmeliaBmEJB implements KmeliaBmBusinessSkeleton, SessionBean {

  private static final long serialVersionUID = 1L;
  private CommentService commentService = null;

  public KmeliaBmEJB() {
  }

  public void ejbCreate() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbCreate()", "root.MSG_GEN_ENTER_METHOD");
  }

  @Override
  public void ejbRemove() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbRemove()", "root.MSG_GEN_ENTER_METHOD");
  }

  @Override
  public void ejbActivate() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbActivate()", "root.MSG_GEN_ENTER_METHOD");
  }

  @Override
  public void ejbPassivate() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbPassivate()", "root.MSG_GEN_ENTER_METHOD");
  }

  private OrganizationController getOrganizationController() {
    // must return a new instance each time
    // This is to resolve Serializable problems
    OrganizationController orga = new OrganizationController();
    return orga;
  }

  @Override
  public void setSessionContext(SessionContext sc) {
  }

  private int getNbPublicationsOnRoot(String componentId) {
    String parameterValue = getOrganizationController().getComponentParameterValue(componentId,
        "nbPubliOnRoot");
    SilverTrace.info("kmelia", "KmeliaBmEJB.getNbPublicationsOnRoot()",
        "root.MSG_GEN_PARAM_VALUE", "parameterValue=" + parameterValue);
    if (isDefined(parameterValue)) {
      return Integer.parseInt(parameterValue);
    } else {
      if (KmeliaHelper.isToolbox(componentId)) {
        return 0;
      }
      // lecture du properties
      ResourceLocator settings = getSettings();
      return Integer.parseInt(settings.getString("HomeNbPublications"));
    }
  }

  private ResourceLocator getSettings() {
    return new ResourceLocator("org.silverpeas.kmelia.settings.kmeliaSettings", "fr");
  }

  private boolean isDraftModeUsed(String componentId) {
    return "yes"
        .equals(getOrganizationController().getComponentParameterValue(componentId, "draft"));
  }

  @Override
  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getNodeBm()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  @Override
  public PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(PUBLICATIONBM_EJBHOME,
          PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationBm()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME", e);
    }
    return publicationBm;
  }

  public SubscriptionService getSubscribeBm() {
    return SubscriptionServiceFactory.getFactory().getSubscribeService();
  }

  public StatisticBm getStatisticBm() {
    StatisticBm statisticBm = null;
    try {
      StatisticBmHome statisticBmHome = EJBUtilitaire.getEJBObjectRef(STATISTICBM_EJBHOME,
          StatisticBmHome.class);
      statisticBm = statisticBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getStatisticBm()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_STATISTICBM_HOME", e);
    }
    return statisticBm;
  }

  @Override
  public PdcBm getPdcBm() {
    PdcBm pdcBm = null;
    try {
      PdcBmHome pdcBmHome = EJBUtilitaire.getEJBObjectRef(PDCBM_EJBHOME, PdcBmHome.class);
      pdcBm = pdcBmHome.create();
    } catch (Exception e) {
      throw new PdcBmRuntimeException("KmeliaBmEJB.getPdcBm", ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return pdcBm;
  }

  /**
   * "Kmax" method
   *
   * @return
   */
  public CoordinatesBm getCoordinatesBm() {
    CoordinatesBm currentCoordinatesBm = null;
    try {
      CoordinatesBmHome coordinatesBmHome = EJBUtilitaire.getEJBObjectRef(COORDINATESBM_EJBHOME,
          CoordinatesBmHome.class);
      currentCoordinatesBm = coordinatesBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getCoordinatesBm()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_FABRIQUER_COORDINATESBM_HOME", e);
    }
    return currentCoordinatesBm;
  }

  /**
   * Return a the detail of a topic
   *
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
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_ENTER_METHOD");
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    NodeDetail nodeDetail = null;
    NodeBm nodeBm = getNodeBm();

    // get the basic information (Header) of this topic
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
        "root.MSG_GEN_PARAM_VALUE", "nodeBm.getDetail(pk) BEGIN");
    try {
      nodeDetail = nodeBm.getDetail(pk);
      if (isRightsOnTopicsUsed) {
        OrganizationController orga = getOrganizationController();
        if (nodeDetail.haveRights() && !orga.isObjectAvailable(nodeDetail.getRightsDependsOn(),
            ObjectType.NODE, pk.getInstanceId(), userId)) {
          nodeDetail.setUserRole("noRights");
        }
        List<NodeDetail> availableChildren = getAllowedSubfolders(nodeDetail, userId);
        nodeDetail.setChildrenDetails(availableChildren);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.goTo()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_PARAM_VALUE",
        "nodeBm.getDetail(pk) END");

    // get publications
    List<KmeliaPublication> pubDetails =
        getPublicationsOfFolder(pk, userProfile, userId, isTreeStructureUsed, isRightsOnTopicsUsed);

    // get the path to this topic
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_PARAM_VALUE", "GetPath BEGIN");
    if (pk.isRoot()) {
      newPath.add(nodeDetail);
    } else {
      newPath = getPathFromAToZ(nodeDetail);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_PARAM_VALUE", "GetPath END");
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_EXIT_METHOD");

    // set the currentTopic and return it
    return new TopicDetail(newPath, nodeDetail, pubDetails);
  }

  @Override
  public List<KmeliaPublication> getPublicationsOfFolder(NodePK pk, String userProfile,
      String userId, boolean isTreeStructureUsed, boolean isRightsOnTopicsUsed) {
    Collection<PublicationDetail> pubDetails = null;
    PublicationBm pubBm = getPublicationBm();
    // get the publications associated to this topic
    if (pk.isTrash()) {
      // Topic = Basket
      pubDetails = getPublicationsInBasket(pk, userProfile, userId);
    } else if (pk.isRoot()) {
      SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_PARAM_VALUE",
          "pubBm.getUnavailablePublicationsByPublisherId(pubPK, currentUser.getId()) BEGIN");

      try {
        int nbPublisOnRoot = getNbPublicationsOnRoot(pk.getInstanceId());
        if (nbPublisOnRoot == 0 || !isTreeStructureUsed || KmeliaHelper.isToolbox(
            pk.getInstanceId())) {
          pubDetails = pubBm.getDetailsByFatherPK(pk, "P.pubUpdateDate desc", false);
        } else {
          return getLatestPublications(pk.getInstanceId(), nbPublisOnRoot, isRightsOnTopicsUsed,
              userId);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationsOfFolder()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DAVOIR_LES_DERNIERES_PUBLICATIONS", e);
      }
    } else {
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsOfFolder()",
          "root.MSG_GEN_PARAM_VALUE", "pubBm.getDetailsByFatherPK(pk) BEGIN");
      try {
        // get the publication details linked to this topic
        pubDetails = pubBm.getDetailsByFatherPK(pk, "P.pubUpdateDate DESC, P.pubId DESC", false);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationsOfFolder()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DAVOIR_LA_LISTE_DES_PUBLICATIONS", e);
      }
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsOfFolder()",
          "root.MSG_GEN_PARAM_VALUE", "pubBm.getDetailsByFatherPK(pk) END");
    }
    return pubDetails2userPubs(pubDetails);
  }

  @Override
  public List<KmeliaPublication> getLatestPublications(String instanceId, int nbPublisOnRoot,
      boolean isRightsOnTopicsUsed, String userId) throws RemoteException {
    PublicationPK pubPK = new PublicationPK("unknown", instanceId);
    Collection<PublicationDetail> pubDetails =
        getPublicationBm().getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(pubPK,
        PublicationDetail.VALID, nbPublisOnRoot, NodePK.BIN_NODE_ID);
    if (isRightsOnTopicsUsed) {// The list of publications must be filtered
      List<PublicationDetail> filteredList = new ArrayList<PublicationDetail>();
      KmeliaSecurity security = new KmeliaSecurity();
      for (PublicationDetail pubDetail : pubDetails) {
        if (security.isObjectAvailable(instanceId, userId, pubDetail.getPK().getId(),
            "Publication")) {
          filteredList.add(pubDetail);
        }
      }
      pubDetails.clear();
      pubDetails.addAll(filteredList);
    }
    return pubDetails2userPubs(pubDetails);
  }

  @Override
  public List<NodeDetail> getAllowedSubfolders(NodeDetail folder, String userId)
      throws RemoteException {
    OrganizationController orga = getOrganizationController();
    NodePK pk = folder.getNodePK();
    List<NodeDetail> children = (List<NodeDetail>) folder.getChildrenDetails();
    List<NodeDetail> availableChildren = new ArrayList<NodeDetail>();
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
          Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(child).iterator();
          boolean childAllowed = false;
          while (!childAllowed && descendants.hasNext()) {
            NodeDetail descendant = descendants.next();
            if (descendant.getRightsDependsOn() == rightsDependsOn) {
              // same rights of father (which is not available) so it is not available too
            } else {
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
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) getNodeBm().getPath(nd.getNodePK());
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPathFromAToZ()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CHEMIN_COURANT", e);
    }
    return newPath;
  }

  /**
   * Add a subtopic to a topic - If a subtopic of same name already exists a NodePK with id=-1 is
   * returned else the new topic NodePK
   *
   * @param fatherPK the topic Id of the future father
   * @param subTopic the NodeDetail of the new sub topic
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   */
  @Override
  public NodePK addToTopic(NodePK fatherPK, NodeDetail subTopic) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addToTopic()", "root.MSG_GEN_ENTER_METHOD");
    NodePK theNodePK = null;
    try {
      NodeDetail fatherDetail = getNodeBm().getHeader(fatherPK);
      theNodePK = getNodeBm().createNode(subTopic, fatherDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addToTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LE_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.addToTopic()", "root.MSG_GEN_EXIT_METHOD");
    return theNodePK;
  }

  /**
   * Add a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   *
   * @param fatherPK
   * @param subTopic the NodeDetail of the new sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   */
  @Override
  public NodePK addSubTopic(NodePK fatherPK, NodeDetail subTopic, String alertType) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubTopic()", "root.MSG_GEN_ENTER_METHOD");
    // Construction de la date de création (date courante)
    String creationDate = DateUtil.today2SQLDate();
    subTopic.setCreationDate(creationDate);
    // Web visibility parameter. The topic is by default invisible.
    subTopic.setStatus("Invisible");
    // add new topic to current topic
    NodePK pk = addToTopic(fatherPK, subTopic);
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubTopic()", "root.MSG_GEN_PARAM_VALUE",
        "pk = " + pk.toString());
    // Creation alert
    if (!"-1".equals(pk.getId())) {
      topicCreationAlert(pk, fatherPK, alertType);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubTopic()", "root.MSG_GEN_EXIT_METHOD");
    return pk;
  }

  private String displayPath(Collection<NodeDetail> path, int beforeAfter, String language) {
    StringBuilder pathString = new StringBuilder();
    boolean first = true;

    List<NodeDetail> pathAsList = new ArrayList<NodeDetail>(path);
    Collections.reverse(pathAsList); // reverse path from root to node
    for (NodeDetail nodeInPath : pathAsList) {
      if (!first) {
        pathString.append(" > ");
      }
      first = false;
      pathString.append(nodeInPath.getName(language));
    }
    return pathString.toString();
  }

  /**
   * Alert all users, only publishers or nobody of the topic creation or update
   *
   * @param pk the NodePK of the new sub topic
   * @param alertType alertType = "All"|"Publisher"|"None"
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  private void topicCreationAlert(final NodePK nodePK, final NodePK fatherPK, final String alertType) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.topicCreationAlert()",
        "root.MSG_GEN_ENTER_METHOD");

    UserNotificationHelper
        .buildAndSend(new KmeliaTopicUserNotification(nodePK, fatherPK, alertType));

    SilverTrace.info("kmelia", "KmeliaBmEJB.topicCreationAlert()", "root.MSG_GEN_PARAM_VALUE",
        "AlertType alert = " + alertType);
    SilverTrace.info("kmelia", "KmeliaBmEJB.topicCreationAlert()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Update a subtopic to currentTopic and alert users - If a subtopic of same name already exists a
   * NodePK with id=-1 is returned else the new topic NodePK
   *
   * @param topic the NodeDetail of the updated sub topic
   * @param alertType Alert all users, only publishers or nobody of the topic creation alertType =
   * "All"|"Publisher"|"None"
   * @return If a subtopic of same name already exists a NodePK with id=-1 is returned else the new
   * topic NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  @Override
  public NodePK updateTopic(NodeDetail topic, String alertType) {
    try {
      // Order of the node must be unchanged
      NodeDetail node = getNodeBm().getHeader(topic.getNodePK());
      int order = node.getOrder();
      topic.setOrder(order);
      getNodeBm().setDetail(topic);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updateTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
    // Update Alert
    topicCreationAlert(topic.getNodePK(), null, alertType);
    return topic.getNodePK();
  }

  @Override
  public NodeDetail getSubTopicDetail(NodePK pk) {
    NodeDetail subTopic = null;
    // get the basic information (Header) of this topic
    try {
      subTopic = getNodeBm().getDetail(pk);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSubTopicDetail()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e);
    }
    return subTopic;
  }

  /**
   * Delete a topic and all descendants. Delete all links between descendants and publications. This
   * publications will be visible in the Declassified zone. Delete All subscriptions and favorites
   * on this topics and all descendants
   *
   * @param pkToDelete the id of the topic to delete
   * @since 1.0
   */
  @Override
  public void deleteTopic(NodePK pkToDelete) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()", "root.MSG_GEN_ENTER_METHOD");
    PublicationBm pubBm = getPublicationBm();
    NodeBm nodeBm = getNodeBm();

    try {
      // get all nodes which will be deleted
      Collection<NodePK> nodesToDelete = nodeBm.getDescendantPKs(pkToDelete);
      nodesToDelete.add(pkToDelete);
      SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()", "root.MSG_GEN_PARAM_VALUE",
          "nodesToDelete = " + nodesToDelete.toString());

      Iterator<PublicationPK> itPub = null;
      Collection<PublicationPK> pubsToCheck = null; // contains all PubPKs concerned by
      // the delete
      NodePK oneNodeToDelete = null; // current node to delete
      Collection<NodePK> pubFathers = null; // contains all fatherPKs to a given
      // publication
      PublicationPK onePubToCheck = null; // current pub to check
      Iterator<NodePK> itNode = nodesToDelete.iterator();
      List<Alias> aliases = new ArrayList<Alias>();
      while (itNode.hasNext()) {
        oneNodeToDelete = itNode.next();
        // get pubs linked to current node (includes alias)
        pubsToCheck = pubBm.getPubPKsInFatherPK(oneNodeToDelete);
        itPub = pubsToCheck.iterator();
        // check each pub contained in current node
        while (itPub.hasNext()) {
          onePubToCheck = itPub.next();
          if (onePubToCheck.getInstanceId().equals(oneNodeToDelete.getInstanceId())) {
            // get fathers of the pub
            pubFathers = pubBm.getAllFatherPK(onePubToCheck);
            if (pubFathers.size() >= 2) {
              // the pub have got many fathers
              // delete only the link between pub and current node
              pubBm.removeFather(onePubToCheck, oneNodeToDelete);
              SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()", "root.MSG_GEN_PARAM_VALUE",
                  "RemoveFather(pubId, fatherId) with  pubId = " + onePubToCheck.getId()
                  + ", fatherId = " + oneNodeToDelete);
            } else {
              sendPublicationToBasket(onePubToCheck);
              SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()", "root.MSG_GEN_PARAM_VALUE",
                  "RemoveAllFather(pubId) with pubId = " + onePubToCheck.getId());
            }
          } else {
            // remove alias
            aliases.clear();
            aliases.add(new Alias(oneNodeToDelete.getId(), oneNodeToDelete.getInstanceId()));
            pubBm.removeAlias(onePubToCheck, aliases);
          }
        }
      }

      // Delete all subscriptions on this topic and on its descendants
      removeSubscriptionsByTopic(nodesToDelete);

      // Delete the topic
      nodeBm.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void changeSubTopicsOrder(String way, NodePK subTopicPK,
      NodePK fatherPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changeSubTopicsOrder()", "root.MSG_GEN_ENTER_METHOD",
        "way = " + way + ", subTopicPK = " + subTopicPK.toString());

    List<NodeDetail> subTopics = null;
    try {
      subTopics = (List<NodeDetail>) getNodeBm().getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.changeSubTopicsOrder()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_LISTER_THEMES", e);
    }

    if (subTopics != null && !subTopics.isEmpty()) {
      int indexOfTopic = 0;

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
        SilverTrace.info("kmelia", "KmeliaBmEJB.changeSubTopicsOrder()",
            "root.MSG_GEN_PARAM_VALUE", "updating Node : nodeId = "
            + nodeDetail.getNodePK().getId() + ", order = " + i);
        try {
          nodeDetail.setOrder(i);
          getNodeBm().setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException("KmeliaBmEJB.changeSubTopicsOrder()",
              ERROR, "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
        }
      }
    }
  }

  private int getIndexOfNode(String nodeId, List<NodeDetail> nodes) {
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getIndexOfNode()",
        "root.MSG_GEN_ENTER_METHOD", "nodeId = " + nodeId);
    int index = 0;
    if (nodes != null) {
      for (NodeDetail node : nodes) {
        if (nodeId.equals(node.getNodePK().getId())) {
          SilverTrace.debug("kmelia", "KmeliaBmEJB.getIndexOfNode()",
              "root.MSG_GEN_EXIT_METHOD", "index = " + index);
          return index;
        }
        index++;
      }
    }
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getIndexOfNode()",
        "root.MSG_GEN_EXIT_METHOD", "index = " + index);
    return index;
  }

  @Override
  public void changeTopicStatus(String newStatus, NodePK nodePK, boolean recursiveChanges) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changeTopicStatus()", "root.MSG_GEN_ENTER_METHOD",
        "newStatus = " + newStatus + ", nodePK = " + nodePK.toString()
        + ", recursiveChanges = " + recursiveChanges);
    try {
      if (!recursiveChanges) {
        NodeDetail nodeDetail = getNodeBm().getHeader(nodePK);
        changeTopicStatus(newStatus, nodeDetail);
      } else {
        List<NodeDetail> subTree = getNodeBm().getSubTree(nodePK);
        for (NodeDetail aSubTree : subTree) {
          NodeDetail nodeDetail = aSubTree;
          changeTopicStatus(newStatus, nodeDetail);
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.changeTopicStatus()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
  }

  @Override
  public void sortSubTopics(NodePK fatherPK) {
    sortSubTopics(fatherPK, false, null);
  }

  @Override
  public void sortSubTopics(NodePK fatherPK, boolean recursive, String[] criteria) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.sortSubTopics()",
        "root.MSG_GEN_ENTER_METHOD", "fatherPK = " + fatherPK.toString());

    List<NodeDetail> subTopics = null;
    try {
      subTopics = (List<NodeDetail>) getNodeBm().getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.sortSubTopics()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_LISTER_THEMES", e);
    }

    if (subTopics != null && subTopics.size() > 0) {
      Collections.sort(subTopics, new TopicComparator(criteria));
      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        NodeDetail nodeDetail = subTopics.get(i);
        SilverTrace.info("kmelia", "KmeliaBmEJB.sortSubTopics()", "root.MSG_GEN_PARAM_VALUE",
            "updating Node : nodeId = " + nodeDetail.getNodePK().getId() + ", order = " + i);
        try {
          nodeDetail.setOrder(i);
          getNodeBm().setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException("KmeliaBmEJB.sortSubTopics()", ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
        }
        if (recursive) {
          sortSubTopics(nodeDetail.getNodePK(), true, criteria);
        }
      }
    }
  }

  private void changeTopicStatus(String newStatus, NodeDetail topic) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changeTopicStatus()",
        "root.MSG_GEN_ENTER_METHOD", "newStatus = " + newStatus + ", nodePK = "
        + topic.getNodePK().toString());
    try {
      topic.setStatus(newStatus);
      getNodeBm().setDetail(topic);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.changeTopicStatus()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
  }

  @Override
  public List<NodeDetail> getTreeview(NodePK nodePK, String profile, boolean coWritingEnable,
      boolean draftVisibleWithCoWriting, String userId, boolean displayNb,
      boolean isRightsOnTopicsUsed) {
    String instanceId = nodePK.getInstanceId();
    try {
      List<NodeDetail> tree = getNodeBm().getSubTree(nodePK);

      List<NodeDetail> allowedTree = new ArrayList<NodeDetail>();
      OrganizationController orga = getOrganizationController();
      if (isRightsOnTopicsUsed) {
        // filter allowed nodes
        for (NodeDetail node2Check : tree) {
          if (!node2Check.haveRights()) {
            allowedTree.add(node2Check);
            if (node2Check.getNodePK().isRoot()) {// case of root. Check if publications on root are
              // allowed
              int nbPublisOnRoot = Integer.parseInt(orga.getComponentParameterValue(nodePK.
                  getInstanceId(), "nbPubliOnRoot"));
              if (nbPublisOnRoot != 0) {
                node2Check.setUserRole("user");
              }
            }
          } else {
            int rightsDependsOn = node2Check.getRightsDependsOn();
            String[] profiles = orga.getUserProfiles(userId, instanceId,
                rightsDependsOn, ObjectType.NODE);
            if (profiles != null && profiles.length > 0) {
              node2Check.setUserRole(KmeliaHelper.getProfile(profiles));
              allowedTree.add(node2Check);
            } else { // check if at least one descendant is available
              Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(node2Check).
                  iterator();
              NodeDetail descendant = null;
              boolean node2CheckAllowed = false;
              while (!node2CheckAllowed && descendants.hasNext()) {
                descendant = descendants.next();
                if (descendant.getRightsDependsOn() != rightsDependsOn) {
                  // different rights of father check if it is available
                  profiles = orga.getUserProfiles(userId, instanceId,
                      descendant.getRightsDependsOn(), ObjectType.NODE);
                  if (profiles != null && profiles.length > 0) {
                    node2Check.setUserRole(KmeliaHelper.getProfile(profiles));
                    allowedTree.add(node2Check);
                    node2CheckAllowed = true;
                  }
                }
              }
            }
          }
        }
      } else {
        if (tree != null && !tree.isEmpty()) {
          // case of root. Check if publications on root are allowed
          String sNB = orga.getComponentParameterValue(nodePK.getInstanceId(), "nbPubliOnRoot");
          if (!isDefined(sNB)) {
            sNB = "0";
          }
          int nbPublisOnRoot = Integer.parseInt(sNB);
          if (nbPublisOnRoot != 0) {
            NodeDetail root = tree.get(0);
            root.setUserRole("user");
          }
        }
        allowedTree.addAll(tree);
      }

      if (displayNb) {
        boolean checkVisibility = false;
        StringBuilder statusSubQuery = new StringBuilder();
        if (profile.equals("user")) {
          checkVisibility = true;
          statusSubQuery.append(" AND sb_publication_publi.pubStatus = 'Valid' ");
        } else if (profile.equals("writer")) {
          statusSubQuery.append(" AND (");
          if (coWritingEnable && draftVisibleWithCoWriting) {
            statusSubQuery.append("sb_publication_publi.pubStatus = 'Valid' OR ").append(
                "sb_publication_publi.pubStatus = 'Draft' OR ").append(
                "sb_publication_publi.pubStatus = 'Unvalidate' ");
          } else {
            checkVisibility = true;
            statusSubQuery.append("sb_publication_publi.pubStatus = 'Valid' OR ").append(
                "(sb_publication_publi.pubStatus = 'Draft' AND ").append(
                "sb_publication_publi.pubUpdaterId = '").
                append(userId).append(
                "') OR (sb_publication_publi.pubStatus = 'Unvalidate' AND ").
                append("sb_publication_publi.pubUpdaterId = '").append(userId).append("') ");
          }
          statusSubQuery.append("OR (sb_publication_publi.pubStatus = 'ToValidate' ").append(
              "AND sb_publication_publi.pubUpdaterId = '").append(userId).append("') ");
          statusSubQuery.append("OR sb_publication_publi.pubUpdaterId = '").append(userId).append(
              "')");
        } else {
          statusSubQuery.append(" AND (");
          if (coWritingEnable && draftVisibleWithCoWriting) {
            statusSubQuery.append(
                "sb_publication_publi.pubStatus IN ('Valid','ToValidate','Draft') ");
          } else {
            if (profile.equals("publisher")) {
              checkVisibility = true;
            }
            statusSubQuery.
                append(
                "sb_publication_publi.pubStatus IN ('Valid','ToValidate') OR (sb_publication_publi.pubStatus = 'Draft' AND sb_publication_publi.pubUpdaterId = '").
                append(userId).append("') ");
          }
          statusSubQuery.append("OR sb_publication_publi.pubUpdaterId = '").append(userId).append(
              "')");
        }

        NodeTree root = getPublicationBm().getDistributionTree(nodePK.getInstanceId(),
            statusSubQuery.toString(), checkVisibility);

        // set right number of publications in basket
        NodePK trashPk = new NodePK(NodePK.BIN_NODE_ID, nodePK.getInstanceId());
        int nbPubsInTrash = getPublicationsInBasket(trashPk, profile, userId).size();
        for (NodeTree node : root.getChildren()) {
          if (node.getKey().isTrash()) {
            node.setNbPublications(nbPubsInTrash);
          }
        }

        Map<NodePK, NodeDetail> allowedNodes = new HashMap<NodePK, NodeDetail>(allowedTree.size());
        for (NodeDetail allowedNode : allowedTree) {
          allowedNodes.put(allowedNode.getNodePK(), allowedNode);
        }

        countPublisInNodes(allowedNodes, root);
      }
      return allowedTree;
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getTreeview()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DAVOIR_LA_LISTE_DES_PUBLICATIONS", e);
    }
  }

  private Collection<PublicationDetail> getPublicationsInBasket(NodePK pk, String userProfile,
      String userId) {
    String currentUserId = userId;
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsInBasket()",
        "root.MSG_GEN_ENTER_METHOD", "pk = " + pk.toString() + ", userProfile = " + userProfile
        + ", userId = " + currentUserId);
    try {
      // Give the publications associated to basket topic and visibility period expired
      if (SilverpeasRole.admin.isInRole(userProfile)) {// Admin can see all Publis in the basket.
        currentUserId = null;
      }
      Collection<PublicationDetail> pubDetails = getPublicationBm().getDetailsByFatherPK(pk, null,
          false, currentUserId);
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsInBasket()",
          "root.MSG_GEN_EXIT_METHOD", "nbPublis = " + pubDetails.size());
      return pubDetails;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationsInBasket()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CONTENU_DE_LA_CORBEILLE", e);
    }
  }

  /**
   * Counts number of publications recursively in allowed nodes only
   *
   * @param allowedNodes a Map of all nodes allowed to user
   * @param tree the whole tree which contains the number of publications in each node independently
   * of rights
   */
  private void countPublisInNodes(Map<NodePK, NodeDetail> allowedNodes, NodeTree tree) {
    for (NodeDetail node : allowedNodes.values()) {
      NodeTree nodeTree = findNode(node, tree);
      if (nodeTree != null) {
        int nbPublis = countNbPublis(nodeTree);
        SilverTrace.debug("kmelia", "KmeliaBmEJB.countPublisInNodes", "root.MSG_GEN_PARAM_VALUE",
            nbPublis + " pubs in node " + node.getNodePK().getId());
        node.setNbObjects(nbPublis);
      }
    }
  }

  private NodeTree findNode(NodeDetail node, NodeTree tree) {
    SilverTrace.debug("kmelia", "KmeliaBmEJB.findNode", "root.MSG_GEN_ENTER_METHOD",
        "looking for node " + node.getNodePK().getId());
    String path = node.getFullPath();
    if (path.length() > 1) {
      path = path.substring(1, path.length() - 1); // remove starting and ending slash
      SilverTrace.debug("kmelia", "KmeliaBmEJB.findNode", "root.MSG_GEN_PARAM_VALUE", " path = "
          + path);
      ArrayList<String> pathItems = new ArrayList<String>(Arrays.asList(path.split("/")));
      pathItems.remove(0); // remove root
      NodeTree current = tree;
      for (String pathItem : pathItems) {
        if (current != null) {
          current = findNodeTree(pathItem, current.getChildren());
        }
      }
      if (current != null) {
        SilverTrace.debug("kmelia", "KmeliaBmEJB.findNode", "root.MSG_GEN_EXIT_METHOD",
            "node " + current.getKey().getId() + " found");
        return current;
      }
    }
    SilverTrace.error("kmelia", "KmeliaBmEJB.findNode", "root.MSG_GEN_EXIT_METHOD",
        "node " + node.getNodePK().getId() + " not found");
    return null;
  }

  private NodeTree findNodeTree(String nodeId, List<NodeTree> children) {
    for (NodeTree node : children) {
      if (node.getKey().getId().equals(nodeId)) {
        return node;
      }
    }
    return null;
  }

  /**
   * Counts the number of publications in the node and all its descendants
   *
   * @param tree the subtree which have tree.currentNode as root
   * @return the number of publications in this node and all its descendants
   */
  private int countNbPublis(NodeTree tree) {
    if (tree == null) {
      return 0;
    }
    int nb = tree.getNbPublications();
    // add nb of each descendant
    for (NodeTree node : tree.getChildren()) {
      nb += countNbPublis(node);
    }
    SilverTrace.debug("kmelia", "KmeliaBmEJB.countNbPublis", "root.MSG_GEN_EXIT_METHOD",
        nb + " pubs in node " + tree.getKey().getId());
    return nb;
  }

  /**
   * Subscriptions - get the subscription list of the current user
   *
   * @param userId
   * @param componentId
   * @return a Path Collection - it's a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<Collection<NodeDetail>> getSubscriptionList(String userId, String componentId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getSubscriptionList()", "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<Subscription> list = getSubscribeBm()
          .getBySubscriberAndComponent(UserSubscriptionSubscriber.from(userId), componentId);
      Collection<Collection<NodeDetail>> detailedList = new ArrayList<Collection<NodeDetail>>();
      // For each favorite, get the path from root to favorite
      for (Subscription subscription : list) {
        Collection<NodeDetail> path =
            getNodeBm().getPath((NodePK) subscription.getResource().getPK());
        detailedList.add(path);
      }
      SilverTrace.info("kmelia", "KmeliaBmEJB.getSubscriptionList()", "root.MSG_GEN_EXIT_METHOD");
      return detailedList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSubscriptionList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
    }
  }

  /**
   * Subscriptions - remove a subscription to the subscription list of the current user
   *
   * @param topicPK the subscribe topic Id to remove
   * @param userId
   * @since 1.0
   */
  @Override
  public void removeSubscriptionToCurrentUser(NodePK topicPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeSubscriptionToCurrentUser()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getSubscribeBm().unsubscribe(new NodeSubscription(userId, topicPK));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.removeSubscriptionToCurrentUser()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_ABONNEMENT", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeSubscriptionToCurrentUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Subscriptions - remove all subscriptions from topic
   * @param topicPKsToDelete the subscription topic Ids to remove
   * @since 1.0
   */
  private void removeSubscriptionsByTopic(Collection<NodePK> topicPKsToDelete) {
    SilverTrace
        .info("kmelia", "KmeliaBmEJB.removeSubscriptionsByTopic()", "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<SubscriptionResource> subscriptionResourcesToDelete =
          new ArrayList<SubscriptionResource>();
      for (NodePK topicPK : topicPKsToDelete) {
        subscriptionResourcesToDelete.add(NodeSubscriptionResource.from(topicPK));
      }
      getSubscribeBm().unsubscribeByResources(subscriptionResourcesToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.removeSubscriptionsByTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_ABONNEMENTS", e);
    }
    SilverTrace
        .info("kmelia", "KmeliaBmEJB.removeSubscriptionsByTopic()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Subscriptions - add a subscription
   *
   * @param topicPK the subscription topic Id to add
   * @param userId the subscription userId
   * @since 1.0
   */
  @Override
  public void addSubscription(NodePK topicPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubscription()", "root.MSG_GEN_ENTER_METHOD");
    getSubscribeBm().subscribe(new NodeSubscription(userId, topicPK));
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubscription()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   *
   * @param topicPK
   * @param userId
   * @return true if this topic does not exists in user subscriptions and can be added to them.
   */
  @Override
  public boolean checkSubscription(NodePK topicPK, String userId) {
    return !getSubscribeBm().existsSubscription(new NodeSubscription(userId, topicPK));
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
   *
   * @param pubPK the id of the publication
   * @return a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  public PublicationDetail getPublicationDetail(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetail()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetail()", "root.MSG_GEN_EXIT_METHOD");
      return getPublicationBm().getDetail(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationDetail()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
  }

  /**
   * Return list of all path to this publication - it's a Collection of NodeDetail collection
   *
   * @param pubPK the id of the publication
   * @return a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<Collection<NodeDetail>> getPathList(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPathList()", "root.MSG_GEN_ENTER_METHOD");
    Collection<NodePK> fatherPKs = null;
    try {
      // get all nodePK whick contains this publication
      fatherPKs = getPublicationBm().getAllFatherPK(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPathList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    try {
      List<Collection<NodeDetail>> pathList = new ArrayList<Collection<NodeDetail>>();
      if (fatherPKs != null) {
        // For each topic, get the path to it
        for (NodePK pk : fatherPKs) {
          Collection<NodeDetail> path = getNodeBm().getAnotherPath(pk);
          // add this path
          pathList.add(path);
        }
      }
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPathList()", "root.MSG_GEN_EXIT_METHOD");
      return pathList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPathList()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * Create a new Publication (only the header - parameters) to the current Topic
   *
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  public String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK) {
    PdcClassificationService classifier = PdcServiceFactory.getFactory().
        getPdcClassificationService();
    PdcClassification predefinedClassification =
        classifier.findAPreDefinedClassification(fatherPK.getId(), pubDetail.getInstanceId());
    return createPublicationIntoTopic(pubDetail, fatherPK, predefinedClassification);
  }

  @Override
  public String createPublicationIntoTopic(PublicationDetail pubDetail, NodePK fatherPK,
      PdcClassification classification) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createPublicationIntoTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    String pubId = null;
    try {
      pubId = createPublicationIntoTopicWithoutNotifications(pubDetail, fatherPK, classification);

      // creates todos for publishers
      createTodosForPublication(pubDetail, true);

      // alert supervisors
      sendAlertToSupervisors(fatherPK, pubDetail);

      // alert subscribers
      sendSubscriptionsNotification(pubDetail, false);

    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.createPublicationIntoTopic()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.createPublicationIntoTopic()",
        "root.MSG_GEN_EXIT_METHOD");
    return pubId;
  }

  public String createPublicationIntoTopicWithoutNotifications(PublicationDetail pubDetail,
      NodePK fatherPK, PdcClassification classification) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createPublicationIntoTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = null;
    try {
      pubDetail = changePublicationStatusOnCreation(pubDetail, fatherPK);
      // create the publication
      pubPK = getPublicationBm().createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());
      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
      // add this publication to the current topic
      addPublicationToTopicWithoutNotifications(pubPK, fatherPK, true);
      // classify the publication on the PdC if its classification is defined
      if (!classification.isEmpty()) {
        PdcClassificationService service = PdcServiceFactory.getFactory().
            getPdcClassificationService();
        classification.ofContent(pubPK.getId());
        service.classifyContent(pubDetail, classification);
      }

    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createPublicationIntoTopic()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.createPublicationIntoTopic()",
        "root.MSG_GEN_EXIT_METHOD");
    return pubPK.getId();
  }

  private String getProfile(String userId, NodePK nodePK) throws RemoteException {
    String profile = null;
    OrganizationController orgCtrl = getOrganizationController();
    if (StringUtil.getBooleanValue(
        orgCtrl.getComponentParameterValue(nodePK.getInstanceId(), "rightsOnTopics"))) {
      NodeDetail topic = getNodeBm().getHeader(nodePK);
      if (topic.haveRights()) {
        profile = KmeliaHelper.getProfile(orgCtrl.getUserProfiles(userId, nodePK.getInstanceId(),
            topic.getRightsDependsOn(), ObjectType.NODE));
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

    String profile = null;
    try {
      profile = getProfile(userId, nodePK);
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "KmeliaBmEJB.externalElementsOfPublicationHaveChanged",
          "kmelia.ERROR_ON_GETTING_PROFILE", "userId = " + userId + ", node = "
          + nodePK.toString(), e);
    }
    return profile;
  }

  private PublicationDetail changePublicationStatusOnCreation(
      PublicationDetail pubDetail, NodePK nodePK) throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changePublicationStatusOnCreation()",
        "root.MSG_GEN_ENTER_METHOD", "status = " + pubDetail.getStatus());
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
    SilverTrace.info("kmelia", "KmeliaBmEJB.changePublicationStatusOnCreation()",
        "root.MSG_GEN_EXIT_METHOD", "status = " + pubDetail.getStatus()
        + ", indexOperation = " + pubDetail.getIndexOperation());
    return pubDetail;
  }

  private boolean changePublicationStatusOnMove(PublicationDetail pub, NodePK to) throws
      RemoteException {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changePublicationStatusOnMove()",
        "root.MSG_GEN_ENTER_METHOD", "status = " + pub.getStatus());
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
    SilverTrace.info("kmelia", "KmeliaBmEJB.changePublicationStatusOnMove()",
        "root.MSG_GEN_EXIT_METHOD", "status = " + pub.getStatus()
        + ", indexOperation = " + pub.getIndexOperation());
    return !oldStatus.equals(status);
  }

  /**
   * determine new publication's status according to actual status and current user's profile
   *
   * @param pubDetail
   * @return true if status has changed, false otherwise
   */
  private boolean changePublicationStatusOnUpdate(PublicationDetail pubDetail)
      throws RemoteException {
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
   *
   * @param pubDetail a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
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
    SilverTrace.info("kmelia", "KmeliaBmEJB.updatePublication()",
        "root.MSG_GEN_ENTER_METHOD", "updateScope = " + updateScope);
    try {
      // if pubDetail is a clone
      boolean isClone = isDefined(pubDetail.getCloneId()) && !"-1".equals(pubDetail.getCloneId())
          && !isDefined(pubDetail.getCloneStatus());
      SilverTrace.info("kmelia", "KmeliaBmEJB.updatePublication()", "root.MSG_GEN_PARAM_VALUE",
          "This publication is clone ? " + isClone);
      if (isClone) {
        // update only updateDate
        getPublicationBm().setDetail(pubDetail, forceUpdateDate);
      } else {
        PublicationDetail old = getPublicationDetail(pubDetail.getPK());

        boolean statusChanged = changePublicationStatusOnUpdate(pubDetail);

        getPublicationBm().setDetail(pubDetail, forceUpdateDate);

        if (!isPublicationInBasket(pubDetail.getPK())) {
          if (statusChanged) {
            // creates todos for publishers
            this.createTodosForPublication(pubDetail, false);
          }

          updateSilverContentVisibility(pubDetail);

          // la publication a été modifié par un superviseur
          // le créateur de la publi doit être averti
          String profile =
              KmeliaHelper.getProfile(getOrganizationController().getUserProfiles(pubDetail.
              getUpdaterId(), pubDetail.getPK().getInstanceId()));
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
      // notification pour modification
      sendSubscriptionsNotification(pubDetail, true);

      boolean isNewsManage = getBooleanValue(getOrganizationController().getComponentParameterValue(
          pubDetail.getPK().getInstanceId(), "isNewsManage"));
      if (isNewsManage) {
        // mécanisme de callback
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_HEADER_PUBLICATION_UPDATE,
            Integer.parseInt(pubDetail.getId()), pubDetail.getInstanceId(), pubDetail);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updatePublication()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.updatePublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private boolean isVisibilityPeriodUpdated(PublicationDetail pubDetail, PublicationDetail old) {
    boolean beginVisibilityPeriodUpdated =
        ((pubDetail.getBeginDate() != null && old.getBeginDate() == null) || (pubDetail.
        getBeginDate() == null && old.getBeginDate() != null) || (pubDetail.getBeginDate()
        != null && old.getBeginDate() != null && !pubDetail.getBeginDate().equals(old.
        getBeginDate())));
    boolean endVisibilityPeriodUpdated =
        ((pubDetail.getEndDate() != null && old.getEndDate() == null) || (pubDetail.getEndDate()
        == null && old.getEndDate() != null) || (pubDetail.getEndDate() != null && old.
        getEndDate() != null && !pubDetail.getEndDate().equals(old.getEndDate())));
    return beginVisibilityPeriodUpdated || endVisibilityPeriodUpdated;
  }

  @Override
  public void movePublicationInSameApplication(PublicationPK pubPK, NodePK from, NodePK to,
      String userId)
      throws RemoteException {
    PublicationDetail pub = getPublicationDetail(pubPK);

    // check if user can cut publication from source folder
    String profile = getUserTopicProfile(from, userId);
    boolean cutAllowed = KmeliaPublicationHelper.isCanBeCut(from.getComponentName(), userId,
        profile, pub.getCreator());

    // check if user can paste publication into target folder
    String profileInTarget = getUserTopicProfile(to, userId);
    boolean pasteAllowed = KmeliaPublicationHelper.isCreationAllowed(to, profileInTarget);

    if (cutAllowed && pasteAllowed) {
      movePublicationInSameApplication(pub, to, userId);
    }
  }

  @Override
  public void movePublicationInSameApplication(PublicationDetail pub, NodePK to, String userId)
      throws RemoteException {
    if (to.isTrash()) {
      sendPublicationToBasket(pub.getPK());
    } else {
      // update parent
      getPublicationBm().removeAllFather(pub.getPK());
      getPublicationBm().addFather(pub.getPK(), to);
      processPublicationAfterMove(pub, to, userId);
    }
  }

  @Override
  public void movePublicationInAnotherApplication(PublicationDetail pub, NodePK to, String userId)
      throws RemoteException {
    getPublicationBm().movePublication(pub.getPK(), to, false);
    processPublicationAfterMove(pub, to, userId);
  }

  private void processPublicationAfterMove(PublicationDetail pub, NodePK to, String userId)
      throws RemoteException {
    // update last modifier
    pub.setUpdaterId(userId);

    // status must be checked according to topic rights and last modifier (current user)
    boolean statusChanged = changePublicationStatusOnMove(pub, to);

    // update publication
    getPublicationBm().setDetail(pub, statusChanged);

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
    // send notifications like a creation
    sendSubscriptionsNotification(pub, false);
  }

  private void updatePublication(PublicationPK pubPK, int updateScope) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    updatePublication(pubDetail, updateScope, false);
  }

  @Override
  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId,
      int action) {
    // check if related contribution is managed by kmelia
    if (pubPK != null && StringUtil.isDefined(pubPK.getInstanceId()) &&
        (pubPK.getInstanceId().startsWith("kmelia") || 
         pubPK.getInstanceId().startsWith("toolbox") || 
         pubPK.getInstanceId().startsWith("kmax"))) {

      PublicationDetail pubDetail = null;
      try {
        pubDetail = getPublicationDetail(pubPK);
      } catch (Exception e) {
        // publication no longer exists
        // do not throw exception because this method is called by JMS layer
        // if exception is throw, JMS will attempt to execute it again and again...
        SilverTrace.info("kmelia", "KmeliaBmEJB.externalElementsOfPublicationHaveChanged",
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", "pubPK = " + pubPK.toString(), e);
      }

      // The treatment is stopped if publication is not found or if publication doesn't correspond
      // with parameter of given publication pk. The second condition could happen, for now,
      // with applications dealing with wysiwyg without using publication for their storage
      // (infoletter for example).
      if (pubDetail == null || (StringUtil.isDefined(pubPK.getInstanceId()) &&
          !pubDetail.getInstanceId().equals(pubPK.getInstanceId()))) {
        return;
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
        if ("supervisor".equals(profile) || SilverpeasRole.publisher.isInRole(profile)
            || SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.writer.isInRole(profile)) {
          updatePublication(pubDetail, KmeliaHelper.PUBLICATION_CONTENT, false);
        } else {
          SilverTrace.warn("kmelia", "KmeliaBmEJB.externalElementsOfPublicationHaveChanged",
              "kmelia.PROBLEM_DETECTED", "user " + userId
                  + " is not allowed to update publication " + pubDetail.getPK().toString());
        }
      }
      // index all attached files to taking into account visibility period
      indexExternalElementsOfPublication(pubDetail);
    }
  }

  /**
   * HEAD Delete a publication If this publication is in the basket or in the DZ, it's deleted from
   * the database Else it only send to the basket.
   *
   * @param pubPK the id of the publication to delete
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   */
  @Override
  public void deletePublication(PublicationPK pubPK) {
    // if the publication is in the basket or in the DZ
    // this publication is deleted from the database
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublication()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      // remove form content
      removeXMLContentOfPublication(pubPK);
      // delete all reading controls associated to this publication
      deleteAllReadingControlsByPublication(pubPK);
      // delete all links
      getPublicationBm().removeAllFather(pubPK);
      // delete the publication
      getPublicationBm().removePublication(pubPK);
      // delete reference to contentManager
      deleteSilverContent(pubPK);

      removeExternalElementsOfPublications(pubPK);

    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deletePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION", e);
    }

    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublication()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Send the publication in the basket topic
   *
   * @param pubPK the id of the publication
   * @param kmaxMode
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   * @since 1.0
   */
  @Override
  public void sendPublicationToBasket(PublicationPK pubPK, boolean kmaxMode) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.sendPublicationToBasket()",
        "root.MSG_GEN_ENTER_METHOD");
    try {

      // remove coordinates for Kmax
      if (kmaxMode) {
        CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK.getSpaceId(), pubPK.
            getComponentName());
        PublicationBm pubBm = getPublicationBm();
        Collection<NodePK> fatherPKs = pubBm.getAllFatherPK(pubPK);
        // delete publication coordinates
        Iterator<NodePK> it = fatherPKs.iterator();
        List<String> coordinates = new ArrayList<String>();
        SilverTrace.info("kmelia", "KmeliaBmEJB.sendPublicationToBasket()",
            "root.MSG_GEN_PARAM_VALUE", "fatherPKs" + fatherPKs);
        while (it.hasNext()) {
          String coordinateId = (it.next()).getId();
          coordinates.add(coordinateId);
        }
        if (coordinates.size() > 0) {
          getCoordinatesBm().deleteCoordinates(coordinatePK, (ArrayList<String>) coordinates);
        }
      }

      // remove all links between this publication and topics
      getPublicationBm().removeAllFather(pubPK);
      // add link between this publication and the basket topic
      getPublicationBm().addFather(pubPK, new NodePK("1", pubPK));

      getPublicationBm().deleteIndex(pubPK);

      // remove all the todos attached to the publication
      removeAllTodosForPublication(pubPK);

      // publication is no more accessible
      updateSilverContentVisibility(pubPK, false);

      unIndexExternalElementsOfPublication(pubPK);

      boolean isNewsManage = getBooleanValue(getOrganizationController().getComponentParameterValue(
          pubPK.getInstanceId(), "isNewsManage"));
      if (isNewsManage) {
        // mécanisme de callback
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_PUBLICATION_REMOVE,
            Integer.parseInt(pubPK.getId()), pubPK.getInstanceId(), "");
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.sendPublicationToBasket()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DENVOYER_LA_PUBLICATION_A_LA_CORBEILLE", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.sendPublicationToBasket()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void sendPublicationToBasket(PublicationPK pubPK) {
    sendPublicationToBasket(pubPK, KmeliaHelper.isKmax(pubPK.getInstanceId()));
  }

  /**
   * Add a publication to a topic and send email alerts to topic subscribers
   *
   * @param pubPK the id of the publication
   * @param fatherPK the id of the topic
   * @param isACreation
   */
  @Override
  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK, boolean isACreation) {
    addPublicationToTopicWithoutNotifications(pubPK, fatherPK, isACreation);
    SilverTrace.info("kmelia", "KmeliaBmEJB.addPublicationToTopic()", "root.MSG_GEN_ENTER_METHOD");
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    sendSubscriptionsNotification(pubDetail, false);
    SilverTrace.info("kmelia", "KmeliaBmEJB.addPublicationToTopic()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void addPublicationToTopicWithoutNotifications(PublicationPK pubPK, NodePK fatherPK,
      boolean isACreation) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addPublicationToTopic()", "root.MSG_GEN_ENTER_METHOD");
    PublicationDetail pubDetail = getPublicationDetail(pubPK);

    if (!isACreation) {
      try {
        Collection<NodePK> fathers = getPublicationBm().getAllFatherPK(pubPK);
        if (isPublicationInBasket(pubPK, fathers)) {
          getPublicationBm().removeFather(pubPK, new NodePK("1", fatherPK));

          if (pubDetail.getStatus().equalsIgnoreCase(PublicationDetail.VALID)) {
            // index publication
            getPublicationBm().createIndex(pubPK);

            // index external elements
            indexExternalElementsOfPublication(pubDetail);

            // publication is accessible again
            updateSilverContentVisibility(pubDetail);
          } else if (pubDetail.getStatus().equalsIgnoreCase(
              PublicationDetail.TO_VALIDATE)) {
            // create validation todos for publishers
            createTodosForPublication(pubDetail, true);
          }
        } else if (fathers.isEmpty()) {
          // The publi have got no father
          // change the end date to make this publi visible
          pubDetail.setEndDate(null);
          getPublicationBm().setDetail(pubDetail);

          // publication is accessible again
          updateSilverContentVisibility(pubDetail);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.addPublicationToTopic()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", e);
      }
    }

    try {
      getPublicationBm().addFather(pubPK, fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addPublicationToTopic()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", e);
    }
  }

  private boolean isPublicationInBasket(PublicationPK pubPK) throws RemoteException {
    return isPublicationInBasket(pubPK, null);
  }

  private boolean isPublicationInBasket(PublicationPK pubPK, Collection<NodePK> fathers)
      throws RemoteException {
    if (fathers == null) {
      fathers = getPublicationBm().getAllFatherPK(pubPK);
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

  private NodePK sendSubscriptionsNotification(PublicationDetail pubDetail, boolean update) {
    NodePK oneFather = null;
    // We alert subscribers only if publication is Valid
    if (!pubDetail.haveGotClone() && PublicationDetail.VALID.equals(pubDetail.getStatus())) {
      // topic subscriptions
      Collection<NodePK> fathers = getPublicationFathers(pubDetail.getPK());
      if (fathers != null) {
        for (NodePK father : fathers) {
          oneFather = father;
          sendSubscriptionsNotification(oneFather, pubDetail, update);
        }
      }

      // PDC subscriptions
      try {
        int silverObjectId = getSilverObjectId(pubDetail.getPK());

        List<ClassifyPosition> positions = getPdcBm().getPositions(silverObjectId,
            pubDetail.getPK().getInstanceId());

        PdcSubscriptionUtil pdc = new PdcSubscriptionUtil();

        if (positions != null) {
          for (ClassifyPosition position : positions) {
            pdc.checkSubscriptions(position.getValues(), pubDetail.getPK().getInstanceId(),
                silverObjectId);
          }
        }
      } catch (RemoteException e) {
        SilverTrace.error("kmelia", "KmeliaBmEJB.sendSubscriptionsNotification",
            "kmelia.CANT_SEND_PDC_SUBSCRIPTIONS", e);
      }
    }
    return oneFather;
  }

  private void sendSubscriptionsNotification(NodePK fatherPK,
      PublicationDetail pubDetail, boolean update) {

    // send email alerts
    try {

      // Computing the action
      final NotifAction action;
      if (update) {
        action = NotifAction.UPDATE;
      } else {
        action = NotifAction.CREATE;
      }

      // Building and sending the notification
      UserNotificationHelper
          .buildAndSend(new KmeliaSubscriptionPublicationUserNotification(fatherPK, pubDetail,
          action));

    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendSubscriptionsNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
          + fatherPK.getId() + ", pubId = " + pubDetail.getPK().getId(), e);
    }
  }

  /**
   * Delete a path between publication and topic
   *
   * @param pubPK
   * @param fatherPK
   */
  @Override
  public void deletePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublicationFromTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<NodePK> pubFathers = getPublicationBm().getAllFatherPK(pubPK);
      if (pubFathers.size() >= 2) {
        getPublicationBm().removeFather(pubPK, fatherPK);
      } else {
        // la publication n'a qu'un seul emplacement
        // elle est donc placée dans la corbeille du créateur
        sendPublicationToBasket(pubPK);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.deletePublicationFromTopic()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION_DE_CE_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublicationFromTopic()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void deletePublicationFromAllTopics(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublicationFromAllTopics()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getPublicationBm().removeAllFather(pubPK);

      // la publication n'a qu'un seul emplacement
      // elle est donc placée dans la corbeille du créateur
      sendPublicationToBasket(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.deletePublicationFromAllTopics()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION_DE_CE_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublicationFromAllTopics()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * get all available models
   *
   * @return a Collection of ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @since 1.0
   */
  @Override
  public Collection<ModelDetail> getAllModels() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAllModels()", "root.MSG_GEN_ENTER_METHOD");
    try {
      SilverTrace.info("kmelia", "KmeliaBmEJB.getAllModels()", "root.MSG_GEN_EXIT_METHOD");
      return getPublicationBm().getAllModelsDetail();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAllModels()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_MODELES", e);
    }
  }

  /**
   * Return the detail of a model
   *
   * @param modelId the id of the model
   * @return a ModelDetail
   * @since 1.0
   */
  @Override
  public ModelDetail getModelDetail(String modelId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getModelDetail()", "root.MSG_GEN_ENTER_METHOD");
    try {
      ModelPK modelPK = new ModelPK(modelId);
      SilverTrace.info("kmelia", "KmeliaBmEJB.getModelDetail()",
          "root.MSG_GEN_EXIT_METHOD");
      return getPublicationBm().getModelDetail(modelPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getModelDetail()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_DETAIL_DU_MODELE", e);
    }
  }

  /**
   * Create info attached to a publication
   *
   * @param pubPK the id of the publication
   * @param modelId the id of the selected model
   * @param infos an InfoDetail containing info
   * @since 1.0
   */
  @Override
  public void createInfoDetail(PublicationPK pubPK, String modelId, InfoDetail infos) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ModelPK modelPK = new ModelPK(modelId, pubPK);
      checkIndex(pubPK, infos);
      getPublicationBm().createInfoDetail(pubPK, modelPK, infos);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createInfoDetail()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DENREGISTRER_LE_CONTENU_DU_MODELE", e);
    }
    updatePublication(pubPK, KmeliaHelper.PUBLICATION_CONTENT);
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoDetail()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Create model info attached to a publication
   *
   * @param pubPK the id of the publication
   * @param modelId the id of the selected model
   * @param infos an InfoDetail containing info
   * @since 1.0
   */
  @Override
  public void createInfoModelDetail(PublicationPK pubPK, String modelId, InfoDetail infos) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoModelDetail()", "root.MSG_GEN_ENTER_METHOD");
    try {
      ModelPK modelPK = new ModelPK(modelId, pubPK);
      checkIndex(pubPK, infos);
      getPublicationBm().createInfoModelDetail(pubPK, modelPK, infos);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createInfoModelDetail()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DENREGISTRER_LE_CONTENU_DU_MODELE", e);
    }
    updatePublication(pubPK, KmeliaHelper.PUBLICATION_CONTENT);
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoModelDetail()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * get info attached to a publication
   *
   * @param pubPK the id of the publication
   * @return an InfoDetail
   * @since 1.0
   */
  @Override
  public InfoDetail getInfoDetail(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getInfoDetail()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getPublicationBm().getInfoDetail(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getInfoDetail()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_CONTENU_DU_MODELE", e);
    }
  }

  /**
   * Update info attached to a publication
   *
   * @param pubPK the id of the publication
   * @param infos an InfoDetail containing info to updated
   * @since 1.0
   */
  @Override
  public void updateInfoDetail(PublicationPK pubPK, InfoDetail infos) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.updateInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      checkIndex(pubPK, infos);
      getPublicationBm().updateInfoDetail(pubPK, infos);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updateInfoDetail()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
    updatePublication(pubPK, KmeliaHelper.PUBLICATION_CONTENT);
    SilverTrace.info("kmelia", "KmeliaBmEJB.updateInfoDetail()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Updates the publication links
   *
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  @Override
  public void addInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addInfoLinks()", "root.MSG_GEN_ENTER_METHOD",
        "pubId = " + pubPK.getId() + ", pubIds = " + links.toString());
    try {
      getPublicationBm().addLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addInfoLinks()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
  }

  @Override
  public void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteInfoLinks()", "root.MSG_GEN_ENTER_METHOD",
        "pubId = " + pubPK.getId() + ", pubIds = " + links.toString());
    try {
      getPublicationBm().deleteInfoLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteInfoLinks()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
  }

  @Override
  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getCompletePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    CompletePublication completePublication = null;

    try {
      completePublication = getPublicationBm().getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getCompletePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.getCompletePublication()", "root.MSG_GEN_EXIT_METHOD");
    return completePublication;
  }

  @Override
  public KmeliaPublication getPublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    return KmeliaPublication.aKmeliaPublicationWithPk(pubPK);
  }

  @Override
  public TopicDetail getPublicationFather(PublicationPK pubPK,
      boolean isTreeStructureUsed, String userId, boolean isRightsOnTopicsUsed) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFather()", "root.MSG_GEN_ENTER_METHOD");
    // fetch one of the publication fathers
    NodePK fatherPK = getPublicationFatherPK(pubPK, isTreeStructureUsed, userId,
        isRightsOnTopicsUsed);
    String profile = KmeliaHelper.getProfile(getOrganizationController().getUserProfiles(userId,
        pubPK.getInstanceId()));
    TopicDetail fatherDetail = goTo(fatherPK, userId, isTreeStructureUsed, profile,
        isRightsOnTopicsUsed);
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFather()", "root.MSG_GEN_EXIT_METHOD");
    return fatherDetail;
  }

  public NodePK getPublicationFatherPK(PublicationPK pubPK, boolean isTreeStructureUsed,
      String userId, boolean isRightsOnTopicsUsed) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFatherId()", "root.MSG_GEN_ENTER_METHOD");
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
          if (!father.haveRights() || getOrganizationController().isObjectAvailable(
              father.getRightsDependsOn(), ObjectType.NODE, fatherPK.getInstanceId(), userId)) {
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
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    try {
      Collection<NodePK> fathers = getPublicationBm().getAllFatherPK(pubPK);
      if (fathers == null || fathers.isEmpty()) {
        SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
            "root.MSG_GEN_PARAM_VALUE", "Following publication have got no fathers : pubPK = "
            + pubPK.toString());
        // This publication have got no father !
        // Check if it's a clone (a clone have got no father ever)
        boolean alwaysVisibleModeActivated = StringUtil.getBooleanValue(getOrganizationController().
            getComponentParameterValue(pubPK.getInstanceId(), "publicationAlwaysVisible"));
        if (alwaysVisibleModeActivated) {
          SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
              "root.MSG_GEN_PARAM_VALUE", "Getting the publication");
          PublicationDetail publi = getPublicationBm().getDetail(pubPK);
          if (publi != null) {
            boolean isClone = isDefined(publi.getCloneId()) && !isDefined(publi.getCloneStatus());
            SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
                "root.MSG_GEN_PARAM_VALUE", "This publication is clone ? " + isClone);
            if (isClone) {
              // This publication is a clone
              // Get fathers from main publication
              fathers = getPublicationBm().getAllFatherPK(publi.getClonePK());
              SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
                  "root.MSG_GEN_PARAM_VALUE", "Main publication's fathers fetched. # of fathers = "
                  + fathers.size());
            }
          }
        }
      }
      return fathers;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationFathers()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_UN_PERE_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * gets a list of PublicationDetail corresponding to the links parameter
   *
   * @param links list of publication (componentID + publicationId)
   * @return a list of PublicationDetail
   */
  @Override
  public Collection<PublicationDetail> getPublicationDetails(List<ForeignPK> links) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetails()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<PublicationDetail> publications = null;
    List<PublicationPK> publicationPKs = new ArrayList<PublicationPK>();

    for (ForeignPK link : links) {
      PublicationPK pubPK = new PublicationPK(link.getId(),
          link.getInstanceId());
      publicationPKs.add(pubPK);
    }
    try {
      publications = getPublicationBm().getPublications(publicationPKs);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationDetails()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_PUBLICATIONS", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetails()", "root.MSG_GEN_EXIT_METHOD");
    return publications;
  }

  /**
   * gets a list of authorized publications
   *
   * @param links list of publication defined by his id and component id
   * @param userId identifier User. allow to check if the publication is accessible for current user
   * @param isRightsOnTopicsUsed indicates if the right must be checked
   * @return a collection of Kmelia publication
   * @since 1.0
   */
  @Override
  public Collection<KmeliaPublication> getPublications(List<ForeignPK> links, String userId,
      boolean isRightsOnTopicsUsed) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublications()", "root.MSG_GEN_ENTER_METHOD");
    // initialization of the publications list
    List<ForeignPK> allowedPublicationIds = new ArrayList<ForeignPK>(links);
    if (isRightsOnTopicsUsed) {
      KmeliaSecurity security = new KmeliaSecurity();
      allowedPublicationIds.clear();

      // check if the publication is authorized for current user
      for (ForeignPK link : links) {
        if (security.isObjectAvailable(link.getInstanceId(), userId, link.getId(),
            KmeliaSecurity.PUBLICATION_TYPE)) {
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
   *
   * @param publication the publication from which linked publications are get.
   * @param userId the unique identifier of a user. It allows to check if a linked publication is
   * accessible for the specified user.
   * @return a list of Kmelia publications.
   * @throws RemoteException if an error occurs while communicating with the remote business
   * service.
   */
  @Override
  public List<KmeliaPublication> getLinkedPublications(KmeliaPublication publication,
      String userId) throws RemoteException {
    KmeliaSecurity security = new KmeliaSecurity();
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
   *
   * @param publication the publication from which linked publications are get.
   * @return a list of Kmelia publications.
   * @throws RemoteException if an error occurs while communicating with the remote business
   * service.
   */
  @Override
  public List<KmeliaPublication> getLinkedPublications(KmeliaPublication publication)
      throws RemoteException {
    List<ForeignPK> allLinkIds = publication.getCompleteDetail().getLinkList();
    List<KmeliaPublication> linkedPublications = new ArrayList<KmeliaPublication>(allLinkIds.size());
    for (ForeignPK linkId : allLinkIds) {
      PublicationPK pubPk = new PublicationPK(linkId.getId(), linkId.getInstanceId());
      linkedPublications.add(KmeliaPublication.aKmeliaPublicationWithPk(pubPk));
    }
    return linkedPublications;
  }

  @Override
  public List<KmeliaPublication> getPublicationsToValidate(String componentId, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsToValidate()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<PublicationDetail> publications = new ArrayList<PublicationDetail>();
    PublicationPK pubPK = new PublicationPK("useless", componentId);
    try {
      Collection<PublicationDetail> temp =
          getPublicationBm().getPublicationsByStatus(PublicationDetail.TO_VALIDATE, pubPK);
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
              // inconsistency in database ! Original publication does not exist
              SilverTrace.warn("kmelia", "KmeliaBmEJB.getPublicationsToValidate()",
                  "kmelia.ORIGINAL_PUBLICATION_OF_CLONE_NOT_FOUND", "cloneId = " + publi.getId()
                  + ", originalId=" + publi.getCloneId());
            }
          }
        } else {
          if (isUserCanValidatePublication(publi.getPK(), userId)) {
            publications.add(publi);
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationsToValidate()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_PUBLICATIONS_A_VALIDER", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsToValidate()",
        "root.MSG_GEN_EXIT_METHOD");
    return pubDetails2userPubs(publications);
  }

  private void sendValidationNotification(final NodePK fatherPK, final PublicationDetail pubDetail,
      final String refusalMotive, final String userIdWhoRefuse) {

    try {

      UserNotificationHelper.buildAndSend(new KmeliaValidationPublicationUserNotification(fatherPK,
          pubDetail, refusalMotive,
          userIdWhoRefuse));

    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendValidationNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
          + fatherPK.getId() + ", pubPK = " + pubDetail.getPK(), e);
    }
  }

  private void sendAlertToSupervisors(final NodePK fatherPK, final PublicationDetail pubDetail) {
    if (pubDetail.isValid()) {
      try {
        UserNotificationHelper
            .buildAndSend(new KmeliaSupervisorPublicationUserNotification(fatherPK, pubDetail));
      } catch (Exception e) {
        SilverTrace.warn("kmelia", "KmeliaBmEJB.alertSupervisors()",
            "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
            + fatherPK.getId() + ", pubPK = " + pubDetail.getPK(), e);
      }
    }
  }

  private int getValidationType(String instanceId) {
    String sParam =
        getOrganizationController().getComponentParameterValue(instanceId,
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
  public List<String> getAllValidators(PublicationPK pubPK) throws RemoteException {
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());

    // get all users who have to validate
    List<String> allValidators = new ArrayList<String>();
    if (isTargetedValidationEnabled(pubPK.getInstanceId())) {
      PublicationDetail publi = getPublicationBm().getDetail(pubPK);
      allValidators = getValidatorIds(publi);
    }
    if (allValidators.isEmpty()) {
      // It's not a targeted validation or it is but no validators has
      // been selected !
      List<String> roles = new ArrayList<String>(2);
      roles.add(SilverpeasRole.admin.name());
      roles.add(SilverpeasRole.publisher.name());

      if (KmeliaHelper.isKmax(pubPK.getInstanceId())) {
        allValidators.addAll(Arrays.asList(getOrganizationController().getUsersIdsByRoleNames(
            pubPK.getInstanceId(), roles)));
      } else {
        // get admin and publishers of all nodes where publication is
        List<NodePK> nodePKs = (List<NodePK>) getPublicationFathers(pubPK);
        NodePK nodePK = null;
        NodeDetail node = null;
        boolean oneNodeIsPublic = false;
        for (int n = 0; !oneNodeIsPublic && nodePKs != null && n < nodePKs.size(); n++) {
          nodePK = nodePKs.get(n);
          node = getNodeHeader(nodePK);
          if (node != null) {
            SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators",
                "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK.toString());
            if (!node.haveRights()) {
              allValidators.addAll(Arrays.asList(getOrganizationController().getUsersIdsByRoleNames(
                  pubPK.getInstanceId(), roles)));
              oneNodeIsPublic = true;
            } else {
              allValidators.addAll(Arrays.asList(getOrganizationController().getUsersIdsByRoleNames(
                  pubPK.getInstanceId(), Integer.toString(node.getRightsDependsOn()),
                  ObjectType.NODE, roles)));
            }
          }
        }
      }
    }
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators", "root.MSG_GEN_EXIT_METHOD",
        "pubId = " + pubPK.getId() + ", allValidators = " + allValidators.toString());
    return allValidators;
  }

  /**
   *
   * @param pubPK
   * @param allValidators
   * @return
   * @throws RemoteException
   */
  private boolean isValidationComplete(PublicationPK pubPK, List<String> allValidators)
      throws RemoteException {
    List<ValidationStep> steps = getPublicationBm().getValidationSteps(pubPK);

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

  /* (non-Javadoc)
   * @see com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmBusinessSkeleton#validatePublication(com.stratelia.webactiv.util.publication.model.PublicationPK, java.lang.String, boolean)
   */
  @Override
  public boolean validatePublication(PublicationPK pubPK, String userId, boolean force) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.validatePublication()", "root.MSG_GEN_ENTER_METHOD");
    boolean validationComplete = false;

    try {
      CompletePublication currentPub = getPublicationBm().getCompletePublication(pubPK);
      PublicationDetail currentPubDetail = currentPub.getPublicationDetail();
      boolean validationOnClone = currentPubDetail.haveGotClone();
      PublicationPK validatedPK = pubPK;
      if (validationOnClone) {
        validatedPK = currentPubDetail.getClonePK();
      }
      if (force) {
        validationComplete = true;
      } else {
        int validationType = getValidationType(pubPK.getInstanceId());
        if (validationType == KmeliaHelper.VALIDATION_CLASSIC || validationType
            == KmeliaHelper.VALIDATION_TARGET_1) {
          validationComplete = true;
        } else {
          if (validationType == KmeliaHelper.VALIDATION_TARGET_N) {
            // check that validators are well defined
            // If not, considering validation as classic one
            PublicationDetail publi = getPublicationBm().getDetail(validatedPK);
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

              ValidationStep validation = new ValidationStep(validatedPK, userId,
                  PublicationDetail.VALID);
              getPublicationBm().addValidationStep(validation);
              // check if all validators have give their decision
              validationComplete = isValidationComplete(validatedPK, allValidators);
            }
          }
        }

      }

      if (validationComplete) {
        removeAllTodosForPublication(validatedPK);
        if (validationOnClone) {
          removeAllTodosForPublication(pubPK);
        }
        if (currentPubDetail.haveGotClone()) {
          currentPubDetail = mergeClone(currentPub, userId);
        } else if (currentPubDetail.isValidationRequired()) {
          currentPubDetail.setValidatorId(userId);
          currentPubDetail.setValidateDate(new Date());
          currentPubDetail.setStatus(PublicationDetail.VALID);
        }
        KmeliaHelper.checkIndex(currentPubDetail);
        getPublicationBm().setDetail(currentPubDetail);
        updateSilverContentVisibility(currentPubDetail);
        // index all publication's elements
        indexExternalElementsOfPublication(currentPubDetail);
        // the publication has been validated
        // we must alert all subscribers of the different topics
        NodePK oneFather = sendSubscriptionsNotification(currentPubDetail, false);

        // we have to alert publication's creator
        sendValidationNotification(oneFather, currentPubDetail, null, userId);

        // alert supervisors
        sendAlertToSupervisors(oneFather, currentPubDetail);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.validatePublication()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DE_VALIDER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.validatePublication()",
        "root.MSG_GEN_EXIT_METHOD", "validationComplete = " + validationComplete);
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

    return clone;
  }

  private PublicationDetail mergeClone(CompletePublication currentPub,
      String userId) throws RemoteException, FormException, PublicationTemplateException,
      AttachmentException {
    PublicationDetail currentPubDetail = currentPub.getPublicationDetail();
    String memInfoId = currentPubDetail.getInfoId();
    PublicationPK pubPK = currentPubDetail.getPK();
    // merge du clone sur la publi de référence
    String cloneId = currentPubDetail.getCloneId();
    if (!"-1".equals(cloneId)) {
      PublicationPK tempPK = new PublicationPK(cloneId, pubPK);
      CompletePublication tempPubli = getPublicationBm().getCompletePublication(tempPK);
      PublicationDetail tempPubliDetail = tempPubli.getPublicationDetail();
      // le clone devient la publi de référence
      currentPubDetail = getClone(tempPubliDetail);

      currentPubDetail.setPk(pubPK);
      if (userId != null) {
        currentPubDetail.setValidatorId(userId);
        currentPubDetail.setValidateDate(new Date());
      }
      currentPubDetail.setStatus(PublicationDetail.VALID);
      currentPubDetail.setCloneId("-1");
      currentPubDetail.setCloneStatus(null);
      // merge des fichiers joints
      ForeignPK pkFrom = new ForeignPK(pubPK.getId(), pubPK.getInstanceId());
      ForeignPK pkTo = new ForeignPK(cloneId, tempPK.getInstanceId());
      Map<String, String> attachmentIds = AttachmentServiceFactory.getAttachmentService().
          mergeDocuments(pkFrom, pkTo, DocumentType.attachment);
      // merge du contenu DBModel
      if (tempPubli.getModelDetail() != null) {
        if (currentPub.getModelDetail() != null) {
          currentPubDetail.setInfoId(memInfoId);

          // il existait déjà un contenu
          getPublicationBm().updateInfoDetail(pubPK, tempPubli.getInfoDetail());
        } else {
          // il n'y avait pas encore de contenu
          ModelPK modelPK = new ModelPK(tempPubli.getModelDetail().getId(), "useless",
              tempPK.getInstanceId());
          getPublicationBm().createInfoModelDetail(pubPK, modelPK, tempPubli.getInfoDetail());

          // recupere nouvel infoId
          PublicationDetail modifiedPubli = getPublicationDetail(pubPK);
          currentPubDetail.setInfoId(modifiedPubli.getInfoId());
        }
      } else {
        // merge du contenu XMLModel
        String infoId = tempPubli.getPublicationDetail().getInfoId();
        if (infoId != null && !"0".equals(infoId) && !isInteger(infoId)) {
          // register xmlForm to publication
          String xmlFormShortName = infoId;

          // get xmlContent to paste

          PublicationTemplateManager publicationTemplateManager = PublicationTemplateManager.
              getInstance();
          PublicationTemplate pubTemplate = publicationTemplateManager.
              getPublicationTemplate(
              tempPK.getInstanceId() + ":" + xmlFormShortName);

          RecordSet set = pubTemplate.getRecordSet();
          // DataRecord data = set.getRecord(fromId);

          if (memInfoId != null && !"0".equals(memInfoId)) {
            // il existait déjà un contenu
            set.merge(cloneId, pubPK.getInstanceId(), pubPK.getId(), pubPK.getInstanceId(),
                attachmentIds);
          } else {
            // il n'y avait pas encore de contenu
            publicationTemplateManager.addDynamicPublicationTemplate(tempPK.getInstanceId()
                + ":" + xmlFormShortName, xmlFormShortName + ".xml");

            set.clone(cloneId, pubPK.getInstanceId(), pubPK.getId(), pubPK.getInstanceId(),
                attachmentIds);
          }
        }
      }
      // merge du contenu Wysiwyg
      boolean cloneWysiwyg = WysiwygController.haveGotWysiwyg(tempPK.getInstanceId(), cloneId,
          tempPubli.getPublicationDetail().getLanguage());
      if (cloneWysiwyg) {
        WysiwygController.copy(tempPK.getInstanceId(), cloneId, pubPK.getInstanceId(),
            pubPK.getId(), tempPubli.getPublicationDetail().getUpdaterId());
      }
      // delete xml content
      removeXMLContentOfPublication(tempPK);
      // suppression du clone
      deletePublication(tempPK);
    }
    return currentPubDetail;
  }

  @Override
  public void unvalidatePublication(PublicationPK pubPK, String userId,
      String refusalMotive, int validationType) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.unvalidatePublication()", "root.MSG_GEN_ENTER_METHOD");
    try {
      switch (validationType) {
        case KmeliaHelper.VALIDATION_COLLEGIATE:
        case KmeliaHelper.VALIDATION_TARGET_N:
          // reset other decisions
          getPublicationBm().removeValidationSteps(pubPK);
          break;
        case KmeliaHelper.VALIDATION_CLASSIC:
        case KmeliaHelper.VALIDATION_TARGET_1:
        default:
          break;// do nothing
        }

      PublicationDetail currentPubDetail = getPublicationBm().getDetail(pubPK);

      if (currentPubDetail.haveGotClone()) {
        String cloneId = currentPubDetail.getCloneId();
        PublicationPK tempPK = new PublicationPK(cloneId, pubPK);
        PublicationDetail clone = getPublicationBm().getDetail(tempPK);

        // change clone's status
        clone.setStatus("UnValidate");
        clone.setIndexOperation(IndexManager.NONE);
        getPublicationBm().setDetail(clone);

        // Modification de la publication de reference
        currentPubDetail.setCloneStatus(PublicationDetail.REFUSED);
        currentPubDetail.setUpdateDateMustBeSet(false);
        getPublicationBm().setDetail(currentPubDetail);

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

        getPublicationBm().setDetail(currentPubDetail);

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
      throw new KmeliaRuntimeException("KmeliaBmEJB.unvalidatePublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_REFUSER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.unvalidatePublication()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void suspendPublication(PublicationPK pubPK, String defermentMotive,
      String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.suspendPublication()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      PublicationDetail currentPubDetail = getPublicationBm().getDetail(pubPK);

      // change publication's status
      currentPubDetail.setStatus(PublicationDetail.TO_VALIDATE);

      KmeliaHelper.checkIndex(currentPubDetail);

      getPublicationBm().setDetail(currentPubDetail);

      // change visibility over PDC
      updateSilverContentVisibility(currentPubDetail);

      unIndexExternalElementsOfPublication(currentPubDetail.getPK());

      // we have to alert publication's creator
      sendDefermentNotification(currentPubDetail, defermentMotive, userId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.unvalidatePublication()",
          ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_REFUSER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.suspendPublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void sendDefermentNotification(final PublicationDetail pubDetail,
      final String defermentMotive,
      final String senderId) {
    try {
      UserNotificationHelper.buildAndSend(new KmeliaDefermentPublicationUserNotification(pubDetail,
          defermentMotive));
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendDefermentNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "pubPK = "
          + pubDetail.getPK(), e);
    }
  }

  /**
   * Change publication status from draft to valid (for publisher) or toValidate (for redactor).
   *
   * @param pubPK
   * @param topicPK
   * @param userProfile
   */
  @Override
  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK, String userProfile) {
    PublicationDetail pubDetail = draftOutPublicationWithoutNotifications(pubPK, topicPK,
        userProfile);
    indexExternalElementsOfPublication(pubDetail);
    sendTodosAndNotificationsOnDraftOut(pubDetail, topicPK, userProfile);
  }

  /**
   * This method is here to manage correctly transactional scope of EJB (conflicted by EJB and
   * UserPreferences service)
   */
  @Override
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
    SilverTrace.info("kmelia", "KmeliaBmEJB.draftOutPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());

    try {
      CompletePublication currentPub = getPublicationBm().getCompletePublication(pubPK);
      PublicationDetail pubDetail = currentPub.getPublicationDetail();

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftOutPublication()",
          "root.MSG_GEN_PARAM_VALUE", "actual status = " + pubDetail.getStatus());
      if (userProfile.equals("publisher") || userProfile.equals("admin")) {
        if (pubDetail.haveGotClone()) {
          pubDetail = mergeClone(currentPub, null);
        }

        pubDetail.setStatus(PublicationDetail.VALID);
      } else {
        if (pubDetail.haveGotClone()) {
          // changement du statut du clone
          PublicationDetail clone = getPublicationBm().getDetail(pubDetail.getClonePK());
          clone.setStatus(PublicationDetail.TO_VALIDATE);
          clone.setIndexOperation(IndexManager.NONE);
          clone.setUpdateDateMustBeSet(false);
          getPublicationBm().setDetail(clone);
          pubDetail.setCloneStatus(PublicationDetail.TO_VALIDATE);
        } else {
          pubDetail.setStatus(PublicationDetail.TO_VALIDATE);
        }
      }

      KmeliaHelper.checkIndex(pubDetail);

      getPublicationBm().setDetail(pubDetail, forceUpdateDate);

      if (!KmeliaHelper.isKmax(pubDetail.getInstanceId())) {
        // update visibility attribute on PDC
        updateSilverContentVisibility(pubDetail);
      }

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftOutPublication()",
          "root.MSG_GEN_PARAM_VALUE", "new status = " + pubDetail.getStatus());

      if (!inTransaction) {
        // index all publication's elements
        indexExternalElementsOfPublication(pubDetail);

        sendTodosAndNotificationsOnDraftOut(pubDetail, topicPK, userProfile);
      }

      return pubDetail;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.draftOutPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
  }

  private void sendTodosAndNotificationsOnDraftOut(PublicationDetail pubDetail, NodePK topicPK,
      String userProfile) {
    if (SilverpeasRole.writer.isInRole(userProfile)) {
      try {
        createTodosForPublication(pubDetail, true);
      } catch (RemoteException e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.sendTodosAndNotificationsOnDraftOut()", ERROR,
            "kmelia.CANT_CREATE_TODOS", e);
      }
    }

    // Subscriptions and supervisors are supported by kmelia and filebox only
    if (!KmeliaHelper.isKmax(pubDetail.getInstanceId())) {
      // alert subscribers
      sendSubscriptionsNotification(pubDetail, false);

      // alert supervisors
      if (topicPK != null) {
        sendAlertToSupervisors(topicPK, pubDetail);
      }
    }
  }

  /**
   * Change publication status from any state to draft.
   *
   * @param pubPK
   */
  @Override
  public void draftInPublication(PublicationPK pubPK) {
    draftInPublication(pubPK, null);
  }

  @Override
  public void draftInPublication(PublicationPK pubPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.draftInPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    try {
      PublicationDetail pubDetail = getPublicationBm().getDetail(pubPK);
      SilverTrace.info("kmelia", "KmeliaBmEJB.draftInPublication()",
          "root.MSG_GEN_PARAM_VALUE", "actual status = " + pubDetail.getStatus());
      pubDetail.setStatus(PublicationDetail.DRAFT);
      pubDetail.setUpdaterId(userId);
      KmeliaHelper.checkIndex(pubDetail);
      getPublicationBm().setDetail(pubDetail);
      updateSilverContentVisibility(pubDetail);
      unIndexExternalElementsOfPublication(pubDetail.getPK());
      removeAllTodosForPublication(pubPK);

      boolean isNewsManage = getBooleanValue(getOrganizationController().getComponentParameterValue(
          pubDetail.getPK().getInstanceId(), "isNewsManage"));
      if (isNewsManage) {
        // mécanisme de callback
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_PUBLICATION_REMOVE,
            Integer.parseInt(pubDetail.getId()), pubDetail.getInstanceId(), pubDetail);
      }

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftInPublication()",
          "root.MSG_GEN_PARAM_VALUE", "new status = " + pubDetail.getStatus());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.draftInPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
  }

  @Override
  public NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK,
      NodePK topicPK, String senderName) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAlertNotificationMetaData()",
        "root.MSG_GEN_ENTER_METHOD");

    final PublicationDetail pubDetail = getPublicationDetail(pubPK);

    final NotificationMetaData notifMetaData =
        UserNotificationHelper.build(new KmeliaSubscriptionPublicationUserNotification(topicPK,
        pubDetail, NotifAction.REPORT, senderName));

    SilverTrace.info("kmelia", "KmeliaBmEJB.getAlertNotificationMetaData()",
        "root.MSG_GEN_EXIT_METHOD");

    return notifMetaData;
  }

  /**
   * @param pubPK
   * @param documentPk
   * @param topicPK
   * @param senderName
   * @return
   * @throws RemoteException
   */
  @Override
  public NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK,
      SimpleDocumentPK documentPk, NodePK topicPK, String senderName) throws RemoteException {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAlertNotificationMetaData(document)",
        "root.MSG_GEN_ENTER_METHOD");

    final PublicationDetail pubDetail = getPublicationDetail(pubPK);
    final SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(documentPk, null).getLastPublicVersion();

    final NotificationMetaData notifMetaData = UserNotificationHelper
        .build(new KmeliaDocumentSubscriptionPublicationUserNotification(topicPK, pubDetail,
        document, senderName));

    SilverTrace.info("kmelia", "KmeliaBmEJB.getAlertNotificationMetaData(document)",
        "root.MSG_GEN_EXIT_METHOD");
    return notifMetaData;
  }

  /**
   * delete reading controls to a publication
   *
   * @param pubPK the id of a publication
   * @since 1.0
   */
  @Override
  public void deleteAllReadingControlsByPublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteAllReadingControlsByPublication()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getStatisticBm().deleteHistoryByAction(new ForeignPK(pubPK.getId(), pubPK.getInstanceId()),
          1, "Publication");
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteAllReadingControlsByPublication()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_CONTROLES_DE_LECTURE", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteAllReadingControlsByPublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void indexKmelia(String componentId) {
    indexTopics(new NodePK("useless", componentId));
    indexPublications(new PublicationPK("useless", componentId));
  }

  private void indexPublications(PublicationPK pubPK) {
    Collection<PublicationDetail> pubs = null;
    try {
      pubs = getPublicationBm().getAllPublications(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.indexPublications()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_PUBLICATIONS", e);
    }

    if (pubs != null) {
      for (PublicationDetail pub : pubs) {
        try {
          pubPK = pub.getPK();
          List<NodePK> pubFathers = null;
          // index only valid publications
          if (pub.getStatus() != null && pub.isValid()) {
            pubFathers = (List<NodePK>) getPublicationBm().getAllFatherPK(pubPK);
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
          throw new KmeliaRuntimeException("KmeliaBmEJB.indexPublications()",
              ERROR, "kmelia.EX_IMPOSSIBLE_DINDEXER_LA_PUBLICATION",
              "pubPK = " + pubPK.toString(), e);
        }
      }
    }
  }

  private void indexPublication(PublicationDetail pub) throws RemoteException {
    // index publication itself
    getPublicationBm().createIndex(pub.getPK());

    // index external elements
    indexExternalElementsOfPublication(pub);
  }

  private void indexTopics(NodePK nodePK) {
    try {
      Collection<NodeDetail> nodes = getNodeBm().getAllNodes(nodePK);
      if (nodes != null) {
        for (NodeDetail node : nodes) {
          if (!node.getNodePK().isRoot() && !node.getNodePK().isTrash()
              && !node.getNodePK().getId().equals("2")) {
            getNodeBm().createIndex(node);
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.indexTopics()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_THEMES", e);
    }
  }

  /*
   * Creates todos for all publishers of this kmelia instance
   * @param pubDetail publication to be validated
   * @param creation true if it's the creation of the publi
   */
  private void createTodosForPublication(PublicationDetail pubDetail,
      boolean creation) throws RemoteException {
    if (!creation) {
      /* remove all todos attached to that publication */
      removeAllTodosForPublication(pubDetail.getPK());
    }
    if (pubDetail.isValidationRequired()
        || PublicationDetail.TO_VALIDATE.equalsIgnoreCase(pubDetail.getCloneStatus())) {
      int validationType = getValidationType(pubDetail.getPK().getInstanceId());
      if (validationType == KmeliaHelper.VALIDATION_TARGET_N || validationType
          == KmeliaHelper.VALIDATION_COLLEGIATE) {
        // removing potential older validation decision
        getPublicationBm().removeValidationSteps(pubDetail.getPK());
      }
      List<String> validators = getAllValidators(pubDetail.getPK());
      String[] users = validators.toArray(new String[validators.size()]);
      // For each publisher create a todo
      addTodo(pubDetail, users);
      // Send a notification to alert admins and publishers
      sendValidationAlert(pubDetail, users);
    }
  }

  private String addTodo(PublicationDetail pubDetail, String[] users) {
    ResourceLocator message = new ResourceLocator(
        "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", "fr");

    TodoDetail todo = new TodoDetail();

    todo.setId(pubDetail.getPK().getId());
    todo.setSpaceId(pubDetail.getPK().getSpace());
    todo.setComponentId(pubDetail.getPK().getComponentName());
    todo.setName(message.getString("ToValidateShort") + " : "
        + pubDetail.getName());

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

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    return todoBBA.addEntry(todo);
  }

  /*
   * Remove todos for all pubishers of this kmelia instance
   * @param pubDetail corresponding publication
   */
  private void removeAllTodosForPublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeAllTodosForPublication()",
        "root.MSG_GEN_ENTER_METHOD", "Enter pubPK =" + pubPK.toString());

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    todoBBA.removeEntriesFromExternal("useless", pubPK.getInstanceId(), pubPK.getId());
  }

  private void removeTodoForPublication(PublicationPK pubPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeTodoForPublication()",
        "root.MSG_GEN_ENTER_METHOD", "Enter pubPK =" + pubPK.toString());

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    todoBBA.removeAttendeeToEntryFromExternal(pubPK.getInstanceId(), pubPK.getId(), userId);
  }

  private void sendValidationAlert(final PublicationDetail pubDetail, final String[] users) {
    UserNotificationHelper.buildAndSend(new KmeliaPendingValidationPublicationUserNotification(
        pubDetail, users));
  }

  private void sendModificationAlert(final int modificationScope, final PublicationDetail pubDetail) {
    UserNotificationHelper.buildAndSend(new KmeliaModificationPublicationUserNotification(pubDetail,
        modificationScope));
  }

  @Override
  public void sendModificationAlert(int modificationScope, PublicationPK pubPK) {
    sendModificationAlert(modificationScope, getPublicationDetail(pubPK));
  }

  @Override
  public int getSilverObjectId(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());
    int silverObjectId = -1;
    PublicationDetail pubDetail = null;
    try {
      silverObjectId = getKmeliaContentManager().getSilverObjectId(
          pubPK.getId(), pubPK.getInstanceId());
      if (silverObjectId == -1) {
        pubDetail = getPublicationDetail(pubPK);
        silverObjectId = createSilverContent(pubDetail, pubDetail.getCreatorId());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSilverObjectId()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(PublicationDetail pubDetail, String creatorId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createSilverContent()", "root.MSG_GEN_ENTER_METHOD",
        "pubId = " + pubDetail.getPK().getId());
    Connection con = null;
    try {
      con = getConnection();
      return getKmeliaContentManager().createSilverContent(con, pubDetail, creatorId);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createSilverContent()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void deleteSilverContent(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());
    Connection con = getConnection();
    try {
      getKmeliaContentManager().deleteSilverContent(con, pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteSilverContent()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    } finally {
      freeConnection(con);
    }
  }

  private void updateSilverContentVisibility(PublicationDetail pubDetail) {
    try {
      getKmeliaContentManager().updateSilverContentVisibility(pubDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updateSilverContentVisibility()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  private void updateSilverContentVisibility(PublicationPK pubPK,
      boolean isVisible) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    try {
      getKmeliaContentManager().updateSilverContentVisibility(pubDetail,
          isVisible);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updateSilverContentVisibility()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  private KmeliaContentManager getKmeliaContentManager() {
    return new KmeliaContentManager();
  }

  private void checkIndex(PublicationPK pubPK, InfoDetail infos) {
    infos.setIndexOperation(IndexManager.NONE);
  }

  private void indexExternalElementsOfPublication(PublicationDetail pubDetail) {
    if (KmeliaHelper.isIndexable(pubDetail)) {
      try {
        AttachmentServiceFactory.getAttachmentService().indexAllDocuments(pubDetail.getPK(),
            pubDetail.getBeginDate(), pubDetail.getEndDate());
      } catch (Exception e) {
        SilverTrace.error("kmelia", "KmeliaBmEJB.indexExternalElementsOfPublication",
            "Indexing versioning documents failed", "pubPK = " + pubDetail.getPK().toString(), e);
      }
      try {
        // index comments
        getCommentService().indexAllCommentsOnPublication(pubDetail.getContributionType(),
            pubDetail.getPK());
      } catch (Exception e) {
        SilverTrace.error("kmelia", "KmeliaBmEJB.indexExternalElementsOfPublication",
            "Indexing comments failed", "pubPK = " + pubDetail.getPK().toString(), e);
      }
    }
  }

  private void unIndexExternalElementsOfPublication(PublicationPK pubPK) {
    try {
      AttachmentServiceFactory.getAttachmentService().unindexAttachmentsOfExternalObject(pubPK);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaBmEJB.indexExternalElementsOfPublication",
          "Indexing versioning documents failed", "pubPK = " + pubPK.toString(), e);
    }
    try {
      // index comments
      getCommentService().unindexAllCommentsOnPublication(PublicationDetail.getResourceType(),
          pubPK);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaBmEJB.indexExternalElementsOfPublication",
          "Indexing comments failed", "pubPK = " + pubPK.toString(), e);
    }
  }

  private void removeExternalElementsOfPublications(PublicationPK pubPK) {
    // remove attachments
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listAllDocumentsByForeignKey(pubPK, null);
    for (SimpleDocument doc : documents) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc);
    }
    // remove comments
    try {
      getCommentService().deleteAllCommentsOnPublication(PublicationDetail.getResourceType(), pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.removeExternalElementsOfPublications()",
          ERROR, "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COMMENTAIRES", e);
    }

    // remove Wysiwyg content
    try {
      WysiwygController.deleteWysiwygAttachments(pubPK.getInstanceId(), pubPK.getId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.removeExternalElementsOfPublications",
          ERROR, "root.EX_DELETE_ATTACHMENT_FAILED", e);
    }

    // remove Thumbnail content
    try {
      ThumbnailDetail thumbToDelete = new ThumbnailDetail(pubPK.getInstanceId(),
          Integer.parseInt(pubPK.getId()),
          ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      ThumbnailController.deleteThumbnail(thumbToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.removeExternalElementsOfPublications",
          ERROR, "root.EX_DELETE_THUMBNAIL_FAILED", e);
    }
  }

  @Override
  public void removeContentOfPublication(PublicationPK pubPK) {
    // remove XML content
    PublicationDetail publication = getPublicationDetail(pubPK);
    if (!com.silverpeas.util.StringUtil.isInteger(publication.getInfoId())) {
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
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeXMLContentOfPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (FormException e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeXMLContentOfPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(SILVERPEAS_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getConnection()", ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("kmelia", "KmeliaBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  @Override
  public void addModelUsed(String[] models, String instanceId, String nodeId) {
    Connection con = getConnection();
    try {
      ModelDAO.deleteModel(con, instanceId, nodeId);
      for (String modelId : models) {
        ModelDAO.addModel(con, instanceId, modelId, nodeId);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addModelUsed()", ERROR,
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
      Collection<String> result = com.silverpeas.formTemplate.dao.ModelDAO.getModelUsed(con,
          instanceId, nodeId);
      if (isDefined(nodeId) && result.isEmpty()) {
        // there is no templates defined for the given node, check the parent nodes
        Collection<NodeDetail> parents = getNodeBm().getPath(new NodePK(nodeId, instanceId));
        Iterator<NodeDetail> iter = parents.iterator();
        while (iter.hasNext() && result.isEmpty()) {
          NodeDetail parent = iter.next();
          result = com.silverpeas.formTemplate.dao.ModelDAO.getModelUsed(con, instanceId, parent.
              getNodePK().getId());
        }
      }
      return result;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getModelUsed()", ERROR,
          "kmelia.IMPOSSIBLE_DE_RECUPERER_LES_MODELES", e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }
  }

  @Override
  public List<NodeDetail> getAxis(String componentId) {
    ResourceLocator nodeSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.node.nodeSettings", "");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    SilverTrace.info("kmax", "KmeliaBmEjb.getAxis()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId
        + " sortField=" + sortField + " sortOrder=" + sortOrder);

    List<NodeDetail> axis = new ArrayList<NodeDetail>();
    try {
      List<NodeDetail> headers = getAxisHeaders(componentId);
      for (NodeDetail header : headers) {
        // Do not get hidden nodes (Basket and unclassified)
        if (!NodeDetail.STATUS_INVISIBLE.equals(header.getStatus())) {
          // get content  of  this axis
          axis.addAll(getNodeBm().getSubTree(header.getNodePK(), sortField + " " + sortOrder));
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_AXES", e);
    }
    return axis;
  }

  @Override
  public List<NodeDetail> getAxisHeaders(String componentId) {
    List<NodeDetail> axisHeaders = null;
    try {
      axisHeaders = getNodeBm().getHeadersByLevel(
          new NodePK("useless", componentId), 2);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getAxisHeaders()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_ENTETES_DES_AXES", e);
    }
    return axisHeaders;
  }

  @Override
  public NodePK addAxis(NodeDetail axis, String componentId) {
    NodePK axisPK = new NodePK("toDefine", componentId);
    NodeDetail rootDetail = new NodeDetail(new NodePK("0"), "Root", "desc",
        "unknown", "unknown", "/0", 1, new NodePK("-1"), null);
    rootDetail.setStatus(NodeDetail.STATUS_VISIBLE);
    axis.setNodePK(axisPK);
    CoordinatePK coordinatePK = new CoordinatePK("useless", axisPK);
    try {
      // axis creation
      axisPK = getNodeBm().createNode(axis, rootDetail);
      // add this new axis to existing coordinates
      CoordinatePoint point = new CoordinatePoint(-1, Integer.parseInt(axisPK.getId()), true);
      getCoordinatesBm().addPointToAllCoordinates(coordinatePK, point);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_CREER_L_AXE", e);
    }
    return axisPK;
  }

  @Override
  public void updateAxis(NodeDetail axis, String componentId) {
    axis.getNodePK().setComponentName(componentId);
    SilverTrace.info("kmax", "KmeliaBmEjb.updateAxis()", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId + " nodePk.getComponentId()=" + axis.getNodePK().
        getInstanceId());
    try {
      getNodeBm().setDetail(axis);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.updateAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_MODIFIER_L_AXE", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void deleteAxis(String axisId, String componentId) {
    NodePK pkToDelete = new NodePK(axisId, componentId);
    PublicationPK pubPK = new PublicationPK("useless");

    CoordinatePK coordinatePK = new CoordinatePK("useless", pkToDelete);
    List<String> fatherIds = new ArrayList<String>();
    Collection<String> coordinateIds = null;
    // Delete the axis
    try {
      // delete publicationFathers
      if (getAxisHeaders(componentId).size() == 1) {
        coordinateIds = getCoordinatesBm().getCoordinateIdsByNodeId(coordinatePK, axisId);
        Iterator<String> coordinateIdsIt = coordinateIds.iterator();
        String coordinateId = "";
        while (coordinateIdsIt.hasNext()) {
          coordinateId = coordinateIdsIt.next();
          fatherIds.add(coordinateId);
        }
        if (fatherIds.size() > 0) {
          getPublicationBm().removeFathers(pubPK, fatherIds);
        }
      }
      // delete coordinate which contains subComponents of this component
      Collection<NodeDetail> subComponents = getNodeBm().getDescendantDetails(pkToDelete);
      Iterator<NodeDetail> it = subComponents.iterator();
      List<NodePK> points = new ArrayList<NodePK>();
      points.add(pkToDelete);
      while (it.hasNext()) {
        points.add((it.next()).getNodePK());
      }
      removeCoordinatesByPoints(points, componentId);
      // delete axis
      getNodeBm().removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.deleteAxis()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_L_AXE", e);
    }
  }

  private void removeCoordinatesByPoints(List<NodePK> nodePKs, String componentId) {
    Iterator<NodePK> it = nodePKs.iterator();
    List<String> coordinatePoints = new ArrayList<String>();
    String nodeId = "";
    while (it.hasNext()) {
      nodeId = (it.next()).getId();
      coordinatePoints.add(nodeId);
    }
    CoordinatePK coordinatePK = new CoordinatePK("useless", "useless", componentId);
    try {
      getCoordinatesBm().deleteCoordinatesByPoints(coordinatePK,
          (ArrayList<String>) coordinatePoints);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.removeCoordinatesByPoints()", ERROR,
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
      nodeDetail = getNodeBm().getHeader(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getNodeHeader()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
    }
    return nodeDetail;
  }

  @Override
  public NodePK addPosition(String fatherId, NodeDetail position,
      String componentId, String userId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.addPosition()", "root.MSG_GEN_PARAM_VALUE",
        "fatherId = " + fatherId + " And position = " + position.toString());
    position.getNodePK().setComponentName(componentId);
    position.setCreationDate(DateUtil.today2SQLDate());
    position.setCreatorId(userId);
    NodeDetail fatherDetail = null;
    NodePK componentPK = null;

    fatherDetail = getNodeHeader(fatherId, componentId);
    SilverTrace.info("kmax", "KmeliaBmEjb.addPosition()", "root.MSG_GEN_PARAM_VALUE",
        "fatherDetail = " + fatherDetail.toString());
    try {
      componentPK = getNodeBm().createNode(position, fatherDetail);
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
      getNodeBm().setDetail(position);
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
      Collection<NodeDetail> subComponents = getNodeBm().getDescendantDetails(pkToDelete);
      Iterator<NodeDetail> it = subComponents.iterator();
      List<NodePK> points = new ArrayList<NodePK>();
      points.add(pkToDelete);
      while (it.hasNext()) {
        points.add((it.next()).getNodePK());
      }
      removeCoordinatesByPoints(points, componentId);
      // delete component
      getNodeBm().removeNode(pkToDelete);
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
    NodeBm nodeBm = getNodeBm();
    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeBm.getPath(nodePK);
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
    SilverTrace.info("kmax", "KmeliaBmEJB.getKmaxPathList()", "root.MSG_GEN_ENTER_METHOD");
    Collection<Coordinate> coordinates = null;
    try {
      coordinates = getPublicationCoordinates(pubPK.getId(), pubPK.getInstanceId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getKmaxPathList()", ERROR,
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
    SilverTrace.info("kmax", "KmeliaBmEjb.search()", "root.MSG_GEN_PARAM_VALUE",
        "publications = " + publications);
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
      int nodeLevel = 0;
      String axisValue = "";
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
        publications = getPublicationBm().getDetailsNotInFatherPK(basketPK);
      } else {
        if (combination != null && combination.size() > 0) {
          coordinates = getCoordinatesBm().getCoordinatesByFatherPaths(
              (ArrayList<String>) combination, coordinatePK);
        }
        if (!coordinates.isEmpty()) {
          publications = getPublicationBm().getDetailsByFatherIds((ArrayList<String>) coordinates,
              pk, false);
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.search()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_RESULTATS", e);
    }
    return publications;
  }

  @Override
  public Collection<KmeliaPublication> getUnbalancedPublications(String componentId) {
    PublicationPK pk = new PublicationPK("useless", componentId);
    Collection<PublicationDetail> publications = null;
    try {
      publications = getPublicationBm().getOrphanPublications(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getUnbalancedPublications()", ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_PUBLICATIONS_NON_CLASSEES", e);
    }
    return pubDetails2userPubs(publications);
  }

  private Collection<PublicationDetail> filterPublicationsByBeginDate(
      Collection<PublicationDetail> publications,
      int nbDays) {
    List<PublicationDetail> pubOK = new ArrayList<PublicationDetail>();
    if (publications != null) {
      Calendar rightNow = Calendar.getInstance();
      if (nbDays == 0) {
        nbDays = 1;
      }
      rightNow.add(Calendar.DATE, 0 - nbDays);
      Date day = rightNow.getTime();
      Iterator<PublicationDetail> it = publications.iterator();
      PublicationDetail pub = null;
      Date dateToCompare = null;
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
      Collection<NodeDetail> nodes = getNodeBm().getAllNodes(nodePK);
      if (nodes != null) {
        for (NodeDetail nodeDetail : nodes) {
          if ("corbeille".equalsIgnoreCase(nodeDetail.getName()) && nodeDetail.getNodePK().isTrash()) {
            // do not index the bin
          } else {
            getNodeBm().createIndex(nodeDetail);
          }
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.indexAxis()",
          ERROR, "kmax.EX_IMPOSSIBLE_DINDEXER_LES_AXES", e);
    }
  }

  @Override
  public KmeliaPublication getKmaxPublication(String pubId, String currentUserId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.getKmaxCompletePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = null;
    CompletePublication completePublication = null;
    PublicationBm pubBm = getPublicationBm();
    try {
      pubPK = new PublicationPK(pubId);
      completePublication = pubBm.getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmaxRuntimeException(
          "KmeliaBmEjb.getKmaxCompletePublication()",
          ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_INFORMATIONS_DE_LA_PUBLICATION", e);
    }
    KmeliaPublication publication = KmeliaPublication.aKmeliaPublicationFromCompleteDetail(completePublication);
    SilverTrace.info("kmax", "KmeliaBmEjb.getKmaxCompletePublication()",
        "root.MSG_GEN_EXIT_METHOD");
    return publication;
  }

  @Override
  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.getPublicationCoordinates()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getPublicationBm().getCoordinates(pubId, componentId);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.getPublicationCoordinates()", ERROR,
          "root.MSG_GEN_PARAM_VALUE", e);
    }
  }

  @Override
  public void addPublicationToCombination(String pubId, List<String> combination,
      String componentId) {
    SilverTrace.info("kmax", "KmeliaBmEJB.addPublicationToCombination()",
        "root.MSG_GEN_PARAM_VALUE", "combination =" + combination.toString());
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK);
    try {
      NodeBm nodeBm = getNodeBm();
      Collection<Coordinate> coordinates = getPublicationCoordinates(pubId, componentId);

      if (!checkCombination(coordinates, combination)) {
        return;
      }

      NodeDetail nodeDetail = null;
      // enrich combination by get ancestors
      Iterator<String> it = combination.iterator();
      List<CoordinatePoint> allnodes = new ArrayList<CoordinatePoint>();
      int i = 1;
      while (it.hasNext()) {
        String nodeId = it.next();
        NodePK nodePK = new NodePK(nodeId, componentId);
        SilverTrace.info("kmax", "KmeliaBmEjb.addPublicationToCombination()",
            "root.MSG_GEN_PARAM_VALUE", "avant nodeBm.getPath() ! i = " + i);
        Collection<NodeDetail> path = nodeBm.getPath(nodePK);
        SilverTrace.info("kmax", "KmeliaBmEjb.addPublicationToCombination()",
            "root.MSG_GEN_PARAM_VALUE", "path for nodeId " + nodeId + " = "
            + path.toString());
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
      int coordinateId = getCoordinatesBm().addCoordinate(coordinatePK, allnodes);
      getPublicationBm().addFather(pubPK, new NodePK(String.valueOf(coordinateId), pubPK));
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
  public void deleteCoordinates(CoordinatePK coordinatePK, ArrayList<?> coordinates) {
    try {
      getCoordinatesBm().deleteCoordinates(coordinatePK, coordinates);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.deleteCoordinates()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COORDINATES", e);
    }
  }

  @Override
  public void deletePublicationFromCombination(String pubId,
      String combinationId, String componentId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.deletePublicationFromCombination()",
        "root.MSG_GEN_PARAM_VALUE", "combinationId = " + combinationId);
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    NodePK fatherPK = new NodePK(combinationId, componentId);
    CoordinatePK coordinatePK = new CoordinatePK(combinationId, pubPK);
    try {
      // remove publication fathers
      getPublicationBm().removeFather(pubPK, fatherPK);
      // remove coordinate
      List<String> coordinateIds = new ArrayList<String>();
      coordinateIds.add(combinationId);
      getCoordinatesBm().deleteCoordinates(coordinatePK, (ArrayList<?>) coordinateIds);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.deletePublicationFromCombination()", ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_COMBINAISON_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * Create a new Publication (only the header - parameters)
   *
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  @Override
  public String createKmaxPublication(PublicationDetail pubDetail) {
    SilverTrace.info("kmax", "KmeliaBmEJB.createKmaxPublication()", "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = null;
    Connection con = getConnection(); // connection usefull for content
    // service
    try {
      // create the publication
      pubDetail = changePublicationStatusOnCreation(pubDetail, new NodePK("useless",
          pubDetail.getPK()));
      pubPK = getPublicationBm().createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());

      // creates todos for publishers
      this.createTodosForPublication(pubDetail, true);

      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createKmaxPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("kmax", "KmeliaBmEJB.createKmaxPublication()", "root.MSG_GEN_EXIT_METHOD");
    return pubPK.getId();
  }

  @Override
  public Collection<Alias> getAlias(PublicationPK pubPK) {
    try {
      return getPublicationBm().getAlias(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAlias()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LES_ALIAS_DE_PUBLICATION", e);
    }
  }

  @Override
  public void setAlias(PublicationPK pubPK, List<Alias> alias) {
    List<Alias> oldAliases = (List<Alias>) getAlias(pubPK);
    List<Alias> newAliases = new ArrayList<Alias>(alias.size());
    List<Alias> remAliases = new ArrayList<Alias>(oldAliases.size());
    // Compute the remove list
    for (Alias a : oldAliases) {
      if (!alias.contains(a)) {
        remAliases.add(a);
      }
    }
    // Compute the add and stay list
    for (Alias a : alias) {
      if (!oldAliases.contains(a)) {
        newAliases.add(a);
      }
    }
    try {
      getPublicationBm().addAlias(pubPK, newAliases);
      getPublicationBm().removeAlias(pubPK, remAliases);
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.setAlias()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DENREGISTRER_LES_ALIAS_DE_PUBLICATION", e);
    }
    // Send subscriptions to aliases subscribers
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    String originalComponentId = pubPK.getInstanceId();
    for (Alias a : newAliases) {
      pubDetail.getPK().setComponentName(a.getInstanceId()); // Change the instanceId to make the
      // right URL
      sendSubscriptionsNotification(new NodePK(a.getId(), a.getInstanceId()), pubDetail, false);
    }
    // restore original primary key
    pubDetail.getPK().setComponentName(originalComponentId);
  }

  @Override
  public void addAttachmentToPublication(PublicationPK pubPK, String userId, String filename,
      String description, byte[] contents) {
    try {
      Date creationDate = new Date();
      SimpleAttachment file = new SimpleAttachment(FileUtil.getFilename(filename),
          I18NHelper.defaultLanguage, filename, "", contents.length, FileUtil.getMimeType(filename),
          userId, creationDate, null);
      boolean versioningActive = StringUtil.getBooleanValue(getOrganizationController().
          getComponentParameterValue(pubPK.getComponentName(), "versionControl"));
      SimpleDocument document;
      if (versioningActive) {
        document = new HistorisedDocument(new SimpleDocumentPK(null, pubPK.getComponentName()),
            pubPK.
            getId(), 0, file);
        document.setPublicDocument(true);
      } else {
        document = new SimpleDocument(new SimpleDocumentPK(null, pubPK.getComponentName()), pubPK.
            getId(), 0, false, file);
      }
      AttachmentServiceFactory.getAttachmentService().createAttachment(document,
          new ByteArrayInputStream(contents));
    } catch (org.silverpeas.attachment.AttachmentException fnfe) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addAttachmentToPublication()", ERROR,
          "kmelia.EX_IMPOSSIBLE_DAJOUTER_ATTACHEMENT", fnfe);
    }
  }

  /**
   * Creates or updates a publication.
   *
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
   * @throws RemoteException
   */
  @Override
  public boolean importPublication(String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(
        this, componentId, topicId, spaceId, userId);
    return publicationImport.importPublication(publiParams, formParams,
        language, xmlFormName, discrimatingParameterName, userProfile);
  }

  public boolean importPublication(String componentId, String topicId, String userId,
      Map<String, String> publiParams, Map<String, String> formParams, String language,
      String xmlFormName, String discriminantParameterName, String userProfile,
      boolean ignoreMissingFormFields) throws RemoteException {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, null, userId);
    publicationImport.setIgnoreMissingFormFields(ignoreMissingFormFields);
    return publicationImport.importPublication(publiParams, formParams, language, xmlFormName,
        discriminantParameterName, userProfile);
  }

  /**
   * Creates or updates a publication.
   *
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
   * @throws RemoteException
   */
  @Override
  public boolean importPublication(String publicationId, String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName, String userProfile)
      throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(
        this, componentId, topicId, spaceId, userId);
    return publicationImport.importPublication(
        publicationId, publiParams, formParams, language, xmlFormName, userProfile);
  }

  @Override
  public void importPublications(String componentId, String topicId,
      String spaceId, String userId, List<Map<String, String>> publiParamsList,
      List<Map<String, String>> formParamsList, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile)
      throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this,
        componentId, topicId, spaceId, userId);
    publicationImport.importPublications(publiParamsList, formParamsList,
        language, xmlFormName, discrimatingParameterName, userProfile);
  }

  @Override
  public List<XMLField> getPublicationXmlFields(String publicationId, String componentId,
      String spaceId, String userId) {
    PublicationImport publicationImport = new PublicationImport(this, componentId, null, spaceId,
        userId);
    return publicationImport.getPublicationXmlFields(publicationId);
  }

  @Override
  public List<XMLField> getPublicationXmlFields(String publicationId, String componentId,
      String spaceId,
      String userId, String language) {
    PublicationImport publicationImport = new PublicationImport(
        this, componentId, null, spaceId, userId);
    return publicationImport.getPublicationXmlFields(publicationId, language);
  }

  @Override
  public String createTopic(String componentId, String topicId, String spaceId, String userId,
      String name, String description) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this, componentId, topicId, spaceId,
        userId);
    return publicationImport.createTopic(name, description);
  }

  @Override
  public Collection<String> getPublicationsSpecificValues(String componentId, String xmlFormName,
      String fieldName) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this, componentId);
    return publicationImport.getPublicationsSpecificValues(componentId, xmlFormName, fieldName);
  }

  @Override
  public void draftInPublication(String componentId, String xmlFormName,
      String fieldName, String fieldValue) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this, componentId);
    publicationImport.draftInPublication(xmlFormName, fieldName, fieldValue);
  }

  @Override
  public void updatePublicationEndDate(String componentId, String spaceId, String userId,
      String xmlFormName, String fieldName, String fieldValue, Date endDate) throws
      RemoteException {
    PublicationImport publicationImport = new PublicationImport(this, componentId, null, spaceId,
        userId);
    publicationImport.updatePublicationEndDate(xmlFormName, fieldName, fieldValue, endDate);
  }

  /**
   * Find a publication imported only by a xml field (old id for example)
   *
   * @param componentId
   * @param xmlFormName
   * @param fieldName
   * @param fieldValue
   * @param topicId
   * @param spaceId
   * @param userId
   * @return pubId
   * @throws RemoteException
   */
  @Override
  public String findPublicationIdBySpecificValue(String componentId, String xmlFormName,
      String fieldName, String fieldValue, String topicId, String spaceId, String userId)
      throws RemoteException {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport.getPublicationId(xmlFormName, fieldName, fieldValue);
  }

  @Override
  public void doAutomaticDraftOut() {
    // get all clones with draftoutdate <= current date
    // pubCloneId <> -1 AND pubCloneStatus == 'Draft'
    Collection<PublicationDetail> pubs;
    try {
      pubs = getPublicationBm().getPublicationsToDraftOut(true);

      // for each clone, call draftOutPublication method
      for (PublicationDetail pub : pubs) {
        draftOutPublication(pub.getClonePK(), null, "admin", true);
      }
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.doAutomaticDraftOut()",
          ERROR,
          "kmelia.CANT_DO_AUTOMATIC_DRAFTOUT", e);
    }
  }

  @Override
  public String clonePublication(CompletePublication refPubComplete, PublicationDetail pubDetail,
      String nextStatus) {
    String cloneId = null;
    try {
      // récupération de la publi de référence
      PublicationDetail refPub = refPubComplete.getPublicationDetail();

      String fromId = refPub.getPK().getId();
      String fromComponentId = refPub.getPK().getInstanceId();

      PublicationDetail clone = getClone(refPub);

      ResourceLocator publicationSettings =
          new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", "");
      String imagesSubDirectory = publicationSettings.getString("imagesSubDirectory");
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

      PublicationPK clonePK = getPublicationBm().createPublication(clone);
      clonePK.setComponentName(fromComponentId);
      cloneId = clonePK.getId();

      // clone attachments
      List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService()
          .listDocumentsByForeignKey(new ForeignPK(fromId, fromComponentId), null);
      Map<String, String> attachmentIds = new HashMap<String, String>(documents.size());
      for (SimpleDocument document : documents) {
        AttachmentServiceFactory.getAttachmentService().cloneDocument(document, cloneId);
      }

      // eventually, paste the model content
      if (refPubComplete.getModelDetail() != null && refPubComplete.getInfoDetail() != null) {
        // Paste images of model
        if (refPubComplete.getInfoDetail().getInfoImageList() != null) {
          for (InfoImageDetail attachment : refPubComplete.getInfoDetail().getInfoImageList()) {
            String from = absolutePath + imagesSubDirectory + File.separator + attachment.
                getPhysicalName();
            String type = attachment.getPhysicalName().substring(attachment.getPhysicalName().
                lastIndexOf('.') + 1, attachment.getPhysicalName().length());
            String newName = Long.toString(System.currentTimeMillis()) + "." + type;
            attachment.setPhysicalName(newName);
            String to = absolutePath + imagesSubDirectory + File.separator + newName;
            FileRepositoryManager.copyFile(from, to);
          }
        }

        // Paste model content
        createInfoModelDetail(clonePK, refPubComplete.getModelDetail().getId(), refPubComplete.
            getInfoDetail());
      } else {
        String infoId = refPub.getInfoId();
        if (infoId != null && !"0".equals(infoId) && !isInteger(infoId)) {
          PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
          // Content = XMLForm
          // register xmlForm to publication
          String xmlFormShortName = infoId;
          templateManager.addDynamicPublicationTemplate(fromComponentId + ":"
              + xmlFormShortName, xmlFormShortName + ".xml");

          PublicationTemplate pubTemplate =
              templateManager.getPublicationTemplate(fromComponentId + ":" + xmlFormShortName);

          RecordSet set = pubTemplate.getRecordSet();

          // clone dataRecord
          set.clone(fromId, fromComponentId, cloneId, fromComponentId, attachmentIds);
        }
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
        new ThumbnailServiceImpl().createThumbnail(thumbDetail);
      }
    } catch (IOException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION", e);
    } catch (FormException fe) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", fe);
    } catch (PublicationTemplateException pe) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", pe);
    } catch (ThumbnailException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication", ERROR,
          "kmelia.CANT_CLONE_PUBLICATION", e);
    }
    return cloneId;
  }

  /**
   * Gets a service object on the comments.
   *
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentService;
  }

  private ResourceLocator getMultilang() {
    return new ResourceLocator("org.silverpeas.kmelia.multilang.kmeliaBundle", "fr");
  }

  @Override
  public NodeDetail getRoot(String componentId, String userId) throws RemoteException {
    return getRoot(componentId, userId, null);
  }

  private NodeDetail getRoot(String componentId, String userId, List<NodeDetail> treeview)
      throws RemoteException {
    NodePK rootPK = new NodePK(NodePK.ROOT_NODE_ID, componentId);
    NodeDetail root = getNodeBm().getDetail(rootPK);
    setRole(root, userId);
    root.setChildrenDetails(getRootChildren(root, userId, treeview));
    return root;
  }

  private List<NodeDetail> getRootChildren(NodeDetail root, String userId, List<NodeDetail> treeview) {
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
    } catch (RemoteException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return children;
  }

  public Collection<NodeDetail> getFolderChildren(NodePK nodePK, String userId)
      throws RemoteException {
    NodeDetail node = getNodeBm().getDetail(nodePK);
    if (node.getNodePK().isRoot()) {
      node.setChildrenDetails(getRootChildren(node, userId, null));
    } else {
      setAllowedSubfolders(node, userId);
    }

    // set nb objects in nodes
    setNbItemsOfSubfolders(node, null, userId);

    return node.getChildrenDetails();
  }

  private void setNbItemsOfSubfolders(NodeDetail node, List<NodeDetail> treeview, String userId)
      throws RemoteException {
    String instanceId = node.getNodePK().getInstanceId();
    if (isNbItemsDisplayed(instanceId)) {
      if (treeview == null) {
        treeview = getTreeview(node.getNodePK(), userId);
      }
      // set nb objects in each nodes
      setNbItemsOfFolders(instanceId, node.getChildrenDetails(), treeview);
    }
  }

  private List<NodeDetail> getTreeview(NodePK pk, String userId) throws RemoteException {
    String instanceId = pk.getInstanceId();
    if (isUserComponentAdmin(instanceId, userId)) {
      return getTreeview(pk, "admin", isCoWritingEnable(instanceId),
          isDraftVisibleWithCoWriting(), userId, isNbItemsDisplayed(instanceId), false);
    } else {
      return getTreeview(pk, getUserTopicProfile(pk, userId), isCoWritingEnable(instanceId),
          isDraftVisibleWithCoWriting(), userId, isNbItemsDisplayed(instanceId),
          isRightsOnTopicsEnabled(instanceId));
    }
  }

  private void setNbItemsOfFolders(String componentId, Collection<NodeDetail> nodes,
      List<NodeDetail> treeview) throws RemoteException {
    if (isNbItemsDisplayed(componentId)) {
      for (NodeDetail child : nodes) {
        int index = treeview.indexOf(child);
        if (index != -1) {
          child.setNbObjects(treeview.get(index).getNbObjects());
        }
      }
    }
  }

  private void setAllowedSubfolders(NodeDetail node, String userId) throws RemoteException {
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

  private void setRole(NodeDetail node, String userId) throws RemoteException {
    if (isRightsOnTopicsEnabled(node.getNodePK().getInstanceId())) {
      node.setUserRole(getUserTopicProfile(node.getNodePK(), userId));
    }
  }

  private void setRole(Collection<NodeDetail> nodes, String userId) throws RemoteException {
    for (NodeDetail node : nodes) {
      setRole(node, userId);
    }
  }

  private boolean isRightsOnTopicsEnabled(String componentId) {
    return StringUtil.getBooleanValue(getOrganizationController().getComponentParameterValue(
        componentId, InstanceParameters.rightsOnFolders));
  }

  private boolean isNbItemsDisplayed(String componentId) {
    return StringUtil.getBooleanValue(getOrganizationController().getComponentParameterValue(
        componentId, InstanceParameters.displayNbItemsOnFolders));
  }

  private boolean isCoWritingEnable(String componentId) {
    return StringUtil.getBooleanValue(getOrganizationController().getComponentParameterValue(
        componentId, InstanceParameters.coWriting));
  }

  private boolean isDraftVisibleWithCoWriting() {
    return getSettings().getBoolean("draftVisibleWithCoWriting", false);
  }

  @Override
  public String getUserTopicProfile(NodePK pk, String userId) throws RemoteException {
    if (!isRightsOnTopicsEnabled(pk.getInstanceId()) || KmeliaHelper.isToValidateFolder(pk.getId())) {
      return KmeliaHelper.getProfile(getUserRoles(pk.getInstanceId(), userId));
    }

    NodeDetail node = getNodeHeader(pk.getId(), pk.getInstanceId());

    // check if we have to take care of topic's rights
    if (node != null && node.haveRights()) {
      int rightsDependsOn = node.getRightsDependsOn();
      return KmeliaHelper.getProfile(getOrganizationController().getUserProfiles(userId,
          pk.getInstanceId(), rightsDependsOn, ObjectType.NODE));
    } else {
      return KmeliaHelper.getProfile(getUserRoles(pk.getInstanceId(), userId));
    }
  }

  private String[] getUserRoles(String componentId, String userId) {
    return getOrganizationController().getUserProfiles(userId, componentId);
  }

  private NodePK getRootPK(String componentId) {
    return new NodePK(NodePK.ROOT_NODE_ID, componentId);
  }

  @Override
  public boolean isUserCanValidatePublication(PublicationPK pubPK, String userId)
      throws RemoteException {
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
      ValidationStep validationStep = getPublicationBm().getValidationStepByUser(pubPK, userId);
      // user has not yet validated publication, so validation is allowed
      return validationStep == null;
    }
    return true;
  }

  @Override
  public boolean isUserCanValidate(String componentId, String userId) throws RemoteException {
    if (KmeliaHelper.isToolbox(componentId)) {
      return false;
    }

    String profile = KmeliaHelper.getProfile(getUserRoles(componentId, userId));
    boolean isPublisherOrAdmin =
        SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile);

    if (!isPublisherOrAdmin && isRightsOnTopicsEnabled(componentId)) {
      // check if current user is publisher or admin on at least one descendant
      Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(getRootPK(componentId))
          .iterator();
      while (!isPublisherOrAdmin && descendants.hasNext()) {
        NodeDetail descendant = descendants.next();
        AdminController admin = null;
        if (descendant.haveLocalRights()) {
          // check if user is admin or publisher on this topic
          if (admin == null) {
            admin = new AdminController(userId);
          }
          String[] profiles =
              admin.getProfilesByObjectAndUserId(descendant.getId(),
              ObjectType.NODE.getCode(), componentId, userId);
          if (profiles != null && profiles.length > 0) {
            List<String> lProfiles = Arrays.asList(profiles);
            isPublisherOrAdmin =
                lProfiles.contains(SilverpeasRole.admin.name())
                || lProfiles.contains(SilverpeasRole.publisher.name());
          }
        }
      }
    }
    return isPublisherOrAdmin;
  }

  public boolean isUserCanWrite(String componentId, String userId) throws RemoteException {
    String profile = KmeliaHelper.getProfile(getUserRoles(componentId, userId));
    boolean userCanWrite =
        SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile)
        || SilverpeasRole.writer.isInRole(profile);

    if (!userCanWrite && isRightsOnTopicsEnabled(componentId)) {
      // check if current user is publisher or admin on at least one descendant
      Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(getRootPK(componentId))
          .iterator();
      while (!userCanWrite && descendants.hasNext()) {
        NodeDetail descendant = descendants.next();
        AdminController admin = null;
        if (descendant.haveLocalRights()) {
          // check if user is admin, publisher or writer on this topic
          if (admin == null) {
            admin = new AdminController(userId);
          }
          String[] profiles =
              admin.getProfilesByObjectAndUserId(descendant.getId(), ObjectType.NODE.getCode(),
              componentId, userId);
          if (profiles != null && profiles.length > 0) {
            List<String> lProfiles = Arrays.asList(profiles);
            userCanWrite =
                lProfiles.contains(SilverpeasRole.admin.name())
                || lProfiles.contains(SilverpeasRole.publisher.name())
                || lProfiles.contains(SilverpeasRole.writer.name());
          }
        }
      }
    }
    return userCanWrite;
  }

  @Override
  public NodeDetail getExpandedPathToNode(NodePK pk, String userId) throws RemoteException {
    String instanceId = pk.getInstanceId();
    List<NodeDetail> nodes = new ArrayList<NodeDetail>(getNodeBm().getPath(pk));
    Collections.reverse(nodes);
    NodeDetail root = nodes.remove(0);

    List<NodeDetail> treeview = null;
    if (isNbItemsDisplayed(instanceId)) {
      treeview = getTreeview(getRootPK(instanceId), userId);
    }

    root = getRoot(instanceId, userId, treeview);

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
      Collection<NodeDetail> children = getNodeBm().getChildrenDetails(node.getNodePK());
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
   *
   * @param ids the ids of publications to delete
   * @param nodePK the node where the publications are
   * @param userId the user who wants to perform deletion
   * @return the list of publication ids which has been really deleted
   * @throws RemoteException
   */
  @Override
  public List<String> deletePublications(List<String> ids, NodePK nodePK, String userId)
      throws RemoteException {
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
          SilverTrace.spy("kmelia", "KmeliaBmEJB.deletePublications", null, nodePK.getInstanceId(),
              id, userId, SilverTrace.SPY_ACTION_DELETE);
          removedIds.add(id);
        } catch (Exception e) {
          SilverTrace.error("kmelia", "KmeliaBmEJB.deletePublications()",
              "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION", "pk = " + pk.toString(), e);
        }
      }
    }
    return removedIds;
  }

  private boolean isUserCanDeletePublication(PublicationPK pubPK, String profile, String userId)
      throws RemoteException {
    UserDetail owner = getPublication(pubPK).getCreator();
    return KmeliaPublicationHelper.isRemovable(pubPK.getInstanceId(), userId, profile, owner);
  }

  @Override
  public String getWysiwyg(PublicationPK pubPK, String language) {
    try {
      return WysiwygController.load(pubPK.getInstanceId(), pubPK.getId(),
          I18NHelper.checkLanguage(language));
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAttachments()", ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_WYSIWYG", e);
    }
  }
}
