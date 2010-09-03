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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>
<%
ResourceDetail myResource = (ResourceDetail)request.getAttribute("resource");
String idCategory = myResource.getCategoryId();

String resourceId = (String)request.getAttribute("resourceId");
String userId = (String)request.getAttribute("UserId");
String profile = (String)request.getAttribute("Profile");
String url = (String)request.getAttribute("Url") + "Comments";
String provenance = (String)request.getAttribute("provenance");
String indexIt = "0";
%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel,"Main");
if(provenance.equals("resources")){
	// on vient de resources
	String chemin = "<a href=\"ViewCategories\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.listCategorie"))+"</a>";
	String chemin2 ="<a href=\"ViewResources?id="+ idCategory + "\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.categorie"))+"</a>";
	chemin = chemin + " > " + chemin2;
	browseBar.setPath(chemin);
}
else if (provenance.equals("reservation")){
	// on vient du récapitulatif de la réservation
	String chemin ="<a href=\"ViewReservation\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.recapitulatifReservation"))+"</a>";
	browseBar.setPath(chemin);
}
browseBar.setExtraInformation(resource.getString("resourcesManager.informationResource") + " " + myResource.getName());

tabbedPane.addTab(resource.getString("resourcesManager.resource"), "ViewResource", false);
tabbedPane.addTab(resource.getString("resourcesManager.commentaires"), "#", true);

out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());

out.flush(); 
getServletConfig().
getServletContext().
getRequestDispatcher("/comment/jsp/comments.jsp?id="+resourceId
+"&userid="+userId
+"&profile="+profile
+"&url="+url
+"&component_id="+componentId
+"&IndexIt="+indexIt).
include(request, response);

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>