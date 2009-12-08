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
</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
			   	<tr>
			       	<td>
			       		<table width="100%" border="0" cellpadding="0" cellspacing="0" class="zoneRecherche">
			       		<tr>
			    			<td class="titreRecherche"><%=resource.getString("GML.search")%></td>
			    		</tr>			       			
						<tr>
				   			<td>
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
				   			</td>
						</tr>				    		
			       		</table>
			       	</td>
			   	</tr>
			   	<tr>
				   	<td>&nbsp;</td>
				</tr>
			    <tr>
		        	<td id="titreTopics">
				   	<%
					if ("admin".equals(profile))
					{
						out.println("<a href=\"ViewCategory\">"+resource.getString("GML.categories")+"</a>");
					}
					else
					{
						out.println(resource.getString("GML.categories"));
					}
				   	%>
				   	</td>
				</tr>
				<tr>
		        	<td id="topics">
		        	<%
		      		if (categories != null)
		      		{
		      			Iterator itCat = (Iterator) categories.iterator();
		      			while (itCat.hasNext()) 
				  		{
		      				NodeDetail uneCategory = (NodeDetail) itCat.next();
		      				%>
		        			<a href="<%="PostByCategory?CategoryId=" + uneCategory.getNodePK().getId()%>"><%=uneCategory.getName()%></a><br />
		        			<%
				  		}
		      		}
		      		%>
	      		<tr>
				<tr>
				   	<td class="separateur">&nbsp;</td>
				</tr>
				<tr>
					<td class="intfdcolor6">
							<%
						   	Calendar calendar = gef.getCalendar(m_context+"/agenda/jsp/", resource.getLanguage(), dateCalendar);
							calendar.setEmptyDayNonSelectable(true);
							calendar.setEvents(events);
							%>
							<BR><center><%=calendar.print()%></center><BR>
					</td>
				</tr>
				<tr>
				   	<td>&nbsp;</td>
				</tr>
				<tr>
				   	<td id="titreArchive"><%=resource.getString("blog.archives")%></td>
				</tr>
				<tr>
				   	<td id="archives">
		        	<%
		      		if (archives != null)
		      		{
		      			Iterator itAr = (Iterator) archives.iterator();
		      			while (itAr.hasNext()) 
				  		{
		      				Archive uneArchive = (Archive) itAr.next();
		      				%>
		        			<a href="<%="PostByArchive?BeginDate=" + uneArchive.getBeginDate() + "&EndDate=" + uneArchive.getEndDate()%>"><%=resource.getString("GML.mois"+uneArchive.getMonthId())%> <%=uneArchive.getYear()%></a><br />
		        			<%
				  		}
		      		}
		      		%>
				   	</td>
				</tr>
				<tr>
				   	<td class="separateur">&nbsp;</td>
				</tr>
				<tr>
				   	<td id="titreLiens"> 
				   	<%
					if ("admin".equals(profile))
					{
						String url = m_context + blogUrl + "Main";
						String lien = m_context + URLManager.getURL(URLManager.CMP_MYLINKSPEAS) + "ComponentLinks?InstanceId="+ instanceId + "&UrlReturn=" + url;
						out.println("<a href=\""+lien+"\">"+resource.getString("blog.links")+"</a>");
					}
					else
					{
						out.println(resource.getString("blog.links"));
					}
				   	%>
				   	</td>
				</tr>
				<tr>
				   	<td id="liens">
		        	<%
		      		if (links != null)
		      		{
		      			Iterator itL = (Iterator) links.iterator();
		      			while (itL.hasNext()) 
				  		{
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
				   	</td>
				</tr>
				<tr>   
	    			<td class="separateur">&nbsp;</td>
	   			</tr>
	   			
	   			<% if (StringUtil.isDefined(rssURL)) { %>
	   				<tr>
	   					<td><a href="<%=m_context+rssURL%>"><img src="icons/rss.gif" border="0"></a></td>
	   				</tr>
	   				<link rel="alternate" type="application/rss+xml" title="<%=componentLabel%> : <%=resource.getString("blog.rssLast")%>" href="<%=m_context+rssURL%>">
	   			<% } %>
			</table>