<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@page import="org.silverpeas.components.yellowpages.ImportReport"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkYellowpages.jsp" %>

<c:set var="help"><%=resources.getString("yellowpages.importCSVHelp1")%><br/><%=resources.getString("yellowpages.importCSVHelp2")%></c:set>

<html>
<head>
<%
	ImportReport report = (ImportReport) request.getAttribute("Result");
	if (report != null) {
	  	if (report.getNbAdded() > 0) {
			%>
			<script type="text/javascript">
				window.opener.document.refreshList.submit();
			</script>
			<%
		}
	}
%>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="qtip"/>
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
        jQuery.popup.error(errorMsg);
    }
}

whenSilverpeasReady(function() {
  TipManager.simpleHelp(".highlight-silver", "${help}");
});

</script>
</head>
<body>
<view:browseBar extraInformations='<%=resources.getString("yellowpages.importCSV")%>'/>
<view:window popup="true">
<view:frame>
<view:board>
<form name="csvFileForm" action="ImportCSV" method="POST" enctype="multipart/form-data"  accept-charset="UTF-8">
    <table width="100%">
			<% if (report != null) { %>
				<% if (report.getNbAdded() > 0) { %>
				<tr>
					<td align="center">
						<b><%=report.getNbAdded()%>&nbsp;<%=resources.getString("yellowpages.contactsAdded")%></b>
					</td>
				</tr>
				<% } %>
				<% if (report.isInError()) { %>
				<tr>
					<td>
						<b><%=resources.getString("yellowpages.importCSVError")%></b><br/>
						<ul>
						<% for (String error : report.getErrors()) { %>
							<li><%=error%></li>
						<% } %>
						</ul>
					</td>
				</tr>
				<% } %>
			<% } else { %>
		        <tr>
		            <td class="txtlibform">
		                <%=resources.getString("GML.csvFile") %> :
							<a href="#" class="highlight-silver"><img src="<%=m_context%>/util/icons/info.gif" alt=""></a>
		            </td>
		            <td align="left" valign="baseline">
		                <input type="file" name="file_upload" size="50" maxlength="50" VALUE="">
		                &nbsp;<img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5">
		            </td>
		        </tr>
		        <tr>
		            <td colspan="2"><img border="0" src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5"/>
		      : <%=resources.getString("GML.requiredField")%></td>
		        </tr>
			<% } %>
    </table>
</form>
</view:board>
<%
  ButtonPane bouton = gef.getButtonPane();
	if (report != null) {
		bouton.addButton(gef.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false));
	} else {
		bouton.addButton(gef.getFormButton(resources.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
		bouton.addButton(gef.getFormButton(resources.getString("GML.cancel"), "javascript:window.close()", false));
	}
  out.println(bouton.print());
%>
</view:frame>
</view:window>
</body>
</html>