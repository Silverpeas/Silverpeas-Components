<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ include file="checkDataWarning.jsp"%>
<%
  String listGroups = (String) request.getAttribute("listGroups");
  String listUsers = (String) request.getAttribute("listUsers");
  DataWarningScheduler scheduler = (DataWarningScheduler) request.getAttribute("scheduler");
  int i;
%>
<HTML>
<HEAD>
<%
  out.println(gef.getLookStyleSheet());
%>
</HEAD>
<script type="text/javascript"	src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript"	src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
	function openNotificationSender() {
		SP_openWindow('Notification', 'NotificationSender', '700', '760',
				'menubar=no,scrollbars=no,statusbar=no');
	}
	function sendForm(theAction) {
		document.form.action = theAction;
		document.form.submit();
	}
</script>
<BODY bgcolor="#FFFFFF">
<%//operation Pane
      operationPane.addOperation(resource.getIcon("DataWarning.newNotification"), resource.getString("abtMgmt"), "javascript:onClick=openNotificationSender()");
      if (scheduler.getSchedulerState() == DataWarningScheduler.SCHEDULER_STATE_ON)
        operationPane.addOperation(resource.getIcon("DataWarning.stopScheduler"), resource
            .getString("boutonArreter"), "javascript:onClick=sendForm('StopScheduler')");
      else
        operationPane.addOperation(resource.getIcon("DataWarning.startScheduler"), resource
            .getString("boutonDemarrer"), "javascript:onClick=sendForm('StartScheduler')");

      //Les onglets
      tabbedPane = gef.getTabbedPane();
      tabbedPane.addTab(resource.getString("tabbedPaneConsultation"), "dataWarning", false);

      if (flag.equals("publisher") || flag.equals("admin"))
        tabbedPane.addTab(resource.getString("tabbedPaneRequete"), "requestParameters", false);

      if (flag.equals("admin"))
        tabbedPane.addTab(resource.getString("tabbedPaneParametresJDBC"), "connectionParameters",
            false);

      if (flag.equals("publisher") || flag.equals("admin"))
        tabbedPane.addTab(resource.getString("tabbedPaneScheduler"), "schedulerParameters", true);

      out.println(window.printBefore());
      out.println(tabbedPane.print());
      out.println(frame.printBefore());
