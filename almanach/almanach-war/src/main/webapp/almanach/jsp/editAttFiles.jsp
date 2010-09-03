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
	<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
	%>

<%@ include file="checkAlmanach.jsp" %>

<%

	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

  //initialisation  des variables
	EventDetail event = (EventDetail) request.getAttribute("Event");
	Date dateDebutIteration = (Date) request.getAttribute("DateDebutIteration");
	
	String dateDebutIterationString = DateUtil.date2SQLDate(dateDebutIteration);

	String id = event.getPK().getId();
	String title = event.getTitle();
	if (title.length() > 30) {
		title = title.substring(0,30) + "....";
	}
	String url = almanach.getComponentUrl()+"editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString;
%>

<!-- AFFICHAGE BROWSER -->
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(graphicFactory.getLookStyleSheet());
%>
</HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
<% 
        Window window = graphicFactory.getWindow();
		
		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel, "almanach.jsp");
		browseBar.setExtraInformation(title);
		
	    out.println(window.printBefore());

		TabbedPane tabbedPane = graphicFactory.getTabbedPane();
		tabbedPane.addTab(almanach.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		tabbedPane.addTab(almanach.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
        tabbedPane.addTab(resources.getString("GML.attachments"), "editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString, true);
        if (almanach.isPdcUsed()) {
        	tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		}
		out.println(tabbedPane.print());
		
		Frame frame=graphicFactory.getFrame();
        out.println(frame.printBefore());
		out.flush();
%> 

<%
	getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString+"&SpaceId="+spaceId+"&ComponentId="+instanceId+"&Context=Images"+"&Url="+URLEncoder.encode(url)).include(request, response);
%>

<%		
		out.println(frame.printAfter());				
		out.println(window.printAfter());
%>
</BODY>
</HTML>