<%@ page import="org.silverpeas.util.DateUtil" %>
<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

var templateWindow = window;

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

function openTemplate()
{
	windowName = "templateWindow";
	larg = "600";
	haut = "400";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!templateWindow.closed && templateWindow.name== "templateWindow")
    	templateWindow.close();
    templateWindow = SP_openWindow("ViewTemplate", windowName, larg, haut, windowParams);
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

</script>
</head>
<body>
<%
boolean isSuscriber = ((String)request.getAttribute("userIsSuscriber")).equals("true");
boolean isAdmin = ( ((String)request.getAttribute("userIsAdmin")).equals("true") );
boolean isPdcUsed = ( "yes".equals( (String) request.getAttribute("isPdcUsed") ) );
boolean showHeader = ( (Boolean) request.getAttribute("showHeader") ).booleanValue();
boolean isTemplateExist = ( (Boolean) request.getAttribute("IsTemplateExist") ).booleanValue();

if (isAdmin && isPdcUsed) {
	operationPane.addOperation(resource.getIcon("infoLetter.pdcUtilization"), resource.getString("PDCUtilization"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
	operationPane.addLine();
}

if (showHeader) {
	operationPane.addOperation(resource.getIcon("infoLetter.modifierHeader"), resource.getString("infoLetter.modifierHeader"), "LetterHeaders");	
	operationPane.addLine();
}
operationPane.addOperationOfCreation(resource.getIcon("infoLetter.newPubli"), resource.getString("infoLetter.newPubli"), "ParutionHeaders");	
operationPane.addOperation(resource.getIcon("infoLetter.delPubli"), resource.getString("GML.delete"), "javascript:submitForm();");	
operationPane.addLine();
operationPane.addOperation(resource.getIcon("infoLetter.access_SilverAbonnes"), resource.getString("infoLetter.access_SilverAbonnes"), "Suscribers");	
operationPane.addOperation(resource.getIcon("infoLetter.access_ExternAbonnes"), resource.getString("infoLetter.access_ExternAbonnes"), "Emails");
operationPane.addLine();

if (isSuscriber) {
	operationPane.addOperation(resource.getIcon("infoLetter.desabonner"), resource.getString("infoLetter.desabonner"), "UnsuscribeMe");
} else { 
	operationPane.addOperation(resource.getIcon("infoLetter.abonner"), resource.getString("infoLetter.abonner"), "SuscribeMe");
}

out.println(window.printBefore());
%>
<view:frame>
<% if (showHeader) { %>
<view:board>
<center>
	<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<tr> 
			<td class="txtlibform" valign="baseline" align=left nowrap><%=resource.getString("infoLetter.name")%> :</td>
			<td align="left" width="100%"><%= (String) request.getAttribute("letterName") %></td>
		</tr>
		<tr> 
			<td class="txtlibform" valign="top" align=left nowrap><%=resource.getString("GML.description")%> :</td>
			<td align="left"><%= EncodeHelper.javaStringToHtmlParagraphe((String) request.getAttribute("letterDescription")) %></td>
		</tr>
		<tr> 
			<td class="txtlibform" valign="top" align=left nowrap><%=resource.getString("infoLetter.frequence")%> :</td>
			<td align="left"><%= (String) request.getAttribute("letterFrequence") %></td>
		</tr>
		<% if (isTemplateExist) { %>
			<tr> 
				<td class="txtlibform" valign="baseline" align=left nowrap><%=resource.getString("infoLetter.model")%> :</td>
				<td align=left><a href="javaScript:openTemplate();"><%=EncodeHelper.javaStringToHtmlString(resource.getString("infoLetter.modelLink"))%></a></td>
			</tr>
		<% } %>			
	</table>
</center>
</view:board>
<br/>
<% } %>
<view:areaOfOperationOfCreation/>
<form name="deletePublications" action="DeletePublications" method="post">
<%
// Recuperation de la liste des parutions
List<InfoLetterPublication> publications = (List<InfoLetterPublication>) request.getAttribute("listParutions");
int i=0;

				ArrayPane arrayPane = gef.getArrayPane("InfoLetter", "Main", request, session);
		        //arrayPane.setVisibleLineNumber(10);
				
		        arrayPane.setTitle(resource.getString("infoLetter.listParutions"));	
		        
				ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
				arrayColumn0.setSortable(false);
				
				arrayPane.addArrayColumn(resource.getString("infoLetter.name"));
				arrayPane.addArrayColumn(resource.getString("GML.date"));
				ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resource.getString("GML.status"));
				arrayColumn1.setSortable(false);
				ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString("GML.operation"));
				arrayColumn2.setSortable(false);

if (publications.size()>0) {
	for (i = 0; i < publications.size(); i++) {
						InfoLetterPublication pub = (InfoLetterPublication) publications.get(i);
						ArrayLine arrayLine = arrayPane.addArrayLine();
						
						IconPane iconPane1 = gef.getIconPane();
						Icon debIcon = iconPane1.addIcon();
						if (pub._isValid()) debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "", "javascript:openViewParution('" + pub.getPK().getId() + "');");
						else debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "", "javascript:openEditParution('" + pub.getPK().getId() + "');");
						arrayLine.addArrayCellIconPane(iconPane1);	
						
						if (pub._isValid()) arrayLine.addArrayCellLink(pub.getTitle(), "javascript:openViewParution('" + pub.getPK().getId() + "');");
						else arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(pub.getTitle()), "javascript:openEditParution('" + pub.getPK().getId() + "');");
						
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
						
						arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"publis\" value=\"" + pub.getPK().getId() + "\"/>");
	}
}
	out.println(arrayPane.print());		
%>
</form>
<form name="editParution" action="ParutionHeaders" method="post">
	<input type="hidden" name="parution" value=""/>
</form>

<form name="viewParution" action="View" method="post">
	<input type="hidden" name="parution" value=""/>
</form>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>