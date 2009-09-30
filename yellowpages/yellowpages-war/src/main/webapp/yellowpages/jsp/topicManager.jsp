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
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="contactsList.jsp.inc" %>

<% 
String rootId = new Integer(ROOT_TOPIC).toString();

String action = "";
String contactId = "";
String id = "";
String name = "";
String description = "";
String modelId = "";
Collection path = null;
String fatherId = "";
String childId = "";
Collection subTopicList = null;
Collection contactList = null;
String linkedPathString = "";
String pathString = "";
String profile = "";
String topicName = "";
boolean updateFailed = false;

//Récupération des paramètres
action = (String) request.getParameter("Action");
id = (String) request.getParameter("Id");
contactId = (String) request.getParameter("ContactId");
childId = (String) request.getParameter("ChildId");
profile = (String) request.getParameter("Profile");

//Mise a jour de l'espace
TopicDetail currentTopic = null;
if (!StringUtil.isDefined(action) || id == null) {
    action = "Search";
    currentTopic = yellowpagesScc.getCurrentTopic();
    if (currentTopic != null) {
		id = currentTopic.getNodePK().getId();
    } else {
		id=rootId;
    }
}
%>

<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<SCRIPT LANGUAGE="JAVASCRIPT" SRC="<%=m_context%>/util/javaScript/animation.js"></SCRIPT>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<script language="JavaScript1.2">
var topicAddWindow = window;
var topicUpdateWindow = window;
var contactWindow = window;
var userAddWindow = window;

function topicGoTo(id) 
{
	closeWindows();	
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function simpleTopicGoToSelected() {
    var id = document.topicDetailForm.selectSimpleTopic.options[document.topicDetailForm.selectSimpleTopic.selectedIndex].value;
    topicGoTo(id);
}

<% if (profile.equals("admin")) { %>
function topicAdd(fatherId) {
    path = document.topicDetailForm.Path.value;
    url = "addTopic.jsp?Id="+fatherId+"&Path="+breakSpace(path)+"&Action=View";
    windowName = "topicAddWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
    if (!topicAddWindow.closed && topicAddWindow.name == "topicAddWindow")
        topicAddWindow.close();
    topicAddWindow = SP_openWindow( url, windowName, '750' , '400' , windowParams); 
}

function topicUpdate(id) {
    path = document.topicDetailForm.Path.value;
    document.topicDetailForm.ChildId.value = id;
    url = "updateTopic.jsp?ChildId="+id+"&Path="+breakSpace(path);
    windowName = "topicUpdateWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
    if (!topicUpdateWindow.closed && topicUpdateWindow.name == "topicUpdateWindow")
        topicUpdateWindow.close();

    topicUpdateWindow = SP_openWindow( url, windowName, '750' , '400' , windowParams);
}

function topicDeleteConfirm(childId, name) {
    if(window.confirm("<%=yellowpagesScc.getString("ConfirmDeleteTopic")%> '" + name + "' ?")){
          document.topicDetailForm.Action.value = "Delete";
          document.topicDetailForm.ChildId.value = childId;
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
   		location.href = "RemoveGroup?Id="+childId;
    }
}
<% } %>

function closeWindows()
{
		if (!topicAddWindow.closed && topicAddWindow.name == "topicAddWindow")
        topicAddWindow.close();
    if (!topicUpdateWindow.closed && topicUpdateWindow.name == "topicUpdateWindow")
        topicUpdateWindow.close();
    if (!userAddWindow.closed && userAddWindow.name == "userAddWindow")
        userAddWindow.close();
    if (!contactWindow.closed && contactWindow.name == "contactWindow")
        contactWindow.close(); 
}

function contactAdd(){
    closeWindows();
    windowName = "contactWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
    contactWindow = SP_openWindow( "", windowName, '600' , '400' , windowParams);
    document.contactForm.Action.value = "New";
    document.contactForm.submit();
}
function userAdd(){
    closeWindows();
    url = "addUser.jsp";
    windowName = "userAddWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    userAddWindow = SP_openWindow( url, windowName, '600' , '400' , windowParams);
}

function addGroup()
{
    closeWindows();
    url = "ToChooseGroup";
    windowName = "userAddWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
    userAddWindow = SP_openWindow(url, windowName, '750' , '600' , windowParams);
}

function contactGoTo(id){
    closeWindows();
    windowName = "contactWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,height=400,width=600,alwaysRaised,scrollbars=yes";
    if (!contactWindow.closed && contactWindow.name == "contactWindow")
        contactWindow.close();
    contactWindow = SP_openWindow( "", windowName, '600' , '400' , windowParams);
    document.contactForm.Action.value = "View";
    document.contactForm.ContactId.value = id;
    document.contactForm.submit();
}

function contactDeleteConfirm(id) {
    if(window.confirm("<%=yellowpagesScc.getString("ConfirmDeleteContact")%> ?")){
          document.contactDeleteForm.action = "DeleteContact";
          document.contactDeleteForm.ContactId.value = id;
          document.contactDeleteForm.submit();
    }
}

function errorUpdate() {
	alert("<%=resources.getString("ContactAddToTopic3")%>");
}

function consult() {
    closeWindows();
	location.href = "Main.jsp";
}

</script>
</HEAD>
<%
if (action.equals("Add")) {
    name = (String) request.getParameter("Name");
    description = (String) request.getParameter("Description");
    NodePK newNodePK = addTopic(yellowpagesScc, name, description, out);
    if (newNodePK.getId().equals("-1")) {
		updateFailed = true;    
    } 
    action = "Search";
} else if (action.equals("Update")) {
    childId = (String) request.getParameter("ChildId");
    topicName = (String) request.getParameter("Name");
    description = (String) request.getParameter("Description");
    modelId = (String) request.getParameter("ModelId");
    NodePK updatedNodePK = updateTopic(yellowpagesScc, childId, topicName, description, modelId, out);
    if (updatedNodePK.getId().equals("-1")) {
		updateFailed = true;
} 
    action = "Search";
} else if (action.equals("Delete")) {
    childId = (String) request.getParameter("ChildId");
    removeTopic(yellowpagesScc, childId, out);
    action = "Search";
}


if (action.equals("Search")) {
%>
    <BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
    <FORM NAME="topicDetailForm" action="topicManager.jsp" METHOD=POST >
<%
    currentTopic = yellowpagesScc.getTopic(id);
    yellowpagesScc.setCurrentTopic(currentTopic);
    name = currentTopic.getNodeDetail().getName();
    path = currentTopic.getPath();
    pathString = displayPath(yellowpagesScc,path, false, 3);
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
				operationPane.addOperation(resources.getIcon("yellowpages.folderAdd"), resources.getString("CreerSousTheme"), "javascript:onClick=topicAdd('"+id+"')");
				operationPane.addLine();
				operationPane.addOperation(resources.getIcon("yellowpages.groupAdd"), resources.getString("GroupAdd"), "javascript:onClick=addGroup()");
				operationPane.addLine();
			}
			else
			{
				operationPane.addOperation(resources.getIcon("yellowpages.basketDelete"), resources.getString("yellowpages.DeleteBasketContent"), "javascript:onClick=deleteBasketContent()");
			}
    }
    
	// Si nous sommes dans la corbeille, alors nous ne pouvons créer un contact dedans !!
	if (!id.equals(TRASHCAN_ID)){
		operationPane.addOperation(resources.getIcon("yellowpages.contactAdd"), yellowpagesScc.getString("ContactCreer"), "javascript:onClick=contactAdd()");
		operationPane.addLine();
		operationPane.addOperation(resources.getIcon("yellowpages.basket"), yellowpagesScc.getString("ContactBasket"), "javascript:onClick=topicGoTo('1')");
    if (profile.equals("admin")) 
    {
			operationPane.addLine();
			operationPane.addOperation(resources.getIcon("yellowpages.modelUsed"), resources.getString("yellowpages.ModelUsed"), "ModelUsed");
    }
  }

    //Onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(yellowpagesScc.getString("Consultation"),"javascript:consult();",false);
    tabbedPane.addTab(resources.getString("GML.management"),"#",true);

    Frame frame = gef.getFrame();
          
    out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>
