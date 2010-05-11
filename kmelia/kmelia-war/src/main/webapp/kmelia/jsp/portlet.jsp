<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="publicationsList.jsp.inc" %>

<%!
  //Icons
  String folderSrc;
  String publicationSrc;
  String fullStarSrc;
  String emptyStarSrc;
  String topicSrc;
%>

<% 

String rootId = "0";
String space = "";
String action = "";
String sort = "";
String id = "";
String description = "";
String alertType = "";
String creationDate = "";
String creatorName = "";
Collection path = null;
String level = "";
String fatherId = "";
String childId = "";
Collection subTopicList = null;
Collection publicationList = null;
String linkedPathString = "";
String pathString = "";
String language = "";
String profile = "";

//R�cup�ration des param�tres
action = (String) request.getParameter("Action");
sort = (String) request.getParameter("Sort");
id = (String) request.getParameter("Id");
childId = (String) request.getParameter("ChildId");
language = (String) request.getParameter("Language");
space = (String) request.getParameter("Space");
profile = (String) request.getParameter("Profile");
String translation = (String) request.getParameter("Translation");
if (translation == null)
	translation = kmeliaScc.getLanguage();

//Icons
folderSrc = m_context + "/util/icons/component/kmeliaSmall.gif";
publicationSrc = m_context + "/util/icons/publication.gif";
fullStarSrc = m_context + "/util/icons/starFilled.gif";
emptyStarSrc = m_context + "/util/icons/starEmpty.gif";
topicSrc = m_context + "/util/icons/folder.gif";

//Mise a jour de l'espace
if (action == null) {
	id = rootId;
	action = "Search";
}

TopicDetail currentTopic = null;

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", kmeliaScc.getLanguage());

%>

<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<TITLE></TITLE>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<Script language="JavaScript1.2">
function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Translation.value = "<%=translation%>";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function publicationGoTo(id){
    document.pubForm.Action.value = "View";
    document.pubForm.Id.value = id;
    document.pubForm.submit();
}
function publicationGoToFromMain(id){
    document.pubForm.CheckPath.value = "1";
    publicationGoTo(id);
}

function doPagination(index)
{
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToPortlet=1',"Index="+index);
}

function init()
{
	ajaxEngine.registerRequest('refreshPubList', '<%=m_context%>/RAjaxPublicationsListServlet/dummy');
	
	ajaxEngine.registerAjaxElement('pubList');
	
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToPortlet=1');
}
</script>
</HEAD>

<BODY onLoad="init()">

<%
//Traitement = View, Search, Add, Update ou Delete
      if (id == null) {
            if (sort != null) {
                currentTopic = kmeliaScc.getSessionTopic();
                id = currentTopic.getNodePK().getId();
            } else {
                id = rootId;
            }
            action = "Search";
      }
      if (action.equals("Search")) {
		currentTopic = kmeliaScc.getTopic(id);
		kmeliaScc.setSessionTopic(currentTopic);
		path = currentTopic.getPath();
		pathString = displayPath(path, false, 3, translation);
		linkedPathString = displayPath(path, true, 3, translation);

		kmeliaScc.setSessionPath(linkedPathString);
	  } 
	  else if (action.equals("OtherPublications"))
	  {
		currentTopic		= kmeliaScc.getSessionTopic();
		pathString			= kmeliaScc.getSessionPathString();
	    linkedPathString	= kmeliaScc.getSessionPath();
	  }

		Window window = gef.getWindow();

		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setDomainName(kmeliaScc.getSpaceLabel());
        browseBar.setComponentName(kmeliaScc.getComponentLabel(), "javascript:onClick=topicGoTo('0')");
		browseBar.setPath(linkedPathString);

		Frame frame = gef.getFrame();

		out.println(window.printBefore());

		if ((!id.equals("1")) && (!id.equals("2"))) {
			displaySessionTopicsToUsers(kmeliaScc, currentTopic, gef, request, session, resources, out);
		}

		if (id.equals("0"))
		{
			displayLastPublications(currentTopic.getPublicationDetails(), (currentTopic.getNodeDetail().getChildrenNumber() > 0), pathString, kmeliaScc.getString("PublicationsLast"), kmeliaScc, settings, resources, out);
		}
		else
		{
			out.println("<div id=\"pubList\"/>");
		}

		out.println(window.printAfter());
%>

<FORM NAME="topicDetailForm" ACTION="portlet.jsp" METHOD=POST >
  <input type="hidden" name="Action"><input type="hidden" name="Id" value="<%=id%>">
  <input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(pathString)%>"><input type="hidden" name="ChildId">
  <input type="hidden" name="Name"><input type="hidden" name="Description">
  <input type="hidden" name="Translation">
  <input type="hidden" name="AlertType"></TD>
</FORM>

<FORM NAME="pubForm" ACTION="searchResult.jsp" METHOD="POST" target="MyMain">
<input type="hidden" name="Action">
<input type="hidden" name="Id">
<input type="hidden" name="Path">
<input type="hidden" name="CheckPath">
<input type="hidden" name="Space" value="<%=kmeliaScc.getSpaceId()%>">
<input type="hidden" name="Component" value="<%=kmeliaScc.getComponentId()%>">
<input type="hidden" name="Type" value="Publication">
</FORM>

</BODY>
</HTML>