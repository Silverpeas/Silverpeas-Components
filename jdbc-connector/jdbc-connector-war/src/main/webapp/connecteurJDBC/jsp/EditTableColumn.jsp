<%@ page import="org.silverpeas.search.searchEngine.searchEngine.control.ejb.*" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory" %>
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="imports.jsp" %>
<%!
	GraphicElementFactory gef;
	Window window;
	TabbedPane tabbedPane;
	
	ConnecteurJDBCSessionController connecteurJDBC;
	ResourceLocator messages = null;

	String flag = "user";

	boolean inList(String[] list, String item) {
		for (int i=0; i<list.length; i++) {
			  if (list[i] != null) {
				if (list[i].equals(item))
					return true;
			  }
		}
		return false;
	}

%>
<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server

	gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  	
  	flag = request.getParameter("flag");
  	if (flag == null)
  		flag = "user";
  	
	connecteurJDBC = (ConnecteurJDBCSessionController) request.getAttribute("connecteurJDBC");
	
	if (connecteurJDBC == null) {
		// No connecteurJDBC session controller in the request -> security exception
		String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

  	//objet window
  	window = gef.getWindow();
    //browse bar
	String space = connecteurJDBC.getSpaceLabel();
	String component = connecteurJDBC.getComponentLabel();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(space);
	browseBar.setComponentName(component);	

String columnValueS = "";
String action ="";

String[] allTables = null;
String[] allColumns = null;
String[] tables = null;
String[] columns = null;
String[] compares = null;
String[] allCompares = {"=", "<=", ">=", "!=", ">", "<"};
String[] selectedColumns = null;
String[] nselectedColumns = null;
String[] ssCols = null;
String table = null;
String count = null;
String column = null;
String selectedColumn = null;
String ssColumn = null;
String temp = null;
String addConst = "false";
String cpp = null;
String buff = null;
String addCriter = null;
String selectColumn = null;

String compare = "";
String value = "";
String label = "";
String selected = "";
String SCOL = "";

  String sqlRequest = "";

  tables = request.getParameterValues("tables");
  columns = request.getParameterValues("columns");
  compares = request.getParameterValues("compares");
  selectedColumns = request.getParameterValues("sColumns");
  ssCols = request.getParameterValues("ssColumns");

temp = request.getParameter("Temp");
cpp = request.getParameter("Cpp");
buff = request.getParameter("Buff");
columnValueS = request.getParameter("columnValue");
action = request.getParameter("Action");
addCriter = request.getParameter("addCriter");
count = request.getParameter("count");
addConst = request.getParameter("addConst");

String m_context = GeneralPropertiesManager.getString("ApplicationURL");

String arrowRight = m_context + "/util/icons/formButtons/arrowRight.gif";
String arrowLeft = m_context + "/util/icons/formButtons/arrowLeft.gif";
String arrowDoubleRight = m_context + "/util/icons/formButtons/arrowDoubleRight.gif";
String arrowDoubleLeft = m_context + "/util/icons/formButtons/arrowDoubleLeft.gif";

if(temp == null)
{
	temp = "false";
}

if(buff == null)
{
	buff = "false";
}

if(cpp == null)
{
	cpp = "true";
}

if(count == null)
{
	count = "true";
}

if(addConst == null)
{
	addConst = "false";
}

try
{
	allTables = connecteurJDBC.getTableNames();	
}
catch (Exception e)
{
%>
	<jsp:forward page="errorParam.jsp"/>
<% 
}

  
  if (tables != null)
  {
      table = tables[0];
	  connecteurJDBC.setTable(table);
  }
  else table = connecteurJDBC.getTable();

  if (ssCols != null)
  {
	  ssColumn = ssCols[0];
      connecteurJDBC.setSelected(ssColumn);
  }
  else ssColumn = connecteurJDBC.getSelected();
	  
  
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
  
  
  if(action != null)
  {
	// A voir peut etre que �a ne sert � rien !!!!!!!!!!!!!!!!!!!!!!!!!!!  
	if (action.equals("Update")) {
        //selectedColumns = (String[]) request.getParameterValues("sColumns");
        Vector sCol = new Vector();
        if (selectedColumns != null) {
            for (int i = 0; i < selectedColumns.length; i++) {
                  sCol.add(selectedColumns[i]);
			}
        }
		connecteurJDBC.setSelectedColumn(sCol); 
	
	} else if (action.equals("updateColumnList"))
	  {
		  //selectedColumns = (String[]) request.getParameterValues("sColumns");
		  Vector sCol = new Vector();
			if (selectedColumns != null) {
				for (int i = 0; i < selectedColumns.length; i++) {
                  sCol.add(selectedColumns[i]);
				}
			}
		  connecteurJDBC.setSelectedColumn(sCol); 
		  
		  // all columns of the selected table
		  allColumns = connecteurJDBC.getColumnNames(table);

		  // Selected Columns for the SQL Request
		  int n = connecteurJDBC.getSelectedColumn().size();
		  
		  selectedColumns = new String[n];
		  for (int i=0; i < n; i++) {
    		selectedColumns[i]= (String) connecteurJDBC.getSelectedColumn().elementAt(i);
		  }

		  // Non Selected Columns
		  nselectedColumns = new String[allColumns.length - n];
		  
		  int j = 0;
              //For each column, check if it is already selected
              for(int i = 0; i < allColumns.length; i++) {
                  //If not, the column is add to the non selected column list
                  if (!inList(selectedColumns, allColumns[i])) {
                          nselectedColumns[j] = allColumns[i];
                          j++;
                  }
              }

	  }
  }
%>

<%
String separator = "<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0><TR><TD><img src=../../util/colorPix/1px.gif width=3></TD></TR></TABLE>";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<Head>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \________________________________________________________________________</TITLE>
<view:looknfeel/>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<Script language="JavaScript">
	function getColumnList()
	{
		document.editForm.Action.value = "updateColumnList";
		document.editForm.Temp.value = "true";
		document.editForm.count.value = "false";
		document.editForm.Cpp.value = "false";
		document.editForm.submit();
	}

	function getConstraints()
	{
		document.editForm.count.value = "false";
		document.editForm.Temp.value = "selCol";
		document.editForm.Action.value = "updateColumnList";
		document.editForm.addConst.value = "true";

        for (j=0;j<document.editForm.sColumns.length;j++)
            document.editForm.sColumns[j].selected = true;

		document.editForm.submit();
	}
	
	function writeTableName()
	{
    var sqlreq = 'select * from ' +
        document.editForm.tables.options[document.editForm.tables.selectedIndex].value
    window.opener.document.processForm.SQLReq.value = sqlreq;
		window.opener.document.processForm.submit();
		window.close();
	}

	function addCriter()
	{
		document.editForm.count.value = "false";
		document.editForm.Temp.value = "selCol";
		document.editForm.addConst.value = "true";
		document.editForm.Action.value = "addCriter";
		document.editForm.submit();
	}

	function annulCriter()
	{
		document.editForm.count.value = "false";
		document.editForm.Temp.value = "selCol";
		document.editForm.addConst.value = "true";
		document.editForm.Action.value = "annulCriter";
		document.editForm.submit();
	}

	function endCriter()
	{
    window.opener.document.processForm.SQLReq.value = document.editForm.SQLReq.value;
    window.opener.document.processForm.submit();
		window.close();
	}

function move_groups(btn) {
   var z = 0;                       //used to index indexArray
   var indexArray = new Array();    //used to keep track of values in multiple selection case 

   if( btn == ">" )     //check which button
   {
      var listObj = document.editForm.columns;
      var targetObj = document.editForm.sColumns;
   }
   else
   {
      var listObj = document.editForm.sColumns;
      var targetObj = document.editForm.columns;
   }
   
   for( var i = 0; i < listObj.length; i++ )   //loop through list to find selected items
   {
      if(listObj.options[i].selected)          //only do something if item is selected
      {
         var selectedItem = listObj.options[i].text; 
         var selectedItem2 = listObj.options[i].value; 
         targetObj.options[targetObj.length] = new Option( selectedItem, selectedItem2 );   //create new items in target select box
         
         indexArray[z] = i;             //keep track of indices of selected items
         z++;                           //indexArray only gets a value if the item is selected and the 'if' statement is entered
      }
   }
   
   for( var i = listObj.length - 1; i >= 0; i-- )   //cycle backwards through items and clear all selected items
   {                                                //must cycle backwards so the loop does not miss any items when list size changes...
      listObj.options[indexArray[i]] = null;        //...and index of selected item changes 
   }                                                //ex. when loop begins, items 1 and 2 are selected, if 1 is deleted first... 
}

function moveall_groups(btn) {
    
   if( btn == ">>" )     //check which button
   {
      var listObj = document.editForm.columns;
      var targetObj = document.editForm.sColumns;
   }
   else
   {
      var listObj = document.editForm.sColumns;
      var targetObj = document.editForm.columns;
   }

   for( var i = 0; i < listObj.length; i++ )        //loop through list
   {   
      var selectedItem = listObj.options[i].text; 
      var selectedItem2 = listObj.options[i].value; 
      targetObj.options[targetObj.length] = new Option( selectedItem, selectedItem2 );
   }
   
   for( var i = listObj.length - 1; i >= 0; i-- )   //loop backwards through list clearing every item
   {                                               
      listObj.options[i] = null;        
   }                   
}

  function computeSQLRequest() {
    var listTables = document.editForm.tables;
    var selectedTable = "<%=table%>";
    var Count = "<%=count%>";

    var cpt = 0;
    nbr = document.editForm.sColumns.length;

    if (nbr > 0) {
      var Cols = document.editForm.sColumns[0].value;
      cpt = 1;
      while (cpt < nbr) {
        Cols = Cols + "," + document.editForm.sColumns[cpt].value;
        cpt++;
      }
      return 'select ' + Cols + ' from <%=table%>';
    } else {
      return 'select * from <%=table%>';
    }
}

  function validateColumns() {
    window.opener.document.processForm.SQLReq.value = computeSQLRequest();
    window.opener.document.processForm.submit();
    window.close();
  }
</Script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
  	//browse bar
	browseBar.setExtraInformation(connecteurJDBC.getString("titrePopup")) ;

	Frame frame = gef.getFrame();

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>


<form name="editForm" action="EditTableColumn.jsp" >
	<input name="Sender" type="hidden" value = "EditTableColumn.jsp" >
	<input name="Action" type="hidden" >
	<input name="Temp" type="hidden" >
	<input name="Cpp" type="hidden" >
	<input name="count" type="hidden" >
	<input name="Buff" type="hidden" >
	<input name="addConst" type="hidden" >

 <%
 if (count.equals("true"))
 { %>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0"><!--tablcontour-->
<tr align=center> 
	<td nowrap>
		<table border="0" cellspacing="2" cellpadding="5" class=intfdcolor width="100%">
			<tr align=center class="intfdcolor4"> 
              <td nowrap> 
				<table cellpadding=0 cellspacing=0 border=0 width="100%">
           					<tr> 
             					 

				<td valign=top><span class="txtlibform"><%=connecteurJDBC.getString("popupSelection1")%> : </span></td>
				<td valign=middle>
					<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="1%" bgcolor="#000000">
						<TR><TD><!--Cadre bleu-->
						  <TABLE CELLPADDING=2 CELLSPACING=1 BORDER=0 WIDTH="1%">
									
								<tr>
									
									<td nowrap align="left" CLASS=intfdcolor>
									
										  <span class=selectNS> 
										  <select name="tables" size="1">
										
					<%
							for(int nI = 0; nI <allTables.length; nI++) 
							{
								value = allTables[nI];
								label = allTables[nI];
								if ((label == null) || (label.length() == 0)) label = value;
								selected = (value.equals(table))?"selected":"";
								out.println("<option "+selected
											+" value=" + value + ">" + label + "</option>");
							}
					%>
										  </select></span>
						</td></tr></TABLE>
						</td></tr></TABLE>
							
 				  </TD></TR>
					</TABLE>
				</td></tr></table>	</td>
		  </tr>
		 </table>

<br>
 				   <%
		  ButtonPane buttonPane0 = gef.getButtonPane();
		  buttonPane0.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonSuivant"), "javascript:onClick=getColumnList()", false));
		  buttonPane0.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonTerminer"), "javascript:onClick=writeTableName()", false));
		  buttonPane0.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));

		  if(cpp.equals("true"))
			out.println(buttonPane0.print());%>
        
