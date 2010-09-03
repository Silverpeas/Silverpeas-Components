<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
Collection 			dbForms		= (Collection) request.getAttribute("DBForms");
List 				xmlForms	= (List) request.getAttribute("XMLForms");
Collection			modelUsed	= (Collection) request.getAttribute("ModelUsed");

String linkedPathString = kmeliaScc.getSessionPath();

// d�claration des boutons
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main", false);

%>
<html>
<head>
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
</head>
<body onUnload="closeWindows()" id="<%=componentId%>">
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
<form name="model" action="SelectModel" method="post">
<input type="hidden" name="mode"/>

<table cellpadding="5" width="100%" id="templates">
  <tr><td colspan="3" class="txtnav"><%=resources.getString("kmelia.ModelList")%></td></tr>
<%
    ModelDetail modelDetail;
    int nb = 0;
    out.println("<tr>");
    Iterator iterator = dbForms.iterator();
    while (iterator.hasNext()) {
        modelDetail = (ModelDetail) iterator.next();
        
        if (nb != 0 && nb%3==0)
	        out.println("</tr><tr>");
	        
        nb++;
        // recherche si le mod�le est dans la liste
        boolean used = false;
        if (modelUsed.contains(modelDetail.getId()))
        {
        	used = true;
        }
    	String usedCheck = "";
		if (used)
			usedCheck = "checked";
		
        out.println("<td class=\"template\"><img src=\"../../util/icons/model/"+modelDetail.getImageName()+"\" border=\"0\" alt=\""+modelDetail.getDescription()+"\"/><br/>"+modelDetail.getName()+"<br/><input type=\"checkbox\" name=\"modelChoice\" value=\""+modelDetail.getId()+"\" "+usedCheck+"/></a></td>");
    }
    
    if (xmlForms != null)
    {
	    PublicationTemplate xmlForm;
	    String thumbnail = "";
	    iterator = xmlForms.iterator();
	    while (iterator.hasNext()) {
	        xmlForm = (PublicationTemplate) iterator.next();
	        
	        if (nb != 0 && nb%3==0)
		        out.println("</tr><tr>");
		        
	        nb++;
			// recherche si le mod�le est dans la liste
	        boolean used = false;
	        if (modelUsed.contains(xmlForm.getFileName()))
	        {
	        	used = true;
	        }
	    	String usedCheck = "";
			if (used)
				usedCheck = "checked";
			
			thumbnail = xmlForm.getThumbnail();
	        if (!StringUtil.isDefined(thumbnail))
	        {
	        	thumbnail = PublicationTemplate.DEFAULT_THUMBNAIL;
	      	}
			
	        out.println("<td class=\"template\"><img src=\""+thumbnail+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"/><br/>"+xmlForm.getName()+"<br/><input type=\"checkbox\" name=\"modelChoice\" value=\""+xmlForm.getFileName()+"\" "+usedCheck+"/></td>");
	    }
	}
    
	if (nb != 0 && nb%3 == 0)
		out.println("</tr><tr>");
	// recherche si le mod�le est dans la liste
    boolean used = false;
    Iterator it = modelUsed.iterator();
    if (modelUsed.contains("WYSIWYG"))
    {
    	used = true;
    }
	String usedCheck = "";
	if (used)
		usedCheck = "checked";	
	
    out.println("<td class=\"template\"><img src=\"../../util/icons/model/wysiwyg.gif\" border=\"0\" alt=\"Wysiwyg\"/><br/>WYSIWYG<br/><input type=\"checkbox\" name=\"modelChoice\" value=\"WYSIWYG\" "+usedCheck+"/></a></td>");
    out.println("</tr>");
    
%>
</form>

</table>

<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
    out.println(frame.printAfter());
%>

<% out.println(window.printAfter()); %>
</body>
</html>