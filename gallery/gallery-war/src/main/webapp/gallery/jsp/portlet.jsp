<%@ include file="check.jsp" %>

<% 
AlbumDetail root 		= (AlbumDetail) request.getAttribute("root");
List 		photos		= (List) request.getAttribute("Photos");

// paramètrage pour l'affichage des dernières photos téléchargées
int nbTotal 	= 10;
int nbAffiche 	= 0;
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

function goToAlbum(id) {
    document.albumForm.Id.value = id;
    document.albumForm.submit();
}

function goToImage(photoId) {
    document.imageForm.PhotoId.value = photoId;
    document.imageForm.submit();
}

</script>
<style type="text/css">
<!--
div {
	position: relative;
	display:inline;
}
#vignette img {
	margin: 1px;
	padding: 2px;
	border: 2px solid #B3BFD1;
}
-->
</style>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
   
	NavigationList navList = gef.getNavigationList();
   	navList.setTitle(root.getName());
	Iterator it = (Iterator) root.getChildrenDetails().iterator();
		
	while (it.hasNext()) 
	{
		NodeDetail unAlbum = (NodeDetail) it.next();
		int id = unAlbum.getId();
		String nom = unAlbum.getName();
		String lien = null;
		navList.addItem(nom,"javascript:onClick=goToAlbum('"+id+"')",-1,unAlbum.getDescription(), lien);
	}
	out.println(navList.print());

		
	// afficher les dernières photos téléchargées
	// ------------------------------------------
	
	Board	board		 = gef.getBoard();
	%>
			<br>
	<%
	out.println(board.printBefore());
	
	// affichage de l'entête
	%>
		<table border="0" cellspacing="0" cellpadding="0" align=center width="100%">
		<tr>
			<td align="center" class=ArrayNavigation>
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
			while (itP.hasNext() && nbTotal != 0 && nbAffiche < nbTotal) 
			{
				// affichage de la photo
				out.println("<tr><td>&nbsp;</td></tr>");
				out.println("<tr><td align=\"center\">");

				while (itP.hasNext() && nbAffiche < nbTotal)
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
							name = photo.getId() + "_66x50.jpg";
							vignette_url = FileServer.getUrl(spaceId, componentId, name, photo.getImageMimeType(), nomRep);
							if ("bmp".equalsIgnoreCase(type))
								vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_66x50.jpg";
																
							altTitle = Encode.javaStringToHtmlString(photo.getTitle());
							if (photo.getDescription() != null && photo.getDescription().length() > 0)
								altTitle += " : "+Encode.javaStringToHtmlString(photo.getDescription());
						}
						else
						{
							vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_66x50.jpg";
						}
						%>
						<div id="vignette">
							<a href="javascript:onClick=goToImage('<%=idP%>')"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
						</div>
						<%
						nbAffiche = nbAffiche + 1;
					}
				}
				// on passe à la ligne suivante
				out.println("</td></tr>");
				if (itP.hasNext())
					out.println("<tr><td>&nbsp;</td></tr>");
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
<form name="albumForm" action="ViewAlbum" Method="POST" target="MyMain">
	<input type="hidden" name="Id">
</form>
<form name="imageForm" action="PreviewPhoto" Method="POST" target="MyMain">
	<input type="hidden" name="PhotoId">
</form>
</body>
</html>