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

<%@ include file="imports.jsp" %>
<%@ include file="init.jsp.inc" %>

<%
	
	String[] tables = null;
	String[] columns = null;
	String[] compares = null;
	String[] selectedColumns = null;
	
	
	String columnValueS = "";
	String column = "";
	String selectedColumn = "";
	String compare = "";
	String table = "";
	String action = null;
	String pass = null;
	
	boolean ok = false;
	String errorMessage = "";

	pass = request.getParameter("pass");
	action = request.getParameter("Action");
	String url = request.getParameter("JDBCurl");

	tables = (String[]) request.getParameterValues("table");
	columns = (String[]) request.getParameterValues("columns");
	compares = (String[]) request.getParameterValues("compares");
	columnValueS = request.getParameter("columnValue");
	selectedColumns = (String[]) request.getParameterValues("selectedColumns"); 


	if (tables != null)
	{
		table = tables[0];
		connecteurJDBC.setTable(table);
	}
	else table = connecteurJDBC.getTable();
	
	if (columns != null)
	{
		column = columns[0];
		connecteurJDBC.setColumnReq(column);
	}
	else column = connecteurJDBC.getColumnReq();

	if(selectedColumns != null)
	{
		selectedColumn = selectedColumns[0];
		connecteurJDBC.setSelected(selectedColumn);
	}
	else selectedColumn = connecteurJDBC.getSelected();

	if(compares != null)
	{
		compare = compares[0];
		connecteurJDBC.setCompare(compare);
	}
	else compare = connecteurJDBC.getCompare();

	if ((columnValueS != null) && (!columnValueS.equals(connecteurJDBC.getColumnValue())))
    {
		connecteurJDBC.setColumnValue(columnValueS);
    }
	else columnValueS = connecteurJDBC.getColumnValue();

	if (action.equals("setSQLReq"))
	{
		String SQLreq = request.getParameter("SQLReq");
				
		errorMessage = connecteurJDBC.checkRequest(SQLreq);		
		
		ok=(errorMessage==null);
		
		if (ok)
			connecteurJDBC.setValidRequest(SQLreq);	
		connecteurJDBC.setSQLreq(SQLreq);
			
	}
	
	if (action.equals("cancelSQLReq"))
	{
		connecteurJDBC.setSQLreq(connecteurJDBC.getLastValidRequest());
	}
	
	if(action.equals("cancelColumn"))
	{
		String temp = connecteurJDBC.getLastColumn();
		temp = temp.substring(temp.lastIndexOf(","));
	}

	// past the selected table to the SQL request
	if(action.equals("writeTable"))
	{
		String SQLreq = request.getParameter("SQLReq");
		SQLreq = "select * from "+table;  

		connecteurJDBC.setSQLreq(SQLreq);
	}
	
	// past the selected column to the SQL request
	if(action.equals("writeColumn"))
	{
		String SQLreq = request.getParameter("SqlReq");

		connecteurJDBC.setSQLreq(SQLreq);
		connecteurJDBC.setSelectedColumn(new Vector());
	}

	if(action.equals("end"))
	{
		connecteurJDBC.setLastColumn("");
	}

	if (action.equals("endCriter"))
	{
		connecteurJDBC.setSelectedColumn(new Vector());
	}

	if(action.equals("addCriter"))
	{
		String SQLreq = request.getParameter("SQLReq");
		try
		{
			if (SQLreq.indexOf("where") == -1)
			{
				SQLreq =  SQLreq+" where "+selectedColumn + " "+compare +" "+"'"+columnValueS+"'";
			}
			else
			{
				SQLreq =  SQLreq+" and "+selectedColumn + " "+compare +" "+"'"+columnValueS+"'";
			}
		}
		catch (NullPointerException e)
		{
			SilverTrace.warn("connecteurJDBC",
							  "JSPprocessForm", 
				              "connecteurJDBC.MSG_ADD_CRITER_TO_CLOSE_WHERE_FAIL", "request : "+SQLreq, e);
		}

		connecteurJDBC.setSQLreq(SQLreq);
	}

	
	String sender = request.getParameter("Sender");
	if (sender == null) {
		sender = "Main.jsp";
	}

%>



<%

if (!ok && errorMessage != null && errorMessage.length()>0)
{
%>
		<Script language="JavaScript">
			function processErrorMessage()
			{	
								
				errorWin=window.open("errorParam.jsp","errorWin", "width=400, height=130, alwayRaised,dependent");
				errorWin.moveTo(200,100);
				errorWin.focus();
				
				document.location.replace("<%=sender %>");

			}
				
			processErrorMessage();
			
		</Script>
		
<%}else {%>


	<jsp:forward page="<%= sender %>" />
<%}

%>	





