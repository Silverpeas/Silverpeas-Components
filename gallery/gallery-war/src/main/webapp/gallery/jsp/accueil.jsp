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
AlbumDetail root 			= (AlbumDetail) request.getAttribute("root");
String 		profile 		= (String) request.getAttribute("Profile");
String 		userId 			= (String) request.getAttribute("UserId");
List 		photos			= (List) request.getAttribute("Photos");
boolean		isPdcUsed		= ((Boolean) request.getAttribute("IsUsePdc")).booleanValue();
boolean 	isPrivateSearch	= ((Boolean) request.getAttribute("IsPrivateSearch")).booleanValue();
boolean 	isBasket	 	= ((Boolean) request.getAttribute("IsBasket")).booleanValue();
boolean 	isOrder		 	= ((Boolean) request.getAttribute("IsOrder")).booleanValue();
boolean 	isGuest		 	= ((Boolean) request.getAttribute("IsGuest")).booleanValue();

// paramètrage pour l'affichage des dernières photos téléchargées
int nbAffiche 	= 0;
int nbParLigne 	= 5;
int nbTotal 	= 15;
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="javascript">
var albumWindow = window;
var askWindow = window;

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

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
	
function deleteConfirm(id,nom) 
{
	if(window.confirm("<%=resource.getString("gallery.confirmDeleteAlbum")%> '" + nom + "' ?"))
	{
		document.albumForm.action = "DeleteAlbum";
		document.albumForm.Id.value = id;
		document.albumForm.submit();
	}
}

function askPhoto()
{
	windowName = "askWindow";
	larg = "570";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!askWindow.closed && askWindow.name== "askWindow")
    	askWindow.close();
    askWindow = SP_openWindow("AskPhoto", windowName, larg, haut, windowParams);
}

function sendData() 
{
    var query = stripInitialWhitespace(document.searchForm.SearchKeyWord.value);
	if (!isWhitespace(query) && query != "*") {
    	//displayStaticMessage();
		setTimeout("document.searchForm.submit();", 500);
    }
}

function clipboardPaste() {    	
	top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=RGallery&SpaceFrom=<%=spaceId%>&ComponentFrom=<%=componentId%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("GoToCurrentAlbum"))%>&TargetFrame=MyMain&message=REFRESH');
}

