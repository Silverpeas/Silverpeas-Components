<%@ include file="checkProcessManager.jsp" %>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<tr>
		<td><img src="<%=resource.getIcon("processManager.px") %>"></td>
	</tr>
	<tr>			
		<td align="center">
<%
		ButtonPane buttonPane = gef.getButtonPane();

		buttonPane.addButton((Button) gef.getFormButton(resource.getString("processManager.print"), "javascript:window.top.contentFrame.printProcess()" , false));			

		buttonPane.addButton((Button) gef.getFormButton(resource.getString("processManager.close"), "javascript:window.top.close()" , false));
		
		out.println(buttonPane.print());
%>  
		</td>
	</tr>
	<tr>
		<td><img src="<%=resource.getIcon("processManager.px") %>"></td>
	</tr>
</table>

</BODY>
</HTML>