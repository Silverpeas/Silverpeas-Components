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

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="aggregate" value="${requestScope.aggregate}"/>
<c:set var="rssItems" value="${requestScope.items}"/>
<c:set var="rssChannels" value="${requestScope.allChannels}"/>
<c:set var="role" value="${requestScope.Role}"/>
<%!

void displayChannel(SPChannel spChannel, SimpleDateFormat dateFormatter, String role, String context, ResourcesWrapper resource, JspWriter out) throws IOException, java.text.ParseException {

	String		sDate		= null;
	Channel		channel	= null;
	if (spChannel != null)
    {	
    	channel	= spChannel._getChannel();
	}
	
	Item		item		= null;
	int			i			= 0;

  if (spChannel != null)
  {
  		String 	channelName 	= null;
  		ImageIF channelImage 	= null;
  		if (channel != null) {
  			channelName 	= channel.getTitle();
  			if (channelName.length() > 40) {
  				channelName = channelName.substring(0, 39)+"...";
  			}
  			channelImage 	= channel.getImage();
  			if (spChannel.getDisplayImage() == 1 && channelImage != null && channelImage.getLink()!=null && channelImage.getLocation()!=null) {
    			channelName = "<a href=\""+channelImage.getLink()+"\" target=_blank><img class='img-item-rssNews' src=\""+channelImage.getLocation()+"\" border=\"0\" /></a>"+channelName;
      		}  else {
				channelName = "<img class='img-item-rssNews' src=\""+resource.getIcon("rss.logoRSS")+"\" border=\"0\" />"+channelName;
			}
    		} else {
    			channelName = resource.getString("rss.error");
    		}
    	out.println("<h2 class='title-channel'>");
        out.println("&nbsp;"+channelName+"&nbsp;");
   
        if (role != null && role.equals("admin")) {
          out.print("<a href=\"javaScript:onClick=updateChannel('"+spChannel.getPK().getId()+"');\" class='update'><img src=\""+resource.getIcon("rss.updateChannel")+"\" border=\"0\" alt=\""+resource.getString("GML.modify")+"\"></a>&nbsp;");	
         	out.print("<a href=\"javaScript:onClick=deleteChannel('"+spChannel.getPK().getId()+"');\" class='delete'><img src=\""+resource.getIcon("rss.deleteChannel")+"\" border=\"0\" alt=\""+resource.getString("GML.delete")+"\"></a>");
        }
        out.println("</h2>");
      }

    	if (spChannel != null && channel != null)
    	{
        out.println("<ul>");
    		Date pubDate = null;
    		Object[] 	allItems 	= channel.getItems().toArray();
    		java.util.Arrays.sort(allItems, new ItemComparator(true));
    		while (i<allItems.length && i<spChannel.getNbDisplayedItems())
    		{
    			item = (Item) allItems[i];
    
    			sDate = "";
    			if (item.getDate() != null)
    				sDate = dateFormatter.format(item.getDate());
    
    			out.println("<li class='item-channel'>");
				out.println("<a class='deploy-item' title='deploy item' href='#' onclick = 'return false;' ><img class='deploy-item-rssNews' src='../../util/icons/arrow/open.gif' border='0' /></a><a class='deploy-item itemDeploy' title='bend item' href='#' onclick = 'return false;' ><img class='deploy-item-rssNews' src='../../util/icons/arrow/closed.gif' border='0' /></a>");
    			out.println("<h3 class='title-item-rssNews'><a href=\""+item.getLink()+"\" target=_blank>"+item.getTitle()+"</a></h3><div class='lastUpdate-item-rssNews'>"+sDate+"</div>");
    			if (item.getDescription() != null && item.getDescription().length()>0)
    				out.println("<div class='itemDeploy'><div class='description-item-rssNews'>"+item.getDescription()+"<br clear='all'/></div></div>");
    			out.println("</li>");
    
    			i++;
    			if (i<allItems.length && i < spChannel.getNbDisplayedItems())
    			{
    				out.println("");
    			}
    		}
    	out.println("</ul>");
    } else if (spChannel==null) {
    	out.println("<center><br />");
    	out.println(resource.getString("rss.download"));
    	out.println("<br /><img src=\""+context+"/util/icons/attachment_to_upload.gif\" height='20' width='83' /><br /><br />");
    	out.println("</center>");
    } else if (channel==null) {
    	out.println("<center><br />");
    	out.println(resource.getString("rss.nonCorrectURL"));
    	out.println("<br /><br />"+spChannel.getUrl()+"<br /><br />");
    	out.println("</center>");
    } 

}
%>

<%
	List 	channels 	= (List) request.getAttribute("Channels");
	String 	role 		= (String) request.getAttribute("Role");
