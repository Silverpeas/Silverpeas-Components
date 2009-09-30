/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.GalleryContentManager;
import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.dao.OrderDAO;
import com.silverpeas.gallery.dao.PhotoDAO;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.comment.control.CommentController;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;

/**
 * @author
 */
public class GalleryBmEJB implements SessionBean {

  public AlbumDetail getAlbum(NodePK nodePK, boolean viewAllPhoto) {
    try {
      AlbumDetail album = new AlbumDetail(getNodeBm().getDetail(nodePK));
      // récupération des photos
      Collection photos = getAllPhoto(nodePK, viewAllPhoto);
      // ajout des photos à l'album
      album.setPhotos(photos);
      return album;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_EXIST", e);
    }
  }

  public Collection getAllAlbums(String instanceId) {
    try {
      NodePK nodePK = new NodePK("0", instanceId);
      Collection albums = getNodeBm().getSubTree(nodePK);
      return albums;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_EXIST", e);
    }
  }

  public NodePK createAlbum(AlbumDetail album, NodePK nodePK) {
    try {
      AlbumDetail currentAlbum = getAlbum(nodePK, true);
      nodePK = getNodeBm().createNode(album, currentAlbum);
      return nodePK;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_CREATE", e);
    }
  }

  public void updateAlbum(AlbumDetail album) {
    try {
      getNodeBm().setDetail(album);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updateAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_UPDATE", e);
    }
  }

  public void deleteAlbum(NodePK nodePK) {
    try {
      // suppression des photos de l'album et des sous albums
      deletePhotosAlbum(nodePK);
      // suppression de l'album et des sous albums
      getNodeBm().removeNode(nodePK);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.deleteAlbum()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ALBUM_NOT_DELETE", e);
    }
  }

  public void deletePhotosAlbum(NodePK nodePK) throws RemoteException {
    // suppression des photos de l'album
    deleteAllPhotos(nodePK);
    // recherche des sous albums
    Collection childrens = getNodeBm().getChildrenDetails(nodePK);
    if (childrens != null) {
      Iterator itChild = childrens.iterator();
      while (itChild.hasNext()) {
        NodeDetail node = (NodeDetail) itChild.next();
        // suppression des photos du sous album
        NodePK nodePkChild = node.getNodePK();
        deletePhotosAlbum(nodePkChild);
      }
    }
  }

