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
<%@page import="java.util.GregorianCalendar"%>
<%@ include file="check.jsp" %>

<% 
// r�cup�ration des param�tres
PostDetail	post		= (PostDetail) request.getAttribute("Post");
Collection	categories	= (Collection) request.getAttribute("Categories");
Collection	archives	= (Collection) request.getAttribute("Archives");
Collection	links		= (Collection) request.getAttribute("Links");
Collection 	comments	= (Collection) request.getAttribute("AllComments");
String 		profile		= (String) request.getAttribute("Profile");
String		blogUrl		= (String) request.getAttribute("Url");
String		rssURL		= (String) request.getAttribute("RSSUrl");
List		events		= (List) request.getAttribute("Events");
String 		dateCal		= (String) request.getAttribute("DateCalendar");

//d�claration des boutons
Button validateComment 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);

Date 	   dateCalendar	= new Date(dateCal);

String categoryId = "";
if (post.getCategory() != null)
	categoryId = post.getCategory().getNodePK().getId();
String postId = post.getPublication().getPK().getId();
String link	= post.getPermalink();

java.util.Calendar cal = GregorianCalendar.getInstance();
cal.setTime(post.getDateEvent());
String day = resource.getString("GML.jour"+cal.get(java.util.Calendar.DAY_OF_WEEK));

