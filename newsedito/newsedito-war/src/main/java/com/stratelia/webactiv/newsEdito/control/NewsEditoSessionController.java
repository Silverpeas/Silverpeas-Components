/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.newsEdito.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.thumbnail.service.ThumbnailService;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.newsEdito.CreateNewsEditoException;
import com.stratelia.webactiv.newsEdito.NewsEditoException;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.favorit.control.FavoritBm;
import com.stratelia.webactiv.util.favorit.control.FavoritBmHome;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.StatisticResultDetail;

/*
 * CVS Informations
 *
 * $Id$
 *
 * $Log: NewsEditoSessionController.java,v $
 * Revision 1.9.4.1  2009/06/03 15:08:58  sfariello
 * Remplacer formulaires BdD par formulaires XML
 *
 * Revision 1.9  2008/07/11 14:05:29  ehugonnet
 * Suppression méthode non utilisée
 *
 * Revision 1.8  2007/06/25 09:10:52  sfariello
 * no message
 *
 * Revision 1.7  2007/06/14 08:40:52  neysseri
 * no message
 *
 * Revision 1.6.6.2  2007/06/14 08:22:03  neysseri
 * no message
 *
 * Revision 1.6.6.1  2007/05/24 15:33:56  sfariello
 * no message
 *
 * Revision 1.6  2005/09/30 14:19:21  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.5  2005/04/14 18:30:48  neysseri
 * no message
 *
 * Revision 1.4  2004/10/05 13:21:54  dlesimple
 * Couper/Coller composant
 *
 * Revision 1.3  2004/09/28 09:25:37  neysseri
 * Utilisation de la bibliothèque iText au lieu de Libraries/lowagie + nettoyage sources
 *
 * Revision 1.2  2003/12/05 15:01:54  svuillet
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:57  nchaix
 * no message
 *
 * Revision 1.1  2002/01/18 09:07:24  lbertin
 * Stabilisation lot 2 : Request routers et sessioncontrollers
 *
 * Revision 1.10  2002/01/08 12:00:32  santonio
 * SilverTrace & SilverException
 *
 */

/**
 * Class declaration
 * @author
 */
public class NewsEditoSessionController extends AbstractComponentSessionController {
  private String archiveId = null;
  private String titleId = null;
  private String publicationId = null;
  private boolean isConsulting = true;

  private final static String root = "0";
  // private final static java.text.SimpleDateFormat formatter = new
  // java.text.SimpleDateFormat("yyyy/MM/dd");
  private final static java.text.SimpleDateFormat tempFormatter = new java.text.SimpleDateFormat(
      "SSSssmm");
  private NodeBm nodeBm;
  private PublicationBm publicationBm;
  private FavoritBm favoritBm;
  private StatisticBm statisticBm;
  private ThumbnailService thumbnailService = null;

  private ResourceLocator settings;

