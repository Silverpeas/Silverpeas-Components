<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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

<html>
<head>
<title>QuickInfo - User</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<%@ include file="scriptClipboard_js.jsp.inc" %>

<%
	out.println(gef.getLookStyleSheet());
%>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" class="txtlist">
<form name="quickInfoForm" method="post">
  <%
	Window mainWin = gef.getWindow();
	BrowseBar browseBar= mainWin.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");

	Frame maFrame = gef.getFrame();

	OperationPane operationPane = mainWin.getOperationPane();

	// Clipboard
	operationPane.addOperation(m_context+"/util/icons/copy.gif", generalMessage.getString("GML.copy"), "javascript:onClick=ClipboardCopy()");

	out.println(mainWin.printBefore());
	out.println(maFrame.printBefore());
  %>

  <%
	//Collection infos = Les quickInfos visibles
	Iterator infosI = (Iterator) request.getAttribute("infos");

	ArrayPane arrayPane = gef.getArrayPane("quickinfoList", pageContext);
	arrayPane.addArrayColumn(null);
	ArrayColumn arrayColumnOp = arrayPane.addArrayColumn("<A HREF=\"javascript:void(0)\" onMouseDown=\"return SwitchSelection(quickInfoForm, 'selectItem', event)\" onClick=\"return false\">"+resources.getString("GML.selection")+"</A>");
	arrayColumnOp.setSortable(false);

	int index = 0;
	ArrayCellText cellText = null;
	while (infosI.hasNext()) {
		PublicationDetail pub = (PublicationDetail) infosI.next();
		ArrayLine line = arrayPane.addArrayLine();
		String st = "<B>" + pub.getName() + "</B>";
		if (pub.getDescription() != null)
		   st = st + "<BR>" + Encode.javaStringToHtmlParagraphe(pub.getDescription());
		line.addArrayCellText(st);
		cellText = line.addArrayCellText("<input type=checkbox name='selectItem"+index+"' value='"+pub.getPK().getId()+"'>");
		cellText.setValignement("top");
		index++;
	}
	out.println(arrayPane.print());

	out.println(maFrame.printAfter());
	out.println(mainWin.printAfter());
  %>
</form>
</body>
</html>