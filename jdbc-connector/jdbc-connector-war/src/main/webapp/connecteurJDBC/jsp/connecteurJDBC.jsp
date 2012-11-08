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
String columnValueS = "";
String action ="";
String column = "";
String compare = "";
String col = "";
String sort ="";

int cpp =0;

String[] columns = null;
String[] compares = null;
String[] allColumns_rs = new String[1];
allColumns_rs[0] = connecteurJDBC.getString("comboTous");
String[] allCompares = {connecteurJDBC.getString("contient"), "=", "<=", ">=", "!=", ">", "<"};

col				= request.getParameter("colNumber");
columnValueS	= request.getParameter("columnValue");
action			= request.getParameter("Action");
sort			= request.getParameter("Sort");

columns			= (String[]) request.getParameterValues("columns");
compares		= (String[]) request.getParameterValues("compares");

String value = "";
String label = "";
String selected = "";

if(sort == null || sort.equals(""))
{
	sort = "desc";
}
else connecteurJDBC.setSortType(sort);


if (columns != null)
{
	column = columns[0];
	connecteurJDBC.setColumnReq(column);
}
else column = connecteurJDBC.getColumnReq();

if (compares != null)
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

if(col!=null && !col.equals(""))
{
	cpp = (new Integer(col)).intValue();
}

String SQLreq = connecteurJDBC.getSQLreq();
String lastValidSqlReq = connecteurJDBC.getFullRequest();

if(action != null)
{
	if (action.equals("newSQLReq"))
	{
	}
	else if(action.equals("sortColumn"))
	{
		int index = lastValidSqlReq.toLowerCase().indexOf("order by");
		if (index != -1) //** SCO ** d�j� une close order by
		{
			lastValidSqlReq = lastValidSqlReq.substring(0, index -1);
		}
		lastValidSqlReq =  lastValidSqlReq+ " ORDER BY "+connecteurJDBC.getColumnName(cpp) +" "+connecteurJDBC.getSortType();
      connecteurJDBC.setFullRequest(lastValidSqlReq);
	}
		
}
%>


<HTML>
<Head>
  <TITLE><%=connecteurJDBC.getString("windowTitleMain")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<Script language="JavaScript">

	function restrictResults()
	{
		document.sqlReqForm.Action.value = "newSQLReq";
		document.sqlReqForm.submit();
	}
	function doArrayPane(ResultSet, i)
	{
		document.sqlReqForm.Action.value = "sortColumn";
		document.sqlReqForm.colNumber.value= i;
		<% if(sort.equals("desc"))
		   {%>
				document.sqlReqForm.Sort.value = "asc";
		   <%}
		   else
		   {%>
			    document.sqlReqForm.Sort.value = "desc";
		<% } %>
		document.sqlReqForm.submit();
	}
</Script>
<BODY>

<%
	browseBar.setExtraInformation(connecteurJDBC.getString("titreExecution")) ;
	    
    if (flag.equals("publisher") || flag.equals("admin")) {
	    tabbedPane = gef.getTabbedPane();
			tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneConsultation"), "DoRequest", true);
    	tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneRequete"), "ParameterRequest",false );
	}
	
	if (flag.equals("admin"))
		tabbedPane.addTab(connecteurJDBC.getString("tabbedPaneParametresJDBC"),"ParameterConnection", false);

	Frame frame = gef.getFrame();

	out.println(window.printBefore());
	if (flag.equals("publisher") || flag.equals("admin")) {
		out.println(tabbedPane.print());
	}
	out.println(frame.printBefore());

	 //Tableau
	  ArrayPane arrayPane = gef.getArrayPane("ResultSet","",request,session);
	  arrayPane.setSortable(true);
	  arrayPane.setExportData(true);
	  arrayPane.setVisibleLineNumber(15);
	
	if (lastValidSqlReq!=null)
	{	
		connecteurJDBC.startConnection(lastValidSqlReq);	
	  
	  // Add the columns titles
	  allColumns_rs = new String[connecteurJDBC.getColumnCount()]; 
	  for (int i=1 ; i<=connecteurJDBC.getColumnCount() ; i++) {
		arrayPane.addArrayColumn(connecteurJDBC.getColumnName(i));
		allColumns_rs[i-1] = connecteurJDBC.getColumnName(i);
	  }
	  arrayPane.setSortable(true);
	
	  while (connecteurJDBC.getNext()) {
			// apply filter if needed
			boolean rowIsOK = false;
			if ( (column!=null) && (compare!=null) && (columnValueS!= null)
				&& (!"*".equals(column)) && (!"*".equals(compare)) && (!"".equals(columnValueS)) )
			{
				String theValue = connecteurJDBC.getString(Integer.parseInt(column));
				if (theValue == null)
					rowIsOK = false;
				else if ( connecteurJDBC.getString("contient").equals(compare) )
				{
					if (theValue!=null)
						rowIsOK = ( theValue.toLowerCase().indexOf(columnValueS.toLowerCase())!=-1 );
				}
				else
				{
					int compareResult = theValue.compareToIgnoreCase(columnValueS);

					if ( "!=".equals(compare) )
						rowIsOK =  (compareResult != 0);
					if ( "=".equals(compare) )
						rowIsOK = (compareResult == 0);
					if ( ">=".equals(compare) )
						rowIsOK = (compareResult >= 0);
					if ( "<=".equals(compare) )
						rowIsOK = (compareResult <= 0);
					if ( ">".equals(compare) )
						rowIsOK = (compareResult > 0);
					if ( "<".equals(compare) )
						rowIsOK = (compareResult < 0);
				}
			}
			else
				rowIsOK = true;

			if (rowIsOK)
			{
				ArrayLine arrayLine = arrayPane.addArrayLine() ;
				for (int i=1 ; i<=connecteurJDBC.getColumnCount() ; i++) {
					ArrayCellText champ = arrayLine.addArrayCellText(connecteurJDBC.getString(i));
					champ.setCompareOn(new String("NULL"));
					if (connecteurJDBC.getString(i) != null)
						champ.setCompareOn(connecteurJDBC.getString(i));
				}
			}
	  }
	  connecteurJDBC.closeConnection();
	}

 %>
