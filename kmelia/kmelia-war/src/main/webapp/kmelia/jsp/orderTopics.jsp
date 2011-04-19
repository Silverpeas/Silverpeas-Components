<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
List 	nodes 				= (List) request.getAttribute("Nodes");
String	currentLang 		= (String) request.getAttribute("Language");
String 	fatherId 			= null;

Iterator items = nodes.iterator();
NodeDetail node = null;
if(items.hasNext())
{
	node 	= (NodeDetail) items.next();
	fatherId = node.getFatherPK().getId();
}
int nbNodes = nodes.size();
if ("0".equals(fatherId))
{
  nbNodes = nbNodes - 2;
}
%>

<HTML>
<HEAD>
<view:looknfeel />
<style>
li {
	list-style: none;
	background-image: url(<%=resources.getIcon("kmelia.folder")%>);
	background-repeat: no-repeat;
	/*background-position: 0 0.32em;*/
	padding-left: 20px;
	margin-top: 2px;
}
</style>
</HEAD>
<body>
<view:browseBar extraInformations="<%=resources.getString("kmelia.SortTopics")%>"/>
<view:window>
<view:frame>
<view:board>
<img src="<%=resources.getIcon("kmelia.info")%>" align="absmiddle"/> <%=resources.getString("kmelia.SortTopicsHelp")%><br/><br/>
<center><%=gef.getFormButton(resources.getString("kmelia.SortItemsSave"), "javascript:sendData()", false).print() %></center>
</view:board>
<br/>
<view:board>
<table width="100%" cellspacing="0">
<tr valign="middle" class="intfdcolor">
	<td width="20px"><img src="<%=resources.getIcon("kmelia.folder")%>" border="0"/></td>
	<td align="left" class="ArrayNavigation"><%=nbNodes%> <%=resources.getString("Theme").toLowerCase()%>(s)</td>
</tr>
</table>
<br/>
<table width="100%" cellspacing="0">
<tr><td>
<ul id="items" style="cursor: hand; cursor: pointer;">
<%
items = nodes.iterator();
while(items.hasNext())
{
	node = (NodeDetail) items.next();
	
	if (!"1".equals(node.getNodePK().getId()) && !"2".equals(node.getNodePK().getId()))
	{
%>
	<li id="item_<%=node.getNodePK().getId()%>"><b><%=node.getName(currentLang)%></b><br/>
		<% if (StringUtil.isDefined(node.getDescription(currentLang))) { %>
			<%=Encode.javaStringToHtmlParagraphe(node.getDescription(currentLang))%><br/>
		<% } %>
		<br/>
	</li>
<%
	}
}
%>
</ul>
</td></tr>
</table>
</view:board>
</view:frame>
</view:window>
<form name="sortForm" method="POST" action="OrderTopics">
<input type="hidden" name="sortedIds"/>
</form>
<script type='text/javascript'>

$(document).ready(function(){
	$("#items").sortable({opacity: 0.4}); 
});

function sendData() {
	//alert('sortupdate');
	var reg=new RegExp("item", "g");
	
	var data = $('#items').sortable('serialize');
	data += "#";
	var tableau=data.split(reg);
	var param = "";
	for (var i=0; i<tableau.length; i++)
	{
		if (i != 0)
			param += ","
				
		param += tableau[i].substring(3, tableau[i].length-1);
	}
	sortItems(param);
}

function sortItems(orderedList)
{
	//alert(orderedList);
	$.get('<%=m_context%>/KmeliaAJAXServlet', { OrderedList:orderedList,ComponentId:'<%=componentId%>',Action:'SortTopics'}, 
			function(data){
				data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
				if (data == "error")
				{
					alert("Une erreur s'est produite !");
				}
				else
				{
					window.opener.reloadPage(<%=fatherId%>);
					window.close();
				}
			}, 'text');
}
</script>
</body>
</html>