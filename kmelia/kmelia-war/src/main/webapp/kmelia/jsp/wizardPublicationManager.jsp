<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="topicReport.jsp.inc" %>

<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
String name				= "";
String description		= "";
String pubName			= "";
String nextAction 		= "";
String screenMessage 	= "";
  
KmeliaPublication kmeliaPublication = null;
UserDetail ownerDetail = null;

CompletePublication pubComplete = null;
PublicationDetail pubDetail = null;

//R�cup�ration des param�tres
String profile 		= (String) request.getAttribute("Profile");
String action 		= (String) request.getAttribute("Action");
String id 			= (String) request.getAttribute("PubId");
String wizardLast	= (String) request.getAttribute("WizardLast");
String wizardRow	= (String) request.getAttribute("WizardRow");
String currentLang 	= (String) request.getAttribute("Language");

SilverTrace.info("kmelia","JSPdesign", "root.MSG_GEN_PARAM_VALUE","ACTION pubManager = "+action);

TopicDetail currentTopic = null;

String linkedPathString = "";
String pathString = "";

if (wizardRow == null) {
	wizardRow = "1";
}

boolean isEnd = false;
if ("1".equals(wizardLast)) {
	isEnd = true;
}

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "GoToCurrentTopic", false);
Button nextButton = null;

//Action = Wizard (New), UpdateWizard
if (action.equals("UpdateWizard")) 
{
      //Recuperation des parametres de la publication
	  kmeliaPublication = kmeliaScc.getPublication(id);
	  
 	  //kmeliaScc.setSessionPublication(kmeliaPublication);
      pubComplete 	= kmeliaPublication.getCompleteDetail();
      pubDetail 	= pubComplete.getPublicationDetail();
      pubName 		= pubDetail.getName(currentLang);
      ownerDetail 	= kmeliaPublication.getCreator();

      description 	= pubDetail.getDescription(currentLang);
 	  nextAction	= "UpdatePublication";
} 
else if (action.equals("Wizard")) 
{
      currentTopic 	= kmeliaScc.getSessionTopic();
      if (currentTopic != null) {
    	  Collection pathColl = currentTopic.getPath();
    	  linkedPathString = displayPath(pathColl, true, 3);
    	  kmeliaScc.setSessionPath(linkedPathString);
    	  pathString = displayPath(pathColl, false, 3);
    	  kmeliaScc.setSessionPathString(pathString);
      }
	  nextAction = "AddPublication";
}
if (isEnd) {
	nextButton = gef.getFormButton(resources.getString("kmelia.End"), "javascript:onClick=sendPublicationDataToRouter('"+nextAction+"');", false);
} else {
	nextButton = gef.getFormButton(resources.getString("GML.next"), "javascript:onClick=sendPublicationDataToRouter('"+nextAction+"');", false);
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<view:looknfeel/>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function sendPublicationDataToRouter(func) {
	if (isCorrectForm()) {
		<% if (!kmeliaScc.isKmaxMode) { %>
			<view:pdcPositions setIn="document.pubForm.Positions.value"/>
		<% } %>
    	document.pubForm.action = func;
        document.pubForm.submit();
    }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.pubForm.Name.value);

     if (isWhitespace(title)) {
           errorMsg+="  - '<%=resources.getString("PubTitre")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }

     <% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) { %>
  		var validatorId = stripInitialWhitespace(document.pubForm.ValideurId.value);
  		if (isWhitespace(validatorId)) {
     		errorMsg+="  - '<%=resources.getString("kmelia.Valideur")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
        	errorNb++;
	    }
  	 <% } %>
  	
  	<% if (!kmeliaScc.isKmaxMode) { %>
  		<view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
  	<% } %>

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

function init() {
	document.pubForm.Name.focus();
}
</script>
</head>
<body class="publicationManager" onload="init()">
  <%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(kmeliaScc.getSpaceLabel());
    browseBar.setComponentName(kmeliaScc.getComponentLabel(), "javascript:onClick=topicGoTo('0')");
    browseBar.setPath(linkedPathString);
    browseBar.setExtraInformation(pubName);
    out.println(window.printBefore());
    KmeliaDisplayHelper.displayWizardOperations(wizardRow, id, kmeliaScc, gef, action, resources,
        out, kmaxMode);
    out.println(frame.printBefore());
  %>
	<!-- cadre d'aide -->
	<div class="inlineMessage">
		<img border="0" src="<%=resources.getIcon("kmelia.info") %>"/>
		<%=resources.getString("kmelia.HelpView") %>
	</div>
	<br clear="all"/>
	
	<div id="header">
		<form name="pubForm" action="publicationManager.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
			<input type="hidden" name="Positions"/>
			<input type="hidden" name="Position" value="View"/>
			<input type="hidden" name="Action" value="<%=action%>"/>
			<input type="hidden" name="PubId" value="<%=id%>"/>
			<input type="hidden" name="Importance" value="1"/>
			<input type="hidden" name="Status" value=""/>
			<input type="hidden" name="WizardRow" value="<%=wizardRow%>"/>
			
			<fieldset id="pubInfo" class="skinFieldset">
				<legend><%=resources.getString("kmelia.header.fieldset.main") %></legend>
					<div class="fields">
						<div class="field" id="pubNameArea">
						<label for="pubName" class="txtlibform"><%=resources.getString("PubTitre")%></label>
						<div class="champs">
							<input type="text" name="Name" id="pubName" value="<%=EncodeHelper.javaStringToHtmlString(pubName)%>" size="68" maxlength="150" />&nbsp;<img src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5" border="0"/>
						</div>
						</div>
					
						<div class="field" id="descriptionArea">
						<label for="pubDesc" class="txtlibform"><%=resources.getString("PubDescription")%></label>
						<div class="champs">
							<textarea rows="4" cols="65" name="Description" id="pubDesc"><%=EncodeHelper.javaStringToHtmlString(description)%></textarea>
						</div>
						
						<% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {
					           String selectUserLab = resources.getString("kmelia.SelectValidator");
					           String link = "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');\">";
					           link += "<img src=\""
					               + resources.getIcon("kmelia.user")
					               + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
					               + selectUserLab + "\" align=\"absmiddle\" title=\""
					               + selectUserLab + "\"></a>";
					    %>
					    <div class="field" id="validatorArea">
							<label for="Valideur" class="txtlibform"><%=resources.getString("kmelia.Valideur")%></label>
							<div class="champs">
								<% if (kmeliaScc.isTargetValidationEnable()) {%>
		          					<input type="text" name="Valideur" id="Valideur" size="60" readonly="readonly"/>
		          				<% } else {%>
		          					<textarea name="Valideur" id="Valideur" rows="4" cols="40" readonly="readonly"></textarea>
		          				<% }%>
		          				<input type="hidden" name="ValideurId" id="ValideurId" value=""/><%=link%>&nbsp;<img src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5" border="0"/>
							</div>
						</div>
						<% } %>
				</div>
				
				</div>
			</fieldset>
			
			<% if (!kmeliaScc.isKmaxMode) { %>
				<view:pdcNewContentClassification componentId="<%= componentId %>" nodeId="<%= kmeliaScc.getSessionTopic().getNodePK().getId() %>"/>
			<% } %>
			
			<div class="legend">
				<img src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%>
			</div>
		</form>
	</div>
	
  <%
        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(nextButton);
        buttonPane.addButton(cancelButton);
        buttonPane.setHorizontalPosition();
        out.println("<br/><center>"+buttonPane.print()+"</center>");
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<form name="toRouterForm">
	<input type="hidden" name="PubId" value="<%=id%>"/>
</form>
</body>
</html>