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
	String chemin = "<a href=\"ViewCategories\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.listCategorie"))+"</a>";
	String chemin2 ="<a href=\"ViewResources?id="+ idCategory + "\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.categorie"))+"</a>";
	chemin = chemin + " > " + chemin2;
	browseBar.setPath(chemin);
}
else if (provenance.equals("reservation")){
	// on vient du récapitulatif de la réservation
	String chemin ="<a href=\"ViewReservation\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.recapitulatifReservation"))+"</a>";
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