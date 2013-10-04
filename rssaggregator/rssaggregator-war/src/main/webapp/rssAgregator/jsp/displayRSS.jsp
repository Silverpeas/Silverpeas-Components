<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>
<c:set var="componentId" value="<%=componentId%>"/>
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
			if (item.getDate() != null) {
				sDate = dateFormatter.format(item.getDate());
			}
			out.println("<li class='item-channel'>");
		  out.println("<a class='deploy-item' title='deploy item' href='#' onclick = 'return false;' ><img class='deploy-item-rssNews' src='../../util/icons/arrow/open.gif' border='0' /></a><a class='deploy-item itemDeploy' title='bend item' href='#' onclick = 'return false;' ><img class='deploy-item-rssNews' src='../../util/icons/arrow/closed.gif' border='0' /></a>");
			out.println("<h3 class='title-item-rssNews'><a href=\""+item.getLink()+"\" target=_blank>"+item.getTitle()+"</a></h3><div class='lastUpdate-item-rssNews'>"+sDate+"</div>");
			if (item.getDescription() != null && item.getDescription().length()>0) {
				out.println("<div class='itemDeploy'><div class='description-item-rssNews'>"+item.getDescription()+"<br clear='all'/></div></div>");
			}
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

	SimpleDateFormat dateFormatter = new SimpleDateFormat(resource.getString("rss.dateFormat"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
var updateChannelWindow = window;

function updateChannel(id) {
  windowName = "updateChannelWindow";
	larg = "750";
	haut = "280";
  windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
  if (!updateChannelWindow.closed && updateChannelWindow.name=="updateChannelWindow") {
    updateChannelWindow.close();
  }
  updateChannelWindow = SP_openWindow("ToUpdateChannel?Id="+id, windowName, larg, haut, windowParams);
}

function deleteChannel(id) {
	document.deleteChannel.Id.value = id;
	document.deleteChannel.submit();
}
/**
 * This method is only use to test RSS REST service inside displayRSS view
 * change agregate options in order to sort result (yes) or not (no)
 */
function loadChannelsItem() {
  $.ajax({
    url: '<c:out value="${ctxPath}/services/rss/${componentId}"/>',
    async: false,
    data: { agregate: 'yes'},
    success: function(data){
      alert('loadChannelsItem succeeded on application id : <%=componentId%>');
    },
    error: function(HttpRequest, textStatus, errorThrown) {
      //alert('XMLHttpRequest error');
      //HttpRequest, textStatus, errorThrown
      alert(HttpRequest.status + " - " + textStatus+" - "+errorThrown);
    },
    dataType: 'json'
  });
}

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
	
  /*	$('.filter-channel').click(function() {
		$('.item-channel').hide();
		$('.'+this.id).show();
	});*/

<c:forEach var="channel" items="${rssChannels}">
  <c:if test="${channel.displayImage == 0}">$(".channel_${channel.PK.id} .img-item-rssNews").hide();</c:if>
</c:forEach>

});

/**
 * This javascript method allow user to switch from Separated view to Agregated view
 */
function displayAgregatedView() {
  $("#hiddenRssFormAction").val("<%=RSSViewType.AGREGATED.toString()%>");
  $.progressMessage();
  document.rssForm.submit();
}
/**
 * This javascript method allow user to switch from Agregated view to Separated view
 */
function displaySeparatedView() {
  $("#hiddenRssFormAction").val("<%=RSSViewType.SEPARATED.toString()%>");
  $.progressMessage();
  document.rssForm.submit();
}

/**
 * function that display only channelId items
 */
function filterChannel(channelId) {
  $("li[class^='item-channel channel_'], li[class*='item-channel']").hide();
  $("li[class*='item-channel channel_" + channelId + "']").show();
  $("#filterChannelAnchorId").attr("class", "");
}

function displayAll() {
  $("li[class*='item-channel']").show();
  $("#filterChannelAnchorId").attr("class", "active");
}

</script>
</head>
<body class="rssAgregator" id="<%=componentId%>">
<view:browseBar componentId="<%=componentId%>"></view:browseBar>
<c:if test="${fn:contains(role, 'admin')}">
  <view:operationPane>
    <fmt:message var="addChannelTxt" key="rss.addChannel" />
    <fmt:message var="addChannelIcon" key="rss.addChannel" bundle="${icons}" />
    <view:operation altText="${addChannelTxt}" icon="${addChannelIcon}" action="javascript:onClick=addChannel();"></view:operation>
  </view:operationPane>
</c:if>
<view:window>
<fmt:message var="urlErrorMsg" key="rss.nonCorrectURL" />
<c:forEach var="channel" items="${rssChannels}">
  <c:if test="${empty channel.channel}">
  <div class="inlineMessage-nok">${urlErrorMsg} ${channel.url}</div>
  </c:if>
</c:forEach>

