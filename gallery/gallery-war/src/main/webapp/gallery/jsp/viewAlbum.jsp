<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="com.silverpeas.gallery.ImageType"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp"%>
<%
  // recuperation des parametres :
  AlbumDetail currentAlbum = (AlbumDetail) request.getAttribute("CurrentAlbum");
  String userId = (String) request.getAttribute("UserId");
  String profile = (String) request.getAttribute("Profile");
  List<NodeDetail> path = (List) request.getAttribute("Path");
  int firstPhotoIndex = ((Integer) request.getAttribute("FirstPhotoIndex")).intValue();
  int nbPhotosPerPage = ((Integer) request.getAttribute("NbPhotosPerPage")).intValue();
  String taille = (String) request.getAttribute("Taille");
  Boolean dragAndDropEnable = (Boolean) request.getAttribute("DragAndDropEnable");
  Boolean isViewMetadata = (Boolean) request.getAttribute("IsViewMetadata");
  Boolean isViewList = (Boolean) request.getAttribute("IsViewList");
  Collection selectedIds = (Collection) request.getAttribute("SelectedIds");
  boolean isPdcUsed = ((Boolean) request.getAttribute("IsUsePdc")).booleanValue();
  boolean isBasket = ((Boolean) request.getAttribute("IsBasket")).booleanValue();
  boolean isGuest = ((Boolean) request.getAttribute("IsGuest")).booleanValue();
  boolean isPrivateSearch = ((Boolean) request.getAttribute("IsPrivateSearch")).booleanValue();
  List<AlbumDetail> albums = (List) request.getAttribute("Albums");

  session.setAttribute("Silverpeas_Album_ComponentId", componentId);

  //For Drag And Drop
  ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();

  // declaration des variables :
  String fctAddPhoto = "AddPhoto";
  int nbAffiche = 0;
  String albumId = "";
  List photos = null;
  int id = 0;
  int nbParLigne = 1;
  int largeurCellule = 0;
  String extension = "";
  String albumName = "";
  String albumDescription = "";
  String albumUrl = "";
  boolean viewMetadata = isViewMetadata.booleanValue();
  boolean viewList = isViewList.booleanValue();
  String typeAff = "1";
  String galleryName = "";

  if (currentAlbum != null) {
    albumId = String.valueOf(currentAlbum.getId());
    photos = (List) currentAlbum.getPhotos();
    albumName = currentAlbum.getName();
    albumDescription = currentAlbum.getDescription();
    albumUrl = currentAlbum.getLink();
  }

  boolean somePhotos = photos != null && !photos.isEmpty();
  boolean someAlbums = currentAlbum.getChildrenDetails() != null
          && !currentAlbum.getChildrenDetails().isEmpty();

  // initialisation de la pagination
  Pagination pagination = gef.getPagination(photos.size(), nbPhotosPerPage, firstPhotoIndex);
  List affPhotos = photos.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());

  // creation du chemin :
  String namePath = "";
  boolean suivant = false;
  //Collections.reverse(path);
  galleryName = browseBar.getBreadCrumb();

  // calcul du nombre de photo par ligne en fonction de la taille
  if (taille.equals("66x50")) {
    nbParLigne = 8;
    extension = "_66x50.jpg";
  } else if (taille.equals("133x100")) {
    nbParLigne = 5;
    extension = "_133x100.jpg";
    if (viewList) {
      typeAff = "2";
    }
  } else if (taille.equals("266x150")) {
    nbParLigne = 3;
    extension = "_266x150.jpg";
    if (viewList) {
      typeAff = "3";
      nbParLigne = 1;
    }
  }
  largeurCellule = 100 / nbParLigne;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <view:looknfeel/>
    <script type="text/javascript" src="<%=m_context%>/gallery/jsp/javaScript/dragAndDrop.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript">

      var currentGallery = {
        id: "<%= albumId%>",
        name: "<%= albumName%>",
        description: "<%= albumDescription%>"
      };

      $(document).ready(function(){
      <%if ("admin".equals(profile)) {%>
          $("#albumList").sortable({opacity: 0.4, cursor: 'move'});
      
          $('#albumList').bind('sortupdate', function(event, ui) {
            var reg=new RegExp("album", "g");
            var data = $('#albumList').sortable('serialize');
            data += "&";  // pour que le dernier élément soit de la même longueur que les autres
            var tableau=data.split(reg);
            var param = "";
            for (var i=0; i<tableau.length; i++) {
              if (i > 0) {
                param += ",";
              }
              param += tableau[i].substring(3, tableau[i].length-1);
            }
            sortAlbums(param);
          });
      <%}%>
        });

  
        function sortAlbums(orderedList)
        {
          $.get('<%=m_context%>/Album', { orderedList:orderedList,Action:'Sort'},
          function(data){
            data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
            if (data == "error")
            {
              alert("Une erreur s'est produite !");
            }
          }, 'text');
          if (pageMustBeReloadingAfterSorting) {
            //force page reloading to reinit menus
            reloadIncludingPage();
          }
        }
  
        var albumWindow = window;
	
        function addFavorite(m_sAbsolute,m_context,name,description,url) 
        {
          urlWindow = m_sAbsolute + m_context + "/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
          windowName = "albumWindow";
          larg = "550";
          haut = "250";
          windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
          if (!albumWindow.closed && albumWindow.name== "albumWindow") {
            albumWindow.close();
          }
          albumWindow = SP_openWindow(urlWindow, windowName, larg, haut, windowParams);
        }

        function deleteConfirm(id,nom)  {
          // confirmation de suppression de l'album
          if(window.confirm("<%=resource.getString("gallery.confirmDeleteAlbum")%> '" + nom + "' ?")) {
            document.albumForm.action = "DeleteAlbum";
            document.albumForm.Id.value = id;
            document.albumForm.submit();
          }
        }
	
        function choiceGoTo(selectedIndex) {
          // envoi du choix de la taille des vignettes
          if (selectedIndex != 0 && selectedIndex != 1) {
            document.ChoiceSelectForm.Choice.value = document.photoForm.ChoiceSize[selectedIndex].value;
            document.ChoiceSelectForm.submit();
          }
        }
	
        function sendData() {
          // envoi des photos selectionnees pour la modif par lot
          var selectedPhotos = getObjects(true);
          if (selectedPhotos && selectedPhotos.length > 0)
          {
            document.photoForm.SelectedIds.value 	= selectedPhotos;
            document.photoForm.NotSelectedIds.value = getObjects(false);
            document.photoForm.submit();
          }
        }
	
        function sendToBasket() {
          // envoi des photos selectionnees dans le panier
          var selectedPhotos = getObjects(true);
          if (selectedPhotos && selectedPhotos.length > 0)
          {
            document.photoForm.SelectedIds.value 	= selectedPhotos;
            document.photoForm.NotSelectedIds.value = getObjects(false);
            document.photoForm.action	= "BasketAddPhotos";
            document.photoForm.submit(); 
          }
        }
	
        function sendDataDelete() {
          //confirmation de suppression de l'album
          var selectedPhotos = getObjects(true);
          if (selectedPhotos && selectedPhotos.length > 0)
          {
            if(window.confirm("<%=resource.getString("gallery.confirmDeletePhotos")%> ")) {
              // envoi des photos selectionnees pour la modif par lot
              document.photoForm.SelectedIds.value 	= selectedPhotos;
              document.photoForm.NotSelectedIds.value = getObjects(false);
              document.photoForm.action				= "DeleteSelectedPhoto";
              document.photoForm.submit();
            }
          }
        }
	
        function sendDataCategorize() {
          var selectedPhotos = getObjects(true);
          if (selectedPhotos && selectedPhotos.length > 0)
          {
            var selectedIds = selectedPhotos;
            var notSelectedIds = getObjects(false);
		
            urlWindow = "CategorizeSelectedPhoto?SelectedIds="+selectedIds+"&NotSelectedIds="+notSelectedIds;
            windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
            if (!albumWindow.closed && albumWindow.name== "albumWindow") {
              albumWindow.close();
            }
            albumWindow = SP_openWindow(urlWindow, "albumWindow", "550", "250", windowParams);
          }
        }
	
        function sendDataForAddPath() 
        {
          // envoi des photos selectionnees pour le placement par lot
          var selectedPhotos = getObjects(true);
          if (selectedPhotos && selectedPhotos.length > 0)
          {
            document.photoForm.SelectedIds.value  = selectedPhotos;
            document.photoForm.NotSelectedIds.value = getObjects(false);
            document.photoForm.action       = "AddPathForSelectedPhoto";
            document.photoForm.submit();
          }
        }
	
        function getObjects(selected)	{
          var  items = "";
          try {
            var boxItems = document.photoForm.SelectPhoto;
            if (boxItems != null) {
              // au moins une checkbox exist
              var nbBox = boxItems.length;
              if ( (nbBox == null) && (boxItems.checked == selected) ) {
                // il n'y a qu'une checkbox non selectionnee
                items += boxItems.value+",";
              } else {
                // search not checked boxes 
                for (i=0;i<boxItems.length ;i++ ) {
                  if (boxItems[i].checked == selected) {
                    items += boxItems[i].value+",";
                  }
                }
              }
            }
          }
          catch (e)  {
            //Checkboxes are not displayed 
          }
          return items;
        }
	
        function doPagination(index) {
          document.photoForm.SelectedIds.value 	= getObjects(true);
          document.photoForm.NotSelectedIds.value = getObjects(false);
          document.photoForm.Index.value 			= index;
          document.photoForm.action				= "Pagination";
          document.photoForm.submit();
        }
	
        function sortGoTo(selectedIndex) {
          // envoi du choix du tri
          if (selectedIndex != 0 && selectedIndex != 1)  {
            document.OrderBySelectForm.Tri.value = document.photoForm.SortBy[selectedIndex].value;
            document.OrderBySelectForm.submit();
          }
        }
	
        function uploadCompleted(s)  {
          //window.alert("In uploadCompleted !"+s);
          location.href="<%=m_context + URLManager.getURL(null, componentId)%>ViewAlbum?Id=<%=currentAlbum.getNodePK().getId()%>";
          //return true;
        }

        function showDnD() {
          var url = "<%=URLManager.getFullApplicationURL(request)%>/RgalleryDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&AlbumId=<%=currentAlbum.getNodePK().getId()%>";
          var message = "<%=URLManager.getFullApplicationURL(request)%>/upload/Gallery_<%=resource.getLanguage()%>.html";
		
      <%ResourceLocator uploadSettings = new ResourceLocator(
                "com.stratelia.webactiv.util.uploads.uploadSettings", "");
        String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");%>
            showHideDragDrop(url, message,'<%=resource.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resource.getString("GML.DragNDropExpand")%>','<%=resource.getString("GML.DragNDropCollapse")%>');
          }
	
	
          function clipboardPaste() {
            top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=RGallery&SpaceFrom=<%=spaceId%>&ComponentFrom=<%=componentId%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("GoToCurrentAlbum",
                    "UTF-8"))%>&TargetFrame=MyMain&message=REFRESH');
                      }

                      function clipboardCopy() {
                        top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>copy?Object=Node&Id=<%=currentAlbum.getNodePK().getId()%>';
                      }
	
                      function clipboardCut() {
                        top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>cut?Object=Node&Id=<%=currentAlbum.getNodePK().getId()%>';
                      }
	
                      function CopySelectedPhoto()  {
                        var selectedPhotos = getObjects(true);
                        if (selectedPhotos && selectedPhotos.length > 0)
                        {
                          document.photoForm.SelectedIds.value 	= selectedPhotos;
                          document.photoForm.NotSelectedIds.value = getObjects(false);
                          document.photoForm.action				= "CopySelectedPhoto";
                          document.photoForm.submit();
                        }
                      }
	
                      function CutSelectedPhoto()  {
                        var selectedPhotos = getObjects(true);
                        if (selectedPhotos && selectedPhotos.length > 0)
                        {
                          document.photoForm.SelectedIds.value  = selectedPhotos;
                          document.photoForm.NotSelectedIds.value = getObjects(false);
                          document.photoForm.action       = "CutSelectedPhoto";
                          document.photoForm.submit();
                        }
                      }
	  
    </script>
  </head>
  <body>
    <%
      // creation de la barre de navigation
      browseBar.setDomainName(spaceLabel);
      browseBar.setComponentName(componentLabel, "Main");
      displayPath(path, browseBar);

      if ("admin".equals(profile) || "publisher".equals(profile)) {
        operationPane.addOperationOfCreation(resource.getIcon("gallery.addAlbum"), resource.getString("gallery.ajoutSousAlbum"), "javaScript:openGalleryEditor()");
        // modification et suppression de l'album courant
        if ("admin".equals(profile) || ("publisher".equals(profile) && currentAlbum.getCreatorId().equals(userId))) {
          // avec gestion des droits pour les publieurs
          operationPane.addOperation(resource.getIcon("gallery.updatelbum"), resource.getString("gallery.updateAlbum"), "javaScript:openGalleryEditor(currentGallery)");
          operationPane.addOperation(resource.getIcon("gallery.deleteAlbum"), resource.getString("gallery.deleteThisAlbum"), "javaScript:deleteConfirm('" + albumId + "','"
                  + EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(albumName))+ "')");
          operationPane.addLine();
        }

        if ("admin".equals(profile)) {
          operationPane.addOperation(resource.getIcon("gallery.copy"), resource.getString("gallery.copyAlbum"), "javascript:onClick=clipboardCopy()");
          operationPane.addOperation(resource.getIcon("gallery.cut"), resource.getString("gallery.cutAlbum"), "javascript:onClick=clipboardCut()");
          operationPane.addLine();
        }

        // possibilite de modifier ou supprimer les photos par lot
        operationPane.addOperation(resource.getIcon("gallery.updateSelectedPhoto"), resource.getString("gallery.updateSelectedPhoto"), "javascript:onClick=sendData();");
        operationPane.addOperation(resource.getIcon("gallery.deleteSelectedPhoto"), resource.getString("gallery.deleteSelectedPhoto"), "javascript:onClick=sendDataDelete();");
        if (isPdcUsed) {
          // si on a le classement Pdc : possibilite de classer par lot
          operationPane.addOperation(resource.getIcon("gallery.categorizeSelectedPhoto"), resource.getString("gallery.categorizeSelectedPhoto"), "javascript:onClick=sendDataCategorize();");
        }
        if ("admin".equals(profile)) {
          // possibilite de placer les photos par lot
          operationPane.addOperation(resource.getIcon("gallery.addPathForSelectedPhoto"), resource.getString("gallery.addPathForSelectedPhoto"), "javascript:onClick=sendDataForAddPath()");
        }
      }

      // bouton pour tout selectionner ou tout deselectionner
      operationPane.addOperation(resource.getIcon("gallery.allSelect"), resource.getString("gallery.allSelect"), "AllSelected");

      if ("admin".equals(profile)) {
        operationPane.addOperation(resource.getIcon("gallery.copy"), resource.getString("gallery.copySelectedPhoto"), "javascript:onClick=CopySelectedPhoto()");
        operationPane.addOperation(resource.getIcon("gallery.cut"), resource.getString("gallery.cutSelectedPhoto"), "javascript:onClick=CutSelectedPhoto()");
        operationPane.addOperation(resource.getIcon("gallery.paste"), resource.getString("GML.paste"), "javascript:onClick=clipboardPaste()");
        operationPane.addLine();
      }
      if ("admin".equals(profile) || "publisher".equals(profile) || "writer".equals(profile)) {
        // possibilite d'ajouter des photos pour les "admin", "publisher" et "writer"
        operationPane.addOperationOfCreation(resource.getIcon("gallery.addPhoto"), resource.getString("gallery.ajoutPhoto"), "AddPhoto");
        operationPane.addLine();
      }

      if ("user".equals(profile) && isBasket) {
        // ajouter les photos selectionnees au panier
        operationPane.addOperation(resource.getIcon("gallery.addToBasketSelectedPhoto"), resource.getString("gallery.addToBasketSelectedPhoto"), "javascript:onClick=sendToBasket();");
        // voir le panier
        operationPane.addOperation(resource.getIcon("gallery.viewBasket"), resource.getString("gallery.viewBasket"), "BasketView");
        operationPane.addLine();
      }

      if (photos.size() > 1) {
        // diaporama
        operationPane.addOperation(resource.getIcon("gallery.startDiaporama"), resource.getString("gallery.diaporama"), "StartDiaporama?Debut=" + "ok");
      }

      // favoris
      if (!isGuest) {
        operationPane.addOperation(resource.getIcon("gallery.addFavorite"), resource.getString(
                "gallery.addFavorite"), "javaScript:addFavorite('"
                + URLManager.getServerURL(request) + "','" + m_context + "','" + EncodeHelper.
                javaStringToHtmlString(EncodeHelper.javaStringToJsString(galleryName)) + "','"
                + EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(
                albumDescription)) + "','" + albumUrl + "')");
      }

      if (isPrivateSearch) {
        // derniers resultat de la recherche
        operationPane.addLine();
        operationPane.addOperation(resource.getIcon("gallery.lastResult"), resource.getString("gallery.lastResult"), "LastResult");
      }

      out.println(window.printBefore());
      out.println(frame.printBefore());
