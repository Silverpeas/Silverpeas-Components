<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%!
void displaySubscriptionsList(KmeliaSessionController kmeliaScc, String deleteIcon, String hLineSrc, ResourcesWrapper resources, JspWriter out) throws IOException, CreateException, FinderException, NamingException, SQLException {

  String cssClass = null;
  boolean peer = true;

  Collection subscriptionList = kmeliaScc.getSubscriptionList();
  Iterator i = subscriptionList.iterator();
  out.println("<TABLE border=0 cellPadding=0 cellSpacing=3 align=\"center\" width=\"70%\">");
  if (i.hasNext()) {
  		out.println("<TR><TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></TD></TR>");
        out.println("<TR><TD align=\"center\"><b>"+kmeliaScc.getString("MySubscriptions")+"</b></TD><TD align=\"center\"><b>"+kmeliaScc.getString("Suppression")+"</b></TD></TR>");
  		out.println("<TR><TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></TD></TR>");
  } else {
  		out.println("<TR><TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></TD></TR>");
        out.println("<TR><TD colspan=\"2\" align=\"center\" width=\"100%\">"+resources.getString("GML.none")+"</TD></TR>");
  }
  while (i.hasNext()) {
        Collection	path		= (Collection) i.next();
        Iterator	j			= path.iterator();
        String		link		= null;
		String		links		= "";
        String		removeLink	= null;
        if (peer) {
              cssClass = "peer";
              peer = false;
        } else {
              cssClass = "odd";
              peer = true;
        }
        while (j.hasNext()) {
          NodeDetail	header		= (NodeDetail) j.next();
		  String		nodeName	= header.getName(kmeliaScc.getCurrentLanguage());
		  if (header.getNodePK().getId().equals("0"))
				nodeName = kmeliaScc.getComponentLabel();

		  link = "<A class=\""+cssClass+"\" HREF=\"javascript:onClick=goToSubscription('" + 
                 header.getNodePK().getId() + 
                 "')\">" +
                 Encode.javaStringToHtmlString(nodeName) + "</A>";
		  if (links.length() == 0) {
			  links = link;
		  } else {
			  links = link + " > " + links;
		  }

          if (removeLink == null) {
        	  removeLink = "<A HREF=\"javascript:onClick=subscriptionRemoveConfirm('"+header.getNodePK().getId()+"')\"><IMG border=\"0\" src=\""+deleteIcon+"\"></A>";
          }
        }
        out.println("<TR><TD class=\""+cssClass+"\" align=\"center\">"+links+"</TD><TD class=\""+cssClass+"\" align=\"center\">"+removeLink+"</TD></TR>");
  }
  out.println("<TR><TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></TD></TR>");
  out.println("</TABLE>");
  out.println("<BR>");
}
%>

<%
//Récupération des paramètres
String topicId = (String) request.getParameter("Id");
String action = (String) request.getParameter("Action");

//Icons
String deleteSrc = m_context + "/util/icons/delete.gif";
String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);

if (action.equals("AddSubscription")) {
        try {
            kmeliaScc.addSubscription(topicId);
        } catch (Exception e) {
			SilverTrace.warn("kmelia","JSPsubscriptionsManager", "kmelia.EX_SUBSCRIBE_TOPIC_FAILED","topicId = " + topicId,e);
        } finally {
            action = "View";
        }
} else if (action.equals("Remove")) {
        kmeliaScc.removeSubscription(topicId);
        action = "View";
}
if (action.equals("View")) {
%>
<html>
<head><title><%=resources.getString("GML.popupTitle")%></title>

<script language="javaScript">
function subscriptionRemoveConfirm(id) {
    if(window.confirm("<%=kmeliaScc.getString("SubscriptionRemoveQuestion")%>")){
        document.subscriptionForm.Id.value = id;
        document.subscriptionForm.Action.value = "Remove";
        document.subscriptionForm.action = "subscriptionsManager.jsp";
        document.subscriptionForm.submit();
    }
}

function goToSubscription(id) {
    window.opener.location.replace("GoToTopic?Id="+id);
    window.close();
}
</script>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%

    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(kmeliaScc.getSpaceLabel());
    browseBar.setComponentName(kmeliaScc.getComponentLabel());
    browseBar.setPath(kmeliaScc.getString("MySubscriptions"));
	
	//Le cadre
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();

    //Début code
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<!-- Cadre extérieur -->
  <TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%" align="center">
    <tr>
    <td>
<%
        displaySubscriptionsList(kmeliaScc, deleteSrc, hLineSrc, resources, out);
%>	
	</td></tr></table>
<%		
	out.println(board.printAfter());
    
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(cancelButton);
    out.println("<br/><center>"+buttonPane.print()+"</center></br>");
        
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="subscriptionForm" ACTION="subscriptionsManager.jsp" METHOD="POST">
<input type="hidden" name="Action"><input type="hidden" name="Id"></FORM>
</BODY>
</HTML>
<% } %>