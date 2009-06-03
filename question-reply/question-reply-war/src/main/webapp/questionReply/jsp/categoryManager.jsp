<%@ include file="checkQuestionReply.jsp" %>

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
	
	// dans le cas d'une mise à jour, récupération des données 
	if (category != null)
	{
		name 			= category.getName();
		description		= category.getDescription();
		categoryId		= category.getNodePK().getId();
		creationDate 	= resource.getOutputDate(category.getCreationDate());
		action 			= "UpdateCategory";

	}
	
	// déclaration des éléments graphiques
	frame = gef.getFrame();
	window = gef.getWindow();
	
	// déclaration des boutons
	Button validateButton;
	if (action.equals("CreateCategory"))
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData()", false);
	else
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendDataUpdate()", false);
	Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);

	
%>

<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	<script language="javascript">
	
	// fonctions de contrôle des zones du formulaire avant validation
	function sendData() 
	{
		if (isCorrectForm()) 
		{
			document.categoryForm.action = "CreateCategory";
			document.categoryForm.submit();
   		}
	}
	
	function sendDataUpdate() 
	{
		if (isCorrectForm()) 
		{
			document.categoryForm.action = "UpdateCategory";
			document.categoryForm.submit();
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
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="javascript:document.categoryForm.Name.focus();">
<%
	//AFFICHAGE DE LA BARRE DE NAVIGATION
	browseBar = window.getBrowseBar();
	browseBar.setDomainName(scc.getSpaceLabel()); 
	browseBar.setComponentName(scc.getComponentLabel(), "Main");
	if (action.equals("CreateCategory"))
		browseBar.setPath(resource.getString("questionReply.addCategory"));
	else
		browseBar.setPath(resource.getString("questionRpely.editCategory"));

	out.println(window.printBefore());
    out.println(frame.printBefore());

    board = gef.getBoard();
    out.println(board.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
<FORM Name="categoryForm" action="<%=action%>" Method="POST">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
		<TD><input type="text" name="Name" size="60" maxlength="150" value="<%=name%>" >
			<IMG src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5" border="0"></TD>
			<input type="hidden" name="CategoryId" value="<%=categoryId%>"> </td>
			<input type="hidden" name="Langue" value="<%=resource.getLanguage()%>"> </td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<TD><input type="text" name="Description" size="60" maxlength="150" value="<%=description%>" ></TD>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<TD><%=creationDate%>&nbsp;<span class="txtlibform"></TD>
	</tr>
	<tr><td colspan="2">( <img border="0" src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%> )</td></tr>
  </form>
</table>
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