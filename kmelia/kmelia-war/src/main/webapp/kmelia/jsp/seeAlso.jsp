<%@page import="org.silverpeas.kmelia.jstl.KmeliaDisplayHelper"%>
<%@page import="org.silverpeas.search.SearchEngineFactory"%>
<%@ page import="org.silverpeas.search.SearchEngine" %>
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="publicationsList.jsp.inc" %>

<%!
 //Icons
String folderSrc;
String publicationSrc;
String seeAlsoSrc;
String seeAlsoDeleteSrc;
String hLineSrc;

void displayLinkViewSelection(int selectedId, KmeliaSessionController kmeliaScc, JspWriter out) throws IOException {
      out.println("<script language=\"Javascript\">");
      out.println("function goTo(i) {");
      out.println("window.location.replace(\"SeeAlso?Action=\"+document.linksViewForm.linkView[i].value);");
      out.println("}");
      out.println("</script>");

	  out.println("<br>");
	  out.println("<table width=\"100%\" align=center border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
	  out.println("<Form name=\"linksViewForm\">");
	  out.println("<tr><td align=right valign=bottom>");
      out.println("<select name=\"linkView\" onChange=\"javascript:goTo(this.selectedIndex);\">");
      
      String choice1 ="";
      String choice2="";
      String choice4="";
      String defaultchoice="";
        if (selectedId == 1) {
          choice1 = "selected";
        } else if (selectedId == 2) {
          choice2="selected";
        } else if (selectedId == 4){
          choice4="selected";
        }else {
          defaultchoice="selected";
        }      
        out.println("<option value=\"LinkAuthorView\" "+choice1 + ">"+kmeliaScc.getString("PubReferenceeParAuteur")+"</option>");
        out.println("<option value=\"SameSubjectView\" "+choice2 + ">"+kmeliaScc.getString("PubDeMemeSujet")+"</option>");
        out.println("<option value=\"SameTopicView\" "+defaultchoice + ">"+kmeliaScc.getString("PubDeMemeTheme")+"</option>");
        out.println("<option value=\"PubReferencedBy\" "+choice4 + ">"+kmeliaScc.getString("PubReferencedBy")+"</option>");
        
      out.println("</select>");
	  out.println("</td><td width=80>&nbsp;</td></tr>");
      out.println("</form>");
	  out.println("</table>");
}

%>

<%
//Retrieve parameters
String profile 		= (String) request.getAttribute("Profile");
String action 		= (String) request.getAttribute("Action");
String wizard		= (String) request.getAttribute("Wizard");
String	currentLang = (String) request.getAttribute("Language");

//Icons
publicationSrc		= m_context + "/util/icons/publication.gif";
seeAlsoSrc			= "icons/linkedAdd.gif";
seeAlsoDeleteSrc	= "icons/linkedDel.gif";

//Vrai si le user connecte est le createur de cette publication ou si il est admin
boolean isOwner = false;

String linkedPathString = kmeliaScc.getSessionPath();

KmeliaPublication kmeliaPublication = kmeliaScc.getSessionPublication();
CompletePublication 	pubComplete 	= kmeliaPublication.getCompleteDetail();
UserDetail 				ownerDetail 	= kmeliaPublication.getCreator();
String					id				= pubComplete.getPublicationDetail().getPK().getId();
String 					pubName 		= pubComplete.getPublicationDetail().getName(currentLang);
String					instanceId		= pubComplete.getPublicationDetail().getPK().getInstanceId();

if (profile.equals("admin") || profile.equals("publisher") || profile.equals("supervisor") || (ownerDetail != null && kmeliaScc.getUserDetail().getId().equals(ownerDetail.getId()) && profile.equals("writer")))
	isOwner = true;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function seeAlsoDeleteConfirm() {
	if (document.seeAlsoForm.PubIds != null && $("input[type='checkbox']").is(":checked")){
	    if(window.confirm("<%=kmeliaScc.getString("kmelia.ConfirmDeleteSeeAlso")%>")){
	    	document.seeAlsoForm.PubId.value = "<%=id%>";
			document.seeAlsoForm.Action = "<%=routerUrl%>DeleteSeeAlso";
	       	document.seeAlsoForm.submit();
		}
	}
}

function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function publicationGoTo(id, action){
    document.pubForm.action = "<%=URLManager.getApplicationURL() + URLManager.getURL("kmelia", "useless", instanceId)%>ViewPublication";
    document.pubForm.CheckPath.value = "1";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
    if (window.publicVersionsWindow != null)
    	window.publicVersionsWindow.close();
}
</script>
</head>
<body onunload="closeWindows()">
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();
        
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "javascript:onClick=topicGoTo('0')");
        browseBar.setPath(linkedPathString);
		browseBar.setExtraInformation(pubName);
		browseBar.setI18N("SeeAlso", kmeliaScc.getCurrentLanguage());

        OperationPane operationPane = window.getOperationPane();

        if (isOwner && action.equals("LinkAuthorView")) {
            operationPane.addOperation(seeAlsoSrc, resources.getString("AddLinkPub"), "javaScript:goToOperationInAnotherWindow('publicationLinksManager.jsp', '"+id+"', 'Search')");
            operationPane.addLine();
            operationPane.addOperation(seeAlsoDeleteSrc, resources.getString("kmelia.DeleteLinkPub"), "javaScript:seeAlsoDeleteConfirm();");
        }
        
        out.println(window.printBefore());

        if (isOwner) {
            KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, action, resources, out);
        } else {
            KmeliaDisplayHelper.displayUserOperations(id, kmeliaScc, gef, action, resources, out);
        }

        out.println(frame.printBefore());

        if ("finish".equals(wizard)) {
          %>
          	<div class="inlineMessage">
				<img border="0" src="<%=resources.getIcon("kmelia.info") %>"/>
				<%=resources.getString("kmelia.HelpSeeAlso") %>
			</div>
			<br clear="all"/>
    	  <%
    	}
        
        if (action.equals("LinkAuthorView")) {
            displayLinkViewSelection(1, kmeliaScc, out);
            
            Collection linkedPublications = kmeliaScc.getLinkedVisiblePublications();
            displaySameSubjectPublications(linkedPublications, resources.getString("PubReferenceeParAuteur"), kmeliaScc, id, isOwner, resources, out);
        } else if (action.equals("SameTopicView")) {
            displayLinkViewSelection(3, kmeliaScc, out);
            displaySameSubjectPublications(kmeliaScc.getSessionPublicationsList(), resources.getString("PubDeMemeTheme"), kmeliaScc, id, false, resources, out);
        } else if (action.equals("SameSubjectView")) {
            displayLinkViewSelection(2, kmeliaScc, out);
            
            String keywords = kmeliaScc.getSessionPublication().getDetail().getKeywords();
            
            String queryStr = pubName+" "+keywords;
            
            //'*' or '?' not allowed as first character in WildcardQuery
            queryStr = queryStr.replace(" ?", " ");
            queryStr = queryStr.replace(" *", " ");
            
            QueryDescription query = new QueryDescription(queryStr);
            query.setSearchingUser(kmeliaScc.getUserDetail().getId());
            query.addSpaceComponentPair(kmeliaScc.getSpaceId(), kmeliaScc.getComponentId());
            
            List<MatchingIndexEntry> results = SearchEngineFactory.getSearchEngine().search(query).getEntries();
            
            displaySearchResults(results, resources.getString("PubDeMemeSujet"), kmeliaScc, id, resources, out);
        }else if(action.equals("PubReferencedBy")){
          displayLinkViewSelection(4, kmeliaScc, out);
          
          List  targets = pubComplete.getReverseLinkList();
          
          Collection referencedPublications = kmeliaScc.getPublications(targets);
          displaySameSubjectPublications(referencedPublications, resources.getString("PubReferencedBy"), kmeliaScc, id, false, resources, out);
        }
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<form name="pubForm" action="<%=routerUrl%>publicationManager.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="PubId"/>
  <input type="hidden" name="CheckPath"/>
</form>
</body>
</html>