%>      
<view:areaOfOperationOfCreation/>    
<%
      // afficher les sous albums
      // ------------------------
      if (currentAlbum.getChildrenDetails() != null) {
        out.println("<table width=\"98%\">");
        out.println("<tr><td>");
        out.println("<div id=\"subTopics\">");
        out.println("<ul id=\"albumList\" >");
        Iterator it = albums.iterator();
        while (it.hasNext()) {
          AlbumDetail unAlbum = (AlbumDetail) it.next();
          id = unAlbum.getId();
          String nom = unAlbum.getName();
          String link = "";
          if (unAlbum.getPermalink() != null) {
            link = "&nbsp;<a href=\"" + unAlbum.getPermalink() + "\"><img src=\""
                    + resource.getIcon("gallery.link") + "\" border=\"0\" align=\"bottom\" alt=\""
                    + resource.getString("gallery.CopyAlbumLink") + "\" title=\"" + resource.
                    getString("gallery.CopyAlbumLink") + "\"></a>";
          }
    %>
    <li id="album_<%=id%>" class="ui-state-default">
      <a href="ViewAlbum?Id=<%=id%>">
        <strong><%=unAlbum.getName()%>
          <span><%=unAlbum.getNbPhotos()%></span>
        </strong>
        <span><%=unAlbum.getDescription()%></span>
      </a>
    </li>
    <%
        }
        out.println("</ul>");
        out.println("</div>");
      }
      out.println("</td></tr></table>");
    %>

    <%if (!"user".equals(profile) && dragAndDropEnable != null
              && dragAndDropEnable.booleanValue()) {%>
    <!-- Affichage de la zone de drag And Drop -->
    <center>
      <table width="98%">
        <tr>
          <td align="right"><a href="javascript:showDnD()"
                               id="dNdActionLabel"><%=resource.getString("GML.DragNDropExpand")%></a>
            <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding: 0px" align="top"></div>
          </td>
        </tr>
      </table>
    </center>
    <%}%>

    <%// afficher les photos
      // -------------------
      // affichage des photos sous forme de vignettes	
      if (photos != null) {%>
    <br/>
    <%String vignette_url = null;
      int nbPhotos = photos.size();
      Board board = gef.getBoard();

      if (photos.size() > 0) {
        out.println(board.printBefore());
        // affichage de l'entete%>
        <form name="photoForm" action="EditSelectedPhoto">
        	<input type="hidden" name="AlbumId" value="<%=albumId%>"/> 
        	<input type="hidden" name="Index"/> 
        	<input type="hidden" name="SelectedIds"/> <input type="hidden" name="NotSelectedIds"/>
    <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
        <tr>
          <%int textColonne = 0;
            if (typeAff.equals("3")) {
              textColonne = 1;
            }%>
          <td colspan="<%=nbParLigne + textColonne%>" align="center">
            <table border="0" width="100%">
              <tr>
                <td align="center" width="100%" class=ArrayNavigation><%=pagination.printCounter()%>
                  <%if (photos.size() == 1) {
                      out.println(resource.getString("gallery.photo"));
                    } else {
                      out.println(resource.getString("gallery.photos"));
                    }%>
                </td>
                <td align="right" nowrap><select name="ChoiceSize"
                                                 onchange="javascript:choiceGoTo(this.selectedIndex);">
                    <option selected="selected"><%=resource.getString("gallery.choixTaille")%></option>
                    <option>-------------------------------</option>
                    <%String selected = "";
                      if ("66x50".equals(taille)) {
                        selected = "selected";
                      }%>
                    <option value="66x50" <%=selected%>><%="66x50"%></option>
                    <%selected = "";
                      if ("133x100".equals(taille)) {
                        selected = "selected";
                      }%>
                    <option value="133x100" <%=selected%>><%="133x100"%></option>
                    <%selected = "";
                      if ("266x150".equals(taille)) {
                        selected = "selected";
                      }%>
                    <option value="266x150" <%=selected%>><%="266x150"%></option>
                  </select> <select name="SortBy"
                                    onchange="javascript:sortGoTo(this.selectedIndex);">
                    <option selected><%=resource.getString("gallery.orderBy")%></option>
                    <option>-------------------------------</option>
                    <option value="CreationDateAsc"><%=resource.getString("gallery.dateCreatAsc")%></option>
                    <option value="CreationDateDesc"><%=resource.getString("gallery.dateCreatDesc")%></option>
                    <option value="Title"><%=resource.getString("GML.title")%></option>
                    <option value="Size"><%=resource.getString("gallery.taille")%></option>
                    <option value="Author"><%=resource.getString("GML.author")%></option>
                  </select></td>
              </tr>
            </table></td>
        </tr>
    </table>
    <%String photoColor = "";
      PhotoDetail photo;
      String idP;
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      Date today = calendar.getTime();
      Iterator it = affPhotos.iterator();
      while (it.hasNext()) {
        // affichage de la photo%>
    <table width="98%" border="0" cellspacing="5" cellpadding="0"
           align=center>
      <tr>
        <td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td>
      </tr>
      <tr>
        <%while (it.hasNext() && nbAffiche < nbParLigne) {
            photo = (PhotoDetail) it.next();
            if (photo != null) {
              idP = photo.getPhotoPK().getId();
              String nomRep = resource.getSetting("imagesSubDirectory") + idP;
              String name = photo.getImageName();
              String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
              if (StringUtil.isDefined(photo.getDescription())) {
                altTitle += " : " + EncodeHelper.javaStringToHtmlString(photo.getDescription());
              }
              if (name != null) {
                String type = name.substring(name.lastIndexOf(".") + 1, name.length());
                name = photo.getId() + extension;
                vignette_url = FileServerUtils.getUrl(spaceId, componentId, name, photo.
                        getImageMimeType(), nomRep);
                if (!ImageType.isPreviewable(name)) {
                  vignette_url = m_context + "/gallery/jsp/icons/notAvailable_" + resource.
                          getLanguage() + extension;
                }
              } else {
                vignette_url = m_context + "/gallery/jsp/icons/notAvailable_"
                        + resource.getLanguage() + extension;
              }
              photoColor = "fondPhoto";
              if (!photo.isVisible(today)) {
                photoColor = "fondPhotoNotVisible";
              }

              nbAffiche = nbAffiche + 1;
              if ("2".equals(typeAff)) {%>
        <td valign="top" width="<%=largeurCellule%>%">
          <table border="0" align="center" width="10" cellspacing="1"
                 cellpadding="0" class="<%=photoColor%>">
            <tr>
              <td align="center" colspan="2">
                <table cellspacing="1" cellpadding="3" border="0"
                       class="cadrePhoto">
                  <tr>
                    <td bgcolor="#FFFFFF"><a
                        href="PreviewPhoto?PhotoId=<%=idP%>"><img src="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"/>
                      </a></td>
                  </tr>
                </table></td>
            </tr>
            <tr>
              <%//traitement de la case e cocher
                String usedCheck = "";
                if (selectedIds != null && selectedIds.contains(idP)) {
                  usedCheck = "checked";
                }%>
              <td align="center" width="10"><input type="checkbox"
                                                   name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>/>
              </td>
              <td class="txtlibform"><%=photo.getName()%></td>
            </tr>
            <%if (photo.getDescription() != null) {%>
            <tr>
              <td>&nbsp;</td>
              <td><%=photo.getDescription()%></td>
            </tr>
            <%}%>
          </table></td>
          <%}
            if ("1".equals(typeAff)) {%>
        <td valign="bottom" width="<%=largeurCellule%>%">
          <table border="0" width="10" cellspacing="1" cellpadding="0"
                 align="center" class="<%=photoColor%>">
            <tr>
              <td align="center">
                <table cellspacing="1" cellpadding="3" border="0"
                       class="cadrePhoto">
                  <tr>
                    <td bgcolor="#FFFFFF"><a
                        href="PreviewPhoto?PhotoId=<%=idP%>"><img src="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"/>
                      </a></td>
                  </tr>
                </table></td>
            </tr>
            <%//traitement de la case e cocher
              String usedCheck = "";
              if (selectedIds != null && selectedIds.contains(idP)) {
                usedCheck = "checked";
              }%>
            <tr>
              <td align="center"><input type="checkbox" name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>/>
              </td>
            </tr>
          </table></td>
          <%}
            // affichage du texte e cote de la photo pour le cas de l'affichage en liste
            if ("3".equals(typeAff)) {
              // on affiche les photos en colonne avec les metaData e droite%>
        <td valign="middle" align="center">
          <table border="0" width="10" cellspacing="1" cellpadding="0"
                 align="center" class="<%=photoColor%>">
            <tr>
              <td align="center">
                <table cellspacing="1" cellpadding="5" border="0"
                       class="cadrePhoto">
                  <tr>
                    <td bgcolor="#FFFFFF"><a
                        href="PreviewPhoto?PhotoId=<%=idP%>"><img src="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"/>
                      </a></td>
                  </tr>
                </table></td>
            </tr>
          </table></td>
        <td valign="top" width="100%">
          <table border="0" width="100%">
            <tr>
              <td class="txtlibform" nowrap><%=resource.getString("GML.title")%> :</td>
              <td><%=photo.getName()%></td>
            </tr>
            <%if (photo.getDescription() != null) {%>
            <tr>
              <td class="txtlibform" nowrap><%=resource.getString("GML.description")%> :</td>
              <td><%=photo.getDescription()%></td>
            </tr>
            <%}
              if (photo.getAuthor() != null) {%>
            <tr>
              <td class="txtlibform" nowrap><%=resource.getString("GML.author")%> :</td>
              <td><%=photo.getAuthor()%></td>
            </tr>
            <%}
              if (viewMetadata) {
                final Collection<String> metaDataKeys = photo.getMetaDataProperties();
                if (metaDataKeys != null && !metaDataKeys.isEmpty()) {
                  MetaData metaData;
                  for (final String property : metaDataKeys) {
                    // traitement de la metaData
                    metaData = photo.getMetaData(property);
                    String mdLabel = metaData.getLabel();
                    String mdValue = metaData.getValue();
                    if (metaData.isDate()) {
                      mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
                    }
                    // affichage%>
            <tr>
              <td class="txtlibform" nowrap><%=mdLabel%> :</td>
              <td><%=mdValue%></td>
            </tr>
            <%}
                }
              }
              if (photo.getKeyWord() != null) {%>
            <tr>
              <td class="txtlibform" nowrap><%=resource.getString("gallery.keyWord")%>
                :</td>
              <td>
                <%String keyWord = photo.getKeyWord();
                  // decouper la zone keyWord en mots
                  StringTokenizer st = new StringTokenizer(keyWord);
                  // traitement des mots cles
                  while (st.hasMoreTokens()) {
                    String searchKeyWord = (String) st.nextToken();%> <a href="SearchKeyWord?SearchKeyWord=<%=searchKeyWord%>">	<%=searchKeyWord%> </a> <%}
                        out.println("</td></tr>");
                      }
                      //traitement de la case e cocher
                      String usedCheck = "";
                      if (selectedIds != null && selectedIds.contains(idP)) {
                        usedCheck = "checked";
                      }%>

                <tr>
                  <td align="left" valign="top" colspan="2"><input type="checkbox" name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>/></td>
                </tr>
                </table></td>
                <%}
                    }
                  }

                  // on prepare pour la ligne suivante
                  nbAffiche = 0;%>
            </tr>
            <%}

              if (nbPhotos > nbPhotosPerPage) {%>
            <tr>
              <td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td>
            </tr>
            <tr class=intfdcolor4>
              <td colspan="<%=nbParLigne + textColonne%>"><%=pagination.printIndex("doPagination")%>
              </td>
            </tr>
            <%}%>
          </table>
          </form>
          <%out.println(board.printAfter());
            }
          %>

          <%
            String inlineMessage = null;
            if (!somePhotos && ("user".equals(profile) || "privilegedUser".equals(profile))) {
              inlineMessage = resource.getString("gallery.album.emptyForUser");
            } else if (!somePhotos && "writer".equals(profile)) {
              String[] params = new String[2];
              params[0] = resource.getString("gallery.ajoutPhoto");
              params[1] = fctAddPhoto;
              inlineMessage = resource.getStringWithParams("gallery.album.emptyForWriter", params);
            } else if (!somePhotos && !someAlbums && ("publisher".equals(profile) || "admin".equals(
                    profile))) {
              // profile publisher et admin
              String[] params = new String[4];
              params[0] = resource.getString("gallery.ajoutAlbum");
              params[1] = "javaScript:addAlbum()";
              params[2] = resource.getString("gallery.ajoutPhoto");
              params[3] = fctAddPhoto;
              inlineMessage = resource.getStringWithParams("gallery.album.emptyForAdmin", params);
            }
          %>
          <% if (StringUtil.isDefined(inlineMessage)) {%>
          <div class="inlineMessage"><%=inlineMessage%></div>
          <% }%>
          <%
            }
          %>
          <%@include file="albumManager.jsp" %>
          <%

            out.println(frame.printAfter());
            out.println(window.printAfter());%>
          <form name="albumForm" action="" method="post">
            <input type="hidden" name="Id"/> 
            <input type="hidden" name="Name"/> 
            <input type="hidden" name="Description"/>
          </form>
          <form name="ChoiceSelectForm" action="ChoiceSize" method="post">
            <input type="hidden" name="Choice"/>
          </form>
          <form name="OrderBySelectForm" action="SortBy" method="post">
            <input type="hidden" name="Tri"/>
          </form>
          <form name="favorite" action="" method="post">
            <input type="hidden" name="Id"/>
          </form>
</body>
</html>
