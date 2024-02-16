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
<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.components.yellowpages.control.DisplayContactsHelper" %>
<%@ page import="org.silverpeas.components.yellowpages.model.TopicDetail" %>
<%@ page import="org.silverpeas.kernel.util.StringUtil" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp" %>

<% 
String rootId = Integer.toString(ROOT_TOPIC);

Collection path = null;
String linkedPathString = "";

//Recuperation des parametres
String id = request.getParameter("Id");
String profile = request.getParameter("Profile");

//Mise a jour de l'espace
TopicDetail currentTopic;
if (!StringUtil.isDefined(id)) {
    currentTopic = yellowpagesScc.getCurrentTopic();
    if (currentTopic != null) {
		id = currentTopic.getNodePK().getId();
    } else {
		id=rootId;
    }
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<view:includePlugin name="popup"/>
<script type="text/javascript">
var userAddWindow = window;
var importCSVWindow = window;

function topicGoTo(id) {
	closeWindows();	
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function importCSV(id) 
{
	importCSVWindow = SP_openWindow("ToImportCSV", "printWindow", '600', '220', 'scrollbars=yes, alwayRaised');
}

function simpleTopicGoToSelected() {
    var id = document.topicDetailForm.selectSimpleTopic.options[document.topicDetailForm.selectSimpleTopic.selectedIndex].value;
    topicGoTo(id);
}

<% if (profile.equals("admin")) { %>
function ifCorrectFormExecute(callback) {
   var errorMsg = "";
   var errorNb = 0;
   var title = stripInitialWhitespace(window.document.AddAndUpdateFolderForm.Name.value);
   if (isWhitespace(title)) {
     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=yellowpagesScc.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     errorNb++; 
   }
   switch(errorNb) {
      case 0 :
          callback.call(this);
          break;
      case 1 :
          errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
          jQuery.popup.error(errorMsg);
          break;
      default :
          errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
          jQuery.popup.error(errorMsg);
   }
}

function topicDeleteConfirm(childId, name) {
  var label = "<%=yellowpagesScc.getString("ConfirmDeleteTopic")%> '" + name + "' ?";
  jQuery.popup.confirm(label, function() {
    document.topicDetailForm.action = "DeleteFolder";
    document.topicDetailForm.ToDeleteId.value = childId;
    document.topicDetailForm.submit();
  });
}
function deleteBasketContent()
{
	document.topicDetailForm.action = "DeleteBasketContent";
  document.topicDetailForm.submit();
}

function groupDeleteConfirm(childId, name) {
  var label = "<%=yellowpagesScc.getString("ConfirmDeleteTopic")%> '" + name + "' ?";
  jQuery.popup.confirm(label, function() {
    document.topicDetailForm.action = "RemoveGroup";
    document.topicDetailForm.ToDeleteId.value = childId;
    document.topicDetailForm.submit();
  });
}
<% } %>

function closeWindows() {
    if (!userAddWindow.closed && userAddWindow.name === "userAddWindow")
        userAddWindow.close();
}

function contactAdd() {
	location.href = "ContactNew";
}

function addGroup() {
    closeWindows();
    url = "ToChooseGroup";
    windowName = "userAddWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
    userAddWindow = SP_openWindow(url, windowName, '750' , '600' , windowParams);
}

function contactGoTo(id) {
  sp.formRequest("ContactUpdate?ContactId=" + id).byPostMethod().submit();
}

function contactDeleteConfirm(id) {
  var label = "<%=yellowpagesScc.getString("ConfirmDeleteContact")%> ?";
  jQuery.popup.confirm(label, function() {
    document.contactDeleteForm.action = "DeleteContact";
    document.contactDeleteForm.ContactId.value = id;
    document.contactDeleteForm.submit();
  });
}

function consult() {
    closeWindows();
	location.href = "Main.jsp";
}

function toAddOrUpdateFolder(action, id) {
  var dialogTitle = '<%=WebEncodeHelper.javaStringToJsString(resources.getString("TopicCreationTitle"))%>';
  if (action === 'ToUpdateFolder') {
    dialogTitle = '<%=WebEncodeHelper.javaStringToJsString(resources.getString("TopicUpdateTitle"))%>';
  }

  new Promise(function(resolve, reject) {
    $.ajax({
      url: webContext+'<%=URLUtil.getURL("yellowpages", null, componentId)%>'+action+'?Id='+id,
      type: "GET",
      dataType: "html",
      success: function(data) {
        $('#folderDialog').html(data);
        resolve();
      },
      error: function() {
        resolve();
      }
    });
  }).then(function() {
    $('#folderDialog').popup('validation', {
      title : dialogTitle,
      callback : function() {
        ifCorrectFormExecute(function() {
          window.document.AddAndUpdateFolderForm.submit();
        });
        return false;
      }
    });
  });
}

</script>
</head>
    <body>
    <form name="topicDetailForm" action="topicManager.jsp" method="post">
<%
    currentTopic = yellowpagesScc.getTopic(id);
    yellowpagesScc.setCurrentTopic(currentTopic);
    path = currentTopic.getPath();
    linkedPathString = displayPath(yellowpagesScc, path, true, 3);
    yellowpagesScc.setPath(linkedPathString);

    Window window = gef.getWindow();
    BrowseBar browseBar=window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resources.getString("GML.management")+" > "+linkedPathString);

    OperationPane operationPane = window.getOperationPane();
    if (profile.equals("admin"))
    {
		if (!id.equals(TRASHCAN_ID)){
			operationPane.addOperation(resources.getIcon("yellowpages.modelUsed"), resources.getString("yellowpages.ModelUsed"), "ModelUsed");
			operationPane.addLine();
			operationPane.addOperationOfCreation(resources.getIcon("yellowpages.folderAdd"), resources.getString("CreerSousTheme"), "javascript:onClick=toAddOrUpdateFolder('ToAddFolder')");
			operationPane.addOperationOfCreation(resources.getIcon("yellowpages.groupAdd"), resources.getString("GroupAdd"), "javascript:onClick=addGroup()");
			operationPane.addLine();
		}
		else
		{
			operationPane.addOperation(resources.getIcon("yellowpages.basketDelete"), resources.getString("yellowpages.DeleteBasketContent"), "javascript:onClick=deleteBasketContent()");
		}
    }

	// Si nous sommes dans la corbeille, alors nous ne pouvons cr�er un contact dedans !!
	if (!id.equals(TRASHCAN_ID)){
		operationPane.addOperationOfCreation(resources.getIcon("yellowpages.contactAdd"), yellowpagesScc.getString("ContactCreer"), "javascript:onClick=contactAdd()");
		operationPane.addOperationOfCreation(resources.getIcon("yellowpages.importCSV"), resources.getString("yellowpages.importCSV"), "javascript:onClick=importCSV('"+id+"')");
		operationPane.addLine();
		operationPane.addOperation(resources.getIcon("yellowpages.basket"), yellowpagesScc.getString("ContactBasket"), "javascript:onClick=topicGoTo('1')");
  	}

    //Onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(yellowpagesScc.getString("Consultation"),"javascript:consult();",false);
    tabbedPane.addTab(resources.getString("GML.management"),"#",true);

    out.println(window.printBefore());
    out.println(tabbedPane.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- AFFICHAGE HEADER -->
<%
    if (!id.equals(TRASHCAN_ID) && !id.equals("2")) {
        if (profile.equals("admin"))
            displayTopicsToAdmin(yellowpagesScc, id, "<br>", gef, pageContext, request, session, resources, out);
        else
            displayTopicsToUsers(yellowpagesScc, id, "<br>", profile, gef, pageContext, request, session, resources, out);
    }

	out.println("<br/>");

    DisplayContactsHelper.displayContactsAdmin(yellowpagesScc, gef, request, session, resources, out);
%>
</view:frame>
<%
    out.println(window.printAfter());
%>

<input type="hidden" name="Action"/>
<input type="hidden" name="Id" value="<%=id%>"/>
<input type="hidden" name="ToDeleteId" value=""/>
</form>

<form name="contactDeleteForm" action="topicManager.jsp" method="post">
<input type="hidden" name="Action"/>
<input type="hidden" name="ContactId"/>
<input type="hidden" name="Id" value="<%=id%>"/>
</form>
<form name="refreshList" action="topicManager"></form>

<div id="folderDialog" style="display:none" title="">
</div>

</body>
</html>