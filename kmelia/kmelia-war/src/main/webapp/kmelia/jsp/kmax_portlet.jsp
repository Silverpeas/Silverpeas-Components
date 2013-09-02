<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ page import="java.util.*"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="publicationsList.jsp.inc" %>
<%@ include file="kmax_axisReport.jsp" %>

<%!
  //Icons
  String publicationSrc;
  String fullStarSrc;
  String emptyStarSrc;
%>

<% 
String rootId = "0";

//Recuperation des parametres
String translation = (String) request.getAttribute("Translation");
if (translation == null)
	translation = kmeliaScc.getLanguage();

//Icons
publicationSrc = m_context + "/util/icons/publication.gif";
fullStarSrc = m_context + "/util/icons/starFilled.gif";
emptyStarSrc = m_context + "/util/icons/starEmpty.gif";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript1.2">
function search() {
    z = "";
    nbSelectedAxis = 0;
    timeCriteria = "X";
    timeCriteriaUsed = 0;
	<% if (kmeliaScc.isTimeAxisUsed()) { %>
	    timeCriteria = document.axisForm.elements[document.axisForm.length - 1].value;
	    timeCriteriaUsed = 1;
	<% } %>
	for (var i=0; i<document.axisForm.length - timeCriteriaUsed; i++) {
    	if (document.axisForm.elements[i].value.length != 0) {
            if (nbSelectedAxis != 0)
                z += ",";
            nbSelectedAxis = 1;
            truc = document.axisForm.elements[i].value.split("|");
            z += truc[0];
        }
    }
    if (nbSelectedAxis != 1) {
		window.alert("Vous devez s�lectionnez au moins un axe !");
    } else {
		document.managerForm.TimeCriteria.value = timeCriteria;
		document.managerForm.SearchCombination.value = z;
		document.managerForm.action = "KmaxSearch";
		document.managerForm.submit();
    }
}

</script>
</head>
<body>
<%
	Window window = gef.getWindow();
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(kmeliaScc.getSpaceLabel());
	browseBar.setComponentName(kmeliaScc.getComponentLabel(), "KmaxMain");
	browseBar.setExtraInformation(kmeliaScc.getString("PublicationAxis"));

    //Instanciation du cadre avec le view generator
    Frame frame = gef.getFrame();

    out.println(window.printBefore());
    
    frame.addTop(displayAxisToUsers(kmeliaScc, gef, translation));
    frame.addBottom("");

    out.println(frame.print());
    out.println(window.printAfter());
%>

<form name="managerForm" action= "KmaxAxisManager" method="post" target="MyMain">
	<input type="hidden" name="SearchCombination"/>
	<input type="hidden" name="TimeCriteria"/>
</form>

</body>
</html>