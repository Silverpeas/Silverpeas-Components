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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.io.File"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="check.jsp" %>

<%
	String parution = (String) request.getAttribute("parution");
	String url = infoLetterUrl+"pdcPositions.jsp?parution="+parution;
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="Javascript">
function call_wysiwyg (){
	document.toWysiwyg.submit();
}

function goHeaders (){
	document.headerParution.submit();
}

function goValidate (){
	document.validateParution.submit();
}

function goView (){
	document.viewParution.submit();
}

function goFiles (){
	document.attachedFiles.submit();
}

function goTemplate (){
	document.template.submit();
}
</script>
</HEAD>
<BODY>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("PdcClassification"));

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resource.getString("NewPdcPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resource.getString("DeletePdcPosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());

	//Instanciation du cadre avec le view generator
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"javascript:goHeaders();",false);    			    
    tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"javascript:call_wysiwyg();",false);
    tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"javascript:goView();",false);
    tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"javascript:goFiles();",false);
	tabbedPane.addTab(resource.getString("PdcClassification"),"#",true);

    out.println(tabbedPane.print());
	
	out.println(frame.printBefore());

	out.flush();

	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId=" + ((String) request.getAttribute("silverObjectId"))+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="pdcPositions.jsp" METHOD=POST >
	<input type="hidden" name="parution" value="<%= parution %>">
</FORM>

<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
    <input type="hidden" name="SpaceId" value="<%= (String) request.getAttribute("SpaceId") %>">
    <input type="hidden" name="SpaceName" value="<%= (String) request.getAttribute("SpaceName") %>">
    <input type="hidden" name="ComponentId" value="<%= (String) request.getAttribute("ComponentId") %>">
    <input type="hidden" name="ComponentName" value="<%= (String) request.getAttribute("ComponentName") %>">
    <input type="hidden" name="BrowseInfo" value="<%= (String) request.getAttribute("BrowseInfo") %>"> 
    <input type="hidden" name="ObjectId" value="<%= (String) request.getAttribute("ObjectId") %>">
    <input type="hidden" name="Language" value="<%= (String) request.getAttribute("Language") %>">
    <input type="hidden" name="ReturnUrl" value="<%= (String) request.getAttribute("ReturnUrl") %>">
</form>

</center>
<form name="headerParution" action="ParutionHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
<form name="validateParution" action="ValidateParution" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
<form name="viewParution" action="Preview" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
<form name="attachedFiles" action="FilesEdit" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
<form name="template" action="UpdateTemplateFromHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>

</BODY>
</HTML>