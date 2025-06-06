/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.process;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.gallery.Watermark;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataUpdateDelegate;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.model.Sound;
import org.silverpeas.components.gallery.model.Video;
import org.silverpeas.components.gallery.process.media.*;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.util.ProcessList;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import static org.silverpeas.components.gallery.GalleryComponentSettings.getWatermark;

/**
 * @author Yohann Chastagnier
 */
public class GalleryProcessManagement {

  private static final String UNKNOWN = "unknown";
  private final UserDetail user;
  private final String componentInstanceId;
  private final ProcessList<ProcessExecutionContext> processList;

  /**
   * Default constructor
   */
  public GalleryProcessManagement(final UserDetail user, final String componentInstanceId) {
    this.user = user;
    this.componentInstanceId = componentInstanceId;
    processList = new ProcessList<>();
  }

  /*
   * Executor
   */

  /**
   * Execute the transactional processing
   */
  public void execute() {
    Transaction.performInOne(() -> {
      try {
        ProcessProvider.getProcessManagement()
            .execute(processList, new ProcessExecutionContext(user, componentInstanceId));
        return null;
      } catch (final Exception e) {
        throw new GalleryRuntimeException(e);
      }
    });
  }

  /*
   * Media
   */

  public void addCreateMediaProcesses(final Media media, final String albumId, final Object file,
      final Watermark watermark, final MediaDataCreateDelegate delegate) {
    processList.add(GalleryCreateMediaDataProcess.getInstance(media, albumId, delegate));
    processList.add(GalleryCreateMediaFileProcess.getInstance(media, file, watermark));
    processList.add(GalleryUpdateMediaDataProcess.getInstance(media));
    processList.add(GalleryIndexMediaDataProcess.getInstance(media));
  }

  public void addUpdateMediaProcesses(final Media media, final Watermark watermark,
      final MediaDataUpdateDelegate delegate) {
    processList.add(GalleryUpdateMediaDataProcess.getInstance(media, delegate));
    final FileItem fileItem = delegate.getFileItem();
    if (fileItem != null && StringUtil.isDefined(fileItem.getName())) {
      processList.add(GalleryUpdateMediaFileProcess
          .getInstance(media, fileItem, watermark));
      processList.add(GalleryUpdateMediaDataProcess.getInstance(media));
    }
    processList.add(GalleryIndexMediaDataProcess.getInstance(media));
  }

  /**
   * Adds processes to index the given media
   * @param media the media to index
   */
  public void addIndexMediaProcesses(final Media media) {
    processList.add(GalleryIndexMediaDataProcess.getInstance(media));
  }

  /**
   * Adds processes to delete the given media
   * @param media the media to delete
   */
  public void addDeleteMediaProcesses(final Media media) {
    processList.add(GalleryDeleteMediaDataProcess.getInstance(media));
    processList.add(GalleryDeleteMediaFileProcess.getInstance(media));
    processList.add(GalleryDeindexMediaDataProcess.getInstance(media));
  }

  public void addPasteMediaProcesses(final Media mediaToPaste, final NodePK toAlbum,
      final boolean isCut) {
    final MediaPK fromMediaPk = new MediaPK(mediaToPaste.getId(), mediaToPaste.getInstanceId());
    processList.add(GalleryPasteMediaDataProcess
        .getInstance(mediaToPaste, toAlbum.getId(), fromMediaPk, isCut));
    processList.add(GalleryPasteMediaFileProcess.getInstance(mediaToPaste, fromMediaPk, isCut));
    if (isCut) {
      processList.add(GalleryDeindexMediaDataProcess.getInstance(mediaToPaste));
      processList.add(GalleryIndexMediaDataProcess.getInstance(mediaToPaste));
    }
  }

  /*
   * Album
   */

