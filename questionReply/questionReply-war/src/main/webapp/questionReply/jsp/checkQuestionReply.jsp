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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>


<%@ page import="java.util.Collection"%>
<%@ page import="org.silverpeas.core.importexport.report.ExportReport"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.components.questionreply.model.Question" %>
<%@ page import="org.silverpeas.components.questionreply.model.Reply" %>
<%@ page import="org.silverpeas.components.questionreply.model.Category" %>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>

<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>

<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.components.questionreply.control.QuestionReplySessionController" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.core.util.file.FileRepositoryManager" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

QuestionReplySessionController scc = (QuestionReplySessionController) request.getAttribute("questionReply");
if (scc == null)
{
    // No questionReply session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}


MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");

String[] browseContext = (String[]) request.getAttribute("browseContext");
String componentId = browseContext[3];
pageContext.setAttribute("componentId", componentId);

String language = scc.getLanguage();

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

String routerUrl = m_context + URLUtil.getURL("questionReplyPDC", null, componentId);

Window 			window 			= gef.getWindow();
BrowseBar 		browseBar 		= window.getBrowseBar();
OperationPane 	operationPane 	= window.getOperationPane();
TabbedPane 		tabbedPane 		= gef.getTabbedPane();
Frame 			frame 			= gef.getFrame();
Board 			board 			= gef.getBoard();

%>
<script type="text/javascript" src="<c:url value='/util/javaScript/dateUtils.js'/>"></script>
<view:script src="/util/javaScript/checkForm.js"/>
<script type="text/javascript">

<!--
function DeleteQ(id) {
  var label = "<%=resource.getString("MessageSuppressionQ")%>";
  jQuery.popup.confirm(label, function() {
    self.location = "<%=routerUrl%>DeleteQuestions?checkedQuestion="+id;
  });
}
function CloseQ(id) {
  var label = "<%=resource.getString("MessageCloseQ")%>";
  jQuery.popup.confirm(label, function() {
    self.location = "<%=routerUrl%>CloseQuestion?questionId="+id;
  });
}

function existSelected()
{
	for (var i = 0; i < document.mainForm.length; i++)
	{
		 if (document.mainForm.elements[i].checked)
			return true;
	}
	return false;
}

//-->
</script>