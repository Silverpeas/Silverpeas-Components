<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%
	boolean isSubscriber = ((Boolean) request.getAttribute("IsSubscriber")).booleanValue();

	String action = (String)request.getAttribute("Action");
	if (action == null) {
	  action = "Display";
	}
	boolean haveGotContent = ((Boolean)request.getAttribute("haveGotContent")).booleanValue();
	boolean isAnonymous = ((Boolean)request.getAttribute("AnonymousAccess")).booleanValue();
	
	Form form = (Form) request.getAttribute("Form");
	DataRecord data = (DataRecord) request.getAttribute("Data");
	
	PagesContext context = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, "useless");
 	context.setObjectId("0");
	context.setBorderPrinted(false);
	
	boolean operationsVisibles = !action.equals("Portlet") && webPagesScc.isSubscriptionUsed() && !isAnonymous;
%>

<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</head>
<body>
<%
	if (operationsVisibles) {
	 	if (!isSubscriber) {
	 		operationPane.addOperation("useless", resource.getString("webPages.subscriptionAdd"), "AddSubscription");
	 	} else {
	 		operationPane.addOperation("useless", resource.getString("webPages.subscriptionRemove"), "RemoveSubscription");
	 	}
	}
	
	if ("Preview".equals(action) || "Display".equals(action) || operationsVisibles) {
		out.println(window.printBefore());
	}

	//Les onglets
	if ("Preview".equals(action)) {
		TabbedPane tabbedPane = gef.getTabbedPane();
		tabbedPane.addTab(resource.getString("webPages.preview"), "Preview", true);
		tabbedPane.addTab(resource.getString("webPages.edit"), "Edit", false);
		out.println(tabbedPane.print());
		
		out.println(frame.printBefore());
	}
%>
	<table width="100%" border="0">
	<tr><td id="richContent">
		<%
			if (haveGotContent) {
			  	if (data != null) {
			  	  form.display(out, context, data);
			  	} else {			  	  
					out.flush();
					getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+componentId+"&SpaceId="+spaceId+"&ComponentId="+componentId).include(request, response);
			  	}
			} else {
		%>
				<center>
				<img src="<%=resource.getIcon("webPages.underConstruction") %>" alt=""/>
				<span class="txtnav"><%=resource.getString("webPages.emptyPage")%></span>
				<img src="<%=resource.getIcon("webPages.underConstruction") %>" alt=""/>
				</center>
		<% } %>
	</td></tr>
	</table>
<%
	
	if ("Preview".equals(action)) {
		out.println(frame.printAfter());
	}
		
	if ("Preview".equals(action) || "Display".equals(action) || operationsVisibles) {
		out.println(window.printAfter());
	}
%>	
</body>
</html>