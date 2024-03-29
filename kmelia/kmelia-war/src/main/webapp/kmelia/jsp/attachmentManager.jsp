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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ page import="org.silverpeas.kernel.util.StringUtil" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@include file="checkKmelia.jsp" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%
PublicationDetail 	pubDetail 	= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");

String				currentLang = (String) request.getAttribute("Language");
List languages	= (List) request.getAttribute("Languages");
String 				xmlForm		= (String) request.getAttribute("XmlFormForFiles");
if (!StringUtil.isDefined(xmlForm))
{
	xmlForm = "";
}

String pubName 	= pubDetail.getName(currentLang);
String pubId 	= pubDetail.getPK().getId();

String	pIndexIt	= "0";

boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
      isOwner = true;

String linkedPathString = kmeliaScc.getSessionPath();

String url = kmeliaScc.getComponentUrl()+"ViewAttachments";

boolean openUrl = false;
if (request.getParameter("OpenUrl") != null)  {
	openUrl = Boolean.parseBoolean(request.getParameter("OpenUrl"));
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
function showTranslation(lang) {
	location.href="ViewAttachments?SwitchLanguage="+lang;
}

function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}
</script>
</head>
<body>
<%
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);
	browseBar.setI18N(languages, currentLang);

	out.println(window.printBefore());

  if (isOwner) {
    KmeliaDisplayHelper.displayAllOperations(pubId, kmeliaScc, gef, "ViewAttachments",
          resources, out, kmaxMode);
  } else {
    KmeliaDisplayHelper.displayUserOperations(kmeliaScc, out);
  }

	out.println(frame.printBefore());

	out.flush();
  getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+pubId+"&ComponentId="+componentId+"&dnd=true&Context=attachment&IndexIt="+pIndexIt+"&Url="+url+"&UserId="+kmeliaScc.getUserId()+"&OpenUrl="+openUrl+"&Profile="+kmeliaScc.getProfile()+"&Language="+currentLang+"&XMLFormName="+URLEncoder.encode(xmlForm)).include(request, response);
	out.flush();

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<%@ include file="../../sharing/jsp/createTicketPopin.jsp" %>
</body>
</html>