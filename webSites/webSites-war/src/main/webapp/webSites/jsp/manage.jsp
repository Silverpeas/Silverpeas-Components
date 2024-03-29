<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%
String role 		= (String) request.getAttribute("BestRole");

//Icons
String bookmark			= m_context+"/util/icons/create-action/add-bookmark.png";
String bookmarkDelete	= m_context+"/util/icons/bookmark_to_remove.gif";

String upload			= m_context+"/util/icons/create-action/download-website.png";
String create			= m_context+"/util/icons/create-action/create-website.png";
String belpou 			= m_context+"/util/icons/webSites_to_del.gif";

String webLink			= m_context+"/util/icons/webLink.gif";
String localLink		= m_context+"/util/icons/computer.gif";
String check			= m_context+"/util/icons/ok.gif";
String pdcUtilizationSrc = m_context+"/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";

Collection listeSites = (Collection) request.getAttribute("ListSites");
%>

<view:sp-page>
<view:sp-head-part>
<script type="text/javascript">

function URLENCODE(URL){
	return encodeURIComponent(URL);
}

    /*************************************************/

function B_SPECIFIER_BOOK_ONCLICK() {
    location.href="descBookmark.jsp";
}

    /*************************************************/

function B_SPECIFIER_UPLOAD_ONCLICK() {
    document.descriptionSite.submit();
}

    /*************************************************/

function B_SPECIFIER_DESIGN_ONCLICK() {
    location.href="descDesign.jsp";
}

    /*************************************************/

function deleteWebSites(nbSite) {

    listeSite = "";

    if (nbSite > 0) {
      if (nbSite == 1) {
            if (document.liste_liens.supLien.checked)
                    listeSite += document.liste_liens.supLien.value + ",";
      }

      else {
        for (i=0; i<nbSite; i++) {
            if (document.liste_liens.supLien[i] != null) {
                if (document.liste_liens.supLien[i].checked)
                    listeSite += document.liste_liens.supLien[i].value + ",";
            }
            else break;
        }
      }


      if (listeSite != "") {   //on a coch� au - un site
        var label = "<%=resources.getString("MessageSuppressionLien")%>";
        jQuery.popup.confirm(label, function() {
          document.liste_liens.Action.value = "deleteWebSites";
          document.liste_liens.SiteList.value = listeSite;
          document.liste_liens.submit();
        });
      }
    }
}

    /*************************************************/

function updateDescription (id) {
        location.href = "modifDesc.jsp?Id="+id;
}

    /*************************************************/

    /* modification des contenus et de l'arborescence du site crees et uploades */
function designSite(path, id) {
    location.href = "design.jsp?Action=design&Path="+URLENCODE(path)+"&Id="+id;
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</view:sp-head-part>
<view:sp-body-part>

<form name="descriptionSite" action="descUpload.jsp" method="post" enctype="multipart/form-data">
  <input type="hidden" name="Action"/>
</form>


<form name="liste_liens" action="manage.jsp" >
  <input type="hidden" name="Action"/>
  <input type="hidden" name="SiteList"/>

<%

  ArrayList arraySites = new ArrayList(listeSites);

  Window window = gef.getWindow();
  String bodyPart="";
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "manage.jsp");

  //Les operations
  OperationPane operationPane = window.getOperationPane();

	if (scc.isPdcUsed() && "Admin".equals(role)) {
		operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
		operationPane.addLine();
	}
  operationPane.addOperationOfCreation(bookmark, resources.getString("BookmarkSite"), "javascript:onClick=B_SPECIFIER_BOOK_ONCLICK();");
  if (bookmarkMode) {
	operationPane.addOperation(bookmarkDelete, resources.getString("SupprimerSite"), "javascript:onClick=deleteWebSites('"+listeSites.size()+"')");
  } else {
		operationPane.addOperationOfCreation(upload, resources.getString("UploadSite"), "javascript:onClick=B_SPECIFIER_UPLOAD_ONCLICK();");
		operationPane.addOperationOfCreation(create, resources.getString("DesignSite"), "javascript:onClick=B_SPECIFIER_DESIGN_ONCLICK();");
		operationPane.addOperation(belpou, resources.getString("SupprimerSite"), "javascript:onClick=deleteWebSites('"+listeSites.size()+"')");
	}

  out.println(window.printBefore());

  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resources.getString("Consulter"), "Main", false);
  tabbedPane.addTab(resources.getString("Organiser"), "organize.jsp", false);
  tabbedPane.addTab(resources.getString("GML.management"), "manage.jsp", true);

  out.println(tabbedPane.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
  //Le tableau de tri
  ArrayPane arrayPane = gef.getArrayPane("foldersList", "manage.jsp", request, session);
	arrayPane.setVisibleLineNumber(10);
	arrayPane.setTitle(resources.getString("ListeSites"));
	arrayPane.addArrayColumn(resources.getString("GML.name"));
	arrayPane.addArrayColumn(resources.getString("GML.description"));
	if (! bookmarkMode) {
		ArrayColumn arrayColumnType = arrayPane.addArrayColumn(resources.getString("GML.type"));
		arrayColumnType.setSortable(false);
	}
	ArrayColumn arrayColumnStatus = arrayPane.addArrayColumn(resources.getString("GML.status"));
	arrayColumnStatus.setSortable(false);
	ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;");
	arrayColumnDel.setSortable(false);

	SiteDetail site;
	int i=0;
	while (i < arraySites.size()) {
		site = (SiteDetail) arraySites.get(i);
		/* ecriture des lignes du tableau */
		String theId = site.getSitePK().getId();
		String nom = site.getName();
		String theDescription = site.getDescription();
		if (theDescription == null)
			theDescription = "";

		int theType = site.getSiteType();
		int theEtat = site.getState();
		ArrayLine arrayLine = arrayPane.addArrayLine();

		//nom
		if (nom.length() > 40)
			  nom = nom.substring(0, 40) + "...";
		if (theType == 1) {// site externe
			arrayLine.addArrayCellLink(nom, "javascript:onClick=updateDescription('"+theId+"')");
		}
		else {//site interne
			arrayLine.addArrayCellLink(nom, "javascript:onClick=designSite('"+componentId+"/"+theId+"', '"+theId+"')");
		}

		//desc
		if (theDescription.length() > 80)
			  theDescription = theDescription.substring(0, 80) + "...";
		arrayLine.addArrayCellText(theDescription);

		//type
		if (! bookmarkMode) {
			if (theType == 1) {// site externe
				IconPane iconPaneType2 = gef.getIconPane();
				Icon typeIcon2 = iconPaneType2.addIcon();
				typeIcon2.setProperties(webLink,resources.getString("SiteExterne"));
				arrayLine.addArrayCellIconPane(iconPaneType2);
			}
			else {//site interne
				IconPane iconPaneType1 = gef.getIconPane();
				Icon typeIcon1 = iconPaneType1.addIcon();
				typeIcon1.setProperties(localLink,resources.getString("SiteLocal"));
				arrayLine.addArrayCellIconPane(iconPaneType1);
			}
		}

		//etat
		if (theEtat == 1) {//Ajout de l'icones publi�
			IconPane iconPanePub1 = gef.getIconPane();
			Icon checkIcon1 = iconPanePub1.addIcon();
			checkIcon1.setProperties(check,resources.getString("SitePublie"));
			arrayLine.addArrayCellIconPane(iconPanePub1);
		}
		else {
			arrayLine.addArrayCellText("");
		}

		//check
		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"supLien\" value=\""+theId+"\"/>");

		i++;
  }

	out.println(arrayPane.print());
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
</form>
</view:sp-body-part>
</view:sp-page>