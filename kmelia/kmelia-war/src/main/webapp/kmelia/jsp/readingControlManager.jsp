<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ include file="checkKmelia.jsp" %>

<%
PublicationDetail 	publication 		= (PublicationDetail) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
String				currentLang 		= (String) request.getAttribute("Language");

String pubName = publication.getName(currentLang);
String pubId = publication.getPK().getId();

boolean isOwner = kmeliaScc.getSessionOwner();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resources.getString("ReadingControlTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
function topicGoTo(id) {
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</head>
<body onunload="closeWindows()">
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
	  
	if (isOwner) {
		KmeliaDisplayHelper.displayAllOperations(pubId, kmeliaScc, gef, "ViewReadingControl",
          resources, out, kmaxMode);
	} else {
	  KmeliaDisplayHelper.displayUserOperations(kmeliaScc, out);
	}
	  
	out.println(frame.printBefore());
	
	String objectType = "Publication";
	
	out.flush();
	getServletConfig().getServletContext().getRequestDispatcher("/statistic/jsp/readingControl.jsp?id="+pubId+"&componentId="+componentId+"&objectType="+objectType).include(request, response);
		
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>