<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
  String linkedPathString = navigPath(webSitesCurrentFolder.getPath(), true, 3);
%>
<view:sp-page angularJsAppName="silverpeas.bookmark">
<view:sp-head-part>
  <view:includePlugin name="toggle"/>
  <view:script src="javaScript/spacesInURL.js"/>
  <view:script src="javaScript/commons.js"/>
  <script type="text/javascript">
    window.wsm = new WebSiteManager('listSite.jsp');
  </script>
</view:sp-head-part>
<view:sp-body-part>
<view:browseBar path="<%=linkedPathString%>"/>
<view:window>
  <view:tabs>
    <view:tab label='<%=resources.getString("Consulter")%>' action="Main" selected="true"/>
    <view:tab label='<%=resources.getString("Organiser")%>' action="organize.jsp" selected="false"/>
    <view:tab label='<%=resources.getString("GML.management")%>' action="manage.jsp" selected="false"/>
  </view:tabs>
  <view:frame>
    <view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resources.getLanguage()%>"/>
    <c:out escapeXml="false" value="<%=renderTopicNavigation(scc, gef, webSitesCurrentFolder)%>"/>
    <c:out escapeXml="false" value="<%=renderTopicSites(scc, webSitesCurrentFolder)%>"/>
  </view:frame>
</view:window>
</view:sp-body-part>
</view:sp-page>