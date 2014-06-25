<%@ page import="com.silverpeas.gallery.GalleryComponentSettings" %>
<%@ page import="com.silverpeas.gallery.model.Media" %>
<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
  AlbumDetail root = (AlbumDetail) request.getAttribute("root");
  List<Media> mediaList = (List) request.getAttribute("MediaList");

// parametrage pour l'affichage des dernieres photos telechargees
  int nbTotal = 10;
  int nbAffiche = 0;
%>

<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

function goToAlbum(id) {
    document.albumForm.Id.value = id;
    document.albumForm.submit();
}

function goToImage(photoId) {
    document.imageForm.MediaId.value = photoId;
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
    Iterator it = root.getChildrenDetails().iterator();

    while (it.hasNext()) {
      NodeDetail unAlbum = (NodeDetail) it.next();
      int id = unAlbum.getId();
      String nom = unAlbum.getName();
      String lien = null;
      navList.addItem(nom, "javascript:onClick=goToAlbum('" + id + "')", -1, unAlbum.getDescription(),
          lien);
    }
    out.println(navList.print());


    // afficher les dernieres photos telechargees
    // ------------------------------------------

    Board board = gef.getBoard();
  %>
			<br>
	<%
	out.println(board.printBefore());

	// affichage de l'entete
	%>
		<table border="0" cellspacing="0" cellpadding="0" align=center width="100%">
		<tr>
			<td align="center" class=ArrayNavigation>
				<%
					out.println(resource.getString("gallery.last.media"));
				%>
			</td>
		</tr>
        <%
          if (mediaList != null) {
            int nbPhotos = mediaList.size();

            if (nbPhotos > 0) {
              Media media;
              String idP;
              Iterator<Media> itP = mediaList.iterator();
              while (itP.hasNext() && nbAffiche < nbTotal) {
                // affichage de la photo
                out.println("<tr><td>&nbsp;</td></tr>");
                out.println("<tr><td align=\"center\">");

                while (itP.hasNext() && nbAffiche < nbTotal) {
                  media = itP.next();
                  if (media != null) {
                    idP = media.getMediaPK().getId();
                    String vignette_url = media.getThumbnailUrl("_66x50.jpg");
                    String altTitle = EncodeHelper.javaStringToHtmlString(media.getTitle());
                    if (media.getDescription() != null && media.getDescription().length() > 0) {
                      altTitle += " : " + EncodeHelper.javaStringToHtmlString(media.getDescription());
                    }
        %>
        <div id="vignette">
          <a href="javascript:onClick=goToImage('<%=idP%>')"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
        </div>
        <%
                nbAffiche = nbAffiche + 1;
              }
            }
            // on passe e la ligne suivante
            out.println("</td></tr>");
            if (itP.hasNext()) {
              out.println("<tr><td>&nbsp;</td></tr>");
            }
          }
        } else {
        %>
				<tr>
					<td colspan="5" valign="middle" align="center" width="100%">
						<br>
						<%
							out.println(resource.getString("gallery.empty.data"));
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
<form name="imageForm" action="MediaView" Method="POST" target="MyMain">
	<input type="hidden" name="MediaId">
</form>
</body>
</html>