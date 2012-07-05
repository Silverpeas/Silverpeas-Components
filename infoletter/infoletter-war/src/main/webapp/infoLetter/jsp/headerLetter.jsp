<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());

String parution = (String) request.getAttribute("parution");
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
	function call_wysiwyg (){
		document.toWysiwyg.submit();
	}
	
	function goValidate (){
		if (window.confirm("<%= resource.getString("infoLetter.sendLetter")%>"))
		{
			$.progressMessage();
			document.validateParution.submit();
		}
	}
	
	function goView (){
		document.viewParution.submit();
	}
	
	function goFiles (){
		document.attachedFiles.submit();
	}
	
	function goTemplate (){
		document.template.submit();
	}
	
	function submitForm() {
		if (!isValidTextArea(document.changeParutionHeaders.description)) {
			window.alert("<%= resource.getString("infoLetter.soLongPal") %>");
		} else {
			if (document.changeParutionHeaders.title.value=="") {
				alert("<%= resource.getString("infoLetter.fuckingTitleRequired") %>");
			} else {
				document.changeParutionHeaders.action = "ChangeParutionHeaders";
				document.changeParutionHeaders.submit();
			}
		}
	}
	
	function cancelForm() {
	    document.changeParutionHeaders.action = "Accueil";
	    document.changeParutionHeaders.submit();
	}
	
	function sendLetterToManager (){
		$.progressMessage();
		document.viewParution.action = "SendLetterToManager";
		document.viewParution.submit();
	}
</script>
</head>
<body bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\"></a> " + resource.getString("infoLetter.newLetterHeader"));


// Impossible de valider une parution non creee
if (StringUtil.isDefined(parution)) {
	operationPane.addOperation(resource.getIcon("infoLetter.sendLetterToManager"), resource.getString("infoLetter.sendLetterToManager"), "javascript:sendLetterToManager();");	
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("infoLetter.validLetter"), resource.getString("infoLetter.validLetter"), "javascript:goValidate();");
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("infoLetter.asTemplate"), 
	resource.getString("infoLetter.saveTemplate"), "javascript:goTemplate();");
}


	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"#",true);  

// Impossible d'aller sur le WYSIWYG tant que les headers n'ont pas ete valides
boolean isPdcUsed = ( "yes".equals( (String) request.getAttribute("isPdcUsed") ) );
if (parution.equals("")) {
    tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"#",false);
    tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"#",false);
    tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"#",false);

	if (isPdcUsed)
	{
		tabbedPane.addTab(resource.getString("PdcClassification"),"#",false);
	}
} else {
    tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"javascript:call_wysiwyg();",false);
    tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"javascript:goView();",false);
    tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"javascript:goFiles();",false);
	
	if (isPdcUsed)
	{
		tabbedPane.addTab(resource.getString("PdcClassification"),
						"pdcPositions.jsp?Action=ViewPdcPositions&PubId=" + (String) request.getAttribute("ObjectId") + ""
						,false);
	}
}

    out.println(tabbedPane.print());

    
	out.println(frame.printBefore());
%>
<% // Ici debute le code de la page %>
<center>
<form name="changeParutionHeaders" action="ChangeParutionHeaders" method="post">
  <input type="hidden" name="parution" value="<%= parution %>"/>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.name")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
				<input type="text" name="title" size="50" maxlength="50" value="<%= (String) request.getAttribute("title") %>"/>&nbsp;<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"/>
					</td>
				</tr>
				<tr align=center> 

					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("GML.description")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
					<textarea cols="49" rows="4" name="description"><%= (String) request.getAttribute("description") %></textarea>
					</td>
				</tr>
				<tr align=center>				 
					<td class="intfdcolor4" valign="baseline" align=left colspan=2><span class="txt">(<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%>)</span> 
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</form>
<form name="toWysiwyg" action="../../wysiwyg/jsp/htmlEditor.jsp" method="post">
    <input type="hidden" name="SpaceId" value="<%= (String) request.getAttribute("SpaceId") %>"/>
    <input type="hidden" name="SpaceName" value="<%= (String) request.getAttribute("SpaceName") %>"/>
    <input type="hidden" name="ComponentId" value="<%= (String) request.getAttribute("ComponentId") %>"/>
    <input type="hidden" name="ComponentName" value="<%= (String) request.getAttribute("ComponentName") %>"/>
    <input type="hidden" name="BrowseInfo" value="<%= (String) request.getAttribute("BrowseInfo") %>"/> 
    <input type="hidden" name="ObjectId" value="<%= (String) request.getAttribute("ObjectId") %>"/>
    <input type="hidden" name="Language" value="<%= (String) request.getAttribute("Language") %>"/>
    <input type="hidden" name="ReturnUrl" value="<%= (String) request.getAttribute("ReturnUrl") %>"/>
</form>


<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
</center>
<form name="validateParution" action="ValidateParution" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<form name="viewParution" action="Preview" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
  <input type="hidden" name="ReturnUrl" value="ParutionHeaders"/>
</form>
<form name="attachedFiles" action="FilesEdit" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<form name="template" action="UpdateTemplateFromHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>"/>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>
