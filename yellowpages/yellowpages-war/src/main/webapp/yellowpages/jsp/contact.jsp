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
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>
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
UserCompleteContact userContactComplete = (UserCompleteContact) request.getAttribute("Contact");
ContactDetail contact = userContactComplete.getContact().getContactDetail();

Form formView    = (Form) request.getAttribute("Form");
DataRecord data    = (DataRecord) request.getAttribute("Data");
PagesContext context = (PagesContext) request.getAttribute("PagesContext");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
</head>
<body>
<%
Window window = gef.getWindow();
        
OperationPane operationPane = window.getOperationPane();
operationPane.addOperation(resources.getIcon("yellowpages.contactPrint"), resources.getString("GML.print"), "javaScript:window.print();");

BrowseBar browseBar = window.getBrowseBar();
browseBar.setComponentId(componentId);
browseBar.setPath(resources.getString("BBarconsultManager"));
browseBar.setClickable(false);

out.println(window.printBefore());
%>
<view:frame>
<view:board>

<table cellpadding="5" cellspacing="0" border="0">
   <tr>
   	<td class="txtlibform"><%=resources.getString("Contact") %> :</td>
   	<td class="txtnav"><%=EncodeHelper.javaStringToHtmlString(contact.getFirstName()) %> <%= EncodeHelper.javaStringToHtmlString(contact.getLastName()) %></td>
   </tr>
   <tr>
   	<td class="txtlibform"><%=resources.getString("GML.phoneNumber") %> :</td>
   	<td><%=EncodeHelper.javaStringToHtmlString(contact.getPhone()) %></td>
   </tr>
   <tr>
   	<td class="txtlibform"><%=resources.getString("GML.faxNumber") %> :</td>
   	<td><%=EncodeHelper.javaStringToHtmlString(contact.getFax()) %></td>
   </tr>
   <tr>
   	<td class="txtlibform"><%=resources.getString("GML.eMail") %> :</td>
   	<td><a href=mailto:"<%=EncodeHelper.javaStringToHtmlString(contact.getEmail()) %>"><%=EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToHtmlString(contact.getEmail())) %></a></td>
   </tr>
</table>

<%
if (formView != null) {
	formView.display(out, context, data);
}
%>

</view:board>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>