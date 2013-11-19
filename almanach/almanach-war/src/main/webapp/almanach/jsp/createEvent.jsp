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
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${requestScope.Language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>

<c:set var="fieldUtil" value="${requestScope.FieldUtil}"/>
<c:set var="event" value="${requestScope.Event}"/>
<c:set var="day" value="${requestScope.Day}"/>
<c:set var="language" value="${requestScope.Language}"/>
<c:set var="maxDateLength" value="${requestScope.MaxDateFieldLength}"/>
<c:set var="maxTextLength" value="${requestScope.MaxTextFieldLength}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle"/></title>
<view:looknfeel/>
<view:includePlugin name="datepicker"/>
<view:includePlugin name="wysiwyg"/>
<view:includePlugin name="popup"/>
<link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet" />
<script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
<script type="text/javascript">
function reallyAdd() {
	$('.WeekDayWeek').attr("disabled", false); 
	$('.MonthDayWeek').attr("disabled", false); 
  	document.eventForm.ChoiceMonth[0].disabled = false;
  	document.eventForm.ChoiceMonth[1].disabled = false;
  	document.eventForm.MonthNumWeek.disabled = false;
  
  	<view:pdcPositions setIn="document.eventForm.Positions.value"/>
  	document.eventForm.action = "ReallyAddEvent";
  	$(document.eventForm).submit();
}

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;
  var title = stripInitialWhitespace(document.eventForm.Title.value);
  var beginDate = document.eventForm.StartDate.value;
  var endDate = document.eventForm.EndDate.value;
  var beginTime = stripInitialWhitespace(document.eventForm.StartHour.value);
  var endTime = stripInitialWhitespace(document.eventForm.EndHour.value);
  var unity = document.eventForm.Unity.value;
  var frequency = stripInitialWhitespace(document.eventForm.Frequency.value);
  var beginPeriodicity = document.eventForm.PeriodicityStartDate.value;
  var untilDate = document.eventForm.PeriodicityUntilDate.value;

  var beginDateOK = true;
  var beginPeriodicityOK = true;

  if (isWhitespace(title)) {
    errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='GML.name'/>' <fmt:message key='GML.MustBeFilled'/>\n";
    errorNb++;
  }
  if (isWhitespace(beginDate)) {
    errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='GML.dateBegin'/>' <fmt:message key='GML.MustBeFilled'/>\n";
    errorNb++;
  }
  else {
	if (!isDateOK(beginDate, '<c:out value="${language}"/>')) {
      errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='GML.dateBegin'/>' <fmt:message key='GML.MustContainsCorrectDate'/>\n";
      errorNb++;
      beginDateOK = false;
    }
  }

  if (!checkHour(beginTime) || (isWhitespace(beginTime) && !isWhitespace(endTime))) {
    errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='hourBegin'/>' <fmt:message key='MustContainsCorrectHour'/>\n";
    errorNb++;
  }

  if (!isWhitespace(endDate)) {
	if (!isDateOK(endDate, '<c:out value="${language}"/>')) {
      errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='GML.dateEnd'/> <fmt:message key='GML.MustContainsCorrectDate'/>\n";
      errorNb++;
    } else {
        if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
          if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, '<c:out value="${language}"/>')) {
            errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='GML.dateEnd'/>' <fmt:message key='GML.MustContainsPostOrEqualDateTo'/> " + beginDate + "\n";
            errorNb++;
          }
        } else {
          if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
            if (!isFuture(endDate, '<c:out value="${language}"/>')) {
              errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='GML.dateEnd'/>' <fmt:message key='GML.MustContainsPostDate'/>\n";
              errorNb++;
            }
          }
        }
    }
  }

  if (!checkHour(endTime)) {
    errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='hourEnd'/>' <fmt:message key='MustContainsCorrectHour'/>\n";
    errorNb++;
  }
    
  if (beginDate == endDate && !isWhitespace(endTime) && !isWhitespace(beginTime)) {
    var beginHour = atoi(extractHour(beginTime));
    var beginMinute = atoi(extractMinute(beginTime));
    var endHour = atoi(extractHour(endTime));
    var endMinute = atoi(extractMinute(endTime));
    if (beginHour > endHour || (beginHour == endHour && beginMinute > endMinute)) {
      errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='hourEnd'/>' <fmt:message key='GML.MustContainsPostOrEqualDateTo'/> " + beginTime + "\n";
      errorNb++;
    }
  }

  if (unity != "0") {
    if (isWhitespace(frequency)) {
      errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='frequency'/>' <fmt:message key='GML.MustBeFilled'/>\n";
      errorNb++;
    } else {
      if (! isInteger(frequency)) {
        errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='frequency'/>' <fmt:message key='GML.MustContainsNumber'/>\n";
        errorNb++;
      }
    }

    if (! isWhitespace(beginPeriodicity)) {
		if (!isDateOK(beginPeriodicity, '<c:out value="${language}"/>')) {
        	errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='beginDatePeriodicity'/>' <fmt:message key='GML.MustContainsCorrectDate'/>\n";
        	errorNb++;
        	beginPeriodicityOK = false;
      	}
    }

    if (! isWhitespace(untilDate)) {
    	if (!isDateOK(untilDate, '<c:out value="${language}"/>')) {
	        errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='endDatePeriodicity'/>' <fmt:message key='GML.MustContainsCorrectDate'/>\n";
	        errorNb++;
      	} else {
          if (!isWhitespace(beginPeriodicity) && !isWhitespace(untilDate)) {
            if (beginPeriodicityOK && !isDate1AfterDate2(untilDate, beginPeriodicity, '<c:out value="${language}"/>')) {
              errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='endDatePeriodicity'/>' <fmt:message key='GML.MustContainsPostOrEqualDateTo'/> " + beginPeriodicity + "\n";
              errorNb++;
            }
          } else {
            if (isWhitespace(beginPeriodicity) && !isWhitespace(untilDate)) {
              if (!isFuture(untilDate, '<c:out value="${language}"/>')) {
                errorMsg += "  - <fmt:message key='GML.theField'/> '<fmt:message key='endDatePeriodicity'/>' <fmt:message key='GML.MustContainsPostDate'/>\n";
                errorNb++;
              }
            }
          }
        }
      }
  }
  
  <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>;

  switch (errorNb) {
    case 0 :
      result = true;
      break;
    case 1 :
      errorMsg = "<fmt:message key='GML.ThisFormContains'/> 1 <fmt:message key='GML.error'/> : \n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
    default :
      errorMsg = "<fmt:message key='GML.ThisFormContains'/> " + errorNb + " <fmt:message key='GML.errors'/> :\n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
  }
  return result;
}

