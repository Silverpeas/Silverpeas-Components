/*
 *  Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A publication as defined in a Kmelia component. A publication in Kmelia is a publication that
 * is always in a given topic (the father of the publication), and that can be positioned in the
 * PDC, can have attachments, and can be commented.
 * <p>
 * In Kmelia, a publication can be in one or more topics. In such a case, a Kmelia publication that
 * is in a topic other that the original one is said to be an alias of the publication in the
 * original topic: any changes are done in the true publication.
 * </p>
 */
public class KmeliaPublication implements SilverpeasContent {

  private static final long serialVersionUID = 4861635754389280165L;
  private PublicationDetail detail;
  private CompletePublication completeDetail;
  private final PublicationPK pk;
  private int rank;
  private boolean read = false;
  private Location location;

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
   * {@link KmeliaRuntimeException} is thrown. The publication is by default the original one and
   * hence not an alias.
   *
   * @param pk the primary key of the publication to get.
   * @return the Kmelia publication matching the primary key.
   */
  public static KmeliaPublication withPK(final PublicationPK pk) {
    KmeliaPublication publication = new KmeliaPublication(pk);
    publication.loadPublicationDetail();
    return publication;
  }

  /**
   * Gets the Kmelia publication with the specified primary key and that is in the topic specified
   * by its primary key. If no such publication exists with the specified key or if the publication
   * has no such father, then the runtime exception {@link KmeliaRuntimeException} is thrown.
   * @param pk the primary key of the publication to get.
   * @param fatherPk the primary key of the topic that is the father of the publication.
   * @return the Kmelia publication matching the primary key.
   */
  public static KmeliaPublication withPK(final PublicationPK pk, final NodePK fatherPk) {
    KmeliaPublication publication = withPK(pk);
    publication.setFather(fatherPk, null);
    return publication;
  }

  private void setFather(final NodePK fatherPk, final Map<String, List<Location>> locationCache) {
    location = findLocation(fatherPk, locationCache);
    if (location != null) {
      getDetail().setAlias(location.isAlias());
    }
  }

  private Location findLocation(final NodePK pk, final Map<String, List<Location>> locationCache) {
    final Predicate<Location> predicate;
    final String nodeIMsg;
    if (pk != null) {
      predicate = pk::equals;
      nodeIMsg = " in node " + pk.getId() + "(Kmelia " + pk.getInstanceId() + ")";
    } else {
      predicate = l -> !l.isAlias();
      nodeIMsg = "";
    }
    final PublicationPK mainPubPk = getDetail().isClone()
        ? getDetail().getClonePK()
        : getDetail().getPK();
    return getAllLocations(mainPubPk, locationCache)
        .stream()
        .filter(predicate)
        .findFirst()
        .orElseGet(() ->  {
          if (pk == null || !KmeliaHelper.isKmax(pk.getInstanceId())) {
            throw new KmeliaRuntimeException(
                "Unable to find the location of the publication " + getId() + nodeIMsg);
          }
          return null;
        });
  }

  private Collection<Location> getAllLocations(final PublicationPK mainPubPk,
      Map<String, List<Location>> locationCache) {
    if (locationCache != null) {
      return locationCache.getOrDefault(mainPubPk.getId(), Collections.emptyList());
    }
    return PublicationService.get().getAllLocations(mainPubPk);
  }

  public Location getLocation() {
    if (location == null) {
      location = findLocation(null, null);
    }
    return location;
  }

