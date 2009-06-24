<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
Collection 			dbForms		= (Collection) request.getAttribute("DBForms");
List 				xmlForms	= (List) request.getAttribute("XMLForms");
Collection			modelUsed	= (Collection) request.getAttribute("ModelUsed");

String linkedPathString = kmeliaScc.getSessionPath();

//Icons
String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";

// déclaration des boutons
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main", false);

%>
<HTML>
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
    browseBar.setPath(linkedPathString);

    out.println(window.printBefore());

    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<form name="model" action="SelectModel" method="POST">
<input type="hidden" name="mode">

<TABLE CELLPADDING=5 WIDTH="100%">
  <TR><TD colspan="3" class="txtnav"><%=resources.getString("kmelia.ModelList")%></TD></TR>
<%
    ModelDetail modelDetail;
    int nb = 0;
    out.println("<TR>");
    Iterator iterator = dbForms.iterator();
    while (iterator.hasNext()) {
        modelDetail = (ModelDetail) iterator.next();
        
        if (nb != 0 && nb%3==0)
	        out.println("</TR><TR>");
	        
        nb++;
        // recherche si le modèle est dans la liste
        boolean used = false;
        if (modelUsed.contains(modelDetail.getId()))
        {
        	used = true;
        }
    	String usedCheck = "";
		if (used)
			usedCheck = "checked";
		
        out.println("<TD align=\"center\"><IMG SRC=\"../../util/icons/model/"+modelDetail.getImageName()+"\" border=\"0\" alt=\""+modelDetail.getDescription()+"\"><BR>"+modelDetail.getName()+"<BR><input type=\"checkbox\" name=\"modelChoice\" value=\""+modelDetail.getId()+"\" "+usedCheck+"></A></TD>");
    }
    
    if (xmlForms != null)
    {
	    PublicationTemplate xmlForm;
	    iterator = xmlForms.iterator();
	    while (iterator.hasNext()) {
	        xmlForm = (PublicationTemplate) iterator.next();
	        
	        if (nb != 0 && nb%3==0)
		        out.println("</TR><TR>");
		        
	        nb++;
			// recherche si le modèle est dans la liste
	        boolean used = false;
	        if (modelUsed.contains(xmlForm.getFileName()))
	        {
	        	used = true;
	        }
	    	String usedCheck = "";
			if (used)
				usedCheck = "checked";
			
	        out.println("<TD align=\"center\"><IMG SRC=\""+xmlForm.getThumbnail()+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"><BR>"+xmlForm.getName()+"<BR><input type=\"checkbox\" name=\"modelChoice\" value=\""+xmlForm.getFileName()+"\" "+usedCheck+"></TD>");
	    }
	}
    
	if (nb != 0 && nb%3 == 0)
		out.println("</TR><TR>");
	// recherche si le modèle est dans la liste
    boolean used = false;
    Iterator it = modelUsed.iterator();
    if (modelUsed.contains("WYSIWYG"))
    {
    	used = true;
    }
	String usedCheck = "";
	if (used)
		usedCheck = "checked";	
	
    out.println("<TD align=\"center\"><IMG SRC=\"../../util/icons/model/wysiwyg.gif\" border=\"0\" alt=\"Wysiwyg\"><BR>WYSIWYG<BR><input type=\"checkbox\" name=\"modelChoice\" value=\"WYSIWYG\" "+usedCheck+"></A></TD>");
    out.println("</TR>");
    
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