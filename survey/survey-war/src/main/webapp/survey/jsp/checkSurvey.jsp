<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.webactiv.survey.control.SurveySessionController "%>

<%@ page import="java.util.*"%>
<%@ page import="java.lang.Math"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.ParseException"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.question.model.Question "%>
<%@ page import="com.stratelia.webactiv.util.questionResult.model.QuestionResult "%>
<%@ page import="com.stratelia.webactiv.util.answer.model.Answer "%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader "%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail "%>
<%@ page import="com.stratelia.webactiv.util.questionResult.model.QuestionResult "%>
<%@ page import="com.stratelia.webactiv.survey.SurveyException"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.Comment"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Vector"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
    SurveySessionController surveyScc = (SurveySessionController) request.getAttribute("surveyScc");
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
    ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");

    if (surveyScc == null) {
        // No session controller in the request -> security exception
        String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
        return;
    }
    
    ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(surveyScc.getLanguage());
    
    String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel 		= browseContext[0];
	String componentLabel 	= browseContext[1];
	String spaceId 			= browseContext[2];
	String componentId 		= browseContext[3];
    
    boolean pollingStationMode = (componentId != null && componentId.startsWith("pollingStation"));
%>