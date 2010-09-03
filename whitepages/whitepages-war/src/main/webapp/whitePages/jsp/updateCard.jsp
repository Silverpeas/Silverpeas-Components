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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
	browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.editCard"));
		
	Card			card		= (Card) request.getAttribute("card");
	Form			updateForm	= (Form) request.getAttribute("Form");
	PagesContext	context		= (PagesContext) request.getAttribute("context"); 
	DataRecord		data		= (DataRecord) request.getAttribute("data"); 	
%>


<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<%
   updateForm.displayScripts(out, context);
%>
<script language="JavaScript">
<!--	
	function B_VALIDER_ONCLICK(idCard) {
		if (isCorrectForm())
		{	   
		 
			document.myForm.action = "<%=routerUrl%>effectiveUpdate?userCardId="+idCard;
			document.myForm.submit();
		}
	}

	function B_ANNULER_ONCLICK(idCard) {
		self.close();
	}	
//-->
</script>
</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<FORM NAME="myForm" METHOD="POST" ENCTYPE="multipart/form-data">

<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<center>
<%
	updateForm.display(out, context, data);
%>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK('"+card.getPK().getId()+"');", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK('"+card.getPK().getId()+"');", false));
    out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>