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
<%@ include file="check.jsp" %>

<%
List		tasks 		= (List) request.getAttribute("Tasks");
TaskDetail	actionMere 	= (TaskDetail) request.getAttribute("ActionMere");
TaskDetail 	oldest 		= (TaskDetail) request.getAttribute("OldestAction");
String 		role 		= (String) request.getAttribute("Role");
Date 		startDate	= (Date) request.getAttribute("StartDate");
List		holidays	= (List) request.getAttribute("Holidays");

String actionMereId = "";
if (actionMere != null)
	actionMereId = new Integer(actionMere.getId()).toString();
%>
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
    out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

</script>
</HEAD>
<BODY>
<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
	if (actionMere != null) {
    	browseBar.setPath("<a href=\"ToGantt?Id="+actionMere.getMereId()+"\">"+actionMere.getNom()+"</a>");
	}
    
    out.println(window.printBefore());
    
    TabbedPane tabbedPane = gef.getTabbedPane();
    
	tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", false);
    tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
    if ("admin".equals(role))
		tabbedPane.addTab(resource.getString("GML.attachments"), "ToAttachments", false);
	tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
	tabbedPane.addTab(resource.getString("projectManager.Gantt"), "#", true);
	if ("admin".equals(role))
		tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
	
    out.println(tabbedPane.print());
	out.println(frame.printBefore());
	
	Board board = gef.getBoard();
	out.println(board.printBefore());
	
	Date actionStartDate	= new Date();
	Date actionEndDate		= null;
	
	if (actionMere != null)
	{
		actionStartDate		= actionMere.getDateDebut();
		actionEndDate		= actionMere.getDateFin();
	}
	if (oldest != null)
	{
		actionStartDate		= oldest.getDateDebut();
		actionEndDate		= oldest.getDateFin();
	}
	
	if (startDate==null) 
		startDate = actionStartDate;

	Calendar actionStartCalendar = new GregorianCalendar(1980, 1 , 1);
	Calendar actionEndCalendar = new GregorianCalendar(1980, 1 , 1);
	Calendar startCalendar = new GregorianCalendar(1980, 1 , 1);
	Calendar endCalendar = new GregorianCalendar(1980, 1 , 1);
	Calendar nextStartCalendar = new GregorianCalendar(1980, 1 , 1);
	Calendar lastStartCalendar = new GregorianCalendar(1980, 1 , 1);

	// If start or end date is not defined ==> let the dates at 1/1/1980
	if (actionStartDate != null && actionEndDate != null)
	{
		actionStartCalendar.setTime(actionStartDate);
		actionEndCalendar.setTime(actionEndDate);

		// Re-Calculate the number of days displayed 
		// to reduce it if less is enough
		/*actionStartCalendar.add(Calendar.DATE, 7);
		if (actionStartCalendar.after(actionEndCalendar))
		{	// task duration is less than 1 week
			nbDaysDisplayed=7;
			actionStartCalendar.add(Calendar.DATE, -7);
		}
		else
		{	// task duration is between 1 week and 2 weeks
			actionStartCalendar.add(Calendar.DATE, 14);
			if (actionStartCalendar.after(actionEndCalendar)) 
				nbDaysDisplayed=14;
			actionStartCalendar.add(Calendar.DATE, -14);
		}*/
	}

	startCalendar.setTime(startDate);
	startCalendar.set(GregorianCalendar.DATE, 1);
	
	endCalendar.setTime(startCalendar.getTime());
	endCalendar.add(GregorianCalendar.MONTH, 1);
	endCalendar.add(GregorianCalendar.DATE, -1);
	
	nextStartCalendar.setTime(startCalendar.getTime());
	nextStartCalendar.add(GregorianCalendar.MONTH, 1);
	
	lastStartCalendar.setTime(startCalendar.getTime());
	lastStartCalendar.add(GregorianCalendar.MONTH, -1);
	
	String dispStartDate = resource.getOutputDate(startCalendar.getTime());
	String dispEndDate = resource.getOutputDate(endCalendar.getTime());
	String strNextStartDate = resource.getInputDate(nextStartCalendar.getTime());
	String strLastStartDate = resource.getInputDate(lastStartCalendar.getTime());
	
	int nbDaysDisplayed = endCalendar.get(GregorianCalendar.DATE)-startCalendar.get(GregorianCalendar.DATE);
	
	// Calculate width of cells
	int cellWidth = 315/nbDaysDisplayed - 1;
