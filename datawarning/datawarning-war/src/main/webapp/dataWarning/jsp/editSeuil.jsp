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