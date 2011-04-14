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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@page import="java.util.GregorianCalendar"%>
<%@ include file="check.jsp" %>

<% 
// r�cup�ration des param�tres
PostDetail	post		= (PostDetail) request.getAttribute("Post");
Collection	categories	= (Collection) request.getAttribute("Categories");
Collection	archives	= (Collection) request.getAttribute("Archives");
Collection	links		= (Collection) request.getAttribute("Links");
String 		profile		= (String) request.getAttribute("Profile");
String		blogUrl		= (String) request.getAttribute("Url");
String		rssURL		= (String) request.getAttribute("RSSUrl");
List		events		= (List) request.getAttribute("Events");
String 		dateCal		= (String) request.getAttribute("DateCalendar");

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
		    <view:comments userId="<%=userId %>" componentId="<%=instanceId %>" resourceId="<%=postId %>" indexed="true"/>
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