<%@ page import="org.silverpeas.util.DateUtil" %>
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<%@ include file="checkAlmanach.jsp" %>

<%
	String language = almanach.getLanguage();

	EventDetail event = (EventDetail) request.getAttribute("Event");
	Date startDate = (Date) request.getAttribute("EventStartDate");
	Date endDate = (Date) request.getAttribute("EventEndDate");

	String startDateString = DateUtil.date2SQLDate(startDate);

	Periodicity periodicity = event.getPeriodicity();

	String description = "";
	String id = event.getPK().getId();
	String title = EncodeHelper.javaStringToHtmlString(event.getTitle());
	if (StringUtil.isDefined(event.getWysiwyg())) {
		description = event.getWysiwyg();
	} else if (StringUtil.isDefined(event.getDescription())) {
		description = EncodeHelper.javaStringToHtmlParagraphe(event.getDescription());
	}

	String day = "";
	if(event.getStartDate() != null) {
		day = resources.getInputDate(event.getStartDate());
	}
	
	String startHour = "";
	if (event.getStartHour() != null) {
	  startHour = event.getStartHour();
	}
	
	String endHour = "";
	if (event.getEndHour() != null) {
	  endHour = event.getEndHour();
	}
	
	String endPeriodicity = "";
	if (periodicity != null && periodicity.getUntilDatePeriod() != null) {
	  endPeriodicity = resources.getInputDate(periodicity.getUntilDatePeriod());
	}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<view:includePlugin name="datepicker"/>
<view:includePlugin name="wysiwyg"/>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
<!--
function getCKEditor() {
	return CKEDITOR.instances.Description;
}
function reallyUpdate() {
	$('.WeekDayWeek').attr("disabled", false); 
	$('.MonthDayWeek').attr("disabled", false); 
  	document.eventForm.ChoiceMonth[0].disabled = false;
  	document.eventForm.ChoiceMonth[1].disabled = false;
  	document.eventForm.MonthNumWeek.disabled = false;

	document.eventForm.Action.value = "ReallyUpdate";
	document.eventForm.submit();
}

