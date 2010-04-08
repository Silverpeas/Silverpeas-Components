<%@ include file="checkDataWarning.jsp" %>
<%
	DataWarning data = (DataWarning)request.getAttribute("data");
%>
<HTML>
<HEAD>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onload=document.form.SQLReqDescription.focus()>
<script language="JavaScript">
	function isValidText(input, textFieldLength) 
	{
		if (input.length <= textFieldLength)
			return true;
		else
			return false;
	}

	function changetypeAnalyse()
	{
		alert("<%=resource.getString("changeTypeRequete")%>");
	}

	function ClosePopup_onValider()
	{
		if(!isValidText(document.form.SQLReqDescription.value, 256))
			alert('<%=resource.getString("erreurChampsTropLong")%>');
		else
		{
			document.form.action = "SaveParamGen";
			document.form.submit();
		}
	}
</script>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<CENTER>
<FORM name="form" action=""  Method="POST">
	<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
		<TR CLASS=intfdcolor4>
			<TD align="left" valign=top>
				<span class="txtlibform"><%=resource.getString("champsDescription")%> : </span>
			</TD>		
			<TD align="left" valign=top>
				<TEXTAREA NAME="SQLReqDescription" COLS=100 ROWS="8"><%=data.getDescription()%></TEXTAREA >
			</TD>
		</TR>
		<TR CLASS=intfdcolor4>
			<TD align="left" valign=top>
				<span class="txtlibform"><%=resource.getString("typeAnalyse")%> : </span>
			</TD>		
			<TD align="left" valign=top>
				<SELECT name="typeAnalyse" onChange="changetypeAnalyse()">
					<OPTION value="<%=DataWarning.INCONDITIONAL_QUERY%>" <% if (data.getAnalysisType() == DataWarning.INCONDITIONAL_QUERY) { %> selected <% } %>><%=resource.getString("typeAnalyse0")%></OPTION>
					<OPTION value="<%=DataWarning.TRIGGER_ANALYSIS%>" <% if (data.getAnalysisType() == DataWarning.TRIGGER_ANALYSIS) { %> selected <% } %>><%=resource.getString("typeAnalyse1")%></OPTION>
				</SELECT>
			</TD>
		</TR>
	</TABLE>
</FORM>
<%
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonValider"), "javascript:onClick=ClosePopup_onValider()", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));
    out.println(buttonPane.print());
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>