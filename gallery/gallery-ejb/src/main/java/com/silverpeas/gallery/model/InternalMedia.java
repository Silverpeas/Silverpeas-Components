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

import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileServerUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.date.Period;
import org.silverpeas.notification.message.MessageManager;

import java.util.Date;

/**
 * This class represents a Media that the content (the file in other words) is saved on the
 * Silverpeas workspace.
 */
public abstract class InternalMedia extends Media {
  private static final long serialVersionUID = 2070296932101933853L;

  private boolean downloadAuthorized = false;
  private String fileName;
  private long fileSize = 0;
  private String fileMimeType;
  private Period downloadPeriod = Period.UNDEFINED;

  /**
   * Indicates if the media is marked as downloadable.
   * The visibility period is not taken into account here.
   * @return true if marked as downloadable, false otherwise.
   */
  public boolean isDownloadAuthorized() {
    return downloadAuthorized;
  }

  /**
   * Indicates if the download is possible according the visibility information.
   * The date of day must be included into visibility period.
   * @return true if download is possible, false otherwise.
   */
  @Override
  public boolean isDownloadable() {
    Date dateOfDay = DateUtil.getNow();
    boolean isDownloadable = isDownloadAuthorized() && isVisible(dateOfDay);
    if (isDownloadable && getDownloadPeriod().isDefined()) {
      isDownloadable = getDownloadPeriod().contains(dateOfDay);
    }
    return isDownloadable;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public String getFileMimeType() {
    return fileMimeType;
  }

  public void setFileMimeType(String fileMimeType) {
    this.fileMimeType = fileMimeType;
  }

  public void setDownloadAuthorized(boolean downloadAuthorized) {
    this.downloadAuthorized = downloadAuthorized;
  }

  public Period getDownloadPeriod() {
    return downloadPeriod;
  }

  public void setDownloadPeriod(final Period downloadPeriod) {
    this.downloadPeriod = Period.check(downloadPeriod);
  }

  @Override
  public String getThumbnailUrl(MediaResolution mediaResolution) {
    String thumbnailUrl;
    if (mediaResolution == null) {
      mediaResolution = MediaResolution.PREVIEW;
    }
    if (getType().isPhoto()) {
      if (StringUtil.isDefined(getFileName())) {
        thumbnailUrl = FileServerUtils
            .getUrl(getInstanceId(), getId() + mediaResolution.getThumbnailSuffix(),
                getFileMimeType(), GalleryComponentSettings.getMediaFolderNamePrefix() + getId());
        if (!isPreviewable()) {
          thumbnailUrl =
              URLManager.getApplicationURL() + "/gallery/jsp/icons/notAvailable_" + MessageManager.
                  getLanguage() + mediaResolution.getThumbnailSuffix();
        }
      } else {
        thumbnailUrl = URLManager.getApplicationURL() + "/gallery/jsp/icons/notAvailable_" +
            MessageManager.getLanguage() + mediaResolution.getThumbnailSuffix();
      }
    } else {
      thumbnailUrl = super.getThumbnailUrl(mediaResolution);
    }
    return FilenameUtils.normalize(thumbnailUrl, true);
  }
}