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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>
<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>

<%
Form 				formUpdate 	= (Form) request.getAttribute("Form");
DataRecord 			data 		= (DataRecord) request.getAttribute("Data"); 
PublicationDetail 	pubDetail 	= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
String				xmlFormName = (String) request.getAttribute("XMLFormName");

String pubId 	= pubDetail.getPK().getId();
String pubName 	= pubDetail.getName();

PagesContext 		context 	= new PagesContext("myForm", "0", resources.getLanguage(), false, componentId, newsSC.getUserId());
context.setObjectId(pubId);
context.setBorderPrinted(false);

%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<% formUpdate.displayScripts(out, context); %>
<script language="javaScript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function B_VALIDER_ONCLICK()
{
	if (isCorrectForm())
	{
		document.myForm.submit();
	}
}

function B_ANNULER_ONCLICK() 
{
	location.href = "Main";
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}

</script>
</HEAD>
<BODY class="yui-skin-sam" onUnload="closeWindows()">
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    Board boardHelp = gef.getBoard();
    
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");

    out.println(window.printBefore());

    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<FORM NAME="myForm" METHOD="POST" ACTION="UpdateXMLForm" ENCTYPE="multipart/form-data">
	<% 
		formUpdate.display(out, context, data); 
	%>
	<input type="hidden" name="XmlFormName" value="<%=xmlFormName%>">
</FORM>
<%
	out.println(board.printAfter());
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
	out.println("<br><center>"+buttonPane.print()+"</center>");

    out.println(frame.printAfter());
%>
<% out.println(window.printAfter()); %>
</BODY>
<script language="javascript">
	document.myForm.elements[1].focus();
</script>
</HTML>