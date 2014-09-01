/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.silverpeas.export.ExportException;
import com.silverpeas.export.ImportExportDescriptor;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.importExport.control.AbstractExportProcess;
import com.silverpeas.importExport.report.ExportReport;
import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;

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

  private static GalleryBm getMediaService() {
    return MediaServiceFactory.getMediaService();
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

  public void exportPhotos(ImportExportDescriptor exportDescriptor, List<Media> medias,
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
   * Copy media of the given resolution inside given directory
   * @param medias the list of medias to export
   * @param tempExportDir the temporary exported directory
   * @param mediaResolution the exported media resolution
   * @throws IOException
   */
  private void prepareZipContent(List<Media> medias, File tempExportDir,
      MediaResolution mediaResolution) throws IOException {
    for (Media media : medias) {
      if (media.getType().isPhoto()) {
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
        exportAlbumMedia(subAlbum, mediaResolution, tempSubAlbum);
      }
    }
  }
}