<!-- AFFICHAGE HEADER -->
<CENTER>

<%
    if (!id.equals(TRASHCAN_ID) && !id.equals("2")) {
        if (profile.equals("admin"))
            displayTopicsToAdmin(yellowpagesScc, id, "<br>", gef, pageContext, request, session, resources, out);
        else
            displayTopicsToUsers(yellowpagesScc, id, "<br>", profile, gef, pageContext, request, session, resources, out);
    }

	out.println("<br>");
    
    if (!id.equals(TRASHCAN_ID))
    	displayContactsAdmin(resources.getIcon("yellowpages.contact"), yellowpagesScc,profile,currentTopic.getContactDetails(), (currentTopic.getNodeDetail().getChildrenNumber() > 0), resources.getIcon("yellowpages.contactDelete"), gef, request, session, resources, out);
    else
      	displayContactsAdmin(resources.getIcon("yellowpages.contact"), yellowpagesScc,profile,currentTopic.getContactDetails(), (currentTopic.getNodeDetail().getChildrenNumber() > 0), resources.getIcon("yellowpages.delete"), gef, request, session, resources, out);
                  
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>

</CENTER>

<input type="hidden" name="Action"><input type="hidden" name="Id" value="<%=id%>">
<input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(pathString)%>"><input type="hidden" name="ChildId">
<input type="hidden" name="Name"><input type="hidden" name="Description"><input type="hidden" name="ModelId">
</FORM>

<FORM NAME="contactForm" ACTION="contactManager.jsp" target="contactWindow" METHOD="POST">
<input type="hidden" name="Action">
<input type="hidden" name="ContactId">
</FORM>

<FORM NAME="contactDeleteForm" ACTION="topicManager.jsp" METHOD="POST">
<input type="hidden" name="Action">
<input type="hidden" name="ContactId">
<input type="hidden" name="Id" value="<%=id%>">
</FORM>

</BODY>
<%
	if (updateFailed)
	{
		%>
		<SCRIPT LANGUAGE="JavaScript">
		<!--
			errorUpdate();
		//-->
		</SCRIPT>
		<%
	}
} //End if action = search %>
</HTML>