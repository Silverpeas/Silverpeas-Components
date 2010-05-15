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

<%@ include file="init.jsp" %>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());

	TableManager tableManager = myDBSC.getTableManager();
	DataTypeList dataTypeList = tableManager.getDataTypeList();
	ForeignKeys foreignKeys = tableManager.getForeignKeys();
	DbColumn[] columns = tableManager.getTable().getColumns();
	int columnsCount = columns.length;
	
	int index = Integer.parseInt((String)request.getAttribute("index"));
	ForeignKey foreignKey = (ForeignKey)request.getAttribute("foreignKey");
	if (foreignKey == null && index != -1)
	{
		foreignKey = foreignKeys.get(index);
	}
	String foreignKeyName;
	String[] selectedColumns;
	String selectedForeignTable;
	String[] selectedForeignColumnsNames;
	if (foreignKey != null)
	{
		foreignKeyName = foreignKey.getName();
		selectedColumns = foreignKey.getColumns();
		selectedForeignTable = foreignKey.getForeignTable();
		selectedForeignColumnsNames = foreignKey.getForeignColumnsNames();
	}
	else
	{
		foreignKeyName = foreignKeys.getConstraintName();
		selectedColumns = new String[] {""};
		selectedForeignTable = "";
		selectedForeignColumnsNames = new String[0];
	}
	IndexList indexList = myDBSC.getIndexInfo(selectedForeignTable);
	DbColumn[] foreignColumns = indexList.getColumns();
	int foreignColumnsCount = foreignColumns.length;
	int indexInfoMaxColumnsCount = indexList.getIndexInfoMaxColumnsCount();
	
	String[] tableNames = myDBSC.getTableNames();
	int tableCount = (tableNames != null ? tableNames.length : 0);
	boolean displayTableNameInput = (tableCount > 30);
	int colspan = (displayTableNameInput ? 2 : 1);
%>
	<link rel="stylesheet" type="text/css" href="<%=applicationURL%>/myDB/jsp/styleSheet/tableSelection.css">
	<style type="text/css">
		#refreshLink {visibility: hidden}
		.linkedColumn {font-weight: bold}
	</style>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/foreignKey.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/tableSelection.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript">
		function initFK()
		{
			defaultDataType = <%=DbColumn.DEFAULT_DATA_TYPE%>;
			defaultDataSize = <%=DbColumn.DEFAULT_DATA_SIZE%>;
			keySeparator = "<%=DbUtil.KEY_SEPARATOR%>";
			form = document.forms["processForm"];<%
		
	for (int i = 0; i < columnsCount; i++)
	{
%>
			columns[<%=i%>] = new Column("<%=columns[i].getName()%>", <%=columns[i].getDataType()%>, <%=columns[i].getDataSize()%>, "<%=columns[i].getDefaultValueAsString()%>");<%

	}

	for (int i = 0; i < foreignColumnsCount; i++)
	{
%>
			foreignColumns[<%=i%>] = new Column("<%=foreignColumns[i].getName()%>", <%=foreignColumns[i].getDataType()%>, <%=foreignColumns[i].getDataSize()%>, "");<%

	}
%>
			indexInfoMaxColumnsCount = <%=indexInfoMaxColumnsCount%>;
			// Call to init function of file tableSelection.js
			init("foreignTable");
		}

		function validate()
		{
			removeAccents(form.elements["name"]);
			var name = form.elements["name"].value;
			if (name == "")
			{
				alert("<%=resource.getString("ErrorForeignKeyNameRequired")%>");
				return;
			}
			if (!isSqlValidName(name))
			{
				alert("<%=resource.getString("ErrorForeignKeyNameRegExp")%>");
				return;
			}
			var count = getColumnsCount();
			var i;
			var columnName;
			var columnsNames = new Array(count);
			for (i = 0; i < count; i++)
			{
				columnName = form.elements["column_" + i].value;
				columnsNames[i] = columnName;
				if (columnName == "")
				{
					alert("<%=resource.getString("ErrorForeignKeyColumnRequired")%>");
					return;
				}
			}
			var j;
			for (i = 0; i < count; i++)
			{
				columnName = columnsNames[i];
				for (j = (i + 1); j < count; j++)
				{
					if (columnName == columnsNames[j])
					{
						alert("<%=resource.getString("ErrorForeignKeyColumnUnique")%>");
						return;
					}
				}
			}
			if (form.elements["foreignTable"].value == "")
			{
				alert("<%=resource.getString("ErrorForeignKeyForeignTableRequired")%>");
				return;
			}
			var foreignColumnsNames = form.elements["foreignColumnsNames"].value;
			if (foreignColumnsNames == "")
			{
				alert("<%=resource.getString("ErrorForeignKeyForeignColumnRequired")%>");
				return;
			}
			var fColumnsNames = getForeignColumns();
			var fColumnName;
			var typeWarning = false;
			var sizeWarning = false;
			var defaultValueWarning = false;
			for (i = 0; i < count; i++)
			{
				columnName = columnsNames[i];
				fColumnName = fColumnsNames[i];
				typeWarning = typeWarning || (getColumnType(columns, columnName) != getColumnType(foreignColumns, fColumnName));
				sizeWarning = sizeWarning || (getColumnSize(columns, columnName) != getColumnSize(foreignColumns, fColumnName));
				defaultValueWarning = defaultValueWarning || (getColumnDefaultValue(columns, columnName) != "");
			}
			submitForm(typeWarning, sizeWarning, defaultValueWarning);
		}

		function submitForm(typeWarning, sizeWarning, defaultValueWarning)
		{
			var continueProcess = true;
			if (typeWarning || sizeWarning || defaultValueWarning)
			{
				var warning = new Warning("<%=resource.getString("Warning")%>", "<%=resource.getString("WarningConfirm")%>");
				if (typeWarning)
				{
					addWarningDetail(warning, "<%=resource.getString("WarningForeignKeyForeignColumnType")%>");
				}
				if (sizeWarning)
				{
					addWarningDetail(warning, "<%=resource.getString("WarningForeignKeyForeignColumnSize")%>");
				}
				if (defaultValueWarning)
				{
					addWarningDetail(warning, "<%=resource.getString("WarningDefaultValueColumnInForeignKey")%>");
				}
				continueProcess = confirm(displayWarning(warning));
			}
			if (continueProcess)
			{
				processSubmitForm();
			}
		}
	</script>
