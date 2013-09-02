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

<%@page import="org.silverpeas.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ include file="checkKmelia.jsp" %>

<%
//Recuperation des parametres
PublicationDetail 	publication 		= (PublicationDetail) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
String				currentLang 		= (String) request.getAttribute("Language");
List				validationSteps		= (List) request.getAttribute("ValidationSteps");
String				profile				= (String) request.getAttribute("Role");

String pubName = publication.getName(currentLang);
String pubId = publication.getPK().getId();

%>

<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
function topicGoTo(id) 
{
	location.href="GoToTopic?Id="+id;
}

function pubForceValidate() {
	location.href="ForceValidatePublication";
}
</script>
</head>
<body>
<% 
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);
	
	OperationPane operationPane = window.getOperationPane();
	
	out.println(window.printBefore());
	  
	KmeliaDisplayHelper.displayAllOperations(pubId, kmeliaScc, gef, "ViewValidationSteps",
        resources, out, kmaxMode);
	  
	out.println(frame.printBefore());
	
	if ("admin".equals(profile)) {
		Board boardHelp = gef.getBoard();
		
		Button validButton = gef.getFormButton(resources.getString("kmelia.ForceValidation"), "javaScript:pubForceValidate();", false);
		
		out.println(boardHelp.printBefore());
		out.println("<center>");
		out.println("<table border=\"0\" width=\"600px\"><tr><td align=\"center\">");
		out.println(resources.getString("kmelia.ForceValidationHelp")+"<br/>");
		out.println("</td></tr></table>");
		out.println(validButton.print());
		out.println("</center>");
		out.println(boardHelp.printAfter());
		out.println("<br/>");
	}
	
    ArrayPane arrayPane = gef.getArrayPane("validationSteps", "ViewValidationSteps", request, session);
    arrayPane.setVisibleLineNumber(20);

    arrayPane.addArrayColumn(resources.getString("GML.user"));
    arrayPane.addArrayColumn(resources.getString("kmelia.validationDate"));
    
    Iterator it = validationSteps.iterator();
    while (it.hasNext())
    {
    	ArrayLine ligne = arrayPane.addArrayLine();
    	
    	ValidationStep step = (ValidationStep) it.next();
    	ligne.addArrayCellText(step.getUserFullName());
    	
    	Date validationDate = step.getValidationDate();
    	String sDate = "";
        if (validationDate == null) 
        	sDate = resources.getString("kmelia.PublicationValidationInWait");
        else 
        	sDate = resources.getOutputDateAndHour(validationDate);
        ArrayCellText cell1 = ligne.addArrayCellText(sDate);
        cell1.setCompareOn(validationDate);
    }
    	
    out.println(arrayPane.print());  
		
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>