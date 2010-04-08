<%@ include file="checkDataWarning.jsp" %>
<%
	String table = (String)request.getAttribute("tableName");
	String[] nselectedColumns = (String[])request.getAttribute("nselectedColumns");
	DataWarning data = (DataWarning)request.getAttribute("data");
	DataWarningQuery query = (DataWarningQuery)request.getAttribute("dataQuery");
%>
<HTML>
<Head>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
	var isTriggerQuery = <%=((query.getType() == DataWarningQuery.QUERY_TYPE_TRIGGER)?"true":"false")%>;
	function getAgregas()
	{
		if(isTriggerQuery && document.editForm.sColumns.length == 0)
			alert('<%=resource.getString("selectColonne")%>');
		else
		{
			if(document.editForm.sColumns.length == 0)
				for(var i=0; i<document.editForm.columns.length; i++)
					document.editForm.sColumns.options[document.editForm.sColumns.length] = new Option("", document.editForm.columns.options[i].value);
			for (j=0;j<document.editForm.sColumns.length;j++)
				document.editForm.sColumns[j].selected = true;
			document.editForm.action = "SelectAgrega";
			document.editForm.submit();
		}
	}
	function viewRequete()
	{
		var req = buildRequete();
		SP_openWindow("PreviewReq?SQLReq=" + req, "Previsu_Req", "800", "300", "scrollbars=1");
	}

	function buildRequete()
	{
		var req = "";
		var selectedTable = "<%=table%>";
		var cpt = 0;
		nbr = document.editForm.sColumns.length;
		if(nbr > 0)
		{
			var Cols = document.editForm.sColumns[0].value;
			cpt = 1;
			while(cpt < nbr)
			{
				Cols = Cols + "," + document.editForm.sColumns[cpt].value;
				cpt++;
			}
			req = "select " + Cols + " from <%=table%>";
		}
		else
			req = "select * from <%=table%>";
		return req;
	}

	function validateColumns() 
	{
		if(isTriggerQuery && document.editForm.sColumns.length == 0)
			alert('<%=resource.getString("selectColonne")%>');
		else
		{
			document.editForm.SQLReq.value = buildRequete();
			document.editForm.action = "SaveSelectColumns";
			document.editForm.submit();
		}
	}

	function SelectionColonne(obj, obj1, element)
	{
		var selectionne = false;
		for(var i=0; i<eval("document.editForm." + element).length; i++)
			if(eval("document.editForm." + element).options[i].selected == true)
				selectionne = true;
		if(!selectionne)
			alert('<%=resource.getString("selectColonnes")%>');
		else if(isTriggerQuery)
		{
			obj.style.display = 'none';
			obj1.style.display = 'block';
		}
		else
		{
			obj.style.display = 'block';
			obj1.style.display = 'block';
		}
	}

	function move_groups(btn) 
	{
	   var z = 0;                       //used to index indexArray
	   var indexArray = new Array();    //used to keep track of values in multiple selection case 

	   if(btn == ">")     //check which button
	   {
		  var listObj = document.editForm.columns;
		  var targetObj = document.editForm.sColumns;
	   }
	   else
	   {
		  var listObj = document.editForm.sColumns;
		  var targetObj = document.editForm.columns;
	   }
	   
	   for(var i=0; i<listObj.length; i++)   //loop through list to find selected items
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

	   for(var i=listObj.length-1; i>=0; i--)			//cycle backwards through items and clear all selected items
	   {                                                //must cycle backwards so the loop does not miss any items when list size changes...
		  listObj.options[indexArray[i]] = null;        //...and index of selected item changes 
	   }                                                //ex. when loop begins, items 1 and 2 are selected, if 1 is deleted first... 
	}

	function moveall_groups(btn) 
	{
	   if(btn == ">>")     //check which button
	   {
		  var listObj = document.editForm.columns;
		  var targetObj = document.editForm.sColumns;
	   }
	   else
	   {
		  var listObj = document.editForm.sColumns;
		  var targetObj = document.editForm.columns;
	   }

	   for(var i=0; i<listObj.length; i++)        //loop through list
	   {   
		  var selectedItem = listObj.options[i].text; 
		  var selectedItem2 = listObj.options[i].value; 
		  targetObj.options[targetObj.length] = new Option( selectedItem, selectedItem2 );
	   }
	   
	   for(var i=listObj.length-1; i>=0; i--)   //loop backwards through list clearing every item
		  listObj.options[i] = null;        
	}
