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
<%@page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ page import="org.silverpeas.components.kmelia.model.KmeliaPublication" %>
<%@ page import="org.silverpeas.components.kmelia.model.TopicDetail" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp"%>
<%@ include file="topicReport.jsp"%>

<%
  String topicId = "";
  String pubName = "";
  String action = "";
  String path = "";
  String profile = "user";
  String checkPath = "";

  String id =  request.getParameter("Id");
  String currentLang =  (String) request.getAttribute("Language");

  //Vrai si le user connecte est le createur de cette publication ou si il est admin
  boolean isOwner = false;
  TopicDetail currentTopic = null;

  String pathString = "";

  String user_id = kmeliaScc.getUserId();

  //Calcul du chemin de la publication
  currentTopic = kmeliaScc.getBestTopicDetailsOfPublication(id);
  Collection pathColl = currentTopic.getPath();
  pathString = displayPath(pathColl, false, 3);

  //Recuperation des parametres de la publication
  KmeliaPublication kmeliaPublication = kmeliaScc.getPublication(id);
  kmeliaScc.setSessionPublication(kmeliaPublication);
  pubName = kmeliaScc.getSessionPublication().getDetail().getName(currentLang);
  CompletePublication pubComplete = kmeliaPublication.getCompleteDetail();
  User ownerDetail = kmeliaPublication.getCreator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
</head>
<body onunload="closeWindows()">
<%
  Window window = gef.getWindow();
  Frame frame = gef.getFrame();

  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(kmeliaScc.getSpaceLabel());
  browseBar.setComponentName(kmeliaScc.getComponentLabel());
  browseBar.setPath(pathString);
  browseBar.setExtraInformation(pubName);
  browseBar.setClickable(false);

  out.println(window.printBefore());
  out.println(frame.printBefore());

  PublicationDetail detail = pubComplete.getPublicationDetail();
  String status = detail.getStatus();
  int type = 0;
  if ( kmeliaScc.isVersionControlled() )
  {
    type = 1; // Versioning
  }
  String newCssClass = detail.isNew() ? " class='new-contribution'" : "";

%>
	<table border="0" width="98%" align=center>
		<tr>
			<td align="left"<%=newCssClass%>><span class="txtnav publication-name"><strong><%=WebEncodeHelper.javaStringToHtmlString(detail.getName(kmeliaScc.getCurrentLanguage()))%></strong></span><BR>

        <strong><%=WebEncodeHelper.javaStringToHtmlParagraphe(detail.getDescription(kmeliaScc.getCurrentLanguage()))%></strong>
				<br />
				<br /> 
<%
  String userId = detail.getUpdaterId();
  if (userId == null || userId.length() == 0) {
    userId = detail.getCreatorId();
  }

  if (userId != null && userId.length() > 0) {
    User user = kmeliaScc.getUserDetail(userId);
	if (user != null) {
%>
			</td>
			<td valign=top align=right><%=user.getFirstName()+" "+user.getLastName()%><br />
			<i><%=resources.getOutputDate(detail.getLastUpdateDate())%> </i><br />
<%		
    }
  }
%>
			</td>
		</tr>
	</table>
	
	<table border="0" width="98%" align=center>
<%
  if (WysiwygController.haveGotWysiwygToDisplay(detail.getPK().getComponentName(), detail.getPK().getId(), kmeliaScc.getCurrentLanguage())) {
%>
		<tr>
			<td><view:displayWysiwyg objectId="<%=detail.getPK().getId()%>"
					componentId="<%=detail.getPK().getComponentName() %>"
					language="<%=kmeliaScc.getCurrentLanguage() %>" />
			</td>
<%
	if (! ("bottom".equals(settings.getString("attachmentPosition") ) ) ) {
%>
			<td width="25%" valign="top" align="center">
				<a NAME=attachments></a>
<%		
		KmeliaDisplayHelper.displayUserAttachmentsView(detail, m_context, out, kmeliaScc
                    .getLanguage(), true, resources);
%>
			</td>
<% 	
	} else {
%>	  
		</tr>
		
		<tr>
			<td valign="top">
				<a NAME=attachments></a>
<% 				
		KmeliaDisplayHelper.displayUserAttachmentsView(detail, m_context,
				out, kmeliaScc.getLanguage(), false, resources);
%>
			</td>
<%
	}
  }
%>
		</tr>	
	</table>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter()); 
%>
</body>
</html>
