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

<%@ include file="checkDataWarning.jsp" %>
<%
	DataWarningQuery dataQuery = (DataWarningQuery)request.getAttribute("dataQuery");
	DataWarning data = (DataWarning)request.getAttribute("data");
    int currentQuery = ((Integer)request.getAttribute("currentQuery")).intValue();
%>
<HTML>
<HEAD>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
	function changeQuery()
	{
		document.ParamsForm.Action = "changeQuery";
		document.ParamsForm.submit();
	}

	function editTableColumn()
	{
		SP_openWindow("SelectTable", "SqlRequest_Debut", "600", "450","scrollbars=yes, scrollable=yes");
	}

	function editParamGeneraux()
	{
		SP_openWindow("EditParamGenQuery", "Param_Generaux_Query", "800", "350", "");
	}

	function editRequete()
	{
		SP_openWindow("EditReqExpert", "Edit_Req", "700", "450", "scrollbars=yes, scrollable=yes");
	}
</script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	//operation Pane 
	operationPane.addOperation(resource.getIcon("DataWarning.params"), resource.getString("operationPaneParamGen"), "javascript:onClick=editParamGeneraux()");
    operationPane.addOperation(resource.getIcon("DataWarning.request"), resource.getString("operationPaneRequete"), "javascript:onClick=editTableColumn()");
	operationPane.addOperation(resource.getIcon("DataWarning.expert"), resource.getString("operationPaneReqExpert"), "javascript:onClick=editRequete()");
	
	//Les onglets
    tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("tabbedPaneConsultation"), "dataWarning", false);
    
    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(resource.getString("tabbedPaneRequete"), "requestParameters", true);
	
	if (flag.equals("admin"))
		tabbedPane.addTab(resource.getString("tabbedPaneParametresJDBC"), "connectionParameters", false);

	if (flag.equals("publisher") || flag.equals("admin"))
		tabbedPane.addTab(resource.getString("tabbedPaneScheduler"), "schedulerParameters", false);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<CENTER>
<FORM name="ParamsForm" method="post" action="changeQuery">
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor4>
	<TR>
		<TD>
			<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
				<TR CLASS=intfdcolor4>
					<TD align="left" valign=top>
						<span class="txtlibform"><%=resource.getString("typeRequete")%> : </span>
					</TD>		
					<TD align="left" valign=top>
              <SELECT NAME="typeRequete" onchange="javascript:changeQuery()">
<%
	               out.println("<OPTION value=\"" + DataWarningQuery.QUERY_TYPE_RESULT + "\" " + ((currentQuery == DataWarningQuery.QUERY_TYPE_RESULT) ? "selected" : "") + ">" + resource.getString("typeRequete0"));
	               if (data.getAnalysisType() == DataWarning.TRIGGER_ANALYSIS)
	                   out.println("<OPTION value=\"" + DataWarningQuery.QUERY_TYPE_TRIGGER + "\" " + ((currentQuery == DataWarningQuery.QUERY_TYPE_TRIGGER) ? "selected" : "") + ">" + resource.getString("typeRequete1"));
%>
              </SELECT>
					</TD>
				</TR>
				<TR CLASS=intfdcolor4>
					<TD align="left" valign=top>
						<span class="txtlibform"><%=resource.getString("champsDescription")%> : </span>
					</TD>		
					<TD align="left" valign=top>
        		<%=dataQuery.getDescription()%>
					</TD>
				</TR>
				<TR CLASS=intfdcolor4>
					<TD align="left" valign=top>
						<span class="txtlibform"><%=resource.getString("champRequete")%> : </span>
					</TD>		
					<TD align="left" valign=top>
						<%=dataQuery.getQuery()%>
					</TD>
				</TR>
			</TABLE>
		</TD>
	</TR>
</TABLE>	
</FORM>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>