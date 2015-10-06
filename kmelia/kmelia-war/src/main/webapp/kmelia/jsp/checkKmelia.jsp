<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="org.silverpeas.util.DBUtil"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.FileRepositoryManager"%>
<%@ page import="org.silverpeas.importExport.attachment.AttachmentDetail"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.pagination.Pagination"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>

<%@ page import="org.silverpeas.util.exception.*"%>
<%@ page import="com.stratelia.webactiv.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.webactiv.publication.model.PublicationPK"%>
<%@ page import="com.stratelia.webactiv.publication.model.CompletePublication"%>
<%@ page import="com.stratelia.webactiv.publication.model.ValidationStep"%>
<%@ page import="com.stratelia.webactiv.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.node.model.NodeI18NDetail"%>


<%@ page import="org.silverpeas.search.searchEngine.model.* "%>

<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>

<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@ page import="com.stratelia.webactiv.kmelia.KmeliaException"%>
<%@ page import="com.stratelia.webactiv.kmelia.model.*"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper"%>
<%@page import="org.silverpeas.kmelia.jstl.KmeliaDisplayHelper"%>

<%@ page import="com.silverpeas.comment.model.Comment"%>
<%@ page import="org.silverpeas.wysiwyg.control.WysiwygController"%>
<%@ page import="org.silverpeas.wysiwyg.WysiwygException"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ page import="org.silverpeas.util.ForeignPK"%>
<%@ page import="org.silverpeas.util.StringUtil"%>
<%@ page import="org.silverpeas.util.EncodeHelper"%>
<%@ page import="org.silverpeas.util.i18n.*"%>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@ page import="com.stratelia.webactiv.statistic.model.HistoryByUser"%>
<%@page import="com.silverpeas.kmelia.KmeliaConstants"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="org.silverpeas.util.SettingBundle" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
	KmeliaSessionController kmeliaScc = (KmeliaSessionController) request.getAttribute("kmelia");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	if (kmeliaScc == null) {
	    // No session controller in the request -> security exception
	    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}

	MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");

	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel = browseContext[0];
	String componentLabel = browseContext[1];
	String spaceId = browseContext[2];
	String componentId = browseContext[3];

	String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("kmelia", spaceId, componentId);

	String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

	boolean kmaxMode 	= (componentId != null && componentId.startsWith("kmax"));
	boolean toolboxMode = (componentId != null && componentId.startsWith("toolbox"));
	boolean kmeliaMode 	= (componentId != null && componentId.startsWith("kmelia"));

	SettingBundle settings = ResourceLocator.getSettingBundle("com.stratelia.webactiv.kmelia.settings.kmeliaSettings");
%>