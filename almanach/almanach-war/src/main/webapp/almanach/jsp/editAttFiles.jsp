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
	String title = Encode.javaStringToHtmlString(event.getTitle());
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