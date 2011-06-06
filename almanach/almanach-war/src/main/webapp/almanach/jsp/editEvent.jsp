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
<%@ include file="checkAlmanach.jsp" %>

<%
	String language = almanach.getLanguage();

	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);

	EventDetail event = (EventDetail) request.getAttribute("CompleteEvent");
	Date dateDebutIteration = (Date) request.getAttribute("DateDebutIteration");
	Date dateFinIteration = (Date) request.getAttribute("DateFinIteration");

	String dateDebutIterationString = DateUtil.date2SQLDate(dateDebutIteration);

	Periodicity periodicity = event.getPeriodicity();

	String description = "";
	String id = event.getPK().getId();
	String title = event.getTitle();
	if (title.length() > 30) {
		title = title.substring(0,30) + "....";
	}
	if (StringUtil.isDefined(event.getWysiwyg())) {
		description = event.getWysiwyg();
	}
	else if (StringUtil.isDefined(event.getDescription())) {
		description = EncodeHelper.javaStringToHtmlParagraphe(event.getDescription());
	}

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
<script type="text/javascript">
<!--
var oEditor;

function FCKeditor_OnComplete( editorInstance )
{
	oEditor = FCKeditorAPI.GetInstance(editorInstance.Name);
}

function reallyUpdate() {
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
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     if (isWhitespace(beginDate)) {
     	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateBegin")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     	errorNb++;
     }
     else
     {
     	if (!isDateOK(beginDate, '<%=language%>')) {
        	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateBegin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
            errorNb++;
			beginDateOK = false;
        }
     }

     if (!checkHour(beginTime) || (isWhitespace(beginTime) && !isWhitespace(endTime)))
     {
    	 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=almanach.getString("hourBegin")%>' <%=almanach.getString("MustContainsCorrectHour")%>\n";
	     errorNb++;
     }

	if (!isWhitespace(endDate)) {
    	if (!isDateOK(endDate, '<%=language%>')) {
			errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
            errorNb++;
		} else {
			if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
            	if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, '<%=language%>')) {
                	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDate+"\n";
                    errorNb++;
                }
            } else {
			   	if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
			   		if (!isFuture(endDate, '<%=language%>')) {
						errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.dateEnd")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
						errorNb++;
				   	}
			   	}
		 	}
		}
	}
    
     if (!checkHour(endTime))
     {
    	 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=almanach.getString("hourEnd")%>' <%=almanach.getString("MustContainsCorrectHour")%>\n";
         errorNb++;
     }
     
     if (beginDate == endDate && !isWhitespace(endTime)) {
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
     		errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("frequency")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     		errorNb++;
		} else {
			if( ! isInteger(frequency)) {
				errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("frequency")%>' <%=resources.getString("GML.MustContainsNumber")%>\n";
				errorNb++;
			}
		}

		if (!isWhitespace(beginPeriodicity)) {
			if (!isDateOK(beginPeriodicity, '<%=language%>')) {
				errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("beginDatePeriodicity")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
				errorNb++;
				beginPeriodicityOK = false;
			}
		 }

		 if (!isWhitespace(untilDate)) {
			if (!isDateOK(untilDate, '<%=language%>')) {
				errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
				errorNb++;
			} else {
				if (!isWhitespace(beginPeriodicity) && !isWhitespace(untilDate)) {
                	if (beginPeriodicityOK && !isDate1AfterDate2(untilDate, beginPeriodicity, '<%=language%>')) {
                    	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginPeriodicity+"\n";
                        errorNb++;
                     }
				} else {
					if (isWhitespace(beginPeriodicity) && !isWhitespace(untilDate)) {
						if (!isFuture(untilDate, '<%=language%>')) {
							errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("endDatePeriodicity")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
							errorNb++;
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
				var desc = oEditor.GetXHTML(true);
	    		if (oldDesc != desc)
	    		{
	    			isChanged = 1;
	    		}

				var oldStartDate = '<%=resources.getOutputDate(dateDebutIteration)%>';
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

				var oldEndDate = '<%=resources.getOutputDate(dateFinIteration)%>';
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
});
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

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "almanach.jsp");
	browseBar.setExtraInformation(title);

    operationPane.addOperation(m_context + "/util/icons/almanach_to_del.gif", almanach.getString("supprimerEvenement"), "javascript:onClick=eventDeleteConfirm('" + EncodeHelper.javaStringToJsString(title) + "','" + id +"')");
    out.println(window.printBefore());

	tabbedPane.addTab(almanach.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
	tabbedPane.addTab(almanach.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+dateDebutIterationString, true);
	tabbedPane.addTab(resources.getString("GML.attachments"), "editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
	if (almanach.isPdcUsed()) {
		tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
	}

	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<table CELLPADDING="5" WIDTH="100%">
<FORM name="eventForm" action="ReallyUpdateEvent" method="POST">

	  <tr>
        <td nowrap valign="baseline" class="txtlibform"><%=resources.getString("GML.name")%> :</td>
        <td align=left><input type="text" name="Title" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>"
					<%if (event.getTitle()!=null) out.print("value=\""+ EncodeHelper.javaStringToHtmlString(event.getTitle()) + "\"");%>>
            &nbsp;<img src="icons/cube-rouge.gif" width="5" height="5">
        </td>
	    </tr>
      <tr>
        <td nowrap valign="top" class="txtlibform"><%=resources.getString("GML.description")%> :</td>
        <td valign="top">
					<textarea name="Description" id="Description"><%=description%></textarea>
         </td>
      </tr>
	  <tr>
          <td nowrap class="txtlibform"><%=resources.getString("GML.dateBegin")%> :</td>
          <td valign="baseline">
            <input type="text" class="dateToPick" name="StartDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>"
				value="<%=resources.getOutputDate(dateDebutIteration)%>"/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
             <span class="txtlibform">&nbsp;<%=almanach.getString("ToHour")%>&nbsp;</span><input type="text" name="StartHour"
									<%
             						if (event.getStartHour() != null) out.println("value=\""+
             							event.getStartHour()
             							+"\"");
             						%> size="5" maxlength="5"> <span class="txtnote">(hh:mm)</span>&nbsp;<img src="icons/cube-rouge.gif" width="5" height="5">
          </td>
        </tr>
	    <tr>
          <td nowrap class="txtlibform"><%=resources.getString("GML.dateEnd")%> :</td>
          <td align=left>
            <input type="text" class="dateToPick" name="EndDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>"
				value="<%=resources.getOutputDate(dateFinIteration)%>"/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
             <span class="txtlibform">&nbsp;<%=almanach.getString("ToHour")%>&nbsp;</span><input type="text" name="EndHour" <%
   						if (event.getEndHour() != null) out.println("value=\""+
   							event.getEndHour()
   							+"\"");
   						%> size="5" maxlength="5"> <span class="txtnote">(hh:mm)</span>
          </td>
        </tr>
		<tr>
        	<td nowrap class="txtlibform"><%=almanach.getString("lieuEvenement")%> :</td>
        	<td align=left><input type="text" name="Place" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>"
						<%if (event.getPlace()!=null) out.print("value=\""+ EncodeHelper.javaStringToHtmlString(event.getPlace()) + "\"");%>>
					</td>
        </tr>
        <tr>
        	<td nowrap class="txtlibform"><%=almanach.getString("urlEvenement")%> :</td>
        	<td align=left><input type="text" name="EventUrl" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>"
			<%if (event.getEventUrl()!=null) out.print("value=\""+ EncodeHelper.javaStringToHtmlString(event.getEventUrl()) + "\"");%>></td>
        </tr>
        <tr>
          <td nowrap class="txtlibform"><%=resources.getString("GML.priority")%> :</td>
          <td align=left><input type="checkbox" name="Priority" value="checkbox" <%if (event.getPriority() != 0) out.print("CHECKED");%>></td>
        </tr>
		<tr>
          <td nowrap class="txtlibform"><%=resources.getString("periodicity")%> :</td>
          <td align=left>
		  <select name="Unity" size="1" onChange="changeUnity();">
			<option value="0" <%if (periodicity == null) out.print("selected");%>><%=resources.getString("noPeriodicity")%></option>
			<option value="1" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_DAY) out.print("selected");%>><%=resources.getString("allDays")%></option>
			<option value="2" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_WEEK) out.print("selected");%>><%=resources.getString("allWeeks")%></option>
			<option value="3" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_MONTH) out.print("selected");%>><%=resources.getString("allMonths")%></option>
			<option value="4" <%if (periodicity != null && periodicity.getUnity() == Periodicity.UNIT_YEAR) out.print("selected");%>><%=resources.getString("allYears")%></option>
		  </select></td>
        </tr>
        <tr>
	    <tr>
          <td nowrap align=right class="txtlibform"><%=resources.getString("frequency")%> :</td>
		  <td align=left><input type="text" name="Frequency" size="5" maxlength="5" value="<% if(periodicity == null) out.print("1"); else out.print(periodicity.getFrequency());%>"></td>
        </tr>
		<tr>
          <td nowrap align=right><%=resources.getString("choiceDaysWeek")%> :</td>
		  <td align=left>
			<%
			if(periodicity == null ||
				periodicity.getUnity() == Periodicity.UNIT_DAY ||
				periodicity.getUnity() == Periodicity.UNIT_MONTH ||
				periodicity.getUnity() == Periodicity.UNIT_YEAR) {
			%>
				<input type="checkbox" name="WeekDayWeek2" value="2" disabled><%=resources.getString("GML.jour2")%>
				<input type="checkbox" name="WeekDayWeek3" value="3" disabled><%=resources.getString("GML.jour3")%>
				<input type="checkbox" name="WeekDayWeek4" value="4" disabled><%=resources.getString("GML.jour4")%>
				<input type="checkbox" name="WeekDayWeek5" value="5" disabled><%=resources.getString("GML.jour5")%>
				<input type="checkbox" name="WeekDayWeek6" value="6" disabled><%=resources.getString("GML.jour6")%>
				<input type="checkbox" name="WeekDayWeek7" value="7" disabled><%=resources.getString("GML.jour7")%>
				<input type="checkbox" name="WeekDayWeek1" value="1" disabled><%=resources.getString("GML.jour1")%>
			<%
			} else if(periodicity != null && periodicity.getUnity() == Periodicity.UNIT_WEEK) {
			%>
				<input type="checkbox" name="WeekDayWeek2" value="2" <%if(periodicity.getDaysWeekBinary().charAt(0) == '1')  out.print("checked");%>><%=resources.getString("GML.jour2")%>
				<input type="checkbox" name="WeekDayWeek3" value="3" <%if(periodicity.getDaysWeekBinary().charAt(1) == '1')  out.print("checked");%>><%=resources.getString("GML.jour3")%>
				<input type="checkbox" name="WeekDayWeek4" value="4" <%if(periodicity.getDaysWeekBinary().charAt(2) == '1')  out.print("checked");%>><%=resources.getString("GML.jour4")%>
				<input type="checkbox" name="WeekDayWeek5" value="5" <%if(periodicity.getDaysWeekBinary().charAt(3) == '1')  out.print("checked");%>><%=resources.getString("GML.jour5")%>
				<input type="checkbox" name="WeekDayWeek6" value="6" <%if(periodicity.getDaysWeekBinary().charAt(4) == '1')  out.print("checked");%>><%=resources.getString("GML.jour6")%>
				<input type="checkbox" name="WeekDayWeek7" value="7" <%if(periodicity.getDaysWeekBinary().charAt(5) == '1')  out.print("checked");%>><%=resources.getString("GML.jour7")%>
				<input type="checkbox" name="WeekDayWeek1" value="1" <%if(periodicity.getDaysWeekBinary().charAt(6) == '1')  out.print("checked");%>><%=resources.getString("GML.jour1")%>
			<%
			}
			%>
		  </td>
        </tr>

		<tr>
			<%
			if(periodicity == null ||
				periodicity.getUnity() == Periodicity.UNIT_DAY ||
				periodicity.getUnity() == Periodicity.UNIT_WEEK ||
				periodicity.getUnity() == Periodicity.UNIT_YEAR) {
			%>
			  <td nowrap align=right>
				<input type="radio" name="ChoiceMonth" value="MonthDate" disabled checked onClick="changeChoiceMonth();"><%=resources.getString("choiceDateMonth")%>&nbsp;</td>
			  <td align=left>
				<input type="radio" name="ChoiceMonth" value="MonthDay" disabled onClick="changeChoiceMonth();"><%=resources.getString("choiceDayMonth")%> :
				<select name="MonthNumWeek" size="1" disabled>
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
			<%
			}  else if(periodicity != null && periodicity.getUnity() == Periodicity.UNIT_MONTH) {
			%>
			  <td nowrap align=right>
				<input type="radio" name="ChoiceMonth" value="MonthDate" <%if(periodicity.getNumWeek() == 0) out.print("checked");%> onClick="changeChoiceMonth();"><%=resources.getString("choiceDateMonth")%>&nbsp;</td>
			  <td align=left>
				<input type="radio" name="ChoiceMonth" value="MonthDay" <%if(periodicity.getNumWeek() != 0) out.print("checked");%> onClick="changeChoiceMonth();"><%=resources.getString("choiceDayMonth")%> :
				<%if(periodicity.getNumWeek() == 0) {
				%>
					<select name="MonthNumWeek" size="1"  disabled>
					<option value="1" disabled><%=resources.getString("first")%></option>
					<option value="2" disabled><%=resources.getString("second")%></option>
					<option value="3" disabled><%=resources.getString("third")%></option>
					<option value="4" disabled><%=resources.getString("fourth")%></option>
					<option value="-1" disabled><%=resources.getString("fifth")%></option>
					</select>

					<input type="radio" name="MonthDayWeek" value="2" disabled checked><%=resources.getString("GML.jour2")%>
					<input type="radio" name="MonthDayWeek" value="3" disabled><%=resources.getString("GML.jour3")%>
					<input type="radio" name="MonthDayWeek" value="4" disabled><%=resources.getString("GML.jour4")%>
					<input type="radio" name="MonthDayWeek" value="5" disabled><%=resources.getString("GML.jour5")%>
					<input type="radio" name="MonthDayWeek" value="6" disabled><%=resources.getString("GML.jour6")%>
					<input type="radio" name="MonthDayWeek" value="7" disabled><%=resources.getString("GML.jour7")%>
					<input type="radio" name="MonthDayWeek" value="1" disabled><%=resources.getString("GML.jour1")%>
				<%
				} else {
				%>
					<select name="MonthNumWeek" size="1">
					<option value="1" <%if (periodicity.getNumWeek() == 1) out.print("selected");%>><%=resources.getString("first")%></option>
					<option value="2" <%if (periodicity.getNumWeek() == 2) out.print("selected");%>><%=resources.getString("second")%></option>
					<option value="3" <%if (periodicity.getNumWeek() == 3) out.print("selected");%>><%=resources.getString("third")%></option>
					<option value="4" <%if (periodicity.getNumWeek() == 4) out.print("selected");%>><%=resources.getString("fourth")%></option>
					<option value="-1" <%if (periodicity.getNumWeek() == -1) out.print("selected");%>><%=resources.getString("fifth")%></option>
					</select>

					<input type="radio" name="MonthDayWeek" value="2" <%if(periodicity.getDay() == 2) out.print("checked");%>><%=resources.getString("GML.jour2")%>
					<input type="radio" name="MonthDayWeek" value="3" <%if(periodicity.getDay() == 3) out.print("checked");%>><%=resources.getString("GML.jour3")%>
					<input type="radio" name="MonthDayWeek" value="4" <%if(periodicity.getDay() == 4) out.print("checked");%>><%=resources.getString("GML.jour4")%>
					<input type="radio" name="MonthDayWeek" value="5" <%if(periodicity.getDay() == 5) out.print("checked");%>><%=resources.getString("GML.jour5")%>
					<input type="radio" name="MonthDayWeek" value="6" <%if(periodicity.getDay() == 6) out.print("checked");%>><%=resources.getString("GML.jour6")%>
					<input type="radio" name="MonthDayWeek" value="7" <%if(periodicity.getDay() == 7) out.print("checked");%>><%=resources.getString("GML.jour7")%>
					<input type="radio" name="MonthDayWeek" value="1" <%if(periodicity.getDay() == 1) out.print("checked");%>><%=resources.getString("GML.jour1")%>
				<%
				}
				%>
			  </td>
			<%
			}
			%>
        </tr>
		<tr>
          <td  nowrap align=right class="txtlibform"><span><%=resources.getString("beginDatePeriodicity")%> :</span></td>
          <td valign="baseline">
            <input type="text" class="dateToPick" name="PeriodicityStartDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>"
			<%
			if (event.getStartDate() != null) out.print("value=\""+resources.getInputDate(event.getStartDate())+"\"");
			%>/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
          </td>
        </tr>
		<tr>
          <td  nowrap align=right class="txtlibform"><span><%=resources.getString("endDatePeriodicity")%> :</span></td>
          <td valign="baseline">
            <input type="text" class="dateToPick" name="PeriodicityUntilDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>"
			<%
			if (periodicity != null && periodicity.getUntilDatePeriod() != null) out.print("value=\""+resources.getInputDate(periodicity.getUntilDatePeriod())+"\"");
			%>/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
          </td>
        </tr>
        <tr>
          <td colspan="2" valign="baseline" class="txtnote">(<img src="icons/cube-rouge.gif" width="5" height="5"> =  <%=resources.getString("GML.requiredField")%>)</td>
        </tr>

	<input type="hidden" name="Action">
   	<input type="hidden" name="Id" <%out.print("value=\""+event.getPK().getId()+"\"");%>>
	<input type="hidden" name="DateDebutIteration" <%out.print("value=\""+dateDebutIterationString+"\"");%>>
	<input type="hidden" name="DateFinIteration" <%out.print("value=\""+DateUtil.date2SQLDate(dateFinIteration)+"\"");%>>
   </FORM>
   </table>
   <%
		out.println(board.printAfter());
   %>
   <center><br>
   <%
 		ButtonPane buttonPane = graphicFactory.getButtonPane();
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendEventData()", false));
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.back"), "almanach.jsp", false));
		out.println(buttonPane.print());
   %>
   <br></center>
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