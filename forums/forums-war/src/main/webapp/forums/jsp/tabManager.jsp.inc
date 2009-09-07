<%@ page import="com.stratelia.webactiv.util.ResourceLocator,
                 java.util.ArrayList"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.CompletePublication"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.forums.sessionController.ForumsSessionController"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%!

  public String actionURL(boolean admin, String page, String action, String params, String forum) {
    // String retour = "/forums/forums/jsp/" + page + ".jsp?";
    String retour = page + ".jsp?";
    if (admin) retour = retour + "admin=1";
    else retour = retour + "admin=0";
    if (action.length()>0) retour = retour + "&action=" + action;
    if (params.length()>0) retour = retour + "&params=" + params;
    if (forum.length()>0) retour = retour + "&forum=" + forum;
    return retour;
  }

  
void displayTabs(String params, String forum, ForumsSessionController forumsScc, GraphicElementFactory gef, String action, JspWriter out) throws IOException 
{

	ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.forums.multilang.forumsBundle", forumsScc.getLanguage());
	String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("forums", forumsScc.getSpaceId(), forumsScc.getComponentId());
      
      boolean enabled = false;
      if (forum.length()>0)
          enabled = true;
          
      String tabName;
      TabbedPane tabbedPane = gef.getTabbedPane();		//actionURL(admin, "editForumInfo", "2", String.valueOf(forumId), String.valueOf(currentPage))
	  tabbedPane.addTab(message.getString("Description"), routerUrl+"editForumInfo.jsp?call=main&action=2&params="+params+"&forum="+forum, !action.equals("ViewPdcPositions"), enabled);	  
	  tabbedPane.addTab( message.getString("Categorization"), routerUrl+"pdcPositions.jsp?Action=ViewPdcPositions&params="+params+"&forum="+forum, action.equals("ViewPdcPositions"), enabled);
      
      out.println(tabbedPane.print());
}


void displayBeginFrame(JspWriter out) throws IOException {
		out.println("<!-- Cadre extérieur -->");
       	out.println("<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor align=\"center\" valign=\"top\">");
       	 out.println("<tr>");
        	out.println("<td>");

          		out.println("<!-- Cadre intérieur -->");
           		out.println("<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4 align=\"center\">");
             		out.println("<tr valign=\"middle\">");
             			 out.println("<td align=\"center\">");

}
void displayEndFrame(JspWriter out) throws IOException {
    					 out.println("</TD></TR></TABLE> <!-- Fin cadre intérieur -->");
   		out.println("</TD></TR></TABLE> <!-- Fin cadre extérieur -->");
}

%>