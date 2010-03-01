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
<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	AlbumDetail currentAlbum 	= (AlbumDetail) request.getAttribute("CurrentAlbum");
	List 	path 			= (List) request.getAttribute("Path");
	
	// déclaration des variables :
	String albumId 		= "";
	String nom 			= "";
	String description 	= "";
	String action 		= "CreateAlbum";
	
	// récupération des valeurs si l'album existe
	if (currentAlbum != null)
	{
		albumId = new Integer(currentAlbum.getId()).toString();
		nom = currentAlbum.getName();
		description = currentAlbum.getDescription();
		action = "UpdateAlbum";
	}
	
	// déclaration des boutons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
    Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javaScript:window.close()", false);
%>

<html>
		<head>
		<%
			out.println(gef.getLookStyleSheet());
		%>

		<TITLE>Titre de la fenetre</TITLE>
		<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
		<script language="javascript">
			// fonctions de contrôle des zones du formulaire avant validation
			function sendData() 
			{
				if (isCorrectForm()) 
				{
	        		//document.albumForm.submit();
	        		window.opener.document.albumForm.action = "<%=action%>";
	        		window.opener.document.albumForm.Id.value = document.albumForm.Id.value;
	        		window.opener.document.albumForm.Name.value = document.albumForm.Name.value;
	        		window.opener.document.albumForm.Description.value = document.albumForm.Description.value;
	        		window.opener.document.albumForm.submit();
	        		window.close();
	    		}
			}
		
			function isCorrectForm() 
			{
		     	var errorMsg = "";
		     	var errorNb = 0;
		     	var name = stripInitialWhitespace(document.albumForm.Name.value);
		     	var descr = document.albumForm.Description.value;
		     	if (name == "") 
		     	{
		           	errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
		           	errorNb++;
		     	} 
		     	switch(errorNb) 
		     	{
		        	case 0 :
		            	result = true;
		            	break;
		        	case 1 :
		            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
		            	window.alert(errorMsg);
		            	result = false;
		            	break;
		        	default :
		            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
		            	window.alert(errorMsg);
		            	result = false;
		            	break;
		     	} 
		     	return result;
			}		
		</script>
	
		</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="javascript:document.albumForm.Name.focus();">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<form name="albumForm" method="post" action="<%=action%>">
<table CELLPADDING="5" WIDTH="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
		<TD><input type="text" name="Name" value="<%=nom%>" size="60" maxlength="150">
			<IMG src="<%=resource.getIcon("gallery.obligatoire")%>" width="5" height="5" border="0">
			<input type="hidden" name="Id" value="<%=albumId%>"></td>
	</tr>
	<tr>
		<td class="txtlibform"> <%=resource.getString("GML.description")%> :</td>
		<TD><input type="text" name="Description" value="<%=description%>" size="60" maxlength="150"></TD>
	</tr>
	<tr><td colspan="2">( <img border="0" src=<%=resource.getIcon("gallery.obligatoire")%> width="5" height="5"> : Obligatoire )</td></tr>
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