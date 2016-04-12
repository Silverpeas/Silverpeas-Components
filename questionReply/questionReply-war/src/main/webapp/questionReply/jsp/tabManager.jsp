<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="java.io.IOException" %>
<%!

      void displayTabs(boolean updateQ, boolean pdc, String id, MultiSilverpeasBundle resource,
          GraphicElementFactory gef, String action, String routerUrl, JspWriter out)
          throws IOException {
            TabbedPane tabbedPane = gef.getTabbedPane();
            if (updateQ) {
                  tabbedPane.addTab(resource.getString("questionReply.question"),
                      routerUrl + "UpdateQ?QuestionId=" + id, action.equals("CreateQQuery"), true);
            }
            if (pdc) {
                  tabbedPane.addTab(resource.getString("GML.PDC"),
                      routerUrl + "ViewPdcPositions?questionId=" + id,
                      action.equals("ViewPdcPositions"), true);
            }
            out.println(tabbedPane.print());
      }
%>