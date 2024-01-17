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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.gallery.ParameterNames;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.control.GallerySessionController;
import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataUpdateDelegate;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MetaData;
import org.silverpeas.components.gallery.model.Order;
import org.silverpeas.components.gallery.model.OrderRow;
import org.silverpeas.components.gallery.web.MediaSort;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.form.XmlSearchForm;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.mvc.util.AccessForbiddenException;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getSessionCacheService;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.isSqlDefined;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.core.web.http.FileResponse.DOWNLOAD_CONTEXT_PARAM;

public class GalleryRequestRouter extends ComponentRequestRouter<GallerySessionController> {
  private static final long serialVersionUID = 1L;

  private static final String ORDER_VIEW_PAGIN_FUNC = "OrderViewPagin";
  private static final String ORDER_VIEW_LIST_FUNC = "OrderViewList";
  private static final String BASKET_VIEW_FUNC = "BasketView";
  private static final String VIEW_SEARCH_RESULTS_FUNC = "ViewSearchResults";
  private static final String VIEW_NOT_VISIBLE_FUNC = "ViewNotVisible";
  private static final String MEDIA_VIEW_FUNC = "MediaView";
  private static final String GO_TO_CURRENT_ALBUM_FUNC = "GoToCurrentAlbum";
  private static final String ORDER_ID = "OrderId";
  private static final String MEDIA_TYPE_ALERT_ATTR = "MediaTypeAlert";
  private static final String MEDIA_ID = "MediaId";
  private static final String IS_VIEW_LIST = "IsViewList";
  private static final String SELECTED_IDS = "SelectedIds";
  private static final String IS_VIEW_METADATA = "IsViewMetadata";
  private static final String MEDIA_RESOLUTION = "MediaResolution";
  private static final String CURRENT_ALBUM = "CurrentAlbum";
  private static final String CURRENT_PAGE_INDEX = "CurrentPageIndex";
  private static final String NB_MEDIA_PER_PAGE = "NbMediaPerPage";
  private static final String IS_BASKET = "IsBasket";
  private static final String IS_ORDER = "IsOrder";
  private static final String IS_PRIVATE_SEARCH = "IsPrivateSearch";
  private static final String IS_USE_PDC = "IsUsePdc";
  private static final String MEDIA_LIST = "MediaList";
  private static final String ALBUMS = "Albums";
  private static final String ALBUM_ID = "albumId";
  private static final String ALBUM_ID_FIRST_LETTER_UPPERCASE = "AlbumId";
  private static final String SEARCH_KEY_WORD = "SearchKeyWord";
  private static final String MEDIA_PREFIX = "media";
  private static final String UTF_8 = "UTF-8";
  private static final String DESCRIPTION = "Description";

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
   *
   */
  @Override
  public GallerySessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new GallerySessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param gallerySC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, GallerySessionController gallerySC,
      HttpRequest request) {
    String destination = "";
    String rootDest = "/gallery/jsp/";
    request.setAttribute("gallerySC", gallerySC);


    // Set common parameters
    SilverpeasRole highestUserRole = gallerySC.getHighestSilverpeasUserRole();
    String userId = gallerySC.getUserId();

    request.setAttribute("Profile", highestUserRole.getName());
    request.setAttribute("highestUserRole", highestUserRole);
    request.setAttribute("UserId", userId);
    request.setAttribute("IsGuest", gallerySC.isGuest());
    request.setAttribute("IsAnonymous", gallerySC.isAnonymous());
    request.setAttribute("IsExportEnable", gallerySC.isExportEnable());
    request.setAttribute("Sort", gallerySC.getSort());

    try {
      if (function.startsWith("Main")) {
        // récupération des albums de 1er niveau
        gallerySC.setIndexOfCurrentPage("0");

        AlbumDetail root = gallerySC.goToAlbum("0");
        request.setAttribute("root", root);
        request.setAttribute(ALBUMS, gallerySC.addNbMedia(root.getChildrenAlbumsDetails()));
        // chercher les dernièrs médias
        Collection<Media> mediaCollection = gallerySC.getLastRegisteredMedia();
        request.setAttribute(MEDIA_LIST, mediaCollection);
        request.setAttribute(IS_USE_PDC, gallerySC.isUsePdc());
        request.setAttribute(IS_PRIVATE_SEARCH, gallerySC.isPrivateSearch());
        request.setAttribute(IS_BASKET, gallerySC.isBasket());
        request.setAttribute(IS_ORDER, gallerySC.isOrder());
        destination = rootDest + "welcome.jsp";
      } else if ("ManageComponentSubscriptions".equals(function)) {
        destination = gallerySC.manageComponentSubscriptions();
      } else if ("ManageAlbumSubscriptions".equals(function)) {
        destination = gallerySC.manageAlbumSubscriptions();
      } else if ("ViewAlbum".equals(function)) {
        if (StringUtil.isDefined(request.getParameter("deselectAll"))) {
          deselectAll(gallerySC);
        }
        // récupération de l'Id de l'album en cours
        String albumId = request.getParameter("Id");
        gallerySC.goToAlbum(albumId);
        gallerySC.setIndexOfCurrentPage("0");
        // Slideshow requirements
        request.setAttribute(ALBUM_ID, albumId);
        request.setAttribute("wait", gallerySC.getSlideshowWait());
        // retour à l'album courant
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if ("Pagination".equals(function)) {
        processSelection(request, gallerySC);

        // traitement de la pagination : passage des paramètres
        String index = request.getParameter("Index");
        if (index != null && index.length() > 0) {
          gallerySC.setIndexOfCurrentPage(index);
        }
        String nbItemsPerPage = request.getParameter("NbItemsPerPage");
        if (StringUtil.isInteger(nbItemsPerPage)) {
          gallerySC.setNbMediasPerPage(Integer.parseInt(nbItemsPerPage));
        }
        destination = returnToAlbum(request, gallerySC);

      } else if (GO_TO_CURRENT_ALBUM_FUNC.equals(function)) {
        // mise à blanc de l'index de pagination si on arrive de la recherche
        if (gallerySC.isSearchResult() || gallerySC.isViewNotVisible()) {
          gallerySC.setIndexOfCurrentPage("0");
        }
        // mise à blanc de la liste des médias (pour les mots clé et pour les médias non visibles)
        gallerySC.setRestrictedListMedia(new ArrayList<>());
        gallerySC.setSearchResult(false);
        gallerySC.setViewNotVisible(false);

        // récupération de l'album en cours
        String currentAlbumId = gallerySC.getCurrentAlbumId();
        if ("0".equals(currentAlbumId)) {
          // on est à la racine, on retourne à l'accueil
          destination = getDestination("Main", gallerySC, request);
        } else {
          // on est dans un album, on y retourne
          AlbumDetail currentAlbum = gallerySC.goToAlbum();
          request.setAttribute(NB_MEDIA_PER_PAGE, gallerySC.getNbMediaPerPage());
          request.setAttribute(CURRENT_PAGE_INDEX, gallerySC.getIndexOfCurrentPage());
          request.setAttribute(CURRENT_ALBUM, currentAlbum);
          request.setAttribute(ALBUM_ID, currentAlbum.getId());
          request.setAttribute(ALBUMS,
              gallerySC.addNbMedia(currentAlbum.getChildrenAlbumsDetails()));
          request.setAttribute("Path", gallerySC.getPath(currentAlbum.getNodePK()));
          request.setAttribute(MEDIA_RESOLUTION, gallerySC.getDisplayedMediaResolution());
          request.setAttribute("DragAndDropEnable", gallerySC.isDragAndDropEnabled());
          request.setAttribute(IS_VIEW_METADATA, gallerySC.isViewMetadata());
          request.setAttribute(IS_VIEW_LIST, gallerySC.isViewList());
          request.setAttribute(SELECTED_IDS, gallerySC.getListSelected());
          request.setAttribute(IS_USE_PDC, gallerySC.isUsePdc());
          request.setAttribute(IS_BASKET, gallerySC.isBasket());
          request.setAttribute(IS_ORDER, gallerySC.isOrder());
          request.setAttribute(IS_PRIVATE_SEARCH, gallerySC.isPrivateSearch());

          destination = rootDest + "viewAlbum.jsp";
        }
      } else if ("NewAlbum".equals(function)) {
        // passage de l'album courant : null car en création
        request.setAttribute(CURRENT_ALBUM, null);
        request.setAttribute("Path", gallerySC.getPath());
        // appel jsp
        destination = rootDest + "albumManager.jsp";
      } else if ("CreateAlbum".equals(function)) {
        // check user rights
        if (!gallerySC.isAlbumAdmin(highestUserRole, null, userId)) {
          throw new AccessForbiddenException("Gallery creation forbidden");
        }

        // récupération des paramètres de l'album
        String name = request.getParameter("Name");
        String description = request.getParameter(DESCRIPTION);
        // création de l'album
        NodeDetail node =
            new NodeDetail("unknown", name, description, 0, "unknown");
        AlbumDetail album = new AlbumDetail(node);
        gallerySC.createAlbum(album);
        // retour à l'album courant
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if ("EditAlbum".equals(function)) {
        // récupération des paramètres
        String albumId = request.getParameter("Id");

        // check user rights
        if (!gallerySC.isAlbumAdmin(highestUserRole, albumId, userId)) {
          throw new AccessForbiddenException("Gallery edition forbidden");
        }

        // récupération de l'album courant
        AlbumDetail currentAlbum = gallerySC.getAlbum(albumId);
        // passage des paramètres
        request.setAttribute(CURRENT_ALBUM, currentAlbum);
        request.setAttribute("Path", gallerySC.getPath());
        destination = rootDest + "albumManager.jsp";
      } else if ("UpdateAlbum".equals(function)) {
        String albumId = request.getParameter("Id");

        // check user rights
        if (!gallerySC.isAlbumAdmin(highestUserRole, albumId, userId)) {
          throw new AccessForbiddenException("Gallery update forbidden");
        }

        // Retrieve album name and description
        String name = request.getParameter("Name");
        String description = request.getParameter(DESCRIPTION);

        // Retrieve current album
        AlbumDetail album = gallerySC.getAlbum(albumId);
        // set new value
        album.setName(name);
        album.setDescription(description);

        gallerySC.updateAlbum(album);
        // retour à l'album courant
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if ("DeleteAlbum".equals(function)) {
        // Retrieve album identifier to delete
        String albumId = request.getParameter("Id");

        // check user rights
        if (!gallerySC.isAlbumAdmin(highestUserRole, albumId, userId)) {
          throw new AccessForbiddenException("Gallery deletion forbidden");
        }

        gallerySC.deleteAlbum(albumId);
        // retour à l'album courant
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if ("AddMedia".equals(function)) {
        // check user rights
        if (!gallerySC.isMediaAdmin(highestUserRole, null, userId)) {
          throw new AccessForbiddenException("Media adding forbidden");
        }
        MediaType mediaType = MediaType.from(request.getParameter("type"));

        // passage des paramètres
        request.setAttribute("Media", mediaType.newInstance());
        request.setAttribute("Repertoire", "");
        request.setAttribute("Path", gallerySC.getPath());
        request.setAttribute("GetLanguage", gallerySC.getLanguage());
        request.setAttribute("UserName", gallerySC.getUserDetail(userId).getDisplayedName());
        request.setAttribute("NbComments", 0);
        request.setAttribute(IS_USE_PDC, gallerySC.isUsePdc());
        request.setAttribute("XMLFormName", gallerySC.getXMLFormName());
        request.setAttribute("ShowCommentsTab", gallerySC.areCommentsEnabled());
        request.setAttribute(IS_BASKET, gallerySC.isBasket());
        request.setAttribute(IS_ORDER, gallerySC.isOrder());
        request.setAttribute(IS_VIEW_METADATA, gallerySC.isViewMetadata());

        // prepare xml form data
        PublicationTemplate template = gallerySC.getTemplate();
        if (template != null) {
          RecordSet recordSet = template.getRecordSet();
          request.setAttribute("Form", template.getUpdateForm());
          request.setAttribute("Data", recordSet.getEmptyRecord());
        }

        destination = rootDest + MEDIA_PREFIX + mediaType + "Edit.jsp";
      } else if ("CreateMedia".equals(function)) {

        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding(UTF_8);
        }

        // check user rights
        if (!gallerySC.isMediaAdmin(highestUserRole, null, userId)) {
          throw new AccessForbiddenException("Media creation forbidden");
        }

        final String mediaId = createMediaData(request, gallerySC);

        // Reload the album
        gallerySC.loadCurrentAlbum();

        // preview of the new media
        request.setAttribute(MEDIA_ID, mediaId);
        destination = getDestination(MEDIA_VIEW_FUNC, gallerySC, request);
      } else if ("UpdateMedia".equals(function) || "UpdateInformation".equals(function)) {
        boolean isUpdateMediaFromAlbumCase =
            StringUtil.getBooleanValue(request.getParameter("isUpdateMediaFromAlbumCase"));
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding(UTF_8);
        }
        List<FileItem> parameters = request.getFileItems();
        String mediaId = FileUploadUtil
            .getParameter(parameters, MEDIA_ID, null, request.getCharacterEncoding());
        updateMediaData(mediaId, request, gallerySC);
        if (isUpdateMediaFromAlbumCase) {
          // Reload and clean the current album
          gallerySC.loadCurrentAlbum();
          deselectAll(gallerySC);
          // Back to the current album
          destination = returnToAlbum(request, gallerySC);
        } else {
          // Back to the view of the updated media
          request.setAttribute(MEDIA_ID, mediaId);
          destination = getDestination(MEDIA_VIEW_FUNC, gallerySC, request);
        }
      } else if ("DeleteMedia".equals(function)) {
        // Retrieve media identifier to delete
        String mediaId = request.getParameter(MEDIA_ID);

        // check user rights
        if (!gallerySC.isMediaAdmin(highestUserRole, mediaId, userId)) {
          throw new AccessForbiddenException("Media deletion forbidden");
        }
        gallerySC.deleteMedia(mediaId);

        // retour à l'album courant
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if (MEDIA_VIEW_FUNC.equals(function)) {
        // mise à blanc de la liste restreintes des médias (pour les médias non visibles)
        gallerySC.setRestrictedListMedia(new ArrayList<>());
        // remise à blanc des médias selectionnés
        deselectAll(gallerySC);
        // récupération des paramètres
        String mediaId = request.getParameter(MEDIA_ID);
        if (mediaId == null || mediaId.length() == 0 || "null".equals(mediaId)) {
          mediaId = (String) request.getAttribute(MEDIA_ID);
        }
        request.setAttribute(IS_PRIVATE_SEARCH, gallerySC.isPrivateSearch());
        destination = processMedia(gallerySC, request, rootDest, highestUserRole, mediaId);
      } else if ("PreviousMedia".equals(function)) {
        Media media = gallerySC.getPrevious();
        request.setAttribute(MEDIA_ID, media.getId());
        destination = getDestination(MEDIA_VIEW_FUNC, gallerySC, request);
      } else if ("NextMedia".equals(function)) {
        Media media = gallerySC.getNext();
        request.setAttribute(MEDIA_ID, media.getId());
        destination = getDestination(MEDIA_VIEW_FUNC, gallerySC, request);
      } else if (function.startsWith("searchResult")) {
        // traitement des recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        destination = processMediaType(gallerySC, request, id, type);
      } else if ("AccessPath".equals(function)) {

        // visualisation des emplacements
        request.setAttribute("Path", gallerySC.getPath());
        // récupération du média
        String mediaId = request.getParameter(MEDIA_ID);
        if (mediaId == null || mediaId.isEmpty()) {
          mediaId = request.getParameter("PubId");
        }
        if (mediaId == null || mediaId.isEmpty()) {
          mediaId = (String) request.getAttribute(MEDIA_ID);
        }

        // check user rights
        if (!gallerySC.isMediaAdmin(highestUserRole, mediaId, userId)) {
          throw new AccessForbiddenException("Gallery access forbidden");
        }

        Media media = gallerySC.getMedia(mediaId);

        // passage des paramètres
        putMediaCommonParameters(request, gallerySC, media, highestUserRole);

        request.setAttribute("PathList", gallerySC.getAlbumIdsOf(mediaId));
        request.setAttribute(ALBUMS, gallerySC.getAllAlbums());

        destination = rootDest + "albumsOfMedia.jsp";
      } else if ("SelectPath".equals(function)) {
        // modification de la liste des emplacements du média
        String[] albums = request.getParameterValues("albumChoice");
        String mediaId = request.getParameter(MEDIA_ID);

        // check user rights
        if (!gallerySC.isMediaAdmin(highestUserRole, mediaId, userId)) {
          throw new AccessForbiddenException("Media access forbidden");
        }

        gallerySC.setMediaToAlbums(mediaId, albums);
        destination = getDestination(MEDIA_VIEW_FUNC, gallerySC, request);
      } else if ("AskMedia".equals(function)) {
        // demande de médias auprès du gestionnaire de la médiathèque
        destination = rootDest + "askMedia.jsp";
      } else if ("SendAsk".equals(function)) {
        // envoie d'une notification au gestionnaire
        String description = request.getParameter(DESCRIPTION);
        gallerySC.sendAskMedia(description);
        destination = rootDest + "closeWindow.jsp";
      } else if ("ChoiceSize".equals(function)) {
        // traitement du choix des tailles
        String choix = request.getParameter("Choice");
        // mettre à jour la taille avec le choix
        gallerySC.setDisplayedMediaResolution(MediaResolution.fromNameOrLabel(choix));
        // retourner au début de la liste des médias
        gallerySC.initIndex();
        // retour ... en fonction d'ou on viens
        destination = returnToAlbum(request, gallerySC);

      } else if ("SortBy".equals(function)) {
        // traitement du tri selon l'écran en cours
        MediaSort sort = MediaSort.from(request.getParameter("Sort"));
        if ((!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible())) {
          gallerySC.setSort(sort);
        } else {
          gallerySC.setSortSearch(sort);
        }
        destination = returnToAlbum(request, gallerySC);
      } else if ("EditSelectedMedia".equals(function)) {
        // traitement par lot
        String albumId = request.getParameter(ALBUM_ID_FIRST_LETTER_UPPERCASE);

        // check user rights
        if (!highestUserRole.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
          throw new AccessForbiddenException("Media edition fobidden");
        }

        String searchKeyWord = request.getParameter(SEARCH_KEY_WORD);

        processSelection(request, gallerySC);

        // liste des médias sélectionnés
        if (!gallerySC.getListSelected().isEmpty()) {
          if (gallerySC.getListSelected().size() == 1) {
            // One media is being to be edited, the user is redirected to the edit page of the media
            request.setAttribute("MediaIdFromAlbum", gallerySC.getListSelected().iterator().next());
            destination = getDestination("EditInformation", gallerySC, request);
          } else {
            // passage des paramètres globaux
            request.setAttribute("SelectedMediaIds", gallerySC.getListSelected());
            request.setAttribute(ALBUM_ID_FIRST_LETTER_UPPERCASE, albumId);
            request.setAttribute(SEARCH_KEY_WORD, searchKeyWord);
            request.setAttribute("Path", gallerySC.getPath());
            request.setAttribute("GetLanguage", gallerySC.getLanguage());

            // passage des paramètres pour le formulaire
            PublicationTemplate template = gallerySC.getTemplate();
            if (template != null) {
              RecordSet recordSet = template.getRecordSet();
              request.setAttribute("Form", template.getUpdateForm());
              request.setAttribute("Data", recordSet.getEmptyRecord());
            }

            destination = rootDest + "selectedMediaManager.jsp";
          }
        } else {
          destination = returnToAlbum(request, gallerySC);
        }
      } else if ("UpdateSelectedMedia".equals(function)) {
        // récupération des médias modifiés
        Collection<String> mediaIds = gallerySC.getListSelected();

        // mise à jour des médias
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding(UTF_8);
        }
        updateSelectedMedia(request, gallerySC, mediaIds, request.getCharacterEncoding());

        // tout déselectionner
        deselectAll(gallerySC);
        destination = returnToAlbum(request, gallerySC);

      } else if ("UpdateSelectedPaths".equals(function)) {
        // récupération des médias modifiés
        Collection<String> mediaIds = gallerySC.getListSelected();

        // mise à jour des emplacements des médias
        String[] albums = request.getParameterValues("albumChoice");

        for (String mediaId : mediaIds) {
          if (gallerySC.isMediaAdmin(highestUserRole, mediaId, userId)) {
            // ajouter les nouveau emplacements sur les anciens
            gallerySC.addMediaToAlbums(mediaId, albums);
          }
        }
        deselectAll(gallerySC);

        destination = returnToAlbum(request, gallerySC);
      } else if ("DeleteSelectedMedia".equals(function)) {
        processSelection(request, gallerySC);

        // liste des médias sélectionnés
        if (!gallerySC.getListSelected().isEmpty()) {
          // récupération des médias à supprimer
          Collection<String> mediaIds = gallerySC.getListSelected();

          // suppression des médias
          deleteSelectedMedia(gallerySC, mediaIds);

          deselectAll(gallerySC);

          // retour à l'album en cours
          destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
        }
      } else if ("CategorizeSelectedMedia".equals(function)) {
        processSelection(request, gallerySC);

        // liste des médias sélectionnés
        if (!gallerySC.getListSelected().isEmpty()) {
          final List<String> selectedIds = (List<String>) gallerySC.getListSelected();

          // get silverObjectIds according to selected mediaIds
          final List<String> silverObjectIds = new ArrayList<>();
          for (final String selectedId : selectedIds) {
            silverObjectIds.add(Integer.toString(gallerySC.getSilverObjectId(selectedId)));
          }

          request.setAttribute("ObjectIds", silverObjectIds);
          request.setAttribute("ComponentId", gallerySC.getComponentId());
          destination = "/RpdcClassify/jsp/ToAddPositions";
        } else {
          destination = rootDest + "closeWindow.jsp";
        }
      } else if ("AddAlbumForSelectedMedia".equals(function)) {
        // placement par lot
        String albumId = request.getParameter(ALBUM_ID_FIRST_LETTER_UPPERCASE);
        // check user rights
        if (!highestUserRole.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
          throw new AccessForbiddenException("Media adding into gallery forbidden");
        }
        processSelection(request, gallerySC);
        // liste des médias sélectionnés
        if (!gallerySC.getListSelected().isEmpty()) {
          // passage des paramètres globaux
          request.setAttribute(ALBUM_ID_FIRST_LETTER_UPPERCASE, albumId);
          request.setAttribute("Path", gallerySC.getPath());
          request.setAttribute(ALBUMS, gallerySC.getAllAlbums());

          destination = rootDest + "pathsManager.jsp";
        } else {
          destination = returnToAlbum(request, gallerySC);
        }
      } else if ("EditInformation".equals(function)) {
        // récupération des paramètres
        String mediaId = request.getParameter(MEDIA_ID);
        if (StringUtil.isNotDefined(mediaId)) {
          // Firstly, verifying that the user tries to modify a media from album
          mediaId = (String) request.getAttribute("MediaIdFromAlbum");
          if (StringUtil.isDefined(mediaId)) {
            request.setAttribute("isUpdateMediaFromAlbumCase", true);
          }
        }
        // récupération du média
        Media media = gallerySC.getMedia(mediaId);

        // passage des paramètres
        putMediaCommonParameters(request, gallerySC, media, highestUserRole);

        request.setAttribute(IS_VIEW_METADATA, gallerySC.isViewMetadata());

        request.setAttribute("Repertoire", media.getWorkspaceSubFolderName());

        // récupération du formulaire et affichage
        PublicationTemplate template = gallerySC.getTemplate();
        if (template != null) {
          RecordSet recordSet = template.getRecordSet();
          DataRecord data = recordSet.getRecord(mediaId);
          if (data == null) {
            data = recordSet.getEmptyRecord();
            data.setId(mediaId);
          }
          request.setAttribute("Form", template.getUpdateForm());
          request.setAttribute("Data", data);
        }

        // appel jsp
        destination = rootDest + MEDIA_PREFIX + media.getType().name() + "Edit.jsp";
      } else if ("AllSelected".equals(function)) {
        // sélectionne (ou déselectionne) tous les médias de l'album (ou de la liste restreinte
        // dans le cas de la recherche)
        boolean select = !gallerySC.getSelect();
        gallerySC.setSelect(select);

        Collection<Media> media;

        // Returning to the from
        if (!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible()) {
          // Returning to the album
          media = gallerySC.goToAlbum().getMedia();

          if (select) {
            gallerySC.getListSelected().addAll(extractIds(media));
          } else {
            gallerySC.getListSelected().removeAll(extractIds(media));
          }

          destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);

        } else {
          // Returning to search result or to not visible list
          if (gallerySC.isViewNotVisible()) {
            media = gallerySC.getNotVisible();

            if (select) {
              gallerySC.getListSelected().addAll(extractIds(media));
            } else {
              gallerySC.getListSelected().removeAll(extractIds(media));
            }

            gallerySC.setRestrictedListMedia(media);
            destination = getDestination(VIEW_NOT_VISIBLE_FUNC, gallerySC, request);
          } else {
            media = gallerySC.getSearchResultListMedia();

            if (select) {
              gallerySC.getListSelected().addAll(extractIds(media));
            } else {
              gallerySC.getListSelected().removeAll(extractIds(media));
            }

            destination = getDestination(VIEW_SEARCH_RESULTS_FUNC, gallerySC, request);

          }
        }
      } else if ("SearchAdvanced".equals(function)) {
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
        PublicationTemplate template = gallerySC.getTemplate();
        if (template != null) {
          // get previous search
          DataRecord data = gallerySC.getXMLSearchContext();
          if (data == null) {
            RecordSet recordSet = template.getRecordSet();
            data = recordSet.getEmptyRecord();
          }
          request.setAttribute("Form", template.getSearchForm());
          request.setAttribute("Data", data);
        }

        destination = rootDest + "searchAdvanced.jsp";
      } else if ("ClearSearch".equals(function)) {
        gallerySC.clearSearchContext();

        destination = getDestination("SearchAdvanced", gallerySC, request);
      } else if ("LastResult".equals(function)) {
        // Reset pagination index
        gallerySC.setIndexOfCurrentPage("0");

        destination = getDestination(VIEW_SEARCH_RESULTS_FUNC, gallerySC, request);
      } else if (SEARCH_KEY_WORD.equals(function)) {
        // traitement de la recherche par mot clé
        // et de la recherche dédiée
        // récupération du mot clé et de la liste des médias concernés si il existe
        String searchKeyWord = request.getParameter(SEARCH_KEY_WORD);
        if (searchKeyWord == null) {
          searchKeyWord = (String) request.getAttribute(SEARCH_KEY_WORD);
        }
        if (!isDefined(searchKeyWord)) {
          searchKeyWord = gallerySC.getSearchKeyWord();
        }

        // Reset search if keyword has changed
        if (!gallerySC.getSearchKeyWord().equals(searchKeyWord)) {
          gallerySC.setSearchResultListMedia(new ArrayList<>());
          gallerySC.setSearchKeyWord(searchKeyWord);
          // reset pagination index
          gallerySC.setIndexOfCurrentPage("0");
        }

        // mise à jour du tag pour les retours
        gallerySC.setViewNotVisible(false);
        gallerySC.setSearchResult(true);

        QueryDescription query = new QueryDescription(searchKeyWord);

        if (StringUtil.isDefined(query.getQuery())) {
          gallerySC.setSearchResultListMedia(gallerySC.search(query));
        }

        destination = getDestination(VIEW_SEARCH_RESULTS_FUNC, gallerySC, request);
      } else if ("PaginationSearch".equals(function)) {
        processSelection(request, gallerySC);

        // traitement de la pagination : passage des paramètres
        String index = request.getParameter("Index");
        if (index != null && index.length() > 0) {
          gallerySC.setIndexOfCurrentPage(index);
        }

        destination = getDestination(VIEW_SEARCH_RESULTS_FUNC, gallerySC, request);
      } else if (VIEW_SEARCH_RESULTS_FUNC.equals(function)) {
        // passage des paramètres
        request.setAttribute(SEARCH_KEY_WORD, gallerySC.getSearchKeyWord());
        request.setAttribute(MEDIA_LIST, gallerySC.getSearchResultListMedia());
        request.setAttribute(NB_MEDIA_PER_PAGE, gallerySC.getNbMediaPerPage());
        request.setAttribute(CURRENT_PAGE_INDEX, gallerySC.getIndexOfCurrentPage());
        request.setAttribute(MEDIA_RESOLUTION, gallerySC.getDisplayedMediaResolution());
        request.setAttribute(IS_VIEW_METADATA, gallerySC.isViewMetadata());
        request.setAttribute(IS_VIEW_LIST, gallerySC.isViewList());
        request.setAttribute(SELECTED_IDS, gallerySC.getListSelected());
        request.setAttribute(IS_BASKET, gallerySC.isBasket());

        // mise à jour du tag pour les retours
        gallerySC.setSearchResult(true);
        gallerySC.setViewNotVisible(false);
        request.setAttribute(VIEW_NOT_VISIBLE_FUNC, gallerySC.isViewNotVisible());

        // appel jsp
        destination = rootDest + "viewRestrictedMediaList.jsp";
      } else if ("Search".equals(function)) {
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding(UTF_8);
        }
        List<FileItem> items = request.getFileItems();
        QueryDescription query = new QueryDescription();
        // Ajout de la requete classique
        String word = FileUploadUtil
            .getParameter(items, SEARCH_KEY_WORD, null, request.getCharacterEncoding());
        query.setQuery(word);
        gallerySC.setSearchKeyWord(word);

        // Ajout des éléments de recherche sur form XML
        PublicationTemplateImpl template = (PublicationTemplateImpl) gallerySC.getTemplate();
        if (template != null) {
          String templateFileName = template.getFileName();
          String templateName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));

          RecordTemplate searchTemplate = template.getSearchTemplate();
          if (searchTemplate != null) {
            DataRecord data = searchTemplate.getEmptyRecord();

            PagesContext context = new PagesContext("XMLSearchForm", "2", gallerySC.getLanguage(),
                gallerySC.getUserId());
            context.setEncoding(UTF_8);
            XmlSearchForm searchForm = (XmlSearchForm) template.getSearchForm();
            searchForm.update(items, data, context);

            // store xml search data in session
            gallerySC.setXMLSearchContext(data);

            for (final String fieldName : searchTemplate.getFieldNames()) {
              Field field = data.getField(fieldName);
              String fieldValue = field.getStringValue();
              if (fieldValue != null && fieldValue.trim().length() > 0) {
                String fieldQuery = fieldValue.trim().replaceAll("##", " AND "); // case à cocher multiple
                query.addFieldQuery(
                    new FieldDescription(templateName + "$$" + fieldName, fieldQuery, null));
              }
            }
          }
        }

        // Add IPTC search elements
        List<MetaData> iptcFields = gallerySC.getMetaDataKeys();
        // Loop for each xml fields
        for (final MetaData iptcField : iptcFields) {
          // recuperation valeur dans request
          String property = iptcField.getProperty();

          // ajouter à l'objet query
          if (!iptcField.isDate()) {
            String value =
                FileUploadUtil.getParameter(items, property, null, request.getCharacterEncoding());
            if (StringUtil.isDefined(value)) {
              query.addFieldQuery(new FieldDescription("IPTC_" + property, value, null));
            }
          } else {
            // cas particulier des champs de type date
            // recupere les deux champs
            String dateBeginStr = FileUploadUtil
                .getParameter(items, property + "_Begin", null, request.getCharacterEncoding());
            String dateEndStr = FileUploadUtil
                .getParameter(items, property + "_End", null, request.getCharacterEncoding());

            Date dateBegin = null;
            Date dateEnd = null;

            if (StringUtil.isDefined(dateBeginStr)) {
              dateBegin = DateUtil.stringToDate(dateBeginStr, gallerySC.getLanguage());
            }

            if (StringUtil.isDefined(dateEndStr)) {
              dateEnd = DateUtil.stringToDate(dateEndStr, gallerySC.getLanguage());
            }

            if (dateBegin != null || dateEnd != null) {
              query.addFieldQuery(new FieldDescription("IPTC_" + property,
                  dateBegin, dateEnd, null));
            }

          }
        }

        // Ajout élément PDC
        String axisValues = request.getParameter("AxisValueCouples");
        query.setTaxonomyPosition(axisValues);

        // Lancement de la recherche
        gallerySC.search(query);

        destination = getDestination(VIEW_SEARCH_RESULTS_FUNC, gallerySC, request);
      } else if (VIEW_NOT_VISIBLE_FUNC.equals(function)) {
        // traitement de la liste des médias plus visibles
        // récupération de la liste des médias plus visibles, ou création de cette liste si elle est
        // vide
        Collection<Media> media = gallerySC.getRestrictedListMedia();
        if (media.isEmpty()) {
          media = gallerySC.getNotVisible();
        }

        // mise à jour du tag pour les médias non visibles
        gallerySC.setSearchResult(false);
        gallerySC.setViewNotVisible(true);
        request.setAttribute(VIEW_NOT_VISIBLE_FUNC, gallerySC.isViewNotVisible());

        // passage des paramètres
        request.setAttribute(MEDIA_LIST, media);
        request.setAttribute(NB_MEDIA_PER_PAGE, gallerySC.getNbMediaPerPage());
        request.setAttribute(CURRENT_PAGE_INDEX, gallerySC.getIndexOfCurrentPage());
        request.setAttribute(MEDIA_RESOLUTION, gallerySC.getDisplayedMediaResolution());
        request.setAttribute(IS_VIEW_METADATA, gallerySC.isViewMetadata());
        request.setAttribute(IS_VIEW_LIST, gallerySC.isViewList());
        request.setAttribute(SELECTED_IDS, gallerySC.getListSelected());
        request.setAttribute(SEARCH_KEY_WORD, "");
        request.setAttribute(IS_BASKET, gallerySC.isBasket());

        destination = rootDest + "viewRestrictedMediaList.jsp";
      } else if (function.startsWith("portlet")) {
        // récupération des albums de 1er niveau
        gallerySC.setIndexOfCurrentPage("0");
        request.setAttribute("root", gallerySC.goToAlbum("0"));
        // chercher les derniers médias
        Collection<Media> mediaList = gallerySC.getLastRegisteredMedia();
        request.setAttribute(MEDIA_LIST, mediaList);
        destination = rootDest + "portlet.jsp";
      } else if ("copy".equals(function)) {
        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType)) {
          gallerySC.copyAlbum(objectId);
        } else {
          gallerySC.copyMedia(objectId);
        }
        destination =
            URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) +
                "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if ("CopySelectedMedia".equals(function)) {
        processSelection(request, gallerySC);
        // check list of selected media
        if (!gallerySC.getListSelected().isEmpty()) {
          // retrieve media to copy
          Collection<String> mediaIds = gallerySC.getListSelected();
          // copy media
          gallerySC.copySelectedMedia(mediaIds);
          deselectAll(gallerySC);
        }
        // Get back to current album view
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if ("cut".startsWith(function)) {
        String objectType = request.getParameter("Object");
        String objectId = request.getParameter("Id");
        if (StringUtil.isDefined(objectType) && "Node".equalsIgnoreCase(objectType)) {
          gallerySC.cutAlbum(objectId);
        } else {
          gallerySC.cutMedia(objectId);
        }
        destination =
            URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) +
                "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if ("CutSelectedMedia".equals(function)) {
        processSelection(request, gallerySC);

        if (!gallerySC.getListSelected().isEmpty()) {
          Collection<String> mediaIds = gallerySC.getListSelected();
          gallerySC.cutSelectedMedia(mediaIds);
          deselectAll(gallerySC);
        }
        // Get back to current album view
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if (function.startsWith("paste")) {
        gallerySC.paste();
        gallerySC.loadCurrentAlbum();
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      } else if (function.startsWith("Basket")) {
        // Manage basket functions
        if (BASKET_VIEW_FUNC.equals(function)) {
          // Basket view
          request.setAttribute(MEDIA_LIST, gallerySC.getBasketMedias());
          request.setAttribute(NB_MEDIA_PER_PAGE, gallerySC.getNbMediaPerPage());
          request.setAttribute(SELECTED_IDS, gallerySC.getListSelected());
          request.setAttribute(IS_ORDER, gallerySC.isOrder());
          request.setAttribute(MEDIA_TYPE_ALERT_ATTR, request.getAttribute(MEDIA_TYPE_ALERT_ATTR));

          destination = rootDest + "basket.jsp";
        } else if ("BasketDelete".equals(function)) {
          gallerySC.clearBasket();
          destination = getDestination(BASKET_VIEW_FUNC, gallerySC, request);
        } else if ("BasketDeleteMedia".equals(function)) {
          // Delete media from basket
          String mediaId = request.getParameter(MEDIA_ID);
          gallerySC.deletePhotoFromBasket(mediaId);

          destination = getDestination(BASKET_VIEW_FUNC, gallerySC, request);
        } else if ("BasketDeleteSelectedMedia".equals(function)) {
          // delete selected medias from basket
          processSelection(request, gallerySC);
          if (!gallerySC.getListSelected().isEmpty()) {
            gallerySC.deleteSelectedPhotosFromBasket();
          }
          destination = getDestination(BASKET_VIEW_FUNC, gallerySC, request);
        } else if ("BasketAddMediaList".equals(function)) {
          // Add selected photo media inside basket
          processSelection(request, gallerySC);
          if (!gallerySC.getListSelected().isEmpty()) {
            if (!gallerySC.addToBasket()) {
              request.setAttribute(MEDIA_TYPE_ALERT_ATTR, true);
            }
            destination = getDestination(BASKET_VIEW_FUNC, gallerySC, request);
          } else {
            destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
          }
        } else if ("BasketAddMedia".equals(function)) {
          // Add this media inside basket
          String mediaId = request.getParameter(MEDIA_ID);
          gallerySC.addMediaToBasket(mediaId);
          destination = getDestination(BASKET_VIEW_FUNC, gallerySC, request);
        } else if ("BasketPagination".equals(function)) {
          processSelection(request, gallerySC);

          // retour au panier
          destination = getDestination(BASKET_VIEW_FUNC, gallerySC, request);
        }
      } else if (function.startsWith("Order")) {
        if ("OrderAdd".equals(function)) {
          // Retrieve order form
          PublicationTemplate template = gallerySC.getOrderTemplate();
          if (template != null) {
            RecordSet recordSet = template.getRecordSet();
            request.setAttribute("Form", template.getUpdateForm());
            request.setAttribute("Data", recordSet.getEmptyRecord());
          }

          // retrieve convention
          String charteUrl = gallerySC.getCharteUrl();
          request.setAttribute("CharteUrl", charteUrl);

          destination = rootDest + "basketForm.jsp";
        } else if ("OrderCreate".equals(function)) {
          // create order from basket
          String orderId = gallerySC.addOrder();

          // mise à jour des données du formulaire
          if (!StringUtil.isDefined(request.getCharacterEncoding())) {
            request.setCharacterEncoding(UTF_8);
          }
          List<FileItem> items = request.getFileItems();

          PublicationTemplate template = gallerySC.getOrderTemplate();
          if (template != null) {
            RecordSet set = template.getRecordSet();
            Form form = template.getUpdateForm();
            DataRecord data = set.getRecord(orderId);
            if (data == null) {
              data = set.getEmptyRecord();
              data.setId(orderId);
            }

            PagesContext context = new PagesContext("myForm", "0", gallerySC.getLanguage(), false,
                gallerySC.getComponentId(), gallerySC.getUserId());
            context.setEncoding(UTF_8);
            context.setObjectId(orderId);

            // mise à jour des données saisies
            form.update(items, data, context);
            set.save(data);
          }

          // notify gallery manager
          gallerySC.sendAskOrder(orderId);

          // view order list destination
          destination = getDestination(ORDER_VIEW_LIST_FUNC, gallerySC, request);
        } else if ("OrderUpdate".equals(function)) {
          String orderId = request.getParameter(ORDER_ID);
          // update order
          updateOrder(request, gallerySC, orderId, userId);
          // notify reader user
          gallerySC.sendAskOrderUser(orderId);
          destination = getDestination(ORDER_VIEW_LIST_FUNC, gallerySC, request);
        } else if (ORDER_VIEW_LIST_FUNC.equals(function)) {
          Collection<Order> orders;
          if (highestUserRole == SilverpeasRole.ADMIN) {
            // if manager retrieve all orders
            orders = gallerySC.getAllOrders();
          } else {
            // else if retrieve only user orders
            orders = gallerySC.getOrdersByUser();
          }
          request.setAttribute("NbOrdersProcess", getNbOrdersProcess(orders));
          request.setAttribute("Orders", orders);
          destination = rootDest + "orders.jsp";
        } else if ("OrderView".equals(function)) {
          String orderId = request.getParameter(ORDER_ID);
          if (orderId == null || orderId.length() == 0 || "null".equals(orderId)) {
            orderId = (String) request.getAttribute(ORDER_ID);
          }

          if (highestUserRole == SilverpeasRole.ADMIN || gallerySC.isAccessAuthorized(orderId)) {
            gallerySC.setCurrentOrderId(orderId);

            destination = getDestination(ORDER_VIEW_PAGIN_FUNC, gallerySC, request);
          } else {
            destination = "/admin/jsp/accessForbidden.jsp";
          }
        } else if (ORDER_VIEW_PAGIN_FUNC.equals(function)) {
          String orderId = gallerySC.getCurrentOrderId();
          request.setAttribute("Order", gallerySC.getOrder(orderId));
          request.setAttribute(NB_MEDIA_PER_PAGE, gallerySC.getNbMediaPerPage());

          PublicationTemplate template = gallerySC.getOrderTemplate();
          if (template != null) {
            RecordSet recordSet = template.getRecordSet();
            DataRecord data = recordSet.getRecord(orderId);
            if (data != null) {
              request.setAttribute("XMLForm", template.getViewForm());
              request.setAttribute("XMLData", data);
            }
          }
          destination = rootDest + "order.jsp";
        } else if ("OrderPagination".equals(function)) {
          // retour à la demande
          destination = getDestination(ORDER_VIEW_PAGIN_FUNC, gallerySC, request);
        } else if ("OrderDownloadMedia".equals(function)) {
          final SimpleCache cache = getSessionCacheService().getCache();
          destination = Optional.ofNullable(request.getParameter("downloadId"))
              .filter(StringUtil::isDefined)
              .map(d -> Optional.ofNullable(cache.remove(d, String.class))
                    .filter(StringUtil::isDefined)
                    .map(u -> {
                      request.setAttribute(DOWNLOAD_CONTEXT_PARAM, true);
                      return u.replace(URLUtil.getApplicationURL(), StringUtil.EMPTY);
                    })
                    .orElseGet(() -> getDestination(ORDER_VIEW_PAGIN_FUNC, gallerySC, request)))
              .orElseGet(() -> {
                final String mediaId = request.getParameter(MEDIA_ID);
                final String orderId = request.getParameter(ORDER_ID);
                return Optional.of(gallerySC.getUrl(orderId, mediaId))
                    .filter(StringUtil::isDefined)
                    .map(u -> {
                      gallerySC.updateOrderRow(orderId, mediaId);
                      final String downloadUrlKey = UUID.randomUUID().toString();
                      cache.put(downloadUrlKey, u);
                      return sendJson(encodeObject(o -> o.put("downloadId", downloadUrlKey)));
                    }).orElseGet(() -> {
                      final String errorMessage = gallerySC.getString("gallery.alreadyDownloaded");
                      return sendJson(encodeObject(o -> o.put("errorMessage", errorMessage)));
                    });
              });
        }
      } else if (function.startsWith("Export")) {
        if ("ExportAlbum".equals(function)) {
          String albumId = request.getParameter(ALBUM_ID);
          String format = request.getParameter("format");
          ExportReport exportRpt =
              gallerySC.exportAlbum(albumId, MediaResolution.fromNameOrLabel(format));
          request.setAttribute("ExportReport", exportRpt);
          destination = rootDest +"downloadZip.jsp";
        } else if ("ExportSelection".equals(function)) {
          String format = request.getParameter("format");
          ExportReport exportRpt =
              gallerySC.exportSelection(MediaResolution.fromNameOrLabel(format));
          request.setAttribute("ExportReport", exportRpt);
          gallerySC.clearBasket();
          destination = rootDest +"downloadZip.jsp";
        }
      } else {
        destination = rootDest + function;
      }
    } catch (AccessForbiddenException afe) {
      SilverLogger.getLogger(this).warn(afe);
      destination = "/admin/jsp/accessForbidden.jsp";
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String processMediaType(final GallerySessionController gallerySC,
      final HttpRequest request, final String id, final String type) {
    String destination;
    try {
      MediaType mediaType = MediaType.from(type);
      if (mediaType != MediaType.Unknown) {
        // traitement des médias
        request.setAttribute(MEDIA_ID, id);
        destination = getDestination(MEDIA_VIEW_FUNC, gallerySC, request);
      } else if ("Node".equals(type)) {
        // traitement des noeuds = les albums
        destination = getDestination("ViewAlbum", gallerySC, request);
      } else {
        destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
      destination = getDocumentNotFoundDestination(gallerySC, request);
    }
    return destination;
  }

  private String processMedia(final GallerySessionController gallerySC, final HttpRequest request,
      final String rootDest, final SilverpeasRole highestUserRole, final String mediaId) {
    String destination;
    try {
      Media media = gallerySC.getMedia(mediaId);

      request.setAttribute(SEARCH_KEY_WORD, request.getParameter(SEARCH_KEY_WORD));
      request.setAttribute("Rang", gallerySC.getRang());
      request.setAttribute("NbMedia", gallerySC.goToAlbum().getMedia().size());
      request.setAttribute(IS_VIEW_METADATA, gallerySC.isViewMetadata());
      request.setAttribute("IsWatermark", gallerySC.isMakeWatermark());
      request.setAttribute("ImageResolutionPreview", gallerySC.getImageResolutionPreview(media));

      boolean linkDownload = isLinkDownloadable(gallerySC, highestUserRole, media);
      request.setAttribute("ViewLinkDownload", linkDownload);

      putMediaCommonParameters(request, gallerySC, media, highestUserRole);

      // Prepare XML form data
      putXMLDisplayerIntoRequest(media, request, gallerySC);
      // Slideshow requirements
      request.setAttribute(ALBUM_ID, gallerySC.getCurrentAlbumId());
      request.setAttribute("wait", gallerySC.getSlideshowWait());
      // Add this following line for backward compatibility TODO create migration script in
      // order to insert each media inside content manager
      request.setAttribute("SilverObjetId", gallerySC.getSilverObjectId(mediaId));
      // appel jsp
      destination = rootDest + MEDIA_PREFIX + media.getType().toString() + "View.jsp";
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
      destination = getDocumentNotFoundDestination(gallerySC, request);
    }
    return destination;
  }

  private boolean isLinkDownloadable(final GallerySessionController gallerySC,
      final SilverpeasRole highestUserRole, final Media media) {
    if (gallerySC.getHighestSilverpeasUserRole().isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)) {
      return true;
    }
    return (SilverpeasRole.PRIVILEGED_USER == highestUserRole && media.isDownloadable()) ||
        (SilverpeasRole.WRITER == highestUserRole &&
            media.getCreatorId().equals(gallerySC.getUserId()));
  }

  private Integer getNbOrdersProcess(Collection<Order> orders) {
    int nb = 0;
    for (final Order order : orders) {
      if (isSqlDefined(order.getProcessUserId())) {
        nb++;
      }
    }
    return nb;
  }

  private String createMediaData(HttpRequest request, GallerySessionController gallerySC)
      throws ParseException, PublicationTemplateException {

    final List<FileItem> parameters = request.getFileItems();
    MediaType mediaType = MediaType.from(request.getParameter("type"));
    final MediaDataCreateDelegate delegate =
        new MediaDataCreateDelegate(mediaType, gallerySC.getLanguage(),
            gallerySC.getCurrentAlbumId(), parameters);

    // 1. Getting the header data
    delegate.getHeaderData()
        .setHomepageUrl(request.getParameter(ParameterNames.StreamingHomepageUrl));
    delegate.getHeaderData().setTitle(request.getParameter(ParameterNames.MediaTitle));
    delegate.getHeaderData().setDescription(request.getParameter(ParameterNames.MediaDescription));
    delegate.getHeaderData().setAuthor(request.getParameter(ParameterNames.MediaAuthor));
    delegate.getHeaderData().setKeyWord(request.getParameter(ParameterNames.MediaKeyWord));
    delegate.getHeaderData()
        .setBeginVisibilityDate(request.getParameter(ParameterNames.MediaBeginVisibilityDate));
    delegate.getHeaderData()
        .setEndVisibilityDate(request.getParameter(ParameterNames.MediaEndVisibilityDate));
    delegate.getHeaderData()
        .setDownloadAuthorized(request.getParameter(ParameterNames.MediaDownloadAuthorized));
    delegate.getHeaderData()
        .setBeginDownloadDate(request.getParameter(ParameterNames.MediaBeginDownloadDate));
    delegate.getHeaderData()
        .setEndDownloadDate(request.getParameter(ParameterNames.MediaEndDownloadDate));

    String positions = request.getParameter("Positions");
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity withClassification = PdcClassificationEntity.fromJSON(positions);
      delegate.getHeaderData().setPdcPositions(withClassification.getPdcPositions());
    }

    // 2. Getting form data
    PublicationTemplate template = gallerySC.getTemplate();
    if (template != null) {
      delegate.setForm(template.getRecordSet(), template.getUpdateForm());
    }

    // Persisting the media in database & on file system
    return gallerySC.createMedia(delegate);
  }

  private void updateOrder(HttpServletRequest request, GallerySessionController gallerySC,
      String orderId, String userId) {
    // rechercher la demande
    Order order = gallerySC.getOrder(orderId);

    // mettre à jour la date et le user
    order.setProcessUserId(userId);

    // mettre à jour les lignes
    List<OrderRow> rows = order.getRows();
    for (final OrderRow orderRow : rows) {
      String mediaId = orderRow.getMediaId();
      String download = request.getParameter("DownloadType" + mediaId);
      orderRow.setDownloadDecision(download);
    }
    order.setRows(rows);
    gallerySC.updateOrder(order);

  }

  /**
   * Updates a list of media.
   * @param request
   * @param gallerySC
   * @param mediaIds
   * @param encoding
   * @throws Exception
   */
  private void updateSelectedMedia(HttpRequest request, GallerySessionController gallerySC,
      Collection<String> mediaIds, String encoding)
      throws ParseException, PublicationTemplateException {

    // Getting all HTTP parameters
    final List<FileItem> parameters = new ArrayList<>();
    for (FileItem param : request.getFileItems()) {
      parameters.add(param);
    }

    final MediaDataUpdateDelegate delegate =
        new MediaDataUpdateDelegate(MediaType.Photo, gallerySC.getLanguage(),
            gallerySC.getCurrentAlbumId(), parameters);

    // Setting header data
    delegate.getHeaderData()
        .setTitle(FileUploadUtil.getParameter(parameters, "Media$Title", null, encoding));
    delegate.getHeaderData().setDescription(
        FileUploadUtil.getParameter(parameters, "Media$Description", null, encoding));
    delegate.getHeaderData()
        .setAuthor(FileUploadUtil.getParameter(parameters, "Media$Author", null, encoding));
    delegate.getHeaderData()
        .setKeyWord(FileUploadUtil.getParameter(parameters, "Media$KeyWord", null, encoding));
    delegate.getHeaderData().setBeginVisibilityDate(
        FileUploadUtil.getParameter(parameters, "Media$BeginVisibilityDate", null, encoding));
    delegate.getHeaderData().setEndVisibilityDate(
        FileUploadUtil.getParameter(parameters, "Media$EndVisibilityDate", null, encoding));
    delegate.getHeaderData().setDownloadAuthorized(
        FileUploadUtil.getParameter(parameters, "Media$DownloadAuthorized", null, encoding));
    delegate.getHeaderData().setBeginDownloadDate(
        FileUploadUtil.getParameter(parameters, "Media$BeginDownloadDate", null, encoding));
    delegate.getHeaderData().setEndDownloadDate(
        FileUploadUtil.getParameter(parameters, "Media$EndDownloadDate", null, encoding));

    // Setting form
    PublicationTemplate template = gallerySC.getTemplate();
    if (template != null) {
      delegate.setForm(template.getRecordSet(), template.getUpdateForm());
    }

    // Process data
    gallerySC.updateMediaByUser(mediaIds, delegate);

    // Reload media of current album
    gallerySC.loadCurrentAlbum();
  }

  private void deleteSelectedMedia(GallerySessionController gallerySC,
      Collection<String> mediaIds) {
    // suppression des médias selectionnés : traitement par lot
    gallerySC.deleteMedia(mediaIds);
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param));
  }

  private void putXMLDisplayerIntoRequest(Media media, HttpServletRequest request,
      GallerySessionController gallerySC) throws PublicationTemplateException, FormException {
    String mediaId = media.getId();
    PublicationTemplate template = gallerySC.getTemplate();
    if (template != null) {
      RecordSet recordSet = template.getRecordSet();
      DataRecord data = recordSet.getRecord(mediaId);
      if (data != null) {
        request.setAttribute("XMLForm", template.getViewForm());
        request.setAttribute("XMLData", data);
      }
    }
  }

  private void updateMediaData(String mediaId, HttpRequest request,
      GallerySessionController gallerySC) throws ParseException, PublicationTemplateException {

    final MediaDataUpdateDelegate delegate =
        new MediaDataUpdateDelegate(MediaType.Photo, gallerySC.getLanguage(),
            gallerySC.getCurrentAlbumId(), request.getFileItems(), false);

    // 1. Récupération des données de l'entête
    delegate.getHeaderData()
        .setHomepageUrl(request.getParameter(ParameterNames.StreamingHomepageUrl));
    delegate.getHeaderData().setTitle(request.getParameter(ParameterNames.MediaTitle));
    delegate.getHeaderData().setDescription(request.getParameter(ParameterNames.MediaDescription));
    delegate.getHeaderData().setAuthor(request.getParameter(ParameterNames.MediaAuthor));
    delegate.getHeaderData().setKeyWord(request.getParameter(ParameterNames.MediaKeyWord));
    delegate.getHeaderData()
        .setBeginVisibilityDate(request.getParameter(ParameterNames.MediaBeginVisibilityDate));
    delegate.getHeaderData()
        .setEndVisibilityDate(request.getParameter(ParameterNames.MediaEndVisibilityDate));
    delegate.getHeaderData()
        .setDownloadAuthorized(request.getParameter(ParameterNames.MediaDownloadAuthorized));
    delegate.getHeaderData()
        .setBeginDownloadDate(request.getParameter(ParameterNames.MediaBeginDownloadDate));
    delegate.getHeaderData()
        .setEndDownloadDate(request.getParameter(ParameterNames.MediaEndDownloadDate));

    // 2. Récupération des données du formulaire
    PublicationTemplate template = gallerySC.getTemplate();
    if (template != null) {
      delegate.setForm(template.getRecordSet(), template.getUpdateForm());
    }

    // Enregistrement des informations des médias
    gallerySC.updateMediaByUser(mediaId, delegate);

    // mise à jour de l'album courant
    Collection<String> albumIds = gallerySC.getAlbumIdsOf(mediaId);
    // regarder si l'album courant est dans la liste des albums
    boolean inAlbum = false;
    boolean first = true;
    String firstAlbumId = "0";
    for (final String albumId : albumIds) {
      if (first) {
        firstAlbumId = albumId;
        first = false;
      }
      if (albumId.equals(gallerySC.getCurrentAlbumId())) {
        inAlbum = true;
      }
    }
    if (!inAlbum) {
      gallerySC.setCurrentAlbumId(firstAlbumId);
    }
  }

  private void putMediaCommonParameters(HttpServletRequest request,
      GallerySessionController gallerySC, Media media, SilverpeasRole userRole) {
    request.setAttribute("Media", media);
    Integer nbComments = 0;
    try {
      nbComments = gallerySC.getAllComments(media).size();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    request.setAttribute("NbComments", nbComments);
    request.setAttribute("Path", gallerySC.getPath());
    request.setAttribute(IS_USE_PDC, gallerySC.isUsePdc());
    request.setAttribute("XMLFormName", gallerySC.getXMLFormName());
    request.setAttribute(IS_BASKET, gallerySC.isBasket());
    request.setAttribute(IS_ORDER, gallerySC.isOrder());

    boolean allowedToUpdateMedia = userRole.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER) ||
        (userRole == SilverpeasRole.WRITER && media.getCreatorId().equals(gallerySC.getUserId()));
    request.setAttribute("UpdateMediaAllowed", allowedToUpdateMedia);

    request.setAttribute("ShowCommentsTab", gallerySC.areCommentsEnabled());
  }

  /**
   * update gallery session controller list of selected elements
   * @param request
   * @param gallerySC
   */
  private void processSelection(HttpServletRequest request, GallerySessionController gallerySC) {
    String selectedIds = request.getParameter(SELECTED_IDS);
    String notSelectedIds = request.getParameter("NotSelectedIds");
    Collection<String> memSelected = gallerySC.getListSelected();

    if (StringUtil.isDefined(selectedIds)) {
      for (String selectedId : selectedIds.split(",")) {
        if (!memSelected.contains(selectedId)) {
          memSelected.add(selectedId);
        }
      }
    }

    if (StringUtil.isDefined(notSelectedIds)) {
      for (String notSelectedId : notSelectedIds.split(",")) {
        memSelected.remove(notSelectedId);
      }
    }
  }

  private List<String> extractIds(Collection<Media> col) {
    List<String> ids = new ArrayList<>();
    if (col != null) {
      Iterator<Media> it = col.iterator();
      Media media;
      while (it.hasNext()) {
        media = it.next();
        if (media != null) {
          ids.add(media.getId());
        }
      }
    }
    return ids;
  }

  private String returnToAlbum(HttpRequest request, GallerySessionController gallerySC) {
    String destination;
    // retour d'où on vient
    if (!gallerySC.isSearchResult() && !gallerySC.isViewNotVisible()) {
      // retour à l'album en cours
      destination = getDestination(GO_TO_CURRENT_ALBUM_FUNC, gallerySC, request);
    } else {
      // retour aux résultats de recherche ou à la liste des médias non visibles
      if (gallerySC.isViewNotVisible()) {
        destination = getDestination(VIEW_NOT_VISIBLE_FUNC, gallerySC, request);
      } else {
        destination = getDestination(VIEW_SEARCH_RESULTS_FUNC, gallerySC, request);
      }
    }
    return destination;
  }

  private void deselectAll(GallerySessionController gallerySC) {
    gallerySC.setSelect(false);
    gallerySC.clearListSelected();
  }

  private String getDocumentNotFoundDestination(GallerySessionController gallery,
      HttpServletRequest request) {
    request.setAttribute("ComponentId", gallery.getComponentId());
    return "/admin/jsp/documentNotFound.jsp";
  }

}