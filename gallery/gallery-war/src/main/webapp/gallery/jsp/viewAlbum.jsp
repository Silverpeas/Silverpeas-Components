<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	AlbumDetail 	currentAlbum 		= (AlbumDetail) request.getAttribute("CurrentAlbum");
	String 			userId 				= (String) request.getAttribute("UserId");
	String 			profile 			= (String) request.getAttribute("Profile");
	Collection 		path 				= (Collection) request.getAttribute("Path");
	int				firstPhotoIndex		= ((Integer) request.getAttribute("FirstPhotoIndex")).intValue();
	int				nbPhotosPerPage		= ((Integer) request.getAttribute("NbPhotosPerPage")).intValue();
	String 			taille	 			= (String) request.getAttribute("Taille");
	Boolean			dragAndDropEnable 	= (Boolean) request.getAttribute("DragAndDropEnable");
	Boolean 		isViewMetadata		= (Boolean) request.getAttribute("IsViewMetadata");
	Boolean 		isViewList			= (Boolean) request.getAttribute("IsViewList");
	Collection		selectedIds			= (Collection) request.getAttribute("SelectedIds");
	boolean			isPdcUsed			= ((Boolean) request.getAttribute("IsUsePdc")).booleanValue();
	boolean 		isBasket	 		= ((Boolean) request.getAttribute("IsBasket")).booleanValue();

	//For Drag And Drop
	String sRequestURL 		= HttpUtils.getRequestURL(request).toString();
	String m_sAbsolute 		= sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
	ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
	String pathInstallerJre = generalSettings.getString("pathInstallerJre");
	if (pathInstallerJre != null && !pathInstallerJre.startsWith("http"))
		pathInstallerJre = m_sAbsolute+pathInstallerJre;
	
	String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);
	
	// déclaration des variables :
	int 	nbAffiche 		= 0;
	String 	albumId 		= "";
	List 	photos 			= null;
	int 	id 				= 0;
	int 	nbParLigne		= 1;
	int		largeurCellule	= 0;
	String 	extension		= "";
	String 	albumName		= "";
	String 	albumDescription = "";
	String 	albumUrl		= "";
	boolean viewMetadata	= isViewMetadata.booleanValue();
	boolean viewList		= isViewList.booleanValue();
	String 	typeAff			= "1";
	
	if (currentAlbum != null)
	{
		albumId = new Integer(currentAlbum.getId()).toString();
		photos = (List) currentAlbum.getPhotos();
		albumDescription = currentAlbum.getDescription();
		albumUrl = currentAlbum.getLink();
	}
		
	// initialisation de la pagination
	Pagination 	pagination 	= gef.getPagination(photos.size(), nbPhotosPerPage, firstPhotoIndex);
	List 		affPhotos 	= photos.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());

	// création du chemin :
	String 		chemin 		= "";
	String 		namePath	= "";
	boolean 	suivant 	= false;
	Iterator 	itPath 		= (Iterator) path.iterator();
	
	while (itPath.hasNext()) 
	{
		NodeDetail unAlbum = (NodeDetail) itPath.next();
		if (unAlbum.getId() != 0)
		{
			if (suivant) 
			{
				chemin = " > " + chemin;
				namePath = " > " + namePath;
			}
			chemin = "<a href=\"ViewAlbum?Id="+ unAlbum.getNodePK().getId() + "\">" + Encode.javaStringToHtmlString(unAlbum.getName())+"</a>" + chemin;
			namePath = unAlbum.getName() + namePath;
			suivant = itPath.hasNext();
		}
	}
	albumName = spaceLabel + " > " + componentLabel + " > " + namePath; 
			
	// calcul du nombre de photo par ligne en fonction de la taille
	if (taille.equals("66x50"))
	{
		nbParLigne = 8;
		extension = "_66x50.jpg";
	}
	else if (taille.equals("133x100"))
	{
		nbParLigne = 5;
		extension = "_133x100.jpg";
		if (viewList)
			typeAff = "2";
	}
	else if (taille.equals("266x150"))
	{
		nbParLigne = 3;
		extension = "_266x150.jpg";
		if (viewList)
		{
			typeAff = "3";
			nbParLigne = 1;
		}
	}
	largeurCellule = 100/nbParLigne;
	
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
	var albumWindow = window;

	function addAlbum() {
	    windowName = "albumWindow";
		larg = "570";
		haut = "250";
	    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
	    if (!albumWindow.closed && albumWindow.name== "albumWindow")
	        albumWindow.close();
	    albumWindow = SP_openWindow("NewAlbum", windowName, larg, haut, windowParams);
	}
	
	function editAlbum(id) {
	    url = "EditAlbum?Id="+id;
	    windowName = "albumWindow";
		larg = "550";
		haut = "250";
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	    if (!albumWindow.closed && albumWindow.name== "albumWindow")
	        albumWindow.close();
	    albumWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
	
	function addFavorite(m_sAbsolute,m_context,name,description,url) 
	{
  		urlWindow = m_sAbsolute + m_context + "/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
	    windowName = "albumWindow";
		larg = "550";
		haut = "250";
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	    if (!albumWindow.closed && albumWindow.name== "albumWindow")
	        albumWindow.close();
	    albumWindow = SP_openWindow(urlWindow, windowName, larg, haut, windowParams);
	}

	function deleteConfirm(id,nom) 
	{
		// confirmation de suppression de l'album
		if(window.confirm("<%=resource.getString("gallery.confirmDeleteAlbum")%> '" + nom + "' ?"))
		{
  			document.albumForm.action = "DeleteAlbum";
  			document.albumForm.Id.value = id;
  			document.albumForm.submit();
		}
	}
	
	function choiceGoTo(selectedIndex) 
	{
		// envoi du choix de la taille des vignettes
		if (selectedIndex != 0 && selectedIndex != 1) 
		{
			document.ChoiceSelectForm.Choice.value = document.photoForm.ChoiceSize[selectedIndex].value;
			document.ChoiceSelectForm.submit();
		}
	}
	
	function sendData() 
	{
		// envoi des photos sélectionnées pour la modif par lot
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		
		document.photoForm.submit();
	}
	
	function sendToBasket() 
	{
		// envoi des photos sélectionnées dans le panier
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		document.photoForm.action				= "BasketAddPhotos";
		document.photoForm.submit();
	}
	
	function sendDataDelete() 
	{
		//confirmation de suppression de l'album
		if(window.confirm("<%=resource.getString("gallery.confirmDeletePhotos")%> "))
		{
			// envoi des photos sélectionnées pour la modif par lot
			document.photoForm.SelectedIds.value 	= getObjects(true);
			document.photoForm.NotSelectedIds.value = getObjects(false);
			document.photoForm.action				= "DeleteSelectedPhoto";
			document.photoForm.submit();
		}
	}
	
	function sendDataCategorize() 
	{
		var selectedIds = getObjects(true);
		var notSelectedIds = getObjects(false);
		
		urlWindow = "CategorizeSelectedPhoto?SelectedIds="+selectedIds+"&NotSelectedIds="+notSelectedIds;
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	    if (!albumWindow.closed && albumWindow.name== "albumWindow")
	        albumWindow.close();
	    albumWindow = SP_openWindow(urlWindow, "albumWindow", "550", "250", windowParams);
	}
	
	function getObjects(selected)
	{
		var  items = "";
		try
		{
			var boxItems = document.photoForm.SelectPhoto;
			if (boxItems != null){
				// au moins une checkbox exist
				var nbBox = boxItems.length;
				if ( (nbBox == null) && (boxItems.checked == selected) ){
					// il n'y a qu'une checkbox non selectionnée
					items += boxItems.value+",";
				} else{
					// search not checked boxes 
					for (i=0;i<boxItems.length ;i++ ){
						if (boxItems[i].checked == selected){
							items += boxItems[i].value+",";
						}
					}
				}
			}
		}
		catch (e)
		{
			//Checkboxes are not displayed 
		}
		return items;
	}
	
	function doPagination(index)
	{
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		document.photoForm.Index.value 			= index;
		document.photoForm.action				= "Pagination";
		document.photoForm.submit();
	}
	
	function sortGoTo(selectedIndex) 
	{
		// envoi du choix du tri
		if (selectedIndex != 0 && selectedIndex != 1) 
		{
			document.OrderBySelectForm.Tri.value = document.photoForm.SortBy[selectedIndex].value;
			document.OrderBySelectForm.submit();
		}
	}
	
function uploadCompleted(s)
	{
		//window.alert("In uploadCompleted !"+s);
		location.href="<%=m_context+URLManager.getURL(null, componentId)%>ViewAlbum?Id=<%=currentAlbum.getNodePK().getId()%>";
		//return true;
	}

	function showDnD()
	{
		var url = "<%=httpServerBase+m_context%>/RgalleryDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&AlbumId=<%=currentAlbum.getNodePK().getId()%>";
		var message = "<%=httpServerBase%>/weblib/dragAnddrop/Gallery_<%=resource.getLanguage()%>.html";
		var propFile = "<%=httpServerBase%>/weblib/dragAnddrop/raduploadMulti.properties";
		
		<%
		ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
		String maximumFileSize 		= uploadSettings.getString("MaximumFileSize", "10000000");
		String maxFileSizeForApplet = maximumFileSize.substring(0, maximumFileSize.length()-3);
		%>
		showHideDragDrop(url,message,propFile,'<%=maxFileSizeForApplet%>','<%=pathInstallerJre%>','<%=resource.getString("GML.DragNDropExpand")%>','<%=resource.getString("GML.DragNDropCollapse")%>');
	}
	
	
	function clipboardPaste() {
    	top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=RGallery&SpaceFrom=<%=spaceId%>&ComponentFrom=<%=componentId%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("GoToCurrentAlbum"))%>&TargetFrame=MyMain&message=REFRESH');
	}

	function clipboardCopy() {
	    top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>copy?Object=Node&Id=<%=currentAlbum.getNodePK().getId()%>';
	}
	
	function clipboardCut() {
	    top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>cut?Object=Node&Id=<%=currentAlbum.getNodePK().getId()%>';
	}
	
	function CopySelectedPhoto()
	{
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		document.photoForm.action				= "CopySelectedPhoto";
		document.photoForm.submit();
	}
	
	function CutSelectedPhoto()
	{
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		document.photoForm.action				= "CutSelectedPhoto";
		document.photoForm.submit();
	}
	
