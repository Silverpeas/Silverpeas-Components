<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>
<%
  FolderDetail webSitesCurrentFolder = (FolderDetail) request.getAttribute("CurrentFolder");
  Collection<NodeDetail> pathC = webSitesCurrentFolder.getPath();
  String linkedPathString = navigPath(pathC, true, 3);
  Collection<PublicationDetail> siteList = webSitesCurrentFolder.getPublicationDetails();
  String singleSiteId = (siteList != null && siteList.size() == 1) ? siteList.iterator().next().getVersion() : null;
  String suggestionName = (String) request.getAttribute("SuggestionName");
  String suggestionUrl = (String) request.getAttribute("SuggestionUrl");
  boolean suggestionSent = false;
  if (suggestionName != null) {
    suggestionSent = true;
  }
  String suggestLabel = resources.getString("Suggerer");
%>

<!-- listSite_reader -->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.bookmark">
<head>
  <view:looknfeel/>
  <view:includePlugin name="toggle"/>
  <title><%=resources.getString("GML.popupTitle")%></title>
  <view:script src="javaScript/spacesInURL.js"/>
  <view:script src="javaScript/commons.js"/>
  <script type="text/javascript">
    window.wsm = new WebSiteManager('listSite_reader.jsp');

    function publicationGoToUniqueSite() {
      <%
        if (singleSiteId != null) {
          out.println("wsm.openSite('" + singleSiteId + "');");
        }
      %>
    }

    function openSuggestionConfirmation() {
      var theURL = "suggestionConfirmation.jsp?nomSite=<%=suggestionName%>&nomPage=<%=suggestionUrl%>";
      var winName = "suggestionConfirmation";
      var larg = "480";
      var haut = "300";
      var windowParams = "scrollbars=yes, resizable, alwaysRaised";
      SP_openWindow(theURL, winName, larg, haut, windowParams);
    }
  </script>
</head>
<body onload="<%=suggestionSent ? "openSuggestionConfirmation()" : "publicationGoToUniqueSite()"%>">
<view:operationPane>
  <view:operation action="Suggest" altText="<%=suggestLabel%>"/>
</view:operationPane>
<view:browseBar path="<%=linkedPathString%>"/>
<view:window>
  <view:frame>
    <view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resources.getLanguage()%>"/>
    <c:out escapeXml="false" value="<%=renderTopicNavigation(scc, gef, webSitesCurrentFolder)%>"/>
    <c:out escapeXml="false" value="<%=renderTopicSites(scc, webSitesCurrentFolder)%>"/>
  </view:frame>
</view:window>
<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.bookmark', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>