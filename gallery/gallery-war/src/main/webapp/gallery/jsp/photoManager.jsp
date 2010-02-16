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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	PhotoDetail photo			= (PhotoDetail) request.getAttribute("Photo");
	String 		repertoire 		= (String) request.getAttribute("Repertoire");
	Collection 	path 			= (Collection) request.getAttribute("Path");
	String 		userName		= (String) request.getAttribute("UserName");
	
	Integer		nbCom			= (Integer) request.getAttribute("NbComments");
	Boolean		isUsePdc		= (Boolean) request.getAttribute("IsUsePdc");
	String 		XMLFormName		= (String) request.getAttribute("XMLFormName");
	boolean		showComments	= ((Boolean) request.getAttribute("ShowCommentsTab")).booleanValue();

	// déclaration des variables :
	String 		photoId 			= "";
	String 		title 				= "";
	String 		description 		= "";
	String 		author 				= "";
	boolean 	download 			= false;
	boolean 	albumLabel 			= false;
	String 		linkPhoto 			= "";
	String 		nomRep 				= "";
	String 		vignette_url 		= null;
	String 		action 				= "CreatePhoto";	
	String 		chemin 				= "";
	String 		creationDate		= resource.getOutputDate(new Date());
	String 		updateDate 			= null;
	String 		nameFile 			= "";
	String 		name 				= "";
	String 		creatorName 		= userName;
	String 		updateName 			= "";
	String		beginDownloadDate	= "";
	String		endDownloadDate		= "";
	String 		keyWord 			= "";
	String 		beginDate			= "";
	String 		endDate				= "";
	
	// dans le cas d'une mise à jour, récupération des données :
	if (photo != null)
	{
		photoId 		= new Integer(photo.getPhotoPK().getId()).toString();
		title 			= photo.getTitle();
		description 	= photo.getDescription();
		if (description == null)
			description = "";
		author 			= photo.getAuthor();
		if (author == null)
			author 	= "";
		download 		= photo.isDownload();
		albumLabel 		= photo.isAlbumLabel();
		nomRep 			= repertoire;
		nameFile 		= photo.getImageName();
		// name = nameFile.substring(0,nameFile.indexOf(".")) + "_66x50.jpg";
		name 			= photo.getId() + "_66x50.jpg";
		vignette_url 	= FileServer.getUrl(spaceId, componentId, name, photo.getImageMimeType(), nomRep);
		action 			= "UpdatePhoto";
		creationDate 	= resource.getOutputDate(photo.getCreationDate());
		updateDate		= resource.getOutputDate(photo.getUpdateDate());
		creatorName 	= photo.getCreatorName();
		updateName 		= photo.getUpdateName();
		if (photo.getBeginDownloadDate() != null)
          	beginDownloadDate = resource.getInputDate(photo.getBeginDownloadDate());
      	else
          	beginDownloadDate = "";
		if (photo.getEndDownloadDate() != null)
          	endDownloadDate = resource.getInputDate(photo.getEndDownloadDate());
      	else
          	endDownloadDate = "";
		
		if (title.equals(nameFile))
			title = "";
		keyWord 	= photo.getKeyWord();
		if (keyWord == null)
			keyWord 	= "";
		if (photo.getBeginDate() != null)
          	beginDate = resource.getInputDate(photo.getBeginDate());
      	else
          	beginDate = "";
		if (photo.getEndDate() != null)
          	endDate = resource.getInputDate(photo.getEndDate());
      	else
          	endDate = "";
	}
	
	// déclaration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton;
	if (action == "UpdatePhoto")
		cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "PreviewPhoto?PhotoId="+photoId, false);
	else
		cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "GoToCurrentAlbum", false);
	
%>

