<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page import="java.util.GregorianCalendar"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>

<% 
// recuperation des parametres
Collection<PostDetail>	posts		= (Collection) request.getAttribute("Posts");
Collection<NodeDetail>	categories	= (Collection) request.getAttribute("Categories");
Collection<Archive>		archives	= (Collection) request.getAttribute("Archives");
Collection<LinkDetail>	links		= (Collection) request.getAttribute("Links");
String 		profile		= (String) request.getAttribute("Profile");
String		blogUrl		= (String) request.getAttribute("Url");
String		rssURL		= (String) request.getAttribute("RSSUrl");
List		events		= (List) request.getAttribute("Events");
String 		dateCal		= (String) request.getAttribute("DateCalendar");
boolean   isPdcUsed	= ((Boolean) request.getAttribute("IsUsePdc")).booleanValue();
boolean   isDraftVisible  = ((Boolean) request.getAttribute("IsDraftVisible")).booleanValue();
int nbPostDisplayed   = ((Integer) request.getAttribute("NbPostDisplayed")).intValue();
String wallPaperName = (String) request.getAttribute("WallPaperName");
String wallPaperURL = (String) request.getAttribute("WallPaperURL");
String wallPaperSize = (String) request.getAttribute("WallPaperSize");
String styleSheetName = (String) request.getAttribute("StyleSheetName");
String styleSheetURL = (String) request.getAttribute("StyleSheetURL");
String styleSheetSize = (String) request.getAttribute("StyleSheetSize");

if(wallPaperName == null) {
  wallPaperName = "";
}
if(wallPaperURL == null) {
  wallPaperURL = "";
}
if(wallPaperSize == null) {
  wallPaperSize = "";
}
if(styleSheetName == null) {
  styleSheetName = "";
}
if(styleSheetURL == null) {
  styleSheetURL = "";
}
if(styleSheetSize == null) {
  styleSheetSize = "";
}

String 		word 		= "";
Date 	   dateCalendar	= new Date(dateCal);
boolean 	isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());

if (SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile)) || SilverpeasRole.publisher.equals(SilverpeasRole.valueOf(profile))) { 
	if (SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile)) && isPdcUsed) {
		operationPane.addOperation("useless", resource.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+instanceId+"','utilizationPdc1')");
		operationPane.addLine();
	}
	operationPane.addOperationOfCreation(resource.getIcon("blog.addPost"), resource.getString("blog.newPost"), "NewPost");
	if (SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile))) {
	 operationPane.addOperation("useless", resource.getString("blog.viewCategory"), "ViewCategory");
	
	 String url = m_context + blogUrl + "Main";
	 String lien = m_context + URLManager.getURL(URLManager.CMP_MYLINKSPEAS) + "ComponentLinks?InstanceId="+ instanceId + "&amp;UrlReturn=" + url;
	 operationPane.addOperation("useless", resource.getString("blog.viewLinks"), lien);
	 operationPane.addOperation("useless", resource.getString("blog.customize"), "javascript:onClick=customize();");
	 operationPane.addOperation("useless", resource.getString("blog.updateFooter"), "UpdateFooter");
	 operationPane.addLine();
	}
}
	
