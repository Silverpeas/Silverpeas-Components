<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.Vector"
%><%@ page import="java.io.IOException"
%><%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"
%><%@ page import="com.stratelia.webactiv.forums.models.Forum"
%><%@ page import="com.stratelia.webactiv.forums.url.ActionUrl"
%><%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"
%><%@ page import="com.stratelia.webactiv.forums.control.ForumsSessionController"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"
%><%@ page import="com.stratelia.webactiv.util.ResourceLocator"
%>
<%!
public void displayForumLine(String spaceId, String componentId, Forum forum,
    ResourcesWrapper resources, JspWriter out, int currentPage, String call, boolean admin,
    boolean moderator, int depth, boolean hasChildren, boolean deployed,
    ForumsSessionController fsc)
{
	try
    {
		int forumId = forum.getId();
		String forumName = forum.getName();
		String forumDescription = forum.getDescription();
		boolean forumActive = forum.isActive();
		
		String nbSubjects = Integer.toString(fsc.getNbSubjects(forumId));
		String nbMessages = Integer.toString(fsc.getNbMessages(forumId));
		
		int lastMessageId = -1;
		Date dateLastMessage = null;
		String lastMessageDate = "";
		String lastMessageUser = "";
		Object[] lastMessage = fsc.getLastMessage(forumId);
		if (lastMessage != null)
		{
			lastMessageId = Integer.parseInt((String)lastMessage[0]);
			lastMessageDate = convertDate((Date)lastMessage[1], resources);
			lastMessageUser = (String)lastMessage[2];
		}
		
		out.println("<tr>");

		// Cadenas de "lock"
		//premier colonne
		out.print("<td>");

		// rechercher si l'utilisateur a des messages non lu sur ce forum
		boolean isNewMessage = fsc.isNewMessageByForum(fsc.getUserId(), forumId);
		out.print("<img src=\"icons/" + (isNewMessage ? "buletRed" : "buletColoredGreen") + ".gif\">");

		// Icone de deploiement
		out.print("<img src=\"icons/1px.gif\">");
		if (depth > 0)
        {
			out.print("<img src=\"icons/1px.gif\" width=\"" + depth*10 + "\" height=\"1\">");
		}
		if (hasChildren)
        {
			out.print("<a href=\"");
			if (currentPage > 0)
            {
				if (deployed)
                {
					out.print(ActionUrl.getUrl(
                        spaceId, componentId, "viewForum", call,  2, forumId, currentPage));
					out.println("\">");
				}
				else
                {
					out.print(ActionUrl.getUrl(
	                    spaceId, componentId, "viewForum", call, 1, forumId, currentPage));
					out.println("\">");
                    out.println("<img src=\"icons/topnav_r.gif\" width=\"6\" height=\"11\" border=\"0\"></a>");
				}
			}
			else
            { 
				if (deployed)
                {
					out.print(ActionUrl.getUrl(spaceId, componentId, "main", call, 2, forumId, -1));
					out.println("\">");
				}
				else
                {
					out.print(ActionUrl.getUrl(spaceId, componentId, "main", call, 1, forumId, -1));
					out.println("\">");
                    out.println("<img src=\"icons/topnav_r.gif\" width=\"6\" height=\"11\" border=\"0\"></a>");
				}
			}
		}
		else
        {
			out.print("&nbsp;");
        }
		out.print("</td>");

		//deuxieme colonne
		// nom du Forum
		
		out.print("<td width=\"100%\" >");
		out.print("<span class=\"titreForum\">");
		
		out.print("<a href=\"javascript:goto_jsp('"
            + ActionUrl.getUrl(spaceId, componentId, "viewForum", call,  -1, -1, forumId) + "')\">");
		
		out.print(Encode.javaStringToHtmlString(forumName));
		out.println("</a></span><br />");

		// description du forum
		out.print("<span class=\"descriptionForum\">");
		out.print(Encode.javaStringToHtmlString(forumDescription));
		out.println("</span></td>");
		
		// Troisieme colonne
		// nombre de sujets dans le forum
		out.print("<td align=\"center\" class=\"fondClair\"><span class=\"txtnote\">");
		out.print(Encode.javaStringToHtmlString(nbSubjects));
		out.println("</span></td>");
		
		// quatrieme colonne
		// nombre de messages dans le forum
		out.print("<td align=\"center\" class=\"fondFonce\"><span class=\"txtnote\">");
		out.print(Encode.javaStringToHtmlString(nbMessages));
		out.println("</span></td>");
		
		//cinqui�me colonne
		// dernier sujet du forum
		out.print("<td nowrap=\"nowrap\" align=\"center\" class=\"fondClair\"><span class=\"txtnote\">");
		if (lastMessageDate != null)
		{
			out.print(Encode.javaStringToHtmlString(lastMessageDate) + "<br />");
			out.print("<a href=\"javascript:goto_jsp('"
                + ActionUrl.getUrl(spaceId, componentId, "viewMessage", call, -1, lastMessageId, forumId) + "')\">");
			out.print(Encode.javaStringToHtmlString(lastMessageUser));
		}
		out.println("</span></td>");
	}
	catch (IOException ioe)
    {
		SilverTrace.info(
            "forums", "JSPforumsListManager.displayForumLine()", "root.EX_NO_MESSAGE", null, ioe);
	}
}

public void displayForumsList(String spaceId, String componentId, JspWriter out,
    ResourcesWrapper resources, boolean admin, boolean moderator, int currentForumId, String call,
    ForumsSessionController fsc, String categoryId, String nom, String description) 
{
    try 
    {
	    Forum[] forums = fsc.getForumsListByCategory(categoryId);
        if (forums != null)
        {
		   	out.println("<tr>");
		   	out.println("<td colspan=\"5\" class=\"titreCateg\">" + nom + "</td>");
            out.println("</tr>");  
					
			scanForum(spaceId, componentId, forums, resources, out, currentForumId, call, admin,
                moderator, currentForumId, 0, fsc);
		}
	}
    catch (IOException ioe) 
    {
		SilverTrace.info(
            "forums", "JSPforumsListManager.displayForumsList()", "root.EX_NO_MESSAGE", null, ioe);
    }
}
 
public void scanForum(String spaceId, String componentId, Forum[] forums,
    ResourcesWrapper resources,JspWriter out, int currentPage, String call, boolean admin,
    boolean moderator, int currentForumId, int depth, ForumsSessionController fsc)
{
  for (Forum forum : forums) {
    int forumParent = forum.getParentId();
    if (forumParent == currentForumId) {
      int forumId = forum.getId();
      boolean hasChildren = hasChildren(forums, forumId);
      boolean isDeployed = fsc.forumIsDeployed(forumId);

      displayForumLine(spaceId, componentId, forum, resources, out, currentPage, call, admin,
          moderator, depth, hasChildren, isDeployed, fsc);
      if (hasChildren && isDeployed) {
        scanForum(spaceId, componentId, forums, resources, out, currentPage, call, admin, moderator,
            forumId, depth + 1, fsc);
      }
    }
  }
}

public boolean hasChildren(Forum[] forums, int currentForumId)
{
    int i = 0;
    while (i < forums.length)
    {
        if (forums[i].getParentId() == currentForumId)
        {
            return true;
        }
        i++;
    }
    return false;
}

public void displayForumsAdminButtons(boolean admin, boolean moderator, OperationPane operationPane,
    String currentFolderId, String call, ResourceLocator resource)
{
    // operationPane.addOperation(addForum, resource.getString("newForum"),
    //     ActionUrl.getUrl(spaceId, componentId, "editForumInfo", call, 1, currentFolderId, currentFolderId));
}
%>