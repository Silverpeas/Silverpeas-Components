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

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
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

<html>
<head>
<title><fmt:message key="GML.popupTitle"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<style type="text/css">
  <!--
  .eventCells {
    padding-right: 3px;
    padding-left: 3px;
    vertical-align: top;
    background-color: #FFFFFF
  }

  -->
</style>
<view:looknfeel/>
<script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
<script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
<script type="text/javascript" src="<c:url value='/util/javaScript/dateUtils.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wysiwyg/jsp/FCKeditor/fckeditor.js'/>"></script>
<script type="text/javascript">
function reallyAdd() {
  document.eventForm.WeekDayWeek2.disabled = false;
  document.eventForm.WeekDayWeek3.disabled = false;
  document.eventForm.WeekDayWeek4.disabled = false;
  document.eventForm.WeekDayWeek5.disabled = false;
  document.eventForm.WeekDayWeek6.disabled = false;
  document.eventForm.WeekDayWeek7.disabled = false;
  document.eventForm.WeekDayWeek1.disabled = false;
  document.eventForm.ChoiceMonth[0].disabled = false;
  document.eventForm.ChoiceMonth[1].disabled = false;
  document.eventForm.MonthNumWeek.disabled = false;
  document.eventForm.MonthDayWeek[0].disabled = false;
  document.eventForm.MonthDayWeek[1].disabled = false;
  document.eventForm.MonthDayWeek[2].disabled = false;
  document.eventForm.MonthDayWeek[3].disabled = false;
  document.eventForm.MonthDayWeek[4].disabled = false;
  document.eventForm.MonthDayWeek[5].disabled = false;
  document.eventForm.MonthDayWeek[6].disabled = false;

  document.eventForm.action = "ReallyAddEvent";
  document.eventForm.submit();
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
    document.eventForm.WeekDayWeek2.disabled = false;
    document.eventForm.WeekDayWeek3.disabled = false;
    document.eventForm.WeekDayWeek4.disabled = false;
    document.eventForm.WeekDayWeek5.disabled = false;
    document.eventForm.WeekDayWeek6.disabled = false;
    document.eventForm.WeekDayWeek7.disabled = false;
    document.eventForm.WeekDayWeek1.disabled = false;

    document.eventForm.ChoiceMonth[0].disabled = true;
    document.eventForm.ChoiceMonth[1].disabled = true;
    document.eventForm.MonthNumWeek.disabled = true;
    document.eventForm.MonthDayWeek[0].disabled = true;
    document.eventForm.MonthDayWeek[1].disabled = true;
    document.eventForm.MonthDayWeek[2].disabled = true;
    document.eventForm.MonthDayWeek[3].disabled = true;
    document.eventForm.MonthDayWeek[4].disabled = true;
    document.eventForm.MonthDayWeek[5].disabled = true;
    document.eventForm.MonthDayWeek[6].disabled = true;

  } else if (unity == "3") {
    document.eventForm.WeekDayWeek2.disabled = true;
    document.eventForm.WeekDayWeek3.disabled = true;
    document.eventForm.WeekDayWeek4.disabled = true;
    document.eventForm.WeekDayWeek5.disabled = true;
    document.eventForm.WeekDayWeek6.disabled = true;
    document.eventForm.WeekDayWeek7.disabled = true;
    document.eventForm.WeekDayWeek1.disabled = true;

    document.eventForm.ChoiceMonth[0].disabled = false;
    document.eventForm.ChoiceMonth[1].disabled = false;
    if (document.eventForm.ChoiceMonth[0].checked) {
      document.eventForm.MonthNumWeek.disabled = true;
      document.eventForm.MonthDayWeek[0].disabled = true;
      document.eventForm.MonthDayWeek[1].disabled = true;
      document.eventForm.MonthDayWeek[2].disabled = true;
      document.eventForm.MonthDayWeek[3].disabled = true;
      document.eventForm.MonthDayWeek[4].disabled = true;
      document.eventForm.MonthDayWeek[5].disabled = true;
      document.eventForm.MonthDayWeek[6].disabled = true;
    } else {
      document.eventForm.MonthNumWeek.disabled = false;
      document.eventForm.MonthDayWeek[0].disabled = false;
      document.eventForm.MonthDayWeek[1].disabled = false;
      document.eventForm.MonthDayWeek[2].disabled = false;
      document.eventForm.MonthDayWeek[3].disabled = false;
      document.eventForm.MonthDayWeek[4].disabled = false;
      document.eventForm.MonthDayWeek[5].disabled = false;
      document.eventForm.MonthDayWeek[6].disabled = false;
    }
  } else {
    document.eventForm.WeekDayWeek2.disabled = true;
    document.eventForm.WeekDayWeek3.disabled = true;
    document.eventForm.WeekDayWeek4.disabled = true;
    document.eventForm.WeekDayWeek5.disabled = true;
    document.eventForm.WeekDayWeek6.disabled = true;
    document.eventForm.WeekDayWeek7.disabled = true;
    document.eventForm.WeekDayWeek1.disabled = true;

    document.eventForm.ChoiceMonth[0].disabled = true;
    document.eventForm.ChoiceMonth[1].disabled = true;
    document.eventForm.MonthNumWeek.disabled = true;
    document.eventForm.MonthDayWeek[0].disabled = true;
    document.eventForm.MonthDayWeek[1].disabled = true;
    document.eventForm.MonthDayWeek[2].disabled = true;
    document.eventForm.MonthDayWeek[3].disabled = true;
    document.eventForm.MonthDayWeek[4].disabled = true;
    document.eventForm.MonthDayWeek[5].disabled = true;
    document.eventForm.MonthDayWeek[6].disabled = true;
  }
}

