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

<%@ include file="checkAlmanach.jsp" %>
<%
	Collection events = (Collection) request.getAttribute("Events");
	String function = (String) request.getAttribute("Function");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<style type="text/css">
#listEvents .month {
	font-weight: bold;
}

#listEvents ul.months, ul.days, ul.events {
	list-style-type: none;
}

#listEvents ul.days {
	margin: 1em 0 1em 0px;
	padding: 0;
}

#listEvents ul.events {
	margin: 1em 0 1em 40px;
	padding: 0;
}

#listEvents li.event {
	background-image: url(icons/flecheGrise.gif);
	background-repeat: no-repeat;
	background-position: 0px 0px;
	padding-left: 14px;
	margin-bottom:5px;
	margin-top:5px;
}

#listEvents li.priorityEvent {
	background-image: url(icons/flecheRouge.gif);
	background-repeat: no-repeat;
	background-position: 0px 0px;
	padding-left: 14px;
	margin-bottom:5px;
	margin-top:5px;
}

#listEvents li.day {
	background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/degrade20px.jpg);
	background-repeat:repeat-x;
	color:#000000;
	font-size:10pt;
	font-weight:bold;
}

#listEvents li.month {
	color:#000000;
	font-size:13pt;
	font-weight:bold;
}
</style>
</head>
<body>
<div id="listEvents">
<%
	Frame 	frame 	= graphicFactory.getFrame();
	Window 	window 	= graphicFactory.getWindow();
	Board	board	= graphicFactory.getBoard();

	BrowseBar browseBar = window.getBrowseBar();
	if (function.equals("ViewYearEvents")) {
		browseBar.setPath(resources.getString("almanach.browsebar.yearEvents"));
  	} else if (function.equals("ViewMonthEvents")) {
  	  	browseBar.setPath(resources.getString("almanach.browsebar.monthEvents"));
  	}

    OperationPane operationPane = window.getOperationPane();
    operationPane.addOperation(m_context + "/util/icons/almanach_to_print.gif", resources.getString("GML.print"), "javascript:onClick = window.print()");

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());

	int scope = 0; //0 = year
	if (!"ViewYearEvents".equals(function)) {
	  scope = 1; //1 = month
	}

	int currentDay = -1;
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(almanach.getCurrentDay());
	if (scope == 0) {
		calendar.set(Calendar.DAY_OF_YEAR, 1);
	} else {
	  	calendar.set(Calendar.DAY_OF_MONTH, 1);
	}
	int currentYear = calendar.get(Calendar.YEAR);
	int currentMonth = calendar.get(Calendar.MONTH);
	%>
	<ul class="months">
	<%
	while ((scope == 1 && currentMonth == calendar.get(Calendar.MONTH)) || (scope == 0 && currentYear == calendar.get(Calendar.YEAR))) {
%>
		<li class="month"><%=almanach.getString("mois" + calendar.get(Calendar.MONTH)) + " " + calendar.get(Calendar.YEAR) %></li>
		<ul class="days">
<%

		currentMonth = calendar.get(Calendar.MONTH);

		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");

		boolean firstBgColor = true;

		while (currentMonth == calendar.get(Calendar.MONTH)) {
			for (Iterator i = events.iterator(); i.hasNext(); ) {
				EventDetail event = (EventDetail) i.next();
				String theDay = dateFormat.format(calendar.getTime());
				String startDay = dateFormat.format(event.getStartDate());
				String startHour = event.getStartHour();
				String endHour = event.getEndHour();
				String url = m_context+"/Ralmanach/"+event.getPK().getInstanceId()+"/viewEventContent.jsp?Id="+event.getPK().getId()+"&amp;Date="+dateFormat.format(calendar.getTime())+"&amp;Function="+function;
				if (startDay.compareTo(theDay) > 0) continue;
				String endDay = startDay;
				if (event.getEndDate() != null)
					endDay = dateFormat.format(event.getEndDate());
				if (endDay.compareTo(theDay) < 0) continue;

				if (calendar.get(Calendar.DAY_OF_MONTH) != currentDay) {
				   if (currentDay != -1) {
				     	// if it's not the first print
						out.println("</ul>");
				   }
				   if (firstBgColor) {
				     %><li class="day"><%
				   } else {
				     %><li class="day"><%
				   }
				   firstBgColor = ! firstBgColor;
				   %>
						<%=almanach.getString("jour" + calendar.get(Calendar.DAY_OF_WEEK))+
						" " +
						calendar.get(Calendar.DAY_OF_MONTH) +
						" " +
						almanach.getString("mois" + calendar.get(Calendar.MONTH)) +
						" " +
						calendar.get(Calendar.YEAR)%>
					</li>
					<ul class="events">
				   <%
				   currentDay = calendar.get(Calendar.DAY_OF_MONTH);
				}
				%>
					<%if (event.getPriority() == 1) {%>
						<li class="priorityEvent">
					<%} else {%>
						<li class="event">
					<%}%>
					<span class="eventDetail">
					<%
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
					out.print("<a href=\""+url+"\">");
					out.print(title);
					out.println("</a>");
					if (StringUtil.isDefined(description)) {
						out.println(description);
					}

					if (startDay.compareTo(theDay) == 0 && startHour != null && startHour.length() != 0)
					{
						out.print(" (" + startHour);
						if (endDay.compareTo(theDay) == 0 && endHour != null && endHour.length() != 0)
							out.print("-" + endHour);
						out.println(")");
					}
					%>
					<%if (StringUtil.isDefined(event.getPlace())) { %>
						<br/><%=almanach.getString("lieuEvenement")%> : <%=event.getPlace()%>
					<% } %>
					</span>
					</li>
				<%
			} //end for
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}// end while
%>
	</ul>
<% } %>
</ul>
	</div>
	<%
	out.println(board.printAfter());
	Button button = graphicFactory.getFormButton(resources.getString("GML.back"), "almanach.jsp", false);
    out.print("<br/><center>"+button.print()+"</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>