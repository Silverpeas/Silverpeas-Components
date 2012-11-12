<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.*"%>
<%@ page import="java.lang.Math"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.ParseException"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>

<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.survey.control.SurveySessionController "%>
<%@ page import="com.stratelia.webactiv.survey.SurveyException"%>

<%@ page import="org.silverpeas.servlets.FileServer"%>

<%@ page import="com.stratelia.webactiv.quizz.QuestionHelper"%>
<%@ page import="com.stratelia.webactiv.quizz.QuestionForm"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.question.model.Question "%>
<%@ page import="com.stratelia.webactiv.util.questionResult.model.QuestionResult "%>
<%@ page import="com.stratelia.webactiv.util.answer.model.Answer "%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%@ page import="com.stratelia.webactiv.util.questionContainer.model.Comment"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader "%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail "%>
<%@ page import="com.stratelia.webactiv.util.questionResult.model.QuestionResult "%>

<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.FileServerUtils"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>

<%@ page import="com.silverpeas.util.web.servlet.FileUploadUtil"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
  SurveySessionController surveyScc = (SurveySessionController) request.getAttribute("survey");
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