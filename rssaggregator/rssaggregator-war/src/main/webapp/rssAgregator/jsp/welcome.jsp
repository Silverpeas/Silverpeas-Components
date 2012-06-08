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
	out.println("<table width=\"100%\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\" class=\"intfdcolor\">");
  out.println("<tr>");
  out.println("<td class=\"intfdcolor\">");
  out.println("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
  out.println("<tr>");
  out.println("<td colspan=\"3\" class=\"intfdcolor\"><img src=\""+context+"/util/icons/colorPix/1px.gif\" width=\"1\" height=\"1\"></td>");
  out.println("</tr>");
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
  				//test la taille de l'image associe au channel
  				String sNewHeight = "";
  				String sNewWidth = "";
  				boolean displayImage = false;
    			if (channelImage.getHeight()<=17) {
    					displayImage = true;
    			} else if (channelImage.getHeight()>17 && channelImage.getHeight()<40) {
    					displayImage = true;
    					//les dimensions de l'image doivent etre reduites de maniere proportionnelles
    					Float height = Float.valueOf(Integer.toString(channelImage.getHeight()));
    					Float width = Float.valueOf(Integer.toString(channelImage.getWidth()));
    					Float maxHeight = new Float(18);
    					Float ratio = new Float(height.floatValue() / maxHeight.floatValue());
    					sNewHeight = " height=17px ";
    					int newWidth = Math.round(width.floatValue() / ratio.floatValue());
    					sNewWidth = " width="+newWidth+"px ";
    			}
    			if (displayImage && channelImage.getLocation().toString().endsWith(".gif") || channelImage.getLocation().toString().endsWith(".jpg"))
        		channelName = "<a href=\""+channelImage.getLink()+"\" target=_blank><img src=\""+channelImage.getLocation()+"\" border=\"0\" "+sNewHeight+sNewWidth+"></a>";
      		}
    		} else {
    			channelName = resource.getString("rss.error");
    		}
    	  out.println("<tr>");
        out.println("<td class=\"intfdcolor4\" nowrap><span class=\"txtnav\">&nbsp;"+channelName+"&nbsp;</span></td>");
        out.print("<td class=\"intfdcolor\" width=\"100%\" nowrap><img src=\"../../util/icons/portlet/rond.gif\"></td>");
        if (role != null && role.equals("admin")) {
          out.print("<td align=\"right\"><a href=\"javaScript:onClick=updateChannel('"+spChannel.getPK().getId()+"');\"><img src=\""+resource.getIcon("rss.updateChannel")+"\" border=\"0\" alt=\""+resource.getString("GML.modify")+"\"></a>&nbsp;");	
         	out.print("<a href=\"javaScript:onClick=deleteChannel('"+spChannel.getPK().getId()+"');\"><img src=\""+resource.getIcon("rss.deleteChannel")+"\" border=\"0\" alt=\""+resource.getString("GML.delete")+"\"></a></td>");
        }
        out.println("</tr>");
      }
      out.println("<tr bgcolor=\"#000000\">");
      out.println("<td colspan=\"3\"><img src=\""+context+"/util/icons/colorPix/1px.gif\" width=\"1\" height=\"1\"></td>");
      out.println("</tr>");
      out.println("<tr>");
      out.println("<td colspan=\"3\" class=\"intfdcolor4\">");
    	if (spChannel != null && channel != null)
    	{
        out.println("<table>");
    		Date pubDate = null;
    		Object[] 	allItems 	= channel.getItems().toArray();
    		java.util.Arrays.sort(allItems, new ItemComparator(true));
    		while (i<allItems.length && i<spChannel.getNbDisplayedItems())
    		{
    			item = (Item) allItems[i];
    
    			sDate = "";
    			if (item.getDate() != null)
    				sDate = " ("+dateFormatter.format(item.getDate())+")";
    
    			out.println("<tr>");
    			//out.println("<td>"+sDate+"</td>");
    			out.println("<td><b><a href=\""+item.getLink()+"\" target=_blank>"+item.getTitle()+"</a></b>"+sDate+"<br>");
    			if (item.getDescription() != null && item.getDescription().length()>0)
    				out.println(item.getDescription()+"<br>");
    			out.println("</td></tr>");
    
    			i++;
    			if (i<allItems.length && i < spChannel.getNbDisplayedItems())
    			{
    				out.println("<tr><td>&nbsp;</td></tr>");
    			}
    		}
    	out.println("</table>");
    } else if (spChannel==null) {
    	out.println("<center><BR>");
    	out.println(resource.getString("rss.download"));
    	out.println("<BR><img src=\""+context+"/util/icons/attachment_to_upload.gif\" height=20 width=83><BR><BR>");
    	out.println("</center>");
    } else if (channel==null) {
    	out.println("<center><BR>");
    	out.println(resource.getString("rss.nonCorrectURL"));
    	out.println("<BR><BR>"+spChannel.getUrl()+"<BR><BR>");
    	out.println("</center>");
    } 
	out.println("</td>");
  out.println("</tr>");
  out.println("</table>");
  out.println("</td>");
  out.println("</tr>");
  out.println("</table>");
}
%>

