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
<FORM NAME="myForm" METHOD="POST" ACTION="OrderCreate" ENCTYPE="multipart/form-data">
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