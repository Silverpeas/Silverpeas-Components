/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia.control.ejb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.formTemplate.dao.ModelDAO;
import com.silverpeas.pdc.ejb.PdcBm;
import com.silverpeas.pdc.ejb.PdcBmHome;
import com.silverpeas.pdc.ejb.PdcBmRuntimeException;
import com.silverpeas.pdcSubscription.util.PdcSubscriptionUtil;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.comment.control.CommentController;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.kmelia.KmeliaContentManager;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.kmelia.PublicationImport;
import com.stratelia.webactiv.kmelia.model.FullPublication;
import com.stratelia.webactiv.kmelia.model.KmaxRuntimeException;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.TopicComparator;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.kmelia.model.UserCompletePublication;
import com.stratelia.webactiv.kmelia.model.UserPublication;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBm;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBmHome;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.favorit.control.FavoritBm;
import com.stratelia.webactiv.util.favorit.control.FavoritBmHome;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
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
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.ValidationStep;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;

/**
 * This is the KMelia EJB-tier controller of the MVC. It is implemented as a session EJB. It
 * controls all the activities that happen in a client session. It also provides mechanisms to
 * access other session EJBs.
 * @author Nicolas Eysseric
 */
public class KmeliaBmEJB implements KmeliaBmBusinessSkeleton, SessionBean {

  private static final long serialVersionUID = 1L;

  public KmeliaBmEJB() {
  }