%>    
    <table width="100%" border="0" cellspacing="1" cellpadding="2" bgcolor="#000000">
      <tr> 
        <td align="center" colspan=3 nowrap bgcolor=EDEDED> 
          <center>
            <table border="0" cellspacing="0" cellpadding="0" class="intfdcolor1" width="100%">
              <tr align="right"> 
                <td> 
                  <table cellpadding=0 cellspacing=1 border=0 width="100%">
                    <tr>
                      	<td class=intfdcolor><a href="ToGantt?Id=<%=actionMereId%>&StartDate=<%=strLastStartDate%>"><img src="<%=resource.getIcon("projectManager.gauche")%>" border="0"></a></td>
                        <td class=intfdcolor align=center nowrap width="100%"><span class="txtnav">
						&nbsp;<%=dispStartDate + " - " + dispEndDate%>&nbsp;</span></td>
                      	<td class=intfdcolor><a href="ToGantt?Id=<%=actionMereId%>&StartDate=<%=strNextStartDate%>"><img src="<%=resource.getIcon("projectManager.droite")%>" border="0"></a></td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </center>
        </td>
	<%
	String dayToPrint = "";
	for (int nI=0; nI<=nbDaysDisplayed; nI++)
	{
		dayToPrint = String.valueOf(startCalendar.get(GregorianCalendar.DATE));
		if (dayToPrint.length() == 1) dayToPrint = "0" + dayToPrint;
	%>
        <td align="center" bgcolor="#CCCCCC" nowrap width="<%=cellWidth%>"><%=dayToPrint%></td>
	<%
		startCalendar.add(GregorianCalendar.DATE, 1);
	}
	startCalendar.add(GregorianCalendar.DATE, -nbDaysDisplayed-1);
	%>
	  </tr>
      <tr> 
        <td class="intfdcolor51" nowrap height="20"><span class=textePetitbold>&nbsp;<%=resource.getString("projectManager.Taches")%>&nbsp;</span></td>
        <td class="intfdcolor51" nowrap><span class=textePetitbold>&nbsp;<%=resource.getString("projectManager.TacheResponsable")%>&nbsp;</span></td>
        <td class="intfdcolor51" nowrap width="10%"><span class=textePetitbold>&nbsp;<%=resource.getString("projectManager.TacheAvancement")%>&nbsp;</span></td>

	<%
	int nDay= (startCalendar.get(GregorianCalendar.DAY_OF_WEEK)-1) % 7;
	for (int nI=0; nI<=nbDaysDisplayed; nI++)
	{	
	%>
		<td align="center" class="intfdcolor51"><span class=textePetitbold><%=resource.getString("GML.shortJour"+(nDay+1))%></span></td>
	<%
		nDay = (nDay + 1) % 7;
	}
	%>
      </tr>

	<%
	// ADD CHILD COMPOSED TASKS
	TaskDetail 	task 				= null;
	String 			responsibleName 	= null;
	int 			percentCompleted 	= -1;
	Date 			ctdStartDate 		= null;
	Date 			ctdEndDate			= null;
	for (int a=0; a<tasks.size(); a++) 
	{
		// Get the composed task
		task = (TaskDetail) tasks.get(a);

		responsibleName 	= task.getResponsableFullName();
		percentCompleted 	= task.getAvancement();

		// get Start and End dates for current task
		ctdStartDate 	= task.getDateDebut();
		ctdEndDate		= task.getDateFin();
		Calendar ctdStartCalendar = new GregorianCalendar(1980,1,1);
		Calendar ctdEndCalendar = new GregorianCalendar(1980,1,1);

		// If start or end date is not defined ==> let the dates at 1/1/1980
		if (ctdStartDate != null && ctdEndDate != null)
		{
			ctdStartCalendar.setTime(ctdStartDate);
			ctdEndCalendar.setTime(ctdEndDate);
		}
	%>

	  <tr height="15">
	<%  if (task.getEstDecomposee()==1) { %>
        <td bgcolor="EDEDED" nowrap><a href="ToGantt?Id=<%=task.getId()%>"><img src="<%=resource.getIcon("projectManager.treePlus")%>" border="0" align="absmiddle"></a>&nbsp;<a href="ViewTask?Id=<%=task.getId()%>"><%=task.getNom()%></a></td>
    <%  } else { %>
    	<td bgcolor="EDEDED" nowrap>&nbsp;<a href="ViewTask?Id=<%=task.getId()%>"><%=task.getNom()%></a></td>
    <%  } %>
        <td bgcolor="EDEDED" nowrap><%=responsibleName%></td>
        <td bgcolor="ffffff" nowrap align="right"><%=percentCompleted%> %</td>
	<%	String bgColor = "";
		for (int nI=0; nI<=nbDaysDisplayed; nI++)
		{
			if (holidays.contains(startCalendar.getTime()))
			{
				bgColor = "bgcolor=\"EDEDED\"";
			}
			else
			{
				if (startCalendar.before(ctdStartCalendar) || startCalendar.after(ctdEndCalendar))
					bgColor = "bgcolor=\"#FFFFFF\"";
				else
					bgColor = "class=\"intfdcolor\"";
			}
			out.println("<td "+bgColor+">&nbsp;</td>");
			
			startCalendar.add(GregorianCalendar.DATE, 1);
		}
		startCalendar.add(GregorianCalendar.DATE, -nbDaysDisplayed-1);
	%>
	  </tr>
	<%
	}
	%>
    </table>
<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>