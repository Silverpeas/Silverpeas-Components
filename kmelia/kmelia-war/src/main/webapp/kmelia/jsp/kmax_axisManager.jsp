<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.util.*"%>

<%@ page import="com.stratelia.webactiv.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="kmax_axisReport.jsp" %>

<%
String id = "";
String name = "";
String description = "";
String creationDate = "";
String creatorName = "";
String childId = "";
String profile = "admin";

//R�cup�ration des param�tres
String action 		= request.getParameter("Action");
String translation 	= (String) request.getAttribute("Language");

//Icons
String axisAddSrc = m_context + "/util/icons/create-action/add-axis.png";
String publicationSrc = m_context + "/util/icons/publication.gif";
String mandatoryFieldSrc = m_context + "/util/icons/mandatoryField.gif";

//Mise a jour de l'espace
if (action == null) {
	action = "KmaxViewAxis";
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript">
<!--
var axisAddWindow = window;
var componentAddWindow = window;

function closeWindows() {
    if (!axisAddWindow.closed && axisAddWindow.name == "axisAddWindow")
        axisAddWindow.close();
    if (!componentAddWindow.closed && componentAddWindow == "componentAddWindow")
        componentAddWindow.close();
}

function axisAdd() {
    url = "kmax_addAxis.jsp";
    windowName = "axisAddWindow";
	larg = "600";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!axisAddWindow.closed && axisAddWindow.name == "axisAddWindow")
        axisAddWindow.close();
    axisAddWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function axisUpdate() {
	closeWindows();
    if (isCorrectAxisForm()) {
    	document.managerForm.action = "KmaxUpdateAxis";
        document.managerForm.AxisName.value = document.axisManagerForm.Name.value;
        document.managerForm.AxisDescription.value = document.axisManagerForm.Description.value;
        document.managerForm.AxisId.value = document.axisManagerForm.Id.value;
        <% if (I18NHelper.isI18N)  { %>
        	document.managerForm.I18NLanguage.value = document.axisManagerForm.I18NLanguage[document.axisManagerForm.I18NLanguage.selectedIndex].value;
        <% } %>
        if (document.getElementById('TranslationRemoveIt'))  { 
        	document.managerForm.TranslationRemoveIt.value = document.getElementById('TranslationRemoveIt').value;
		}
        document.managerForm.submit();
    }
}

function isCorrectAxisForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.axisManagerForm.Name.value);
	 var description = stripInitialWhitespace(document.axisManagerForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("AxisTitle")%>' <%=kmeliaScc.getString("MustContainsText")%>\n";
       errorNb++;
     }
	if (!isValidTextArea(document.axisManagerForm.Description)) {
          errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("AxisDescription")%>' <%=kmeliaScc.getString("ContainsTooLargeText")%> <%=DBUtil.getTextAreaLength()%> <%=kmeliaScc.getString("Characters")%>\n";
          errorNb++;
    }
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> 1 <%=kmeliaScc.getString("Error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> " + errorNb + " <%=kmeliaScc.getString("Errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function axisDelete() {
	closeWindows();
    if(window.confirm("<%=kmeliaScc.getString("ConfirmDeleteAxis")%> ?")){
		document.managerForm.action = "KmaxDeleteAxis";
		document.managerForm.AxisId.value = document.axisManagerForm.Id.value;
		document.managerForm.submit();
    }
}

function axisManage(id) {
	closeWindows();
	document.managerForm.action = "KmaxManageAxis";
	document.managerForm.Translation.value = "<%=translation%>";
    document.managerForm.AxisId.value = id;
    document.managerForm.submit();
}

function positionManage(selectObject) {
	closeWindows();
	document.managerForm.action = "KmaxManagePosition";
    selectObjectValues = selectObject.value.split("|");
    document.managerForm.PositionId.value = selectObjectValues[0];
    document.managerForm.AxisName.value = selectObjectValues[1];
		document.managerForm.Translation.value = "<%=translation%>";
    document.managerForm.submit();
}


function addPositionToPosition(axisId) {
    url = "kmax_addPositionToPosition.jsp?Action=KmaxView&AxisId="+axisId+"&Translation=<%=translation%>";
    windowName = "componentAddWindow";
	larg = "600";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!componentAddWindow.closed && componentAddWindow.name == "componentAddWindow")
        componentAddWindow.close();
    componentAddWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function addPositionToAxis(axisId) {
    url = "kmax_addPositionToAxis.jsp?AxisId="+axisId+"&Translation=<%=translation%>";
    windowName = "componentAddWindow";
	larg = "600";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!componentAddWindow.closed && componentAddWindow.name == "componentAddWindow")
        componentAddWindow.close();
	componentAddWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}

function positionUpdate() {
	closeWindows();
    if (isCorrectComponentForm()) {
      document.managerForm.action = "KmaxUpdatePosition";
      document.managerForm.PositionName.value = document.axisManagerForm.Name.value;
      document.managerForm.PositionDescription.value = document.axisManagerForm.Description.value;
      document.managerForm.PositionId.value = document.axisManagerForm.Id.value;
      <% if (I18NHelper.isI18N)  { %>
      	document.managerForm.I18NLanguage.value = document.axisManagerForm.I18NLanguage[document.axisManagerForm.I18NLanguage.selectedIndex].value;
      <% } %>
      if (document.getElementById('TranslationRemoveIt'))  { 
     	 document.managerForm.TranslationRemoveIt.value = document.getElementById('TranslationRemoveIt').value;
		} 
      document.managerForm.submit();
    }
}

function isCorrectComponentForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.axisManagerForm.Name.value);
	 var description = stripInitialWhitespace(document.axisManagerForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("ComponentTitle")%>' <%=kmeliaScc.getString("MustContainsText")%>\n";
       errorNb++;
     }
	if (!isValidTextArea(document.axisManagerForm.Description)) {
          errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("ComponentDescription")%>' <%=kmeliaScc.getString("ContainsTooLargeText")%> <%=DBUtil.getTextAreaLength()%> <%=kmeliaScc.getString("Characters")%>\n";
          errorNb++;
    }
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> 1 <%=kmeliaScc.getString("Error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> " + errorNb + " <%=kmeliaScc.getString("Errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function positionDelete() {
	closeWindows();
    if(window.confirm("<%=kmeliaScc.getString("ConfirmDeleteComponent")%> ?")){
    	document.managerForm.action = "KmaxDeletePosition";
        document.managerForm.PositionId.value = document.axisManagerForm.Id.value;
        document.managerForm.submit();
    }
}

function showTranslation(lang)
{
	showFieldTranslation('nodeNameValue', 'name_'+lang);
	showFieldTranslation('nodeDesc', 'desc_'+lang);
}

function removeTranslation()
{
	axisUpdate();
}
-->
</script>
</head>
<body id="<%=componentId %>" class="kmaxManager" onunload="closeWindows()">
	<%
if (action.equals("KmaxViewAxis") || action.equals("KmaxManageAxis") || action.equals("KmaxManagePosition")) {
	  Window window = gef.getWindow();

	  BrowseBar browseBar = window.getBrowseBar();
	  browseBar.setDomainName(kmeliaScc.getSpaceLabel());
	  browseBar.setComponentName(kmeliaScc.getComponentLabel(), "KmaxMain");
	  browseBar.setExtraInformation(kmeliaScc.getString("AdminExplaination"));
	  browseBar.setI18N("KmaxAxisManager?AxisId="+id, translation);

	  OperationPane operationPane = window.getOperationPane();
	  if (profile.equals("admin")) {
		  operationPane.addOperationOfCreation(axisAddSrc, kmeliaScc.getString("AddAxis"), "javascript:onClick=axisAdd()");
		  operationPane.addLine();
		  operationPane.addOperation(resources.getIcon("kmelia.modelUsed"), resources.getString("kmelia.ModelUsed"), "ModelUsed");
	  }

	  TabbedPane tabbedPane = gef.getTabbedPane();
	  tabbedPane.addTab(kmeliaScc.getString("Consultation"), "KmaxMain", false);
	  tabbedPane.addTab(kmeliaScc.getString("Management"), "KmaxAxisManager", true);

	  Frame frame = gef.getFrame();

	  out.println(window.printBefore());
	  
	  out.println(tabbedPane.print());
%>
	  <view:areaOfOperationOfCreation/>
	  <view:frame>
<%
		out.println(displayAxisToAdmins(kmeliaScc, gef, translation));

	  if (action.equals("KmaxManageAxis")) {
	      String axisId = (String) request.getParameter("AxisId");
		    NodeDetail nodeDetail = kmeliaScc.getNodeHeader(axisId);
	      out.println(codeJSForTranslation(nodeDetail));
	      out.println(displayAxisManageView(kmeliaScc, gef, axisId, mandatoryFieldSrc, resources, translation));

	  } else if (action.equals("KmaxManagePosition")) {
	      String positionPath = (String) request.getParameter("PositionId");
		  String positionId = positionPath.substring(positionPath.lastIndexOf("/")+1, positionPath.length());
		    NodeDetail nodeDetail = kmeliaScc.getNodeHeader(positionId);
	      out.println(codeJSForTranslation(nodeDetail));
	      //get path to selected component
	      Collection path = kmeliaScc.getPath(positionId);
	      String pathStr = displayPath(path, false, 3, translation);
	      out.println(displayComponentManageView(kmeliaScc, gef, positionId, pathStr, mandatoryFieldSrc, resources, translation));
	  }
%>
	</view:frame>
<%
	  out.println(window.printAfter());
}
%>

<%!
String codeJSForTranslation(NodeDetail nodeDetail)
{
	String lang = "";
	String result = "";
	Iterator codes = nodeDetail.getTranslations().keySet().iterator();
	result += "<script language=\"javascript\">\n";
	while (codes.hasNext())
	{
		lang = (String) codes.next();
		result += "var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(nodeDetail.getName(lang))+"\";\n";
		result += "var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(nodeDetail.getDescription(lang))+"\";\n";
	}
	result += "\n</script>";
	return result;
}
%>

<form name="managerForm" method="post" action="">
	<input type="hidden" name="AxisId"/>
	<input type="hidden" name="AxisName"/>
	<input type="hidden" name="AxisDescription"/>
	<input type="hidden" name="PositionId"/>
	<input type="hidden" name="PositionName"/>
	<input type="hidden" name="PositionDescription"/>
	<input type="hidden" name="NextAction"/>
	<input type="hidden" name="Translation"/>
	<input type="hidden" name="I18NLanguage"/>
	<input type="hidden" name="TranslationRemoveIt"/>
</form>
</body>
</html>