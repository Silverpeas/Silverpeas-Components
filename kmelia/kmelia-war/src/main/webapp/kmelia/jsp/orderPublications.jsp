<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<%
List 	publications 		= (List) request.getAttribute("Publications");
String	currentLang 		= (String) request.getAttribute("Language");
%>

<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<view:looknfeel />
<style>
li {
	list-style: none;
}
</style>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-1.2.6.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/ui.core.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/ui.sortable.js"></script>
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
<view:browseBar link="Main" path="${requestScope['Path']}" extraInformations="<%=resources.getString("kmelia.OrderPublications")%>"/>
<view:window>
<view:frame>
<view:board>
<img src="<%=resources.getIcon("kmelia.info")%>" align="absmiddle"/> <%=resources.getString("kmelia.OrderPublicationsHelp")%><br/><br/>
<center><%=gef.getFormButton(resources.getString("kmelia.OrderPublicationsSave"), "javascript:sendData()", false).print() %></center> 
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