if (!isUserGuest) { 
  operationPane.addOperation("useless", resource.getString("blog.addSubscription"), "javascript:onClick=addSubscription()");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=componentLabel%></title>
<view:looknfeel/>
<% if (StringUtil.isDefined(rssURL)) { %>
	<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resource.getString("blog.rssLast")%>" href="<%=m_context+rssURL%>"/>
<% } %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function openSPWindow(fonction, windowName) {
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function sendData() {
	window.document.searchForm.action = "Search";
	window.document.searchForm.WordSearch.value = document.searchForm.WordSearch.value;
	window.document.searchForm.submit();
}

function addSubscription() {
	window.alert("<%=resource.getString("blog.addSubscriptionOk")%>");
	window.document.subscriptionForm.action = "AddSubscription";
	window.document.subscriptionForm.submit();
}

function customize() {
  $("#customizationDialog").dialog("open");
}

function areFilesCorrect(wallPaper, styleSheet) {
	var errorMsg = "";
  var errorNb = 0;
    
  switch(errorNb) {
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

$(function() {
  $("#customizationDialog").dialog({
  autoOpen: false,
  resizable: false,
  modal: true,
  height: "auto",
  width: 750,
  buttons: {
    "<%=resource.getString("GML.ok")%>": function() {
      var wallPaperNewFile = $("#customizationDialog #WallPaperNewFile").val();
      var styleSheetNewFile = $("#customizationDialog #StyleSheetNewFile").val();
      if (areFilesCorrect(wallPaperNewFile, styleSheetNewFile)) {
        document.customizationFiles.submit();
      }
    },
    "<%=resource.getString("GML.cancel")%>": function() {
      $(this).dialog("close");
     }
  }
  });
});

function hideWallPaperFile() {
  $("#customizationDialog #WallPaperFile").hide();
	document.customizationFiles.removeWallPaperFile.value = "yes";
}

function hideStyleSheetFile() {
	$("#customizationDialog #StyleSheetFile").hide();
	document.customizationFiles.removeStyleSheetFile.value = "yes";
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
			  <%@ include file="colonneDroite.jsp.inc" %>
			</div>
			<div id="postsList">
				<%
			    Iterator it = (Iterator) posts.iterator();
					
			    java.util.Calendar cal = GregorianCalendar.getInstance();
				  while (nbPostDisplayed > 0 && it.hasNext()) 
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
					 // le billet en mode brouillon n'est pas visible si ce n'est pas le createur
					 visible = false;
					 // sauf si le mode "brouillon visible" est actif et que le user est bloggeur
					 if (isDraftVisible && (SilverpeasRole.admin.equals(SilverpeasRole.valueOf(profile)) || SilverpeasRole.publisher.equals(SilverpeasRole.valueOf(profile)))) {
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
					<div class="separateur"><hr /></div>
				</div>
				 <%
				  // Fin du ticket
				  nbPostDisplayed --;
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
			
			<!-- Dialog to edit files to customize --> 
      <div id="customizationDialog" title="<%=resource.getString("blog.customize")%>">
			  <form name="customizationFiles" action="Customize" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
			    <table>			    
			      <tr id="WallPaper">
			        <td class="txtlibform"><%=resource.getString("blog.wallPaper")%></td>
			        <td>
			        <%
			        if(!"".equals(wallPaperURL)) {
			        %>
			         <div id="WallPaperFile">
			           <a href="<%=wallPaperURL%>" target="_blank"><%=wallPaperName%></a> 
			           <%=wallPaperSize%> 
			           <a href="javascript:onClick=hideWallPaperFile();"><img src="<%=resource.getIcon("blog.smallDelete")%>" border="0"></a> 
			           <BR/>
			         </div> 
			        <% 
			        }
			        %>
			         <input type="file" name="wallPaper" id="WallPaperNewFile" size="50"/> <i>(.gif/.jpg/.png)</i>
			         <input type="hidden" name="removeWallPaperFile" value="no"/>
			        </td>
			      </tr>
			      <tr id="StyleSheet">
			        <td class="txtlibform"><%=resource.getString("blog.styleSheet")%></td>
			        <td>
			        <%
              if(!"".equals(styleSheetURL)) {
              %>
               <div id="StyleSheetFile">
			           <a href="<%=styleSheetURL%>" target="_blank"><%=styleSheetName%></a> 
			           <%=styleSheetSize%> 
			           <a href="javascript:onClick=hideStyleSheetFile();"><img src="<%=resource.getIcon("blog.smallDelete")%>" border="0"></a> 
			           <BR/>
			         </div>
			        <% 
              }
              %>
			         <input type="file" name="styleSheet" id="StyleSheetNewFile" size="50"/> <i>(.css)</i>
			         <input type="hidden" name="removeStyleSheetFile" value="no"/>
			        </td>
			      </tr>
			    </table>
			  </form>
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