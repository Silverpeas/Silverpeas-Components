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
	PhotoDetail photo			= (PhotoDetail) request.getAttribute("Photo");
	Collection 	path 			= (Collection) request.getAttribute("Path");
	String 		profile			= (String) request.getAttribute("Profile");
	Integer		rang			= (Integer) request.getAttribute("Rang");
	Integer		albumSize		= (Integer) request.getAttribute("NbPhotos");
	Integer		nbCom			= (Integer) request.getAttribute("NbComments");
	boolean		pdc 			= ((Boolean) request.getAttribute("IsUsePdc")).booleanValue();
	boolean 	viewMetadata	= ((Boolean) request.getAttribute("IsViewMetadata")).booleanValue();
	boolean 	watermark		= ((Boolean) request.getAttribute("IsWatermark")).booleanValue();
	String 		XMLFormName		= (String) request.getAttribute("XMLFormName");
	boolean		updateAllowed	= ((Boolean) request.getAttribute("UpdateImageAllowed")).booleanValue();
	boolean		showComments	= ((Boolean) request.getAttribute("ShowCommentsTab")).booleanValue();
	String 		sizeParam		= (String) request.getAttribute("PreviewSize");
	boolean 	linkDownload 	= ((Boolean) request.getAttribute("ViewLinkDownload")).booleanValue();
	boolean 	isBasket	 	= ((Boolean) request.getAttribute("IsBasket")).booleanValue();
	
	// paramètres du formulaire
	Form		xmlForm 		= (Form) request.getAttribute("XMLForm");
	DataRecord	xmlData			= (DataRecord) request.getAttribute("XMLData");
	
	// déclaration des variables :
	String 		nomRep 				= resource.getSetting("imagesSubDirectory") + photo.getPhotoPK().getId();
	String 		name 				= "";
	if (photo.getImageName() != null && !photo.getImageName().equals(""))
		name = photo.getImageName();
	String 		namePreview			= photo.getId() + "_" + sizeParam + ".jpg";
	//String 		namePreview			= photo.getId() + "_preview.jpg";
	String 		nameVignette		= photo.getId() + "_266x150.jpg";
	String 		preview_url			= FileServer.getUrl(null, componentId, namePreview, photo.getImageMimeType(), nomRep);
	String 		title 				= photo.getTitle();		
	String 		description			= photo.getDescription();	
	String 		author				= photo.getAuthor();
	String 		creationDate		= resource.getOutputDate(photo.getCreationDate());
	String 		creatorName 		= photo.getCreatorName();
	String 		updateDate			= resource.getOutputDate(photo.getUpdateDate());
	String 		updateName 			= photo.getUpdateName();
	long 		size				= photo.getImageSize();
	int 		height				= photo.getSizeH();
	int 		width				= photo.getSizeL();
	String 		photoId				= new Integer(photo.getPhotoPK().getId()).toString();	
	String 		lien 				= FileServer.getUrl(spaceId, componentId, URLEncoder.encode(name), photo.getImageMimeType(), nomRep);
	String 		lienWatermark		= "";
	String 		lienPreview			= FileServer.getUrl(spaceId, componentId, namePreview, photo.getImageMimeType(), nomRep);
	String 		lienVignette		= FileServer.getUrl(spaceId, componentId, nameVignette, photo.getImageMimeType(), nomRep);
	boolean 	debut				= rang.intValue() == 0;
	boolean 	fin					= rang.intValue() == albumSize.intValue()-1;
	String		beginDownloadDate	= resource.getOutputDate(photo.getBeginDownloadDate());
	String 		endDownloadDate		= resource.getOutputDate(photo.getEndDownloadDate());
	String 		nbComments 			= nbCom.toString();
	String 		link				= photo.getPermalink();
	Collection	metaDataKeys		= photo.getMetaDataProperties();
	String 		keyWord				= photo.getKeyWord();
	String		beginDate			= resource.getOutputDate(photo.getBeginDate());
	String 		endDate				= resource.getOutputDate(photo.getEndDate());
	
	// si le paramètre watermark est actif, récupérer l'image avec le watermark
	if (watermark)
	{
		// image avec le watermarkOther pour le téléchargement
		File fileWatermark = new File(FileRepositoryManager.getAbsolutePath(componentId) + nomRep + File.separator + photo.getId() + "_watermark.jpg");
		
		if( fileWatermark.exists() )
			lienWatermark = FileServer.getUrl(spaceId, componentId, photo.getId() + "_watermark.jpg", photo.getImageMimeType(), nomRep);
	}
	
	Board board	= gef.getBoard();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