%>
<FORM name="form" method="post" action="SetScheduler">
<TABLE align="center" CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS="intfdcolor4">
			<TR>
				<TD colspan=4>
					<span class="txtlibform"><%=resource.getString("schedulerState")%>&nbsp;:</span>
				 <%if (scheduler.getSchedulerState() == DataWarningScheduler.SCHEDULER_STATE_ON)
		        out.print("<font color=\"forestgreen\"><b>"+resource.getString("schedulerMarche")+"</b></font>");
				      else
		        out.print("<font color=\"red\"><b>"+resource.getString("schedulerArret")+"</b></font>");
		        %>
				</TD>
			</TR>
			<TR>
				<TD colspan=4>&nbsp;</TD>
			</TR>
			<TR>
				<td class="txtlibform">Notification :</td>
				<TD><span class="txtlibform"><%=resource.getString("schedulerLaunchStart")%>Intervalle:&nbsp;</span>
					<SELECT NAME="numberOfTimes"
						onchange='javascript:sendForm("UpdateLayer")'>
						<%for (i = 1; i <= 30; i++)
			        out.println("<OPTION value=" + i +
	            ((scheduler.getNumberOfTimes() == i) ? " selected" : "") + ">" + i);%>
					</SELECT>
					<span class="txtlibform">&nbsp;<%=resource.getString("schedulerLaunchEnd")%>&nbsp;</span>

					<SELECT NAME="numberOfTimesMoment"
						onchange='javascript:sendForm("UpdateLayer")'>
						<OPTION value="0"
							<%if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR) {%>
							selected <%}%>><%=resource.getString("heure")%>
						<OPTION value="1"
							<%if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY) {%>
							selected <%}%>><%=resource.getString("jour")%>
						<OPTION value="2"
							<%if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK) {%>
							selected <%}%>><%=resource.getString("semaine")%>
						<OPTION value="3"
							<%if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH) {%>
							selected <%}%>><%=resource.getString("mois")%>
						<OPTION value="4"
							<%if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR) {%>
							selected <%}%>><%=resource.getString("annee")%>
					</SELECT>
				</td>
				<td width="35%">
	 <%if (scheduler.getNumberOfTimes() == 1) {%>
				<TABLE border=0>
					<TR>
						<%if ((scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH)) {%>
						<TD>
							<span class="txtlibform"><%=resource.getString("jour")%>&nbsp;:&nbsp;</span>
							<SELECT NAME="dayOfMonth">
								<%for (i = 0; i < 31; i++)
	            out.println("<OPTION value=" + i +
	                ((scheduler.getDayOfMonth() == i) ? " selected" : "") + ">" + (i + 1));%>
							</SELECT>&nbsp;
						</TD>
						<%}
        if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR) {%>
						<TD>
							<span class="txtlibform"><%=resource.getString("mois")%>&nbsp;:&nbsp;</span>
								<SELECT NAME="month">
									<OPTION value="0" <%if (scheduler.getTheMonth() == 0) {%>
										selected <%}%>><%=resource.getString("GML.mois0")%>
									<OPTION value="1" <%if (scheduler.getTheMonth() == 1) {%>
										selected <%}%>><%=resource.getString("GML.mois1")%>
									<OPTION value="2" <%if (scheduler.getTheMonth() == 2) {%>
										selected <%}%>><%=resource.getString("GML.mois2")%>
									<OPTION value="3" <%if (scheduler.getTheMonth() == 3) {%>
										selected <%}%>><%=resource.getString("GML.mois3")%>
									<OPTION value="4" <%if (scheduler.getTheMonth() == 4) {%>
										selected <%}%>><%=resource.getString("GML.mois4")%>
									<OPTION value="5" <%if (scheduler.getTheMonth() == 5) {%>
										selected <%}%>><%=resource.getString("GML.mois5")%>
									<OPTION value="6" <%if (scheduler.getTheMonth() == 6) {%>
										selected <%}%>><%=resource.getString("GML.mois6")%>
									<OPTION value="7" <%if (scheduler.getTheMonth() == 7) {%>
										selected <%}%>><%=resource.getString("GML.mois7")%>
									<OPTION value="8" <%if (scheduler.getTheMonth() == 8) {%>
										selected <%}%>><%=resource.getString("GML.mois8")%>
									<OPTION value="9" <%if (scheduler.getTheMonth() == 9) {%>
										selected <%}%>><%=resource.getString("GML.mois9")%>
									<OPTION value="10" <%if (scheduler.getTheMonth() == 10) {%>
										selected <%}%>><%=resource.getString("GML.mois10")%>
									<OPTION value="11" <%if (scheduler.getTheMonth() == 11) {%>
										selected <%}%>><%=resource.getString("GML.mois11")%>
								</SELECT>&nbsp;
						</TD>
						<%}
        if (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK) {%>
						<TD>
							<span class="txtlibform"><%=resource.getString("jour")%>&nbsp;:&nbsp;</span>
							<SELECT NAME="dayOfWeek">
								<OPTION value="2" <%if (scheduler.getDayOfWeek() == 2) {%>
									selected <%}%>><%=resource.getString("GML.jour2")%>
								<OPTION value="3" <%if (scheduler.getDayOfWeek() == 3) {%>
									selected <%}%>><%=resource.getString("GML.jour3")%>
								<OPTION value="4" <%if (scheduler.getDayOfWeek() == 4) {%>
									selected <%}%>><%=resource.getString("GML.jour4")%>
								<OPTION value="5" <%if (scheduler.getDayOfWeek() == 5) {%>
									selected <%}%>><%=resource.getString("GML.jour5")%>
								<OPTION value="6" <%if (scheduler.getDayOfWeek() == 6) {%>
									selected <%}%>><%=resource.getString("GML.jour6")%>
								<OPTION value="7" <%if (scheduler.getDayOfWeek() == 7) {%>
									selected <%}%>><%=resource.getString("GML.jour7")%>
								<OPTION value="1" <%if (scheduler.getDayOfWeek() == 1) {%>
									selected <%}%>><%=resource.getString("GML.jour1")%>
							</SELECT>&nbsp;
						</TD>
						<%}
        if ((scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY)) {%>
						<TD>
							<span class="txtlibform"><%=resource.getString("heure")%>&nbsp;:&nbsp;</span>
							<SELECT NAME="heure">
								<%for (i = 0; i < 24; i++)
	            out.println("<OPTION value=" + i + ((scheduler.getHours() == i) ? " selected" : "") +
	                ">" + ((i < 10) ? "0" + Integer.toString(i) : Integer.toString(i)));%>
							</SELECT>
						</TD>
						<%}
        if ((scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY) ||
            (scheduler.getNumberOfTimesMoment() == DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR)) {%>
						<TD>
							<span class="txtlibform"><%=resource.getString("minute")%>&nbsp;:&nbsp;</span>
							<SELECT NAME="min">
								<%for (i = 0; i < 60; i++)
	            out.println("<OPTION value=" + i + ((scheduler.getMinits() == i) ? " selected" : "") +
	                ">" + ((i < 10) ? "0" + Integer.toString(i) : Integer.toString(i)));%>
							</SELECT>
						</TD>
						<%}%>
					</TR>
				</TABLE>
				<%}%>
				</TD>
			</TR>
			<tr>
				<td colspan=4>
				<table>
					<TR>
						<TD>&nbsp;</TD>
					</TR>
					<TR>
						<TD><span class="txtlibform"><%=resource.getString("listeAbonne")%>
						:</span></TD>
					</TR>
					<TR>
						<TD valign=top><span class="txtlibform"><%=resource.getString("champGroupes")%>
						:</span></TD>
						<TD><select name="selectedGroupes" multiple size="6">
							<%=listGroups%>
						</select></TD>
						<TD valign=top>&nbsp;&nbsp;<span class="txtlibform"><%=resource.getString("champUtilisateurs")%>
						:</span></TD>
						<TD>&nbsp; <select name="selectedUsers" multiple size="6">
							<%=listUsers%>
						</select></TD>
					</TR>
				</table>
				</td>
			</tr>
		</TABLE>
</FORM>
<center>
<%buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonValider"),
          "javascript:document.form.submit()", false));
      buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"),
          "schedulerParameters", false));
      out.println(buttonPane.print());%>
</CENTER>
<%out.println(frame.printAfter());
      out.println(window.printAfter());%>
</BODY>
</HTML>