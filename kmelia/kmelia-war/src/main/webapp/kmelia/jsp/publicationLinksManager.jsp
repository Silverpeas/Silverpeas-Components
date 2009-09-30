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
<%@ include file="publicationsList.jsp.inc" %>

<%!
  //Icons
  String folderSrc;
  String publicationLinkSrc;
  String publicationSrc;
  String fullStarSrc;
  String emptyStarSrc;
  String topicSrc;
%>

<% 
//Récupération des paramètres
String 	action 			= (String) request.getParameter("Action");
String 	id 				= (String) request.getParameter("TopicId");
String 	size 			= (String) request.getAttribute("NbLinks");
List 	selectedPubIds 	= (List) request.getAttribute("SelectedPubIds");
String 	reference		= "";

String pubId = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getPK().getId();

Button closeButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:closeAndReturn('"+pubId+"');", false);

//Icons
folderSrc = m_context + "/util/icons/component/kmeliaSmall.gif";
publicationLinkSrc = m_context + "/util/icons/kmelia_publiArrange.gif";
publicationSrc = m_context + "/util/icons/publication.gif";
fullStarSrc = m_context + "/util/icons/starFilled.gif";
emptyStarSrc = m_context + "/util/icons/starEmpty.gif";
topicSrc = m_context + "/util/icons/folder.gif";

%>

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<Script language="JavaScript">
function topicGoTo(id) {
    document.pubChooseLinkForm.Action.value = "Search";
    document.pubChooseLinkForm.TopicId.value = id;
    document.pubChooseLinkForm.submit();
}

function getSelectedOjects()
{
	return getObjects(true);
}
function getNotSelectedOjects()
{
	return getObjects(false);
}

function getObjects(selected)
{
	var  items = "";
	var boxItems = document.publicationsForm.C1;
	if (boxItems != null){
		// au moins une checkbox exist
		var nbBox = boxItems.length;
		if ( (nbBox == null) && (boxItems.checked == selected) ){
			// il n'y a qu'une checkbox non selectionnée
			items += boxItems.value+",";
		} else{
			// search not checked boxes 
			for (i=0;i<boxItems.length ;i++ ){
				if (boxItems[i].checked == selected){
					items += boxItems[i].value+",";
				}
			}
		}
	}
	return items;
}

function doPagination(index)
{
	var  selectItems 	= getSelectedOjects();
	var  notSelectItems = getNotSelectedOjects();
	
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToLink=1',"Index="+index,"SelectedIds="+selectItems,"NotSelectedIds="+notSelectItems);
}

function setPubsAsLinks(nb) {
    var  selectItems 	= getSelectedOjects();
	var  notSelectItems = getNotSelectedOjects();
	
	document.pubChooseLinkForm.SelectedIds.value 		= selectItems;
	document.pubChooseLinkForm.NotSelectedIds.value 	= notSelectItems;
	document.pubChooseLinkForm.action					= "AddLinksToPublication";
    
    document.pubChooseLinkForm.submit();
}

function closeAndReturn(pubId) {
    window.opener.location.replace("SeeAlso?PubId="+pubId);
    window.close();
}

function init()
{
	ajaxEngine.registerRequest('refreshPubList', '<%=m_context%>/RAjaxPublicationsListServlet/dummy');
	
	ajaxEngine.registerAjaxElement('pubList');
	
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToLink=1','SelectedIds=','NotSelectedIds=');
}
</script>
</HEAD>
<BODY onLoad="init()">
<%
      if (id == null) {
	        id = "0";
	        action = "Search";
      }
      if (action.equals("Add")) {
            reference = "<br><TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><tr><td>";
			reference += "<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4><tr>";
			reference += "<td align=center>"+size+"&nbsp;"+kmeliaScc.getString("NPubDeclass")+"</td></tr></table></td></tr></table><br>";
			action = "Search";
      } 
      if (action.equals("Search") || action.equals("OtherPublications")) {
			TopicDetail currentTopicToLink = null;
			if (action.equals("Search"))
			{
				currentTopicToLink = kmeliaScc.getTopic(id, false);
				kmeliaScc.setSessionTopicToLink(currentTopicToLink);
			} else {
				currentTopicToLink = kmeliaScc.getSessionTopicToLink();
			}
 
            Collection path = currentTopicToLink.getPath();
            String pathString = displayPath(path, false, 3);
            String linkedPathString = displayPath(path, true, 3);
            List validPublications = currentTopicToLink.getValidPublications();

            Window window = gef.getWindow();

            BrowseBar browseBar = window.getBrowseBar();
            browseBar.setDomainName(kmeliaScc.getSpaceLabel());
            browseBar.setComponentName(kmeliaScc.getComponentLabel(), "javascript:onClick=topicGoTo('0')");
            browseBar.setPath(linkedPathString);

            OperationPane operationPane = window.getOperationPane();
            operationPane.addOperation(publicationLinkSrc, kmeliaScc.getString("LinkPub"), "javascript:onClick=setPubsAsLinks('"+validPublications.size()+"')");

		  Frame frame = gef.getFrame();
		  
          out.println(window.printBefore());
		  out.println(frame.printBefore());

          displaySessionTopicsToUsers(kmeliaScc, currentTopicToLink, gef, request, session, resources, out);
		  if (!reference.equals("")){
		  	out.println(reference);
		  }
		  out.println("<div id=\"pubList\"/>");

    out.println(frame.printMiddle());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(closeButton);
	
	String bodyPart ="<br><center>";
	bodyPart += buttonPane.print();
	bodyPart +="</center><br>";

	out.println(bodyPart);
 
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
<FORM Name="pubChooseLinkForm" ACTION="publicationLinksManager.jsp" METHOD="POST">
      <input type="hidden" name="Action">
      <input type="hidden" name="TopicId" value="<%=currentTopicToLink.getNodeDetail().getNodePK().getId()%>">
      <input type="hidden" name="PubId" value="<%=pubId%>">
      <input type="hidden" name="SelectedPubIds">
      <input type="hidden" name="Index">
      <input type="hidden" name="SelectedIds">
      <input type="hidden" name="NotSelectedIds">
</FORM>
</BODY>
</HTML>
<% } %>