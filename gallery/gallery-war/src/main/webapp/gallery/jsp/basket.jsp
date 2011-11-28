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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp"%>
<%
  // recuperation des parametres :
    Collection photos = (List) request.getAttribute("Photos");
    Collection selectedIds = (Collection) request.getAttribute("SelectedIds");
    boolean isOrder = ((Boolean) request.getAttribute("IsOrder")).booleanValue();
      
    // declaration des variables :
    int id = 0;
    String extension = "_66x50.jpg";
    String extensionAlt = "_266x150.jpg";
      
    Iterator itP = photos.iterator();
%>

<html>
<head>
<%
  out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript"
	src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
	var albumWindow = window;
	
	function sendDataDelete() 
	{
		//confirmation de suppression des photos selectionnees
		if(window.confirm("<%=resource.getString("gallery.confirmDeletePhotos")%> "))
		{
			// envoi des photos selectionnees pour la suppression
			document.photoForm.SelectedIds.value 	= getObjects(true);
			document.photoForm.NotSelectedIds.value = getObjects(false);
			document.photoForm.action				= "BasketDeleteSelectedPhoto";
			document.photoForm.submit();
		}
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
					// il n'y a qu'une checkbox non selectionnee
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
		document.photoForm.action				= "BasketPagination";
		document.photoForm.submit();
	}
	
	function deleteConfirm(id)
	{
		// confirmation de suppression d'une photo
		if(window.confirm("<%=resource.getString("gallery.confirmDeletePhoto")%>  ?"))
		{
  			document.photoFormDelete.action = "BasketDeletePhoto";
  			document.photoFormDelete.PhotoId.value = id;
  			document.photoFormDelete.submit();
		}
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
var tipWidth= 160;
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
  <%int messagesId = 0;
          while (itP.hasNext()) {
            String photoId = (String) itP.next();
            PhotoDetail photo = gallerySC.getPhoto(photoId);
            String nomRep = resource.getSetting("imagesSubDirectory")
                + photo.getId();%>		
            messages[<%=messagesId%>] = new Array('<%=FileServerUtils.getUrl(spaceId, componentId, photo.getId()
            + extensionAlt, photo.getImageMimeType(),
            nomRep)%>','<%=EncodeHelper.javaStringToJsString(photo.getName()) %>',"#FFFFFF");
        <%messagesId++;
      }%>

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

document.write('<div id="tipDiv" style="position:absolute; visibility:hidden; z-index:100"></div>')
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">
  <%
    // creation de la barre de navigation
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(resource.getString("gallery.basket"));
      
    if (photos.size() != 0) {
      if (isOrder) {
        // transformer le panier en demande
        operationPane.addOperation(resource.getIcon("gallery.AddOrder"), resource.getString(
            "gallery.addOrder"), "OrderAdd");
        operationPane.addLine();
      }
        
      // possibilite de modifier ou supprimer les photos par lot
      operationPane.addOperation(resource.getIcon("gallery.deleteSelectedPhoto"), resource.getString(
          "gallery.deleteSelectedPhoto"),
          "javascript:onClick=sendDataDelete();");
            
      // vider le panier
      operationPane.addOperation(resource.getIcon("gallery.deleteBasket"), resource.getString(
          "gallery.deleteBasket"), "BasketDelete");
    }
      
    out.println(window.printBefore());
    out.println(frame.printBefore());
      
    // afficher les photos du panier
    // -------------------
%>
<FORM NAME="photoForm"><input type="hidden" name="SelectedIds">
<input type="hidden" name="NotSelectedIds"> <%
   if (photos.size() == 0) {
       out.println("<center>"
           + resource.getString("gallery.emptyBasket")
           + "</center>");
     } else {
       // affichage des photos dans un ArrayPane
       ArrayPane arrayPane = gef.getArrayPane("basket", "BasketView",
           request, session);
       boolean ok = false;
       itP = photos.iterator();
       if (itP.hasNext()) {
         arrayPane.addArrayColumn(resource.getString("gallery.photo"));
         ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("gallery.operation"));
         columnOp.setSortable(false);
         ok = true;
       }
       int indexPhoto = 0;
       while (itP.hasNext()) {
         ArrayLine ligne = arrayPane.addArrayLine();
           
         id = Integer.parseInt((String) itP.next());
         PhotoDetail photo = gallerySC.getPhoto(Integer.toString(id));
         String nomRep = resource.getSetting("imagesSubDirectory")
             + id;
         String name = id + extension;
         String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
         if (photo.getDescription() != null
             && photo.getDescription().length() > 0) {
           altTitle += " : "
               + EncodeHelper.javaStringToHtmlString(photo.getDescription());
         }
         String vignette_url = FileServerUtils.getUrl(spaceId, componentId, name, photo.
             getImageMimeType(), nomRep);
         String vignetteAlt = FileServerUtils.getUrl(spaceId,
             componentId, photo.getId() + extensionAlt, photo.getImageMimeType(), nomRep);
         String alt = EncodeHelper.javaStringToHtmlString("<IMG SRC=\""
             + vignetteAlt + "\" border=\"0\">");
               
         ArrayCellText arrayCellText0 = ligne.addArrayCellText("<a href=\"PreviewPhoto?PhotoId="
             + id
             + "\" onmouseover=\"doTooltip(event,"
             + indexPhoto
             + ")\" onmouseout=\"hideTip()\"><IMG SRC=\""
             + vignette_url + "\" border=\"0\"></a>");
         arrayCellText0.setCompareOn(name);
         indexPhoto++;
           
         // case e cocher pour traitement par lot
         String usedCheck = "";
         if (selectedIds != null
             && selectedIds.contains(Integer.toString(id))) {
           usedCheck = "checked";
         }
         ligne.addArrayCellText("<a href=\"#\" onclick=\"javaScript:deleteConfirm('"
             + id
             + "')\"><img src=\""
             + resource.getIcon("gallery.deleteSrc")
             + "\" alt=\""
             + resource.getString("gallery.deletePhoto")
             + "\" border=\"0\" align=\"absmiddle\"></a> <input type=\"checkbox\" name=\"SelectPhoto\" value=\""
             + EncodeHelper.javaStringToHtmlString(String.valueOf(id)) + usedCheck + "\">");
               
       }
       if (ok) {
         out.println(arrayPane.print());
       }
     }
       
     out.println(frame.printAfter());
     out.println(window.printAfter());
 %>
</FORM>

<form name="photoFormDelete" action="" Method="POST"><input
	type="hidden" name="PhotoId"> <input type="hidden" name="Name">
<input type="hidden" name="Description"></form>

<form name="favorite" action="" Method="POST"><input
	type="hidden" name="Id"></form>

</body>
</html>