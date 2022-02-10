/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.components.gallery.constant.GalleryResourceURIs;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.process.media.GalleryLoadMetaDataProcess;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.io.media.video.ThumbnailPeriod;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.isDefined;

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

  public InternalMedia() {
    super();
  }

  protected InternalMedia(final InternalMedia other) {
    super(other);
    this.downloadAuthorized = other.downloadAuthorized;
    this.fileName = other.fileName;
    this.fileSize = other.fileSize;
    this.fileMimeType = other.fileMimeType;
    this.downloadPeriod = other.downloadPeriod;
    if (other.metaData != null) {
      this.metaData = new LinkedHashMap<>(other.metaData);
    }
  }

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
    if (mediaResolution == null || mediaResolution == MediaResolution.ORIGINAL ||
        mediaResolution == MediaResolution.NORMAL) {
      mediaResolution = MediaResolution.PREVIEW;
    }
    if (getType().isPhoto()) {
      return getPhotoThumbnailUrl(mediaResolution);
    } else if (getType().isVideo()) {
      if (getVideoThumbnailUrl()) {
        return GalleryResourceURIs.buildVideoThumbnailURI(this, ThumbnailPeriod.Thumbnail0)
            .toString();
      }
      return super.getApplicationThumbnailUrl(mediaResolution);
    } else {
      return super.getApplicationThumbnailUrl(mediaResolution);
    }
  }

  private boolean getVideoThumbnailUrl() {
    SilverpeasFile thumbFile =
        SilverpeasFileProvider.getFile(FileUtils
            .getFile(Media.BASE_PATH.getPath(), getComponentInstanceId(),
                getWorkspaceSubFolderName(),
                "img0.jpg").getPath());
    return thumbFile != null && thumbFile.exists();
  }

  private String getPhotoThumbnailUrl(final MediaResolution mediaResolution) {
    if (MediaResolution.WATERMARK == mediaResolution) {
      SilverpeasFile fileWatermark = getFile(MediaResolution.WATERMARK);
      if (!fileWatermark.exists()) {
        return "";
      }
    }
    if (StringUtil.isDefined(getFileName()) && isPreviewable()) {
      return GalleryResourceURIs.buildMediaContentURI(this, mediaResolution).toString();
    } else {
      String thumbnailUrl = URLUtil.getApplicationURL() + "/gallery/jsp/icons/notAvailable_" +
          MessageManager.getLanguage() + mediaResolution.getThumbnailSuffix() + ".jpg";
      return FilenameUtils.normalize(thumbnailUrl, true);
    }
  }

  @Override
  public SilverpeasFile getFile(final MediaResolution mediaResolution, final String size) {
    if (StringUtil.isNotDefined(getFileName())) {
      return SilverpeasFile.NO_FILE;
    }
    final int maxSize = 2;
    List<String> potentialFileNames = new ArrayList<>(maxSize);
    potentialFileNames.add(getFileName());
    if (getType().isPhoto()) {
      final String thumbnailSuffix = mediaResolution.getThumbnailSuffix();
      final String originalFileExt = "." + FilenameUtils.getExtension(getFileName());
      if (mediaResolution == MediaResolution.NORMAL) {
        potentialFileNames.add(getId() + thumbnailSuffix + originalFileExt);
      } else if (StringUtil.isDefined(thumbnailSuffix)) {
        potentialFileNames.set(0, getId() + thumbnailSuffix + originalFileExt);
      }
      if (!".jpg".equalsIgnoreCase(originalFileExt)) {
        // this case is about taking in charge the old medias
        potentialFileNames.add(getId() + thumbnailSuffix + ".jpg");
      }
    }
    SilverpeasFile file = SilverpeasFile.NO_FILE;
    for (String potentialFileName : potentialFileNames) {
      File physicalFile = FileUtils
          .getFile(Media.BASE_PATH.getPath(), getComponentInstanceId(), getWorkspaceSubFolderName(),
              potentialFileName);
      if (potentialFileNames.size() > 1 && !physicalFile.exists()) {
        continue;
      }
      if (isDefined(size)) {
        physicalFile = Paths.get(physicalFile.getParentFile().getPath(), size, physicalFile.getName()).toFile();
      }
      file = SilverpeasFileProvider.getFile(physicalFile.getPath());
    }
    return file;
  }

  private Map<String, MetaData> getAllMetaData() {
    if (metaData == null) {
      metaData = new LinkedHashMap<>();
      try {
        GalleryLoadMetaDataProcess.load(this);
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(e);
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
    Collection<String> properties = new ArrayList<>();
    for (MetaData meta : values) {
      if (meta != null) {
        properties.add(meta.getProperty());
      }
    }
    return properties;
  }
}
