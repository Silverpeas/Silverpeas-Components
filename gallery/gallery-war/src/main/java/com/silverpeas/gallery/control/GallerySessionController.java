package com.silverpeas.gallery.control;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.gallery.GSCAuthorComparatorAsc;
import com.silverpeas.gallery.GSCCreationDateComparatorAsc;
import com.silverpeas.gallery.GSCCreationDateComparatorDesc;
import com.silverpeas.gallery.GSCSizeComparatorAsc;
import com.silverpeas.gallery.GSCTitleComparatorAsc;
import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoSelection;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.comment.control.CommentController;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.ejb.CommentBmHome;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeSelection;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;

public class GallerySessionController extends
    AbstractComponentSessionController {
  // déclaration des variables
  private String currentAlbumId = "0";
  private AlbumDetail currentAlbum = getAlbum(currentAlbumId);
  private int rang = 0;
  private String taille = "133x100";
  private String tri = "CreationDateAsc";
  private boolean viewAllPhoto = false;
  private String currentOrderId = "0";

  private Collection listSelected = new ArrayList();
  private boolean isViewNotVisible = false;

  // gestion de la recherche par mot clé
  private String searchKeyWord = "";
  private Collection searchResultListPhotos = new ArrayList();
  private Collection restrictedListPhotos = new ArrayList();
  private boolean isSearchResult = false;
  private QueryDescription query = new QueryDescription();
  private SearchContext pdcSearchContext;
  private DataRecord xmlSearchContext;

  // pour tout selectionner / déselectionner
  private boolean select = false;

  private ResourceLocator gallerySettings = null;
  private ResourceLocator metadataSettings = null;
  private ResourceLocator metadataResources = null;

  private CommentBm commentBm = null;

  // pagination de la liste des résultats (PDC via DomainsBar)
  private int indexOfFirstItemToDisplay = 0;

  private AdminController m_AdminCtrl = null;

  // panier en cours
  private List basket = new ArrayList(); // liste des photos mise dans le panier

  /**
   * Standard Session Controller Constructeur
   * 
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public GallerySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.gallery.multilang.galleryBundle",
        "com.silverpeas.gallery.settings.galleryIcons",
        "com.silverpeas.gallery.settings.gallerySettings");

    // affectation du formulaire à la photothèque
    String xmlFormName = getXMLFormName();
    String xmlFormShortName = null;
    if (StringUtil.isDefined(xmlFormName)) {
      xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/") + 1,
          xmlFormName.indexOf("."));
      try {
        PublicationTemplateManager.addDynamicPublicationTemplate(
            getComponentId() + ":" + xmlFormShortName, xmlFormName);
      } catch (PublicationTemplateException e) {
        throw new GalleryRuntimeException("GallerySessionController.super()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }

    // affectation du formulaire associé aux demandes de photos
    String xmlOrderFormName = getOrderForm();
    String xmlOrderFormShortName = null;
    if (StringUtil.isDefined(xmlOrderFormName)) {
      xmlOrderFormShortName = xmlOrderFormName.substring(xmlOrderFormName
          .indexOf("/") + 1, xmlOrderFormName.indexOf("."));
      try {
        PublicationTemplateManager.addDynamicPublicationTemplate(
            getComponentId() + ":" + xmlOrderFormShortName, xmlOrderFormName);
      } catch (PublicationTemplateException e) {
        throw new GalleryRuntimeException("GallerySessionController.super()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
  }

  public Collection getDernieres() {
    Collection photos = null;
    // va rechercher les dernières photos du composant
    try {
      photos = getGalleryBm().getDernieres(getComponentId(), viewAllPhoto);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getDernieres()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photos;
  }

  public Vector getAllComments(String id) throws RemoteException {
    return getCommentBm().getAllComments(
        new PhotoPK(id, getSpaceId(), getComponentId()));
  }

  public CommentBm getCommentBm() {
    if (commentBm == null) {
      try {
        CommentBmHome commentHome = (CommentBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.COMMENT_EJBHOME, CommentBmHome.class);
        commentBm = commentHome.create();
      } catch (Exception e) {
        throw new CommentRuntimeException(
            "GallerySessionController.getCommentBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }

    return commentBm;
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = getGalleryBm().getSilverObjectId(
          new PhotoPK(objectId, getSpaceId(), getComponentId()));
    } catch (Exception e) {
      SilverTrace.error("gallery",
          "GallerySessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  public AlbumDetail goToAlbum() {
    // return goToAlbum(currentAlbumId);
    // setIndexOfFirstItemToDisplay("0");
    return currentAlbum;
  }

  public AlbumDetail goToAlbum(String albumId) {
    setCurrentAlbumId(albumId);
    // setIndexOfFirstItemToDisplay("0");
    return currentAlbum;
  }

  public AlbumDetail getAlbum(String albumId) {
    NodePK nodePK = new NodePK(albumId, getComponentId());
    AlbumDetail album = null;
    try {
      album = getGalleryBm().getAlbum(nodePK, viewAllPhoto);
      // ajout des métadonnées sur les photos
      Collection photos = album.getPhotos();
      Iterator it = photos.iterator();
      while (it.hasNext()) {
        PhotoDetail photo = (PhotoDetail) it.next();
        try {
          ImageHelper.setMetaData(photo, getMetadataSettings(),
              getMetadataResources());
        } catch (Exception e) {
          SilverTrace.info("gallery", "GallerySessionController.getAlbum",
              "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
        }
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return album;
  }

  public Collection getNotVisible() {
    Collection photos = new ArrayList();
    try {
      photos = getGalleryBm().getNotVisible(getComponentId());
      // ajout des métadonnées sur les photos
      Iterator it = photos.iterator();
      while (it.hasNext()) {
        PhotoDetail photo = (PhotoDetail) it.next();
        try {
          ImageHelper.setMetaData(photo, getMetadataSettings(),
              getMetadataResources());
        } catch (Exception e) {
          SilverTrace.info("gallery", "GallerySessionController.getNotVisible",
              "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
        }
      }
      setRestrictedListPhotos(photos);
      return photos;
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getNotVisible()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public Collection getAllAlbums() {
    try {
      return getGalleryBm().getAllAlbums(getComponentId());
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void setPhotoPath(String photoId, String[] albums) {
    try {
      if (albums != null) {
        SilverTrace.debug("gallery", "GallerySessionController.addAlbumPath()",
            "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId);
        getGalleryBm().setPhotoPath(photoId, albums, getComponentId());
      }
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.addAlbumPath()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_ERR_GENERAL", e);
    }
  }

  public void createAlbum(AlbumDetail album) {
    try {
      album.setCreationDate(DateUtil.date2SQLDate(new Date()));
      album.setCreatorId(getUserId());
      album.getNodePK().setComponentName(getComponentId());

      getGalleryBm().createAlbum(album,
          new NodePK(currentAlbumId, getComponentId()));
      // recharger l'album courant pour prendre en comptes le nouvel album
      goToAlbum(currentAlbumId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.createAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updateAlbum(AlbumDetail album) {
    try {
      getGalleryBm().updateAlbum(album);
      // recharger l'album courant pour prendre en comptes la modification de
      // l'album
      goToAlbum(currentAlbumId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.updateAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void deleteAlbum(String albumId) {
    try {
      getGalleryBm().deleteAlbum(new NodePK(albumId, getComponentId()));
      // recharger l'album courant pour prendre en comptes la suppression
      goToAlbum(currentAlbumId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GGallerySessionController.deleteAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public ResourceLocator getMetadataSettings() {
    if (metadataSettings == null)
      metadataSettings = new ResourceLocator(
          "com.silverpeas.gallery.settings.metadataSettings", getLanguage());
    return metadataSettings;
  }

  public ResourceLocator getMetadataResources() {
    if (metadataResources == null)
      metadataResources = new ResourceLocator(
          "com.silverpeas.gallery.multilang.metadataBundle", getLanguage());
    return metadataResources;
  }

  public PhotoDetail getPhoto(String photoId) throws RemoteException {
    PhotoPK photoPK = new PhotoPK(photoId, getComponentId());
    PhotoDetail photo = null;

    photo = getGalleryBm().getPhoto(photoPK);
    photo
        .setCreatorName(getUserDetail(photo.getCreatorId()).getDisplayedName());
    if (photo.getUpdateId() != null)
      photo
          .setUpdateName(getUserDetail(photo.getUpdateId()).getDisplayedName());

    // ajout des metadata depuis le fichier des properties
    try {
      ImageHelper.setMetaData(photo, getMetadataSettings(),
          getMetadataResources());
    } catch (Exception e) {
      SilverTrace.info("gallery", "GallerySessionController.getPhoto",
          "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
    }

    // Mise à jour de l'album courant
    // String albumId = photo.getAlbumId();
    Collection albumIds = getGalleryBm().getPathList(
        photo.getPhotoPK().getInstanceId(), photo.getId());
    SilverTrace.info("gallery", "GallerySessionController.getPhoto",
        "root.MSG_GEN_PARAM_VALUE", "albumIds = " + albumIds
            + ", currentAlbumId =  " + getCurrentAlbumId());

    // regarder si l'album courant est dans la liste des albums
    boolean inAlbum = false;
    boolean first = true;
    String firstAlbumId = "";
    Iterator it = albumIds.iterator();
    while (it.hasNext()) {
      String albumId = (String) it.next();
      SilverTrace.info("gallery", "GallerySessionController.getPhoto",
          "root.MSG_GEN_PARAM_VALUE", "albumId = " + albumId);
      if (first) {
        firstAlbumId = albumId;
        first = false;
      }
      if (albumId.equals(currentAlbumId))
        inAlbum = true;
    }
    if (!inAlbum)
      setCurrentAlbumId(firstAlbumId);

    SilverTrace.info("gallery", "GallerySessionController.getPhoto",
        "root.MSG_GEN_PARAM_VALUE", "currentAlbumId fin = " + currentAlbumId);
    // mise à jour du rang de la photo courante
    List photos = (List) currentAlbum.getPhotos();
    rang = photos.indexOf(photo);

    return photo;
  }

  public PhotoDetail getPrevious() {
    PhotoDetail photo = null;
    try {
      // rechercher le rang de la photo précédente
      int rangPrevious = rang - 1;
      List photos = (List) currentAlbum.getPhotos();
      photo = (PhotoDetail) photos.get(rangPrevious);
      // ajout des metadata depuis le fichier des properties
      try {
        ImageHelper.setMetaData(photo, getMetadataSettings(),
            getMetadataResources());
      } catch (Exception e) {
        SilverTrace.info("gallery", "GallerySessionController.getPrevious",
            "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
      }
      // on est sur la précédente, mettre à jour le rang de la photo courante
      rang = rangPrevious;
    } catch (Exception e) {
      // traitement des exceptions
      throw new GalleryRuntimeException(
          "GallerySessionController.getPrevious()", SilverpeasException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photo;
  }

  public PhotoDetail getNext() {
    PhotoDetail photo = null;
    try {
      // rechercher le rang de la photo précédente
      int rangNext = rang + 1;
      List photos = (List) currentAlbum.getPhotos();
      photo = (PhotoDetail) photos.get(rangNext);
      // ajout des metadata depuis le fichier des properties
      try {
        ImageHelper.setMetaData(photo, getMetadataSettings(),
            getMetadataResources());
      } catch (Exception e) {
        SilverTrace.info("gallery", "GallerySessionController.getNext",
            "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
      }
      // on est sur la suivante, mettre à jour le rang de la photo courante
      rang = rangNext;
    } catch (Exception e) {
      // traitement des exceptions
      throw new GalleryRuntimeException(
          "GallerySessionController.getPrevious()", SilverpeasException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photo;
  }

  public String getPhotoId(int rang) {
    PhotoDetail photo = null;
    String photoId = null;
    try {
      // rechercher le rang de la photo précédente
      List photos = (List) currentAlbum.getPhotos();
      photo = (PhotoDetail) photos.get(rang);
      photoId = photo.getPhotoPK().getId();
    } catch (Exception e) {
      // traitement des exceptions
      throw new GalleryRuntimeException(
          "GallerySessionController.getPhotoId()", SilverpeasException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photoId;
  }

  public synchronized String createPhoto(PhotoDetail photo) {
    return createPhoto(photo, currentAlbumId);
  }

  public synchronized String createPhoto(PhotoDetail photo, String albumId) {
    try {
      // photo.setAlbumId(getCurrentAlbumId());
      photo.setCreatorId(getUserId());
      PhotoPK pk = new PhotoPK("unknown", getComponentId());
      photo.setPhotoPK(pk);

      String photoId = getGalleryBm().createPhoto(photo, albumId);

      // recharger l'album courant pour prendre en comptes la nouvelle photo
      goToAlbum(albumId);

      return photoId;
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.createPhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updatePhotoHeader(PhotoDetail photo) {
    try {
      photo.setUpdateDate(new Date());
      photo.setUpdateId(getUserId());
      getGalleryBm().updatePhoto(photo);

      // recharger l'album courant pour prendre en comptes la modif sur la photo
      goToAlbum(currentAlbumId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void updatePhoto(PhotoDetail photo) {
    try {
      try {
        ImageHelper.setMetaData(photo, getMetadataSettings(),
            getMetadataResources());
      } catch (Exception e) {
        SilverTrace.info("gallery", "GallerySessionController.updatePhoto",
            "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
      }

      // photo.setUpdateId(getUserId());
      getGalleryBm().updatePhoto(photo);

      // recharger l'album courant pour prendre en comptes la modif sur la photo
      goToAlbum(currentAlbumId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_METADATA", e);
    }
  }

  public void deletePhoto(String photoId) {
    PhotoPK photoPK = new PhotoPK(photoId, getComponentId());
    try {
      // suppression du contenu XML s'il y en a
      removeXMLContentOfPhoto(photoId);

      getGalleryBm().deletePhoto(photoPK);
      // recharger l'album courant pour prendre en comptes la suppression de la
      // photo
      goToAlbum(currentAlbumId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.deletePhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void sortPhotos() {
    if (tri.equals("CreationDateAsc")) {
      GSCCreationDateComparatorAsc comparateur = new GSCCreationDateComparatorAsc();
      Collections.sort((List) currentAlbum.getPhotos(), comparateur);
    } else if (tri.equals("CreationDateDesc")) {
      GSCCreationDateComparatorDesc comparateur = new GSCCreationDateComparatorDesc();
      Collections.sort((List) currentAlbum.getPhotos(), comparateur);
    } else if (tri.equals("Size")) {
      GSCSizeComparatorAsc comparateur = new GSCSizeComparatorAsc();
      Collections.sort((List) currentAlbum.getPhotos(), comparateur);
    }

    else if (tri.equals("Title")) {
      GSCTitleComparatorAsc comparateur = new GSCTitleComparatorAsc();
      Collections.sort((List) currentAlbum.getPhotos(), comparateur);
    } else if (tri.equals("Author")) {
      GSCAuthorComparatorAsc comparateur = new GSCAuthorComparatorAsc();
      Collections.sort((List) currentAlbum.getPhotos(), comparateur);
    }
  }

  public void sortPhotosSearch() {
    if (tri.equals("CreationDateAsc")) {
      GSCCreationDateComparatorAsc comparateur = new GSCCreationDateComparatorAsc();
      Collections.sort((List) getRestrictedListPhotos(), comparateur);
    } else if (tri.equals("CreationDateDesc")) {
      GSCCreationDateComparatorDesc comparateur = new GSCCreationDateComparatorDesc();
      Collections.sort((List) getRestrictedListPhotos(), comparateur);
    } else if (tri.equals("Size")) {
      GSCSizeComparatorAsc comparateur = new GSCSizeComparatorAsc();
      Collections.sort((List) getRestrictedListPhotos(), comparateur);
    }

    else if (tri.equals("Title")) {
      GSCTitleComparatorAsc comparateur = new GSCTitleComparatorAsc();
      Collections.sort((List) getRestrictedListPhotos(), comparateur);
    } else if (tri.equals("Author")) {
      GSCAuthorComparatorAsc comparateur = new GSCAuthorComparatorAsc();
      Collections.sort((List) getRestrictedListPhotos(), comparateur);
    }
  }

  private void removeXMLContentOfPhoto(String photoId) throws RemoteException {
    try {
      String xmlFormName = getXMLFormName();
      if (isDefined(xmlFormName)) {
        String xmlFormShortName = xmlFormName.substring(xmlFormName
            .indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate = PublicationTemplateManager
            .getPublicationTemplate(getComponentId() + ":" + xmlFormShortName);

        RecordSet set = pubTemplate.getRecordSet();
        DataRecord data = set.getRecord(photoId);
        set.delete(data);
      }
    } catch (PublicationTemplateException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.removeXMLContentOfPhoto()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    } catch (FormException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.removeXMLContentOfPhoto()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DE_SUPPRIMER_LE_CONTENU_XML", e);
    }
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param));
  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }

  public Collection getPath(NodePK nodePK) {
    Collection path = null;
    try {
      path = getGalleryBm().getPath(nodePK);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException("GallerySessionController.getPath()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return path;
  }

  public Collection getPath() {
    return getPath(new NodePK(getCurrentAlbumId(), getComponentId()));
  }

  /*
   * public Collection getPathPhoto(String photoId) { String albumId =
   * getPhoto(photoId).getAlbumId(); NodePK nodePK =
   * getAlbum(albumId).getNodePK(); return getPath(nodePK); }
   */

  public synchronized Collection getPathList(String photoId) {
    try {
      return getGalleryBm().getPathList(getComponentId(), photoId);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getPathList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public int getNbPhotosPerPage() {
    int nbPhotosPerPage = 15;
    if (taille.equals("66x50")) {
      nbPhotosPerPage = 35;
    } else if (taille.equals("133x100")) {
      nbPhotosPerPage = 15;
    } else if (taille.equals("266x150")) {
      nbPhotosPerPage = 6;
    }
    return nbPhotosPerPage;
  }

  public String getTaille() {
    return taille;
  }

  public void setTaille(String taille) {
    this.taille = taille;
  }

  public String getTri() {
    return tri;
  }

  public void setTri(String tri) {
    this.tri = tri;
    sortPhotos();
  }

  public void setTriSearch(String tri) {
    this.tri = tri;
    sortPhotosSearch();
  }

  public void setIndexOfFirstItemToDisplay(String index) {
    this.indexOfFirstItemToDisplay = new Integer(index).intValue();
  }

  public int getIndexOfFirstItemToDisplay() {
    return indexOfFirstItemToDisplay;
  }

  public String getCurrentAlbumId() {
    return currentAlbumId;
  }

  public void setCurrentAlbumId(String currentAlbumId) {
    SilverTrace.info("gallery", "GallerySessionController.setCurrentAlbumId",
        "root.MSG_GEN_ENTER_METHOD", "currentAlbumId =  " + currentAlbumId);
    this.currentAlbumId = currentAlbumId;
    this.currentAlbum = getAlbum(this.currentAlbumId);
  }

  public int getRang() {
    return rang;
  }

  public Boolean isDragAndDropEnabled() {
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("dragAndDrop")));
  }

  public Boolean isUsePdc() {
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("usePdc")));
  }

  public Boolean isViewMetadata() {
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("viewMetadata")));
  }

  public Boolean isMakeWatermark() {
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("watermark")));
  }

  public String getWatermarkHD() {
    String watermarkHD = getComponentParameterValue("WatermarkHD");
    return watermarkHD;
  }

  public String getWatermarkOther() {
    String watermarkOther = getComponentParameterValue("WatermarkOther");
    return watermarkOther;
  }

  public Integer getPercentSizeWatermark() {
    String percent = getComponentParameterValue("percentSizeWatermark");
    if (!StringUtil.isDefined(percent))
      percent = "1";
    Integer percentSize = new Integer(percent);
    if (percentSize.intValue() <= 0)
      percentSize = new Integer(1);
    return percentSize;
  }

  public Boolean isViewList() {
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("viewList")));
  }

  public Boolean areCommentsEnabled() {
    return new Boolean(!"no"
        .equalsIgnoreCase(getComponentParameterValue("comments")));
  }

  public String getPreviewSize() {
    String previewSize = getComponentParameterValue("previewSize");
    if (!StringUtil.isDefined(previewSize))
      previewSize = "preview";
    return previewSize;
  }

  public Integer getSlideshowWait() {
    String wait = getComponentParameterValue("slideshow");
    if (wait == null || wait.equalsIgnoreCase("null") || wait.length() == 0)
      wait = "5";
    Integer iWait = new Integer(wait);
    if (iWait.intValue() <= 0)
      iWait = new Integer(5);
    return iWait;
  }

  public Integer getDayVisible() {
    String day = getComponentParameterValue("dayBeforeEndVisible");
    if (day == null || day.equalsIgnoreCase("null") || day.length() == 0)
      day = "3";
    Integer nbDay = new Integer(day);
    if (nbDay.intValue() <= 0)
      nbDay = new Integer(3);
    return nbDay;
  }

  public String getXMLFormName() {
    String formName = getComponentParameterValue("XMLFormName");
    return formName;
  }

  public Boolean isViewInWysiwyg() {
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("viewInWysiwyg")));
  }

  public void initIndex() {
    indexOfFirstItemToDisplay = 0;
  }

  public String initAlertUser(String photoId) throws RemoteException {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel
                                              // selectionPeas (extra param
                                              // permettant de filtrer les users
                                              // ayant acces au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set
                                                                              // nom
                                                                              // du
                                                                              // composant
                                                                              // pour
                                                                              // browsebar
                                                                              // (PairObject(nom_composant,
                                                                              // lien_vers_composant))
                                                                              // NB
                                                                              // :
                                                                              // seul
                                                                              // le
                                                                              // 1er
                                                                              // element
                                                                              // est
                                                                              // actuellement
                                                                              // utilisé
                                                                              // (alertUserPeas
                                                                              // est
                                                                              // toujours
                                                                              // présenté
                                                                              // en
                                                                              // popup
                                                                              // =>
                                                                              // pas
                                                                              // de
                                                                              // lien
                                                                              // sur
                                                                              // nom
                                                                              // du
                                                                              // composant)
    sel.setHostComponentName(hostComponentName);
    SilverTrace.debug("gallery", "GallerySessionController.initAlertUser()",
        "root.MSG_GEN_PARAM_VALUE", "name = " + hostComponentName
            + " componentId=" + getComponentId());
    sel.setNotificationMetaData(getAlertNotificationMetaData(photoId)); // set
                                                                        // NotificationMetaData
                                                                        // contenant
                                                                        // les
                                                                        // informations
                                                                        // à
                                                                        // notifier
    // fin initialisation de AlertUser
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  public Collection search(QueryDescription query) {
    Collection result = new ArrayList();
    try {
      query.setSearchingUser(getUserId());
      query.addComponent(getComponentId());

      result = getGalleryBm().search(query);

      // mise à jour de la liste
      // setSearchResultListPhotos(result);
      isSearchResult = true;

      // sauvegarde de la recherche
      setQuery(query);

      return result;
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getResultSearch()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void sendAskPhoto(String order) {
    // envoyer une notification au gestionnaire de la photothèque concernant la
    // demande de photo
    // 1. création du message

    OrganizationController orga = new OrganizationController();
    UserDetail[] admins = orga.getUsers("useless", getComponentId(), "admin");
    String user = orga.getUserDetail(getUserId()).getDisplayedName();

    ResourceLocator message = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("gallery.notifAskSubject");
    messageBody = messageBody.append(user).append(" ").append(
        message.getString("gallery.notifBodyAsk")).append("\n").append(order);

    // english notifications
    String subject_en = message_en.getString("gallery.notifAskSubject");
    messageBody_en = messageBody_en.append(user).append(" ").append(
        message.getString("gallery.notifBodyAsk")).append("\n").append(order);

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.addUserRecipients(admins);
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "Main");
    notifMetaData.setComponentId(getComponentId());

    // 2. envoie de la notification aux admin
    try {
      getGalleryBm().notifyUsers(notifMetaData, getUserId(), getComponentId());
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.createMessage()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(
      String photoId) throws RemoteException {
    PhotoPK photoPK = new PhotoPK(photoId, getSpaceId(), getComponentId());
    NodePK nodePK = currentAlbum.getNodePK();
    String senderName = getUserDetail().getDisplayedName();
    PhotoDetail photoDetail = getPhoto(photoPK.getId());
    String htmlPath = getGalleryBm().getHTMLNodePath(nodePK);

    ResourceLocator message = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "en");

    String subject = getNotificationSubject(message);
    String body = getNotificationBody(photoDetail, htmlPath, message,
        senderName);

    // english notifications
    String subject_en = getNotificationSubject(message_en);
    String body_en = getNotificationBody(photoDetail, htmlPath, message_en,
        senderName);

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);

    notifMetaData.setLink(getPhotoUrl(photoDetail));
    notifMetaData.setComponentId(photoPK.getInstanceId());
    notifMetaData.setSender(getUserId());

    return notifMetaData;
  }

  private String getNotificationSubject(ResourceLocator message) {
    return message.getString("gallery.notifSubject");
  }

  private String getNotificationBody(PhotoDetail photoDetail, String htmlPath,
      ResourceLocator message, String senderName) {
    StringBuffer messageText = new StringBuffer();
    messageText.append(senderName).append(" ");
    messageText.append(message.getString("gallery.notifInfo")).append("\n\n");
    messageText.append(message.getString("gallery.notifName")).append(" : ")
        .append(photoDetail.getName()).append("\n");
    if (isDefined(photoDetail.getDescription()))
      messageText.append(message.getString("gallery.notifDesc")).append(" : ")
          .append(photoDetail.getDescription()).append("\n");
    messageText.append(message.getString("gallery.path")).append(" : ").append(
        htmlPath);
    return messageText.toString();
  }

  private String getPhotoUrl(PhotoDetail photoDetail) {
    return URLManager.getURL(null, getComponentId()) + photoDetail.getURL();
  }

  public boolean isViewAllPhoto() {
    return viewAllPhoto;
  }

  public void setViewAllPhoto(boolean viewAllPhoto) {
    this.viewAllPhoto = viewAllPhoto;
  }

  public Collection getListSelected() {
    // restitution de la collection des photos selectionnées
    SilverTrace.info("gallery", "GallerySessionControler.getListSelected()",
        "", "listSelected (taille) = (" + listSelected.size() + ") "
            + listSelected.toString());
    return listSelected;
  }

  public void clearListSelected() {
    listSelected.clear();
  }

  public Collection getRestrictedListPhotos() {
    return restrictedListPhotos;
  }

  public void setRestrictedListPhotos(Collection restrictedListPhotos) {
    this.restrictedListPhotos = restrictedListPhotos;
  }

  public Collection getSearchResultListPhotos() {
    return searchResultListPhotos;
  }

  public void setSearchResultListPhotos(Collection searchResultListPhotos) {
    this.searchResultListPhotos = searchResultListPhotos;
  }

  public String getSearchKeyWord() {
    return searchKeyWord;
  }

  public void setSearchKeyWord(String searchKeyWord) {
    this.searchKeyWord = searchKeyWord;
  }

  public boolean getSelect() {
    return select;
  }

  public void setSelect(boolean select) {
    this.select = select;
  }

  public Boolean isPrivateSearch() {
    // retourne true si on utilise le moteur de recherche dédié

    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("privateSearch")));
  }

  public boolean isAlbumAdmin(String profile, String albumId, String userId) {
    if (albumId == null) {
      return isAdminOrPublisher(profile);
    } else {
      // rechercher le créateur de l'album
      AlbumDetail album = getAlbum(albumId);
      return ("admin".equals(profile) || ("publisher".equals(profile) && album
          .getCreatorId().equals(userId)));
    }
  }

  public boolean isPhotoAdmin(String profile, String photoId, String userId)
      throws RemoteException {
    if (photoId == null) {
      return (isAdminOrPublisher(profile) || "writer".equals(profile));
    } else {
      // rechercher le créateur de la photo
      PhotoDetail photo = getPhoto(photoId);
      return (isAdminOrPublisher(profile) || ("writer".equals(profile) && photo
          .getCreatorId().equals(userId)));
    }
  }

  public void copySelectedPhoto(Collection photoIds) throws RemoteException {
    Iterator itPhoto = photoIds.iterator();
    while (itPhoto.hasNext()) {
      String photoId = (String) itPhoto.next();
      copyImage(photoId);
    }
  }

  public void cutSelectedPhoto(Collection photoIds) throws RemoteException {
    Iterator itPhoto = photoIds.iterator();
    while (itPhoto.hasNext()) {
      String photoId = (String) itPhoto.next();
      cutImage(photoId);
    }
  }

  public void copyImage(String photoId) throws RemoteException {
    PhotoDetail photo = getPhoto(photoId);
    PhotoSelection photoSelect = new PhotoSelection(photo);

    SilverTrace.info("gallery", "GallerySessionController.copyImage()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboard().getName()
            + "' count=" + getClipboard().getCount());
    getClipboard().add((ClipboardSelection) photoSelect);
  }

  public void cutImage(String photoId) throws RemoteException {
    PhotoDetail photo = getPhoto(photoId);
    PhotoSelection photoSelect = new PhotoSelection(photo);
    photoSelect.setCutted(true);

    SilverTrace.info("gallery", "GallerySessionController.cutPhoto()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboard().getName()
            + "' count=" + getClipboard().getCount());
    getClipboard().add((ClipboardSelection) photoSelect);
  }

  public void copyAlbum(String albumId) throws RemoteException {
    AlbumDetail album = getAlbum(albumId);
    NodeSelection nodeSelect = new NodeSelection(album);

    SilverTrace.info("gallery", "GallerySessionController.copyAlbum()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboard().getName()
            + "' count=" + getClipboard().getCount());
    SilverTrace.info("gallery", "GallerySessionController.copyAlbum()",
        "root.MSG_GEN_PARAM_VALUE", "nodeSelect = " + nodeSelect.toString()
            + "' albumId =" + album.getId());

    getClipboard().add((ClipboardSelection) nodeSelect);
  }

  public void cutAlbum(String albumId) throws RemoteException {
    NodeSelection nodeSelect = new NodeSelection(getAlbum(albumId));
    nodeSelect.setCutted(true);

    SilverTrace.info("gallery", "GallerySessionController.cutAlbum()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboard().getName()
            + "' count=" + getClipboard().getCount());
    getClipboard().add((ClipboardSelection) nodeSelect);
  }

  public void paste() throws RemoteException {
    try {
      SilverTrace.info("gallery", "GalleryRequestRooter.paste()",
          "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboard().getName()
              + " count=" + getClipboard().getCount());
      Collection clipObjects = getClipboard().getSelectedObjects();
      Iterator clipObjectIterator = clipObjects.iterator();
      while (clipObjectIterator.hasNext()) {
        ClipboardSelection clipObject = (ClipboardSelection) clipObjectIterator
            .next();
        if (clipObject != null) {
          if (clipObject
              .isDataFlavorSupported(PhotoSelection.PhotoDetailFlavor)) {
            PhotoDetail photo = (PhotoDetail) clipObject
                .getTransferData(PhotoSelection.PhotoDetailFlavor);
            pastePhoto(photo, clipObject.isCutted());
            if (clipObject.isCutted())
              CallBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE,
                  Integer.parseInt(getUserId()), getComponentId(), photo
                      .getPhotoPK());
          }
          if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
            AlbumDetail album = (AlbumDetail) clipObject
                .getTransferData(NodeSelection.NodeDetailFlavor);
            SilverTrace.info("gallery", "GalleryRequestRooter.paste()",
                "root.MSG_GEN_PARAM_VALUE", "albumId = " + album.getId());
            pasteAlbum(album, currentAlbum, clipObject.isCutted());
            if (clipObject.isCutted())
              CallBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE,
                  Integer.parseInt(getUserId()), getComponentId(), album
                      .getNodePK());
          }
        }
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.paste()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_PASTE_ERROR", e);
    }
    getClipboard().PasteDone();
  }

  private void pasteAlbum(AlbumDetail albumToPaste, AlbumDetail father,
      boolean isCutted) throws RemoteException {
    NodePK nodeToPastePK = albumToPaste.getNodePK();

    SilverTrace.info("gallery", "GalleryRequestRooter.pasteAlbum()",
        "root.MSG_GEN_PARAM_VALUE", " ENTREE albumToPaste = "
            + albumToPaste.getId() + " father = " + father.getId());

    List treeToPaste = getNodeBm().getSubTree(nodeToPastePK);

    if (isCutted) {
      // move node and subtree
      SilverTrace.info("gallery", "GalleryRequestRooter.pasteAlbum()",
          "root.MSG_GEN_PARAM_VALUE", " AVANT nodeToPastePK = "
              + nodeToPastePK.getId() + " father = "
              + father.getNodePK().getId());
      getNodeBm().moveNode(nodeToPastePK, father.getNodePK());
      SilverTrace.info("gallery", "GalleryRequestRooter.pasteAlbum()",
          "root.MSG_GEN_PARAM_VALUE", " APRES nodeToPastePK = "
              + nodeToPastePK.getId() + " father = "
              + father.getNodePK().getId());

      // move images
      NodeDetail fromNode = null;
      NodePK toNodePK = null;
      for (int i = 0; i < treeToPaste.size(); i++) {
        fromNode = (NodeDetail) treeToPaste.get(i);
        if (fromNode != null) {
          toNodePK = getNodePK(fromNode.getNodePK().getId());

          // move images of album
          SilverTrace.info("gallery", "GalleryRequestRooter.pasteAlbum()",
              "root.MSG_GEN_PARAM_VALUE", "fromNode = " + fromNode.toString()
                  + " toNode = " + toNodePK.toString());
          pasteImagesOfAlbum(fromNode.getNodePK(), toNodePK, true, null);
        }
      }
    } else {
      // paste album
      NodePK nodePK = new NodePK("unknown", getComponentId());
      NodeDetail node = new NodeDetail();
      AlbumDetail album = new AlbumDetail(node);
      album.setNodePK(nodePK);
      album.setCreatorId(getUserId());
      album.setName(albumToPaste.getName());
      album.setDescription(albumToPaste.getDescription());
      album.setTranslations(albumToPaste.getTranslations());
      album.setRightsDependsOn(father.getRightsDependsOn());

      nodePK = getNodeBm().createNode(album, father);

      List nodeIdsToPaste = new ArrayList();
      NodeDetail oneNodeToPaste = null;
      for (int i = 0; i < treeToPaste.size(); i++) {
        oneNodeToPaste = (NodeDetail) treeToPaste.get(i);
        if (oneNodeToPaste != null)
          nodeIdsToPaste.add(oneNodeToPaste.getNodePK());
      }

      // paste images of album
      pasteImagesOfAlbum(nodeToPastePK, nodePK, false, nodeIdsToPaste);

      if (albumToPaste.getChildrenDetails() != null) {
        Iterator it = (Iterator) albumToPaste.getChildrenDetails().iterator();
        while (it != null && it.hasNext()) {
          NodeDetail unNode = (NodeDetail) it.next();
          AlbumDetail unAlbum = new AlbumDetail(unNode);
          if (unAlbum != null)
            pasteAlbum(unAlbum, album, isCutted);
        }
      }
    }
  }

  public ProfileInst getAlbumProfile(String role, String topicId) {
    List profiles = new ArrayList();
    // List profiles = getAdmin().getProfilesByObject(topicId,
    // getComponentId());
    for (int p = 0; profiles != null && p < profiles.size(); p++) {
      ProfileInst profile = (ProfileInst) profiles.get(p);
      if (profile.getName().equals(role))
        return profile;
    }

    ProfileInst profile = new ProfileInst();
    profile.setName(role);
    return profile;
  }

  private AdminController getAdmin() {
    if (m_AdminCtrl == null)
      m_AdminCtrl = new AdminController(getUserId());

    return m_AdminCtrl;
  }

  private void pasteImagesOfAlbum(NodePK fromPK, NodePK toPK, boolean isCutted,
      List nodePKsToPaste) throws RemoteException {
    Collection photos = getGalleryBm().getAllPhoto(fromPK, viewAllPhoto);
    Iterator itPhotos = photos.iterator();
    PhotoDetail photo = null;
    while (itPhotos.hasNext()) {
      photo = (PhotoDetail) itPhotos.next();
      SilverTrace.info("gallery", "GalleryRequestRooter.pasteAlbum()",
          "root.MSG_GEN_PARAM_VALUE", "photo = " + photo.toString()
              + " toPK = " + toPK.toString());
      pastePhoto(photo, isCutted, toPK, nodePKsToPaste);
    }
  }

  private void pastePhoto(PhotoDetail photo, boolean isCutted) {
    pastePhoto(photo, isCutted, null, null);

  }

  private void pastePhoto(PhotoDetail photo, boolean isCutted, NodePK nodePK,
      List nodePKsToPaste) {
    try {
      String fromId = photo.getPhotoPK().getId();
      String fromComponentId = photo.getPhotoPK().getInstanceId();

      ForeignPK fromForeignPK = new ForeignPK(photo.getPhotoPK().getId(),
          fromComponentId);
      PhotoPK fromPhotoPK = new PhotoPK(photo.getPhotoPK().getId(),
          fromComponentId);

      ForeignPK toForeignPK = new ForeignPK(photo.getPhotoPK().getId(),
          getComponentId());
      PhotoPK toPhotoPK = new PhotoPK(photo.getPhotoPK().getId(),
          getComponentId());

      if (isCutted) {
        if (nodePK == null) {
          // Ajoute à l'album courant
          nodePK = currentAlbum.getNodePK();
        }

        if (fromComponentId.equals(getComponentId())) {
          // déplacement de la photo dans le même composant
          String[] albums = new String[1];
          albums[0] = nodePK.getId();
          setPhotoPath(photo.getPhotoPK().getId(), albums);
        } else {
          // déplacer la photo dans un autre composant
          boolean indexIt = true;

          // String id = createImageIntoAlbum(fromPhotoPK, photo,
          // nodePK.getId());
          moveImage(fromPhotoPK, photo, nodePK.getId());
          String id = photo.getPhotoPK().getId();
          // photo.getPhotoPK().setId(id);

          String[] albums = new String[1];
          albums[0] = nodePK.getId();
          setPhotoPath(id, albums);
          getGalleryBm().deleteIndex(fromPhotoPK);

          // move comments
          CommentController.moveComments(fromForeignPK, toForeignPK, indexIt);

          // move pdc positions
          int fromSilverObjectId = getGalleryBm()
              .getSilverObjectId(fromPhotoPK);
          int toSilverObjectId = getGalleryBm().getSilverObjectId(toPhotoPK);

          getPdcBm().copyPositions(fromSilverObjectId, fromComponentId,
              toSilverObjectId, getComponentId());

          String xmlFormName = getXMLFormName();
          try {
            if (isDefined(xmlFormName)) {
              // if XMLForm
              String xmlFormShortName = xmlFormName.substring(xmlFormName
                  .indexOf("/") + 1, xmlFormName.indexOf("."));
              PublicationTemplateManager.addDynamicPublicationTemplate(
                  getComponentId() + ":" + xmlFormShortName, xmlFormShortName
                      + ".xml");

              // get xmlContent to paste
              PublicationTemplate pubTemplateFrom = PublicationTemplateManager
                  .getPublicationTemplate(fromComponentId + ":"
                      + xmlFormShortName);
              IdentifiedRecordTemplate recordTemplateFrom = (IdentifiedRecordTemplate) pubTemplateFrom
                  .getRecordSet().getRecordTemplate();

              PublicationTemplate pubTemplate = PublicationTemplateManager
                  .getPublicationTemplate(getComponentId() + ":"
                      + xmlFormShortName);
              IdentifiedRecordTemplate recordTemplate = (IdentifiedRecordTemplate) pubTemplate
                  .getRecordSet().getRecordTemplate();

              // paste xml content
              GenericRecordSetManager.cloneRecord(recordTemplateFrom, fromId,
                  recordTemplate, id, null);
            }
          } catch (PublicationTemplateException e) {
            SilverTrace.info("gallery", "GallerySessionController.pastPhoto()",
                "gallery.DIFERENT_FORM_COMPONENT", e);
          }

          getGalleryBm().updatePhoto(photo);
        }
        goToAlbum(nodePK.getId());
      } else {
        // paste the photo
        String id = null;
        if (nodePK == null) {
          // Ajoute à l'album courant
          nodePK = currentAlbum.getNodePK();
        }

        if (fromComponentId.equals(getComponentId())) {
          // dupliquer la photo dans le même composant
          id = createPhoto(photo, nodePK.getId());
          photo.getPhotoPK().setId(id);

          ImageHelper.pasteImage(fromPhotoPK, photo, isCutted);
        } else {
          // dupliquer la photo dans un autre composant
          id = createImage(fromPhotoPK, photo, nodePK.getId());

          List fatherPKs = (List) getGalleryBm().getPathList(getComponentId(),
              photo.getPhotoPK().getId());
          if (nodePKsToPaste != null) {
            fatherPKs.removeAll(nodePKsToPaste);
          }
        }

        // update id cause new photo is created
        toPhotoPK.setId(id);

        // Paste positions on Pdc
        int fromSilverObjectId = getGalleryBm().getSilverObjectId(fromPhotoPK);
        int toSilverObjectId = getGalleryBm().getSilverObjectId(
            getPhoto(id).getPhotoPK());
        getPdcBm().copyPositions(fromSilverObjectId,
            fromPhotoPK.getInstanceId(), toSilverObjectId, getComponentId());

        // move comments
        boolean indexIt = true;
        CommentController.moveComments(fromForeignPK, toForeignPK, indexIt);

        String xmlFormName = getXMLFormName();

        try {
          if (isDefined(xmlFormName)) {
            // if XMLForm
            String xmlFormShortName = xmlFormName.substring(xmlFormName
                .indexOf("/") + 1, xmlFormName.indexOf("."));
            PublicationTemplateManager.addDynamicPublicationTemplate(
                getComponentId() + ":" + xmlFormShortName, xmlFormShortName
                    + ".xml");

            // get xmlContent to paste
            PublicationTemplate pubTemplateFrom = PublicationTemplateManager
                .getPublicationTemplate(fromComponentId + ":"
                    + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplateFrom = (IdentifiedRecordTemplate) pubTemplateFrom
                .getRecordSet().getRecordTemplate();

            PublicationTemplate pubTemplate = PublicationTemplateManager
                .getPublicationTemplate(getComponentId() + ":"
                    + xmlFormShortName);
            IdentifiedRecordTemplate recordTemplate = (IdentifiedRecordTemplate) pubTemplate
                .getRecordSet().getRecordTemplate();

            // paste xml content
            GenericRecordSetManager.cloneRecord(recordTemplateFrom, fromId,
                recordTemplate, id, null);
          }
        } catch (PublicationTemplateException e) {
          SilverTrace.info("gallery", "GallerySessionController.pastPhoto()",
              "gallery.DIFERENT_FORM_COMPONENT", e);
        }

        // force the update
        getGalleryBm().updatePhoto(photo);

        goToAlbum(nodePK.getId());

      }
    } catch (PdcException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FormException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public synchronized void moveImage(PhotoPK fromPhotoPK,
      PhotoDetail photoDetail, String fatherId) throws RemoteException {
    photoDetail.getPhotoPK().setSpace(getSpaceId());
    photoDetail.getPhotoPK().setComponentName(getComponentId());
    photoDetail.setCreatorId(getUserId());
    photoDetail.setCreationDate(new Date());
    photoDetail.setAlbumId(fatherId);

    getGalleryBm().updatePhoto(photoDetail);

    SilverTrace.debug("gallery", "GallerySessionController.moveImage()",
        "root.MSG_GEN_PARAM_VALUE", "photoId = "
            + photoDetail.getPhotoPK().getId() + " componentFrom = "
            + fromPhotoPK.getInstanceId() + " componentTo = "
            + getComponentId());
    String[] albums = new String[1];
    albums[0] = fatherId;
    getGalleryBm().updatePhotoPath(photoDetail.getPhotoPK().getId(), albums,
        fromPhotoPK.getInstanceId(), getComponentId());

    ImageHelper.pasteImage(fromPhotoPK, photoDetail, false);

    String id = photoDetail.getPhotoPK().getId();
    SilverTrace.spy("gallery",
        "GallerySessionController.createPhotoIntoAlbum(photoDetail, fatherId)",
        getSpaceId(), getComponentId(), id, getUserDetail().getId(),
        SilverTrace.SPY_ACTION_CREATE);
  }

  public synchronized String createImage(PhotoPK fromPhotoPK,
      PhotoDetail photoDetail, String fatherId) throws RemoteException {
    photoDetail.getPhotoPK().setSpace(getSpaceId());
    photoDetail.getPhotoPK().setComponentName(getComponentId());
    photoDetail.setCreatorId(getUserId());
    photoDetail.setCreationDate(new Date());
    photoDetail.setAlbumId(fatherId);

    String id = getGalleryBm().createPhoto(photoDetail, fatherId);

    photoDetail.getPhotoPK().setId(id);

    ImageHelper.pasteImage(fromPhotoPK, photoDetail, false);

    SilverTrace.spy("gallery",
        "GallerySessionController.createPhotoIntoAlbum(photoDetail, fatherId)",
        getSpaceId(), getComponentId(), id, getUserDetail().getId(),
        SilverTrace.SPY_ACTION_CREATE);
    return id;
  }

  public void sendAskOrder(String orderId) {
    // envoyer une notification au gestionnaire pour le prévenir de la demande
    // de l'utilisateur
    // 1. création du message

    OrganizationController orga = new OrganizationController();
    UserDetail[] admins = orga.getUsers("useless", getComponentId(), "admin");
    String user = orga.getUserDetail(getUserId()).getDisplayedName();

    ResourceLocator message = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("gallery.orderNotifAskSubject");
    messageBody = messageBody.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAsk")).append("\n");

    // english notifications
    String subject_en = message_en.getString("gallery.orderNotifAskSubject");
    messageBody_en = messageBody_en.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAsk")).append("\n");

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.addUserRecipients(admins);
    notifMetaData.setLink(URLManager.getURL(null, getComponentId())
        + "OrderView?OrderId=" + orderId);
    notifMetaData.setComponentId(getComponentId());

    // 2. envoie de la notification aux admin
    try {
      getGalleryBm().notifyUsers(notifMetaData, getUserId(), getComponentId());
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.sendAskOrder()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void sendAskOrderUser(String orderId) throws RemoteException {
    // envoyer une notification au lecteur pour le prévenir du traitement de sa
    // demande
    // 1. création du message

    OrganizationController orga = new OrganizationController();
    UserDetail[] users = new UserDetail[1];
    Order order = getOrder(orderId);
    users[0] = orga.getUserDetail(Integer.toString(order.getUserId()));
    String user = orga
        .getUserDetail(Integer.toString(order.getProcessUserId()))
        .getDisplayedName();

    ResourceLocator message = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("gallery.orderNotifAskSubject");
    messageBody = messageBody.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAskOk")).append("\n");

    // english notifications
    String subject_en = message_en.getString("gallery.orderNotifAskSubject");
    messageBody_en = messageBody_en.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAskOk")).append("\n");

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.addUserRecipients(users);
    notifMetaData.setLink(URLManager.getURL(null, getComponentId())
        + "OrderView?OrderId=" + orderId);
    notifMetaData.setComponentId(getComponentId());

    // 2. envoie de la notification aux admin
    try {
      getGalleryBm().notifyUsers(notifMetaData, getUserId(), getComponentId());
    } catch (RemoteException e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.sendAskOrderUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void createBasket() throws RemoteException {
    basket = new ArrayList();
  }

  public void addToBasket() throws RemoteException {
    // ajout dans le panier toutes les photos sélectionnées
    basket.addAll(listSelected);
    // remettre à blanc la liste des photos sélectionnées
    SilverTrace.debug("gallery", "GallerySessionController.addToBasket()",
        "root.MSG_GEN_PARAM_VALUE", "listSelected = " + listSelected.toString()
            + " basket = " + basket.toString());
    clearListSelected();
  }

  public void addPhotoToBasket(String photoId) throws RemoteException {
    // ajout dans le panier la photo
    if (!basket.contains(photoId))
      basket.add(photoId);
  }

  public void deleteToBasket() throws RemoteException {
    // suppression dans le panier
    Iterator it = listSelected.iterator();
    while (it.hasNext()) {
      String photoId = (String) it.next();
      // rechercher la photo dans le panier et la supprimer
      basket.remove(photoId);

    }
    clearListSelected();
  }

  public void deleteToBasket(String photoId) throws RemoteException {
    basket.remove(photoId);
  }

  public void deleteBasket() throws RemoteException {
    // suppression de toutes les photos du panier
    basket.clear();
  }

  public List getBasketListPhotos() throws RemoteException {
    return basket;
  }

  public Order getOrder(String orderId) throws RemoteException {
    Order order = getGalleryBm().getOrder(orderId, getComponentId());
    List rows = order.getRows();
    List newRows = new ArrayList();
    Iterator it = rows.iterator();
    while (it.hasNext()) {
      OrderRow row = (OrderRow) it.next();
      String photoId = Integer.toString(row.getPhotoId());
      PhotoDetail photo = getPhoto(photoId);
      row.setPhoto(photo);
      newRows.add(row);
    }
    order.setRows(newRows);
    return order;
  }

  public String getCurrentOrderId() {
    return currentOrderId;
  }

  public void setCurrentOrderId(String orderId) {
    currentOrderId = orderId;
  }

  public String addOrder() throws RemoteException {
    // transformer le panier en demande
    String orderId = getGalleryBm().createOrder(basket, getUserId(),
        getComponentId());
    // vider le panier
    basket.clear();
    return orderId;
  }

  public void updateOrderRow(String orderId, String photoId)
      throws RemoteException {
    Order order = getOrder(orderId);
    List rows = order.getRows();
    Iterator it = rows.iterator();
    while (it.hasNext()) {
      OrderRow row = (OrderRow) it.next();
      if (row.getPhotoId() == Integer.parseInt(photoId)) {
        // on est sur la bonne ligne, mettre à jour
        row.setDownloadDecision("T");
        getGalleryBm().updateOrderRow(row);
      }
    }
  }

  public String getUrl(String orderId, String photoId) throws RemoteException {
    String url = "";
    Order order = getOrder(orderId);
    List rows = order.getRows();
    Iterator it = rows.iterator();
    while (it.hasNext()) {
      OrderRow row = (OrderRow) it.next();
      if (row.getPhotoId() == Integer.parseInt(photoId)) {
        // on est sur la bonne ligne
        PhotoDetail photo = getPhoto(Integer.toString(row.getPhotoId()));
        String nomRep = getSettings().getString("imagesSubDirectory") + photoId;
        String download = row.getDownloadDecision();

        // par defaut photo sans watermark
        String title = Encode.javaStringToHtmlString(photo.getImageName());
        if (download.equals("DW")) {
          // demande avec Watermark
          title = photoId + "_watermark.jpg";
          // regarder si la photo existe, sinon prendre celle sans watermark
          String pathFile = FileRepositoryManager
              .getAbsolutePath(getComponentId())
              + nomRep + File.separator;
          String watermarkFile = pathFile + title;
          File file = new File(watermarkFile);
          if (!file.exists()) {
            title = Encode.javaStringToHtmlString(photo.getImageName());
          }
        }
        url = FileServerUtils.getUrl(getSpaceId(), getComponentId(), URLEncoder
            .encode(title), photo.getImageMimeType(), nomRep);
      }
    }
    return url;
  }

  public boolean isAccessAuthorized(String orderId) throws RemoteException {
    return Integer.toString(getOrder(orderId).getUserId()).equals(getUserId());
  }

  public List getMetaDataKeys() {
    List metaDatas = new ArrayList();

    // lire le fichier des properties
    // passer les metadata EXIF
    String property = getMetadataSettings().getString("METADATA_1_TAG");
    int indice = 1;
    while (property != null && !"".equals(property)) {
      // lecture de la property suivante
      indice = indice + 1;
      property = getMetadataSettings().getString(
          "METADATA_" + Integer.toString(indice) + "_TAG");
    }

    indice = indice + 1;

    // Traitement des metadata IPTC
    property = getMetadataSettings().getString(
        "IPTC_" + Integer.toString(indice) + "_TAG");
    while (property != null && !"".equals(property)) {
      // récupération de la valeur
      String label = getMetadataSettings().getString(
          "IPTC_" + Integer.toString(indice) + "_LABEL");
      label = getMetadataResources().getString(label);

      // récupération si elle est à ajouter à la recherche
      String search = Integer.toString(indice)
          + "/"
          + getMetadataSettings().getString(
              "IPTC_" + Integer.toString(indice) + "_SEARCH");
      boolean isSearch = (Integer.toString(indice) + "/" + "true")
          .equalsIgnoreCase(search);

      if (isSearch) {
        // récupération de son type
        String date = Integer.toString(indice)
            + "/"
            + getMetadataSettings().getString(
                "IPTC_" + Integer.toString(indice) + "_DATE");
        boolean isDate = (Integer.toString(indice) + "/" + "true")
            .equalsIgnoreCase(date);

        // création de la MetaData
        MetaData metaData = new MetaData();
        metaData.setProperty(property);
        metaData.setDate(isDate);
        metaData.setLabel(label);

        // rechercher sa valeur dans la query (résultat de la recherche)
        List metadataValue = query.getMultiFieldQuery();
        if (metadataValue != null) {
          Iterator it = metadataValue.iterator();
          while (it.hasNext()) {
            FieldDescription field = (FieldDescription) it.next();
            if (field.getFieldName().equals("IPTC_" + metaData.getProperty()))
              metaData.setValue(field.getContent());

          }
        }
        // ajout de cette metadata à la liste
        metaDatas.add(metaData);
      }

      // lecture de la property suivante
      indice = indice + 1;
      property = getMetadataSettings().getString(
          "IPTC_" + Integer.toString(indice) + "_TAG");
    }
    return metaDatas;
  }

  public void updateOrder(Order order) throws RemoteException {
    getGalleryBm().updateOrder(order);
  }

  public List getAllOrders() throws RemoteException {
    return getGalleryBm().getAllOrders("-1", getComponentId());
  }

  public List getOrdersByUser() throws RemoteException {
    return getGalleryBm().getAllOrders(getUserId(), getComponentId());
  }

  public Date getDownloadDate(String orderId, String photoId)
      throws RemoteException {
    return getGalleryBm().getDownloadDate(orderId, photoId);
  }

  public String getOrderForm() {
    String orderForm = getComponentParameterValue("XMLOrderFormName");
    return orderForm;
  }

  public String getCharteUrl() {
    String url = getComponentParameterValue("UrlCharte");
    if (!StringUtil.isDefined(url))
      url = "";
    return url;
  }

  public Boolean isBasket() {
    if (getUserDetail().getAccessLevel().equals("G"))
      return false;
    else
      return new Boolean("yes"
          .equalsIgnoreCase(getComponentParameterValue("basket")));
  }

  public Boolean isOrder() {
    if (getUserDetail().getAccessLevel().equals("G"))
      return false;
    else
      return new Boolean("yes"
          .equalsIgnoreCase(getComponentParameterValue("order")));
  }

  private PdcBm getPdcBm() {
    PdcBm pdcBm = null;
    pdcBm = new PdcBmImpl();
    return pdcBm;
  }

  private NodePK getNodePK(String id) {
    return new NodePK(id, getSpaceId(), getComponentId());
  }

  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  public boolean isViewNotVisible() {
    return isViewNotVisible;
  }

  public void setViewNotVisible(boolean isViewNotVisible) {
    this.isViewNotVisible = isViewNotVisible;
  }

  public boolean isSearchResult() {
    return isSearchResult;
  }

  public void setSearchResult(boolean isSearchResult) {
    this.isSearchResult = isSearchResult;
  }

  public QueryDescription getQuery() {
    return query;
  }

  public void setQuery(QueryDescription query) {
    this.query = query;
  }

  public void setPDCSearchContext(SearchContext context) {
    pdcSearchContext = context;
  }

  public SearchContext getPDCSearchContext() {
    return pdcSearchContext;
  }

  public void setXMLSearchContext(DataRecord data) {
    xmlSearchContext = data;
  }

  public DataRecord getXMLSearchContext() {
    return xmlSearchContext;
  }

  public void clearSearchContext() {
    setQuery(new QueryDescription());
    setXMLSearchContext(null);
    setPDCSearchContext(null);
  }

  protected boolean isAdminOrPublisher(String profile) {
    return SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile))
        || SilverpeasRole.publisher.equals(SilverpeasRole.valueOf(profile));
  }
}