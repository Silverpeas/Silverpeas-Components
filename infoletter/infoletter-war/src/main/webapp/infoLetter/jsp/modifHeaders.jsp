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
<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function submitForm() {
	if (!isValidTextArea(document.changeLetterHeaders.description)) {
		window.alert("<%= resource.getString("infoLetter.soLongPal") %>");
	} else {
		if (document.changeLetterHeaders.name.value=="") {
			alert("<%= resource.getString("infoLetter.fuckingNameRequired") %>");
		} else {
			document.changeLetterHeaders.action = "ChangeLetterHeaders";
			document.changeLetterHeaders.submit();
		}
	}
}

function cancelForm() {
    document.changeLetterHeaders.action = "Accueil";
    document.changeLetterHeaders.submit();
}
</script>
</head>
<body>
<%
	browseBar.setPath(resource.getString("infoLetter.modifierHeaderBB"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<form name="changeLetterHeaders" action="ChangeLetterHeaders" method="post">
<table width="98%" border="0" cellspacing="0" cellpadding="0">
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr> 
					<td class="txtlibform"><%=resource.getString("infoLetter.name")%> :</td>
					<td valign="baseline" align="left"><input type="text" name="name" size="50" maxlength="50" value="<%= (String) request.getAttribute("letterName") %>"/>&nbsp;<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"/></td>
				</tr>
				<tr> 
					<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
					<td valign="top" align="left"><textarea cols="49" rows="4" name="description"><%= (String) request.getAttribute("letterDescription") %></textarea></td>
				</tr>
				<tr> 
					<td class="txtlibform"><%=resource.getString("infoLetter.frequence")%> :</td>
					<td valign="baseline" align="left"><input type="text" name="frequence" size="50" maxlength="50" value="<%= (String) request.getAttribute("letterFrequence") %>"/></td>
				</tr>
				<tr>				 
					<td colspan="2"><span class="txt">(<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%>)</span></td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</form>

<br/>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>