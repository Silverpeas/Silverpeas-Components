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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.yellowpages.model.TopicDetail"%>
<%@ page import="com.stratelia.webactiv.yellowpages.model.GroupDetail"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.util.exception.*"%>

<%@ page import="com.stratelia.webactiv.beans.admin.*"%>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>

<% 
GroupDetail group 		= (GroupDetail) request.getAttribute("Group");
List		groupPath 	= (List) request.getAttribute("GroupPath");

String name = "";
String description = "";
Collection path = null;
String linkedPathString = "";
String pathString = "";

//Mise a jour de l'espace
String id = "0";
TopicDetail currentTopic = yellowpagesScc.getCurrentTopic();
if (currentTopic != null) {
	id = currentTopic.getNodePK().getId();
}
%>

<HTML>
<HEAD>
<view:looknfeel/>
<SCRIPT LANGUAGE="JAVASCRIPT" SRC="<%=m_context%>/util/javaScript/animation.js"></SCRIPT>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<script language="JavaScript1.2">
var contactWindow = window;

function closeWindows()
{
    if (!contactWindow.closed && contactWindow.name == "contactWindow")
        contactWindow.close();
}

function goToUser(id){
    closeWindows();
    windowName = "contactWindow";
    width = <%=( resources.getSetting("popupWidth") == null ) ? "600" : resources.getSetting("popupWidth")%>;
    height = <%=( resources.getSetting("popupHeight") == null ) ? "480" : resources.getSetting("popupHeight")%>;
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
    contactWindow = SP_openWindow("ViewUserFull?Id="+id, windowName, width, height, windowParams);
}

function consult() {
    closeWindows();
	location.href = "Main.jsp";
}

function topicGoTo(id) 
{
	closeWindows();	
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}
</script>
</HEAD>
    <BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
    
<%
    currentTopic = yellowpagesScc.getTopic(id);
    yellowpagesScc.setCurrentTopic(currentTopic);
    name = currentTopic.getNodeDetail().getName();
    path = currentTopic.getPath();
    linkedPathString = displayPath(yellowpagesScc, path, true, 3);
    
    GroupDetail groupInPath = null;
    for (int g=0; g<groupPath.size(); g++)
    {
    	groupInPath = (GroupDetail) groupPath.get(g);
    	
    	linkedPathString += " > ";
    	linkedPathString += "<a href=\"GoToGroup?Id="+groupInPath.getId()+"\">"+groupInPath.getName()+"</a>";
    }
    
    yellowpagesScc.setPath(linkedPathString);

    Window window = gef.getWindow();
    BrowseBar browseBar=window.getBrowseBar();
	
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resources.getString("GML.management")+" > "+linkedPathString);

    //Onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resources.getString("Consultation"),"javascript:consult();",false);
    tabbedPane.addTab(resources.getString("GML.management"),"#",true);

    Frame frame = gef.getFrame();
          
    out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>
<CENTER>
<%      
    if (group.getSubGroups().size() > 0 ) {
        ArrayPane arrayPane = gef.getArrayPane("topicsList", "GoToGroup?Id="+group.getId(), request, session);
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("&nbsp;");
        arrayColumn1.setSortable(false);
        arrayPane.addArrayColumn(resources.getString("Theme"));
        arrayPane.addArrayColumn(resources.getString("Nb"));
        arrayPane.addArrayColumn(resources.getString("GML.description"));

        GroupDetail subGroup = null;
        String childId;
        String childName;
        String childDescription;
        int nbContact;
        List subGroups = group.getSubGroups();
        Iterator iteratorN = subGroups.iterator();
        while (iteratorN.hasNext()) {
        	subGroup = (GroupDetail) iteratorN.next();
        	
            nbContact = subGroup.getTotalUsers();
            childId = subGroup.getId();
            childName = subGroup.getName();
            childDescription = subGroup.getDescription();
            
			IconPane folderPane = gef.getIconPane();
			Icon folder = folderPane.addIcon();
			folder.setProperties(resources.getIcon("yellowpages.group"), "" , "GoToGroup?Id="+childId);
            
            ArrayLine arrayLine = arrayPane.addArrayLine();
			arrayLine.addArrayCellIconPane(folderPane);
            arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(childName), "GoToGroup?Id="+childId);
            ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(nbContact);
            arrayCellText1.setCompareOn(new Integer(nbContact));
        	arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(childDescription));
            
        } //fin du while
        out.println(arrayPane.print());
    } else {
    	out.println("");
    }
    
	out.println("<br>");
	
	Iterator iterator = group.getUsers().iterator();
    int indexLastNameColumn = 1;

    ArrayPane arrayPane = gef.getArrayPane("tableau1", "GoToGroup?Id="+group.getId(), request, session);
    if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon")))
    {
	    ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
		arrayColumn0.setSortable(false);
	    indexLastNameColumn = 2;
	}
    arrayPane.addArrayColumn(resources.getString("GML.name"));
    arrayPane.addArrayColumn(resources.getString("GML.surname"));
    arrayPane.addArrayColumn(resources.getString("GML.eMail"));
    
	String link = null;
    while (iterator.hasNext()) 
    {
        UserDetail user =  (UserDetail) iterator.next();
	
		link = "javaScript:goToUser('"+user.getId()+"');";
				
        ArrayLine ligne1 = arrayPane.addArrayLine();
	    if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon")))
	    {
	        IconPane iconPane = gef.getIconPane();
	        Icon carte = iconPane.addIcon();
	        carte.setProperties(resources.getIcon("yellowpages.user"), "", link);
        	ligne1.addArrayCellIconPane(iconPane);
	    }
        ArrayCellText arrayCellText1 = ligne1.addArrayCellText("<A HREF=\""+link+"\">"+EncodeHelper.javaStringToHtmlString(user.getLastName())+"</A>");
        ArrayCellText arrayCellText2 = ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(user.getFirstName()));
        ArrayCellText arrayCellText4 = null;
        if (user.geteMail()==null || "".equals(user.geteMail()))
        {
            arrayCellText4 = ligne1.addArrayCellText("");
        }
        else
        {
            arrayCellText4 = ligne1.addArrayCellText("<a href=mailto:"+user.geteMail()+">"+EncodeHelper.javaStringToHtmlString(user.geteMail())+"</A>");
        }
        arrayCellText1.setCompareOn((String) ((user.getLastName() == null)?"":user.getLastName().toLowerCase()));
        arrayCellText2.setCompareOn((String) ((user.getFirstName() == null)?"":user.getFirstName().toLowerCase()));
        arrayCellText4.setCompareOn((String) ((user.geteMail()==null)?"":EncodeHelper.javaStringToHtmlString(user.geteMail().toLowerCase())));
    }   
    if (arrayPane.getColumnToSort() == 0)
    {
        arrayPane.setColumnToSort(indexLastNameColumn);
    }

    out.println(arrayPane.print());
    
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</CENTER>
<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST >
<input type="hidden" name="Action"><input type="hidden" name="Id" value="<%=id%>">
</FORM>
</BODY>
</HTML>