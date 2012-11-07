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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("operationPaneParamGen")%></TITLE>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onload=document.form.SQLReqDescription.focus()>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js">
</script>
<script language="JavaScript">
	function isValidText(input, textFieldLength) 
	{
		if (input.length <= textFieldLength)
			return true;
		else
			return false;
	}
	function ClosePopup_onValider()
	{
		if(!isValidText(document.form.SQLReqDescription.value, 256))
			alert('<%=resource.getString("erreurChampsTropLong")%>');
		else
		{
    <%
            if (dataQuery.getType() == DataWarningQuery.QUERY_TYPE_TRIGGER)
            {
    %>
                if(!isNumericField(document.form.seuil.value))
                    alert('<%=resource.getString("erreurChampsNonNumeric")%>');
                else
                    document.form.submit();
    <%
            }
            else
            {
    %>
                document.form.submit();
    <%
            }
    %>
		}
	}
</script>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<CENTER>
<FORM name="form" action="SaveParamGenQuery"  Method="POST">
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor4>
	<TR>
		<TD>
			<TABLE CELLPADDING=5 CELLSPACING=5 BORDER=0 WIDTH="100%" CLASS=contourintfdcolor>
				<TR CLASS=intfdcolor4>
					<TD align="left" valign=top>
						<span class="txtlibform"><%=resource.getString("champsDescription")%> : </span>
					</TD>		
					<TD align="left" valign=top>
						<TEXTAREA NAME="SQLReqDescription" COLS=100 ROWS="8"><%=dataQuery.getDescription()%></TEXTAREA >
					</TD>
				</TR>
		<%
		        if (dataQuery.getType() == DataWarningQuery.QUERY_TYPE_TRIGGER)
			    {
		%>
		            <TR CLASS=intfdcolor4>
		                <TD align="left" valign=top>
		                    <span class="txtlibform"><%=resource.getString("resultatSeuil")%></span>
		                </TD>
		                <TD align="left" valign=top>
		                    <SELECT name="condition">
		                        <OPTION value="0" <% if(dataQuery.getTheTriggerCondition() == DataWarningQuery.TRIGGER_CONDITION_SUP) { %> selected <% } %>><%=resource.getString("triggerCondition0")%></OPTION>
		                        <OPTION value="1" <% if(dataQuery.getTheTriggerCondition() == DataWarningQuery.TRIGGER_CONDITION_SUP_OU_EG) { %> selected <% } %>><%=resource.getString("triggerCondition1")%></OPTION>
		                        <OPTION value="2" <% if(dataQuery.getTheTriggerCondition() == DataWarningQuery.TRIGGER_CONDITION_INF) { %> selected <% } %>><%=resource.getString("triggerCondition2")%></OPTION>
		                        <OPTION value="3" <% if(dataQuery.getTheTriggerCondition() == DataWarningQuery.TRIGGER_CONDITION_INF_OU_EG) { %> selected <% } %>><%=resource.getString("triggerCondition3")%></OPTION>
		                        <OPTION value="4" <% if(dataQuery.getTheTriggerCondition() == DataWarningQuery.TRIGGER_CONDITION_EG) { %> selected <% } %>><%=resource.getString("triggerCondition4")%></OPTION>
		                        <OPTION value="5" <% if(dataQuery.getTheTriggerCondition() == DataWarningQuery.TRIGGER_CONDITION_DIF) { %> selected <% } %>><%=resource.getString("triggerCondition5")%></OPTION>
		                    </SELECT>
		                    &nbsp;
		                    <INPUT type=text name="seuil" value="<%=dataQuery.getTheTrigger()%>">
		                </TD>
		            </TR>
		<%
			    }
		%>
		        <TR CLASS=intfdcolor4>
		            <TD align="left" valign=top>
		                <span class="txtlibform"><%=resource.getString("requetePersonnalisee")%></span>
		            </TD>
		            <TD align="left" valign=top>
		            	<INPUT type="checkbox" name="PersoValid" value="1"<%=((dataQuery.getPersoValid() == DataWarningQuery.QUERY_PERSO_VALID) ? " checked" : "")%>>&nbsp;<%=resource.getString("requetePersoAvail")%>
		            	<BR><%=resource.getString("requetePersoUID")%> : 
		                <SELECT name="PersoUID">
		                    <OPTION value="<%=DataWarningQuery.QUERY_PERSO_UID_ID%>" <% if(DataWarningQuery.QUERY_PERSO_UID_ID.equals(dataQuery.getPersoUID())) { %> selected <% } %>><%=resource.getString("requetePersoUID_ID")%></OPTION>
		                    <OPTION value="<%=DataWarningQuery.QUERY_PERSO_UID_LOGIN%>" <% if(DataWarningQuery.QUERY_PERSO_UID_LOGIN.equals(dataQuery.getPersoUID())) { %> selected <% } %>><%=resource.getString("requetePersoUID_LOGIN")%></OPTION>
		                    <OPTION value="<%=DataWarningQuery.QUERY_PERSO_UID_LASTNAME%>" <% if(DataWarningQuery.QUERY_PERSO_UID_LASTNAME.equals(dataQuery.getPersoUID())) { %> selected <% } %>><%=resource.getString("requetePersoUID_LASTNAME")%></OPTION>
		                    <OPTION value="<%=DataWarningQuery.QUERY_PERSO_UID_SPECIFICID%>" <% if(DataWarningQuery.QUERY_PERSO_UID_SPECIFICID.equals(dataQuery.getPersoUID())) { %> selected <% } %>><%=resource.getString("requetePersoUID_SPECIFICID")%></OPTION>
		                    <OPTION value="<%=DataWarningQuery.QUERY_PERSO_UID_EMAIL%>" <% if(DataWarningQuery.QUERY_PERSO_UID_EMAIL.equals(dataQuery.getPersoUID())) { %> selected <% } %>><%=resource.getString("requetePersoUID_EMAIL")%></OPTION>
		                </SELECT>
		                <BR><%=resource.getString("requetePersoColNum")%> : 
		                <SELECT name="PersoCol">
		                <%
		                	for (int nc = 1; nc <= 10; nc++)
		                	{
		                		out.println("<OPTION value=" + Integer.toString(nc) + ((dataQuery.getPersoColNB() == nc) ? " selected" : "") + ">#" + Integer.toString(nc) + "</OPTION>");
		                	}
		                %>
		                </SELECT>
		                
		            </TD>
		        </TR>
			</TABLE>
		</td>
	</tr>
</table>
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