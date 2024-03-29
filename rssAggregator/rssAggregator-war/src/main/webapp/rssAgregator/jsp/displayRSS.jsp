<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="com.rometools.rome.feed.synd.SyndEntry" %>
<%@ page import="com.rometools.rome.feed.synd.SyndFeed" %>
<%@ page import="com.rometools.rome.feed.synd.SyndImage" %>
<%@ page import="org.silverpeas.components.rssaggregator.service.FeedComparator" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="language" value="${sessionScope[sessionController].language}"/>
<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<fmt:message key="rss.addChannel" var="addChannelLabel" />
<fmt:message key="GML.modify" var="modifyLabel" />
<fmt:message key="GML.delete" var="deleteLabel" />

<c:set var="aggregate" value="${requestScope.aggregate}"/>
<c:set var="rssItems" value="${requestScope.items}"/>
<c:set var="rssChannels" value="${requestScope.allChannels}"/>
<c:set var="role" value="${requestScope.Role}"/>

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>
<c:set var="componentId" value="<%=componentId%>"/>
<%!

void displayChannel(SPChannel spChannel, SimpleDateFormat dateFormatter, String role, String context, MultiSilverpeasBundle resource, JspWriter out) throws IOException, java.text.ParseException {

	String		sDate		= null;
	SyndFeed feed	= null;
	if (spChannel != null)
    {
	feed	= spChannel.getFeed();
	}

  if (spChannel != null)
  {
		String 	channelName 	= null;
		SyndImage channelImage 	= null;
		if (feed != null) {
			channelName 	= feed.getTitle();
			if (channelName != null && channelName.length() > 40) {
				channelName = channelName.substring(0, 39)+"...";
			}
			channelImage 	= feed.getImage();
			if (spChannel.getDisplayImage() == 1 && channelImage != null && channelImage.getLink()!=null && channelImage.getUrl()!=null) {
			channelName = "<a href=\""+channelImage.getLink()+"\" target=_blank><img class='img-item-rssNews' src=\""+channelImage.getUrl()+"\" border=\"0\" /></a>"+channelName;
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

	if (spChannel != null && feed != null)
	{
    out.println("<ul>");
		SyndEntry[] 	allItems 	= feed.getEntries().toArray(new SyndEntry[feed.getEntries().size()]);
		java.util.Arrays.sort(allItems, new FeedComparator(true));
		for (int i = 0; i < allItems.length && i<spChannel.getNbDisplayedItems(); i++)
		{
			SyndEntry feedEntry = allItems[i];

			sDate = "";
			Date entryDate = feedEntry.getUpdatedDate() == null ? feedEntry.getPublishedDate() : feedEntry.getUpdatedDate();
			if (entryDate != null) {
				sDate = dateFormatter.format(entryDate);
			}
			out.println("<li class='item-channel'>");
		  out.println("<a class='deploy-item' title='deploy item' href='#' onclick = 'return false;' ><img class='deploy-item-rssNews' src='../../util/icons/arrow/open.gif' border='0' /></a><a class='deploy-item itemDeploy' title='bend item' href='#' onclick = 'return false;' ><img class='deploy-item-rssNews' src='../../util/icons/arrow/closed.gif' border='0' /></a>");
			out.println("<h3 class='title-item-rssNews'><a href=\""+feedEntry.getLink()+"\" target=_blank>"+feedEntry.getTitle()+"</a></h3><div class='lastUpdate-item-rssNews'>"+sDate+"</div>");
			if (feedEntry.getDescription() != null && !feedEntry.getDescription().getValue().isEmpty()) {
				out.println("<div class='itemDeploy'><div class='description-item-rssNews'>"+feedEntry.getDescription().getValue()+"<br clear='all'/></div></div>");
			}
			out.println("</li>");
			if (i+1<allItems.length && i+1 < spChannel.getNbDisplayedItems())
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
  } else if (feed==null) {
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
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.rssAggregator">
<head>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="toggle"/>
<script type="text/javascript">
/**
 * This method is only use to test RSS REST service inside displayRSS view
 * change agregate options in order to sort result (yes) or not (no)
 */
function loadChannelsItem() {
  $.ajax({
    url: '<c:out value="${ctxPath}/services/rss/${componentId}"/>',
    data: { agregate: 'yes'},
    success: function(data){
      notySuccess('loadChannelsItem succeeded on application id : <%=componentId%>');
    },
    error: function(HttpRequest, textStatus, errorThrown) {
      notyError(HttpRequest.status + " - " + textStatus+" - "+errorThrown);
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
 * This javascript method allow user to switch from Separated view to Aggregated view
 */
function displayAggregatedView() {
  $("#hiddenRssFormAction").val("<%=RSSViewType.AGGREGATED.toString()%>");
  $.progressMessage();
  document.rssForm.submit();
}
/**
 * This javascript method allow user to switch from Aggregated view to Separated view
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

function addChannel() {
  jQuery.popup.load('ToAddChannel').show('validation', {
    title : "${addChannelLabel}",
    callback : function() {
      validateChannelForm();
    }
  });
}

function updateChannel(id) {
  jQuery.popup.load('ToModifyChannel', {params : {'Id' : id}}).show('validation', {
    title : "${modifyLabel}",
    callback : function() {
      validateChannelForm();
    }
  });
}

function deleteChannel(id) {
  jQuery.popup.load('ToRemoveChannel', {params : {'Id' : id}}).show('confirmation', {
    title : "${deleteLabel}",
    callback : function() {
      validateChannelForm();
    }
  });
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
  <view:componentInstanceIntro componentId="${componentId}" language="${language}"/>
<fmt:message var="urlErrorMsg" key="rss.nonCorrectURL" />
<c:forEach var="channel" items="${rssChannels}">
  <c:if test="${empty channel.feed}">
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
    <a id="aggregate-displaying" class="<c:if test='${aggregate}'>active</c:if>" href="javascript:displayAggregatedView();"><fmt:message key="rss.filter.agregate.view.on"/></a>
    <a id="no-aggregate-displaying" class="<c:if test='${not aggregate}'>active</c:if>" href="javascript:displaySeparatedView();"><fmt:message key="rss.filter.agregate.view.off"/></a>
  </div>
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
          <c:when test="${empty channel.feed}"><c:set var="channelTitle" value="${urlErrorMsg} ${channel.url}"/> </c:when>
          <c:otherwise><c:set var="channelTitle" value="${channel.feed.title}"/></c:otherwise>
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
				<a href="${item.channelImage.link }" target="_blank"><img class="img-item-rssNews" src="${item.channelImage.url}" border="0" /></a>
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
<script language="javascript" type="text/javascript">
  window.setTimeout(function() {
    sp.navRequest("LoadChannels").go();
  }, 500);
</script>
<% } %>

  </c:otherwise>
</c:choose>

<view:form name="rssForm" action="Main" method="post">
  <input type="hidden" name="action" id="hiddenRssFormAction"/>
  <input type="hidden" name="id" id="hiddenRssFormId"/>
</view:form>

</view:window>

<view:progressMessage/>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.rssAggregator', ['silverpeas.services', 'silverpeas.directives']);
</script>

</body>
</html>