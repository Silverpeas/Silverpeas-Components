<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ include file="init.jsp" %>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
	<link rel="stylesheet" type="text/css" href="<%=applicationURL%>/myDB/jsp/styleSheet/consultation.css">
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/consultation.js"></script>
	<script type="text/javascript" src="<%=applicationURL%>/myDB/jsp/javaScript/util.js"></script>
	<script type="text/javascript">
	$(document).ready(function() {
		init();
	});
	</script>
</head>

<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	DbTable dbTable = myDBSC.getDbTable();
	StringBuffer pageTitleSb = new StringBuffer(resource.getString("PageTitleConsultation"));	
	if (dbTable != null)
	{
		pageTitleSb.append(" : ").append(resource.getString("Table")).append(" ").append(dbTable.getName());
	}
	browseBar.setExtraInformation(pageTitleSb.toString());
	
	if (userRoleLevel.equals("admin"))
	{
		String tableAction = MyDBConstants.ACTION_UPDATE_TABLE
			+ "?command=init"
			+ "&mode=" + TableManager.MODE_CREATION
			+ "&originPage=" + MyDBConstants.PAGE_CONSULTATION;
		operationPane.addOperation(
			resource.getIcon("myDB.addTable"), resource.getString("OperationCreateTable"), tableAction);
	    if (dbTable != null)
	    {
	    	operationPane.addOperation(resource.getIcon("myDB.addRecord"), resource.getString("OperationAddRecord"),
	    		MyDBConstants.ACTION_ADD_LINE);
	    }
	}

	out.println(window.printBefore());
	
	// Current page tab index.
	int tabIndex = 1;
%>
	<%@ include file="tabs.jsp" %>
<%
	out.println(frame.printBefore());

	if (dbTable != null)
	{
		String[] columnsNames = dbTable.getColumnsNames();
		int n = columnsNames.length;
		
		DbFilter dbFilter = myDBSC.getDbFilter();
		String filterColumn = dbFilter.getColumn();
		String filterCompare = dbFilter.getCompare();
		String filterValue = dbFilter.getValue();
		String column;
		String compare;
		String value;
%>
	<form name="filterForm" action="<%=MyDBConstants.ACTION_FILTER%>" method="POST">
		<center>
			<table cellpadding="0" cellspacing="0" border="0" width="98%" bgcolor="000000">
				<tr>
					<td>
						<table cellpadding="2" cellspacing="1" border="0" width="100%">
							<tr>
								<td class="intfdcolor" align="center" nowrap height="24">
									<span class="selectNS">
										<select name="filterColumn" size="1">
											<option value="<%=DbFilter.ALL%>" <%if (DbFilter.ALL.equals(filterColumn)) {%>selected<%}%>><%=resource.getString("SelectAll")%></option><%

		for (int i = 0; i < n; i++)
		{
			column = columnsNames[i];
%>
											<option value="<%=column%>" <%if (column.equals(filterColumn)) {%>selected<%}%>><%=column%></option><%

		}
%>
										</select>
									</span>
								</td>
								<td class="intfdcolor" align="center" nowrap height="24">
									<span class="selectNS">
										<select name="filterCompare" size="1">
											<option value="<%=DbFilter.ALL%>" <%if (DbFilter.ALL.equals(filterCompare)) {%>selected<%}%>><%=resource.getString("SelectAll")%></option>
											<option value="<%=DbFilter.CONTAINS%>" <%if (DbFilter.CONTAINS.equals(filterCompare)) {%>selected<%}%>><%=resource.getString("SelectContains")%></option><%

		for (int i = 0, m = DbFilter.COMPARES_SYMBOLS.length; i < m; i++)
		{
			compare = DbFilter.COMPARES_SYMBOLS[i];
%>
											<option value="<%=compare%>" <%if (compare.equals(filterCompare)) {%>selected<%}%>><%=compare%></option><%

		}
					  
%>
										</select>
									</span>
								</td>
								<td class="intfdcolor" align="center" nowrap height="24">
									<span class="selectNS">&nbsp;<%=resource.getString("Value")%> : <input type="text" name="filterValue" size="30" value="<%=filterValue%>"></span>&nbsp;
								</td>
								<td class="intfdcolor">
<%
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("Ok"), "javascript:filterData()", false));
		out.println(buttonPane.print());
