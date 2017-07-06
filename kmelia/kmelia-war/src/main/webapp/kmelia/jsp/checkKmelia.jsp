<%--

    Copyright (C) 2000 - 2017 Silverpeas

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

<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>

<%@ page import="java.io.File"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List" %>

<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Collections" %>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory "%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.util.file.FileRepositoryManager"%>
<%@ page import="org.silverpeas.core.importexport.attachment.AttachmentDetail"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>

<%@ page import="org.silverpeas.core.contribution.publication.model.PublicationDetail"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.PublicationPK"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.CompletePublication"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.ValidationStep"%>
<%@ page import="org.silverpeas.core.node.model.NodePK"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.core.node.model.NodeI18NDetail"%>


<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>

<%@ page import="org.silverpeas.components.kmelia.control.KmeliaSessionController"%>
<%@ page import="org.silverpeas.components.kmelia.KmeliaException"%>
<%@ page import="org.silverpeas.components.kmelia.service.KmeliaHelper"%>
<%@page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>

<%@ page import="org.silverpeas.core.comment.model.Comment"%>
<%@ page import="org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController"%>
<%@ page import="org.silverpeas.core.contribution.content.wysiwyg.WysiwygException"%>

<%@ page import="org.silverpeas.core.ForeignPK"%>
<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.core.contribution.template.publication.PublicationTemplate"%>
<%@ page import="org.silverpeas.core.silverstatistics.access.model.HistoryByUser"%>
<%@page import="org.silverpeas.components.kmelia.KmeliaConstants"%>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>

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

	String routerUrl = URLUtil.getApplicationURL() + URLUtil.getURL("kmelia", spaceId, componentId);

	String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

	boolean kmaxMode 	= (componentId != null && componentId.startsWith("kmax"));
	boolean toolboxMode = (componentId != null && componentId.startsWith("toolbox"));
	boolean kmeliaMode 	= (componentId != null && componentId.startsWith("kmelia"));

	SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.kmelia.settings.kmeliaSettings");
%>