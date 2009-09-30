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
<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
	var importFileWindow = window;
	var exportFileWindow = window;
	function deleteCheckedEmails() {
	    if (confirm("<%= resource.getString("infoLetter.confirmDeleteEmails") %>"))
	    {
				document.deleteEmails.action = "DeleteEmails";
				document.deleteEmails.submit();
	    }
	}

	function deleteAllEmails() {
	    if (confirm("<%= resource.getString("infoLetter.confirmDeleteAllEmails") %>"))
	    {
				document.deleteEmails.action = "DeleteAllEmails";
				document.deleteEmails.submit();
	    }
	}
	
	function displayEmailsCsvImport()
	{
	    url = "importEmailsCsv.jsp";
	    windowName = "importFileWindow";
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
			larg = "610";
			haut = "370";
			if (!importFileWindow.closed && importFileWindow.name=="importFileWindow")
				importFileWindow.close();
	    importFileWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}

	function displayEmailsCsvExport()
	{
	    url = "exportEmailsCsv.jsp";
	    windowName = "exportFileWindow";
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
			larg = "640";
			haut = "370";
			if (!exportFileWindow.closed && exportFileWindow.name=="exportFileWindow")
				exportFileWindow.close();
			exportFileWindow = SP_openWindow(url, windowName, larg, haut, windowParams); 
	}

</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<form name="refreshEmails" action="Emails">
</form>
<form name="deleteEmails" action="DeleteEmails" method="post">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\"></a> " + resource.getString("infoLetter.externSubscribers"));

	operationPane.addOperation(resource.getIcon("infoLetter.addMail"), resource.getString("infoLetter.addMail"), "addEmail.jsp");	
	operationPane.addOperation(resource.getIcon("infoLetter.importEmailsCsv"), resource.getString("infoLetter.importEmailsCsv"), "javascript:displayEmailsCsvImport();");	
	operationPane.addOperation(resource.getIcon("infoLetter.exportEmailsCsv"), resource.getString("infoLetter.exportEmailsCsv"), "javascript:displayEmailsCsvExport();");	
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("infoLetter.delMail"), resource.getString("GML.delete"), "javascript:deleteCheckedEmails();");	
	operationPane.addOperation(resource.getIcon("infoLetter.delAllMail"), resource.getString("GML.deleteAll"), "javascript:deleteAllEmails();");	
	
	out.println(window.printBefore());
	out.println(frame.printBefore());	

	// Recuperation de la liste des emails
	Vector emails = (Vector) request.getAttribute("listEmails");
	int i=0;
	ArrayPane arrayPane = gef.getArrayPane("InfoLetter", "Emails", request, session);
       //arrayPane.setVisibleLineNumber(10);
		
	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);

	arrayPane.addArrayColumn(resource.getString("GML.eMail"));
	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.operation"));
	arrayColumn.setSortable(false);		
	if (emails.size()>0) {
		for (i = 0; i < emails.size(); i++) {		
					String email = (String) emails.elementAt(i);
					ArrayLine arrayLine = arrayPane.addArrayLine();
						
					IconPane iconPane1 = gef.getIconPane();
					Icon debIcon = iconPane1.addIcon();
					debIcon.setProperties(resource.getIcon("infoLetter.pictoMail"), "");
					arrayLine.addArrayCellIconPane(iconPane1);	
							
													
					arrayLine.addArrayCellText(Encode.javaStringToHtmlString(email));
							
					arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"mails\" value=\"" + email + "\">");
		}
	}			
	out.println(arrayPane.print());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</form>
</BODY>
</HTML>