</center>
<%  
} %>
 
<%  
if (count.equals("false"))
{ %>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0"><!--tablcontour-->
<tr align=center> 
	<td nowrap>
		<table border="0" cellspacing="2" cellpadding="5" class=intfdcolor width="100%"><!--tabl1-->
			<tr align=center class="intfdcolor4"> 
              <td nowrap> 
				<table cellpadding=0 cellspacing=0 border=0 width="100%">
			<tr><td valign="top" nowrap>
			<span class="txtlibform"><%=connecteurJDBC.getString("popupChamp1")%> :</span>
			<td>&nbsp;</td>
			<td valign="top"><input type="text" name="Name" size="50" maxlength="20" VALUE="<%=table%>" readonly> 
			<br><br></td>
			</tr>
			<% } %>

			<% if(temp.equals("true"))
			{
			%>


				<TR>
					<TD colspan="3" align="center" class="intfdcolor"  height="1"><img src="<%=m_context%>/util/icons/colorPix/1px.gif"></TD>
				</TR>
			<tr> 
				<td colspan=3 align="center">
					<table cellpadding=0 cellspacing=0 border=0 width="100%">
						<tr>
							<td valign="top" nowrap align="center" width="50%"><br><span class="txtlibform"><%=connecteurJDBC.getString("popupAvailableColumns")%> : </span><br><br>
							</td>
							<td valign="top" nowrap width="1"><br>&nbsp;<br><br>
							</td>
							<td valign="top" nowrap align="center" width="50%"><br><span class="txtlibform"><%=connecteurJDBC.getString("popupSelected")%> : </span><br><br>
							</td>
						</tr>
					</table>
				</td>
			
			</tr>
			<tr> <td colspan=3 align="center"> 
					<table cellpadding=0 cellspacing=0 border=0 width="100%">
						<tr>
				<td valign="top" align="center" width="50%"><select name="columns" multiple size="10">
					<%
						for(int nI = 0; nI < nselectedColumns.length; nI++)
							out.println("<option value=\"" + nselectedColumns[nI] + "\">" + nselectedColumns[nI] + "</option>");
					%></select></td>
				<td width="1" valign="middle" align="center">
					<table border="0" cellpadding="0" cellspacing="0" width="37">
					  <tr> 
						<td class="intfdcolor" width="37"><a href="javascript:move_groups('>');"><IMG src="<%=arrowRight%>" width="37" height="24" border="0"></a><a href="javascript:moveall_groups('>>');"><IMG src="<%=arrowDoubleRight%>" width="37" height="24" border="0"></a><a href="javascript:move_groups('<');"><IMG src="<%=arrowLeft%>" width="37" height="24" border="0"></a><a href="javascript:moveall_groups('<<');"><IMG src="<%=arrowDoubleLeft%>" width="37" height="24" border="0"></a></td>
					  </tr>
					</table>
				</td>
				<td valign="top" align="center" width="50%"><select name="sColumns" multiple size="10">
				<%
						for(int nI = 0; nI < selectedColumns.length; nI++) {
							out.println("<option value=\"" + selectedColumns[nI] + "\">" +selectedColumns[nI] + "</option>");
						}
				%></select>
				</td></tr></table>

			</td></tr></table></td></tr></table>	</td>
		  </tr>
		 </table>
<br>	
					<%
					  ButtonPane buttonPane2 = gef.getButtonPane();
					  buttonPane2.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonSuivant"), "javascript:onClick=getConstraints()", false));
					  buttonPane2.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonTerminer"), "javascript:onClick=validateColumns();", false));
					  buttonPane2.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));
					  
					  out.println(buttonPane2.print());
					  %>