  public void ejbCreate() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbCreate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  @Override
  public void ejbRemove() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbRemove()",
        "root.MSG_GEN_ENTER_METHOD");
    // currentTopic = null;
  }

  @Override
  public void ejbActivate() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbActivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  @Override
  public void ejbPassivate() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.ejbPassivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  private NotificationSender getNotificationSender(String componentId) {
    // must return a new instance each time
    // This is to resolve Serializable problems
    NotificationSender notifSender = new NotificationSender(componentId);
    return notifSender;
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

  private String getComponentLabel(String componentId, String language) {
    ComponentInstLight component = getOrganizationController()
        .getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel(language);
    }
    return componentLabel;
  }

  private int getNbPublicationsOnRoot(String componentId) {
    String parameterValue = getOrganizationController()
        .getComponentParameterValue(componentId, "nbPubliOnRoot");
    SilverTrace.info("kmelia", "KmeliaBmEJB.getNbPublicationsOnRoot()",
        "root.MSG_GEN_PARAM_VALUE", "parameterValue=" + parameterValue);
    if (StringUtil.isDefined(parameterValue)) {
      return new Integer(parameterValue).intValue();
    } else {
      if (KmeliaHelper.isToolbox(componentId)) {
        return 0;
      } else {
        // lecture du properties
        ResourceLocator settings = new ResourceLocator(
            "com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "fr");
        return new Integer(settings.getString("HomeNbPublications")).intValue();
      }
    }
  }

  /****************************************************************/
  /* Draft mode used ? */
  /****************************************************************/
  private boolean isDraftModeUsed(String componentId) {
    return "yes".equals(getOrganizationController().getComponentParameterValue(
        componentId, "draft"));
  }

  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getNodeBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  public PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
              PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME", e);
    }
    return publicationBm;
  }

  public FavoritBm getFavoriteBm() {
    FavoritBm favoriteBm = null;
    try {
      FavoritBmHome favoriteBmHome = (FavoritBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.FAVORITBM_EJBHOME, FavoritBmHome.class);
      favoriteBm = favoriteBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getFavoriteBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_FAVORITBM_HOME", e);
    }
    return favoriteBm;
  }

  public SubscribeBm getSubscribeBm() {
    SubscribeBm subscribeBm = null;
    try {
      SubscribeBmHome subscribeBmHome = (SubscribeBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.SUBSCRIBEBM_EJBHOME, SubscribeBmHome.class);
      subscribeBm = subscribeBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSubscribeBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_SUBSCRIBEBM_HOME", e);
    }
    return subscribeBm;
  }

  public StatisticBm getStatisticBm() {
    StatisticBm statisticBm = null;
    try {
      StatisticBmHome statisticBmHome = (StatisticBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
      statisticBm = statisticBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getStatisticBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_STATISTICBM_HOME", e);
    }
    return statisticBm;
  }

  public VersioningBm getVersioningBm() {
    VersioningBm versioningBm = null;
    try {
      VersioningBmHome versioningBmHome = (VersioningBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      versioningBm = versioningBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getVersioningBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_VERSIONING_EJBHOME", e);
    }
    return versioningBm;
  }

  public PdcBm getPdcBm() {
    PdcBm pdcBm = null;
    try {
      PdcBmHome pdcBmHome = (PdcBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.PDCBM_EJBHOME, PdcBmHome.class);
      pdcBm = pdcBmHome.create();
    } catch (Exception e) {
      throw new PdcBmRuntimeException("KmeliaBmEJB.getPdcBm",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return pdcBm;
  }

  /**
   * "Kmax" method
   * @return
   */
  public CoordinatesBm getCoordinatesBm() {
    CoordinatesBm currentCoordinatesBm = null;
    try {
      CoordinatesBmHome coordinatesBmHome = (CoordinatesBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.COORDINATESBM_EJBHOME,
              CoordinatesBmHome.class);
      currentCoordinatesBm = coordinatesBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getCoordinatesBm()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_FABRIQUER_COORDINATESBM_HOME", e);
    }
    return currentCoordinatesBm;
  }

  public SearchEngineBm getSearchEngineBm() {
    SearchEngineBm searchEngineBm = null;
    try {
      SearchEngineBmHome searchEngineBmHome = (SearchEngineBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME, SearchEngineBmHome.class);
      searchEngineBm = searchEngineBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSearchEngineBm()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_FABRIQUER_SEARCHENGINEBM_HOME", e);
    }
    return searchEngineBm;
  }

  /**
   * Return a the detail of a topic
   * @param id the id of the topic
   * @return a TopicDetail
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   * @since 1.0
   */
  public TopicDetail goTo(NodePK pk, String userId,
      boolean isTreeStructureUsed, String userProfile,
      boolean isRightsOnTopicsUsed) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    Collection<PublicationDetail> pubDetails = null;
    NodeDetail nodeDetail = null;
    NodeBm nodeBm = getNodeBm();
    PublicationBm pubBm = getPublicationBm();
    PublicationPK pubPK = new PublicationPK("unknown", pk);

    // get the basic information (Header) of this topic
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
        "root.MSG_GEN_PARAM_VALUE", "nodeBm.getDetail(pk) BEGIN");
    try {
      nodeDetail = nodeBm.getDetail(pk);

      if (isRightsOnTopicsUsed) {
        OrganizationController orga = getOrganizationController();

        if (nodeDetail.haveRights()
            && !orga.isObjectAvailable(nodeDetail.getRightsDependsOn(),
                ObjectType.NODE, pk.getInstanceId(), userId)) {
          nodeDetail.setUserRole("noRights");
        }

        List<NodeDetail> children = (List<NodeDetail>) nodeDetail.getChildrenDetails();

        List<NodeDetail> availableChildren = new ArrayList<NodeDetail>();

        NodeDetail child = null;
        String childId = null;
        for (int c = 0; c < children.size(); c++) {
          child = children.get(c);
          childId = child.getNodePK().getId();
          if (childId.equals("1") || childId.equals("2") || !child.haveRights()) {
            availableChildren.add(child);
          } else {
            int rightsDependsOn = child.getRightsDependsOn();
            boolean nodeAvailable = orga.isObjectAvailable(rightsDependsOn,
                ObjectType.NODE, pk.getInstanceId(), userId);
            if (nodeAvailable) {
              availableChildren.add(child);
            } else {
              // check if at least one descendant is available
              Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(child)
                  .iterator();
              NodeDetail descendant = null;
              boolean childAllowed = false;
              while (!childAllowed && descendants.hasNext()) {
                descendant = descendants.next();
                if (descendant.getRightsDependsOn() == rightsDependsOn) {
                  // same rights of father (which is not
                  // available)
                  // so it is not available too
                } else {
                  // different rights of father
                  // check if it is available
                  if (orga.isObjectAvailable(descendant.getRightsDependsOn(),
                      ObjectType.NODE, pk.getInstanceId(), userId)) {
                    // child.setUserRole("noRights");
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
        nodeDetail.setChildrenDetails(availableChildren);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.goTo()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
        "root.MSG_GEN_PARAM_VALUE", "nodeBm.getDetail(pk) END");

    // get the publications associated to this topic
    if (pk.getId().equals("1")) {
      // Topic = Basket
      pubDetails = getPublicationsInBasket(pk, userProfile, userId);
    } else if (pk.getId().equals("0")) {
      SilverTrace
          .info(
              "kmelia",
              "KmeliaBmEJB.goTo()",
              "root.MSG_GEN_PARAM_VALUE",
              "pubBm.getUnavailablePublicationsByPublisherId(pubPK, currentUser.getId()) BEGIN");
      try {
        int nbPublisOnRoot = getNbPublicationsOnRoot(pk.getInstanceId());
        if (nbPublisOnRoot == 0 || !isTreeStructureUsed
            || KmeliaHelper.isToolbox(pk.getInstanceId())) {
          pubDetails = pubBm.getDetailsByFatherPK(pk, "P.pubUpdateDate desc",
              false);
        } else {
          pubDetails = pubBm
              .getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(pubPK,
                  PublicationDetail.VALID, nbPublisOnRoot, "1");
          if (isRightsOnTopicsUsed) {
            // The list of publications must be filtered
            List<PublicationDetail> filteredList = new ArrayList<PublicationDetail>();
            Iterator<PublicationDetail> it = pubDetails.iterator();
            KmeliaSecurity security = new KmeliaSecurity();
            while (it.hasNext()) {
              PublicationDetail pubDetail = it.next();
              if (security.isObjectAvailable(pk.getInstanceId(), userId,
                  pubDetail.getPK().getId(), "Publication")) {
                filteredList.add(pubDetail);
              }
            }
            pubDetails.clear();
            pubDetails.addAll(filteredList);
          }
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.goTo()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DAVOIR_LES_DERNIERES_PUBLICATIONS", e);
      }
      SilverTrace
          .info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_PARAM_VALUE",
              "pubBm.getUnavailablePublicationsByPublisherId(pubPK, currentUser.getId()) END");
    } else {
      SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
          "root.MSG_GEN_PARAM_VALUE", "pubBm.getDetailsByFatherPK(pk) BEGIN");
      try {
        // get the publication details linked to this topic
        pubDetails = pubBm.getDetailsByFatherPK(pk, "P.pubUpdateDate desc",
            false);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.goTo()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DAVOIR_LA_LISTE_DES_PUBLICATIONS", e);
      }
      SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
          "root.MSG_GEN_PARAM_VALUE", "pubBm.getDetailsByFatherPK(pk) END");
    }

    // get the path to this topic
    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
        "root.MSG_GEN_PARAM_VALUE", "GetPath BEGIN");

    if (pk.getId().equals("0")) {
      newPath.add(nodeDetail);
    } else {
      newPath = getPathFromAToZ(nodeDetail);
    }

    SilverTrace.info("kmelia", "KmeliaBmEJB.goTo()",
        "root.MSG_GEN_PARAM_VALUE", "GetPath END");
    SilverTrace
        .info("kmelia", "KmeliaBmEJB.goTo()", "root.MSG_GEN_EXIT_METHOD");

    // set the currentTopic and return it
    return new TopicDetail(newPath, nodeDetail,
        pubDetails2userPubs(pubDetails));
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
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPathFromAToZ()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CHEMIN_COURANT", e);
    }
    return newPath;
  }

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
  public NodePK addToTopic(NodePK fatherPK, NodeDetail subTopic) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addToTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    NodePK theNodePK = null;
    try {
      NodeDetail fatherDetail = getNodeBm().getHeader(fatherPK);

      theNodePK = getNodeBm().createNode(subTopic, fatherDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addToTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LE_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.addToTopic()",
        "root.MSG_GEN_EXIT_METHOD");
    return theNodePK;
  }

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
  public NodePK addSubTopic(NodePK fatherPK, NodeDetail subTopic,
      String alertType) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    NodePK pk = null;

    // Construction de la date de création (date courante)
    String creationDate = DateUtil.today2SQLDate();
    subTopic.setCreationDate(creationDate);

    // Web visibility parameter. The topic is by default invisible.
    subTopic.setStatus("Invisible");

    // add new topic to current topic
    pk = addToTopic(fatherPK, subTopic);
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubTopic()",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString());
    // Creation alert
    if (!pk.getId().equals("-1")) {
      topicCreationAlert(pk, fatherPK, alertType);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubTopic()",
        "root.MSG_GEN_EXIT_METHOD");
    return pk;
  }

  private String displayPath(Collection<NodeDetail> path, int beforeAfter, String language) {
    String pathString = new String();
    int nbItemInPath = path.size();
    Iterator<NodeDetail> iterator = path.iterator();
    boolean alreadyCut = false;
    int nb = 0;

    NodeDetail nodeInPath = null;
    while (iterator.hasNext()) {
      nodeInPath = iterator.next();
      if ((nb <= beforeAfter) || (nb + beforeAfter >= nbItemInPath - 1)) {
        pathString = nodeInPath.getName(language) + " " + pathString;
        if (iterator.hasNext()) {
          pathString = " > " + pathString;
        }
      } else {
        if (!alreadyCut) {
          pathString += " ... > ";
          alreadyCut = true;
        }
      }
      nb++;
    }
    return pathString;
  }

  private void notifyUsers(NotificationMetaData notifMetaData, String senderId) {
    Connection con = null;
    try {
      con = getConnection();
      notifMetaData.setConnection(con);
      if (notifMetaData.getSender() == null
          || notifMetaData.getSender().length() == 0) {
        notifMetaData.setSender(senderId);
      }
      getNotificationSender(notifMetaData.getComponentId()).notifyUser(
          notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.notifyUsers()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Alert all users, only publishers or nobody of the topic creation or update
   * @param pk the NodePK of the new sub topic
   * @param alertType alertType = "All"|"Publisher"|"None"
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  private void topicCreationAlert(NodePK pk, NodePK fatherPK, String alertType) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.topicCreationAlert()",
        "root.MSG_GEN_ENTER_METHOD");
    NodeDetail nodeDetail = null;
    NodeDetail fatherDetail = null;
    try {
      nodeDetail = getNodeBm().getHeader(pk);
      if (fatherPK != null) {
        fatherDetail = getNodeBm().getHeader(fatherPK);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.topicCreationAlert()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DALERTER_POUR_MANIPULATION_THEME", e);
    }
    Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
    ResourceLocator message = new ResourceLocator(
        "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
    String subject = message.getString("kmelia.NewTopic");
    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
            "notificationCreateTopic");
    for (String lang : getAllLanguages()) {
      SilverpeasTemplate template = getNewTemplate();
      templates.put(lang, template);
      template.setAttribute("path", getHTMLNodePath(fatherPK, lang));
      template.setAttribute("topic", nodeDetail);
      template.setAttribute("topicName", nodeDetail.getName(lang));
      template.setAttribute("topicDescription", nodeDetail.getDescription(lang));
      template.setAttribute("senderName", "");
      template.setAttribute("silverpeasURL", getNodeUrl(nodeDetail));
      ResourceLocator localizedMessage = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
      notifMetaData.addLanguage(lang, localizedMessage.getString("kmelia.NewTopic", subject), "");
    }
    notifMetaData.setSender(nodeDetail.getCreatorId());
    boolean haveRights = nodeDetail.haveRights();
    int rightsDependOn = nodeDetail.getRightsDependsOn();
    if (fatherDetail != null) {
      // Case of creation only
      haveRights = fatherDetail.haveRights();
      rightsDependOn = fatherDetail.getRightsDependsOn();
    }

    if (!haveRights) {
      if (alertType.equals("All")) {
        UserDetail[] users = getOrganizationController().getAllUsers(
            pk.getInstanceId());
        notifMetaData.addUserRecipients(users);
      } else if (alertType.equals("Publisher")) {
        // Get the list of all publishers and admin
        List<String> profileNames = new ArrayList<String>();
        profileNames.add("admin");
        profileNames.add("publisher");
        profileNames.add("writer");

        String[] users = getOrganizationController().getUsersIdsByRoleNames(
            pk.getInstanceId(), profileNames);

        notifMetaData.addUserRecipients(users);
      }
    } else {
      List<String> profileNames = new ArrayList<String>();
      profileNames.add("admin");
      profileNames.add("publisher");
      profileNames.add("writer");

      if (alertType.equals("All")) {
        profileNames.add("user");

        String[] users = getOrganizationController().getUsersIdsByRoleNames(
            pk.getInstanceId(), Integer.toString(rightsDependOn),
            ObjectType.NODE, profileNames);
        notifMetaData.addUserRecipients(users);
      } else if (alertType.equals("Publisher")) {
        String[] users = getOrganizationController().getUsersIdsByRoleNames(
            pk.getInstanceId(), Integer.toString(rightsDependOn),
            ObjectType.NODE, profileNames);

        notifMetaData.addUserRecipients(users);
      }
    }
    notifMetaData.setLink(getNodeUrl(nodeDetail));
    notifMetaData.setComponentId(pk.getInstanceId());

    notifyUsers(notifMetaData, nodeDetail.getCreatorId());

    SilverTrace.info("kmelia", "KmeliaBmEJB.topicCreationAlert()",
        "root.MSG_GEN_PARAM_VALUE", "AlertType alert = " + alertType);
    SilverTrace.info("kmelia", "KmeliaBmEJB.topicCreationAlert()",
        "root.MSG_GEN_EXIT_METHOD");
  }

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
  public NodePK updateTopic(NodeDetail topic, String alertType) {
    try {
      // Order of the node must be unchanged
      NodeDetail node = getNodeBm().getHeader(topic.getNodePK());
      int order = node.getOrder();
      topic.setOrder(order);
      getNodeBm().setDetail(topic);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updateTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
    // Update Alert
    topicCreationAlert(topic.getNodePK(), null, alertType);
    return topic.getNodePK();
  }

  public NodeDetail getSubTopicDetail(NodePK pk) {
    NodeDetail subTopic = null;
    // get the basic information (Header) of this topic
    try {
      subTopic = getNodeBm().getDetail(pk);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSubTopicDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e);
    }
    return subTopic;
  }

  /**
   * Delete a topic and all descendants. Delete all links between descendants and publications. This
   * publications will be visible in the Declassified zone. Delete All subscriptions and favorites
   * on this topics and all descendants
   * @param topicId the id of the topic to delete
   * @since 1.0
   */
  public void deleteTopic(NodePK pkToDelete) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationBm pubBm = getPublicationBm();
    NodeBm nodeBm = getNodeBm();

    try {
      // get all nodes which will be deleted
      Collection<NodePK> nodesToDelete = nodeBm.getDescendantPKs(pkToDelete);
      nodesToDelete.add(pkToDelete);
      SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()",
          "root.MSG_GEN_PARAM_VALUE", "nodesToDelete = "
              + nodesToDelete.toString());

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
          onePubToCheck = (PublicationPK) itPub.next();
          if (onePubToCheck.getInstanceId().equals(
              oneNodeToDelete.getInstanceId())) {
            // get fathers of the pub
            pubFathers = pubBm.getAllFatherPK(onePubToCheck);
            if (pubFathers.size() >= 2) {
              // the pub have got many fathers
              // delete only the link between pub and current node
              pubBm.removeFather(onePubToCheck, oneNodeToDelete);
              SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()",
                  "root.MSG_GEN_PARAM_VALUE",
                  "RemoveFather(pubId, fatherId) with  pubId = "
                      + onePubToCheck.getId() + ", fatherId = "
                      + oneNodeToDelete);
            } else {
              sendPublicationToBasket(onePubToCheck);
              SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()",
                  "root.MSG_GEN_PARAM_VALUE",
                  "RemoveAllFather(pubId) with pubId = "
                      + onePubToCheck.getId());
            }
          } else {
            // remove alias
            aliases.clear();
            aliases.add(new Alias(oneNodeToDelete.getId(), oneNodeToDelete
                .getInstanceId()));
            pubBm.removeAlias(onePubToCheck, aliases);
          }
        }
      }

      // Delete all subscriptions on this topic and on its descendants
      removeSubscriptionsByTopic(pkToDelete);

      // Delete the topic
      nodeBm.removeNode(pkToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteTopic()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void changeSubTopicsOrder(String way, NodePK subTopicPK,
      NodePK fatherPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changeSubTopicsOrder()",
        "root.MSG_GEN_ENTER_METHOD", "way = " + way + ", subTopicPK = "
            + subTopicPK.toString());

    List<NodeDetail> subTopics = null;
    try {
      subTopics = (List<NodeDetail>) getNodeBm().getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.changeSubTopicsOrder()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_LISTER_THEMES", e);
    }

    if (subTopics != null && subTopics.size() > 0) {
      int indexOfTopic = 0;

      if ("0".equals(fatherPK.getId())
          && !KmeliaHelper.isToolbox(subTopicPK.getInstanceId())) {
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

      NodeDetail nodeDetail = null;
      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        nodeDetail = (NodeDetail) subTopics.get(i);

        SilverTrace.info("kmelia", "KmeliaBmEJB.changeSubTopicsOrder()",
            "root.MSG_GEN_PARAM_VALUE", "updating Node : nodeId = "
                + nodeDetail.getNodePK().getId() + ", order = " + i);
        try {
          nodeDetail.setOrder(i);
          getNodeBm().setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException(
              "KmeliaBmEJB.changeSubTopicsOrder()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
        }
      }
    }
  }

  private int getIndexOfNode(String nodeId, List<NodeDetail> nodes) {
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getIndexOfNode()",
        "root.MSG_GEN_ENTER_METHOD", "nodeId = " + nodeId);
    NodeDetail node = null;
    int index = 0;
    if (nodes != null) {
      for (int i = 0; i < nodes.size(); i++) {
        node = nodes.get(i);
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

  public void changeTopicStatus(String newStatus, NodePK nodePK,
      boolean recursiveChanges) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.changeTopicStatus()",
        "root.MSG_GEN_ENTER_METHOD", "newStatus = " + newStatus + ", nodePK = "
            + nodePK.toString() + ", recursiveChanges = " + recursiveChanges);
    try {
      NodeDetail nodeDetail = null;
      if (!recursiveChanges) {
        nodeDetail = getNodeBm().getHeader(nodePK);
        changeTopicStatus(newStatus, nodeDetail);
      } else {
        List<NodeDetail> subTree = (List<NodeDetail>) getNodeBm().getSubTree(nodePK);
        for (int i = 0; i < subTree.size(); i++) {
          nodeDetail = subTree.get(i);
          changeTopicStatus(newStatus, nodeDetail);
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.changeTopicStatus()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
  }

  public void sortSubTopics(NodePK fatherPK) {
    sortSubTopics(fatherPK, false, null);
  }

  public void sortSubTopics(NodePK fatherPK, boolean recursive,
      String[] criteria) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.sortSubTopics()",
        "root.MSG_GEN_ENTER_METHOD", "fatherPK = " + fatherPK.toString());

    List<NodeDetail> subTopics = null;
    try {
      subTopics = (List<NodeDetail>) getNodeBm().getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.sortSubTopics()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_LISTER_THEMES", e);
    }

    if (subTopics != null && subTopics.size() > 0) {
      Collections.sort(subTopics, new TopicComparator(criteria));
      NodeDetail nodeDetail = null;
      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        nodeDetail = subTopics.get(i);
        SilverTrace.info("kmelia", "KmeliaBmEJB.sortSubTopics()",
            "root.MSG_GEN_PARAM_VALUE", "updating Node : nodeId = "
                + nodeDetail.getNodePK().getId() + ", order = " + i);
        try {
          nodeDetail.setOrder(i);
          getNodeBm().setDetail(nodeDetail);
        } catch (Exception e) {
          throw new KmeliaRuntimeException("KmeliaBmEJB.sortSubTopics()",
              SilverpeasRuntimeException.ERROR,
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
      throw new KmeliaRuntimeException("KmeliaBmEJB.changeTopicStatus()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_THEME", e);
    }
  }

  public List<NodeDetail> getTreeview(NodePK nodePK, String profile,
      boolean coWritingEnable, boolean draftVisibleWithCoWriting,
      String userId, boolean displayNb, boolean isRightsOnTopicsUsed) {
    String instanceId = nodePK.getInstanceId();
    try {
      List<NodeDetail> tree = getNodeBm().getSubTree(nodePK);

      List<NodeDetail> allowedTree = new ArrayList<NodeDetail>();
      OrganizationController orga = getOrganizationController();
      if (isRightsOnTopicsUsed) {
        // filter allowed nodes
        NodeDetail node2Check = null;
        for (int t = 0; tree != null && t < tree.size(); t++) {
          node2Check = tree.get(t);
          if (!node2Check.haveRights()) {
            allowedTree.add(node2Check);

            if (t == 0) {
              // case of root. Check if publications on root are
              // allowed
              int nbPublisOnRoot = Integer.parseInt(orga
                  .getComponentParameterValue(nodePK.getInstanceId(),
                      "nbPubliOnRoot"));
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
            } else {
              // check if at least one descendant is available
              Iterator<NodeDetail> descendants = getNodeBm().getDescendantDetails(
                  node2Check).iterator();
              NodeDetail descendant = null;
              boolean node2CheckAllowed = false;
              while (!node2CheckAllowed && descendants.hasNext()) {
                descendant = descendants.next();
                if (descendant.getRightsDependsOn() == rightsDependsOn) {
                  // same rights of father (which is not
                  // available)
                  // so it is not available too
                } else {
                  // different rights of father
                  // check if it is available
                  profiles = orga.getUserProfiles(userId, instanceId,
                      descendant.getRightsDependsOn(), ObjectType.NODE);
                  if (profiles != null && profiles.length > 0) // if
                  // (orga.isObjectAvailable(descendant.getRightsDependsOn(),
                  // descendant.getNodePK().getInstanceId(),
                  // userId))
                  {
                    // String[] profiles =
                    // orga.getUserProfiles(userId,
                    // instanceId,
                    // descendant.getRightsDependsOn());
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
        if (tree.size() > 0) {
          // case of root. Check if publications on root are allowed
          String sNB = orga.getComponentParameterValue(nodePK.getInstanceId(),
              "nbPubliOnRoot");
          if (!StringUtil.isDefined(sNB)) {
            sNB = "0";
          }
          int nbPublisOnRoot = Integer.parseInt(sNB);
          if (nbPublisOnRoot != 0) {
            NodeDetail root = (NodeDetail) tree.get(0);
            root.setUserRole("user");
          }
        }

        allowedTree.addAll(tree);
      }

      if (displayNb) {
        boolean checkVisibility = false;
        String statusSubQuery = null;
        if (profile.equals("user")) {
          checkVisibility = true;
          statusSubQuery = " AND P.pubStatus = 'Valid' ";
        } else if (profile.equals("writer")) {
          statusSubQuery = " AND (";
          if (coWritingEnable && draftVisibleWithCoWriting) {
            statusSubQuery +=
                "P.pubStatus = 'Valid' OR P.pubStatus = 'Draft' OR P.pubStatus = 'Unvalidate' ";
          } else {
            checkVisibility = true;
            statusSubQuery +=
                "P.pubStatus = 'Valid' OR (P.pubStatus = 'Draft' AND P.pubUpdaterId = '"
                    + userId
                    + "') OR (P.pubStatus = 'Unvalidate' AND P.pubUpdaterId = '"
                    + userId + "') ";
          }
          statusSubQuery += "OR (P.pubStatus = 'ToValidate' AND P.pubUpdaterId = '"
              + userId + "') ";
          statusSubQuery += "OR P.pubUpdaterId = '" + userId + "'";
          statusSubQuery += ")";
        } else {
          statusSubQuery = " AND (";
          if (coWritingEnable && draftVisibleWithCoWriting) {
            statusSubQuery += "P.pubStatus IN ('Valid','ToValidate','Draft') ";
          } else {
            if (profile.equals("publisher")) {
              checkVisibility = true;
            }
            statusSubQuery +=
                "P.pubStatus IN ('Valid','ToValidate') OR (P.pubStatus = 'Draft' AND P.pubUpdaterId = '"
                    +
                    userId + "') ";
          }
          statusSubQuery += "OR P.pubUpdaterId = '" + userId + "'";
          statusSubQuery += ")";
        }

        Hashtable<String, Integer> distribution = getPublicationBm().getDistribution(
            nodePK.getInstanceId(), statusSubQuery, checkVisibility);

        NodeDetail node = null;
        for (int t = 0; t < allowedTree.size(); t++) {
          node = allowedTree.get(t);
          if (node.getNodePK().getId().equals("1")) {
            Collection<PublicationDetail> pubs =
                getPublicationsInBasket(node.getNodePK(), profile, userId);
            node.setNbObjects(pubs.size());
          } else {
            node.setNbObjects(getNbPublis(node, distribution, allowedTree));
          }
        }
      }
      return allowedTree;
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getTreeview()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LA_LISTE_DES_PUBLICATIONS", e);
    }
  }

  private Collection<PublicationDetail> getPublicationsInBasket(NodePK pk, String userProfile,
      String userId) {
    Collection<PublicationDetail> pubDetails = null;
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsInBasket()",
        "root.MSG_GEN_ENTER_METHOD", "pk = " + pk.toString() + ", userProfile = " + userProfile +
            ", userId = " + userId);
    try {
      // Give the publications associated to basket topic and
      // visibility period expired
      if ("admin".equals(userProfile)) {
        // Admin can see all Publis in the basket.
        userId = null;
      }
      pubDetails = getPublicationBm().getDetailsByFatherPK(pk, null, false, userId);
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsInBasket()",
          "root.MSG_GEN_EXIT_METHOD", "nbPublis = " + pubDetails.size());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.goTo()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LE_CONTENU_DE_LA_CORBEILLE", e);
    }
    return pubDetails;
  }

  private int getNbPublis(NodeDetail node, Hashtable<String, Integer> distribution,
      List<NodeDetail> allowedNodes) {
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getNbPublis()",
        "root.MSG_GEN_ENTER_METHOD", "node = " + node.getNodePK().toString());
    int result = 0;
    // String nodeFullPath = node.getPath()+node.getNodePK().getId()+"/";
    Enumeration<String> nodePaths = distribution.keys();
    String nodePath = null;
    Integer nb = null;

    while (nodePaths.hasMoreElements()) {
      nodePath = nodePaths.nextElement();
      // SilverTrace.debug("kmelia","KmeliaBmEJB.getNbPublis()",
      // "root.MSG_GEN_PARAM_VALUE", "nodePath = "+nodePath);
      if (isNodeAllowed(nodePath, allowedNodes)
          && nodePath.startsWith(node.getFullPath())
      /* && !getLastNodeId(nodePath).equals("1") */) {
        nb = (Integer) distribution.get(nodePath);
        if (nb != null) {
          result += nb.intValue();
        }
      }
    }
    return result;
  }

  private List<String> tempAllowedNodes = new ArrayList<String>();

  private boolean isNodeAllowed(String nodePath, List<NodeDetail> allowedNodes) {
    if (tempAllowedNodes.contains(nodePath)) {
      return true;
    }

    Iterator<NodeDetail> it = allowedNodes.iterator();

    NodeDetail node = null;
    while (it.hasNext()) {
      node = it.next();
      if (nodePath.equals(node.getFullPath())) {
        tempAllowedNodes.add(nodePath);
        return true;
      }
    }

    return false;
  }

  private static String getLastNodeId(String path) {
    path = path.substring(0, path.length() - 1); // remove last /
    return path.substring(path.lastIndexOf("/") + 1);
  }

  /**************************************************************************************/
  /* Interface - Gestion des abonnements */
  /**************************************************************************************/
  /**
   * Subscriptions - get the subscription list of the current user
   * @return a Path Collection - it's a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection<Collection<NodeDetail>> getSubscriptionList(String userId, String componentId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getSubscriptionList()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<NodePK> list = getSubscribeBm().getUserSubscribePKsByComponent(userId,
          componentId);
      Collection<Collection<NodeDetail>> detailedList = new ArrayList<Collection<NodeDetail>>();
      Iterator<NodePK> i = list.iterator();
      // For each favorite, get the path from root to favorite
      while (i.hasNext()) {
        NodePK pk = i.next();
        Collection<NodeDetail> path = getNodeBm().getPath(pk);
        detailedList.add(path);
      }
      SilverTrace.info("kmelia", "KmeliaBmEJB.getSubscriptionList()",
          "root.MSG_GEN_EXIT_METHOD");
      return detailedList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSubscriptionList()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
    }
  }

  /**
   * Subscriptions - remove a subscription to the subscription list of the current user
   * @param topicId the subscribe topic Id to remove
   * @since 1.0
   */
  public void removeSubscriptionToCurrentUser(NodePK topicPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeSubscriptionToCurrentUser()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getSubscribeBm().removeSubscribe(userId, topicPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeSubscriptionToCurrentUser()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_ABONNEMENT", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeSubscriptionToCurrentUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Subscriptions - remove all subscriptions from topic
   * @param topicId the subscription topic Id to remove
   * @since 1.0
   */
  private void removeSubscriptionsByTopic(NodePK topicPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeSubscriptionsByTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = getNodeBm().getDetail(topicPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeSubscriptionsByTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_ABONNEMENTS", e);
    }
    try {
      getSubscribeBm().removeNodeSubscribes(topicPK, nodeDetail.getPath());
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeSubscriptionsByTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_ABONNEMENTS", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeSubscriptionsByTopic()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Subscriptions - add a subscription
   * @param topicId the subscription topic Id to add
   * @since 1.0
   */
  public void addSubscription(NodePK topicPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubscription()",
        "root.MSG_GEN_ENTER_METHOD");

    if (!checkSubscription(topicPK, userId)) {
      return;
    }

    try {
      getSubscribeBm().addSubscribe(userId, topicPK);
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.addSubscription()",
          "kmelia.EX_SUBSCRIPTION_ADD_FAILED", "topicId = " + topicPK.getId(),
          e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.addSubscription()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * @return true if this topic does not exists in user subscriptions and can be added to them.
   */
  public boolean checkSubscription(NodePK topicPK, String userId) {
    try {
      Collection<NodePK> subscriptions = getSubscribeBm()
          .getUserSubscribePKsByComponent(userId, topicPK.getInstanceId());
      for (Iterator<NodePK> iterator = subscriptions.iterator(); iterator.hasNext();) {
        NodePK nodePK = iterator.next();
        if (topicPK.getId().equals(nodePK.getId())) {
          return false;
        }
      }
      return true;

    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.checkSubscription()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
    }
  }

  /**************************************************************************************/
  /* Interface - Gestion des publications */
  /**************************************************************************************/
  private Collection<UserPublication> pubDetails2userPubs(Collection<PublicationDetail> pubDetails) {
    Iterator<PublicationDetail> iterator = pubDetails.iterator();
    String[] users = new String[pubDetails.size()];
    int i = 0;
    while (iterator.hasNext()) {
      users[i] = (iterator.next()).getCreatorId();
      i++;
    }
    OrganizationController orga = getOrganizationController();
    UserDetail[] userDetails = orga.getUserDetails(users);
    List<UserPublication> list = new ArrayList<UserPublication>();
    iterator = pubDetails.iterator();
    i = 0;
    UserPublication uPub = null;
    while (iterator.hasNext()) {
      uPub = new UserPublication(userDetails[i], (PublicationDetail) iterator
          .next());
      i++;
      list.add(uPub);
    }
    return list;
  }

  /**
   * Return the detail of a publication (only the Header)
   * @param pubId the id of the publication
   * @return a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetail()",
          "root.MSG_GEN_EXIT_METHOD");
      return getPublicationBm().getDetail(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
  }

  /**
   * Return list of all path to this publication - it's a Collection of NodeDetail collection
   * @param pubId the id of the publication
   * @return a Collection of NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection<Collection<NodeDetail>> getPathList(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPathList()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<NodePK> fatherPKs = null;
    try {
      // get all nodePK whick contains this publication
      fatherPKs = getPublicationBm().getAllFatherPK(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPathList()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    try {
      List<Collection<NodeDetail>> pathList = new ArrayList<Collection<NodeDetail>>();
      if (fatherPKs != null) {
        Iterator<NodePK> i = fatherPKs.iterator();
        // For each topic, get the path to it
        while (i.hasNext()) {
          NodePK pk = i.next();
          Collection<NodeDetail> path = getNodeBm().getAnotherPath(pk);
          // add this path
          pathList.add(path);
        }
      }
      SilverTrace.info("kmelia", "KmeliaBmEJB.getPathList()",
          "root.MSG_GEN_EXIT_METHOD");
      return pathList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPathList()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * Create a new Publication (only the header - parameters) to the current Topic
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public String createPublicationIntoTopic(PublicationDetail pubDetail,
      NodePK fatherPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createPublicationIntoTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = null;
    Connection con = getConnection(); // connection usefull for content
    // service
    try {
      pubDetail = changePublicationStatusOnCreation(pubDetail, fatherPK);

      // create the publication
      pubPK = getPublicationBm().createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());

      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
      // add this publication to the current topic
      addPublicationToTopic(pubPK, fatherPK, true);

      // creates todos for publishers
      this.createTodosForPublication(pubDetail, true);

      // alert supervisors
      sendAlertToSupervisors(fatherPK, pubDetail);

    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.createPublicationIntoTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.createPublicationIntoTopic()",
        "root.MSG_GEN_EXIT_METHOD");
    return pubPK.getId();
  }

  private String getProfile(String userId, NodePK nodePK)
      throws RemoteException {
    String profile = null;
    OrganizationController orgCtrl = getOrganizationController();
    if ("yes".equalsIgnoreCase(orgCtrl.getComponentParameterValue(nodePK
        .getInstanceId(), "rightsOnTopics"))) {
      NodeDetail topic = getNodeBm().getHeader(nodePK);
      if (topic.haveRights()) {
        profile = KmeliaHelper.getProfile(orgCtrl
            .getUserProfiles(userId, nodePK.getInstanceId(), topic
                .getRightsDependsOn(), ObjectType.NODE));
      } else {
        profile = KmeliaHelper.getProfile(orgCtrl.getUserProfiles(userId,
            nodePK.getInstanceId()));
      }
    } else {
      profile = KmeliaHelper.getProfile(orgCtrl.getUserProfiles(userId, nodePK
          .getInstanceId()));
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
          "kmelia.ERROR_ON_GETTING_PROFILE", "userId = " + userId + ", node = " +
              nodePK.toString(), e);
    }
    return profile;
  }

  private PublicationDetail changePublicationStatusOnCreation(
      PublicationDetail pubDetail, NodePK nodePK) throws RemoteException {
    SilverTrace.info("kmelia",
        "KmeliaBmEJB.changePublicationStatusOnCreation()",
        "root.MSG_GEN_ENTER_METHOD", "status = " + pubDetail.getStatus());
    String status = pubDetail.getStatus();
    if (status == null || status.equalsIgnoreCase("")) {
      status = PublicationDetail.TO_VALIDATE;

      boolean draftModeUsed = isDraftModeUsed(pubDetail.getPK().getInstanceId());

      if (draftModeUsed) {
        status = PublicationDetail.DRAFT;
      }

      String profile = getProfile(pubDetail.getCreatorId(), nodePK);
      if ("publisher".equals(profile) || "admin".equals(profile)) {
        if (!draftModeUsed) {
          status = PublicationDetail.VALID;
          pubDetail.setValidatorId(pubDetail.getCreatorId());
        }
      }
    }
    pubDetail.setStatus(status);

    KmeliaHelper.checkIndex(pubDetail);

    SilverTrace.info("kmelia",
        "KmeliaBmEJB.changePublicationStatusOnCreation()",
        "root.MSG_GEN_EXIT_METHOD", "status = " + pubDetail.getStatus()
            + ", indexOperation = " + pubDetail.getIndexOperation());

    return pubDetail;
  }

  /**
   * determine new publication's status according to actual status and current user's profile
   * @param pubDetail
   * @return true if status has changed, false otherwise
   */
  private boolean changePublicationStatusOnUpdate(PublicationDetail pubDetail)
      throws RemoteException {
    String oldStatus = pubDetail.getStatus();
    String newStatus = oldStatus;

    List<NodePK> fathers = (List<NodePK>) getPublicationFathers(pubDetail.getPK());

    if (pubDetail.isStatusMustBeChecked()) {
      if (PublicationDetail.DRAFT.equals(oldStatus)
          || PublicationDetail.CLONE.equals(oldStatus)) {
        // the publication is a draft.
        // No status change need.
      } else {
        newStatus = PublicationDetail.TO_VALIDATE;

        NodePK nodePK = new NodePK("unknown", pubDetail.getPK().getInstanceId());
        if (fathers != null && fathers.size() > 0) {
          nodePK = fathers.get(0);
        }

        String profile = getProfile(pubDetail.getUpdaterId(), nodePK);
        if ("supervisor".equals(profile) || KmeliaHelper.ROLE_PUBLISHER.equals(profile) ||
            KmeliaHelper.ROLE_ADMIN.equals(profile)) {
          newStatus = PublicationDetail.VALID;
          pubDetail.setValidatorId(pubDetail.getUpdaterId());
        }
        pubDetail.setStatus(newStatus);
      }
    }

    KmeliaHelper.checkIndex(pubDetail);

    if (fathers == null
        || fathers.size() == 0
        || (fathers.size() == 1 && ((NodePK) fathers.get(0)).getId()
            .equals("1"))) {
      // la publication est dans la corbeille
      pubDetail.setIndexOperation(IndexManager.NONE);
    }

    return !oldStatus.equalsIgnoreCase(newStatus);
  }

  /**
   * Update a publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public void updatePublication(PublicationDetail pubDetail) {
    updatePublication(pubDetail, KmeliaHelper.PUBLICATION_HEADER, false);
  }

  public void updatePublication(PublicationDetail pubDetail, boolean forceUpdateDate) {
    updatePublication(pubDetail, KmeliaHelper.PUBLICATION_HEADER, forceUpdateDate);
  }

  private void updatePublication(PublicationDetail pubDetail, int updateScope,
      boolean forceUpdateDate) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.updatePublication()",
        "root.MSG_GEN_ENTER_METHOD", "updateScope = " + updateScope);
    try {
      // if pubDetail is a clone
      boolean isClone =
          StringUtil.isDefined(pubDetail.getCloneId()) && !"-1".equals(pubDetail.getCloneId()) &&
              !StringUtil.isDefined(pubDetail.getCloneStatus());
      SilverTrace.info("kmelia", "KmeliaBmEJB.updatePublication()", "root.MSG_GEN_PARAM_VALUE",
          "This publication is clone ? " + isClone);
      if (isClone) {
        // update only updateDate
        getPublicationBm().setDetail(pubDetail, forceUpdateDate);
      } else {
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
          String profile = KmeliaHelper.getProfile(getOrganizationController()
              .getUserProfiles(pubDetail.getUpdaterId(),
                  pubDetail.getPK().getInstanceId()));
          if ("supervisor".equals(profile)) {
            sendModificationAlert(updateScope, pubDetail.getPK());
          }

          if (statusChanged) {
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

    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updatePublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.updatePublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /******************************************************************************************/
  /* KMELIA - Copier/coller des documents versionnés */
  /******************************************************************************************/
  public void pasteDocuments(PublicationPK pubPKFrom, String pubId)
      throws Exception {
    SilverTrace.info("kmelia", "KmeliaBmEJB.pasteDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "pubPKFrom = " + pubPKFrom.toString()
            + ", pubId = " + pubId);

    // paste versioning documents attached to publication
    List<Document> documents = getVersioningBm().getDocuments(new ForeignPK(pubPKFrom));

    SilverTrace.info("kmelia", "KmeliaBmEJB.pasteDocuments()",
        "root.MSG_GEN_PARAM_VALUE", documents.size() + " to paste");

    VersioningUtil versioningUtil = new VersioningUtil();
    String pathFrom = null; // where the original files are
    String pathTo = null; // where the copied files will be

    ForeignPK pubPK = new ForeignPK(pubId, pubPKFrom.getInstanceId());

    // change the list of workers
    List<Worker> workers = new ArrayList<Worker>();
    if (documents.size() > 0) {
      List<String> workingProfiles = new ArrayList<String>();
      workingProfiles.add("writer");
      workingProfiles.add("publisher");
      workingProfiles.add("admin");
      String[] userIds = getOrganizationController().getUsersIdsByRoleNames(
          pubPKFrom.getInstanceId(), workingProfiles);

      String userId = null;
      Worker worker = null;
      for (int u = 0; u < userIds.length; u++) {
        userId = (String) userIds[u];
        worker = new Worker(new Integer(userId).intValue(), -1, u, false, true,
            pubPKFrom.getInstanceId(), "U", false, true, 0);
        workers.add(worker);
      }
    }

    // paste each document
    Document document = null;
    List<DocumentVersion> versions = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = (Document) documents.get(d);

      SilverTrace.info("kmelia", "KmeliaBmEJB.pasteDocuments()",
          "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getName());

      // retrieve all versions of the document
      versions = getVersioningBm().getDocumentVersions(document.getPk());

      // retrieve the initial version of the document
      version = (DocumentVersion) versions.get(0);

      if (pathFrom == null) {
        pathFrom = versioningUtil.createPath(document.getPk().getSpaceId(),
            document.getPk().getInstanceId(), null);
      }

      // change some data to paste
      document.setPk(new DocumentPK(-1, pubPKFrom));
      document.setForeignKey(pubPK);
      document.setStatus(Document.STATUS_CHECKINED);
      document.setLastCheckOutDate(new Date());
      document.setWorkList((ArrayList<Worker>) workers);

      if (pathTo == null) {
        pathTo = versioningUtil.createPath("useless",
            pubPKFrom.getInstanceId(), null);
      }

      String newVersionFile = null;
      if (version != null) {
        // paste file on fileserver
        newVersionFile = pasteVersionFile(version, pathFrom, pathTo);
        version.setPhysicalName(newVersionFile);
      }

      // create the document with its first version
      DocumentPK documentPK = getVersioningBm().createDocument(document,
          version);
      document.setPk(documentPK);

      for (int v = 1; v < versions.size(); v++) {
        version = (DocumentVersion) versions.get(v);
        version.setDocumentPK(documentPK);
        SilverTrace.info("kmelia", "KmeliaBmEJB.pasteDocuments()",
            "root.MSG_GEN_PARAM_VALUE", "paste version = "
                + version.getLogicalName());

        // paste file on fileserver
        newVersionFile = pasteVersionFile(version, pathFrom, pathTo);
        version.setPhysicalName(newVersionFile);

        // paste data
        getVersioningBm().addVersion(version);
      }
    }
  }

  private String pasteVersionFile(DocumentVersion version, String from,
      String to) {
    String fileNameFrom = version.getPhysicalName();
    SilverTrace.info("kmelia", "KmeliaBmEJB.pasteVersionFile()",
        "root.MSG_GEN_ENTER_METHOD", "version = " + fileNameFrom);

    if (!fileNameFrom.equals("dummy")) {
      // we have to rename pasted file (in case the copy/paste append in
      // the same instance)
      String type = fileNameFrom.substring(fileNameFrom.lastIndexOf(".") + 1,
          fileNameFrom.length());
      String fileNameTo = new Long(new Date().getTime()).toString() + "."
          + type;

      try {
        // paste file associated to the first version
        FileRepositoryManager.copyFile(from + fileNameFrom, to + fileNameTo);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.pasteVersionFile()",
            SilverpeasRuntimeException.ERROR, "root.EX_FILE_NOT_FOUND", e);
      }
      return fileNameTo;
    } else {
      return fileNameFrom;
    }
  }

  private void updatePublication(PublicationPK pubPK, int updateScope) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    updatePublication(pubDetail, updateScope, false);
  }

  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK) {
    externalElementsOfPublicationHaveChanged(pubPK, null);
  }

  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK,
      String userId) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    if (StringUtil.isDefined(userId)) {
      pubDetail.setUpdaterId(userId);
    }

    // check if related publication is managed by kmelia
    // test due to really hazardous abusive notifications
    if (pubDetail.getPK().getInstanceId().startsWith("kmelia") ||
        pubDetail.getPK().getInstanceId().startsWith("toolbox") ||
        pubDetail.getPK().getInstanceId().startsWith("kmax")) {

      if (!StringUtil.isDefined(userId)) {
        updatePublication(pubDetail, KmeliaHelper.PUBLICATION_CONTENT, false);
      } else {
        // check if user have sufficient rights to update a publication
        String profile = getProfileOnPublication(userId, pubDetail.getPK());
        if ("supervisor".equals(profile) || KmeliaHelper.ROLE_PUBLISHER.equals(profile) ||
            KmeliaHelper.ROLE_ADMIN.equals(profile) || KmeliaHelper.ROLE_WRITER.equals(profile)) {
          updatePublication(pubDetail, KmeliaHelper.PUBLICATION_CONTENT, false);
        } else {
          SilverTrace.warn("kmelia", "KmeliaBmEJB.externalElementsOfPublicationHaveChanged",
              "kmelia.PROBLEM_DETECTED", "user " + userId +
                  " is not allowed to update publication " + pubDetail.getPK().toString());
        }
      }
    }
  }

  /**
   * Delete a publication If this publication is in the basket or in the DZ, it's deleted from the
   * database Else it only send to the basket
   * @param pubId the id of the publication to delete
   * @return a TopicDetail
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   * @since 1.0
   */
  public void deletePublication(PublicationPK pubPK) {
    // if the publication is in the basket or in the DZ
    // this publication is deleted from the database
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublication()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
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
      throw new KmeliaRuntimeException("KmeliaBmEJB.deletePublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION", e);
    }

    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Send the publication in the basket topic
   * @param pubId the id of the publication
   * @see com.stratelia.webactiv.kmelia.model.TopicDetail
   * @since 1.0
   */
  public void sendPublicationToBasket(PublicationPK pubPK, boolean kmaxMode) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.sendPublicationToBasket()",
        "root.MSG_GEN_ENTER_METHOD");
    try {

      // remove coordinates for Kmax
      if (kmaxMode) {
        CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK
            .getSpaceId(), pubPK.getComponentName());
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

    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.sendPublicationToBasket()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DENVOYER_LA_PUBLICATION_A_LA_CORBEILLE", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.sendPublicationToBasket()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void sendPublicationToBasket(PublicationPK pubPK) {
    sendPublicationToBasket(pubPK, false);
  }

  /**
   * Add a publication to a topic and send email alerts to topic subscribers
   * @param pubId the id of the publication
   * @param fatherId the id of the topic
   * @since 1.0
   */
  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK,
      boolean isACreation) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addPublicationToTopic()",
        "root.MSG_GEN_ENTER_METHOD");
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
        } else if (fathers.size() == 0) {
          // The publi have got no father
          // change the end date to make this publi visible
          pubDetail.setEndDate(null);
          getPublicationBm().setDetail(pubDetail);

          // publication is accessible again
          updateSilverContentVisibility(pubDetail);
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.addPublicationToTopic()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", e);
      }
    }

    try {
      getPublicationBm().addFather(pubPK, fatherPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addPublicationToTopic()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", e);
    }

    // sendSubscriptionsNotification(fatherPK, pubDetail);
    sendSubscriptionsNotification(pubDetail, false);
    SilverTrace.info("kmelia", "KmeliaBmEJB.addPublicationToTopic()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private boolean isPublicationInBasket(PublicationPK pubPK)
      throws RemoteException {
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
        if (pk.getId().equals("1")) {
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
      List<NodePK> fathers = (ArrayList<NodePK>) getPublicationFathers(pubDetail.getPK());
      if (fathers != null) {
        for (int i = 0; i < fathers.size(); i++) {
          oneFather = fathers.get(i);
          sendSubscriptionsNotification(oneFather, pubDetail, update);
        }
      }

      // PDC subscriptions
      try {
        int silverObjectId = getSilverObjectId(pubDetail.getPK());

        List<ClassifyPosition> positions = getPdcBm().getPositions(silverObjectId,
            pubDetail.getPK().getInstanceId());

        PdcSubscriptionUtil pdc = new PdcSubscriptionUtil();

        ClassifyPosition position = null;
        for (int p = 0; positions != null && p < positions.size(); p++) {
          position = positions.get(p);
          pdc.checkSubscriptions(position.getValues(), pubDetail.getPK()
              .getInstanceId(), silverObjectId);
        }
      } catch (RemoteException e) {
        SilverTrace.error("kmelia",
            "KmeliaBmEJB.sendSubscriptionsNotification",
            "kmelia.CANT_SEND_PDC_SUBSCRIPTIONS", e);
      }
    }
    return oneFather;
  }

  private void sendSubscriptionsNotification(NodePK fatherPK,
      PublicationDetail pubDetail, boolean update) {
    // send email alerts
    try {
      Collection<NodeDetail> path = null;
      if (!"kmax".equals(pubDetail.getInstanceId())) {
        try {
          path = getNodeBm().getPath(fatherPK);
        } catch (RemoteException re) {
          throw new KmeliaRuntimeException(
              "KmeliaBmEJB.sendSubscriptionsNotification()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_PLACER_LA_PUBLICATION_DANS_LE_THEME", re);
        }
      }

      // build a Collection of nodePK which are the ascendants of fatherPK
      List<NodePK> descendantPKs = new ArrayList<NodePK>();
      if (path != null) {
        Iterator<NodeDetail> it = path.iterator();
        NodePK nodePK = null;
        while (it.hasNext()) {
          nodePK = (it.next()).getNodePK();
          descendantPKs.add(nodePK);
        }
      }

      Collection<String> subscriberIds = getSubscribeBm().getNodeSubscriberDetails(
          descendantPKs);

      OrganizationController orgaController = getOrganizationController();
      if (subscriberIds != null && subscriberIds.size() > 0) {
        // get only subscribers who have sufficient rights to read
        // pubDetail
        Iterator<String> it = subscriberIds.iterator();
        String userId = null;
        NodeDetail node = getNodeHeader(fatherPK);
        List<String> newSubscribers = new ArrayList<String>();
        while (it.hasNext()) {
          userId = it.next();

          if (orgaController.isComponentAvailable(fatherPK.getInstanceId(),
              userId)) {
            if (!node.haveRights()
                || orgaController.isObjectAvailable(node.getRightsDependsOn(),
                    ObjectType.NODE, fatherPK.getInstanceId(), userId)) {
              newSubscribers.add(userId);
            }
          }
        }

        if (newSubscribers.size() > 0) {

          ResourceLocator rs =
              new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "");

          Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
          ResourceLocator message = new ResourceLocator(
              "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
          String subject = message.getString("Subscription");
          String fileName = "notificationSubscriptionCreate";
          if (update) {
            fileName = "notificationSubscriptionUpdate";
          }

          NotificationMetaData notifMetaData =
              new NotificationMetaData(NotificationParameters.NORMAL, subject, templates, fileName);
          for (String lang : getAllLanguages()) {
            SilverpeasTemplate template = getNewTemplate();
            templates.put(lang, template);
            template.setAttribute("path", getHTMLNodePath(fatherPK, lang));
            template.setAttribute("publication", pubDetail);
            template.setAttribute("publicationName", pubDetail.getName(lang));
            template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
            template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
            template.setAttribute("senderName", "");
            template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
            ResourceLocator localizedMessage = new ResourceLocator(
                "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
            notifMetaData
                .addLanguage(lang, localizedMessage.getString("Subscription", subject), "");
          }
          notifMetaData.setUserRecipients(new Vector<String>(newSubscribers));
          notifMetaData.setLink(getPublicationUrl(pubDetail));
          notifMetaData.setComponentId(fatherPK.getInstanceId());
          String senderId = "";
          if (update) {
            senderId = pubDetail.getUpdaterId();
          } else {
            senderId = pubDetail.getCreatorId();
          }
          notifyUsers(notifMetaData, senderId);
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendSubscriptionsNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
              + fatherPK.getId() + ", pubId = " + pubDetail.getPK().getId(), e);
    }
  }

  /**
   * Delete a path between publication and topic
   * @param pubId the id of the publication
   * @param fatherId the id of the topic
   * @since 1.0
   */
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
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION_DE_CE_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublicationFromTopic()",
        "root.MSG_GEN_EXIT_METHOD");
  }

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
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_PUBLICATION_DE_CE_THEME", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.deletePublicationFromAllTopics()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * get all available models
   * @return a Collection of ModelDetail
   * @see com.stratelia.webactiv.util.publication.info.model.ModelDetail
   * @since 1.0
   */
  public Collection<ModelDetail> getAllModels() {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAllModels()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      SilverTrace.info("kmelia", "KmeliaBmEJB.getAllModels()",
          "root.MSG_GEN_EXIT_METHOD");
      return getPublicationBm().getAllModelsDetail();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAllModels()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_MODELES", e);
    }
  }

  /**
   * Return the detail of a model
   * @param modelId the id of the model
   * @return a ModelDetail
   * @see com.stratelia.webactiv.util.publication.Info.model.ModelDetail
   * @since 1.0
   */
  public ModelDetail getModelDetail(String modelId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getModelDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ModelPK modelPK = new ModelPK(modelId);
      SilverTrace.info("kmelia", "KmeliaBmEJB.getModelDetail()",
          "root.MSG_GEN_EXIT_METHOD");
      return getPublicationBm().getModelDetail(modelPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getModelDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_DETAIL_DU_MODELE", e);
    }
  }

  /**
   * Create info attached to a publication
   * @param pubId the id of the publication
   * @param modelId the id of the selected model
   * @param infos an InfoDetail containing info
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public void createInfoDetail(PublicationPK pubPK, String modelId,
      InfoDetail infos) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ModelPK modelPK = new ModelPK(modelId, pubPK);
      checkIndex(pubPK, infos);
      getPublicationBm().createInfoDetail(pubPK, modelPK, infos);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DENREGISTRER_LE_CONTENU_DU_MODELE", e);
    }
    updatePublication(pubPK, KmeliaHelper.PUBLICATION_CONTENT);
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoDetail()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Create model info attached to a publication
   * @param pubId the id of the publication
   * @param modelId the id of the selected model
   * @param infos an InfoDetail containing info
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public void createInfoModelDetail(PublicationPK pubPK, String modelId,
      InfoDetail infos) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoModelDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      ModelPK modelPK = new ModelPK(modelId, pubPK);

      checkIndex(pubPK, infos);
      getPublicationBm().createInfoModelDetail(pubPK, modelPK, infos);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createInfoModelDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DENREGISTRER_LE_CONTENU_DU_MODELE", e);
    }
    updatePublication(pubPK, KmeliaHelper.PUBLICATION_CONTENT);
    SilverTrace.info("kmelia", "KmeliaBmEJB.createInfoModelDetail()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * get info attached to a publication
   * @param pubId the id of the publication
   * @return an InfoDetail
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public InfoDetail getInfoDetail(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getPublicationBm().getInfoDetail(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_CONTENU_DU_MODELE", e);
    }
  }

  /**
   * Update info attached to a publication
   * @param pubId the id of the publication
   * @param infos an InfoDetail containing info to updated
   * @see com.stratelia.webactiv.util.Publication.Info.model.InfoDetail
   * @since 1.0
   */
  public void updateInfoDetail(PublicationPK pubPK, InfoDetail infos) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.updateInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      checkIndex(pubPK, infos);
      getPublicationBm().updateInfoDetail(pubPK, infos);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.updateInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
    updatePublication(pubPK, KmeliaHelper.PUBLICATION_CONTENT);
    SilverTrace.info("kmelia", "KmeliaBmEJB.updateInfoDetail()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   */
  public void addInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.addInfoLinks()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId() + ", pubIds = "
            + links.toString());
    try {
      getPublicationBm().addLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addInfoLinks()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
  }

  public void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteInfoLinks()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId() + ", pubIds = "
            + links.toString());
    try {
      getPublicationBm().deleteInfoLinks(pubPK, links);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteInfoLinks()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LE_CONTENU_DU_MODELE", e);
    }
  }

  /**
   * Return all info of a publication and add a reading statistic
   * @param pubId the id of a publication
   * @return a CompletePublication
   * @see com.stratelia.webactiv.util.publication.model.CompletePublication
   * @since 1.0
   */
  public UserCompletePublication getUserCompletePublication(
      PublicationPK pubPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getUserCompletePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    CompletePublication completePublication = null;

    try {
      completePublication = getPublicationBm().getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.getUserCompletePublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }

    PublicationDetail pub = completePublication.getPublicationDetail();
    UserDetail userDetail = getOrganizationController().getUserDetail(
        pub.getCreatorId());

    SilverTrace.info("kmelia", "KmeliaBmEJB.getUserCompletePublication()",
        "root.MSG_GEN_EXIT_METHOD");

    return new UserCompletePublication(userDetail, completePublication);
  }

  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getCompletePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    CompletePublication completePublication = null;

    try {
      completePublication = getPublicationBm().getCompletePublication(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getCompletePublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.getCompletePublication()",
        "root.MSG_GEN_EXIT_METHOD");
    return completePublication;
  }

  public FullPublication getFullPublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getFullPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    FullPublication fullPublication = null;
    try {
      // retrieve completePublication
      CompletePublication completePublication = getPublicationBm()
          .getCompletePublication(pubPK);

      // retrieve attachments
      List<AttachmentDetail> attachments = (List<AttachmentDetail>) getAttachments(pubPK);

      // retrieve pdc positions of publication
      List pdcPositions = getPdcBm().getPositions(getSilverObjectId(pubPK),
          pubPK.getInstanceId());

      fullPublication = new FullPublication(completePublication, attachments,
          pdcPositions);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getFullPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", e);
    }
    return fullPublication;
  }

  public TopicDetail getPublicationFather(PublicationPK pubPK,
      boolean isTreeStructureUsed, String userId, boolean isRightsOnTopicsUsed) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFather()",
        "root.MSG_GEN_ENTER_METHOD");
    TopicDetail fatherDetail = null;
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
          if (!father.haveRights()
              || getOrganizationController().isObjectAvailable(
                  father.getRightsDependsOn(), ObjectType.NODE,
                  fatherPK.getInstanceId(), userId)) {
            allowedFather = father;
          }
        }
        if (allowedFather != null) {
          fatherPK = allowedFather.getNodePK();
        }
      }
    }
    String profile = KmeliaHelper.getProfile(getOrganizationController()
        .getUserProfiles(userId, pubPK.getInstanceId()));
    fatherDetail = this.goTo(fatherPK, userId, isTreeStructureUsed, profile,
        isRightsOnTopicsUsed);
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFather()",
        "root.MSG_GEN_EXIT_METHOD");
    return fatherDetail;
  }

  public Collection<NodePK> getPublicationFathers(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    try {
      Collection<NodePK> fathers = getPublicationBm().getAllFatherPK(pubPK);
      if (fathers == null || fathers.size() == 0) {
        SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
            "root.MSG_GEN_PARAM_VALUE", "Following publication have got no fathers : pubPK = " +
                pubPK.toString());
        // This publication have got no father !
        // Check if it's a clone (a clone have got no father ever)
        boolean alwaysVisibleModeActivated =
            "yes".equalsIgnoreCase(getOrganizationController().getComponentParameterValue(
                pubPK.getInstanceId(), "publicationAlwaysVisible"));
        if (alwaysVisibleModeActivated) {
          SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
              "root.MSG_GEN_PARAM_VALUE", "Getting the publication");
          PublicationDetail publi = getPublicationBm().getDetail(pubPK);
          if (publi != null) {
            boolean isClone =
                StringUtil.isDefined(publi.getCloneId()) &&
                    !StringUtil.isDefined(publi.getCloneStatus());
            SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
                "root.MSG_GEN_PARAM_VALUE", "This publication is clone ? " + isClone);
            if (isClone) {
              // This publication is a clone
              // Get fathers from main publication
              fathers = getPublicationBm().getAllFatherPK(publi.getClonePK());
              SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationFathers()",
                  "root.MSG_GEN_PARAM_VALUE",
                  "Main publication's fathers fetched. # of fathers = " + fathers.size());
            }
          }
        }
      }
      return fathers;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationFathers()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_UN_PERE_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * gets a list of PublicationDetail corresponding to the links parameter
   * @param links list of publication (componentID + publicationId)
   * @return a list of PublicationDetail
   */
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
      throw new KmeliaRuntimeException("KmeliaBmEJB.getPublicationDetails()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_PUBLICATIONS", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationDetails()",
        "root.MSG_GEN_EXIT_METHOD");
    return publications;
  }

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
      boolean isRightsOnTopicsUsed) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublications()", "root.MSG_GEN_ENTER_METHOD");
    // initialization of the publications list
    List<ForeignPK> allowedPublicationIds = new ArrayList<ForeignPK>(links);
    if (isRightsOnTopicsUsed) {
      KmeliaSecurity security = new KmeliaSecurity();
      allowedPublicationIds.clear();

      // check if the publication is authorized for current user
      for (ForeignPK link : links) {
        PublicationPK pubPK = new PublicationPK(link.getId(),
            link.getInstanceId());
        if (security.isObjectAvailable(link.getInstanceId(), userId, link.getId(), "Publication")) {
          allowedPublicationIds.add(link);
        }
      }
    }
    Collection<PublicationDetail> publications =
        getPublicationDetails(allowedPublicationIds);
    return pubDetails2userPubs(publications);
  }

  public List<UserPublication> getPublicationsToValidate(String componentId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsToValidate()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<PublicationDetail> publications = null;
    PublicationPK pubPK = new PublicationPK("useless", componentId);
    try {
      publications = getPublicationBm().getPublicationsByStatus(
          PublicationDetail.TO_VALIDATE, pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.getPublicationsToValidate()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_PUBLICATIONS_A_VALIDER", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationsToValidate()",
        "root.MSG_GEN_EXIT_METHOD");
    return (List<UserPublication>) pubDetails2userPubs(publications);
  }

  private void sendValidationNotification(NodePK fatherPK,
      PublicationDetail pubDetail, String refusalMotive, String userIdWhoRefuse) {
    String userId = pubDetail.getUpdaterId();
    if (!StringUtil.isDefined(userId)) {
      userId = pubDetail.getCreatorId();
    }

    try {
      if (userId != null) {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
        String fileName = "notificationRefused";
        String subject = message.getString("PublicationRefused");
        if (!StringUtil.isDefined(refusalMotive)) {
          fileName = "notificationValidation";
          subject = message.getString("PublicationValidated");
        }
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates, fileName);
        for (String lang : getAllLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("path", getHTMLNodePath(fatherPK, lang));
          template.setAttribute("publication", pubDetail);
          template.setAttribute("publicationName", pubDetail.getName(lang));
          template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
          template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
          template.setAttribute("senderName", "");
          template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
          template.setAttribute("refusalMotive", refusalMotive);
          ResourceLocator localizedMessage = new ResourceLocator(
              "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
          subject = localizedMessage.getString("PublicationRefused");
          if (!StringUtil.isDefined(refusalMotive)) {
            subject = message.getString("PublicationValidated");
          }
          notifMetaData.addLanguage(lang, subject, "");
        }
        notifMetaData.addUserRecipient(userId);
        notifMetaData.setLink(getPublicationUrl(pubDetail));
        notifMetaData.setComponentId(pubDetail.getPK().getInstanceId());
        notifyUsers(notifMetaData, userIdWhoRefuse);
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendValidationNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
              + fatherPK.getId() + ", pubPK = " + pubDetail.getPK(), e);
    }
  }

  /**
   * @param nodePK
   * @return a String like Space1 > SubSpace > Component2 > Topic1 > Topic2
   */
  private String getHTMLNodePath(NodePK nodePK, String language) {
    // get the path of the topic where the publication is classified
    String htmlPath = "";
    if (nodePK != null) {
      try {
        List<NodeDetail> path = (List<NodeDetail>) getNodeBm().getPath(nodePK);
        if (path.size() > 0) {
          // remove root topic "Accueil"
          path.remove(path.size() - 1);
        }
        htmlPath = getSpacesPath(nodePK.getInstanceId(), language)
            + getComponentLabel(nodePK.getInstanceId(), language) + " > "
            + displayPath(path, 10, language);
      } catch (RemoteException re) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.getHTMLNodePath()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION",
            re);
      }
    }
    return htmlPath;
  }

  private String getSpacesPath(String componentId, String language) {
    String spacesPath = "";
    List<SpaceInst> spaces = getOrganizationController().getSpacePathToComponent(
        componentId);
    Iterator<SpaceInst> iSpaces = spaces.iterator();
    SpaceInst spaceInst = null;
    while (iSpaces.hasNext()) {
      spaceInst = iSpaces.next();
      spacesPath += spaceInst.getName(language);
      spacesPath += " > ";
    }
    return spacesPath;
  }

  private void sendAlertToSupervisors(NodePK fatherPK,
      PublicationDetail pubDetail) {
    if (PublicationDetail.VALID.equalsIgnoreCase(pubDetail.getStatus())) {
      try {

        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);

        String subject = message.getString("kmelia.SupervisorNotifSubject");

        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                "notificationSupervisor");
        for (String lang : getAllLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("path", getHTMLNodePath(fatherPK, lang));
          template.setAttribute("publication", pubDetail);
          template.setAttribute("publicationName", pubDetail.getName(lang));
          template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
          template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
          template.setAttribute("senderName", "");
          template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
          ResourceLocator localizedMessage = new ResourceLocator(
              "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
          notifMetaData.addLanguage(lang, localizedMessage.getString(
              "kmelia.SupervisorNotifSubject", subject), "");
        }
        notifMetaData.setSender(pubDetail.getUpdaterId());
        List<String> roles = new ArrayList<String>();
        roles.add("supervisor");
        String[] supervisors = getOrganizationController()
            .getUsersIdsByRoleNames(pubDetail.getPK().getInstanceId(), roles);
        SilverTrace.debug("kmelia", "KmeliaBmEJB.alertSupervisors()",
            "root.MSG_GEN_PARAM_VALUE", supervisors.length
                + " users in role supervisor !");
        notifMetaData.addUserRecipients(supervisors);

        notifMetaData.setLink(getPublicationUrl(pubDetail));
        notifMetaData.setComponentId(pubDetail.getPK().getInstanceId());
        notifyUsers(notifMetaData, pubDetail.getUpdaterId());
      } catch (Exception e) {
        SilverTrace.warn("kmelia", "KmeliaBmEJB.alertSupervisors()",
            "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "fatherId = "
                + fatherPK.getId() + ", pubPK = " + pubDetail.getPK(), e);
      }
    }
  }

  public List<String> getAllValidators(PublicationPK pubPK, int validationType)
      throws RemoteException {
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId()
            + ", validationType = " + validationType);
    if (validationType == -1) {
      String sParam = getOrganizationController().getComponentParameterValue(
          pubPK.getInstanceId(), "targetValidation");
      if (StringUtil.isDefined(sParam)) {
        validationType = Integer.parseInt(sParam);
      } else {
        validationType = KmeliaHelper.VALIDATION_CLASSIC;
      }
    }
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators",
        "root.MSG_GEN_PARAM_VALUE", "validationType = " + validationType);

    // get all users who have to validate
    List<String> allValidators = new ArrayList<String>();
    if (validationType == KmeliaHelper.VALIDATION_TARGET_N
        || validationType == KmeliaHelper.VALIDATION_TARGET_1) {
      PublicationDetail publi = getPublicationBm().getDetail(pubPK);
      if (StringUtil.isDefined(publi.getTargetValidatorId())) {
        StringTokenizer tokenizer = new StringTokenizer(publi
            .getTargetValidatorId(), ",");
        while (tokenizer.hasMoreTokens()) {
          allValidators.add(tokenizer.nextToken());
        }
      }
    }
    if (allValidators.isEmpty()) {
      // It's not a targeted validation or it is but no validators has
      // been selected !
      List<String> roles = new ArrayList<String>();
      roles.add("admin");
      roles.add("publisher");

      if (KmeliaHelper.isKmax(pubPK.getInstanceId())) {
        allValidators.addAll(Arrays.asList(getOrganizationController()
            .getUsersIdsByRoleNames(pubPK.getInstanceId(), roles)));
      } else {
        // get admin and publishers of all nodes where publication is
        List<NodePK> nodePKs = (List<NodePK>) getPublicationFathers(pubPK);
        NodePK nodePK = null;
        NodeDetail node = null;
        boolean oneNodeIsPublic = false;
        for (int n = 0; !oneNodeIsPublic && nodePKs != null
            && n < nodePKs.size(); n++) {
          nodePK = (NodePK) nodePKs.get(n);
          node = getNodeHeader(nodePK);
          if (node != null) {
            SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators",
                "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK.toString());
            if (!node.haveRights()) {
              allValidators.addAll(Arrays.asList(getOrganizationController()
                  .getUsersIdsByRoleNames(pubPK.getInstanceId(), roles)));
              oneNodeIsPublic = true;
            } else {
              allValidators.addAll(Arrays.asList(getOrganizationController()
                  .getUsersIdsByRoleNames(pubPK.getInstanceId(),
                      Integer.toString(node.getRightsDependsOn()),
                      ObjectType.NODE, roles)));
            }
          }
        }
      }
    }
    SilverTrace.debug("kmelia", "KmeliaBmEJB.getAllValidators",
        "root.MSG_GEN_EXIT_METHOD", "pubId = " + pubPK.getId()
            + ", allValidators = " + allValidators.toString());
    return allValidators;
  }

  private boolean isValidationComplete(PublicationPK pubPK, int validationType)
      throws RemoteException {
    List<ValidationStep> steps = getPublicationBm().getValidationSteps(pubPK);

    // get users who have already validate
    List<String> stepUserIds = new ArrayList<String>();
    for (int s = 0; s < steps.size(); s++) {
      stepUserIds.add((steps.get(s)).getUserId());
    }

    // get all users who have to validate
    List<String> allValidators = getAllValidators(pubPK, validationType);

    // check if all users have validate
    boolean validationOK = true;
    String validatorId = null;
    for (int i = 0; validationOK && i < allValidators.size(); i++) {
      validatorId = allValidators.get(i);
      validationOK = stepUserIds.contains(validatorId);
    }

    return validationOK;
  }

  public boolean validatePublication(PublicationPK pubPK, String userId,
      int validationType, boolean force) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.validatePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    boolean validationComplete = false;
    try {
      if (force) {
        /* remove all todos attached to that publication */
        removeAllTodosForPublication(pubPK);

        validationComplete = true;
      } else {
        ValidationStep validation = null;
        switch (validationType) {
          case KmeliaHelper.VALIDATION_CLASSIC:
          case KmeliaHelper.VALIDATION_TARGET_1:

            /* remove all todos attached to that publication */
            removeAllTodosForPublication(pubPK);

            validationComplete = true;

            break;

          case KmeliaHelper.VALIDATION_COLLEGIATE:
          case KmeliaHelper.VALIDATION_TARGET_N:

            // remove todo to this user. Job is done !
            removeTodoForPublication(pubPK, userId);

            // save his decision
            validation = new ValidationStep(pubPK, userId,
                PublicationDetail.VALID);
            getPublicationBm().addValidationStep(validation);

            // check if all validators have give their decision
            validationComplete = isValidationComplete(pubPK, validationType);

            if (validationComplete) {
              removeAllTodosForPublication(pubPK);
            }
        }
      }

      if (validationComplete) {
        // remove validation steps cause it is complete
        getPublicationBm().removeValidationSteps(pubPK);

        CompletePublication currentPub = getPublicationBm()
            .getCompletePublication(pubPK);
        PublicationDetail currentPubDetail = currentPub.getPublicationDetail();

        if (currentPubDetail.haveGotClone()) {
          currentPubDetail = mergeClone(currentPub, userId);
        } else if (PublicationDetail.TO_VALIDATE
            .equalsIgnoreCase(currentPubDetail.getStatus())) {
          currentPubDetail.setValidatorId(userId);
          currentPubDetail.setValidateDate(new Date());
          currentPubDetail.setStatus(PublicationDetail.VALID);
        } else if (PublicationDetail.REFUSED.equalsIgnoreCase(currentPubDetail
            .getStatus())) {
          // do nothing
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
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_VALIDER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.validatePublication()",
        "root.MSG_GEN_EXIT_METHOD", "validationComplete = "
            + validationComplete);
    return validationComplete;
  }

  private PublicationDetail getClone(PublicationDetail refPub) {
    PublicationDetail clone = new PublicationDetail();
    if (refPub.getAuthor() != null) {
      clone.setAuthor(new String(refPub.getAuthor()));
    }
    if (refPub.getBeginDate() != null) {
      clone.setBeginDate(new Date(refPub.getBeginDate().getTime()));
    }
    if (refPub.getBeginHour() != null) {
      clone.setBeginHour(new String(refPub.getBeginHour()));
    }
    if (refPub.getContent() != null) {
      clone.setContent(new String(refPub.getContent()));
    }
    clone.setCreationDate(new Date(refPub.getCreationDate().getTime()));
    clone.setCreatorId(new String(refPub.getCreatorId()));
    if (refPub.getDescription() != null) {
      clone.setDescription(new String(refPub.getDescription()));
    }
    if (refPub.getEndDate() != null) {
      clone.setEndDate(new Date(refPub.getEndDate().getTime()));
    }
    if (refPub.getEndHour() != null) {
      clone.setEndHour(new String(refPub.getEndHour()));
    }
    clone.setImportance(refPub.getImportance());
    if (refPub.getInfoId() != null) {
      clone.setInfoId(new String(refPub.getInfoId()));
    }
    if (refPub.getKeywords() != null) {
      clone.setKeywords(new String(refPub.getKeywords()));
    }
    if (refPub.getName() != null) {
      clone.setName(new String(refPub.getName()));
    }
    clone.setPk(new PublicationPK(new String(refPub.getPK().getId()), refPub
        .getPK().getInstanceId()));
    if (refPub.getStatus() != null) {
      clone.setStatus(new String(refPub.getStatus()));
    }
    if (refPub.getTargetValidatorId() != null) {
      clone.setTargetValidatorId(new String(refPub.getTargetValidatorId()));
    }
    if (refPub.getCloneId() != null) {
      clone.setCloneId(new String(refPub.getCloneId()));
    }
    if (refPub.getUpdateDate() != null) {
      clone.setUpdateDate(new Date(refPub.getUpdateDate().getTime()));
    }
    if (refPub.getUpdaterId() != null) {
      clone.setUpdaterId(new String(refPub.getUpdaterId()));
    }
    if (refPub.getValidateDate() != null) {
      clone.setValidateDate(new Date(refPub.getValidateDate().getTime()));
    }
    if (refPub.getValidatorId() != null) {
      clone.setValidatorId(new String(refPub.getValidatorId()));
    }
    if (refPub.getVersion() != null) {
      clone.setVersion(new String(refPub.getVersion()));
    }

    return clone;
  }

  private PublicationDetail mergeClone(CompletePublication currentPub,
      String userId) throws RemoteException, FormException,
      PublicationTemplateException, AttachmentException {
    PublicationDetail currentPubDetail = currentPub.getPublicationDetail();
    String memInfoId = currentPubDetail.getInfoId();
    PublicationPK pubPK = currentPubDetail.getPK();
    // merge du clone sur la publi de référence
    String cloneId = currentPubDetail.getCloneId();
    if (!"-1".equals(cloneId)) {
      PublicationPK tempPK = new PublicationPK(cloneId, pubPK);
      CompletePublication tempPubli = getPublicationBm()
          .getCompletePublication(tempPK);
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

      // merge du contenu DBModel
      if (tempPubli.getModelDetail() != null) {
        if (currentPub.getModelDetail() != null) {
          currentPubDetail.setInfoId(memInfoId);

          // il existait déjà un contenu
          getPublicationBm().updateInfoDetail(pubPK, tempPubli.getInfoDetail());
        } else {
          // il n'y avait pas encore de contenu
          ModelPK modelPK = new ModelPK(tempPubli.getModelDetail().getId(),
              "useless", tempPK.getInstanceId());
          getPublicationBm().createInfoModelDetail(pubPK, modelPK,
              tempPubli.getInfoDetail());

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
          PublicationTemplateManager publicationTemplateManager =
                  PublicationTemplateManager.getInstance();
          PublicationTemplate pubTemplate = publicationTemplateManager.getPublicationTemplate(
                  tempPK.getInstanceId() + ":" + xmlFormShortName);

          RecordSet set = pubTemplate.getRecordSet();
          // DataRecord data = set.getRecord(fromId);

          if (memInfoId != null && !"0".equals(memInfoId)) {
            // il existait déjà un contenu
            set.merge(cloneId, pubPK.getInstanceId(), pubPK.getId(), pubPK.getInstanceId());
          } else {
            // il n'y avait pas encore de contenu
            publicationTemplateManager.addDynamicPublicationTemplate(tempPK
                .getInstanceId()
                + ":" + xmlFormShortName, xmlFormShortName + ".xml");

            set.clone(cloneId, pubPK.getInstanceId(), pubPK.getId(), pubPK.getInstanceId());
          }
        }
      }

      // merge du contenu Wysiwyg
      boolean cloneWysiwyg = WysiwygController.haveGotWysiwyg("useless", tempPK
          .getInstanceId(), cloneId);
      if (cloneWysiwyg) {
        WysiwygController.copy("useless", tempPK.getInstanceId(), cloneId,
            "useless", pubPK.getInstanceId(), pubPK.getId(), tempPubli
                .getPublicationDetail().getUpdaterId());
      }

      // merge des fichiers joints
      AttachmentPK pkFrom = new AttachmentPK(pubPK.getId(), pubPK
          .getInstanceId());
      AttachmentPK pkTo = new AttachmentPK(cloneId, tempPK.getInstanceId());
      AttachmentController.mergeAttachments(pkFrom, pkTo);

      // merge des fichiers versionnés

      // delete xml content
      removeXMLContentOfPublication(tempPK);

      // suppression du clone
      deletePublication(tempPK);
    }
    return currentPubDetail;
  }

  public void unvalidatePublication(PublicationPK pubPK, String userId,
      String refusalMotive, int validationType) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.unvalidatePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      switch (validationType) {
        case KmeliaHelper.VALIDATION_CLASSIC:
        case KmeliaHelper.VALIDATION_TARGET_1:

          // do nothing
          break;

        case KmeliaHelper.VALIDATION_COLLEGIATE:
        case KmeliaHelper.VALIDATION_TARGET_N:

          // reset other decisions
          getPublicationBm().removeValidationSteps(pubPK);
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
      } else {
        // send unvalidate publication to basket
        // sendPublicationToBasket(pubPK);

        // change publication's status
        currentPubDetail.setStatus("UnValidate");

        KmeliaHelper.checkIndex(currentPubDetail);

        getPublicationBm().setDetail(currentPubDetail);

        // change visibility over PDC
        updateSilverContentVisibility(currentPubDetail);

        // we have to alert publication's creator
        List<NodePK> fathers = (List<NodePK>) getPublicationFathers(pubPK);
        NodePK oneFather = null;
        if (fathers != null && fathers.size() > 0) {
          oneFather = fathers.get(0);
        }
        sendValidationNotification(oneFather, currentPubDetail, refusalMotive,
            userId);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.unvalidatePublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_REFUSER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.unvalidatePublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

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
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_REFUSER_LA_PUBLICATION", e);
    }
    SilverTrace.info("kmelia", "KmeliaBmEJB.suspendPublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void sendDefermentNotification(PublicationDetail pubDetail,
      String defermentMotive, String senderId) {
    String userId = pubDetail.getUpdaterId();
    if (!StringUtil.isDefined(userId)) {
      userId = pubDetail.getCreatorId();
    }

    try {
      if (userId != null) {
        Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
        ResourceLocator message = new ResourceLocator(
            "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
        String subject = message.getString("kmelia.PublicationSuspended");

        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
                "notification");
        for (String lang : getAllLanguages()) {
          SilverpeasTemplate template = getNewTemplate();
          templates.put(lang, template);
          template.setAttribute("path", "");
          template.setAttribute("publication", pubDetail);
          template.setAttribute("publicationName", pubDetail.getName(lang));
          template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
          template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
          template.setAttribute("senderName", "");
          template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
          template.setAttribute("refusalMotive", defermentMotive);
          ResourceLocator localizedMessage = new ResourceLocator(
              "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
          notifMetaData.addLanguage(lang, localizedMessage.getString("kmelia.PublicationSuspended",
              subject), "");
        }

        notifMetaData.setSender(userId);
        notifMetaData.setLink(getPublicationUrl(pubDetail));
        notifMetaData.setComponentId(pubDetail.getPK().getInstanceId());
        notifyUsers(notifMetaData, senderId);
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "KmeliaBmEJB.sendDefermentNotification()",
          "kmelia.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "pubPK = "
              + pubDetail.getPK(), e);
    }
  }

  /**
   * Change publication status from draft to valid (for publisher) or toValidate (for redactor)
   * @param publicationId the id of the publication
   * @since 3.0
   */
  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile) {
    draftOutPublication(pubPK, topicPK, userProfile, false);
  }

  public void draftOutPublication(PublicationPK pubPK, NodePK topicPK,
      String userProfile, boolean forceUpdateDate) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.draftOutPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());

    try {
      CompletePublication currentPub = getPublicationBm()
          .getCompletePublication(pubPK);
      PublicationDetail pubDetail = currentPub.getPublicationDetail();

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftOutPublication()",
          "root.MSG_GEN_PARAM_VALUE", "actual status = "
              + pubDetail.getStatus());
      if (userProfile.equals("publisher") || userProfile.equals("admin")) {
        if (pubDetail.haveGotClone()) {
          pubDetail = mergeClone(currentPub, null);
        }

        pubDetail.setStatus(PublicationDetail.VALID);
      } else {
        if (pubDetail.haveGotClone()) {
          // changement du statut du clone
          PublicationDetail clone = getPublicationBm().getDetail(
              pubDetail.getClonePK());
          clone.setStatus(PublicationDetail.TO_VALIDATE);
          clone.setIndexOperation(IndexManager.NONE);
          clone.setUpdateDateMustBeSet(false);

          getPublicationBm().setDetail(clone);

          pubDetail.setCloneStatus(PublicationDetail.TO_VALIDATE);
        } else {
          pubDetail.setStatus(PublicationDetail.TO_VALIDATE);
        }

        // create validation todos for publishers
        createTodosForPublication(pubDetail, true);
      }

      KmeliaHelper.checkIndex(pubDetail);

      getPublicationBm().setDetail(pubDetail, forceUpdateDate);

      // update visibility attribute on PDC
      updateSilverContentVisibility(pubDetail);

      // index all publication's elements
      indexExternalElementsOfPublication(pubDetail);

      // alert subscribers
      sendSubscriptionsNotification(pubDetail, false);

      // alert supervisors
      if (topicPK != null) {
        sendAlertToSupervisors(topicPK, pubDetail);
      }

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftOutPublication()",
          "root.MSG_GEN_PARAM_VALUE", "new status = " + pubDetail.getStatus());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.draftOutPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
  }

  /**
   * Change publication status from any state to draft
   * @param publicationId the id of the publication
   * @since 3.0
   */
  public void draftInPublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.draftInPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
    try {
      PublicationDetail pubDetail = getPublicationBm().getDetail(pubPK);

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftInPublication()",
          "root.MSG_GEN_PARAM_VALUE", "actual status = "
              + pubDetail.getStatus());
      pubDetail.setStatus(PublicationDetail.DRAFT);

      KmeliaHelper.checkIndex(pubDetail);

      getPublicationBm().setDetail(pubDetail);

      updateSilverContentVisibility(pubDetail);

      unIndexExternalElementsOfPublication(pubDetail.getPK());

      removeAllTodosForPublication(pubPK);

      SilverTrace.info("kmelia", "KmeliaBmEJB.draftInPublication()",
          "root.MSG_GEN_PARAM_VALUE", "new status = " + pubDetail.getStatus());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.draftInPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", e);
    }
  }

  /*************************************************************/
  /** SCO - 26/12/2002 Integration AlertUser et AlertUserPeas **/
  /*************************************************************/
  public NotificationMetaData getAlertNotificationMetaData(PublicationPK pubPK,
      NodePK topicPK, String senderName) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAlertNotificationMetaData()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationDetail pubDetail = getPublicationDetail(pubPK);

    Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
    ResourceLocator message = new ResourceLocator(
        "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
    String subject = message.getString("Alert");

    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL, subject, templates, "notification");
    for (String lang : getAllLanguages()) {
      SilverpeasTemplate template = getNewTemplate();
      templates.put(lang, template);
      template.setAttribute("path", getHTMLNodePath(topicPK, lang));
      template.setAttribute("publication", pubDetail);
      template.setAttribute("publicationName", pubDetail.getName(lang));
      template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
      template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
      template.setAttribute("senderName", senderName);
      template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
      ResourceLocator localizedMessage = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
      notifMetaData.addLanguage(lang, localizedMessage.getString("Alert", subject), "");
    }
    notifMetaData.setLink(getPublicationUrl(pubDetail));
    notifMetaData.setComponentId(pubPK.getInstanceId());
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAlertNotificationMetaData()",
        "root.MSG_GEN_EXIT_METHOD");
    return notifMetaData;
  }

  /*************************************************************/
  /**************************************************************************************/
  /* Controle de lecture */
  /**************************************************************************************/
  /**
   * delete reading controls to a publication
   * @param pubId the id of a publication
   * @since 1.0
   */
  public void deleteAllReadingControlsByPublication(PublicationPK pubPK) {
    SilverTrace.info("kmelia",
        "KmeliaBmEJB.deleteAllReadingControlsByPublication()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      // getReadingControlBm().removeReadingControlByPublication(pubPK);
      getStatisticBm()
          .deleteHistoryByAction(
              new ForeignPK(pubPK.getId(), pubPK.getInstanceId()), 1,
              "Publication");
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.deleteAllReadingControlsByPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_CONTROLES_DE_LECTURE", e);
    }
    SilverTrace.info("kmelia",
        "KmeliaBmEJB.deleteAllReadingControlsByPublication()",
        "root.MSG_GEN_EXIT_METHOD");
  }

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
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_PUBLICATIONS", e);
    }

    if (pubs != null) {
      Iterator<PublicationDetail> it = pubs.iterator();
      PublicationDetail pub = null;
      while (it.hasNext()) {
        pub = it.next();

        try {
          pubPK = pub.getPK();
          List<NodePK> pubFathers = null;
          // index only valid publications
          if (pub.getStatus() != null
              && pub.getStatus().equalsIgnoreCase(PublicationDetail.VALID)) {
            pubFathers = (List<NodePK>) getPublicationBm().getAllFatherPK(pubPK);
            // index only valid publications which are not only in
            // dz or basket
            if (pubFathers.size() >= 2) {
              indexPublication(pubPK);
            } else if (pubFathers.size() == 1) {
              NodePK nodePK = pubFathers.get(0);
              // index the valid publication if it is not in the
              // basket
              if (!nodePK.getId().equals("1")) {
                indexPublication(pubPK);
              }
            } else {
              // don't index publications in the dz
            }
          }
        } catch (Exception e) {
          throw new KmeliaRuntimeException("KmeliaBmEJB.indexPublications()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DINDEXER_LA_PUBLICATION", "pubPK = "
                  + pubPK.toString(), e);
        }
      }
    }
  }

  private void indexPublication(PublicationPK pubPK) throws RemoteException {
    // index publication itself
    getPublicationBm().createIndex(pubPK);

    // index external elements
    indexExternalElementsOfPublication(pubPK);
  }

  private void indexTopics(NodePK nodePK) {
    Collection<NodeDetail> nodes = null;
    try {
      nodes = getNodeBm().getAllNodes(nodePK);
      if (nodes != null) {
        Iterator<NodeDetail> it = nodes.iterator();
        NodeDetail node = null;
        while (it.hasNext()) {
          node = it.next();
          if (!node.getNodePK().getId().equals("0")
              && !node.getNodePK().getId().equals("1")
              && !node.getNodePK().getId().equals("2")) {
            getNodeBm().createIndex(node);
          }
        }
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.indexTopics()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DINDEXER_LES_THEMES", e);
    }
  }

  /**************************************************************************************/
  /* Gestion des todos */
  /**************************************************************************************/
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
    if (PublicationDetail.TO_VALIDATE.equals(pubDetail.getStatus())
        || PublicationDetail.TO_VALIDATE.equals(pubDetail.getCloneStatus())) {
      List<String> validators = getAllValidators(pubDetail.getPK(), -1);
      String[] users = (String[]) validators.toArray(new String[validators
          .size()]);

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

    Vector<Attendee> attendees = new Vector<Attendee>();
    for (int i = 0; i < users.length; i++) {
      if (users[i] != null) {
        attendees.add(new Attendee(users[i]));
      }
    }
    todo.setAttendees(attendees);
    if (pubDetail.getUpdaterId() != null) {
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
    todoBBA.removeEntriesFromExternal("useless", pubPK.getInstanceId(), pubPK
        .getId());
  }

  private void removeTodoForPublication(PublicationPK pubPK, String userId) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.removeTodoForPublication()",
        "root.MSG_GEN_ENTER_METHOD", "Enter pubPK =" + pubPK.toString());

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    todoBBA.removeAttendeeToEntryFromExternal(pubPK.getInstanceId(), pubPK
        .getId(), userId);
  }

  private void sendValidationAlert(PublicationDetail pubDetail, String[] users) {
    String userId = pubDetail.getUpdaterId();
    if (!StringUtil.isDefined(userId)) {
      userId = pubDetail.getCreatorId();
    }

    if (userId != null) {
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
      String subject = message.getString("ToValidateForNotif");

      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
              "notificationToValidate");
      for (String lang : getAllLanguages()) {
        SilverpeasTemplate template = getNewTemplate();
        templates.put(lang, template);
        template.setAttribute("path", "");
        template.setAttribute("publication", pubDetail);
        template.setAttribute("publicationName", pubDetail.getName(lang));
        template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
        template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
        template.setAttribute("senderName", userId);
        template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
        ResourceLocator localizedMessage = new ResourceLocator(
            "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
        notifMetaData.addLanguage(lang, localizedMessage.getString("ToValidateForNotif", subject),
            "");
      }
      notifMetaData.setSender(userId);
      notifMetaData.addUserRecipients(users);
      notifMetaData.setLink(getPublicationUrl(pubDetail));
      notifMetaData.setComponentId(pubDetail.getPK().getInstanceId());
      notifyUsers(notifMetaData, pubDetail.getUpdaterId());
    }
  }

  private synchronized List<String> getAllLanguages() {
    ResourceLocator resources = new ResourceLocator(
        "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
        "");
    List<String> allLanguages = new ArrayList<String>();
    try {
      StringTokenizer st = new StringTokenizer(
          resources.getString("languages"), ",");
      while (st.hasMoreTokens()) {
        String langue = st.nextToken();
        allLanguages.add(langue);
      }
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaBmEJB.getAllLanguages()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return allLanguages;
  }

  private void sendModificationAlert(int modificationScope,
      PublicationDetail pubDetail) {
    String userId = pubDetail.getUpdaterId();
    if (!StringUtil.isDefined(userId)) {
      userId = pubDetail.getCreatorId();
    }
    if (StringUtil.isDefined(userId)) {
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", I18NHelper.defaultLanguage);
      String subject = message.getString("kmelia.PublicationModified");
      String fileName = "notificationUpdateContent";
      if (modificationScope == KmeliaHelper.PUBLICATION_HEADER) {
        fileName = "notificationUpdateHeader";
      }

      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, subject, templates,
              fileName);
      for (String lang : getAllLanguages()) {
        SilverpeasTemplate template = getNewTemplate();
        templates.put(lang, template);
        template.setAttribute("path", "");
        template.setAttribute("publication", pubDetail);
        template.setAttribute("publicationName", pubDetail.getName(lang));
        template.setAttribute("publicationDesc", pubDetail.getDescription(lang));
        template.setAttribute("publicationKeywords", pubDetail.getKeywords(lang));
        template.setAttribute("senderName", userId);
        template.setAttribute("silverpeasURL", getPublicationUrl(pubDetail));
        ResourceLocator localizedMessage = new ResourceLocator(
            "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", lang);
        notifMetaData.addLanguage(lang, localizedMessage.getString("kmelia.PublicationModified",
            subject), "");
      }
      notifMetaData.setSender(userId);
      notifMetaData.addUserRecipient(userId);
      notifMetaData.setLink(getPublicationUrl(pubDetail));
      notifMetaData.setComponentId(pubDetail.getPK().getInstanceId());
      notifyUsers(notifMetaData, pubDetail.getUpdaterId());
    }
  }

  public void sendModificationAlert(int modificationScope, PublicationPK pubPK) {
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    sendModificationAlert(modificationScope, pubDetail);
  }

  /*****************************************************************************************************************/
  /** ContentManager utilization to use PDC **/
  /*****************************************************************************************************************/
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
        silverObjectId = createSilverContent(pubDetail, pubDetail
            .getCreatorId());
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
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
      throw new KmeliaRuntimeException("KmeliaBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    } finally {
      freeConnection(con);
    }
  }

  public void deleteSilverContent(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());
    Connection con = getConnection();
    try {
      getKmeliaContentManager().deleteSilverContent(con, pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    } finally {
      freeConnection(con);
    }
  }

  private void updateSilverContentVisibility(PublicationDetail pubDetail) {
    try {
      getKmeliaContentManager().updateSilverContentVisibility(pubDetail);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.updateSilverContentVisibility()",
          SilverpeasRuntimeException.ERROR,
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
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.updateSilverContentVisibility()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  private KmeliaContentManager getKmeliaContentManager() {
    return new KmeliaContentManager();
  }

  private String getPublicationUrl(PublicationDetail pubDetail) {
    return KmeliaHelper.getPublicationUrl(pubDetail);
  }

  private String getNodeUrl(NodeDetail nodeDetail) {
    return KmeliaHelper.getNodeUrl(nodeDetail);
  }

  /**************************************************************************************/
  /* Interface - Fichiers joints */
  /**************************************************************************************/
  public Collection<AttachmentDetail> getAttachments(PublicationPK pubPK) {
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubPK.getId());
    String ctx = "Images";
    AttachmentPK foreignKey = new AttachmentPK(pubPK.getId(), pubPK);
    SilverTrace.info("kmelia", "KmeliaBmEJB.getAttachments()",
        "root.MSG_GEN_PARAM_VALUE", "foreignKey = " + foreignKey.toString());

    Connection con = null;
    try {
      con = getConnection();
      Collection<AttachmentDetail> attachmentList = AttachmentController
          .searchAttachmentByPKAndContext(foreignKey, ctx, con);
      SilverTrace.info("kmelia", "KmeliaBmEJB.getAttachments()",
          "root.MSG_GEN_PARAM_VALUE", "attachmentList.size() = "
              + attachmentList.size());
      return attachmentList;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAttachments()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", e);
    }
  }

  public String getWysiwyg(PublicationPK pubPK) {
    String wysiwygContent = null;
    try {
      wysiwygContent = WysiwygController.loadFileAndAttachment(pubPK
          .getSpaceId(), pubPK.getInstanceId(), pubPK.getId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAttachments()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_WYSIWYG", e);
    }
    return wysiwygContent;
  }

  /*****************************************************************************************************************/
  /** Indexing methods **/
  /*****************************************************************************************************************/
  private void checkIndex(PublicationPK pubPK, InfoDetail infos) {
    infos.setIndexOperation(IndexManager.NONE);
  }

  private void indexExternalElementsOfPublication(PublicationDetail pubDetail) {
    if (KmeliaHelper.isIndexable(pubDetail)) {
      indexExternalElementsOfPublication(pubDetail.getPK());
    }
  }

  private void indexExternalElementsOfPublication(PublicationPK pubPK) {
    // index attachments
    AttachmentController.attachmentIndexer(pubPK);

    try {
      // index versioning
      VersioningUtil versioning = new VersioningUtil();
      versioning.indexDocumentsByForeignKey(new ForeignPK(pubPK));
    } catch (Exception e) {
      SilverTrace.error("kmelia",
          "KmeliaBmEJB.indexExternalElementsOfPublication",
          "Indexing versioning documents failed",
          "pubPK = " + pubPK.toString(), e);
    }

    try {
      // index comments
      CommentController.indexCommentsByForeignKey(pubPK);
    } catch (Exception e) {
      SilverTrace.error("kmelia",
          "KmeliaBmEJB.indexExternalElementsOfPublication",
          "Indexing comments failed", "pubPK = " + pubPK.toString(), e);
    }
  }

  private void unIndexExternalElementsOfPublication(PublicationPK pubPK) {
    // unindex attachments
    AttachmentController.unindexAttachmentsByForeignKey(pubPK);

    try {
      // index versioning
      VersioningUtil versioning = new VersioningUtil();
      versioning.unindexDocumentsByForeignKey(new ForeignPK(pubPK));
    } catch (Exception e) {
      SilverTrace.error("kmelia",
          "KmeliaBmEJB.indexExternalElementsOfPublication",
          "Indexing versioning documents failed",
          "pubPK = " + pubPK.toString(), e);
    }

    try {
      // index comments
      CommentController.unindexCommentsByForeignKey(pubPK);
    } catch (Exception e) {
      SilverTrace.error("kmelia",
          "KmeliaBmEJB.indexExternalElementsOfPublication",
          "Indexing comments failed", "pubPK = " + pubPK.toString(), e);
    }
  }

  private void removeExternalElementsOfPublications(PublicationPK pubPK) {
    // remove attachments
    AttachmentController.deleteAttachmentByCustomerPK(pubPK);

    // remove versioning
    try {
      getVersioningBm().deleteDocumentsByForeignPK(new ForeignPK(pubPK));
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeExternalElementsOfPublications()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_FICHIERS_VERSIONNES", e);
    }

    // remove comments
    try {
      CommentController.deleteCommentsByForeignPK(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeExternalElementsOfPublications()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COMMENTAIRES", e);
    }

    // remove Wysiwyg content
    try {
      WysiwygController.deleteWysiwygAttachments("useless", pubPK
          .getInstanceId(), pubPK.getId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeExternalElementsOfPublications",
          SilverpeasRuntimeException.ERROR, "root.EX_DELETE_ATTACHMENT_FAILED",
          e);
    }

    // remove Thumbnail content
    try {
      ThumbnailDetail thumbToDelete = new ThumbnailDetail(pubPK
                .getInstanceId(), Integer.parseInt(pubPK.getId()),
                ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      ThumbnailController.deleteThumbnail(thumbToDelete);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeExternalElementsOfPublications",
          SilverpeasRuntimeException.ERROR, "root.EX_DELETE_THUMBNAIL_FAILED",
          e);
    }
  }

  private void removeXMLContentOfPublication(PublicationPK pubPK) {
    try {
      PublicationDetail pubDetail = getPublicationDetail(pubPK);
      String infoId = pubDetail.getInfoId();
      if (!isInteger(infoId)) {
        String xmlFormShortName = infoId;

        PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance()
            .getPublicationTemplate(pubDetail.getPK().getInstanceId() + ":"
                + xmlFormShortName);

        RecordSet set = pubTemplate.getRecordSet();
        DataRecord data = set.getRecord(pubDetail.getPK().getId());
        set.delete(data);
      }
    } catch (PublicationTemplateException e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeXMLContentOfPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (FormException e) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.removeXMLContentOfPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private static boolean isInteger(String id) {
    try {
      Integer.parseInt(id);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /*****************************************************************************************************************/
  /** Connection management methods used for the content service **/
  /*****************************************************************************************************************/
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
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

  public void addModelUsed(String[] models, String instanceId, String nodeId) {
    Connection con = getConnection();
    try {
      ModelDAO.deleteModel(con, instanceId, nodeId);
      for (String modelId : models) {
        ModelDAO.addModel(con, instanceId, modelId, nodeId);
      }
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addModelUsed()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }
  }

  public Collection<String> getModelUsed(String instanceId, String nodeId) {
    Connection con = getConnection();
    try {
      // get templates defined for the given node
      Collection<String> result = ModelDAO.getModelUsed(con, instanceId, nodeId);
      if (StringUtil.isDefined(nodeId) && result.size() == 0) {
        // there is no templates defined for the given node, check the parent nodes
        List<NodeDetail> parents =
            (List<NodeDetail>) getNodeBm().getPath(new NodePK(nodeId, instanceId));
        for (int n = 0; n < parents.size() && result.size() == 0; n++) {
          NodeDetail parent = parents.get(n);
          result = ModelDAO.getModelUsed(con, instanceId, parent.getNodePK().getId());
        }
      }
      return result;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getModelUsed()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.IMPOSSIBLE_DE_RECUPERER_LES_MODELES", e);
    } finally {
      // fermer la connexion
      freeConnection(con);
    }
  }

  /**************************************************************************************/
  /**************************************************************************************
   * Kmax Specific methods
   **************************************************************************************/
  /**************************************************************************************/
  /* Kmax - Axis */
  /**************************************************************************************/
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
      NodeDetail header = null;
      for (int h = 0; h < headers.size(); h++) {
        header = headers.get(h);
        // Do not get hidden nodes (Basket and unclassified)
        if (!NodeDetail.STATUS_INVISIBLE.equals(header.getStatus())) // get
        // content
        // of
        // this
        // axis
        {
          axis.addAll(getNodeBm().getSubTree(header.getNodePK(),
              sortField + " " + sortOrder));
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getAxis()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_AXES", e);
    }
    return axis;
  }

  public List<NodeDetail> getAxisHeaders(String componentId) {
    List<NodeDetail> axisHeaders = null;
    try {
      axisHeaders = getNodeBm().getHeadersByLevel(
          new NodePK("useless", componentId), 2);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getAxisHeaders()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_ENTETES_DES_AXES", e);
    }
    return axisHeaders;
  }

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
      CoordinatePoint point = new CoordinatePoint(-1, new Integer(axisPK
          .getId()).intValue(), true);
      getCoordinatesBm().addPointToAllCoordinates(coordinatePK, point);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.addAxis()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_CREER_L_AXE", e);
    }
    return axisPK;
  }

  public void updateAxis(NodeDetail axis, String componentId) {
    axis.getNodePK().setComponentName(componentId);
    SilverTrace.info("kmax", "KmeliaBmEjb.updateAxis()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId
            + " nodePk.getComponentId()=" + axis.getNodePK().getInstanceId());
    try {
      getNodeBm().setDetail(axis);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.updateAxis()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_MODIFIER_L_AXE", e);
    }
  }

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
        coordinateIds = getCoordinatesBm().getCoordinateIdsByNodeId(
            coordinatePK, axisId);
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
      throw new KmaxRuntimeException("KmeliaBmEJB.deleteAxis()",
          SilverpeasRuntimeException.ERROR,
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
    CoordinatePK coordinatePK = new CoordinatePK("useless", "useless",
        componentId);
    try {
      getCoordinatesBm().deleteCoordinatesByPoints(coordinatePK,
          (ArrayList<String>) coordinatePoints);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.removeCoordinatesByPoints()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COORDONNEES_PAR_UN_POINT", e);
    }
  }

  public NodeDetail getNodeHeader(String id, String componentId) {
    NodePK pk = new NodePK(id, componentId);
    return getNodeHeader(pk);
  }

  private NodeDetail getNodeHeader(NodePK pk) {
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = getNodeBm().getHeader(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getNodeHeader()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
    }
    return nodeDetail;
  }

  public NodePK addPosition(String fatherId, NodeDetail position,
      String componentId, String userId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.addPosition()",
        "root.MSG_GEN_PARAM_VALUE", "fatherId = " + fatherId
            + " And position = " + position.toString());
    position.getNodePK().setComponentName(componentId);
    position.setCreationDate(DateUtil.today2SQLDate());
    position.setCreatorId(userId);
    NodeDetail fatherDetail = null;
    NodePK componentPK = null;

    fatherDetail = getNodeHeader(fatherId, componentId);
    SilverTrace
        .info("kmax", "KmeliaBmEjb.addPosition()", "root.MSG_GEN_PARAM_VALUE",
            "fatherDetail = " + fatherDetail.toString());
    try {
      componentPK = getNodeBm().createNode(position, fatherDetail);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.addPosition()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DAJOUTER_UNE_COMPOSANTE_A_L_AXE", e);
    }
    return componentPK;
  }

  public void updatePosition(NodeDetail position, String componentId) {
    position.getNodePK().setComponentName(componentId);
    try {
      getNodeBm().setDetail(position);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.updatePosition()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_MODIFIER_LA_COMPOSANTE_DE_L_AXE", e);
    }
  }

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
      throw new KmaxRuntimeException("KmeliaBmEjb.deletePosition()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_COMPOSANTE_DE_L_AXE", e);
    }
  }

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
      throw new KmaxRuntimeException("KmeliaBmEjb.getPath()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LE_CHEMIN", e);
    }
    return newPath;
  }

  public Collection<Coordinate> getKmaxPathList(PublicationPK pubPK) {
    SilverTrace.info("kmax", "KmeliaBmEJB.getKmaxPathList()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<Coordinate> coordinates = null;
    try {
      coordinates = getPublicationCoordinates(pubPK.getId(), pubPK
          .getInstanceId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getKmaxPathList()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    return coordinates;
  }

  /**************************************************************************************/
  /* Kmax - Search */
  /**************************************************************************************/
  public Collection<UserPublication> search(List<String> combination, String componentId) {
    Collection<PublicationDetail> publications = searchPublications(combination, componentId);
    if (publications == null) {
      return new ArrayList<UserPublication>();
    } else {
      return pubDetails2userPubs(publications);
    }
  }

  public Collection<UserPublication> search(List<String> combination, int nbDays, String componentId) {
    Collection<PublicationDetail> publications = searchPublications(combination, componentId);
    SilverTrace.info("kmax", "KmeliaBmEjb.search()",
        "root.MSG_GEN_PARAM_VALUE", "publications = " + publications);
    return pubDetails2userPubs(filterPublicationsByBeginDate(publications,
        nbDays));
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
      if (combination.size() == 0) {
        // all criterias is "Toutes Catégories"
        // get all publications classified
        NodePK basketPK = new NodePK("1", componentId);
        publications = getPublicationBm().getDetailsNotInFatherPK(basketPK);
      } else {
        if (combination != null && combination.size() > 0) {
          coordinates = getCoordinatesBm().getCoordinatesByFatherPaths(
              (ArrayList<String>) combination, coordinatePK);
        }
        if (coordinates.size() > 0) {
          publications =
              getPublicationBm().getDetailsByFatherIds((ArrayList<String>) coordinates, pk, false);
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.search()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_RESULTATS", e);
    }
    return publications;
  }

  public Collection<UserPublication> getUnbalancedPublications(String componentId) {
    PublicationPK pk = new PublicationPK("useless", componentId);
    Collection<PublicationDetail> publications = null;
    try {
      publications = getPublicationBm().getOrphanPublications(pk);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.getUnbalancedPublications()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_PUBLICATIONS_NON_CLASSEES",
          e);
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

  /**************************************************************************************/
  /* Kmax - Indexation */
  /**************************************************************************************/
  public void indexKmax(String componentId) {
    indexAxis(componentId);
    indexPublications(new PublicationPK("useless", componentId));
  }

  private void indexAxis(String componentId) {
    Collection<NodeDetail> nodes = null;
    NodePK nodePK = new NodePK("useless", componentId);
    try {
      nodes = getNodeBm().getAllNodes(nodePK);
      if (nodes != null) {
        Iterator<NodeDetail> it = nodes.iterator();
        NodeDetail node = null;
        while (it.hasNext()) {
          node = (NodeDetail) it.next();
          if (node.getName().equalsIgnoreCase("corbeille")
              && node.getNodePK().getId().equals("1")) {
            // do not index the bin
          } else {
            getNodeBm().createIndex(node);
          }
        }
      }
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.indexAxis()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DINDEXER_LES_AXES", e);
    }
  }

  /**************************************************************************************/
  /* Kmax - Publications */
  /**************************************************************************************/
  public UserCompletePublication getKmaxCompletePublication(String pubId,
      String currentUserId) {
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
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DOBTENIR_LES_INFORMATIONS_DE_LA_PUBLICATION", e);
    }
    PublicationDetail pub = completePublication.getPublicationDetail();
    OrganizationController orga = getOrganizationController();
    UserDetail userDetail = orga.getUserDetail(pub.getCreatorId());
    UserCompletePublication userCompletePublication = new UserCompletePublication(
        userDetail, completePublication);
    SilverTrace.info("kmax", "KmeliaBmEjb.getKmaxCompletePublication()",
        "root.MSG_GEN_EXIT_METHOD");
    return userCompletePublication;
  }

  public Collection<Coordinate> getPublicationCoordinates(String pubId, String componentId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.getPublicationCoordinates()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return getPublicationBm().getCoordinates(pubId, componentId);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEjb.getPublicationCoordinates()",
          SilverpeasRuntimeException.ERROR, "root.MSG_GEN_PARAM_VALUE", e);
    }
  }

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
      CoordinatePoint point = null;
      String anscestorId = "";
      int nodeLevel;
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
        Iterator<NodeDetail> pathIt = path.iterator();
        while (pathIt.hasNext()) {
          nodeDetail = pathIt.next();
          anscestorId = nodeDetail.getNodePK().getId();
          nodeLevel = nodeDetail.getLevel();
          if (!anscestorId.equals("0")) {
            if (anscestorId.equals(nodeId)) {
              point = new CoordinatePoint(-1, new Integer(anscestorId)
                  .intValue(), true, nodeLevel, i);
            } else {
              point = new CoordinatePoint(-1, new Integer(anscestorId)
                  .intValue(), false, nodeLevel, i);
            }
            allnodes.add(point);
          }
        }
        i++;
      }
      int coordinateId = getCoordinatesBm().addCoordinate(coordinatePK,
          (ArrayList<CoordinatePoint>) allnodes);
      getPublicationBm().addFather(pubPK,
          new NodePK(new Integer(coordinateId).toString(), pubPK));
    } catch (Exception e) {
      throw new KmaxRuntimeException(
          "KmeliaBmEjb.addPublicationToCombination()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DAJOUTER_LA_PUBLICATION_A_CETTE_COMBINAISON", e);
    }
  }

  protected boolean checkCombination(Collection<Coordinate> coordinates,
      List<String> combination) {
    for (Iterator<Coordinate> iter = coordinates.iterator(); iter.hasNext();) {
      Coordinate coordinate = iter.next();
      Collection<CoordinatePoint> points = coordinate.getCoordinatePoints();

      if (points.size() <= 0) {
        continue;
      }

      boolean matchFound = false;

      for (Iterator<CoordinatePoint> pIter = points.iterator(); pIter.hasNext();) {
        CoordinatePoint point = pIter.next();
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
    for (int i = 0; i < combination.size(); i++) {
      String intVal = combination.get(i);
      if (Integer.parseInt(intVal) == point.getNodeId()) {
        return true;
      }
    }
    return false;
  }

  public void deleteCoordinates(CoordinatePK coordinatePK, ArrayList coordinates) {
    try {
      getCoordinatesBm().deleteCoordinates(coordinatePK, coordinates);
    } catch (Exception e) {
      throw new KmaxRuntimeException("KmeliaBmEJB.deleteCoordinates()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LES_COORDINATES", e);
    }
  }

  public void deletePublicationFromCombination(String pubId,
      String combinationId, String componentId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.deletePublicationFromCombination()",
        "root.MSG_GEN_PARAM_VALUE", "combinationId = "
            + combinationId.toString());
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    NodePK fatherPK = new NodePK(combinationId, componentId);
    CoordinatePK coordinatePK = new CoordinatePK(combinationId, pubPK);
    try {
      // remove publication fathers
      getPublicationBm().removeFather(pubPK, fatherPK);
      // remove coordinate
      List<String> coordinateIds = new ArrayList<String>();
      coordinateIds.add(combinationId);
      getCoordinatesBm().deleteCoordinates(coordinatePK, (ArrayList) coordinateIds);
    } catch (Exception e) {
      throw new KmaxRuntimeException(
          "KmeliaBmEjb.deletePublicationFromCombination()",
          SilverpeasRuntimeException.ERROR,
          "kmax.EX_IMPOSSIBLE_DE_SUPPRIMER_LA_COMBINAISON_DE_LA_PUBLICATION", e);
    }
  }

  /**
   * Create a new Publication (only the header - parameters)
   * @param pubDetail a PublicationDetail
   * @return the id of the new publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public String createKmaxPublication(PublicationDetail pubDetail) {
    SilverTrace.info("kmax", "KmeliaBmEJB.createKmaxPublication()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = null;
    Connection con = getConnection(); // connection usefull for content
    // service
    try {
      // create the publication
      pubDetail = changePublicationStatusOnCreation(pubDetail, new NodePK(
          "useless", pubDetail.getPK()));
      pubPK = getPublicationBm().createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());

      // creates todos for publishers
      this.createTodosForPublication(pubDetail, true);

      // register the new publication as a new content to content manager
      createSilverContent(pubDetail, pubDetail.getCreatorId());
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.createKmaxPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_CREER_LA_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("kmax", "KmeliaBmEJB.createKmaxPublication()",
        "root.MSG_GEN_EXIT_METHOD");
    return pubPK.getId();
  }

  /************************************************************************************************/
  /** Alias management */
  /************************************************************************************************/
  public Collection<Alias> getAlias(PublicationPK pubPK) {
    try {
      return getPublicationBm().getAlias(pubPK);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.getAlias()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DAVOIR_LES_ALIAS_DE_PUBLICATION", e);
    }
  }

  public void setAlias(PublicationPK pubPK, List<Alias> alias) {
    List<Alias> oldAliases = (List<Alias>) getAlias(pubPK);

    List<Alias> newAliases = new ArrayList<Alias>();
    List<Alias> remAliases = new ArrayList<Alias>();
    List<Alias> stayAliases = new ArrayList<Alias>();

    // Compute the remove list
    Alias a = null;
    for (int nI = 0; nI < oldAliases.size(); nI++) {
      a = oldAliases.get(nI);
      if (alias.indexOf(a) == -1) {
        remAliases.add(a);
      }
    }

    // Compute the add and stay list
    for (int nI = 0; nI < alias.size(); nI++) {
      a = (Alias) alias.get(nI);
      if (oldAliases.indexOf(a) == -1) {
        newAliases.add(a);
      } else {
        stayAliases.add(a);
      }
    }

    try {
      getPublicationBm().addAlias(pubPK, newAliases);

      getPublicationBm().removeAlias(pubPK, remAliases);
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.setAlias()", SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DENREGISTRER_LES_ALIAS_DE_PUBLICATION", e);
    }

    // Send subscriptions to aliases subscribers
    PublicationDetail pubDetail = getPublicationDetail(pubPK);
    String originalComponentId = new String(pubPK.getInstanceId());
    for (int i = 0; i < newAliases.size(); i++) {
      a = (Alias) newAliases.get(i);
      pubDetail.getPK().setComponentName(a.getInstanceId()); // Change the instanceId to make the
      // right URL
      sendSubscriptionsNotification(new NodePK(a.getId(), a.getInstanceId()), pubDetail, false);
    }
    // restore original primary key
    pubDetail.getPK().setComponentName(originalComponentId);
  }

  public void addAttachmentToPublication(PublicationPK pubPK, String userId,
      String filename, String description, byte[] contents) {
    String context;
    String path;
    Date creationDate = new Date();

    String type = FileRepositoryManager.getFileExtension(filename);

    String versioning = getOrganizationController().getComponentParameterValue(
        pubPK.getComponentName(), "versionControl");
    boolean versioningActive = "yes".equalsIgnoreCase(versioning);

    VersioningUtil versioningUtil = new VersioningUtil();
    if (versioningActive) {
      context = null;
      path = versioningUtil.createPath(null, pubPK.getComponentName(), context);
    } else {
      context = "Images";
      path = AttachmentController.createPath(pubPK.getInstanceId(), context);
    }

    String physicalName = new Long(creationDate.getTime()).toString() + "."
        + type;
    String logicalName = filename;

    java.io.File f = new java.io.File(path + physicalName);
    try {
      java.io.FileOutputStream fos = new java.io.FileOutputStream(f);

      if (contents != null && contents.length > 0) {
        fos.write(contents);
        fos.close();

        String mimeType = javax.activation.MimetypesFileTypeMap
            .getDefaultFileTypeMap().getContentType(f);
        long size = contents.length;

        if (versioningActive) {
          int user_id = Integer.parseInt(userId);
          ForeignPK pubForeignKey = new ForeignPK(pubPK.getId(), pubPK
              .getComponentName());
          DocumentPK docPK = new DocumentPK(-1, pubPK.getSpaceId(), pubPK
              .getComponentName());
          Document document = new Document(docPK, pubForeignKey, logicalName,
              description, -1, user_id, creationDate, null, null, null, null,
              0, 0);

          int majorNumber = 1;
          int minorNumber = 0;

          DocumentVersion newVersion = new DocumentVersion(null, docPK,
              majorNumber, minorNumber, user_id, creationDate, "", 0, 0,
              physicalName, logicalName, mimeType, (int) size, pubPK
                  .getComponentName());

          // create the document with its first version
          DocumentPK documentPK = getVersioningBm().createDocument(document,
              newVersion);
          document.setPk(documentPK);

          if (newVersion.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
            CallBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE,
                newVersion.getAuthorId(), document.getForeignKey()
                    .getInstanceId(), document.getForeignKey().getId());
            PublicationDetail detail = getPublicationDetail(pubPK);
            if (KmeliaHelper.isIndexable(detail)) {
              versioningUtil.createIndex(document, newVersion);
            }
          }
        } else {
          // create AttachmentPK with componentId
          AttachmentPK atPK = new AttachmentPK(null, pubPK.getComponentName());

          // create foreignKey with spaceId, componentId and id
          // use AttachmentPK to build the foreign key of customer
          // object.
          WAPrimaryKey pubForeignKey = new AttachmentPK(pubPK.getId(), pubPK
              .getComponentName());

          // create AttachmentDetail Object
          AttachmentDetail ad = new AttachmentDetail(atPK, physicalName,
              logicalName, description, mimeType, size, context, creationDate,
              pubForeignKey);
          ad.setAuthor(userId);

          AttachmentController.createAttachment(ad, true);
        }
      }

    } catch (FileNotFoundException fnfe) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.addAttachmentToPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DAJOUTER_ATTACHEMENT", fnfe);
    } catch (IOException ioe) {
      throw new KmeliaRuntimeException(
          "KmeliaBmEJB.addAttachmentToPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DAJOUTER_ATTACHEMENT", ioe);
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
   * @throws RemoteException
   */
  public boolean importPublication(String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(
        this, componentId, topicId, spaceId, userId);
    return publicationImport.importPublication(publiParams, formParams,
        language, xmlFormName, discrimatingParameterName, userProfile);
  }

  /**
   * Creates or updates a publication.
   * @param publicationToUpdateId The id of the publication to update.
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
  public boolean importPublication(String publicationId, String componentId, String topicId,
      String spaceId, String userId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName, String userProfile)
      throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(
        this, componentId, topicId, spaceId, userId);
    return publicationImport.importPublication(
        publicationId, publiParams, formParams, language, xmlFormName, userProfile);
  }

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

  public List<XMLField> getPublicationXmlFields(String publicationId, String componentId,
      String spaceId, String userId) {
    PublicationImport publicationImport = new PublicationImport(this,
        componentId, null, spaceId, userId);
    return publicationImport.getPublicationXmlFields(publicationId);
  }

  public List getPublicationXmlFields(String publicationId, String componentId, String spaceId,
      String userId, String language) {
    PublicationImport publicationImport = new PublicationImport(
        this, componentId, null, spaceId, userId);
    return publicationImport.getPublicationXmlFields(publicationId, language);
  }

  public String createTopic(String componentId, String topicId, String spaceId,
      String userId, String name, String description) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this,
        componentId, topicId, spaceId, userId);
    return publicationImport.createTopic(name, description);
  }

  /**
   * Case standard -> LogicalName take from file name
   * @param publicationId
   * @param componentId
   * @param userId
   * @param filePath
   * @param title
   * @param description
   * @param creationDate
   * @throws RemoteException
   */
  public void importAttachment(String publicationId, String componentId, String userId,
      String filePath, String title, String info, Date creationDate)
      throws RemoteException {
    importAttachment(publicationId, componentId, userId,
        filePath, title, info, creationDate, null);
  }

  /**
   * In case of move -> can force the logicalName
   * @param publicationId
   * @param componentId
   * @param userId
   * @param filePath
   * @param title
   * @param description
   * @param creationDate
   * @param logicalName
   * @throws RemoteException
   */
  public void importAttachment(String publicationId, String componentId, String userId,
      String filePath, String title, String info, Date creationDate, String logicalName)
      throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this, componentId);
    publicationImport.importAttachment(
        publicationId, userId, filePath, title, info, creationDate, logicalName);
  }

  public void deleteAttachment(AttachmentDetail attachmentDetail)
      throws RemoteException {
    AttachmentController.deleteAttachment(attachmentDetail);
  }

  public Collection<String> getPublicationsSpecificValues(String componentId,
      String xmlFormName, String fieldName) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this,
        componentId);
    return publicationImport.getPublicationsSpecificValues(componentId,
        xmlFormName, fieldName);
  }

  public void draftInPublication(String componentId, String xmlFormName,
      String fieldName, String fieldValue) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this,
        componentId);
    publicationImport.draftInPublication(xmlFormName, fieldName, fieldValue);
  }

  public void updatePublicationEndDate(String componentId, String spaceId,
      String userId, String xmlFormName, String fieldName, String fieldValue,
      Date endDate) throws RemoteException {
    PublicationImport publicationImport = new PublicationImport(this,
        componentId, null, spaceId, userId);
    publicationImport.updatePublicationEndDate(xmlFormName, fieldName,
        fieldValue, endDate);
  }

  /**
   * find a publication imported only by a xml field (old id for example)
   * @param componentId
   * @param xmlFormName
   * @param fieldName
   * @param fieldValue
   * @return pubId
   */
  public String findPublicationIdBySpecificValue(String componentId, String xmlFormName,
      String fieldName, String fieldValue, String topicId, String spaceId, String userId)
      throws RemoteException {
    PublicationImport publicationImport =
        new PublicationImport(this, componentId, topicId, spaceId, userId);
    return publicationImport.getPublicationId(xmlFormName, fieldName, fieldValue);
  }

  protected SilverpeasTemplate getNewTemplate() {
    ResourceLocator rs =
        new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs
        .getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs
        .getString("customersTemplatePath"));

    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

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
          SilverpeasRuntimeException.ERROR,
          "kmelia.CANT_DO_AUTOMATIC_DRAFTOUT", e);
    }
  }

  public String clonePublication(CompletePublication refPubComplete, PublicationDetail pubDetail,
      String nextStatus) {
    String cloneId = null;
    try {
      // récupération de la publi de référence
      PublicationDetail refPub = refPubComplete.getPublicationDetail();

      String fromId = new String(refPub.getPK().getId());
      String fromComponentId = new String(refPub.getPK().getInstanceId());

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

      // eventually, paste the model content
      if (refPubComplete.getModelDetail() != null && refPubComplete.getInfoDetail() != null) {
        // Paste images of model
        if (refPubComplete.getInfoDetail().getInfoImageList() != null) {
          for (Iterator<InfoImageDetail> i =
              refPubComplete.getInfoDetail().getInfoImageList().iterator(); i.hasNext();) {
            InfoImageDetail attachment = (InfoImageDetail) i.next();
            String from =
                absolutePath + imagesSubDirectory + File.separator + attachment.getPhysicalName();
            String type =
                attachment.getPhysicalName().substring(
                    attachment.getPhysicalName().lastIndexOf(".") + 1,
                    attachment.getPhysicalName().length());
            String newName = new Long(new java.util.Date().getTime()).toString() + "." + type;
            attachment.setPhysicalName(newName);
            String to = absolutePath + imagesSubDirectory + File.separator + newName;
            FileRepositoryManager.copyFile(from, to);
          }
        }

        // Paste model content
        createInfoModelDetail(clonePK, refPubComplete.getModelDetail().getId(), refPubComplete
            .getInfoDetail());
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
          set.clone(fromId, fromComponentId, cloneId, fromComponentId);
        }
      }
      // paste only links, reverseLinks can't be cloned because it'is a new content not referenced
      // by any publication
      if (refPubComplete.getLinkList() != null && refPubComplete.getLinkList().size() > 0) {
        addInfoLinks(clonePK, refPubComplete.getLinkList());
      }

      // paste wysiwyg
      WysiwygController.copy(null, fromComponentId, fromId, null, fromComponentId, cloneId, clone.
          getCreatorId());

      // clone attachments
      AttachmentPK pkFrom = new AttachmentPK(fromId, fromComponentId);
      AttachmentPK pkTo = new AttachmentPK(cloneId, fromComponentId);
      AttachmentController.cloneAttachments(pkFrom, pkTo);

      // paste versioning documents
      // pasteDocuments(pubPKFrom, clonePK.getId());

      // affectation de l'id du clone à la publication de référence
      refPub.setCloneId(cloneId);
      refPub.setCloneStatus(nextStatus);
      refPub.setStatusMustBeChecked(false);
      refPub.setUpdateDateMustBeSet(false);
      updatePublication(refPub);

      // paste vignette
      String vignette = refPub.getImage();
      if (vignette != null) {
        String thumbnailsSubDirectory = publicationSettings.getString("imagesSubDirectory");
        String from = absolutePath + thumbnailsSubDirectory + File.separator + vignette;

        String type = vignette.substring(vignette.lastIndexOf(".") + 1, vignette.length());
        String newVignette = new Long(new java.util.Date().getTime()).toString() + "." + type;

        String to = absolutePath + thumbnailsSubDirectory + File.separator + newVignette;
        FileRepositoryManager.copyFile(from, to);

        ThumbnailDetail thumbDetail = new ThumbnailDetail(
            clone.getPK().getInstanceId(),
            Integer.valueOf(clone.getPK().getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
        thumbDetail.setOriginalFileName(newVignette);
        thumbDetail.setMimeType(refPub.getImageMimeType());

        new ThumbnailServiceImpl().createThumbnail(thumbDetail);
      }
    } catch (IOException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION", e);
    } catch (AttachmentException ae) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION_FILES", ae);
    } catch (FormException fe) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", fe);
    } catch (PublicationTemplateException pe) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION_XMLCONTENT", pe);
    } catch (ThumbnailException e) {
      throw new KmeliaRuntimeException("KmeliaBmEJB.clonePublication",
          SilverpeasException.ERROR, "kmelia.CANT_CLONE_PUBLICATION", e);
    }
    return cloneId;
  }

}
