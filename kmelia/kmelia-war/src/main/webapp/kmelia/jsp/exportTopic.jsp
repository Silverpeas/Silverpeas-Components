<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%

String topicId		= request.getParameter("TopicId");
String formAction 	= "ExportTopic";
if (!StringUtil.isDefined(topicId) || "0".equals(topicId))
	formAction = "ExportComponent";

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button exportButton = gef.getFormButton(resources.getString("GML.export"), "javascript:onClick=validateForm();", false);

%>

<html>
<head><title><%=resources.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="javaScript">
function validateForm() {
	var obj = document.getElementById("Processing");
	if (obj != null)
		obj.style.visibility = "visible";
	obj = document.getElementById("ImgProcessing");
	if (obj != null)
		obj.style.visibility = "visible";
	document.frm_exportComponent.submit();	
}
</script>
</head>
<body>
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel);
  browseBar.setPath(resources.getString("kmelia.ExportTitle"));
	
  Frame frame = gef.getFrame();
  Board board = gef.getBoard();

  out.println(window.printBefore());
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
	<TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
		<tr>
			<td align="center"><b><%=resources.getString("kmelia.ExportTitle")%></b></td>
		</tr>
		<tr>
		  	<td align="center"><%=resources.getString("kmelia.WarningExport")%></td>
		</tr>
		<tr>
			<td>
				<div align="center" id="ImgProcessing" style="visibility:hidden"><img src="<%=resources.getIcon("kmelia.progress")%>" border="0"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div align="center" id="Processing" style="visibility:hidden"><b><%=resources.getString("kmelia.ExportProcessing")%></b></div>
			</td>				
		</tr>
	</table>
<%
	out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(exportButton);
    buttonPane.addButton(cancelButton);
    out.println("<br/><center>"+buttonPane.print()+"</center>");
    
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="frm_exportComponent" action="<%=formAction%>" Method="POST">
	<input type="hidden" name="TopicId" value="<%=topicId%>"/>
</form>
</body>
</html>