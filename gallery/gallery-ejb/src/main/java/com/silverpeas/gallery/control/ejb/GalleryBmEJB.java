/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.gallery.control.ejb;

import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.GalleryContentManager;
import com.silverpeas.gallery.dao.OrderDAO;
import com.silverpeas.gallery.dao.PhotoDAO;
import com.silverpeas.gallery.delegate.GalleryPasteDelegate;
import com.silverpeas.gallery.delegate.PhotoDataCreateDelegate;
import com.silverpeas.gallery.delegate.PhotoDataUpdateDelegate;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.gallery.process.GalleryProcessManagement;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.control.dao.NodeDAO;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import org.silverpeas.process.ProcessFactory;
import org.silverpeas.process.util.ProcessList;
import org.silverpeas.search.SearchEngineFactory;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.JNDINames.PUBLICATIONBM_EJBHOME;

/**
 * @author
 */
public class GalleryBmEJB implements SessionBean, GalleryBmBusinessSkeleton {

  private static final long serialVersionUID = 8148021767416025104L;
  private final OrderDAO orderDao = new OrderDAO();

  @Override
  public AlbumDetail getAlbum(final NodePK nodePK, final boolean viewAllPhoto) {
    try {
      final AlbumDetail album = new AlbumDetail(getNodeBm().getDetail(nodePK));
      // récupération des photos
      final Collection<PhotoDetail> photos = getAllPhoto(nodePK, viewAllPhoto);
      // ajout des photos à l'album
      album.setPhotos(photos);
      return album;
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_EXIST", e);
    }
  }

  @Override
  public Collection<AlbumDetail> getAllAlbums(final String instanceId) {
    try {
      final NodePK nodePK = new NodePK("0", instanceId);
      final Collection<NodeDetail> nodes = getNodeBm().getSubTree(nodePK);
      final List<AlbumDetail> albums = new ArrayList<AlbumDetail>(nodes.size());
      for (final NodeDetail node : nodes) {
        albums.add(new AlbumDetail(node));
      }
      return albums;
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_EXIST", e);
    }
  }

  @Override
  public NodePK createAlbum(final AlbumDetail album, final NodePK nodePK) {
    try {
      final AlbumDetail currentAlbum = getAlbum(nodePK, true);
      return getNodeBm().createNode(album, currentAlbum);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_CREATE", e);
    }
  }

  @Override
  public void updateAlbum(final AlbumDetail album) {
    try {
      getNodeBm().setDetail(album);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updateAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_UPDATE", e);
    }
  }