  /**
   * Constructor declaration
   * @see
   */
  public NewsEditoSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.newsEdito.multilang.newsEditoBundle");
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.constructor",
        "NewsEdito.MSG_ENTRY_METHOD");

    try {
      nodeBm = ((NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).create();
      publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      favoritBm = ((FavoritBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.FAVORITBM_EJBHOME, FavoritBmHome.class)).create();
      statisticBm = ((StatisticBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class)).create();
    } catch (Exception e) {
      throw new EJBException("NewsEditoSessionControl() : Exception : " + e);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.webactiv.newsEdito.settings.newsEditoSettings", "");
    }
    return settings;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public UserDetail[] getUserList() {
    return getOrganizationController().getAllUsers();
  }

  /**
   * Method declaration
   * @param userId
   * @return
   * @see
   */
  public UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getArchiveId() {
    return archiveId;
  }

  /**
   * Method declaration
   * @param archiveId
   * @see
   */
  public void setArchiveId(String archiveId) {
    this.archiveId = archiveId;
    setTitleId(null);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTitleId() {
    return titleId;
  }

  /**
   * Method declaration
   * @param titleId
   * @see
   */
  public void setTitleId(String titleId) {
    this.titleId = titleId;
    setPublicationId(null);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getPublicationId() {
    return publicationId;
  }

  /**
   * Method declaration
   * @param publicationId
   * @see
   */
  public void setPublicationId(String publicationId) {
    this.publicationId = publicationId;
  }

  /**
   * Method declaration
   * @param pubId
   * @see
   */
  public void initNavigationForPublication(String pubId)
      throws NewsEditoException {
    try {
      Collection<NodePK> result = publicationBm.getAllFatherPK(new PublicationPK(pubId,
          getSpaceId(), getComponentId()));

      if (result.size() > 2) // 1 -> article normal, 2->article apparaissant
      // aussi dans l'édito
      {
        throw new EJBException(
            "Cette publication a plus de deux noeud pere, mais "
            + result.size());
      }
      Iterator<NodePK> i = result.iterator();
      NodePK titlePK = i.next();

      if (nodeBm.getHeader(titlePK).getFatherPK().getId().equals("0")
          && (i.hasNext())) {
        titlePK = (NodePK) i.next();
      }

      NodePK archivePK = nodeBm.getHeader(titlePK).getFatherPK();

      if (!nodeBm.getHeader(archivePK).getFatherPK().getId().equals("0")) {
        throw new EJBException(
            "Cette publication n'est ratachee a aucun titre ");

      }
      setArchiveId(archivePK.getId());
      setTitleId(titlePK.getId());
      setPublicationId(pubId);
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getArchiveList",
          NewsEditoException.ERROR,
          "NewsEdito.EX_PROBLEM_TO_INITIALIZE_NAV_PUBLI", e);
    }
  }

  /**
   * Method declaration
   * @param nodeId
   * @see
   */
  public void initNavigationForNode(String nodeId) throws NewsEditoException {
    try {
      NodePK nodePK = new NodePK(nodeId, getSpaceId(), getComponentId());
      NodePK fatherPK = nodeBm.getHeader(nodePK).getFatherPK();

      if (fatherPK.getId().equals("0")) {
        // nodeId is an archive
        setArchiveId(nodeId);
        return;
      }
      NodePK superfatherPK = nodeBm.getHeader(fatherPK).getFatherPK();

      if (superfatherPK.getId().equals("0")) {
        // nodeId is a title
        setArchiveId(fatherPK.getId());
        setTitleId(nodeId);
        return;
      }

    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getArchiveList",
          NewsEditoException.ERROR,
          "NewsEdito.EX_PROBLEM_TO_INITIALIZE_NAV_NODE", e);
    }
  }

  /**
   * getArchiveList() This method returns all the newsPaper present in the space (the items are
   * ordered by creation date)
   */
  public Collection<NodeDetail> getArchiveList() throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.getArchiveList",
        "NewsEdito.MSG_ENTRY_METHOD");
    Collection<NodeDetail> result;
    NodePK pk = new NodePK(root, getSpaceId(), getComponentId());

    try {
      result = nodeBm.getFrequentlyAskedChildrenDetails(pk);
      if (!result.iterator().hasNext()) {
        setArchiveId(null);
      }
      return result;
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getArchiveList",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_OBTAIN_ARCHIVES", e);
    }
  }

  /**
   * selectFirstArchive() select first archive in archive list
   */
  public void selectFirstArchive() throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.selectFirstArchive",
        "NewsEdito.MSG_ENTRY_METHOD");
    Collection<NodeDetail> result;
    NodePK pk = new NodePK(root, getSpaceId(), getComponentId());

    try {
      result = nodeBm.getFrequentlyAskedChildrenDetails(pk);
      Iterator<NodeDetail> i = result.iterator();

      String bestDate = null;
      String firstArchiveId = null;

      while (i.hasNext()) {
        NodeDetail node = i.next();

        if ((bestDate == null)
            || ((node.getCreationDate() != null) && (node.getCreationDate()
            .compareTo(bestDate) > 0))) {
          firstArchiveId = node.getNodePK().getId();
          bestDate = node.getCreationDate();
        }
      }

      setArchiveId(firstArchiveId);
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.selectFirstArchive",
          NewsEditoException.WARNING, "NewsEdito.EX_ERROR_SELECT_ARCHIVE", e);
    }
  }

  /**
   * selectFirstOnLineArchive() select first archive in line in archive list
   */
  public void selectFirstOnLineArchive() throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.selectFirstOnLineArchive",
        "NewsEdito.MSG_ENTRY_METHOD");
    Collection<NodeDetail> result;
    NodePK pk = new NodePK(root, getSpaceId(), getComponentId());

    try {
      result = nodeBm.getFrequentlyAskedChildrenDetails(pk);
      Iterator<NodeDetail> i = result.iterator();

      String bestDate = null;
      String firstArchiveId = null;

      while (i.hasNext()) {
        NodeDetail node = i.next();

        if ((node.getStatus() != null) && (node.getStatus().equals("onLine"))) {
          if ((bestDate == null)
              || ((node.getCreationDate() != null) && (node.getCreationDate()
              .compareTo(bestDate) > 0))) {
            firstArchiveId = node.getNodePK().getId();
            bestDate = node.getCreationDate();
          }
        }
      }

      setArchiveId(firstArchiveId);

    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.selectFirstOnLineArchive",
          NewsEditoException.WARNING, "NewsEdito.EX_ERROR_SELECT_ARCHIVE", e);
    }
  }

  /**
   * Method declaration
   * @param fatherId
   * @return
   * @throws NewsEditoException
   * @see
   */
  public Collection<StatisticResultDetail> getArchiveUsage(String fatherId)
      throws NewsEditoException {
    try {
      if (fatherId == null)
        fatherId = root;
      if (fatherId.length() == 0)
        fatherId = root;

      Collection<StatisticResultDetail> statList = new ArrayList<StatisticResultDetail>();

      NodePK pk = new NodePK(fatherId, getSpaceId(), getComponentId());
      NodeDetail nd = nodeBm.getDetail(pk);

      // récupération de la liste de toutes les rubriques (les nodes)
      Collection<NodeDetail> archiveList = nd.getChildrenDetails();
      Iterator<NodeDetail> i = archiveList.iterator();
      while (i.hasNext()) {
        NodeDetail nodeDetail = i.next();

        // récupérer par rubrique, la liste de tous les articles (publications)
        Collection<PublicationDetail> publications = publicationBm.getDetailsByFatherPK(nodeDetail
            .getNodePK());

        // pour chaque liste d'articles (rubrique), compter le nombre de
        // lectures
        int accessByNode = 0;
        Iterator<PublicationDetail> it = publications.iterator();
        while (it.hasNext()) {
          // ajouter le nombre d'accès à chaque publication
          PublicationDetail pub = it.next();
          ForeignPK foreignPK = new ForeignPK(pub.getPK().getId(), pub
              .getInstanceId());
          int accessByPub = statisticBm.getCount(foreignPK, 1, "Publication");
          pub.setNbAccess(accessByPub);
          accessByNode = accessByNode + accessByPub;
        }

        // créer le StatisticResultDetail
        StatisticResultDetail statDetail = new StatisticResultDetail(pk,
            Integer.toString(accessByNode));
        if (statDetail != null)
          statDetail.setDetail(nodeDetail);

        statList.add(statDetail);
      }
      return statList;
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getArchiveUsage",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE", e);
    }
  }

  /**
   * getArchiveContent for one archive, give all its titles
   */
  public NodeDetail getArchiveContent() throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.getArchiveContent",
        "NewsEdito.MSG_ENTRY_METHOD");
    if (getArchiveId() == null) {
      return null;
    }
    NodePK pk = new NodePK(getArchiveId(), getSpaceId(), getComponentId());

    try {
      // return nodeBm.getTwoLevelDetails(pk);
      return nodeBm.getDetail(pk);
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getArchiveContent",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_RETURN_TITLES",
          e);
    }
  }

  /**
   * getTitleDetail
   */
  public NodeDetail getTitleDetail() throws NewsEditoException {
    try {
      if (getTitleId() == null) {
        return null;
      }
      return getNodeDetail(getTitleId());
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getTitleDetail",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_RETURN_TITLE", e);
    }
  }

  /**
   * getNodeDetail
   */
  public NodeDetail getNodeDetail(String nodeId) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.getNodeDetail",
        "NewsEdito.MSG_ENTRY_METHOD");
    NodePK pk = new NodePK(nodeId, getSpaceId(), getComponentId());
    NodeDetail result;

    try {
      result = nodeBm.getHeader(pk);
      return result;
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getNodeDetail",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_GET_NODE", e);
    }
  }

  /**
   * Manipulation des publications
   */
  public Collection<PublicationDetail> getTitlePublicationDetails() throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.getTitlePublicationDetails",
        "NewsEdito.MSG_ENTRY_METHOD");
    if (getTitleId() == null) {
      return null;
    }
    try {
      Collection<PublicationDetail> result = publicationBm.getDetailsByFatherPK(new NodePK(
          getTitleId(), getSpaceId(), getComponentId()));

      return result;
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.getTitlePublicationDetails",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_RETURN_PUBLI_TITLE", e);
    }
  }

  /**
   * for editorials
   */
  public Collection<PublicationDetail> getArchivePublicationDetails() throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.getArchivePublicationDetails",
        "NewsEdito.MSG_ENTRY_METHOD");
    if (getArchiveId() == null) {
      return null;
    }
    try {
      Collection<PublicationDetail> result = publicationBm.getDetailsByFatherPK(new NodePK(
          getArchiveId(), getSpaceId(), getComponentId()));

      return result;
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.getArchivePublicationDetails",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE", e);
    }
  }

  /**
   * Method declaration
   * @param name
   * @param description
   * @return
   * @throws NewsEditoException
   * @see
   */
  public String createPublication(String name, String description)
      throws NewsEditoException, CreateNewsEditoException {
    if (getTitleId() == null) {
      throw new CreateNewsEditoException(
          "NewsEditoSessionControl.createPublication",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_TITLE");

    }
    if ((name == null) || (name.length() == 0)) {
      throw new CreateNewsEditoException(
          "NewsEditoSessionControl.createPublication",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_NAME");

    }
    try {
      PublicationDetail detail = new PublicationDetail(new PublicationPK(
          "unknown", getSpaceId(), getComponentId()), name, description,
          new java.util.Date(), null, // formatter.parse("1999/12/25"),
          null, // formatter.parse("3000/12/25"),
          getUserId(), 1, "", "", "");
      PublicationPK pubPK = publicationBm.createPublication(detail);

      publicationBm.addFather(pubPK, new NodePK(getTitleId(), getSpaceId(),
          getComponentId()));
      setPublicationId(pubPK.getId());
      return pubPK.getId();
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.createPublication",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_CREATE_PUBLI", e);
    }
  }

  /**
   * Method declaration
   * @param name
   * @param description
   * @throws NewsEditoException
   * @see
   */
  public void updatePublication(String name, String description)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.updatePublication",
        "NewsEdito.MSG_ENTRY_METHOD");
    if ((name == null) || (name.length() == 0)) {
      throw new CreateNewsEditoException(
          "NewsEditoSessionControl.updatePublication",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_NAME");
    }
    try {
      PublicationDetail detail = publicationBm.getDetail(new PublicationPK(
          getPublicationId(), getSpaceId(), getComponentId()));

      detail.setName(name);
      detail.setDescription(description);
      publicationBm.setDetail(detail);

    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.updatePublication",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_UPDATE_PUBLI", e);
    }
  }

  public void updatePublication(PublicationDetail pubDetail)
      throws RemoteException {
    publicationBm.setDetail(pubDetail);
  }

  /**
   * Method declaration
   * @param name
   * @param description
   * @param imageName
   * @param mimeType
   * @throws NewsEditoException
   * @see
   */
  public void updatePublication(String name, String description,
      String imageName, String mimeType) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.updatePublication",
        "NewsEdito.MSG_ENTRY_METHOD");
    if ((name == null) || (name.length() == 0)) {
      throw new CreateNewsEditoException(
          "NewsEditoSessionControl.createPublication",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_NAME");
    }
    try {
      PublicationDetail detail = publicationBm.getDetail(new PublicationPK(
          getPublicationId(), getSpaceId(), getComponentId()));

      detail.setName(name);
      detail.setDescription(description);
      	publicationBm.setDetail(detail);
      	// update de l'image
      	ThumbnailDetail thumbDetail = new ThumbnailDetail(
      		  getComponentId(),
      		  Integer.valueOf(getPublicationId()),
  			  ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
        	thumbDetail.setOriginalFileName(imageName);
        	thumbDetail.setMimeType(mimeType);

        	if(getThumbnailService().getCompleteThumbnail(thumbDetail) != null){
        		// case update
        		getThumbnailService().updateThumbnail(thumbDetail);
        	}else{
        		// case create
        		getThumbnailService().createThumbnail(thumbDetail);
        	}
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.updatePublication",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_UPDATE_PUBLI", e);
    }
  }

  public ThumbnailService getThumbnailService() {
	    if (thumbnailService == null)
	    	thumbnailService = new ThumbnailServiceImpl();
	    return thumbnailService;
	  }
  
  /**
   * Method declaration
   * @param pubId
   * @throws NewsEditoException
   * @see
   */
  public void removePublication(String pubId) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.removePublication",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      publicationBm.removePublication(new PublicationPK(pubId, getSpaceId(),
          getComponentId()));
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.removePublication",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_DELETE_PUBLI", e);
    }
  }

  /**
   * Method declaration
   * @param publicationId
   * @return
   * @throws NewsEditoException
   * @see
   */
  public PublicationDetail getPublicationDetail(String publicationId)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.getPublicationDetail",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      return publicationBm.getDetail(new PublicationPK(publicationId,
          getSpaceId(), getComponentId()));
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.getPublicationDetail",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_GET_PUBLI", e);
    }
  }

  /**
   * Return title to which the publication is bound Ignore the archive to which it is bound if the
   * publication is in the editorial
   */
  public NodeDetail getPublicationTitleDetail(String publicationId)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.getPublicationTitleDetail",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      Collection<NodePK> result = publicationBm.getAllFatherPK(new PublicationPK(
          publicationId, getSpaceId(), getComponentId()));

      if (result.size() > 2) // 1 -> article normal, 2->article apparaissant
      // aussi dans l'édito
      {
        throw new CreateNewsEditoException(
            "NewsEditoSessionControl.getPublicationTitleDetail",
            CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_NUMBER_TITLE");
        // throw new
        // EJBException("Cette publication a plus de deux noeud pere, mais " +
        // result.size() );
      }
      Iterator<NodePK> i = result.iterator();
      NodePK titlePK = i.next();

      if (nodeBm.getHeader(titlePK).getFatherPK().getId().equals("0")
          && (i.hasNext())) {
        titlePK = i.next();
      }

      NodeDetail finalResult = nodeBm.getHeader(titlePK);

      return finalResult;

    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.getPublicationTitleDetail",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_GET_PUBLI", e);
    }
  }

  /**
   * Manipulation des modeles
   */
  public CompletePublication getCompletePublication() throws NewsEditoException {
    try {
      return getCompletePublication(getPublicationId());
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.getCompletePublication",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_RETURN_EDITORIAL", e);
    }
  }

  /**
   * Method declaration
   * @param id
   * @return
   * @throws NewsEditoException
   * @see
   */
  public CompletePublication getCompletePublication(String id)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.getCompletePublication",
        "NewsEdito.MSG_ENTRY_METHOD");
    PublicationPK pubPK = new PublicationPK(id, getSpaceId(), getComponentId());

    try {
      CompletePublication complete = publicationBm
          .getCompletePublication(pubPK);

      if ((getUserId() != null) && (getTitleId() != null) && (isConsulting)) {
        statisticBm.addStat(getUserId(), new ForeignPK(id, getComponentId()),
            1, "Publication");
      }
      return complete;
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.getCompletePublication",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_RETURN_EDITORIAL", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws NewsEditoException
   * @see
   */
  public Collection<ModelDetail> getAllModels() throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.getAllModels",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      return publicationBm.getAllModelsDetail(/* pubPK */
      );
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getAllModels",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_RETURN_MODELS",
          e);
    }
  }

  /**
   * Method declaration
   * @param modelId
   * @throws NewsEditoException
   * @see
   */
  public void setPublicationModel(String modelId) throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.setPublicationModel",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      PublicationPK pubPK = new PublicationPK(getPublicationId(), getSpaceId(),
          getComponentId());
      ModelPK modelPK = new ModelPK(modelId, getSpaceId(), getComponentId());

      publicationBm.createInfoModelDetail(pubPK, modelPK, null);

    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.setPublicationModel",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_SET_PUBLI", e);
    }
  }

  public PublicationTemplateImpl setPublicationXmlForm(String xmlFormName)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.setPublicationXmlForm",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      PublicationDetail pubDetail = publicationBm.getDetail(new PublicationPK(
          getPublicationId(), getSpaceId(), getComponentId()));

      String xmlFormShortName = null;
      PublicationTemplateManager publicationTemplateManager =
              PublicationTemplateManager.getInstance();
      if (!StringUtil.isDefined(xmlFormName)) {
        xmlFormShortName = pubDetail.getInfoId();
        xmlFormName = null;
      } else {
        xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
            xmlFormName.indexOf("."));

        // register xmlForm to publication
        publicationTemplateManager.addDynamicPublicationTemplate(
            getComponentId() + ":" + xmlFormShortName, xmlFormName);
      }

      PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) publicationTemplateManager
          .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName,
          xmlFormName);
      return pubTemplate;
    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.setPublicationXmlModel",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_SET_PUBLI", e);
    }
  }

  public void updateXMLForm(List<FileItem> items, String name) throws NewsEditoException {
    try {
      PublicationDetail pubDetail = publicationBm.getDetail(new PublicationPK(
          getPublicationId(), getSpaceId(), getComponentId()));

      String xmlFormShortName = null;

      // Is it the creation of the content or an update ?
      String infoId = pubDetail.getInfoId();
      if (infoId == null || "0".equals(infoId)) {
        String xmlFormName = name;

        // The publication have no content
        // We have to register xmlForm to publication
        xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
            xmlFormName.indexOf("."));
        pubDetail.setInfoId(xmlFormShortName);
      } else {
        xmlFormShortName = pubDetail.getInfoId();
      }

      String pubId = pubDetail.getPK().getId();

      PublicationTemplate pub = PublicationTemplateManager.getInstance()
          .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);

      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();

      DataRecord data = set.getRecord(pubId, getLanguage());
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(pubId);
        data.setLanguage(getLanguage());
      }

      PagesContext context = new PagesContext("myForm", "3", getLanguage(),
          false, getComponentId(), getUserId());
      context.setObjectId(pubId);
      context.setContentLanguage(getLanguage());

      form.update(items, data, context);
      set.save(data);

      // updatePublication(pubDetail);
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.updateXMLForm",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_SET_PUBLI", e);
    }
  }

  /**
   * Method declaration
   * @param infos
   * @throws NewsEditoException
   * @see
   */
  public void setInfoDetail(InfoDetail infos) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.setInfoDetail",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      PublicationPK pubPK = new PublicationPK(getPublicationId(), getSpaceId(),
          getComponentId());

      publicationBm.updateInfoDetail(pubPK, infos);
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.setInfoDetail",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_SET_INFO", e);
    }
  }

  /**
   * remove a title or an archive
   */
  public void removeTitle(String titleId) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.removeTitle",
        "NewsEdito.MSG_ENTRY_METHOD");
    if (titleId.equals(getArchiveId())) {
      setArchiveId(null);
    }
    if (titleId.equals(getTitleId())) {
      setTitleId(null);
    }
    try {
      try {
        NodePK node = new NodePK(titleId, getSpaceId(), getComponentId());
        NodeDetail detail = nodeBm.getHeader(node);

        // try to remove this favorites, from all users, even if it does not
        // exist
        favoritBm.removeFavoritByNodePath(node, detail.getPath());

      } catch (Exception ex) {
        SilverTrace.info("NewsEdito", "NewsEditoSessionControl.removeTitle",
            "NewsEdito.MSG_REMOVE_TITLE");
      }
      NodePK titlePK = new NodePK(titleId, getSpaceId(), getComponentId());

      // on enleve les publications attachées
      if (nodeBm.getHeader(titlePK).getFatherPK().getId().equals("0")) // cas de
      // l'archive
      {
        Collection<NodeDetail> subList = nodeBm.getDetail(titlePK).getChildrenDetails();
        Iterator<NodeDetail> i = subList.iterator();

        while (i.hasNext()) {
          NodeDetail subTitleDetail = i.next();
          Collection<PublicationDetail> pubList = publicationBm
              .getDetailsByFatherPK(subTitleDetail.getNodePK());
          Iterator<PublicationDetail> j = pubList.iterator();

          while (j.hasNext()) {

            publicationBm.removePublication((j.next())
                .getPK());
          }
        }

      } else // cas du titre
      {
        Collection<PublicationDetail> pubList = publicationBm.getDetailsByFatherPK(titlePK);
        Iterator<PublicationDetail> j = pubList.iterator();

        while (j.hasNext()) {

          publicationBm.removePublication((j.next())
              .getPK());
        }
      }

      nodeBm.removeNode(titlePK);

    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.removeTitle",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_DELETE_TITLE", e);
    }
  }

  /**
   * add a publication to the selected archive editorial
   */
  public void addPublicationToEditorial(String pubId) throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.addPublicationToEditorial",
        "NewsEdito.MSG_ENTRY_METHOD");
    if (getArchiveId() == null) {
      throw new CreateNewsEditoException(
          "NewsEditoSessionControl.addPublicationToEditorial",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_ARCHIVE");
    }
    try {

      PublicationPK pubPK = new PublicationPK(pubId, getSpaceId(),
          getComponentId());

      publicationBm.addFather(pubPK, new NodePK(getArchiveId(), getSpaceId(),
          getComponentId()));

    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.addPublicationToEditorial",
          NewsEditoException.WARNING, "NewsEdito.EX_NO_PUBLI_ADDED", e);
    }
  }

  /**
   * remove the publication from the selected archive editorial
   */
  public void removePublicationFromEditorial(String pubId)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito",
        "NewsEditoSessionControl.removePublicationFromEditorial",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {

      PublicationPK pubPK = new PublicationPK(pubId, getSpaceId(),
          getComponentId());

      publicationBm.removeFather(pubPK, new NodePK(getArchiveId(),
          getSpaceId(), getComponentId()));

    } catch (Exception e) {
      throw new NewsEditoException(
          "NewsEditoSessionControl.removePublicationFromEditorial",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_DELETE_PUBLI", e);
    }
  }

  /**
   * Method declaration
   * @param fatherId
   * @param name
   * @param description
   * @return
   * @throws NewsEditoException
   * @see
   */
  public String addTitle(String fatherId, String name, String description)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.addTitle",
        "NewsEdito.MSG_ENTRY_METHOD");

    if ((name == null) || (name.length() == 0)) {
      throw new CreateNewsEditoException("NewsEditoSessionControl.addTitle",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_NAME");

    }
    try {
      if (fatherId == null) {
        fatherId = root;
      } else if (fatherId.length() == 0) {
        fatherId = root;

      }
      String status = null;

      if (fatherId == root) {
        status = "hidden";
      } else {
        status = "notRelevant";
      }

      String modelId = null;

      if (fatherId == root) {
        modelId = "1";
      } else {
        modelId = "notRelevant";
      }

      NodeDetail father = nodeBm.getHeader(new NodePK(fatherId, getSpaceId(),
          getComponentId()));
      NodeDetail node = new NodeDetail(new NodePK("unknown", getSpaceId(),
          getComponentId()), name, description, DateUtil
          .date2SQLDate(new java.util.Date()), getUserId(), "", 0, null,
          modelId, status, null);
      NodePK newNodePK = nodeBm.createNode(node, father);

      return newNodePK.getId();
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.addTitle",
          NewsEditoException.WARNING, "NewsEdito.EX_NO_TITLE_ADDED", e);
    }
  }

  /**
   * updateTitle() Update a Node
   */
  public void updateTitle(String nodeId, String name, String description)
      throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.updateTitle",
        "NewsEdito.MSG_ENTRY_METHOD");
    if ((name == null) || (name.length() == 0)) {
      throw new CreateNewsEditoException("NewsEditoSessionControl.updateTitle",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_NAME");

    }
    try {
      NodeDetail node = nodeBm.getHeader(new NodePK(nodeId, getSpaceId(),
          getComponentId()));
      NodeDetail newNode = new NodeDetail(node.getNodePK(), name, description,
          node.getCreationDate(), node.getCreatorId(), node.getPath(), node
          .getLevel(), node.getFatherPK(), null);

      nodeBm.setDetail(newNode);
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.updateTitle",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_UPDATE_TITLE", e);
    }
  }

  /**
   * updateTitle() Update a Node
   */

  public void updateTitle(String nodeId, String name, String description,
      String model, String status) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.updateTitle",
        "NewsEdito.MSG_ENTRY_METHOD");
    if ((name == null) || (name.length() == 0)) {
      throw new CreateNewsEditoException("NewsEditoSessionControl.updateTitle",
          CreateNewsEditoException.ERROR, "NewsEdito.MSG_BAD_FORMAT_NAME");

    }
    try {
      NodeDetail node = nodeBm.getHeader(new NodePK(nodeId, getSpaceId(),
          getComponentId()));
      NodeDetail newNode = new NodeDetail(node.getNodePK(), name, description,
          node.getCreationDate(), node.getCreatorId(), node.getPath(), node
          .getLevel(), node.getFatherPK(), model, status, null);

      nodeBm.setDetail(newNode);
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.updateTitle",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_UPDATE_TITLE", e);
    }
  }

  /**
   * getFavoritList() Retuns a collection of Path, corresponding to user's favorits.
   */
  public Collection getFavoritList() throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.getFavoritList",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      Collection<NodePK> list = favoritBm.getFavoritNodePKsByComponent(getUserId(),
          getComponentId());
      Collection detailedList = new ArrayList();
      Iterator<NodePK> i = list.iterator();

      while (i.hasNext()) {
        NodePK pk = i.next();
        Collection path = nodeBm.getPath(pk);

        detailedList.add(path);
      }
      return detailedList;
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.getFavoritList",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_RETURN_FAVORITS", e);
    }
  }

  /**
   * removeFavorit() remove the favorit link between the user and the node
   */
  public void removeFavorit(String nodeId) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.removeFavorit",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      favoritBm.removeFavoritNode(getUserId(), new NodePK(nodeId, getSpaceId(),
          getComponentId()));
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.removeFavorit",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_DELETE_FAVORIT",
          e);
    }
  }

  /**
   * addFavorit() add a favorit link between the user and the node
   */
  public void addFavorit(String nodeId) throws NewsEditoException {
    try {
      favoritBm.addFavoritNode(getUserId(), new NodePK(nodeId, getSpaceId(),
          getComponentId()));
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.addFavorit",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_CREATE_FAVORIT",
          e);
    }
  }

  /**
   * Method declaration
   * @param pubList
   * @return
   * @throws NewsEditoException
   * @see
   */
  public String generatePdf(String[] pubList) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "NewsEditoSessionControl.generatePdf",
        "NewsEdito.MSG_ENTRY_METHOD");
    String name = "text" + tempFormatter.format(new java.util.Date()) + ".pdf";

    try {
      if (pubList == null) {
        NodeDetail root = getArchiveContent();

        PdfGenerator.generateArchive(name, root, publicationBm, getLanguage());
      } else {
        ArrayList<CompletePublication> completePubList = new ArrayList<CompletePublication>();

        for (int i = 0; i < pubList.length; i++) {
          try {
            PublicationPK pk = new PublicationPK(pubList[i], getSpaceId(),
                getComponentId());
            CompletePublication complete = publicationBm
                .getCompletePublication(pk);

            completePubList.add(complete);
          } catch (Exception e) {
            SilverTrace.info("NewsEdito",
                "NewsEditoSessionControl.generatePdf",
                "NewsEdito.MSG_RETURN_COMPLETE_LIST_OF_PUBLI");
          }
        }
        PdfGenerator.generatePubList(name, completePubList, getLanguage());
      }
    } catch (Exception e) {
      throw new NewsEditoException("NewsEditoSessionControl.generatePdf",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_GENERATE_PDF", e);
    }
    return name;
  }

  /**
   * Cette méthode initialise la variable IsConsulting. Cette variable indique si l'utilisateur est
   * en train de consulter ou de modifier une publication.<BR>
   * En effet, les statistiques ne doivent s'incrémenter seulement si l'utilisateur consulte une
   * publication. Attention, l'utilisateur
   */
  public void setIsConsulting(boolean val) {
    isConsulting = val;
  }

  public void close() {
    try {
      if (favoritBm != null)
        favoritBm.remove();
    } catch (RemoteException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    } catch (RemoveException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    }
    try {
      if (nodeBm != null)
        nodeBm.remove();
    } catch (RemoteException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    } catch (RemoveException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    }
    try {
      if (publicationBm != null)
        publicationBm.remove();
    } catch (RemoteException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    } catch (RemoveException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    }
    try {
      if (statisticBm != null)
        statisticBm.remove();
    } catch (RemoteException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    } catch (RemoveException e) {
      SilverTrace.error("newsEditoSession", "NewsEditoSessionController.close",
          "", e);
    }
  }

  public void index() throws RemoteException, NewsEditoException {
    // recuperation des archives
    Collection<NodeDetail> archives = getArchiveList();
    NodeDetail archive = null;
    for (Iterator<NodeDetail> i = archives.iterator(); i.hasNext();) {
      archive = i.next();
      nodeBm.createIndex(archive);

      setArchiveId(archive.getNodePK().getId());
      // recuperation du detail d'une archive
      archive = getArchiveContent();
      if (archive.getChildrenDetails() != null) {
        // parcours des titres
        NodeDetail title = null;
        for (Iterator<NodeDetail> j = archive.getChildrenDetails().iterator(); j.hasNext();) {
          title = j.next();
          nodeBm.createIndex(title);

          setTitleId(title.getNodePK().getId());

          Collection<PublicationDetail> publications = getTitlePublicationDetails();
          PublicationDetail detail = null;
          for (Iterator<PublicationDetail> l = publications.iterator(); l.hasNext();) {
            detail = l.next();
            publicationBm.createIndex(detail.getPK());
          }
        }
      }
    }
  }
}