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
package com.silverpeas.gallery.servlets;

import java.io.File;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.XmlSearchForm;
import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.ParameterNames;
import com.silverpeas.gallery.control.GallerySessionController;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.gallery.model.OrderRow;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.peasUtil.AccessForbiddenException;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FieldDescription;
import com.stratelia.webactiv.util.node.model.NodeDetail;

public class GalleryRequestRouter extends ComponentRequestRouter {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "Gallery";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new GallerySessionController(mainSessionCtrl, componentContext);
  }

  // recherche du profile de l'utilisateur
  public String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      } else if (profiles[i].equals("writer")) {
        if (!flag.equals("publisher")) {
          flag = profiles[i];
        }
      } else if (profiles[i].equals("privilegedUser")) {
        flag = profiles[i];
      }
    }
    return flag;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    String rootDest = "/gallery/jsp/";

    GallerySessionController gallerySC = (GallerySessionController) componentSC;
    request.setAttribute("gallerySC", gallerySC);
    SilverTrace.info("gallery", "GalleryRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

    // création des paramètres généraux
    String flag = getFlag(gallerySC.getUserRoles());
    String userId = gallerySC.getUserId();

    request.setAttribute("Profile", flag);
    request.setAttribute("UserId", userId);
    request.setAttribute("IsGuest", gallerySC.isGuest());

    SilverTrace.debug("gallery", "GalleryRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Profile=" + flag);

    try {
      if (function.startsWith("Main")) {
        // récupération des albums de 1er niveau
        gallerySC.setIndexOfFirstItemToDisplay("0");
        request.setAttribute("root", gallerySC.goToAlbum("0"));
        // chercher les dernières photos
        boolean viewAllPhoto = false;
        if (flag.equals("admin"))
          viewAllPhoto = true;
        gallerySC.setViewAllPhoto(viewAllPhoto);
        Collection<PhotoDetail> photos = gallerySC.getDernieres();
        request.setAttribute("Photos", photos);
        request.setAttribute("IsUsePdc", gallerySC.isUsePdc());
        request.setAttribute("IsPrivateSearch", gallerySC.isPrivateSearch());
        request.setAttribute("IsBasket", gallerySC.isBasket());
        request.setAttribute("IsOrder", gallerySC.isOrder());
        // appel jsp
        destination = rootDest + "accueil.jsp";
      } else if (function.equals("ViewAlbum")) {
        // récupération de l'Id de l'album en cours
        String albumId = request.getParameter("Id");
        gallerySC.goToAlbum(albumId);
        gallerySC.setIndexOfFirstItemToDisplay("0");
        // retour à l'album courant
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.equals("Pagination")) {
        processSelection(request, gallerySC);

        // traitement de la pagination : passage des paramètres
        String index = request.getParameter("Index");
        if (index != null && index.length() > 0)
          gallerySC.setIndexOfFirstItemToDisplay(index);
        // retour d'ou on viens
        if (!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible()) {
          // retour à l'album en cours
          destination = getDestination("GoToCurrentAlbum", gallerySC, request);
        } else {
          // retour aux résultats de recherche ou à la liste des photos non visibles
          if (gallerySC.isViewNotVisible())
            destination = getDestination("ViewNotVisible", gallerySC, request);
          else
            destination = getDestination("SearchKeyWord", gallerySC, request);
        }

      } else if (function.equals("GoToCurrentAlbum")) {
        // mise à blanc de l'index de pagination si on arrive de la recherche
        if (gallerySC.isSearchResult() || gallerySC.isViewNotVisible()) {
          gallerySC.setIndexOfFirstItemToDisplay("0");
        }
        // mise à blanc de la liste des photos (pour les mots clé et pour les photos non visibles)
        gallerySC.setRestrictedListPhotos(new ArrayList<PhotoDetail>());
        gallerySC.setSearchResult(false);
        gallerySC.setViewNotVisible(false);

        // récupération de l'album en cours
        String currentAlbumId = gallerySC.getCurrentAlbumId();
        if (currentAlbumId.equals("0")) {
          // on est à la racine, on retourne à l'accueil
          destination = getDestination("Main", gallerySC, request);
        } else {
          // on est dans un album, on y retourne
          boolean viewAllPhoto = false;
          if (flag.equals("admin"))
            viewAllPhoto = true;
          gallerySC.setViewAllPhoto(viewAllPhoto);
          AlbumDetail currentAlbum = gallerySC.goToAlbum();
          request.setAttribute("NbPhotosPerPage", new Integer(gallerySC.getNbPhotosPerPage()));
          request.setAttribute("FirstPhotoIndex", new Integer(gallerySC
              .getIndexOfFirstItemToDisplay()));
          request.setAttribute("CurrentAlbum", currentAlbum);
          request.setAttribute("Path", gallerySC.getPath(currentAlbum.getNodePK()));
          request.setAttribute("Taille", gallerySC.getTaille());
          request.setAttribute("DragAndDropEnable", gallerySC.isDragAndDropEnabled());
          request.setAttribute("IsViewMetadata", gallerySC.isViewMetadata());
          request.setAttribute("IsViewList", gallerySC.isViewList());
          request.setAttribute("SelectedIds", gallerySC.getListSelected());
          request.setAttribute("Tri", gallerySC.getTri());
          request.setAttribute("IsUsePdc", gallerySC.isUsePdc());
          request.setAttribute("IsBasket", gallerySC.isBasket());
          request.setAttribute("IsOrder", gallerySC.isOrder());
          request.setAttribute("IsPrivateSearch", gallerySC.isPrivateSearch());

          // appel jsp
          destination = rootDest + "viewAlbum.jsp";
        }
      } else if (function.equals("NewAlbum")) {
        // passage de l'album courant : null car en création
        request.setAttribute("CurrentAlbum", null);
        request.setAttribute("Path", gallerySC.getPath());
        // appel jsp
        destination = rootDest + "albumManager.jsp";
      } else if (function.equals("CreateAlbum")) {
        // check user rights
        if (!gallerySC.isAlbumAdmin(flag, null, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.CreateAlbum",
              SilverpeasException.WARNING, null);

        // récupération des paramètres de l'album
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        // création de l'album
        NodeDetail node =
            new NodeDetail("unknown", name, description, null, null, null, "0", "unknown");
        AlbumDetail album = new AlbumDetail(node);
        gallerySC.createAlbum(album);
        // retour à l'album courant
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.equals("EditAlbum")) {
        // récupération des paramètres
        String albumId = request.getParameter("Id");

        // check user rights
        if (!gallerySC.isAlbumAdmin(flag, albumId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.EditAlbum",
              SilverpeasException.WARNING, null);

        // récupération de l'album courant
        AlbumDetail currentAlbum = gallerySC.getAlbum(albumId);
        // passage des paramètres
        request.setAttribute("CurrentAlbum", currentAlbum);
        request.setAttribute("Path", gallerySC.getPath());
        // appel jsp
        destination = rootDest + "albumManager.jsp";
      } else if (function.equals("UpdateAlbum")) {
        String albumId = request.getParameter("Id");

        // check user rights
        if (!gallerySC.isAlbumAdmin(flag, albumId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.UpdateAlbum",
              SilverpeasException.WARNING, null);

        // récupération des paramètres
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");

        // récupération de l'album en cours
        AlbumDetail album = gallerySC.getAlbum(albumId);
        // modification des valeurs
        album.setName(name);
        album.setDescription(description);

        // mise à jour de l'album courant
        gallerySC.updateAlbum(album);
        // retour à l'album courant
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.equals("DeleteAlbum")) {
        // récupération de l'Id de l'album
        String albumId = request.getParameter("Id");

        // check user rights
        if (!gallerySC.isAlbumAdmin(flag, albumId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.DeleteAlbum",
              SilverpeasException.WARNING, null);

        // suppression de l'album
        gallerySC.deleteAlbum(albumId);
        // retour à l'album courant
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.equals("AddPhoto")) {
        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, null, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.AddPhoto",
              SilverpeasException.WARNING, null);

        // passage des paramètres
        request.setAttribute("Photo", null);
        String repertoire = "";
        request.setAttribute("Repertoire", repertoire);
        request.setAttribute("Path", gallerySC.getPath());
        request.setAttribute("GetLanguage", gallerySC.getLanguage());
        request.setAttribute("UserName", gallerySC.getUserDetail(userId).getDisplayedName());
        request.setAttribute("NbComments", new Integer(0));
        request.setAttribute("IsUsePdc", gallerySC.isUsePdc());
        request.setAttribute("XMLFormName", gallerySC.getXMLFormName());
        request.setAttribute("ShowCommentsTab", gallerySC.areCommentsEnabled());
        request.setAttribute("IsBasket", gallerySC.isBasket());
        request.setAttribute("IsOrder", gallerySC.isOrder());
        request.setAttribute("IsViewMetadata", gallerySC.isViewMetadata());

        // récupération du formulaire et affichage
        String xmlFormName = gallerySC.getXMLFormName();
        String xmlFormShortName = null;
        Form formUpdate = null;
        DataRecord data = null;
        if (isDefined(xmlFormName)) {
          xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(gallerySC
              .getComponentId() +
              ":" + xmlFormShortName, xmlFormName);
          formUpdate = pubTemplate.getUpdateForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getEmptyRecord();
        }
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);

        // appel jsp
        // destination = rootDest + "photoManager.jsp";
        destination = rootDest + "information.jsp";
      } else if (function.equals("CreatePhoto")) {
        // création de la photo dans la base de donnée
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);
        String photoId = createPhoto(parameters, gallerySC, request.getCharacterEncoding());

        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.CreatePhoto",
              SilverpeasException.WARNING, null);

        // création du répertoire sur disque contenant la photo ainsi que les vignettes
        processPhoto(photoId, parameters, gallerySC);

        // preview de la nouvelle image
        request.setAttribute("PhotoId", photoId);
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("EditPhoto")) {
        // récupération des paramètres
        String photoId = request.getParameter("PhotoId");

        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.EditPhoto",
              SilverpeasException.WARNING, null);

        // récupération de la photo
        PhotoDetail photo = gallerySC.getPhoto(photoId);

        // passage des paramètres
        putPhotoCommonParameters(request, gallerySC, photo, flag);

        String repertoire =
            gallerySC.getSettings().getString("imagesSubDirectory") + photo.getPhotoPK().getId();
        request.setAttribute("Repertoire", repertoire);

        // appel jsp
        destination = rootDest + "photoManager.jsp";
      } else if (function.equals("UpdatePhoto")) {
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);
        String photoId =
            FileUploadUtil
            .getParameter(parameters, "PhotoId", null, request.getCharacterEncoding());
        // gallerySC.setCurrentAlbumId(albumId);
        updateHeaderImage(photoId, parameters, gallerySC, request.getCharacterEncoding());
        // retour à la preview
        request.setAttribute("PhotoId", photoId);
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("DeletePhoto")) {
        // récupération de l'Id de la photo
        String photoId = request.getParameter("PhotoId");

        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.DeletePhoto",
              SilverpeasException.WARNING, null);

        // suppression de la photo
        gallerySC.deletePhoto(photoId);

        // retour à l'album courant
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.equals("UpdateInformation")) {
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        List<FileItem> parameters = FileUploadUtil.parseRequest(request);
        String photoId =
            FileUploadUtil
            .getParameter(parameters, "PhotoId", null, request.getCharacterEncoding());
        updateHeaderImage(photoId, parameters, gallerySC, request.getCharacterEncoding());

        // récupération du formulaire
        updateXMLFormImage(photoId, parameters, gallerySC);

        // retour à la preview
        request.setAttribute("PhotoId", photoId);
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("PreviewPhoto")) {
        // mise à blanc de la liste restreintes des photos (pour les photos non visibles)
        gallerySC.setRestrictedListPhotos(new ArrayList<PhotoDetail>());
        // remise à blanc des photos selectionnées
        gallerySC.clearListSelected();
        // récupération des paramètres
        String photoId = request.getParameter("PhotoId");
        if (photoId == null || photoId.length() == 0 || "null".equals(photoId))
          photoId = (String) request.getAttribute("PhotoId");
        PhotoDetail photo = null;
        request.setAttribute("IsPrivateSearch", gallerySC.isPrivateSearch());
        try {
          photo = gallerySC.getPhoto(photoId);
          request.setAttribute("Rang", new Integer(gallerySC.getRang()));
          request.setAttribute("NbPhotos", new Integer(gallerySC.goToAlbum().getPhotos().size()));

          SilverTrace.debug("gallery", "GalleryRequestRouter.getDestination()", "", "rang = " +
              new Integer(gallerySC.getRang()) + " nb photos = " +
              new Integer(gallerySC.goToAlbum().getPhotos().size()));

          request.setAttribute("IsViewMetadata", gallerySC.isViewMetadata());
          request.setAttribute("IsWatermark", gallerySC.isMakeWatermark());

          boolean linkDownload =
              "admin".equals(flag) || "publisher".equals(flag) || "privilegedUser".equals(flag) ||
              ("writer".equals(flag) && photo.getCreatorId().equals(gallerySC.getUserId()));
          request.setAttribute("ViewLinkDownload", new Boolean(linkDownload));

          putPhotoCommonParameters(request, gallerySC, photo, flag);

          // taille pour l'affichage de la preview
          request.setAttribute("PreviewSize", gallerySC.getPreviewSize());

          // pour l'affichage du formulaire
          putXMLDisplayerIntoRequest(photo, request, gallerySC);
          // appel jsp
          destination = rootDest + "preview.jsp";
        } catch (Exception e) {
          destination = getDocumentNotFoundDestination(gallerySC, request);
        }
      } else if (function.equals("PreviousPhoto")) {
        // récupération de la photo précédente
        PhotoDetail photo = gallerySC.getPrevious();
        request.setAttribute("PhotoId", photo.getPhotoPK().getId());
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("NextPhoto")) {
        // récupération de la photo suivante
        PhotoDetail photo = gallerySC.getNext();
        request.setAttribute("PhotoId", photo.getPhotoPK().getId());
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("StartDiaporama")) {
        String debut = request.getParameter("Debut");
        int rang;
        if ("ok".equals(debut)) {
          rang = 0;
        } else {
          rang = gallerySC.getRang();
        }
        List<PhotoDetail> photos = (List<PhotoDetail>) gallerySC.goToAlbum().getPhotos();
        request.setAttribute("Photos", photos);
        request.setAttribute("Rang", new Integer(rang));
        request.setAttribute("Path", gallerySC.getPath());
        request.setAttribute("Wait", gallerySC.getSlideshowWait());
        destination = rootDest + "diaporama.jsp";
      } else if (function.equals("StopDiaporama")) {
        // récupération du rang de la photo
        int rang = Integer.parseInt(request.getParameter("Rang"));
        // recherche de l'Id de la photo en fonction de son rang
        String photoId = gallerySC.getPhotoId(rang);
        request.setAttribute("PhotoId", photoId);
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.startsWith("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        try {
          if (type.equals("Photo")) {
            // traitement des photos
            request.setAttribute("PhotoId", id);
            destination = getDestination("PreviewPhoto", gallerySC, request);
          } else if (type.startsWith("Comment")) {
            // traitement des commentaires
            request.setAttribute("PhotoId", id);
            destination = getDestination("Comments", gallerySC, request);
          } else if (type.equals("Node")) {
            // traitement des noeuds = les albums
            destination = getDestination("ViewAlbum", gallerySC, request);
          } else {
            destination = getDestination("GoToCurrentAlbum", gallerySC, request);
          }
        } catch (Exception e) {
          destination = getDocumentNotFoundDestination(gallerySC, request);
        }
      } else if (function.equals("Comments")) {
        // visualisation des commentaires
        request.setAttribute("Path", gallerySC.getPath());
        // récupération de la photo
        String photoId = request.getParameter("PhotoId");
        if (photoId == null || photoId.equals(""))
          photoId = request.getParameter("PubId");
        if (photoId == null || photoId.equals(""))
          photoId = (String) request.getAttribute("PhotoId");
        PhotoDetail photo = gallerySC.getPhoto(photoId);

        // passage des paramètres
        putPhotoCommonParameters(request, gallerySC, photo, flag);

        // appel de la jsp
        destination = rootDest + "comments.jsp";
      } else if (function.equals("AccessPath")) {

        // visualisation des emplacements
        request.setAttribute("Path", gallerySC.getPath());
        // récupération de la photo
        String photoId = request.getParameter("PhotoId");
        if (photoId == null || photoId.equals(""))
          photoId = request.getParameter("PubId");
        if (photoId == null || photoId.equals(""))
          photoId = (String) request.getAttribute("PhotoId");

        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.AccessPath",
              SilverpeasException.WARNING, null);

        PhotoDetail photo = gallerySC.getPhoto(photoId);

        // passage des paramètres
        putPhotoCommonParameters(request, gallerySC, photo, flag);

        request.setAttribute("PathList", gallerySC.getPathList(photoId));
        request.setAttribute("Albums", gallerySC.getAllAlbums());

        destination = rootDest + "photoPaths.jsp";
      } else if (function.equals("SelectPath")) {
        // modification de la liste des emplacements de la photo
        String[] albums = request.getParameterValues("albumChoice");
        String photoId = request.getParameter("PhotoId");

        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.SelectPath",
              SilverpeasException.WARNING, null);

        gallerySC.setPhotoPath(photoId, albums);
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("AskPhoto")) {
        // demande de photo auprès du gestionnaire de la photothèque
        destination = rootDest + "askPhoto.jsp";
      } else if (function.equals("SendAsk")) {
        // envoie d'une notification au gestionnaire
        // String title = request.getParameter("Title");
        String description = request.getParameter("Description");
        gallerySC.sendAskPhoto(description);
      } else if (function.equals("PdcPositions")) {

        // traitement du plan de classement
        request.setAttribute("Path", gallerySC.getPath());
        String photoId = request.getParameter("PhotoId");
        if (photoId == null || photoId.equals(""))
          photoId = request.getParameter("PubId");
        if (photoId == null || photoId.equals(""))
          photoId = (String) request.getAttribute("PhotoId");

        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.PdcPositions",
              SilverpeasException.WARNING, null);

        PhotoDetail photo = gallerySC.getPhoto(photoId);
        putPhotoCommonParameters(request, gallerySC, photo, flag);

        request.setAttribute("SilverObjetId", new Integer(gallerySC.getSilverObjectId(photoId)));

        destination = rootDest + "pdcPositions.jsp";
      } else if (function.equals("ChoiceSize")) {
        // traitement du choix des tailles
        String choix = request.getParameter("Choice");
        // mettre à jour la taille avec le choix
        gallerySC.setTaille(choix);
        // retourner au début de la liste des photos
        gallerySC.initIndex();
        // retour ... en fonction d'ou on viens
        if (!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible()) {
          // retour à l'album en cours
          destination = getDestination("GoToCurrentAlbum", gallerySC, request);
        } else {
          // retour à la liste des mots clés ou à la liste des photos non visibles
          if (gallerySC.isViewNotVisible())
            destination = getDestination("ViewNotVisible", gallerySC, request);
          else
            destination = getDestination("SearchKeyWord", gallerySC, request);

        }
      } else if (function.equals("SortBy")) {
        // traitement du tri
        String tri = request.getParameter("Tri");
        // retour ... en fonction d'ou on viens
        if ((!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible())) {
          gallerySC.setTri(tri);
          // retour à l'album en cours
          destination = getDestination("GoToCurrentAlbum", gallerySC, request);
        } else {
          gallerySC.setTriSearch(tri);
          // retour à la liste des mots clés ou à la liste des photos non visibles
          if (gallerySC.isViewNotVisible())
            destination = getDestination("ViewNotVisible", gallerySC, request);
          else
            destination = getDestination("SearchKeyWord", gallerySC, request);
        }
      } else if (function.equals("ToAlertUser")) {
        String photoId = request.getParameter("PhotoId");
        try {
          destination = gallerySC.initAlertUser(photoId);
        } catch (Exception e) {
          SilverTrace.warn("gallery", "GalleryRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if (function.equals("EditSelectedPhoto")) {
        // traitement par lot
        String albumId = request.getParameter("AlbumId");

        // check user rights
        if (!"admin".equals(flag) && !"publisher".equals(flag))
          throw new AccessForbiddenException("GalleryRequestRouter.EditSelectedPhoto",
              SilverpeasException.WARNING, null);

        String searchKeyWord = request.getParameter("SearchKeyWord");

        processSelection(request, gallerySC);

        // liste des photos sélectionnées
        if (gallerySC.getListSelected().size() > 0) {
          // passage des paramètres globaux
          request.setAttribute("AlbumId", albumId);
          request.setAttribute("SearchKeyWord", searchKeyWord);
          request.setAttribute("Path", gallerySC.getPath());
          request.setAttribute("GetLanguage", gallerySC.getLanguage());

          // passage des paramètres pour le formulaire
          String xmlFormName = gallerySC.getXMLFormName();
          String xmlFormShortName = null;
          if (isDefined(xmlFormName)) {
            xmlFormShortName =
                xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(
                gallerySC.getComponentId() + ":" + xmlFormShortName, xmlFormName);
            Form formUpdate = pubTemplate.getUpdateForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getEmptyRecord();
            request.setAttribute("Form", formUpdate);
            request.setAttribute("Data", data);
          }

          destination = rootDest + "selectedPhotoManager.jsp";
        } else {
          // pas de photos sélectionnées, on retourne d'ou on viens
          if ((!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible())) {
            // retour à l'album en cours
            destination = getDestination("GoToCurrentAlbum", gallerySC, request);
          } else {
            // retour à la liste des mots clés ou à la liste des photos non visibles
            if (gallerySC.isViewNotVisible())
              destination = getDestination("ViewNotVisible", gallerySC, request);
            else
              destination = getDestination("SearchKeyWord", gallerySC, request);
          }
        }
      } else if (function.equals("UpdateSelectedPhoto")) {
        // récupération des photos modifiées
        Collection<String> photoIds = gallerySC.getListSelected();

        // mise à jour des photos
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        updateSelectedPhoto(request, gallerySC, photoIds, request.getCharacterEncoding());

        // tout déselectionner
        gallerySC.setSelect(false);
        gallerySC.clearListSelected();

        // retour d'ou on viens
        if (!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible()) {
          // retour à l'album en cours
          destination = getDestination("GoToCurrentAlbum", gallerySC, request);
        } else {
          // retour aux résultats de recherche ou à la liste des photos non visibles
          if (gallerySC.isViewNotVisible())
            destination = getDestination("ViewNotVisible", gallerySC, request);
          else
            destination = getDestination("SearchKeyWord", gallerySC, request);
        }
      } else if (function.equals("DeleteSelectedPhoto")) {
        processSelection(request, gallerySC);

        // liste des photos sélectionnées
        if (gallerySC.getListSelected().size() > 0) {
          // récupération des photos à supprimer
          Collection<String> photoIds = gallerySC.getListSelected();

          // suppression des photos
          deleteSelectedPhoto(gallerySC, photoIds);

          // tout déselectionner
          gallerySC.setSelect(false);
          gallerySC.clearListSelected();

          // retour à l'album en cours
          destination = getDestination("GoToCurrentAlbum", gallerySC, request);
        }
      } else if (function.equals("CategorizeSelectedPhoto")) {
        processSelection(request, gallerySC);

        // liste des photos sélectionnées
        if (gallerySC.getListSelected().size() > 0) {
          List<String> selectedIds = (List<String>) gallerySC.getListSelected();

          // get silverObjectIds according to selected photoIds
          List<String> silverObjectIds = new ArrayList<String>();
          String selectedId = null;
          String silverObjectId = null;
          for (int s = 0; s < selectedIds.size(); s++) {
            selectedId = (String) selectedIds.get(s);

            silverObjectId = Integer.toString(gallerySC.getSilverObjectId(selectedId));

            silverObjectIds.add(silverObjectId);
          }

          request.setAttribute("ObjectIds", silverObjectIds);
          request.setAttribute("ComponentId", gallerySC.getComponentId());
          destination = "/RpdcClassify/jsp/ToAddPositions";

          // tout déselectionner
          gallerySC.setSelect(false);
          gallerySC.clearListSelected();
        } else {
          destination = rootDest + "closeWindow.jsp";
        }
      } else if (function.equals("GoToXMLForm")) {
        // visualisation du formulaire associé à la photo
        String photoId = request.getParameter("PhotoId");

        // récupération du formulaire et affichage
        String xmlFormName = gallerySC.getXMLFormName();
        String xmlFormShortName = null;
        if (isDefined(xmlFormName))
          xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

        PublicationTemplateImpl pubTemplate =
            (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(gallerySC
            .getComponentId() +
            ":" + xmlFormShortName, xmlFormName);
        Form formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(photoId);
        if (data == null) {
          data = recordSet.getEmptyRecord();
          data.setId(photoId);
        }

        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        request.setAttribute("XMLFormName", xmlFormName);

        // passage des paramètres
        putPhotoCommonParameters(request, gallerySC, gallerySC.getPhoto(photoId), flag);

        destination = rootDest + "xmlForm.jsp";
      } else if (function.equals("UpdateXMLForm")) {
        // mise à jour des données du formulaire
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        List<FileItem> items = FileUploadUtil.parseRequest(request);
        String photoId =
            FileUploadUtil.getParameter(items, "PhotoId", null, request.getCharacterEncoding());
        // check user rights
        if (!gallerySC.isPhotoAdmin(flag, photoId, userId))
          throw new AccessForbiddenException("GalleryRequestRouter.UpdateXMLForm",
              SilverpeasException.WARNING, null);

        updateXMLFormImage(photoId, items, gallerySC);
        request.setAttribute("PhotoId", photoId);
        destination = getDestination("PreviewPhoto", gallerySC, request);
      } else if (function.equals("EditInformation")) {
        // récupération des paramètres
        String photoId = request.getParameter("PhotoId");
        // récupération de la photo
        PhotoDetail photo = gallerySC.getPhoto(photoId);

        // passage des paramètres
        putPhotoCommonParameters(request, gallerySC, photo, flag);

        request.setAttribute("IsViewMetadata", gallerySC.isViewMetadata());

        String repertoire =
            gallerySC.getSettings().getString("imagesSubDirectory") + photo.getPhotoPK().getId();
        request.setAttribute("Repertoire", repertoire);

        // récupération du formulaire et affichage
        String xmlFormName = gallerySC.getXMLFormName();
        String xmlFormShortName = null;
        Form formUpdate = null;
        DataRecord data = null;

        if (isDefined(xmlFormName)) {
          xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(gallerySC
              .getComponentId() +
              ":" + xmlFormShortName, xmlFormName);
          formUpdate = pubTemplate.getUpdateForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          data = recordSet.getRecord(photoId);
          if (data == null) {
            data = recordSet.getEmptyRecord();
            data.setId(photoId);
          }
        }

        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);

        // appel jsp
        destination = rootDest + "information.jsp";
      } else if (function.equals("AllSelected")) {
        // sélectionne (ou déselectionne) toutes les photos de l'album (ou de la liste restreinte
        // dans le cas de la recherche)
        boolean select = !gallerySC.getSelect();
        gallerySC.setSelect(select);

        Collection<PhotoDetail> photos = null;

        // retour d'ou on viens
        if (!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible()) {
          // retour à l'album
          photos = gallerySC.goToAlbum().getPhotos();

          if (select)
            gallerySC.getListSelected().addAll(extractIds(photos));
          else
            gallerySC.getListSelected().removeAll(extractIds(photos));

          destination = getDestination("GoToCurrentAlbum", gallerySC, request);

        } else {
          // retour sur les résultats de la recherche ou à la liste des photos non visibles
          if (gallerySC.isViewNotVisible()) {
            photos = gallerySC.getNotVisible();

            if (select)
              gallerySC.getListSelected().addAll(extractIds(photos));
            else
              gallerySC.getListSelected().removeAll(extractIds(photos));

            gallerySC.setRestrictedListPhotos(photos);
            destination = getDestination("ViewNotVisible", gallerySC, request);
          } else {
            photos = gallerySC.getSearchResultListPhotos();

            if (select)
              gallerySC.getListSelected().addAll(extractIds(photos));
            else
              gallerySC.getListSelected().removeAll(extractIds(photos));

            destination = getDestination("ViewSearchResults", gallerySC, request);

          }
        }
      } else if (function.equals("SearchAdvanced")) {
        // recherche avancée
        request.setAttribute("MetaDataKeys", gallerySC.getMetaDataKeys());

        // récupérer le contexte
        QueryDescription query = gallerySC.getQuery();

        // passage des paramètre pour garder le contexte
        String keyWord = query.getQuery();
        request.setAttribute("KeyWord", keyWord);

        // contexte de recherche du PDC
        request.setAttribute("PDCSearchContext", gallerySC.getPDCSearchContext());

        // pour les formulaires
        String xmlFormName = gallerySC.getXMLFormName();
        String xmlFormShortName = null;
        if (isDefined(xmlFormName)) {
          xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(gallerySC
              .getComponentId() +
              ":" + xmlFormShortName, xmlFormName);
          Form form = pubTemplate.getSearchForm();

          // get previous search
          DataRecord data = gallerySC.getXMLSearchContext();
          if (data == null) {
            RecordSet recordSet = pubTemplate.getRecordSet();
            data = recordSet.getEmptyRecord();
          }
          request.setAttribute("Form", form);
          request.setAttribute("Data", data);
        }

        destination = rootDest + "searchAdvanced.jsp";
      } else if (function.equals("ClearSearch")) {
        gallerySC.clearSearchContext();

        destination = getDestination("SearchAdvanced", gallerySC, request);
      } else if (function.equals("LastResult")) {
        // mise à jour du compteur de paginiation
        gallerySC.setIndexOfFirstItemToDisplay("0");

        destination = getDestination("ViewSearchResults", gallerySC, request);
      } else if (function.equals("SearchKeyWord")) {
        // traitement de la recherche par mot clé
        // et de la recherche dédiée
        // récupération du mot clé et de la liste des photos concernées si elle existe
        String searchKeyWord = request.getParameter("SearchKeyWord");
        if (searchKeyWord == null)
          searchKeyWord = (String) request.getAttribute("SearchKeyWord");
        if (!isDefined(searchKeyWord))
          searchKeyWord = gallerySC.getSearchKeyWord();

        // remise à blanc de la liste de recherche si on a changé de mot clé
        if (!gallerySC.getSearchKeyWord().equals(searchKeyWord)) {
          gallerySC.setSearchResultListPhotos(new ArrayList<PhotoDetail>());
          gallerySC.setSearchKeyWord(searchKeyWord);
          // mise à jour du compteur de pagination
          gallerySC.setIndexOfFirstItemToDisplay("0");
        }

        // mise à jour du tag pour les retours
        gallerySC.setViewNotVisible(false);
        gallerySC.setSearchResult(true);

        QueryDescription query = new QueryDescription(searchKeyWord);

        if (StringUtil.isDefined(query.getQuery()))
          gallerySC.setSearchResultListPhotos(gallerySC.search(query));

        destination = getDestination("ViewSearchResults", gallerySC, request);
      } else if (function.equals("PaginationSearch")) {
        processSelection(request, gallerySC);

        // traitement de la pagination : passage des paramètres
        String index = request.getParameter("Index");
        if (index != null && index.length() > 0)
          gallerySC.setIndexOfFirstItemToDisplay(index);

        destination = getDestination("ViewSearchResults", componentSC, request);
      } else if (function.equals("ViewSearchResults")) {
        // passage des paramètres
        request.setAttribute("SearchKeyWord", gallerySC.getSearchKeyWord());
        request.setAttribute("Photos", gallerySC.getSearchResultListPhotos());
        request.setAttribute("NbPhotosPerPage", new Integer(gallerySC.getNbPhotosPerPage()));
        request.setAttribute("FirstPhotoIndex", new Integer(gallerySC
            .getIndexOfFirstItemToDisplay()));
        request.setAttribute("Tri", gallerySC.getTri());
        request.setAttribute("Taille", gallerySC.getTaille());
        request.setAttribute("IsViewMetadata", gallerySC.isViewMetadata());
        request.setAttribute("IsViewList", gallerySC.isViewList());
        request.setAttribute("Tri", gallerySC.getTri());
        request.setAttribute("SelectedIds", gallerySC.getListSelected());
        request.setAttribute("IsBasket", gallerySC.isBasket());

        // mise à jour du tag pour les retours
        gallerySC.setSearchResult(true);
        gallerySC.setViewNotVisible(false);
        request.setAttribute("ViewVisible", new Boolean(gallerySC.isViewNotVisible()));

        // appel jsp
        destination = rootDest + "viewRestrictedPhotos.jsp";
      } else if (function.equals("Search")) {
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        List<FileItem> items = FileUploadUtil.parseRequest(request);
        QueryDescription query = new QueryDescription();
        // Ajout de la requete classique
        String word =
            FileUploadUtil.getParameter(items, "SearchKeyWord", null, request
            .getCharacterEncoding());
        query.setQuery(word);
        gallerySC.setSearchKeyWord(word);

        // Ajout des éléments de recherche sur form XML
        String xmlFormName = gallerySC.getXMLFormName();
        if (StringUtil.isDefined(xmlFormName)) {
          String xmlFormShortName =
              xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
          PublicationTemplateImpl template =
              (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(gallerySC
              .getComponentId() +
              ":" + xmlFormShortName);

          String templateFileName = template.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));

          RecordTemplate searchTemplate = template.getSearchTemplate();
          DataRecord data = searchTemplate.getEmptyRecord();

          PagesContext context =
              new PagesContext("XMLSearchForm", "2", gallerySC.getLanguage(), gallerySC.getUserId());
          context.setEncoding("UTF-8");
          XmlSearchForm searchForm = (XmlSearchForm) template.getSearchForm();
          searchForm.update(items, data, context);

          // store xml search data in session
          gallerySC.setXMLSearchContext(data);

          String[] fieldNames = searchTemplate.getFieldNames();
          String fieldValue = "";
          String fieldName = "";
          String fieldQuery = "";
          Field field = null;
          for (int f = 0; f < fieldNames.length; f++) {
            fieldName = fieldNames[f];
            field = data.getField(fieldName);
            fieldValue = field.getStringValue();
            if (fieldValue != null && fieldValue.trim().length() > 0) {
              fieldQuery = fieldValue.trim().replaceAll("##", " AND "); // case à cocher multiple
              query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName, fieldQuery,
                  null));
            }
          }
        }

        // Ajout des éléments de recherche IPTC
        List<MetaData> iptcFields = gallerySC.getMetaDataKeys();
        // Parcours des champs XML recherchables
        Iterator<MetaData> it = iptcFields.iterator();
        while (it.hasNext()) {
          MetaData iptcField = it.next();
          // recuperation valeur dans request
          String property = iptcField.getProperty();

          // ajouter à l'objet query
          if (!iptcField.isDate()) {
            String value =
                FileUploadUtil.getParameter(items, property, null, request.getCharacterEncoding());
            if (StringUtil.isDefined(value))
              query.addFieldQuery(new FieldDescription("IPTC_" + property, value, null));
          } else {
            // cas particulier des champs de type date
            // recupere les deux champs
            String dateBeginStr =
                FileUploadUtil.getParameter(items, property + "_Begin", null, request
                .getCharacterEncoding());
            String dateEndStr =
                FileUploadUtil.getParameter(items, property + "_End", null, request
                .getCharacterEncoding());

            Date dateBegin = null;
            Date dateEnd = null;

            if (StringUtil.isDefined(dateBeginStr))
              dateBegin = DateUtil.stringToDate(dateBeginStr, gallerySC.getLanguage());

            if (StringUtil.isDefined(dateEndStr))
              dateEnd = DateUtil.stringToDate(dateEndStr, gallerySC.getLanguage());

            if (dateBegin != null || dateEnd != null)
              query
                  .addFieldQuery(new FieldDescription("IPTC_" + property, dateBegin, dateEnd, null));

          }
        }

        // Ajout élément PDC
        SearchContext pdcContext = new SearchContext();
        List<Integer> silverObjectIds = null;

        // Récupération des couples (axe, valeur)
        Iterator<FileItem> itItems = items.iterator();
        while (itItems.hasNext()) {
          FileItem item = itItems.next();
          if (item.isFormField() && item.getFieldName().startsWith("Axis")) {
            String axisParam = item.getString();
            if (StringUtil.isDefined(axisParam)) {
              String axisId = axisParam.substring(0, axisParam.indexOf("_"));
              String value = axisParam.substring(axisParam.indexOf("_") + 1);
              SearchCriteria criteria = new SearchCriteria(Integer.parseInt(axisId), value);
              pdcContext.addCriteria(criteria);
            }
          }
        }

        if (!pdcContext.isEmpty()) {
          // store pdcContext in session
          gallerySC.setPDCSearchContext(pdcContext);

          List<String> componentIds = new ArrayList<String>();
          componentIds.add(gallerySC.getComponentId());

          PdcBm pdc = new PdcBmImpl();
          silverObjectIds = pdc.findSilverContentIdByPosition(pdcContext, componentIds);
        }

        // Lancement de la recherche
        Collection<PhotoDetail> photos = gallerySC.search(query);
        gallerySC.setSearchResultListPhotos(photos);

        if (silverObjectIds != null && silverObjectIds.size() > 0) {
          Collection<PhotoDetail> result = null;
          if (!query.isEmpty()) {
            // Intersection des résultats Lucene et PDC
            result = mixedSearch(gallerySC, photos, silverObjectIds);
          } else {
            result =
                getPhotosBySilverObjectIds(new TreeSet<Integer>(silverObjectIds),
                new ContentManager(),
                gallerySC);
          }
          // mise à jour de la liste des photos résultat de la recherche
          gallerySC.setSearchResultListPhotos(result);
        }

        // mise à jour du compteur de paginiation
        gallerySC.setIndexOfFirstItemToDisplay("0");

        destination = getDestination("ViewSearchResults", gallerySC, request);
      } else if (function.equals("ViewNotVisible")) {
        // traitement de la liste des photos plus visibles
        // récupération de la liste des photos plus visibles, ou création de cette liste si elle est
        // vide
        Collection<PhotoDetail> photos = gallerySC.getRestrictedListPhotos();
        if (photos.isEmpty())
          photos = gallerySC.getNotVisible();

        // mise à jour du tag pour les photos non visibles
        gallerySC.setSearchResult(false);
        gallerySC.setViewNotVisible(true);
        request.setAttribute("ViewVisible", new Boolean(gallerySC.isViewNotVisible()));

        // passage des paramètres
        request.setAttribute("Photos", photos);
        request.setAttribute("NbPhotosPerPage", new Integer(gallerySC.getNbPhotosPerPage()));
        request.setAttribute("FirstPhotoIndex", new Integer(gallerySC
            .getIndexOfFirstItemToDisplay()));
        request.setAttribute("Tri", gallerySC.getTri());
        request.setAttribute("Taille", gallerySC.getTaille());
        request.setAttribute("IsViewMetadata", gallerySC.isViewMetadata());
        request.setAttribute("IsViewList", gallerySC.isViewList());
        request.setAttribute("Tri", gallerySC.getTri());
        request.setAttribute("SelectedIds", gallerySC.getListSelected());
        request.setAttribute("SearchKeyWord", "");
        request.setAttribute("IsBasket", gallerySC.isBasket());

        // appel jsp
        destination = rootDest + "viewRestrictedPhotos.jsp";
      } else if (function.startsWith("portlet")) {
        // récupération des albums de 1er niveau
        gallerySC.setIndexOfFirstItemToDisplay("0");
        request.setAttribute("root", gallerySC.goToAlbum("0"));
        // chercher les dernières photos
        boolean viewAllPhoto = false;
        gallerySC.setViewAllPhoto(viewAllPhoto);
        Collection<PhotoDetail> photos = gallerySC.getDernieres();
        request.setAttribute("Photos", photos);
        // appel jsp
        destination = rootDest + "portlet.jsp";
      } else if (function.equals("copy")) {
        SilverTrace.debug("gallery", "GalleryRequestRouter.copy", "root.MSG_GEN_PARAM_VALUE",
            "Entrée copie");

        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType))
          gallerySC.copyAlbum(objectId);
        else
          gallerySC.copyImage(objectId);

        SilverTrace.debug("gallery", "GalleryRequestRouter.copy", "root.MSG_GEN_PARAM_VALUE",
            "objectType = " + objectType + " objectId = " + objectId);
        SilverTrace.debug("gallery", "GalleryRequestRouter.copy", "root.MSG_GEN_PARAM_VALUE",
            "destination = " + URLManager.getURL(URLManager.CMP_CLIPBOARD) +
            "Idle.jsp?message=REFRESHCLIPBOARD");

        destination =
            URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.equals("CopySelectedPhoto")) {
        processSelection(request, gallerySC);

        // liste des photos sélectionnées
        if (gallerySC.getListSelected().size() > 0) {
          // récupération des photos à copier
          Collection<String> photoIds = gallerySC.getListSelected();

          // copie des photos
          gallerySC.copySelectedPhoto(photoIds);

          // tout déselectionner
          gallerySC.setSelect(false);
          gallerySC.clearListSelected();
        }
        // retour à l'album en cours
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.startsWith("cut")) {
        SilverTrace.debug("gallery", "GalleryRequestRouter.cut", "root.MSG_GEN_PARAM_VALUE",
            "Entrée couper");

        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType))
          gallerySC.cutAlbum(objectId);
        else
          gallerySC.cutImage(objectId);

        SilverTrace.debug("gallery", "GalleryRequestRouter.cut", "root.MSG_GEN_PARAM_VALUE",
            "objectType = " + objectType + " objectId = " + objectId);
        SilverTrace.debug("gallery", "GalleryRequestRouter.cut", "root.MSG_GEN_PARAM_VALUE",
            "destination = " + URLManager.getURL(URLManager.CMP_CLIPBOARD) +
            "Idle.jsp?message=REFRESHCLIPBOARD");

        destination =
            URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.equals("CutSelectedPhoto")) {
        processSelection(request, gallerySC);

        // liste des photos sélectionnées
        if (gallerySC.getListSelected().size() > 0) {
          // récupération des photos à couper
          Collection<String> photoIds = gallerySC.getListSelected();

          // coupe des photos
          gallerySC.cutSelectedPhoto(photoIds);

          // tout déselectionner
          gallerySC.setSelect(false);
          gallerySC.clearListSelected();
        }
        // retour à l'album en cours
        destination = getDestination("GoToCurrentAlbum", gallerySC, request);
      } else if (function.startsWith("paste")) {
        SilverTrace.debug("gallery", "GalleryRequestRouter.paste", "root.MSG_GEN_PARAM_VALUE",
            "Entrée coller");

        gallerySC.paste();

        SilverTrace.debug("gallery", "GalleryRequestRouter.paste", "root.MSG_GEN_PARAM_VALUE",
            "destination = " + URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp");

        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp";
      }
      // fonctions de gestion du panier et des demandes
      else if (function.startsWith("Basket")) {
        if (function.equals("BasketView")) {
          // voir le panier
          request.setAttribute("Photos", gallerySC.getBasketListPhotos());
          request.setAttribute("NbPhotosPerPage", new Integer(gallerySC.getNbPhotosPerPage()));
          // request.setAttribute("FirstPhotoIndex", new
          // Integer(gallerySC.getIndexOfFirstItemToDisplay()));
          request.setAttribute("SelectedIds", gallerySC.getListSelected());
          request.setAttribute("IsOrder", gallerySC.isOrder());
          destination = rootDest + "basket.jsp";

        } else if (function.equals("BasketDelete")) {
          gallerySC.deleteBasket();
          destination = getDestination("BasketView", gallerySC, request);
        } else if (function.equals("BasketDeletePhoto")) {
          // suppression la photo du panier
          String photoId = request.getParameter("PhotoId");
          gallerySC.deleteToBasket(photoId);

          destination = getDestination("BasketView", gallerySC, request);
        } else if (function.equals("BasketDeleteSelectedPhoto")) {
          // suppression les photos sélectionnées du panier
          processSelection(request, gallerySC);
          if (gallerySC.getListSelected().size() > 0) {
            gallerySC.deleteToBasket();
          }
          destination = getDestination("BasketView", gallerySC, request);
        } else if (function.equals("BasketAddPhotos")) {
          // ajouter les photos sélectionnées au panier
          processSelection(request, gallerySC);
          if (gallerySC.getListSelected().size() > 0) {
            gallerySC.addToBasket();
            // on va sur le panier
            destination = getDestination("BasketView", gallerySC, request);
          } else {
            // si on a pas choisit de photo, on reste sur l'album courant
            destination = getDestination("GoToCurrentAlbum", gallerySC, request);
          }
        } else if (function.equals("BasketAddPhoto")) {
          // ajouter la photo au panier
          String photoId = request.getParameter("PhotoId");
          gallerySC.addPhotoToBasket(photoId);
          destination = getDestination("BasketView", gallerySC, request);
        } else if (function.equals("BasketPagination")) {
          processSelection(request, gallerySC);

          // retour au panier
          destination = getDestination("BasketView", gallerySC, request);
        }
      } else if (function.startsWith("Order")) {
        if (function.equals("OrderAdd")) {
          // recherche du formulaire de demande
          String xmlFormName = gallerySC.getOrderForm();
          String xmlFormShortName = null;

          if (isDefined(xmlFormName)) {
            xmlFormShortName =
                xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(
                gallerySC.getComponentId() + ":" + xmlFormShortName, xmlFormName);
            Form formUpdate = pubTemplate.getUpdateForm();
            RecordSet recordSet = pubTemplate.getRecordSet();
            DataRecord data = recordSet.getEmptyRecord();
            request.setAttribute("Form", formUpdate);
            request.setAttribute("Data", data);
          }

          // rechercher de la charte
          String charteUrl = gallerySC.getCharteUrl();

          request.setAttribute("CharteUrl", charteUrl);

          destination = rootDest + "basketForm.jsp";
        } else if (function.equals("OrderCreate")) {
          // création de la demande à partir du panier
          String orderId = gallerySC.addOrder();

          // mise à jour des données du formulaire
          if (!StringUtil.isDefined(request.getCharacterEncoding())) {
            request.setCharacterEncoding("UTF-8");
          }
          List<FileItem> items = FileUploadUtil.parseRequest(request);

          String xmlFormName = gallerySC.getOrderForm();
          if (isDefined(xmlFormName)) {
            String xmlFormShortName =
                xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

            PublicationTemplate pub =
                PublicationTemplateManager.getPublicationTemplate(gallerySC.getComponentId() + ":" +
                xmlFormShortName);
            RecordSet set = pub.getRecordSet();
            Form form = pub.getUpdateForm();
            DataRecord data = set.getRecord(orderId);
            if (data == null) {
              data = set.getEmptyRecord();
              data.setId(orderId);
            }

            PagesContext context =
                new PagesContext("myForm", "0", gallerySC.getLanguage(), false, gallerySC
                .getComponentId(), gallerySC.getUserId());
            context.setEncoding("UTF-8");
            context.setObjectId(orderId);

            // mise à jour des données saisies
            form.update(items, data, context);
            set.save(data);
          }

          // envoie d'une notification au gestionnaire
          gallerySC.sendAskOrder(orderId);

          // on va sur la liste des demandes
          destination = getDestination("OrderViewList", gallerySC, request);
        } else if (function.equals("OrderUpdate")) {
          String orderId = request.getParameter("OrderId");
          // mettre à jour la demande
          updateOrder(request, gallerySC, orderId, userId);
          // envoie d'une notification au lecteur ayant fait la demande
          gallerySC.sendAskOrderUser(orderId);
          // retour à la liste des demandes
          destination = getDestination("OrderViewList", gallerySC, request);
        } else if (function.equals("OrderViewList")) {
          Collection<Order> orders;
          if (flag.equals("admin")) {
            // si gestionnaire, liste de toutes les demandes
            orders = gallerySC.getAllOrders();
          } else {
            // sinon, liste des demandes de l'utilisateur
            orders = gallerySC.getOrdersByUser();
          }
          request.setAttribute("NbOrdersProcess", getNbOrdersProcess(orders));
          request.setAttribute("Orders", orders);
          // appel jsp
          destination = rootDest + "orders.jsp";
        } else if (function.equals("OrderView")) {
          String orderId = request.getParameter("OrderId");
          if (orderId == null || orderId.length() == 0 || "null".equals(orderId))
            orderId = (String) request.getAttribute("OrderId");

          if (flag.equals("admin") || gallerySC.isAccessAuthorized(orderId)) {
            // sauvegarder la demande en cours
            gallerySC.setCurrentOrderId(orderId);

            destination = getDestination("OrderViewPagin", gallerySC, request);
          } else
            destination = "/admin/jsp/accessForbidden.jsp";
        } else if (function.equals("OrderViewPagin")) {
          // voir la demande
          String orderId = gallerySC.getCurrentOrderId();
          request.setAttribute("Order", gallerySC.getOrder(orderId));
          request.setAttribute("NbPhotosPerPage", new Integer(gallerySC.getNbPhotosPerPage()));
          request.setAttribute("Taille", gallerySC.getTaille());

          Form formView = null;
          DataRecord data = null;

          String xmlFormName = gallerySC.getOrderForm();
          if (isDefined(xmlFormName)) {
            String xmlFormShortName =
                xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
            PublicationTemplateImpl pubTemplate =
                (PublicationTemplateImpl) PublicationTemplateManager
                .getPublicationTemplate(gallerySC.getComponentId() + ":" + xmlFormShortName);

            if (pubTemplate != null) {
              formView = pubTemplate.getViewForm();

              RecordSet recordSet = pubTemplate.getRecordSet();
              data = recordSet.getRecord(orderId);
              if (data != null) {
                request.setAttribute("XMLForm", formView);
                request.setAttribute("XMLData", data);
              }
            }
          }

          destination = rootDest + "order.jsp";

        } else if (function.equals("OrderPagination")) {
          // retour à la demande
          destination = getDestination("OrderViewPagin", gallerySC, request);
        } else if (function.equals("OrderDownloadImage")) {
          String photoId = request.getParameter("PhotoId");
          String orderId = request.getParameter("OrderId");

          // rechercher l'url de la photo
          String url = gallerySC.getUrl(orderId, photoId);

          // mise à jour de la date de téléchargement
          gallerySC.updateOrderRow(orderId, photoId);

          request.setAttribute("Url", url);
          destination = rootDest + "download.jsp";
        }
      } else {
        destination = rootDest + function;
      }
    } catch (AccessForbiddenException afe) {
      destination = "/admin/jsp/accessForbidden.jsp";
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("gallery", "GalleryRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  private Collection<PhotoDetail> mixedSearch(GallerySessionController gallerySC,
      Collection<PhotoDetail> photos,
      List<Integer> alSilverContentIds) throws Exception {
    ContentManager contentManager = new ContentManager();
    // On créait une liste triée d'indexEntry
    SortedSet<Integer> basicSearchList = new TreeSet<Integer>();
    String instanceId = "";
    String objectId = "";
    List<String> docFeature = (List<String>) new ArrayList<String>();

    Iterator<PhotoDetail> itPhotos = photos.iterator();
    while (itPhotos.hasNext()) {
      PhotoDetail photo = itPhotos.next();
      instanceId = photo.getInstanceId();
      objectId = photo.getId();
      docFeature.add(objectId);
      docFeature.add(instanceId);
    }
    try {
      // on récupère le silverContentId à partir de la recherche classique
      basicSearchList = contentManager.getSilverContentId(docFeature);
    } catch (Exception e) {
      SilverTrace.info("gallery", "GalleryRequestRouter.mixedSearch", "root.MSG_GEN_EXIT_METHOD");
    }

    // ne garde que les objets communs aux 2 listes basicSearchList - alSilverContentIds
    // en effet, la liste resultante du PDC n'est pas la meme que celle
    // élaborée à partir de la recherche classique
    if (alSilverContentIds != null) {
      basicSearchList.retainAll(alSilverContentIds);
    }

    // la liste basicSearchList ne contient maintenant que les silverContentIds des documents
    // trouvés
    // mais ces documents sont également dans le tableau résultat de la recherche classique
    // il faut donc créer une liste de photos pour afficher le resultat
    List<PhotoDetail> result =
        getPhotosBySilverObjectIds(basicSearchList, contentManager, gallerySC);

    return result;
  }

  private List<PhotoDetail> getPhotosBySilverObjectIds(SortedSet<Integer> silverObjectIds,
      ContentManager contentManager,
      GallerySessionController gallerySC) {
    List<PhotoDetail> result = new ArrayList<PhotoDetail>();

    if (silverObjectIds != null) {
      // la liste contient bien des résultats
      Iterator<Integer> it = silverObjectIds.iterator();
      PhotoDetail photo = null;
      String photoId = null;
      // for each silverContentId, we get the corresponding photoId
      while (it.hasNext()) {
        int cId = (it.next()).intValue();
        try {
          photoId = contentManager.getInternalContentId(cId);
          photo = gallerySC.getPhoto(photoId);
          result.add(photo);
        } catch (ClassCastException ignored) {
          // ignore unknown item
        } catch (ContentManagerException ignored) {
          // ignore unknown item
        } catch (RemoteException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    return result;
  }

  private Integer getNbOrdersProcess(Collection<Order> orders) {
    int nb = 0;
    Iterator<Order> it = orders.iterator();
    while (it.hasNext()) {
      Order order = it.next();
      if (order.getProcessUserId() >= 0) {
        nb = nb + 1;
      }
    }
    return new Integer(nb);
  }

  private PhotoDetail updateFilePhoto(String photoId, List<FileItem> parameters,
      GallerySessionController gallerySC) throws Exception {
    PhotoDetail photo = gallerySC.getPhoto(photoId);

    FileItem file = getUploadedFile(parameters, "WAIMGVAR0");
    String name = file.getName();
    if (StringUtil.isDefined(name)) {
      // suppression du répertoire contenant les anciennes photos
      FileRepositoryManager.deleteAbsolutePath(null, gallerySC.getComponentId(), gallerySC
          .getSettings().getString("imagesSubDirectory") +
          photoId);

      // création du répertoire et création de la preview et des vignettes avec la nouvelle photo
      boolean watermark = gallerySC.isMakeWatermark().booleanValue();
      String watermarkHD = gallerySC.getWatermarkHD();
      String watermarkOther = gallerySC.getWatermarkOther();

      ImageHelper.processImage(photo, file,
          gallerySC.getSettings().getString("imagesSubDirectory"), watermark, watermarkHD,
          watermarkOther);

      // mettre à jour la table avec les données de la photo
      gallerySC.updatePhoto(photo);
    }

    return photo;
  }

  private void processPhoto(String photoId, List<FileItem> parameters,
      GallerySessionController gallerySC)
      throws Exception {
    FileItem file = getUploadedFile(parameters, "WAIMGVAR0");
    PhotoDetail photo = gallerySC.getPhoto(photoId);

    boolean watermark = gallerySC.isMakeWatermark().booleanValue();
    String watermarkHD = gallerySC.getWatermarkHD();
    String watermarkOther = gallerySC.getWatermarkOther();

    ImageHelper.processImage(photo, file, gallerySC.getSettings().getString("imagesSubDirectory"),
        watermark, watermarkHD, watermarkOther);

    // ajout des métadata
    try {
      ImageHelper.setMetaData(photo, gallerySC.getLanguage());
    } catch (Exception e) {
      SilverTrace.info("gallery", "GalleryRequestRouter.processPhoto",
          "gallery.MSG_NOT_ADD_METADATA", "photoId =  " + photo.getId());
    }

    // mettre à jour la table avec les données de la photo
    gallerySC.updatePhoto(photo);
  }

  private String extractFileNameFromFilePath(FileItem file) {
    String name = file.getName();
    boolean runOnUnix = !FileUtil.isWindows();
    if (runOnUnix) {
      name = name.replace('\\', File.separatorChar);
      SilverTrace.info("gallery", "GalleryRequestRouter.createPhoto", "root.MSG_GEN_PARAM_VALUE",
          "fileName on Unix = " + name);
    }

    name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
    return name;
  }

  private String createPhoto(List<FileItem> parameters, GallerySessionController gallerySC,
      String encoding)
      throws ParseException {
    // récupération des paramètres
    String title =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageTitle, null, encoding);
    String description =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageDescription, null, encoding);
    String author =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageAuthor, null, encoding);
    FileItem file = getUploadedFile(parameters, "WAIMGVAR0");
    String beginDownloadDate =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageBeginDownloadDate, null,
        encoding);
    String endDownloadDate =
        FileUploadUtil
        .getParameter(parameters, ParameterNames.ImageEndDownloadDate, null, encoding);
    String beginDate =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageBeginDate, null, encoding);
    String endDate =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageEndDate, null, encoding);
    String keyWord =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageKeyWord, null, encoding);

    if (!StringUtil.isDefined(title)) {
      title = extractFileNameFromFilePath(file);
    }

    // mettre à null les valeurs non remplies
    if (!StringUtil.isDefined(description))
      description = null;
    if (!StringUtil.isDefined(author))
      author = null;
    // récupération des booléens
    boolean download = false;
    boolean albumLabel = false;
    if ("true".equals(FileUploadUtil.getParameter(parameters, "Download", null, encoding)))
      download = true;
    if ("true".equals(FileUploadUtil.getParameter(parameters, "AlbumLabel", null, encoding)))
      albumLabel = true;
    // récupération et transformation des dates de téléchargement
    Date jBeginDownloadDate = null;
    Date jEndDownloadDate = null;
    if (StringUtil.isDefined(beginDownloadDate))
      jBeginDownloadDate = DateUtil.stringToDate(beginDownloadDate, gallerySC.getLanguage());
    if (StringUtil.isDefined(endDownloadDate))
      jEndDownloadDate = DateUtil.stringToDate(endDownloadDate, gallerySC.getLanguage());
    Date jBeginDate = null;
    Date jEndDate = null;
    if (StringUtil.isDefined(beginDate))
      jBeginDate = DateUtil.stringToDate(beginDate, gallerySC.getLanguage());
    else
      jBeginDate = null;
    if (StringUtil.isDefined(endDate))
      jEndDate = DateUtil.stringToDate(endDate, gallerySC.getLanguage());
    else
      jEndDate = null;
    if (!StringUtil.isDefined(keyWord))
      keyWord = null;

    // création de la photo dans la base de donnée
    PhotoDetail newPhoto;
    newPhoto =
        new PhotoDetail(title, description, new Date(), null, null, author, download, albumLabel,
        jBeginDate, jEndDate, keyWord, jBeginDownloadDate, jEndDownloadDate);
    String photoId = gallerySC.createPhoto(newPhoto);
    return photoId;
  }

  private void updateOrder(HttpServletRequest request, GallerySessionController gallerySC,
      String orderId,
      String userId) throws RemoteException, FileUploadException {
    // rechercher la demande
    Order order = gallerySC.getOrder(orderId);

    // mettre à jour la date et le user
    order.setProcessUserId(Integer.parseInt(userId));

    // mettre à jour les lignes
    List<OrderRow> rows = order.getRows();
    Iterator<OrderRow> it = rows.iterator();
    while (it.hasNext()) {
      OrderRow orderRow = it.next();
      int photoId = orderRow.getPhotoId();
      String download = request.getParameter("DownloadType" + photoId);
      orderRow.setDownloadDecision(download);
    }
    order.setRows(rows);
    gallerySC.updateOrder(order);

  }

  private void updateSelectedPhoto(HttpServletRequest request, GallerySessionController gallerySC,
      Collection<String> photoIds, String encoding) throws FileUploadException, ParseException,
      PublicationTemplateException, FormException, RemoteException, UtilException {
    // mise à jour des photos selectionnées : traitement par lot

    // récupération des paramètres
    List<FileItem> items = FileUploadUtil.parseRequest(request);

    // tri des paramètres entre ceux de l'entête et ceux propre au formulaire
    List<FileItem> parameters = new ArrayList<FileItem>();
    List<FileItem> paramForm = new ArrayList<FileItem>();
    Iterator<FileItem> it = items.iterator();
    while (it.hasNext()) {
      FileItem param = it.next();
      String paramName = param.getFieldName();
      if (paramName.startsWith("Im$")) {
        // c'est un paramètre de l'entête
        parameters.add(param);
      } else {
        // c'est un paramètre du formulaire
        paramForm.add(param);
      }
    }

    // 1. Récupération des données de l'entête
    String title = FileUploadUtil.getParameter(parameters, "Im$Title", null, encoding);
    String description = FileUploadUtil.getParameter(parameters, "Im$Description", null, encoding);
    String author = FileUploadUtil.getParameter(parameters, "Im$Author", null, encoding);
    String beginDownloadDate =
        FileUploadUtil.getParameter(parameters, "Im$BeginDownloadDate", null, encoding);
    Date jBeginDownloadDate = null;
    if (!beginDownloadDate.equals("")) {
      if (beginDownloadDate != null && !beginDownloadDate.trim().equals(""))
        jBeginDownloadDate = DateUtil.stringToDate(beginDownloadDate, gallerySC.getLanguage());
    }
    String endDownloadDate =
        FileUploadUtil.getParameter(parameters, "Im$EndDownloadDate", null, encoding);
    Date jEndDownloadDate = null;
    if (!endDownloadDate.equals("")) {
      if (endDownloadDate != null && !endDownloadDate.trim().equals(""))
        jEndDownloadDate = DateUtil.stringToDate(endDownloadDate, gallerySC.getLanguage());
    }
    String beginDate = FileUploadUtil.getParameter(parameters, "Im$BeginDate", null, encoding);
    Date jBeginDate = null;
    if (!beginDate.equals("")) {
      if (beginDate != null && !beginDate.trim().equals(""))
        jBeginDate = DateUtil.stringToDate(beginDate, gallerySC.getLanguage());
    }
    String endDate = FileUploadUtil.getParameter(parameters, "Im$EndDate", null, encoding);
    Date jEndDate = null;
    if (!endDate.equals("")) {
      if (endDate != null && !endDate.trim().equals(""))
        jEndDate = DateUtil.stringToDate(endDate, gallerySC.getLanguage());
    }
    String keyWord = FileUploadUtil.getParameter(parameters, "Im$KeyWord", null, encoding);
    boolean download = false;
    if ("true".equals(FileUploadUtil.getParameter(parameters, "Im$Download", null, encoding)))
      download = true;

    // 2. Récupération des données du formulaire
    String xmlFormName = gallerySC.getXMLFormName();
    RecordSet set = null;
    Form form = null;
    if (isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      PublicationTemplate pub =
          PublicationTemplateManager.getPublicationTemplate(gallerySC.getComponentId() + ":" +
          xmlFormShortName);
      set = pub.getRecordSet();
      form = pub.getUpdateForm();
    }

    // parcours de la liste des photos et mise à jour
    Iterator<String> itPhoto = photoIds.iterator();
    while (itPhoto.hasNext()) {
      // 1. mise à jour de l'entête de la photo
      String photoId = itPhoto.next();
      PhotoDetail photo = gallerySC.getPhoto(photoId);
      if (!title.equals(""))
        photo.setTitle(title);
      if (!description.equals(""))
        photo.setDescription(description);
      if (!author.equals(""))
        photo.setAuthor(author);
      if (download)
        photo.setDownload(download);
      if (!beginDownloadDate.equals(""))
        photo.setBeginDownloadDate(jBeginDownloadDate);
      if (!endDownloadDate.equals(""))
        photo.setEndDownloadDate(jEndDownloadDate);
      if (!keyWord.equals(""))
        photo.setKeyWord(keyWord);
      if (!beginDate.equals(""))
        photo.setBeginDate(jBeginDate);
      if (!endDate.equals(""))
        photo.setEndDate(jEndDate);
      // modification de la photo
      gallerySC.updatePhotoHeader(photo);

      if (set != null) {
        // 2. mise à jour du formulaire pour la photo
        DataRecord data = set.getRecord(photoId);
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId(photoId);
        }
        PagesContext context =
            new PagesContext("photoForm", "0", gallerySC.getLanguage(), false, gallerySC
            .getComponentId(), gallerySC.getUserId(), gallerySC.getAlbum(
            gallerySC.getCurrentAlbumId()).getNodePK().getId());
        context.setEncoding("UTF-8");
        context.setObjectId(photoId);
        context.setUpdatePolicy(PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES);
        form.update(paramForm, data, context);
        set.save(data);
        // mise à jour de la photo
        gallerySC.updatePhoto(photo);
      }
    }
  }

  private void deleteSelectedPhoto(GallerySessionController gallerySC, Collection<String> photoIds) {
    // suppression des photos selectionnées : traitement par lot

    // parcours de la liste des photos et suppression
    Iterator<String> itPhoto = photoIds.iterator();
    while (itPhoto.hasNext()) {
      String photoId = itPhoto.next();
      gallerySC.deletePhoto(photoId);
    }
  }

  private FileItem getUploadedFile(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField() && parameterName.equals(item.getFieldName()))
        return item;
    }
    return null;
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param));
  }

  private void putXMLDisplayerIntoRequest(PhotoDetail photo, HttpServletRequest request,
      GallerySessionController gallerySC) throws PublicationTemplateException, FormException {
    Form formView = null;
    DataRecord data = null;

    String photoId = photo.getPhotoPK().getId();
    String xmlFormName = gallerySC.getXMLFormName();
    if (isDefined(xmlFormName)) {
      String xmlFormShortName =
          xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
      PublicationTemplateImpl pubTemplate =
          (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(gallerySC
          .getComponentId() +
          ":" + xmlFormShortName);

      if (pubTemplate != null) {
        formView = pubTemplate.getViewForm();

        RecordSet recordSet = pubTemplate.getRecordSet();
        data = recordSet.getRecord(photoId);
        if (data != null) {
          request.setAttribute("XMLForm", formView);
          request.setAttribute("XMLData", data);
        }
      }
    }
  }

  private String updateXMLFormImage(String photoId, List<FileItem> parameters,
      GallerySessionController gallerySC) throws Exception {

    // récup&ration de la photo
    PhotoDetail photo = gallerySC.getPhoto(photoId);

    String xmlFormName = gallerySC.getXMLFormName();
    String xmlFormShortName =
        xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

    PublicationTemplate pub =
        PublicationTemplateManager.getPublicationTemplate(gallerySC.getComponentId() + ":" +
        xmlFormShortName);
    RecordSet set = pub.getRecordSet();
    Form form = pub.getUpdateForm();
    DataRecord data = set.getRecord(photo.getId());
    if (data == null) {
      data = set.getEmptyRecord();
      data.setId(photo.getId());
    }

    PagesContext context =
        new PagesContext("myForm", "0", gallerySC.getLanguage(), false, gallerySC.getComponentId(),
        gallerySC.getUserId(), gallerySC.getAlbum(gallerySC.getCurrentAlbumId()).getNodePK()
        .getId());
    context.setEncoding("UTF-8");
    context.setObjectId(photo.getId());

    // mise à jour des données saisies
    form.update(parameters, data, context);
    set.save(data);

    // mise à jour de la photo
    gallerySC.updatePhoto(photo);
    return photoId;
  }

  private void updateHeaderImage(String photoId, List<FileItem> parameters,
      GallerySessionController gallerySC, String encoding)
      throws Exception {
    // récupération des paramètres
    String title =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageTitle, null, encoding);
    String description =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageDescription, null, encoding);
    String author =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageAuthor, null, encoding);
    FileItem file = getUploadedFile(parameters, "WAIMGVAR0");
    String beginDownloadDate =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageBeginDownloadDate, null,
        encoding);
    String endDownloadDate =
        FileUploadUtil
        .getParameter(parameters, ParameterNames.ImageEndDownloadDate, null, encoding);
    String beginDate =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageBeginDate, null, encoding);
    String endDate =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageEndDate, null, encoding);
    String keyWord =
        FileUploadUtil.getParameter(parameters, ParameterNames.ImageKeyWord, null, encoding);
    // formatage des paramètres
    if (!StringUtil.isDefined(title)) {
      // le titre est vide
      String name = extractFileNameFromFilePath(file);

      // si la photo est changée, mettre le nom de la photo dans le titre
      if (StringUtil.isDefined(name))
        title = name;
      // NOTE : si le titre est vide mais sans changement de photo, on mettra le nom du fichier de
      // la base plus tard
    }
    if (description.equals(""))
      description = null;
    if (author.equals(""))
      author = null;
    boolean download = false;
    boolean albumLabel = false;
    if ("true".equals(FileUploadUtil.getParameter(parameters, ParameterNames.ImageDownload, null,
        encoding)))
      download = true;
    // if ("true".equals(getParameterValue(parameters, "AlbumLabel")))
    // albumLabel = true;
    if (beginDownloadDate.equals(""))
      beginDownloadDate = null;
    if (endDownloadDate.equals(""))
      endDownloadDate = null;
    if (keyWord.equals(""))
      keyWord = null;
    if (beginDate.equals(""))
      beginDate = null;
    if (endDate.equals(""))
      endDate = null;

    // mise à jour des attributs
    PhotoDetail photo = updateFilePhoto(photoId, parameters, gallerySC);
    // traitement du nom de la photo si il est vide (dans le cas ou il serait vide sans modif de
    // photo)
    if (title.equals(""))
      title = photo.getImageName();
    photo.setTitle(title);
    photo.setDescription(description);
    photo.setAuthor(author);
    photo.setDownload(download);
    photo.setAlbumLabel(albumLabel);
    // traitement des dates
    Date jBeginDownloadDate = null;
    Date jEndDownloadDate = null;
    if (beginDownloadDate != null && !beginDownloadDate.trim().equals(""))
      jBeginDownloadDate = DateUtil.stringToDate(beginDownloadDate, gallerySC.getLanguage());
    if (endDownloadDate != null && !endDownloadDate.trim().equals(""))
      jEndDownloadDate = DateUtil.stringToDate(endDownloadDate, gallerySC.getLanguage());
    photo.setBeginDownloadDate(jBeginDownloadDate);
    photo.setEndDownloadDate(jEndDownloadDate);
    photo.setKeyWord(keyWord);
    Date jBeginDate = null;
    Date jEndDate = null;
    if (beginDate != null && !beginDate.trim().equals(""))
      jBeginDate = DateUtil.stringToDate(beginDate, gallerySC.getLanguage());
    if (endDate != null && !endDate.trim().equals(""))
      jEndDate = DateUtil.stringToDate(endDate, gallerySC.getLanguage());
    photo.setBeginDate(jBeginDate);
    photo.setEndDate(jEndDate);

    // modification de la photo
    gallerySC.updatePhotoHeader(photo);

    // mise à jour de l'album courant
    // String albumId = photo.getAlbumId();
    Collection<String> albumIds = gallerySC.getPathList(photo.getId());
    // regarder si l'album courant est dans la liste des albums
    boolean inAlbum = false;
    boolean first = true;
    String firstAlbumId = "0";
    Iterator<String> it = albumIds.iterator();
    while (it.hasNext()) {
      String albumId = it.next();
      if (first) {
        firstAlbumId = albumId;
        first = false;
      }
      if (albumId.equals(gallerySC.getCurrentAlbumId()))
        inAlbum = true;
    }
    if (!inAlbum)
      gallerySC.setCurrentAlbumId(firstAlbumId);
  }

  private void putPhotoCommonParameters(HttpServletRequest request,
      GallerySessionController gallerySC, PhotoDetail photo, String profile) throws Exception {
    request.setAttribute("Photo", photo);
    request.setAttribute("NbComments", new Integer(gallerySC.getAllComments(photo.getId()).size()));
    request.setAttribute("Path", gallerySC.getPath());
    request.setAttribute("IsUsePdc", gallerySC.isUsePdc());
    request.setAttribute("XMLFormName", gallerySC.getXMLFormName());
    request.setAttribute("IsBasket", gallerySC.isBasket());
    request.setAttribute("IsOrder", gallerySC.isOrder());

    boolean allowedToUpdateImage =
        "admin".equals(profile) || "publisher".equals(profile) ||
        ("writer".equals(profile) && photo.getCreatorId().equals(gallerySC.getUserId()));
    request.setAttribute("UpdateImageAllowed", new Boolean(allowedToUpdateImage));

    request.setAttribute("ShowCommentsTab", gallerySC.areCommentsEnabled());
  }

  private void processSelection(HttpServletRequest request, GallerySessionController gallerySC) {
    String selectedIds = request.getParameter("SelectedIds");
    String notSelectedIds = request.getParameter("NotSelectedIds");

    Collection<String> memSelected = gallerySC.getListSelected();

    StringTokenizer st;
    String id;

    if (StringUtil.isDefined(selectedIds)) {
      st = new StringTokenizer(selectedIds, ",");
      while (st.hasMoreTokens()) {
        id = st.nextToken();
        if (!memSelected.contains(id))
          memSelected.add(id);
      }
    }

    if (StringUtil.isDefined(notSelectedIds)) {
      st = new StringTokenizer(notSelectedIds, ",");
      while (st.hasMoreTokens()) {
        id = st.nextToken();
        memSelected.remove(id);
      }
    }
  }

  private List<String> extractIds(Collection<PhotoDetail> col) {
    List<String> ids = new ArrayList<String>();
    if (col != null) {
      Iterator<PhotoDetail> it = col.iterator();
      PhotoDetail photo = null;
      while (it.hasNext()) {
        photo = it.next();
        if (photo != null)
          ids.add(photo.getId());
      }
    }
    return ids;
  }

  private String getDocumentNotFoundDestination(GallerySessionController gallery,
      HttpServletRequest request) {
    request.setAttribute("ComponentId", gallery.getComponentId());
    return "/admin/jsp/documentNotFound.jsp";
  }

}