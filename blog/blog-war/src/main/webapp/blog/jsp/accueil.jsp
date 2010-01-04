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
Boolean		isUsePdc	= (Boolean) request.getAttribute("IsUsePdc");
String footer = (String) request.getAttribute("Footer");

String 		word 		= "";
Date 	   dateCalendar	= new Date(dateCal);
boolean		isPdcUsed 	= isUsePdc.booleanValue();
boolean 	isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<% if (StringUtil.isDefined(rssURL)) { %>
	<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resource.getString("blog.rssLast")%>" href="<%=m_context+rssURL%>"/>
<% } %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
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
  <div id="blogContainer">
    <div id="bandeau"><a href="<%="Main"%>"><%=componentLabel%></a></div>
    <div id="backHomeBlog"><a href="<%="Main"%>"><%=resource.getString("blog.accueil")%></a></div>
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
          %>
          <!--Debut d'un ticket-->
          <div id="post<%=postId%>" class="post">
            <div class="titreTicket">
              <a href="<%="ViewPost?PostId=" + postId%>" class="titreTicket"><%=post.getPublication().getName()%></a>
				        <%  if ( link != null && !link.equals("")) {  %>
				          <a href=<%=link%> ><img src=<%=resource.getIcon("blog.link")%> border="0" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>' ></a>
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
                <a href="<%="ViewPost?PostId=" + postId%>" class="versCommentaires">&gt;&gt; <%=resource.getString("blog.comments")%></a> (<%=post.getNbComments()%>) 
             </span>

              <span class="categoryTicket">
              <%
              if (!categoryId.equals(""))
              {  %>
                &nbsp;|&nbsp;
                <a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic">&gt;&gt; <%=post.getCategory().getName()%> </a>
              <% } %>
              </span>
              <span class="creatorTicket"> 
              &nbsp;|&nbsp;
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
            <div class="separateur" ></div>
        </div>
        <!--Fin du ticket-->  
       
        <% 
        }
        %>  
    </div>
    <div id="navBlog">
      <%
      String myOperations = "";
      // ajouter les opérations dans cette chaine et la passer à afficher dans la colonneDroite.jsp.inc
      if ("admin".equals(profile)) { 
        if (isPdcUsed) { 
          myOperations += "<a href=\"javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+instanceId+"','utilizationPdc1')\">"+resource.getString("GML.PDCParam")+"</a><br/>";
        } 
        myOperations += "<a href=\"NewPost\">"+resource.getString("blog.newPost")+"</a><br/>";
        myOperations += "<a href=\"UpdateFooter\">"+resource.getString("blog.updateFooter")+"</a><br/>";
      } 
      if (!isUserGuest) { 
        myOperations += "<a href=\"javascript:onClick=addSubscription()\">"+resource.getString("blog.addSubscription")+"</a><br/>";
      } 
      %>
      <%@ include file="colonneDroite.jsp.inc" %>
    </div>
    <div id="footer">
      <%
        out.flush();
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+instanceId+"&ComponentId="+instanceId).include(request, response);
      %>      
    </div>
  </div>
</div>

<form name="subscriptionForm" action="AddSubscription" Method="POST">
</form>

</body>
</html>