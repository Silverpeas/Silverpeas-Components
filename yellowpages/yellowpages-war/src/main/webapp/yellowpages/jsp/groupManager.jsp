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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.components.yellowpages.model.TopicDetail"%>
<%@ page import="org.silverpeas.components.yellowpages.model.YellowPagesGroupDetail"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="java.util.List" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.admin.user.model.Group" %>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp" %>

<% 
YellowPagesGroupDetail group 		= (YellowPagesGroupDetail) request.getAttribute("Group");
//noinspection unchecked
List<YellowPagesGroupDetail> groupPath 	= (List<YellowPagesGroupDetail>) request.getAttribute("GroupPath");

Collection<NodeDetail> path;
StringBuilder linkedPathString;

//Mise a jour de l'espace
String id = "0";
TopicDetail currentTopic = yellowpagesScc.getCurrentTopic();
if (currentTopic != null) {
	id = currentTopic.getNodePK().getId();
}
%>

<!DOCTYPE html>
<HTML lang="<%=resources.getLanguage()%>">
<HEAD>
<title></title>
<view:looknfeel/>
<script>
let contactWindow = window;

function closeWindows()
{
    if (!contactWindow.closed && contactWindow.name === "contactWindow")
        contactWindow.close();
}

function goToUser(id){
    closeWindows();
    const windowName = "contactWindow";
    const width = <%=( resources.getSetting("popupWidth") == null ) ? "600" : resources.getSetting("popupWidth")%>;
    const height = <%=( resources.getSetting("popupHeight") == null ) ? "480" : resources.getSetting("popupHeight")%>;
    const windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=yes";
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
    <BODY style="margin: 5px">
    
<%
    currentTopic = yellowpagesScc.getTopic(id);
    yellowpagesScc.setCurrentTopic(currentTopic);
    path = currentTopic.getPath();
    linkedPathString = new StringBuilder(displayPath(yellowpagesScc, path, true, 3));
    
    YellowPagesGroupDetail groupInPath;
    for (YellowPagesGroupDetail yellowPagesGroupDetail : groupPath) {
        groupInPath = yellowPagesGroupDetail;

        linkedPathString.append(" > ");
        linkedPathString.append("<a href=\"GoToGroup?Id=")
                .append(groupInPath.getId())
                .append("\">")
                .append(groupInPath.getName())
                .append("</a>");
    }
    
    yellowpagesScc.setPath(linkedPathString.toString());

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
<div class="center">
<%      
    if (!group.getSubGroups().isEmpty()) {
        ArrayPane arrayPane = gef.getArrayPane("topicsList", "GoToGroup?Id="+group.getId(), request, session);
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("&nbsp;");
        arrayColumn1.setSortable(false);
        arrayPane.addArrayColumn(resources.getString("Theme"));
        arrayPane.addArrayColumn(resources.getString("Nb"));
        arrayPane.addArrayColumn(resources.getString("GML.description"));

        YellowPagesGroupDetail subGroup;
        String childId;
        String childName;
        String childDescription;
        int nbContact;
        List<Group> subGroups = group.getSubGroups();
        for (Group value : subGroups) {
            subGroup = (YellowPagesGroupDetail) value;

            nbContact = subGroup.getTotalUsers();
            childId = subGroup.getId();
            childName = subGroup.getName();
            childDescription = subGroup.getDescription();

            IconPane folderPane = gef.getIconPane();
            Icon folder = folderPane.addIcon();
            folder.setProperties(resources.getIcon("yellowpages.group"), "", "GoToGroup?Id=" + childId);

            ArrayLine arrayLine = arrayPane.addArrayLine();
            arrayLine.addArrayCellIconPane(folderPane);
            arrayLine.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(childName), "GoToGroup?Id=" + childId);
            ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(nbContact);
            arrayCellText1.setCompareOn(nbContact);
            arrayLine.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(childDescription));

        } //fin du while
        out.println(arrayPane.print());
    } else {
    	out.println("");
    }
    
	out.println("<br>");
	
	Iterator<UserDetail> iterator = group.getUsers().iterator();
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
    
	String link;
    while (iterator.hasNext()) 
    {
        UserDetail user = iterator.next();
	
		link = "javaScript:goToUser('"+user.getId()+"');";
				
        ArrayLine ligne1 = arrayPane.addArrayLine();
	    if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon")))
	    {
	        IconPane iconPane = gef.getIconPane();
	        Icon carte = iconPane.addIcon();
	        carte.setProperties(resources.getIcon("yellowpages.user"), "", link);
        	ligne1.addArrayCellIconPane(iconPane);
	    }
        ArrayCellText arrayCellText1 = ligne1.addArrayCellText("<A HREF=\""+link+"\">"+WebEncodeHelper.javaStringToHtmlString(user.getLastName())+"</A>");
        ArrayCellText arrayCellText2 = ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(user.getFirstName()));
        ArrayCellText arrayCellText4;
        if (user.getEmailAddress()==null || "".equals(user.getEmailAddress()))
        {
            arrayCellText4 = ligne1.addArrayCellText("");
        }
        else
        {
            arrayCellText4 = ligne1.addArrayCellText("<a href=mailto:"+user.getEmailAddress()+">"+WebEncodeHelper.javaStringToHtmlString(user.getEmailAddress())+"</A>");
        }
        arrayCellText1.setCompareOn((user.getLastName() == null)?"":user.getLastName().toLowerCase());
        arrayCellText2.setCompareOn((user.getFirstName() == null)?"":user.getFirstName().toLowerCase());
        arrayCellText4.setCompareOn((user.getEmailAddress()==null)?"":
            WebEncodeHelper.javaStringToHtmlString(user.getEmailAddress().toLowerCase()));
    }   
    if (arrayPane.getColumnToSort() == 0)
    {
        arrayPane.setColumnToSort(indexLastNameColumn);
    }

    out.println(arrayPane.print());
    
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</div>
<FORM NAME="topicDetailForm" ACTION="topicManager.jsp" METHOD=POST >
<input type="hidden" name="Action"><input type="hidden" name="Id" value="<%=id%>">
</FORM>
</BODY>
</HTML>