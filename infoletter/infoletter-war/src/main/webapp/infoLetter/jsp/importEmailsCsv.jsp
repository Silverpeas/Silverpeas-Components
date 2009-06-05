<%@ include file="check.jsp" %>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "javascript:window.close();");
	browseBar.setPath("<a href=\"Accueil\"></a> " + resource.getString("infoLetter.importEmailsCsv"));
	
	String result = (String) request.getParameter("Result");
	boolean importOk = false;
	if ("OK".equals(result))
	{
		importOk = true;
		%>
		<script language="javascript">
      window.opener.document.refreshEmails.submit();
		</script>
		<%
	}
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">

function SubmitWithVerif(verifParams)
{
    var csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(csvFilefld)) {
            errorMsg = "<% out.print(resource.getString("GML.thefield")+resource.getString("GML.csvFile")+resource.getString("CSV.isRequired")); %>";
         } else {
			var ext = csvFilefld.substring(csvFilefld.length - 4);
	        
    	    if (ext.toLowerCase() != ".csv") {
    			errorMsg = "<% out.print(resource.getString("GML.errorCsvFile")); %>";		
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
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<form name="csvFileForm" action="ImportEmailsCsv" method="POST" enctype="multipart/form-data">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
			<% if (importOk)
			{ %>
				<tr>
					<td colspan="2" align="center">
						<%=resource.getString("infoLetter.importEmailsCsvSucceed") %>
					</td>
				</tr>
			<%
			}
			else
			{ %>
				<tr>
					<td colspan="2">
						<%=resource.getString("infoLetter.importEmailsCsvWarning") %>
					</td>
				</tr>
        <tr>			
            <td valign="baseline" align=left  class="txtlibform">
                <%=resource.getString("GML.csvFile") %> :
            </td>
            <td align=left valign="baseline">
                <input type="file" name="file_upload" size="50" maxlength="50" VALUE="">&nbsp;<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"> 
            </td>
        </tr>
        <tr> 
            <td colspan="2">(<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"> 
      : <%=resource.getString("GML.requiredField")%>)</td>
        </tr>
		<% } %>
    </table>

<%
out.println(board.printAfter());
%>
</form>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
			if (importOk)
			{
				  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));
			}
			else
			{
				  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
		      bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
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