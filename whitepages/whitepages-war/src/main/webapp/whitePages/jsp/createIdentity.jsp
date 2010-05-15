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

<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
		
	browseBar.setDomainName(spaceLabel);
	browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "javascript:goToMain();");

	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.createCard"));
	
	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"createIdentity", true, false);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"createCard", false, true);
	
	
	Card card = (Card) request.getAttribute("card");
	Form userForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 

%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<SCRIPT LANGUAGE="JavaScript">
	function goToMain()
	{
		if (window.confirm("<%=resource.getString("whitePages.messageCancelCreate")%>"))
		{
			<% if (containerContext == null) { %>
			   location.href = "Main";
			<% } else { %>
			   location.href = "<%= m_context + containerContext.getReturnURL()%>"; 
			<% } %>
		}
	}
</SCRIPT>
</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM NAME="myForm" METHOD="POST" ACTION="#">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>

<center>
<%
	userForm.display(out, context, data);
%>
<br>
</center>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</FORM>
</BODY>
</HTML>
