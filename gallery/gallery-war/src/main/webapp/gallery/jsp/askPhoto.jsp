<%@ include file="check.jsp" %>

<% 
	// déclaration des boutons
	Button validateButton 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
    Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "javaScript:window.close()", false);
%>

<html>
		<head>
		<%
			out.println(gef.getLookStyleSheet());
		%>

		<TITLE>Titre de la fenetre</TITLE>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
		<script language="javascript">
			
			function sendData() 
			{
				if (isCorrectForm()) 
				{
					document.askPhotoForm.action = "SendAsk";
	        		document.askPhotoForm.submit();
	        		window.close();
	    		}
			}
		
			function isCorrectForm() 
			{
		     	var errorMsg = "";
		     	var errorNb = 0;
		     	var title = stripInitialWhitespace(document.askPhotoForm.Description.value);
		     	if (title == "") 
		     	{
		           	errorMsg+="  - '<%=resource.getString("gallery.request")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
		           	errorNb++;
		     	} 
		     	switch(errorNb) 
		     	{
		        	case 0 :
		            	result = true;
		            	break;
		        	case 1 :
		            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
		            	window.alert(errorMsg);
		            	result = false;
		            	break;
		        	default :
		            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
		            	window.alert(errorMsg);
		            	result = false;
		            	break;
		     	} 
		     	return result;
			}		
		</script>
	
		</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="javascript:document.askPhotoForm.Description.focus();">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<form name="askPhotoForm" method="post" action="SendAsk">
<table CELLPADDING="5" WIDTH="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("gallery.request")%> :<br><TEXTAREA ROWS="5" COLS="90" name="Description"></TEXTAREA></td>
    </tr>
</table>
</form>
<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</body>
</html>