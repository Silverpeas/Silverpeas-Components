<%@ page import="com.silverpeas.gallery.GalleryComponentSettings" %>
<%@ page import="com.silverpeas.gallery.model.Media" %>
<%@ page import="com.silverpeas.gallery.model.Photo" %>
<%@ page import="com.silverpeas.gallery.constant.MediaResolution" %>
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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="mediaList" value="${requestScope.MediaList}"/>
<jsp:useBean id="mediaList" type="java.util.List<com.silverpeas.gallery.model.Media>"/>
<c:set var="mediaResolution" value="${requestScope.MediaResolution}"/>
<jsp:useBean id="mediaResolution" type="com.silverpeas.gallery.constant.MediaResolution"/>
<c:set var="nbMediaPerPage" value="${requestScope.NbMediaPerPage}"/>
<c:set var="currentPageIndex" value="${requestScope.CurrentPageIndex}"/>
<c:set var="firstMediaIndex" value="${nbMediaPerPage * currentPageIndex}"/>
<c:set var="lastMediaIndex" value="${firstMediaIndex + nbMediaPerPage - 1}"/>

<%
  // récupération des paramètres :
  String searchKeyWord = (String) request.getAttribute("SearchKeyWord");
  String profile = (String) request.getAttribute("Profile");
  int firstPhotoIndex = ((Integer) request.getAttribute("CurrentPageIndex")).intValue();
  int nbPhotosPerPage = ((Integer) request.getAttribute("NbMediaPerPage")).intValue();
  Boolean isViewMetadata = (Boolean) request.getAttribute("IsViewMetadata");
  Boolean isViewList = (Boolean) request.getAttribute("IsViewList");
  Collection<String> selectedIds = (Collection) request.getAttribute("SelectedIds");
  boolean isViewNotVisible = ((Boolean) request.getAttribute("ViewVisible")).booleanValue();
  boolean isBasket = ((Boolean) request.getAttribute("IsBasket")).booleanValue();

  // déclaration des variables :
  int nbAffiche = 0;
  int nbParLigne = 1;
  int largeurCellule = 0;
  boolean viewMetadata = isViewMetadata.booleanValue();
  boolean viewList = isViewList.booleanValue();
  String typeAff = "1";

  // initialisation de la pagination
  Pagination pagination = gef.getPagination(mediaList.size(), nbPhotosPerPage, firstPhotoIndex);
  List<Media> subMediaList = mediaList.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());

  // création du chemin :
  String chemin = " ";
  if (isViewNotVisible) {
    chemin = resource.getString("gallery.viewNotVisible");
  } else {
    chemin = "<a href=\"SearchAdvanced\">"
        + resource.getString("gallery.searchAdvanced") + "</a>";
    chemin = chemin + " > "
        + resource.getString("gallery.resultSearch");
    if (StringUtil.isDefined(searchKeyWord)) {
      chemin = chemin + " '" + searchKeyWord + "'";
    }
  }

  // calcul du nombre de photo par ligne en fonction de la taille
  if (mediaResolution.isTiny()) {
    nbParLigne = 8;
  } else if (mediaResolution.isSmall()) {
    nbParLigne = 5;
    if (viewList) {
      typeAff = "2";
    }
  } else if (mediaResolution.isMedium()) {
    nbParLigne = 3;
    if (viewList) {
      typeAff = "3";
      nbParLigne = 1;
    }
  }
  largeurCellule = 100 / nbParLigne;
%>

