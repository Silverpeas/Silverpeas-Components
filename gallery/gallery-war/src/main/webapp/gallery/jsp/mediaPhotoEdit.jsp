<%@ page import="com.silverpeas.gallery.GalleryComponentSettings" %>
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
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>
<c:set var="photo" value="${requestScope.Media}" />
<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>

<%
	// récupération des paramètres :
  Photo photo = (Photo) request.getAttribute("Media");
  List<NodeDetail> path = (List<NodeDetail>) request.getAttribute("Path");

  boolean viewMetadata = ((Boolean) request.getAttribute("IsViewMetadata")).booleanValue();

  // paramètres pour le formulaire
  Form formUpdate = (Form) request.getAttribute("Form");
  DataRecord data = (DataRecord) request.getAttribute("Data");

  // déclaration des variables :
  String photoId = "";
  String vignette_url = null;
  String action = "CreateMedia";
  String beginDownloadDate = "";
  String endDownloadDate = "";
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
    photoId = String.valueOf(photo.getMediaPK().getId());
    vignette_url = photo.getThumbnailUrl(MediaResolution.MEDIUM);
    action = "UpdateInformation";
    if (photo.getDownloadPeriod().getBeginDatable().isDefined()) {
      beginDownloadDate = resource.getInputDate(photo.getDownloadPeriod().getBeginDatable());
    } else {
      beginDownloadDate = "";
    }
    if (photo.getDownloadPeriod().getEndDatable().isDefined()) {
      endDownloadDate = resource.getInputDate(photo.getDownloadPeriod().getEndDatable());
    } else {
      endDownloadDate = "";
    }

    if (photo.getVisibilityPeriod().getBeginDatable().isDefined()) {
      beginDate = resource.getInputDate(photo.getVisibilityPeriod().getBeginDatable());
    } else {
      beginDate = "";
    }
    if (photo.getVisibilityPeriod().getEndDatable().isDefined()) {
      endDate = resource.getInputDate(photo.getVisibilityPeriod().getEndDatable());
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
  if (action.equals("UpdateInformation")) {
    cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "MediaView?MediaId=" + photoId, false);
  } else {
    cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "GoToCurrentAlbum", false);
  }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
  <view:looknfeel/>
  <link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
	<%
		if (formUpdate != null)
			formUpdate.displayScripts(out, context);
	%>
  <view:includePlugin name="datepicker"/>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script type="text/javascript">
