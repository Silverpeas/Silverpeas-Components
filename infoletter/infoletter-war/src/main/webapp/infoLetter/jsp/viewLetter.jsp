<%--
  Copyright (C) 2000 - 2015 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<%
String parutionTitle = (String) request.getAttribute("parutionTitle");
String parution = (String) request.getAttribute("parution");
%>
<script type="text/javascript">
function goFiles (){
	document.attachedFiles.submit();
}
</script>
</head>
<body>
<%
	browseBar.setPath(EncodeHelper.javaStringToHtmlString(parutionTitle));
	out.println(window.printBefore());
%>
<view:frame>
	<table width="100%">
		<tr><td width="80%">
		<view:displayWysiwyg objectId="<%=parution%>" componentId="<%=componentId %>" language="<%=I18NHelper.defaultLanguage %>" />
		</td>
		<td valign="top">
			<%
				out.flush();
				getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+parution+"&ComponentId="+componentId+"&Context=attachment").include(request, response);
			%>
		</td></tr>
	</table>
	<form name="attachedFiles" action="FilesView" method="POST">	
		<input type="hidden" name="parution" value="<%= parution %>"/>
	</form>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>