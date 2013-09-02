<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%@ page import="org.silverpeas.resourcemanager.model.Category"%>
<%@ page import="org.silverpeas.resourcemanager.model.Resource"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>
<%
  //Recuperation des details de l'utilsateur
  //list est la liste des ressources de la categorie
  //listCategory est la liste de l'ensemble des categories
  List<Resource> resources = (List) request.getAttribute("list");
  List<Category> listCategory = (List<Category>) request.getAttribute("listCategories");
  Long idCategory = (Long) request.getAttribute("categoryId");
  String categoryName = "";

  while (!listCategory.isEmpty()) {
    Category maCategory = listCategory.get(0);
    Long categoryIdTemp = maCategory.getId();
    if (categoryIdTemp.equals(idCategory)) {
      categoryName = maCategory.getName();
    }
    listCategory.remove(0);
  }
  ArrayLine arrayLine;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript">
function deleteResource(resourceId, name,categoryId) {
	if (confirm("<%=resource.getString("resourcesManager.deleteResource")%>" + " " + name + " ?")) {
		location.href="DeleteRessource?resourceId="+resourceId+"&categoryId="+categoryId;
	}
}
</script>
</head>
<body>
	<%
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel,"Main");
		
		String chemin = "<a href=\"ViewCategories\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.listCategorie"))+"</a>";
		browseBar.setExtraInformation(resource.getString("resourcesManager.gererresource") + " " + categoryName);	
		browseBar.setPath(chemin);

		operationPane.addOperationOfCreation(resource.getIcon("resourcesManager.creerResource"), resource.getString("resourcesManager.creerressource"),"NewResource?categoryId="+idCategory);
		
//		Definition du tableau et des colonnes
		ArrayPane arrayPane = gef.getArrayPane("resourceList", "ViewResources?id="+idCategory, request, session);
		ArrayColumn colreservable = arrayPane.addArrayColumn(resource.getString("resourcesManager.reservable"));
		colreservable.setWidth("20");
		arrayPane.addArrayColumn(resource.getString("GML.lastName"));
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("resourcesManager.operations"));
		columnOp.setSortable(false);

    for (Resource myResource : resources) {
      IconPane iconPane = gef.getIconPane();
      IconPane iconPane1 = gef.getIconPane();
      Icon reservableIcon = iconPane.addIcon();
      Icon editIcon = iconPane1.addIcon();
      Icon deleteIcon = iconPane1.addIcon();

      String name = myResource.getName();
      boolean bookable = myResource.isBookable();
      Long resourceId = myResource.getId();
      arrayLine = arrayPane.addArrayLine();
      if (bookable) {
        reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredGreen"),
            resource.getString("resourcesManager.resourcereservable"), "");
      } else {
        reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredRed"),
            resource.getString("resourcesManager.resourceirreservable"), "");
      }
      arrayLine.addArrayCellIconPane(iconPane);
      arrayLine.addArrayCellLink(name,
          "ViewResource?resourceId=" + resourceId + "&categoryId=" + idCategory + "&provenance=" +
              "resources");
      deleteIcon.setProperties(resource.getIcon("resourcesManager.smallDelete"), resource.getString("resourcesManager.supprimerressource"),
          "javascript:deleteResource('" + resourceId + "','" + name + "','" + idCategory + "')");
      editIcon.setProperties(resource.getIcon("resourcesManager.updateCategory"), resource.getString("resourcesManager.modifierresource"),
          "EditResource?resourceId=" + resourceId + "&categoryId=" + idCategory);
      arrayLine.addArrayCellIconPane(iconPane1);
    }
		
		out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%		
		out.println(arrayPane.print());
%>
</view:frame>
<%
		out.println(window.printAfter());
%>
</body>
</html>