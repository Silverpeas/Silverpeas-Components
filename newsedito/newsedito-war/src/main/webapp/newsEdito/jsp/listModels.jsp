<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
List 				xmlForms	= (List) request.getAttribute("ListModels");

//Icons
String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";

%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script language="javaScript">

function sendToWysiwyg() {
    document.toWysiwyg.submit();
}

function goToForm(xmlFormName) {
    document.xmlForm.Name.value = xmlFormName;
    document.xmlForm.submit();
}

function topicGoTo(id) {
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</HEAD>
<BODY onUnload="closeWindows()">
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
 
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    
%>

<TABLE CELLPADDING=5 WIDTH="100%">
  <TR><TD colspan="3" class="txtnav"><%=resources.getString("ModelChoiceTitle")%></TD></TR>
<%
	int nb = 0;
  
    if (xmlForms != null)
    {
	    PublicationTemplate xmlForm;
	    Iterator iterator = xmlForms.iterator();
	    while (iterator.hasNext()) {
	        xmlForm = (PublicationTemplate) iterator.next();
	        
	        if (nb != 0 && nb%3==0)
		        out.println("</TR><TR>");
		        
	        nb++;
	        out.println("<TD align=\"center\"><A href=\"javaScript:goToForm('"+xmlForm.getFileName()+"')\"><IMG SRC=\""+xmlForm.getThumbnail()+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"><BR>"+xmlForm.getName()+"</A></TD>");
	    }
	}
    
    if (nb != 0 && nb%3 == 0)
		out.println("</TR><TR>");
			
    out.println("<TD align=\"center\"><A href=\"javaScript:sendToWysiwyg();\"><IMG SRC=\"../../util/icons/model/wysiwyg.gif\" border=\"0\" alt=\"Wysiwyg\"><BR>WYSIWYG</A></TD>");
    out.println("</TR>");
%>
</TABLE>

<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
%>

<FORM name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
  <input type="hidden" name="SpaceId" value="<%=news.getSpaceId()%>">
  <input type="hidden" name="SpaceName" value="<%=news.getSpaceLabel()%>">
  <input type="hidden" name="ComponentId" value="<%=news.getComponentId()%>">
  <input type="hidden" name="ComponentName" value="<%=news.getComponentLabel()%>">
  <input type="hidden" name="BrowseInfo" value="Edition Wysiwyg">
  <input type="hidden" name="ObjectId" value="<%=news.getPublicationId()%>">
  <input type="hidden" name="Language" value="<%=news.getLanguage()%>">
  <input type="hidden" name="ReturnUrl" value="../..<%=news.getComponentUrl()%>publication.jsp?Action=SelectPublication&PublicationId=<%=news.getPublicationId()%>">
</FORM>

<FORM NAME="xmlForm" ACTION="SelectModel" METHOD="POST">
	<input type="hidden" name="Name">
</FORM>
<% out.println(window.printAfter()); %>
</BODY>
</HTML>