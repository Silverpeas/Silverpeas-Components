<%@ include file="check.jsp" %>

<% 
// récupération des paramètres
Collection	posts		= (Collection) request.getAttribute("Posts");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

function goToPost(id) {
    document.postForm.PostId.value = id;
    document.postForm.submit();
}

function goToCategory(id) {
    document.categoryForm.CategoryId.value = id;
    document.categoryForm.submit();
}

</script>
</head>

<body id="blog">
<div id="<%=instanceId %>">
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="blog">
  	<tr>
  		<td colspan="3">&nbsp;</td>
	</tr>
  	<tr>
    	<td valign="top" class="colonneGauche">
	    	<table width="100%" border="0" cellspacing="0" cellpadding="0">
		      	<%
		      	Iterator it = (Iterator) posts.iterator();
		      	if (!it.hasNext())
		      	{
		      		out.println("&nbsp;");
		      	}
		  		while (it.hasNext()) 
		  		{
		  			PostDetail post = (PostDetail) it.next();
		  			String categoryId = "";
	  				if (post.getCategory() != null)
		  				categoryId = post.getCategory().getNodePK().getId();
		  			String postId = post.getPublication().getPK().getId();
					%>
					<!--Debut d'un ticket-->
				    <tr>
				       	<td>
				       		<a href="javascript:onClick=goToPost('<%=postId%>')" class="titreTicket"><%=post.getPublication().getName()%></a>
				       	</td>
				    </tr>
				    <tr>
				    	<td class="infoTicket"><%=post.getCreatorName()%> - <%=resource.getOutputDate(post.getDateEvent())%></td>
				    </tr>
				    <tr>
				    	<td>&nbsp;</td>
				    </tr>
				    <!-- <tr>
					    <td>
				        <%
				        	out.flush();
			        		getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+postId+"&ComponentId="+instanceId).include(request, response);
			        	%>
			        	</td>
					</tr>
					<tr>
				    	<td>&nbsp;</td>
				    </tr>-->
				    <tr>
				    	<td>
							<% if (!categoryId.equals(""))
							{  %>
								<a href="javascript:onClick=goToCategory('<%=categoryId%>')" class="versTopic">&gt;&gt; <%=post.getCategory().getName()%> </a>
							<% } %>
							&nbsp;
						</td>
				    </tr>
				    <!--Fin du ticket-->
				    <tr>
				    	<td class="separateur">&nbsp;</td>
				   	</tr>
				    <%
		  		}
		  	 %>
			</table>
		</td>
		<td>&nbsp;&nbsp;</td>
	</tr>
</table>

<form name="postForm" action="ViewPost" Method="POST" target="MyMain">
	<input type="hidden" name="PostId">
</form>
<form name="categoryForm" action="PostByCategory" Method="POST" target="MyMain">
	<input type="hidden" name="CategoryId">
</form>
</div>
</body>

</html>
