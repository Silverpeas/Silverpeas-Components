<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%@ page import="java.beans.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.io.File"%>
<%@ page import="java.lang.Integer"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.ParsePosition"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.text.ParsePosition"%>
<%@ page import="java.util.*"%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="javax.ejb.RemoveException,javax.ejb.CreateException,java.sql.SQLException,javax.naming.NamingException,java.rmi.RemoteException,javax.ejb.FinderException"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.silverpeas.util.web.servlet.FileUploadUtil"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.quizz.QuizzException"%>
<%@ page import="com.stratelia.webactiv.quizz.QuestionForm"%>
<%@ page import="com.stratelia.webactiv.quizz.QuestionHelper"%>
<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.score.model.ScoreDetail"%>
<%@ page import="com.stratelia.webactiv.util.question.model.Question"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail"%>
<%@ page import="com.stratelia.webactiv.util.questionResult.model.QuestionResult"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.question.model.Question"%>
<%@ page import="com.stratelia.webactiv.util.answer.model.Answer"%>
<%@ page import="com.stratelia.webactiv.util.score.model.*"%>
<%@ page import="com.stratelia.webactiv.util.score.control.*"%>

<%@ page import="org.apache.commons.fileupload.FileItem"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
  GraphicElementFactory gef =
      (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  QuizzSessionController quizzScc = (QuizzSessionController) request.getAttribute("quizz");
  ResourcesWrapper resources = (ResourcesWrapper) request.getAttribute("resources");
  if (quizzScc == null) {
    String sessionTimeout =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(
        request, response);
    return;
  }
%>