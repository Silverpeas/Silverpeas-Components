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
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ include file="checkKmelia.jsp" %>

<%
List<PublicationTemplate> xmlForms	= (List<PublicationTemplate>) request.getAttribute("XMLForms");
PublicationDetail 	pubDetail 	= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
Boolean				wysiwygValid		= (Boolean) request.getAttribute("WysiwygValid");
String				currentLang = (String) request.getAttribute("Language");

String pubId = pubDetail.getPK().getId();
String pubName = pubDetail.getName(currentLang);

String linkedPathString = kmeliaScc.getSessionPath();

boolean isOwner = kmeliaScc.getSessionOwner();

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "DeletePublication?PubId="+pubId, false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel withFieldsetStyle="true"/>
<script language="javaScript">
function sendToWysiwyg() {
    document.toWysiwyg.submit();
}

function goToForm(xmlFormName) {
    document.xmlForm.Name.value = xmlFormName;
    document.xmlForm.submit();
}

function topicGoTo(id) {
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</head>
<body onunload="closeWindows()" id="<%=componentId%>">
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(linkedPathString);
	  browseBar.setExtraInformation(pubName);

    out.println(window.printBefore());

    if (isOwner) {
        KmeliaDisplayHelper.displayAllOperations(pubId, kmeliaScc, gef, "ModelChoice",
              resources, out, kmaxMode);
    } else {
        KmeliaDisplayHelper.displayUserOperations(kmeliaScc, out);
    }
    out.println(frame.printBefore());
    
%>

<fieldset id="pubExtraForm" class="skinFieldset">
  <legend><%=resources.getString("ModelChoiceTitle")%></legend>
  <table cellpadding="5" width="100%" id="templates">
<%
  int nb = 0;
  out.println("<tr>");
  if (xmlForms != null) {
    String thumbnail = "";
    for (PublicationTemplate xmlForm : xmlForms) {
      if (nb != 0 && nb%3==0) {
        out.println("</tr><tr>");
      }
      nb++;

      thumbnail = xmlForm.getThumbnail();
      if (!StringUtil.isDefined(thumbnail)) {
        thumbnail = PublicationTemplate.DEFAULT_THUMBNAIL;
      }
      out.println("<td class=\"template\"><a href=\"javaScript:goToForm('"+xmlForm.getFileName()+"')\"><img src=\""+thumbnail+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"/><br/>"+xmlForm.getName()+"</a></td>");
    }
	}
    
	if (wysiwygValid) {
    if (nb != 0 && nb%3 == 0) {
			out.println("</tr><tr>");
    }
			
	  out.println("<td class=\"template\"><a href=\"javaScript:sendToWysiwyg();\"><img src=\"../../util/icons/model/wysiwyg.gif\" border=\"0\" alt=\"Wysiwyg\"/><br/>WYSIWYG</a></td>");
	  out.println("</tr>");
	}
%>
</table>
</fieldset>
<%
    out.println(frame.printAfter());
%>

<form name="toWysiwyg" action="ToWysiwyg" method="post">
</form>
<form name="xmlForm" action="GoToXMLForm" method="post">
	<input type="hidden" name="Name"/>
	<input type="hidden" name="PubId" value="<%=pubId%>"/>
</form>
<% out.println(window.printAfter()); %>
</body>
</html>