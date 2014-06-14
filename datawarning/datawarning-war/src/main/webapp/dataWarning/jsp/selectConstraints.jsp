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
<%@ include file="checkDataWarning.jsp" %>
<%
	String req = (String)request.getAttribute("req");
	String[] columns = (String[])request.getAttribute("columns");
	String[] constraints = (String[])request.getAttribute("constraints");
%>
<HTML>
<Head>
<view:looknfeel/>
</head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
	function viewRequete()
	{
		var req = buildRequete();
		SP_openWindow("PreviewReq?SQLReq=" + req, "Previsu_Req", "800", "300", "scrollbars=1");
	}
	
	function buildRequete()
	{
		var req = document.editForm.SQLReq.value;
<%
		if(constraints != null && constraints.length > 0)
		{
%>
			req += " where " + document.editForm.Contrainte0.value;
<%
			if(constraints.length > 1)
			{
%>
				for(var i=1; i< <%=constraints.length%>; i++)
					req += " and " + eval("document.editForm.Contrainte" + i).value;
<%
			}
		}
%>
		return req;
	}

	function addCriter()
	{
		var sCol;
		var sOp;
		for(var i=0; i<document.editForm.sColumn.length; i++)
			if(document.editForm.sColumn[i].selected)
				sCol = document.editForm.sColumn[i].value;
		for(var i=0; i<document.editForm.operateur.length; i++)
			if(document.editForm.operateur[i].selected)
				sOp = document.editForm.operateur[i].value;
		var crit = sCol + " " + sOp + " " + document.editForm.contrainteValeur.value;
		document.editForm.critere.value = crit;
<%
		if(constraints != null)
		{
%>
			document.editForm.critereSize.value = <%=constraints.length%>
<%
		}
%>
		if(document.editForm.contrainteValeur.value == "")
			alert('<%=resource.getString("erreurChampsVide")%>');
		else
		{
			document.editForm.action = "AddCriter";
			document.editForm.submit();
		}
	}

	function delCriter()
	{
		var select = false;
		for(var i=0; i<document.editForm.elements.length; i++)
			if(document.editForm.elements[i].type == 'checkbox' && document.editForm.elements[i].checked == true)
			{
				select = true;
				break;
			}
<%
		if(constraints != null)
		{
%>
			document.editForm.critereSize.value = <%=constraints.length%>
<%
		}
%>
		if(select == false)
			alert('<%=resource.getString("selectCritere")%>');
		else
		{
			document.editForm.action = "DelCriter";
			document.editForm.submit();
		}
	}

	function validateConstraints()
	{
		document.editForm.SQLReq.value = buildRequete();
		document.editForm.action = "SaveSelectColumns";
		document.editForm.submit();

	}

var selectAll = true;
	function selection()
	{
		for(var i=0; i<document.editForm.elements.length; i++)
			if(document.editForm.elements[i].type == 'checkbox')	
				if(selectAll)
				{
					document.editForm.elements[i].checked = true;
					if(i == document.editForm.elements.length - 4)
						selectAll = false;
				}
				else
				{
					document.editForm.elements[i].checked = false;
					if(i == document.editForm.elements.length - 4)
						selectAll = true;
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
<input type="hidden" name="critere">
<input type="hidden" name="critereSize">
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
									<span class="txtlibform"><%=resource.getString("tabbedPaneRequete")%> :&nbsp;</span>
								</td>
								<td valign="top">
									<input type="text" name="SQLReq" size="70" VALUE="<%=req%>" readonly>
									<br><br>
								</td>
							</tr>
<%
							if(constraints != null && constraints.length > 0)
							{
%>
							<tr>
								<td class="txtlibform" rowspan="<%=constraints.length + 2%>" valign="top">
									<%=resource.getString("criteres")%> :
								</td>
								<td valign="top">
<%
								for(int i=0; i<constraints.length; i++)
								{
%>
									<tr>
										<td>
											<input type="text" name="Contrainte<%=i%>" size="30" maxlength="20" VALUE="<%=constraints[i]%>" readonly>
											<input type="checkbox" name="ContrainteSupp<%=i%>">
										</td>
									</tr>
<%
								}
%>
									<tr>
										<td>
											<a href="javascript:selection();" title='<%=resource.getString("selectAllCase")%>'><%=resource.getString("selectAll")%></a> - <a href="javascript:delCriter();" title='<%=resource.getString("suppCritere")%>'><%=resource.getString("boutonSupprimer")%></a>
										</td>
									</tr>
								</td>
							</tr>
<%							
							}
%>
							<tr>
								<td valign="top" align="left" colspan="2">
									<br>
									<span class="txtlibform"><%=resource.getString("popupSelection3")%> :</span>
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<br>
									<table cellpadding=2 cellspacing=1 border=0 width="100%" bgcolor="#000000">
										<tr> 
											<td class=intfdcolor align=center nowrap height="24"> 
												<span class=selectNS>
													<select name="sColumn">
<%
														for(int i=0; i<columns.length; i++)
															out.println("<option value='" + columns[i] + "'>" + columns[i] + "</option>");
%>
													</select>
												</span>
											</td>
											<td class=intfdcolor align=center nowrap height="24"> 
												<span class=selectNS>
													<select name="operateur">
														<option value=">">&#62;</option>
														<option value=">=">&#62;&#61;</option>
														<option value="<">&#60;</option>
														<option value="<=">&#60;&#61;</option>
														<option value="=">&#61;</option>
														<option value="!=">&#33;&#61;</option>
													</select>
												</span>
											</td>
											<td class=intfdcolor align=center nowrap height="24"> 
												<span class=selectNS> 
													<%=resource.getString("champValeur")%> :
												</span>
											</td>
											<td class=intfdcolor align=center nowrap height="24"> 
												<span class=selectNS> 
													<input type="text" name="contrainteValeur" value="" size="30">
												</span>
											</td>
											<td class=intfdcolor align=center nowrap height="24" width="1%"> 
												<span class=selectNS> 
<%
													  ButtonPane buttonPane0 = gef.getButtonPane();
													  buttonPane0.addButton((Button) gef.getFormButton("Ok", "javascript:onClick=addCriter()", false));
													  out.println(buttonPane0.print());
%>
												</span>
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
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("boutonTerminer"), "javascript:onClick=validateConstraints();", false));
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