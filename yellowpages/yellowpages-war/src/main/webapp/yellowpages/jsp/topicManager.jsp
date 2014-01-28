<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.yellowpages.control.DisplayContactsHelper" %>
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
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>

<% 
String rootId = Integer.toString(ROOT_TOPIC);

Collection path = null;
String linkedPathString = "";

//Recuperation des parametres
String id = request.getParameter("Id");
String profile = request.getParameter("Profile");

//Mise a jour de l'espace
TopicDetail currentTopic = null;
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
<view:looknfeel />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js" />"></script>
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
function isCorrectForm() {
   var errorMsg = "";
   var errorNb = 0;
   var title = stripInitialWhitespace(window.document.AddAndUpdateFolderForm.Name.value);
   if (isWhitespace(title)) {
     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=yellowpagesScc.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
     errorNb++; 
   }
   switch(errorNb) {
      case 0 :
          result = true;
          break;
      case 1 :
          errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
      default :
          errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
   }
   return result;
}

function topicDeleteConfirm(childId, name) {
    if(window.confirm("<%=yellowpagesScc.getString("ConfirmDeleteTopic")%> '" + name + "' ?")){
          document.topicDetailForm.action = "DeleteFolder";
          document.topicDetailForm.ToDeleteId.value = childId;
          document.topicDetailForm.submit();
    }
}
function deleteBasketContent()
{
	document.topicDetailForm.action = "DeleteBasketContent";
  document.topicDetailForm.submit();
}

function groupDeleteConfirm(childId, name) {
    if(window.confirm("<%=resources.getString("ConfirmDeleteTopic")%> '" + name + "' ?")){
      document.topicDetailForm.action = "RemoveGroup";
      document.topicDetailForm.ToDeleteId.value = childId;
      document.topicDetailForm.submit();
    }
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
	location.href = "ContactUpdate?ContactId="+id;
}

function contactDeleteConfirm(id) {
    if(window.confirm("<%=yellowpagesScc.getString("ConfirmDeleteContact")%> ?")){
          document.contactDeleteForm.action = "DeleteContact";
          document.contactDeleteForm.ContactId.value = id;
          document.contactDeleteForm.submit();
    }
}

function consult() {
    closeWindows();
	location.href = "Main.jsp";
}

function toAddOrUpdateFolder(action, id) {
	$.ajax({
		url: webContext+'<%=URLManager.getURL("yellowpages", null, componentId)%>'+action+'?Id='+id,
		async: false,
		type: "GET",
		dataType: "html",
		success: function(data) {
			  $('#folderDialog').html(data);
			  if (action === 'ToUpdateFolder') {
			  	$('#folderDialog').attr('title', '<%=EncodeHelper.javaStringToJsString(resources.getString("TopicUpdateTitle"))%>');
			  } else {
				$('#folderDialog').attr('title', '<%=EncodeHelper.javaStringToJsString(resources.getString("TopicCreationTitle"))%>');
			  }
			}
		});
	
	$('#folderDialog').popup('validation', {
	    callback : function() {
	      if (isCorrectForm()) {
			  window.document.AddAndUpdateFolderForm.submit();
		  }
	      return true;
	    }
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
    
    if (!id.equals(TRASHCAN_ID))
    	DisplayContactsHelper.displayContactsAdmin(resources.getIcon("yellowpages.contact"), yellowpagesScc,profile,currentTopic.getContactDetails(), (currentTopic.getNodeDetail().getChildrenNumber() > 0), resources.getIcon("yellowpages.contactDelete"), gef, request, session, resources, out);
    else
      	DisplayContactsHelper.displayContactsAdmin(resources.getIcon("yellowpages.contact"), yellowpagesScc,profile,currentTopic.getContactDetails(), (currentTopic.getNodeDetail().getChildrenNumber() > 0), resources.getIcon("yellowpages.delete"), gef, request, session, resources, out);
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