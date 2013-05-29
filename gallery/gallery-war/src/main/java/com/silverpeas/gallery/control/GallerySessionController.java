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
package com.silverpeas.gallery.control;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.search.indexEngine.model.FieldDescription;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.DataRecord;
import com.silverpeas.gallery.GSCAuthorComparatorAsc;
import com.silverpeas.gallery.GSCCreationDateComparatorAsc;
import com.silverpeas.gallery.GSCCreationDateComparatorDesc;
import com.silverpeas.gallery.GSCSizeComparatorAsc;
import com.silverpeas.gallery.GSCTitleComparatorAsc;
import com.silverpeas.gallery.control.ejb.GalleryBm;
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
import com.silverpeas.gallery.model.PhotoSelection;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardException;
import com.silverpeas.util.clipboard.ClipboardSelection;

import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeSelection;

public final class GallerySessionController extends AbstractComponentSessionController {
  // déclaration des variables

  private String currentAlbumId = "0";
  private AlbumDetail currentAlbum = getAlbum(currentAlbumId);
  private int rang = 0;
  private String taille = "133x100";
  private String tri = "CreationDateAsc";
  private boolean viewAllPhoto = false;
  private String currentOrderId = "0";
  private List<String> listSelected = new ArrayList<String>();
  private boolean isViewNotVisible = false;
  // gestion de la recherche par mot clé
  private String searchKeyWord = "";
  private List<PhotoDetail> searchResultListPhotos = new ArrayList<PhotoDetail>();
  private List<PhotoDetail> restrictedListPhotos = new ArrayList<PhotoDetail>();
  private boolean isSearchResult = false;
  private QueryDescription query = new QueryDescription();
  private SearchContext pdcSearchContext;
  private DataRecord xmlSearchContext;
  // pour tout selectionner / déselectionner
  private boolean select = false;
  private ResourceLocator metadataResources = null;
  private CommentService commentService = null;
  // pagination de la liste des résultats (PDC via DomainsBar)
  private int indexOfFirstItemToDisplay = 0;
  // panier en cours
  private List<String> basket = new ArrayList<String>(); // liste des photos mise dans le panier
  static final Properties defaultSettings = new Properties();

  static {
    try {
      FileUtil.loadProperties(defaultSettings,
          "org/silverpeas/gallery/settings/metadataSettings.properties");
    } catch (IOException e) {
      SilverTrace.fatal("gallery", "GallerySessionController()", "root.EX_CANT_GET_REMOTE_OBJECT",
          e);
    }
  }
  Properties settings = new Properties(defaultSettings);