//  Boolean aggregate = (Boolean) request.getAttribute("aggregate");
//  List<RSSItem> items = (List<RSSItem>) request.getAttribute("items");

	SimpleDateFormat dateFormatter = new SimpleDateFormat(resource.getString("rss.dateFormat"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
var addChannelWindow = window;
var updateChannelWindow = window;

function addChannel() {
    windowName = "addChannelWindow";
	larg = "750";
	haut = "280";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!addChannelWindow.closed && addChannelWindow.name== "addChannelWindow")
        addChannelWindow.close();
    addChannelWindow = SP_openWindow("ToCreateChannel", windowName, larg, haut, windowParams);
}

function updateChannel(id) {
    windowName = "updateChannelWindow";
	larg = "750";
	haut = "280";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!updateChannelWindow.closed && updateChannelWindow.name=="updateChannelWindow")
        updateChannelWindow.close();
    updateChannelWindow = SP_openWindow("ToUpdateChannel?Id="+id, windowName, larg, haut, windowParams);
}

function deleteChannel(id) {
	document.deleteChannel.Id.value = id;
	document.deleteChannel.submit();
}

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>
<c:set var="componentId" value="<%=gef.getComponentId()%>"/>

function loadChannelsItem() {
  $.ajax({
    url: '<c:out value="${ctxPath}/services/rss/${componentId}"/>',
    async: false,
    data: { agregate: 'yes'},
    success: function(data){
      alert('loadChannelsItem succeeded on application id : <%=gef.getComponentId()%>');
    },
    error: function(HttpRequest, textStatus, errorThrown) {
      //alert('XMLHttpRequest error');
      //HttpRequest, textStatus, errorThrown
      alert(HttpRequest.status + " - " + textStatus+" - "+errorThrown);
    },
    dataType: 'json'
  });
}

 <!-- Commentaire AuroreA. Ajout js pour filtre ( à finir ) et deploiement -->
$(document).ready(function(){

	$('.description-item-rssNews a').attr( "target" , "_blank"  ) ;

	$('.deploy-item').click(function() {
		$(this).parent().children('.itemDeploy').toggle();
	});
	$('.deploy-all-item').click(function() {
		$('.itemDeploy').show();
		$('.btn-deploy-all-item').toggle();
	});
	$('.bend-all-item').click(function() {
		$('.itemDeploy').hide();
		$('.btn-deploy-all-item').toggle();
	});
	
	$('.filter-channel').click(function() {
		$('.item-channel').hide();
		$('.'+this.id).show();
	});
});

</script>
</head>
<body class="rssAgregator" id="<%=gef.getComponentId()%>">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	
	if (role.equals("admin"))
	{
		operationPane.addOperation(resource.getIcon("rss.addChannel"), resource.getString("rss.addChannel"), "javascript:onClick=addChannel()");
	}


    out.println(window.printBefore());
%>



