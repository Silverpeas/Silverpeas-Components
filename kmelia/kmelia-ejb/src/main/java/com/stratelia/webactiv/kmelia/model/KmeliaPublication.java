/*
 *  Copyright (C) 2000 - 2014 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception. You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmelia.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceProvider;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.publication.model.CompletePublication;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import com.stratelia.webactiv.statistic.control.StatisticService;
import com.stratelia.webactiv.statistic.model.StatisticRuntimeException;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A publication as defined in a Kmelia component. A publication in Kmelia can be positionned in the
 * PDC, it can have attachments and it can be commented.
 */
public class KmeliaPublication implements SilverpeasContent {

  private static final long serialVersionUID = 4861635754389280165L;
  private PublicationDetail detail;
  private CompletePublication completeDetail;
  private boolean alias = false;
  private final PublicationPK pk;
  private int rank;
  public boolean read = false;

  /**
   * Gets the Kmelia publication with the specified primary key identifying it uniquely. If no such
   * publication exists with the specified key, then the runtime exception
   * SilverpeasRuntimeException is thrown.
   *
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
   *
   * @param detail the detail about the publication to get.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication aKmeliaPublicationFromDetail(final PublicationDetail detail) {
    return aKmeliaPublicationFromDetail(detail, 0);
  }

  public static KmeliaPublication aKmeliaPublicationFromDetail(final PublicationDetail detail,
      int rank) {
    KmeliaPublication publication = new KmeliaPublication(detail.getPK(), rank);
    publication.setPublicationDetail(detail);
    return publication;
  }

  /**
   * Gets the Kmelia publication from the specified complete publication detail.
   *
   * @param detail the complete detail about the publication to get.
   * @return the Kmelia publication matching the specified complete publication detail.
   */
  public static KmeliaPublication aKmeliaPublicationFromCompleteDetail(
      final CompletePublication detail) {
    KmeliaPublication publication = new KmeliaPublication(detail.getPublicationDetail().getPK());
    publication.setPublicationCompleteDetail(detail);
    return publication;
  }

  /**
   * Sets this Kmelia publication as an alias one.
   *
   * @return itself.
   */
  public KmeliaPublication asAlias() {
    this.alias = true;
    return this;
  }

  /**
   * Is this publication an alias of an existing Kmelia publication?
   *
   * @return true if this publication is an alias, false otherwise.
   */
  public boolean isAlias() {
    return this.alias;
  }

  /**
   * Gets the primary key of this publication.
   *
   * @return the publication primary key.
   */
  public PublicationPK getPk() {
    return pk;
  }

  /**
   * Gets the unique identifier of this publication.
   *
   * @return the unique identifier of this publication.
   */
  @Override
  public String getId() {
    return pk.getId();
  }

  /**
   * Gets the complete URL at which this publication is located.
   *
   * @return the publication URL.
   */
  public String getURL() {
    String defaultURL = getOrganizationController().getDomain(getCreator().getDomainId()).
        getSilverpeasServerURL();
    String serverURL = GeneralPropertiesManager.getString("httpServerBase", defaultURL);
    return serverURL + URLManager.getSimpleURL(URLManager.URL_PUBLI, getPk().getId());
  }

  /**
   * Gets the details about this publication.
   *
   * @return the publication details.
   */
  public PublicationDetail getDetail() {
    if (detail == null) {
      setPublicationDetail(getKmeliaService().getPublicationDetail(pk));
    }
    return detail;
  }

  /**
   * Gets the complete detail about this publication.
   *
   * @return the publication complete details.
   */
  public CompletePublication getCompleteDetail() {
    if (completeDetail == null) {
      setPublicationCompleteDetail(getKmeliaService().getCompletePublication(pk));
    }
    return completeDetail;
  }

  /**
   * Gets the creator of this publication (the initial author).
   *
   * @return the detail about the creator of this publication.
   */
  @Override
  public UserDetail getCreator() {
    String creatorId = getDetail().getCreatorId();
    return getOrganizationController().getUserDetail(creatorId);
  }

  /**
   * Gets the user that has lastly modified this publication. He's the last one that has worked on
   * this publication. If this publication was not modified since its creation, the creator is
   * returned as he's the last user that has worked on this publication.
   *
   * @return the detail about the last modifier of this publication.
   */
  public UserDetail getLastModifier() {
    UserDetail lastModifier;
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
   *
   * @return an unmodifiable list with the comments on this publication.
   */
  public List<Comment> getComments() {
    return Collections.unmodifiableList(getCommentService().getAllCommentsOnPublication(
        PublicationDetail.getResourceType(), pk));
  }

  /**
   * Gets the positions in the PDC of this publication.
   *
   * @return an unmodifiable list with the PDC positions of this publication.
   */
  public List<ClassifyPosition> getPDCPositions() {
    int silverObjectId = getKmeliaService().getSilverObjectId(pk);
    try {
      return PdcManager.get().getPositions(silverObjectId, pk.getInstanceId());
    } catch (PdcException e) {
      throw new KmeliaRuntimeException("kmelia", e.getErrorLevel(), e.getMessage(),
          e.getExtraInfos(), e);
    }

  }

  public int getNbAccess() {
    try {
      return getStatisticService().getCount(new ForeignPK(pk), 1, "Publication");
    } catch (Exception e) {
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
    try {
      return ServiceProvider.getService(KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaPublication.getKmeliaService()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_KmeliaBm_HOME", e);
    }
  }

  private StatisticService getStatisticService() {
    try {
      return ServiceProvider.getService(StatisticService.class);
    } catch (Exception e) {
      throw new StatisticRuntimeException("KmeliaPublication.getStatisticService()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private CommentService getCommentService() {
    return CommentServiceProvider.getCommentService();
  }

  private OrganizationController getOrganizationController() {
    return OrganizationControllerProvider.getOrganisationController();
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
  public String getDescription() {
    return getDetail().getDescription();
  }

  @Override
  public String getContributionType() {
    return getDetail().getContributionType();
  }

  /**
   * Is the specified user can access this publication?
   * <p/>
   * A user can access a publication if he has enough rights to access both the Kmelia instance
   * in which is managed this publication and the topics to which this publication belongs to.
   * @param user a user in Silverpeas.
   * @return true if the user can access this publication, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    return getDetail().canBeAccessedBy(user);
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public int getRank() {
    return rank;
  }
}
