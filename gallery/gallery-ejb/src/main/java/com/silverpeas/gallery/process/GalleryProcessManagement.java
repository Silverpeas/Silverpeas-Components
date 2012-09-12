/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.io.File;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.process.util.ProcessList;

import com.silverpeas.gallery.ImageType;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.delegate.PhotoDataCreateDelegate;
import com.silverpeas.gallery.delegate.PhotoDataUpdateDelegate;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.process.photo.GalleryCreatePhotoDataProcess;
import com.silverpeas.gallery.process.photo.GalleryCreatePhotoFileProcess;
import com.silverpeas.gallery.process.photo.GalleryDeindexPhotoDataProcess;
import com.silverpeas.gallery.process.photo.GalleryDeletePhotoDataProcess;
import com.silverpeas.gallery.process.photo.GalleryDeletePhotoFileProcess;
import com.silverpeas.gallery.process.photo.GalleryIndexPhotoDataProcess;
import com.silverpeas.gallery.process.photo.GalleryPastePhotoDataProcess;
import com.silverpeas.gallery.process.photo.GalleryPastePhotoFileProcess;
import com.silverpeas.gallery.process.photo.GalleryUpdatePhotoDataProcess;
import com.silverpeas.gallery.process.photo.GalleryUpdatePhotoFileProcess;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * @author Yohann Chastagnier
 */
public class GalleryProcessManagement {

  private final UserDetail user;
  private final String componentInstanceId;
  private final ProcessList<GalleryProcessExecutionContext> processList =
      new ProcessList<GalleryProcessExecutionContext>();

