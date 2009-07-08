<%@ include file="checkYellowpages.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
Collection			xmlForms	= (List) request.getAttribute("XMLForms");
Collection			modelUsed	= (Collection) request.getAttribute("ModelUsed");

//Icons
String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";

// déclaration des boutons
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main", false);

%>

<%@page import="com.silverpeas.publicationTemplate.PublicationTemplate"%><HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script language="javaScript">

function sendData() {
	document.model.mode.value = 'delete';
	document.model.submit();
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</HEAD>
<BODY onUnload="closeWindows()">
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
//    browseBar.setPath(linkedPathString);

    out.println(window.printBefore());

    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<form name="model" action="SelectModel" method="POST">
<input type="hidden" name="mode">

<TABLE CELLPADDING=5 WIDTH="100%">
  <TR><TD colspan="3" class="txtnav"><%=resources.getString("yellowPages.ModelList")%></TD></TR>
<%
    int nb = 0;
    out.println("<TR>");
   
    if (xmlForms != null)
    {
	    PublicationTemplate xmlForm;
	    
	    Iterator  iterator = xmlForms.iterator();
	    while (iterator.hasNext()) {
	        xmlForm = (PublicationTemplate) iterator.next();
	        
	        if (nb != 0 && nb%3==0)
		        out.println("</TR><TR>");
		        
	        nb++;
					// recherche si le modèle est dans la liste
	        boolean used = false;
	    		String usedCheck = "";
	    	  if (modelUsed.contains(xmlForm.getFileName()))
	        	used = true;
					if (used)
						usedCheck = "checked";
	        out.println("<TD align=\"center\"><IMG SRC=\""+xmlForm.getThumbnail()+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"><BR>"+xmlForm.getName()+"<BR><input type=\"checkbox\" name=\"modelChoice\" value=\""+xmlForm.getFileName()+"\" "+usedCheck+"></TD>");
	    }
	}
    
%>
</form>
</TABLE>

<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
  out.println(frame.printAfter());
%>

<% out.println(window.printAfter()); %>
</BODY>
</HTML>