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

<%@ include file="check.jsp" %>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper" %>
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
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
	
	if (action.equals("Preview") || operationsVisibles) {
		out.println(window.printBefore());
	}

	//Les onglets
	if (action.equals("Preview")) {
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
			  	  %>
			  	  <view:displayWysiwyg objectId="<%=componentId%>" componentId="<%=componentId %>" language="<%=I18NHelper.defaultLanguage %>"/>
			  	  <%
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
	
	if (action.equals("Preview")) {
		out.println(frame.printAfter());
	}
		
	if (action.equals("Preview") || operationsVisibles) {
		out.println(window.printAfter());
	}
%>	
</body>
</html>