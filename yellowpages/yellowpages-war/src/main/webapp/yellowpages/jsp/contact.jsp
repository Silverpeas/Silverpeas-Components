<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.apache.commons.lang3.BooleanUtils"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>

<%
CompleteContact fullContact = (CompleteContact) request.getAttribute("Contact");
ContactDetail contact = fullContact.getContactDetail();

Form formView    = fullContact.getViewForm();
PagesContext context = (PagesContext) request.getAttribute("PagesContext");

boolean externalView = BooleanUtils.isTrue((Boolean) request.getAttribute("ExternalView"));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<style type="text/css">
<% if (externalView) { %>
.cellBrowseBar, .cellOperation {
  display: none;
}
<% } %>
</style>
</head>
<body>
<view:browseBar path='<%=resources.getString("BBarconsultManager")%>'/>
<view:operationPane>
<view:operation altText='<%=resources.getString("GML.print")%>' action='javaScript:window.print();'/>
</view:operationPane>
<view:window popup="true">
<view:frame>
<view:board>

<table cellpadding="5" cellspacing="0" border="0">
   <tr>
   	<td class="txtlibform"><%=resources.getString("Contact") %> :</td>
   	<td class="txtnav"><%=EncodeHelper.javaStringToHtmlString(contact.getFirstName()) %> <%= EncodeHelper.javaStringToHtmlString(contact.getLastName()) %></td>
   </tr>
   <% if (StringUtil.isDefined(contact.getPhone())) { %>
   	<tr>
   		<td class="txtlibform"><%=resources.getString("GML.phoneNumber") %> :</td>
   		<td><%=EncodeHelper.javaStringToHtmlString(contact.getPhone()) %></td>
   	</tr>
   <% } %>
   <% if (StringUtil.isDefined(contact.getFax())) { %>
   <tr>
   	<td class="txtlibform"><%=resources.getString("GML.faxNumber") %> :</td>
   	<td><%=EncodeHelper.javaStringToHtmlString(contact.getFax()) %></td>
   </tr>
   <% } %>
   <% if (StringUtil.isDefined(contact.getEmail())) { %>
   <tr>
   	<td class="txtlibform"><%=resources.getString("GML.eMail") %> :</td>
   	<td><a href=mailto:"<%=EncodeHelper.javaStringToHtmlString(contact.getEmail()) %>"><%=EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToHtmlString(contact.getEmail())) %></a></td>
   </tr>
   <% } %>
</table>

<%
if (formView != null) {
	formView.display(out, context);
}
%>

</view:board>
</view:frame>
</view:window>
</body>
</html>