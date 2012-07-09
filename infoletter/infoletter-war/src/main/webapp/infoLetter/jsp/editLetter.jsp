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
<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function call_wysiwyg (){
document.toWysiwyg.submit();
}
</script>
</head>
<body bgcolor="#FFFFFF" onload="javascript:call_wysiwyg();">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\">" + resource.getString("infoLetter.listParutions") + "</a> > " + resource.getString("infoLetter.newLetterEdit"));



	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"headerLetter.jsp",false);    			    
  tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"#",true);
  tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"previewLetter.jsp",false);


  out.println(tabbedPane.print());
	out.println(frame.printBefore());	
	
%>

<% // Ici debute le code de la page %>
  <form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="post">
    <input type="hidden" name="SpaceId" value="<%= (String) request.getAttribute("SpaceId") %>"/>
    <input type="hidden" name="SpaceName" value="<%= (String) request.getAttribute("SpaceName") %>"/>
    <input type="hidden" name="ComponentId" value="<%= (String) request.getAttribute("ComponentId") %>"/>
    <input type="hidden" name="ComponentName" value="<%= (String) request.getAttribute("ComponentName") %>"/>
    <input type="hidden" name="BrowseInfo" value="<%= (String) request.getAttribute("BrowseInfo") %>"/> 
    <input type="hidden" name="ObjectId" value="<%= (String) request.getAttribute("ObjectId") %>"/>
    <input type="hidden" name="Language" value="<%= (String) request.getAttribute("Language") %>"/>
    <input type="hidden" name="ReturnUrl" value="<%= (String) request.getAttribute("ReturnUrl") %>"/>

    <!-- Bouton a virer -->
    <!--<input type="submit" value="WYSIWYG">-->

  </form>
    			


<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>

