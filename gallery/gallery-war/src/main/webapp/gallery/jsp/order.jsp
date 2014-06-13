<%@ page import="com.silverpeas.gallery.GalleryComponentSettings" %>
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
	// récupération des paramètres :
	String			profile				= (String) request.getAttribute("Profile");
	Order			order				= (Order) request.getAttribute("Order");
	Collection		selectedIds			= (Collection) request.getAttribute("SelectedIds");

	// paramètres du formulaire
	Form		xmlForm 		= (Form) request.getAttribute("XMLForm");
	DataRecord	xmlData			= (DataRecord) request.getAttribute("XMLData");


	// déclaration des variables :
	int 	photoId			= 0;
	String 	extension		= "_66x50.jpg";
	String 	extensionAlt 	= "_266x150.jpg";

	List 		rows 	= order.getRows();
	Iterator 	itP 	= (Iterator) rows.iterator();

	PagesContext 		context 	= new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
	context.setBorderPrinted(false);
%>

<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

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
<%
	int messagesId = 0;
	while (itP.hasNext())
	{
		OrderRow row = (OrderRow) itP.next();
		PhotoDetail photo = row.getPhoto();
		String nomRep = GalleryComponentSettings.getMediaFolderNamePrefix() + photo.getId();
%>
		messages[<%=messagesId%>] = new Array('<%=FileServerUtils.getUrl( componentId, photo.getId()
		    + extensionAlt, photo.getImageMimeType(), nomRep)%>',
		    '<%=EncodeHelper.javaStringToJsString(photo.getName())%>',"#FFFFFF");
<%
		messagesId++;
	}
%>

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
<script language="javascript">
	var albumWindow = window;

	function getObjects(selected)
	{
		var  items = "";
		try
		{
			var boxItems = document.orderForm.SelectPhoto;
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
		}
		catch (e)
		{
			//Checkboxes are not displayed
		}
		return items;
	}

	function doPagination(index)
	{
		document.orderForm.SelectedIds.value 	= getObjects(true);
		document.orderForm.NotSelectedIds.value = getObjects(false);
		document.orderForm.Index.value 			= index;
		document.orderForm.action				= "OrderPagination";
		document.orderForm.submit();
	}

	var orderWindow = window;

	function download(photoId)
	{
		var url = "OrderDownloadImage?PhotoId="+photoId;
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	    if (!orderWindow.closed && orderWindow.name == "orderWindow")
	        orderWindow.close();
	    orderWindow = SP_openWindow(url, "orderWindow", "740", "600", windowParams);
	}

	function updateOrder()
	{
		if (isCorrectForm())
		{
			if(window.confirm("<%=resource.getString("gallery.confirmValidOrder")%> "))
			{
				document.orderForm.action			= "OrderUpdate";
				document.orderForm.submit();
			}
		}
		else
		{
			var errorMsg = "<%=resource.getString("gallery.checkAll")%>";
			window.alert(errorMsg);
		}
	}

	function isCorrectForm()
	{
		<%
			// tableau des photoIds
			itP = (Iterator) rows.iterator();
			String elementIds = "";
			while (itP.hasNext())
			{
				OrderRow row = (OrderRow) itP.next();

				elementIds += "\"DownloadType"+row.getPhotoId()+"\"";
				if (itP.hasNext())
					elementIds += ", ";
			}
		%>

		var elementIds = new Array(<%=elementIds%>);

		var selectItem;
		var nbErrors = 0;
		for (i=0; i<elementIds.length; i++)
		{
			selectItem = document.getElementById(elementIds[i]);
			if (selectItem != null && selectItem.value == "0")
			{
				nbErrors++;
			}
		}

		if (nbErrors > 0)
			return false;
		else
			return true;
	}
