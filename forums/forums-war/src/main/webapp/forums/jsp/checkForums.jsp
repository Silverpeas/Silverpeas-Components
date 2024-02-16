<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.silverpeas.kernel.util.StringUtil"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="org.silverpeas.components.forums.service.ForumsException"%>
<%@ page import="org.silverpeas.components.forums.model.Forum"%>
<%@ page import="org.silverpeas.components.forums.model.Message"%>
<%@ page import="org.silverpeas.components.forums.control.ForumsSessionController"%>
<%@ page import="org.silverpeas.components.forums.url.ActionUrl"%>
<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil"%>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.kernel.bundle.LocalizationBundle" %>
<%@ page import="org.silverpeas.kernel.logging.SilverLogger" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%!  
  public static String IMAGE_UPDATE = "../../util/icons/update.gif";
  public static String IMAGE_UNLOCK = "../../util/icons/lock.gif";
  public static String IMAGE_LOCK = "../../util/icons/unlock.gif";
  public static String IMAGE_DELETE = "../../util/icons/delete.gif";
  public static String IMAGE_MOVE = "../../util/icons/moveMessage.gif";
  public static String IMAGE_ADD_FORUM = "../../util/icons/create-action/add-forum.png";
  public static String IMAGE_ADD_CATEGORY = "../../util/icons/create-action/add-folder.png";
  public static String IMAGE_WORD = "icons/word.gif";
  public static String IMAGE_NOTATION_OFF = "../../util/icons/starEmpty.gif";
  public static String IMAGE_NOTATION_ON = "../../util/icons/starFilled.gif";
  public static String IMAGE_NOTATION_EMPTY = "../../util/icons/shim.gif";
  private static final String STATUS_VALIDATE = "V";
  private static final String STATUS_FOR_VALIDATION = "A";
  private static final String STATUS_REFUSED = "R";

  public int getIntParameter(HttpServletRequest request, String name) {
    return getIntParameter(request, name, -1);
  }

  public int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
    String param = request.getParameter(name);
    if (param != null) {
      param = param.trim();
      if (param.length() > 0) {
        return Integer.parseInt(param);
      }
    }
    return defaultValue;
  }

  public String convertDate(Date date, MultiSilverpeasBundle resources) {
    return resources.getOutputDateAndHour(date);
  }

  public void addBodyOnload(JspWriter out, ForumsSessionController fsc) {
    addBodyOnload(out, fsc, "");
  }

  public void addBodyOnload(JspWriter out, ForumsSessionController fsc, String call) {
    try {
      if (call == null) {
        call = "";
      }
      out.print("onload=\"");
      out.print(call);
      if (fsc.isResizeFrame()) {
        if (call.length() > 0 && !call.endsWith(";")) {
          out.print(";");
        }
        out.print("resizeFrame();");
      }
      out.print("\"");
    } catch (IOException ioe) {
      SilverLogger.getLogger(this).error(ioe);
    }
  }

  public void addJsResizeFrameCall(JspWriter out, ForumsSessionController fsc) {
    try {
      if (fsc.isResizeFrame()) {
        out.print("resizeFrame();");
      }
    } catch (IOException ioe) {
      SilverLogger.getLogger(this).error(ioe);
    }
  }
%>
<%
      ForumsSessionController fsc = (ForumsSessionController) request.getAttribute(
          "forumsSessionClientController");
      MultiSilverpeasBundle resources = (MultiSilverpeasBundle) request.getAttribute("resources");

      LocalizationBundle resource = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.forums.multilang.forumsBundle", fsc.getLanguage());

      if (fsc == null) {
        // No forums session controller in the request -> security exception
        String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString(
            "sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request,
            response);
        return;
      }

      String context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

      GraphicElementFactory graphicFactory = (GraphicElementFactory) session.getAttribute(
          "SessionGraphicElementFactory");

      String[] browseContext = (String[]) request.getAttribute("browseContext");
      String spaceLabel = browseContext[0];
      String componentLabel = browseContext[1];
      String spaceId = browseContext[2];
      String instanceId = browseContext[3];

      String userId = fsc.getUserId();
      boolean isAdmin = fsc.isAdmin();
      boolean isUser = fsc.isUser();
      boolean isReader = fsc.isReader();
%>