  @Override
  public void deleteAlbum(final UserDetail user, final String componentInstanceId,
      final NodePK nodePK) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      processManagement.addDeleteAlbumProcesses(nodePK);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.deleteAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_DELETE", e);
    }
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      final NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getNodeBM()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return nodeBm;
  }

  @Override
  public PhotoDetail getPhoto(final PhotoPK photoPK) {
    final Connection con = initCon();
    try {
      return getPhoto(con, photoPK);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private PhotoDetail getPhoto(final Connection con, final PhotoPK photoPK) throws Exception {
    final int photoId = Integer.parseInt(photoPK.getId());
    return PhotoDAO.getPhoto(con, photoId);
  }

  @Override
  public Collection<PhotoDetail> getAllPhotos(final String instanceId) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getAllPhotos(con, instanceId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotos()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PhotoDetail> getAllPhoto(final NodePK nodePK, final boolean viewAllPhoto) {
    final Connection con = initCon();
    try {
      final String albumId = nodePK.getId();
      final String instanceId = nodePK.getInstanceId();
      return PhotoDAO.getAllPhoto(con, albumId, instanceId, viewAllPhoto);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PhotoDetail> getAllPhotosSorted(final NodePK nodePK,
      final HashMap<String, String> parsedParameters, final boolean viewAllPhoto) {
    final Connection con = initCon();
    try {
      final String albumId = nodePK.getId();
      final String instanceId = nodePK.getInstanceId();
      return PhotoDAO.getAllPhotosSorted(con, albumId, instanceId, parsedParameters, viewAllPhoto);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PhotoDetail> getNotVisible(final String instanceId) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getPhotoNotVisible(con, instanceId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getNotVisible()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void paste(final UserDetail user, final String componentInstanceId,
      final GalleryPasteDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement = new GalleryProcessManagement(user,
          componentInstanceId);
      // Photos
      for (final Map.Entry<PhotoDetail, Boolean> photoToPaste : delegate.getPhotosToPaste()
          .entrySet()) {
        processManagement.addPastePhotoProcesses(getPhoto(photoToPaste.getKey().getPhotoPK()),
            delegate.getAlbum().getNodePK(), photoToPaste.getValue());
      }
      // Albums
      for (final Map.Entry<AlbumDetail, Boolean> albumToPaste : delegate.getAlbumsToPaste()
          .entrySet()) {
        processManagement.addPasteAlbumProcesses(albumToPaste.getKey(), delegate.getAlbum(),
            albumToPaste.getValue());
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBm.paste()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_PASTE_ERROR", e);
    }
  }

  @Override
  public void importFromRepository(final UserDetail user, final String componentInstanceId,
      final File repository, final boolean watermark, final String watermarkHD,
      final String watermarkOther, final PhotoDataCreateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      processManagement.addImportFromRepositoryProcesses(repository, delegate.getAlbumId(),
          watermark, watermarkHD, watermarkOther, delegate);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBm.importFromRepository()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTOS_NOT_IMPORTED", e);
    }
  }

  @Override
  public void createPhoto(final UserDetail user, final String componentInstanceId,
      final PhotoDetail photo, final boolean watermark, final String watermarkHD,
      final String watermarkOther, final PhotoDataCreateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      processManagement.addCreatePhotoProcesses(photo, delegate.getAlbumId(),
          delegate.getFileItem(), watermark, watermarkHD, watermarkOther, delegate);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBm.createPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_CREATE", e);
    }
  }

  @Override
  public void updatePhoto(final UserDetail user, final String componentInstanceId,
      final Collection<String> photoIds, final String albumId,
      final PhotoDataUpdateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      for (final String photoId : photoIds) {
        processManagement.addUpdatePhotoProcesses(
            getPhoto(new PhotoPK(photoId, componentInstanceId)), false, null, null, delegate);
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBm.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_UPDATE", e);
    }
  }

  @Override
  public void updatePhoto(final UserDetail user, final String componentInstanceId,
      final PhotoDetail photo, final boolean watermark, final String watermarkHD,
      final String watermarkOther, final PhotoDataUpdateDelegate delegate) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      processManagement.addUpdatePhotoProcesses(photo, watermark, watermarkHD, watermarkOther,
          delegate);
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBm.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_UPDATE", e);
    }
  }

  @Override
  public void deletePhoto(final UserDetail user, final String componentInstanceId,
      final Collection<String> photoIds) {
    try {
      final GalleryProcessManagement processManagement =
          new GalleryProcessManagement(user, componentInstanceId);
      PhotoDetail photo;
      for (final String photoId : photoIds) {
        photo = new PhotoDetail();
        photo.setPhotoPK(new PhotoPK(photoId, componentInstanceId));
        processManagement.addDeletePhotoProcesses(photo);
      }
      processManagement.execute();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBm.deletePhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_DELETE", e);
    }
  }

  @Override
  public Collection<PhotoDetail> getDernieres(final String instanceId, final boolean viewAllPhoto) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getDernieres(con, instanceId, viewAllPhoto);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection initCon() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (final UtilException e) {
      // traitement des exceptions

      throw new GalleryRuntimeException("GalleryBmEJB.initCon()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  @Override
  public Collection<NodeDetail> getPath(final NodePK nodePK) {
    Collection<NodeDetail> path;
    try {
      path = getNodeBm().getPath(nodePK);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getPath()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_PATH", e);
    }
    return path;
  }

  @Override
  public Collection<String> getPathList(final String instanceId, final String photoId) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getPathList(con, instanceId, photoId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getPathList()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PATH", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void setPhotoPath(final String photoId, final String instanceId, final String... albums) {
    final Connection con = initCon();
    try {
      SilverTrace.debug("gallery", "GalleryBmEJB.setPhotoPath()", "root.MSG_GEN_PARAM_VALUE",
          "photoId = " + photoId);
      PhotoDAO.deletePhotoPath(con, photoId, instanceId);
      for (final String albumId : albums) {
        SilverTrace.debug("gallery", "GalleryBmEJB.setPhotoPath()", "root.MSG_GEN_PARAM_VALUE",
            "albumId = " + albumId);
        PhotoDAO.addPhotoPath(con, photoId, albumId, instanceId);
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.setPhotoPath()",
          SilverpeasRuntimeException.ERROR, "gallery.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addPhotoPaths(final String photoId, final String instanceId, final String... albums) {
    final Connection con = initCon();
    try {
      SilverTrace.debug("gallery", "GalleryBmEJB.setPhotoPath()", "root.MSG_GEN_PARAM_VALUE",
          "photoId = " + photoId);
      final Collection<String> paths = PhotoDAO.getPathList(con, instanceId, photoId);
      for (final String albumId : albums) {
        if (!paths.contains(albumId)) {
          SilverTrace.debug("gallery", "GalleryBmEJB.setPhotoPath()", "root.MSG_GEN_PARAM_VALUE",
              "albumId = " + albumId);
          PhotoDAO.addPhotoPath(con, photoId, albumId, instanceId);
        }
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.setPhotoPath()",
          SilverpeasRuntimeException.ERROR, "gallery.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updatePhotoPath(final String photoId, final String instanceIdFrom,
      final String instanceIdTo, final String... albums) {
    final Connection con = initCon();
    try {
      SilverTrace.debug("gallery", "GalleryBmEJB.updatePhotoPath()", "root.MSG_GEN_PARAM_VALUE",
          "photoId = " + photoId);
      PhotoDAO.deletePhotoPath(con, photoId, instanceIdFrom);
      for (final String albumId : albums) {
        SilverTrace.debug("gallery", "GalleryBmEJB.addAlbumPath()", "root.MSG_GEN_PARAM_VALUE",
            "albumId = " + albumId);
        PhotoDAO.addPhotoPath(con, photoId, albumId, instanceIdTo);
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updatePhotoPath()",
          SilverpeasRuntimeException.ERROR, "gallery.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public String getHTMLNodePath(final NodePK nodePK) {
    String htmlPath = "";
    try {
      final List<NodeDetail> path = (List<NodeDetail>) getPath(nodePK);
      if (path.size() > 0) {
        path.remove(path.size() - 1);
      }
      htmlPath =
          getSpacesPath(nodePK.getInstanceId()) + getComponentLabel(nodePK.getInstanceId()) + " > "
          + displayPath(path, 10);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getHTMLNodePath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    return htmlPath;
  }

  private String getSpacesPath(final String componentId) {
    String spacesPath = "";
    final List<SpaceInst> spaces = getOrganizationController().getSpacePathToComponent(componentId);
    for (final SpaceInst spaceInst : spaces) {
      spacesPath += spaceInst.getName();
      spacesPath += " > ";
    }
    return spacesPath;
  }

  private String getComponentLabel(final String componentId) {
    final ComponentInstLight component =
        getOrganizationController().getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  private OrganizationController getOrganizationController() {
    return new OrganizationController();
  }

  private String displayPath(final Collection<NodeDetail> path, final int beforeAfter) {
    String pathString = "";
    final int nbItemInPath = path.size();
    final Iterator<NodeDetail> iterator = path.iterator();
    boolean alreadyCut = false;
    int nb = 0;

    NodeDetail nodeInPath = null;
    while (iterator.hasNext()) {
      nodeInPath = iterator.next();
      if ((nb <= beforeAfter) || (nb + beforeAfter >= nbItemInPath - 1)) {
        pathString = nodeInPath.getName() + " " + pathString;
        if (iterator.hasNext()) {
          pathString = " > " + pathString;
        }
      } else {
        if (!alreadyCut) {
          pathString += " ... > ";
          alreadyCut = true;
        }
      }
      nb++;
    }
    return pathString;
  }

  @Override
  public void indexGallery(final String instanceId) {
    // parcourir tous les albums
    final Collection<AlbumDetail> albums = getAllAlbums(instanceId);
    if (albums != null) {

      // pour chaque album, parcourir toutes les photos
      for (final AlbumDetail album : albums) {

        // indexation de l'album
        try {
          getNodeBm().createIndex(album);
        } catch (final Exception e) {
          throw new GalleryRuntimeException("GalleryBmEJB.indexGallery()",
              SilverpeasRuntimeException.ERROR, "gallery.MSG_INDEXALBUM", e);
        }

        final Collection<PhotoDetail> photos = getAllPhoto(album.getNodePK(), true);
        if (photos != null) {
          for (final PhotoDetail photo : photos) {
            // indéxation de la photo
            createIndex(photo);
          }
        }
      }
    }
  }

  @Override
  public void createIndex(final PhotoDetail photo) {
    SilverTrace.info("gallery", "GalleryBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
        "photoDetail = " + photo.toString());
    FullIndexEntry indexEntry = null;

    if (photo != null) {
      // Index the Photo
      indexEntry = new FullIndexEntry(photo.getPhotoPK().getComponentName(), "Photo",
          photo.getPhotoPK().getId());
      indexEntry.setTitle(photo.getTitle());
      indexEntry.setPreView(photo.getDescription());
      indexEntry.setCreationDate(photo.getCreationDate());
      indexEntry.setCreationUser(photo.getCreatorId());
      indexEntry.setKeyWords(photo.getKeyWord());
      if (photo.getBeginDate() != null) {
        indexEntry.setStartDate(DateUtil.date2SQLDate(photo.getBeginDate()));
      }
      if (photo.getEndDate() != null) {
        indexEntry.setEndDate(DateUtil.date2SQLDate(photo.getEndDate()));
      }

      if (photo.getImageName() != null) {
        final ResourceLocator gallerySettings =
            new ResourceLocator("org.silverpeas.gallery.settings.gallerySettings", "");
        indexEntry.setThumbnail(photo.getImageName());
        indexEntry.setThumbnailMimeType(photo.getImageMimeType());
        indexEntry.setThumbnailDirectory(gallerySettings.getString("imagesSubDirectory") + photo.
            getPhotoPK().getId());
      }

      // récupération des méta données pour les indéxer
      String metaDataStr = "";
      MetaData metaData;
      final Collection<String> properties = photo.getMetaDataProperties();
      for (final String property : properties) {
        metaData = photo.getMetaData(property);
        final String value = metaData.getValue();
        metaDataStr = metaDataStr + " " + value;
      }
      indexEntry.addTextContent(metaDataStr);
      SilverTrace.info("gallery", "GalleryBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
          "metaData = " + metaDataStr + " indexEntry = " + indexEntry.toString());
      // indexation des méta données (une donnée par champ d'index)
      for (final String property : properties) {
        metaData = photo.getMetaData(property);
        final String value = metaData.getValue();
        SilverTrace.info("gallery", "GalleryBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
            "property = " + property + " value = " + value);
        if (metaData.isDate()) {
          indexEntry.addField("IPTC_" + property, metaData.getDateValue());
        } else {
          indexEntry.addField("IPTC_" + property, value);
        }
      }

      // indexation du contenu du formulaire XML
      final String xmlFormName =
          getOrganizationController().getComponentParameterValue(photo.getInstanceId(),
          "XMLFormName");
      SilverTrace.info("gallery", "GalleryBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
          "xmlFormName = " + xmlFormName);
      if (StringUtil.isDefined(xmlFormName)) {
        final String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate;
        try {
          pubTemplate =
              PublicationTemplateManager.getInstance().getPublicationTemplate(
              photo.getInstanceId() + ":" + xmlFormShortName);
          final RecordSet set = pubTemplate.getRecordSet();
          set.indexRecord(photo.getPhotoPK().getId(), xmlFormShortName, indexEntry);
          SilverTrace.info("gallery", "GalleryBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
              "indexEntry = " + indexEntry.toString());
        } catch (final Exception e) {
          SilverTrace.info("gallery", "GalleryBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
              "xmlFormName = " + xmlFormName);
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  @Override
  public int getSilverObjectId(final PhotoPK photoPK) {
    SilverTrace.info("gallery", "GalleryBmEJB.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD",
        "photoId = " + photoPK.getId());
    int silverObjectId = -1;
    PhotoDetail photoDetail = null;
    try {
      silverObjectId =
          getGalleryContentManager().getSilverObjectId(photoPK.getId(), photoPK.getInstanceId());

      if (silverObjectId == -1) {
        photoDetail = getPhoto(photoPK);
        silverObjectId = createSilverContent(null, photoDetail, photoDetail.getCreatorId());
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(final Connection con, final PhotoDetail photoDetail,
      final String creatorId) {
    SilverTrace.info("gallery", "GalleryBmEJB.createSilverContent()", "root.MSG_GEN_ENTER_METHOD",
        "photoId = " + photoDetail.getPhotoPK().getId());
    try {
      return getGalleryContentManager().createSilverContent(con, photoDetail, creatorId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  @Override
  public Collection<PhotoDetail> search(final QueryDescription query) {
    final Collection<PhotoDetail> photos = new ArrayList<PhotoDetail>();
    try {
      final List<MatchingIndexEntry> result = SearchEngineFactory.getSearchEngine().search(query).
          getEntries();
      // création des photos à partir des resultats
      for (final MatchingIndexEntry matchIndex : result) {
        // Ne retourne que les photos
        if (matchIndex.getObjectType().equals("Photo")) {
          final PhotoPK photoPK = new PhotoPK(matchIndex.getObjectId());
          final PhotoDetail photo = getPhoto(photoPK);

          if (photo != null) {
            SilverTrace.info("gallery", "GalleryBmEJB.getResultSearch()",
                "root.MSG_GEN_ENTER_METHOD", "photo = " + photo.getPhotoPK().getId());
            photos.add(photo);
          }
        }
      }
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getResultSearch()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_OBJECT", e);
    }
    return photos;
  }

  @Override
  public Collection<PhotoDetail> getAllPhotoEndVisible(final int nbDays) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getAllPhotoEndVisible(con, nbDays);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotos()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public String createOrder(final Collection<String> basket, final String userId,
      final String componentId) {
    final Connection con = initCon();
    try {
      return orderDao.createOrder(con, basket, userId, componentId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_NOT_CREATE", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<Order> getAllOrders(final String userId, final String instanceId) {
    final Connection con = initCon();
    try {
      return orderDao.getAllOrders(con, userId, instanceId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllOrders()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_LIST_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateOrder(final Order order) {
    final Connection con = initCon();
    try {
      OrderDAO.updateOrder(con, order);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updateORder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_ORDER_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateOrderRow(final OrderRow row) {
    final Connection con = initCon();
    try {
      OrderDAO.updateOrderRow(con, row);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updateOrderRow()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_ORDER_ROW_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Order getOrder(final String orderId, final String instanceId) {
    final Connection con = initCon();
    try {
      return orderDao.getOrder(con, orderId, instanceId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Order> getAllOrderToDelete(final int nbDays) {
    final Connection con = initCon();
    try {
      return orderDao.getAllOrdersToDelete(con, nbDays);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteOrder(final String orderId) {
    final Connection con = initCon();
    try {
      orderDao.deleteOrder(con, orderId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.deleteOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Date getDownloadDate(final String orderId, final String photoId) {
    final Connection con = initCon();
    try {
      return OrderDAO.getDownloadDate(con, orderId, photoId);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.photoDateDownload()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      final PublicationBmHome publicationBmHome =
          EJBUtilitaire.getEJBObjectRef(PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (final Exception e) {
      throw new CommentRuntimeException("GallerySessionController.getPublicationBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return publicationBm;
  }

  private GalleryContentManager getGalleryContentManager() {
    return new GalleryContentManager();
  }

  public void ejbCreate() {
    // not implemented
  }

  @Override
  public void setSessionContext(final SessionContext context) {
    // not implemented
  }

  @Override
  public void ejbRemove() {
    // not implemented
  }

  @Override
  public void ejbActivate() {
    // not implemented
  }

  @Override
  public void ejbPassivate() {
    // not implemented
  }

  /**
   * @param userId ID of user
   * @see PhotoDetail
   * @return the list of photos that the user has created or updated
   * @throws SQLException, ParseException
   */
  public List<PhotoDetail> getAllPhotosbyUserid(final String userId) {
    final Connection con = initCon();
    try {
      final List<String> photoIds = PhotoDAO.getAllPhotosIDbyUserid(con, userId);
      final List<PhotoDetail> photos = new ArrayList<PhotoDetail>(photoIds.size());
      for (final String id : photoIds) {
        photos.add(PhotoDAO.getPhoto(con, Integer.parseInt(id)));
      }
      return photos;
    } catch (final ParseException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotobyUserid()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotobyUserid()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * get my list of SocialInformationGallery according to options and number of Item and the first
   * Index
   *
   * @return: List <SocialInformation>
   * @param : String myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  @Override
  public List<SocialInformation> getAllPhotosByUserid(final String userId, final Date begin,
      final Date end) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getAllPhotosIDbyUserid(con, userId, begin, end);
    } catch (final ParseException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotosUpdatebyUserid()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotosUpdatebyUserid()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * get list of SocialInformationGallery of my contacts according to options and number of Item and
   * the first Index
   *
   * @return: List <SocialInformation>
   * @param : String myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(
      final List<String> listOfuserId, final List<String> availableComponent, final Date begin,
      final Date end) {
    final Connection con = initCon();
    try {
      return PhotoDAO.getSocialInformationsListOfMyContacts(con, listOfuserId, availableComponent,
          begin, end);
    } catch (final ParseException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotosUpdatebyUserid()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotosUpdatebyUserid()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void sortAlbums(final List<NodePK> nodePKs) {
    final Connection con = initCon();
    try {
      NodeDAO.sortNodes(con, nodePKs);
    } catch (final SQLException e) {
      throw new GalleryRuntimeException("sortAlbums()", SilverpeasRuntimeException.ERROR,
          "gallery.MSG_NOT_ORDER", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Executes a process list
   *
   * @param processList
   * @throws Exception
   */
  @Override
  public void executeProcessList(final ProcessList<GalleryProcessExecutionContext> processList,
      final GalleryProcessExecutionContext processExecutionContext) {
    final Connection connection = initCon();
    try {
      processExecutionContext.setConnection(connection);
      ProcessFactory.getProcessManagement().execute(processList, processExecutionContext);
    } catch (final Exception e) {
      throw new GalleryRuntimeException("executeProcessList()", SilverpeasRuntimeException.ERROR,
          "gallery.TRANSACTION_ERROR", e);
    } finally {
      DBUtil.close(connection);
    }
  }
}