<html>
		<head>
		<%
			out.println(gef.getLookStyleSheet());
		%>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
		<script language="javascript">
	
			// fonctions de contrôle des zones du formulaire avant validation
			function sendData() 
			{
				if (isCorrectForm()) 
				{
	        		document.photoForm.submit();
	    		}
			}
		
			function isCorrectForm() 
			{
		     	var errorMsg = "";
		     	var errorNb = 0;
		     	var title = stripInitialWhitespace(document.photoForm.<%=ParameterNames.ImageTitle%>.value);
		     	var descr = document.photoForm.<%=ParameterNames.ImageDescription%>.value;
		     	var file = stripInitialWhitespace(document.photoForm.WAIMGVAR0.value);
		     	var beginDownloadDate = document.photoForm.<%=ParameterNames.ImageBeginDownloadDate%>.value;
		     	var endDownloadDate = document.photoForm.<%=ParameterNames.ImageEndDownloadDate%>.value;
		     	var beginDate = document.photoForm.<%=ParameterNames.ImageBeginDate%>.value;
		     	var endDate = document.photoForm.<%=ParameterNames.ImageEndDate%>.value;
		     	var langue = "<%=resource.getLanguage()%>";
		     	var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
		     	
     			var yearDownloadBegin = extractYear(beginDownloadDate, langue);
     			var monthDownloadBegin = extractMonth(beginDownloadDate, langue);
     			var dayDownloadBegin = extractDay(beginDownloadDate, langue);
     			var yearDownloadEnd = extractYear(endDownloadDate, langue);
     			var monthDownloadEnd = extractMonth(endDownloadDate, langue);
     			var dayDownloadEnd = extractDay(endDownloadDate, langue);
     			var yearBegin = extractYear(beginDate, langue);
     			var monthBegin = extractMonth(beginDate, langue);
     			var dayBegin = extractDay(beginDate, langue);
     			var yearEnd = extractYear(endDate, langue);
     			var monthEnd = extractMonth(endDate, langue);
     			var dayEnd = extractDay(endDate, langue);
     					
     			var beginDownloadDateOK = true;
     			var beginDateOK = true;
		     	
		     	if (title.length > 255) 
		     	{ 
					errorMsg+="  - '<%=resource.getString("GML.title")%>'  <%=resource.getString("gallery.MsgTaille")%>\n";
		           	errorNb++;
		     	}
		   		if (descr.length > 255) 
		     	{
		     		errorMsg+="  - '<%=resource.getString("GML.description")%>'  <%=resource.getString("gallery.MsgTaille")%>\n";
		           	errorNb++;
		     	}				
		     	if (<%=(vignette_url == null)%> && file == "")
				{
	           		errorMsg+="  - '<%=resource.getString("gallery.photo")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
	           		errorNb++;
		     	}
		     	// vérifier les dates de début et de fin de période
		     	// les dates de téléchargements
		     	if (!isWhitespace(beginDownloadDate)) 
		     	{
		   			if (beginDownloadDate.replace(re, "OK") != "OK") 
		   			{
		       			errorMsg+="  - '<%=resource.getString("gallery.dateBegin")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
		       			errorNb++;
			   			beginDownloadDateOK = false;
		   			} 
		   			else 
		   			{
		       			if (isCorrectDate(yearDownloadBegin, monthDownloadBegin, dayDownloadBegin)==false) 
		       			{
		         			errorMsg+="  - '<%=resource.getString("GML.dateBegin")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
		         			errorNb++;
				 			beginDownloadDateOK = false;
		       			}
		   			}
     			}
			     if (!isWhitespace(endDownloadDate)) 
			     {
			           if (endDownloadDate.replace(re, "OK") != "OK") 
			           {
			                 errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
			                 errorNb++;
			           } 
			           else 
			           {
			                 if (isCorrectDate(yearDownloadEnd, monthDownloadEnd, dayDownloadEnd)==false) 
			                 {
			                     errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
			                     errorNb++;
			                 } 
			                 else 
			                 {
			                     if ((isWhitespace(beginDownloadDate) == false) && (isWhitespace(endDownloadDate) == false)) 
			                     {
			                           if (beginDownloadDateOK && isD1AfterD2(yearDownloadEnd, monthDownloadEnd, dayDownloadEnd, yearDownloadBegin, monthDownloadBegin, dayDownloadBegin) == false) 
			                           {
			                                  errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDownloadDate+"\n";
			                                  errorNb++;
			                           }
			                     } 
			                     else 
			                     {
									   if ((isWhitespace(beginDownloadDate) == true) && (isWhitespace(endDownloadDate) == false)) 
									   {
										   if (isFutureDate(yearDownloadEnd, monthDownloadEnd, dayDownloadEnd) == false) 
										   {
												  errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsPostDate")%>\n";
												  errorNb++;
										   }
									   }
								 }
			                 }
			           }
			     }		
			     // les dates de visibilité
			     if (!isWhitespace(beginDate)) {
			   			if (beginDate.replace(re, "OK") != "OK") 
			   			{
			       			errorMsg+="  - '<%=resource.getString("GML.dateBegin")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
			       			errorNb++;
				   			beginDateOK = false;
			   			} 
			   			else 
			   			{
			       			if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) 
			       			{
			         			errorMsg+="  - '<%=resource.getString("GML.dateBegin")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
			         			errorNb++;
					 			beginDateOK = false;
			       			}
			   			}
	     			}
				     if (!isWhitespace(endDate)) 
				     {
				           if (endDate.replace(re, "OK") != "OK") 
				           {
				                 errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
				                 errorNb++;
				           } 
				           else 
				           {
				                 if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) 
				                 {
				                     errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
				                     errorNb++;
				                 } 
				                 else 
				                 {
				                     if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) 
				                     {
				                           if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) 
				                           {
				                                  errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDate+"\n";
				                                  errorNb++;
				                           }
				                     } 
				                     else 
				                     {
										   if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) 
										   {
											   if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) 
											   {
													  errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsPostDate")%>\n";
													  errorNb++;
											   }
										   }
									 }
				                 }
				           }
				     }				
			     
		     	// vérifier que le document est bien une image
		     	if (file != "")
		     	{
 					var verif = /[.][jpg,gif,bmp,tiff,tif,jpeg,png,JPG,GIF,BMP,TIFF,TIF,JPEG,PNG]{3,4}$/;
 					if (verif.exec(file) == null)
 					{
  						errorMsg+="  - '<%=resource.getString("gallery.photo")%>'  <%=resource.getString("gallery.format")%>\n";
	           			errorNb++;
 					}
 				}
		     	switch(errorNb) 
		     	{
		        	case 0 :
		            	result = true;
		            	break;
		        	case 1 :
		            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
		            	window.alert(errorMsg);
		            	result = false;
		            	break;
		        	default :
		            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
		            	window.alert(errorMsg);
		            	result = false;
		            	break;
		     	} 
		     	return result;
			}
		</script>
		
		</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="javascript:document.photoForm.WAIMGVAR0.focus();">