%>
								</td>
							</tr>
						</table>
					</td>
					<td width="100%" class="intfdcolor51"></td>
				</tr>
			</table>
		</center>
	</form>
	<br>
	<form name="processFormTest" action="<%=MyDBConstants.ACTION_MAIN%>">
<%
		boolean userPublisherOrAdmin = (userRoleLevel.equals("publisher") || userRoleLevel.equals("admin"));
		ArrayPane arrayPane = gef.getArrayPane("ResultSet", MyDBConstants.ACTION_MAIN, request, session);
		arrayPane.setVisibleLineNumber(15);
		DbColumn dbColumn;
		int[] columnsTypes = new int[n];
		for (int i = 0; i < n; i++)
		{
			arrayPane.addArrayColumn(columnsNames[i]);
			columnsTypes[i] = dbTable.getColumn(i).getDataType();
		}
		// Action column.
		arrayPane.addArrayColumn("&nbsp;").setWidth(userPublisherOrAdmin ? "40" : "15");
		DbLine[] dbLines = dbTable.getLines();
		DbLine dbLine;
		String dbValue;
		int dbValueMaxLengthDisplayed = 20;
		for (int i = 0, m = dbLines.length; i < m; i++)
		{
			dbLine = dbLines[i];
			ArrayLine arrayLine = arrayPane.addArrayLine();
			for (int j = 0; j < n; j++)
			{
				dbValue = dbLine.getData(columnsNames[j]);
				ArrayCellText text = arrayLine.addArrayCellText(dbValue.length() > dbValueMaxLengthDisplayed
					? dbValue.substring(0, dbValueMaxLengthDisplayed) + "..." : dbValue);
				switch (columnsTypes[j])
				{
					case Types.INTEGER :
						text.setCompareOn(new Integer(dbValue.length() > 0 ? dbValue : "0"));
						break;
					case Types.DOUBLE :
						text.setCompareOn(new Double(dbValue.length() > 0 ? dbValue : "0"));
						break;
					case Types.FLOAT :
						text.setCompareOn(new Float(dbValue.length() > 0 ? dbValue : "0"));
						break;
					case Types.DATE :
						text.setCompareOn(dbValue.length() > 0
							? myDBSC.getDateFormatter().stringToFormString(dbValue) : "");
						break;
					default :
						text.setCompareOn(dbValue);
						break;
				}
			}
			
			// Action cell.
			IconPane iconPane = gef.getIconPane();
			if (userPublisherOrAdmin)
			{
	            iconPane.setSpacing("10px");
                Icon updateIcon = iconPane.addIcon();
                updateIcon.setProperties(resource.getIcon("myDB.updateLine"),
                	resource.getString("Update"), "javascript:detail(" + i + ", 'modify')");
                Icon deleteIcon = iconPane.addIcon();
                deleteIcon.setProperties(resource.getIcon("myDB.deleteLine"),
                	resource.getString("Delete"), "javascript:detail(" + i + ", 'delete')");
	            				
			}
			else
			{
                Icon viewIcon = iconPane.addIcon();
                viewIcon.setProperties(resource.getIcon("myDB.viewLine"),
                	resource.getString("Detail"), "javascript:detail(" + i + ", 'view')");
			}
			arrayLine.addArrayCellIconPane(iconPane);
		}
%>
		<div id="tableDiv">
<%
		out.println(arrayPane.print());
%>
		</div>
<%
		String errorMessage = (String)request.getAttribute("errorMessage");
		if (errorMessage != null)
		{
%>
		<br>
		<center>
			<span class="MessageReadHighPriority"><%=errorMessage%></span>
		</center>
<%
		}
%>
	</form>
<%
	}
	else
	{
%>
	<br>
	<center>
		<span class="MessageNotRead"><%=resource.getString("NoDataAvailable")%></span>
	</center>
<%
	}

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
	<form name="processForm" action="">
		<input name="index" type="hidden" value=""/>
		<input name="command" type="hidden" value=""/>
	</form>
</body>
</html>