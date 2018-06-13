<%--

    Copyright (C) 2000 - 2018 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/kmelia" prefix="kmelia" %>
<%@ include file="checkKmelia.jsp" %>

<%@page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@page import="org.silverpeas.core.admin.user.model.SilverpeasRole"%>
<%@page import="org.silverpeas.components.kmelia.SearchContext"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>

<c:set var='highestUserRole' value='<%=SilverpeasRole.from((String) request.getAttribute("Profile"))%>'/>

<%
String id = "0";

// création du nom pour les favoris
String namePath = spaceLabel + " > " + componentLabel;

//R?cup?ration des param?tres
String 	profile			= (String) request.getAttribute("Profile");
String  translation 	= (String) request.getAttribute("Language");
boolean	isGuest			= (Boolean) request.getAttribute("IsGuest");
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");
int		currentPageIndex = (Integer) request.getAttribute("PageIndex");

SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
String query = "";
if (searchContext != null) {
  query = searchContext.getQuery();
}

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String language = kmeliaScc.getLanguage();

String urlTopic	= URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId, true);

String userId = kmeliaScc.getUserId();

boolean userCanCreatePublications = SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile) || SilverpeasRole.writer.isInRole(profile);
boolean userCanValidatePublications = SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile);

boolean userCanSeeStats = kmeliaScc.isStatisticAllowed();

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.kmelia">
<head>
<view:looknfeel/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<view:includePlugin name="rating" />

<view:script src="javaScript/navigation.js"/>
<view:script src="javaScript/searchInTopic.js"/>
<view:script src="javaScript/publications.js"/>
<script type="text/javascript">

window.i18n.properties({
  name: 'kmeliaBundle',
  path: webContext + '/services/bundles/org/silverpeas/kmelia/multilang/',
  language: '<%=language%>',
  mode: 'map'
});

function getString(key) {
	return window.i18n.prop(key);
}

function getCurrentNodeId() {
	return "0";
}

function getCurrentUserId() {
  return "<%=userId%>";
}

function getWebContext() {
	return "<%=m_context%>";
}

function getComponentId() {
	return "<%=componentId%>";
}

function getPubIdToHighlight() {
  return "<%=pubIdToHighlight%>";
}

function fileUpload() {
    document.fupload.submit();
}

function getPubIdToHighlight() {
	return "<%=pubIdToHighlight%>";
}

function topicWysiwyg() {
	closeWindows();
	document.topicDetailForm.action = "ToTopicWysiwyg";
	document.topicDetailForm.ChildId.value = "0";
	document.topicDetailForm.submit();
}

function pasteFromOperations() {
  checkOnPaste('0');
}

function pasteDone(folderId) {
  displayPublications(folderId);
}

var searchInProgress = <%=searchContext != null%>;

