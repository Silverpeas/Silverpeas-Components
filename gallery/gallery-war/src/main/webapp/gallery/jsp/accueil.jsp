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
List<AlbumDetail> albums    = (List<AlbumDetail>) request.getAttribute("Albums");

// parametrage pour l'affichage des dernieres photos telechargees
int nbAffiche 	= 0;
int nbParLigne 	= 5;
int nbTotal 	= 15;

session.setAttribute("Silverpeas_Album_ComponentId", componentId);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.cookie.js"></script>

<script type="text/javascript">
  
	  $(document).ready(function(){
		  <%if ( "admin".equals(profile)) { %>
		    showAlbumsHelp();
		    
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
		  <%} %>
	  });
  
	  var albumsHelpAlreadyShown = false;

	  function showAlbumsHelp() {
		  var albumsCookieName = "Silverpeas_GALLERY_AlbumsHelp";
		  var albumsCookieValue = $.cookie(albumsCookieName);
		  if (!albumsHelpAlreadyShown && "IKnowIt" != albumsCookieValue) {
		    albumsHelpAlreadyShown = true;
		    $( "#albums-message" ).dialog({
		      modal: true,
		      resizable: false,
		      width: 400,
		      dialogClass: 'help-modal-message',
		      buttons: {
		        "<%=resource.getString("gallery.help.albums.buttons.ok") %>": function() {
		          $.cookie(albumsCookieName, "IKnowIt", { expires: 3650, path: '/' });
		          $( this ).dialog( "close" );
		        },
		        "<%=resource.getString("gallery.help.albums.buttons.remind") %>": function() {
		          $( this ).dialog( "close" );
		        }
		      }
		    });
		  }
		}
	  
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
  
  function clipboardPaste() {     
	  top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=RGallery&SpaceFrom=<%=spaceId%>&ComponentFrom=<%=componentId%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("GoToCurrentAlbum"))%>&TargetFrame=MyMain&message=REFRESH');
	}

var albumWindow = window;
var askWindow = window;

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
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

</script>
</head>

<body>
<div id="<%=componentId %>">
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
		operationPane.addOperation(resource.getIcon("gallery.addAlbum"),resource.getString("gallery.ajoutAlbum"), "javaScript:openGalleryEditor()");
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
		// demande de photo aupres du gestionnaire
		operationPane.addOperation(resource.getIcon("gallery.askPhoto"),resource.getString("gallery.askPhoto"), "javaScript:askPhoto()");
		operationPane.addLine();
	}

	
	if ("admin".equals(profile))
	{
       	operationPane.addOperation(resource.getIcon("gallery.paste"), resource.getString("GML.paste"), "javascript:onClick=clipboardPaste()");
       	operationPane.addLine();
	}
	
	// derniers resultat de la recherche
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
		 Button validateButton 	= gef.getFormButton("OK", "javascript:onClick=sendData();", false);
		 %>
		 <center>
		 	<form name="searchForm" action="SearchKeyWord" method="post" onsubmit="javascript:sendData();">
		 	<table border="0" cellpadding="0" cellspacing="0">
		 			<tr>
		 				<td valign="middle" align="left" class="txtlibform" width="30%"><%=resource.getString("GML.search")%></td>
		 				<td align="left" valign="middle">
		 					<table border="0" cellspacing="0" cellpadding="0">
		 						<tr valign="middle">
		 							<td valign="middle"><input type="text" name="SearchKeyWord" size="36"/></td>
		 							<td valign="middle">&nbsp;</td>
		 							<td valign="middle" align="left" width="100%"><% out.println(validateButton.print());%></td>
		 							<td valign="middle">&nbsp;</td>
		 							<td valign="middle"><a href="SearchAdvanced"><%=resource.getString("gallery.searchAdvanced")%></a></td>
		 						</tr>
		 					</table>
		 				</td>
		 			</tr>
		     </table>
		     </form>
		 </center>
    <%
    out.println(b.printAfter());
    %>
    <br/>
  <% } %>
   
  <div id="subTopics">
  <ul id="albumList">
