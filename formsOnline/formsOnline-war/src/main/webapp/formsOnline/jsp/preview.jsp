<%--

    Copyright (C) 2000 - 2022 Silverpeas

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

<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>

<%
	Form formUpdate = (Form) request.getAttribute("Form");

	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("myForm");
	context.setFormIndex("0");
	context.setBorderPrinted(false);
%>

<view:sp-page>
<view:sp-head-part>
<% formUpdate.displayScripts(out, context); %>
</view:sp-head-part>
<view:sp-body-part cssClass="yui-skin-sam">
<view:window popup="true" browseBarVisible="false">
<view:frame>
	<form name="myForm" method="post" action="UpdateXMLForm" enctype="multipart/form-data">
	<%
	formUpdate.display(out, context);
	%>
	</form>

</view:frame>
</view:window>
</view:sp-body-part>
</view:sp-page>