</head>

<body onload="initFK()" marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setExtraInformation(resource.getString(
		index == -1 ? "PageTitleForeignKeyCreation" : "PageTitleForeignKeyModification"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<form name="processForm" action="<%=MyDBConstants.ACTION_UPDATE_TABLE%>" method="post" onsubmit="return false;">
		<input type="hidden" name="command" value=""/>
		<input type="hidden" name="refreshForeignColumns" value=""/>
		<input type="hidden" name="index" value="<%=index%>"/><%

	for (int i = 0; i < indexInfoMaxColumnsCount; i++)
	{
%>
		<input type="hidden" name="foreignColumnType_<%=i%>" value=""/>
		<input type="hidden" name="foreignColumnSize_<%=i%>" value=""/><%

	}
		%>
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform" nowrap><%=resource.getString("ForeignKeyName")%> :</td>
								<td colspan="<%=colspan%>"><input type="text" name="name" size="50" maxlength="64" value="<%=foreignKeyName%>" onkeyup="removeAccents(this)"/></td>
							</tr>
							<tr>
								<td class="txtlibform" nowrap><%=resource.getString("ForeignKeyColumns")%> : </td>
								<td colspan="<%=colspan%>"><%
								
	String selectedColumn;
	DbColumn currentColumn;
	String columnInfo;
	String columnName;
	int selectedColumnsCount = selectedColumns.length;
	for (int i = 0; i < indexInfoMaxColumnsCount; i++)
	{
		selectedColumn = (i < selectedColumnsCount ? selectedColumns[i] : "");

								  %><select name="column_<%=i%>"<%if (i >= selectedColumnsCount) {%>style="display: none"<%}%>><%

		if (selectedColumn == null || selectedColumn.length() == 0)
		{
%>
										<option value="" selected></option><%

		}
	
		for (int j = 0; j < columnsCount; j++)
		{
			currentColumn = columns[j];
			columnName = currentColumn.getName();
			columnInfo = currentColumn.getInfo(dataTypeList);
%>
										<option value="<%=columnName%>" <%if (columnName.equals(selectedColumn)) {%>selected<%}%>><%=columnInfo%></option><%

		}
%>
									</select>&nbsp;&nbsp;&nbsp;<%
									
	}
%>
								</td>
							</tr>
							<tr>
								<td class="txtlibform" nowrap width="30%"><%=resource.getString("ForeignKeyForeignTable")%> : </td>
								<td><select name="foreignTable" onchange="updateForeignColumns()"><%

	if (selectedForeignTable == null || selectedForeignTable.length() == 0)
	{
%>
										<option value="" selected></option><%

	}

	String tableName;
	for (int i = 0; i < tableCount; i++)
	{
		tableName = tableNames[i];
%>
										<option value="<%=tableName%>" <%if (tableName.equals(selectedForeignTable)) {%>selected<%}%>><%=tableName%></option><%

	}
%>
									</select>
								</td><%

	if (displayTableNameInput)
	{
%>
								<td width="50%"><input type="text" name="foreignTableInput" value=""/></td><%

	}
%>
							</tr>
							<tr>
								<td class="txtlibform" nowrap><%=resource.getString("ForeignKeyForeignColumns")%> : </td>
								<td colspan="<%=colspan%>"><select name="foreignColumnsNames" onchange="updateColumnsLists()">
										<option value=""<%if (selectedForeignColumnsNames.length == 0) {%> selected<%}%>></option><%

	String selectedIndexInfoColumnsKey = DbUtil.getListAsKey(selectedForeignColumnsNames);
	String[] indexInfoColumns;
	String indexInfoColumnsKey;
	String indexInfoColumnsLabel;
	for (int i = 0, n = indexList.getIndexInfosCount(); i < n; i++)
	{
		indexInfoColumns = indexList.getIndexInfo(i).getColumns();
		indexInfoColumnsKey = DbUtil.getListAsKey(indexInfoColumns);
		indexInfoColumnsLabel = DbUtil.getListAsString(indexInfoColumns);
%>
										<option value="<%=indexInfoColumnsKey%>" <%if (indexInfoColumnsKey.equals(selectedIndexInfoColumnsKey)) {%>selected<%}%>><%=indexInfoColumnsLabel%></option><%

	}
%>
									</select>&nbsp;<a href="javascript:refreshForeignColumns()" id="refreshLink"><img src="<%=resource.getIcon("myDB.refreshForeignColumns")%>" width="15" height="15" border="0" alt="<%=resource.getString("RefreshForeignColumns")%>" title="<%=resource.getString("RefreshForeignColumns")%>"/></a>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
<%
	if (foreignColumnsCount > 0)
	{
%>
			<br>
			<div id="foreignColumnsDiv">
<%
		ArrayPane arrayPane = gef.getArrayPane("Columns", MyDBConstants.ACTION_UPDATE_TABLE, request, session);
		arrayPane.setSortable(false);
		arrayPane.setVisibleLineNumber(-1);
		arrayPane.setTitle(MessageFormat.format(
			resource.getString("ForeignKeyTableForeignColumns"), new String[] {selectedForeignTable}));
		
		String[] columnsTitles = {"ColumnName", "ColumnType", "ColumnSize"};
		for (int i = 0, n = columnsTitles.length; i < n; i++)
		{
			arrayPane.addArrayColumn(resource.getString(columnsTitles[i])).setWidth("33%");
		}
		
		List linkedColumns = Arrays.asList(selectedForeignColumnsNames);
		DbColumn column;
		for (int i = 0; i < foreignColumnsCount; i++)
		{
			column = foreignColumns[i];
			ArrayLine arrayLine = arrayPane.addArrayLine();
			if (linkedColumns.contains(column.getName()))
			{
				arrayLine.setStyleSheet("ArrayCell linkedColumn");
			}
			arrayLine.addArrayCellText(column.getName());
			arrayLine.addArrayCellText(dataTypeList.getDataTypeName(column.getDataType()));
			arrayLine.addArrayCellText(column.getDataSizeAsString());
		}
		
		out.println(arrayPane.print());
%>
			</div>
<%
	}
%>
		</center>
	</form>
<%
	if (tableManager.hasErrorLabel())
	{
%>
	<br>
	<center>
		<span class="MessageReadHighPriority"><%=tableManager.getErrorLabel()%></span>
	</center>
	<br>
<%
	}
%>
	<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonValidate"), "javascript:validate();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:cancelForeignKey();", false));
	out.print(buttonPane.print());
%>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>