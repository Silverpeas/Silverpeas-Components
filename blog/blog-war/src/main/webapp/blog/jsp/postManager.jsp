<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	PostDetail 	post			= (PostDetail) request.getAttribute("Post");
	Collection 	categories		= (Collection) request.getAttribute("AllCategories");
	String 		userName		= (String) request.getAttribute("UserName");
	Boolean		isUsePdc		= (Boolean) request.getAttribute("IsUsePdc");
	
	// déclaration des variables :
	String 		title			= "";
	String 		postId			= "";
	String 		categoryId		= "";
	String 		creationDate	= resource.getOutputDate(new Date());
	String 		creatorId		= "";
	String 		creatorName 	= userName;
	Date		dateEvent		= new Date();
	String 		updateDate		= null;
	
	String 		action 			= "CreatePost";	
	
	boolean		isPdcUsed 	= isUsePdc.booleanValue();
	
	// dans le cas d'une mise à jour, récupération des données :
	if (post != null)
	{
		title 			= post.getPublication().getName();
		postId			= post.getPublication().getPK().getId();
		if (post.getCategory() != null)
			categoryId	= post.getCategory().getNodePK().getId();
		creationDate 	= resource.getOutputDate(post.getPublication().getCreationDate());
		updateDate		= resource.getOutputDate(post.getPublication().getUpdateDate());
		creatorId 		= post.getPublication().getCreatorId();
		dateEvent 		= post.getDateEvent();
		action 			= "UpdatePost";

	}
	
	// déclaration des boutons
	Button validateButton 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);
	
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
					document.postForm.submit();
	    		}
			}
			
			function isCorrectForm() 
			{
		     	var errorMsg = "";
		     	var errorNb = 0;
		     	var title = stripInitialWhitespace(document.postForm.Title.value);

		     	if (title == "") 
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
<body id="blog" onLoad="javascript:document.postForm.Title.focus();">
<div id="<%=instanceId %>">
<%
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.head"), "#", true);
	if (action == "UpdatePost")
	{
		tabbedPane.addTab(resource.getString("blog.content"), "ViewContent?PostId=" + postId, false);
		if (isPdcUsed)
			tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PostId=" + postId, false);
	}
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    
    Board board = gef.getBoard();
    out.println(board.printBefore());
    
%>
<table CELLPADDING="5" WIDTH="100%">
<FORM Name="postForm" action="<%=action%>" Method="POST">
	
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
		<TD><input type="text" name="Title" size="60" maxlength="150" value="<%=title%>"/>
			<IMG src="<%=resource.getIcon("blog.obligatoire")%>" width="5" height="5" border="0"/>
			<input type="hidden" name="PostId" value="<%=postId%>"/>
			<input type="hidden" name="Langue" value="<%=resource.getLanguage()%>"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("blog.dateEvent")%> :</td>
		<td><input type="text" name="DateEvent" size="60" maxlength="150" value="<%=resource.getOutputDate(dateEvent)%>"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.category")%> :</td>
		<TD>
			<select name="CategoryId">
				<option value="" selected><%=resource.getString("GML.category")%></option>
				<option value="">-------------------------------</option>
				<%
				if (categories != null)
	      		{
					String selected = "";
	      			Iterator it = (Iterator) categories.iterator();
	      			while (it.hasNext()) 
			  		{
	      				NodeDetail uneCategory = (NodeDetail) it.next();
	      				if (categoryId.equals(uneCategory.getNodePK().getId()))
	      					selected = "selected";
	      				%>
	      				<option value="<%=uneCategory.getNodePK().getId()%>" <%=selected%>><%=uneCategory.getName()%></option>
	      				<%
	      				selected = "";
			  		}
	      		}
				%>
			</select>
	  	</TD>

	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.creationDate")%> :</td>
		<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resource.getString("GML.by")%></span>&nbsp;<%=creatorName%></TD>
		<% if (updateDate != null )
		{ %>
			</tr>
			<tr>
				<td class="txtlibform"><%=resource.getString("GML.updateDate")%> :</td>
				<TD><%=updateDate%></TD>
		<% } %>
	</tr>
	<tr><td colspan="2">( <img border="0" src=<%=resource.getIcon("blog.obligatoire")%> width="5" height="5"/> : <%=resource.getString("GML.requiredField")%> )</td></tr>

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
</div>
</body>
</html>