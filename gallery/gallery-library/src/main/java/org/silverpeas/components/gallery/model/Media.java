/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.gallery.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.components.gallery.constant.GalleryResourceURIs;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.notification.AlbumMediaEventNotifier;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.process.io.file.FileBasePath;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a Media and provides all the common data.
 */
public abstract class Media implements SilverContentInterface, Serializable {
  private static final long serialVersionUID = -3193781401588525351L;

  public static final FileBasePath BASE_PATH = FileBasePath.UPLOAD_PATH;

  private MediaPK mediaPK;
  private String title = "";
  private String description = "";
  private String author = "";
  private String keyWord = "";
  private Period visibilityPeriod = Period.UNDEFINED;
  private Date createDate;
  private String createdBy;
  private User creator;
  private Date lastUpdateDate;
  private String lastUpdatedBy;
  private User lastUpdater;
  private String silverpeasContentId;
  private String iconUrl;

  public Media() {
    mediaPK = new MediaPK(null);
  }

  protected Media(final Media other) {
    if (other.mediaPK != null) {
      this.mediaPK = new MediaPK(other.mediaPK.getId(), other.mediaPK.getInstanceId());
    }
    this.title = other.title;
    this.description = other.description;
    this.author = other.author;
    this.keyWord = other.keyWord;
    this.visibilityPeriod = other.visibilityPeriod;
    this.createDate = other.createDate;
    this.createdBy = other.createdBy;
    this.creator = other.creator;
    this.lastUpdateDate = other.lastUpdateDate;
    this.lastUpdatedBy = other.lastUpdatedBy;
    this.lastUpdater = other.lastUpdater;
    this.silverpeasContentId = other.silverpeasContentId;
    this.iconUrl = other.iconUrl;
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
  public User getCreator() {
    if (StringUtil.isDefined(getCreatorId())) {
      if (creator == null || !getCreatorId().equals(creator.getId())) {
        creator = User.getById(getCreatorId());
      }
    } else {
      creator = null;
    }
    return creator;
  }

  public void setCreator(User creator) {
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

  @Override
  public User getLastModifier() {
    return getLastUpdater();
  }

  @Override
  public Date getLastModificationDate() {
    return getLastUpdateDate();
  }

  public Date getLastUpdateDate() {
    return lastUpdateDate != null ? lastUpdateDate : getCreationDate();
  }

  public void setLastUpdateDate(Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
  }

  public User getLastUpdater() {
    if (StringUtil.isDefined(getLastUpdatedBy())) {
      if (lastUpdater == null || !getLastUpdatedBy().equals(lastUpdater.getId())) {
        lastUpdater = User.getById(getLastUpdatedBy());
      }
    } else {
      setLastUpdater(getCreator());
    }
    return lastUpdater;
  }

  public void setLastUpdater(User lastUpdater) {
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
  public boolean canBeAccessedBy(final User user) {
    return SilverContentInterface.super.canBeAccessedBy(user) &&
        (isVisible(DateUtil.getDate()) || (user.isAccessAdmin() || getHighestUserRole(user)
            .isGreaterThanOrEquals(SilverpeasRole.publisher) || (getHighestUserRole(user)
            .isGreaterThanOrEquals(SilverpeasRole.writer) && user.getId().equals(getCreatorId()))));
  }

  /**
   * Gets the sub folder name of the media in the Silverpeas workspace.
   * @return the sub folder name of the media.
   */
  public String getWorkspaceSubFolderName() {
    return getType().getTechnicalFolder() + getId();
  }

  /**
   * Gets the permalink of a media.
   * @return the permalink string of a media.
   */
  public String getPermalink() {
    return URLUtil.getPermalink(URLUtil.Permalink.MEDIA, getId());
  }

  /**
   * Indicates if the media is previewable.
   * @return true if the media is previewable, false otherwise.
   */
  public boolean isPreviewable() {
    return true;
  }

  /**
   * Gets the Application URL thumbnail of the media according the specified media resolution.
   * @param mediaResolution
   * @return the URL of media thumbnail.
   */
  public String getApplicationThumbnailUrl(MediaResolution mediaResolution) {
    if (mediaResolution == null) {
      mediaResolution = MediaResolution.PREVIEW;
    }
    String thumbnailUrl =
        URLUtil.getApplicationURL() + "/gallery/jsp/icons/" + getType().name().toLowerCase() +
            "_";
    switch (mediaResolution) {
      case TINY:
        thumbnailUrl += MediaResolution.TINY.getLabel();
        break;
      case SMALL:
        thumbnailUrl += MediaResolution.SMALL.getLabel();
        break;
      case WATERMARK:
        return "";
      default:
        thumbnailUrl += MediaResolution.MEDIUM.getLabel();
        break;
    }
    thumbnailUrl += ".png";
    return FilenameUtils.normalize(thumbnailUrl, true);
  }

  /**
   * Gets the Application URL thumbnail of the media according the specified media resolution.
   * @param mediaResolution
   * @return the URL of media thumbnail.
   */
  public String getApplicationEmbedUrl(MediaResolution mediaResolution) {
    return GalleryResourceURIs.buildMediaEmbedURI(this, mediaResolution).toString();
  }

  /**
   * Gets the original URL of a media with cache handling.
   * @return
   */
  public String getApplicationOriginalUrl() {
    if (StringUtil.isNotDefined(getId())) {
      return "";
    }
    return GalleryResourceURIs.buildMediaContentURI(this, MediaResolution.ORIGINAL).toString();
  }

  /**
   * Gets the Silverpeas file.
   * @param mediaResolution the aimed resolution.
   * @return a {@link SilverpeasFile} instance which could represents also an non existing file.
   */
  public SilverpeasFile getFile(final MediaResolution mediaResolution) {
    return getFile(mediaResolution, null);
  }

  /**
   * Gets the Silverpeas file.
   * @param mediaResolution the aimed resolution.
   * @param size a specific size applied on the aimed resolution, ignored if not defined.
   * @return a {@link SilverpeasFile} instance which could represents also an non existing file.
   */
  public abstract SilverpeasFile getFile(final MediaResolution mediaResolution, final String size);

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
    return "searchResult?Type=" + getType().name() + "&Id=" + getId();
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
  public Collection<String> getLanguages() {
    return Collections.emptyList();
  }

  public String toString() {
    return "(pk = " + (getMediaPK() != null ? getMediaPK().toString() : "") + ", name = " +
        getTitle() + ")";
  }

  public boolean equals(Object o) {
    if (o instanceof Media) {
      Media anotherPhoto = (Media) o;
      return getMediaPK().equals(anotherPhoto.getMediaPK());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 29)
        .append(getMediaPK()).append(getTitle()).append(getDescription()).append(getPermalink())
        .toHashCode();
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
    MediaServiceProvider.getMediaService().removeMediaFromAllAlbums(this);
  }

  /**
   * Adds the current media to the album represented by specified identifiers.
   * @param albumIds the identifier of albums.
   */
  public void addToAlbums(String... albumIds) {
    MediaServiceProvider.getMediaService().addMediaToAlbums(this, albumIds);
  }

  /**
   * Sets the current media to the album represented by specified identifiers. (all not specified
   * album attachments will be deleted)
   * @param albumIds the identifier of albums.
   */
  public void setToAlbums(String... albumIds) {
    final Collection<String> previousAlbumIds = MediaServiceProvider.getMediaService().getAlbumIdsOf(this);
    final List<String> newAlbumIdsToNotify = Stream
        .of(albumIds)
        .filter(i -> !previousAlbumIds.contains(i))
        .collect(Collectors.toList());
    final List<String> oldAlbumIdsToNotify = previousAlbumIds
        .stream()
        .filter(i -> ArrayUtil.indexOf(albumIds, i) < 0)
        .collect(Collectors.toList());
    removeFromAllAlbums();
    addToAlbums(albumIds);
    final AlbumMediaEventNotifier notifier = AlbumMediaEventNotifier.get();
    for (final String albumId : oldAlbumIdsToNotify) {
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, new AlbumMedia(albumId, this));
    }
    for (final String albumId : newAlbumIdsToNotify) {
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, new AlbumMedia(albumId, this));
    }
  }

  /**
   * Retrieve highest user role
   * @param user the current user detail
   * @return the highest user role
   */
  protected SilverpeasRole getHighestUserRole(final User user) {
    Set<SilverpeasRole> userRoles =
        SilverpeasRole.from(OrganizationControllerProvider.getOrganisationController()
            .getUserProfiles(user.getId(), getComponentInstanceId()));
    return SilverpeasRole.getHighestFrom(userRoles);
  }

  /**
   * Creates a copy of the instance.
   * @return the new instance.
   */
  public abstract Media getCopy();
}