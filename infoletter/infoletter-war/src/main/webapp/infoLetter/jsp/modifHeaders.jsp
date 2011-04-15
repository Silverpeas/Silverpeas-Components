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
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
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
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\">" + resource.getString("infoLetter.listParutions") + "</a> > " + resource.getString("infoLetter.modifierHeaderBB"));

/*
Collection listDocument = (Collection) request.getAttribute("docList");

operationPane.addOperation(m_context+icon.getString("fileBoxPlus.add"), scc.getString("fileBoxPlus.creer_doc"), "javascript:onClick=B_CREATE_ONCLICK();");	
operationPane.addLine();
operationPane.addOperation(m_context+icon.getString("fileBoxPlus.delete"), scc.getString("fileBoxPlus.del_doc"), "javascript:onClick=B_DELETE_ONCLICK('"+listDocument.size()+"');");	
*/

	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator

    
	out.println(frame.printBefore());	
	
%>

<% // Ici debute le code de la page %>

<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<form name="changeLetterHeaders" action="ChangeLetterHeaders" method="post">
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.name")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
					<input type="text" name="name" size="50" maxlength="50" value="<%= (String) request.getAttribute("letterName") %>">&nbsp;<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5">
					</td>
				</tr>
				<tr align=center> 

					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("GML.description")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
					<textarea cols="49" rows="4" name="description"><%= (String) request.getAttribute("letterDescription") %></textarea>
					</td>
				</tr>
				<tr align=center> 

					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.frequence")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
					<input type="text" name="frequence" size="50" maxlength="50" value="<%= (String) request.getAttribute("letterFrequence") %>">
					</td>
				</tr>
				<tr align=center>				 
					<td class="intfdcolor4" valign="baseline" align=left colspan=2><span class="txt">(<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</span> 
					</td>
				</tr>
			</table>
		</td>
	</tr>
</form>
</table>

<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
</center>

<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>

