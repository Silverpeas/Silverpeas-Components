<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ include file="check.jsp"%>
<%
/*String beginDate = "";
String beginHour = "";
String endDate = "";
String endHour = "";

	 if (pubDetail.getBeginDate() != null) {
        beginDate = resources.getInputDate(pubDetail.getBeginDate());
      } else {
        beginDate = "";
      }
      if (pubDetail.getEndDate() != null) {
        if (resources.getDBDate(pubDetail.getEndDate()).equals("1000/01/01")) {
          endDate = "";
        } else {
          endDate = resources.getInputDate(pubDetail.getEndDate());
        }
      } else {
        if (action.equals("View")) {
          endDate = "&nbsp;";
        } else {
          endDate = "";
        }
      }
      if (beginDate == null || beginDate.length() == 0) {
        beginHour = "";
      } else {
        beginHour = pubDetail.getBeginHour();
      }
      if (endDate == null || endDate.length() == 0) {
        endHour = "";
      } else {
        endHour = pubDetail.getEndHour();
      }

      if (beginHour == null) {
        beginHour = "";
      }
      if (endHour == null) {
        endHour = "";
      }*/
      
%>
  <c:set var="pubId" value="${requestScope.PubId}"/>
  <c:set var="beginDate" value="${requestScope.BeginDate}"/>
  <c:set var="beginHour" value="${requestScope.BeginHour}"/>
  <c:set var="endDate" value="${requestScope.EndDate}"/>
  <c:set var="endHour" value="${requestScope.EndHour}"/>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle bundle="${requestScope.resources.multilangBundle}" var="DML"/>
  <view:setBundle basename="com.stratelia.webactiv.kmelia.multilang.kmeliaBundle" var="KML"/>
  <view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><fmt:message key="delegatednews.visibilityDates" bundle="${DML}"/></title>
    <view:looknfeel />
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
    <script type="text/javascript">
    function isCorrectForm(pubBeginDate, pubBeginHour, pubEndDate, pubEndHour) {
        var errorMsg = "";
        var errorNb = 0;
     
        var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
        var beginDate = document.updateDateForm.BeginDate.value;
        var endDate = document.updateDateForm.EndDate.value;
        
        var yearBegin = extractYear(beginDate, '${sessionScope[sessionController].language}');
        var monthBegin = extractMonth(beginDate, '${sessionScope[sessionController].language}');
        var dayBegin = extractDay(beginDate, '${sessionScope[sessionController].language}');
        var yearEnd = extractYear(endDate, '${sessionScope[sessionController].language}');
        var monthEnd = extractMonth(endDate, '${sessionScope[sessionController].language}');
        var dayEnd = extractDay(endDate, '${sessionScope[sessionController].language}');
        var beginHour = document.updateDateForm.BeginHour.value;
        var endHour = document.updateDateForm.EndHour.value;
        
        var pubYearBegin = extractYear(pubBeginDate, '${sessionScope[sessionController].language}');
        var pubMonthBegin = extractMonth(pubBeginDate, '${sessionScope[sessionController].language}');
        var pubDayBegin = extractDay(pubBeginDate, '${sessionScope[sessionController].language}');
        var pubYearEnd = extractYear(pubEndDate, '${sessionScope[sessionController].language}');
        var pubMonthEnd = extractMonth(pubEndDate, '${sessionScope[sessionController].language}');
        var pubDayEnd = extractDay(pubEndDate, '${sessionScope[sessionController].language}');

		var beginDateOK = true;

		if (!isWhitespace(beginDate)) {
			if (beginDate.replace(re, "OK") != "OK") {
				errorMsg+="  - '<fmt:message key="PubDateDebut" bundle="${KML}"/>' <fmt:message key="GML.MustContainsCorrectDate" bundle="${GML}"/>\n";
				errorNb++;
				beginDateOK = false;
			} else {
				if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
					errorMsg+="  - '<fmt:message key="PubDateDebut" bundle="${KML}"/>' <fmt:message key="GML.MustContainsCorrectDate" bundle="${GML}"/>\n";
					errorNb++;
					beginDateOK = false;
				}
			}
			
			if (!isWhitespace(pubBeginDate)) {
				if (beginDateOK && isD1AfterD2(yearBegin, monthBegin, dayBegin, pubYearBegin, pubMonthBegin, pubDayBegin) == false) {
					errorMsg+="  - '<fmt:message key="PubDateDebut" bundle="${KML}"/>' <fmt:message key="GML.MustContainsPostOrEqualDateTo" bundle="${GML}"/> "+pubBeginDate+"\n";
					errorNb++;
				}
			}
		}
		if (!checkHour(beginHour)) {
			errorMsg+="  - '<fmt:message key="ToHour" bundle="${KML}"/>' <fmt:message key="GML.MustContainsCorrectHour" bundle="${GML}"/>\n";
            errorNb++;
		}
		
		if (!isWhitespace(endDate)) {
			if (endDate.replace(re, "OK") != "OK") {
				errorMsg+="  - '<fmt:message key="PubDateFin" bundle="${KML}"/>' <fmt:message key="GML.MustContainsCorrectDate" bundle="${GML}"/>\n";
				errorNb++;
			} else {
				if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
					errorMsg+="  - '<fmt:message key="PubDateFin" bundle="${KML}"/>' <fmt:message key="GML.MustContainsCorrectDate" bundle="${GML}"/>\n";
					errorNb++;
				} else {
					if (!isWhitespace(pubEndDate)) {
						if (beginDateOK && isD1AfterD2(pubYearEnd, pubMonthEnd, pubDayEnd, yearEnd, monthEnd, dayEnd) == false) {
							errorMsg+="  - '<fmt:message key="PubDateFin" bundle="${KML}"/>' <fmt:message key="delegatednews.MustContainsPreviousOrEqualDateTo" bundle="${DML}"/> "+pubEndDate+"\n";
							errorNb++;
						}
					}
					
					if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
						if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
							errorMsg+="  - '<fmt:message key="PubDateFin" bundle="${KML}"/>' <fmt:message key="GML.MustContainsPostOrEqualDateTo" bundle="${GML}"/> "+beginDate+"\n";
							errorNb++;
						}
					} else {
						if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) {
							if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) {
								errorMsg+="  - '<fmt:message key="PubDateFin" bundle="${KML}"/>' <fmt:message key="GML.MustContainsPostDate" bundle="${GML}"/>\n";
								errorNb++;
							}
						}
					}
				}
			}
		}
		
		if (!checkHour(endHour)) {
			errorMsg+="  - '<fmt:message key="ToHour" bundle="${KML}"/>' <fmt:message key="GML.MustContainsCorrectHour" bundle="${GML}"/>\n";
            errorNb++;
		}
        
		switch(errorNb) {
			case 0 :
				result = true;
		break;
			case 1 :
				errorMsg = "<fmt:message key="GML.ThisFormContains" bundle="${GML}"/> 1 <fmt:message key="GML.error" bundle="${GML}"/> : \n" + errorMsg;
				window.alert(errorMsg);
				result = false;
		break    ;
			default :
				errorMsg = "<fmt:message key="GML.ThisFormContains" bundle="${GML}"/> " + errorNb + " <fmt:message key="GML.errors" bundle="${GML}"/> :\n" + errorMsg;
				window.alert(errorMsg);
				result = false;
				break;
		}
		   
		return result;
    }
    
    function updateDateDelegatedNew(pubBeginDate, pubBeginHour, pubEndDate, pubEndHour)
	{
		if (isCorrectForm(pubBeginDate, pubBeginHour, pubEndDate, pubEndHour)) {
			window.opener.document.listDelegatedNew.action = "UpdateDateDelegatedNew";
			window.opener.document.listDelegatedNew.PubId.value = document.updateDateForm.PubId.value;
			window.opener.document.listDelegatedNew.BeginDate.value = document.updateDateForm.BeginDate.value;
			window.opener.document.listDelegatedNew.BeginHour.value = document.updateDateForm.BeginHour.value;
			window.opener.document.listDelegatedNew.EndDate.value = document.updateDateForm.EndDate.value;
			window.opener.document.listDelegatedNew.EndHour.value = document.updateDateForm.EndHour.value;
			window.opener.document.listDelegatedNew.submit();
			window.close();
        }
    }
    </script>
  </head>  
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
      <view:frame>
        <view:board>
         
		<table class="intfdcolor4" border="0" cellpadding="0" cellspacing="0" width="98%">
		<form name="updateDateForm">
		<input type="hidden" name="PubId" value="<c:out value='${pubId}'/>">
		<tr align="center">
			<td valign="top" align="center">
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%">
				<TR id="beginArea">
					<TD class="txtlibform"><fmt:message key="PubDateDebut" bundle="${KML}"/></TD>
					<TD><input type="text" class="dateToPick" name="BeginDate" value="<c:out value='${beginDate}'/>" size="12" maxlength="10"/>
						<span class="txtsublibform">&nbsp;<fmt:message key="ToHour" bundle="${KML}"/>&nbsp;</span><input type="text" name="BeginHour" value="<c:out value='${beginHour}'/>" size="5" maxlength="5"> <i>(hh:mm)</i></TD>
				</TR>
				<TR id="endArea">
					<TD class="txtlibform"><fmt:message key="PubDateFin" bundle="${KML}"/></TD>
					<TD><input type="text" class="dateToPick" name="EndDate" value="<c:out value='${endDate}'/>" size="12" maxlength="10"/>
						<span class="txtsublibform">&nbsp;<fmt:message key="ToHour" bundle="${KML}"/>&nbsp;</span><input type="text" name="EndHour" value="<c:out value='${endHour}'/>" size="5" maxlength="5"> <i>(hh:mm)</i></TD>
				</TR>
			</table>
               </td>
            </tr>
        </form>
        </table>   
              
        <br/>
        <center>
        <fmt:message key="GML.validate" var="validate" bundle="${GML}"/>
        <fmt:message key="GML.cancel" var="cancel" bundle="${GML}"/>
          <view:buttonPane>
            <view:button action="javascript:onClick=updateDateDelegatedNew('${beginDate}', '${beginHour}', '${endDate}', '${endHour}')" label="${validate}" disabled="false" />
            <view:button action="javascript:onClick=window.close()" label="${cancel}" disabled="false" />
          </view:buttonPane>
        </center>
	
        </view:board>
      </view:frame>
  </body>
</html>
