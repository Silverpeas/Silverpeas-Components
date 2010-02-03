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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.silverpeas.questionReply.control.*"%>
<%@ page import="com.silverpeas.questionReply.model.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.silverpeas.containerManager.*"%>

<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.silverpeas.importExport.report.ExportReport"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%

MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

QuestionReplySessionController scc = (QuestionReplySessionController) request.getAttribute("questionReply");
if (scc == null)
{
    // No questionReply session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}
	

ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");

String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];

String language = scc.getLanguage();

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

String routerUrl = m_context + URLManager.getURL("questionReplyPDC", spaceId, componentId);
	
Window 			window 			= gef.getWindow();
BrowseBar 		browseBar 		= window.getBrowseBar();
OperationPane 	operationPane 	= window.getOperationPane();
TabbedPane 		tabbedPane 		= gef.getTabbedPane();
Frame 			frame 			= gef.getFrame();
Board 			board 			= gef.getBoard();

ContainerContext containerContext = (ContainerContext) request.getAttribute("ContainerContext");
String returnURL = (String) request.getAttribute("ReturnURL");

browseBar.setComponentName(componentLabel, "Main");

/*if (containerContext == null) {
	if (returnURL != null && returnURL.length() > 0)
		browseBar.setComponentName(componentLabel, m_context+returnURL);
	else
		browseBar.setComponentName(componentLabel, "Main");
} else {
	browseBar.setComponentName(componentLabel, m_context+containerContext.getReturnURL()); 
}*/

%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<%!
String displayIcon(String source, String messageAlt)
{
	String Html_display = "";
	Html_display = "<img src=\""+source+"\" alt=\""+messageAlt+"\" title=\""+messageAlt+"\">&nbsp;";
	return Html_display;
}
boolean existQuestionStatus(Collection questions, int status)
{
	Iterator it = questions.iterator();
	while(it.hasNext())
	{
		Question question = (Question) it.next();
		if (question.getStatus() == status)
			return true;
	}
	return false;
}
boolean existPublicR(Collection replies)
{
	Iterator it = replies.iterator();
	while(it.hasNext())
	{
		Reply reply = (Reply) it.next();
		if (reply.getPublicReply() == 1)
			return true;
	}
	return false;
}
%>
<SCRIPT LANGUAGE="JavaScript">

<!--
function vueR(replyId){
	SP_openWindow('<%=routerUrl%>ConsultReplyQuery?replyId='+replyId, 'consult_reponse', '500', '230', 'menubar=no,scrollbars=no,statusbar=no');
}
function addR(){
	SP_openWindow('<%=routerUrl%>CreateRQuery', 'consult_reponse', '500', '360', 'menubar=no,scrollbars=no,statusbar=no')
}
function updQ(){
	SP_openWindow('<%=routerUrl%>UpdateQQuery', 'consult_reponse', '500', '300', 'menubar=no,scrollbars=no,statusbar=no')
}
function updR(){
	SP_openWindow('<%=routerUrl%>UpdateRQuery', 'consult_reponse', '500', '300', 'menubar=no,scrollbars=no,statusbar=no')
}
function DeletesR(nb)
{
	if (existSelected())
	{
		if (window.confirm("<%=resource.getString("MessageSuppressionsR")%>")) { 
			document.forms[0].action = "<%=routerUrl%>DeleteReplies";
			document.forms[0].submit();
		}
	}
}
function DeleteQ(id)
{
    if (window.confirm("<%=resource.getString("MessageSuppressionQ")%>")) { 
	self.location = "<%=routerUrl%>DeleteQuestions?checkedQuestion="+id;
    }
}
function DeleteR(id)
{
    if (window.confirm("<%=resource.getString("MessageSuppressionR")%>")) { 
	self.location = "<%=routerUrl%>DeleteReply?replyId="+id;
    }
}
function DeleteRadmin(id)
{
    if (window.confirm("<%=resource.getString("MessageSuppressionR")%>")) { 
	self.location = "<%=routerUrl%>DeleteReplies?checkedReply="+id;
    }
}
function DeletesQ()
{
	if (existSelected())
	{
		if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>")) { 
		document.forms[0].action = "<%=routerUrl%>DeleteQuestions";
		document.forms[0].submit();
		}
	}
}
function ClosesQ()
{
	if (existSelected())
	{
		if (window.confirm("<%=resource.getString("MessageClosesQ")%>")) { 
		document.forms[0].action = "<%=routerUrl%>CloseQuestions";
		document.forms[0].submit();
		}
	}
}
function CloseQ(id)
{
    if (window.confirm("<%=resource.getString("MessageCloseQ")%>")) { 
	self.location = "<%=routerUrl%>CloseQuestion?questionId="+id;
    }
}


function existSelected()
{
	for (var i = 0; i < document.forms[0].length; i++) 
	{
		 if (document.forms[0].elements[i].checked)
			return true;			
	}
	return false;
}



//-->
</SCRIPT>