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
DataWarning         dataWarningObject = (DataWarning)request.getAttribute("dataWarningObject");
DataWarningDBDriver[] dataWarningDBDrivers = (DataWarningDBDriver[])request.getAttribute("dataWarningDBDrivers");
DataWarningDBDriver currentDBDriver = (DataWarningDBDriver)request.getAttribute("currentDBDriver");
%>
<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js">
</script>
<Script language="JavaScript">
    function selectDriver()
    {
        document.processForm.action = "updateConnection";
        document.processForm.submit();
    }

	function processUpdate()
	{
		if (isValidTextField(document.processForm.Login)== false)
		{
			document.processForm.Login.focus();
			alert("<%=resource.getString("erreurChampsTropLong")%>");
		}
		else if (isValidTextField(document.processForm.Password)== false)
		{
			document.processForm.Password.focus();
			alert("<%=resource.getString("erreurChampsTropLong")%>");
		}
		else if(document.processForm.RowLimit.value == "")
		{
			document.processForm.RowLimit.focus();
			alert("<%=resource.getString("erreurChampsVide")%>");
		}
		else if (isFinite(document.processForm.RowLimit.value)== false)
		{
			document.processForm.RowLimit.focus();
			alert("<%=resource.getString("erreurChampsNonEntier")%>");
		}
		else
		{
			document.processForm.action = "SetConnection";
			document.processForm.submit();
		}
	}
    //updateConnection
</Script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	//Les onglets
    tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("tabbedPaneConsultation"), "dataWarning", false);

    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(resource.getString("tabbedPaneRequete"), "requestParameters",false );

	if (flag.equals("admin"))
		tabbedPane.addTab(resource.getString("tabbedPaneParametresJDBC"), "connectionParameters", true);

	if (flag.equals("publisher") || flag.equals("admin"))
		tabbedPane.addTab(resource.getString("tabbedPaneScheduler"), "schedulerParameters", false);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<form name="processForm" action="">
<%
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonValider"), "javascript:onClick=processUpdate()", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"), "javascript:history.back()", false));
%>

<CENTER>
<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<TR>
		<TD>
			<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champNomDriver")%> :</TD>
					<TD>
                        <select name="JDBCdriverNameSelect" onChange="javascript:selectDriver();">
<%
                            for (int i = 0; i < dataWarningDBDrivers.length; i++)
                            {
                      			out.println("<option value=\""+dataWarningDBDrivers[i].getDriverUniqueID()+"\" " + ((currentDBDriver.getDriverUniqueID().equals(dataWarningDBDrivers[i].getDriverUniqueID())) ? "selected" : "") + ">"+dataWarningDBDrivers[i].getDriverName());
                            }
%>
                        </select>
                    </TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champsDescription")%> :</TD>
					<TD>
						<input type="text" name="DescriptionDrv" size="100" disabled value="<%=currentDBDriver.getDescription()%>">
					</TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champUrlJDBC")%> :</TD>
					<TD>
						<input type="text" name="JDBCUrl" size="100" disabled value="<%=currentDBDriver.getJDBCUrl()%>">
                    </TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champIdentifiant")%> :</TD>
					<TD>
						<input type="text" name="Login" size="100" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=dataWarningObject.getLogin()%>">
					</TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champMotDePasse")%> :</TD>
					<TD>
						<input type="password" name="Password" size="100" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=dataWarningObject.getPwd()%>">
					</TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champLignesMax")%> :</TD>
					<TD>
						<input type="text" name="RowLimit" size="10" maxlength="<%=String.valueOf(Integer.MAX_VALUE).length()%>" value="<%=dataWarningObject.getRowLimit()%>">
					</TD>
				</TR>
			</TABLE>
		</TD>
	</TR>
</TABLE>
<% 	out.println(buttonPane.print()); %>
</CENTER>
</form>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>
