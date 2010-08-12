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
	<%@ include file="checkAlmanach.jsp" %>

<%

	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

	// récupération du user
	String user = request.getParameter("flag");
	
	EventDetail event = (EventDetail) request.getAttribute("CompleteEvent");
	Date dateDebutIteration = (Date) request.getAttribute("DateDebutIteration");
	Date dateFinIteration = (Date) request.getAttribute("DateFinIteration");

	String dateDebutIterationString = DateUtil.date2SQLDate(dateDebutIteration);

	Periodicity periodicity = event.getPeriodicity();
	String id = event.getPK().getId();

  //initialisation de l'objet event
	String title = null;
	String link = null; 
	String description = "";
	try{
		event = almanach.getEventDetail(id);
		title = event.getTitle();
		if (title.length() > 30) {
			title = title.substring(0,30) + "....";
		}
		link = event.getPermalink();
		if (StringUtil.isDefined(event.getWysiwyg())) {
			description = event.getWysiwyg();
		}
		else if (StringUtil.isDefined(event.getDescription())) {
			description = EncodeHelper.javaStringToHtmlParagraphe(event.getDescription());
		}
	} catch(AlmanachPrivateException ace){
		request.setAttribute("error", ace);
		getServletConfig().getServletContext().getRequestDispatcher(almanach.getComponentUrl()+"erreurSaisie.jsp").forward(request, response);
		return;
	 }
%>

<HTML>
<HEAD>
<% out.println(graphicFactory.getLookStyleSheet());%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

var notifyWindow = window;

