<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.components.yellowpages.control.YellowpagesSessionController" %>
<%@ page import="org.silverpeas.components.yellowpages.model.TopicDetail" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>
<%@ page import="org.silverpeas.core.node.model.NodePK" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine" %>
<%@ page import="org.silverpeas.core.node.model.NodeDetail" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkYellowpages.jsp" %>
<%!

private String afficheArbo(ArrayPane arrayPane, String idNode, YellowpagesSessionController yellowpagesScc, int nbEsp, Collection<NodePK> fathers, String currentTopicId) throws Exception {
	String resultat = "";
    String espace = "";
    int n = nbEsp;
    String checkText = "";
    
    for (int i=0; i<nbEsp; i++) {
    	espace += "&nbsp;";
    }
    n += 4;
    
    TopicDetail rootFolder = yellowpagesScc.getTopic(idNode);
    ArrayLine arrayLine = arrayPane.addArrayLine();
    String nodeName = rootFolder.getNodeDetail().getName();
    if (idNode.equals("0"))
        nodeName = yellowpagesScc.getComponentLabel();
    arrayLine.addArrayCellText(espace+nodeName);
    checkText = "<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\"";
    if (fathers != null) {
        for (NodePK fatherPK : fathers) {
        	if (fatherPK.getId().equals(rootFolder.getNodeDetail().getNodePK().getId())) {
            	checkText += " checked=\"checked\"";
        	}
        }
    }
    checkText += " />";
    
	arrayLine.addArrayCellText(checkText);
	resultat = arrayPane.print();

    Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
    if (subThemes != null) {
        for (NodeDetail theme : subThemes) {
            String idTheme = theme.getNodePK().getId();
      		if (!idTheme.equals("1") && !idTheme.equals("2") && !idTheme.startsWith("group_"))
            	resultat = afficheArbo(arrayPane, idTheme, yellowpagesScc, n, fathers, currentTopicId);
        }
   }

    return resultat;
}
%>

<% 
TopicDetail currentTopic = yellowpagesScc.getCurrentTopic();
String contactId = org.owasp.encoder.Encode.forUriComponent(request.getParameter("ContactId"));

String linkedPathString = yellowpagesScc.getPath();
%>
          
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
<!--
function B_VALIDER_ONCLICK() {
    f = "";
    if (String(document.AddTopicLink.topic.length) != "undefined") {
        for (i=0; i<document.AddTopicLink.topic.length; i++) {
            if (document.AddTopicLink.topic[i].checked)
                f += document.AddTopicLink.topic[i].value + ",";
        }
    } else {
        if (document.AddTopicLink.topic.checked)
            f += document.AddTopicLink.topic.value + ",";
    }
    if (f != "") {
        document.AddTopicLink.ListeTopics.value = f;
        document.AddTopicLink.submit();
    } else {
        jQuery.popup.error('<%=yellowpagesScc.getString("ErrorAddLink")%>');
    }
}
//-->
</script>
</head>
<body>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentId(componentId);
	browseBar.setPath(resources.getString("TopicLink"));

    out.println(window.printBefore());
%>
<view:frame>
	<form name="AddTopicLink" action="ContactSetFolders" method="post">
<input type="hidden" name="ContactId" value="<%=contactId%>"/>
<input type="hidden" name="ListeTopics"/>
<%
    ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
    arrayPane.setVisibleLineNumber(1000);
    ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(yellowpagesScc.getString("TopicTitle"));
    arrayColumnTopic.setSortable(false);
    ArrayColumn arrayColumnContact = arrayPane.addArrayColumn("");
    arrayColumnContact.setSortable(false);

    String resultat = afficheArbo(arrayPane, "0", yellowpagesScc, 0, yellowpagesScc.getContactFathers(contactId),currentTopic.getNodeDetail().getNodePK().getId());

    out.println(resultat);
%>
</form>
<br/>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "ContactUpdate?ContactId="+contactId, false);
    Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onclick=B_VALIDER_ONCLICK();", false);

    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
%>
</view:frame>
<%
    out.println(window.printAfter());
%>
</body>     
</html>