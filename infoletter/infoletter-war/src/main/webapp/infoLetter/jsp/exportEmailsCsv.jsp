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

<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<TITLE></TITLE>
<link href="<%=m_context%>/util/styleSheets/modal-message.css" rel="stylesheet"  type="text/css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
<script language="JavaScript">
	messageObj = new DHTML_modalMessage();	// We only create one object of this class
	messageObj.setShadowOffset(5);	// Large shadow
	
	function displayStaticMessage()
	{
		messageObj.setHtmlContent("<center><table border=0><tr><td align=\"center\"><br><b><%=resource.getString("GML.ExportInProgress")%></b></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=m_context%>/util/icons/inProgress.gif\"/></td></tr></table></center>");
		messageObj.setSize(200,150);
		messageObj.setCssClassMessageBox(false);
		messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes
		messageObj.display();
	}
	
	function closeMessage()
	{
		messageObj.close();
	}
</script>
</HEAD>

<BODY>
<form name="exportForm" action="ExportEmailsCsv" METHOD=POST>
</form>
<%
	String statusMessage = "";
	boolean exportOk = false;
	String emailCsvFileName = "";
	String urlEmailCsv = "";
	
	if (StringUtil.isDefined((String) request.getAttribute("ExportOk")))
	{
		exportOk = new Boolean((String) request.getAttribute("ExportOk")).booleanValue();
		if (exportOk)
		{
			 statusMessage = resource.getString("GML.ExportSucceeded");
			 emailCsvFileName = (String) request.getAttribute("EmailCsvName");
			 urlEmailCsv = FileServerUtils.getUrlToTempDir(emailCsvFileName, emailCsvFileName, "text/csv");
		 }
		else
			 statusMessage = resource.getString("GML.ExportFailed");
	}
	else
	{ %>
		<script language="javascript">
			displayStaticMessage();
			document.exportForm.submit();
		</script>
	<%	}
%>
	  
<%
	out.println(window.printBefore());
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<CENTER>
<% if (exportOk) { %>
	<table width="100%" cellpadding="2" cellspacing="2" border="0">
		<tr>
			<td align="center"><span class="txtlibform"><%=statusMessage%></span></td>
	    </tr>
	    <tr>
	    	<td align="center"><a href="<%=urlEmailCsv%>"><%=emailCsvFileName%></a>
	    </tr>
	 </table>
	<% } %>
<%
	out.println(board.printAfter());
	Button button = gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false);
	out.print("<br/><center>"+button.print()+"</center>");
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>