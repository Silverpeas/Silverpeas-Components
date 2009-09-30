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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.util.*"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>


<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>


<%@ include file="../checkChat.jsp" %>

<%
	String pubId = (String) request.getParameter("PubId");
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(chatScc.getLanguage());
%>

<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \________________________________________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">


function ValidateUsers() {
	document.EDform.submit();
}

</script>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>

<%

    browseBar.setDomainName(chatScc.getSpaceLabel());
    browseBar.setComponentName(chatScc.getComponentLabel());

	out.println(window.printBefore());

		out.println(frame.printBefore());

		//button
		ButtonPane buttonPane = gef.getButtonPane();
		Button cancelButton = (Button) gef.getFormButton(chatScc.getString("chat.annuler"), "javascript:onClick=window.close();", false);
		Button validateButton = (Button) gef.getFormButton(chatScc.getString("chat.valider"), "javascript:onClick=ValidateUsers();", false);
		Button closeButton = (Button) gef.getFormButton(chatScc.getString("chat.fermer"), "javascript:onClick=window.close();", false);

		//Icons
		String noColorPix = m_context + "/util/icons/colorPix/1px.gif";
%>
			<FORM name="EDform" Action="ToAlert" METHOD="POST">
<CENTER>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="0" class="contourintfdcolor" width="100%"><!--tabl1-->
				<TR>
					<TD align="center" class="txttitrecol" colspan="2">
						<%=chatScc.getString("chat.user")%>
					</TD>
				</TR>
				<TR>
					<TD colspan="2" align="center" class="intfdcolor" height="1" width="70%"><img src="<%=noColorPix%>"></TD>
				</TR>
				
				<%
					String[] nameUser = chatScc.getNameSelectedUsers();
					String[]  lastnameUser = chatScc.getLastnameSelectedUsers();
					if(nameUser.length>0){
						for(int i=0; i<nameUser.length; i++){
							String actorName = lastnameUser[i] + " " + nameUser[i];
							//code html inclus dans la boucle
							%>
							<TR>
								<TD align="center" colspan="2">
									<%=actorName%>
								</TD>
							</TR>
							<%
						}
						%>
				<TR width="70%">
					<TD colspan="2" align="center" class="intfdcolor"  height="1" width="70%"><img src="<%=noColorPix%>"></TD>
				</TR>
				<TR>
					<TD colspan="2" align="center" class="txtlibform">
								<b><%=chatScc.getString("chat.authorMessage")%></b> : <BR><textarea cols="40" rows="4" name="MessageAux"></textarea>
					</TD>
				</TR>
						<%
					}
					else{
						%>
				<TR>
					<TD align="center" colspan="2">
									<%=chatScc.getString("NotUser")%>
					</TD>
				</TR>
						<%
					}
					%>

			</TABLE>
		</td>
	</tr>
</table>
</CENTER>
	<input type="hidden" name="PubId" value="<%=pubId%>">
</FORM>
<%
	if(nameUser.length>0){
		buttonPane.addButton(validateButton);
		buttonPane.addButton(cancelButton);
	}
	else{
		buttonPane.addButton(closeButton);
	}
	buttonPane.setHorizontalPosition();
	out.println("<BR><center>"+buttonPane.print()+"<br></center>");

      out.println(frame.printAfter());
      out.println(window.printAfter());
%>

</BODY>
</HTML>