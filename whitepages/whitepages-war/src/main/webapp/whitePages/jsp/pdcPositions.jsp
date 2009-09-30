<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.io.File"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="checkWhitePages.jsp" %>

<%

String userCardId		= (String) request.getAttribute("UserCardId");
String url				= (String) request.getAttribute("ReturnURL");
String silverContentId	= (String) request.getAttribute("SilverContentId");

String firstVisite		= (String) request.getAttribute("FirstVisite");

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

	function newPosition() {
		document.newPosition.action = "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')";	
		document.newPosition.submit();	
	}

</script>
</HEAD>

<BODY <%if (firstVisite.equals("1")) out.print("onload=\"javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition');\"");%>>

<BODY>
<%
	browseBar.setDomainName(spaceLabel);
	if (containerContext == null) {
		browseBar.setComponentName(componentLabel, "Main");
	} else {
		browseBar.setComponentName(componentLabel, m_context+containerContext.getReturnURL()); 
	}
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.PdcClassification"));

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resource.getString("whitePages.NewPdcPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resource.getString("whitePages.DeletePdcPosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());

	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"consultIdentity?userCardId="+userCardId, false, true);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"consultCard?userCardId="+userCardId, false, true);
	tabbedPane.addTab(resource.getString("whitePages.PdcClassification"), routerUrl+"ViewPdcPositions?userCardId="+userCardId, true, false);

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
	
	out.println(tabbedPane.print());
	out.println(frame.printBefore());

	out.flush();
 
	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+silverContentId+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);
		
%>
<br>
<center>
<%=buttonPane.print() %>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="ViewPdcPositions" METHOD=POST>
	<input type="hidden" name="userCardId" value="<%=userCardId%>">
</FORM>
<FORM NAME="newPosition" ACTION="" METHOD=POST>
</FORM>
</BODY>
</HTML>