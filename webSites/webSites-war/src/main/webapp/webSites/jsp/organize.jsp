<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%
String rootId = "0";
String linkedPathString = "";
String pathString = "";

String addFolder=m_context+"/util/icons/create-action/add-folder.png";
String addSite=m_context+"/util/icons/create-action/add-website-to-topic.png";
String belpou=m_context+"/util/icons/webSites_topic_to_trash.gif";

if (bookmarkMode)
{
	addSite	= m_context+"/util/icons/create-action/add-bookmark-to-topic.png";
	belpou	= m_context+"/util/icons/bookmark_topic_to_trash.gif";
}

String folderUpdate=m_context+"/util/icons/update.gif";

String upIconSrc=m_context+"/util/icons/arrow/arrowUp.gif";
String downIconSrc=m_context+"/util/icons/arrow/arrowDown.gif";

String action = request.getParameter("Action");
String id = request.getParameter("Id");

FolderDetail webSitesCurrentFolder = (FolderDetail) request.getAttribute("CurrentFolder");

//Mise a jour de l'espace
if (action == null) {
  id = rootId;
  action = "Search";
}

%>

<view:sp-page>
<view:sp-head-part>
<view:script src="javaScript/spacesInURL.js"/>
<view:script src="javaScript/commons.js"/>
<script type="text/javascript">

var topicAddWindow = window;
var topicUpdateWindow = window;

/***************************************************************************/

function declassify(nbTopic, nbSite) {
    closeWindows();

    okTopic = "false";
    okSites = "false";

    if (nbTopic > 0) {
        if (nbTopic == 1) {
            if (document.liste.checkbox.checked)
                okTopic = "true";
        }
        else {
            for (i=0; i<nbTopic; i++) {
                if (document.liste.checkbox[i] != null) {
                    if (document.liste.checkbox[i].checked)
                        okTopic = "true";
                }
                else break;
            }
        }
    }

    if (nbSite > 0) {
        if (nbSite == 1) {
            if (document.liste.supSite.checked)
                    okSites = "true";
        }
        else {
            for (i=0; i<nbSite; i++) {
                if (document.liste.supSite[i] != null) {
                    if (document.liste.supSite[i].checked)
                        okSites = "true";
                }
                else break;
            }
        }
     }


    if (okTopic != "false"  || okSites != "false") { //au moins un theme ou un site est selectionne

    var label = "<%=resources.getString("FolderSiteDeleteConfirmation")%>";
    jQuery.popup.confirm(label, function() {
      listeSite = "";

      if (nbSite > 0) {
        if (nbSite == 1) {
          if (document.liste.supSite.checked)
            listeSite += document.liste.supSite.value + ",";
        } else {
          for (i=0; i<nbSite; i++) {
              if (document.liste.supSite[i] != null) {
                if (document.liste.supSite[i].checked)
                  listeSite += document.liste.supSite[i].value + ",";
              }
              else break;
          }
        }
      }

      document.liste.Action.value = "Delete";
      document.liste.SiteList.value = listeSite;
      document.liste.submit();
	  });
     }
}

/***************************************************************************/

function topicAdd(fatherId) {
	if (!topicAddWindow.closed && topicAddWindow.name == "topicAddWindow")
        topicAddWindow.close();

    path = document.liste.Path.value;
    url = "addTopic.jsp?Id="+fatherId+"&Path="+breakSpace(path)+"&Action=View";
    windowName = "topicAddWindow";
    larg = "670";
    haut = "270";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    topicAddWindow = SP_openWindow(url, windowName, larg , haut, windowParams);
}

/***************************************************************************/

function topicUpdate(id) {
	if (!topicUpdateWindow.closed && topicUpdateWindow.name== "topicUpdateWindow")
        topicUpdateWindow.close();

    document.liste.ChildId.value = id;
    path = document.liste.Path.value;
    url = "updateTopic.jsp?ChildId="+id+"&Path="+breakSpace(path);
    windowName = "topicUpdateWindow";
    larg = "670";
    haut = "270";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    topicUpdateWindow = SP_openWindow(url, windowName, larg , haut, windowParams);
}

/***************************************************************************/

function publicationAdd(topicId){
    closeWindows();

   /* classifier Declassifier dans ce theme */
    document.pubForm.Action.value = "View";
    document.pubForm.TopicId.value = topicId;
    document.pubForm.submit();
}

