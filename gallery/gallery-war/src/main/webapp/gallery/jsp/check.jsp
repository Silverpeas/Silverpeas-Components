<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Calendar"%>

<%@ page import="java.text.NumberFormat"%>
<%@ page import="java.text.ParsePosition"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail "%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBarElement"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList"%>

<%@ page import="com.stratelia.webactiv.node.control.NodeService"%>
<%@ page import="com.stratelia.webactiv.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.node.model.NodePK"%>

<%@ page import="org.silverpeas.components.gallery.constant.MediaResolution" %>
<%@ page import="org.silverpeas.components.gallery.control.GallerySessionController"%>
<%@ page import="org.silverpeas.components.gallery.model.AlbumDetail"%>
<%@ page import="org.silverpeas.components.gallery.model.Media"%>
<%@ page import="org.silverpeas.components.gallery.model.MetaData"%>
<%@ page import="org.silverpeas.components.gallery.model.Order"%>
<%@ page import="org.silverpeas.components.gallery.model.OrderRow"%>
<%@ page import="org.silverpeas.components.gallery.model.Photo" %>
<%@ page import="org.silverpeas.components.gallery.ParameterNames"%>
<%@ page import="org.silverpeas.components.gallery.GalleryComponentSettings" %>

<%@ page import="org.silverpeas.util.StringUtil"%>
<%@ page import="org.silverpeas.util.EncodeHelper"%>

<%@ page import="com.silverpeas.form.DataRecord" %>
<%@ page import="com.silverpeas.form.Form" %>
<%@ page import="com.silverpeas.form.PagesContext" %>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%!
    String displayPath(Collection path, BrowseBar browseBar) {
        //creation du chemin
        String chemin = "";
        boolean suivant = false;
        if (path != null) {
          Iterator itPath = path.iterator();
          while (itPath.hasNext()) {
            NodeDetail unAlbum = (NodeDetail) itPath.next();
            if (unAlbum.getId() != 0) {
              browseBar.addElement(new BrowseBarElement(unAlbum.getName(), "ViewAlbum?Id=" + unAlbum.
                  getNodePK().getId(), unAlbum.getNodePK().getId()));
            }
          }
        }
        return chemin;
      }
%>

<%
	GallerySessionController gallerySC = (GallerySessionController) request.getAttribute("gallerySC");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
	String m_context = URLManager.getApplicationURL();
	MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	OperationPane operationPane = window.getOperationPane();
	String[] browseContext = (String[]) request.getAttribute("browseContext");
 	String spaceLabel = browseContext[0];
 	String componentLabel = browseContext[1];
 	String spaceId = browseContext[2];
	String componentId = browseContext[3];
	Frame frame = gef.getFrame();
%>