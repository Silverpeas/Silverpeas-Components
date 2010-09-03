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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.IOException"
%><%@ page import="javax.servlet.http.HttpServletRequest"
%><%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"
%><%@ page import="com.stratelia.webactiv.forums.sessionController.ForumsSessionController"
%><%@ page import="com.stratelia.webactiv.util.ResourceLocator"
%>
<%!
public void actionManagement(HttpServletRequest request, boolean isAdmin, boolean isModerator,
    String userId, ResourceLocator resource, JspWriter out, ForumsSessionController fsc)
{
    int action = getIntParameter(request, "action");
    
    if (action != -1)
    {
        int params = getIntParameter(request, "params");
        String forumName;
        String forumDescription;
        int forumId;
        int messageId;
        String[] forumModerators;
        int forumParent;
        String categoryId;
        String keywords;
        
        try
        {
            switch (action)
            {
                case 1 :
                    fsc.deployForum(params);
                    break;
                    
                case 2 :
                    fsc.undeployForum(params);
                    break;
                    
                case 3 :
                    forumName = request.getParameter("forumName").trim(); 
                    forumDescription = request.getParameter("forumDescription").trim();
                    forumModerators = (String[]) request.getParameterValues("moderators");
                    forumParent = getIntParameter(request, "forumFolder");
                    categoryId = request.getParameter("CategoryId").trim();
                    keywords = request.getParameter("forumKeywords").trim();
                    forumId = fsc.createForum(
                        forumName, forumDescription, userId, forumParent, categoryId, keywords);
                    if (forumModerators != null)
                    {
                        for (int i = 0; i < forumModerators.length; i++)
                        {
                            fsc.addModerator(forumId, forumModerators[i].trim());
                        }
                    }
                    break;
                    
                case 4 :
                    fsc.deleteForum(params);
                    break;
                    
                case 5 :
                    if (isAdmin)
                    {
                        fsc.lockForum(params, 1);
                    }
                    else if (isModerator)
                    {
                        fsc.lockForum(params, 2);
                    }
                    break;
                    
                case 6:
                    int success = 0;
                    if (isAdmin)
                    {
                        success = fsc.unlockForum(params, 1);
                    }
                    else if (isModerator)
                    {
                        success = fsc.unlockForum(params, 2);
                    }
                    if (success == 0)
                    {
                        out.println("<script language=\"Javascript\">");
                        out.println("alert(\"" + resource.getString("adminTopicLock") + "\");");
                        out.println("</script>");
                    }
                    break;
                    
                case 7 :
                    forumName = request.getParameter("forumName").trim();
                    forumDescription = request.getParameter("forumDescription").trim();
                    forumId = getIntParameter(request, "forumId");
                    keywords = request.getParameter("forumKeywords").trim();
                    forumParent = getIntParameter(request, "forumFolder");
                    forumModerators = (String[]) request.getParameterValues("moderators");
                    fsc.removeAllModerators(forumId);
                    if (forumModerators != null)
                    {
                        for (int i = 0; i < forumModerators.length; i++)
                        {
                            fsc.addModerator(forumId, forumModerators[i].trim());
                        }
                    }
                    categoryId = request.getParameter("CategoryId").trim();
                    fsc.updateForum(
                        forumId, forumName, forumDescription, forumParent, categoryId, keywords);
                    break;
                    
                case 8 :
                    forumId = getIntParameter(request, "forumId");
                    int parentId = getIntParameter(request, "parentId", 0);
                    String messageTitle = request.getParameter("messageTitle").trim();
                    String messageText = request.getParameter("messageText").trim();
                    String forumKeywords = request.getParameter("forumKeywords");
                    String subscribe = request.getParameter("subscribeMessage");
                    if ((messageTitle.length() > 0) && (messageText.length() > 0))
                    {
                        int result = fsc.createMessage(
                            messageTitle, userId, forumId, parentId, messageText, forumKeywords);
                        if (subscribe == null)
                        {
                            subscribe = "0";
                        }
                        else
                        {
                            subscribe = "1";
                            if (result != 0)
                            {
                                fsc.subscribeMessage(result, userId);
                            }
                        }
                        if (parentId > 0)
                        {
                            fsc.deployMessage(parentId);
                        }
                    }
                    break;
                    
                case 9 :
                    fsc.deleteMessage(params);
                    break;
                    
                case 10 :
                    fsc.deployMessage(params);
                    break;
                    
                case 11 :
                    fsc.undeployMessage(params);
                    break;
                    
                case 12 :
                    messageId = getIntParameter(request, "messageId");
                    int folderId = getIntParameter(request, "messageNewFolder");
                    fsc.moveMessage(messageId, folderId);
                    break;
                    
                case 13 :
                    fsc.unsubscribeMessage(params, userId);
                    break;
                    
                case 14 :
                    fsc.subscribeMessage(params, userId);
                    break;
                    
                case 15 :
                    // Modification des mots clés d'un message.
                    messageId = getIntParameter(request, "messageId");
                    keywords = request.getParameter("forumKeywords").trim();
                    fsc.updateMessageKeywords(messageId, keywords);
                    break;
                    
                case 16 :
                    // Notation d'un forum.
                    forumId = getIntParameter(request, "forumId");
                    int note = getIntParameter(request, "note", -1);
                    if (note > 0)
                    {
                        fsc.updateForumNotation(forumId, note);
                    }
                    break;
            }
        }
        catch (NumberFormatException nfe)
        {
            SilverTrace.info(
                "forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, nfe);
        }
        catch (IOException ioe)
        {
            SilverTrace.info(
                "forums", "JSPforumsListActionManager", "root.EX_NO_MESSAGE", null, ioe);
        }
    }
}
%>