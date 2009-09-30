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


String 		word 		= "";
Date 	   dateCalendar	= new Date(dateCal);
boolean		isPdcUsed 	= isUsePdc.booleanValue();
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
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="blog">
	<tr>
    	<td colspan="3" id="bandeau" align="center"><a href="<%="Main"%>"><%=componentLabel%></a></td>
    	<td align="left" rowspan="3" valign="top">
    	<% if ("admin".equals(profile)) { %>
    		<% if (isPdcUsed) 
			{ %>
			&nbsp;<a href="<%="javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+instanceId+"','utilizationPdc1')"%>"><img src="<%=resource.getIcon("blog.pdcUtilizationSrc")%>" border="0" alt="<%=resource.getString("GML.PDCParam")%>" title="<%=resource.getString("GML.PDCParam")%>"/></a><br/>
			<% } %>
			&nbsp;<a href="<%="NewPost"%>"><img src="<%=resource.getIcon("blog.addPost")%>" border="0" alt="<%=resource.getString("blog.newPost")%>" title="<%=resource.getString("blog.newPost")%>"/></a>
		<% } %>
		&nbsp;<a href="<%="javascript:onClick=addSubscription()"%>"><img src="<%=resource.getIcon("blog.addSubscription")%>" border="0" alt="<%=resource.getString("blog.addSubscription")%>" title="<%=resource.getString("blog.addSubscription")%>"/></a>
		</td>
  	</tr>
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
		  			String link	= post.getPermalink();
					%>
					<!--Debut d'un ticket-->
				    <tr>
				       	<td>
				       		<a href="<%="ViewPost?PostId=" + postId%>" class="titreTicket"><%=post.getPublication().getName()%></a>
						<%	if ( link != null && !link.equals("")) {	%>
							<a href=<%=link%> ><img src=<%=resource.getIcon("blog.link")%> border="0" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>' ></a>
						<%	}	%>
				       	</td>
				    </tr>
				    <tr>
				    	<td class="infoTicket"><%=post.getCreatorName()%> - <%=resource.getOutputDate(post.getDateEvent())%></td>
				    </tr>
				    <tr>
				    	<td>&nbsp;</td>
				    </tr>
				    <tr>
					    <td>
				        <%
				        	out.flush();
			        		getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+postId+"&ComponentId="+instanceId).include(request, response);
			        	%>
			        	</td>
					</tr>
					<tr>
				    	<td>&nbsp;</td>
				    </tr>
				    <tr>
				    	<td>
							<span class="versCommentaires">
								<a href="<%="ViewPost?PostId=" + postId%>" class="versCommentaires">&gt;&gt; <%=resource.getString("blog.comments")%></a> (<%=post.getNbComments()%>) 
							</span>
							&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<%
							if (!categoryId.equals(""))
							{  %>
								<a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic">&gt;&gt; <%=post.getCategory().getName()%> </a>
							<% } %>
							&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<span class="versCommentaires"> 
								<% // date de création et de modification %>
								<%=resource.getString("GML.creationDate")%> <%=resource.getOutputDate(post.getPublication().getCreationDate())%> - 
								<%=resource.getString("GML.updateDate")%> <%=resource.getOutputDate(post.getPublication().getUpdateDate())%>
							</span>
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
		<td valign="top" class="colonneDroite">
			<%@ include file="colonneDroite.jsp.inc" %>
		</td>
	</tr>
</table>
</div>

<form name="subscriptionForm" action="AddSubscription" Method="POST">
</form>

</body>

</html>