  /**
   * Gets the business service of operations on comments
   *
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentService;
  }

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public GallerySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "com.silverpeas.gallery.multilang.galleryBundle",
        "com.silverpeas.gallery.settings.galleryIcons",
        "com.silverpeas.gallery.settings.gallerySettings");

    // affectation du formulaire à la photothèque
    String xmlFormName = getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
          xmlFormName.indexOf('.'));
      try {
        getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ':'
            + xmlFormShortName, xmlFormName);
      } catch (PublicationTemplateException e) {
        SilverTrace.info("gallery", "GallerySessionController()",
            "root.EX_CANT_GET_REMOTE_OBJECT", "xmlFormName = " + getXMLFormName(), e);
      }
    }

    // affectation du formulaire associé aux demandes de photos
    String xmlOrderFormName = getOrderForm();
    if (StringUtil.isDefined(xmlOrderFormName)) {
      String xmlOrderFormShortName = xmlOrderFormName.substring(xmlOrderFormName.indexOf('/') + 1,
          xmlOrderFormName.indexOf('.'));
      try {
        getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ':'
            + xmlOrderFormShortName, xmlOrderFormName);
      } catch (PublicationTemplateException e) {
        throw new GalleryRuntimeException("GallerySessionController.super()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }

    viewAllPhoto = SilverpeasRole.admin.isInRole(getRole());
  }

  public Collection<PhotoDetail> getDernieres() {
    Collection<PhotoDetail> photos;
    // va rechercher les dernières photos du composant
    try {
      photos = getGalleryBm().getDernieres(getComponentId(), viewAllPhoto);
    } catch (RemoteException e) {
      throw new GalleryRuntimeException("GallerySessionController.getDernieres()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photos;
  }

  public List<Comment> getAllComments(String id) {
    return getCommentService().getAllCommentsOnPublication(PhotoDetail.getResourceType(),
        new PhotoPK(id, getSpaceId(), getComponentId()));
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = getGalleryBm().getSilverObjectId(
          new PhotoPK(objectId, getSpaceId(), getComponentId()));
    } catch (Exception e) {
      SilverTrace.error("gallery", "GallerySessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  public AlbumDetail goToAlbum() {
    return currentAlbum;
  }

  public AlbumDetail goToAlbum(String albumId) {
    setCurrentAlbumId(albumId);
    return currentAlbum;
  }

  public AlbumDetail loadCurrentAlbum() {
    return goToAlbum(currentAlbumId);
  }

  public AlbumDetail getAlbum(String albumId) {
    NodePK nodePK = new NodePK(albumId, getComponentId());
    AlbumDetail album;
    try {
      album = getGalleryBm().getAlbum(nodePK, viewAllPhoto);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return album;
  }

  public AlbumDetail getAlbumLight(String albumId) {
    NodePK nodePK = new NodePK(albumId, getComponentId());
    AlbumDetail album;
    try {
      album = getGalleryBm().getAlbum(nodePK, viewAllPhoto);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbumLight()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return album;
  }

  public Collection<PhotoDetail> getNotVisible() {
    Collection<PhotoDetail> photos;
    try {
      photos = getGalleryBm().getNotVisible(getComponentId());
      setRestrictedListPhotos(photos);
      return photos;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getNotVisible()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public Collection<AlbumDetail> getAllAlbums() {
    try {
      return getGalleryBm().getAllAlbums(getComponentId());
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void setPhotoPath(String photoId, String[] albums) {
    if (albums != null) {
      SilverTrace.debug("gallery", "GallerySessionController.addAlbumPath()",
          "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId);
      getGalleryBm().setPhotoPath(photoId, getComponentId(), albums);
    }
  }

  public void addPhotoPaths(String photoId, String[] albums) {
    if (albums != null) {
      SilverTrace.debug("gallery", "GallerySessionController.addAlbumPath()",
          "root.MSG_GEN_PARAM_VALUE", "photoId = " + photoId);
      getGalleryBm().addPhotoPaths(photoId, getComponentId(), albums);
    }
  }

  public void createAlbum(AlbumDetail album) {
    album.setCreationDate(DateUtil.date2SQLDate(new Date()));
    album.setCreatorId(getUserId());
    album.getNodePK().setComponentName(getComponentId());

    getGalleryBm().createAlbum(album, new NodePK(currentAlbumId, getComponentId()));
    // recharger l'album courant pour prendre en compte le nouvel album
    loadCurrentAlbum();
  }

  public void updateAlbum(AlbumDetail album) {
    getGalleryBm().updateAlbum(album);
    // recharger l'album courant pour prendre en compte la modification de l'album
    loadCurrentAlbum();
  }

  public void deleteAlbum(String albumId) {
    try {
      final String parentId = getAlbumLight(albumId).getFatherPK().getId();
      getGalleryBm().deleteAlbum(getUserDetail(), getComponentId(), getAlbum(albumId).getNodePK());
      goToAlbum(parentId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GGallerySessionController.deleteAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public ResourceLocator getMetadataResources() {
    if (metadataResources == null) {
      metadataResources = new ResourceLocator("com.silverpeas.gallery.multilang.metadataBundle",
          getLanguage());
    }
    return metadataResources;
  }

  public PhotoDetail getPhoto(String photoId) throws RemoteException {
    PhotoPK photoPK = new PhotoPK(photoId, getComponentId());
    PhotoDetail photo = getGalleryBm().getPhoto(photoPK);
    photo.setCreatorName(getUserDetail(photo.getCreatorId()).getDisplayedName());
    if (photo.getUpdateId() != null) {
      photo.setUpdateName(getUserDetail(photo.getUpdateId()).getDisplayedName());
    }

    // Mise à jour de l'album courant
    Collection<String> albumIds = getGalleryBm().getPathList(photo.getPhotoPK().getInstanceId(),
        photo.getId());
    SilverTrace.info("gallery", "GallerySessionController.getPhoto", "root.MSG_GEN_PARAM_VALUE",
        "albumIds = " + albumIds + ", currentAlbumId =  " + getCurrentAlbumId());

    // regarder si l'album courant est dans la liste des albums
    boolean inAlbum = false;
    boolean first = true;
    String firstAlbumId = "";
    for (String albumId : albumIds) {
      SilverTrace.info("gallery", "GallerySessionController.getPhoto", "root.MSG_GEN_PARAM_VALUE",
          "albumId = " + albumId);
      if (first) {
        firstAlbumId = albumId;
        first = false;
      }
      if (albumId.equals(currentAlbumId)) {
        inAlbum = true;
      }
    }
    if (!inAlbum) {
      setCurrentAlbumId(firstAlbumId);
    }

    SilverTrace.info("gallery", "GallerySessionController.getPhoto", "root.MSG_GEN_PARAM_VALUE",
        "currentAlbumId fin = " + currentAlbumId);
    // mise à jour du rang de la photo courante
    List<PhotoDetail> photos = currentAlbum.getPhotos();
    rang = photos.indexOf(photo);

    return photo;
  }

  public PhotoDetail getPrevious() {
    return getPreviousOrNext(true);
  }

  public PhotoDetail getNext() {
    return getPreviousOrNext(false);
  }

  private PhotoDetail getPreviousOrNext(boolean isPreviousRequired) {
    String type = isPreviousRequired ? "Previous" : "Next";
    int offset = isPreviousRequired ? -1 : 1;
    PhotoDetail photo;
    try {

      // rechercher le nouveau rang de la photo
      int newRang = rang + offset;
      photo = currentAlbum.getPhotos().get(newRang);

      // mettre à jour le rang de la photo courante
      rang = newRang;

    } catch (Exception e) {

      // traitement des exceptions
      throw new GalleryRuntimeException("GallerySessionController.get" + type + "()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return photo;
  }

  /**
   * Creating one photo (just only one)
   *
   * @param delegate
   * @return
   */
  public synchronized String createPhoto(
      final PhotoDataCreateDelegate delegate) {
    try {

      final PhotoDetail newPhoto = new PhotoDetail();

      // Persisting data
      getGalleryBm().createPhoto(getUserDetail(), getComponentId(), newPhoto, isMakeWatermark(),
          getWatermarkHD(), getWatermarkOther(), delegate);

      // recharger l'album courant pour prendre en comptes la nouvelle photo
      goToAlbum(currentAlbumId);

      return newPhoto.getId();
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.createPhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Updating one photo (just only one)
   *
   * @param photoId
   * @param delegate
   * @throws Exception
   */
  public void updatePhotoByUser(final String photoId, final PhotoDataUpdateDelegate delegate)
      throws Exception {
    try {

      // Persisting data
      getGalleryBm().updatePhoto(getUserDetail(), getComponentId(), getPhoto(photoId),
          isMakeWatermark(), getWatermarkHD(), getWatermarkOther(), delegate);

    } catch (RemoteException e) {
      throw new GalleryRuntimeException("GallerySessionController.updatePhotoByUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Updating severals photos (no file have to be handled)
   *
   * @param photoIds
   * @param delegate
   * @throws Exception
   */
  public void updatePhotoByUser(final Collection<String> photoIds,
      final PhotoDataUpdateDelegate delegate) throws Exception {
    if (photoIds != null) {
      // Persisting data
      getGalleryBm().updatePhoto(getUserDetail(), getComponentId(), photoIds, getCurrentAlbumId(),
          delegate);
    }
  }

  public void updatePhoto(final PhotoDetail photo) {
    try {
      getGalleryBm().updatePhoto(getUserDetail(), getComponentId(), photo, isMakeWatermark(),
          getWatermarkHD(), getWatermarkOther(), null);
      // recharger l'album courant pour prendre en comptes la modif sur la photo
      loadCurrentAlbum();

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.updatePhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_METADATA", e);
    }
  }

  public void deletePhoto(String photoId) {
    deletePhoto(Collections.singletonList(photoId));
  }

  public void deletePhoto(final Collection<String> photoIds) {
    try {

      // Delete photos
      getGalleryBm().deletePhoto(getUserDetail(), getComponentId(), photoIds);

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.deletePhoto()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    // recharger l'album courant pour prendre en comptes la suppression de la photo
    loadCurrentAlbum();
  }

  private void sortPhotos() {
    sort(currentAlbum.getPhotos());
  }

  private void sortPhotosSearch() {
    sort((List<PhotoDetail>) getSearchResultListPhotos());
    sort((List<PhotoDetail>) getRestrictedListPhotos());
  }

  private void sort(final List<PhotoDetail> photos) {
    final Comparator<PhotoDetail> comparateur;
    if (tri.equals("CreationDateAsc")) {
      comparateur = new GSCCreationDateComparatorAsc();
    } else if (tri.equals("CreationDateDesc")) {
      comparateur = new GSCCreationDateComparatorDesc();
    } else if (tri.equals("Size")) {
      comparateur = new GSCSizeComparatorAsc();
    } else if (tri.equals("Title")) {
      comparateur = new GSCTitleComparatorAsc();
    } else if (tri.equals("Author")) {
      comparateur = new GSCAuthorComparatorAsc();
    } else {
      comparateur = null;
    }
    if (comparateur != null) {
      Collections.sort(photos, comparateur);
    }
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param));
  }

  private static GalleryBm getGalleryBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
  }

  public Collection<NodeDetail> getPath(NodePK nodePK) {
    List<NodeDetail> path = (List<NodeDetail>) getGalleryBm().getPath(nodePK);
    Collections.reverse(path);
    return path;
  }

  public Collection<NodeDetail> getPath() {
    return getPath(new NodePK(getCurrentAlbumId(), getComponentId()));
  }

  public synchronized Collection<String> getPathList(String photoId) {
    return getGalleryBm().getPathList(getComponentId(), photoId);
  }

  public int getNbPhotosPerPage() {
    int nbPhotosPerPage = 15;
    if ("66x50".equals(taille)) {
      nbPhotosPerPage = 35;
    } else if ("133x100".equals(taille)) {
      nbPhotosPerPage = 15;
    } else if ("266x150".equals(taille)) {
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
    this.indexOfFirstItemToDisplay = Integer.parseInt(index);
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
    return "yes".equalsIgnoreCase(getComponentParameterValue("dragAndDrop"));
  }

  public Boolean isUsePdc() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("usePdc"));
  }

  public Boolean isViewMetadata() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("viewMetadata"));
  }

