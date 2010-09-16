<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
List 	publications 		= (List) request.getAttribute("Publications");
String	currentLang 		= (String) request.getAttribute("Language");
%>

<HTML>
<HEAD>
<view:looknfeel />
<style>
li {
	list-style: none;
}
</style>
<script type='text/javascript'>
	$(document).ready(function(){
		$("#publis").sortable({opacity: 0.4}); 
	});

function sendData() 
{
	var reg=new RegExp("publi", "g");
	
	var data = $('#publis').sortable('serialize');
	data += "#";
	var tableau=data.split(reg);
	var param = "";
	for (var i=0; i<tableau.length; i++)
	{
		//alert(tableau[i].substring(3, tableau[i].length-1));
		if (i != 0)
			param += ","
				
		param += tableau[i].substring(3, tableau[i].length-1);
	}
	document.sortForm.sortedIds.value = param;
	document.sortForm.submit();
}

function topicGoTo(id) 
{
	location.href="GoToTopic?Id="+id;
}

</script>
</HEAD>
<body>
<view:browseBar extraInformations="<%=resources.getString("kmelia.OrderPublications")%>">
  <view:browseBarElt link="Main" label="${requestScope['Path']}" />
</view:browseBar>
<view:window>
<view:frame>
<view:board>
<img src="<%=resources.getIcon("kmelia.info")%>" align="absmiddle"/> <%=resources.getString("kmelia.OrderPublicationsHelp")%><br/><br/>
<center><%=gef.getFormButton(resources.getString("kmelia.SortItemsSave"), "javascript:sendData()", false).print() %></center> 
</view:board>
<br/>
<view:board>
<table width="100%" cellspacing="0">
<tr valign="middle" class="intfdcolor">
	<td width="20px"><img src="<%=resources.getIcon("kmelia.publication")%>" border="0"/></td>
	<td align="left" class="ArrayNavigation"><%=publications.size() %> <%=resources.getString("GML.publication")%></td>
</tr>
</table>
<br/>
<table width="100%" cellspacing="0">
<tr><td>
<ul id="publis" style="cursor: hand; cursor: pointer;">
<%
Iterator publis = publications.iterator();
UserPublication userPub = null;
PublicationDetail pub = null;
while(publis.hasNext())
{
	userPub 	= (UserPublication) publis.next();
	pub 		= userPub.getPublication();
%>
	<li id="publi_<%=pub.getPK().getId()%>">&#8226;&#160;<b><%=pub.getName(currentLang)%></b><br/>
		<% if (StringUtil.isDefined(pub.getDescription(currentLang))) { %>
			<%=Encode.javaStringToHtmlParagraphe(pub.getDescription(currentLang))%><br/>
		<% } %>
		<br/>
	</li>
<%
}
%>
</ul>
</td></tr>
</table>
</view:board>
</view:frame>
</view:window>
<form name="sortForm" method="POST" action="OrderPublications">
<input type="hidden" name="sortedIds"/>
</form>
</body>
</html>