<c:choose>
  <c:when test="${aggregate}">
  
  
	<!-- Commentaire AuroreA. ATTENTION : Texte et icone mis en dur -->
	<div class="deploy-all-item btn-deploy-all-item bgDegradeGris " title="deploy all item" onclick = "return false;" ><img class="deploy-item-rssNews" src="../../util/icons/arrow/open.gif" border="0" /> Tout d&eacute;ployer</div>
	<div class="bend-all-item btn-deploy-all-item bgDegradeGris " title="bend item" onclick = "return false;" ><img class="deploy-item-rssNews" src="../../util/icons/arrow/closed.gif" border="0" /> Tout r&eacute;duire</div>	
	<div id="displaying"  class="bgDegradeGris">Affichage agr&eacute;g&eacute; des flux : <a id="aggregate-displaying" class="active" href="">On</a><a id="no-aggregate-displaying" href="">Off</a></div>
	<a id="dynamicalLoad" class="bgDegradeGris" href="javascript:loadChannelsItem();"><span>Actualiser les flux</span></a>
	
  <c:if test="${not empty rssChannels}">

    <div class="sousNavBulle">
	
	  	<!-- Commentaire AuroreA. ATTENTION : Texte  mis en dur -->
		<div>Afficher : 
			<a onclick="changeScope('ALL')" href="#" class="active">Tous</a>	
			
			

			<c:forEach var="channel" items="${rssChannels}">
			<span <c:if test="${fn:contains(role, 'admin')}"> class="filter" </c:if> >
			
				<!-- Commentaire AuroreA. Sur le lien mettre channel_$idDuChannel -->
				<a id="channel_<c:out value="${channel.PK.id}"/>" href="#" class="filter-channel"><c:out value="${channel.channel.title}"/></a>
				
				<c:if test="${fn:contains(role, 'admin')}">
				<span class="operation-chanel">
					<a href="javaScript:onClick=updateChannel('<c:out value="${channel.PK.id}"/>');" class="update" title="update channel">
						<fmt:message key="rss.updateChannel" bundle="${icons}" var="updateChannelIcon"/>
						<img src="<c:url value="${updateChannelIcon}"/>" border="0" alt="<fmt:message key="GML.modify"/>" />
					</a>&nbsp;
					<a href="javaScript:onClick=deleteChannel('<c:out value="${channel.PK.id}"/>');" class="delete" title="delete channel">
						<fmt:message key="rss.deleteChannel" bundle="${icons}" var="deleteChannelIcon"/>
						<img src="<c:url value="${deleteChannelIcon}"/>" border="0" alt="<fmt:message key="GML.delete"/>" />
					</a>
				</span>
				</c:if>
			</span>
				
			</c:forEach>
		</div>
    </div>
  </c:if>
  
  <!-- Commentaire AuroreA.  POURQUOI CE TEXTE ???<div id="agregateWelcome"><fmt:message key="rss.agregate.welcome"/></div> -->
  

  
  <c:if test="${not empty rssItems}">
  <div id="rssNews">
  
	  <ul>


	  <c:forEach var="item" items="${rssItems}">
		<c:set var="channelName" value="${item.channelTitle}"/>

		<c:if test="${fn:length(channelName) gt 40}">
		  <c:set var="channelName" value="${fn:substring(item.channelTitle, 0, 39)}..." />
		</c:if>
		
		
		<!-- Commentaire AuroreA. Sur le li mettre la classe channel_$idDuChannel -->
		<li class="item-channel channel_Id ">
			
			<a class="deploy-item" title="deploy item" href="#" onclick = "return false;" ><img class="deploy-item-rssNews" src="../../util/icons/arrow/open.gif" border="0" /></a>
			<a class="deploy-item itemDeploy" title="bend item" href="#" onclick = "return false;" ><img class="deploy-item-rssNews" src="../../util/icons/arrow/closed.gif" border="0" /></a>
			<h3 class="title-item-rssNews">
				<c:if test="${not empty item.channelImage}">
				<a href="${item.channelImage.link }" target="_blank"><img class="img-item-rssNews" src="${item.channelImage.location}" border="0" /></a>    
				</c:if>
				<span  class="channelName-rssNews"><c:out value="${channelName}"/> </span> 
				<a href="<c:out value="${item.itemLink}"/>"><c:out value="${item.itemTitle}"/> </a> 
			</h3>
			<div class="lastUpdate-item-rssNews"><c:out value="${dateFormatter.format(item.itemDate)}"/></div>
			<div class="itemDeploy" >
				<div class="description-item-rssNews"><c:out value="${item.itemDescription}" escapeXml="false"/></div>
				<br class="clear"/>
			</div>
		</li>

	  </c:forEach>
	  </ul>
  </div>
  </c:if>
  
  </c:when>
  <c:otherwise>
  
	<!-- Commentaire AuroreA.  ATTENTION : Texte et icone mis en dur -->
  	<div class="deploy-all-item btn-deploy-all-item bgDegradeGris " title="deploy all item" onclick = "return false;" ><img class="deploy-item-rssNews" src="../../util/icons/arrow/open.gif" border="0" /> Tout d&eacute;ployer</div>
	<div class="bend-all-item btn-deploy-all-item bgDegradeGris " title="bend item" onclick = "return false;" ><img class="deploy-item-rssNews" src="../../util/icons/arrow/closed.gif" border="0" /> Tout r&eacute;duire</div>	
	<div id="displaying"  class="bgDegradeGris">Affichage agr&eacute;g&eacute; des flux : <a id="aggregate-displaying"href="">On</a><a  class="active" id="no-aggregate-displaying" href="">Off</a></div>
	<a id="dynamicalLoad" class="bgDegradeGris" href="javascript:loadChannelsItem();"><span>Actualiser les flux</span></a>
	
	<hr id="sep-no-agregate" />

	<div id='rssNews' class="no-agregate">
	  <ul>
	<%   
	  SPChannel channel = null;
	  int nbChannelsToLoad = 0;
	  for (int c=0; c<channels.size(); c++)
	  {
		channel = (SPChannel) channels.get(c);
		if (channel == null) {
		  nbChannelsToLoad++;
		}
		
		if (c%2 == 0) {
		  out.println("<li class='left'>");
		}else {
		  out.println("<li class='right'>");
		}

		displayChannel(channel, dateFormatter, role, context, resource, out);
		
		out.println("</li>");
		
	  }
	%> 
	  </ul> 
	</div>
<form name="refresh" action="LoadChannels" method="post"></form>
<form name="deleteChannel" action="DeleteChannel" method="post">
  <input type="hidden" name="Id">
</form>
<%
if (nbChannelsToLoad > 0) { %>
<form name="loadChannels" action="LoadChannels" method="post"></form>
<script language="javascript">
  window.setTimeout("document.loadChannels.submit()", 500);
</script>
<% } %>
  
  </c:otherwise>
</c:choose>


<% out.println(window.printAfter()); %>  
</body>
</html>