<%
  for (AlbumDetail unAlbum : albums) {
    IconPane icon = gef.getIconPane();
    Icon albumIcon = icon.addIcon();
    albumIcon.setProperties(resource.getIcon("gallery.gallerySmall"), "");
    icon.setSpacing("30px");
      
    int id = unAlbum.getId();
    String nom = unAlbum.getName();
    String link = "";
    if (unAlbum.getPermalink() != null) {
      link = "&nbsp;<a href=\"" + unAlbum.getPermalink() + "\"><img src=\"" + resource.
          getIcon("gallery.link") + "\" border=\"0\" align=\"bottom\" alt=\"" + resource.
          getString("gallery.CopyAlbumLink") + "\" title=\"" + resource.getString(
          "gallery.CopyAlbumLink") + "\"></a>";
    }
    %>
    <li id="album_<%=id%>" class="ui-state-default">
	    <a href="ViewAlbum?Id=<%=id%>">
	 		<strong><%=unAlbum.getName()%>
	 		<span><%=unAlbum.getNbPhotos() %></span>
	 		</strong>
	 		<span><%=unAlbum.getDescription()%></span>
	 	</a>
    </li>
    <%
} %>
</ul>
</div>
<br/>
<%  
           // afficher les dernieres photos telechargees
           // ------------------------------------------
             
           Board board = gef.getBoard();
	out.println(board.printBefore());
	
	// affichage de l'entete
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
		int		nbPhotos	 	= photos.size();

		if (nbPhotos>0) 
		{
			PhotoDetail photo;
			String idP;
			Iterator itP = photos.iterator();
	
			while (itP.hasNext() && nbTotal != 0) 
			{
				// affichage de la photo
				out.println("<tr><td colspan=\""+nbParLigne+"\">&nbsp;</td></tr>");
				out.println("<tr>");
				while (itP.hasNext() && nbAffiche < nbParLigne)
				{			
					photo 		= (PhotoDetail) itP.next();
					if (photo != null) {
						idP = photo.getPhotoPK().getId();
						String nomRep = resource.getSetting("imagesSubDirectory") + idP;
						String name = photo.getImageName();
						String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
						if (StringUtil.isDefined(photo.getDescription())) {
							altTitle += " : "+EncodeHelper.javaStringToHtmlString(photo.getDescription());
						}
						if (name != null) {
							String type = name.substring(name.lastIndexOf(".") + 1, name.length());
							name = photo.getId() + "_133x100.jpg";
							vignette_url = FileServerUtils.getUrl(spaceId, componentId, name, photo.getImageMimeType(), nomRep);
							if (!ImageType.isPreviewable(name)) {
								vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_133x100.jpg";
							}
						} else {
							vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_133x100.jpg";
						}
						nbTotal 	= nbTotal - 1 ;	
						nbAffiche 	= nbAffiche + 1;
						
						// on affiche encore sur la meme ligne
						%>
							<td valign="middle" align="center">
								<table border="0" width="10" align="center" cellspacing="1" cellpadding="0" class="fondPhoto"><tr><td align="center">
									<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
										<a href="PreviewPhoto?PhotoId=<%=idP%>"><img src="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"/></a>
									</td></tr></table>
								</td></tr></table>
							</td>
						<%
					}
				}
				// on passe e la ligne suivante
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
						<br/>
						<%
							out.println(resource.getString("gallery.pasPhoto"));
						%>
						<br/>
					</td>
				</tr>
			<%
		}
	}
	%>
		</table>
        
    <%@include file="albumManager.jsp" %>
	<%
		
  	out.println(board.printAfter());
	
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="albumForm" action="" method="post">
	<input type="hidden" name="Id"/>
	<input type="hidden" name="Name"/>
	<input type="hidden" name="Description"/>
</form>
</div>
<div id="albums-message" title="<%=resource.getString("gallery.help.albums.title") %>" style="display: none;">
  <p>
    <%=resource.getStringWithParam("gallery.help.albums.content", componentLabel) %>
  </p>
</div>
<view:progressMessage/>
</body>
</html>