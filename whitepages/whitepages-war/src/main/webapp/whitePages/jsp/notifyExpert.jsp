<%@ include file="checkWhitePages.jsp" %>

<%
		
	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.notifyExpert"));
	
	String notifiedExpert = (String) request.getAttribute("notifiedExpert");
		
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.ok"), "javascript:onClick=B_SEND_ONCLICK();", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
	
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>

<script language="JavaScript">
<!--	
	function B_SEND_ONCLICK() {
		 document.forms[0].submit();
	}
//-->
</script>	
</HEAD>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM NAME="myForm" METHOD="POST" ACTION="sendExpertNotification">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>

<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap width="200" class="txttitrecol">
			<%=resource.getString("whitePages.expertName")%>
		</td>
		<td nowrap>
			<%=notifiedExpert%>
		</td>
	</tr>
	<tr> 
		<td nowrap width="200" class="txttitrecol">
			<%=resource.getString("whitePages.message")%>
		</td>
		<td nowrap>
			<textarea cols="80" rows="8" name="messageToExpert"></textarea>
		</td>
	</tr>
</table>
<br>
<%=buttonPane.print() %>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</FORM>
</BODY>
</HTML>