// fonctions de contrôle des zones des formulaires avant validation
function sendData() {
	<% if (formUpdate != null) { %>
	if (isCorrectForm() && isCorrectLocalForm()) {
	<% } else { %>
	if (isCorrectLocalForm()) {
	<% } %>
	  <c:if test="${requestScope.IsUsePdc and empty photo.id}">
	    <view:pdcPositions setIn="document.mediaForm.Positions.value"/>
	  </c:if>
	  document.mediaForm.submit();
  }
}

function isCorrectLocalForm()
{
   	var errorMsg = "";
   	var errorNb = 0;
   	var title = stripInitialWhitespace(document.mediaForm.<%=ParameterNames.MediaTitle%>.value);
   	var descr = document.mediaForm.<%=ParameterNames.MediaDescription%>.value;
   	var file = stripInitialWhitespace(document.mediaForm.WAIMGVAR0.value);
   	var beginDownloadDate = document.mediaForm.<%=ParameterNames.MediaBeginDownloadDate%>.value;
   	var endDownloadDate = document.mediaForm.<%=ParameterNames.MediaEndDownloadDate%>.value;
   	var beginDate = document.mediaForm.<%=ParameterNames.MediaBeginVisibilityDate%>.value;
   	var endDate = document.mediaForm.<%=ParameterNames.MediaEndVisibilityDate%>.value;
   	var langue = "<%=resource.getLanguage()%>";
		var beginDownloadDateOK = true;
		var beginDateOK = true;

   	if (title.length > 255)
   	{
  		errorMsg+="  - '<fmt:message key="GML.title"/>'  <fmt:message key="gallery.MsgSize"/>\n";
     	errorNb++;
   	}
 		if (descr.length > 255)
   	{
   		errorMsg+="  - '<fmt:message key="GML.description"/>'  <fmt:message key="gallery.MsgSize"/>\n";
     	errorNb++;
   	}
   	if (<%=(vignette_url == null)%> && file == "") {
   		errorMsg+="  - '<fmt:message key="gallery.media"/>'  <fmt:message key="GML.MustBeFilled"/>\n";
   		errorNb++;
   	}
   	// vérifier les dates de début et de fin de période
   	// les dates de téléchargements
   	if (!isWhitespace(beginDownloadDate)) {
   		if (!isDateOK(beginDownloadDate, langue)) {
   			errorMsg+="  - '<fmt:message key="gallery.beginDownloadDate"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
   			errorNb++;
   			beginDownloadDateOK = false;
 			}
		}
     if (!isWhitespace(endDownloadDate)) {
		   if (!isDateOK(endDownloadDate, langue)) {
             errorMsg+="  - '<fmt:message key="GML.toDate"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
             errorNb++;
       } else {
         if (!isWhitespace(beginDownloadDate) && !isWhitespace(endDownloadDate)) {
					 if (beginDownloadDateOK && !isDate1AfterDate2(endDownloadDate, beginDownloadDate, langue)) {
            	errorMsg+="  - '<fmt:message key="GML.toDate"/>' <fmt:message key="GML.MustContainsPostOrEqualDateTo"/> "+beginDownloadDate+"\n";
            	errorNb++;
					 }
         } else {
				   if (isWhitespace(beginDownloadDate) && !isWhitespace(endDownloadDate)) {
						 if (!isFuture(endDownloadDate, langue)) {
							 errorMsg+="  - '<fmt:message key="GML.toDate"/>' <fmt:message key="GML.MustContainsPostDate"/>\n";
							 errorNb++;
						 }
					 }
				 }
       }
     }
     // les dates de visibilité
     if (!isWhitespace(beginDate)) {
    	 	if (!isDateOK(beginDate, langue)) {
   				errorMsg+="  - '<fmt:message key="GML.dateBegin"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
     			errorNb++;
	   			beginDateOK = false;
   			}
 			}
	     if (!isWhitespace(endDate))
	     {
	    	   if (!isDateOK(endDate, langue)) {
             errorMsg+="  - '<fmt:message key="GML.dateEnd"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
             errorNb++;
	           } else {
					if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
	                	if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, langue)) {
                    	errorMsg+="  - '<fmt:message key="GML.dateEnd"/>' <fmt:message key="GML.MustContainsPostOrEqualDateTo"/> "+beginDate+"\n";
                      errorNb++;
                    }
	                } else {
						if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
							if (!isFuture(endDate, langue)) {
								errorMsg+="  - '<fmt:message key="GML.dateEnd"/>' <fmt:message key="GML.MustContainsPostDate"/>\n";
								errorNb++;
							}
						}
					}
				}
	     }

   	// vérifier que le document est bien une image
   	if (file != "") {
   	  var verif = /[.][jpg,gif,bmp,tiff,tif,jpeg,png,JPG,GIF,BMP,TIFF,TIF,JPEG,PNG]{3,4}$/;
   	  if (verif.exec(file) == null) {
				errorMsg+="  - '<fmt:message key="gallery.media"/>'  <fmt:message key="gallery.format"/>\n";
        errorNb++;
      }
	  }

  <c:if test="${requestScope.IsUsePdc and empty photo.id}">
    <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>;
  </c:if>

   	switch(errorNb)
   	{
      	case 0 :
          	result = true;
          	break;
      	case 1 :
          	errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
          	window.alert(errorMsg);
          	result = false;
          	break;
      	default :
          	errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
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
    String nameRep = GalleryComponentSettings.getMediaFolderNamePrefix() + photo.getId();
%>
messages[0] = new Array('<%=FileServerUtils.getUrl( componentId,
    photo.getId() + extensionAlt, photo.getFileMimeType(), nameRep)%>','<%=EncodeHelper.javaStringToJsString(photo.
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
	if ((mouseX+offX+tpWd)>winWd) {
    tipcss.left = mouseX-(tpWd+offX)+"px";
	} else {
	  tipcss.left = mouseX+offX+"px";
	}
	if ((mouseY+offY+tpHt)>winHt) {
		tipcss.top = winHt-(tpHt+offY)+"px";
	} else {
	  tipcss.top = mouseY+offY+"px";
	}
	if (!tipFollowMouse) {
	  t1=setTimeout("tipcss.visibility='visible'",100);
	}
}

function hideTip() {
	if (!tooltip) return;
	t2=setTimeout("tipcss.visibility='hidden'",100);
	tipOn = false;
}
</script>

</head>
<body class="gallery createPhoto yui-skin-sam" id="<%=componentId%>">
<%

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	displayPath(path, browseBar);

	Board board	= gef.getBoard();

	TabbedPane tabbedPane = gef.getTabbedPane();
	if (photo != null)
	{
		tabbedPane.addTab(resource.getString("gallery.media"), "MediaView?MediaId="+photoId, false);
		tabbedPane.addTab(resource.getString("gallery.info"), "#", true);
		tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?MediaId="+photoId, false);
	}

	out.println(window.printBefore());
	out.println(tabbedPane.print());
%>
<view:frame>
<form name="mediaForm" action="<%=action%>" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
<input type="hidden" name="MediaId" value="<%=photoId%>"/>
<input type="hidden" name="Positions"/>

<table cellpadding="5" width="100%">
<tr>
	<td valign="top">
	<%if (photo != null) { %>
		<%if (vignette_url != null) {
			if (!photo.isPreviewable()) {
				vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_266x150.jpg";
			}
		%>
  		<center>
  		<a href="#" onmouseover="doTooltip(event,0)" onmouseout="hideTip()"><img src="<%=vignette_url%>" border="0"/></a>
  		</center>
  <%
  // AFFICHAGE des métadonnées
  if (metaDataKeys != null && !metaDataKeys.isEmpty()) {
%>
  <div class="metadata bgDegradeGris" id="metadata">
    <div class="header bgDegradeGris">
      <h4 class="clean"><fmt:message key="GML.metadata"/></h4>
    </div>
    <div id="metadata_list">
    <%
    MetaData metaData;
    for (final String propertyLong : metaDataKeys) {
      metaData = photo.getMetaData(propertyLong);
      String mdLabel = metaData.getLabel();
      String mdValue = metaData.getValue();
      if (metaData.isDate()) {
        mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
      }
      %>
    <p id="metadata_<%=mdLabel%>"><%=mdLabel%> <b><%=mdValue%></b></p>
      <%
    }
    %>
    </div>
  </div>
  <%
     }
   }
 } %>
	</td>
	<td>

  <fieldset id="photoInfo" class="skinFieldset">
    <legend><fmt:message key="GML.bloc.information.principals"/></legend>
    <div class="fields">
      <div class="field" id="fileArea">
        <label for="WAIMGVAR0" class="txtlibform"><fmt:message key="gallery.media" /></label>
        <div class="champs">
          <input id="fileId" type="file" name="WAIMGVAR0" size="60" />&nbsp;<img alt="<fmt:message key="GML.mandatory" />" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
        </div>
      </div>
      <div class="field" id="fileNameArea">
        <label for="fileName" class="txtlibform"><fmt:message key="gallery.fileName" /></label>
        <div class="champs">
          ${photo.fileName}
        </div>
      </div>
      <div class="field" id="titleArea">
        <label for="title" class="txtlibform"><fmt:message key="GML.title"/></label>
        <div class="champs">
          <c:if test="${photo.title != photo.fileName}">
            <c:set var="photoTitle" value="${photo.title}"/>
          </c:if>
          <input id="title" type="text" name="SP$$MediaTitle" size="60" maxlength="150" value="${photoTitle}"/>&nbsp;
        </div>
      </div>
      <div class="field" id="descriptionArea">
        <label for="description" class="txtlibform"><fmt:message key="GML.description"/></label>
        <div class="champs">
          <input id="description" type="text" name="SP$$MediaDescription" size="60" maxlength="150" value="<c:out value='${photo.description}'/>"/>&nbsp;
        </div>
      </div>
      <div class="field" id="authorArea">
        <label for="author" class="txtlibform"><fmt:message key="GML.author"/></label>
        <div class="champs">
          <input id="author" type="text" name="SP$$MediaAuthor" size="60" maxlength="150" value="<c:out value='${photo.author}'/>"/>&nbsp;
        </div>
      </div>
      <div class="field" id="keywordArea">
        <label for="keyword" class="txtlibform"><fmt:message key="gallery.keyword"/></label>
        <div class="champs">
          <input id="keyword" type="text" name="SP$$MediaKeyWord" size="60" maxlength="150" value="<c:out value='${photo.keyWord}'/>"/>&nbsp;
        </div>
      </div>
    </div>
  </fieldset>

  <fieldset class="skinFieldset" id="photoOptions">
    <legend><fmt:message key="gallery.options" /></legend>
    <div class="fields">
      <div class="field" id="downloadArea">
        <label for="download" class="txtlibform"><fmt:message key="gallery.download"/></label>
        <div class="champs">
          <c:set var="downloadChecked" value=""/>
          <c:if test="${photo.downloadable}">
            <c:set var="downloadChecked" value="checked=\"checked\""/>
          </c:if>
          <input id="download" type="checkbox" name="SP$$MediaDownloadAuthorized" value="true" ${downloadChecked} />
        </div>
      </div>
      <div class="field" id="beginDownloadDateArea">
        <label for="beginDownloadDate" class="txtlibform"><fmt:message key="gallery.beginDownloadDate"/></label>
        <div class="champs">
          <input id="beginDownloadDate" type="text" class="dateToPick" name="SP$$MediaBeginDownloadDate" size="12" maxlength="10" value="<%=beginDownloadDate%>"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
      <div class="field" id="endDownloadDateArea">
        <label for="endDownloadDate" class="txtlibform"><fmt:message key="GML.toDate"/></label>
        <div class="champs">
          <input id="endDownloadDate" type="text" class="dateToPick" name="SP$$MediaEndDownloadDate" size="12" maxlength="10" value="<%=endDownloadDate%>"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
      <div class="field" id="beginDateArea">
        <label for="beginDate" class="txtlibform"><fmt:message key="gallery.beginDate"/></label>
        <div class="champs">
          <input id="beginDate" type="text" class="dateToPick" name="SP$$MediaBeginVisibilityDate" size="12" maxlength="10" value="<%=beginDate%>"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
      <div class="field" id="endDateArea">
        <label for="endDate" class="txtlibform"><fmt:message key="GML.toDate"/></label>
        <div class="champs">
          <input id="endDate" type="text" class="dateToPick" name="SP$$MediaEndVisibilityDate" size="12" maxlength="10" value="<%=endDate%>"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
    </div>
  </fieldset>

  <c:if test="${requestScope.IsUsePdc}">
    <%-- Display PDC form --%>
    <c:choose>
      <c:when test="${not empty photo.id}">
        <view:pdcClassification componentId="${instanceId}" contentId="${photo.id}" editable="true" />
      </c:when>
      <c:otherwise>
        <view:pdcNewContentClassification componentId="${instanceId}"/>
      </c:otherwise>
  </c:choose>
  </c:if>

		<br/>
		<% if (formUpdate != null) { %>
			<%-- Display XML form --%>
      <fieldset id="formInfo" class="skinFieldset">
        <legend><fmt:message key="GML.bloc.further.information"/></legend>
				<%
					formUpdate.display(out, context, data);
				%>
      </fieldset>
		<% } %>
	</td>
</tr>
</table>
</form>
<%
	ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(validateButton);
  buttonPane.addButton(cancelButton);
	out.println(buttonPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
<div id="tipDiv" style="position:absolute; visibility:hidden; z-index:100000"></div>
</body>
</html>