<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@page import="java.util.GregorianCalendar"%>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.components.blog.model.PostDetail" %>
<%@page import="org.silverpeas.components.blog.control.StyleSheet"%>
<%@page import="org.silverpeas.components.blog.control.WallPaper"%>
<%@ page import="org.silverpeas.core.notification.user.NotificationContext" %>
<%@ include file="check.jsp" %>

<% 
// recuperation des parametres
PostDetail post		= (PostDetail) request.getAttribute("Post");
Collection<NodeDetail>	categories	= (Collection) request.getAttribute("Categories");
Collection<Archive>		archives	= (Collection) request.getAttribute("Archives");
Collection<LinkDetail>	links		= (Collection) request.getAttribute("Links");
String 		profile		= (String) request.getAttribute("Profile");
String		blogUrl		= (String) request.getAttribute("Url");
String		rssURL		= (String) request.getAttribute("RSSUrl");
List		events		= (List) request.getAttribute("Events");
String 		dateCal		= (String) request.getAttribute("DateCalendar");
WallPaper wallPaper = (WallPaper) request.getAttribute("WallPaper");
StyleSheet styleSheet = (StyleSheet) request.getAttribute("StyleSheet");

  Date 	   dateCalendar	= DateUtil.parse(dateCal);
  String categoryId = "";
  if (post.getCategory() != null) {
    categoryId = post.getCategory().getNodePK().getId();
  }
  String postResourceType = post.getContributionType();
  String postId = post.getPublication().getPK().getId();
  String link = post.getPermalink();

  java.util.Calendar cal = GregorianCalendar.getInstance();
  cal.setTime(post.getDateEvent());
  String day = resource.getString("GML.jour" + cal.get(java.util.Calendar.DAY_OF_WEEK));

  if (SilverpeasRole.ADMIN.equals(SilverpeasRole.fromString(profile)) ||
      SilverpeasRole.PUBLISHER.equals(SilverpeasRole.fromString(profile))) {
    operationPane.addOperation("useless", resource.getString("blog.updatePost"),
        "EditPost?PostId=" + postId);
    if (post.getPublication().getStatus().equals(PublicationDetail.DRAFT_STATUS)) {
      operationPane.addOperation("useless", resource.getString("blog.draftOutPost"),
          "DraftOutPost?PostId=" + postId);
    }
    operationPane.addOperation("useless", resource.getString("blog.deletePost"),
        "javascript:onClick=deletePost('" + postId + "')");
    operationPane.addLine();
  }
  if (!m_MainSessionCtrl.getCurrentUserDetail().isAccessGuest()) {
    operationPane.addOperation("useless", resource.getString("GML.notify"),
        "javaScript:onClick=sp.messager.open('" + instanceId + "', {" + NotificationContext.CONTRIBUTION_ID + ": '" + postId + "'});");
  }
%>

<c:set var="post" value="<%=post%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=language%>">
<head>
<title></title>
<view:looknfeel withCheckFormScript="true"/>
<% if(wallPaper != null) { %>
<style type="text/css">
#blog #blogContainer #bandeau {
  background:url("<%=wallPaper.getUrl()%>") center no-repeat;
}
</style>
<% } %>
  
<% if(styleSheet != null) { %>
<style type="text/css">
  <%=styleSheet.getContent()%>
</style>
<% } %>
<script type="text/javascript">

	function deletePost(postId) {
    var label = "<%=resource.getString("blog.confirmDeletePost")%>";
    jQuery.popup.confirm(label, function() {
      document.postForm.action = "DeletePost";
      document.postForm.PostId.value = postId;
      document.postForm.submit();
    });
	}
</script>
</head>

<body id="blog">
<div id="<%=instanceId %>">
<%
out.println(window.printBefore());
%>
	<div id="blogContainer">
	<div id="bandeau"><h2><a class="txttitrecol" href="<%="Main"%>"><%=componentLabel%></a></h2></div>
		     
		<%
			String blocClass = "viewPost";
			String status = "";
          	if (post.getPublication().isDraft()) {
           		blocClass = "viewPostDraft";
           		status = resource.getString("GML.draft");
          	}
         %>
		 
		  <div id="navBlog">
			<%@ include file="colonneDroite.jsp" %>
		  </div>
		 
		  <div id="<%=blocClass%>">
        <div class="titreTicket"><%=WebEncodeHelper.javaStringToHtmlString(post.getPublication().getName())%> <span class="status">(<%=status%>)</span>
			   	<%if (link != null && !link.equals("")) 
			   	{	%>
				  	<a class="sp-permalink" href="<%=link%>"><img src="<%=resource.getIcon("blog.link")%>" border="0" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>' /></a>
				  <%}	%> 
				</div>
				<div class="infoTicket"><%=day%> <%=resource.getOutputDate(post.getDateEvent())%></div>
				<% if (!categoryId.equals("")) { %>
				  <div id="list-categoryTicket">
		         	<span class="categoryTicket">
		            <a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic"><%=post.getCategory().getName()%> </a>
		            </span>
					</div>
		         <% } %>		       
				<div class="contentTicket">
				<view:displayWysiwyg objectId="<%=postId%>" componentId="<%=instanceId %>" language="<%=resource.getLanguage() %>" />
				</div>   
				<div class="footerTicket">    	
				   <span class="versCommentaires">
						<img alt="commentaires" src="<%=resource.getIcon("blog.commentaires")%>" /> <%=post.getNbComments()%>
				   </span>
		        
		       <span class="creatorTicket"> 
	  	       &nbsp;|&nbsp;
		         <% // date de cr�ation et de modification %>
		         <%=resource.getString("GML.creationDate")%> <%=resource.getOutputDate(post.getPublication().getCreationDate())%> <%=resource.getString("GML.by")%> <view:username userId="<%=post.getPublication().getCreatorId()%>" />
		         <% if (!resource.getOutputDate(post.getPublication().getCreationDate()).equals(resource.getOutputDate(post.getPublication().getUpdateDate())) || !post.getPublication().getCreatorId().equals(post.getPublication().getUpdaterId())) { %>
		           - <%=resource.getString("GML.updateDate")%> <%=resource.getOutputDate(post.getPublication().getUpdateDate())%> <%=resource.getString("GML.by")%> <view:username userId="<%=post.getPublication().getUpdaterId()%>" />
		         <% } %>
		       </span>
		    </div>
		    <div class="separateur"><hr /></div>
        <view:componentParam var="commentsActivated" componentId="<%=instanceId%>" parameter="comments"/>
        <c:if test="${empty commentsActivated or silfn:booleanValue(commentsActivated)}">
          <view:comments 	userId="<%=userId %>" componentId="<%=instanceId %>"
                  resourceType="<%=postResourceType %>" resourceId="<%=postId %>" indexed="true"/>
        </c:if>
			</div>
	<div id="footer">
      <%
        out.flush();
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+instanceId+"&ComponentId="+instanceId).include(request, response);
      %>      
    </div>	 
	</div>

<form name="postForm" action="DeletePost" method="post">
	<input type="hidden" name="PostId"/>
</form>
<%
out.println(window.printAfter());
%> 

</div>
</body>
</html>