/***************************************************************************/

function closeWindows() {
	if (!topicAddWindow.closed && topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();

	if (!topicUpdateWindow.closed && topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
}

/***************************************************************************/
function topicDown(topicId) {
	closeWindows();

	document.liste.action = "TopicDown";
	document.liste.Id.value = topicId;
	document.liste.submit();
}

/***************************************************************************/

function topicUp(topicId) {
	closeWindows();

	document.liste.action = "TopicUp";
	document.liste.Id.value = topicId;
	document.liste.submit();
}

/***************************************************************************/

function pubDown(pubId) {
	closeWindows();

	document.liste.action = "PubDown";
	document.liste.Id.value = pubId;
	document.liste.submit();
}

/***************************************************************************/

function pubUp(pubId) {
	closeWindows();

	document.liste.action = "PubUp";
	document.liste.Id.value = pubId;
	document.liste.submit();
}

window.wsm = new WebSiteManager({
  contextUrl : 'organize.jsp',
  forceSitePopupOpening : true,
  beforeOpenSiteTopicCallback : closeWindows,
  beforeOpenSiteCallback : closeWindows
});
</script>
</view:sp-head-part>
<view:sp-body-part>
<form name="liste" action="organize.jsp" method="post">
<%
	//Traitement = View, Search, Add, Update, Delete, Classify, Declassify
	if (id == null) {
		id=rootId;
		action = "Search";
	}

	/* SEARCH */
	if (action.equals("Search")) {
		Collection<NodeDetail> pathC = webSitesCurrentFolder.getPath();
		pathString = navigPath(pathC, false, 3);
		linkedPathString = navigPath(pathC, true, 3);

		Collection<NodeDetail> subThemes = webSitesCurrentFolder.getNodeDetail().getChildrenDetails();

		Collection<PublicationDetail> listeSites = webSitesCurrentFolder.getPublicationDetails();

		Window window = gef.getWindow();

		// La barre de naviagtion
		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel, "organize.jsp");
		browseBar.setPath(linkedPathString);

		//Les op�rations
		OperationPane operationPane = window.getOperationPane();
		operationPane.addOperationOfCreation(addFolder, resources.getString("GML.createTheme") , "javascript:onClick=topicAdd('"+id+"')");
		operationPane.addOperationOfCreation(addSite, resources.getString("AjouterSitesTheme"), "javascript:onClick=publicationAdd('"+id+"')");
		operationPane.addOperation(belpou,resources.getString("DeclasserSitesSupprimerThemes"), "javascript:onClick=declassify('"+subThemes.size()+"','"+listeSites.size()+"');");

		out.println(window.printBefore());

		//Les onglets
		TabbedPane tabbedPane = gef.getTabbedPane();
		tabbedPane.addTab(resources.getString("Consulter"), "Main", false);
		tabbedPane.addTab(resources.getString("Organiser"), "organize.jsp", true);
		tabbedPane.addTab(resources.getString("GML.management"), "manage.jsp", false);

		out.println(tabbedPane.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
		//Le tableau de tri
		ArrayPane arrayPane = gef.getArrayPane("foldersList", "organize.jsp?Action=Search&Id="+id, request, session);
		arrayPane.setVisibleLineNumber(10);
		arrayPane.setTitle(resources.getString("ListeThemes"));

		arrayPane.addArrayColumn(resources.getString("GML.theme"));
		arrayPane.addArrayColumn(resources.getString("GML.description"));
		ArrayColumn arrayColumnOp = arrayPane.addArrayColumn(resources.getString("GML.operation"));
		arrayColumnOp.setSortable(false);

		if (scc.isSortedTopicsEnabled()) {
			ArrayColumn arrayColumnOrder = arrayPane.addArrayColumn(resources.getString("GML.order"));
			arrayColumnOrder.setSortable(false);
		}

		ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;");
		arrayColumnDel.setSortable(false);

    int nbChild = 0;
    for (NodeDetail theme : subThemes) {
      /* ecriture des lignes du tableau */
      String themeName = theme.getName();
      String themeDescription = theme.getDescription();
      String themeId = theme.getNodePK().getId();

      ArrayLine arrayLine = arrayPane.addArrayLine();

      if (themeName.length() > 40)
        themeName = themeName.substring(0, 40) + "...";
      arrayLine.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(themeName), "javascript:wsm.goToAppTopic('"+themeId+"')");

      if (themeDescription.length() > 80)
        themeDescription = themeDescription.substring(0, 80) + "...";
      arrayLine.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(themeDescription));

      IconPane iconPane = gef.getIconPane();
      Icon checkIcon1 = iconPane.addIcon();
      checkIcon1.setProperties(folderUpdate, resources.getString("GML.modify")+" '"+WebEncodeHelper.javaStringToHtmlString(themeName)+"'" , "javascript:onClick=topicUpdate('"+themeId+"')");
      arrayLine.addArrayCellIconPane(iconPane);

      if (scc.isSortedTopicsEnabled()) {
        IconPane sortPane = gef.getIconPane();
        if (nbChild != 0) {
          Icon upIcon = sortPane.addIcon();
          upIcon.setProperties(upIconSrc, resources.getString("TopicUp")+" '"+themeName+"'", "javascript:onClick=topicUp('"+themeId+"')");
        }

        if (nbChild < subThemes.size()-1) {
          Icon downIcon = sortPane.addIcon();
          downIcon.setProperties(downIconSrc, resources.getString("TopicDown")+" '"+themeName+"'", "javascript:onClick=topicDown('"+themeId+"')");
        }
        arrayLine.addArrayCellIconPane(sortPane);
      }

      arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkbox\" value=\""+themeId+"\"/>");

      nbChild++;
    }
		out.println(arrayPane.print());

		//Liste des sites du th�me courant
		String liste = "";

		if (listeSites.size() > 0) {
			liste += "<table border=\"0\">\n";

			nbChild = 0;

			for (PublicationDetail site : listeSites) {
				String pubId = site.getPK().getId();
        String siteId = site.getVersion();
				String siteName = site.getName();
				String siteDescription = WebEncodeHelper.javaStringToHtmlParagraphe(site.getDescription());
				liste += "<tr>\n";
				liste += "<td valign=\"top\" width=\"5%\"><input type=\"checkbox\" name=\"supSite\" value=\""+pubId+"\"/></td>\n";
				if (scc.isSortedTopicsEnabled()) {
					IconPane sortPane = gef.getIconPane();
					if (nbChild != 0) {
						Icon upIcon = sortPane.addIcon();
						upIcon.setProperties(upIconSrc, resources.getString("PubUp")+" '"+siteName+"'", "javascript:onClick=pubUp('"+pubId+"')");
					}

					if (nbChild < listeSites.size()-1) {
						Icon downIcon = sortPane.addIcon();
						downIcon.setProperties(downIconSrc, resources.getString("PubDown")+" '"+siteName+"'", "javascript:onClick=pubDown('"+pubId+"')");
					}

					liste += "<td width=\"10px\">&nbsp;</td>\n";

					liste += "<td width=\"20px\" valign=\"top\">\n";
					liste += sortPane.print();
					liste += "</td>\n";

					liste += "<td width=\"10px\">&nbsp;</td>\n";
				}
				liste += "<td valign=\"top\">&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=wsm.goToSite('"+siteId+"')\">"+siteName+"</a><br/>\n";

				liste += "<span class=\"txtnote\">&nbsp;&nbsp;"+siteDescription+"</span><br/><br/></td>\n";
				liste += "</tr>\n";

				nbChild++;
			}

			liste += "</table>\n";
%>
			<br/>
			<view:board>
			<%=liste %>
			</view:board>
<%
	}
%>
</view:frame>
<%
		out.println(window.printAfter());
	}
%>

  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id" value="<%=id%>"/>
  <input type="hidden" name="Path" value="<%=WebEncodeHelper.javaStringToHtmlString(pathString)%>"/>
  <input type="hidden" name="ChildId"/>
  <input type="hidden" name="Name"/>
  <input type="hidden" name="description"/>
  <input type="hidden" name="SiteList"/>
</form>

<form name="pubForm" action="classifyDeclassify.jsp" method="post">
<input type="hidden" name="Action"/>
<input type="hidden" name="TopicId"/>
<input type="hidden" name="Path" value="<%=WebEncodeHelper.javaStringToHtmlString(linkedPathString)%>"/>
</form>

</view:sp-body-part>
</view:sp-page>