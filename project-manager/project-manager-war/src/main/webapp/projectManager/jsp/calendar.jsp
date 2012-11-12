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

<%@ include file="check.jsp" %>
<%!

String getHTMLMonthCalendar(Date date, ResourcesWrapper resource, List holidays) {

		String  weekDayStyle 		= "class=\"txtnav\"";
    	String  selectedDayStyle	= "class=\"intfdcolor3\"";

     	StringBuffer result = new StringBuffer(255);
     	result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"1\">");

        Calendar calendar = Calendar.getInstance();
        
        int firstDayOfWeek = Calendar.MONDAY;

        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // calcul du nombre de jour dans le mois
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        int numDays = calendar.get(Calendar.DAY_OF_MONTH);

        // calcul du jour de depart
        calendar.setTime(date);
        int startDay = 1;

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek)
        {
            calendar.add(Calendar.DATE, -1);
            startDay++;
        }

        result.append("<TR class=\"txtnav2\"><TD COLSPAN=7>\n");
        result.append("<TABLE width=\"100%\" BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"0\"><TR>");
        result.append("<TD class=\"intfdcolor3\" ALIGN=\"center\"><span class=txtNav4>").append(resource.getString("GML.mois" + month)).append(" ").append(year).append("</span></TD>");
        result.append("</TR></TABLE>\n");
        result.append("</TD></tr>");

        result.append("<TR class=\"intfdcolor2\">\n");

        do
        {
 	        result.append("<TH ").append(weekDayStyle).append("><a href=\"javaScript:changeDayStatus('").append(year).append("', '").append(month).append("', '").append(calendar.get(Calendar.DAY_OF_WEEK)).append("');\">").append(resource.getString("GML.shortJour" + calendar.get(Calendar.DAY_OF_WEEK))).append("</a></TH>");
            calendar.add(Calendar.DATE, 1);
        }
        while (calendar.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);

        result.append("</TR>\n");

        // put blank table entries for days of week before beginning of the month
        result.append("<TR>\n");
        int column = 0;

        for (int i = 0; i < startDay - 1; i++)
        {
            result.append("<TD width=\"14%\">&nbsp;</TD>");
            column++;
        }

        calendar.setTime(date);
        
        Date 	currentDate 		= null;
        String 	sCurrentDate		= null; 
        String 	currentDateStyle	= null;
        int		nextStatus			= 0;
        for (int i = 1; i <= numDays; i++)
        {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            
            currentDate 	= calendar.getTime();
            sCurrentDate 	= resource.getInputDate(currentDate);
            
            if (holidays.contains(currentDate))
            {
            	currentDateStyle 	= selectedDayStyle;
            	nextStatus			= 0;
            }
            else
            {
				currentDateStyle 	= "";
				nextStatus			= 1;
			}
				
            result.append("<TD width=\"14%\" align=\"right\" ").append(currentDateStyle).append("><A HREF=\"javascript:changeDateStatus('").append(sCurrentDate).append("','").append(nextStatus).append("');\">").append(i).append("</A></TD>\n");

            // Check for end of week/row
            if ((++column == 7) && (numDays > i))
            {
                result.append("</TR>\n<TR>");
                column = 0;
            }
        }
        for (int i = column; i <= 6; i++)
        {
            result.append("<TD>&nbsp;</TD>\n");
        }
        result.append("</TR></TABLE>\n");

        return result.toString();
    }

%>
<%
Date 				beginDate 	= (Date) request.getAttribute("BeginDate");
Date 				endDate		= (Date) request.getAttribute("EndDate");
List			 	holidays	= (List) request.getAttribute("HolidayDates");
%>
<html>
<head>
<title></title>
<%
    out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function changeDateStatus(day, status)
{
	document.calendarForm.Date.value = day;
	document.calendarForm.Status.value = status;
	document.calendarForm.submit();
}
function changeDayStatus(year, month, day)
{
	document.calendarDayForm.DayOfWeek.value = day;
	document.calendarDayForm.Month.value = month;
	document.calendarDayForm.Year.value = year;
	document.calendarDayForm.submit();
}
</script>

</head>
<body>
<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    
    out.println(window.printBefore());
    
    TabbedPane tabbedPane = gef.getTabbedPane();
    
	tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", false);
    tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
	tabbedPane.addTab(resource.getString("GML.attachments"), "ToAttachments", false);
	tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
	tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
	tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "#", true);
	
    out.println(tabbedPane.print());
    
    out.println(frame.printBefore());
    
    Board board = gef.getBoard();
    out.println(board.printBefore());
    
    Calendar calendar = Calendar.getInstance();
    
    out.println("<center>");
    out.println("<table border=0 class=\"contourintfdcolor\" cellpadding=\"2\" cellspacing=\"2\">");
    %>

	<tr><td colspan=4><%=resource.getString("projectManager.calendarDays")%></td></tr>

	<%
    int i = 1;
    
    while (beginDate.before(endDate))
    {
    	if (i==1)
    		out.println("<tr>");
    		
    	if (i-4 > 0)
    	{
    		out.println("</tr>");
    		i = 1;
    	}
    		
    	out.println("<td valign=\"top\" class=\"contourintfdcolor\">");
    	out.println(getHTMLMonthCalendar(beginDate, resource, holidays));
    	out.println("</td>");
    	
    	calendar.setTime(beginDate);
    	calendar.add(Calendar.MONTH, 1);
    	beginDate = calendar.getTime();
    	
    	i++;
    }
    
	out.println("</table>");
	out.println("</center>");
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
<form name="calendarForm" action="ChangeDateStatus" method="POST">
	<input type="hidden" name="Date">
	<input type="hidden" name="Status">
</form>
<form name="calendarDayForm" action="ChangeDayOfWeekStatus" method="POST">
	<input type="hidden" name="DayOfWeek">
	<input type="hidden" name="Month">
	<input type="hidden" name="Year">
</form>
</body>
</html>