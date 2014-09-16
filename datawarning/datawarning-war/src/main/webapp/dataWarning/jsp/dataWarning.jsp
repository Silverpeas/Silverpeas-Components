<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkDataWarning.jsp" %>
<%
	DataWarning data = (DataWarning)request.getAttribute("data");
    DataWarningScheduler scheduler = (DataWarningScheduler)request.getAttribute("scheduler");
    DataWarningResult result = (DataWarningResult)request.getAttribute("result");
    DataWarningQueryResult resultQuery = (result == null) ? null : result.getQueryResult();
		String userId = (String)request.getAttribute("userId");

    Boolean isAbonne = (Boolean)request.getAttribute("isAbonne");
		String analysisTypeString = (String)request.getAttribute("analysisTypeString");

    String textFrequenceScheduler = (String)request.getAttribute("textFrequenceScheduler");
%>
<HTML>
<HEAD>
<view:looknfeel/>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<script language=javascript>
	function suscribe()
	{
		document.form.action = "Suscribe";
		document.form.suscribeAction.value = "true";
		document.form.submit();
	}
	function unsuscribe()
	{
		document.form.action = "Suscribe";
		document.form.suscribeAction.value = "false";
		document.form.submit();
	}
	function editParamGeneraux()
	{
		SP_openWindow("EditParamGen", "Param_Generaux", "800", "280", "");
	}
</script>
<FORM name="form" method="post" action="">
	<INPUT type="hidden" name="suscribeAction" value="">
</FORM>
<%
    if (flag.equals("publisher") || flag.equals("admin"))
    	operationPane.addOperation(resource.getIcon("DataWarning.params"), resource.getString("operationPaneParamGen"), "javascript:onClick=editParamGeneraux()");

    //operation Pane
	if(!isAbonne.booleanValue())
		operationPane.addOperation(resource.getIcon("DataWarning.suscribe"), resource.getString("abonner"), "javascript:onClick=suscribe()");
	else
		operationPane.addOperation(resource.getIcon("DataWarning.unsuscribe"), resource.getString("desabonner"), "javascript:onClick=unsuscribe()");

	//Les onglets
    tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("tabbedPaneConsultation"), "dataWarning", true);

    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(resource.getString("tabbedPaneRequete"), "requestParameters", false);

	if (flag.equals("admin"))
		tabbedPane.addTab(resource.getString("tabbedPaneParametresJDBC"), "connectionParameters", false);

	if (flag.equals("publisher") || flag.equals("admin"))
		tabbedPane.addTab(resource.getString("tabbedPaneScheduler"), "schedulerParameters", false);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<CENTER>
<br>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor4>
	<TR>
		<TD>
			<TABLE CELLPADDING=5 CELLSPACING=1 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
		<%
			if(isAbonne != null)
			{
		%>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign="top" colspan="2">
					<span class="txtlibform">
		<%
						if(isAbonne.booleanValue())
							out.print(resource.getString("abonne"));
						else
							out.print(resource.getString("nonabonne"));
		%>
						<BR><BR>
					</span>
				</TD>
			</TR>
		<%
			}
		%>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("champsDescription")%> : </span>
				</TD>
				<TD align="left" valign=top>
					<%=data.getDescription()%>
				</TD>
			</TR>
		<%
		    if (result != null)
		    {
		%>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("typeAnalyse")%> :</span>
				</TD>
				<TD align="left">
							<%=analysisTypeString%>
				</TD>
			</TR>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("requetePersonnalisee")%> :</span>
				</TD>
				<TD align="left">
					<%
					if (resultQuery != null)
					{
						if (resultQuery.isPersoEnabled())
							out.println(resource.getString("requetePersonnaliseeYes"));
						else
							out.println(resource.getString("requetePersonnaliseeNo"));
					}
					else
						out.println(resource.getString("requetePersonnaliseeNo"));
					%>
				</TD>
			</TR>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("resultat")%> :</span>
				</TD>
				<TD align="left">
		<%
					if(resultQuery != null)
					{
						ArrayPane arrayPane = gef.getArrayPane("ViewRequeteOngletConsult","",request,session);
						arrayPane.setSortable(false);
						arrayPane.setVisibleLineNumber(20);

						Iterator itCols = resultQuery.getColumns(userId).iterator();
						while (itCols.hasNext())
							arrayPane.addArrayColumn((String)itCols.next());

							Iterator itRows = resultQuery.getValues(userId).iterator();
							while (itRows.hasNext())
							{
								ArrayList theRow = (ArrayList)itRows.next();
								if (resultQuery.isPersoEnabled())
									theRow.remove(resultQuery.getPersoColumnNumber());
								ArrayLine arrayLine = arrayPane.addArrayLine();
								Iterator itVals = theRow.iterator();
								while (itVals.hasNext())
									arrayLine.addArrayCellText((String)itVals.next());
							}
							out.println(arrayPane.print());
					}
		%>
				</TD>
			</TR>
		<%
		        if (data.getAnalysisType() == DataWarning.TRIGGER_ANALYSIS)
		        {
		%>
		        <TR CLASS=intfdcolor4>
		            <TD align="left" valign=top>
		                <span class="txtlibform"><%=resource.getString("seuil")%> :</span>
		            </TD>
		            <TD align="left">
									<%=resource.getString("resultatSeuilValeur")%>&nbsp;<%=resource.getString("triggerCondition"+Integer.toString(result.getTriggerCondition()))%>&nbsp;<%=result.getTrigger()%><BR><BR>
			<%
		            if (result.getTriggerEnabled(userId))
			                out.println("<B>" + resource.getString("seuilAtteint") + "</B>");
		            else
			                out.println(resource.getString("seuilNonAtteint"));
		%>
		            </TD>
		        </TR>
		<%
		        }
			}
		    else // Query Vide
		    {
		%>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("noQuery")%> !</span>
				</TD>
			</TR>
		<%
			}
			if(scheduler.getSchedulerState() == 1)
			{
		%>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("frequenceScheduler")%> :</span>
				</TD>
				<TD align="left">
					<%=textFrequenceScheduler%>
				</TD>
			</TR>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("nextTimeScheduler")%> :</span>
				</TD>
				<TD align="left">
					<%
						SimpleDateFormat sdf = new SimpleDateFormat(resource.getString("dateFormatScheduler"));
						out.print(sdf.format(new Date(scheduler.getWakeUp())));
					%>
				</TD>
			</TR>
		<%
			}
		%>
			<TR CLASS=intfdcolor4>
				<TD align="left" valign=top>
					<span class="txtlibform"><%=resource.getString("schedulerState")%> :</span>
				</TD>
				<TD align="left">
		<%
					if(scheduler.getSchedulerState() == 1)
						out.print("<b>" + resource.getString("schedulerMarche")+"</b>");
					else
						out.print(resource.getString("schedulerArret"));
		%>
				</TD>
			</TR>
			</table>
		</td>
	</tr>
</table>
<br>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</Body>
</HTML>
