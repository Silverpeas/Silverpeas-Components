<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="checkKmelia.jsp" %>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:url var="cssFieldset" value="/util/styleSheets/fieldset.css"/>

<%
List<ProfileInst>		profiles = (List<ProfileInst>) request.getAttribute("Profiles");
NodeDetail 	node			= (NodeDetail) request.getAttribute("NodeDetail");
ProfileInst currentProfile 	= (ProfileInst) request.getAttribute("CurrentProfile");
List<Group> 		groups 			= (List<Group>) request.getAttribute("Groups");
List<UserDetail> 		users 			= (List<UserDetail>) request.getAttribute("Users");

String rightsDependsOn = (String) request.getAttribute("RightsDependsOn");
String explainRightsDependsOn = resources.getString("kmelia.RightsDependsOn"+rightsDependsOn);
String linkedPathString = (String) request.getAttribute("Path");
String nodeId = node.getNodePK().getId();

String updateCallback = "";
if ("ThisTopic".equals(rightsDependsOn)) {
  updateCallback = "TopicProfileSelection?Role="+currentProfile.getName()+"&NodeId="+nodeId;
}
%>
<html>
<head>
<view:looknfeel/>
<link type="text/css" href="${cssFieldset}" rel="stylesheet" />
<script type="text/javascript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}
function backToFolder() {
  location.href="ToUpdateTopic?Id=<%=nodeId%>";
}
</script>
</head>
<body>
<%
    Window window = gef.getWindow();
    OperationPane operationPane = window.getOperationPane();
    
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(linkedPathString);
	
	out.println(window.printBefore());
    
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("Theme"), "javascript:backToFolder()", false);
	
    for (ProfileInst theProfile : profiles) {
    	tabbedPane.addTab(resources.getString("kmelia.Role"+theProfile.getName()), "ViewTopicProfiles?Id="+theProfile.getId()+"&Role="+theProfile.getName()+"&NodeId="+nodeId, theProfile.getName().equals(currentProfile.getName()));
    }

    out.println(tabbedPane.print());
%>
<view:frame>
  <div class="inlineMessage"><%=explainRightsDependsOn%></div>

  <form name="roleList" action="TopicProfileSetUsersAndGroups" method="post">
    <input type="hidden" name="Role" value="<%=currentProfile.getName()%>"/>
    <input type="hidden" name="NodeId" value="<%=nodeId%>"/>
    <fmt:message var="listLabel" key="GML.selection"/>
    <viewTags:displayListOfUsersAndGroups users="<%=users%>" groups="<%=groups%>" id="roleItems" label="${listLabel}" updateCallback="<%=updateCallback%>" />
  </form>
  <view:buttonPane>
    <% if (StringUtil.isDefined(updateCallback)) { %>
    <fmt:message var="buttonOK" key="GML.validate"/>
    <fmt:message var="buttonCancel" key="GML.cancel"/>
    <view:button label="${buttonOK}" action="javascript:document.roleList.submit()"/>
    <view:button label="${buttonCancel}" action="javascript:backToFolder()"/>
    <% } %>
  </view:buttonPane>
</view:frame>
<%
    out.println(window.printAfter());
%>
</body>
</html>