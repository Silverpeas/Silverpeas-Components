<%!
void displayViewWysiwyg(String id, String spaceId, String componentId, HttpServletRequest request, HttpServletResponse response)
	throws com.stratelia.silverpeas.infoLetter.InfoLetterException {
    try {
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId=" + 
		id + "&SpaceId=" + spaceId + "&ComponentId=" + componentId).include(request, response);
    } catch (Exception e) {
		throw new com.stratelia.silverpeas.infoLetter.InfoLetterException("viewLetter_JSP.displayViewWysiwyg",
		com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR, e.getMessage());			
    }
}
%>
<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<link href="<%=m_context%>/util/styleSheets/modal-message.css" rel="stylesheet"  type="text/css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
	messageObj = new DHTML_modalMessage();	// We only create one object of this class
	messageObj.setShadowOffset(5);	// Large shadow
	
	function displayStaticMessage()
	{
		messageObj.setHtmlContent("<center><table border=0><tr><td align=\"center\"><br><b><%=resource.getString("infoLetter.sendLetterToManagerInProgress")%></b></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=m_context%>/util/icons/inProgress.gif\"/></td></tr></table></center>");
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
	
	function goHeaders (){
		document.headerParution.submit();
	}
	
	function goFiles (){
		document.attachedFiles.submit();
	}
	
	function sendLetterToManager (){
		displayStaticMessage();
		document.headerParution.action = "SendLetterToManager";
		document.headerParution.submit();
	}

</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
String parutionTitle = (String) request.getAttribute("parutionTitle");
String parution = (String) request.getAttribute("parution");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\"></a> " + Encode.javaStringToHtmlString(parutionTitle));	

	operationPane.addOperation(resource.getIcon("infoLetter.sendLetterToManager"), resource.getString("infoLetter.sendLetterToManager"), "javascript:sendLetterToManager();");	

	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("infoLetter.headerLetter"),"javascript:goHeaders();",false);  
    tabbedPane.addTab(resource.getString("infoLetter.editionLetter"),"javascript:call_wysiwyg();",false);
    tabbedPane.addTab(resource.getString("infoLetter.previewLetter"),"#",true);
    tabbedPane.addTab(resource.getString("infoLetter.attachedFiles"),"javascript:goFiles();",false);
	boolean isPdcUsed = ( "yes".equals( (String) request.getAttribute("isPdcUsed") ) );
	if (isPdcUsed)
	{
		tabbedPane.addTab(resource.getString("PdcClassification"),
						"pdcPositions.jsp?Action=ViewPdcPositions&PubId=" + (String) request.getAttribute("ObjectId") + ""
						,false);
	}
    out.println(tabbedPane.print());
    
	out.println(frame.printBefore());	
	
%>

<% // Ici debute le code de la page %>
<%
out.flush();
displayViewWysiwyg(parution, spaceId, componentId, request, response);		
%>
<form name="headerParution" action="ParutionHeaders" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
  <input type="hidden" name="ReturnUrl" value="Preview">
</form>
<form name="attachedFiles" action="FilesEdit" method="post">			
	<input type="hidden" name="parution" value="<%= parution %>">
</form>
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
<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>

