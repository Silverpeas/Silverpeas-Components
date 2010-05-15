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
	PrimaryKey primaryKey = tableManager.getPrimaryKey();
	UnicityKeys unicityKeys = tableManager.getUnicityKeys();
	ForeignKeys foreignKeys = tableManager.getForeignKeys();
	ForeignKeyError[] fkErrors = foreignKeys.getErrors(dataTypeList, resource);
	DbTable table = tableManager.getTable();
	DbColumn[] columns = table.getColumns();
	
	int columnsCount = columns.length;
	
	String errorIndex = "";
	boolean displayErrorColumn = ("true".equals(request.getAttribute("errorColumn")));
	if (displayErrorColumn)
	{
		errorIndex = (String)request.getAttribute("index");
	}
	
	String[][] keysImpacts = tableManager.getKeysImpacts();
%>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/tableUpdate.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/util/javaScript/animation.js"></script>
	<script type="text/javascript">
		function init()
		{
			updateTableAction = "<%=MyDBConstants.ACTION_UPDATE_TABLE%>";
			form = document.forms["processForm"];<%
		
	for (int i = 0, n = keysImpacts.length; i < n; i++)
	{
%>
			keysImpacts["<%=i%>"] = new Array("<%=keysImpacts[i][0]%>", "<%=keysImpacts[i][1]%>", "<%=keysImpacts[i][2]%>");<%

	}			
	if (displayErrorColumn)
	{
%>
			openErrorColumnWindow(<%=errorIndex%>);<%
			
	}
%>
		}
		
		function validate()
		{ 
			removeAccents(form.elements["tableName"]);
			var tableName = form.elements["tableName"].value;
			
			if (tableName == "")
			{
				alert("<%=resource.getString("ErrorTableNameRequired")%>");
			}
			else
			{			
				if (!isSqlValidName(tableName))
				{
					alert("<%=resource.getString("ErrorTableNameRegExp")%>");
				}
				else
				{<%
			
	if (columnsCount > 0)
	{
		if (tableManager.getPrimaryKey().isEmpty())
		{
%>
					alert("<%=resource.getString("ErrorPrimaryKeyRequired")%>");<%

		}
		else
		{
			if (fkErrors.length > 0)
			{
%>
					alert("<%=resource.getString("ErrorForeignKeyErrors")%>");<%

			}
			else
			{
%>
					form.elements["command"].value = "update";
					form.submit();<%

			}
		}
	}
	else
	{
%>
					alert("<%=resource.getString("ErrorColumnRequired")%>");<%
				
	}
%>
				}
			}
		}

		function updateColumn(index, update)
		{
			if (update)
			{
				var url = updateTableAction
					+ "?command=displayColumn"
					+ "&index=" + index
					+ "&tableName=" + form.elements["tableName"].value;
				openColumnWindow(url);
			}
			else
			{
				var removeColumn = true;
				var modifiedKeys = keysImpacts[index][1];
				var removedKeys = keysImpacts[index][2];
				if (modifiedKeys != "" || removedKeys != "")
				{
					var startLabel = "<%=resource.getString("KeysImpactsWarning")%>";
					startLabel = startLabel.replace("{0}", keysImpacts[index][0]);
					var warning = new Warning(startLabel, "<%=resource.getString("WarningConfirm")%>");
					if (modifiedKeys != "")
					{
						addWarningDetail(warning, "<%=resource.getString("KeysImpactsModification")%> " + modifiedKeys);
					}
					if (removedKeys != "")
					{
						addWarningDetail(warning, "<%=resource.getString("KeysImpactsDeletion")%> " + removedKeys);
					}
					removeColumn = confirm(displayWarning(warning));
				}
				if (removeColumn)
				{
					form.elements["command"].value = "removeColumn";
					form.elements["index"].value = index;
					form.submit();
				}
			}
		}
	</script>
</head>

