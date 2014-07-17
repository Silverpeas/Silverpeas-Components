/*
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
package com.silverpeas.gallery.process;

import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.delegate.MediaDataCreateDelegate;
import com.silverpeas.gallery.delegate.MediaDataUpdateDelegate;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaCriteria;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Sound;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.gallery.process.media.*;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.process.util.ProcessList;

import java.io.File;
import java.util.Collection;
import java.util.Date;

/**
 * @author Yohann Chastagnier
 */
public class GalleryProcessManagement {

  private final UserDetail user;
  private final String componentInstanceId;
  private final ProcessList<GalleryProcessExecutionContext> processList;

  /**
   * Default constructor
   */
  public GalleryProcessManagement(final UserDetail user, final String componentInstanceId) {
    this.user = user;
    this.componentInstanceId = componentInstanceId;
    processList = new ProcessList<GalleryProcessExecutionContext>();
  }

  /*
   * Executor
   */

  /**
   * Execute the transactional processing
   * @throws Exception
   */
  public void execute() throws Exception {
    getGalleryBm().executeProcessList(processList,
        new GalleryProcessExecutionContext(user, componentInstanceId));
  }

  /*
   * Media
   */

  /**
   * Adds processes to create the given media
   * @param media
   * @param albumId
   * @param file
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @param delegate
   */
  public void addCreateMediaProcesses(final Media media, final String albumId, final Object file,
      final boolean watermark, final String watermarkHD, final String watermarkOther,
      final MediaDataCreateDelegate delegate) {
    processList.add(GalleryCreateMediaDataProcess.getInstance(media, albumId, delegate));
    processList.add(GalleryCreateMediaFileProcess
        .getInstance(media, file, watermark, watermarkHD, watermarkOther));
    processList.add(GalleryUpdateMediaDataProcess.getInstance(media));
    processList.add(GalleryIndexMediaDataProcess.getInstance(media));
  }

  /**
   * Adds processes to update the given media
   * @param media
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @param delegate
   */
  public void addUpdateMediaProcesses(final Media media, final boolean watermark,
      final String watermarkHD, final String watermarkOther,
      final MediaDataUpdateDelegate delegate) {
    processList.add(GalleryUpdateMediaDataProcess.getInstance(media, delegate));
    final FileItem fileItem = delegate.getFileItem();
    if (fileItem != null && StringUtil.isDefined(fileItem.getName())) {
      processList.add(GalleryUpdateMediaFileProcess
          .getInstance(media, fileItem, watermark, watermarkHD, watermarkOther));
      processList.add(GalleryUpdateMediaDataProcess.getInstance(media));
    }
    processList.add(GalleryIndexMediaDataProcess.getInstance(media));
  }

  /**
   * Adds processes to index the given media
   * @param media
   */
  public void addIndexMediaProcesses(final Media media) {
    processList.add(GalleryIndexMediaDataProcess.getInstance(media));
  }

  /**
   * Adds processes to delete the given media
   * @param media
   */
  public void addDeleteMediaProcesses(final Media media) {
    processList.add(GalleryDeleteMediaDataProcess.getInstance(media));
    processList.add(GalleryDeleteMediaFileProcess.getInstance(media));
    processList.add(GalleryDeindexMediaDataProcess.getInstance(media));
  }

  /**
   * Adds processes to paste the given media to the given album
   * @param mediaToPaste
   * @param toAlbum
   * @param isCutted
   */
  public void addPasteMediaProcesses(final Media mediaToPaste, final NodePK toAlbum,
      final boolean isCutted) {
    final MediaPK fromMediaPk = new MediaPK(mediaToPaste.getId(), mediaToPaste.getInstanceId());
    processList.add(GalleryPasteMediaDataProcess
        .getInstance(mediaToPaste, toAlbum.getId(), fromMediaPk, isCutted));
    processList.add(GalleryPasteMediaFileProcess.getInstance(mediaToPaste, fromMediaPk, isCutted));
    if (isCutted) {
      processList.add(GalleryDeindexMediaDataProcess.getInstance(mediaToPaste));
      processList.add(GalleryIndexMediaDataProcess.getInstance(mediaToPaste));
    }
  }

  /*
   * Album
   */

  /**
   * Recursive method to add processes to create albums from a file repository
   * @param repository
   * @param albumId
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @param delegate
   * @throws Exception
   */
  public void addImportFromRepositoryProcesses(final File repository, final String albumId,
      final boolean watermark, final String watermarkHD, final String watermarkOther,
      final MediaDataCreateDelegate delegate) throws Exception {
    final File[] fileList = repository.listFiles();
    if (fileList != null) {
      for (final File file : fileList) {
        if (file.isFile()) {
          MediaMimeType mediaMimeType = MediaMimeType.fromFile(file);
          Media newMedia = null;
          if (mediaMimeType.isSupportedPhotoType()) {
            newMedia = new Photo();
          } else if (mediaMimeType.isSupportedVideoType()) {
            newMedia = new Video();
          } else if (mediaMimeType.isSupportedSoundType()) {
            newMedia = new Sound();
          }
          if (newMedia != null) {
            // Creation of the media
            addCreateMediaProcesses(newMedia, albumId, file, watermark, watermarkHD, watermarkOther,
                delegate);
          }
        } else if (file.isDirectory()) {
          addImportFromRepositoryProcesses(file,
              createAlbum(file.getName(), albumId).getNodePK().getId(), watermark, watermarkHD,
              watermarkOther, delegate);
        }
      }
    }
  }

