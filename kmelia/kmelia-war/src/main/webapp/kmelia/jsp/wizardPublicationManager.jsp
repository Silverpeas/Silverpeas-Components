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

<%@ include file="checkKmelia.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%
String name				= "";
String description		= "";
String creatorName		= "";
String creationDate		= "";
String pubName			= "";
String nextAction 		= "";
String screenMessage 	= "";
  
UserCompletePublication userPubComplete = null;
UserDetail ownerDetail = null;

CompletePublication pubComplete = null;
PublicationDetail pubDetail = null;

//Récupération des paramètres
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

if (wizardRow == null)
	wizardRow = "1";

boolean isEnd = false;
if ("1".equals(wizardLast))
	isEnd = true;

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "GoToCurrentTopic", false);
Button nextButton = null;

//Action = Wizard (New), UpdateWizard
if (action.equals("UpdateWizard")) 
{
      //Recuperation des parametres de la publication
	  userPubComplete = kmeliaScc.getUserCompletePublication(id);
	  
 	  //kmeliaScc.setSessionPublication(userPubComplete);
      pubComplete 	= userPubComplete.getPublication();
      pubDetail 	= pubComplete.getPublicationDetail();
      pubName 		= pubDetail.getName(currentLang);
      ownerDetail 	= userPubComplete.getOwner();

      description 	= pubDetail.getDescription(currentLang);
      creationDate 	= resources.getOutputDate(pubDetail.getCreationDate());
      if (ownerDetail != null)
          creatorName = ownerDetail.getDisplayedName();
      else
          creatorName = resources.getString("UnknownAuthor");
 	  nextAction	= "UpdatePublication";
} 
else if (action.equals("Wizard")) 
{
      creationDate	= resources.getOutputDate(new Date());
      creatorName	= kmeliaScc.getUserDetail().getDisplayedName();
      currentTopic 	= kmeliaScc.getSessionTopic();
      if (currentTopic != null)
      {
    	  Collection pathColl = currentTopic.getPath();
    	  linkedPathString = displayPath(pathColl, true, 3);
    	  kmeliaScc.setSessionPath(linkedPathString);
    	  pathString = displayPath(pathColl, false, 3);
    	  kmeliaScc.setSessionPathString(pathString);
      }
	  nextAction = "AddPublication";
}
if (isEnd)
	nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "javascript:onClick=sendPublicationDataToRouter('"+nextAction+"');", false);
else
	nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "javascript:onClick=sendPublicationDataToRouter('"+nextAction+"');", false);

%>
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function publicationGoTo(id, action){
    document.pubForm.Action.value = "ViewPublication";
    document.pubForm.CheckPath.value = "1";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function sendOperation(operation) {
    document.pubForm.Action.value = operation;
    document.pubForm.submit();
}

function sendPublicationData(operation) {
    if (isCorrectForm()) {
         document.pubForm.Action.value = operation;
         document.pubForm.submit();
     }
}

function sendPublicationDataToRouter(func) {
	if (isCorrectForm()) {
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
</HEAD>
<BODY onLoad="init()">
<% 
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();
        Board board = gef.getBoard();
        Board boardHelp = gef.getBoard();
        
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(kmeliaScc.getSpaceLabel());
        browseBar.setComponentName(kmeliaScc.getComponentLabel(), "javascript:onClick=topicGoTo('0')");
        browseBar.setPath(linkedPathString);
		browseBar.setExtraInformation(pubName);
        
        out.println(window.printBefore());
        
        displayWizardOperations(wizardRow, id, kmeliaScc, gef, action, resources, out, kmaxMode);
        
        out.println(frame.printBefore());
        
		// cadre d'aide
	    out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+resources.getString("kmelia.HelpView")+"</td>");
		out.println("</tr></table>");
	    out.println(boardHelp.printAfter());
	    out.println("</BR>");
        
        out.println(board.printBefore());
%>
	<TABLE CELLPADDING="5" WIDTH="100%">
		<FORM Name="pubForm" Action="publicationManager.jsp" Method="POST" ENCTYPE="multipart/form-data">
  			<TR><TD class="txtlibform"><%=resources.getString("PubTitre")%></TD>
      			<TD><input type="text" name="Name" value="<%=Encode.javaStringToHtmlString(pubName)%>" size="60" maxlength="150">&nbsp;<IMG src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5" border="0"></TD></TR>
  			<TR><TD class="txtlibform"><%=resources.getString("PubDescription")%></TD>
      			<TD><TEXTAREA ROWS="4" COLS="70" name="Description"><%=Encode.javaStringToHtmlString(description)%></TEXTAREA></TD></TR>
  			<TR><TD class="txtlibform"><%=resources.getString("PubDateCreation")%></TD>
      			<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resources.getString("kmelia.By")%></span>&nbsp;<%=creatorName%></TD></TR>

			<% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {
  				String selectUserLab = resources.getString("kmelia.SelectValidator");
  				String link = "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');\">";
         		link += "<img src=\"" 
              			+ resources.getIcon("kmelia.user") 
              			+ "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" 
              			+ selectUserLab + "\" align=\"absmiddle\" title=\"" 
              			+ selectUserLab + "\"></a>";
  			%>
	  			<TR><TD class="txtlibform"><%=resources.getString("kmelia.Valideur")%></TD>
	      		<TD>
			      	<% if (kmeliaScc.isTargetValidationEnable()) { %> 
			      		<input type="text" name="Valideur" value="" size="60" readonly>
			      	<% } else { %>
			      		<textarea name="Valideur" value="" rows="4" cols="40" readonly></textarea>
			      	<% } %>
			      	<input type="hidden" name="ValideurId" value=""><%=link%>&nbsp;<img src="<%=resources.getIcon("kmelia.mandatory")%>" align="absmiddle" width="5" height="5" border="0"></TD></TR>
  			<% } %>

  			<TR><TD><input type="hidden" name="Position" value="View"><input type="hidden" name="Action" value="<%=action%>"><input type="hidden" name="PubId" value="<%=id%>"><input type="hidden" name="Importance" value="1"><input type="hidden" name="Status" value=""><input type="hidden" name="WizardRow" value="<%=wizardRow%>"></TD></TR>
  			<TR><TD colspan="2">( <img border="0" src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD></TR>
  		</FORM>
	</TABLE>
  <%
  		out.println(board.printAfter());
        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(nextButton);
        buttonPane.addButton(cancelButton);
        buttonPane.setHorizontalPosition();
        out.println("<BR/><center>"+buttonPane.print()+"</center>");
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<FORM name="toRouterForm">
	<input type="hidden" name="PubId" value="<%=id%>">
</FORM>
</BODY>
</HTML>