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

<%@ include file="checkKmelia.jsp" %>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>

<%
List<ProfileInst>		profiles = (List<ProfileInst>) request.getAttribute("Profiles");
NodeDetail 	node			= (NodeDetail) request.getAttribute("NodeDetail");
ProfileInst currentProfile 	= (ProfileInst) request.getAttribute("CurrentProfile");
List<Group> 		groups 			= (List<Group>) request.getAttribute("Groups");
List<String> 		users 			= (List<String>) request.getAttribute("Users");

String		rightsDependsOn = (String) request.getAttribute("RightsDependsOn");

String explainRightsDependsOn = resources.getString("kmelia.RightsDependsOn"+rightsDependsOn);

String linkedPathString = (String) request.getAttribute("Path");

String nodeId = node.getNodePK().getId();
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function goToOperationInUserPanel(action) {
	url = action;
	windowName = "userPanelWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	userPanelWindow = SP_openUserPanel(url, windowName, windowParams);
}  
</script>
</head>
<body>
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    OperationPane operationPane = window.getOperationPane();
    
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(linkedPathString);
    
    if (rightsDependsOn.equals("ThisTopic")) {
    	operationPane.addOperation(resources.getIcon("kmelia.userManage"),resources.getString("GML.modify"),"javaScript:onClick=goToOperationInUserPanel('TopicProfileSelection?Role="+currentProfile.getName()+"&NodeId="+nodeId+"')");
    }
	
	if (rightsDependsOn.equals("ThisTopic") && (!groups.isEmpty() || !users.isEmpty())) { 
		operationPane.addOperation(resources.getIcon("kmelia.usersGroupsDelete"),resources.getString("GML.delete"), "TopicProfileRemove?Role="+currentProfile.getName()+"&Id="+currentProfile.getId()+"&NodeId="+nodeId);
	}
	
	out.println(window.printBefore());
    
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("Theme"), "ToUpdateTopic?Id="+nodeId, false);
	
    for (ProfileInst theProfile : profiles) {
    	tabbedPane.addTab(resources.getString("kmelia.Role"+theProfile.getName()), "ViewTopicProfiles?Id="+theProfile.getId()+"&Role="+theProfile.getName()+"&NodeId="+nodeId, theProfile.getName().equals(currentProfile.getName()));
    }

    out.println(tabbedPane.print());
    out.println(frame.printBefore());
	out.println(board.printBefore());
%>
	<table width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<tr>
			<td colspan="2" align="center">
				<div class="inlineMessage"><%=explainRightsDependsOn%></div>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resources.getIcon("kmelia.1px")%>"></td>
		</tr>
		<tr>
			<td align="center" class="txttitrecol"><%=resources.getString("GML.type")%></td>
			<td align="center" class="txttitrecol"><%=resources.getString("GML.name")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resources.getIcon("kmelia.1px")%>"></td>
		</tr>
		
		<%
		// La boucle sur les groupes 
		for (Group group : groups) {
		%>
			<tr>
			<% if (group.isSynchronized()) {  %>
				<td align="center"><img src="<%=resources.getIcon("kmelia.scheduledGroup")%>"/></td>
			<% } else { %>
				<td align="center"><img src="<%=resources.getIcon("kmelia.group")%>"/></td>
			<% } %>
			<td align="center"><%=group.getName() %></td>
			</tr>
		<% } %>
		
		<% for (String user : users) { %>
			<tr>
				<td align="center"><img src="<%=resources.getIcon("kmelia.user")%>"/></td>
				<td align="center"><%out.println(user);%></td>
			</tr>
		<% } %>				
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resources.getIcon("kmelia.1px")%>"/></TD>
		</tr>
	</table>
<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</body>
</html>