<%@ page import="com.silverpeas.gallery.model.Media" %>
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

<%
	Media media = (Media) request.getAttribute("Media");
	List 	path 			= (List) request.getAttribute("Path");
	Collection 	pathList 		= (Collection) request.getAttribute("PathList");
	Collection 	albums			= (Collection) request.getAttribute("Albums");
	String 		xmlFormName		= (String) request.getAttribute("XMLFormName");

	String mediaId = media.getMediaPK().getId();

	Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "MediaView?MediaId="+
      mediaId, false);
%>

<html>
<head>
<view:looknfeel/>
<script type="text/javascript">

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
	tabbedPane.addTab(resource.getString("gallery.media"), "MediaView?MediaId="+ mediaId, false);
	tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?MediaId="+ mediaId, false);
	tabbedPane.addTab(resource.getString("gallery.accessPath"), "#", true);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());

    Board board	= gef.getBoard();

    out.println(board.printBefore());
    %>
  <form name="paths" action="SelectPath" method="POST">
    <input type="hidden" name="MediaId" value="<%=mediaId%>">
    <table>
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
%>
	</table>
</form>
<%
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