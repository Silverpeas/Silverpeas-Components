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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="publicationsList.jsp" %>
<%@ include file="kmax_axisReport.jsp" %>

<% 
String rootId = "0";

//Recuperation des parametres
String translation = (String) request.getAttribute("Translation");
if (translation == null)
	translation = kmeliaScc.getLanguage();

//Icons
String publicationSrc = m_context + "/util/icons/publication.gif";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<view:looknfeel/>
<script language="JavaScript1.2">
function search() {
    z = "";
    nbSelectedAxis = 0;
    timeCriteria = "X";
    timeCriteriaUsed = 0;
	<% if (kmeliaScc.isTimeAxisUsed()) { %>
      // -2 instead of -1 because of security tokens
      // before, it was :
      //  - document.axisForm.elements[document.axisForm.length - 1].value
      //  - timeCriteriaUsed = 1;
      timeCriteria = document.axisForm.elements[document.axisForm.length - 2].value;
	    timeCriteriaUsed = 2;
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
      jQuery.popup.error("Vous devez s�lectionnez au moins un axe !");
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