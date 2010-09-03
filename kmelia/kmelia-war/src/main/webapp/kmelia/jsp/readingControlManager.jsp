<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
//R�cup�ration des param�tres
PublicationDetail 	publication 		= (PublicationDetail) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
String 				wizard 				= (String) request.getAttribute("Wizard");
String				currentLang 		= (String) request.getAttribute("Language");

String pubName = publication.getName(currentLang);
String pubId = publication.getPK().getId();

boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
      isOwner = true;
%>

<html>
<head><title><%=resources.getString("ReadingControlTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
function topicGoTo(id) 
{
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() 
{
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</head>
<body onUnload="closeWindows()">
<% 
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	Board boardHelp = gef.getBoard();
	  
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);
	
	OperationPane operationPane = window.getOperationPane();
	
	out.println(window.printBefore());
	  
	if (isOwner)
		displayAllOperations(pubId, kmeliaScc, gef, "ViewReadingControl", resources, out, kmaxMode);
	else
	    displayUserOperations(pubId, kmeliaScc, gef, "ViewReadingControl", resources, out, kmaxMode);
	  
	out.println(frame.printBefore());
	if ("finish".equals(wizard))
	{
		//  cadre d'aide
		out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+resources.getString("kmelia.HelpReadingControl")+"</td>");
		out.println("</tr></table>");
		out.println(boardHelp.printAfter());
		out.println("<BR>");
	}
	
	String url = kmeliaScc.getComponentUrl()+"ReadingControl";
	String objectType = "Publication";
	
	out.flush();
	getServletConfig().getServletContext().getRequestDispatcher("/statistic/jsp/readingControl.jsp?id="+pubId+"&url="+url+"&componentId="+componentId+"&objectType="+objectType).include(request, response);
		
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>