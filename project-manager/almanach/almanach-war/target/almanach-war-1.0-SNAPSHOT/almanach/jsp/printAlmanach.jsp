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
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

	Collection events = (Collection) request.getAttribute("ListEvent");
%>

<HTML>
<HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>

<%
out.println(graphicFactory.getLookStyleSheet());
%>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	Frame 	frame 	= graphicFactory.getFrame();
	Window 	window 	= graphicFactory.getWindow();
	Board	board	= graphicFactory.getBoard();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "almanach.jsp");
	browseBar.setPath(almanach.getString("impression"));
	
    OperationPane operationPane = window.getOperationPane();
    operationPane.addOperation(m_context + "/util/icons/almanach_to_print.gif", resources.getString("GML.print"), "javascript:onClick = window.print()");

	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<center>
    <table width="90%" border="0" cellspacing="1" cellpadding="2" class="intfdcolor4">
      <tr> 
	<td nowrap colspan="3"><span class="titremodule">
	<%=
		almanach.getString("mois" + almanach.getCurrentDay().get(Calendar.MONTH)) + 
		" " + 
		almanach.getCurrentDay().get(Calendar.YEAR)
	%></span></td>
      </tr>
    </table>
		<%
		if (events.size() == 0) {
		%>
    <table width="90%" border="0" cellspacing="1" cellpadding="2" class="intfdcolor4">
      <tr> 
	<td nowrap colspan="3"><span class="txtnav"><%=almanach.getString("aucunEvenement")%></span></td>
      </tr>
		<%
		}

		int currentDay = -1;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(almanach.getCurrentDay().getTime());
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int currentMonth = calendar.get(Calendar.MONTH);

		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
		
		boolean firstBgColor = true;

		while (currentMonth == calendar.get(Calendar.MONTH)) {
			for (Iterator i = events.iterator(); i.hasNext(); ) {
				EventDetail event = (EventDetail) i.next();
				String theDay = dateFormat.format(calendar.getTime());
				String startDay = dateFormat.format(event.getStartDate());
				String startHour = event.getStartHour();
				String endHour = event.getEndHour();
				if (startDay.compareTo(theDay) > 0) continue;
				String endDay = startDay;
				if (event.getEndDate() != null)
					endDay = dateFormat.format(event.getEndDate());
				if (endDay.compareTo(theDay) < 0) continue;
				
				if (calendar.get(Calendar.DAY_OF_MONTH) != currentDay) {
				   if (currentDay != -1) // if it's not the first print
					out.println("</table>");
				   if (firstBgColor) {
				     %><table width="90%" border="0" cellspacing="1" cellpadding="2" class="intfdcolor4"><%
				   } else {
				     %><table width="90%" border="0" cellspacing="1" cellpadding="2" class="intfdcolor"><%
				   }
				   firstBgColor = ! firstBgColor;
				   %>
				      <tr> 
					<td nowrap colspan="2"><span class="txtnav"><%=almanach.getString("jour" + calendar.get(Calendar.DAY_OF_WEEK))+ 
						" " + 
						calendar.get(Calendar.DAY_OF_MONTH) + 
						" " + 
						almanach.getString("mois" + calendar.get(Calendar.MONTH)) +
						" " +
						calendar.get(Calendar.YEAR)%>
					</span></td>
				      </tr>
				   <%
				   currentDay = calendar.get(Calendar.DAY_OF_MONTH);
				}
				%>
				      <tr> 
					<%if (event.getPriority() == 1) {%>
					<td nowrap align="center" width="4%" valign="top"><img src="icons/flecheRouge.gif" width="6" height="11"></td>
					<%} else {%>
					<td nowrap align="center" width="4%" valign="top"><img src="icons/flecheGrise.gif" width="6" height="11"></td>
					<%}%>
					<td width="61%"><span class="txtnote">
					<%
					String title = Encode.javaStringToHtmlString(event.getTitle());
					String description = null;
					
					if (StringUtil.isDefined(event.getWysiwyg())) {
						description = event.getWysiwyg();
					}
				    else if (StringUtil.isDefined(event.getDescription())) {
		      			 description = Encode.javaStringToHtmlParagraphe(event.getDescription());
					}
					
					if (almanach.isAgregationUsed()) 
					{
						String eventColor = almanach.getAlmanachColor(event.getInstanceId());
						title = "<b><span style=\"color :"+eventColor+"\">"+title+"</span></b>";
						if (StringUtil.isDefined(description)) {
							description = "<span style=\"color :"+eventColor+"\">"+description+"</span>";
						}
					}
					out.println(title);
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
						<br><%=almanach.getString("lieuEvenement")%> : <%=event.getPlace()%>
					<% } %>
					</span><br></td>
				    </tr>
				<%
			} //end for
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}// end while
	%>
    </table>
    </center>
	<%
	out.println(board.printAfter());
	Button button = graphicFactory.getFormButton(resources.getString("GML.back"), "almanach.jsp", false);
    out.print("<br><center>"+button.print()+"</center>");
	  
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>