  /**
   * Default constructor
   */
  public GalleryProcessManagement(final UserDetail user, final String componentInstanceId) {
    this.user = user;
    this.componentInstanceId = componentInstanceId;
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
   * Photo
   */

  /**
   * Adds processes to create the given photo
   * @param photo
   * @param albumId
   * @param file
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @param delegate
   */
  public void addCreatePhotoProcesses(final PhotoDetail photo, final String albumId,
      final Object file, final boolean watermark, final String watermarkHD,
      final String watermarkOther, final PhotoDataCreateDelegate delegate) {
    processList.add(GalleryCreatePhotoDataProcess.getInstance(photo, albumId, delegate));
    processList.add(GalleryCreatePhotoFileProcess.getInstance(photo, file, watermark, watermarkHD,
        watermarkOther));
    processList.add(GalleryIndexPhotoDataProcess.getInstance(photo));
  }

  /**
   * Adds processes to update the given photo
   * @param photo
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @param delegate
   */
  public void addUpdatePhotoProcesses(final PhotoDetail photo, final boolean watermark,
      final String watermarkHD, final String watermarkOther, final PhotoDataUpdateDelegate delegate) {
    processList.add(GalleryUpdatePhotoDataProcess.getInstance(photo, delegate));
    final FileItem fileItem = delegate.getFileItem();
    if (fileItem != null) {
      processList.add(GalleryUpdatePhotoFileProcess.getInstance(photo, fileItem, watermark,
          watermarkHD, watermarkOther));
    }
    processList.add(GalleryIndexPhotoDataProcess.getInstance(photo));
  }

  /**
   * Adds processes to delete the given photo
   * @param photo
   */
  public void addDeletePhotoProcesses(final PhotoDetail photo) {
    processList.add(GalleryDeletePhotoDataProcess.getInstance(photo));
    processList.add(GalleryDeletePhotoFileProcess.getInstance(photo));
    processList.add(GalleryDeindexPhotoDataProcess.getInstance(photo));
  }

  /**
   * Adds processes to paste the given photo to the given album
   * @param photoToPaste
   * @param toAlbum
   * @param isCutted
   */
  public void addPastePhotoProcesses(final PhotoDetail photoToPaste, final NodePK toAlbum,
      final boolean isCutted) {
    final PhotoPK fromPhotoPk = new PhotoPK(photoToPaste.getId(), photoToPaste.getInstanceId());
    processList.add(GalleryPastePhotoDataProcess.getInstance(photoToPaste, toAlbum.getId(),
        fromPhotoPk, isCutted));
    processList.add(GalleryPastePhotoFileProcess.getInstance(photoToPaste, fromPhotoPk, isCutted));
    if (isCutted) {
      processList.add(GalleryDeindexPhotoDataProcess.getInstance(photoToPaste));
      processList.add(GalleryIndexPhotoDataProcess.getInstance(photoToPaste));
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
      final PhotoDataCreateDelegate delegate) throws Exception {
    for (final File file : repository.listFiles()) {
      if (file.isFile()) {
        if (ImageType.isImage(file.getName())) {
          // création de la photo
          addCreatePhotoProcesses(new PhotoDetail(), albumId, file, watermark, watermarkHD,
              watermarkOther, delegate);
        }
      } else if (file.isDirectory()) {
        addImportFromRepositoryProcesses(file, createAlbum(file.getName(), albumId).getNodePK()
            .getId(), watermark, watermarkHD, watermarkOther, delegate);
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
    newAlbum.setNodePK(getGalleryBm().createAlbum(newAlbum,
        new NodePK(albumId, componentInstanceId)));
    return newAlbum;
  }

  /**
   * Recursive method to add photo paste processes for given albums (because of sub album)
   * @param fromAlbum
   * @param toAlbum
   * @param isCutted
   * @throws Exception
   */
  public void addPasteAlbumProcesses(final AlbumDetail fromAlbum, final AlbumDetail toAlbum,
      final boolean isCutted) throws Exception {

    if (isCutted) {

      // CUT & PASTE

      // Move images
      NodePK toSubAlbumPK = null;
      for (final NodeDetail subAlbumToPaste : getNodeBm().getSubTree(fromAlbum.getNodePK())) {
        toSubAlbumPK = new NodePK(subAlbumToPaste.getNodePK().getId(), componentInstanceId);
        addPastePhotoAlbumProcesses(subAlbumToPaste.getNodePK(), toSubAlbumPK, isCutted);
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
      addPastePhotoAlbumProcesses(fromAlbum.getNodePK(), newAlbum.getNodePK(), isCutted);

      // Perform sub albums
      for (final NodeDetail subNode : getNodeBm().getChildrenDetails(fromAlbum.getNodePK())) {
        addPasteAlbumProcesses(new AlbumDetail(subNode), newAlbum, isCutted);
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
  private void addPastePhotoAlbumProcesses(final NodePK fromAlbumPk, final NodePK toAlbumPk,
      final boolean isCutted) throws Exception {
    for (final PhotoDetail photo : getGalleryBm().getAllPhoto(fromAlbumPk, true)) {
      addPastePhotoProcesses(photo, toAlbumPk, isCutted);
    }
  }

  /**
   * Recursive method to add photo delete processes for the given album (because of sub album)
   * @param albumPk
   * @throws Exception
   */
  public void addDeleteAlbumProcesses(final NodePK albumPk) throws Exception {
    addDeletePhotoAlbumProcesses(albumPk);
    final Collection<NodeDetail> childrens = getNodeBm().getChildrenDetails(albumPk);
    for (final NodeDetail node : childrens) {
      addDeleteAlbumProcesses(node.getNodePK());
    }
    getNodeBm().removeNode(albumPk);
  }

  /**
   * Adds processes to delete all photos from the given album
   * @param albumPk
   * @throws Exception
   */
  private void addDeletePhotoAlbumProcesses(final NodePK albumPk) throws Exception {
    for (final PhotoDetail photo : getGalleryBm().getAllPhoto(albumPk, true)) {
      addDeletePhotoProcesses(photo);
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
      return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class)
          .create();
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
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class).create();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryProcessBuilder.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}
