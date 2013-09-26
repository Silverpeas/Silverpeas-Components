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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.contact.model.ContactDetail, com.stratelia.webactiv.util.contact.model.CompleteContact"%>
<%@ page import="java.util.Iterator, java.util.ArrayList, java.util.Collection, java.util.Date"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.EJBException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="java.io.File"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>

<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@ page import="com.silverpeas.form.DataRecord"%>
<%@ page import="com.silverpeas.form.Form"%>
<%@ page import="com.silverpeas.form.RecordSet"%>
<%@ page import="com.silverpeas.form.PagesContext"%>


<%@ include file="checkYellowpages.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="modelUtils.jsp.inc" %>

<%
String contactId = (String) request.getAttribute("ContactId");
String modelId = (String) request.getAttribute("ModelId");
String action = (String) request.getAttribute("Action");

String linkedPathString = yellowpagesScc.getPath();

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=reallyClose();", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=goToModel()", false);
Button sendNewButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendModelData('Add')", false);

if (action.equals("NewModel")) {

	Form formUpdate    = (Form) request.getAttribute("Form");
	DataRecord data    = (DataRecord) request.getAttribute("Data"); 
	PagesContext context = (PagesContext) request.getAttribute("PagesContext");
	context.setBorderPrinted(true);
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">

function topicAddGoTo() {
  document.topicAddLink.submit();
}

function sendModelData(operation) {
	document.modelForm.Action.value = operation;
    document.modelForm.submit();
}

function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}
function reallyClose()
{
  window.opener.document.topicDetailForm.Action.value = "Search";
  window.opener.document.topicDetailForm.submit();
  window.close();
}
</script>
<%
	formUpdate.displayScripts(out, context);
%>
</HEAD>
<BODY class="yui-skin-sam">
<%
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resources.getString("Model"));

	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation(resources.getIcon("yellowpages.contactTopicLink"), yellowpagesScc.getString("TopicLink"), "javascript:topicAddGoTo();");

	out.println(window.printBefore());
	displayContactOperations(resources, contactId, gef, action, out, true);
	out.println(frame.printBefore());

%> 
<FORM NAME="modelForm" ACTION="modelManager.jsp" METHOD="POST" ENCTYPE="multipart/form-data">
<input type="hidden" name="ModelId" value="<%=modelId%>">
<input type="hidden" name="ContactId" value="<%=contactId%>">
<input type="hidden" name="Action">

<%
    formUpdate.display(out, context, data); 
%>
</FORM>

<%
	out.println(frame.printMiddle());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(sendNewButton);
	buttonPane.addButton(cancelButton);
	buttonPane.setHorizontalPosition();
	out.println("<BR><center>"+buttonPane.print()+"</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST >
  <input type="hidden" name="Action"><input type="hidden" name="Id" value="">
</FORM>
</BODY>
</HTML>
<%
} else if (action.equals("Add")) {
    %>
    <BODY onload="reallyClose()">
    <script>
      <!--
      function reallyClose()
      {
        window.opener.document.topicDetailForm.Action.value = "Search";
        window.opener.document.topicDetailForm.submit();
        window.close();
      }
      reallyClose();
      //-->
    </script>
    </BODY>
    <%   
}
%>
<form name="topicAddLink" action="TopicLink.jsp" method="post">
<input type=hidden name=ContactId value="<%=contactId%>"/>
</form>