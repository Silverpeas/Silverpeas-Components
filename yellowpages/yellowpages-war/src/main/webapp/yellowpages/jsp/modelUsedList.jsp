<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>

<%@page import="org.silverpeas.core.contribution.template.publication.PublicationTemplate"%>
<%@ page import="java.util.Collection" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkYellowpages.jsp" %>

<%
Collection xmlForms	= (Collection) request.getAttribute("XMLForms");
Collection			modelUsed	= (Collection) request.getAttribute("ModelUsed");

// declaration des boutons
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "Main", false);

%>
<HTML>
<HEAD>
<view:looknfeel/>
<script language="javaScript">

function sendData() {
	document.model.mode.value = 'delete';
	document.model.submit();
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
<form name="model" action="SelectModel" method="POST">
<input type="hidden" name="mode">

<TABLE CELLPADDING=5 WIDTH="100%">
  <TR><TD colspan="3" class="txtnav"><%=resources.getString("yellowPages.ModelList")%></TD></TR>
<%
    int nb = 0;
    out.println("<TR>");
   
    if (xmlForms != null)
    {
	    PublicationTemplate xmlForm;
	    
	    Iterator  iterator = xmlForms.iterator();
	    while (iterator.hasNext()) {
	        xmlForm = (PublicationTemplate) iterator.next();
	        
	        if (nb != 0 && nb%3==0)
		        out.println("</TR><TR>");
		        
	        nb++;
					// recherche si le modele est dans la liste
	        boolean used = false;
	    		String usedCheck = "";
	    	  if (modelUsed.contains(xmlForm.getFileName()))
	        	used = true;
					if (used)
						usedCheck = "checked";
	        out.println("<TD align=\"center\"><IMG SRC=\""+xmlForm.getThumbnail()+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"><BR>"+xmlForm.getName()+"<BR><input type=\"checkbox\" name=\"modelChoice\" value=\""+xmlForm.getFileName()+"\" "+usedCheck+"></TD>");
	    }
	}
    
%>
</form>
</TABLE>

<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
  out.println(frame.printAfter());
%>

<% out.println(window.printAfter()); %>
</BODY>
</HTML>