function eventDeleteConfirm(t, id)
{
    if (window.confirm("<%=EncodeHelper.javaStringToJsString(almanach.getString("suppressionConfirmation"))%> '" + t + "' ?")){
    	<% if (event.getPeriodicity() != null ) { %>
    		displayBoxOnDelete();
    	<% } else { %>
    		sendEvent('RemoveEvent', 'ReallyDelete');
    	<% } %>
    }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.eventForm.Title.value);
	 var unity = document.eventForm.Unity.value;

     if (isWhitespace(title)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     
     var beginDate = {dateId : 'eventStartDate', hourId : 'StartHour', isMandatory : true};
     var endDate = {dateId : 'eventEndDate', hourId : 'EndHour', defaultDateHour : '23:59'};
     var dateErrors = isPeriodValid(beginDate, endDate);
     $(dateErrors).each(function(index, error) {
       errorMsg += " - " + error.message + "\n";
       errorNb++;
     });

	 if (unity != "0") {
		var frequency = stripInitialWhitespace(document.eventForm.Frequency.value);
		if (isWhitespace(frequency)) {
     		errorMsg+=" - '<%=resources.getString("frequency")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     		errorNb++;
		} else {
			if( ! isInteger(frequency)) {
				errorMsg+=" - '<%=resources.getString("frequency")%>' <%=resources.getString("GML.MustContainsNumber")%>\n";
				errorNb++;
			}
		}
		
		var beginPeriodicity = {dateId : 'eventPeriodicityStartDate'};
		var untilDate = {dateId : 'eventPeriodicityUntil'};
		var periodicityErrors = isPeriodValid(beginPeriodicity, untilDate);
	     $(periodicityErrors).each(function(index, error) {
	       errorMsg += " - " + error.message + "\n";
	       errorNb++;
	     });		
     }

     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function closeMessage()
{
	$("#modalDialogOnUpdate").dialog("close");
	$("#modalDialogOnDelete").dialog("close");
}

function displayBoxOnUpdate()
{
	$("#modalDialogOnUpdate").dialog("open");
}

function displayBoxOnDelete()
{
	$("#modalDialogOnDelete").dialog("open");
}

function sendEventData() {
    if (isCorrectForm()) {
    	var isChanged = 0;
    	<% if(event.getPeriodicity() != null) { %>
    		var unity = document.eventForm.Unity.value;
    		if (unity != '0')
    		{
	    		var oldTitle = '<%=EncodeHelper.javaStringToJsString(event.getTitle())%>';
	    		var title = stripInitialWhitespace(document.eventForm.Title.value);
	    		if (oldTitle != title)
	    		{
	    			isChanged = 1;
	    		}

	    		var oldDesc = '<%=EncodeHelper.javaStringToJsString(description)%>';
				var desc = getCKEditor().getData();
	    		if (oldDesc != desc) {
	    			isChanged = 1;
	    		}

				var oldStartDate = '<%=resources.getOutputDate(startDate)%>';
	    		var startDate = document.eventForm.StartDate.value;
	    		if (oldStartDate != startDate)
	    		{
	    			isChanged = 1;
	    		}

	    		var oldStartHour = '<%=event.getStartHour()%>';
	    		var startHour = document.eventForm.StartHour.value;
	    		if (oldStartHour != startHour)
	    		{
	    			isChanged = 1;
	    		}

				var oldEndDate = '<%=resources.getOutputDate(endDate)%>';
	    		var endDate = document.eventForm.EndDate.value;
	    		if (oldEndDate != endDate)
	    		{
	    			isChanged = 1;
	    		}

	    		var oldEndHour = '<%=event.getEndHour()%>';
	    		var endHour = document.eventForm.EndHour.value;
	    		if (oldEndHour != endHour)
	    		{
	    			isChanged = 1;
	    		}

	    		var oldPlace = '<%=EncodeHelper.javaStringToJsString(event.getPlace())%>';
	    		var place = stripInitialWhitespace(document.eventForm.Place.value);
	    		if (oldPlace != place)
	    		{
	    			isChanged = 1;
	    		}

	    		var oldEventURL = '<%=EncodeHelper.javaStringToJsString(event.getEventUrl())%>';
	    		var eventURL = stripInitialWhitespace(document.eventForm.EventUrl.value);
	    		if (oldEventURL != eventURL)
	    		{
	    			isChanged = 1;
	    		}

	    		var oldPriority = '<%=event.getPriority()%>';
	    		var priority = 0;
				if(document.eventForm.Priority.checked) {
					priority = 1;
				}

	    		if (oldPriority != priority)
	    		{
	    			isChanged = 1;
	    		}

    		}
    	<% } %>

    	if (isChanged == 1)
    	{
    		displayBoxOnUpdate();
    	}
    	else
    	{
    		reallyUpdate();
    	}
    }
}

function changeUnity() {
  var unity = document.eventForm.Unity.value;
  if (unity == "2") {
	  
	$('#eventTypePeriodicitySelected').html('<fmt:message key='almanach.header.periodicity.frequency.weeks'/>');
	$('#eventFrequencyArea').show();
	$('.eventPeriodicityDateArea').show();
		
	$('.WeekDayWeek').attr("disabled", false);
	$('.MonthDayWeek').attr("disabled", true);

    document.eventForm.ChoiceMonth[0].disabled = true;
    document.eventForm.ChoiceMonth[1].disabled = true;
    document.eventForm.MonthNumWeek.disabled = true;
    
    $('#eventPeriodicityChoiceThisMonthArea').hide();	
	$('#eventPeriodicityChoiceMonthArea').hide();
	$('#eventFrequencyWeekArea').show();
	 
  } else if (unity == "3") {

	$('#eventTypePeriodicitySelected').html('<fmt:message key='almanach.header.periodicity.frequency.months'/>');
	$('#eventFrequencyArea').show();
	$('.eventPeriodicityDateArea').show();
	
	$('.WeekDayWeek').attr("disabled", true); 
	
    document.eventForm.ChoiceMonth[0].disabled = false;
    document.eventForm.ChoiceMonth[1].disabled = false;
    if (document.eventForm.ChoiceMonth[0].checked) {
      document.eventForm.MonthNumWeek.disabled = true;
      $('.MonthDayWeek').attr("disabled", true); 
    } else {
      document.eventForm.MonthNumWeek.disabled = false;
      $('.MonthDayWeek').attr("disabled", false);
    }
    
	$('#eventFrequencyWeekArea').hide();
	$('#eventPeriodicityChoiceThisMonthArea').show();	
	$('#eventPeriodicityChoiceMonthArea').show();
  } else {  
	if (unity == "1") {
		$('#eventTypePeriodicitySelected').html('<fmt:message key='almanach.header.periodicity.frequency.days'/>');
		$('#eventFrequencyArea').show();
		$('.eventPeriodicityDateArea').show();
	} else if (unity == "4") {
		$('#eventTypePeriodicitySelected').html('<fmt:message key='almanach.header.periodicity.frequency.years'/>');
		$('#eventFrequencyArea').show();
		$('.eventPeriodicityDateArea').show();
	} else { 
		$('#eventFrequencyArea').hide();
		$('.eventPeriodicityDateArea').hide();
	}
	
	$('.WeekDayWeek').attr("disabled", true);
	document.eventForm.ChoiceMonth[0].disabled = true;
	document.eventForm.ChoiceMonth[1].disabled = true;
	document.eventForm.MonthNumWeek.disabled = true;
	$('.MonthDayWeek').attr("disabled", true); 
	
	$('#eventFrequencyWeekArea').hide();
	$('#eventPeriodicityChoiceThisMonthArea').hide();	
	$('#eventPeriodicityChoiceMonthArea').hide();
  }
}

function changeChoiceMonth() {
	if (document.eventForm.ChoiceMonth[0].checked) {
		document.eventForm.MonthNumWeek.disabled = true;
	    $('.MonthDayWeek').attr("disabled", true); 
	} else {
	    document.eventForm.MonthNumWeek.disabled = false;
	    $('.MonthDayWeek').attr("disabled", false); 
	}
}

function sendEvent(mainAction, action) {
	document.eventForm.action = mainAction;
	document.eventForm.Action.value = action;
	document.eventForm.submit();
}

$(document).ready(function(){
	$("#modalDialogOnUpdate").dialog({
  	  	autoOpen: false,
        modal: true,
        title: "<%=resources.getString("almanach.dialog.update")%>",
        height: 'auto',
        width: 650});

	$("#modalDialogOnDelete").dialog({
  	  	autoOpen: false,
        modal: true,
        title: "<%=resources.getString("almanach.dialog.delete")%>",
        height: 'auto',
        width: 650});
	
	changeUnity();
	
	<view:wysiwyg replace="Description" language="${language}" width="600" height="300" toolbar="almanach" displayFileBrowser="${false}"/>
});
//-->
</script>
</head>
<body onload="document.eventForm.Title.focus()">
  <%
    Window 		window 		= graphicFactory.getWindow();
    Frame 		frame		= graphicFactory.getFrame();
    Board 		board 		= graphicFactory.getBoard();
    TabbedPane 	tabbedPane 	= graphicFactory.getTabbedPane();

    OperationPane operationPane = window.getOperationPane();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "almanach.jsp");
	browseBar.setExtraInformation(event.getTitle());

    operationPane.addOperation(m_context + "/util/icons/almanach_to_del.gif", almanach.getString("supprimerEvenement"), "javascript:onClick=eventDeleteConfirm('" + EncodeHelper.javaStringToJsString(title) + "','" + id +"')");
    out.println(window.printBefore());

	tabbedPane.addTab(almanach.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+startDateString, false);
	tabbedPane.addTab(almanach.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+startDateString, true);

	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<form name="eventForm" action="ReallyUpdateEvent" method="post">

	<fieldset id="eventInfo" class="skinFieldset">
		<legend><fmt:message key='almanach.header.fieldset.main'/></legend>
		<div class="fields">
			<div class="field" id="eventNameArea">
				<label for="eventName" class="txtlibform"><fmt:message key='GML.name'/></label>
				<div class="champs">
					<input id="eventName" type="text" name="Title" size="60" maxlength="<c:out value='${maxTextLength}'/>" value="<%=title%>"/>&nbsp;<img alt="obligatoire" src="icons/cube-rouge.gif" width="5" height="5"/>
				</div>
			</div>
			
			<div class="field" id="eventDescriptionArea">
				<label for="Description" class="txtlibform"><fmt:message key='GML.description'/></label>
				<div class="champs">
					<textarea rows="5" cols="10" name="Description" id="Description"><%=description %></textarea>
				</div>
			</div>
			
			<div class="field" id="eventStartDateArea">
				<label for="eventStartDate" class="txtlibform"><fmt:message key='GML.dateBegin'/></label>
				<div class="champs">
					<input type="text" class="dateToPick" name="StartDate" id="eventStartDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" value="<%=resources.getOutputDate(startDate)%>" onchange="javascript:updateDates();"/>
					<span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
					<span class="txtlibform">&nbsp;<fmt:message key='ToHour'/>&nbsp;</span>
					<input class="inputHour" type="text" name="StartHour" id="StartHour" size="5" maxlength="5" value="<%=startHour%>"/> <span class="txtnote">(hh:mm)</span>&nbsp;<img  alt="obligatoire" src="icons/cube-rouge.gif" width="5" height="5"/>
				</div>
			</div>
			
			<div class="field" id="eventEndDateArea">
				<label for="eventEndDate" class="txtlibform"><fmt:message key='GML.dateEnd'/></label>
				<div class="champs">
					<input id="eventEndDate" type="text" class="dateToPick" name="EndDate" id="eventEndDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" value="<%=resources.getOutputDate(endDate)%>"/>
					<span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
					<span class="txtlibform">&nbsp;<fmt:message key='ToHour'/>&nbsp;</span>
					<input class="inputHour" type="text" name="EndHour" id="EndHour" size="5" maxlength="5" value="<%=endHour%>"/> <span class="txtnote">(hh:mm)</span>
				</div>
			</div>
			
			<div class="field" id="eventPlaceArea">
				<label for="eventPlace" class="txtlibform"><fmt:message key='lieuEvenement'/></label>
				<div class="champs">
					<input type="text" id="eventPlace" name="Place" size="60" maxlength="<c:out value='${maxTextLength}'/>" value="<%=event.getPlace()%>"/>
				</div>
			</div>
			
			<div class="field" id="eventUrlArea">
				<label for="eventUrl" class="txtlibform"><fmt:message key='urlEvenement'/></label>
				<div class="champs">
					<input type="text" id="eventUrl" name="EventUrl" size="60" maxlength="<c:out value='${maxTextLength}'/>" value="<%=event.getEventUrl()%>"/>
				</div>
			</div>
			
			<div class="field" id="eventPriorityArea">
				<label for="eventPriority" class="txtlibform"><fmt:message key='GML.priority'/></label>
				<div class="champs">
					<input type="checkbox" class="checkbox" name="Priority" id="eventPriority" value="checkbox" <%if (event.getPriority() != 0) out.print("checked=\"checked\"");%>/>
				</div>
			</div>		
		</div>
	</fieldset>
	
	<fieldset id="eventPeriodicity" class="skinFieldset">
		<legend><fmt:message key='periodicity'/></legend>
		<div class="fields">
			<div class="field" id="eventTypePeriodicityArea">
				<label for="eventTypePeriodicity" class="txtlibform"><fmt:message key='periodicity'/></label>
				<div class="champs">
					<select id="eventTypePeriodicity" name="Unity" size="1" onchange="changeUnity();">
						<option value="0" <%if (periodicity == null) out.print("selected=\"selected\"");%>><fmt:message key='noPeriodicity'/></option>
	              		<option value="1" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_DAY) out.print("selected=\"selected\"");%>><fmt:message key='allDays'/></option>
	              		<option value="2" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_WEEK) out.print("selected=\"selected\"");%>><fmt:message key='allWeeks'/></option>
	              		<option value="3" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_MONTH) out.print("selected=\"selected\"");%>><fmt:message key='allMonths'/></option>
	              		<option value="4" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_YEAR) out.print("selected=\"selected\"");%>><fmt:message key='allYears'/></option>
					</select>
				</div>
			</div>
			
			<div class="field" id="eventFrequencyArea">
				<label for="eventFrequency" class="txtlibform"><fmt:message key='frequency'/></label>
				<div class="champs">
					<input type="text" id="eventFrequency" name="Frequency" size="5" maxlength="5" value="<% if(periodicity == null) out.print("1"); else out.print(periodicity.getFrequency());%>"/> <span id="eventTypePeriodicitySelected"> <fmt:message key='almanach.header.periodicity.frequency.months'/></span>
				</div>
				
			</div>
			
			<div class="field" id="eventFrequencyWeekArea">
				<label class="txtlibform"><fmt:message key='almanach.header.periodicity.week.on'/></label>
				<div class="champs">
					<input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek2" value="2" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(0) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour2'/>
		            <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek3" value="3" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(1) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour3'/>
		            <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek4" value="4" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(2) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour4'/>
		            <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek5" value="5" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(3) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour5'/>
		            <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek6" value="6" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(4) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour6'/>
		            <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek7" value="7" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(5) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour7'/>
		            <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek1" value="1" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDaysWeekBinary().charAt(6) == '1') out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour1'/>
				</div>
			</div>
			
			<div class="field" id="eventPeriodicityChoiceThisMonthArea">
				<label class="txtlibform"><fmt:message key='almanach.header.periodicity.rule'/></label>
				<div class="champs">
					<input id="eventPeriodicityChoiceMonth" type="radio" class="radio" name="ChoiceMonth" value="MonthDate" disabled="disabled" <%if(event.isPeriodic() && periodicity.getNumWeek() == 0) out.print("checked=\"checked\"");%> onclick="changeChoiceMonth();"/><fmt:message key='choiceDateMonth'/>&nbsp;
				</div>
			</div>
			
			<div class="field" id="eventPeriodicityChoiceMonthArea">
				<div class="champs">
					<input type="radio" class="radio" name="ChoiceMonth" value="MonthDay" disabled="disabled" <%if(event.isPeriodic() && periodicity.getNumWeek() != 0) out.print("checked=\"checked\"");%> onclick="changeChoiceMonth();"/><fmt:message key='choiceDayMonth'/>&nbsp;:&nbsp;
					<select name="MonthNumWeek" size="1" disabled="disabled">
						<option value="1" <%if (event.isPeriodic() && periodicity.getNumWeek() == 1) out.print("selected=\"selected\"");%>><fmt:message key='first'/></option>
		                <option value="2" <%if (event.isPeriodic() && periodicity.getNumWeek() == 2) out.print("selected=\"selected\"");%>><fmt:message key='second'/></option>
		                <option value="3" <%if (event.isPeriodic() && periodicity.getNumWeek() == 3) out.print("selected=\"selected\"");%>><fmt:message key='third'/></option>
		                <option value="4" <%if (event.isPeriodic() && periodicity.getNumWeek() == 4) out.print("selected=\"selected\"");%>><fmt:message key='fourth'/></option>
		                <option value="-1" <%if (event.isPeriodic() && periodicity.getNumWeek() == -1) out.print("selected=\"selected\"");%>><fmt:message key='fifth'/></option>
					</select>
					
					<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="2" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 2) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour2'/>
	              	<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="3" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 3) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour3'/>
					<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="4" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 4) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour4'/>
	              	<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="5" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 5) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour5'/>
	              	<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="6" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 6) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour6'/>
	              	<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="7" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 7) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour7'/>
	              	<input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="1" disabled="disabled" <%if(event.isPeriodic() && periodicity.getDay() == 1) out.print("checked=\"checked\"");%>/><fmt:message key='GML.jour1'/>
				</div>
			</div>
			
			<div class="field eventPeriodicityDateArea" id="eventPeriodicityStartDateArea">
				<label for="eventPeriodicityStartDate" class="txtlibform"><fmt:message key='beginDatePeriodicity'/></label>
				<div class="champs">
					<input type="text" id="eventPeriodicityStartDate" class="dateToPick" name="PeriodicityStartDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" readonly="readonly" value="<%=resources.getInputDate(event.getStartDate())%>" /> (<fmt:message key='GML.dateFormatExemple'/>)
				</div>
			</div>
			
			<div class="field eventPeriodicityDateArea" id="eventPeriodicityUntilDateArea">
				<label for="eventPeriodicityUntil" class="txtlibform"><fmt:message key='endDatePeriodicity'/></label>
				<div class="champs">
					<input type="text" id="eventPeriodicityUntil" class="dateToPick" name="PeriodicityUntilDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" value="<%=endPeriodicity%>"/><span class="txtnote"> (<fmt:message key='GML.dateFormatExemple'/>)</span>
				</div>
			</div>
			
		</div>
	</fieldset>
	
	<view:pdcClassification componentId="<%= instanceId %>" contentId="<%= id %>" editable="true" />

  	<input type="hidden" name="Action"/>
  	<input type="hidden" name="Id" value="<%=event.getPK().getId()%>"/>
  	<input type="hidden" name="EventStartDate" value="<%=startDateString%>"/>
  	<input type="hidden" name="EventEndDate" value="<%=DateUtil.date2SQLDate(endDate)%>"/>
 </form>
   <center><br/>
   <%
 		ButtonPane buttonPane = graphicFactory.getButtonPane();
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendEventData()", false));
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.back"), "almanach.jsp", false));
		out.println(buttonPane.print());
   %>
   </center>
<%
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
<div id="modalDialogOnUpdate" style="display: none">
	<%
	ButtonPane buttonPaneOnUpdate = graphicFactory.getButtonPane();
	buttonPaneOnUpdate.addButton(graphicFactory.getFormButton(resources.getString("occurenceOnly"), "javascript:onClick=sendEvent('ReallyUpdateEvent', 'ReallyUpdateOccurence')", false));
	buttonPaneOnUpdate.addButton(graphicFactory.getFormButton(resources.getString("allEvents"), "javascript:onClick=sendEvent('ReallyUpdateEvent', 'ReallyUpdateSerial')", false));
	buttonPaneOnUpdate.addButton(graphicFactory.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=closeMessage()", false));
	%>

	<table><tr><td align="center"><br/><%=resources.getString("eventsToUpdate") %>
	<br/><br/>
	<center><%=buttonPaneOnUpdate.print() %></center>
	</td></tr></table>
</div>
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
