<%@ include file="init.jsp" %>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
	<link rel="stylesheet" type="text/css" href="<%=applicationURL%>/myDB/jsp/styleSheet/tableSelection.css">
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/tableSelection.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript">
		function terminate()
		{
			var test = "";
			var form = document.forms["processForm"];
			if (form.elements["tableName"].value == "")
			{
				alert("<%=resource.getString("SelectTable")%>");
			}
			else
			{
				form.submit();
			}
		}
	</script>
</head>

<body onload="init('tableName')" marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setExtraInformation(resource.getString("PageTitleTableSelection"));

	if (userRoleLevel.equals("admin"))
	{
		String tableAction = new StringBuffer(MyDBConstants.ACTION_UPDATE_TABLE)
			.append("?command=init")
			.append("&mode=").append(TableManager.MODE_CREATION)
			.append("&originPage=").append(MyDBConstants.PAGE_TABLE_SELECTION)
			.toString();
		operationPane.addOperation(
			resource.getIcon("myDB.addTable"), resource.getString("OperationCreateTable"), tableAction);
	}

	out.println(window.printBefore());
	
	// Current page tab index.
	int tabIndex = 2;
	
	String[] tableNames = myDBSC.getTableNames();
	int tableCount = (tableNames != null ? tableNames.length : 0);
%>
	<%@ include file="tabs.jsp" %>
<%
	out.println(frame.printBefore());

	if (tableCount > 0)
	{
		boolean displayTableNameInput = (tableCount > 30);
%>
	<form name="processForm" action="<%=MyDBConstants.ACTION_TABLE_SELECTION%>">
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform" nowrap><%=resource.getString("SelectTable")%> : </td>
								<td align="<%=(displayTableNameInput ? "right" : "left")%>"><select name="tableName"><%

		String selectedTableName = myDBSC.getTableName();
		if (selectedTableName == null || selectedTableName.length() == 0)
		{
%>
										<option value="" selected></option><%

		}
	
		String tableName;
		for (int i = 0; i < tableCount; i++)
		{
			tableName = tableNames[i];
%>
										<option value="<%=tableName%>" <%if (tableName.equals(selectedTableName)) {%>selected<%}%>><%=tableName%></option><%

		}
%>
									</select>
								</td><%

		if (displayTableNameInput)
		{
%>
								<td width="30%"><input type="text" name="tableNameInput" value=""/></td><%

		}
%>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</center>
	</form>
	<center>
<%
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonTerminate"), "javascript:terminate();", false));
		buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:history.back();", false));
		out.print(buttonPane.print());
%>
	</center>
<%
	}
	else
	{
%>
	<br>
	<center>
		<span class="MessageNotRead"><%=resource.getString("NoTablesAvailable")%></span>
<%
		if (tableNames == null)
		{
%>
		<br>
		<br>
		<span class="MessageNotRead"><i><%=resource.getString("CheckConnectionSetting")%></i></span>
<%
		}
%>
	</center>
	<br>
<%
	}
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>