<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@ include file="init.jsp" %>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
	
	TableManager tableManager = myDBSC.getTableManager();
	DataTypeList dataTypeList = tableManager.getDataTypeList();
	DataType[] dataTypes = dataTypeList.getDataTypes();
	DataType dataType;
	
	DbColumn column = null;
	int index = Integer.parseInt(request.getParameter("index"));
	boolean displayErrorColumn = ("true".equals(request.getAttribute("errorColumn")));
	if (displayErrorColumn)
	{
		column = tableManager.getErrorColumn();
	}
	else if (index != -1)
	{
		column = tableManager.getTable().getColumn(index);
	}

	String columnName = "";
	int columnType = DbColumn.DEFAULT_DATA_TYPE;
	String columnSize = "";
	boolean columnNotNull = false;
	boolean forceColumnNotNull = false;
	boolean forceColumnNoDefaultValue = false;
	String columnDefaultValue = "";
	boolean isPK = false;
	boolean isUK = false;
	boolean isFK = false;
	if (column != null)
	{
		columnName = column.getName();
		columnType = column.getDataType();
		columnSize = column.getDataSizeAsString();
		columnNotNull = !column.isNullable();
		isPK = tableManager.getPrimaryKey().isPrimaryKey(columnName);
		isUK = tableManager.getUnicityKeys().isUnicityKey(columnName);
		isFK = tableManager.getForeignKeys().isForeignKey(columnName);
		forceColumnNotNull = (isPK || isUK);
		forceColumnNoDefaultValue = (isUK || isFK);
		if (!forceColumnNoDefaultValue)
		{
			columnDefaultValue = column.getDefaultValueAsString();
		}
	}
	DataType currentDataType = dataTypeList.get(columnType);
%>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/tableColumn.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript">
		function init()
		{
			form = document.forms["columnForm"];<%

	for (int i = 0, n = dataTypes.length; i < n; i++)
	{
		dataType = dataTypes[i];
%>
			dataTypes[<%=i%>] = new DataType("<%=dataType.getName()%>", <%=dataType.getSqlType()%>, <%=dataType.isSizeEnabled()%>);<%

	}
	if (columnName.length() == 0)
	{
%>
			form.elements["columnName"].focus();<%

	}
%>
		}

		function validate()
		{
			removeAccents(form.elements["columnName"]);
			var columnName = form.elements["columnName"].value;
			
			if (columnName == "")
			{
				form.elements["columnName"].focus();
				alert("<%=resource.getString("ErrorColumnNameRequired")%>");
			}
			else
			{
				if (!isSqlValidName(columnName))
				{
					form.elements["columnName"].focus();
					alert("<%=resource.getString("ErrorColumnNameRegExp")%>");
				}
				else if (form.elements["columnType"].value == "<%=DbColumn.DEFAULT_DATA_TYPE%>")
				{
					form.elements["columnType"].focus();
					alert("<%=resource.getString("ErrorColumnTypeRequired")%>");
				}
				else
				{
					var columnSize = form.elements["columnSize"].value;
					if (columnSize != "" && ("" + parseInt(columnSize)) != columnSize)
					{
						form.elements["columnSize"].focus();
						alert("<%=resource.getString("ErrorColumnSizeFormat")%>");
					}
					else
					{
						var columnNotNullInput = form.elements["columnNotNull"];
						columnNotNullInput.value = (columnNotNullInput.checked ? "true" : "false");
						window.opener.validateColumn();
					}
				}
			}
		}
	</script>
</head>

