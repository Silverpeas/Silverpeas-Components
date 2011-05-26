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

<% 
	// récupération des paramètres :
	String 		  albumId		 = (String) request.getAttribute("AlbumId");
	List  	    path 			 = (List) request.getAttribute("Path");
	Collection  albums     = (Collection) request.getAttribute("Albums");

		
	// déclaration des variables :
	
	
	PagesContext 		context 	= new PagesContext("photoForm", "0", resource.getLanguage(), false, componentId, gallerySC.getUserId(), gallerySC.getAlbum(gallerySC.getCurrentAlbumId()).getNodePK().getId()); //Pas de passage de l'objectId dans le contexte car on est en traitement par lot, ce passage se fera lors de la validation du formulaire
	context.setBorderPrinted(false);
	context.setCurrentFieldIndex("10");
	context.setUseBlankFields(true);
	context.setUseMandatory(false);
		
	// déclaration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton   = (Button) gef.getFormButton(resource.getString("GML.cancel"), "GoToCurrentAlbum?AlbumId="+albumId, false);
%>

<html>
<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	
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

  
  
  out.println(window.printBefore());
  out.println(frame.printBefore());
    
    Board board = gef.getBoard();
    
    out.println(board.printBefore());
    %>
    <form name="paths" action="UpdateSelectedPaths" method="POST">
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
        boolean used = false;
        String usedCheck = "";
        if (used)
          usedCheck = " checked";
            out.println("<tr><td>&nbsp;&nbsp;<input type=\"checkbox\" valign=\"absmiddle\" name=\"albumChoice\" value=\""+ album.getId() +"\""+usedCheck+">&nbsp;</td><td>"+name+"</td></tr>");

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