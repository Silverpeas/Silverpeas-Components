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
        if (selectedId == 1) {
            out.println("<option value=\"LinkAuthorView\" selected>"+kmeliaScc.getString("PubReferenceeParAuteur")+"</option>");
            out.println("<option value=\"SameSubjectView\">"+kmeliaScc.getString("PubDeMemeSujet")+"</option>");
            out.println("<option value=\"SameTopicView\">"+kmeliaScc.getString("PubDeMemeTheme")+"</option>");
        } else if (selectedId == 2) {
            out.println("<option value=\"LinkAuthorView\">"+kmeliaScc.getString("PubReferenceeParAuteur")+"</option>");
            out.println("<option value=\"SameSubjectView\" selected>"+kmeliaScc.getString("PubDeMemeSujet")+"</option>");
            out.println("<option value=\"SameTopicView\">"+kmeliaScc.getString("PubDeMemeTheme")+"</option>");
        } else {
            out.println("<option value=\"LinkAuthorView\">"+kmeliaScc.getString("PubReferenceeParAuteur")+"</option>");
            out.println("<option value=\"SameSubjectView\">"+kmeliaScc.getString("PubDeMemeSujet")+"</option>");
            out.println("<option value=\"SameTopicView\" selected>"+kmeliaScc.getString("PubDeMemeTheme")+"</option>");
        }
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
                            
            Collection targets = pubComplete.getInfoDetail().getInfoLinkList();
            Iterator targetIterator = targets.iterator();
            ArrayList targetIds = new ArrayList();
            while (targetIterator.hasNext()) {
              String targetId = ((InfoLinkDetail) targetIterator.next()).getTargetId();
              targetIds.add(targetId);
            }
            Collection linkedPublications = kmeliaScc.getPublications(targetIds);
            displaySameSubjectPublications(linkedPublications, resources.getString("PubReferenceeParAuteur"), kmeliaScc, id, isOwner, resources, out);
        } else if (action.equals("SameTopicView")) {
            displayLinkViewSelection(3, kmeliaScc, out);
            currentTopic = kmeliaScc.getSessionTopic();
            displaySameSubjectPublications(kmeliaScc.getSessionTopic().getPublicationDetails(), resources.getString("PubDeMemeTheme"), kmeliaScc, id, false, resources, out);
        } else if (action.equals("SameSubjectView")) {
            displayLinkViewSelection(2, kmeliaScc, out);
            
            String keywords = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getKeywords();
            
            SearchEngineBm searchEngine = kmeliaScc.getSearchEngine();
            QueryDescription query = new QueryDescription(pubName +" "+keywords);
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