<body onload="init()" onunload="closeColumnWindow()" marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	String browseBarLabel = "";
	switch (tableManager.getMode())
	{
		case TableManager.MODE_CREATION :
			browseBarLabel = resource.getString("PageTitleTableCreation");
			break;
		case TableManager.MODE_UPDATE :
			browseBarLabel = resource.getString("PageTitleTableModification");
			break;
	}
	String tableName = table.getName();
	if (tableName != null && tableName.length() > 0)
	{
		browseBarLabel += " : " + tableName;
	}

	browseBar.setExtraInformation(browseBarLabel);
	
	operationPane.addOperation(
		resource.getIcon("myDB.addColumn"), resource.getString("OperationAddColumn"), "javascript:updateColumn(-1, true)");
	if (columnsCount > 0)
	{
		operationPane.addOperation(
			resource.getIcon("myDB.primaryKey"),
			resource.getString(primaryKey.isEmpty() ? "OperationAddPrimaryKey" : "OperationModifyPrimaryKey"),
			"javascript:updatePrimaryKey(true)");
		operationPane.addOperation(
			resource.getIcon("myDB.unicityKey"), resource.getString("OperationAddUnicityKey"), "javascript:updateUnicityKey(-1, true)");
		operationPane.addOperation(
			resource.getIcon("myDB.foreignKey"), resource.getString("OperationAddForeignKey"), "javascript:updateForeignKey(-1, true)");
	}
	
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<form name="processForm" action="<%=MyDBConstants.ACTION_UPDATE_TABLE%>" method="post" onsubmit="return false;">
		<input type="hidden" name="command" value="update"/>
		<input type="hidden" name="index" value=""/>
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform"><%=resource.getString("TableName")%> :</td><%

	if (tableManager.isCreationMode())
	{
%>
								<td><input type="text" name="tableName" size="50" maxlength="64" value="<%=tableName%>" onkeyup="removeAccents(this)"/></td><%

	}
	else
	{
%>
								<td><input type="text" name="tableName" size="50" disabled value="<%=tableName%>"/></td><%

	}
