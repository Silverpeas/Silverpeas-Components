/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.FileServerUtils;
import org.silverpeas.date.Period;
import org.silverpeas.notification.message.MessageManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * This class is an old one. {@link Photo} must be used instead.
 * It became a wrapper of Photo class.
 */
public class PhotoDetail implements SilverContentInterface, Serializable {
  private static final long serialVersionUID = -1907932374204169173L;

  private final Photo photo;

  public PhotoDetail() {
    photo = new Photo();
    photo.setMediaPK(null);
  }

  public PhotoDetail(Photo photo) {
    this.photo = photo;
  }

  public PhotoDetail(String title, String description, Date creationDate,
      Date updateDate, String vueDate, String author, boolean download,
      boolean albumLabel) {
    this();
    setTitle(title);
    setDescription(description);
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    setVueDate(vueDate);
    setAuthor(author);
    setDownload(download);
    setAlbumLabel(albumLabel);
  }

  public PhotoDetail(String title, String description, Date creationDate,
      Date updateDate, String vueDate, String author, boolean download,
      boolean albumLabel, Date beginDate, Date endDate, String keyWord,
      Date beginDownloadDate, Date endDownloadDate) {
    this();
    setTitle(title);
    setDescription(description);
    setCreationDate(creationDate);
    setUpdateDate(updateDate);
    setVueDate(vueDate);
    setAuthor(author);
    setDownload(download);
    setAlbumLabel(albumLabel);
    setBeginDate(beginDate);
    setEndDate(endDate);
    setKeyWord(keyWord);
    setBeginDownloadDate(beginDownloadDate);
    setEndDownloadDate(endDownloadDate);
  }

  /**
   * Gets the wrapped photo object.
   * @return the wrapped photo object.
   */
  public Photo getPhoto() {
    return photo;
  }

  public String getKeyWord() {
    return photo.getKeyWord();
  }

  public void setKeyWord(String keyWord) {
    photo.setKeyWord(keyWord);
  }

  public void setSilverObjectId(String silverObjectId) {
    photo.setSilverpeasContentId(silverObjectId);
  }

  public void setSilverObjectId(int silverObjectId) {
    photo.setSilverpeasContentId(Integer.toString(silverObjectId));
  }

  public String getSilverObjectId() {
    return photo.getSilverpeasContentId();
  }

  public void setIconUrl(String iconUrl) {
    photo.setIconUrl(iconUrl);
  }

  @Override
  public String getIconUrl() {
    return photo.getIconUrl();
  }

  public Date getBeginDate() {
    return photo.getVisibilityPeriod().getBeginDate();
  }

  public void setBeginDate(Date beginDate) {
    photo.setVisibilityPeriod(Period.from(beginDate, getEndDate()));
  }

  public Date getEndDate() {
    return photo.getVisibilityPeriod().getEndDate();
  }

  public void setEndDate(Date endDate) {
    photo.setVisibilityPeriod(Period.from(getBeginDate(), endDate));
  }

  public String getAuthor() {
    return photo.getAuthor();
  }

  public void setAuthor(String author) {
    photo.setAuthor(author);
  }

  @Override
  public String getCreatorId() {
    return photo.getCreatorId();
  }

  public void setCreatorId(String creatorId) {
    photo.setCreatorId(creatorId);
  }

  public Date getCreationDate() {
    return photo.getCreationDate();
  }

  public void setCreationDate(Date creationDate) {
    photo.setCreationDate(creationDate);
  }

  public Date getUpdateDate() {
    return photo.getLastUpdateDate();
  }

  public void setUpdateDate(Date updateDate) {
    photo.setLastUpdateDate(updateDate);
  }

  public String getVueDate() {
    // TODO : at the end og media migration, this method must be removed
    return null;
  }

  public void setVueDate(String vueDate) {
    // TODO : at the end og media migration, this method must be removed
  }

  @Override
  public String getDescription() {
    return photo.getDescription();
  }

  public void setDescription(String description) {
    photo.setDescription(description);
  }

  @Override
  public String getInstanceId() {
    return getMediaPK().getInstanceId();
  }

  public String getStatus() {
    // TODO : at the end og media migration, this method must be removed
    return null;
  }

