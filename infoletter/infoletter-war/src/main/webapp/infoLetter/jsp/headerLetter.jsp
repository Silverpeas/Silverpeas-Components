<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());

String parution = (String) request.getAttribute("parution");
%>
<link href="<%=m_context%>/util/styleSheets/modal-message.css" rel="stylesheet"  type="text/css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
	messageObj = new DHTML_modalMessage();	// We only create one object of this class
	messageObj.setShadowOffset(5);	// Large shadow
	
	function displayStaticMessage(message)
	{
		messageObj.setHtmlContent("<center><table border=0><tr><td align=\"center\"><br><b>"+message+"</b></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=m_context%>/util/icons/inProgress.gif\"/></td></tr></table></center>");
		messageObj.setSize(300,150);
		messageObj.setCssClassMessageBox(false);
		messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes
		messageObj.display();
	}
	
	function closeMessage()
	{
		messageObj.close();
	}
	function call_wysiwyg (){
		document.toWysiwyg.submit();
	}
	
	function goValidate (){
		if (window.confirm("<%= resource.getString("infoLetter.sendLetter")%>"))
		{
			displayStaticMessage("<%=resource.getString("infoLetter.sendLetterInProgress")%>");
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
		displayStaticMessage("<%=resource.getString("infoLetter.sendLetterToManagerInProgress")%>");
		document.viewParution.action = "SendLetterToManager";
		document.viewParution.submit();
	}
</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
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
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<form name="changeParutionHeaders" action="ChangeParutionHeaders" method="post">
<input type="hidden" name="parution" value="<%= parution %>">
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.name")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
				<input type="text" name="title" size="50" maxlength="50" value="<%= (String) request.getAttribute("title") %>">&nbsp;<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5">
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
					<td class="intfdcolor4" valign="baseline" align=left colspan=2><span class="txt">(<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</span> 
					</td>
				</tr>
			</table>
		</td>
	</tr>
</form>
</table>
<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
    <input type="hidden" name="SpaceId" value="<%= (String) request.getAttribute("SpaceId") %>">
    <input type="hidden" name="SpaceName" value="<%= (String) request.getAttribute("SpaceName") %>">
    <input type="hidden" name="ComponentId" value="<%= (String) request.getAttribute("ComponentId") %>">
    <input type="hidden" name="ComponentName" value="<%= (String) request.getAttribute("ComponentName") %>">
    <input type="hidden" name="BrowseInfo" value="<%= (String) request.getAttribute("BrowseInfo") %>"> 
    <input type="hidden" name="ObjectId" value="<%= (String) request.getAttribute("ObjectId") %>">
    <input type="hidden" name="Language" value="<%= (String) request.getAttribute("Language") %>">
    <input type="hidden" name="ReturnUrl" value="<%= (String) request.getAttribute("ReturnUrl") %>">
</form>


<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
</center>
<form name="validateParution" action="ValidateParution" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
<form name="viewParution" action="Preview" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
  <input type="hidden" name="ReturnUrl" value="ParutionHeaders">
</form>
<form name="attachedFiles" action="FilesEdit" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
<form name="template" action="UpdateTemplateFromHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>

<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>

