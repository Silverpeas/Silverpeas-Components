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
<%@ include file="publicationsList.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%!
 //Icons
String folderSrc;
String publicationSrc;
String fullStarSrc;
String emptyStarSrc;
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
// Fin des déclarations
%>

<%
//Récupération des paramètres
String profile 		= (String) request.getAttribute("Profile");
String action 		= (String) request.getAttribute("Action");
String wizard		= (String) request.getAttribute("Wizard");
String	currentLang = (String) request.getAttribute("Language");

//Icons
publicationSrc		= m_context + "/util/icons/publication.gif";
fullStarSrc			= m_context + "/util/icons/starFilled.gif";
emptyStarSrc		= m_context + "/util/icons/starEmpty.gif";
seeAlsoSrc			= "icons/linkedAdd.gif";
seeAlsoDeleteSrc	= "icons/linkedDel.gif";

//Vrai si le user connecte est le createur de cette publication ou si il est admin
boolean isOwner = false;
TopicDetail currentTopic = null;

String linkedPathString = kmeliaScc.getSessionPath();

UserCompletePublication userPubComplete = kmeliaScc.getSessionPublication();
CompletePublication 	pubComplete 	= userPubComplete.getPublication();
UserDetail 				ownerDetail 	= userPubComplete.getOwner();
String					id				= pubComplete.getPublicationDetail().getPK().getId();
String 					pubName 		= pubComplete.getPublicationDetail().getName(currentLang);
String					instanceId		= pubComplete.getPublicationDetail().getPK().getInstanceId();

if (profile.equals("admin") || profile.equals("publisher") || profile.equals("supervisor") || (ownerDetail != null && kmeliaScc.getUserDetail().getId().equals(ownerDetail.getId()) && profile.equals("writer")))
	isOwner = true;
%>
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function seeAlsoDeleteConfirm() {
	if (document.seeAlsoForm.PubIds != null){
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
</HEAD>
<BODY onUnload="closeWindows()">
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();
        Board boardHelp = gef.getBoard();
        
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

        if (isOwner)
            displayAllOperations(id, kmeliaScc, gef, action, resources, out);
        else
            displayUserOperations(id, kmeliaScc, gef, action, resources, out);

        out.println(frame.printBefore());

        if ("finish".equals(wizard))
    	{
    		//  cadre d'aide
    	    out.println(boardHelp.printBefore());
    		out.println("<table border=\"0\"><tr>");
    		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
    		out.println("<td>"+kmeliaScc.getString("kmelia.HelpSeeAlso")+"</td>");
    		out.println("</tr></table>");
    	    out.println(boardHelp.printAfter());
    	    out.println("<BR>");
    	}
        
        if (action.equals("LinkAuthorView")) {
            displayLinkViewSelection(1, kmeliaScc, out);
                            
            List  targets = pubComplete.getLinkList();
            
            Collection linkedPublications = kmeliaScc.getPublications(targets);
            displaySameSubjectPublications(linkedPublications, resources.getString("PubReferenceeParAuteur"), kmeliaScc, id, isOwner, resources, out);
        } else if (action.equals("SameTopicView")) {
            displayLinkViewSelection(3, kmeliaScc, out);
            currentTopic = kmeliaScc.getSessionTopic();
            displaySameSubjectPublications(kmeliaScc.getSessionTopic().getPublicationDetails(), resources.getString("PubDeMemeTheme"), kmeliaScc, id, false, resources, out);
        } else if (action.equals("SameSubjectView")) {
            displayLinkViewSelection(2, kmeliaScc, out);
            
            String keywords = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getKeywords();
            
            SearchEngineBm searchEngine = kmeliaScc.getSearchEngine();
            String queryStr = pubName+" "+keywords;
            
            //'*' or '?' not allowed as first character in WildcardQuery
            queryStr = queryStr.replace(" ?", " ");
            queryStr = queryStr.replace(" *", " ");
            
            QueryDescription query = new QueryDescription(queryStr);
            query.setSearchingUser(kmeliaScc.getUserDetail().getId());
            query.addSpaceComponentPair(kmeliaScc.getSpaceId(), kmeliaScc.getComponentId());
            MatchingIndexEntry[] result = null;
            try {
                searchEngine.search(query);
                result = searchEngine.getRange(0, searchEngine.getResultLength());
            } catch (com.stratelia.webactiv.searchEngine.model.ParseException pe) {
				  throw new KmeliaException("JSPpublicationManager",SilverpeasRuntimeException.ERROR,"root.EX_SEARCH_ENGINE_FAILED", pe);
            }
            displaySearchResults(result, resources.getString("PubDeMemeSujet"), kmeliaScc, id, resources, out);
        }else if(action.equals("PubReferencedBy")){
          displayLinkViewSelection(4, kmeliaScc, out);
          
          List  targets = pubComplete.getReverseLinkList();
          
          Collection referencedPublications = kmeliaScc.getPublications(targets);
          displaySameSubjectPublications(referencedPublications, resources.getString("PubReferencedBy"), kmeliaScc, id, false, resources, out);
        }
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<FORM NAME="pubForm" action="<%=routerUrl%>publicationManager.jsp" METHOD="POST">
	<input type="hidden" name="Action"><input type="hidden" name="PubId">
	<input type="hidden" name="CheckPath">
</FORM>
</BODY>
</HTML>