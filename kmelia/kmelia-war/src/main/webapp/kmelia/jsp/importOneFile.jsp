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

<%@ page import="com.stratelia.silverpeas.versioning.model.DocumentVersion"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkKmelia.jsp" %>

<%
	String topicId = request.getParameter("TopicId");
	String message = "";
	if (request.getAttribute("Message") != null) {
		message = (String) request.getAttribute("Message");
	}

//Icons
Button cancelButton = gef.getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=validateForm();", false);
%>
<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript">
function validateForm() {
	formValid = false;
	fileName = document.frm_import.file_name.value;
	if (fileName != "") {
		formValid = true;
	}			
	if (formValid) {
		var obj = document.getElementById("Processing");
		if (obj != null) {
			obj.style.visibility = "visible";
		}
		obj = document.getElementById("ImgProcessing");
		if (obj != null) {
			obj.style.visibility = "visible";
		}
		document.frm_import.submit();	
	}
}
</script>
</head>
<body>
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setPath(kmeliaScc.getString("kmelia.ImportFile"));
	
  Frame frame = gef.getFrame();

  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<view:board>
<form name="frm_import" action="ImportFileUpload" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
		<input type="hidden" name="topicId" value="<%=topicId%>"/>
		<input type="hidden" name="opt_importmode" value="0"/>
		<table cellpadding="5" cellspacing="0" width="100%">
	    <tr align="center">
		    <td class="txtlibform"><%=kmeliaScc.getString("kmelia.ImportModeUnitaireTitre")%>&nbsp;</td>
		  </tr>
		  <% if (kmeliaScc.isDraftEnabled() && !kmeliaScc.isPDCClassifyingMandatory()) { %>
		    <tr>
			    <td><%=kmeliaScc.getString("kmelia.DraftMode")%>&nbsp;<input type="checkbox" name="chk_draft" value="true"/></td>
			  </tr>
			<% } %>
		  <% if (kmeliaScc.isVersionControlled()) { %>
		    <tr>
			    <td><%=kmeliaScc.getString("kmelia.TypeVersion")%>&nbsp;
					<input type="radio" name="opt_versiontype" value="<%=DocumentVersion.TYPE_DEFAULT_VERSION%>" checked="checked"/><%=kmeliaScc.getString("kmelia.PrivateVersion")%>&nbsp;
					<input type="radio" name="opt_versiontype" value="<%=DocumentVersion.TYPE_PUBLIC_VERSION%>"/><%=kmeliaScc.getString("kmelia.PublicVersion")%>
			    </td>
			  </tr>
		<% } %>
	    <tr>
		    <td><%=kmeliaScc.getString("kmelia.FileToImport")%>&nbsp;<input type="file" name="file_name" size="50" value=""/></td>
		  </tr>
			<% if (!message.equals(""))	{ %>
			    <tr>
				    <td align="center"><span class="MessageReadHighPriority"><%=message%></span></td>
				  </tr>
			<% } %>
			<tr>
				<td>
					<div align="center" id="ImgProcessing" style="visibility:hidden"><img src="<%=resources.getIcon("kmelia.progress")%>"/></div>
				</td>
			</tr>
			<tr>
				<td>
					<div align="center" id="Processing" style="visibility:hidden"><b><%=kmeliaScc.getString("kmelia.Processing")%></b></div>
				</td>				
			</tr>
		</table>
</form>
</view:board>
<div align="center"><br/>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
%>
<br/>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</div>
</body>
</html>