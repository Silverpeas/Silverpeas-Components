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
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>
<% 
//Récupération des détails de l'ulisateur
//list est la liste des ressources de la catégorie
//listCategory est la liste de l'ensemble des catégories
List list = (List)request.getAttribute("list");
List listCategory = (List)request.getAttribute("listCategories");
String idCategory = (String)request.getAttribute("categoryId");
String categoryName ="";

while(listCategory.isEmpty() == false){
	CategoryDetail maCategory = (CategoryDetail)listCategory.get(0);
	String categoryIdTemp = maCategory.getId();
	if(categoryIdTemp.equals(idCategory)){
		categoryName = maCategory.getName();
	}
	listCategory.remove(0);
}
ArrayLine arrayLine;
ArrayCellText arrayCellText2;
%>
<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script language="JavaScript">
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
		
		String chemin = "<a href=\"ViewCategories\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.listCategorie"))+"</a>";
		browseBar.setExtraInformation(resource.getString("resourcesManager.gererresource") + " " + categoryName);	
		browseBar.setPath(chemin);

		operationPane.addOperation(resource.getIcon("resourcesManager.creerResource"), resource.getString("resourcesManager.creerressource"),"NewResource?categoryId="+idCategory);
		
//		Definition du tableau et des colonnes
		ArrayPane arrayPane = gef.getArrayPane("resourceList", "ViewResources?id="+idCategory, request, session);
		ArrayColumn colreservable = arrayPane.addArrayColumn(resource.getString("resourcesManager.reservable"));
		colreservable.setWidth("20");
		arrayPane.addArrayColumn(resource.getString("GML.lastName"));
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("resourcesManager.operations"));
		columnOp.setSortable(false);
		
		for (int i=0; i<list.size(); i++){
			IconPane iconPane = gef.getIconPane();
			IconPane iconPane1 = gef.getIconPane();
			Icon reservableIcon = iconPane.addIcon();
			Icon editIcon = iconPane1.addIcon();
			Icon deleteIcon = iconPane1.addIcon();
			
			ResourceDetail maResource = (ResourceDetail)list.get(i);
			String name = maResource.getName();
			boolean bookable = maResource.getBookable();
			String resourceId = maResource.getId();
			arrayLine = arrayPane.addArrayLine();
			if(bookable == true)
				reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredGreen"),resource.getString("resourcesManager.resourcereservable"),"");
			else
				reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredRed"),resource.getString("resourcesManager.resourceirreservable"),"");
			arrayLine.addArrayCellIconPane(iconPane);
			arrayLine.addArrayCellLink(name,"ViewResource?resourceId="+resourceId+"&categoryId="+idCategory+"&provenance="+"resources");
			deleteIcon.setProperties(resource.getIcon("resourcesManager.smallDelete"), resource.getString("resourcesManager.supprimerressource"),"javascript:deleteResource('"+resourceId+"','"+name+"','"+idCategory+"')");
			editIcon.setProperties(resource.getIcon("resourcesManager.updateCategory"), resource.getString("resourcesManager.modifierresource"),"EditResource?resourceId="+resourceId+"&categoryId="+idCategory);
			arrayLine.addArrayCellIconPane(iconPane1);
		}
		
		out.println(window.printBefore());
		out.println(frame.printBefore());
		out.println(arrayPane.print());
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>

</body>
</html>