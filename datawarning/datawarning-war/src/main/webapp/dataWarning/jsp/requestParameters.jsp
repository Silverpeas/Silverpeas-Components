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