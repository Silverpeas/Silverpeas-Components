<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function openViewParution(par) {
    document.viewParution.parution.value = par;
    document.viewParution.submit();
}
</script>
</head>
<body>
<%
boolean showHeader = (Boolean) request.getAttribute("showHeader");
boolean isSuscriber = "true".equals(request.getAttribute("userIsSuscriber"));
if (isSuscriber) {
	operationPane.addOperation(resource.getIcon("infoLetter.desabonner"), resource.getString("infoLetter.desabonner"), "UnsuscribeMe");
} else {
	operationPane.addOperation(resource.getIcon("infoLetter.abonner"), resource.getString("infoLetter.abonner"), "SuscribeMe");
}
out.println(window.printBefore());
%>
<view:frame>
<% if (showHeader) { %>
<view:board>
	<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<tr>
			<td class="txtlibform" valign="baseline" nowrap="nowrap"><%=resource.getString("infoLetter.name")%> :</td>
			<td align="left" width="100%"><%= (String) request.getAttribute("letterName") %></td>
		</tr>
		<tr>
			<td class="txtlibform" valign="top" nowrap="nowrap"><%=resource.getString("GML.description")%> :</td>
			<td align="left"><%= EncodeHelper.javaStringToHtmlParagraphe((String) request.getAttribute("letterDescription")) %></td>
		</tr>
		<tr>
			<td class="txtlibform" valign="baseline" nowrap="nowrap"><%=resource.getString("infoLetter.frequence")%> :</td>
			<td align="left"><%= (String) request.getAttribute("letterFrequence") %></td>
		</tr>
	</table>
</view:board>
<br/>
<% } %>
<view:areaOfOperationOfCreation/>
<%
// Recuperation de la liste des parutions
List<InfoLetterPublication> publications = (List<InfoLetterPublication>) request.getAttribute("listParutions");
int i=0;
				ArrayPane arrayPane = gef.getArrayPane("InfoLetter", "Main", request, session);
		        //arrayPane.setVisibleLineNumber(10);

		        arrayPane.setTitle(resource.getString("infoLetter.listParutions"));

		        ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
				arrayColumn0.setSortable(false);

				arrayPane.addArrayColumn(resource.getString("infoLetter.name"));
				arrayPane.addArrayColumn(resource.getString("GML.date"));
				// ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.operation"));
				// arrayColumn.setSortable(false);
if (publications.size()>0) {
	for (i = 0; i < publications.size(); i++) {
						InfoLetterPublication pub = (InfoLetterPublication) publications.get(i);
						if (pub._isValid()) {
							ArrayLine arrayLine = arrayPane.addArrayLine();

							IconPane iconPane1 = gef.getIconPane();
							Icon debIcon = iconPane1.addIcon();
							debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "#");
							arrayLine.addArrayCellIconPane(iconPane1);

							arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(pub.getTitle()), "javascript:openViewParution('" + pub.getPK().getId() + "');");

							java.util.Date date = DateUtil.parse(pub.getParutionDate());
							ArrayCellText cell = arrayLine.addArrayCellText(resource.getOutputDate(date));
							cell.setCompareOn(date);
						}
	}
}
		out.println(arrayPane.print());

%>
<form name="viewParution" action="View" method="post">
	<input type="hidden" name="parution" value=""/>
</form>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>