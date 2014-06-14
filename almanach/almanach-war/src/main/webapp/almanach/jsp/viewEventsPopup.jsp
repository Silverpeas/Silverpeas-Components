<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

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
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.almanach.control.*"%>
<%@ page import="com.stratelia.webactiv.almanach.model.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkAlmanach.jsp" %>
<%
  AlmanachCalendarView calendarView = (AlmanachCalendarView) request.getAttribute("calendarView");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
function viewEvent(componentId, id) {
	window.opener.location.href="<%=m_context%>/Ralmanach/"+componentId+"/viewEventContent.jsp?Id="+id;
}
</script>
</head>
<body>
<%
	Frame 	frame 	= graphicFactory.getFrame();
	Window 	window 	= graphicFactory.getWindow();
	Board	board	= graphicFactory.getBoard();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setClickable(false);

	out.println(window.printBefore());
	out.println(frame.printBefore());

	int currentDay = -1;
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(almanach.getCurrentDay());
	calendar.set(Calendar.DAY_OF_YEAR, 1);
	int currentYear = calendar.get(Calendar.YEAR);
	int currentMonth = calendar.get(Calendar.MONTH);

	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");

	ArrayPane arrayPane = graphicFactory.getArrayPane("YearEvents", "ViewYearEventsPOPUP", request, session);
	arrayPane.setVisibleLineNumber(resources.getSetting("almanach.yeareventspopup.arraypagesize", 15));
  	arrayPane.setXHTML(true);
  	arrayPane.setExportData(true);
  	arrayPane.addArrayColumn(resources.getString("GML.month"));
  	arrayPane.addArrayColumn(resources.getString("GML.title"));
  	ArrayColumn column1 = arrayPane.addArrayColumn(resources.getString("GML.dateBegin"));
  	column1.setWidth("100px");
  	ArrayColumn column2 = arrayPane.addArrayColumn(resources.getString("GML.dateEnd"));
  	column2.setWidth("100px");

    List<DisplayableEventOccurrence> occurrences = calendarView.getEvents();
	for (DisplayableEventOccurrence occurrence: occurrences) {
		EventDetail event = occurrence.getEventDetail();
		String startDay = dateFormat.format(occurrence.getStartDate());
		String url = m_context+"/Ralmanach/"+event.getPK().getInstanceId()+"/viewEventContent.jsp?Id="+event.getPK().getId()+"&amp;Date="+dateFormat.format(calendar.getTime());
		String endDay = startDay;
		if (occurrence.getEndDate() != null) {
			endDay = dateFormat.format(occurrence.getEndDate());
		}
		calendar.setTime(occurrence.getStartDate().asDate());

		String title = EncodeHelper.javaStringToHtmlString(event.getTitle());
		String description = null;

		if (StringUtil.isDefined(event.getWysiwyg())) {
			description = event.getWysiwyg();
		}
	    else if (StringUtil.isDefined(event.getDescription())) {
     			 description = EncodeHelper.javaStringToHtmlParagraphe(event.getDescription());
		}

		if (almanach.isAgregationUsed())
		{
			String eventColor = almanach.getAlmanachColor(event.getInstanceId());
			title = "<b><span style=\"color :"+eventColor+"\">"+title+"</span></b>";
			if (StringUtil.isDefined(description)) {
				description = "<span style=\"color :"+eventColor+"\">"+description+"</span>";
			}
		}

		ArrayLine line = arrayPane.addArrayLine();
		ArrayCellText month = line.addArrayCellText(resources.getString("GML.mois"+calendar.get(Calendar.MONTH)));
		month.setCompareOn(Integer.valueOf(calendar.get(Calendar.MONTH)));
		ArrayCellText link = line.addArrayCellText("<a href=\"javascript:viewEvent('"+event.getPK().getInstanceId()+"','"+event.getPK().getId()+"')\">"+title+"</a>");
		link.setCompareOn(title);
		ArrayCellText start = line.addArrayCellText(resources.getOutputDate(startDay));
		start.setCompareOn(startDay);
		ArrayCellText end = line.addArrayCellText(resources.getOutputDate(endDay));
		end.setCompareOn(endDay);
	}

	out.println(arrayPane.print());

	Button button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false);
    out.print("<br/><center>"+button.print()+"</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>