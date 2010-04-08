<%@ include file="checkDataWarning.jsp" %>
<%
	String table = (String)request.getAttribute("tableName");
	String[] columns = (String[])request.getAttribute("columns");
%>
<HTML>
<Head>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
	function viewRequete()
	{
		if(!isAllAgregaChoisi())
			alert('<%=resource.getString("agregaColonne")%>');
		else
		{
			var req = buildRequete();
			SP_openWindow("PreviewReq?SQLReq=" + req, "Previsu_Req", "800", "300", "scrollbars=1");
		}
	}

	function isAllAgregaChoisi()
	{
		var compteur = 0;
		var valid = true;
		for(x=0; x < <%=columns.length%>; x++)
			for(y=0; y < eval("document.editForm.Operateur" + x).options.length; y++)
				if(eval("document.editForm.Operateur" + x).options[y].value == "" && eval("document.editForm.Operateur" + x).options[y].selected == true)
				{
					compteur++;
					valid = false;
				}
		if((valid && compteur == 0) || (compteur == <%=columns.length%> && !valid))
			return true;
		return false;
	}

	function buildRequete()
	{
		var req = "";
		var selectedTable = "<%=table%>";
		var premierSelect;
		for(w=0; w<document.editForm.Operateur0.options.length; w++)
			if(document.editForm.Operateur0.options[w].selected == true)
				premierSelect = document.editForm.Operateur0.options[w].value;
		var col = premierSelect + "(<%=columns[0]%>)";

		if(<%=columns.length%> > 1)
		{
			for(z=1; z < <%=columns.length%>; z++)
			{
				var select = "";
				for(w=0; w<eval("document.editForm.Operateur" + z).options.length; w++)
					if(eval("document.editForm.Operateur" + z).options[w].selected == true)
						select = eval("document.editForm.Operateur" + z).options[w].value;
				col = col + ", " + select + "(" + eval("document.editForm.Colonne" + z).value + ")";
			}
		}
		req = "select " + col + " from <%=table%>";
		return req;
	}

	function validateAgrega() 
	{
		if(!isAllAgregaChoisi())
			alert('<%=resource.getString("agregaColonne")%>');
		else
		{
			document.editForm.SQLReq.value = buildRequete();
			document.editForm.action = "SaveSelectColumns";
			document.editForm.submit();
		}
	}
	function getConstraints()
	{
		if(!isAllAgregaChoisi())
			alert('<%=resource.getString("agregaColonne")%>');
		else
		{
			document.editForm.SQLReq.value = buildRequete();
			document.editForm.action = "SelectConstraints";
			document.editForm.submit();
		}
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
<input type="hidden" name="columnsSize" value="<%=columns.length%>">
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0"><!--tablcontour-->
	<tr align=center> 
		<td nowrap>
			<table border="0" cellspacing="2" cellpadding="5" class=intfdcolor width="100%"><!--tabl1-->
				<tr align=center class="intfdcolor4"> 
					<td nowrap> 
						<table cellpadding=0 cellspacing=0 border=0 width="100%">
							<tr>
								<td valign="top" width="1%">
									<span class="txtlibform"><%=resource.getString("popupChamp1")%> :&nbsp;</span>
								</td>
								<td valign="top">
									<input type="text" name="Name" size="30" maxlength="20" VALUE="<%=table%>" readonly>
									<br><br>
								</td>
							</tr>
<%
							for(int i=0; i<columns.length; i++)
							{
%>
							<tr>
								<td>
									<span class="txtlibform"><%=resource.getString("colonne")%> :&nbsp;</span>
								</td>
								<td>
									<input type="text" size="30" name="Colonne<%=i%>" value="<%=columns[i]%>" readonly>
								</td>
								<td>
									<span class="txtlibform"><%=resource.getString("operateur")%> :&nbsp;</span>
								</td>
								<td>
									<select name="Operateur<%=i%>">
										<option value="">pas d'opérateur</option>
										<option value="sum">somme</option>
										<option value="min">min</option>
										<option value="max">max</option>
										<option value="count">count</option>
										<option value="avg">moyenne</option>
									</select>
								</td>
							</tr>
<%
							}
%>
						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br>	
<%
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonSuivant"), "javascript:onClick=getConstraints()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonTerminer"), "javascript:onClick=validateAgrega();", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonAnnuler"), "javascript:onClick=window.close()", false));

	out.println(buttonPane.print());
%>
</center>
</form>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>