</script>
</head>
<body>
<%
	// création de la barre de navigation
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	String chemin = "<a href=\"OrderViewList\">" + resource.getString("gallery.viewOrderList")+"</a>" + " > " + resource.getString("gallery.order");
	browseBar.setPath(chemin);

	int orderId = order.getOrderId();

	out.println(window.printBefore());
    out.println(frame.printBefore());

 	// déclaration des boutons
	Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:updateOrder()", false);
	Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "OrderViewList", false);
	Button returnButton	= gef.getFormButton(resource.getString("GML.back"), "OrderViewList", false);

    // entête de la demande
    // --------------------
    Board board	= gef.getBoard();

    board.printBefore();
    %>
	<table border="0" width="80%">
		<tr>
			<td class="txtlibform" nowrap><%=resource.getString("gallery.descriptionOrder")%> :</td>
			<td><%=order.getOrderId()%></td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap><%=resource.getString("gallery.orderOf")%> :</td>
			<td><%=order.getUserName()%></td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap><%=resource.getString("gallery.orderDate")%> :</td>
			<td><%=resource.getOutputDateAndHour(order.getCreationDate())%></td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap><%=resource.getString("gallery.nbRows")%> :</td>
			<td><%=order.getNbRows()%></td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap><%=resource.getString("GML.status")%> :</td>
			<%
			String processDate = resource.getOutputDateAndHour(order.getProcessDate());
			String status = resource.getString("gallery.wait");
			if (!processDate.equals(""))
		    	status = resource.getString("gallery.processDate") + processDate;
			%>
			<td><%=status%></td>
		</tr>
	</table>
	<%
	board.printAfter();

	// formulaire
	if (xmlForm != null)
	{
		%>
			<br/>

			<%=board.printBefore()%>
			<table border="0" width="50%">
			<!-- AFFICHAGE du formulaire -->
				<tr>
					<td colspan="2">
					<%
						PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
						xmlContext.setBorderPrinted(false);

				    	xmlForm.display(out, xmlContext, xmlData);
				    %>
					</td>
				</tr>
			</table>
			<%=board.printAfter()%>
			<br/>
		<% }

	// afficher la charte

	%>
		<table><tr>
			<td><input type="checkbox" checked=true disabled=true name="CheckCharte"/> </td><td><%=resource.getString("gallery.validCharte")%></td>
		</tr></table>


	<FORM NAME="orderForm" Method="POST" accept-charset="UTF-8">
	<input type="hidden" name="SelectedIds">
	<input type="hidden" name="NotSelectedIds">
	<input type="hidden" name="OrderId" value="<%=orderId%>">
	<%

  // afficher les photos
  // -------------------

  // affichage des lignes de la demande dans un ArrayPane
  ArrayPane arrayPane = gef.getArrayPane("order", "OrderViewPagin", request, session);
  arrayPane.setVisibleLineNumber(100);
  boolean viewValid = true;

  if ("admin".equals(profile)) {

    boolean ok = false;
    itP = rows.iterator();
    if (itP.hasNext()) {
      ArrayColumn columnOp0 = arrayPane.addArrayColumn(resource.getString("gallery.photo"));
      columnOp0.setSortable(false);
      ArrayColumn columnOp1 = arrayPane.addArrayColumn(resource.getString("gallery.choiceDownload"));
      columnOp1.setSortable(false);
      ok = true;
    }
    int indexPhoto = 0;
    while (itP.hasNext()) {
      ArrayLine ligne = arrayPane.addArrayLine();

      OrderRow row = (OrderRow) itP.next();
      photoId = row.getPhotoId();

      String download = row.getDownloadDecision();

      String nomRep = GalleryComponentSettings.getMediaFolderNamePrefix() + photoId;
      String name = photoId + extension;
      PhotoDetail photo = row.getPhoto();
      String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
      if (StringUtil.isDefined(photo.getDescription())) {
        altTitle += " : " + EncodeHelper.javaStringToHtmlString(photo.getDescription());
      }
      String vignette_url = FileServerUtils.getUrl(componentId, name, photo.
          getImageMimeType(), nomRep);

      ArrayCellText arrayCellText0 = ligne.addArrayCellText(
          "<a href=\"PreviewPhoto?PhotoId=" + photoId + "\" onmouseover=\"doTooltip(event," + indexPhoto + ")\" onmouseout=\"hideTip()\"><IMG SRC=\"" + vignette_url + "\" border=\"0\"></a>");
      arrayCellText0.setCompareOn(name);
      indexPhoto++;


      // colonne des choix de téléchargement
      String choix = "";
      if ("T".equals(download)) {
        // la photo a été téléchargée
        Date dateDownload = row.getDownloadDate();

        choix = resource.getString("gallery.downloadDate") + resource.getOutputDateAndHour(
            dateDownload);
        viewValid = false;
      } else {
        if (order.getProcessUserId() != -1) {
          // la demande est déjà traitée
          if (("R").equals(download)) {
            // la photo a été refusée
            choix = resource.getString("gallery.refused");
          } else if (("D").equals(download)) {
            // la photo est autorisée en téléchargement
            choix = resource.getString("gallery.downloadOk");
          } else if (("DW").equals(download)) {
            // la photo est autorisée en téléchargement avec le watermark
            choix = resource.getString("gallery.downloadWithWatermark");
          }
          viewValid = false;
        } else {
          choix = "<select name=\"DownloadType" + photoId + "\" id=\"DownloadType" + photoId + "\" onChange=\"javascript:downloadGoTo(this.selectedIndex);\">";
          choix = choix + "<option value=\"0\" selected>" + resource.getString(
              "gallery.choiceDownload") + "</option>";
          choix = choix + "<option value=\"0\">-------------------------------</option>";
          String selected = "";
          if ("R".equals(download)) {
            selected = "selected";
          }
          choix = choix + "<option value=\"R\" " + selected + ">" + resource.getString(
              "gallery.refused") + "</option>";
          selected = "";
          if ("D".equals(download)) {
            selected = "selected";
          }
          choix = choix + "<option value=\"D\" " + selected + ">" + resource.getString(
              "gallery.downloadOk") + "</option>";
          selected = "";
          if ("DW".equals(download)) {
            selected = "selected";
          }
          choix = choix + "<option value=\"DW\" " + selected + ">" + resource.getString(
              "gallery.downloadWithWatermark") + "</option>";
          choix = choix + "</select>";
        }
      }
      ligne.addArrayCellText(choix);
    }
    if (ok) {
      out.println(arrayPane.print());
    }
  } else {
    boolean ok = false;
    itP = rows.iterator();
    if (itP.hasNext()) {
      ArrayColumn columnOp0 = arrayPane.addArrayColumn(resource.getString("gallery.photo"));
      columnOp0.setSortable(false);
      ArrayColumn columnOp1 = arrayPane.addArrayColumn(resource.getString("gallery.downloadDate"));
      columnOp1.setSortable(false);
      ok = true;
    }
    int indexPhoto = 0;
    while (itP.hasNext()) {
      ArrayLine ligne = arrayPane.addArrayLine();

      OrderRow row = (OrderRow) itP.next();
      photoId = row.getPhotoId();
      String nomRep = GalleryComponentSettings.getMediaFolderNamePrefix() + photoId;
      String name = photoId + extension;
      PhotoDetail photo = row.getPhoto();
      String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
      if (StringUtil.isDefined(photo.getDescription())) {
        altTitle += " : " + EncodeHelper.javaStringToHtmlString(photo.getDescription());
      }
      String vignette_url = FileServerUtils.getUrl(componentId, name, photo.getImageMimeType(),
          nomRep);

      ArrayCellText arrayCellText0 = ligne.addArrayCellText(
          "<a href=\"PreviewPhoto?PhotoId=" + photoId + "\" onmouseover=\"doTooltip(event," + indexPhoto + ")\" onmouseout=\"hideTip()\"><IMG SRC=\"" + vignette_url + "\" border=\"0\"></a>");
      arrayCellText0.setCompareOn(name);
      indexPhoto++;

      // SECOND TELECHARGEMENT AVEC MISE A JOUR
      // traitement du téléchargement
      String download = resource.getString("gallery.wait");

      // rechercher l'état de la photo
      String downloadDecision = row.getDownloadDecision();
      if (("R").equals(downloadDecision)) {
        // la photo a été refusée
        download = resource.getString("gallery.refused");
      } else if (("D").equals(downloadDecision)) {
        // la photo est autorisée en téléchargement
        download = "<a href=\"OrderDownloadImage?PhotoId=" + photoId + "&OrderId=" + orderId + "\" target=_blank>" + EncodeHelper.
            javaStringToHtmlString(resource.getString("gallery.telecharger")) + "</a>";
      } else if (("DW").equals(downloadDecision)) {
        // la photo est autorisée en téléchargement avec le watermark
        download = "<a href=\"OrderDownloadImage?PhotoId=" + photoId + "&OrderId=" + orderId + "\" target=_blank>" + EncodeHelper.
            javaStringToHtmlString(resource.getString("gallery.telecharger")) + "</a>";
      } else if (("T").equals(downloadDecision)) {
        // la photo est déjà téléchargée
        Date dateDownload = row.getDownloadDate();
        if (dateDownload != null) {
          download = resource.getString("gallery.downloadDate") + resource.getOutputDateAndHour(
              dateDownload);
        }
      }
      ligne.addArrayCellText(download);
    }
    if (ok) {
      out.println(arrayPane.print());
    }
  }


  ButtonPane buttonPane = gef.getButtonPane();

  if ("admin".equals(profile)) {
    if (viewValid) {
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
    } else {
      buttonPane.addButton(returnButton);
    }
  } else {
    buttonPane.addButton(returnButton);
  }

  out.println("<br/><center>" + buttonPane.print() + "</center><br/>");

  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</FORM>

</body>
</html>