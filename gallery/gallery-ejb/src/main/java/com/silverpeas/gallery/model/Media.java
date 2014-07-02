/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents a Media and provides all the common data.
 */
public abstract class Media implements SilverpeasContent, SilverContentInterface, Serializable {
  private static final long serialVersionUID = -3193781401588525351L;

  private MediaPK mediaPK;
  private String title = "";
  private String description = "";
  private String author = "";
  private String keyWord = "";
  private Period visibilityPeriod = Period.UNDEFINED;
  private Date createDate;
  private String createdBy;
  private UserDetail creator;
  private Date lastUpdateDate;
  private String lastUpdatedBy;
  private UserDetail lastUpdater;
  private String silverpeasContentId;
  private String iconUrl;

  public Media() {
    mediaPK = new MediaPK(null);
  }

  public MediaPK getMediaPK() {
    return mediaPK;
  }

  public void setMediaPK(MediaPK mediaPK) {
    this.mediaPK = mediaPK;
  }

  public void setId(String mediaId) {
    getMediaPK().setId(mediaId);
  }

  @Override
  public String getId() {
    return getMediaPK() != null ? getMediaPK().getId() : null;
  }

  public void setComponentInstanceId(String instanceId) {
    getMediaPK().setComponentName(instanceId);
  }

  @Override
  public String getInstanceId() {
    return getMediaPK() != null ? getMediaPK().getInstanceId() : null;
  }

  @Override
  public String getComponentInstanceId() {
    return getInstanceId();
  }

  @Override
  public String getContributionType() {
    return getType().name();
  }