<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
	var albumWindow = window;

	function sendData()
	{
		// envoi des photos sélectionnées pour la modif par lot
		document.mediaForm.SelectedIds.value 	= getObjects(true);
		document.mediaForm.NotSelectedIds.value = getObjects(false);

		document.mediaForm.submit();
	}

	function sendToBasket()
	{
		// envoi des photos sélectionnées dans le panier
		document.mediaForm.SelectedIds.value 	= getObjects(true);
		document.mediaForm.NotSelectedIds.value = getObjects(false);
		document.mediaForm.action				= "BasketAddMediaList";
		document.mediaForm.submit();
	}

	function getObjects(selected)
	{
		var  items = "";
		var boxItems = document.mediaForm.SelectMedia;
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
		return items;
	}

	function doPagination(index)
	{
		document.mediaForm.SelectedIds.value 	= getObjects(true);
		document.mediaForm.NotSelectedIds.value = getObjects(false);
		document.mediaForm.Index.value 			= index;
		document.mediaForm.action				= "PaginationSearch";
		document.mediaForm.submit();
	}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">

  <%
    // création de la barre de navigation
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(chemin);

    Button returnButton;
    if (isViewNotVisible) {
      returnButton = gef.getFormButton(resource.getString("GML.back"), "Main", false);
    } else {
      returnButton = gef.getFormButton(resource.getString("GML.back"), "SearchAdvanced",
          false);
    }

    if ("admin".equals(profile) || "publisher".equals(profile) || "writer".equals(profile)) {
      // possibilité de modifier les photos par lot
      operationPane.addOperation(resource.getIcon("gallery.updateSelectedMedia"), resource.getString(
          "gallery.updateSelectedMedia"), "javascript:onClick=sendData();");
      operationPane.addOperation(resource.getIcon("gallery.allSelect"), resource.getString(
          "gallery.allSelect"), "AllSelected");
    }
    if ("user".equals(profile) && isBasket) {
      // ajouter les photos sélectionnées au panier
      operationPane.addOperation(resource.getIcon("gallery.addToBasketSelectedMedia"), resource.
          getString("gallery.addToBasketSelectedMedia"), "javascript:onClick=sendToBasket();");
    }

    out.println(window.printBefore());
    out.println(frame.printBefore());

    // afficher les photos
    // -------------------
    // affichage des photos sous forme de vignettes
    if (mediaList != null) {
%>
<br>
<%
	String vignette_url = null;
		int nbPhotos = mediaList.size();
		Board board = gef.getBoard();

		if (mediaList.size() > 0) {
			out.println(board.printBefore());
			// affichage de l'entête
%>
<form name="mediaForm" action="EditSelectedMedia" accept-charset="UTF-8">
<table width="98%" border="0" cellspacing="0" cellpadding="0"
	align=center>
	<input type="hidden" name="SearchKeyWord" value="<%=searchKeyWord%>">
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
      <gallery:albumListHeader currentMediaResolution="${mediaResolution}"
                               nbMediaPerPage="${nbMediaPerPage}"
                               currentPageIndex="${currentPageIndex}"
                               mediaList="${mediaList}"/>
		</td>
	</tr>
</table>
<%
	String photoColor = "";
	Media media;
	String idP;
	Iterator<Media> it = subMediaList.iterator();
	while (it.hasNext()) {
		// affichage de la photo
%>
<table width="98%" border="0" cellspacing="5" cellpadding="0"
	align=center>
	<tr>
		<td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td>
	</tr>
	<tr>
      <%
          while (it.hasNext() && nbAffiche < nbParLigne) {
            media = it.next();
            if (media != null) {
              idP = media.getMediaPK().getId();
              vignette_url = media.getApplicationThumbnailUrl(mediaResolution);
              photoColor = "fondPhoto";
              if (!media.isVisible()) {
                photoColor = "fondPhotoNotVisible";
              }

              nbAffiche = nbAffiche + 1;

              String altTitle = EncodeHelper.javaStringToHtmlString(media.getTitle());
              if (StringUtil.isDefined(media.getDescription())) {
                altTitle += " : "
                    + EncodeHelper.javaStringToHtmlString(media.getDescription());
              }

              // on affiche encore sur la même ligne
		%>

		<%
			if (typeAff.equals("2")) {
		%>
		<td valign="top" width="<%=largeurCellule%>%">
		<table border="0" align="center" width="10" cellspacing="1"
			cellpadding="0" class="<%=photoColor%>">
			<tr>
				<td align="center" colspan="2">
				<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto">
					<tr>
						<td bgcolor="#FFFFFF"><a href="MediaView?MediaId=<%=idP%>"><IMG
							SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>"
							title="<%=altTitle%>"></a></td>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
				<%
					//traitement de la case à cocher
											String usedCheck = "";
											if (selectedIds != null
													&& selectedIds.contains(idP))
												usedCheck = "checked";
				%>
				<td align="center" width="10"><input type="checkbox"
					name="SelectMedia" value="<%=idP%>" <%=usedCheck%>></td>
				<td class="txtlibform"><%=media.getName()%></td>
			</tr>
			<%
				if (media.getDescription() != null) {
			%>
			<tr>
				<td>&nbsp;</td>
				<td><%=media.getDescription()%></td>
			</tr>
			<%
				}
			%>
		</table>
		</td>
		<%
			}
								if (typeAff.equals("1")) {
		%>
		<td valign="bottom" width="<%=largeurCellule%>%">
		<table border="0" width="10" align="center" cellspacing="1"
			cellpadding="0" class="<%=photoColor%>">
			<tr>
				<td align="center">
				<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto">
					<tr>
						<td bgcolor="#FFFFFF"><a href="MediaView?MediaId=<%=idP%>"><IMG
							SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>"
							title="<%=altTitle%>"></a></td>
					</tr>
				</table>
				</td>
			</tr>
			<%
				//traitement de la case à cocher
										String usedCheck = "";
										if (selectedIds != null
												&& selectedIds.contains(idP))
											usedCheck = "checked";
			%>
			<tr>
				<td align="center"><input type="checkbox" name="SelectMedia"
					value="<%=idP%>" <%=usedCheck%>></td>
			</tr>
		</table>
		</td>
		<%
			}
								// affichage du texte à coté de la photo pour le cas de l'affichage en liste
								if (typeAff.equals("3")) {
									// on affiche les photos en colonne avec les métaData à droite
		%>
		<td valign="middle" align="center">
		<table border="0" width="10" align="center" cellspacing="1"
			cellpadding="0" class="<%=photoColor%>">
			<tr>
				<td align="center">
				<table cellspacing="1" cellpadding="5" border="0" class="cadrePhoto">
					<tr>
						<td bgcolor="#FFFFFF"><a href="MediaView?MediaId=<%=idP%>"><IMG
							SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>"
							title="<%=altTitle%>"></a></td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
		<td valign="top" width="100%">
		<table border="0" width="100%">
			<tr>
				<td class="txtlibform" nowrap><%=resource.getString("GML.title")%>
				:</td>
				<td><%=media.getName()%></td>
			</tr>
			<%
				if (media.getDescription() != null) {
			%>
			<tr>
				<td class="txtlibform" nowrap><%=resource
																.getString("GML.description")%>
				:</td>
				<td><%=media.getDescription()%></td>
			</tr>
			<%
				}
										if (media.getAuthor() != null) {
			%>
			<tr>
				<td class="txtlibform" nowrap><%=resource
																.getString("GML.author")%>
				:</td>
				<td><%=media.getAuthor()%></td>
			</tr>
			<%
    }

      Photo photo = media.getPhoto();
      if (viewMetadata && photo != null) {
        final Collection<String> metaDataKeys = photo.getMetaDataProperties();
        if (metaDataKeys != null && !metaDataKeys.isEmpty()) {
          MetaData metaData;
          for (final String property : metaDataKeys) {
            metaData = photo.getMetaData(property);
            String mdLabel = metaData.getLabel();
            String mdValue = metaData.getValue();
            if (metaData.isDate()) {
              mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
            }
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
			if (media.getKeyWord() != null) {
			%>
			<tr>
				<td class="txtlibform" nowrap><%=resource
																.getString("gallery.keyword")%>
				:</td>
				<td>
				<%
					String keyWord = media.getKeyWord();
                      // découper la zone keyWord en mots
                      StringTokenizer st = new StringTokenizer(
                          keyWord);
                      // traitement des mots clés
                      while (st.hasMoreTokens()) {
                        String searchWord = st.nextToken();
				%> <a href="SearchKeyWord?SearchKeyWord=<%=searchWord%>">
				<%=searchWord%> </a> <%
 	}
 								out.println("</td></tr>");
 							}
 							//traitement de la case à cocher
 							String usedCheck = "";
 							if (selectedIds != null
 									&& selectedIds.contains(idP))
 								usedCheck = "checked";
 %>

			<tr>
				<td align="left" valign="top" colspan="2"><input
					type="checkbox" name="SelectMedia" value="<%=idP%>" <%=usedCheck%>></td>
			</tr>
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

				if (nbPhotos > nbPhotosPerPage) {
	%>
	<tr>
		<td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td>
	</tr>
	<tr class=intfdcolor4>
		<td colspan="<%=nbParLigne + textColonne%>"><%=pagination.printIndex("doPagination")%>
		</td>
	</tr>
	<%
		}
	%>
</table>
</form>
<%
	out.println(board.printAfter());
		} else {
			// pas de photos
			out.println(board.printBefore());
			if (isViewNotVisible)
				out.println("<center>"+ resource.getString("gallery.empty.data")+ "</center>");
			else
				out.println("<center>"+ resource.getString("gallery.noPhotoSearchBegin") + searchKeyWord + " " + resource.getString("gallery.noPhotoSearchEnd")+ "</center>");
			out.println("<BR/><center>" + returnButton.print()
					+ "</center><BR/>");
			out.println(board.printAfter());
		}
	}

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</body>
</html>