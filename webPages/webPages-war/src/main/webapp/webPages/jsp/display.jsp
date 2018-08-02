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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>
<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%
	boolean isSubscriber = (Boolean) request.getAttribute("IsSubscriber");
	boolean subscriptionEnabled = (Boolean) request.getAttribute("SubscriptionEnabled");

	String action = (String)request.getAttribute("Action");
	if (action == null) {
	  action = "Display";
	}
	boolean haveGotContent = (Boolean)request.getAttribute("haveGotContent");
	boolean isAnonymous = (Boolean)request.getAttribute("AnonymousAccess");

	Form form = (Form) request.getAttribute("Form");
	DataRecord data = (DataRecord) request.getAttribute("Data");

	PagesContext context = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, "useless");
	context.setObjectId("0");
	context.setBorderPrinted(false);

	boolean operationsVisibles = !action.equals("Portlet") && !isAnonymous;

  String labelSubscribe = resource.getString("GML.subscribe");
  String labelUnsubscribe = resource.getString("GML.unsubscribe");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.webPage">
<head>
<view:looknfeel/>
<link type="text/css" rel="stylesheet" href="styleSheets/webPages-print.css" media="print"/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<view:includePlugin name="toggle"/>
<script type="text/javascript">
  function addSubscription() {
    $.post(webContext+"/services/subscribe/<%=componentId%>", successSubscribe(), 'json');
  }

  function successSubscribe() {
    // changing label and href of operation on-the-fly
    $("a[href='javascript:addSubscription()']").first().attr('href', "javascript:removeSubscription()").text("<%=labelUnsubscribe%>");
  }

  function removeSubscription() {
    $.post(webContext+"/services/unsubscribe/<%=componentId%>", successUnsubscribe(), 'json');
  }

  function successUnsubscribe() {
    //changing label and href of operation on-the-fly
    $("a[href='javascript:removeSubscription()']").first().attr('href', "javascript:addSubscription()").text("<%=labelSubscribe%>");
  }
</script>
</head>
<body>
<%
	if (operationsVisibles) {
	  if ("Preview".equals(action)) {
      operationPane.addOperation("useless", resource.getString("webPages.edit"), "Edit");
      if (subscriptionEnabled) {
        operationPane.addOperation("useless", resource.getString("GML.manageSubscriptions"), "ManageSubscriptions");
      }
      operationPane.addLine();
    }
    if (subscriptionEnabled) {
      if (!isSubscriber) {
        operationPane.addOperation("useless", labelSubscribe, "javascript:addSubscription()");
      } else {
        operationPane.addOperation("useless", labelUnsubscribe, "javascript:removeSubscription()");
      }
    }
    operationPane.addOperation("useless", resource.getString("GML.print"), "javaScript:print();");
		out.println(window.printBefore());
	}
%>
<view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resource.getLanguage()%>"/>
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
				<img src="<%=resource.getIcon("webPages.underConstruction") %>" alt=""/>
				<span class="txtnav"><%=resource.getString("webPages.emptyPage")%></span>
				<img src="<%=resource.getIcon("webPages.underConstruction") %>" alt=""/>
		<% } %>
	</td></tr>
	</table>
<%
	if (operationsVisibles) {
		out.println(window.printAfter());
	}
%>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.webPage', ['silverpeas.services', 'silverpeas.directives']);
</script>

</body>
</html>