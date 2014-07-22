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

import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.constant.GalleryResourceURIs;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.process.media.GalleryLoadMetaDataProcess;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.date.Period;
import org.silverpeas.file.SilverpeasFile;
import org.silverpeas.file.SilverpeasFileProvider;
import org.silverpeas.notification.message.MessageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a Media that the content (the file in other words) is saved on the
 * Silverpeas workspace.
 */
public abstract class InternalMedia extends Media {
  private static final long serialVersionUID = 2070296932101933853L;

  private boolean downloadAuthorized = false;
  private String fileName;
  private long fileSize = 0;
  private MediaMimeType fileMimeType = MediaMimeType.ERROR;
  private Period downloadPeriod = Period.UNDEFINED;

  private LinkedHashMap<String, MetaData> metaData = null;

  /**
   * Indicates if the media is marked as downloadable. The visibility period is not taken into
   * account here.
   * @return true if marked as downloadable, false otherwise.
   */
  public boolean isDownloadAuthorized() {
    return downloadAuthorized;
  }

  /**
   * Indicates if the download is possible according the visibility information. The date of day
   * must be included into visibility period.
   * @return true if download is possible, false otherwise.
   */
  @Override
  public boolean isDownloadable() {
    Date dateOfDay = DateUtil.getDate();
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

  public MediaMimeType getFileMimeType() {
    return fileMimeType;
  }

  public void setFileMimeType(MediaMimeType fileMimeType) {
    this.fileMimeType = fileMimeType != null ? fileMimeType : MediaMimeType.ERROR;
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
  public String getApplicationThumbnailUrl(MediaResolution mediaResolution) {
    if (mediaResolution == null || mediaResolution == MediaResolution.ORIGINAL) {
      mediaResolution = MediaResolution.PREVIEW;
    }
    if (getType().isPhoto()) {
      if (MediaResolution.WATERMARK == mediaResolution) {
        SilverpeasFile fileWatermark = getFile(MediaResolution.WATERMARK);
        if (!fileWatermark.exists()) {
          return "";
        }
      }
      if (StringUtil.isDefined(getFileName()) && isPreviewable()) {
        return GalleryResourceURIs.buildMediaContentURI(this, mediaResolution).toString();
      } else {
        String thumbnailUrl = URLManager.getApplicationURL() + "/gallery/jsp/icons/notAvailable_" +
            MessageManager.getLanguage() + mediaResolution.getThumbnailSuffix();
        return FilenameUtils.normalize(thumbnailUrl, true);
      }
    } else {
      return super.getApplicationThumbnailUrl(mediaResolution);
    }
  }

  @Override
  public SilverpeasFile getFile(final MediaResolution mediaResolution) {
    String origOrThumbFileName = getFileName();
    if (getType().isPhoto() && StringUtil.isDefined(mediaResolution.getThumbnailSuffix())) {
      origOrThumbFileName = getId() + mediaResolution.getThumbnailSuffix();
    }
    return SilverpeasFileProvider.getInstance().getSilverpeasFile(FileUtils
        .getFile(Media.BASE_PATH.getPath(), getComponentInstanceId(), getWorkspaceSubFolderName(),
            origOrThumbFileName).getPath());
  }

  private Map<String, MetaData> getAllMetaData() {
    if (metaData == null) {
      metaData = new LinkedHashMap<String, MetaData>();
      try {
        GalleryLoadMetaDataProcess.load(this);
      } catch (Exception e) {
        SilverTrace.error("gallery", "Media.getAllMetaData", "gallery.MSG_NOT_ADD_METADATA",
            "mediaId =  " + getId());
      }
    }
    return metaData;
  }

  /**
   * Adds a metadata.
   * @param data a metadata.
   */
  public void addMetaData(MetaData data) {
    getAllMetaData().put(data.getProperty(), data);
  }

  /**
   * Gets a metadata according to the specified property name.
   * @param property the property name for which the metadata is requested.
   * @return the metadata if it exists, null otherwise.
   */
  public MetaData getMetaData(String property) {
    return getAllMetaData().get(property);
  }

  /**
   * Gets all metadata property names.
   * @return the list of metadata property names, empty list if no metadata.
   */
  public Collection<String> getMetaDataProperties() {
    Collection<MetaData> values = getAllMetaData().values();
    Collection<String> properties = new ArrayList<String>();
    for (MetaData meta : values) {
      if (meta != null) {
        properties.add(meta.getProperty());
      }
    }
    return properties;
  }
}