<%

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(displayPath(path));
	
	Board board	= gef.getBoard();
	
	TabbedPane tabbedPane = gef.getTabbedPane();
	if (photo != null)
		tabbedPane.addTab(resource.getString("gallery.photo"), "PreviewPhoto?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("GML.head"), "#", true, false);
	if (photo != null)
	{
		//if (XMLFormName != null && !XMLFormName.equals(""))
			//tabbedPane.addTab(resource.getString("gallery.form"), "GoToXMLForm?PhotoId="+photoId, false);
		tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?PhotoId="+photoId, false);
		if (showComments)
			tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbCom.intValue()+")", "Comments?PhotoId="+photoId, false);
		tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?PhotoId="+photoId, false);
		if (isUsePdc.booleanValue())
			tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PhotoId="+photoId, false);
	}
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    
%>
<FORM Name="photoForm" action="<%=action%>" Method="POST" ENCTYPE="multipart/form-data" accept-charset="UTF-8">

<table CELLPADDING="5" WIDTH="100%">
	<tr>
		<td class="txtlibform"> <%=resource.getString("gallery.photo")%> :</td>
      	<td><input type="file" name="WAIMGVAR0" size="60">
      		<% if (vignette_url == null) { %>
	      		<IMG src="<%=resource.getIcon("gallery.obligatoire")%>" width="5" height="5" border="0">
      		<% } %>
      	</td>
	</tr>
	<% if (vignette_url != null) { 
		String type = nameFile.substring(nameFile.lastIndexOf(".") + 1, nameFile.length());
		if ("bmp".equalsIgnoreCase(type))
			vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_66x50.jpg";
	%> 
		<tr>
			<td class="txtlibform"><%=resource.getString("gallery.vignette")%> : </td>
      		<td><IMG SRC="<%=vignette_url%>"></td>
		</tr>
		<tr>
			<td class="txtlibform"><%=resource.getString("gallery.nomFic")%> :</td>
			<td class="txtlibform"><%=nameFile%></td>
		</tr>
	<% } %>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageTitle%>" size="60" maxlength="150" value="<%=title%>">
			<input type="hidden" name="PhotoId" value="<%=photoId%>"> </td>
	</tr>
	<tr>
		<td class="txtlibform"> <%=resource.getString("GML.description")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageDescription%>" size="60" maxlength="150" value="<%=description%>" ></TD>
	</tr>
	<tr>
		<td class="txtlibform"> <%=resource.getString("GML.author")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageAuthor%>" size="60" maxlength="150" value="<%=author%>" ></TD>
	</tr>
	<tr>
		<td class="txtlibform"> <%=resource.getString("gallery.keyWord")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageKeyWord%>" size="60" maxlength="150" value="<%=keyWord%>" ></TD>
	</tr>
	<tr>
		<td class="txtlibform"> <%=resource.getString("gallery.download")%> :</td>
		<%
			String downloadCheck = "";
			if (download)
				downloadCheck = "checked";
		%>
	    <td><input type="checkbox" name="<%=ParameterNames.ImageDownload%>" value="true" <%=downloadCheck%>></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.beginDownloadDate")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageBeginDownloadDate%>" size="12" maxlength="10" value=<%=beginDownloadDate%>>&nbsp;</TD>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.endDownloadDate")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageEndDownloadDate%>" size="12" maxlength="10" value=<%=endDownloadDate%>>&nbsp;</TD>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.beginDate")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageBeginDate%>" size="12" maxlength="10" value=<%=beginDate%>>&nbsp;</TD>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.endDate")%> :</td>
		<TD><input type="text" name="<%=ParameterNames.ImageEndDate%>" size="12" maxlength="10" value=<%=endDate%>>&nbsp;</TD>
	</tr>
<!--	<tr>
		<td class="txtlibform"> <%=resource.getString("gallery.albumLabel")%> :</td>
		<%
			String albumLabelCheck = "";
			if (albumLabel)
				albumLabelCheck = "checked";
		%>
	    <td><input type="checkbox" name="AlbumLabel" value="true" <%=albumLabelCheck%>></td>
	</tr>-->
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.creationDate")%> :</td>
		<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resource.getString("gallery.par")%></span>&nbsp;<%=creatorName%></TD>
	</tr>
	<% if (updateDate != null && updateName != null) { %>
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.updateDate")%> :</td>
		<TD><%=updateDate%>&nbsp;<span class="txtlibform"><%=resource.getString("gallery.par")%></span>&nbsp;<%=updateName%></TD>
	</tr>
	<% } %>
  	<tr>
  		<td colspan="2">( <img border="0" src=<%=resource.getIcon("gallery.obligatoire")%> width="5" height="5"> : Obligatoire )</td>
  	</tr>
</table>
</form>
<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</body>
</html>