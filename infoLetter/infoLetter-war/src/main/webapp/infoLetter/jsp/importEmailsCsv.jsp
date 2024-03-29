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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
	browseBar.setPath(resource.getString("infoLetter.importEmailsCsv"));

	String result = request.getParameter("Result");
	boolean importOk = false;
	if ("OK".equals(result)) {
		importOk = true;
		%>
		<script language="javascript">
      window.opener.document.refreshEmails.submit();
		</script>
		<%
	}
%>

<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">
function SubmitWithVerif(verifParams) {
    var csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
    var errorMsg = "";

    if (verifParams) {
      if (isWhitespace(csvFilefld)) {
        errorMsg = "<% out.print(resource.getString("GML.thefield")+resource.getString("GML.csvFile")+resource.getString("GML.mandatory")); %>";
      } else {
			  var ext = csvFilefld.substring(csvFilefld.length - 4);
	      if (ext.toLowerCase() != ".csv") {
			    errorMsg = "<% out.print(resource.getString("GML.errorCsvFile")); %>";
		    }
		  }
    }
    if (errorMsg == "") {
        document.csvFileForm.submit();
    } else {
        jQuery.popup.error(errorMsg);
    }
}
</script>
</head>
<body>
<view:window popup="true">
<view:frame>
<view:board>
<form name="csvFileForm" action="ImportEmailsCsv" method="post" enctype="multipart/form-data">

			<% if (importOk) { %>
			<div class="inlineMessage-ok">
				<%=resource.getString("infoLetter.importEmailsCsvSucceed") %>
			</div>
			<% } else { %>
				<div class="inlineMessage">
						<%=resource.getString("infoLetter.importEmailsCsvWarning") %>
				</div>
	<table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr>
            <td class="txtlibform"><%=resource.getString("GML.csvFile") %> :</td>
            <td>
                <input type="file" name="file_upload" size="50" maxlength="50" value=""/>&nbsp;<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
            </td>
        </tr>
        <tr>
            <td colspan="2"><img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%></td>
        </tr>
		<% } %>
    </table>
</form>
</view:board>
		<%
		  ButtonPane bouton = gef.getButtonPane();
			if (importOk) {
				bouton.addButton(gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));
			} else {
				bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
			bouton.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
			}
		  out.println(bouton.print());
		%>
</view:frame>
</view:window>
</body>
</html>