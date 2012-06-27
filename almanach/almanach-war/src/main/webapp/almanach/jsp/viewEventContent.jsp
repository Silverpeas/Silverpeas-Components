<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
	<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
	%>
	<%@ include file="checkAlmanach.jsp" %>

<%

	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

	String user = request.getParameter("flag");
	
	EventDetail event = (EventDetail) request.getAttribute("Event");
	Date startDate = (Date) request.getAttribute("EventStartDate");
	Date endDate = (Date) request.getAttribute("EventEndDate");
	String from = (String) request.getAttribute("From");
	UserDetail contributor = (UserDetail) request.getAttribute("Contributor");

	String startDateString = DateUtil.date2SQLDate(startDate);

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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

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
    	<% if (event.isPeriodic()) { %>
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
</head>
<body class="viewEvent" id="<%=instanceId%>">
  <% 
    Window 	window 	= graphicFactory.getWindow();
    Frame 	frame	= graphicFactory.getFrame();
    Board 	board 	= graphicFactory.getBoard();
    OperationPane operationPane = window.getOperationPane();
        
	BrowseBar browseBar = window.getBrowseBar();
	if (StringUtil.isDefined(from)) {
	  	if (from.equals("ViewYearEvents")) {
			browseBar.setPath("<a href=\""+from+"\">"+resources.getString("almanach.browsebar.yearEvents")+"</a>");
	  	} else if (from.equals("ViewMonthEvents")) {
	  	  	browseBar.setPath("<a href=\""+from+"\">"+resources.getString("almanach.browsebar.monthEvents")+"</a>");
	  	}
	}
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
		tabbedPane.addTab(almanach.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+startDateString, true);
		tabbedPane.addTab(almanach.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+startDateString, false);
		tabbedPane.addTab(resources.getString("GML.attachments"), "editAttFiles.jsp?Id="+id+"&Date="+startDateString, false);
		out.println(tabbedPane.print());
    }
    
    out.println(frame.printBefore());
%>

<div class="rightContent">
	<!--  Attachments -->  
	<%
  		out.flush();
  		getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+event.getId()+"&ComponentId="+instanceId+"&Context=Images").include(request, response);
  	%>
  	<!-- Periodicity -->
	<% if(event.isPeriodic()) { %>
	<div id="eventPeriodicity" class="bgDegradeGris">
		<div class="bgDegradeGris header">
			<h4 class="clean"><%=resources.getString("periodicity")%></h4>
		</div>
		<p>
			<b>
			<%
				if (periodicity.getUnity() == Periodicity.UNIT_DAY) {
					out.print(resources.getString("allDays"));
				} else if (periodicity.getUnity() == Periodicity.UNIT_WEEK) {
					out.print(resources.getString("allWeeks"));
				} else if (periodicity.getUnity() == Periodicity.UNIT_MONTH) {
					out.print(resources.getString("allMonths"));
				} else if (periodicity.getUnity() == Periodicity.UNIT_YEAR) {
					out.print(resources.getString("allYears"));
				}
			%>
			</b>
			
			<% if (periodicity.getUnity() == Periodicity.UNIT_WEEK) { %>
				<br /><span class="eventPeriodicityDaysWeek"><%=resources.getString("choiceDaysWeek")%>&nbsp;:&nbsp;<b>		 
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
			</b></span>
			<%
				} else if (periodicity.getUnity() == Periodicity.UNIT_MONTH) {
					if(periodicity.getDay() > 0) {
			%>
			<br /><span class="eventPeriodicityDayMonth"><%=resources.getString("choiceDayMonth")%>&nbsp;:&nbsp;<b>
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
			</b>  </span>
			<%
					}
				} 
						
			 if (event.getStartDate() != null && periodicity.getUntilDatePeriod() != null) {
						%>
						<br />
						<span class="eventPeriodicityDate"> 
						<% if (event.getStartDate() != null) { 
							out.println(resources.getString("GML.fromDate"));
							out.println("<b>");
							out.print(resources.getInputDate(event.getStartDate()));
						    out.println("</b> ");
						}
						%>
						
						<% if (periodicity.getUntilDatePeriod() != null) {
							out.println(resources.getString("GML.toDate"));
							out.println("<b>");
							out.print(resources.getInputDate(periodicity.getUntilDatePeriod()));
							out.println("</b> ");
						}
						%>
						</span>
						<%
			 }
			
			   
			 if (periodicity.getFrequency() != 1) { %>
				<br />
				<span class="eventPeriodicityFrequency"> <%=resources.getString("frequency")%> :  <b><% out.print(periodicity.getFrequency());  %> </b></span> 
			<%	
			}
			%>
		</p>
	</div>
<%	}   %>
			
	
		
		   <div id="eventInfoPublication" class="bgDegradeGris">
		   	<div class="paragraphe">
		   		<b><%=resources.getString("almanach.createdBy")%></b> <view:username userId="<%=contributor.getId()%>" />
		   		<div class="profilPhoto"><img src="<%=m_context %><%=contributor.getAvatar() %>" alt="" class="defaultAvatar"/></div>
	   		</div>
		   		
			<%	if (StringUtil.isDefined(link)) {	%>
				<p id="permalinkInfo">
					<a href="<%=link%>" title='<%=resources.getString("CopyEventLink")%>'><img src="<%=m_context%>/util/icons/link.gif" border="0" alt='<%=resources.getString("CopyEventLink")%>'/></a>
					<%=resources.getString("GML.permalink")%> <br />
					<input class="inputPermalink" type="text" onfocus="select();" value="<%=URLManager.getServerURL(request)+link %>" />
				</p>
			<% } %>
		   </div>
		   
		   <view:pdcClassificationPreview componentId="<%= instanceId %>" contentId="<%= id %>" />