%>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<br>
<%
	ArrayPane arrayPane = gef.getArrayPane("Columns", MyDBConstants.ACTION_UPDATE_TABLE, request, session);
	arrayPane.setSortable(false);
	arrayPane.setVisibleLineNumber(-1);
	arrayPane.setTitle(resource.getString("Columns"));
	
	String[] columnsTitles = {"ColumnName", "ColumnType", "ColumnSize", "ColumnNotNull", "ColumnDefaultValue"};
	for (int i = 0, n = columnsTitles.length; i < n; i++)
	{
		arrayPane.addArrayColumn(resource.getString(columnsTitles[i]));
	}
	if (columnsCount > 0)
	{
		arrayPane.addArrayColumn("&nbsp;").setWidth("40");
	}
	
	DbColumn column;
	for (int i = 0; i < columnsCount; i++)
	{
		column = columns[i];
		ArrayLine arrayLine = arrayPane.addArrayLine();
		arrayLine.addArrayCellText(column.getName());
		arrayLine.addArrayCellText(dataTypeList.getDataTypeName(column.getDataType()));
		arrayLine.addArrayCellText(column.getDataSizeAsString());
		arrayLine.addArrayCellText(resource.getString(column.isNullable() ? "No" : "Yes"));
		arrayLine.addArrayCellText(column.getDefaultValueAsString());
		
        IconPane iconPane = gef.getIconPane();
        Icon updateIcon = iconPane.addIcon();
        updateIcon.setProperties(resource.getIcon("myDB.updateColumn"),
        	resource.getString("Update"), "javascript:updateColumn(" + i + ", true)");
        iconPane.setSpacing("10px");
        Icon deleteIcon = iconPane.addIcon();
        deleteIcon.setProperties(resource.getIcon("myDB.deleteColumn"),
        	resource.getString("Delete"), "javascript:updateColumn(" + i + ", false)");
        arrayLine.addArrayCellIconPane(iconPane);
	}
	
	out.println(arrayPane.print());
	
	if (!primaryKey.isEmpty())
	{
%>
			<br>
<%
		ArrayPane pkArrayPane = gef.getArrayPane("Primary Key", MyDBConstants.ACTION_UPDATE_TABLE, request, session);
		pkArrayPane.setSortable(false);
		pkArrayPane.setVisibleLineNumber(-1);
		pkArrayPane.setTitle(resource.getString("PrimaryKey"));
		
		pkArrayPane.addArrayColumn(resource.getString("PrimaryKeyName"));
		pkArrayPane.addArrayColumn(resource.getString("PrimaryKeyColumns"));
		pkArrayPane.addArrayColumn("&nbsp;").setWidth("40");
		
		ArrayLine pkArrayLine = pkArrayPane.addArrayLine();
		pkArrayLine.addArrayCellText(primaryKey.getName());
		pkArrayLine.addArrayCellText(DbUtil.getListAsString(primaryKey.getColumns()));
		
        IconPane iconPane = gef.getIconPane();
        Icon updateIcon = iconPane.addIcon();
        updateIcon.setProperties(resource.getIcon("myDB.updatePrimaryKey"),
        	resource.getString("Update"), "javascript:updatePrimaryKey(true)");
        iconPane.setSpacing("10px");
        Icon deleteIcon = iconPane.addIcon();
        deleteIcon.setProperties(resource.getIcon("myDB.deletePrimaryKey"),
        	resource.getString("Delete"), "javascript:updatePrimaryKey(false)");
        pkArrayLine.addArrayCellIconPane(iconPane);
		
		out.println(pkArrayPane.print());
	}
	
	if (!unicityKeys.isEmpty())
	{
%>
			<br>
<%
		ArrayPane ukArrayPane = gef.getArrayPane("Unicity Keys", MyDBConstants.ACTION_UPDATE_TABLE, request, session);
		ukArrayPane.setSortable(false);
		ukArrayPane.setVisibleLineNumber(-1);
		ukArrayPane.setTitle(resource.getString("UnicityKeys"));
		
		ukArrayPane.addArrayColumn(resource.getString("UnicityKeyName"));
		ukArrayPane.addArrayColumn(resource.getString("UnicityKeyColumns"));
		ukArrayPane.addArrayColumn("&nbsp;").setWidth("40");
		
		int unicityKeysCount = unicityKeys.getSize();
		UnicityKey unicityKey;
		for (int i = 0; i < unicityKeysCount; i++)
		{
			unicityKey = unicityKeys.get(i);
			
			ArrayLine ukArrayLine = ukArrayPane.addArrayLine();
			ukArrayLine.addArrayCellText(unicityKey.getName());
			ukArrayLine.addArrayCellText(DbUtil.getListAsString(unicityKey.getColumns()));
			
	        IconPane iconPane = gef.getIconPane();
	        Icon updateIcon = iconPane.addIcon();
	        updateIcon.setProperties(resource.getIcon("myDB.updateUnicityKey"),
	        	resource.getString("Update"), "javascript:updateUnicityKey(" + i + ", true)");
	        iconPane.setSpacing("10px");
	        Icon deleteIcon = iconPane.addIcon();
	        deleteIcon.setProperties(resource.getIcon("myDB.deleteUnicityKey"),
	        	resource.getString("Delete"), "javascript:updateUnicityKey(" + i + ", false)");
	        ukArrayLine.addArrayCellIconPane(iconPane);
		}
		
		out.println(ukArrayPane.print());
	}
	
	if (!foreignKeys.isEmpty())
	{
%>
			<br>
<%
		ArrayPane fkArrayPane = gef.getArrayPane("Foreign Keys", MyDBConstants.ACTION_UPDATE_TABLE, request, session);
		fkArrayPane.setSortable(false);
		fkArrayPane.setVisibleLineNumber(-1);
		fkArrayPane.setTitle(resource.getString("ForeignKeys"));
		
		fkArrayPane.addArrayColumn(resource.getString("ForeignKeyName"));
		fkArrayPane.addArrayColumn(resource.getString("ForeignKeyColumns"));
		fkArrayPane.addArrayColumn(resource.getString("ForeignKeyForeignTable"));
		fkArrayPane.addArrayColumn(resource.getString("ForeignKeyForeignColumns"));
		fkArrayPane.addArrayColumn("&nbsp;").setWidth("40");
		
		int foreignKeysCount = foreignKeys.getSize();
		ForeignKey foreignKey;
		String[] columnNames;
		DbColumn[] foreignKeyColumns;
		DbColumn foreignKeyColumn;
		StringBuffer foreignKeyColumnsNames;
		StringBuffer foreignKeyColumnsInfos;
		for (int i = 0; i < foreignKeysCount; i++)
		{
			foreignKey = foreignKeys.get(i);
			columnNames = foreignKey.getColumns();
			foreignKeyColumnsNames = new StringBuffer();
			for (int j = 0, m = columnNames.length; j < m; j++)
			{
				if (j > 0)
				{
					foreignKeyColumnsNames.append("<br>");
				}
				foreignKeyColumnsNames.append(columnNames[j]);
			}
			foreignKeyColumns = foreignKey.getForeignColumns();
			foreignKeyColumnsInfos = new StringBuffer();
			for (int j = 0, m = foreignKeyColumns.length; j < m; j++)
			{
				foreignKeyColumn = foreignKeyColumns[j];
				if (j > 0)
				{
					foreignKeyColumnsInfos.append("<br>");
				}
				foreignKeyColumnsInfos.append(foreignKeyColumn.getInfo(dataTypeList));
			}
			
			ArrayLine fkArrayLine = fkArrayPane.addArrayLine();
			fkArrayLine.addArrayCellText(foreignKey.getName());
			fkArrayLine.addArrayCellText(foreignKeyColumnsNames.toString());
			fkArrayLine.addArrayCellText(foreignKey.getForeignTable());
			fkArrayLine.addArrayCellText(foreignKeyColumnsInfos.toString());
			
	        IconPane iconPane = gef.getIconPane();
	        Icon updateIcon = iconPane.addIcon();
	        updateIcon.setProperties(resource.getIcon("myDB.updateForeignKey"),
	        	resource.getString("Update"), "javascript:updateForeignKey(" + i + ", true)");
	        iconPane.setSpacing("10px");
	        Icon deleteIcon = iconPane.addIcon();
	        deleteIcon.setProperties(resource.getIcon("myDB.deleteForeignKey"),
	        	resource.getString("Delete"), "javascript:updateForeignKey(" + i + ", false)");
	        fkArrayLine.addArrayCellIconPane(iconPane);
		}
		
		out.println(fkArrayPane.print());
	}