var notifyWindow = window;

function deleteConfirm(id,nom) 
{
	if(window.confirm("<%=resource.getString("gallery.confirmDeletePhoto")%> '"+nom+"' ?"))
	{
		document.photoForm.action = "DeletePhoto?PhotoId="+id;
		document.photoForm.submit();
	}
}

function goToNotify(url) 
{
	windowName = "notifyWindow";
	larg = "740";
	haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
        notifyWindow.close();
    notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

	function clipboardCopy() {
	    top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>copy?Object=Image&Id=<%=photo.getId()%>';
	}
	
	function clipboardCut() {
	    top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>cut?Object=Image&Id=<%=photo.getId()%>';
	}

</script>
</head>
<body class="yui-skin-sam" bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(displayPath(path));
	
	String url = "ToAlertUser?PhotoId="+photoId;
	operationPane.addOperation(resource.getIcon("gallery.alert"), resource.getString("GML.notify"), "javaScript:onClick=goToNotify('"+url+"')");
	operationPane.addLine();
	
	if (updateAllowed)
	{
		operationPane.addOperation(resource.getIcon("gallery.deletePhoto"),resource.getString("gallery.deletePhoto"),"javaScript:deleteConfirm('"+photoId+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(title))+"')");
	}
	if ("admin".equals(profile))
	{
   		operationPane.addOperation(resource.getIcon("gallery.copy"), resource.getString("GML.copy"), "javascript:onClick=clipboardCopy()");
   		operationPane.addOperation(resource.getIcon("gallery.cut"), resource.getString("GML.cut"), "javascript:onClick=clipboardCut()");
       	operationPane.addLine();
	}
	if (albumSize.intValue() > 1)
	{
   		// diaporama
		operationPane.addOperation(resource.getIcon("gallery.startDiaporama"), resource.getString("gallery.diaporama"), "StartDiaporama");
	}

	if ("user".equals(profile) && isBasket)
	{
		operationPane.addLine();
		// ajouter la photo au panier
		operationPane.addOperation(resource.getIcon("gallery.addPhotoToBasket"),resource.getString("gallery.addPhotoToBasket"),"BasketAddPhoto?PhotoId="+photoId);
	}
	
	// derniers résultat de la recherche
	operationPane.addLine();
    operationPane.addOperation(resource.getIcon("gallery.lastResult"), resource.getString("gallery.lastResult"), "LastResult");


   	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("gallery.photo"), "#", true, false);
	if (updateAllowed)
	{
		tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?PhotoId="+photoId, false);
	}
	if (showComments)
		tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbComments+")", "Comments?PhotoId="+photoId, false);
	if (updateAllowed)
	{
		tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?PhotoId="+photoId, false);
		if (pdc)
			tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PhotoId="+photoId, false);
	}
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
<FORM Name="photoForm" Method="POST" accept-charset="UTF-8">
	<tr>
	<!-- AFFICHAGE des boutons de navigation -->
		<td align="center">
			<table border="0">
				<tr>
					<td align="center" width="25">
						<%	if ( !debut ) { %>
							<a href="PreviousPhoto"><img src="/silverpeas/util/viewGenerator/icons/arrows/arrowLeft.gif" align="middle" border=0 alt="<%=resource.getString("gallery.previous")%>" title="<%=resource.getString("gallery.previous")%>"></a>
						<% } else { %>
							&nbsp;
						<% } %>
					</td>
					<td align="center" nowrap class="txtlibform" width="50">
						<%=rang.intValue()+1%> / <%=albumSize.intValue()%>
					</td>
					<td align="center" width="25">
						<% if ( !fin ) { %>
							<a href="NextPhoto"><img src="/silverpeas/util/viewGenerator/icons/arrows/arrowRight.gif" align="middle" border=0 alt="<%=resource.getString("gallery.next")%>" title="<%=resource.getString("gallery.next")%>"></a>
						<% } else { %>
							&nbsp;
						<% } %>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<!-- AFFICHAGE de la preview de la photo -->
      	<td> 
			<%
				String type = name.substring(name.lastIndexOf(".") + 1, name.length());
				if ("bmp".equalsIgnoreCase(type))
					preview_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_" + sizeParam + ".jpg";
				if ( preview_url != null )
				{
					%>
					<table border="0" width="10" align="center" cellspacing="1" cellpadding="0" class="fondPhoto"><tr><td align="center">
						<table cellspacing="1" cellpadding="5" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
							<center><IMG SRC="<%=preview_url%>"></center>
						</td></tr></table>
					</td></tr></table>
					<%
				}
			%>        
		</td>
	</tr>
	</table>
	<table width="600" align="center">
	<tr>
		<td align="center">
			<%=board.printBefore()%>
			<table align="left" border="0" CELLPADDING="5">
				<!-- AFFICHAGE des données de la photo -->
				<%	if ( link != null && !link.equals("")) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("gallery.permalink")%> :</td>
						<td><a href=<%=link%> ><img src=<%=resource.getIcon("gallery.link")%> border="0" alt='<%=resource.getString("gallery.CopyPhotoLink")%>' title='<%=resource.getString("gallery.CopyPhotoLink")%>' ></a></td>
					</tr>
				<%	}	
				if ( title != null && !title.equals(name)) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("GML.title")%> :</td>
						<td><%=title%></td>
					</tr>
				<%	}	
				if ( description != null && !description.equals("") ) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("GML.description")%> :</td>
						<td><%=description%></td>
					</tr>
				<%	}	
						if (linkDownload || photo.isDownloadable()) 
						{ %>
						<tr align="left">
							<td class="txtlibform" nowrap><%=resource.getString("gallery.originale")%> :</td>
							<td><a href="<%=lien%>" target=_blank><%=Encode.javaStringToHtmlString(resource.getString("gallery.telecharger"))%></a></td>
						</tr>
						
						<% if (!lienWatermark.equals(""))
							{%>
						<tr align="left">
							<td class="txtlibform" nowrap><%=resource.getString("gallery.originaleWatermark")%> :</td>
							<td><a href="<%=lienWatermark%>" target=_blank><%=Encode.javaStringToHtmlString(resource.getString("gallery.telecharger"))%></a></td>
						</tr>
						<% } 
						} %>
						
						<% if (photo.isDownload() && (photo.getBeginDownloadDate() != null || photo.getEndDownloadDate() != null)) { %>
						<tr align="left">
							<td class="txtlibform" nowrap><%=resource.getString("gallery.beginDownloadDate")%> :</td>
							<TD><%=beginDownloadDate%>
							<% if (photo.getEndDownloadDate() != null) { %>
								&nbsp;<span class="txtlibform"><%=resource.getString("gallery.endDownloadDate")%></span>&nbsp;<%=endDownloadDate%>
							<% } %>
							</TD>
						</tr>
						<% } %>
				<% if (photo.getBeginDate() != null || photo.getEndDate() != null) { %>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("gallery.beginDate")%> :</td>
						<TD><%=beginDate%>
						<% if (photo.getEndDate() != null) { %>
							&nbsp;<span class="txtlibform"><%=resource.getString("gallery.endDate")%></span>&nbsp;<%=endDate%>
						<% } %>
						</TD>
					</tr>
				<% 	}  %>
	
			<!--		
				<tr>
					<td class="txtlibform" nowrap><%=resource.getString("gallery.preview")%> :</td>
					<td><a href="<%=lienPreview%>" target=_blank><%=Encode.javaStringToHtmlString(resource.getString("gallery.telechargerPreview"))%></a></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap><%=resource.getString("gallery.vignette")%> :</td>
					<td><a href="<%=lienVignette%>" target=_blank><%=Encode.javaStringToHtmlString(resource.getString("gallery.telechargerVignette"))%></a></td>
				</tr> 	 
			-->
			
				<% 
				if ( name != null ) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("gallery.nomFic")%> :</td>
						<td><%=name%></td>
					</tr>
				<%	}
				if ( size != 0 ) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("gallery.poids")%> :</td>
						<td><%=FileRepositoryManager.formatFileSize(size)%></td>
					</tr>
				<%	}	
				if ( height != 0 ) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("gallery.taille")%> :</td>
						<td><%=width%> x <%=height%> <%=resource.getString("gallery.pixels")%> </td>
					</tr>
				<%	}
				if ( author != null && !author.equals("") ) {	%>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("GML.author")%> :</td>
						<td><%=author%></td>
					</tr>
				<%	}	%>
				<tr align="left">
					<td class="txtlibform" nowrap><%=resource.getString("gallery.creationDate")%> :</td>
					<td><%=creationDate%>&nbsp;<span class="txtlibform"><%=resource.getString("gallery.par")%></span>&nbsp;<%=creatorName%></td>
				</tr>
				<% if (updateDate != null && updateName != null) { %>
					<tr align="left">
						<td class="txtlibform" nowrap><%=resource.getString("gallery.updateDate")%> :</td>
						<td><%=updateDate%>&nbsp;<span class="txtlibform"><%=resource.getString("gallery.par")%></span>&nbsp;<%=updateName%></td>
					</tr>
				<%	} 
				if ( keyWord != null && !keyWord.equals("") ) 
				{ %>
					<tr align="left">
					<td class="txtlibform" nowrap><%=resource.getString("gallery.keyWord")%> :</td>
					<td>
					<%
					StringTokenizer st = new StringTokenizer(keyWord);
					while (st.hasMoreTokens()) 
					{
						String searchKeyWord = (String) st.nextToken();
						%>
						<a href="<%="SearchKeyWord?SearchKeyWord=" + searchKeyWord%>"> <%=searchKeyWord%> </a>
					 <% } %>
					</td></tr>
				<% } %>
				</table>
				<%=board.printAfter()%>
							
				<%
				// AFFICHAGE des métadonnées
				if (viewMetadata)
				{	
					if (metaDataKeys != null && metaDataKeys.size() > 0) 
					{
						out.println("<br/>");
						out.println(board.printBefore());
						out.println("<table align=\"left\" border=\"0\" CELLPADDING=\"5\">");
						Iterator it = (Iterator) metaDataKeys.iterator();
						while (it.hasNext())
						{
							// traitement de la metaData
							String propertyLong = (String) it.next();
							// extraire le nom de la propertie
							MetaData metaData = photo.getMetaData(propertyLong);
							String mdLabel = metaData.getLabel();
							String mdValue = metaData.getValue();
							if (metaData.isDate())
								mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
							// affichage
							%>
								<tr align="left">
									<td class="txtlibform" nowrap valign="top"><%=mdLabel%> :</td>
									<td><%=mdValue%></td>
								</tr>
							<%
						}
						out.println(board.printAfter());
						out.println("</table>");
					}
				} 
				
				if (xmlForm != null) {
				%>
					<br/>
									
					<%=board.printBefore()%>
					<table align="left" border="0" width="50%">
					<!-- AFFICHAGE du formulaire -->
						<tr align="left">
							<td colspan="2">
							<%
								PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, gallerySC.getUserId(), gallerySC.getAlbum(gallerySC.getCurrentAlbumId()).getNodePK().getId());
								xmlContext.setObjectId(photoId);
								xmlContext.setBorderPrinted(false);
								xmlContext.setIgnoreDefaultValues(true);
								
						    	xmlForm.display(out, xmlContext, xmlData);
						    %>
							</td>	
						</tr>
					</table>
					<%=board.printAfter()%>
				<% } %>	
		</td>
	</tr>
  </form>
</table>
<% 
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	
</body>
</html>