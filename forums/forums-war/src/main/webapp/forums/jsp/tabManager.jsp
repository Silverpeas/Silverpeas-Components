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