  /**
   * Recursive method to add processes to create albums from a file repository.
   * This method performs a transaction between each file to save.<br>
   * It could happen, in the very particular case of space memory quota exception, that an album
   * is created with no media inside.
   * @param user the user invoking the import.
   * @param componentInstanceId the unique identifier of a Gallery instance.
   * @param repository the repository in the filesystem into which the album will be imported.
   * @param albumId the unique identifier of the father album.
   * @param delegate media data
   */
  public static void importFromRepositoryProcesses(final UserDetail user,
      final String componentInstanceId, final File repository, final String albumId,
      final MediaDataCreateDelegate delegate) {

    final Watermark watermark = getWatermark(componentInstanceId);

    final File[] fileList = repository.listFiles();
    if (fileList != null) {
      for (final File file : fileList) {
        if (file.isFile()) {
          MediaMimeType mediaMimeType = MediaMimeType.fromFile(file);
          Media newMedia = getMediaByType(mediaMimeType);
          if (newMedia != null) {
            // Creation of the media
            // In a transaction.
            final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
                componentInstanceId);
            processManagement.addCreateMediaProcesses(newMedia, albumId, file, watermark, delegate);
            processManagement.execute();
          }
        } else if (file.isDirectory()) {
          final AlbumDetail newAlbum = GalleryProcessManagement
              .createAlbum(user, componentInstanceId, file.getName(), albumId);
          importFromRepositoryProcesses(user, componentInstanceId, file,
              newAlbum.getNodePK().getId(), delegate);
        }
      }
    }
  }

  private static Media getMediaByType(final MediaMimeType mediaMimeType) {
    Media newMedia;
    if (mediaMimeType.isSupportedPhotoType()) {
      newMedia = new Photo();
    } else if (mediaMimeType.isSupportedVideoType()) {
      newMedia = new Video();
    } else if (mediaMimeType.isSupportedSoundType()) {
      newMedia = new Sound();
    } else {
      newMedia = null;
    }
    return newMedia;
  }

  /**
   * Centralized method to create an album
   * @param name the album name
   * @param componentInstanceId the identifier of component instance
   * @param albumId an album identifier
   * @return an AlbumDetail
   */
  private static AlbumDetail createAlbum(final UserDetail user, final String componentInstanceId,
      final String name, final String albumId) {
    final AlbumDetail newAlbum =
        new AlbumDetail(new NodeDetail(UNKNOWN, name, null, 0, UNKNOWN));
    newAlbum.setCreationDate(new Date());
    newAlbum.setCreatorId(user.getId());
    newAlbum.getNodePK().setComponentName(componentInstanceId);
    return getGalleryService().createAlbum(newAlbum, new NodePK(albumId, componentInstanceId));
  }

  /**
   * Recursive method to add media paste processes for given albums (because of sub album)
   * @param fromAlbum the album to paste.
   * @param toAlbum the destination album.
   * @param isCut is the album to paste has been cut or copied.
   */
  public void addPasteAlbumProcesses(final AlbumDetail fromAlbum, final AlbumDetail toAlbum,
      final boolean isCut) {

    // Check if node can be copied or not (parent or same object)
    boolean pasteAllowed = !fromAlbum.equals(toAlbum) && !fromAlbum.isFatherOf(toAlbum);
    if (!pasteAllowed) {
      return;
    }

    if (isCut) {

      // CUT & PASTE

      // Move images
      NodePK toSubAlbumPK;
      for (final NodeDetail subAlbumToPaste : getNodeService().getSubTree(fromAlbum.getNodePK())) {
        toSubAlbumPK = new NodePK(subAlbumToPaste.getNodePK().getId(), componentInstanceId);
        addPasteMediaAlbumProcesses(subAlbumToPaste.getNodePK(), toSubAlbumPK, true);
      }

      // Move album
      getNodeService().moveNode(fromAlbum.getNodePK(), toAlbum.getNodePK());

    } else {

      // COPY & PASTE

      // Create new album
      final AlbumDetail newAlbum = new AlbumDetail(new NodeDetail());
      final NodePK newAlbumPK = new NodePK(UNKNOWN, componentInstanceId);
      newAlbum.setNodePK(newAlbumPK);
      newAlbum.setCreatorId(user.getId());
      newAlbum.setName(fromAlbum.getName());
      newAlbum.setDescription(fromAlbum.getDescription());
      newAlbum.setTranslations(fromAlbum.getTranslations());
      newAlbum.setCreationDate(fromAlbum.getCreationDate());
      newAlbum.setRightsDependsOn(toAlbum.getRightsDependsOn());

      // Persisting the new album
      getNodeService().createNode(newAlbum, toAlbum);

      // Paste images of album
      addPasteMediaAlbumProcesses(fromAlbum.getNodePK(), newAlbum.getNodePK(), false);

      // Perform sub albums
      for (final NodeDetail subNode : getNodeService().getChildrenDetails(fromAlbum.getNodePK())) {
        addPasteAlbumProcesses(new AlbumDetail(subNode), newAlbum, false);
      }
    }
  }

  private void addPasteMediaAlbumProcesses(final NodePK fromAlbumPk, final NodePK toAlbumPk,
      final boolean isCut) {
    for (final Media media : getGalleryService()
        .getAllMedia(fromAlbumPk, MediaCriteria.VISIBILITY.FORCE_GET_ALL)) {
      addPasteMediaProcesses(media, toAlbumPk, isCut);
    }
  }

  /**
   * Recursive method to add media delete processes for the given album (because of sub album)
   * @param albumPk the unique identifier of an album.
   */
  public void addDeleteAlbumProcesses(final NodePK albumPk) {
    addDeleteMediaAlbumProcesses(albumPk);
    final Collection<NodeDetail> children = getNodeService().getChildrenDetails(albumPk);
    for (final NodeDetail node : children) {
      addDeleteAlbumProcesses(node.getNodePK());
    }
    getNodeService().deleteNode(albumPk);
  }

  /**
   * Adds processes to delete all media from the given album
   * @param albumPk the unique identifier of the album.
   */
  private void addDeleteMediaAlbumProcesses(final NodePK albumPk) {
    for (final Media media : getGalleryService()
        .getAllMedia(albumPk, MediaCriteria.VISIBILITY.FORCE_GET_ALL)) {
      Collection<String> albumIds = getGalleryService().getAlbumIdsOf(media);
      if (albumIds.size() > 1) {
        // the image is in several albums
        // delete only the link between it and album to delete
        albumIds.remove(albumPk.getId());
        media.setToAlbums(albumIds.toArray(new String[0]));
      } else {
        addDeleteMediaProcesses(media);
      }
    }
  }

  private static GalleryService getGalleryService() {
      return ServiceProvider.getService(GalleryService.class);
  }

  private static NodeService getNodeService() {
      return NodeService.get();
  }
}
