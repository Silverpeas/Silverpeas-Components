<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

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