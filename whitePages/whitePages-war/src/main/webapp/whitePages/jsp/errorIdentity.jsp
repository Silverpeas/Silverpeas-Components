<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkWhitePages.jsp" %>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</HEAD>
<BODY>
<%
   browseBar.setDomainName(spaceLabel);
   browseBar.setComponentName(componentLabel, "Main");

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>

<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr>
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align="center">
					<td  class="intfdcolor4" valign="baseline" align="center">
						<span class="txtnav"><%=resource.getString("whitePages.messageUserDeleted")%>.&nbsp;</span>
					</td>
				</tr>
		</table>
		</td>
	</tr>
</table>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
    out.println(buttonPane.print());
%>

</center>
<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</BODY>
</HTML>