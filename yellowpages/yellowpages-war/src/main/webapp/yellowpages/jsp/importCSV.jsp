<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="checkYellowpages.jsp" %>

<html>
<head>
<%
	Window window = gef.getWindow();
	OperationPane operationPane = window.getOperationPane();
	Board board = gef.getBoard();
	Frame frame = gef.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setClickable(false);
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resources.getString("yellowpages.importCSV"));
	boolean importOk = false;
	String message = "";

	if (request.getAttribute("Result") != null)
	{
		HashMap result = (HashMap) request.getAttribute("Result");
		importOk = result.get(new Boolean(true))==null;
		if (importOk)
		{
			%>
			<script language="javascript">
				window.opener.document.refreshList.submit();
			</script>
			<%
			message = (String) result.get(new Boolean(false));
		}
		else
		{
			message = (String) result.get(new Boolean(true));
		}

	}

%>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/infoHighlight.js"></script>

<script type="text/javascript">

function SubmitWithVerif(verifParams)
{
    var csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(csvFilefld)) {
            errorMsg = "<% out.print(resources.getString("GML.thefield")+resources.getString("GML.csvFile")+" "+resources.getString("GML.isRequired")); %>";
         } else {
			var ext = csvFilefld.substring(csvFilefld.length - 4);

    	    if (ext.toLowerCase() != ".csv") {
    			errorMsg = "<% out.print(resources.getString("GML.errorCsvFile")); %>";
    		}
		}
    }
    if (errorMsg == "")
    {
        document.csvFileForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}

</script>
</head>
<body>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<form name="csvFileForm" action="ImportCSV" method="POST" enctype="multipart/form-data"  accept-charset="UTF-8">
    <table width="100%">
			<% if (importOk)
			{ %>
				<tr>
					<td align="center">
						<%=resources.getString("yellowpages.importCSVSucceed")%>
					</td>
				</tr>
				<tr>
					<td align="center">
						<b><%=message%>&nbsp;<%=resources.getString("yellowpages.contactsAdded")%></b>
					</td>
				</tr>
			<%
			}
			else if (StringUtil.isDefined(message))
			{ %>
				<tr>
					<td colspan="2">
						<b><%=resources.getString("yellowpages.importCSVError")%></b><br>
						<%=message%>
					</td>
				</tr>
			<%
			}
			else { %>
		        <tr>
		            <td class="txtlibform">
		                <%=resources.getString("GML.csvFile") %> :
							<a href="#" class="highlight-silver" title="<%=resources.getString("yellowpages.importCSVHelp1")%><br><%=resources.getString("yellowpages.importCSVHelp2")%>"><img src="<%=m_context%>/util/icons/info.gif" alt="<%=resources.getString("yellowpages.importCSVHelp1")%> <%=resources.getString("yellowpages.importCSVHelp2")%>"></a>
		            </td>
		            <td align="left" valign="baseline">
		                <input type="file" name="file_upload" size="50" maxlength="50" VALUE="">
		                &nbsp;<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5">
		            </td>
		        </tr>
		        <tr>
		            <td colspan="2">(<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5">
		      : <%=resources.getString("GML.requiredField")%>)</td>
		        </tr>
			<% } %>
    </table>
<%
out.println(board.printAfter());
%>
</form>
<br/>
<center>
<%
  ButtonPane bouton = gef.getButtonPane();
	if (request.getAttribute("Result") != null)
	{
		bouton.addButton((Button) gef.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false));
	}
	else
	{
		bouton.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
		bouton.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:window.close()", false));
	}
  out.println(bouton.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>