  public void setStatus(String status) {
    // TODO : at the end og media migration, this method must be removed
  }

  public int getSizeH() {
    return photo.getResolutionH();
  }

  public void setSizeH(int sizeH) {
    photo.setResolutionH(sizeH);
  }

  public int getSizeL() {
    return photo.getResolutionW();
  }

  public void setSizeL(int sizeL) {
    photo.setResolutionW(sizeL);
  }

  public boolean isDownload() {
    return photo.isDownloadAuthorized();
  }

  public boolean isDownloadable() {
    return photo.isDownloadable();
  }

  public void setDownload(boolean download) {
    photo.setDownloadAuthorized(download);
  }

  public String getTitle() {
    return photo.getTitle();
  }

  public void setTitle(String title) {
    photo.setTitle(title);
  }

  public boolean isAlbumLabel() {
    // TODO : at the end og media migration, this method must be removed
    return false;
  }

  public void setAlbumLabel(boolean albumLabel) {
    // TODO : at the end og media migration, this method must be removed
  }

  public String getImageMimeType() {
    return photo.getFileMimeType();
  }

  public void setImageMimeType(String imageMimeType) {
    photo.setFileMimeType(imageMimeType);
  }

  public String getImageName() {
    return photo.getFileName();
  }

  public void setImageName(String imageName) {
    photo.setFileName(imageName);
  }

  public long getImageSize() {
    return photo.getFileSize();
  }

  public void setImageSize(long imageSize) {
    photo.setFileSize(imageSize);
  }

  public MediaPK getMediaPK() {
    return photo.getMediaPK();
  }

  public void setMediaPK(MediaPK mediaPK) {
    photo.setMediaPK(mediaPK);
  }

  public String toString() {
    return photo.toString();
  }

  @Override
  public boolean equals(Object o) {
    return photo.equals(o);
  }

  public String getCreatorName() {
    return photo.getCreatorName();
  }

  public String getUpdateId() {
    return photo.getLastUpdatedBy();
  }

  public void setUpdateId(String updateId) {
    photo.setLastUpdatedBy(updateId);
  }

  public String getUpdateName() {
    return photo.getLastUpdaterName();
  }

  @Override
  public String getName() {
    return photo.getName();
  }

  @Override
  public String getURL() {
    return photo.getURL();
  }

  @Override
  public String getId() {
    return getMediaPK().getId();
  }

  @Override
  public String getDate() {
    return photo.getDate();
  }

  @Override
  public String getSilverCreationDate() {
    return photo.getSilverCreationDate();
  }

  public String getPermalink() {
    return photo.getPermalink();
  }

  public void addMetaData(MetaData data) {
    photo.addMetaData(data);
  }

  public MetaData getMetaData(String property) {
    return photo.getMetaData(property);
  }

  public Collection<String> getMetaDataProperties() {
    return photo.getMetaDataProperties();
  }

  public Date getBeginDownloadDate() {
    return photo.getDownloadPeriod().getBeginDatable().isDefined() ?
        photo.getDownloadPeriod().getBeginDate() : null;
  }

  public void setBeginDownloadDate(Date beginDownloadDate) {
    photo.setDownloadPeriod(Period.from(beginDownloadDate, photo.getDownloadPeriod().getEndDate()));
  }

  public Date getEndDownloadDate() {
    return photo.getDownloadPeriod().getEndDatable().isDefined() ?
        photo.getDownloadPeriod().getEndDate() : null;
  }

  public void setEndDownloadDate(Date endDownloadDate) {
    photo.setDownloadPeriod(Period.from(photo.getDownloadPeriod().getBeginDate(), endDownloadDate));
  }

  @Override
  public String getDescription(String language) {
    return getDescription();
  }

  @Override
  public String getName(String language) {
    return getName();
  }

  @Override
  public Iterator<String> getLanguages() {
    return photo.getLanguages();
  }

  public boolean isVisible(Date today) {
    return photo.isVisible(today);
  }

  public String getContributionType() {
    return photo.getContributionType();
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return Photo.getResourceType();
  }
  
  public boolean isPreviewable() {
    return photo.isPreviewable();
  }

  public String getThumbnailUrl(final String formatPrefix) {
    return photo.getThumbnailUrl(formatPrefix);
  }
}