<%  } %>

<% if (temp.equals("selCol"))
{
  sqlRequest = request.getParameter("SQLReq");
  if (connecteurJDBC.getSelectedColumn().size() > 0) {
    SCOL = (String) connecteurJDBC.getSelectedColumn().elementAt(0);
    int n = connecteurJDBC.getSelectedColumn().size();
    int i = 1;
    while (i < n) {
      SCOL = SCOL + " , " + (String) connecteurJDBC.getSelectedColumn().elementAt(i);
      i++;
    }
    connecteurJDBC.setColumn(SCOL);
  }
  if (connecteurJDBC.getColumn().equals("")) {
    sqlRequest = "select * from " + table;
  } else if (!connecteurJDBC.getColumn().equals("")) {
    sqlRequest = "select " + connecteurJDBC.getColumn() + " from " + table;
  }

	if(action != null)
	{
		if(action.equals("addCriter"))
		{
      connecteurJDBC.setValidRequest(sqlRequest);

			try
			{
        if (sqlRequest.indexOf("where") == -1)
				{
          sqlRequest =
              sqlRequest + " where " + ssColumn + " " + compare + " " + "'" + columnValueS + "'";

				}
				else
				{
          sqlRequest =
              sqlRequest + " and " + ssColumn + " " + compare + " " + "'" + columnValueS + "'";
				}
        connecteurJDBC.getCurrentConnectionInfo().setSqlRequest(sqlRequest);
			}
			catch (NullPointerException e)
			{
				SilverTrace.warn("connecteurJDBC",
							     "JSPEditTableColumn", "connecteurJDBC.MSG_ADD_CRITER_TO_CLOSE_WHERE_FAIL",
            "request : " + sqlRequest, e);
			}
		}
		else if(action.equals("annulCriter"))
		{
      connecteurJDBC.getCurrentConnectionInfo().setSqlRequest(connecteurJDBC.getLastValidRequest());
		}
	}
%>
  <input name="SQLReq" type="hidden" value="<%=connecteurJDBC.getCurrentConnectionInfo().getSqlRequest()%>">

<tr><td valign="top" nowrap>
	<span class="txtlibform"><%=connecteurJDBC.getString("popupChamp2")%> :</span>  
	<td>&nbsp;</td>
	<td valign="top"><input type="text" name="Name" size="50" maxlength="20" VALUE="<%=SCOL%>" readonly> 
	<br><br></td>
</tr>
				<TR>
					<TD colspan="3" align="center" class="intfdcolor"  height="1"><img src="<%=m_context%>/util/icons/colorPix/1px.gif"></TD>
				</TR>
<%  } %>


