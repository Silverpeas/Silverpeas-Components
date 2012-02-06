/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/legal/licensing"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmelia.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A publication as defined in a Kmelia component.
 * A publication in Kmelia can be positionned in the PDC, it can have
 * attachments and it can be commented.
 */
public class KmeliaPublication implements SilverpeasContent {

  private static final long serialVersionUID = 4861635754389280165L;
  private static final OrganizationController organisationService = new OrganizationController();
  private PublicationDetail detail;
  private CompletePublication completeDetail;
  private boolean versioned = false;
  private boolean alias = false;
  private final PublicationPK pk;
  private int rank;
  
  /**
   * Gets the Kmelia publication with the specified primary key identifying it uniquely.
   * If no such publication exists with the specified key, then the runtime exception
   * SilverpeasRuntimeException is thrown.
   * @param pk the primary key of the publication to get.
   * @return the Kmelia publication matching the primary key.
   */
  public static KmeliaPublication aKmeliaPublicationWithPk(final PublicationPK pk) {
    KmeliaPublication publication = new KmeliaPublication(pk);
    publication.getDetail();
    return publication;
  }
  
  /**
   * Gets the Kmelia publication from the specified publication detail.
   * @param detail the detail about the publication to get.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication aKmeliaPublicationFromDetail(final PublicationDetail detail) {
    return aKmeliaPublicationFromDetail(detail, 0);
  }
  
  public static KmeliaPublication aKmeliaPublicationFromDetail(final PublicationDetail detail, int rank) {
    KmeliaPublication publication = new KmeliaPublication(detail.getPK(), rank);
    publication.setPublicationDetail(detail);
    return publication;
  }
  
  /**
   * Gets the Kmelia publication from the specified complete publication detail.
   * @param detail the complete detail about the publication to get.
   * @return the Kmelia publication matching the specified complete publication detail.
   */
  public static KmeliaPublication aKmeliaPublicationFromCompleteDetail(final CompletePublication detail) {
    KmeliaPublication publication = new KmeliaPublication(detail.getPublicationDetail().getPK());
    publication.setPublicationCompleteDetail(detail);
    return publication;
  }
  
  /**
   * Sets this publication as versionned.
   * @return itself.
   */
  public KmeliaPublication versioned() {
    this.versioned = true;
    return this;
  }
  
  /**
   * Sets this Kmelia publication as an alias one.
   * @return itself.
   */
  public KmeliaPublication asAlias() {
    this.alias = true;
    return this;
  } 
  
  /**
   * Is this publication an alias of an existing Kmelia publication?
   * @return true if this publication is an alias, false otherwise.
   */
  public boolean isAlias() {
    return this.alias;
  }
  
  /**
   * Is this publication versionned?
   * @return true if this publication is versionned, false otherwise.
   */
  public boolean isVersioned() {
    return this.versioned;
  }

  /**
   * Gets the primary key of this publication.
   * @return the publication primary key.
   */
  public PublicationPK getPk() {
    return pk;
  }
  
  /**
   * Gets the unique identifier of this publication.
   * @return the unique identifier of this publication.
   */
  @Override
  public String getId() {
    return pk.getId();
  }
  
  /**
   * Gets the complete URL at which this publication is located.
   * @return the publication URL.
   */
  public String getURL() {
    String defaultURL =
            getOrganizationController().getDomain(getCreator().getDomainId()).getSilverpeasServerURL();
    ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
    String serverURL = generalSettings.getString("httpServerBase", defaultURL);
    return serverURL + URLManager.getSimpleURL(URLManager.URL_PUBLI, getPk().getId());
  }

  /**
   * Gets the details about this publication.
   * @return the publication details.
   */
  public PublicationDetail getDetail() {
    if (detail == null) {
      try {
        setPublicationDetail(getKmeliaService().getPublicationDetail(pk));
      } catch (RemoteException ex) {
        throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getDetail()",
                SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", ex);
      }
    }
    return detail;
  }

  /**
   * Gets the complete detail about this publication.
   * @return the publication complete details.
   */
  public CompletePublication getCompleteDetail() {
    if (completeDetail == null) {
      try {
        setPublicationCompleteDetail(getKmeliaService().getCompletePublication(pk));
      } catch (RemoteException ex) {
        throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getCompleteDetail()",
                SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LA_PUBLICATION", ex);
      }
    }
    return completeDetail;
  }

  /**
   * Gets the creator of this publication (the initial author).
   * @return the detail about the creator of this publication.
   */
  @Override
  public UserDetail getCreator() {
    String creatorId = getDetail().getCreatorId();
    return getOrganizationController().getUserDetail(creatorId);
  }

