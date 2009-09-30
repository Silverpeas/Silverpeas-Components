<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.stratelia.webactiv.util.node.control.NodeBm"%>
<%@ page import="com.stratelia.webactiv.util.node.control.NodeBmHome"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%@ page import="com.silverpeas.gallery.control.GallerySessionController"%>
<%@ page import="com.silverpeas.gallery.model.AlbumDetail"%>
<%@ page import="com.silverpeas.gallery.model.PhotoDetail"%>
<%@ page import="com.silverpeas.gallery.model.Order"%>
<%@ page import="com.silverpeas.gallery.model.OrderRow"%>
<%@ page import="com.silverpeas.gallery.ParameterNames"%>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.io.File"%>

<%@ page import="java.text.NumberFormat"%>
<%@ page import="java.text.ParsePosition"%>
<%@ page import="java.util.StringTokenizer"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Calendar"%>

<%@ page import="com.stratelia.webactiv.searchEngine.model.* "%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail "%>
<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.stratelia.webactiv.util.publication.info.model.ModelDetail"%>
<%@ page import="com.silverpeas.gallery.model.MetaData"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%!
	String displayPath(Collection path)
	{
		//création du chemin
		String chemin = "";
		boolean suivant = false;
		if (path != null)
		{
			Iterator itPath = (Iterator) path.iterator();
			while (itPath.hasNext()) 
			{
				NodeDetail unAlbum = (NodeDetail) itPath.next();
				if (unAlbum.getId() != 0)
				{
					if (suivant) 
						chemin = " >> " + chemin;
					chemin = "<a href=\"ViewAlbum?Id="+ unAlbum.getNodePK().getId() + "\">" + Encode.javaStringToHtmlString(unAlbum.getName())+"</a>" + chemin;
					if (itPath.hasNext()) 
				  		suivant = true;
				}
			}	
		}
		return chemin;
	}
%>

<%
	GallerySessionController gallerySC = (GallerySessionController) request.getAttribute("gallerySC");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
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