<%
	List 	channels 	= (List) request.getAttribute("Channels");
	String 	role 		= (String) request.getAttribute("Role");
//  Boolean aggregate = (Boolean) request.getAttribute("aggregate");
//  List<RSSItem> items = (List<RSSItem>) request.getAttribute("items");

	SimpleDateFormat dateFormatter = new SimpleDateFormat(resource.getString("rss.dateFormat"));
%>

<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
var addChannelWindow = window;
var updateChannelWindow = window;

function addChannel() {
    windowName = "addChannelWindow";
	larg = "600";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!addChannelWindow.closed && addChannelWindow.name== "addChannelWindow")
        addChannelWindow.close();
    addChannelWindow = SP_openWindow("ToCreateChannel", windowName, larg, haut, windowParams);
}

function updateChannel(id) {
    windowName = "updateChannelWindow";
	larg = "600";
	haut = "350";
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

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
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
  <p><fmt:message key="rss.agregate.welcome"/></p>
  <c:if test="${not empty rssChannels && fn:contains(role, 'admin')}">
    <div id="adminChannels">
    <c:forEach var="channel" items="${rssChannels}">
      <div id="channel<c:out value="${channel.PK.id}"/>">
      <p>
      <c:out value="${channel.channel.title}"/>
        <a href="javaScript:onClick=updateChannel('<c:out value="${channel.PK.id}"/>');">
        <fmt:message key="rss.updateChannel" bundle="${icons}" var="updateChannelIcon"/>
        <img src="<c:url value="${updateChannelIcon}"/>" border="0" alt="<fmt:message key="GML.modify"/>">
        </a>&nbsp;
        <a href="javaScript:onClick=deleteChannel('<c:out value="${channel.PK.id}"/>');">
        <fmt:message key="rss.deleteChannel" bundle="${icons}" var="deleteChannelIcon"/>
        <img src="<c:url value="${deleteChannelIcon}"/>" border="0" alt="<fmt:message key="GML.delete"/>">
        </a>
      </p>     
      </div>
    </c:forEach>
    </div>
  </c:if>
  
  <c:if test="${not empty rssItems}">
  <div id="rssNews">
  <ul>
  <c:forEach var="item" items="${rssItems}">
    <c:set var="channelName" value="${item.channelTitle}"/>
    <c:if test="${fn:length(channelName) gt 40}">
      <c:set var="channelName" value="${fn:substring(item.channelTitle, 0, 39)}..." />
    </c:if>
    <li>
    <c:if test="${not empty item.channelImage}">
      <a href="${item.channelImage.link }" target=_blank><img src="${item.channelImage.location}" border="0"></a>    
    </c:if>
    
    <c:out value="${channelName}"/> : <a href="<c:out value="${item.itemLink}"/>"><c:out value="${item.itemTitle}"/> - <c:out value="${item.itemDate}"/></a> <c:out value="${item.itemDescription}"/></li>
  </c:forEach>
  </ul>
  </div>
  </c:if>
  
  </c:when>
  <c:otherwise>
  <table>
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
      out.println("<tr>");
    }
    out.println("<td width=\"50%\" valign=\"top\">");
    displayChannel(channel, dateFormatter, role, context, resource, out);
    out.println("</td>");
    if (c%2 != 0)
    {
      out.println("</tr>");
    }
  }
%> 
  </table> 
<form name="refresh" Action="LoadChannels" method="post"></form>
<form name="deleteChannel" Action="DeleteChannel" method="post">
  <input type="hidden" name="Id">
</form>
<%
if (nbChannelsToLoad > 0) { %>
<form name="loadChannels" Action="LoadChannels" method="post"></form>
<script language="javascript">
  window.setTimeout("document.loadChannels.submit()", 500);
</script>
<% } %>
  
  </c:otherwise>
</c:choose>

<div id="dynamicalLoad">
  <a href="javascript:loadChannelsItem();">Lien pour charger les flux RSS</a>
  <div id="rssItems">
  </div>
</div>

<%
out.println(window.printAfter());
%>
</body>
</html>