</Script>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	//operation Pane 
	operationPane.addOperation(resource.getIcon("DataWarning.visuReq"), resource.getString("operationPaneReqVisu"), "javascript:onClick=viewRequete()");

	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<form name="editForm" method="post" action="">
<input type="hidden" name="SQLReq">
<input type="hidden" name="table" value="<%=table%>">
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0"><!--tablcontour-->
	<tr align=center> 
		<td nowrap>
			<table border="0" cellspacing="2" cellpadding="5" class=intfdcolor width="100%"><!--tabl1-->
				<tr align=center class="intfdcolor4"> 
					<td nowrap> 
						<table cellpadding=0 cellspacing=0 border=0 width="100%">
							<tr>
								<td valign="top" nowrap>
									<span class="txtlibform"><%=resource.getString("popupChamp1")%> :</span>
								</td>
								<td>&nbsp;</td>
								<td valign="top">
									<input type="text" name="Name" size="50" maxlength="20" VALUE="<%=table%>" readonly>
									<br><br>
								</td>
							</tr>
							<tr>
								<td colspan="3" align="center" class="intfdcolor"  height="1">
									<img src="<%=m_context%>/util/icons/colorPix/1px.gif"></td>
							</tr>
							<tr>
								<td colspan=3 align="center">
									<table cellpadding=0 cellspacing=0 border=0 width="100%">
										<tr>
											<td valign="top" nowrap align="center" width="50%">
												<br>
												<span class="txtlibform"><%=resource.getString("popupAvailableColumns")%> : </span>
												<br><br>
											</td>
											<td valign="top" nowrap width="1">
												<br>&nbsp;<br><br>
											</td>
											<td valign="top" nowrap align="center" width="50%">
												<br>
												<span class="txtlibform"><%=resource.getString("popupSelected")%> : </span>
												<br><br>
											</td>
										</tr>
									</table>
								</td>
							</tr>
							<tr>
								<td colspan=3 align="center">
									<table cellpadding=0 cellspacing=0 border=0 width="100%">
										<tr>
											<td valign="top" align="center" width="50%">
<%
												if(query.getType() == DataWarningQuery.QUERY_TYPE_TRIGGER)
													out.println("<select name='columns' size='10'>");
												else
													out.println("<select name='columns' multiple size='10'>");

													for(int i=0; i<nselectedColumns.length; i++)
														out.println("<option value=\""+nselectedColumns[i]+"\">"+nselectedColumns[i]+"</option>");
%>
												</select>
											</td>
											<td width="1" valign="middle" align="center">
												<table border="0" cellpadding="0" cellspacing="0" width="37">
													<tr>
														<td width="37">
															<a id="aRight" class="intfdcolor" onclick="javascript:SelectionColonne(this, document.getElementById('aLeft'), 'columns');" href="javascript:move_groups('>');" style="display:block"><img src="<%=m_context%>/util/icons/formButtons/arrowRight.gif" width="37" height="24" border="0"></a>
															<%if(query.getType() != DataWarningQuery.QUERY_TYPE_TRIGGER){%>
																<a class="intfdcolor" href="javascript:moveall_groups('>>');"><img src="<%=m_context%>/util/icons/formButtons/arrowDoubleRight.gif" width="37" height="24" border="0"></a>
															<%}%>
															<a id="aLeft" class="intfdcolor" onclick="javascript:SelectionColonne(this, document.getElementById('aRight'), 'sColumns');" href="javascript:move_groups('<');" style="display:none"><img src="<%=m_context%>/util/icons/formButtons/arrowLeft.gif" width="37" height="24" border="0"></a>
															<%if(query.getType() != DataWarningQuery.QUERY_TYPE_TRIGGER){%>
																<a class="intfdcolor" href="javascript:moveall_groups('<<');"><img src="<%=m_context%>/util/icons/formButtons/arrowDoubleLeft.gif" width="37" height="24" border="0"></a>
															<%}%>
														</td>
													</tr>
												</table>
											</td>
											<td valign="top" align="center" width="50%">
												<select name="sColumns" multiple size="10">
												</select>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br>	
<%
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonSuivant"), "javascript:onClick=getAgregas()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonTerminer"), "javascript:onClick=validateColumns();", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));

	out.println(buttonPane.print());
%>
</center>
</form>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());

	if(query.getType() != DataWarningQuery.QUERY_TYPE_TRIGGER)
	{
%>
<script language=javascript>
	document.getElementById('aLeft').style.display = 'block';
	document.getElementById('aRight').style.display = 'block';
</script>	
<%
	}
%>
</BODY>
</HTML>