function goToNotify(url) 
{
	windowName = "notifyWindow";
	larg = "740";
	haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
        notifyWindow.close();
    notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function eventDeleteConfirm(t)
{
    if (window.confirm("<%=EncodeHelper.javaStringToJsString(almanach.getString("suppressionConfirmation"))%> '" + t + "' ?")){
    	<% if (periodicity != null ) { %>
    		$("#modalDialogOnDelete").dialog("open");
    	<% } else { %>
    		sendEvent('RemoveEvent', 'ReallyDelete');
    	<% } %>
    }
}

function sendEvent(mainAction, action) {
	document.eventForm.action = mainAction;
	document.eventForm.Action.value = action;
	document.eventForm.submit();
}

function closeMessage()
{
	$("#modalDialogOnDelete").dialog("close");
}

$(document).ready(function(){
	$("#modalDialogOnDelete").dialog({
  	  	autoOpen: false,
        modal: true,
        title: "<%=resources.getString("almanach.dialog.delete")%>",
        height: 'auto',
        width: 650});
});
</script>
</HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
  <% 
    Window 	window 	= graphicFactory.getWindow();
    Frame 	frame	= graphicFactory.getFrame();
    Board 	board 	= graphicFactory.getBoard();
    OperationPane operationPane = window.getOperationPane();
        
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setExtraInformation(title);
	    
    String url = "ToAlertUser?Id="+id;
    operationPane.addOperation(m_context+"/util/icons/alert.gif", resources.getString("GML.notify"),"javaScript:onClick=goToNotify('"+url+"')") ;
    if (!"user".equals(user))
    {
      operationPane.addLine();
      operationPane.addOperation(m_context + "/util/icons/almanach_to_del.gif", almanach.getString("supprimerEvenement"), "javascript:onClick=eventDeleteConfirm('" + EncodeHelper.javaStringToJsString(title) + "')");
    }

    out.println(window.printBefore());
    
    if (!"user".equals(user))
    {
    	TabbedPane tabbedPane = graphicFactory.getTabbedPane();
		tabbedPane.addTab(almanach.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+dateDebutIterationString, true);
		tabbedPane.addTab(almanach.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		tabbedPane.addTab(resources.getString("GML.attachments"), "editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
  		if (almanach.isPdcUsed()) {
			tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		}
		out.println(tabbedPane.print());
    }
    
    out.println(frame.printBefore());
%>
<center>
<table border="0"><tr><td width="100%">
<%
        out.println(board.printBefore());
%>
<table CELLPADDING=5 width="100%">
		<tr> 
      <td nowrap class="txtlibform" width="20%"><%=resources.getString("GML.name")%> :</td>
      <td><%=EncodeHelper.javaStringToHtmlString(event.getTitle())%></td>
    </tr>
    <tr> 
      <td nowrap class="txtlibform" valign="top"><%=resources.getString("GML.description")%> :</td>
      <td><%=description%></td>
    </tr>
    <tr> 
      <td nowrap class="txtlibform"><%=resources.getString("GML.dateBegin")%> :</td>
      <td><%=resources.getOutputDate(dateDebutIteration)%>
	  <%if (event.getStartHour() != null && event.getStartHour().length() != 0) {%>
	  	<%=almanach.getString("ToHour")%> 
		<%=EncodeHelper.javaStringToHtmlString(event.getStartHour())%>
	  <%}%>
	  </td>
    </tr>
    <tr> 
      <td nowrap class="txtlibform"><%=resources.getString("GML.dateEnd")%> :</td>
      <td><%=resources.getOutputDate(dateFinIteration)%>
	  <%if (event.getEndHour() != null && event.getEndHour().length() != 0) {%>
		   	<%=almanach.getString("ToHour")%> 
			<%=EncodeHelper.javaStringToHtmlString(event.getEndHour())%>
		  <%}%>
	  </td>
    </tr>   
    <tr> 
    	<td nowrap class="txtlibform"><%=almanach.getString("lieuEvenement")%> :</td>
      	<td><%=EncodeHelper.javaStringToHtmlString(event.getPlace())%></td>
    </tr>
    <tr> 
    	<td nowrap class="txtlibform"><%=almanach.getString("urlEvenement")%> :</td>
    	<% if (StringUtil.isDefined(event.getEventUrl())) {
    		String eventURL = event.getEventUrl();
    		if (eventURL.indexOf("://") == -1)
    			eventURL = "http://"+eventURL;
    		%>
    		<td>
    		<a href="<%=EncodeHelper.javaStringToHtmlString(eventURL)%>" target="_blank"><%=EncodeHelper.javaStringToHtmlString(event.getEventUrl())%></a>
			</td>
		<%}%>
    </tr>
    <tr> 
      <td nowrap class="txtlibform"><%=resources.getString("GML.priority")%> :</td>
      <%if (event.getPriority() != 0){ %>
        <td><%=almanach.getString("prioriteImportante")%></td>
	<% } else{ %>
        <td><%=almanach.getString("prioriteNormale")%></td>
	<% } %>
    </tr>
    
    <%	if (StringUtil.isDefined(link)) {	%>
    	<tr>
    		<td nowrap class="txtlibform"><%=almanach.getString("permalink")%> :</td>
    		<td><a href="<%=link%>"><img  src="<%=m_context%>/util/icons/link.gif" border="0" alt='<%=resources.getString("CopyEventLink")%>' title='<%=resources.getString("CopyEventLink")%>' ></a></td>
    	</tr>
	<% } %>

		<tr>
          <td nowrap class="txtlibform"><%=resources.getString("periodicity")%>&nbsp;:&nbsp;</td>
		  <td align=left>
			<%
			if(periodicity == null) {
				out.print(resources.getString("noPeriodicity"));
			}
			else
			{
				if (periodicity.getUnity() == Periodicity.UNIT_DAY) {
					out.print(resources.getString("allDays"));
				} else if (periodicity.getUnity() == Periodicity.UNIT_WEEK) {
					out.print(resources.getString("allWeeks"));
				} else if (periodicity.getUnity() == Periodicity.UNIT_MONTH) {
					out.print(resources.getString("allMonths"));
				} else if (periodicity.getUnity() == Periodicity.UNIT_YEAR) {
					out.print(resources.getString("allYears"));
				}
			}
			%>
		  </td>
        </tr>
		<%
			if(periodicity != null) {
		%>
			<tr>
	          <td nowrap align=right class="txtlibform"><%=resources.getString("frequency")%>&nbsp;:&nbsp;</td>
			  <td align=left><% out.print(periodicity.getFrequency());  %> </td> 
	        </tr>
			
			<%
				if (periodicity.getUnity() == Periodicity.UNIT_WEEK) {
			%>
			<tr>
	          <td nowrap align=right class="txtlibform"><%=resources.getString("choiceDaysWeek")%>&nbsp;:&nbsp;</td>
			  <td align=left>
			<%
					String days = "";
					if(periodicity.getDaysWeekBinary().charAt(0) == '1') { 
						days += resources.getString("GML.jour2")+", ";//Monday
					}
					if(periodicity.getDaysWeekBinary().charAt(1) == '1') {
						days += resources.getString("GML.jour3")+", "; //Tuesday
					}
					if(periodicity.getDaysWeekBinary().charAt(2) == '1') {
						days += resources.getString("GML.jour4")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(3) == '1') {
						days += resources.getString("GML.jour5")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(4) == '1') {
						days += resources.getString("GML.jour6")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(5) == '1') {
						days += resources.getString("GML.jour7")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(6) == '1') {
						days += resources.getString("GML.jour1")+", ";
					}
					if(days.length() > 0) {
						days = days.substring(0, days.length() - 2);
					}
					out.print(days);
			%>
			 </td>
	        </tr>
			<%
				} else if (periodicity.getUnity() == Periodicity.UNIT_MONTH) {
					if(periodicity.getDay() > 0) {
			%>
			<tr>
			  <td nowrap align=right class="txtlibform"><%=resources.getString("choiceDayMonth")%>&nbsp;:&nbsp;</td>
			  <td align=left>	
			<%
						if(periodicity.getNumWeek() == 1) {
							out.print(resources.getString("first"));
						} else if (periodicity.getNumWeek() == 2) {
							out.print(resources.getString("second"));
						} else if (periodicity.getNumWeek() == 3) {
							out.print(resources.getString("second"));
						} else if (periodicity.getNumWeek() == 2) {
							out.print(resources.getString("third"));
						} else if (periodicity.getNumWeek() == -1) {
							out.print(resources.getString("fifth"));
						} 
						out.print(" ");
						if(periodicity.getDay() == 2) {
							out.print(resources.getString("GML.jour2"));
						} else if (periodicity.getDay() == 3) {
							out.print(resources.getString("GML.jour3"));
						} else if (periodicity.getDay() == 4) {
							out.print(resources.getString("GML.jour4"));
						} else if (periodicity.getDay() == 5) {
							out.print(resources.getString("GML.jour5"));
						} else if (periodicity.getDay() == 6) {
							out.print(resources.getString("GML.jour6"));
						} else if (periodicity.getDay() == 7) {
							out.print(resources.getString("GML.jour7"));
						} else if (periodicity.getDay() == 1) {
							out.print(resources.getString("GML.jour1"));
						}
			%>
			  </td> 
			</tr>
			<%
					}
				} 
			%>
			<tr> 
	          <td nowrap align=right class="txtlibform"><span><%=resources.getString("beginDatePeriodicity")%>&nbsp;:&nbsp;</td>
	          <td valign="baseline">  
				<% if (event.getStartDate() != null) out.print(resources.getInputDate(event.getStartDate()));%>
	          </td>
	        </tr>
			<tr> 
	          <td nowrap align=right class="txtlibform"><span><%=resources.getString("endDatePeriodicity")%>&nbsp;:&nbsp;</td>
	          <td valign="baseline"> 
	           <% if (periodicity.getUntilDatePeriod() != null) out.print(resources.getInputDate(periodicity.getUntilDatePeriod()));%>
	          </td>
	        </tr>
		<%
		}
		%>
  </table>
  <%
		out.println(board.printAfter());
  %>
  </td><td valign="top">
  <%
  		out.flush();
  		getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+event.getId()+"&ComponentId="+instanceId+"&Context=Images").include(request, response);
  %>
  </td></tr>
  </table>
  <%
		out.println("<br>");
 		ButtonPane buttonPane = graphicFactory.getButtonPane();
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.back"), "almanach.jsp", false));
		out.println(buttonPane.print());
		out.println("<br>");
		out.println("</center>");
		out.println(frame.printAfter());				
		out.println(window.printAfter());
	%>
	<form name="eventForm" action="RemoveEvent" method="POST">
		<input type="hidden" name="Action"/>
   		<input type="hidden" name="Id" value="<%=id%>"/>
   		<% if (periodicity != null) { %>
   			<input type="hidden" name="DateDebutIteration" value="<%=dateDebutIterationString%>"/>
   			<input type="hidden" name="DateFinIteration" value="<%=DateUtil.date2SQLDate(dateFinIteration)%>"/>
   		<% } %>
	</form>
<div id="modalDialogOnDelete" style="display: none">
	<% 
	ButtonPane buttonPaneOnDelete = graphicFactory.getButtonPane();
	buttonPaneOnDelete.addButton(graphicFactory.getFormButton(resources.getString("occurenceOnly"), "javascript:onClick=sendEvent('RemoveEvent', 'ReallyDeleteOccurence')", false));
	buttonPaneOnDelete.addButton(graphicFactory.getFormButton(resources.getString("allEvents"), "javascript:onClick=sendEvent('RemoveEvent', 'ReallyDelete')", false));
	buttonPaneOnDelete.addButton(graphicFactory.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=closeMessage()", false));
	%>
	<table><tr><td align="center"><br/><%=resources.getString("eventsToDelete") %>
	<br/><br/>
	<center><%=buttonPaneOnDelete.print()%></center>
	</td></tr></table>
</div>	
</BODY>
</HTML>