$(document).ready(function() {
	if (searchInProgress) {
		doPagination(<%=currentPageIndex%>);
	} else {
		displayPublications("<%=id%>");
	}
	displayTopicDescription("0");
});
</script>
</head>
<body id="kmelia" onunload="closeWindows()" class="yui-skin-sam">
<div compile-directive style="display: none"></div>
<div id="<%=componentId %>">
<%
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setI18N("GoToCurrentTopic", translation);

        //Display operations
        OperationPane operationPane = window.getOperationPane();
        if (SilverpeasRole.admin.isInRole(profile)){
          	if (kmeliaScc.isPdcUsed()) {
	        	operationPane.addOperation("useless", resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+kmeliaScc.getComponentId()+"','utilizationPdc1')");
                operationPane.addOperation("useless", resources.getString("GML.PDCPredefinePositions"), "javascript:onClick=openPredefinedPdCClassification(" + id + ");");
          	}
          	if (kmeliaScc.isContentEnabled()) {
	        	operationPane.addOperation(resources.getIcon("kmelia.modelUsed"), resources.getString("kmelia.ModelUsed"), "ModelUsed");
          	}
          	if (kmeliaScc.isWysiwygOnTopicsEnabled()) {
				operationPane.addOperation("useless", kmeliaScc.getString("TopicWysiwyg"), "javascript:onClick=topicWysiwyg('"+id+"')");
			}
          if (SilverpeasRole.admin.isInRole(profile)) {
            operationPane.addOperation("useless", resources.getString("GML.manageSubscriptions"), "ManageSubscriptions");
          }
          	if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportZipAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportTopic()");
          	}
          	if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportPdfAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ExportPDF"), "javascript:openExportPDFPopup()");
          	}
	        operationPane.addOperation(resources.getIcon("kmelia.sortPublications"), kmeliaScc.getString("kmelia.OrderPublications"), "ToOrderPublications");
			operationPane.addLine();
        }
        if (userCanCreatePublications) {
	        operationPane.addOperationOfCreation(resources.getIcon("kmelia.operation.addPubli"), kmeliaScc.getString("PubCreer"), "NewPublication");
	        if (kmeliaScc.isImportFileAllowed()) {
	      		operationPane.addOperationOfCreation(resources.getIcon("kmelia.operation.importFile"), kmeliaScc.getString("kmelia.ImportFile"), "javascript:onClick=importFile()");
	        }
	        if (kmeliaScc.isImportFilesAllowed()) {
	        	operationPane.addOperationOfCreation(resources.getIcon("kmelia.operation.importFiles"), kmeliaScc.getString("kmelia.ImportFiles"), "javascript:onClick=importFiles()");
	        }
	        operationPane.addOperation("useless", resources.getString("kmelia.operation.copyPublications"), "javascript:onclick=copyPublications()");
	        operationPane.addOperation("useless", resources.getString("kmelia.operation.cutPublications"), "javascript:onclick=cutPublications()");
	        operationPane.addOperation(resources.getIcon("kmelia.paste"), resources.getString("GML.paste"), "javascript:onClick=pasteFromOperations()");
	        operationPane.addOperation("useless", resources.getString("kmelia.operation.deletePublications"), "javascript:onclick=deletePublications()");
	        operationPane.addLine();
        }

    	if (!isGuest) {
    	  	operationPane.addOperation("useless", resources.getString("kmelia.operation.exportSelection"), "javascript:onclick=exportPublications()");
    		operationPane.addOperation("useless", resources.getString("kmelia.folderSubscription"), "javascript:onClick=addSubscription()");
      		operationPane.addOperation("useless", resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+
              WebEncodeHelper.javaStringToHtmlString(WebEncodeHelper.javaStringToJsString(namePath))+"','','"+urlTopic+"')");
    	}

    	if (userCanCreatePublications) {
      		operationPane.addLine();
          	operationPane.addOperation("useless", resources.getString("PubBasket"), "GoToBasket");
          	if (userCanValidatePublications) {
          		operationPane.addOperation("useless", resources.getString("ToValidate"), "ViewPublicationsToValidate");
          	}
  		}
      if (userCanSeeStats) {
        operationPane.addLine();
        operationPane.addOperation("useless", resources.getString("kmelia.operation.statistics"), "javascript:showStats();");
      }

    out.println(window.printBefore());
%>
<view:frame>
					<% if (displaySearch.booleanValue()) {
						Button searchButton = gef.getFormButton(resources.getString("GML.search"), "javascript:onClick=searchInTopic();", false); %>
						<div id="searchZone">
						<view:board>
						<table id="searchLine">
						<tr><td><div id="searchLabel"><%=resources.getString("kmelia.SearchInTopics") %></div>&nbsp;<input type="text" id="topicQuery" size="50" value="<%=query%>" onkeydown="checkSubmitToSearch(event)"/></td><td><%=searchButton.print() %></td></tr>
						</table>
						</view:board>
						</div>
					<% } %>
					<div id="topicDescription"></div>
					<view:areaOfOperationOfCreation/>
          <div class="dragAndDropUpload" style="min-height: 75px">
            <div id="pubList">
              <br/>
              <view:board>
                <br/>
                <center><%=resources.getString("kmelia.inProgressPublications") %>
                  <br/><br/><img src="<%=resources.getIcon("kmelia.progress") %>"/></center>
                <br/>
              </view:board>
            </div>
          </div>
					<div id="footer" class="txtBaseline"></div>
	</view:frame>
	<%
		out.println(window.printAfter());
	%>

<form name="topicDetailForm" method="post">
	<input type="hidden" name="Id" value="<%=id%>"/>
	<input type="hidden" name="Path" value=""/>
	<input type="hidden" name="ChildId"/>
	<input type="hidden" name="Status"/><input type="hidden" name="Recursive"/>
</form>

<form name="pubForm" action="ViewPublication" method="post">
	<input type="hidden" name="PubId"/>
</form>

<form name="fupload" action="fileUpload.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
	<input type="hidden" name="Action" value="initial"/>
</form>

</div>

<%@ include file="../../sharing/jsp/createTicketPopin.jsp" %>
<view:progressMessage/>
<kmelia:paste highestUserRole="${highestUserRole}" componentInstanceId="<%=componentId%>" />
<kmelia:dragAndDrop highestUserRole="${highestUserRole}" componentInstanceId="<%=componentId%>" forceIgnoreFolder="true" contentLanguage="<%=translation%>" />
<script type="text/javascript">
/* declare the module myapp and its dependencies (here in the silverpeas module) */
var myapp = angular.module('silverpeas.kmelia', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>