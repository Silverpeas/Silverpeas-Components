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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>

<%
	String hostSpaceName = (String) request.getAttribute("SpaceName");
	String hostComponentName = (String) request.getAttribute("ComponentName");
  String[] emailErrors = (String[])request.getAttribute("EmailErrors");
	String returnUrl = "Accueil";
  if (request.getAttribute("ReturnUrl") != null) {
	  returnUrl = (String) request.getAttribute("ReturnUrl");
  }
%>

<body bgcolor="#FFFFFF">

<%
	browseBar.setDomainName(hostSpaceName);
	browseBar.setComponentName(hostComponentName);

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<%
Button closeButton = (Button) gef.getFormButton(resource.getString("GML.ok"), returnUrl, false);
%>

<table align="center" cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
	<tr><td>
		<table align="center" cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
			<tr><td>
				<table border="0" cellpadding="1" cellspacing="1" align="center">
					<tr>
						<td align="left" class="textePetitBold"><%=EncodeHelper.javaStringToHtmlString(resource.getString("infoLetter.sended"))%></td>
					</tr>
                    <% 
                        if (emailErrors.length > 0)
                        {
                    %>
					<tr><td><br/></td>
					</tr>
					<tr>
						<td align="left"><%=EncodeHelper.javaStringToHtmlString(resource.getString("infoLetter.emailErrors"))%></td>
					</tr>
                    <% 
                            for (int i = 0; i < emailErrors.length; i++)
                            {
                    %>
					<tr>
						<td align="left"><%=EncodeHelper.javaStringToHtmlString(emailErrors[i])%></td>
					</tr>
                    <% 
                            }
                        }
                    %>
				</table>
			</td></tr>
		</table>
	</td></tr>
</table>
<%		
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(closeButton);
	buttonPane.setHorizontalPosition();
	out.println(frame.printMiddle());
	out.println("<br/><center>"+buttonPane.print()+"<br/></center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</body>
</html>
