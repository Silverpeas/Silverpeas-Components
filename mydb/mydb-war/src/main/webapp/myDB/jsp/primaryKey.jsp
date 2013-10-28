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
	PrimaryKey primaryKey = (PrimaryKey)request.getAttribute("primaryKey");
	if (primaryKey == null)
	{
		primaryKey = tableManager.getPrimaryKey();
	}
	DbTable table = tableManager.getTable();
	DbColumn[] columns = table.getColumns();
	int columnsCount = columns.length;
	
	StringBuffer nullableColumnsSb = new StringBuffer();
	for (int i = 0; i < columnsCount; i++)
	{
		if (columns[i].isNullable())
		{
			if (nullableColumnsSb.length() > 0)
			{
				nullableColumnsSb.append(", ");
			}
			nullableColumnsSb.append("\"").append(columns[i].getName()).append("\"");
		}
	}
%>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript">
		var nullableColumns = new Array(<%=nullableColumnsSb%>);
		
		function validate()
		{
			var form = document.forms["processForm"];
			form.elements["command"].value = "validatePK";
			removeAccents(form.elements["name"]);
			var name = form.elements["name"].value;
			if (name == "")
			{
				alert("<%=resource.getString("ErrorPrimaryKeyNameRequired")%>");
			}
			else
			{
				if (!isSqlValidName(name))
				{
					alert("<%=resource.getString("ErrorPrimaryKeyNameRegExp")%>");
				}
				else
				{
					var i = 0;
					var input;
					var pkEmpty = true;
					while (i < form.elements.length && pkEmpty)
					{
						input = form.elements[i];
						if (input.name.indexOf("<%=PrimaryKey.PRIMARY_KEY_PREFIX%>") != 1 && input.checked)
						{
							pkEmpty = false;
						}
						i++;
					}
					if (pkEmpty)
					{
						alert("<%=resource.getString("ErrorPrimaryKeyEmpty")%>");
					}
					else
					{
						var continueProcess = true;
						var nullableColumnInPK = false;
						for (i = 0; i < nullableColumns.length ; i++)
						{
							if (form.elements["<%=PrimaryKey.PRIMARY_KEY_PREFIX%>" + nullableColumns[i]].checked)
							{
								nullableColumnInPK = true;
							}
						}
						if (nullableColumnInPK)
						{
							var warning = new Warning("<%=resource.getString("Warning")%>", "<%=resource.getString("WarningConfirm")%>");
							addWarningDetail(warning, "<%=resource.getString("WarningNullableColumnInPrimaryKey")%>");
							continueProcess = confirm(displayWarning(warning));
						}
						if (continueProcess)
						{
							form.submit();
						}
					}
				}
			}
		}

		function cancelPrimaryKey()
		{
			document.forms["processForm"].elements["command"].value = "";
			document.forms["processForm"].submit();
		}
	</script>
</head>

<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setExtraInformation(resource.getString("PageTitlePrimaryKey"));

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
	<form name="processForm" action="<%=MyDBConstants.ACTION_UPDATE_TABLE%>" method="post" onsubmit="return false;">
		<input type="hidden" name="command" value=""/>
		<center>
			<table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
				<tr>
					<td>
						<table cellpadding="5" cellspacing="0" border="0" width="100%" class="intfdcolor4">
							<tr>
								<td class="txtlibform"><%=resource.getString("PrimaryKeyName")%> :</td>
								<td><input type="text" name="name" size="50" maxlength="64" value="<%=primaryKey.getName()%>" onkeyup="removeAccents(this)"/></td>
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
								<td><input type="checkbox" name="<%=PrimaryKey.PRIMARY_KEY_PREFIX%><%=columnName%>" size="50" maxlength="64" value="true"<%if (primaryKey.isPrimaryKey(columnName)) {%> checked<%}%>/></td>
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
	buttonPane.addButton(gef.getFormButton(resource.getString("ButtonCancel"), "javascript:cancelPrimaryKey();", false));
	out.print(buttonPane.print());
%>
	</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>