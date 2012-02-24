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
<%@page import="java.util.GregorianCalendar"%>
<%@ include file="check.jsp" %>

<% 
// récupération des paramètres
Collection	posts		= (Collection) request.getAttribute("Posts");
Collection	categories	= (Collection) request.getAttribute("Categories");
Collection	archives	= (Collection) request.getAttribute("Archives");
Collection	links		= (Collection) request.getAttribute("Links");
String 		profile		= (String) request.getAttribute("Profile");
String		blogUrl		= (String) request.getAttribute("Url");
String		rssURL		= (String) request.getAttribute("RSSUrl");
List		events		= (List) request.getAttribute("Events");
String 		dateCal		= (String) request.getAttribute("DateCalendar");
boolean   isPdcUsed	= ((Boolean) request.getAttribute("IsUsePdc")).booleanValue();
String footer = (String) request.getAttribute("Footer");

boolean   isDraftVisible  = ((Boolean) request.getAttribute("IsDraftVisible")).booleanValue();

String 		word 		= "";
Date 	   dateCalendar	= new Date(dateCal);
boolean 	isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><%=componentLabel%></title>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<% if (StringUtil.isDefined(rssURL)) { %>
		<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resource.getString("blog.rssLast")%>" href="<%=m_context+rssURL%>"/>
	<% } %>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript">
	function openSPWindow(fonction, windowName)
	{
		pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
	}

	function sendData() 
	{
		window.document.searchForm.action = "Search";
		window.document.searchForm.WordSearch.value = document.searchForm.WordSearch.value;
		window.document.searchForm.submit();
	}

	function addSubscription()
	{
		window.alert("<%=resource.getString("blog.addSubscriptionOk")%>");
		window.document.subscriptionForm.action = "AddSubscription";
		window.document.subscriptionForm.submit();
	}

	</script>
</head>

<body id="blog">
	<div id="<%=instanceId %>">

	<%
	out.println(window.printBefore());
	%>
		<div id="blogContainer">
			<div id="bandeau"><h2><a href="<%="Main"%>"><%=componentLabel%></a></h2></div>
			<div id="navBlog">
			  <%
			  String myOperations = "<ul class=\"yuimenu\">";
			  // ajouter les opérations dans cette chaine et la passer à afficher dans la colonneDroite.jsp.inc
			  if ("admin".equals(profile)) { 
				if (isPdcUsed) { 
				  myOperations += "<li><a href=\"javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+instanceId+"','utilizationPdc1')\">"+resource.getString("GML.PDCParam")+"</a></li>";
				} 
				myOperations += "<li><a href=\"NewPost\">"+resource.getString("blog.newPost")+"</a></li>";
				myOperations += "<li><a href=\"ViewCategory\">"+resource.getString("blog.viewCategory")+"</a></li>";
				String url = m_context + blogUrl + "Main";
				String lien = m_context + URLManager.getURL(URLManager.CMP_MYLINKSPEAS) + "ComponentLinks?InstanceId="+ instanceId + "&amp;UrlReturn=" + url;
				myOperations += "<li><a href=\"+lien+\">"+resource.getString("blog.viewLinks")+"</a></li>";
				myOperations += "<li><a href=\"UpdateFooter\" id=\"toUpdateFooter\">"+resource.getString("blog.updateFooter")+"</a></li>";
			
			  } 
			  if (!isUserGuest) { 
				myOperations += "<li><a href=\"javascript:onClick=addSubscription()\" id=\"subscription\">"+resource.getString("blog.addSubscription")+"</a></li>";
			  } 
			  myOperations += "</ul>";
			  %>
			  <%@ include file="colonneDroite.jsp.inc" %>
			</div>
			<div id="postsList">
				<%
				Iterator it = (Iterator) posts.iterator();
					
				java.util.Calendar cal = GregorianCalendar.getInstance();
				  while (it.hasNext()) 
				  {
					PostDetail post = (PostDetail) it.next();
					String categoryId = "";
					if (post.getCategory() != null)
					  categoryId = post.getCategory().getNodePK().getId();
					String postId = post.getPublication().getPK().getId();
					String link = post.getPermalink();
				  
				  //Debut d'un ticket
				  String blocClass = "post";
				  String status = "";
				  if (post.getPublication().getStatus().equals(PublicationDetail.DRAFT)) {
					blocClass = "postDraft";
					status = resource.getString("GML.saveDraft");
				  }
				  boolean visible = true;
				  if (post.getPublication().getStatus().equals(PublicationDetail.DRAFT) 
					  && !post.getPublication().getCreatorId().equals(userId)) {
					// le billet en mode brouillon n'est pas visible si ce n'est pas le créateur
					visible = false;
					// sauf si le mode "brouillon visible" est actif et que le user est bloggeur
					if (isDraftVisible && "admin".equals(profile)) {
					  visible = true;
					}
				  }

				  if (visible) {
				  %>
				  
				  <div id="post<%=postId%>" class="<%=blocClass%>">
					<div class="titreTicket">
					  <a href="<%="ViewPost?PostId=" + postId%>"><%=post.getPublication().getName()%></a> <span class="status">(<%=status%>)</span>
								<%  if ( link != null && !link.equals("")) {  %>
								  <span class="permalink"><a href="<%=link%>"><img src="<%=resource.getIcon("blog.link")%>" border="0" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>'/></a></span>
								<%  } %>
					</div>
					<%
					  cal.setTime(post.getDateEvent());
					  String day = resource.getString("GML.jour"+cal.get(java.util.Calendar.DAY_OF_WEEK));
				   %>
				   <div class="infoTicket"><%=day%> <%=resource.getOutputDate(post.getDateEvent())%></div>
				   <div class="contentTicket">
					 <%
					   out.flush();
					   getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+postId+"&ComponentId="+instanceId).include(request, response);
					 %>
				   </div>

				   <div class="footerTicket">
					 <span class="versCommentaires">
						<a href="<%="ViewPost?PostId=" + postId%>#commentaires" class="versCommentaires">&gt;&gt; <%=resource.getString("blog.comments")%> (<%=post.getNbComments()%>) </a> 
					 </span>

					  <% if (!categoryId.equals("")) {  %>
						<span class="categoryTicket">
							<span class="sep">&nbsp;|&nbsp;</span>
						<a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic">&gt;&gt; <%=post.getCategory().getName()%> </a>
						</span>
					  <% } %>
					  
					  <span class="creatorTicket"> 
						<span class="sep">&nbsp;|&nbsp;</span>
						<% // date de création et de modification %>
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
					<div class="separateur"><hr /></div>
				</div>
				 <%
				  // Fin du ticket
				  }
				 }
				%>  
			</div>
			
			<div id="footer">
			  <%
				out.flush();
				getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+instanceId+"&ComponentId="+instanceId).include(request, response);
			  %>      
			</div>
		</div>
	</div>

<form name="subscriptionForm" action="AddSubscription" method="post">
</form>
<%
out.println(window.printAfter());
%>  
</body>
</html>