</script>
<script src="<%=m_context%>/gallery/jsp/javaScript/dragAndDrop.js" type="text/javascript"></script>
</head>
<body>
<%
	// création de la barre de navigation
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(chemin);
	
	if ( "admin".equals(profile) || "publisher".equals(profile))
	{
		// possibilité d'ajouter des albums pour les "admin" et "publisher"
		operationPane.addOperation(resource.getIcon("gallery.addAlbum"),resource.getString("gallery.ajoutAlbum"),"javaScript:addAlbum()");
		operationPane.addLine();
		
		if ("admin".equals(profile))
		{
	   		operationPane.addOperation(resource.getIcon("gallery.copy"), resource.getString("gallery.copyAlbum"), "javascript:onClick=clipboardCopy()");
	   		operationPane.addOperation(resource.getIcon("gallery.cut"), resource.getString("gallery.cutAlbum"), "javascript:onClick=clipboardCut()");
	       	operationPane.addLine();
		}
		
		// possibilité de modifier ou supprimer les photos par lot
		operationPane.addOperation(resource.getIcon("gallery.updateSelectedPhoto"),resource.getString("gallery.updateSelectedPhoto"),"javascript:onClick=sendData();");
		operationPane.addOperation(resource.getIcon("gallery.deleteSelectedPhoto"),resource.getString("gallery.deleteSelectedPhoto"),"javascript:onClick=sendDataDelete();");
		if (isPdcUsed)
		{
			// si on a le classement Pdc : possibilité de classer par lot
			operationPane.addOperation(resource.getIcon("gallery.categorizeSelectedPhoto"),resource.getString("gallery.categorizeSelectedPhoto"),"javascript:onClick=sendDataCategorize();");
		}
	}
	
	// bouton pour tout selectionner ou tout déselectionner
	operationPane.addOperation(resource.getIcon("gallery.allSelect"),resource.getString("gallery.allSelect"),"AllSelected");
	
	if ("admin".equals(profile))
	{
   		operationPane.addOperation(resource.getIcon("gallery.copy"), resource.getString("gallery.copySelectedPhoto"), "javascript:onClick=CopySelectedPhoto()");
   		operationPane.addOperation(resource.getIcon("gallery.cut"), resource.getString("gallery.cutSelectedPhoto"), "javascript:onClick=CutSelectedPhoto()");
       	operationPane.addOperation(resource.getIcon("gallery.paste"), resource.getString("GML.paste"), "javascript:onClick=clipboardPaste()");
       	operationPane.addLine();
	}
	if ( "admin".equals(profile) || "publisher".equals(profile) || "writer".equals(profile))
	{
		// possibilité d'ajouter des photos pour les "admin", "publisher" et "writer"
		operationPane.addOperation(resource.getIcon("gallery.addPhoto"),resource.getString("gallery.ajoutPhoto"),"AddPhoto");
	}
	
	if ("user".equals(profile) && isBasket)
	{
		operationPane.addLine();
		// ajouter les photos sélectionnées au panier
		operationPane.addOperation(resource.getIcon("gallery.addToBasketSelectedPhoto"),resource.getString("gallery.addToBasketSelectedPhoto"),"javascript:onClick=sendToBasket();");
		// voir le panier
		operationPane.addOperation(resource.getIcon("gallery.viewBasket"),resource.getString("gallery.viewBasket"), "BasketView");
	}
	
	if (photos.size() > 1)
	{
		// diaporama
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("gallery.startDiaporama"), resource.getString("gallery.diaporama"), "StartDiaporama?Debut="+"ok");
	}
	
	// favoris
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("gallery.addFavorite"),resource.getString("gallery.addFavorite"),"javaScript:addFavorite('"+m_sAbsolute+"','"+m_context+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(albumName))+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(albumDescription))+"','"+albumUrl+"')");
		
	// derniers résultat de la recherche
	operationPane.addLine();
    operationPane.addOperation(resource.getIcon("gallery.lastResult"), resource.getString("gallery.lastResult"), "LastResult");

	out.println(window.printBefore());
    out.println(frame.printBefore());
 	
	
	// afficher les sous albums
	// ------------------------
	// création et remplissage d'un ArrayPane avec les sous albums de l'album courant (s'il y en a)
	if (currentAlbum.getChildrenDetails() != null)
	{
		if ( "user".equals(profile) || "writer".equals(profile) )
		{
			NavigationList navList = gef.getNavigationList();
      		navList.setTitle(currentAlbum.getName());
      		boolean ok = false;
			Iterator it = (Iterator) currentAlbum.getChildrenDetails().iterator();
			if (it.hasNext())
				ok = true;
			
			while (it.hasNext()) 
			{
				NodeDetail unAlbum = (NodeDetail) it.next();
				id = unAlbum.getId();
				String nom = unAlbum.getName();
				String lien = null;
				navList.addItem(nom,"ViewAlbum?Id="+id,-1,unAlbum.getDescription(), lien);
			}
			if (ok)
				out.println(navList.print());
		}
		else
		{
			ArrayPane arrayPane = gef.getArrayPane("albumList", "GoToCurrentAlbum", request, session);
			ArrayColumn columnIcon = arrayPane.addArrayColumn("&nbsp;");
        	columnIcon.setSortable(false);
			boolean ok = false;
			Iterator it = (Iterator) currentAlbum.getChildrenDetails().iterator();
			if (it.hasNext())
			{
				arrayPane.addArrayColumn(resource.getString("gallery.albums"));
				arrayPane.addArrayColumn(resource.getString("GML.description"));
				ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("gallery.operation"));
				columnOp.setSortable(false);
				ok = true;
			}
			
			while (it.hasNext()) 
			{
				ArrayLine ligne = arrayPane.addArrayLine();
				
				IconPane icon = gef.getIconPane();
				Icon albumIcon = icon.addIcon();
       			albumIcon.setProperties(resource.getIcon("gallery.gallerySmall"), "");
       			icon.setSpacing("30px");
       			ligne.addArrayCellIconPane(icon);
       		
				NodeDetail unAlbum = (NodeDetail) it.next();
				id = unAlbum.getId();
				String nom = unAlbum.getName();
				String link = "";
				if (unAlbum.getPermalink() != null)
				{
					link = "&nbsp;<a href=\""+unAlbum.getPermalink()+"\"><img src=\""+resource.getIcon("gallery.link")+"\" border=\"0\" align=\"bottom\" alt=\""+resource.getString("gallery.CopyAlbumLink")+"\" title=\""+resource.getString("gallery.CopyAlbumLink")+"\"></a>";
				}
				//ligne.addArrayCellLink(unAlbum.getName()+ link,"ViewAlbum?Id="+id);
				
				ArrayCellText arrayCellText0 = ligne.addArrayCellText("<a href=\"ViewAlbum?Id="+id+"\">"+unAlbum.getName()+"</a>"+link);
	            arrayCellText0.setCompareOn(unAlbum.getName());
				
				ligne.addArrayCellText(unAlbum.getDescription());
				if ( "admin".equals(profile) || ("publisher".equals(profile) && unAlbum.getCreatorId().equals(userId)) )
				{
					// création de la colonne des icônes
					IconPane iconPane = gef.getIconPane();
					// icône "modifier"
					Icon updateIcon = iconPane.addIcon();
	        		updateIcon.setProperties(resource.getIcon("gallery.updateAlbum"), resource.getString("gallery.updateAlbum"),"javaScript:editAlbum('"+id+"')");
	        		// icône "supprimer"
	        		Icon deleteIcon = iconPane.addIcon();
	        		deleteIcon.setProperties(resource.getIcon("gallery.deleteAlbum"), resource.getString("gallery.deleteAlbum"),"javaScript:deleteConfirm('"+id+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(nom))+"')");
	        		iconPane.setSpacing("30px");
	        		ligne.addArrayCellIconPane(iconPane);
	        	}
			}
			if (ok)
				out.println(arrayPane.print());
		}
	}
	%>
	
	<% if (!"user".equals(profile) && dragAndDropEnable != null && dragAndDropEnable.booleanValue()) { %>
		<!-- Affichage de la zone de drag And Drop -->
		<center>
		<table width="98%">
			<tr>
				<td align="right">
				<a href="javascript:showDnD()" id="dNdActionLabel"><%=resource.getString("GML.DragNDropExpand")%></a>
				<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px" align="top"></div>
				</td>
			</tr>
		</table>
		</center>
	<% } %>
	
	<%
	// afficher les photos
	// -------------------
	// affichage des photos sous forme de vignettes	
	if (photos != null)
	{
		%>
			<br>
		<%
		String	vignette_url = null;
		int		nbPhotos	 = photos.size();
		Board	board		 = gef.getBoard();
		
		if (photos.size()>0) 
		{
			out.println(board.printBefore());
			// affichage de l'entête
			%>
			<table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
				<form name="photoForm" action="EditSelectedPhoto">
				<input type="hidden" name="AlbumId" value="<%=albumId%>">
				<input type="hidden" name="Index">
				<input type="hidden" name="SelectedIds">
				<input type="hidden" name="NotSelectedIds">
				<tr>
					<% 
					int textColonne = 0;
					if (typeAff.equals("3"))
						textColonne = 1;
					%>						
					<td colspan="<%=nbParLigne + textColonne%>" align="center">
						<table border="0" width="100%">
							<tr>
								<td align="center" width="100%" class=ArrayNavigation>
									<%=pagination.printCounter()%>
									<%
										if (photos.size() == 1)
											out.println(resource.getString("gallery.photo"));
										else
											out.println(resource.getString("gallery.photos"));
									%>
								</td>
								<td align="right" nowrap>
									<select name="ChoiceSize" onChange="javascript:choiceGoTo(this.selectedIndex);">
										<option selected><%=resource.getString("gallery.choixTaille")%></option>
										<option>-------------------------------</option>
										<%
											String selected = "";
											if (taille.equals("66x50"))
												selected = "selected";
										%>
										<option value="66x50" <%=selected%>><%="66x50"%></option>
										<%
											selected = "";
											if (taille.equals("133x100"))
												selected = "selected";
										%>
										<option value="133x100" <%=selected%>><%="133x100"%></option>
										<%
											selected = "";
											if (taille.equals("266x150"))
												selected = "selected";
										%>
										<option value="266x150" <%=selected%>><%="266x150"%></option>
									</select>
									<select name="SortBy" onChange="javascript:sortGoTo(this.selectedIndex);">
										<option selected><%=resource.getString("gallery.orderBy")%></option>
										<option>-------------------------------</option>
										<option value="CreationDateAsc"><%=resource.getString("gallery.dateCreatAsc")%></option>
										<option value="CreationDateDesc"><%=resource.getString("gallery.dateCreatDesc")%></option>
										<option value="Title"><%=resource.getString("GML.title")%></option>
										<option value="Size"><%=resource.getString("gallery.taille")%></option>
										<option value="Author"><%=resource.getString("GML.author")%></option>
									</select>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<%
			
			String		photoColor		= "";
			String 		altTitle 		= "";
			PhotoDetail photo;
			String idP;
			Calendar	calendar		= Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date today = calendar.getTime();
			Iterator it = (Iterator) affPhotos.iterator();
			while (it.hasNext())
			{
				// affichage de la photo
				%>
				<table width="98%" border="0" cellspacing="5" cellpadding="0" align=center>
				<tr><td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td></tr>
				<tr>
				<%
				
				while (it.hasNext() && nbAffiche < nbParLigne)
				{
					photo 		= (PhotoDetail) it.next();
					altTitle 	= "";
					if (photo != null)
					{
						idP = photo.getPhotoPK().getId();
						String nomRep = resource.getSetting("imagesSubDirectory") + idP;
						String name = photo.getImageName();
						if (name != null)
						{
							String type = name.substring(name.lastIndexOf(".") + 1, name.length());
							name = photo.getId() + extension;
							vignette_url = FileServer.getUrl(spaceId, componentId, name, photo.getImageMimeType(), nomRep);
							if ("bmp".equalsIgnoreCase(type))
								vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+extension;
															
							altTitle = Encode.javaStringToHtmlString(photo.getTitle());
							if (photo.getDescription() != null && photo.getDescription().length() > 0)
								altTitle += " : "+Encode.javaStringToHtmlString(photo.getDescription());
						}
						else
						{
							vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+extension;
						}
						photoColor = "fondPhoto";
						if (!photo.isVisible(today))
							photoColor = "fondPhotoNotVisible";
						
						nbAffiche = nbAffiche + 1;
						
						// on affiche encore sur la même ligne
						%>
						
						<% 
							if (typeAff.equals("2"))
							{ %>
								<td valign="top" width="<%=largeurCellule%>%">	
								<table border="0" align="center" width="10" cellspacing="1" cellpadding="0" class="<%=photoColor%>">
									<tr><td align="center" colspan="2">
										<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
											<a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
										</td></tr></table>
									</td></tr>
									<tr>
									<%
									//traitement de la case à cocher
									String usedCheck = "";
									if (selectedIds != null && selectedIds.contains(idP))
										usedCheck = "checked";
									%>
									<td align="center" width="10"><input type="checkbox" name="SelectPhoto" value="<%=idP%>" <%=usedCheck%> ></td>
									<td class="txtlibform"><%=photo.getName()%></td>
									</tr>
									<% if (photo.getDescription() != null) 
									{ %> 
										<tr>
											<td>&nbsp;</td>
											<td><%=photo.getDescription()%></td>
										</tr>
									<% } %>									
								</table>
								</td>
							<% }
							if (typeAff.equals("1"))
							{ %>
								<td valign="bottom" width="<%=largeurCellule%>%">
								<table border="0" width="10" cellspacing="1" cellpadding="0" align="center" class="<%=photoColor%>">
									<tr><td align="center">
										<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
										<a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
										</td></tr></table>
									</td></tr>
									<%
									//traitement de la case à cocher
									String usedCheck = "";
									if (selectedIds != null && selectedIds.contains(idP))
										usedCheck = "checked";
									%>
									<tr><td align="center"><input type="checkbox" name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>></td></tr>
								</table>
								</td>
							<% } 
							// affichage du texte à coté de la photo pour le cas de l'affichage en liste
							if (typeAff.equals("3"))
							{	
								// on affiche les photos en colonne avec les métaData à droite
								%>
								<td valign="middle" align="center">
									<table border="0" width="10" cellspacing="1" cellpadding="0" align="center" class="<%=photoColor%>"><tr><td align="center">
										<table cellspacing="1" cellpadding="5" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
											<a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
										</td></tr></table>
									</td></tr></table>	
								</td>
								<td valign="top" width="100%">
									<table border="0" width="100%">
										<tr>
											<td class="txtlibform" nowrap><%=resource.getString("GML.title")%> :</td>
											<td><%=photo.getName()%></td>
										</tr>
										<% 
										if (photo.getDescription() != null) 
										{ %>
											<tr>
												<td class="txtlibform" nowrap><%=resource.getString("GML.description")%> :</td>
												<td><%=photo.getDescription()%></td>
											</tr>
										<% } 
										if (photo.getAuthor() != null) 
										{ %>
											<tr>
												<td class="txtlibform" nowrap><%=resource.getString("GML.author")%> :</td>
												<td><%=photo.getAuthor()%></td>
											</tr>
										<% } 
										Collection	metaDataKeys = photo.getMetaDataProperties();
										if (viewMetadata)
										{	
											if (metaDataKeys != null) 
											{	
												Iterator itMeta = (Iterator) metaDataKeys.iterator();
												while (itMeta.hasNext())
												{
													// traitement de la metaData
													String property = (String) itMeta.next();
													
													MetaData metaData = photo.getMetaData(property);
													String mdLabel = metaData.getLabel();
													String mdValue = metaData.getValue();
													if (metaData.isDate())
														mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
													// affichage
													%>
														<tr>
															<td class="txtlibform" nowrap><%=mdLabel%> :</td>
															<td><%=mdValue%></td>
														</tr>
													<%
												}
											}
										}
										if (photo.getKeyWord() != null) 
										{ 
											%>
											<tr>
												<td class="txtlibform" nowrap><%=resource.getString("gallery.keyWord")%> :</td>
												<td>
											<%
											String keyWord = photo.getKeyWord();   
											// découper la zone keyWord en mots
											StringTokenizer st = new StringTokenizer(keyWord);
											// traitement des mots clés
											while (st.hasMoreTokens()) 
											{
												String searchKeyWord = (String) st.nextToken();
												%>
												<a href="SearchKeyWord?SearchKeyWord=<%=searchKeyWord%>"> <%=searchKeyWord%> </a>
											 <% } 
											out.println("</td></tr>"); 
										}
										//traitement de la case à cocher
										String usedCheck = "";
										if (selectedIds != null && selectedIds.contains(idP))
											usedCheck = "checked";
										%>
										<tr><td align="left" valign="top" colspan="2"><input type="checkbox" name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>></td></tr>
									</table>									
								</td>
							<%
							}
						}
					}
				
				// on prépare pour la ligne suivante
				nbAffiche = 0;
				%>
				</tr>
				<%
			}

			if (nbPhotos > nbPhotosPerPage)
			{
				%>
				<tr><td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td></tr>
				<tr class=intfdcolor4>
					<td colspan="<%=nbParLigne + textColonne%>">
						<%=pagination.printIndex("doPagination")%>
					</td>
				</tr>
				<%
			}
			%>
			</form>
			</table>
			<%
			out.println(board.printAfter());
		}
  	}    
  	
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="albumForm" action="" Method="POST">
	<input type="hidden" name="Id">
	<input type="hidden" name="Name">
	<input type="hidden" name="Description">
</form>
<form name="ChoiceSelectForm" action="ChoiceSize" Method="POST" >
  	<input type="hidden" name="Choice">
</form>
<form name="OrderBySelectForm" action="SortBy" Method="POST" >
  	<input type="hidden" name="Tri">
</form>
<form name="favorite" action="" Method="POST">
	<input type="hidden" name="Id">
</form>

</body>
</html>