<!-- ******************** Les Contraintes ici **********************************************************-->
				  

<%  
if(addConst.equals("true"))
{
%>
				<tr>
				<td valign="top" nowrap align="left" colspan="3"><br><span class="txtlibform"><%=connecteurJDBC.getString("popupSelection3")%> :</span>
				</td>
<tr><td colspan="3"><br>
		<table cellpadding=2 cellspacing=1 border=0 width="100%" bgcolor="#000000">
				
				<tr> 
                     <td class=intfdcolor align=center nowrap height="24"> 
                      <span class=selectNS> 
                      <select name="ssColumns" size="1">
		              <% int n = connecteurJDBC.getSelectedColumn().size();
		if(n > 0)
		{
			for(int nI = 0; nI < n; nI++) 
			{
				value = (String) connecteurJDBC.getSelectedColumn().elementAt(nI);
				label = (String) connecteurJDBC.getSelectedColumn().elementAt(nI);
				if ((label == null) || (label.length() == 0)) label = value;
				selected = (value.equals(ssColumn))?"selected":"";
				out.println("<option "+selected
							+" value=" + value + ">" + label + "</option>");
			}
		}
		else
		{
			allColumns = connecteurJDBC.getColumnNames(table);
			int m = allColumns.length;
			for(int nI = 0; nI <m; nI++) 
			{
				value = allColumns[nI];
				label = allColumns[nI];
				if ((label == null) || (label.length() == 0)) label = value;
				selected = (value.equals(ssColumn))?"selected":"";
				out.println("<option "+selected
							+" value=" + value + ">" + label + "</option>");
			}
		}

%>
                      </select></span>
                    </td>
	
					<td class=intfdcolor align=center nowrap height="24"> 
                     <span class=selectNS> 
					  <select name="compares" size="1">
                      <%
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
					<td class=intfdcolor width="1%">
				  
				   <%
					  ButtonPane buttonPane22 = gef.getButtonPane();
					  buttonPane22.addButton((Button) gef.getFormButton("Ok", "javascript:onClick=addCriter()", false));
					  buttonPane22.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:onClick=annulCriter()", false));
					  out.println(buttonPane22.print());
					%>

					</td>

				</tr>
			</table>
		

</td></tr></table>		</td></tr></table></td></tr></table><br>

				   <%
		  ButtonPane buttonPane3 = gef.getButtonPane();
		  buttonPane3.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonTerminer"), "javascript:onClick=endCriter()", false));
		  buttonPane3.addButton((Button) gef.getFormButton(connecteurJDBC.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));

		  out.println(buttonPane3.print());%>

<%  } %>
			
<!-- ******************** Fin des Contraintes ici **********************************************************-->			

</center>
</form>

<form name="processForm">
<input name="Temp" type="hidden" >
</form>

<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>