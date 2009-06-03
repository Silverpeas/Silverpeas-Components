
<%@ include file="checkWhitePages.jsp" %>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
   browseBar.setDomainName(spaceLabel);
   if (containerContext == null) {
       browseBar.setComponentName(componentLabel, "Main");
   } else {
      browseBar.setComponentName(componentLabel, 
	                              m_context+containerContext.getReturnURL()); 
   }

	out.println(window.printBefore());
 
    
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center>

<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align="center"> 
					<td  class="intfdcolor4" valign="baseline" align="center">
						<span class="txtnav"><%=resource.getString("whitePages.messageUserDeleted")%>.&nbsp;</span>
					</td>
				</tr>
		</table>
		</td>
	</tr>
</table>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
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