<body onload="init()" marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(null);
	browseBar.setComponentName(null);
	browseBar.setExtraInformation(resource.getString(
		index == -1 ? "PageTitleColumnCreation" : "PageTitleColumnModification"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<form name="columnForm">
		<input type="hidden" name="index" value="<%=index%>"/>
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform"><%=resource.getString("ColumnName")%> :</td>
								<td><input type="text" name="columnName" value="<%if (columnName != null) {%><%=columnName%><%}%>" maxlength="64" onkeyup="removeAccents(this)"/>&nbsp;
									<img src="<%=Util.getIcon("mandatoryField")%>" border="0" width="5" height="5"/>
								</td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("ColumnType")%> :</td>
								<td><select name="columnType" onchange="updateFields()"><%

	if (columnType == DbColumn.DEFAULT_DATA_TYPE)
	{
%>
										<option value="<%=DbColumn.DEFAULT_DATA_TYPE%>" selected></option><%

	}
	
	int type;
	for (int i = 0, n = dataTypes.length; i < n; i++)
	{
		dataType = dataTypes[i];
		type = dataType.getSqlType();
%>
										<option value="<%=type%>"<%if (columnType == type) {%> selected<%}%>><%=dataType.getName()%></option><%

	}
%>
									</select>&nbsp;
									<img src="<%=Util.getIcon("mandatoryField")%>" border="0" width="5" height="5"/>
								</td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("ColumnSize")%> :</td>
								<td><input type="text" name="columnSize" value="<%=columnSize%>"<%if (currentDataType == null || !currentDataType.isSizeEnabled()) {%> disabled<%}%>/></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("ColumnNotNull")%> :</td>
								<td><input type="checkbox" name="columnNotNull" value="<%=columnNotNull%>"<%if (columnNotNull) {%> checked<%} if (forceColumnNotNull) {%> disabled<%}%>/><%

	if (forceColumnNotNull)
	{
								  %>&nbsp;(<%=resource.getString(isPK ? "DefinedAsPrimaryKey" : "DefinedAsUnicityKey")%>)<%

	}
							  %></td>
							</tr>
							<tr>
								<td class="txtlibform"><%=resource.getString("ColumnDefaultValue")%> :</td>
								<td><input type="text" name="columnDefaultValue" value="<%=columnDefaultValue%>"<%if (forceColumnNoDefaultValue) {%> disabled<%}%>/><%
								
	if (forceColumnNoDefaultValue)
	{
								  %>&nbsp;(<%=resource.getString(isUK ? "DefinedAsUnicityKey" : "DefinedAsForeignKey")%>)<%
								  
	}
							  %></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</center>
	</form>
<%

	ForeignKey[] linkedForeignKeys = tableManager.getForeignKeys().getList(columnName);
	if (linkedForeignKeys.length > 0)
	{
%>
	<br>
<%
		ArrayPane arrayPane = gef.getArrayPane("Foreign Keys", MyDBConstants.ACTION_UPDATE_TABLE, request, session);
		arrayPane.setSortable(false);
		arrayPane.setVisibleLineNumber(-1);
		arrayPane.setTitle(resource.getString("LinkedForeignKeys"));
		String[] columnsTitles = {"LinkedForeignKeysName", "LinkedForeignKeysTable", "LinkedForeignKeysColumn",
			"LinkedForeignKeysType", "LinkedForeignKeysSize"};
		for (int i = 0, n = columnsTitles.length; i < n; i++)
		{
			arrayPane.addArrayColumn(resource.getString(columnsTitles[i]));
		}
		
		ForeignKey linkedForeignKey;
		DbColumn linkedForeignKeyColumn;
		for (int i = 0, n = linkedForeignKeys.length; i < n; i++)
		{
			linkedForeignKey = linkedForeignKeys[i];
			linkedForeignKeyColumn = linkedForeignKey.getLinkedForeignColumn(columnName);
			ArrayLine arrayLine = arrayPane.addArrayLine();
			arrayLine.addArrayCellText(linkedForeignKey.getName());
			arrayLine.addArrayCellText(linkedForeignKey.getForeignTable());
			arrayLine.addArrayCellText(linkedForeignKeyColumn.getName());
			arrayLine.addArrayCellText(dataTypeList.getDataTypeName(linkedForeignKeyColumn.getDataType()));
			arrayLine.addArrayCellText(linkedForeignKeyColumn.getDataSizeAsString());
		}
		
		out.println(arrayPane.print());
%>
	<br>
<%
	}

	if (displayErrorColumn)
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
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:window.close();", false));
	out.print(buttonPane.print());
%>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>