<center> 
<table cellpadding=0 cellspacing=0 border=0 width="98%" bgcolor=000000>
          <tr> 
            <td> 
				<table cellpadding=2 cellspacing=1 border=0 width="100%" >
				<FORM name="sqlReqForm" action="connecteurJDBC.jsp" method="POST">
				  <input name="Sender" type="hidden" value = "connecteurJDBC.jsp" >
				  <input type="hidden" name="skip" value="0">
				  <input name="Action" type="hidden" >
				  <input name="colNumber" type="hidden" >
				  <input name="Sort" type="hidden" value="<%=connecteurJDBC.getSortType()%>">
				  <input name="SQLreq" type="hidden" value="<%=connecteurJDBC.getSQLreq()%>">

                  <tr> 
                     <td class=intfdcolor align=center nowrap height="24"> 
                      <span class=selectNS> 
                      <select name="columns" size="1">
		              <%
		value = "*";
		label = connecteurJDBC.getString("comboTous");
		selected = value.equals(column)?"selected":"";
		out.println("<option "+selected
	              + " value=" + value + ">"
				  + label + "</option>");
		for(int nI = 0; nI <allColumns_rs.length; nI++) 
		{
			//value = allColumns_rs[nI];
			value = Integer.toString(nI+1);
			label = allColumns_rs[nI];
			if ((label == null) || (label.length() == 0)) label = value;
			selected = (value.equals(column))?"selected":"";
			out.println("<option "+selected
						+" value=" + value + ">" + label + "</option>");
		}
%>
                      </select></span>
                    </td>
                    <td class=intfdcolor align=center nowrap height="24"> 
                      <span class=selectNS> 
					  <select name="compares" size="1">
                      <%
					  value = "*";
					  label = connecteurJDBC.getString("comboTous");
					  out.println("<option "+selected
	                             + " value=" + value + ">"
		                         + label + "</option>");
					  for(int nI = 0; nI <allCompares.length; nI++) 
					  {
                 		 value = allCompares[nI];
						 label = allCompares[nI];
			             if ((label == null) || (label.length() == 0)) label = value;
			             selected = (value.equals(compare))?"selected":"";
			             out.println("<option "+selected
						             +" value='" + value + "'>" + label + "</option>");
					  }
%>
                      </select></span>
                    </td>
                    <td class=intfdcolor align=center nowrap height="24"> 
                      <span class=selectNS> 
                      &nbsp;<%=connecteurJDBC.getString("champValeur")%> : <input type="text" name="columnValue" size="30" value="<%=columnValueS%>"></span>&nbsp;
                    </td>
					<td class=intfdcolor>
				  
				   <%
						ButtonPane buttonPane0 = gef.getButtonPane();
						buttonPane0.addButton((Button) gef.getFormButton("Ok", "javascript:onClick=restrictResults()", false));
						out.println(buttonPane0.print());
					%>
					</td>
				
				 </tr>
				 </Form>
			   </table>             
				  
              
			</td><td width="100%" class=intfdcolor51></td>
		  </tr>
		 </table>
</center>    	
<br>
<% 
	out.println(arrayPane.print());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<form name="navigationForm">
</form>

</Body>
</HTML>