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
%><%@ page import="java.util.ArrayList"
%><%@page import="com.stratelia.webactiv.forums.url.ActionUrl"
%><%@ page import="com.stratelia.webactiv.util.ResourceLocator"
%><%@ page import="com.stratelia.webactiv.util.publication.model.CompletePublication"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"
%><%@ page import="com.stratelia.webactiv.forums.sessionController.ForumsSessionController"
%><%@ page import="com.stratelia.silverpeas.peasCore.URLManager"
%>
<%!
public void displayTabs(int params, int forumId, ForumsSessionController forumsScc,
        GraphicElementFactory gef, String action, JspWriter out)
    throws IOException 
{
	ResourceLocator resource = new ResourceLocator(
        "com.stratelia.webactiv.forums.multilang.forumsBundle", forumsScc.getLanguage());
	String routerUrl = URLManager.getApplicationURL()
        + URLManager.getURL("forums", forumsScc.getSpaceId(), forumsScc.getComponentId());
    boolean enabled = (forumId != -1);
    
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("Description"),
        routerUrl + ActionUrl.getUrl("editForumInfo", "main", 2, params, forumId),
        !action.equals("ViewPdcPositions"), enabled);
    tabbedPane.addTab(resource.getString("Categorization"),
        routerUrl + "pdcPositions.jsp?Action=ViewPdcPositions&params=" + params + "&forumId=" + forumId,
        action.equals("ViewPdcPositions"), enabled);
    out.println(tabbedPane.print());
}

public void displayBeginFrame(JspWriter out)
    throws IOException
{
    out.println("<!-- Cadre extérieur -->");
    out.println("<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"98%\" class=\"intfdcolor\" align=\"center\" valign=\"top\">");
    out.println("<tr>");
    out.println("<td>");
    
    out.println("<!-- Cadre intérieur -->");
    out.println("<table cellpadding=\"5\" cellspacing=\"0\" border=\"0\" width=\"100%\" class=\"intfdcolor4\" align=\"center\">");
    out.println("<tr valign=\"middle\">");
    out.println("<td align=\"center\">");
}

void displayEndFrame(JspWriter out)
    throws IOException
{
    out.println("</td></tr></table> <!-- Fin cadre intérieur -->");
   	out.println("</td></tr></table> <!-- Fin cadre extérieur -->");
}

%>