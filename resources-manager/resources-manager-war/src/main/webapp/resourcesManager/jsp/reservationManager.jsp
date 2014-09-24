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
<%@ page import="org.silverpeas.resourcemanager.model.Reservation"%>
<%@ page import="org.silverpeas.util.StringUtil" %>

<fmt:setLocale value="${requestScope.resources.language}"/>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  Reservation reservation = (Reservation) request.getAttribute("reservation");
  String defaultDate = (String) request.getAttribute("defaultDate");
  String defaultTime = (String) request.getAttribute("defaultTime");


  String dateBegin = "";
  String dateEnd = "";
  String minuteHourDateBegin = "";
  String minuteHourDateEnd = "";
  if (StringUtil.isDefined(defaultDate)) {
    dateBegin = defaultDate;
    dateEnd = defaultDate;
    if (StringUtil.isDefined(defaultTime)) {
      minuteHourDateBegin = defaultTime;
    }
  }

  String event = "";
  String reason = "";
  String place = "";
  Long reservationId = null;

  if (reservation != null) {
    reservationId = reservation.getId();
    event = reservation.getEvent();
    reason = reservation.getReason();
    if (reason == null) {
      reason = "";
    }
    place = reservation.getPlace();
    if (place == null) {
      place = "";
    }
    dateEnd = resource.getOutputDate(reservation.getEndDate());
    dateBegin = resource.getOutputDate(reservation.getBeginDate());
    minuteHourDateBegin = DateUtil.getFormattedTime(reservation.getBeginDate());
    minuteHourDateEnd = DateUtil.getFormattedTime(reservation.getEndDate());
  }
  //creation des boutons Valider et Annuler
  Button validateButton =
      gef.getFormButton(resource.getString("GML.validate"), "javaScript:verification()", false);
  Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "Calendar?objectView="+request.getAttribute("objectView"), false);
%>
<html>
<head>
  <view:looknfeel/>
  <view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
  function validerNom() {
    if (document.getElementById("evenement").value == 0) {
      document.getElementById('validationNom').innerHTML = "Evenement obligatoire";
    } else {
      document.getElementById('validationNom').style.display = 'none';
    }
  }
  function isCorrectForm() {
    var errorNb = 0;
    var errorMsg = "";

    if (isWhitespace(document.getElementById("evenement").value)) {
      errorNb++;
      errorMsg +=
          "  - '<%=resource.getString("resourcesManager.evenement")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
    }

    var dateErrors = isPeriodValid({
      dateId : 'startDate',
      hourId : 'startHour',
      isMandatory : true,
      isMandatoryHour : true
    }, {
      dateId : 'endDate',
      hourId : 'endHour',
      isMandatory : true,
      isMandatoryHour : true,
      canBeEqualToAnother : false
    });
    $(dateErrors).each(function(index, error) {
      errorMsg += "  - " + error.message + "\n";
      errorNb++;
    });

    switch (errorNb) {
      case 0 :
        result = true;
        break;
      case 1 :
        errorMsg =
            "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error").toLowerCase()%> : \n" +
                errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
      default :
        errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb +
            " <%=resource.getString("GML.errors").toLowerCase()%> :\n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
    }
    return result;
  }

  function verification() {
    if (isCorrectForm()) {
      document.createForm.submit();
    }
  }
</script>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel,"Main");
browseBar.setPath(resource.getString("resourcesManager.reservationParametre"));

Board	board		 = gef.getBoard();
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());

ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);
%>
<form NAME="createForm" method="post" action="GetAvailableResources">
<input type="hidden" name="objectView" value="${requestScope.objectView}"/>
<TABLE ALIGN="CENTER" CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> : </TD>
		<TD width="100%"><input type="text" name="evenement" size="60" maxlength="60" id="evenement" onChange="validerNom()" value="<%=event%>" >&nbsp;<span id="validationNom" style="color:red"></span><IMG src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5" border="0"></TD>
	</tr>

	<tr>
		<td class="txtlibform" nowrap="nowrap"><label for="startDate"><%=resource.getString("GML.dateBegin")%> : </label></td>
		<td valign="baseline">
		<input type="text" class="dateToPick" name="startDate" size="14" id="startDate" maxlength="<%=DBUtil.getDateFieldLength()%>" value="<%=dateBegin%>"/>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span>
		<span class="txtlibform">&nbsp;</span><input type="text" name="startHour" id="startHour" size="5" maxlength="5" value="<%=minuteHourDateBegin%>"/>&nbsp;<span class="txtnote">(hh:mm)</span>&nbsp;<img src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5"/>
	</tr>

	<tr>
		<td class="txtlibform" nowrap="nowrap"><label for="endDate"><%=resource.getString("GML.dateEnd")%> : </label></td>
		<td valign="baseline">
		<input type="text" class="dateToPick" name="endDate" id="endDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" value="<%=dateEnd%>"/>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span>
		<span class="txtlibform">&nbsp;</span><input type="text" name="endHour" id="endHour" size="5" maxlength="5" value="<%=minuteHourDateEnd%>"/>&nbsp;<span class="txtnote">(hh:mm)</span>&nbsp;<img src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5"/>
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.raisonReservation"));%> : </TD>
		<TD><textarea name="raison" rows="6" cols="57" ><%=reason%></textarea></TD>
	</tr>


	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> : </TD>
		<TD><input type="text" name="lieu" size="60" maxlength="60" value="<%=place%>">&nbsp;</TD>
	</tr>


	<tr>
		<td colspan="2">( <img border="0" src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5" alt=""/> : <%=resource.getString("GML.requiredField")%> )</td>
	</tr>
	<!-- si le champs cache n est pas vide, cela signifie qu on est en train de modifier la reservation -->
	<%if (reservation != null)
	{
		%><input type="HIDDEN" name="reservationId" value="<%=reservationId%>"/>
  <%}%>
</TABLE>
</form>

<SCRIPT>document.createForm.evenement.focus();</SCRIPT>
<%
out.println(board.printAfter());
out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>