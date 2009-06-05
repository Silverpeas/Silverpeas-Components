<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function goto_jsp(jsp, param)
{
  //    alert("../../RinfoLetter/<%=spaceId%>_<%=componentId%>/"+jsp+"?"+param);
	window.open("../../RinfoLetter/<%=spaceId%>_<%=componentId%>/"+jsp+"?"+param,"MyMain");
}

function submitForm() {
	if (confirm("<%= resource.getString("infoLetter.confirmDeleteParutions") %>"))
	document.deletePublications.submit();
}

function openEditParution(par) {
    document.editParution.parution.value = par;
    document.editParution.submit();
}

function openViewParution(par) {
    document.viewParution.parution.value = par;
    document.viewParution.submit();
}
</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%


	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "javascript:goto_jsp('Main','')");
	browseBar.setPath("<a href=# onClick=\"goto_jsp('Main','')\">" + resource.getString("infoLetter.listParutions") + "</a>");
	//browseBar.setPath("javascript:goto_jsp('Main','')");


boolean isSuscriber = "true".equals(((String)request.getAttribute("userIsSuscriber")));


if (isSuscriber) operationPane.addOperation(resource.getIcon("infoLetter.desabonner"), resource.getString("infoLetter.desabonner"), "UnsuscribeMe");
else operationPane.addOperation(resource.getIcon("infoLetter.abonner"), resource.getString("infoLetter.abonner"), "SuscribeMe");	
operationPane.addLine();
operationPane.addOperation(resource.getIcon("infoLetter.modifierHeader"), resource.getString("infoLetter.modifierHeader"), "LetterHeaders");	
operationPane.addLine();
operationPane.addOperation(resource.getIcon("infoLetter.newPubli"), resource.getString("infoLetter.newPubli"), "ParutionHeaders");	
operationPane.addLine();
operationPane.addOperation(resource.getIcon("infoLetter.delPubli"), resource.getString("GML.delete"), "javascript:submitForm();");	
operationPane.addLine();
operationPane.addOperation(resource.getIcon("infoLetter.access_SilverAbonnes"), resource.getString("infoLetter.access_SilverAbonnes"), "Suscribers");	
operationPane.addLine();
operationPane.addOperation(resource.getIcon("infoLetter.access_ExternAbonnes"), resource.getString("infoLetter.access_ExternAbonnes"), "Emails");	


	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator

    
	out.println(frame.printBefore());	
	
%>

<% // Ici debute le code de la page %>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.name")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
					<input type="text" name="name" size="50" maxlength="50" value="<%= (String) request.getAttribute("letterName") %>" readonly>
					</td>
				</tr>
				<tr align=center> 

					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("GML.description")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
					<textarea cols="49" rows="4" name="description" readonly><%= (String) request.getAttribute("letterDescription") %></textarea>
					</td>
				</tr>
				<tr align=center> 

					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.frequence")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
					<input type="text" name="frequence" size="50" maxlength="50" value="<%= (String) request.getAttribute("letterFrequence") %>" readonly>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</CENTER>
<br>
<form name="deletePublications" action="DeletePublications" method="post">
<%
// Recuperation de la liste des parutions
Vector publications = (Vector) request.getAttribute("listParutions");

int i=0;

				ArrayPane arrayPane = gef.getArrayPane("InfoLetter", "Main", request, session);

		        //arrayPane.setVisibleLineNumber(10);
					
				arrayPane.addArrayColumn(resource.getString(""));
				arrayPane.addArrayColumn(resource.getString("infoLetter.name"));
				arrayPane.addArrayColumn(resource.getString("GML.date"));
				ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resource.getString("GML.status"));
				arrayColumn1.setSortable(false);
				ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString("GML.operation"));
				arrayColumn2.setSortable(false);

if ((publications != null) && (publications.size()>0)) {
	for (i = 0; i < publications.size(); i++) {
						InfoLetterPublication pub = (InfoLetterPublication) publications.elementAt(i);
						ArrayLine arrayLine = arrayPane.addArrayLine();
						
						IconPane iconPane1 = gef.getIconPane();
						Icon debIcon = iconPane1.addIcon();
						//if (pub._isValid()) debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "", "javascript:openViewParution('" + pub.getPK().getId() + "');");
						//else debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "", "javascript:openEditParution('" + pub.getPK().getId() + "');");
						if (pub._isValid()) debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "", "javascript:goto_jsp('View','parution=" + pub.getPK().getId() + "');");
						else debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "", "javascript:goto_jsp('ParutionHeaders','parution=" + pub.getPK().getId() + "');");

						arrayLine.addArrayCellIconPane(iconPane1);	
						
						//if (pub._isValid()) arrayLine.addArrayCellLink(pub.getTitle(), "javascript:openViewParution('" + pub.getPK().getId() + "');");
						//else arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(pub.getTitle()), "javascript:openEditParution('" + pub.getPK().getId() + "');");
						
						if (pub._isValid()) arrayLine.addArrayCellLink(pub.getTitle(), "javascript:goto_jsp('View','parution=" + pub.getPK().getId() + "');");
						else arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(pub.getTitle()), "javascript:goto_jsp('ParutionHeaders','parution=" + pub.getPK().getId() + "');");

						if (pub._isValid())
						{
							java.util.Date date = DateUtil.parse(pub.getParutionDate());
							ArrayCellText cell = arrayLine.addArrayCellText(resource.getOutputDate(date));
							cell.setCompareOn(date);
						}
						else
						{
							arrayLine.addArrayCellText("");
						}
						
						IconPane iconPane2 = gef.getIconPane();
						Icon statusIcon = iconPane2.addIcon();
						if (pub._isValid()) statusIcon.setProperties(resource.getIcon("infoLetter.visible"), resource.getString("infoLetter.paru"));
						else statusIcon.setProperties(resource.getIcon("infoLetter.nonvisible"), resource.getString("infoLetter.nonParu"));
						
						arrayLine.addArrayCellIconPane(iconPane2);					
						
						arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"publis\" value=\"" + pub.getPK().getId() + "\">");
	}
}
				
				
		out.println(arrayPane.print());
		
%>
</form>
<form name="editParution" action="ParutionHeaders" method="post">
	<input type="hidden" name="parution" value="">
</form>

<form name="viewParution" action="View" method="post">
	<input type="hidden" name="parution" value="">
</form>
<% // Ici se termine le code de la page %>


<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>