  /**
   * Centralized method to create an album
   * @param name
   * @param albumId
   * @return
   * @throws Exception
   */
  private AlbumDetail createAlbum(final String name, final String albumId) throws Exception {
    final AlbumDetail newAlbum =
        new AlbumDetail(new NodeDetail("unknown", name, null, null, null, null, "0", "unknown"));
    newAlbum.setCreationDate(DateUtil.date2SQLDate(new Date()));
    newAlbum.setCreatorId(user.getId());
    newAlbum.getNodePK().setComponentName(componentInstanceId);
    newAlbum
        .setNodePK(getGalleryBm().createAlbum(newAlbum, new NodePK(albumId, componentInstanceId)));
    return newAlbum;
  }

  /**
   * Recursive method to add media paste processes for given albums (because of sub album)
   * @param fromAlbum
   * @param toAlbum
   * @param isCutted
   * @throws Exception
   */
  public void addPasteAlbumProcesses(final AlbumDetail fromAlbum, final AlbumDetail toAlbum,
      final boolean isCutted) throws Exception {

    // Check if node can be copied or not (parent or same object)
    boolean pasteAllowed = !fromAlbum.equals(toAlbum) && !fromAlbum.isFatherOf(toAlbum);
    if (!pasteAllowed) {
      return;
    }

    if (isCutted) {

      // CUT & PASTE

      // Move images
      NodePK toSubAlbumPK;
      for (final NodeDetail subAlbumToPaste : getNodeBm().getSubTree(fromAlbum.getNodePK())) {
        toSubAlbumPK = new NodePK(subAlbumToPaste.getNodePK().getId(), componentInstanceId);
        addPasteMediaAlbumProcesses(subAlbumToPaste.getNodePK(), toSubAlbumPK, true);
      }

      // Move album
      getNodeBm().moveNode(fromAlbum.getNodePK(), toAlbum.getNodePK());

    } else {

      // COPY & PASTE

      // Create new album
      final AlbumDetail newAlbum = new AlbumDetail(new NodeDetail());
      final NodePK newAlbumPK = new NodePK("unknown", componentInstanceId);
      newAlbum.setNodePK(newAlbumPK);
      newAlbum.setCreatorId(user.getId());
      newAlbum.setName(fromAlbum.getName());
      newAlbum.setDescription(fromAlbum.getDescription());
      newAlbum.setTranslations(fromAlbum.getTranslations());
      newAlbum.setCreationDate(fromAlbum.getCreationDate());
      newAlbum.setRightsDependsOn(toAlbum.getRightsDependsOn());

      // Persisting the new album
      getNodeBm().createNode(newAlbum, toAlbum);

      // Paste images of album
      addPasteMediaAlbumProcesses(fromAlbum.getNodePK(), newAlbum.getNodePK(), false);

      // Perform sub albums
      for (final NodeDetail subNode : getNodeBm().getChildrenDetails(fromAlbum.getNodePK())) {
        addPasteAlbumProcesses(new AlbumDetail(subNode), newAlbum, false);
      }
    }
  }

  /**
   * Adds processes to paste an album to an other one
   * @param fromAlbumPk
   * @param toAlbumPk
   * @param isCutted
   * @throws Exception
   */
  private void addPasteMediaAlbumProcesses(final NodePK fromAlbumPk, final NodePK toAlbumPk,
      final boolean isCutted) throws Exception {
    for (final Media media : getGalleryBm()
        .getAllMedia(fromAlbumPk, MediaCriteria.VISIBILITY.FORCE_GET_ALL)) {
      addPasteMediaProcesses(media, toAlbumPk, isCutted);
    }
  }

  /**
   * Recursive method to add media delete processes for the given album (because of sub album)
   * @param albumPk
   * @throws Exception
   */
  public void addDeleteAlbumProcesses(final NodePK albumPk) throws Exception {
    addDeleteMediaAlbumProcesses(albumPk);
    final Collection<NodeDetail> childrens = getNodeBm().getChildrenDetails(albumPk);
    for (final NodeDetail node : childrens) {
      addDeleteAlbumProcesses(node.getNodePK());
    }
    getNodeBm().removeNode(albumPk);
  }

  /**
   * Adds processes to delete all media from the given album
   * @param albumPk
   * @throws Exception
   */
  private void addDeleteMediaAlbumProcesses(final NodePK albumPk) throws Exception {
    for (final Media media : getGalleryBm()
        .getAllMedia(albumPk, MediaCriteria.VISIBILITY.FORCE_GET_ALL)) {
      Collection<String> albumIds = getGalleryBm().getAlbumIdsOf(media);
      if (albumIds.size() > 1) {
        // the image is in several albums
        // delete only the link between it and album to delete
        albumIds.remove(albumPk.getId());
        media.setToAlbums(albumIds.toArray(new String[albumIds.size()]));
      } else {
        addDeleteMediaProcesses(media);
      }
    }
  }

  /*
   * Tools
   */

  /**
   * Gets the GalleryBm EJB proxy
   * @return
   */
  private static GalleryBm getGalleryBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryProcessBuilder.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Gets the NodeBm EJB proxy
   * @return
   */
  private static NodeBm getNodeBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryProcessBuilder.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}