</script>
</head>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	
	// affichage de la gestion du plan de classement (seulement pour les administrateurs)
	if ( "admin".equals(profile))
	{
		if (isPdcUsed) 
		{
			operationPane.addOperation(resource.getIcon("gallery.pdcUtilizationSrc"), resource.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
			operationPane.addLine();
		}
	}
	if ( "admin".equals(profile) || "publisher".equals(profile))
	{
		operationPane.addOperation(resource.getIcon("gallery.addAlbum"),resource.getString("gallery.ajoutAlbum"), "javaScript:addAlbum()");
		operationPane.addLine();
		
		//visualisation des photos non visibles par les lecteurs
		operationPane.addOperation(resource.getIcon("gallery.viewNotVisible"),resource.getString("gallery.viewNotVisible"),"ViewNotVisible");
		operationPane.addLine();
	}

	if ("user".equals(profile) && isBasket)
	{
		// voir le panier
		operationPane.addOperation(resource.getIcon("gallery.viewBasket"),resource.getString("gallery.viewBasket"), "BasketView");
	}
	if (isOrder)
	{
		if ("admin".equals(profile) || "user".equals(profile))
		{
			// voir la liste des demandes
			operationPane.addOperation(resource.getIcon("gallery.viewOrderList"),resource.getString("gallery.viewOrderList"), "OrderViewList");
		}
	}
	
	if (!"admin".equals(profile) && !isGuest)
	{
		// demande de photo auprès du gestionnaire
		operationPane.addOperation(resource.getIcon("gallery.askPhoto"),resource.getString("gallery.askPhoto"), "javaScript:askPhoto()");
		operationPane.addLine();
	}

	
	if ("admin".equals(profile))
	{
       	operationPane.addOperation(resource.getIcon("gallery.paste"), resource.getString("GML.paste"), "javascript:onClick=clipboardPaste()");
       	operationPane.addLine();
	}
	
	// derniers résultat de la recherche
	if (isPrivateSearch)
	{
    	operationPane.addOperation(resource.getIcon("gallery.lastResult"), resource.getString("gallery.lastResult"), "LastResult");
	}
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    
    
	if (isPrivateSearch)
	{
		 // affichage de la zone de recherche
		 // ---------------------------------
		 Board b	= gef.getBoard();
		 out.println(b.printBefore());
		 Button validateButton 	= (Button) gef.getFormButton("OK", "javascript:onClick=sendData();", false);
		 %>
		 <center>
		 	<table border="0" cellpadding="0" cellspacing="0">
		 		<form name="searchForm" action="SearchKeyWord" method="POST" onSubmit="javascript:sendData();">
		 			<tr>
		 				<td valign="middle" align="left" class="txtlibform" width="30%"><%=resource.getString("GML.search")%></td>
		 				<td align="left" valign="middle">
		 					<table border="0" cellspacing="0" cellpadding="0">
		 						<tr valign="middle">
		 							<td valign="middle"><input type="text" name="SearchKeyWord" size="36"></td>
		 							<td valign="middle">&nbsp;</td>
		 							<td valign="middle" align="left" width="100%"><% out.println(validateButton.print());%></td>
		 							<td valign="middle">&nbsp;</td>
		 							<td valign="middle"><a href="SearchAdvanced"><%=resource.getString("gallery.searchAdvanced")%></a>
		 						</tr>
		 					</table>
		 				</td>
		 			</tr>
		 		</form>
		     </table>
		 </center>
		 <%
		 out.println(b.printAfter());
		 out.println("<br>");
	}
    
    if ( "user".equals(profile) ||"privilegedUser".equals(profile) || "writer".equals(profile) )
	{
		NavigationList navList = gef.getNavigationList();
    	navList.setTitle(root.getName());
		Iterator it = (Iterator) root.getChildrenDetails().iterator();
		
		while (it.hasNext()) 
		{
			NodeDetail unAlbum = (NodeDetail) it.next();
			int id = unAlbum.getId();
			String nom = unAlbum.getName();
			String lien = null;
			navList.addItem(nom,"ViewAlbum?Id="+id,-1,unAlbum.getDescription(), lien);
		}
		out.println(navList.print());
	}
	else
	{
	    ArrayPane arrayPane = gef.getArrayPane("albumList", "GoToCurrentAlbum", request, session);
		ArrayColumn columnIcon = arrayPane.addArrayColumn("&nbsp;");
        columnIcon.setSortable(false);
		arrayPane.addArrayColumn(resource.getString("gallery.albums"));
		arrayPane.addArrayColumn(resource.getString("GML.description"));
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("gallery.operation"));
		columnOp.setSortable(false);
		
		// remplissage de l'ArrayPane avec les albums de niveau 1
		// ------------------------------------------------------
		Iterator it = (Iterator) root.getChildrenDetails().iterator();
		while (it.hasNext()) 
		{
			ArrayLine ligne = arrayPane.addArrayLine();
			
			IconPane icon = gef.getIconPane();
			Icon albumIcon = icon.addIcon();
       		albumIcon.setProperties(resource.getIcon("gallery.gallerySmall"), "");
       		icon.setSpacing("30px");
       		ligne.addArrayCellIconPane(icon);
			
			NodeDetail unAlbum = (NodeDetail) it.next();
			int id = unAlbum.getId();
			String nom = unAlbum.getName();
			String link = "";
			if (unAlbum.getPermalink() != null)
			{
				link = "&nbsp;<a href=\""+unAlbum.getPermalink()+"\"><img src=\""+resource.getIcon("gallery.link")+"\" border=\"0\" align=\"bottom\" alt=\""+resource.getString("gallery.CopyAlbumLink")+"\" title=\""+resource.getString("gallery.CopyAlbumLink")+"\"></a>";
			}
			//ligne.addArrayCellLink(unAlbum.getName()+link,"ViewAlbum?Id="+id);
			
			ArrayCellText arrayCellText0 = ligne.addArrayCellText("<a href=\"ViewAlbum?Id="+id+"\">"+unAlbum.getName()+"</a>"+link);
            arrayCellText0.setCompareOn(unAlbum.getName());
			
			ligne.addArrayCellText(unAlbum.getDescription());
			if ( "admin".equals(profile) || ("publisher".equals(profile) && unAlbum.getCreatorId().equals(userId)) )
			{
				// si publisher, possibilité de modif que sur ses albums
				// création de la colonne des icônes
				IconPane iconPane = gef.getIconPane();
				// icône "modifier"
				Icon updateIcon = iconPane.addIcon();
	       		updateIcon.setProperties(resource.getIcon("gallery.updateAlbum"), resource.getString("gallery.updateAlbum"), "javaScript:editAlbum('"+id+"')");
	   			// icône "supprimer"
	       		Icon deleteIcon = iconPane.addIcon();
	       		deleteIcon.setProperties(resource.getIcon("gallery.deleteAlbum"), resource.getString("gallery.deleteAlbum"), "javaScript:deleteConfirm('"+id+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(nom))+"')");
	       		iconPane.setSpacing("30px");
	       		ligne.addArrayCellIconPane(iconPane);
	        }
		}
		out.println(arrayPane.print());
	}
		
	// afficher les dernières photos téléchargées
	// ------------------------------------------
	
	Board	board		 = gef.getBoard();
	%>
			<br>
	<%
	out.println(board.printBefore());
	
	// affichage de l'entête
	%>
		<table width="100%" border="0" cellspacing="0" cellpadding="0" align=center>
		<tr>
			<td colspan="5" align="center" class=ArrayNavigation>
				<%
					out.println(resource.getString("gallery.dernieres"));
				%>
			</td>
		</tr>
	<%
	if (photos != null)
	{
		String	vignette_url 	= null;
		String 	altTitle 		= ""; 
		int		nbPhotos	 	= photos.size();

		if (nbPhotos>0) 
		{
			PhotoDetail photo;
			String idP;
			Iterator itP = (Iterator) photos.iterator();
	
			while (itP.hasNext() && nbTotal != 0) 
			{
				// affichage de la photo
				out.println("<tr><td colspan=\""+nbParLigne+"\">&nbsp;</td></tr>");
				out.println("<tr>");
				while (itP.hasNext() && nbAffiche < nbParLigne)
				{			
					photo 		= (PhotoDetail) itP.next();
					altTitle 	= "";
					if (photo != null)
					{
						idP = photo.getPhotoPK().getId();
						String nomRep = resource.getSetting("imagesSubDirectory") + idP;
						String name = photo.getImageName();
						if (name != null)
						{
							String type = name.substring(name.lastIndexOf(".") + 1, name.length());
							name = photo.getId() + "_133x100.jpg";
							vignette_url = FileServer.getUrl(spaceId, componentId, name, photo.getImageMimeType(), nomRep);
							if ("bmp".equalsIgnoreCase(type))
								vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_133x100.jpg";
																
							altTitle = Encode.javaStringToHtmlString(photo.getTitle());
							if (photo.getDescription() != null && photo.getDescription().length() > 0)
								altTitle += " : "+Encode.javaStringToHtmlString(photo.getDescription());
						}
						else
						{
							vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_133x100.jpg";
						}
						nbTotal 	= nbTotal - 1 ;	
						nbAffiche 	= nbAffiche + 1;
						
						// on affiche encore sur la même ligne
						%>
							<td valign="middle" align="center">
								<table border="0" width="10" align="center" cellspacing="1" cellpadding="0" class="fondPhoto"><tr><td align="center">
									<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
										<a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
									</td></tr></table>
								</td></tr></table>
							</td>
						<%
					}
				}
				// on passe à la ligne suivante
				nbAffiche = 0;
				out.println("</tr>");
				if (itP.hasNext())
					out.println("<tr><td colspan=\""+nbParLigne+"\">&nbsp;</td></tr>");
			}
		}
		else
		{
			%>
				<tr>
					<td colspan="5" valign="middle" align="center" width="100%">
						<br>
						<%
							out.println(resource.getString("gallery.pasPhoto"));
						%>
						<br>
					</td>
				</tr>
			<%
		}
	}
	%>
		</table>
	<%
		
  	out.println(board.printAfter());
	
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="albumForm" action="" Method="POST">
	<input type="hidden" name="Id">
	<input type="hidden" name="Name">
	<input type="hidden" name="Description">
</form>
</body>
</html>