boolean isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());

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

	var notifyWindow = window;
	
	// fonctions de contr�le des zones du formulaire avant validation
	function sendData() 
	{
		if (isCorrectForm()) 
		{
			document.commentForm.action = "AddComment";
			document.commentForm.submit();
		}
	}

	function isCorrectForm() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var message = stripInitialWhitespace(document.commentForm.Message.value);

     	if (message == "") 
     	{ 
			errorMsg+="  - '<%=resource.getString("blog.message")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
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

	function updateComment(id, postId)
	{
	    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?id="+id+"&amp;IndexIt=1", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
	    document.commentForm.action = "UpdateComment";
	    document.commentForm.CommentId.value = id;
	   	document.commentForm.PostId.value = postId;
		document.commentForm.submit();
	}
	
	function removeComment(id)
	{
	    if (window.confirm("<%=resource.getString("blog.confirmDeleteComment")%>"))
	    {
	    	document.commentForm.action = "DeleteComment";
	    	document.commentForm.CommentId.value = id;
			document.commentForm.submit();
	    }
	}

	function commentCallBack()
	{
		location.href="<%=m_context+URLManager.getURL("useless", instanceId)%>ViewPost?PostId=<%=postId%>";
	}
	
	function goToNotify(url) 
	{
		windowName = "notifyWindow";
		larg = "740";
		haut = "600";
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	    if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
	        notifyWindow.close();
	    notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
	function deletePost(postId)
	{
		if (window.confirm("<%=resource.getString("blog.confirmDeletePost")%>"))
	    {
	    	document.postForm.action = "DeletePost";
	    	document.postForm.PostId.value = postId;
			document.postForm.submit();
	    }
	}
	
</script>
</head>

<body id="blog">
<div id="<%=instanceId %>">
		<div id="blogContainer">
		    <div id="bandeau"><a href="<%="Main"%>"><%=componentLabel%></a></div>
		    <div id="backHomeBlog"><a href="<%="Main"%>"><%=resource.getString("blog.accueil")%></a></div>
		<%
			String blocClass = "viewPost";
			String status = "";
          	if (PublicationDetail.DRAFT.equals(post.getPublication().getStatus())) {
           		blocClass = "viewPostDraft";
           		status = resource.getString("GML.saveDraft");
          	}
         %>
		  <div id="<%=blocClass%>">
		   	<div class="titreTicket"><%=post.getPublication().getName()%> <span class="status">(<%=status%>)</span>
			   	<%if (link != null && !link.equals("")) 
			   	{	%>
				  	<a href="<%=link%>"><img src="<%=resource.getIcon("blog.link")%>" border="0" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>' /></a>
				  <%}	%> 
				</div>
				<div class="infoTicket"><%=day%> <%=resource.getOutputDate(post.getDateEvent())%></div>
				<div class="contentTicket">
		      <%
		      	out.flush();
		     		getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+postId+"&ComponentId="+instanceId).include(request, response);
		     	%>
				</div>   
				<div class="footerTicket">    	
				   <span class="versCommentaires">
						&gt;&gt; <%=resource.getString("blog.comments")%> (<%=post.getNbComments()%>) 
				   </span>
		        <% if (!categoryId.equals("")) { %>
		         	<span class="categoryTicket">
		            &nbsp;|&nbsp;
		            <a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic">&gt;&gt; <%=post.getCategory().getName()%> </a>
		            </span>
		         <% } %>		       
		       <span class="creatorTicket"> 
	  	       &nbsp;|&nbsp;
		         <% // date de cr�ation et de modification %>
		         <%=resource.getString("GML.creationDate")%> <%=resource.getOutputDate(post.getPublication().getCreationDate())%> <%=resource.getString("GML.by")%> <%=post.getCreatorName() %>
		         <% if (!resource.getOutputDate(post.getPublication().getCreationDate()).equals(resource.getOutputDate(post.getPublication().getUpdateDate())) || !post.getPublication().getCreatorId().equals(post.getPublication().getUpdaterId())) 
		         {
		           UserDetail updater = m_MainSessionCtrl.getOrganizationController().getUserDetail(post.getPublication().getUpdaterId());
		           String updaterName = "Unknown";
		           if (updater != null)
		             updaterName = updater.getDisplayedName();
		           %>
		           - <%=resource.getString("GML.updateDate")%> <%=resource.getOutputDate(post.getPublication().getUpdateDate())%> <%=resource.getString("GML.by")%> <%=updaterName %>
		         <% } %>
		       </span>
		    </div>
		    <div class="separateur"></div>
		      <!--Afficher les commentaires-->
				<div class="commentaires">
					<% if (!isUserGuest) { %> 
						<form name="commentForm" action="AddComment" method="post">	
							<p class="txtlibform"><%=resource.getString("blog.addComment")%></p>
							<textarea rows="4" cols="100" name="Message"></textarea>
							<input type="hidden" name="PostId" value="<%=postId%>"/>
							<input type="hidden" name="CommentId" value=""/>
						</form>
	
						<%
						ButtonPane buttonPaneComment = gef.getButtonPane();
						buttonPaneComment.addButton(validateComment);
						buttonPaneComment.addButton(cancelButton);
						out.println("<br/><center>"+buttonPaneComment.print()+"</center><br/>");
						%>
	
						<hr />
					<% } %>
		
					<% if (comments != null) {
					  		ResourceLocator commentSettings = new ResourceLocator("com.stratelia.webactiv.util.comment.Comment","");
							boolean adminAllowedToUpdate = commentSettings.getBoolean("AdminAllowedToUpdate", true);
							Iterator itCom = (Iterator) comments.iterator();
							while (itCom.hasNext()) {
								Comment unComment = (Comment) itCom.next();
								String commentDate = resource.getOutputDate(unComment.getCreationDate());
								String ownerId = Integer.toString(unComment.getOwnerId());
								%>
									<div class="oneComment">
										<div>
											<div class="avatar">
												<img src="<%=m_context%><%=unComment.getOwnerDetail().getAvatar() %>"/>
											</div>
											<p class="author">
												<%=unComment.getOwnerDetail().getDisplayedName()%>
												<span class="date"> - <%=resource.getString("blog.postOn")%> <%=commentDate%></span>
											</p>
											<% if (ownerId.equals(userId)) { %>
												<div class="action">
													<a href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=postId%>)"><img src="<%=resource.getIcon("blog.smallUpdate") %>" alt="<%=resource.getString("GML.update")%>" title="<%=resource.getString("GML.update")%>" align="absmiddle"/></a>
													<a href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><img src="<%=resource.getIcon("blog.smallDelete") %>" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" align="absmiddle"/></a>
												</div>
											<% } else if ("admin".equals(profile)) { %>
												<div class="action">
													<% if (adminAllowedToUpdate) { %>
														<a href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=postId%>)"><img src="<%=resource.getIcon("blog.smallUpdate") %>" alt="<%=resource.getString("GML.update")%>" title="<%=resource.getString("GML.update")%>" align="absmiddle"/></a>
													<% } %>
													<a href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><img src="<%=resource.getIcon("blog.smallDelete") %>" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" align="absmiddle"/></a>
												</div>
											<% } %>
											<p class="message"><%=Encode.javaStringToHtmlParagraphe(unComment.getMessage())%></p>
										</div>
									</div>
								<%
							}
						}
						%>
					
					</div><!-- End commentaires-->
			</div>
				 
			<div id="navBlog">
				<% String myOperations = ""; 
			  // ajouter les op�rations dans cette chaine et la passer � afficher dans la colonneDroite.jsp.inc
			   if ("admin".equals(profile)) {
            myOperations += "<a href=\"EditPost?PostId="+postId+"\">"+resource.getString("blog.updatePost")+"</a><br/>";
            if (post.getPublication().getStatus().equals(PublicationDetail.DRAFT)) {
              myOperations += "<a href=\"DraftOutPost?PostId="+postId+"\">"+resource.getString("blog.draftOutPost")+"</a><br/>";
            }
            myOperations += "<a href=\"javascript:onClick=deletePost('"+postId+"')\">"+resource.getString("blog.deletePost")+"</a><br/>";
            myOperations += "<a href=\"javaScript:onClick=goToNotify('ToAlertUser?PostId="+postId+"')\" id=\"toNotify\">"+resource.getString("GML.notify")+"</a><br/>";
          } 
			   else if (!isUserGuest) { 
            myOperations += "<a href=\"javaScript:onClick=goToNotify('ToAlertUser?PostId="+postId+"')\">"+resource.getString("GML.notify")+"</a><br/>";
          }
				%>
				<%@ include file="colonneDroite.jsp.inc" %>
		  </div>
		
		 
  </div>
</div>

<form name="postForm" action="DeletePost" method="post">
	<input type="hidden" name="PostId"/>
</form>

</body>
</html>