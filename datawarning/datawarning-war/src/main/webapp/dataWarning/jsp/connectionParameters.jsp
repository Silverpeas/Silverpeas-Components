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
                      			out.println("<option value=\""+dataWarningDBDrivers[i].DriverUniqueID+"\" " + ((currentDBDriver.DriverUniqueID.equals(dataWarningDBDrivers[i].DriverUniqueID)) ? "selected" : "") + ">"+dataWarningDBDrivers[i].DriverName);
                            }
%>
                        </select>
                    </TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champsDescription")%> :</TD>
					<TD>
						<input type="text" name="DescriptionDrv" size="100" disabled value="<%=currentDBDriver.Description%>">
					</TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champUrlJDBC")%> :</TD>
					<TD>
						<input type="text" name="JDBCUrl" size="100" disabled value="<%=currentDBDriver.JDBCUrl%>">
                    </TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champIdentifiant")%> :</TD>
					<TD>
						<input type="text" name="Login" size="100" maxlength="<%=DBUtil.TextFieldLength%>" value="<%=dataWarningObject.getLogin()%>">
					</TD>
				</TR>
				<TR>
					<TD class="txtlibform"><%=resource.getString("champMotDePasse")%> :</TD>
					<TD>
						<input type="password" name="Password" size="100" maxlength="<%=DBUtil.TextFieldLength%>" value="<%=dataWarningObject.getPwd()%>">
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
