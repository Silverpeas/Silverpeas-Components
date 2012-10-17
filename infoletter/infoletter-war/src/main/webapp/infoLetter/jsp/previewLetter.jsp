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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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
		com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR, e.getMessage(), e);			
    }
}
%>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
	function call_wysiwyg (){
		document.toWysiwyg.submit();
	}
	
	function goHeaders (){
		document.headerParution.submit();
	}
	
	function goFiles (){
		document.attachedFiles.submit();
	}
	
	function sendLetterToManager (){
		$.progressMessage();
		document.headerParution.action = "SendLetterToManager";
		document.headerParution.submit();
	}
</script>
</head>
<body>
<%
String parutionTitle = (String) request.getAttribute("parutionTitle");
String parution = (String) request.getAttribute("parution");

	browseBar.setPath(EncodeHelper.javaStringToHtmlString(parutionTitle));	

	operationPane.addOperation(resource.getIcon("infoLetter.sendLetterToManager"), resource.getString("infoLetter.sendLetterToManager"), "javascript:sendLetterToManager();");	

	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"javascript:goHeaders();",false);  
  tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"javascript:call_wysiwyg();",false);
  tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"#",true);
  tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"javascript:goFiles();",false);

  out.println(tabbedPane.print());
    
	out.println(frame.printBefore());	
	
%>

<%
out.flush();
displayViewWysiwyg(parution, spaceId, componentId, request, response);		
%>
<form name="headerParution" action="ParutionHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
  <input type="hidden" name="ReturnUrl" value="Preview"/>
</form>
<form name="attachedFiles" action="FilesEdit" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<form name="toWysiwyg" action="../../wysiwyg/jsp/htmlEditor.jsp" method="post">
    <input type="hidden" name="SpaceId" value="<%= (String) request.getAttribute("SpaceId") %>"/>
    <input type="hidden" name="SpaceName" value="<%= (String) request.getAttribute("SpaceName") %>"/>
    <input type="hidden" name="ComponentId" value="<%= (String) request.getAttribute("ComponentId") %>"/>
    <input type="hidden" name="ComponentName" value="<%= (String) request.getAttribute("ComponentName") %>"/>
    <input type="hidden" name="BrowseInfo" value="<%= (String) request.getAttribute("BrowseInfo") %>"/> 
    <input type="hidden" name="ObjectId" value="<%= (String) request.getAttribute("ObjectId") %>"/>
    <input type="hidden" name="Language" value="<%= (String) request.getAttribute("Language") %>"/>
    <input type="hidden" name="ReturnUrl" value="<%= (String) request.getAttribute("ReturnUrl") %>"/>
</form>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>