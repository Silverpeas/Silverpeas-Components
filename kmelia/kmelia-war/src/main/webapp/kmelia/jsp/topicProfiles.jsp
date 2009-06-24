<%@ include file="checkKmelia.jsp" %>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>

<%
List 		profiles 		= (List) request.getAttribute("Profiles");
NodeDetail 	node			= (NodeDetail) request.getAttribute("NodeDetail");
ProfileInst currentProfile 	= (ProfileInst) request.getAttribute("CurrentProfile");
List 		groups 			= (List) request.getAttribute("Groups");
List 		users 			= (List) request.getAttribute("Users");

String		rightsDependsOn = (String) request.getAttribute("RightsDependsOn");

String explainRightsDependsOn = resources.getString("kmelia.RightsDependsOn"+rightsDependsOn);

String linkedPathString = (String) request.getAttribute("Path");

String nodeId = node.getNodePK().getId();
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javaScript">
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
</HEAD>
<BODY>
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    OperationPane operationPane = window.getOperationPane();
    
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(linkedPathString);
    browseBar.setExtraInformation(node.getName());
    
    if (rightsDependsOn.equals("ThisTopic"))
    	operationPane.addOperation(resources.getIcon("kmelia.userManage"),resources.getString("GML.modify"),"javaScript:onClick=goToOperationInUserPanel('TopicProfileSelection?Role="+currentProfile.getName()+"&NodeId="+nodeId+"')");
	
	if (rightsDependsOn.equals("ThisTopic") && (groups.size() > 0 || users.size() > 0)) 
		operationPane.addOperation(resources.getIcon("kmelia.usersGroupsDelete"),resources.getString("GML.delete"), "TopicProfileRemove?Role="+currentProfile.getName()+"&Id="+currentProfile.getId()+"&NodeId="+nodeId);
	
	out.println(window.printBefore());
    
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("Theme"), "ToUpdateTopic?Id="+nodeId, false);
	
    Iterator p = profiles.iterator();
    ProfileInst theProfile = null;
    while (p.hasNext()) {
    	theProfile = (ProfileInst) p.next();
    	
    	tabbedPane.addTab(resources.getString("kmelia.Role"+theProfile.getName()), "ViewTopicProfiles?Id="+theProfile.getId()+"&Role="+theProfile.getName()+"&NodeId="+nodeId, theProfile.getName().equals(currentProfile.getName()));
    }

    out.println(tabbedPane.print());
    out.println(frame.printBefore());
	out.println(board.printBefore());
%>
	<TABLE width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<TR>
			<TD colspan="2" align="center">
				<%=explainRightsDependsOn%><BR/>
				<!--<%
				if (rightsDependsOn.equals("ThisTopic"))
					out.println("<a href=\"TopicSpecificRightsDisable?Role="+currentProfile.getName()+"\">"+resources.getString("kmelia.RightsSpecificDisable")+"</a>");
				else
					out.println("<a href=\"TopicSpecificRightsEnable?Role="+currentProfile.getName()+"\">"+resources.getString("kmelia.RightsSpecificEnable")+"</a>");
				%>--><BR/><BR/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resources.getIcon("kmelia.1px")%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=resources.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=resources.getString("GML.name")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resources.getIcon("kmelia.1px")%>"></TD>
		</TR>
		
		<%
		// La boucle sur les groupes 
		int i = 0;
		Group group = null;
		while (i < groups.size()) 
		{
			group = (Group) groups.get(i);
			
			out.println("<TR>");
			if (group.isSynchronized())
				out.println("<TD align=\"center\"><IMG SRC=\""+resources.getIcon("kmelia.scheduledGroup")+"\"></TD>");
			else
				out.println("<TD align=\"center\"><IMG SRC=\""+resources.getIcon("kmelia.group")+"\"></TD>");
			out.println("<TD align=\"center\">"+group.getName()+"</TD>");
			out.println("</TR>");
			i++;
		}
		
		// La boucle sur les users
		i = 0;
		while (i < users.size()) 
		{
		%>
			<TR>
				<TD align="center"><IMG SRC="<%=resources.getIcon("kmelia.user")%>"></TD>
				<TD align="center"><%out.println((String) users.get(i));%></TD>
			</TR>
		<%
			i++;
		}
		%>				
		<TR>
			<TD colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=resources.getIcon("kmelia.1px")%>"></TD>
		</TR>
	</TABLE>
<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</BODY>
</HTML>