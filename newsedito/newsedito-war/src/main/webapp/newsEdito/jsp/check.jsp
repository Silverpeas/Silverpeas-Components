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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.rmi.RemoteException"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.naming.NamingException"%>
<%@ page import="javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException"%>
<%@ page import="javax.ejb.CreateException"%>
<%@ page import="javax.ejb.FinderException"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>

<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>

<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationPK"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.CompletePublication"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.ValidationStep"%>

<%@ page import="com.stratelia.silverpeas.wysiwyg.control.WysiwygController"%>
<%@ page import="com.stratelia.silverpeas.wysiwyg.WysiwygException"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.i18n.*"%>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@ page import="com.stratelia.webactiv.util.statistic.model.HistoryByUser"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate" %>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplateException" %>
<%@ page import="com.silverpeas.form.DataRecord" %>
<%@ page import="com.silverpeas.form.Form" %>
<%@ page import="com.silverpeas.form.PagesContext" %>
<%@ page import="com.silverpeas.form.RecordSet" %>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplateImpl" %>
<%@ page import="com.stratelia.webactiv.newsEdito.control.NewsEditoSessionController" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
	NewsEditoSessionController newsSC = (NewsEditoSessionController) request.getAttribute("newsEdito");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	if (newsSC == null) {
	    // No session controller in the request -> security exception
	    String sessionTimeout = GeneralPropertiesManager.getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}

	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel = browseContext[0];
	String componentLabel = browseContext[1];
	String spaceId = browseContext[2];
	String componentId = browseContext[3];

	String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("newsEdito", spaceId, componentId);

	//R�cup�ration du contexte
	String m_context = GeneralPropertiesManager.getString("ApplicationURL");
%>