<fmt:message key="rss.filter.expand.all" var="expandAllMsg"/>
<fmt:message key="rss.filter.collapse.all" var="collapseAllMsg"/>
<fmt:message key="rss.filter.expand" var="expandMsg"/>
<fmt:message key="rss.filter.collapse" var="collapseMsg"/>

  <!-- Display filters and some actions -->
  <div class="deploy-all-item btn-deploy-all-item bgDegradeGris " title="<fmt:message key="rss.filter.expand.all"/>" onclick = "return false;" >
    <img class="deploy-item-rssNews" src="${ctxPath}<fmt:message key="rss.openChannel" bundle="${icons}"/>" border="0" /> ${expandAllMsg}</div>
  <div class="bend-all-item btn-deploy-all-item bgDegradeGris " title="<fmt:message key="rss.filter.collapse.all"/>" onclick = "return false;" >
    <img class="deploy-item-rssNews" src="${ctxPath}<fmt:message key="rss.closeChannel" bundle="${icons}"/>" border="0" /> ${collapseAllMsg}</div> 
  <div id="displaying"  class="bgDegradeGris"><fmt:message key="rss.filter.agregate.view"/>
    <a id="aggregate-displaying" class="<c:if test='${aggregate}'>active</c:if>" href="javascript:displayAgregatedView();"><fmt:message key="rss.filter.agregate.view.on"/></a>
    <a id="no-aggregate-displaying" class="<c:if test='${not aggregate}'>active</c:if>" href="javascript:displaySeparatedView();"><fmt:message key="rss.filter.agregate.view.off"/></a>
  </div>
  <!-- 
  <a id="dynamicalLoad" class="bgDegradeGris" href="javascript:loadChannelsItem();"><span><fmt:message key="rss.filter.agregate.refresh"/></span></a>
  -->
<c:choose>
  <c:when test="${aggregate}">
  
  <c:if test="${not empty rssChannels}">

    <div class="sousNavBulle">
	
  	<!-- Display filters on channel -->
		<div><fmt:message key="rss.filter.display" />
			<a href="javascript:displayAll();" class="active" id="filterChannelAnchorId"><fmt:message key="rss.filter.display.all" /></a>	

			<c:forEach var="channel" items="${rssChannels}">
			<span <c:if test="${fn:contains(role, 'admin')}"> class="filter" </c:if> >
        
        <c:choose>
          <c:when test="${empty channel.channel}"><c:set var="channelTitle" value="${urlErrorMsg} ${channel.url}"/> </c:when>
          <c:otherwise><c:set var="channelTitle" value="${channel.channel.title}"/></c:otherwise>
        </c:choose>

        <!-- Display filter functionnality -->		
				<a id="channel_${channel.PK.id}" href="javascript:filterChannel('${channel.PK.id}');" class="filter-channel">${channelTitle}</a>
				
				<c:if test="${fn:contains(role, 'admin')}">
        <!-- Display admin operation for each channel (modify, delete) -->
				<span class="operation-chanel">
					<a href="javaScript:onClick=updateChannel('<c:out value="${channel.PK.id}"/>');" class="update" title="<fmt:message key="GML.modify"/>">
						<fmt:message key="rss.updateChannel" bundle="${icons}" var="updateChannelIcon"/>
						<img src="<c:url value="${updateChannelIcon}"/>" border="0" alt="<fmt:message key="GML.modify"/>" />
					</a>&nbsp;
					<a href="javaScript:onClick=deleteChannel('<c:out value="${channel.PK.id}"/>');" class="delete" title="<fmt:message key="GML.delete"/>">
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
  
  <c:if test="${not empty rssItems}">
  <div id="rssNews">
  
    <fmt:message key="rss.dateFormat" var="dateFormatter" />
	  <ul>

  <c:forEach var="item" items="${rssItems}">
		<c:set var="channelName" value="${item.channelTitle}"/>

		<c:if test="${fn:length(channelName) gt 40}">
		  <c:set var="channelName" value="${fn:substring(item.channelTitle, 0, 39)}..." />
		</c:if>
		
		<!-- Display each RSS items -->
		<li class="item-channel channel_${item.channelId} ">
			
			<a class="deploy-item" title="${expandMsg}" href="#" onclick = "return false;" ><img class="deploy-item-rssNews" src="${ctxPath}<fmt:message key="rss.openChannel" bundle="${icons}"/>" border="0" /></a>
			<a class="deploy-item itemDeploy" title="${collapseMsg}" href="#" onclick = "return false;" ><img class="deploy-item-rssNews" src="${ctxPath}<fmt:message key="rss.closeChannel" bundle="${icons}"/>" border="0" /></a>
			<h3 class="title-item-rssNews">
				<c:if test="${not empty item.channelImage}">
				<a href="${item.channelImage.link }" target="_blank"><img class="img-item-rssNews" src="${item.channelImage.location}" border="0" /></a>    
				</c:if>
				<span  class="channelName-rssNews"><c:out value="${channelName}" /> </span> 
				<a href="<c:out value="${item.itemLink}"/>" target="_blank"><c:out value="${item.itemTitle}" escapeXml="false"/> </a> 
			</h3>
			<div class="lastUpdate-item-rssNews"><fmt:formatDate value="${item.itemDate}" pattern="${dateFormatter}"/> </div>
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

<%
if (nbChannelsToLoad > 0) { %>
<form name="loadChannels" action="LoadChannels" method="post"></form>
<script language="javascript">
  window.setTimeout("document.loadChannels.submit()", 500);
</script>
<% } %>
  
  </c:otherwise>
</c:choose>

<form name="refresh" action="LoadChannels" method="post"></form>
<form name="deleteChannel" action="DeleteChannel" method="post">
  <input type="hidden" name="Id"/>
</form>
<form name="rssForm" action="Main" method="post">
  <input type="hidden" name="action" id="hiddenRssFormAction"/>
  <input type="hidden" name="id" id="hiddenRssFormId"/>
</form>

</view:window>

<view:progressMessage/>

<%@include file="channelManager.jsp" %>

</body>
</html>