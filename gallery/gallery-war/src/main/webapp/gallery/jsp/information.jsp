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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<% 
	// récupération des paramètres :
      PhotoDetail photo = (PhotoDetail) request.getAttribute("Photo");
      String repertoire = (String) request.getAttribute("Repertoire");
      List<NodeDetail> path = (List) request.getAttribute("Path");
      String userName = (String) request.getAttribute("UserName");
        
      boolean viewMetadata = ((Boolean) request.getAttribute("IsViewMetadata")).booleanValue();
        
      Integer nbCom = (Integer) request.getAttribute("NbComments");
      Boolean isUsePdc = (Boolean) request.getAttribute("IsUsePdc");
      boolean showComments = ((Boolean) request.getAttribute("ShowCommentsTab")).booleanValue();
        
      // paramètres pour le formulaire
      Form formUpdate = (Form) request.getAttribute("Form");
      DataRecord data = (DataRecord) request.getAttribute("Data");
        
      // déclaration des variables :
      String photoId = "";
      String title = "";
      String description = "";
      String author = "";
      boolean download = false;
      String nomRep = "";
      String vignette_url = null;
      String action = "CreatePhoto";
      String chemin = "";
      String creationDate = resource.getOutputDate(new Date());
      String updateDate = null;
      String nameFile = "";
      String name = "";
      String creatorName = userName;
      String updateName = "";
      String beginDownloadDate = "";
      String endDownloadDate = "";
      String keyWord = "";
      String beginDate = "";
      String endDate = "";
      Collection<String> metaDataKeys = null;
        
      String extensionAlt = "_preview.jpg";
        
      PagesContext context = new PagesContext("myForm", "0", resource.getLanguage(), false,
          componentId, null);
      context.setBorderPrinted(false);
      context.setCurrentFieldIndex("11");
      context.setIgnoreDefaultValues(true);
        
      if (photo != null) {
        photoId = String.valueOf(photo.getPhotoPK().getId());
        title = photo.getTitle();
        description = photo.getDescription();
        if (description == null) {
          description = "";
        }
        author = photo.getAuthor();
        if (author == null) {
          author = "";
        }
        download = photo.isDownload();
        nomRep = repertoire;
        nameFile = photo.getImageName();
        name = photo.getId() + "_266x150.jpg";
        vignette_url = FileServerUtils.getUrl(spaceId, componentId, name, photo.getImageMimeType(),
            nomRep);
        action = "UpdateInformation";
        creationDate = resource.getOutputDate(photo.getCreationDate());
        updateDate = resource.getOutputDate(photo.getUpdateDate());
        creatorName = photo.getCreatorName();
        updateName = photo.getUpdateName();
        if (photo.getBeginDownloadDate() != null) {
          beginDownloadDate = resource.getInputDate(photo.getBeginDownloadDate());
        } else {
          beginDownloadDate = "";
        }
        if (photo.getEndDownloadDate() != null) {
          endDownloadDate = resource.getInputDate(photo.getEndDownloadDate());
        } else {
          endDownloadDate = "";
        }
          
        if (title.equals(nameFile)) {
          title = "";
        }
        keyWord = photo.getKeyWord();
        if (keyWord == null) {
          keyWord = "";
        }
        if (photo.getBeginDate() != null) {
          beginDate = resource.getInputDate(photo.getBeginDate());
        } else {
          beginDate = "";
        }
        if (photo.getEndDate() != null) {
          endDate = resource.getInputDate(photo.getEndDate());
        } else {
          endDate = "";
        }
        if (viewMetadata) {
          metaDataKeys = photo.getMetaDataProperties();
        }
      }
        
      // déclaration des boutons
      Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
      Button cancelButton;
      if (action == "UpdateInformation") {
        cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "PreviewPhoto?PhotoId=" + photoId, false);
      } else {
        cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "GoToCurrentAlbum", false);
      }
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
		<head>
          <view:looknfeel/>
		<%
			if (formUpdate != null)
				formUpdate.displayScripts(out, context); 
		%>
        <view:includePlugin name="datepicker"/>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
		<script type="text/javascript">
		
		// fonctions de contrôle des zones des formulaires avant validation
			function sendData() 
			{
				<% if (formUpdate != null) { %>
					if (isCorrectForm() && isCorrectLocalForm())
					{
		        		document.photoForm.submit();
		    		}
		    	<% } else { %>
			    	if (isCorrectLocalForm())
					{
		       			document.photoForm.submit();
		    		}
		    	<% } %>
			}
		
			function isCorrectLocalForm() 
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
		     	if (!isWhitespace(beginDownloadDate)) {
		     		if (!isDateOK(beginDownloadDate, langue)) {
		       			errorMsg+="  - '<%=resource.getString("gallery.beginDownloadDate")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
		       			errorNb++;
			   			beginDownloadDateOK = false;
		   			}
     			}
			     if (!isWhitespace(endDownloadDate)) {
					   if (!isDateOK(endDownloadDate, langue)) {
			                 errorMsg+="  - '<%=resource.getString("gallery.endDownloadDate")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
			                 errorNb++;
			           } else {
							if (!isWhitespace(beginDownloadDate) && !isWhitespace(endDownloadDate)) {
								if (beginDownloadDateOK && !isDate1AfterDate2(endDownloadDate, beginDownloadDate, langue)) {
		                        	errorMsg+="  - '<%=resource.getString("gallery.endDownloadDate")%>' <%=resource.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDownloadDate+"\n";
									errorNb++;
								}
		                    } else {
								if (isWhitespace(beginDownloadDate) && !isWhitespace(endDownloadDate)) {
									if (!isFuture(endDownloadDate, langue)) {
										errorMsg+="  - '<%=resource.getString("gallery.endDownloadDate")%>' <%=resource.getString("GML.MustContainsPostDate")%>\n";
										errorNb++;
									}
								}
							 }
			           }
			     }
			     // les dates de visibilité
			     if (!isWhitespace(beginDate)) {
			    	 	if (!isDateOK(beginDate, langue)) {
			   				errorMsg+="  - '<%=resource.getString("GML.dateBegin")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
			       			errorNb++;
				   			beginDateOK = false;
			   			}
	     			}
				     if (!isWhitespace(endDate)) 
				     {
				    	   if (!isDateOK(endDate, langue)) { 
				                 errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
				                 errorNb++;
				           } else {
								if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
				                	if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, langue)) {
				                    	errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDate+"\n";
				                        errorNb++;
				                    }
				                } else {
									if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
										if (!isFuture(endDate, langue)) {
											errorMsg+="  - '<%=resource.getString("GML.dateEnd")%>' <%=resource.getString("GML.MustContainsPostDate")%>\n";
											errorNb++;
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
	
/***********************************************
* Image w/ description tooltip- By Dynamic Web Coding (www.dyn-web.com)
* Copyright 2002-2007 by Sharon Paine
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

/* IMPORTANT: Put script after tooltip div or 
	 put tooltip div just before </BODY>. */

var dom = (document.getElementById) ? true : false;
var ns5 = (!document.all && dom || window.opera) ? true: false;
var ie5 = ((navigator.userAgent.indexOf("MSIE")>-1) && dom) ? true : false;
var ie4 = (document.all && !dom) ? true : false;
var nodyn = (!ns5 && !ie4 && !ie5 && !dom) ? true : false;

var origWidth, origHeight;

// avoid error of passing event object in older browsers
if (nodyn) { event = "nope" }

///////////////////////  CUSTOMIZE HERE   ////////////////////
// settings for tooltip 
// Do you want tip to move when mouse moves over link?
var tipFollowMouse= false;	
// Be sure to set tipWidth wide enough for widest image
var tipWidth= 400;
var offX= 20;	// how far from mouse to show tip
var offY= 12; 
var tipFontFamily= "Verdana, arial, helvetica, sans-serif";
var tipFontSize= "8pt";
// set default text color and background color for tooltip here
// individual tooltips can have their own (set in messages arrays)
// but don't have to
var tipFontColor= "#000000";
var tipBgColor= "#DDECFF"; 
var tipBorderColor= "#000000";
var tipBorderWidth= 1;
var tipBorderStyle= "solid";
var tipPadding= 0;

// tooltip content goes here (image, description, optional bgColor, optional textcolor)
var messages = new Array();
// multi-dimensional arrays containing: 
// image and text for tooltip
// optional: bgColor and color to be sent to tooltip
<%
if (photo != null) {
    String nameRep = resource.getSetting("imagesSubDirectory") + photo.getId();
%>		
messages[0] = new Array('<%=FileServerUtils.getUrl(spaceId, componentId,
    photo.getId() + extensionAlt, photo.getImageMimeType(), nameRep)%>','<%=EncodeHelper.javaStringToJsString(photo.
    getName())%>',"#FFFFFF");
  
<% } %>

////////////////////  END OF CUSTOMIZATION AREA  ///////////////////

// preload images that are to appear in tooltip
// from arrays above
if (document.images) {
	var theImgs = new Array();
	for (var i=0; i<messages.length; i++) {
  	theImgs[i] = new Image();
		theImgs[i].src = messages[i][0];
  }
}

// to layout image and text, 2-row table, image centered in top cell
// these go in var tip in doTooltip function
// startStr goes before image, midStr goes between image and text
var startStr = '<table><tr><td align="center" width="100%"><img src="';
var midStr = '" border="0"></td></tr><tr><td valign="top" align="center">';
var endStr = '</td></tr></table>';

////////////////////////////////////////////////////////////
//  initTip	- initialization for tooltip.
//		Global variables for tooltip. 
//		Set styles
//		Set up mousemove capture if tipFollowMouse set true.
////////////////////////////////////////////////////////////
var tooltip, tipcss;
function initTip() {
	if (nodyn) return;
	tooltip = (ie4)? document.all['tipDiv']: (ie5||ns5)? document.getElementById('tipDiv'): null;
	tipcss = tooltip.style;
	if (ie4||ie5||ns5) {	// ns4 would lose all this on rewrites
		//tipcss.width = tipWidth+"px";
		tipcss.fontFamily = tipFontFamily;
		tipcss.fontSize = tipFontSize;
		tipcss.color = tipFontColor;
		tipcss.backgroundColor = tipBgColor;
		tipcss.borderColor = tipBorderColor;
		tipcss.borderWidth = tipBorderWidth+"px";
		tipcss.padding = tipPadding+"px";
		tipcss.borderStyle = tipBorderStyle;
	}
	if (tooltip&&tipFollowMouse) {
		document.onmousemove = trackMouse;
	}
}

window.onload = initTip;

/////////////////////////////////////////////////
//  doTooltip function
//			Assembles content for tooltip and writes 
//			it to tipDiv
/////////////////////////////////////////////////
var t1,t2;	// for setTimeouts
var tipOn = false;	// check if over tooltip link
function doTooltip(evt,num) {
	if (!tooltip) return;
	if (t1) clearTimeout(t1);	if (t2) clearTimeout(t2);
	tipOn = true;
	// set colors if included in messages array
	if (messages[num][2])	var curBgColor = messages[num][2];
	else curBgColor = tipBgColor;
	if (messages[num][3])	var curFontColor = messages[num][3];
	else curFontColor = tipFontColor;
	if (ie4||ie5||ns5) {
		var tip = startStr + messages[num][0] + midStr + '<span style="font-family:' + tipFontFamily + '; font-size:' + tipFontSize + '; color:' + curFontColor + ';">' + messages[num][1] + '</span>' + endStr;
		tipcss.backgroundColor = curBgColor;
	 	tooltip.innerHTML = tip;
	}
	if (!tipFollowMouse) positionTip(evt);
	else t1=setTimeout("tipcss.visibility='visible'",100);
}

var mouseX, mouseY;
function trackMouse(evt) {
	standardbody=(document.compatMode=="CSS1Compat")? document.documentElement : document.body //create reference to common "body" across doctypes
	mouseX = (ns5)? evt.pageX: window.event.clientX + standardbody.scrollLeft;
	mouseY = (ns5)? evt.pageY: window.event.clientY + standardbody.scrollTop;
	if (tipOn) positionTip(evt);
}

/////////////////////////////////////////////////////////////
//  positionTip function
//		If tipFollowMouse set false, so trackMouse function
//		not being used, get position of mouseover event.
//		Calculations use mouseover event position, 
//		offset amounts and tooltip width to position
//		tooltip within window.
/////////////////////////////////////////////////////////////
function positionTip(evt) {
	if (!tipFollowMouse) {
		standardbody=(document.compatMode=="CSS1Compat")? document.documentElement : document.body
		mouseX = (ns5)? evt.pageX: window.event.clientX + standardbody.scrollLeft;
		mouseY = (ns5)? evt.pageY: window.event.clientY + standardbody.scrollTop;
	}
	// tooltip width and height
	var tpWd = (ie4||ie5)? tooltip.clientWidth: tooltip.offsetWidth;
	var tpHt = (ie4||ie5)? tooltip.clientHeight: tooltip.offsetHeight;
	// document area in view (subtract scrollbar width for ns)
	var winWd = (ns5)? window.innerWidth-20+window.pageXOffset: standardbody.clientWidth+standardbody.scrollLeft;
	var winHt = (ns5)? window.innerHeight-20+window.pageYOffset: standardbody.clientHeight+standardbody.scrollTop;
	// check mouse position against tip and window dimensions
	// and position the tooltip 
	if ((mouseX+offX+tpWd)>winWd) 
		tipcss.left = mouseX-(tpWd+offX)+"px";
	else tipcss.left = mouseX+offX+"px";
	if ((mouseY+offY+tpHt)>winHt) 
		tipcss.top = winHt-(tpHt+offY)+"px";
	else tipcss.top = mouseY+offY+"px";
	if (!tipFollowMouse) t1=setTimeout("tipcss.visibility='visible'",100);
}

function hideTip() {
	if (!tooltip) return;
	t2=setTimeout("tipcss.visibility='hidden'",100);
	tipOn = false;
}
</script>
		
</head>
<body class="yui-skin-sam">
<%

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	displayPath(path, browseBar);
	
	Board board	= gef.getBoard();
	
	TabbedPane tabbedPane = gef.getTabbedPane();
	if (photo != null)
	{
		tabbedPane.addTab(resource.getString("gallery.photo"), "PreviewPhoto?PhotoId="+photoId, false);
		tabbedPane.addTab(resource.getString("gallery.info"), "#", true, false);
		if (showComments)
			tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbCom.intValue()+")", "Comments?PhotoId="+photoId, false);
		tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?PhotoId="+photoId, false);
		if (isUsePdc.booleanValue())
			tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PhotoId="+photoId, false);
	}
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    
    //out.println(board.printBefore());
    
%>
<FORM Name="photoForm" action="<%=action%>" Method="POST" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
<table CELLPADDING="5" WIDTH="100%">
<tr> 
	<td valign="top"> 
	<%if (photo != null) { %>
		<%=board.printBefore()%>
		<%if (vignette_url != null) { 
			
			String type = nameFile.substring(nameFile.lastIndexOf(".") + 1, nameFile.length());
			if ("bmp".equalsIgnoreCase(type))
				vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_266x150.jpg";
		%> 
			
      		<center>
      		<a href="#" onmouseover="doTooltip(event,0)" onmouseout="hideTip()"><img src="<%=vignette_url%>" border="0"/></a>
      		</center>
      		
      	<%
				// AFFICHAGE des métadonnées
				if (metaDataKeys != null && !metaDataKeys.isEmpty()) 
				{
					%>
					<br/>
					<table align="left" border="0" CELLPADDING="5">
					<%
					MetaData metaData;
					for (final String propertyLong : metaDataKeys)
					{
						// extraire le nom de la propertie
						metaData = photo.getMetaData(propertyLong);
						String mdLabel = metaData.getLabel();
						String mdValue = metaData.getValue();
						if (metaData.isDate()) {
							mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
						}
						// affichage
						%>
						<tr align="left">
							<td class="txtlibform" nowrap valign="top"><%=mdLabel%> :</td>
							<td><%=mdValue%></td>
						</tr>
						<%
					}
					%>
					</table>
					<%
				}
		}
      	%>
		<%=board.printAfter()%>
		<% } %>
	</td>
	<td> 
		<%=board.printBefore()%>		
		<table align="left">
			<tr align="left">
				<td class="txtlibform"> <%=resource.getString("gallery.photo")%> :</td>
		      	<td><input type="file" name="WAIMGVAR0" size="60">
		      		<% if (vignette_url == null) { %>
			      		<IMG src="<%=resource.getIcon("gallery.obligatoire")%>" width="5" height="5" border="0">
		      		<% } %>
		      	</td>
			</tr> 			
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("gallery.nomFic")%> :</td>
				<td class="txtlibform"><%=nameFile%></td>
			</tr>
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
				<TD><input type="text" name="<%=ParameterNames.ImageTitle%>" size="60" maxlength="150" value="<%=EncodeHelper.javaStringToHtmlString(title)%>">
					<input type="hidden" name="PhotoId" value="<%=photoId%>"> </td>
			</tr>
			<tr align="left">
				<td class="txtlibform"> <%=resource.getString("GML.description")%> :</td>
				<TD><input type="text" name="<%=ParameterNames.ImageDescription%>" size="60" maxlength="150" value="<%=EncodeHelper.javaStringToHtmlString(description)%>" ></TD>
			</tr>
			<tr align="left">
				<td class="txtlibform"> <%=resource.getString("GML.author")%> :</td>
				<TD><input type="text" name="<%=ParameterNames.ImageAuthor%>" size="60" maxlength="150" value="<%=EncodeHelper.javaStringToHtmlString(author)%>" ></TD>
			</tr>
			<tr align="left">
				<td class="txtlibform"> <%=resource.getString("gallery.keyWord")%> :</td>
				<TD><input type="text" name="<%=ParameterNames.ImageKeyWord%>" size="60" maxlength="150" value="<%=EncodeHelper.javaStringToHtmlString(keyWord)%>" ></TD>
			</tr>
			<tr align="left">
				<td class="txtlibform"> <%=resource.getString("gallery.download")%> :</td>
				<%
					String downloadCheck = "";
					if (download)
						downloadCheck = "checked";
				%>
			    <td><input type="checkbox" name="<%=ParameterNames.ImageDownload%>" value="true" <%=downloadCheck%>></td>
			</tr>
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("gallery.beginDownloadDate")%> :</td>
				<td><input type="text" class="dateToPick" name="<%=ParameterNames.ImageBeginDownloadDate%>" size="12" maxlength="10" value="<%=beginDownloadDate%>"/></td>
			</tr>
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("gallery.endDownloadDate")%> :</td>
				<td><input type="text" class="dateToPick" name="<%=ParameterNames.ImageEndDownloadDate%>" size="12" maxlength="10" value="<%=endDownloadDate%>"/></td>
			</tr>
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("gallery.beginDate")%> :</td>
				<td><input type="text" class="dateToPick" name="<%=ParameterNames.ImageBeginDate%>" size="12" maxlength="10" value="<%=beginDate%>"/></td>
			</tr>
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("gallery.endDate")%> :</td>
				<td><input type="text" class="dateToPick" name="<%=ParameterNames.ImageEndDate%>" size="12" maxlength="10" value="<%=endDate%>"/></td>
			</tr>
			<tr align="left">
				<td class="txtlibform"><%=resource.getString("gallery.creationDate")%> :</td>
				<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resource.getString("gallery.par")%></span>&nbsp;<%=creatorName%></TD>
			</tr>
			<% if (updateDate != null && updateName != null) { %>
				<tr align="left">
					<td class="txtlibform"><%=resource.getString("gallery.updateDate")%> :</td>
					<TD><%=updateDate%>&nbsp;<span class="txtlibform"><%=resource.getString("gallery.par")%></span>&nbsp;<%=updateName%></TD>
				</tr>
			<% } %>
		</table>
		<%=board.printAfter()%>
		<br/>
			<% if (formUpdate != null) { %>
	  				<%=board.printBefore()%>
					<!-- AFFICHAGE du formulaire -->
					<table>
					<tr align="left">
						<td>
							<% 
								formUpdate.display(out, context, data); 
							%>
						</td>	
					</tr>
					</table>
					<%=board.printAfter()%>
			<% } %>	
	</td>
</tr>
</table>	
</form>
<% 
	//out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<div id="tipDiv" style="position:absolute; visibility:hidden; z-index:100000"></div>
</body>
</html>