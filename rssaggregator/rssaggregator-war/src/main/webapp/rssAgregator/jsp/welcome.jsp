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

<%@ include file="check.jsp" %>

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
              			if (channelName.length() > 40)
              				channelName = channelName.substring(0, 39)+"...";
              			channelImage 	= channel.getImage();
              			if (spChannel.getDisplayImage() == 1 && channelImage != null && channelImage.getLink()!=null && channelImage.getLocation()!=null) {
              				//test la taille de l'image associ� au channel
              				String sNewHeight = "";
              				String sNewWidth = "";
              				boolean displayImage = false;
              				if (channelImage.getHeight()<=17) {
              					displayImage = true;
              				} else if (channelImage.getHeight()>17 && channelImage.getHeight()<40) {
              					displayImage = true;
              					//les dimensions de l'image doivent �tre r�duites de mani�re proportionnelles
              					Float height = Float.valueOf(new Integer(channelImage.getHeight()).toString());
              					Float width = Float.valueOf(new Integer(channelImage.getWidth()).toString());
              					Float maxHeight = new Float(18);
              					Float ratio = new Float(height.floatValue() / maxHeight.floatValue());
              					sNewHeight = " height=17px ";
              					int newWidth = Math.round(width.floatValue() / ratio.floatValue());
              					sNewWidth = " width="+newWidth+"px ";
              				}
              				if (displayImage && channelImage.getLocation().toString().endsWith(".gif") || channelImage.getLocation().toString().endsWith(".jpg"))
		              			channelName = "<a href=\""+channelImage.getLink()+"\" target=_blank><img src=\""+channelImage.getLocation()+"\" border=\"0\" "+sNewHeight+sNewWidth+"></a>";
	              		}
              		}
              		else
              			channelName = resource.getString("rss.error");
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

	SimpleDateFormat dateFormatter = new SimpleDateFormat(resource.getString("rss.dateFormat"));
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script language="JavaScript1.2">

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

	out.println("<table>");
	
	SPChannel channel = null;
	int nbChannelsToLoad = 0;
	for (int c=0; c<channels.size(); c++)
	{
		channel = (SPChannel) channels.get(c);
		if (channel == null)
		{
			nbChannelsToLoad++;
		}
		if (c%2 == 0)
		{
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

	out.println("</table>");

	out.println(window.printAfter());
%>
<form name="refresh" Action="LoadChannels" method="POST"></form>
<form name="deleteChannel" Action="DeleteChannel" method="POST">
	<input type="hidden" name="Id">
</form>
<% if (nbChannelsToLoad > 0) { %>
<form name="loadChannels" Action="LoadChannels" method="POST"></form>
<script language="JavaScript">
	window.setTimeout("document.loadChannels.submit()", 500);
</script>
<% } %>
</body>
</html>