%>
		</center>
	</form>
<%
	if (fkErrors.length > 0)
	{
%>
	<br>
	<form name="fkErrorForm" action="<%=MyDBConstants.ACTION_UPDATE_TABLE%>" method="post">
		<input type="hidden" name="command" value="modifyColumn"/>
		<input type="hidden" name="columnName" value=""/>
		<input type="hidden" name="type" value=""/>
		<input type="hidden" name="correctedValue" value=""/>
		<center>
			<table cellpadding="5" cellspacing="0" border="0" width="98%" class="intfdcolor4">
				<tr>
					<td colspan="2" class="txtlibform"><%=resource.getString("TableCreationWarning")%></td>
				</tr><%

		ForeignKeyError fkError;
		String fkErrorColumn;
		int fkErrorType;
		String fkErrorTitle;
		for (int i = 0, n = fkErrors.length; i < n; i++)
		{
			fkError = fkErrors[i];
			fkErrorColumn = fkError.getColumn();
			fkErrorType = fkError.getType();
			switch (fkErrorType)
			{
				case ForeignKeyError.ERROR_TYPE :
					fkErrorTitle = MessageFormat.format(
						resource.getString("ModifyColumnType"), new String[] {fkErrorColumn});
					break;
				case ForeignKeyError.ERROR_SIZE :
					fkErrorTitle = MessageFormat.format(
						resource.getString("ModifyColumnSize"), new String[] {fkErrorColumn});
					break;
				default :
					fkErrorTitle = "";
					break;
			}
%>
				<tr>
					<td width="20" align="center"><img src="<%=resource.getIcon("myDB.errorBulet")%>" border="0" width="15" height="15"/></td>
					<td class="txtlibform"><%=fkError.getLabel()%>&nbsp;<img align="top" src="<%=resource.getIcon("myDB.correctionArrow")%>" border="0" width="13" height="13"/>
						<a class="txtlibform" href="javascript:modifyColumn('<%=fkErrorColumn%>', <%=fkErrorType%>, '<%=fkError.getCorrectedValue()%>')" alt="<%=fkErrorTitle%>" title="<%=fkErrorTitle%>"><i><%=resource.getString("ModifyColumn")%></i></a></td>
				</tr><%

		}
%>
			</table>
		</center>
	</form>
<%
	}

	if (!displayErrorColumn && tableManager.hasErrorLabel())
	{
%>
	<br>
	<center>
		<span class="MessageReadHighPriority"><%=tableManager.getErrorLabel()%></span>
	</center>
<%
	}
%>
	<br>
	<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonValidate"), "javascript:validate();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:cancelUpdate();", false));
	out.print(buttonPane.print());
%>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>