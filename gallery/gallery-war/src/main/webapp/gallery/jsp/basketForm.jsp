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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>

<%
Form 			formUpdate 		= (Form) request.getAttribute("Form");
DataRecord 		data 			= (DataRecord) request.getAttribute("Data"); 
Collection		path 			= (Collection) request.getAttribute("Path");

String 			charteUrl		= (String) request.getAttribute("CharteUrl");

PagesContext 	context 		= new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
context.setBorderPrinted(false);

%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<% 
if (formUpdate != null)
	formUpdate.displayScripts(out, context); 
%>
<script language="javaScript">

function B_VALIDER_ONCLICK()
{
	if (isCorrectForm())
	{
		if (!document.myForm.CheckCharte.checked)
		{
			var errorMsg = "<%=resource.getString("gallery.mesValidCharte")%>";
			window.alert(errorMsg);
		}
		else
		{
			document.myForm.submit();
		}
	}
}
</script>
</HEAD>
<BODY>
<%
    Board board = gef.getBoard();
    
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(displayPath(path));

	Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "BasketView", false);
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
	
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    
%>
<FORM NAME="myForm" METHOD="POST" ACTION="OrderCreate" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
	<% 
	if (formUpdate != null)
	{
		formUpdate.display(out, context, data);
	}
	
	if (StringUtil.isDefined(charteUrl))
	{
		%>
		<!--  ajout de la zone de la charte -->
		<iframe src="<%=charteUrl%>" height="200" width="600" scrolling="auto"></iframe> 
		<br/>
		<table>
			<tr>
				<td><input type="checkbox" name="CheckCharte"/> </td><td><b><%=resource.getString("gallery.validCharte")%></b></td>
			</tr>
		</table>
	<%} %>	 
</form>
<%
	out.println(board.printAfter());
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<br/><center>"+buttonPane.print()+"</center>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>