  public Boolean isMakeWatermark() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("watermark"));
  }

  public String getWatermarkHD() {
    String watermarkHD = getComponentParameterValue("WatermarkHD");
    if (StringUtil.isInteger(watermarkHD)) {
      return watermarkHD;
    }
    return StringUtil.EMPTY;
  }

  public String getWatermarkOther() {
    String watermarkOther = getComponentParameterValue("WatermarkOther");
    if (StringUtil.isInteger(watermarkOther)) {
      return watermarkOther;
    }
    return StringUtil.EMPTY;
  }

  public Boolean isViewList() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("viewList"));
  }

  public Boolean areCommentsEnabled() {
    return !"no".equalsIgnoreCase(getComponentParameterValue("comments"));
  }

  public String getPreviewSize() {
    String previewSize = getComponentParameterValue("previewSize");
    if (!StringUtil.isDefined(previewSize)) {
      previewSize = "preview";
    }
    return previewSize;
  }

  public Integer getSlideshowWait() {
    String wait = getComponentParameterValue("slideshow");
    if (wait == null || "null".equalsIgnoreCase(wait) || wait.length() == 0) {
      wait = "5";
    }
    Integer iWait = new Integer(wait);
    if (iWait <= 0) {
      iWait = 5;
    }
    return iWait;
  }

  public String getXMLFormName() {
    String formName = getComponentParameterValue("XMLFormName");
    // contrôle du formulaire et retour du nom si convenable
    if (isDefined(formName)) {
      try {
        String xmlFormShortName = formName.substring(formName.indexOf("/") + 1, formName
            .indexOf("."));
        getPublicationTemplateManager().getPublicationTemplate(getComponentId()
            + ":" + xmlFormShortName, formName);
      } catch (PublicationTemplateException e) {
        formName = null;
      }
    }
    return formName;
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
    // selectionPeas (extra param permettant de filtrer les users
    // ayant acces au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set
    // nom du composant pour browsebar (PairObject(nom_composant, lien_vers_composant))
    // NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours
    // présenté en popup => pas de lien sur nom du composant)
    sel.setHostComponentName(hostComponentName);
    SilverTrace.debug("gallery", "GallerySessionController.initAlertUser()",
        "root.MSG_GEN_PARAM_VALUE", "name = " + hostComponentName + " componentId="
        + getComponentId());
    sel.setNotificationMetaData(getAlertNotificationMetaData(photoId)); // set
    // NotificationMetaData contenant les informations à notifier fin initialisation de
    // AlertUser l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  public Collection<PhotoDetail> search(QueryDescription query) {
    query.setSearchingUser(getUserId());
    query.addComponent(getComponentId());
    Collection<PhotoDetail> result = getGalleryBm().search(query);
    // mise à jour de la liste
    isSearchResult = true;
    // sauvegarde de la recherche
    setQuery(query);
    return result;
  }

  public void sendAskPhoto(String order) {
    // envoyer une notification au gestionnaire de la photothèque concernant la
    // demande de photo
    // 1. création du message
    OrganisationController orga = getOrganisationController();
    UserDetail[] admins = orga.getUsers("useless", getComponentId(), "admin");
    String user = getUserDetail().getDisplayedName();

    ResourceLocator message = new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "org.silverpeas.gallery.multilang.galleryBundle", "en");

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
    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());
    for (UserDetail admin : admins) {
      notifMetaData.addUserRecipient(new UserRecipient(admin));
    }
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "Main");

    // 2. envoie de la notification aux admin
    notifyUsers(notifMetaData);
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String photoId)
      throws RemoteException {
    PhotoPK photoPK = new PhotoPK(photoId, getSpaceId(), getComponentId());
    NodePK nodePK = currentAlbum.getNodePK();
    String senderName = getUserDetail().getDisplayedName();
    PhotoDetail photoDetail = getPhoto(photoPK.getId());
    String htmlPath = getGalleryBm().getHTMLNodePath(nodePK);

    ResourceLocator message = new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "org.silverpeas.gallery.multilang.galleryBundle", "en");

    String subject = getNotificationSubject(message);
    String body = getNotificationBody(photoDetail, htmlPath, message, senderName);

    // english notifications
    String subject_en = getNotificationSubject(message_en);
    String body_en = getNotificationBody(photoDetail, htmlPath, message_en, senderName);

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, body);
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
    StringBuilder messageText = new StringBuilder();
    messageText.append(senderName).append(" ");
    messageText.append(message.getString("gallery.notifInfo")).append("\n\n");
    messageText.append(message.getString("gallery.notifName")).append(" : ").append(
        photoDetail.getName()).append("\n");
    if (isDefined(photoDetail.getDescription())) {
      messageText.append(message.getString("gallery.notifDesc")).append(" : ").append(
          photoDetail.getDescription()).append("\n");
    }
    messageText.append(message.getString("gallery.path")).append(" : ").append(htmlPath);
    return messageText.toString();
  }

  private String getPhotoUrl(PhotoDetail photoDetail) {
    return URLManager.getURL(null, getComponentId()) + photoDetail.getURL();
  }

  public Collection<String> getListSelected() {
    // restitution de la collection des photos selectionnées
    SilverTrace.info("gallery", "GallerySessionControler.getListSelected()", "",
        "listSelected (taille) = (" + listSelected.size() + ") " + listSelected.toString());
    return listSelected;
  }

  public void clearListSelected() {
    listSelected.clear();
  }

  public Collection<PhotoDetail> getRestrictedListPhotos() {
    return restrictedListPhotos;
  }

  public void setRestrictedListPhotos(Collection<PhotoDetail> restrictedListPhotos) {
    this.restrictedListPhotos = (restrictedListPhotos == null ? null
        : new ArrayList<PhotoDetail>(restrictedListPhotos));
  }

  public Collection<PhotoDetail> getSearchResultListPhotos() {
    return searchResultListPhotos;
  }

  public void setSearchResultListPhotos(Collection<PhotoDetail> searchResultListPhotos) {
    this.searchResultListPhotos = (searchResultListPhotos == null ? null
        : new ArrayList<PhotoDetail>(searchResultListPhotos));
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
    return "yes".equalsIgnoreCase(getComponentParameterValue("privateSearch"));
  }

  public boolean isAlbumAdmin(String profile, String albumId, String userId) {
    if (albumId == null) {
      return isAdminOrPublisher(profile);
    }
    // rechercher le créateur de l'album
    AlbumDetail album = getAlbum(albumId);
    return ("admin".equals(profile) || ("publisher".equals(profile) && album.getCreatorId().equals(
        userId)));
  }

  public boolean isPhotoAdmin(String profile, String photoId, String userId) throws RemoteException {
    if (photoId == null) {
      return (isAdminOrPublisher(profile) || "writer".equals(profile));
    }
    // rechercher le créateur de la photo
    PhotoDetail photo = getPhoto(photoId);
    return (isAdminOrPublisher(profile) || ("writer".equals(profile) && photo.getCreatorId().
        equals(userId)));
  }

  public void copySelectedPhoto(Collection<String> photoIds) throws ClipboardException,
      RemoteException {
    for (String photoId : photoIds) {
      copyImage(photoId);
    }
  }

  public void cutSelectedPhoto(Collection<String> photoIds) throws ClipboardException,
      RemoteException {
    for (String photoId : photoIds) {
      cutImage(photoId);
    }
  }

  public void copyImage(String photoId) throws ClipboardException, RemoteException {
    PhotoDetail photo = getPhoto(photoId);
    PhotoSelection photoSelect = new PhotoSelection(photo);
    SilverTrace.info("gallery", "GallerySessionController.copyImage()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(photoSelect);
  }

  public void cutImage(String photoId) throws ClipboardException, RemoteException {
    PhotoDetail photo = getPhoto(photoId);
    PhotoSelection photoSelect = new PhotoSelection(photo);
    photoSelect.setCutted(true);

    SilverTrace.info("gallery", "GallerySessionController.cutPhoto()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(photoSelect);
  }

  public void copyAlbum(String albumId) throws ClipboardException {
    AlbumDetail album = getAlbum(albumId);
    NodeSelection nodeSelect = new NodeSelection(album);
    SilverTrace.info("gallery", "GallerySessionController.copyAlbum()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    SilverTrace.info("gallery", "GallerySessionController.copyAlbum()", "root.MSG_GEN_PARAM_VALUE",
        "nodeSelect = " + nodeSelect.toString() + "' albumId =" + album.getId());
    addClipboardSelection(nodeSelect);
  }

  public void cutAlbum(String albumId) throws ClipboardException {
    NodeSelection nodeSelect = new NodeSelection(getAlbum(albumId));
    nodeSelect.setCutted(true);

    SilverTrace.info("gallery", "GallerySessionController.cutAlbum()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(nodeSelect);
  }

  public void paste() throws ClipboardException {
    try {
      GalleryPasteDelegate delegate = new GalleryPasteDelegate(currentAlbum);
      SilverTrace.info("gallery", "GalleryRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
          "clipboard = " + getClipboardName() + " count=" + getClipboardCount());

      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      Map<Object, ClipboardSelection> clipObjectPerformed
          = new HashMap<Object, ClipboardSelection>();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(PhotoSelection.PhotoDetailFlavor)) {
            PhotoDetail photo = (PhotoDetail) clipObject.getTransferData(
                PhotoSelection.PhotoDetailFlavor);

            delegate.addPhoto(photo, clipObject.isCutted());
            clipObjectPerformed.put(photo.getPhotoPK(), clipObject);
          }
          if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
            AlbumDetail album = (AlbumDetail) clipObject.getTransferData(
                NodeSelection.NodeDetailFlavor);
            SilverTrace.info("gallery", "GalleryRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
                "albumId = " + album.getId());

            delegate.addAlbum(album, clipObject.isCutted());
            clipObjectPerformed.put(album.getNodePK(), clipObject);
          }
        }
      }

      // Persisting the paste operation
      getGalleryBm().paste(getUserDetail(), getComponentId(), delegate);

      // End of treatment
      CallBackManager callBackManager = CallBackManager.get();
      for (Map.Entry<Object, ClipboardSelection> entry : clipObjectPerformed.entrySet()) {
        if (entry.getValue().isCutted()) {
          callBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE, Integer.parseInt(
              getUserId()), getComponentId(), entry.getKey());
        }
      }

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.paste()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_PASTE_ERROR", e);
    }
    clipboardPasteDone();
  }

  public Collection<PhotoDetail> getAllPhotos(AlbumDetail album) {
    try {
      return getGalleryBm().getAllPhoto(album.getNodePK(), viewAllPhoto);
    } catch (Exception e) {
      return null;
    }
  }

  public void sendAskOrder(String orderId) {
    // envoyer une notification au gestionnaire pour le prévenir de la demande
    // de l'utilisateur
    // 1. création du message

    OrganisationController orga = getOrganisationController();
    UserDetail[] admins = orga.getUsers("useless", getComponentId(), "admin");
    String user = getUserDetail().getDisplayedName();

    ResourceLocator message = new ResourceLocator("com.silverpeas.gallery.multilang.galleryBundle",
        "fr");
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

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    for (UserDetail admin : admins) {
      notifMetaData.addUserRecipient(new UserRecipient(admin));
    }
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "OrderView?OrderId="
        + orderId);

    // 2. envoie de la notification aux admin
    notifyUsers(notifMetaData);
  }

  public void sendAskOrderUser(String orderId) throws RemoteException {
    // envoyer une notification au lecteur pour le prévenir du traitement de sa
    // demande
    // 1. création du message
    Order order = getOrder(orderId);
    String user = getOrganisationController().getUserDetail(Integer.toString(order.
        getProcessUserId()))
        .getDisplayedName();

    ResourceLocator message = new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "org.silverpeas.gallery.multilang.galleryBundle", "en");

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

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.addUserRecipient(new UserRecipient(String.valueOf(order.getUserId())));
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "OrderView?OrderId="
        + orderId);

    // 2. envoie de la notification aux admin
    notifyUsers(notifMetaData);
  }

  public void addToBasket() {
    // ajout dans le panier toutes les photos sélectionnées
    basket.addAll(listSelected);
    // remettre à blanc la liste des photos sélectionnées
    SilverTrace.debug("gallery", "GallerySessionController.addToBasket()",
        "root.MSG_GEN_PARAM_VALUE", "listSelected = " + listSelected.toString() + " basket = "
        + basket.toString());
    clearListSelected();
  }

  public void addPhotoToBasket(String photoId) {
    // ajout dans le panier la photo
    if (!basket.contains(photoId)) {
      basket.add(photoId);
    }
  }

  public void deleteToBasket() {
    // suppression dans le panier
    for (String photoId : listSelected) {
      // rechercher la photo dans le panier et la supprimer
      basket.remove(photoId);

    }
    clearListSelected();
  }

  public void deleteToBasket(String photoId) {
    basket.remove(photoId);
  }

  public void deleteBasket() {
    // suppression de toutes les photos du panier
    basket.clear();
  }

  public List<String> getBasketListPhotos() {
    return basket;
  }

  public Order getOrder(String orderId) throws RemoteException {
    Order order = getGalleryBm().getOrder(orderId, getComponentId());
    List<OrderRow> rows = order.getRows();
    List<OrderRow> newRows = new ArrayList<OrderRow>();
    for (OrderRow row : rows) {
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
    String orderId = getGalleryBm().createOrder(basket, getUserId(), getComponentId());
    // vider le panier
    basket.clear();
    return orderId;
  }

  public void updateOrderRow(String orderId, String photoId) throws RemoteException {
    Order order = getOrder(orderId);
    List<OrderRow> rows = order.getRows();
    for (OrderRow row : rows) {
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
    List<OrderRow> rows = order.getRows();
    for (OrderRow row : rows) {
      if (row.getPhotoId() == Integer.parseInt(photoId)) {
        // on est sur la bonne ligne
        PhotoDetail photo = getPhoto(Integer.toString(row.getPhotoId()));
        String nomRep = getSettings().getString("imagesSubDirectory") + photoId;
        String download = row.getDownloadDecision();

        if (!download.equals("T")) {
          // la photo n'a pas déjà été téléchargée
          // par defaut photo sans watermark
          String title = EncodeHelper.javaStringToHtmlString(photo.getImageName());
          if (download.equals("DW")) {
            // demande avec Watermark
            title = photoId + "_watermark.jpg";
            // regarder si la photo existe, sinon prendre celle sans watermark
            String pathFile = FileRepositoryManager.getAbsolutePath(getComponentId()) + nomRep
                + File.separator;
            String watermarkFile = pathFile + title;
            File file = new File(watermarkFile);
            if (!file.exists()) {
              title = EncodeHelper.javaStringToHtmlString(photo.getImageName());
            }
          }
          url = FileServerUtils.getUrl(getComponentId(), getUrlEncodedParameter(
              title), photo.getImageMimeType(), nomRep);
        }
      }
    }
    return url;
  }

  public boolean isAccessAuthorized(String orderId) throws RemoteException {
    return Integer.toString(getOrder(orderId).getUserId()).equals(getUserId());
  }

  public List<MetaData> getMetaDataKeys() {
    List<MetaData> metaDatas = new ArrayList<MetaData>();
    try {
      FileUtil.loadProperties(settings, "org/silverpeas/gallery/settings/metadataSettings_"
          + getComponentId() + ".properties");
    } catch (Exception e) {
      settings = defaultSettings;
    }
    String display = settings.getProperty("display");
    Iterable<String> propertyNames = StringUtil.splitString(display, ',');

    for (String value : propertyNames) {
      if (value.startsWith("IPTC_")) {
        String property = settings.getProperty(value + "_TAG");
        String label = settings.getProperty(value + "_LABEL");
        label = getMetadataResources().getString(label);
        boolean isSearch = StringUtil.getBooleanValue(settings.getProperty(value + "_SEARCH"));
        if (isSearch) {
          boolean isDate = StringUtil.getBooleanValue(settings.getProperty(value + "_DATE"));
          MetaData metaData = new MetaData();
          metaData.setProperty(property);
          metaData.setDate(isDate);
          metaData.setLabel(label);
          // rechercher sa valeur dans la query (résultat de la recherche)
          List<FieldDescription> metadataValue = query.getMultiFieldQuery();
          if (metadataValue != null) {
            for (FieldDescription field : metadataValue) {
              if (field.getFieldName().equals("IPTC_" + metaData.getProperty())) {
                metaData.setValue(field.getContent());
              }

            }
          }
          // ajout de cette metadata à la liste
          metaDatas.add(metaData);
        }
      }
    }
    return metaDatas;
  }

  public void updateOrder(Order order) throws RemoteException {
    getGalleryBm().updateOrder(order);
  }

  public List<Order> getAllOrders() throws RemoteException {
    return getGalleryBm().getAllOrders("-1", getComponentId());
  }

  public List<Order> getOrdersByUser() throws RemoteException {
    return getGalleryBm().getAllOrders(getUserId(), getComponentId());
  }

  public String getOrderForm() {
    return getComponentParameterValue("XMLOrderFormName");
  }

  public String getCharteUrl() {
    String url = getComponentParameterValue("UrlCharte");
    if (!StringUtil.isDefined(url)) {
      url = "";
    }
    return url;
  }

  public Boolean isBasket() {
    return !isGuest() && StringUtil.getBooleanValue(getComponentParameterValue("basket"));
  }

  public Boolean isGuest() {
    return getUserDetail().isAccessGuest();
  }

  public Boolean isOrder() {
    return !isGuest() && StringUtil.getBooleanValue(getComponentParameterValue("order"));
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

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  public static void sortAlbums(List<NodePK> albumPKs) {
    try {
      getGalleryBm().sortAlbums(albumPKs);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.sortAlbums()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  // recherche du profile de l'utilisateur
  public String getRole() {
    String[] profiles = getUserRoles();
    String flag = "user";
    for (final String profile : profiles) {
      if (profile.equals("admin")) {
        return profile;
      }
      if (profile.equals("publisher")) {
        flag = profile;
      } else if (profile.equals("writer")) {
        if (!flag.equals("publisher")) {
          flag = profile;
        }
      } else if (profile.equals("privilegedUser")) {
        flag = profile;
      }
    }
    return flag;
  }

  public Collection<AlbumDetail> addNbPhotos(Collection<AlbumDetail> albums) {
    // retourne la liste des albums avec leurs nombre de photos
    for (AlbumDetail album : albums) {
      // pour chaque sous album, rechercher le nombre de photos
      int nbPhotos;
      Collection<PhotoDetail> allPhotos = getAllPhotos(album);
      nbPhotos = allPhotos.size();
      // parcourir ses sous albums pour comptabiliser aussi ses photos
      AlbumDetail thisAlbum = getAlbumLight(Integer.toString(album.getId()));
      Collection<AlbumDetail> subAlbums = addNbPhotos(thisAlbum.getChildrenAlbumsDetails());
      for (AlbumDetail oneSubAlbum : subAlbums) {
        nbPhotos = nbPhotos + oneSubAlbum.getNbPhotos();
      }
      album.setNbPhotos(nbPhotos);
    }
    return albums;
  }

  public void notifyUsers(NotificationMetaData notifMetaData) {
    try {
      notifMetaData.setSender(getUserId());
      notifMetaData.setComponentId(getComponentId());
      NotificationSender notifSender = new NotificationSender(getComponentId());
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new GalleryRuntimeException("GallerySessionController.notifyUsers()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }
}
