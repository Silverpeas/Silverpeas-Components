<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%
	// récupération des paramètres :
	PhotoDetail photo			= (PhotoDetail) request.getAttribute("Photo");
	List 	path 			= (List) request.getAttribute("Path");
	Integer		nbCom			= (Integer) request.getAttribute("NbComments");
	Collection 	pathList 		= (Collection) request.getAttribute("PathList");
	Collection 	albums			= (Collection) request.getAttribute("Albums");
	String 		xmlFormName		= (String) request.getAttribute("XMLFormName");
	Boolean		isUsePdc		= (Boolean) request.getAttribute("IsUsePdc");
	boolean		showComments	= ((Boolean) request.getAttribute("ShowCommentsTab")).booleanValue();
	
	// déclaration des variables :
	String 		photoId			= new Integer(photo.getPhotoPK().getId()).toString();
	String 		nbComments 		= nbCom.toString();

	// déclaration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "PreviewPhoto?PhotoId="+photoId, false);
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script language="javascript">

function sendData() 
{
	document.paths.submit();
}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	displayPath(path, browseBar);

   	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("gallery.photo"), "PreviewPhoto?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?PhotoId="+photoId, false);
	if (showComments)
		tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbComments+")", "Comments?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("gallery.accessPath"), "#", true, false);
	if (isUsePdc.booleanValue())
		tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PhotoId="+photoId, false);
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    
    Board board	= gef.getBoard();
    
    out.println(board.printBefore());
    %>
    <form name="paths" action="SelectPath" method="POST">
    <table>
    <input type="hidden" name="PhotoId" value="<%=photoId%>">
    <%
	if(albums !=null && !albums.isEmpty())
	{
		Iterator iter = albums.iterator();
		while(iter.hasNext())
		{
			NodeDetail album = (NodeDetail)iter.next();
			if(album.getLevel() > 1)
			{ // on n'affiche pas le noeud racine
				String name = album.getName();
			
				String ind = "";
				if(album.getLevel() > 2)
				{// calcul chemin arbre
					int sizeOfInd = album.getLevel() - 2;
					if(sizeOfInd > 0)
					{
						for(int i=0;i<sizeOfInd;i++)
						{
							ind += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
						}
					}
				}
				name = ind + name;	
				// recherche si cet album est dans la liste des albums de la photo
				boolean used = false;
				Iterator i = pathList.iterator();
				while (i.hasNext()) 
			    {
					String nodeId = (String) i.next();
					if (new Integer(album.getId()).toString().equals(nodeId))
						used = true;
		        }
		    	String usedCheck = "";
				if (used)
					usedCheck = " checked";
	        	out.println("<tr><td>&nbsp;&nbsp;<input type=\"checkbox\" valign=\"absmiddle\" name=\"albumChoice\" value=\""+ album.getId() +"\""+usedCheck+">&nbsp;</td><td>"+name+"</td></tr>");
				//ligne.addArrayCellText(name);
			}
		}
	}
	
	out.println("</table>");
	out.println("</form>");
	
	out.println(board.printAfter());
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
	
</body>
</html>