<%@ include file="imports.jsp" %>
<%@ include file="init.jsp" %>
<%
	Long seuil = (Long)request.getAttribute("seuil");
	Integer condition = (Integer)request.getAttribute("condition");
	if(condition == null) condition = new Integer(0);
%>
<HTML>
<HEAD>
<TITLE><%=messages.getString("operationPaneSeuil")%></TITLE>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js">
</script>
<script language="JavaScript">
	function ClosePopup_onValider()
	{
		document.form.action = "SaveSeuil";
		alert(document.form.seuil.value)
		if(!isNumericField(document.form.seuil.value))
			alert('<%=messages.getString("erreurChampsNonNumeric")%>');
		else
			document.form.submit();
	}
</script>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<CENTER>
<FORM name="form" method="POST">
	<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
		<TR CLASS=intfdcolor4>
			<TD align="left" valign=top>
				<span class="txtlibform"><%=messages.getString("resultatSeuil")%></span>
			</TD>
			<TD align="left" valign=top>
				<SELECT name="condition">
					<OPTION value="0" <% if(condition.intValue() == 0) { %> selected <% } %>><%=messages.getString("triggerCondition0")%></OPTION>
					<OPTION value="1" <% if(condition.intValue() == 1) { %> selected <% } %>><%=messages.getString("triggerCondition1")%></OPTION>
					<OPTION value="2" <% if(condition.intValue() == 2) { %> selected <% } %>><%=messages.getString("triggerCondition2")%></OPTION>
					<OPTION value="3" <% if(condition.intValue() == 3) { %> selected <% } %>><%=messages.getString("triggerCondition3")%></OPTION>
					<OPTION value="4" <% if(condition.intValue() == 4) { %> selected <% } %>><%=messages.getString("triggerCondition4")%></OPTION>
					<OPTION value="5" <% if(condition.intValue() == 5) { %> selected <% } %>><%=messages.getString("triggerCondition5")%></OPTION>
				</SELECT>
			</TD>
			<TD align="left" valign=top>
<%
				if(seuil == null) {
%>
					<INPUT type=text name="seuil" value="">
<%
				} else {
%>
					<INPUT type=text name="seuil" value="<%=seuil.longValue()%>">
<%
				}
%>
			</TD>
		</TR>
	</TABLE>
</FORM>
<%
    buttonPane.addButton((Button) gef.getFormButton(messages.getString("boutonValider"), "javascript:onClick=ClosePopup_onValider()", false));
    buttonPane.addButton((Button) gef.getFormButton(messages.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));
    out.println(buttonPane.print());
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>