  /**
   * Gets the user that has lastly modified this publication. He's the last one that has worked on
   * this publication.
   * If this publication was not modified since its creation, the creator is returned as he's the
   * last user that has worked on this publication.
   * @return the detail about the last modifier of this publication.
   */
  public UserDetail getLastModifier() {
    UserDetail lastModifier = null;
    String modifierId = getDetail().getUpdaterId();
    if (modifierId == null) {
      lastModifier = getCreator();
    } else {
      lastModifier = getOrganizationController().getUserDetail(modifierId);
    }
    return lastModifier;
  }

  /**
   * Gets the comments on this publication.
   * @return an unmodifiable list with the comments on this publication.
   */
  public List<Comment> getComments() {
    return Collections.unmodifiableList(getCommentService().getAllCommentsOnPublication(pk));
  }

  /**
   * Gets the attachments that belong to this publication and that were uploaded by a user.
   * If this publication is versioned, this method isn't supported. In that case, please use the
   * getVersionnedAttachments() method instead.
   * @return an unmodifiable list with the details of each uploaded attachment of this publication.
   */
  public List<AttachmentDetail> getAttachments() {
    if (isVersioned()) {
      throw new UnsupportedOperationException();
    }
    try {
      return Collections.unmodifiableList(new ArrayList<AttachmentDetail>(getKmeliaService().
              getAttachments(pk)));
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getAttachments()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", ex);
    }
  }
  
  /**
   * Gets the versioned attachments that belongs to this publication.
   * If this publication isn't versioned, this method is then not supported. In that case, please
   * use the getAttachments() method instead.
   * @return an unmodifiable list with the versioned documents attached to this publication.
   */
  public List<Document> getVersionedAttachments() {
    if (!isVersioned()) {
      throw new UnsupportedOperationException();
    }
    try {
      return Collections.unmodifiableList(getVersioningService().getDocuments(new ForeignPK(pk)));
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getVersionedAttachments()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", ex);
    }
  }

  /**
   * Gets the positions in the PDC of this publication.
   * @return an unmodifiable list with the PDC positions of this publication.
   */
  public List<ClassifyPosition> getPDCPositions() {
    try {
      int silverObjectId = getKmeliaService().getSilverObjectId(pk);
      return getKmeliaService().getPdcBm().getPositions(silverObjectId, pk.getInstanceId());
    } catch (RemoteException ex) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getPDCPositions()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DOBTENIR_LES_POSTIONSPDC", ex);
    }
  }
  
  public int getNbAccess() {
    try {
      return getStatisticService().getCount(new ForeignPK(pk), 1, "Publication");
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "KmeliaPublication.getNbAccess", "kmelia.CANT_GET_NB_ACCESS",
          "pubId = " + pk.getId(), e);
    }
    return -1;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KmeliaPublication other = (KmeliaPublication) obj;
    if (this.pk != other.pk && (this.pk == null || !this.pk.equals(other.pk))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }
  
  private KmeliaPublication(PublicationPK id) {
    this.pk = id;
  }

  private KmeliaPublication(PublicationPK id, int rank) {
    this.pk = id;
    this.rank = rank;
  }
  
  private void setPublicationDetail(final PublicationDetail detail) {
    this.detail = detail;
  }
  
  private void setPublicationCompleteDetail(final CompletePublication detail) {
    setPublicationDetail(detail.getPublicationDetail());
    this.completeDetail = detail;
  }

  private KmeliaBm getKmeliaService() {
    KmeliaBm KmeliaBm = null;
    try {
      KmeliaBmHome KmeliaBmHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBmHome.class);
      KmeliaBm = KmeliaBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaPublication.getKmeliaService()",
              SilverpeasRuntimeException.ERROR,
              "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_KmeliaBm_HOME", e);
    }
    return KmeliaBm;
  }
  
  private StatisticBm getStatisticService() {
    StatisticBm statisticBm = null;
    try {
      StatisticBmHome statisticHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
      statisticBm = statisticHome.create();
    } catch (Exception e) {
      throw new StatisticRuntimeException("KmeliaPublication.getStatisticService()",
                SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return statisticBm;
  }

  private CommentService getCommentService() {
    return CommentServiceFactory.getFactory().getCommentService();
  }
  
  private VersioningUtil getVersioningService() {
    return new VersioningUtil();
  }

  private OrganizationController getOrganizationController() {
    return organisationService;
  }

  @Override
  public String getComponentInstanceId() {
    return getDetail().getComponentInstanceId();
  }

  @Override
  public String getSilverpeasContentId() {
    return getDetail().getSilverpeasContentId();
  }

  @Override
  public Date getCreationDate() {
    return getDetail().getCreationDate();
  }

  @Override
  public String getTitle() {
    return getDetail().getTitle();
  }

  @Override
  public String getContributionType() {
    return getDetail().getContributionType();
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public int getRank() {
    return rank;
  }

}
