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
	Category 	category		= (Category) request.getAttribute("Category");
	String 		userName		= (String) request.getAttribute("UserName");

	// déclaration des variables :
	String 		name			= "";
	String 		description		= "";
	String 		categoryId		= "";
	String 		creationDate	= resource.getOutputDate(new Date());
	String 		creatorName 	= userName;
	
	String 		action 			= "CreateCategory";	
	
	// dans le cas d'une mise à jour, récupération des données :
	if (category != null)
	{
		name 			= category.getName();
		description		= category.getDescription();
		categoryId		= category.getNodePK().getId();
		creationDate 	= resource.getOutputDate(category.getCreationDate());
		//creatorName 	= category.getCreatorName();
		action 			= "UpdateCategory";

	}
	
	// déclaration des boutons
	Button validateButton;
	if (action.equals("CreateCategory"))
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData()", false);
	else
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendDataUpdate()", false);
	Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false);

	
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<title></title>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
	
	// fonctions de contrôle des zones du formulaire avant validation
	function sendData() 
	{
		if (isCorrectForm()) 
		{
			window.opener.document.categoryForm.action = "CreateCategory";
			window.opener.document.categoryForm.Name.value = document.categoryForm.Name.value;
			window.opener.document.categoryForm.Description.value = document.categoryForm.Description.value;
			window.opener.document.categoryForm.submit();
			window.close();
   		}
	}
	
	function sendDataUpdate() 
	{
		if (isCorrectForm()) 
		{
			window.opener.document.categoryForm.action = "UpdateCategory";
			window.opener.document.categoryForm.Name.value = document.categoryForm.Name.value;
			window.opener.document.categoryForm.CategoryId.value = document.categoryForm.CategoryId.value;
			window.opener.document.categoryForm.Description.value = document.categoryForm.Description.value;
			window.opener.document.categoryForm.submit();
			window.close();
   		}
	}
		
	function isCorrectForm() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var name = stripInitialWhitespace(document.categoryForm.Name.value);

     	if (name == "") 
     	{ 
			errorMsg+="  - '<%=resource.getString("GML.title")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
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
<body id="blog" onload="javascript:document.categoryForm.Name.focus();">
<div id="<%=instanceId %>">
<%
	out.println(window.printBefore());
    out.println(frame.printBefore());

    Board board = gef.getBoard();
    out.println(board.printBefore());
%>
<form name="categoryForm" action="<%=action%>" method="post">
<table cellpadding="5" width="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
		<td><input type="text" name="Name" size="60" maxlength="150" value="<%=name%>"/>
			<img src="<%=resource.getIcon("blog.obligatoire")%>" width="5" height="5" border="0" alt="<%=resource.getString("GML.requiredField") %>"/>
			<input type="hidden" name="CategoryId" value="<%=categoryId%>"/>
			<input type="hidden" name="Langue" value="<%=resource.getLanguage()%>"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td><input type="text" name="Description" size="60" maxlength="150" value="<%=description%>"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.creationDate")%> :</td>
		<td><%=creationDate%></td>
	</tr>
	<tr><td colspan="2">( <img border="0" src="<%=resource.getIcon("blog.obligatoire")%>" width="5" height="5" alt="<%=resource.getString("GML.requiredField") %>"/> : <%=resource.getString("GML.requiredField")%> )</td></tr>
</table>
</form>
<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</div>
</body>
</html>