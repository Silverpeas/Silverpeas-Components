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
<%@ include file="checkAlmanach.jsp" %>

<%
	String language = almanach.getLanguage();
  
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);

	EventDetail event = (EventDetail) request.getAttribute("Event");

	String day = "";
	if(event.getStartDate() != null) {
		day = resources.getInputDate(event.getStartDate());
	}
	
%>

<HTML>
<HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css">
<!--
.eventCells {  padding-right: 3px; padding-left: 3px; vertical-align: top; background-color: #FFFFFF}
-->
</style>
<%
out.println(graphicFactory.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<script language="JavaScript">
<!--
var yearDateDebut;
var yearDateFin;
var monthDateDebut;
var monthDateFin;
var dayDateDebut;
var dayDateFin;
var hourHeureDebut;
var hourHeureFin;
var minuteHeureDebut;
var minuteHeureFin;
var hour;
var minute;

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
     var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
     var beginDate = document.eventForm.StartDate.value;
     var endDate = document.eventForm.EndDate.value;
     var beginHour = stripInitialWhitespace(document.eventForm.StartHour.value);
     var endHour = stripInitialWhitespace(document.eventForm.EndHour.value);
     var yearBegin = extractYear(beginDate, '<%=language%>');
     var monthBegin = extractMonth(beginDate, '<%=language%>');
     var dayBegin = extractDay(beginDate, '<%=language%>');
     var yearEnd = extractYear(endDate, '<%=language%>');
     var monthEnd = extractMonth(endDate, '<%=language%>');
     var dayEnd = extractDay(endDate, '<%=language%>');
     var hour = "";
     var minute = "";
	 var unity = document.eventForm.Unity.value;
	 var frequency = stripInitialWhitespace(document.eventForm.Frequency.value);
	 var beginPeriodicity = document.eventForm.PeriodicityStartDate.value;
	 var yearBeginPeriodicity = extractYear(beginPeriodicity, '<%=language%>');
     var monthBeginPeriodicity = extractMonth(beginPeriodicity, '<%=language%>');
     var dayBeginPeriodicity = extractDay(beginPeriodicity, '<%=language%>');
	 var untilDate = document.eventForm.PeriodicityUntilDate.value;
	 var yearUntil = extractYear(untilDate, '<%=language%>');
     var monthUntil = extractMonth(untilDate, '<%=language%>');
     var dayUntil = extractDay(untilDate, '<%=language%>');

	 var beginDateOK = true;
	 var beginPeriodicityOK = true;

     if (isWhitespace(title)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     if (isWhitespace(beginDate)) {
     	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateBegin")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     	errorNb++;
     }
     else
     {
	       if (beginDate.replace(re, "OK") != "OK") {
	           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateBegin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
	           errorNb++;
			   beginDateOK = false;
	       } else {
	           if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
	             errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateBegin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
	             errorNb++;
				 beginDateOK = false;
	           }
	       }
     }
     
     if (!checkHour(beginHour))
     {
    	 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=almanach.getString("hourBegin")%>' <%=almanach.getString("MustContainsCorrectHour")%>\n";
	     errorNb++;
     }
     
     if (!isWhitespace(endDate)) {
           if (endDate.replace(re, "OK") != "OK") {
                 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                 errorNb++;
           } else {
                 if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                     errorNb++;
                 } else {
                     if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
                           if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
                                  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDate+"\n";
                                  errorNb++;
                           }
                     } else {
						   if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) {
							   if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) {
									  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
									  errorNb++;
							   }
						   }
					 }
                 }
           }
     }
     
     if (!checkHour(endHour))
     {
    	 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=almanach.getString("hourEnd")%>' <%=almanach.getString("MustContainsCorrectHour")%>\n";
         errorNb++;
     }
     
	 if (unity != "0") {
		if (isWhitespace(frequency)) {
     		errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("frequency")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     		errorNb++;
		} else {
			if( ! isInteger(frequency)) {
				errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("frequency")%>' <%=resources.getString("GML.MustContainsNumber")%>\n";
				errorNb++;
			}
		}

		if (! isWhitespace(beginPeriodicity)) {
			   if (beginPeriodicity.replace(re, "OK") != "OK") {
				   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("beginDatePeriodicity")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
				   errorNb++;
				   beginPeriodicityOK = false;
			   } else {
				   if (isCorrectDate(yearBeginPeriodicity, monthBeginPeriodicity, dayBeginPeriodicity)==false) {
					 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("beginDatePeriodicity")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
					 errorNb++;
					 beginPeriodicityOK = false;
				   }
			   }
		 }

		 if (! isWhitespace(untilDate)) {
			   if (untilDate.replace(re, "OK") != "OK") {
				   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
				   errorNb++;
			   } else {
				   if (isCorrectDate(yearUntil, monthUntil, dayUntil)==false) {
					 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
					 errorNb++;
				   }
				   else {
                     if ((isWhitespace(beginPeriodicity) == false) && (isWhitespace(untilDate) == false)) {
                           if (beginPeriodicityOK && isD1AfterD2(yearUntil, monthUntil, dayUntil, yearBeginPeriodicity, monthBeginPeriodicity, dayBeginPeriodicity) == false) {
                                  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginPeriodicity+"\n";
                                  errorNb++;
                           }
                     } else {
						   if ((isWhitespace(beginPeriodicity) == true) && (isWhitespace(untilDate) == false)) {
							   if (isFutureDate(yearUntil, monthUntil, dayUntil) == false) {
									  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
									  errorNb++;
							   }
						   }
					 }
                 }
			   }
		 }
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

function sendEventData() {
    if (isCorrectForm()) {
         reallyAdd();
    }
}

function changeUnity () {
	var unity = document.eventForm.Unity.value;
	if(unity == "2") {
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
		if(document.eventForm.ChoiceMonth[0].checked) {
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
	if(document.eventForm.ChoiceMonth[0].checked) {
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

//-->
</script>
</HEAD>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5" onLoad="document.eventForm.Title.focus()">
  <% 
    Window 		window 		= graphicFactory.getWindow();
    Frame 		frame		= graphicFactory.getFrame();
    Board 		board 		= graphicFactory.getBoard();
    TabbedPane 	tabbedPane 	= graphicFactory.getTabbedPane();
    
    OperationPane operationPane = window.getOperationPane();
        
	String bar = almanach.getString("accueil");
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "almanach.jsp");
	browseBar.setExtraInformation(bar);

    out.println(window.printBefore());

	tabbedPane.addTab(almanach.getString("evenement"), "createEvent.jsp?Day="+day, true);

	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
<FORM name="eventForm" action="editEvent.jsp" method="POST">
	<tr> 
        <td class="txtlibform"><%=resources.getString("GML.name")%>&nbsp;:&nbsp;</td>
        <td><input type="text" name="Title" size="60" maxlength="<%=DBUtil.TextFieldLength%>">&nbsp;<img src="icons/cube-rouge.gif" width="5" height="5">
        </td>
	    </tr>
      <tr> 
        <td nowrap valign="top" class="txtlibform"><%=resources.getString("GML.description")%>&nbsp;:&nbsp;</td>
        <td valign="top"> 
					<textarea name="Description" id="Description"></textarea>
         </td>
      </tr>
        <tr> 
          <td  nowrap class="txtlibform"><%=resources.getString("GML.dateBegin")%>&nbsp;:&nbsp;</td>
          <td valign="baseline"> 
            <input type="text" class="dateToPick" name="StartDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>" <% 
						if (event.getStartDate() != null) out.print("value=\""+resources.getInputDate(event.getStartDate())+"\"");
						%>/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span> 
             <span class="txtlibform">&nbsp;<%=almanach.getString("ToHour")%>&nbsp;</span><input type="text" name="StartHour" size="5" maxlength="5"> <span class="txtnote">(hh:mm)</span>&nbsp;<img src="icons/cube-rouge.gif" width="5" height="5">
          </td>
        </tr>
        <tr> 
          <td  nowrap class="txtlibform"><%=resources.getString("GML.dateEnd")%>&nbsp;:&nbsp;</td>
          <td> 
            <input type="text" class="dateToPick" name="EndDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>"/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
             <span class="txtlibform">&nbsp;<%=almanach.getString("ToHour")%>&nbsp;</span><input type="text" name="EndHour"	size="5" maxlength="5"> <span class="txtnote">(hh:mm)</span>
          </td>
        </tr>
        <tr>
        	<td nowrap class="txtlibform"><%=almanach.getString("lieuEvenement")%>&nbsp;:&nbsp;</td>
        	<td><input type="text" name="Place" size="60" maxlength="<%=DBUtil.TextFieldLength%>"></td>
        </tr>
        <tr> 
        	<td nowrap class="txtlibform"><%=almanach.getString("urlEvenement")%>&nbsp;:&nbsp;</td>
        	<td><input type="text" name="EventUrl" size="60" maxlength="<%=DBUtil.TextFieldLength%>"></td>
        </tr>                    
        <tr>
          <td nowrap class="txtlibform"><%=resources.getString("GML.priority")%>&nbsp;:&nbsp;</td>
          <td><input type="checkbox" name="Priority" value="checkbox"></td>
        </tr>
		<tr>
          <td nowrap class="txtlibform"><%=resources.getString("periodicity")%>&nbsp;:&nbsp;</td>
          <td>
		  <select name="Unity" size="1" onChange="changeUnity();">
			<option value="0"><%=resources.getString("noPeriodicity")%></option>
			<option value="1"><%=resources.getString("allDays")%></option>
			<option value="2"><%=resources.getString("allWeeks")%></option>
			<option value="3"><%=resources.getString("allMonths")%></option>
			<option value="4"><%=resources.getString("allYears")%></option>
		  </select></td>
        </tr>
		<tr>
          <td nowrap align=right class="txtlibform"><%=resources.getString("frequency")%>&nbsp;:&nbsp;</td>
		  <td><input type="text" name="Frequency" size="5" maxlength="5" value="1"></td> 
        </tr>
		<tr>
          <td nowrap align=right><%=resources.getString("choiceDaysWeek")%>&nbsp;:&nbsp;</td>
		  <td>
			<input type="checkbox" name="WeekDayWeek2" value="2" disabled><%=resources.getString("GML.jour2")%>
			<input type="checkbox" name="WeekDayWeek3" value="3" disabled><%=resources.getString("GML.jour3")%>
			<input type="checkbox" name="WeekDayWeek4" value="4" disabled><%=resources.getString("GML.jour4")%>
			<input type="checkbox" name="WeekDayWeek5" value="5" disabled><%=resources.getString("GML.jour5")%>
			<input type="checkbox" name="WeekDayWeek6" value="6" disabled><%=resources.getString("GML.jour6")%>
			<input type="checkbox" name="WeekDayWeek7" value="7" disabled><%=resources.getString("GML.jour7")%>
			<input type="checkbox" name="WeekDayWeek1" value="1" disabled><%=resources.getString("GML.jour1")%>
		  </td>
        </tr>
		
		<tr>
          <td nowrap align=right>
			<input type="radio" name="ChoiceMonth" value="MonthDate" disabled checked onClick="changeChoiceMonth();"><%=resources.getString("choiceDateMonth")%>&nbsp;</td>
		  <td>	
			<input type="radio" name="ChoiceMonth" value="MonthDay" disabled onClick="changeChoiceMonth();"><%=resources.getString("choiceDayMonth")%>&nbsp;:&nbsp;
			<select name="MonthNumWeek" size="1"  disabled>
			<option value="1"><%=resources.getString("first")%></option>
			<option value="2"><%=resources.getString("second")%></option>
			<option value="3"><%=resources.getString("third")%></option>
			<option value="4"><%=resources.getString("fourth")%></option>
			<option value="-1"><%=resources.getString("fifth")%></option>
			</select>
			
			<input type="radio" name="MonthDayWeek" value="2" disabled checked><%=resources.getString("GML.jour2")%>
			<input type="radio" name="MonthDayWeek" value="3" disabled><%=resources.getString("GML.jour3")%>
			<input type="radio" name="MonthDayWeek" value="4" disabled><%=resources.getString("GML.jour4")%>
			<input type="radio" name="MonthDayWeek" value="5" disabled><%=resources.getString("GML.jour5")%>
			<input type="radio" name="MonthDayWeek" value="6" disabled><%=resources.getString("GML.jour6")%>
			<input type="radio" name="MonthDayWeek" value="7" disabled><%=resources.getString("GML.jour7")%>
			<input type="radio" name="MonthDayWeek" value="1" disabled><%=resources.getString("GML.jour1")%>
		  </td> 
        </tr>
	
		<tr> 
          <td  nowrap align=right class="txtlibform"><span><%=resources.getString("beginDatePeriodicity")%>&nbsp;:&nbsp;</td>
          <td valign="baseline"> 
            <input type="text" name="PeriodicityStartDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>" readonly 
			<% 
			if (event.getStartDate() != null) out.print("value=\""+resources.getInputDate(event.getStartDate())+"\"");
			%>>&nbsp;
          </td>
        </tr>
		<tr> 
          <td  nowrap align=right class="txtlibform"><span><%=resources.getString("endDatePeriodicity")%>&nbsp;:&nbsp;</td>
          <td valign="baseline"> 
            <input type="text" class="dateToPick" name="PeriodicityUntilDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>"/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
          </td>
        </tr>
        <tr> 
          <td colspan="2" valign="baseline" class="txtnote">(<img src="icons/cube-rouge.gif" width="5" height="5"> =  <%=resources.getString("GML.requiredField")%>)</td>
        </tr>
   	<input type="hidden" name="Action">
   	<input type="hidden" name="Id">
   </FORM>
   </table>
   <%
		out.println(board.printAfter());
   %>
   <center><br>
   <%
 		ButtonPane buttonPane = graphicFactory.getButtonPane();
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendEventData()", false));
		Button button = null;
		button = graphicFactory.getFormButton(resources.getString("GML.cancel"), "almanach.jsp", false);
		buttonPane.addButton(button);
		out.println(buttonPane.print());
   %>
   <br></center>
<%
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
<form name="almanachForm" action="almanach.jsp" method="POST">
  <input type="hidden" name="Action">
  <input type="hidden" name="Id">
</form>
</BODY>
</HTML>
<%                    
out.println("<script language=\"JavaScript\">");
out.println("var oFCKeditor = new FCKeditor('Description');");
out.println("oFCKeditor.Width = \"500\";");
out.println("oFCKeditor.Height = \"300\";");
out.println("oFCKeditor.BasePath = \""+URLManager.getApplicationURL()+"/wysiwyg/jsp/FCKeditor/\" ;");
out.println("oFCKeditor.DisplayErrors = true;");
out.println("oFCKeditor.Config[\"AutoDetectLanguage\"] = false");
out.println("oFCKeditor.Config[\"DefaultLanguage\"] = \""+language+"\";");
String configFile = SilverpeasSettings.readString(settings, "configFile", URLManager.getApplicationURL() +"/wysiwyg/jsp/javaScript/myconfig.js");
out.println("oFCKeditor.Config[\"CustomConfigurationsPath\"] = \""+configFile+"\";");
out.println("oFCKeditor.ToolbarSet = 'almanach';");
out.println("oFCKeditor.Config[\"ToolbarStartExpanded\"] = true;");
out.println("oFCKeditor.ReplaceTextarea();");
out.println("</script>");
%>