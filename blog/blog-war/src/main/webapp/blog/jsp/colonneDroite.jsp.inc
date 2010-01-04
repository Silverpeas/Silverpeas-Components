<%
Button searchButton = gef.getFormButton(" OK ", "javascript:onClick=search();", false);
%>
<script language="javascript">
	function selectDay(day) 
	{
		location.href = "PostByDay?Day="+day;
	}

	function gotoPreviousMonth()
	{
		location.href = "PreviousMonth";
	}

	function gotoNextMonth()
	{
		location.href = "NextMonth";
	}
	
	function search() 
	{
		document.searchForm.action = "Search";
		document.searchForm.submit();
	}
</script>

  <div class="zoneRecherche">
	   <div class="titreRecherche">
	       <%=resource.getString("GML.search")%>
	   </div>
	   <table width="100%" border="0" cellspacing="0" cellpadding="0">
		    <form name="searchForm" action="Search" Method="POST">	
		      <tr>
		        <td>
		          <input type="text" name="WordSearch" class="inputRecherche">
		        </td>
		        <td valign="top">
				      <%
				      ButtonPane buttonPane = gef.getButtonPane();
				      buttonPane.addButton(searchButton);
				      out.print(buttonPane.print());
				      %>
				    </td>
				  </tr>
		    </form>
		  </table>
	</div>
	<br />
	<div id="operationBlog">
      <%=myOperations%>
	</div>
	<br />
	<div id="titreTopics">
		  <%
		  if ("admin".equals(profile)) {
		    out.println("<a href=\"ViewCategory\">"+resource.getString("GML.categories")+"</a>");
		  }
		  else {
		    out.println(resource.getString("GML.categories"));
		  }
		  %>
	</div>
	<div id="topics">
	   <%
		 if (categories != null) {
        Iterator itCat = (Iterator) categories.iterator();
		    while (itCat.hasNext()) {
		        NodeDetail uneCategory = (NodeDetail) itCat.next();
		      	%>
		        <a href="<%="PostByCategory?CategoryId=" + uneCategory.getNodePK().getId()%>"><%=uneCategory.getName()%></a><br />
		        <%
				}
		 }
		 %>
	</div>
	<div class="separateur">&nbsp;</div>
	<div class="intfdcolor6">
	   <%
	   Calendar calendar = gef.getCalendar(m_context+"/agenda/jsp/", resource.getLanguage(), dateCalendar);
		 calendar.setEmptyDayNonSelectable(true);
		 calendar.setEvents(events);
		 %>
		 <BR><center><%=calendar.print()%></center><BR>
	</div>
	<br />
	<div id="titreArchive"><%=resource.getString("blog.archives")%></div>
	<div id="archives">
	   <%
		 if (archives != null) {
		    Iterator itAr = (Iterator) archives.iterator();
		    while (itAr.hasNext())  {
		        Archive uneArchive = (Archive) itAr.next();
		      	%>
		        <a href="<%="PostByArchive?BeginDate=" + uneArchive.getBeginDate() + "&EndDate=" + uneArchive.getEndDate()%>"><%=resource.getString("GML.mois"+uneArchive.getMonthId())%> <%=uneArchive.getYear()%></a><br />
		        <%
				 }
		 }
		 %>
	</div>
	<div class="separateur">&nbsp;</div>
	<div id="titreLiens"> 
	   <%
		 if ("admin".equals(profile)) {
		    String url = m_context + blogUrl + "Main";
				String lien = m_context + URLManager.getURL(URLManager.CMP_MYLINKSPEAS) + "ComponentLinks?InstanceId="+ instanceId + "&UrlReturn=" + url;
				out.println("<a href=\""+lien+"\">"+resource.getString("blog.links")+"</a>");
		 }
		 else {
		    out.println(resource.getString("blog.links"));
		 }
		 %>
	</div>
	<div id="liens">
	   <%
		 if (links != null)  {
		    Iterator itL = (Iterator) links.iterator();
		    while (itL.hasNext()) {
		        LinkDetail unLink = (LinkDetail) itL.next();
		      	String nameLink = "";
		      	nameLink = unLink.getName();
		      	if (nameLink.equals(""))
		      	   nameLink = unLink.getUrl();
		      	String lien = unLink.getUrl();
		      	if (lien.indexOf("://") == -1) {
               lien = m_context + lien;
            }
		      	String target = "";
		      	if (unLink.isPopup())
		      	   target = "_blank";
		      	%>
		        <a href="<%=lien%>" target="<%=target%>"><%=nameLink%></a><br />
		        <%
				}
		 }
		 %>
  </div>
	<div class="separateur">&nbsp;</div>
	
	<% if (StringUtil.isDefined(rssURL)) { %>
	   <table>
	       <tr>
	         <td><a href="<%=m_context+rssURL%>"><img src="icons/rss.gif" border="0"></a></td>
	   		 </tr>
	   		 <link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resource.getString("blog.rssLast")%>" href="<%=m_context+rssURL%>">
	   </table>
	<% } %>