function sendEventData() {
  if (isCorrectForm()) {
    reallyAdd();
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

function updateDates() {
  document.eventForm.EndDate.value = document.eventForm.StartDate.value;
  document.eventForm.PeriodicityStartDate.value = document.eventForm.StartDate.value;
}

$(document).ready(function() {
	$('#eventFrequencyWeekArea').hide();
	$('#eventPeriodicityChoiceThisMonthArea').hide();
	$('#eventPeriodicityChoiceMonthArea').hide();
	$('#eventFrequencyArea').hide();
	$('.eventPeriodicityDateArea').hide();
	
	<view:wysiwyg replace="Description" language="${language}" width="600" height="300" toolbar="almanach"/>
});
</script>
</head>
<body onload="document.eventForm.Title.focus()" class="eventManager">
<fmt:message key="evenement" var="eventTab"/>
<fmt:message key="accueil" var="currentPathLabel"/>
<view:browseBar componentId="${instanceId}" extraInformations="${currentPathLabel}"/>
<view:window>
  <view:tabs>
    <view:tab action="#" label="${eventTab}" selected="true"/>
  </view:tabs>
  <view:frame>
      <form name="eventForm" action="editEvent.jsp" method="post">
      
      	<fieldset id="eventInfo" class="skinFieldset">
			<legend><fmt:message key='almanach.header.fieldset.main'/></legend>
			<div class="fields">
				<div class="field" id="eventNameArea">
					<label for="eventName" class="txtlibform"><fmt:message key='GML.name'/></label>
					<div class="champs">
						<input id="eventName" type="text" name="Title" size="60" maxlength="<c:out value='${maxTextLength}'/>"/>&nbsp;<img alt="obligatoire" src="icons/cube-rouge.gif" width="5" height="5"/>
					</div>
				</div>
				
				<div class="field" id="eventDescriptionArea">
					<label for="Description" class="txtlibform"><fmt:message key='GML.description'/></label>
					<div class="champs">
						<textarea rows="5" cols="10" name="Description" id="Description"></textarea>
					</div>
				</div>
				
				<div class="field" id="eventStartDateArea">
					<label for="eventStartDate" class="txtlibform"><fmt:message key='GML.dateBegin'/></label>
					<div class="champs">
						<input id="eventStartDate" type="text" class="dateToPick" name="StartDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" value="<c:out value='${day[0]}'/>" onchange="javascript:updateDates();"/>
						<span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
						<span class="txtlibform">&nbsp;<fmt:message key='ToHour'/>&nbsp;</span>
						<input class="inputHour" type="text" name="StartHour" size="5" maxlength="5" value="<c:out value='${day[1]}'/>"/> <span class="txtnote">(hh:mm)</span>&nbsp;<img  alt="obligatoire" src="icons/cube-rouge.gif" width="5" height="5"/>
					</div>
				</div>
				
				<div class="field" id="eventEndDateArea">
					<label for="eventEndDate" class="txtlibform"><fmt:message key='GML.dateEnd'/></label>
					<div class="champs">
						<input id="eventEndDate" type="text" class="dateToPick" name="EndDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" value="<c:out value='${day[0]}'/>"/>
						<span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
						<span class="txtlibform">&nbsp;<fmt:message key='ToHour'/>&nbsp;</span>
						<input class="inputHour" type="text" name="EndHour" size="5" maxlength="5"/> <span class="txtnote">(hh:mm)</span>
					</div>
				</div>
				
				<div class="field" id="eventPlaceArea">
					<label for="eventPlace" class="txtlibform"><fmt:message key='lieuEvenement'/></label>
					<div class="champs">
						<input type="text" id="eventPlace" name="Place" size="60" maxlength="<c:out value='${maxTextLength}'/>"/>
					</div>
				</div>
				
				<div class="field" id="eventUrlArea">
					<label for="eventUrl" class="txtlibform"><fmt:message key='urlEvenement'/></label>
					<div class="champs">
						<input type="text" id="eventUrl" name="EventUrl" size="60" maxlength="<c:out value='${maxTextLength}'/>"/>
					</div>
				</div>
				
				<div class="field" id="eventPriorityArea">
					<label for="eventPriority" class="txtlibform"><fmt:message key='GML.priority'/></label>
					<div class="champs">
						<input type="checkbox" class="checkbox" name="Priority" id="eventPriority" value="checkbox"/>
					</div>
				</div>		
			</div>
		</fieldset>
        <div class="table">
          <div class="cell">
            <fieldset id="eventPeriodicity" class="skinFieldset">

              <legend><fmt:message key='periodicity'/></legend>
              <div class="fields">
                <div class="field" id="eventTypePeriodicityArea">
                  <label for="eventTypePeriodicity" class="txtlibform"><fmt:message key='periodicity'/></label>

                  <div class="champs">
                    <select id="eventTypePeriodicity" name="Unity" size="1" onchange="changeUnity();">
                      <option value="0"><fmt:message key='noPeriodicity'/></option>
                      <option value="1"><fmt:message key='allDays'/></option>
                      <option value="2"><fmt:message key='allWeeks'/></option>
                      <option value="3"><fmt:message key='allMonths'/></option>
                      <option value="4"><fmt:message key='allYears'/></option>
                    </select>
                  </div>
                </div>

                <div class="field" id="eventFrequencyArea">
                  <label for="eventFrequency" class="txtlibform"><fmt:message key='frequency'/></label>

                  <div class="champs">
                    <input type="text" id="eventFrequency" name="Frequency" size="5" maxlength="5" value="1"/>
                    <span id="eventTypePeriodicitySelected"> <fmt:message key='almanach.header.periodicity.frequency.months'/></span>
                  </div>

                </div>

                <div class="field" id="eventFrequencyWeekArea">
                  <label class="txtlibform"><fmt:message key='almanach.header.periodicity.week.on'/></label>

                  <div class="champs">
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek2" value="2" disabled="disabled"/><fmt:message key='GML.jour2'/>
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek3" value="3" disabled="disabled"/><fmt:message key='GML.jour3'/>
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek4" value="4" disabled="disabled"/><fmt:message key='GML.jour4'/>
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek5" value="5" disabled="disabled"/><fmt:message key='GML.jour5'/>
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek6" value="6" disabled="disabled"/><fmt:message key='GML.jour6'/>
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek7" value="7" disabled="disabled"/><fmt:message key='GML.jour7'/>
                    <input type="checkbox" class="checkbox WeekDayWeek" name="WeekDayWeek1" value="1" disabled="disabled"/><fmt:message key='GML.jour1'/>
                  </div>
                </div>

                <div class="field" id="eventPeriodicityChoiceThisMonthArea">
                  <label class="txtlibform"><fmt:message key='almanach.header.periodicity.rule'/></label>

                  <div class="champs">
                    <input id="eventPeriodicityChoiceMonth" type="radio" class="radio" name="ChoiceMonth" value="MonthDate" disabled="disabled" checked="checked" onclick="changeChoiceMonth();"/><fmt:message key='choiceDateMonth'/>&nbsp;
                  </div>
                </div>

                <div class="field" id="eventPeriodicityChoiceMonthArea">
                  <div class="champs">
                    <input type="radio" class="radio" name="ChoiceMonth" value="MonthDay" disabled="disabled" onclick="changeChoiceMonth();"/><fmt:message key='choiceDayMonth'/>&nbsp;:&nbsp;
                    <select name="MonthNumWeek" size="1" disabled="disabled">
                      <option value="1"><fmt:message key='first'/></option>
                      <option value="2"><fmt:message key='second'/></option>
                      <option value="3"><fmt:message key='third'/></option>
                      <option value="4"><fmt:message key='fourth'/></option>
                      <option value="-1"><fmt:message key='fifth'/></option>
                    </select>

                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="2" disabled="disabled" checked="checked"/><fmt:message key='GML.jour2'/>
                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="3" disabled="disabled"/><fmt:message key='GML.jour3'/>
                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="4" disabled="disabled"/><fmt:message key='GML.jour4'/>
                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="5" disabled="disabled"/><fmt:message key='GML.jour5'/>
                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="6" disabled="disabled"/><fmt:message key='GML.jour6'/>
                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="7" disabled="disabled"/><fmt:message key='GML.jour7'/>
                    <input type="radio" class="radio MonthDayWeek" name="MonthDayWeek" value="1" disabled="disabled"/><fmt:message key='GML.jour1'/>
                  </div>
                </div>

                <div class="field eventPeriodicityDateArea" id="eventPeriodicityStartDateArea">
                  <label for="eventPeriodicityStartDate" class="txtlibform">
                    <fmt:message key='beginDatePeriodicity'/> </label>

                  <div class="champs">
                    <input type="text" id="eventPeriodicityStartDate" class="dateToPick" name="PeriodicityStartDate" size="14" maxlength="<c:out value='${maxDateLength}'/>" readonly="readonly" value="<c:out value='${day[0]}'/>"/>
                    (<fmt:message key='GML.dateFormatExemple'/>)
                  </div>
                </div>

                <div class="field eventPeriodicityDateArea" id="eventPeriodicityUntilDateArea">
                  <label for="eventPeriodicityUntil" class="txtlibform"><fmt:message key='endDatePeriodicity'/></label>

                  <div class="champs">
                    <input type="text" id="eventPeriodicityUntil" class="dateToPick" name="PeriodicityUntilDate" size="14" maxlength="<c:out value='${maxDateLength}'/>"/><span class="txtnote"> (<fmt:message key='GML.dateFormatExemple'/>)</span>
                  </div>
                </div>
              </div>
            </fieldset>
          </div>
          <div class="cell" style="width: 50%">
            <view:fileUpload fieldset="true" jqueryFormSelector="form[name='eventForm']" />
          </div>
        </div>
		
		<view:pdcNewContentClassification componentId="${instanceId}"/>
		
		<div class="legend">
			<img alt="obligatoire" src="icons/cube-rouge.gif" width="5" height="5"/> : <fmt:message key='GML.requiredField'/>
		</div>
		
		<input type="hidden" name="Action"/>
        <input type="hidden" name="Id"/>
        <input type="hidden" name="Positions"/>
      </form>
    <br/>
    <center>
    <fmt:message key="GML.validate" var="validateLabel"/>
    <fmt:message key="GML.cancel" var="cancelLabel"/>
    <view:buttonPane>
      <view:button action="javascript:onClick=sendEventData();" disabled="false"
                   label="${validateLabel}"/>
      <view:button action="almanach.jsp" disabled="false" label="${cancelLabel}"/>
    </view:buttonPane>
    </center>
  </view:frame>
</view:window>
<form name="almanachForm" action="almanach.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id"/>
</form>
</body>
</html>