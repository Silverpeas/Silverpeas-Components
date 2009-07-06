<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkAlmanach.jsp" %>

<%

	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

	EventDetail event = (EventDetail) request.getAttribute("Event");
	Date dateDebutIteration = (Date) request.getAttribute("DateDebutIteration");
	
	String dateDebutIterationString = DateUtil.date2SQLDate(dateDebutIteration);

	String id = event.getPK().getId();
	String title = Encode.javaStringToHtmlString(event.getTitle());
	if (title.length() > 30) {
		title = title.substring(0,30) + "....";
	}
	String url = almanach.getComponentUrl()+"pdcPositions.jsp?Id="+id+"&Date="+dateDebutIterationString;
%>

<!-- AFFICHAGE BROWSER -->
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<script language="JavaScript">
function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
<%

        Window window = graphicFactory.getWindow();

		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "almanach.jsp");
        browseBar.setExtraInformation(Encode.javaStringToHtmlString(title));

        OperationPane operationPane = window.getOperationPane();

	    operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
        operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

	    out.println(window.printBefore());

		TabbedPane tabbedPane = graphicFactory.getTabbedPane();
		tabbedPane.addTab(resources.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		tabbedPane.addTab(resources.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		tabbedPane.addTab(resources.getString("GML.attachments"), "editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Id="+id+"&Date="+dateDebutIterationString, true);
		out.println(tabbedPane.print());

		Frame frame=graphicFactory.getFrame();
        out.println(frame.printBefore());
        out.flush();

        getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+almanach.getSilverObjectId(id)+"&ComponentId="+instanceId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<FORM NAME="toComponent" ACTION="pdcPositions.jsp" METHOD=POST >
    <input type="hidden" name="Id" value="<%=id%>">
	<input type="hidden" name="Date" value="<%=dateDebutIterationString%>">
</FORM>
</BODY>
</HTML>