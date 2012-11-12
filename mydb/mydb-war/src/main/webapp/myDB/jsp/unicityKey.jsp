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

<%@ include file="init.jsp" %>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());

	TableManager tableManager = myDBSC.getTableManager();
	UnicityKeys unicityKeys = tableManager.getUnicityKeys();
	DbColumn[] columns = tableManager.getTable().getColumns();
	int columnsCount = columns.length;

	int index = Integer.parseInt((String)request.getAttribute("index"));
	UnicityKey unicityKey = (UnicityKey)request.getAttribute("unicityKey");
	if (unicityKey == null && index != -1)
	{
		unicityKey = unicityKeys.get(index);
	}
	String unicityKeyName = (unicityKey != null ? unicityKey.getName() : unicityKeys.getConstraintName());
	
	StringBuffer nullableColumnsSb = new StringBuffer();
	StringBuffer defaultValueColumnsSb = new StringBuffer();
	DbColumn column;
	for (int i = 0; i < columnsCount; i++)
	{
		column = columns[i];
		if (column.isNullable())
		{
			if (nullableColumnsSb.length() > 0)
			{
				nullableColumnsSb.append(", ");
			}
			nullableColumnsSb.append("\"").append(column.getName()).append("\"");
		}
		if (column.hasDefaultValue())
		{
			if (defaultValueColumnsSb.length() > 0)
			{
				defaultValueColumnsSb.append(", ");
			}
			defaultValueColumnsSb.append("\"").append(column.getName()).append("\"");
		}
	}
%>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript">
		var nullableColumns = new Array(<%=nullableColumnsSb.toString()%>);
		var defaultValueColumns = new Array(<%=defaultValueColumnsSb.toString()%>);
	
		function validate()
		{
			var form = document.forms["processForm"];
			form.elements["command"].value = "validateUK";
			removeAccents(form.elements["name"]);
			var name = form.elements["name"].value;
			if (name == "")
			{
				alert("<%=resource.getString("ErrorUnicityKeyNameRequired")%>");
			}
			else
			{
				if (!isSqlValidName(name))
				{
					alert("<%=resource.getString("ErrorUnicityKeyNameRegExp")%>");
				}
				else
				{
					var i = 0;
					var input;
					var ukEmpty = true;
					while (i < form.elements.length && ukEmpty)
					{
						input = form.elements[i];
						if (input.name.indexOf("<%=UnicityKey.UNICITY_KEY_PREFIX%>") != 1 && input.checked)
						{
							ukEmpty = false;
						}
						i++;
					}
					if (ukEmpty)
					{
						alert("<%=resource.getString("ErrorUnicityKeyEmpty")%>");
					}
					else
					{
						var nullableColumnInUK = false;
						for (i = 0; i < nullableColumns.length ; i++)
						{
							if (form.elements["<%=UnicityKey.UNICITY_KEY_PREFIX%>" + nullableColumns[i]].checked)
							{
								nullableColumnInUK = true;
							}
						}
						var defaultValueColumnInUK = false;
						for (i = 0; i < defaultValueColumns.length ; i++)
						{
							if (form.elements["<%=UnicityKey.UNICITY_KEY_PREFIX%>" + defaultValueColumns[i]].checked)
							{
								defaultValueColumnInUK = true;
							}
						}
						submitForm(nullableColumnInUK, defaultValueColumnInUK);
					}
				}
			}
		}

		function submitForm(nullableColumn, defaultValueColumn)
		{
			var continueProcess = true;
			if (nullableColumn || defaultValueColumn)
			{
				var warning = new Warning("<%=resource.getString("Warning")%>", "<%=resource.getString("WarningConfirm")%>");
				if (nullableColumn)
				{
					addWarningDetail(warning, "<%=resource.getString("WarningNullableColumnInUnicityKey")%>");
				}
				if (defaultValueColumn)
				{
					addWarningDetail(warning, "<%=resource.getString("WarningDefaultValueColumnInUnicityKey")%>");
				}
				continueProcess = confirm(displayWarning(warning));
			}
			if (continueProcess)
			{
				document.forms["processForm"].submit();
			}
		}

		function cancelUnicityKey()
		{
			document.forms["processForm"].elements["command"].value = "";
			document.forms["processForm"].submit();
		}
	</script>
</head>

<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setExtraInformation(resource.getString(
		index == -1 ? "PageTitleUnicityKeyCreation" : "PageTitleUnicityKeyModification"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<form name="processForm" action="<%=MyDBConstants.ACTION_UPDATE_TABLE%>" method="post" onsubmit="return false;">
		<input type="hidden" name="command" value=""/>
		<input type="hidden" name="index" value="<%=index%>"/>
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform"><%=resource.getString("UnicityKeyName")%> :</td>
								<td><input type="text" name="name" size="50" maxlength="64" value="<%=unicityKeyName%>" onkeyup="removeAccents(this)"/></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<br>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
<%
	String columnName;
	for (int i = 0; i < columnsCount; i++)
	{
		columnName = columns[i].getName();
%>
							<tr>
								<td><input type="checkbox" name="<%=UnicityKey.UNICITY_KEY_PREFIX%><%=columnName%>" size="50" maxlength="64" value="true"<%if (unicityKey != null && unicityKey.containsColumn(columnName)) {%> checked<%}%>/></td>
								<td class="txtlibform">&nbsp;<%=columnName%></td>
							</tr>
<%
	}
%>
						</table>
					</td>
				</tr>
			</table>
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
<%
	}
%>
	<br>
	<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonValidate"), "javascript:validate();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:cancelUnicityKey();", false));
	out.print(buttonPane.print());
%>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>