  private void deleteAllPhotos(NodePK nodePK) {
    Collection photos = getAllPhoto(nodePK, true);
    if (photos != null) {
      Iterator it = photos.iterator();
      while (it.hasNext()) {
        PhotoDetail photo = (PhotoDetail) it.next();
        PhotoPK photoPK = photo.getPhotoPK();
        deletePhoto(photoPK);
      }
    }
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getNodeBM()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", e);
    }
    return nodeBm;
  }

  public PhotoDetail getPhoto(PhotoPK photoPK) {
    Connection con = initCon();
    try {
      int photoId = Integer.parseInt(photoPK.getId());
      PhotoDetail photo = PhotoDAO.getPhoto(con, photoId);
      return photo;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getAllPhotos(String instanceId) {
    Connection con = initCon();
    try {
      Collection photos = PhotoDAO.getAllPhotos(con, instanceId);
      return photos;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotos()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getAllPhoto(NodePK nodePK, boolean viewAllPhoto) {
    Connection con = initCon();
    try {
      String albumId = nodePK.getId();
      String instanceId = nodePK.getInstanceId();
      Collection photos = PhotoDAO.getAllPhoto(con, albumId, instanceId,
          viewAllPhoto);

      return photos;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getNotVisible(String instanceId) {
    Connection con = initCon();
    try {
      Collection photos = PhotoDAO.getPhotoNotVisible(con, instanceId);

      return photos;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getNotVisible()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public String createPhoto(PhotoDetail photo, String albumId) {
    Connection con = initCon();
    try {
      String id = PhotoDAO.createPhoto(con, photo);

      photo.getPhotoPK().setId(id);
      ResourceLocator metadataSettings = new ResourceLocator(
          "com.silverpeas.gallery.settings.metadataSettings", "fr");
      ImageHelper.setMetaData(photo, metadataSettings);
      createIndex(photo);

      // creation de l'emplacament
      PhotoDAO.createPath(con, photo, albumId);
      return id;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_CREATE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void updatePhoto(PhotoDetail photo) {
    Connection con = initCon();
    try {
      SilverTrace.info("gallery", "GalleryBmEJB.updatePhoto()",
          "root.MSG_GEN_ENTER_METHOD", "PhotoPK = " + photo.toString());
      PhotoDAO.updatePhoto(con, photo);
      // ajouter les metadatas pour les indexer
      ResourceLocator metadataSettings = new ResourceLocator(
          "com.silverpeas.gallery.settings.metadataSettings", "fr");
      ImageHelper.setMetaData(photo, metadataSettings);
      // deleteIndex(photo.getPhotoPK());
      createIndex(photo);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_UPDATE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void deletePhoto(PhotoPK photoPK) {
    Connection con = initCon();
    try {
      int photoId = Integer.parseInt(photoPK.getId());
      PhotoDAO.removePhoto(con, photoId);

      // supprimer les commentaires
      CommentController.deleteCommentsByForeignPK(photoPK);

      // supprime le répertoire de la photo et tout ce qu'il contient
      String componentId = photoPK.getInstanceId();
      ResourceLocator gallerySettings = new ResourceLocator(
          "com.silverpeas.gallery.settings.gallerySettings", "");
      String nomRep = gallerySettings.getString("imagesSubDirectory")
          + photoPK.getId();
      FileRepositoryManager.deleteAbsolutePath(null, componentId, nomRep);

      // supprime l'index
      deleteIndex(photoPK);

      // supprime le silverObject correspond
      getGalleryContentManager().deleteSilverContent(con, photoPK);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.deletePhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_DELETE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getDernieres(String instanceId, boolean viewAllPhoto) {
    Connection con = initCon();
    try {
      Collection photos = PhotoDAO.getDernieres(con, instanceId, viewAllPhoto);
      return photos;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhoto()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  private void fermerCon(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new GalleryRuntimeException("GalleryBmEJB.fermerCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private Connection initCon() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new GalleryRuntimeException("GalleryBmEJB.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  public Collection getPath(NodePK nodePK) {
    Collection path;
    try {
      path = getNodeBm().getPath(nodePK);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getPath()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PATH", e);
    }
    return path;
  }

  public Collection getPathList(String instanceId, String photoId) {
    Connection con = initCon();
    try {
      return PhotoDAO.getPathList(con, instanceId, photoId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getPathList()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PATH", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void setPhotoPath(String photoId, String[] albums, String instanceId) {
    Connection con = initCon();
    try {
      SilverTrace.debug("gallery", "GalleryBmEJB.setPhotoPath()",
          "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId);
      PhotoDAO.deletePhotoPath(con, photoId, instanceId);
      for (int i = 0; i < albums.length; i++) {
        String albumId = albums[i];
        SilverTrace.debug("gallery", "GalleryBmEJB.setPhotoPath()",
            "root.MSG_GEN_PARAM_VALUE", "albumId = " + albumId);
        PhotoDAO.addPhotoPath(con, photoId, albumId, instanceId);
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.setPhotoPath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void updatePhotoPath(String photoId, String[] albums,
      String instanceIdFrom, String instanceIdTo) {
    Connection con = initCon();
    try {
      SilverTrace.debug("gallery", "GalleryBmEJB.updatePhotoPath()",
          "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId);
      PhotoDAO.deletePhotoPath(con, photoId, instanceIdFrom);
      for (int i = 0; i < albums.length; i++) {
        String albumId = albums[i];
        SilverTrace.debug("gallery", "GalleryBmEJB.addAlbumPath()",
            "root.MSG_GEN_PARAM_VALUE", "albumId = " + albumId);
        PhotoDAO.addPhotoPath(con, photoId, albumId, instanceIdTo);
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updatePhotoPath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.IMPOSSIBLE_D_AJOUTER_LES_MODELES", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public String getHTMLNodePath(NodePK nodePK) {
    String htmlPath = "";
    try {
      List path = (List) getPath(nodePK);
      if (path.size() > 0)
        path.remove(path.size() - 1);
      htmlPath = getSpacesPath(nodePK.getInstanceId())
          + getComponentLabel(nodePK.getInstanceId()) + " > "
          + displayPath(path, 10);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getHTMLNodePath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION",
          e);
    }
    return htmlPath;
  }

  private String getSpacesPath(String componentId) {
    String spacesPath = "";
    List spaces = getOrganizationController().getSpacePathToComponent(
        componentId);
    Iterator iSpaces = spaces.iterator();
    SpaceInst spaceInst = null;
    while (iSpaces.hasNext()) {
      spaceInst = (SpaceInst) iSpaces.next();
      spacesPath += spaceInst.getName();
      spacesPath += " > ";
    }
    return spacesPath;
  }

  private String getComponentLabel(String componentId) {
    ComponentInstLight component = getOrganizationController()
        .getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null)
      componentLabel = component.getLabel();
    return componentLabel;
  }

  private OrganizationController getOrganizationController() {
    OrganizationController orga = new OrganizationController();
    return orga;
  }

  private String displayPath(Collection path, int beforeAfter) {
    String pathString = new String();
    int nbItemInPath = path.size();
    Iterator iterator = path.iterator();
    boolean alreadyCut = false;
    int nb = 0;

    NodeDetail nodeInPath = null;
    while (iterator.hasNext()) {
      nodeInPath = (NodeDetail) iterator.next();
      if ((nb <= beforeAfter) || (nb + beforeAfter >= nbItemInPath - 1)) {
        pathString = nodeInPath.getName() + " " + pathString;
        if (iterator.hasNext())
          pathString = " > " + pathString;
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

  public void indexGallery(String instanceId) {
    ResourceLocator metadataSettings = new ResourceLocator(
        "com.silverpeas.gallery.settings.metadataSettings", "fr");

    // parcourir tous les albums
    Collection albums = getAllAlbums(instanceId);
    if (albums != null) {
      Iterator it = albums.iterator();
      while (it.hasNext()) {
        // pour chaque album, parcourir toutes les photos
        NodeDetail album = (NodeDetail) it.next();

        // indexation de l'album
        try {
          getNodeBm().createIndex(album);
        } catch (Exception e) {
          throw new GalleryRuntimeException("GalleryBmEJB.indexGallery()",
              SilverpeasRuntimeException.ERROR, "gallery.MSG_INDEXALBUM", e);
        }

        Collection photos = getAllPhoto(album.getNodePK(), true);
        if (photos != null) {
          Iterator itP = photos.iterator();
          while (itP.hasNext()) {
            PhotoDetail photo = (PhotoDetail) itP.next();
            // ajout des métadata pour les indéxer
            try {
              ImageHelper.setMetaData(photo, metadataSettings);
            } catch (Exception e) {
              SilverTrace.info("gallery", "GalleryBmEJB.indexGallery()",
                  "root.MSG_GEN_ENTER_METHOD",
                  "Impossible d'ajouter les métadata à la photo "
                      + photo.toString());
            }
            // indéxation de la photo
            createIndex(photo);
          }
        }
      }
    }
  }

  public void createIndex(PhotoDetail photo) {
    SilverTrace.info("gallery", "GalleryBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "photoDetail = " + photo.toString());
    FullIndexEntry indexEntry = null;

    if (photo != null) {
      // Index the Photo
      indexEntry = new FullIndexEntry(photo.getPhotoPK().getComponentName(),
          "Photo", photo.getPhotoPK().getId());
      indexEntry.setTitle(photo.getTitle());
      indexEntry.setPreView(photo.getDescription());
      indexEntry.setCreationDate(photo.getCreationDate());
      indexEntry.setCreationUser(photo.getCreatorId());
      indexEntry.setKeyWords(photo.getKeyWord());
      if (photo.getBeginDate() != null)
        indexEntry.setStartDate(DateUtil.date2SQLDate(photo.getBeginDate()));
      if (photo.getEndDate() != null)
        indexEntry.setEndDate(DateUtil.date2SQLDate(photo.getEndDate()));

      if (photo.getImageName() != null) {
        ResourceLocator gallerySettings = new ResourceLocator(
            "com.silverpeas.gallery.settings.gallerySettings", "");
        indexEntry.setThumbnail(photo.getImageName());
        indexEntry.setThumbnailMimeType(photo.getImageMimeType());
        indexEntry.setThumbnailDirectory(gallerySettings
            .getString("imagesSubDirectory")
            + photo.getPhotoPK().getId());
      }

      // récupération des méta données pour les indéxer
      String metaDataStr = "";
      MetaData metaData;
      Collection properties = photo.getMetaDataProperties();
      Iterator it = properties.iterator();
      while (it.hasNext()) {
        String property = (String) it.next();
        metaData = photo.getMetaData(property);
        String value = metaData.getValue();
        metaDataStr = metaDataStr + " " + value;
      }
      indexEntry.addTextContent(metaDataStr);
      SilverTrace.info("gallery", "GalleryBmEJB.createIndex()",
          "root.MSG_GEN_ENTER_METHOD", "metaData = " + metaDataStr
              + " indexEntry = " + indexEntry.toString());

      // indexation des méta données (une donnée par champ d'index)
      it = properties.iterator();
      while (it.hasNext()) {
        String property = (String) it.next();
        metaData = photo.getMetaData(property);
        String value = metaData.getValue();
        SilverTrace.info("gallery", "GalleryBmEJB.createIndex()",
            "root.MSG_GEN_ENTER_METHOD", "property = " + property + " value = "
                + value);

        if (metaData.isDate())
          indexEntry.addField("IPTC_" + property, metaData.getDateValue());
        else
          indexEntry.addField("IPTC_" + property, value);
      }

      // indéxation du contenu du formulaire XML
      String xmlFormName = getOrganizationController()
          .getComponentParameterValue(photo.getInstanceId(), "XMLFormName");
      SilverTrace.info("gallery", "GalleryBmEJB.createIndex()",
          "root.MSG_GEN_ENTER_METHOD", "xmlFormName = " + xmlFormName);
      if (xmlFormName != null && !xmlFormName.equals("")
          && !xmlFormName.equals("null")) {
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate;
        try {
          pubTemplate = PublicationTemplateManager.getPublicationTemplate(photo
              .getInstanceId()
              + ":" + xmlFormShortName);
          RecordSet set = pubTemplate.getRecordSet();
          set.indexRecord(photo.getPhotoPK().getId(), xmlFormShortName,
              indexEntry);
          SilverTrace.info("gallery", "GalleryBmEJB.createIndex()",
              "root.MSG_GEN_ENTER_METHOD", "indexEntry = "
                  + indexEntry.toString());
        } catch (Exception e) {
          throw new GalleryRuntimeException("GalleryBmEJB.createIndex()",
              SilverpeasRuntimeException.ERROR,
              "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  public void deleteIndex(PhotoPK photoPK) {
    SilverTrace.info("gallery", "GalleryBmEJB.deleteIndex()",
        "root.MSG_GEN_ENTER_METHOD", "PhotoPK = " + photoPK.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(photoPK.getComponentName(),
        "Photo", photoPK.getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public int getSilverObjectId(PhotoPK photoPK) {
    SilverTrace.info("gallery", "GalleryBmEJB.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "photoId = " + photoPK.getId());
    int silverObjectId = -1;
    PhotoDetail photoDetail = null;
    try {
      silverObjectId = getGalleryContentManager().getSilverObjectId(
          photoPK.getId(), photoPK.getInstanceId());
      if (silverObjectId == -1) {
        photoDetail = getPhoto(photoPK);
        silverObjectId = createSilverContent(null, photoDetail, photoDetail
            .getCreatorId());
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(Connection con, PhotoDetail photoDetail,
      String creatorId) {
    SilverTrace.info("gallery", "GalleryBmEJB.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "photoId = "
            + photoDetail.getPhotoPK().getId());
    try {
      return getGalleryContentManager().createSilverContent(con, photoDetail,
          creatorId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  public Collection search(QueryDescription query) {
    Collection photos = new ArrayList();
    MatchingIndexEntry[] result = null;
    try {
      SearchEngineBm searchEngineBm = getSearchEngineBm();
      searchEngineBm.search(query);
      result = searchEngineBm.getRange(0, searchEngineBm.getResultLength());

      // création des photos à partir des resultats
      for (int i = 0; i < result.length; i++) {
        MatchingIndexEntry matchIndex = result[i];
        // Ne retourne que les photos
        if (matchIndex.getObjectType().equals("Photo")) {
          PhotoPK photoPK = new PhotoPK(matchIndex.getObjectId());
          PhotoDetail photo = getPhoto(photoPK);

          if (photo != null) {
            SilverTrace.info("gallery", "GalleryBmEJB.getResultSearch()",
                "root.MSG_GEN_ENTER_METHOD", "photo = "
                    + photo.getPhotoPK().getId());
            photos.add(photo);
          }
        }
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getResultSearch()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_OBJECT", e);
    }
    return photos;
  }

  public Collection getAllPhotoEndVisible(int nbDays) {
    Connection con = initCon();
    try {
      return PhotoDAO.getAllPhotoEndVisible(con, nbDays);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllPhotos()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void notifyUsers(NotificationMetaData notifMetaData, String senderId,
      String componentId) {
    Connection con = null;
    try {
      con = initCon();
      notifMetaData.setConnection(con);
      if (notifMetaData.getSender() == null
          || notifMetaData.getSender().length() == 0)
        notifMetaData.setSender(senderId);
      NotificationSender notifSender = new NotificationSender(componentId);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new GalleryRuntimeException("GalleryBmEJB.notifyUsers()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      fermerCon(con);
    }
  }

  public String createOrder(Collection basket, String userId, String componentId) {
    Connection con = initCon();
    try {
      return OrderDAO.createOrder(con, basket, userId, componentId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.createOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_REQUEST_NOT_CREATE", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public List getAllOrders(String userId, String instanceId) {
    Connection con = initCon();
    try {
      return OrderDAO.getAllOrders(con, userId, instanceId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getAllOrders()",
          SilverpeasRuntimeException.ERROR,
          "gallery.MSG_REQUEST_LIST_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void updateOrder(Order order) {
    Connection con = initCon();
    try {
      OrderDAO.updateOrder(con, order);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updateORder()",
          SilverpeasRuntimeException.ERROR,
          "gallery.MSG_REQUEST_ORDER_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void updateOrderRow(OrderRow row) {
    Connection con = initCon();
    try {
      OrderDAO.updateOrderRow(con, row);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.updateOrderRow()",
          SilverpeasRuntimeException.ERROR,
          "gallery.MSG_REQUEST_ORDER_ROW_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Order getOrder(String orderId, String instanceId) {
    Connection con = initCon();
    try {
      return OrderDAO.getOrder(con, orderId, instanceId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Collection getAllOrderToDelete(int nbDays) {
    Connection con = initCon();
    try {
      return OrderDAO.getAllOrdersToDelete(con, nbDays);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.getOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public void deleteOrder(String orderId) {
    Connection con = initCon();
    try {
      OrderDAO.deleteOrder(con, orderId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.deleteOrder()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ORDER_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public Date getDownloadDate(String orderId, String photoId) {
    Connection con = initCon();
    try {
      return OrderDAO.getDownloadDate(con, orderId, photoId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryBmEJB.photoDateDownload()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      fermerCon(con);
    }
  }

  public PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
              PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new CommentRuntimeException(
          "GallerySessionController.getPublicationBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return publicationBm;
  }

  public SearchEngineBm getSearchEngineBm() {
    SearchEngineBm searchEngineBm = null;
    {
      try {
        SearchEngineBmHome searchEngineHome = (SearchEngineBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
                SearchEngineBmHome.class);
        searchEngineBm = searchEngineHome.create();
      } catch (Exception e) {
        throw new CommentRuntimeException(
            "GallerySessionController.getSearchEngineBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }

    return searchEngineBm;
  }

  private GalleryContentManager getGalleryContentManager() {
    return new GalleryContentManager();
  }

  public void ejbCreate() {
    // not implemented
  }

  public void setSessionContext(SessionContext context) {
    // not implemented
  }

  public void ejbRemove() {
    // not implemented
  }

  public void ejbActivate() {
    // not implemented
  }

  public void ejbPassivate() {
    // not implemented
  }
}