function changeChoiceMonth() {
  if (document.eventForm.ChoiceMonth[0].checked) {
    document.eventForm.MonthNumWeek.disabled = true;
    document.eventForm.MonthDayWeek[0].disabled = true;
    document.eventForm.MonthDayWeek[1].disabled = true;
    document.eventForm.MonthDayWeek[2].disabled = true;
    document.eventForm.MonthDayWeek[3].disabled = true;
    document.eventForm.MonthDayWeek[4].disabled = true;
    document.eventForm.MonthDayWeek[5].disabled = true;
    document.eventForm.MonthDayWeek[6].disabled = true;
  } else {
    document.eventForm.MonthNumWeek.disabled = false;
    document.eventForm.MonthDayWeek[0].disabled = false;
    document.eventForm.MonthDayWeek[1].disabled = false;
    document.eventForm.MonthDayWeek[2].disabled = false;
    document.eventForm.MonthDayWeek[3].disabled = false;
    document.eventForm.MonthDayWeek[4].disabled = false;
    document.eventForm.MonthDayWeek[5].disabled = false;
    document.eventForm.MonthDayWeek[6].disabled = false;
  }
}

function updateDates() {
  document.eventForm.EndDate.value = document.eventForm.StartDate.value;
  document.eventForm.PeriodicityStartDate.value = document.eventForm.StartDate.value;
}

</script>
</head>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5"
      onLoad="document.eventForm.Title.focus()">