  public abstract MediaType getType();

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = StringUtil.isDefined(title) ? title : "";
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = StringUtil.isDefined(description) ? description : "";
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = StringUtil.isDefined(author) ? author : "";
  }

  public String getKeyWord() {
    return keyWord;
  }

  public void setKeyWord(String keyWord) {
    this.keyWord = StringUtil.isDefined(keyWord) ? keyWord : "";
  }

  public Period getVisibilityPeriod() {
    return visibilityPeriod;
  }

  public void setVisibilityPeriod(final Period visibilityPeriod) {
    this.visibilityPeriod = Period.check(visibilityPeriod);
  }

  public boolean isVisible() {
    Date today = DateUtil.getDate();
    return isVisible(today);
  }

  protected boolean isVisible(Date today) {
    boolean result = true;
    if (today != null && getVisibilityPeriod().isDefined()) {
      result = getVisibilityPeriod().contains(today);
    }
    return result;
  }

  @Override
  public Date getCreationDate() {
    return createDate;
  }

  public void setCreationDate(Date createDate) {
    this.createDate = createDate;
  }

  @Override
  public UserDetail getCreator() {
    if (StringUtil.isDefined(getCreatorId())) {
      if (creator == null || !getCreatorId().equals(creator.getId())) {
        creator = UserDetail.getById(getCreatorId());
      }
    } else {
      creator = null;
    }
    return creator;
  }

  public void setCreator(UserDetail creator) {
    this.creator = creator;
    setCreatorId((creator != null) ? creator.getId() : null);
  }

  @Override
  public String getCreatorId() {
    return createdBy;
  }

  public void setCreatorId(String creatorId) {
    createdBy = creatorId;
  }

  public String getCreatorName() {
    return getCreator() != null ? getCreator().getDisplayedName() : "";
  }

  public Date getLastUpdateDate() {
    return lastUpdateDate != null ? lastUpdateDate : getCreationDate();
  }

  public void setLastUpdateDate(Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
  }

  public UserDetail getLastUpdater() {
    if (StringUtil.isDefined(getLastUpdatedBy())) {
      if (lastUpdater == null || !getLastUpdatedBy().equals(lastUpdater.getId())) {
        lastUpdater = UserDetail.getById(getLastUpdatedBy());
      }
    } else {
      setLastUpdater(getCreator());
    }
    return lastUpdater;
  }

  public void setLastUpdater(UserDetail lastUpdater) {
    this.lastUpdater = lastUpdater;
    setLastUpdatedBy((lastUpdater != null) ? lastUpdater.getId() : null);
  }

  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public String getLastUpdaterName() {
    return getLastUpdater() != null ? getLastUpdater().getDisplayedName() : "";
  }

  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController =
        AccessControllerProvider.getAccessController("componentAccessController");
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId()) &&
        (isVisible(DateUtil.getNow()) || (user.isAccessAdmin() || getGreatestUserRole(user)
            .isGreaterThanOrEquals(SilverpeasRole.publisher) || (getGreatestUserRole(user)
            .isGreaterThanOrEquals(SilverpeasRole.writer) && user.getId().equals(getCreatorId()))));
  }

  /**
   * Gets the permalink of a media.
   * @return the permalink string of a media.
   */
  public String getPermalink() {
    return URLManager.getPermalink(URLManager.Permalink.Media, getId());
  }

  public boolean isPreviewable() {
    return true;
  }

  /**
   * Gets the URL prefix the media thumbnail according the specified media resolution.
   * @param mediaResolution
   * @return the URL of media thumbnail.
   */
  public String getThumbnailUrl(MediaResolution mediaResolution) {
    if (mediaResolution == null) {
      mediaResolution = MediaResolution.PREVIEW;
    }
    String thumbnailUrl =
        URLManager.getApplicationURL() + "/gallery/jsp/icons/" + getType().name().toLowerCase() +
            "_";
    switch (mediaResolution) {
      case TINY:
        thumbnailUrl += MediaResolution.TINY.getLabel();
        break;
      case SMALL:
        thumbnailUrl += MediaResolution.SMALL.getLabel();
        break;
      case MEDIUM:
      case LARGE:
      case WATERMARK:
      case PREVIEW:
        thumbnailUrl += MediaResolution.MEDIUM.getLabel();
        break;
    }
    thumbnailUrl += ".png";
    return FilenameUtils.normalize(thumbnailUrl, true);
  }

  @Override
  public String getSilverpeasContentId() {
    return silverpeasContentId;
  }

  public void setSilverpeasContentId(String silverpeasContentId) {
    this.silverpeasContentId = silverpeasContentId;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  @Override
  public String getIconUrl() {
    return this.iconUrl;
  }

  /**
   * Indicated if the download is possible.
   * @return true if download is possible, false otherwise.
   */
  public boolean isDownloadable() {
    return true;
  }

  @Override
  public String getURL() {
    return "searchResult?Type=Media&Id=" + getId();
  }

  @Override
  public String getDate() {
    return DateUtil.date2SQLDate(getLastUpdateDate());
  }

  @Override
  public String getSilverCreationDate() {
    return DateUtil.date2SQLDate(getCreationDate());
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public String getName(String language) {
    return getName();
  }

  @Override
  public String getDescription(String language) {
    return getDescription();
  }

  @Override
  public Iterator<String> getLanguages() {
    return null;
  }

  public String toString() {
    return "(pk = " + (getMediaPK() != null ? getMediaPK().toString() : "") + ", name = " +
        getTitle() + ")";
  }

  public boolean equals(Object o) {
    if (o instanceof Media) {
      Media anotherPhoto = (Media) o;
      return getMediaPK().equals(anotherPhoto.getMediaPK());
    } else if (o instanceof PhotoDetail) {
      // TODO this case must be deleted after the end of work of Gallery to Media migration
      PhotoDetail anotherPhoto = (PhotoDetail) o;
      return getMediaPK().equals(anotherPhoto.getMediaPK());
    }
    return false;
  }

  /**
   * Gets the internal media instance if type of the current media is {@link MediaType#Photo} or
   * {@link MediaType#Video} or {@link MediaType#Sound}.
   * @return internal media instance, null if media type is not {@link MediaType#Photo} or
   * {@link MediaType#Video} or {@link MediaType#Sound}.
   */
  public InternalMedia getInternalMedia() {
    if (this instanceof InternalMedia) {
      return (InternalMedia) this;
    }
    return null;
  }

  /**
   * Gets the photo instance if type of the current media is {@link MediaType#Photo}.
   * @return photo instance, null if media type is not {@link MediaType#Photo}.
   */
  public Photo getPhoto() {
    if (MediaType.Photo == getType()) {
      return (Photo) this;
    }
    return null;
  }

  /**
   * Gets the video instance if type of the current media is {@link MediaType#Video}.
   * @return video instance, null if media type is not {@link MediaType#Video}.
   */
  public Video getVideo() {
    if (MediaType.Video == getType()) {
      return (Video) this;
    }
    return null;
  }

  /**
   * Gets the sound instance if type of the current media is {@link MediaType#Sound}.
   * @return sound instance, null if media type is not {@link MediaType#Sound}.
   */
  public Sound getSound() {
    if (MediaType.Sound == getType()) {
      return (Sound) this;
    }
    return null;
  }

  /**
   * Gets the streaming instance if type of the current media is {@link MediaType#Streaming}.
   * @return streaming instance, null if media type is not {@link MediaType#Streaming}.
   */
  public Streaming getStreaming() {
    if (MediaType.Streaming == getType()) {
      return (Streaming) this;
    }
    return null;
  }

  /**
   * Removes the current media from all albums which it is attached to.
   */
  public void removeFromAllAlbums() {
    MediaServiceFactory.getMediaService().removeMediaFromAllAlbums(this);
  }

  /**
   * Adds the current media to the album represented by specified identifiers.
   * @param albumIds the identifier of albums.
   */
  public void addToAlbums(String... albumIds) {
    MediaServiceFactory.getMediaService().addMediaToAlbums(this, albumIds);
  }

  /**
   * Sets the current media to the album represented by specified identifiers. (all not specified
   * album attachments will be deleted)
   * @param albumIds the identifier of albums.
   */
  public void setToAlbums(String... albumIds) {
    removeFromAllAlbums();
    addToAlbums(albumIds);
  }

  /**
   * Retrieve greatest user role
   * @param user the current user detail
   * @return the greatest user role
   */
  protected SilverpeasRole getGreatestUserRole(final UserDetail user) {
    Set<SilverpeasRole> userRoles =
        SilverpeasRole.from(OrganisationControllerFactory.getOrganisationController()
            .getUserProfiles(user.getId(), getComponentInstanceId()));
    return SilverpeasRole.getGreaterFrom(userRoles);
  }
}