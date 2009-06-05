<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>

<%
	String hostSpaceName = (String) request.getAttribute("SpaceName");
	String hostComponentName = (String) request.getAttribute("ComponentName");
  String[] emailErrors = (String[])request.getAttribute("EmailErrors");
	String returnUrl = "Accueil";
  if (request.getAttribute("ReturnUrl") != null)
	  returnUrl = (String) request.getAttribute("ReturnUrl");
%>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>

<%
	browseBar.setDomainName(hostSpaceName);
	browseBar.setComponentName(hostComponentName);

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<%
Button closeButton = (Button) gef.getFormButton(resource.getString("GML.ok"), returnUrl, false);
%>

<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr><td>
		<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<tr><td>
				<TABLE border=0 cellPadding=1 cellSpacing=1 align="center">
					<TR>
						<TD align="left" class="textePetitBold"><%=Encode.javaStringToHtmlString(resource.getString("infoLetter.sended"))%></TD>
					</TR>
                    <% 
                        if (emailErrors.length > 0)
                        {
                    %>
					<TR><td><br></td>
					</TR>
					<TR>
						<TD align="left"><%=Encode.javaStringToHtmlString(resource.getString("infoLetter.emailErrors"))%></TD>
					</TR>
                    <% 
                            for (int i = 0; i < emailErrors.length; i++)
                            {
                    %>
					<TR>
						<TD align="left"><%=Encode.javaStringToHtmlString(emailErrors[i])%></TD>
					</TR>
                    <% 
                            }
                        }
                    %>
				</TABLE>
			</td></tr>
		</table>
	</td></tr>
</table>
<%		
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(closeButton);
	buttonPane.setHorizontalPosition();
	out.println(frame.printMiddle());
	out.println("<BR><center>"+buttonPane.print()+"<br></center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>
