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
package org.silverpeas.components.gallery.control;

import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.ImportExportDescriptor;
import org.silverpeas.core.importexport.control.AbstractExportProcess;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Service
public class GalleryExporter extends AbstractExportProcess {

  /**
   * Expected export parameter giving the detail about the user that calls the export of media.
   */
  public static final String EXPORT_FOR_USER = "gallery.export.user";

  /**
   * Expected export parameter giving the detail about the user that calls the export of media.
   */
  public static final String EXPORT_RESOLUTION = "gallery.export.media.resolution";

  /**
   * Optional export parameter giving the album which contains media to be performed.
   */
  public static final String EXPORT_ALBUM = "gallery.export.album";

  /**
   * Optional export parameter giving the media identifiers (photo) to be performed.
   */
  public static final String EXPORT_PHOTOS = "gallery.export.photos";

  private static GalleryService getMediaService() {
    return MediaServiceProvider.getMediaService();
  }

  public void exportAlbum(ImportExportDescriptor exportDescriptor, ExportReport exportReport)
      throws ExportException {
    try {
      UserDetail userDetail = exportDescriptor.getParameter(EXPORT_FOR_USER);
      File tempExportDir = createExportDir(userDetail);
      MediaResolution mediaResolution = exportDescriptor.getParameter(EXPORT_RESOLUTION);

      // Retrieve all picture from current album and sub albums
      AlbumDetail albumDetail = exportDescriptor.getParameter(EXPORT_ALBUM);
      exportAlbumMedia(albumDetail, mediaResolution, tempExportDir);
      createZipFile(tempExportDir, exportReport);
    } catch (Exception e) {
      throw new ExportException(e);
    }
  }

  public void exportPhysicalMedias(ImportExportDescriptor exportDescriptor, List<Media> medias,
      ExportReport exportReport) throws ExportException {
    try {
      UserDetail userDetail = exportDescriptor.getParameter(EXPORT_FOR_USER);
      File tempExportDir = createExportDir(userDetail);
      MediaResolution mediaResolution = exportDescriptor.getParameter(EXPORT_RESOLUTION);
      // Retrieve all picture from current album and sub albums
      prepareZipContent(medias, tempExportDir, mediaResolution);
      createZipFile(tempExportDir, exportReport);
    } catch (Exception e) {
      throw new ExportException(e);
    }
  }

  /**
   * Copy media of the given resolution inside temporary given directory
   * @param medias the list of medias to export
   * @param tempExportDir the temporary exported directory
   * @param mediaResolution the exported media resolution
   * @throws IOException
   */
  private void prepareZipContent(List<Media> medias, File tempExportDir,
      MediaResolution mediaResolution) throws IOException {
    for (Media media : medias) {
      if (!media.getType().isStreaming()) {
        File mediaFile = media.getFile(mediaResolution);
        FileUtil.copyFile(mediaFile, new File(tempExportDir, mediaFile.getName()));
      }
    }
  }

  private void exportAlbumMedia(AlbumDetail albumDetail, MediaResolution mediaResolution,
      File tempExportDir) throws IOException {
    List<Media> medias = albumDetail.getMedia();
    prepareZipContent(medias, tempExportDir, mediaResolution);
    if (albumDetail.getChildrenNumber() > 0) {
      Collection<AlbumDetail> subAlbums = albumDetail.getChildrenAlbumsDetails();
      for (AlbumDetail subAlbum : subAlbums) {
        subAlbum = getMediaService().getAlbum(subAlbum.getNodePK());
        File tempSubAlbum = new File(tempExportDir, subAlbum.getName());
        FileFolderManager.createFolder(tempSubAlbum);
        exportAlbumMedia(subAlbum, mediaResolution, tempSubAlbum);
      }
    }
  }
}
