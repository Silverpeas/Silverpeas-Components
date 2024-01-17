<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="org.silverpeas.components.blog.control.WallPaper"%>
<%@page import="org.silverpeas.components.blog.control.StyleSheet"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationControllerProvider" %>
<%@ page import="org.silverpeas.components.blog.model.Archive" %>
<%@ page import="org.silverpeas.components.blog.model.PostDetail" %>
<%@ page import="org.silverpeas.core.mylinks.model.LinkDetail" %>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="org.silverpeas.core.admin.user.model.SilverpeasRole" %>
<%@ page import="org.silverpeas.core.node.model.NodeDetail" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
// recuperation des parametres
Collection<PostDetail>  posts   = (Collection<PostDetail>) request.getAttribute("Posts");
Collection<NodeDetail>  categories  = (Collection<NodeDetail>) request.getAttribute("Categories");
Collection<Archive>   archives  = (Collection<Archive>) request.getAttribute("Archives");
Collection<LinkDetail>  links   = (Collection<LinkDetail>) request.getAttribute("Links");
Boolean  isUserSubscribed   = (Boolean) request.getAttribute("IsUserSubscribed");
String    profile   = (String) request.getAttribute("Profile");
String    blogUrl   = (String) request.getAttribute("Url");
String    rssURL    = (String) request.getAttribute("RSSUrl");
List    events    = (List) request.getAttribute("Events");
String    dateCal   = (String) request.getAttribute("DateCalendar");
Boolean   isPdcUsed = (Boolean) request.getAttribute("IsUsePdc");
Boolean   isDraftVisible  = (Boolean) request.getAttribute("IsDraftVisible");
int nbPostDisplayed   = (Integer) request.getAttribute("NbPostDisplayed");
WallPaper wallPaper = (WallPaper) request.getAttribute("WallPaper");
StyleSheet styleSheet = (StyleSheet) request.getAttribute("StyleSheet");
Date dateCalendar = DateUtil.parse(dateCal);

if (SilverpeasRole.ADMIN.equals(SilverpeasRole.fromString(profile)) || SilverpeasRole.PUBLISHER.equals(SilverpeasRole.fromString(profile))) {
  if (SilverpeasRole.ADMIN.equals(SilverpeasRole.fromString(profile))) {
    if (isPdcUsed) {
      operationPane.addOperation("useless", resource.getString("GML.PDCParam"),
          "javascript:onClick=openSPWindow('" + m_context +
              "/RpdcUtilization/jsp/Main?ComponentId=" + instanceId + "','utilizationPdc1')");
    }
    operationPane.addOperation("useless", resource.getString("GML.manageSubscriptions"), "ManageSubscriptions");
    operationPane.addLine();
  }
  operationPane.addOperationOfCreation(resource.getIcon("blog.addPost"), resource.getString("blog.newPost"), "NewPost");
  if (SilverpeasRole.ADMIN.equals(SilverpeasRole.fromString(profile))) {
   operationPane.addOperation("useless", resource.getString("blog.viewCategory"), "ViewCategory");

   String url = m_context + blogUrl + "Main";
   String lien = m_context + URLUtil.getURL(URLUtil.CMP_MYLINKSPEAS) + "ComponentLinks?InstanceId="+ instanceId + "&amp;UrlReturn=" + url;
   operationPane.addOperation("useless", resource.getString("blog.viewLinks"), lien);
   operationPane.addOperation("useless", resource.getString("blog.customize"), "javascript:onClick=customize();");
   operationPane.addOperation("useless", resource.getString("blog.updateFooter"), "UpdateFooter");
   operationPane.addLine();
  }
}

if (!m_MainSessionCtrl.getCurrentUserDetail().isAccessGuest() && isUserSubscribed != null) {
  operationPane.addOperation("useless", "<span id='subscriptionMenuLabel'></span>", "javascript:spSubManager.switchUserSubscription()");
}
%>
<view:sp-page angularJsAppName="silverpeas.blog">
<view:sp-head-part withCheckFormScript="true">
<view:includePlugin name="blog"/>
<view:includePlugin name="toggle"/>
<view:includePlugin name="subscription"/>

<c:set var="wallPaper" value="<%=wallPaper != null ? wallPaper : new WallPaper()%>"/>
<c:set var="styleSheet" value="<%=styleSheet != null ? styleSheet : new StyleSheet()%>"/>

<% if (StringUtil.isDefined(rssURL)) { %>
<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resource.getString("blog.rssLast")%>" href="<%=m_context+rssURL%>"/>
<% } %>
  <script type="text/javascript">

  SUBSCRIPTION_PROMISE.then(function() {
    window.spSubManager = new SilverpeasSubscriptionManager('<%=instanceId%>');
  });

  function openSPWindow(fonction, windowName) {
    pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
  }

  function sendData() {
    window.document.searchForm.action = "Search";
    window.document.searchForm.WordSearch.value = document.searchForm.WordSearch.value;
    window.document.searchForm.submit();
  }

  function customize() {
    blogApp.manager.openPersonalization();
  }

  whenSilverpeasReady(function() {
    window.blogApp = SpVue.createApp({
      data : function() {
        return {
          manager : undefined,
          wallpaper : ${wallPaper.asJson()},
          stylesheet : ${styleSheet.asJson()}
        }
      },
      methods : {
        customizeChange : function() {
          sp.navRequest(location.href).go();
        }
      }
    }).mount('#blog-app');
  });
  </script>
  <c:if test="${not empty wallPaper.url}">
    <style>
      #blog #blogContainer #bandeau {
        background: url("${wallPaper.url}") center no-repeat;
      }
    </style>
  </c:if>
  <c:if test="${not empty styleSheet.content}">
    <style>
      ${styleSheet.content}
    </style>
  </c:if>
