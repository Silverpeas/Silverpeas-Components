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

<%@ include file="checkQuickInfo.jsp" %>

<%
	Iterator infosI = (Iterator) request.getAttribute("infos");
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - Portlet</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript" src="../../util/javaScript/formUtil.js"></script>

<% out.println(gef.getLookStyleSheet()); %>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" class="txtlist">
<form name="quickInfoForm" method="post">
  <%
	ArrayPane arrayPane = gef.getArrayPane(quickinfo.getComponentId() + "QuickinfoList", pageContext);
	arrayPane.addArrayColumn(null);

	String 	st 		= null;
	ArrayCellText cellText = null;
	String description = "";
	while (infosI.hasNext()) {
		PublicationDetail pub = (PublicationDetail) infosI.next();
		ArrayLine line = arrayPane.addArrayLine();

		st = "<B>"+pub.getName()+"</B>";
		if (pub.getWysiwyg() != null && !"".equals(pub.getWysiwyg()))
    	description = pub.getWysiwyg();
		else if (pub.getDescription() != null && !pub.getDescription().equals(""))
			description = Encode.javaStringToHtmlParagraphe(pub.getDescription());
		st = st + "<br/>"+description;
		cellText = line.addArrayCellText(st);
		cellText.setValignement("top");
	}
	out.println(arrayPane.print());
  %>
</form>
</body>
</html>