<fmt:message key="evenement" var="eventTab"/>
<fmt:message key="accueil" var="currentPathLabel"/>
<view:browseBar componentId="${instanceId}" extraInformations="${currentPathLabel}"/>
<view:window>
  <view:tabs>
    <view:tab action="createEvent.jsp?Day=${day}" label="${eventTab}" selected="true"/>
  </view:tabs>
  <view:frame>
    <view:board>
      <form name="eventForm" action="editEvent.jsp" method="POST">
        <table CELLPADDING=5 WIDTH="100%">
          <tr>
            <td class="txtlibform"><fmt:message key='GML.name'/>&nbsp;:&nbsp;</td>
            <td><input type="text" name="Title" size="60"
                       maxlength="<c:out value='${maxTextLength}'/>"/>&nbsp;<img
                src="icons/cube-rouge.gif" width="5" height="5"/>
            </td>
          </tr>
          <tr>
            <td nowrap valign="top" class="txtlibform"><fmt:message key='GML.description'/>&nbsp;:&nbsp;</td>
            <td valign="top">
              <textarea name="Description" id="Description"></textarea>
            </td>
          </tr>
          <tr>
            <td nowrap class="txtlibform"><fmt:message key='GML.dateBegin'/>&nbsp;:&nbsp;</td>
            <td valign="baseline">
              <input type="text" class="dateToPick" name="StartDate" size="14"
                     maxlength="<c:out value='${maxDateLength}'/>"
                     value="<c:out value='${day[0]}'/>"
                     onchange="javascript:updateDates();"/>
              <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
              <span class="txtlibform">&nbsp;<fmt:message key='ToHour'/>&nbsp;</span>
              <input
                type="text" name="StartHour" size="5" maxlength="5"
                value="<c:out value='${day[1]}'/>"/> <span class="txtnote">(hh:mm)</span>&nbsp;<img
                src="icons/cube-rouge.gif" width="5" height="5"/>
            </td>
          </tr>
          <tr>
            <td nowrap class="txtlibform"><fmt:message key='GML.dateEnd'/>&nbsp;:&nbsp;</td>
            <td>
              <input type="text" class="dateToPick" name="EndDate" size="14"
                     maxlength="<c:out value='${maxDateLength}'/>"
                     value="<c:out value='${day[0]}'/>"/>
              <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
              <span class="txtlibform">&nbsp;<fmt:message key='ToHour'/>&nbsp;</span>
              <input
                type="text" name="EndHour" size="5" maxlength="5"/> <span
                class="txtnote">(hh:mm)</span>
            </td>
          </tr>
          <tr>
            <td nowrap class="txtlibform"><fmt:message key='lieuEvenement'/>&nbsp;:&nbsp;</td>
            <td><input type="text" name="Place" size="60"
                       maxlength="<c:out value='${maxTextLength}'/>"/></td>
          </tr>
          <tr>
            <td nowrap class="txtlibform"><fmt:message key='urlEvenement'/>&nbsp;:&nbsp;</td>
            <td><input type="text" name="EventUrl" size="60"
                       maxlength="<c:out value='${maxTextLength}'/>"/></td>
          </tr>
          <tr>
            <td nowrap class="txtlibform"><fmt:message key='GML.priority'/>&nbsp;:&nbsp;</td>
            <td><input type="checkbox" name="Priority" value="checkbox"/></td>
          </tr>
          <tr>
            <td nowrap class="txtlibform"><fmt:message key='periodicity'/>&nbsp;:&nbsp;</td>
            <td>
              <select name="Unity" size="1" onChange="changeUnity();">
                <option value="0"><fmt:message key='noPeriodicity'/></option>
                <option value="1"><fmt:message key='allDays'/></option>
                <option value="2"><fmt:message key='allWeeks'/></option>
                <option value="3"><fmt:message key='allMonths'/></option>
                <option value="4"><fmt:message key='allYears'/></option>
              </select>
            </td>
          </tr>
          <!--here-->
          <tr>
            <td nowrap align=right class="txtlibform"><fmt:message
                key='frequency'/>&nbsp;:&nbsp;</td>
            <td><input type="text" name="Frequency" size="5" maxlength="5" value="1"/></td>
          </tr>
          <tr>
            <td nowrap align=right><fmt:message key='choiceDaysWeek'/>&nbsp;:&nbsp;</td>
            <td>
              <input type="checkbox" name="WeekDayWeek2" value="2" disabled="disabled"/><fmt:message
                key='GML.jour2'/>
              <input type="checkbox" name="WeekDayWeek3" value="3" disabled="disabled"/><fmt:message
                key='GML.jour3'/>
              <input type="checkbox" name="WeekDayWeek4" value="4" disabled="disabled"/><fmt:message
                key='GML.jour4'/>
              <input type="checkbox" name="WeekDayWeek5" value="5" disabled="disabled"/><fmt:message
                key='GML.jour5'/>
              <input type="checkbox" name="WeekDayWeek6" value="6" disabled="disabled"/><fmt:message
                key='GML.jour6'/>
              <input type="checkbox" name="WeekDayWeek7" value="7" disabled="disabled"/><fmt:message
                key='GML.jour7'/>
              <input type="checkbox" name="WeekDayWeek1" value="1" disabled="disabled"/><fmt:message
                key='GML.jour1'/>
            </td>
          </tr>

          <tr>
            <td nowrap align=right>
              <input type="radio" name="ChoiceMonth" value="MonthDate" disabled="disabled" checked
                     onClick="changeChoiceMonth();"/><fmt:message key='choiceDateMonth'/>&nbsp;</td>
            <td>
              <input type="radio" name="ChoiceMonth" value="MonthDay" disabled="disabled"
                     onClick="changeChoiceMonth();"/><fmt:message key='choiceDayMonth'/>&nbsp;:&nbsp;
              <select name="MonthNumWeek" size="1" disabled="disabled">
                <option value="1"><fmt:message key='first'/></option>
                <option value="2"><fmt:message key='second'/></option>
                <option value="3"><fmt:message key='third'/></option>
                <option value="4"><fmt:message key='fourth'/></option>
                <option value="-1"><fmt:message key='fifth'/></option>
              </select>

              <input type="radio" name="MonthDayWeek" value="2" disabled="disabled"
                     checked="checked"/><fmt:message key='GML.jour2'/>
              <input type="radio" name="MonthDayWeek" value="3" disabled="disabled"/><fmt:message
                key='GML.jour3'/>
              <input type="radio" name="MonthDayWeek" value="4" disabled="disabled"/><fmt:message
                key='GML.jour4'/>
              <input type="radio" name="MonthDayWeek" value="5" disabled="disabled"/><fmt:message
                key='GML.jour5'/>
              <input type="radio" name="MonthDayWeek" value="6" disabled="disabled"/><fmt:message
                key='GML.jour6'/>
              <input type="radio" name="MonthDayWeek" value="7" disabled="disabled"/><fmt:message
                key='GML.jour7'/>
              <input type="radio" name="MonthDayWeek" value="1" disabled="disabled"/><fmt:message
                key='GML.jour1'/>
            </td>
          </tr>

          <tr>
            <td nowrap align=right class="txtlibform"><span><fmt:message
                key='beginDatePeriodicity'/>&nbsp;:&nbsp;</td>
            <td valign="baseline">
              <input type="text" name="PeriodicityStartDate" size="14"
                     maxlength="<c:out value='${maxDateLength}'/>" readonly="readonly"
                     value="<c:out value='${day[0]}'/>" />&nbsp;
            </td>
          </tr>
          <tr>
            <td nowrap align=right class="txtlibform"><span><fmt:message key='endDatePeriodicity'/>&nbsp;:&nbsp;
            </td>
            <td valign="baseline">
              <input type="text" class="dateToPick" name="PeriodicityUntilDate" size="14"
                     maxlength="<c:out value='${maxDateLength}'/>"/><span
                class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
            </td>
          </tr>
          <tr>
            <td colspan="2" valign="baseline" class="txtnote">(<img src="icons/cube-rouge.gif"
                                                                    width="5" height="5"/> =
              <fmt:message key='GML.requiredField'/>)
            </td>
            <td>
              <input type="hidden" name="Action"/>
              <input type="hidden" name="Id"/>
            </td>
          </tr>
        </table>
      </form>
    </view:board>
    <br/>
    <fmt:message key="GML.validate" var="validateLabel"/>
    <fmt:message key="GML.cancel" var="cancelLabel"/>
    <view:buttonPane>
      <view:button action="javascript:onClick=sendEventData();" disabled="false"
                   label="${validateLabel}"/>
      <view:button action="almanach.jsp" disabled="false" label="${cancelLabel}"/>
    </view:buttonPane>
    <br/>
  </view:frame>
</view:window>
<form name="almanachForm" action="almanach.jsp" method="POST">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id"/>
</form>
</BODY>
</HTML>
<script type="text/javascript">
  <fmt:message key='configFile' var='configFile'/>
  <c:if test="${configFile eq '???configFile???'}">
  <c:url value="/wysiwyg/jsp/javaScript/myconfig.js" var="configFile"/>
  </c:if>
  var oFCKeditor = new FCKeditor('Description');
  oFCKeditor.Width = "500";
  oFCKeditor.Height = "300";
  oFCKeditor.BasePath = "<c:url value='/wysiwyg/jsp/FCKeditor/'/>";
  oFCKeditor.DisplayErrors = true;
  oFCKeditor.Config["AutoDetectLanguage"] = false;
  oFCKeditor.Config["DefaultLanguage"] = "<c:out value='${language}'/>";
  oFCKeditor.Config["CustomConfigurationsPath"] = "<c:out value='${configFile}'/>"
  oFCKeditor.ToolbarSet = 'almanach';
  oFCKeditor.Config["ToolbarStartExpanded"] = true;
  oFCKeditor.ReplaceTextarea();
</script>