</view:sp-head-part>
<view:sp-body-part id="blog">
<div id="<%=instanceId %>">
  <%
  out.println(window.printBefore());
  %>
  <div id="blog-app">
    <silverpeas-blog-management v-on:api="manager = $event"
                                v-bind:wallpaper="wallpaper"
                                v-bind:stylesheet="stylesheet"
                                v-on:customize-change="customizeChange"></silverpeas-blog-management>
  </div>
  <div id="blogContainer">
    <div id="bandeau">
      <h2><a href="<%="Main"%>"><%=componentLabel%></a></h2>
    </div>
    <view:componentInstanceIntro componentId="<%=instanceId%>" language="<%=resource.getLanguage()%>"/>
    <div id="navBlog">
      <%@ include file="colonneDroite.jsp" %>
    </div>
    <div id="postsList">
      <%
          Iterator it = (Iterator) posts.iterator();

          java.util.Calendar cal = GregorianCalendar.getInstance();
          while (nbPostDisplayed > 0 && it.hasNext())
          {
          PostDetail post = (PostDetail) it.next();
          String categoryId = "";
          if (post.getCategory() != null) {
            categoryId = post.getCategory().getNodePK().getId();
          }
          String postId = post.getPublication().getPK().getId();
          String link = post.getPermalink();

          //Debut d'un ticket
          String blocClass = "post";
          String status = "";
          if (post.getPublication().isDraft()) {
          	blocClass = "postDraft";
          	status = resource.getString("GML.draft");
          }
          boolean visible = true;
          if (post.getPublication().isDraft() && !post.getPublication().getCreatorId().equals(userId)) {
           // le billet en mode brouillon n'est pas visible si ce n'est pas le createur
           visible = false;
           // sauf si le mode "brouillon visible" est actif et que le user est bloggeur
           if (isDraftVisible && (SilverpeasRole.ADMIN.equals(SilverpeasRole.fromString(profile)) || SilverpeasRole.PUBLISHER
               .equals(SilverpeasRole.fromString(profile)))) {
            visible = true;
           }
          }

          if (visible) {
          %>
      <div id="post<%=postId%>" class="post <%=blocClass%>">
        <div class="titreTicket"> <a href="<%="ViewPost?PostId=" + postId%>"><%=WebEncodeHelper
            .javaStringToHtmlString(post.getPublication().getName())%></a> <span class="status">(<%=status%>)</span>
          <%  if ( link != null && !link.equals("")) {  %>
          <span class="permalink sp-permalink"><a href="<%=link%>"><img src="<%=resource.getIcon("blog.link")%>" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>'/></a></span>
          <%  } %>
        </div>

        <%
            cal.setTime(post.getDateEvent());
            String day = resource.getString("GML.jour"+cal.get(java.util.Calendar.DAY_OF_WEEK));
           %>

		  <div class="infoTicket"><%=day%> <%=resource.getOutputDate(post.getDateEvent())%></div>

		   <% if (!categoryId.equals("")) {  %>
		     <div id="list-categoryTicket">
			  <span class="categoryTicket">  <a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic"><%=post.getCategory().getName()%> </a> </span>
			 </div>
			  <% } %>
        <div class="contentTicket rich-content">
          <view:displayWysiwyg objectId="<%=postId%>" componentId="<%=instanceId %>" language="<%=resource.getLanguage() %>" />
        </div>
        <div class="footerTicket">
			<span class="versCommentaires"> <a href="<%="ViewPost?PostId=" + postId%>#commentaires" class="versCommentaires"><img alt="commentaires" src="<%=resource.getIcon("blog.commentaires")%>" /> <%=post.getNbComments()%> </a> </span>
			  <span class="creatorTicket"> <span class="sep">&nbsp;|&nbsp;</span>
			  <% // date de cr�ation et de modification %>
			  <%=resource.getString("GML.creationDate")%> <%=resource.getOutputDate(post.getPublication().getCreationDate())%> <%=resource.getString("GML.by")%> <%=post.getCreatorName() %>
			  <% if (!resource.getOutputDate(post.getPublication().getCreationDate()).equals(resource.getOutputDate(post.getPublication().getLastUpdateDate())) || !post.getPublication().getCreatorId().equals(post.getPublication().getUpdaterId()))
				   {
				  User updater = User.getById(post.getPublication().getUpdaterId());
				  String updaterName = "Unknown";
				  if (updater != null)
				  updaterName = updater.getDisplayedName();
				%>
			  - <%=resource.getString("GML.updateDate")%> <%=resource.getOutputDate(post.getPublication().getLastUpdateDate())%> <%=resource.getString("GML.by")%> <%=updaterName %>
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
      <c:import url='<%="/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+instanceId+"&ComponentId="+instanceId%>'/>
    </div>
</div>
<%
out.println(window.printAfter());
%>

  </div>
</view:sp-body-part>
</view:sp-page>