  /**
   * Gets the Kmelia publication from the specified publication detail.
   * The publication is by default the original one and not an alias.
   *
   * @param detail the detail about the publication to get.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication fromDetail(final PublicationDetail detail) {
    KmeliaPublication publication = new KmeliaPublication(detail.getPK());
    publication.setPublicationDetail(detail);
    return publication;
  }

  /**
   * Gets the Kmelia publication from the specified publication detail and with the given rank.
   * The publication is by default the original one and not an alias.
   *
   * @param detail the detail about the publication to get.
   * @param rank the rank of the publication among others ones.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication fromDetail(final PublicationDetail detail, int rank) {
    KmeliaPublication publication = new KmeliaPublication(detail.getPK(), rank);
    publication.setPublicationDetail(detail);
    return publication;
  }

  /**
   * Gets the Kmelia publication in the given topic from the specified publication detail.
   *
   * @param detail the detail about the publication to get.
   * @param fatherPK the primary key of the topic that is father of the publication to get.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication fromDetail(final PublicationDetail detail,
      final NodePK fatherPK) {
    return fromDetail(detail, fatherPK, 0);
  }

  /**
   * Gets the Kmelia publication in the given topic from the specified publication detail.
   *
   * @param detail the detail about the publication to get.
   * @param fatherPK the primary key of the topic that is father of the publication to get.
   * @param locationCache cache of locations already loaded.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication fromDetail(final PublicationDetail detail,
      final NodePK fatherPK, final Map<String, List<Location>> locationCache) {
    return fromDetail(detail, fatherPK, 0, locationCache);
  }

  /**
   * Gets the Kmelia publication in the given topic from the specified publication detail and
   * with the given rank.
   *
   * @param detail the detail about the publication to get.
   * @param fatherPK the primary key of the topic that is father of the publication to get.
   * @param rank the rank of the publication among others ones.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication fromDetail(final PublicationDetail detail, final NodePK fatherPK,
      int rank) {
    return fromDetail(detail, fatherPK, rank, null);
  }

  /**
   * Gets the Kmelia publication in the given topic from the specified publication detail and
   * with the given rank.
   *
   * @param detail the detail about the publication to get.
   * @param fatherPK the primary key of the topic that is father of the publication to get.
   * @param rank the rank of the publication among others ones.
   * @param locationCache cache of locations already loaded.
   * @return the Kmelia publication matching the specified publication detail.
   */
  public static KmeliaPublication fromDetail(final PublicationDetail detail, final NodePK fatherPK,
      int rank, final Map<String, List<Location>> locationCache) {
    final KmeliaPublication publication = new KmeliaPublication(detail.getPK(), rank);
    publication.setPublicationDetail(detail);
    publication.setFather(fatherPK, locationCache);
    return publication;
  }

  /**
   * Gets the Kmelia publication from the specified complete publication detail.
   * The publication is by default the original one and not an alias.
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
    return getDetail().isAlias();
  }

  /**
   * Is this publication visible?
   * @return true if this publication is visible, false otherwise.
   */
  public boolean isVisible() {
    return getDetail().isVisible();
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
    return User.getById(creatorId);
  }

  /**
   * Gets the user that has lastly modified this publication. He's the last one that has worked on
   * this publication. If this publication was not modified since its creation, the creator is
   * returned as he's the last user that has worked on this publication.
   *
   * @return the detail about the last modifier of this publication.
   */
  @Override
  public User getLastUpdater() {
    User lastModifier;
    String modifierId = getDetail().getUpdaterId();
    if (modifierId == null) {
      lastModifier = getCreator();
    } else {
      lastModifier = User.getById(modifierId);
    }
    return lastModifier;
  }

  /**
   * Gets the comments on this publication.
   *
   * @return an unmodifiable list with the comments on this publication.
   */
  public List<Comment> getComments() {
    return Collections.unmodifiableList(getCommentService().getAllCommentsOnResource(
        PublicationDetail.getResourceType(), new ResourceReference(pk)));
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
    return Objects.equals(this.pk, other.pk);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }

  private void setPublicationDetail(final PublicationDetail detail) {
    if (detail == null) {
      throw new KmeliaRuntimeException(
          SilverpeasExceptionMessages.failureOnGetting("publication detail", getId()));
    }
    this.detail = detail;
  }

  private void setPublicationCompleteDetail(final CompletePublication detail) {
    if (this.detail == null) {
      setPublicationDetail(detail.getPublicationDetail());
    }
    this.completeDetail = detail;
  }

  private KmeliaService getKmeliaService() {
    try {
      return KmeliaService.get();
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }
  }

  private StatisticService getStatisticService() {
    return StatisticService.get();
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
    return getDetail().getInstanceId();
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
  public Date getLastUpdateDate() {
    return getDetail().getLastUpdateDate();
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
    return getCommentService().getCommentsCountOnResource(PublicationDetail.getResourceType(),
        new ResourceReference(getPk()));
  }

  /**
   * Gets the original location of this alias according to the access right the of the specified
   * user. If this location isn't an alias, then returns itself. If the user has no access right
   * to the original location, then returns nothing.
   * If no original location can be found, whatever the access right of the user, a
   * {@link KmeliaRuntimeException} is thrown.
   * @param userId the unique identifier of the user for which the access right on the original
   * location has to be checked.
   * @return either the original location (itself if this location is already the original one) or
   * nothing whether the given user has no access right on it.
   */
  public Optional<Location> getOriginalLocation(String userId) {
    final Location originalLocation = findLocation(null, null);
    if (originalLocation != null && NodeAccessControl.get().isUserAuthorized(userId, originalLocation)) {
      return Optional.of(originalLocation);
    } else {
      return Optional.empty();
    }
  }

  public ValidatorsList getValidators() {
    return getKmeliaService().getAllValidators(getPk());
  }
}
