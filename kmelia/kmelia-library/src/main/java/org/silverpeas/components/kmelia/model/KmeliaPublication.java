/*
 *  Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.kmelia.model;

import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

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
  private final PublicationPK pk;
  private int rank;
  private boolean read = false;

  private KmeliaPublication(PublicationPK id) {
    this.pk = id;
  }

  private KmeliaPublication(PublicationPK id, int rank) {
    this.pk = id;
    this.rank = rank;
  }

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
    publication.loadPublicationDetail();
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
   * Is the specified publication located in the given Kmelia topic an alias?
   * @param pub the publication. In the case of an alias, it is the original publication referred
   * by the alias.
   * @param topicPK the identifying key of a topic in a Kmelia instance that contains the
   * publication.
   * @return true if the specified publication located in the given topic is in fact an alias of
   * an original publication. False otherwise.
   */
  public static boolean isAnAlias(final PublicationDetail pub, final NodePK topicPK) {
    return !pub.getPK().getInstanceId().equals(topicPK.getInstanceId());
  }

  public boolean isRead() {
    return this.read;
  }

  public void setAsRead() {
    this.read = true;
  }

  /**
   * Is this publication an alias of an existing Kmelia publication?
   *
   * @return true if this publication is an alias, false otherwise.
   */
  public boolean isAlias() {
    return isAnAlias(getDetail(), new NodePK(null, getPk().getInstanceId()));
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
    String serverURL =
        ResourceLocator.getGeneralSettingBundle().getString("httpServerBase", defaultURL);
    return serverURL + URLUtil.getSimpleURL(URLUtil.URL_PUBLI, getPk().getId());
  }

  /**
   * Gets the details about this publication.
   *
   * @return the publication details.
   */
  public PublicationDetail getDetail() {
    if (detail == null) {
      loadPublicationDetail();
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
  public User getCreator() {
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
  @Override
  public User getLastModifier() {
    User lastModifier;
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
      throw new KmeliaRuntimeException(e);
    }

  }

  public int getNbAccess() {
    try {
      return getStatisticService().getCount(new ResourceReference(detail.getPK()), 1, "Publication");
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
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
    return this.pk == other.pk || (this.pk != null && this.pk.equals(other.pk));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }

  private void setPublicationDetail(final PublicationDetail detail) {
    this.detail = detail;
  }

  private void setPublicationCompleteDetail(final CompletePublication detail) {
    setPublicationDetail(detail.getPublicationDetail());
    this.completeDetail = detail;
  }

  private KmeliaService getKmeliaService() {
    try {
      return ServiceProvider.getService(KmeliaService.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private StatisticService getStatisticService() {
    return ServiceProvider.getService(StatisticService.class);
  }

  private CommentService getCommentService() {
    return CommentServiceProvider.getCommentService();
  }

  private OrganizationController getOrganizationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  private void loadPublicationDetail() {
    setPublicationDetail(getKmeliaService().getPublicationDetail(pk));
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
  public Date getLastModificationDate() {
    return getDetail().getLastModificationDate();
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
  public boolean canBeAccessedBy(final User user) {
    return getDetail().canBeAccessedBy(user);
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public int getRank() {
    return rank;
  }

  /**
   * Get the number of comments on this publication
   *
   * @return the number.
   */
  public int getNumberOfComments() {
    return getCommentService().getCommentsCountOnPublication(PublicationDetail.getResourceType(),
        getPk());
  }

  @SuppressWarnings("unchecked")
  public NodePK getOriginalLocation(String userId) {
    List<NodePK> fatherPKs = (List) getKmeliaService().getPublicationFathers(detail.getPK());
    if (CollectionUtil.isNotEmpty(fatherPKs)) {
      AccessController<NodePK> accessController =
          AccessControllerProvider.getAccessController(NodeAccessControl.class);
      for (NodePK fatherPK : fatherPKs) {
        if (accessController.isUserAuthorized(userId, fatherPK)) {
          return fatherPK;
        }
      }
    }
    return null;
  }
}