</div>


<div  class="principalContent">
	<h2 class="eventName">
			<%=EncodeHelper.javaStringToHtmlString(event.getTitle())%>
			 <%if (event.getPriority() != 0){ %>
       			 <span class="eventPriorityHight"><img src='<%= m_context %>/util/icons/important.gif' alt='<%=almanach.getString("prioriteImportante")%>'/></span>
			<% } %>
	</h2>
	
	<div class="eventInfo">	
		<% if (StringUtil.isDefined(event.getPlace())) { %>
			<div class="eventPlace">
				<div class="bloc">
					 <span><%=EncodeHelper.javaStringToHtmlString(event.getPlace())%></span>
			 	</div>
			</div>
		<%}%>
		<div class="eventDate">
			<div class="bloc">
				<span class="eventBeginDate">
					<%=resources.getString("GML.fromDate")%> 
					<%=resources.getOutputDate(startDate)%>
					<%if (event.getStartHour() != null && event.getStartHour().length() != 0) {%>
						<%=almanach.getString("ToHour")%> 
						<%=EncodeHelper.javaStringToHtmlString(event.getStartHour())%>
					<%}%>
					</span>
					
					<span class="eventEndDate">  
					<%
						out.println(resources.getString("GML.toDate"));
						out.println(resources.getOutputDate(endDate));
						if (event.getEndHour() != null && event.getEndHour().length() != 0) {
							out.println(almanach.getString("ToHour"));
							out.println(EncodeHelper.javaStringToHtmlString(event.getEndHour()));
						}%>
					</span>
				</div>
		</div>
			
    	<% if (StringUtil.isDefined(event.getEventUrl())) {
	    		String eventURL = event.getEventUrl();
	    		if (eventURL.indexOf("://") == -1){
	    			eventURL = "http://"+eventURL;
	    		}
	    			
	    		%>
	    		<div class="eventURL">
	    			<div class="bloc">
			    		<span>
			    			<a href="<%=EncodeHelper.javaStringToHtmlString(eventURL)%>" target="_blank"><%=resources.getString("linkToVisit")%></a>
						</span>
					</div>
				</div>
    		<%    			
    		}
    		%>
    <br clear="left"/>&nbsp;
	</div>
	
	
	<div class="eventDesc"><%=description%></div>
		
 </div>   

  <%
		out.println("<br/>");
 		ButtonPane buttonPane = graphicFactory.getButtonPane();
 		String backURL = "almanach.jsp";
 		if (StringUtil.isDefined(from)) {
 		 	backURL = from;
 		}
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.back"), backURL, false));
		out.println("<center>"+buttonPane.print()+"</center>");
		out.println("<br/>");
		out.println(frame.printAfter());				
		out.println(window.printAfter());
	%>
	<form name="eventForm" action="RemoveEvent" method="post">
		<input type="hidden" name="Action"/>
   		<input type="hidden" name="Id" value="<%=id%>"/>
   		<% if (periodicity != null) { %>
   			<input type="hidden" name="EventStartDate" value="<%=startDateString%>"/>
   			<input type="hidden" name="EventEndDate" value="<%=DateUtil.date2SQLDate(endDate)%>"/>
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
</body>
</html>
