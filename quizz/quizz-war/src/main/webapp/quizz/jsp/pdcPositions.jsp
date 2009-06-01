<%@ page import="javax.servlet.*,
                 com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail,
                 com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.io.File"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="checkQuizz.jsp" %>

<%

String m_context                = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String url                      = quizzScc.getComponentUrl()+"pdcPositions.jsp";

%>
<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<%
        Window                  window                  = gef.getWindow();
        Frame                   frame                   = gef.getFrame();
        OperationPane			operationPane			= window.getOperationPane();

    QuestionContainerDetail quizz = (QuestionContainerDetail) session.getAttribute("currentQuizz");

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(quizzScc.getSpaceLabel());
    browseBar.setComponentName(quizzScc.getComponentLabel());
    browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");
    browseBar.setExtraInformation(quizz.getHeader().getTitle());

    operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
    operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

    out.println(window.printBefore());

    TabbedPane tabbedPane1 = gef.getTabbedPane();
    tabbedPane1.addTab(resources.getString("GML.head"),"quizzQuestionsNew.jsp?QuizzId="+quizz.getHeader().getId()+"&Action=ViewQuizz",false);
    tabbedPane1.addTab(resources.getString("GML.PDC"),"pdcPositions.jsp",true);
    out.println(tabbedPane1.print());
    out.println(frame.printBefore());

    out.flush();
    int silverObjectId = quizzScc.getSilverObjectId(quizz.getHeader().getId()); //quizz.getHeader().getId()
    getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+ silverObjectId +"&ComponentId="+quizzScc.getComponentId()+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="pdcPositions.jsp" METHOD=POST >
        <input type="hidden" name="Action" value="ViewPdcPositions">
</FORM>
</BODY>
</HTML>