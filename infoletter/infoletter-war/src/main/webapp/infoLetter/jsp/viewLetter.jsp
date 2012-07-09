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
<%!
void displayViewWysiwyg(String id, String spaceId, String componentId, HttpServletRequest request, HttpServletResponse response)
	throws com.stratelia.silverpeas.infoLetter.InfoLetterException {
    try {
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId=" + 
		id + "&SpaceId=" + spaceId + "&ComponentId=" + componentId).include(request, response);
    } catch (Exception e) {
		throw new com.stratelia.silverpeas.infoLetter.InfoLetterException("viewLetter_JSP.displayViewWysiwyg",
		com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR, e.getMessage());			
    }
}
%>
<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
String parutionTitle = (String) request.getAttribute("parutionTitle");
String parution = (String) request.getAttribute("parution");
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function goFiles (){
	document.attachedFiles.submit();
}
</script>
</head>
<body bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\"></a> " + EncodeHelper.javaStringToHtmlString(parutionTitle));
	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
	out.println(frame.printBefore());	

%>

	<table width="100%">
		<tr><td width="80%">
			<%
				out.flush();
				displayViewWysiwyg(parution, spaceId, componentId, request, response);		
			%>
		</td>
		<td valign="top">
			<%
				out.flush();
				getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+parution+"&ComponentId="+componentId+"&Context=Images").include(request, response);
			%>
		</td></tr>
	</table>
	<form name="attachedFiles" action="FilesView" method="post">			
		<input type="hidden" name="parution" value="<%= parution %>"/>
	</form>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>

