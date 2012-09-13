<%
Button searchButton = gef.getFormButton(" OK ", "javascript:onClick=search();", false);
%>
<script type="text/javascript">
	function selectDay(day) {
		location.href = "PostByDay?Day="+day;
	}

	function gotoPreviousMonth() {
		location.href = "PreviousMonth";
	}

	function gotoNextMonth() {
		location.href = "NextMonth";
	}
	
	function search() {
		document.searchForm.action = "Search";
		document.searchForm.submit();
	}
</script>

  
<div id="zoneRecherche" class="bgDegradeGris">
	<div id="titreRecherche" class="bgDegradeGris header">
	    <h4><%=resource.getString("GML.search")%></h4>
   </div>
   <form name="searchForm" action="Search" method="post">
		<input type="text" name="WordSearch" class="inputRecherche"/>
		<%
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(searchButton);
		out.print(buttonPane.print());
		%>
	</form>
</div>

<div id="operationBlog">
</div>

<view:areaOfOperationOfCreation/>
	
<div id="topics" class="bgDegradeGris">
	<div id="titreTopics" class="bgDegradeGris  header">
		<h4 class="clean">
			<%
			  if ("admin".equals(profile)) {
				out.println("<a href=\"ViewCategory\">"+resource.getString("GML.categories")+"</a>");
			  }
			  else {
				out.println(resource.getString("GML.categories"));
			  }
			%>
		</h4>
	</div>
	
	<% if (categories != null) { %>
			<ul>
			<% for (NodeDetail uneCategory : categories) { %>
					<li><a title="<%=uneCategory.getDescription()%>" href="<%="PostByCategory?CategoryId=" + uneCategory.getNodePK().getId()%>"><%=uneCategory.getName()%></a></li>
			<% } %>
			</ul>
	<% } %>
</div>


	
<div id="archives" class="bgDegradeGris" >
		<div id="titreArchive" class="bgDegradeGris header">
			<h4 class="clean"><%=resource.getString("blog.archives")%></h4>
		</div>
	
	   <% if (archives != null) { %>
			<ul>
			<% for (Archive uneArchive : archives) { %>
					<li><a href="<%="PostByArchive?BeginDate=" + uneArchive.getBeginDate() + "&amp;EndDate=" + uneArchive.getEndDate()%>"><%=resource.getString("GML.mois"+uneArchive.getMonthId())%> <%=uneArchive.getYear()%></a></li>
			<% } %>
			</ul>
		<% } %>
	</div>
	<div id="calendar" class="bgDegradeGris">
	   <%
			Calendar calendar = gef.getCalendar(m_context+"/agenda/jsp/", resource.getLanguage(), dateCalendar);
			calendar.setEmptyDayNonSelectable(true);
			calendar.setEvents(events);
		%>
		<%=calendar.print()%>
</div>
	<div id="liens" class="bgDegradeGris">
		<div id="titreLiens" class="bgDegradeGris  header"> 
			<h4 class="clean">
				<%
				if ("admin".equals(profile)) {
					String url = m_context + blogUrl + "Main";
						String lien = m_context + URLManager.getURL(URLManager.CMP_MYLINKSPEAS) + "ComponentLinks?InstanceId="+ instanceId + "&amp;UrlReturn=" + url;
						out.println("<a href=\""+lien+"\">"+resource.getString("blog.links")+"</a>");
				}
				else {
					out.println(resource.getString("blog.links"));
				}
				%>
			</h4>
		</div>
	
	   <% if (links != null)  { %>
			<ul>
			<%
		    for (LinkDetail unLink : links) {
		      	String nameLink = unLink.getName();
		      	if (nameLink.equals("")) {
		      	   nameLink = unLink.getUrl();
		      	}
		      	String lien = unLink.getUrl();
		      	String description = unLink.getDescription();
		      	if (lien.indexOf("://") == -1) {
					lien = m_context + lien;
				}
		      	String target = "";
		      	if (unLink.isPopup())
		      	   target = "target=\"_blank\"";
		      	%>
		        <li><a title="<%=description%>" href="<%=lien%>" <%=target%>><%=nameLink%></a></li>
		        <%
			}
			
			%>
			</ul>
		<% } %>
  </div>
	
	<% if (StringUtil.isDefined(rssURL)) { %>
	   <div class="separateur"><hr /></div>
	   <a href="<%=m_context+rssURL%>"><img src="<%=m_context+"/util/icons/rss.gif" %>" border="0" alt="rss"/></a>
	<% } %>
