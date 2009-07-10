<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>
<% 
	// Récupération de la liste des catégories
	List list = (List)request.getAttribute("categories");

	// declaration des éléments du tableau
	ArrayCellText arrayCellText1;
	ArrayCellText arrayCellText2;
	ArrayLine arrayLine;
%>
<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	
	<script language="JavaScript">
	function deleteCategory(categoryId, name) {
		  if (confirm("<%=resource.getString("resourcesManager.deleteCategorie")%>" + " " + name + " ?")) {
			location.href="DeleteCategory?id="+categoryId;
		  }
	}
	</script>
	
	</head>
	<body>
	<%
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel,"Main");
		browseBar.setPath(resource.getString("resourcesManager.gerercategorie"));
	
		operationPane.addOperation(resource.getIcon("resourcesManager.creationOfCategory"), resource.getString("resourcesManager.creercategorie"),"NewCategory");
		//operationPane.addOperation(resource.getIcon("resourcesManager.creerResource"), resource.getString("resourcesManager.creerressource"),"NewResource?categoryId=noCategory");
		
		//Definition du tableau et des colonnes
		ArrayPane arrayPane = gef.getArrayPane("categoryList", "ViewCategories", request, session);
		ArrayColumn colreservable = arrayPane.addArrayColumn(resource.getString("resourcesManager.reservable"));
		colreservable.setWidth("20");
		arrayPane.addArrayColumn(resource.getString("GML.lastName"));
		arrayPane.addArrayColumn(resource.getString("resourcesManager.formulaire"));
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("resourcesManager.operations"));
		columnOp.setSortable(false);
		
		for(int i=0;i<list.size();i++){ 
			CategoryDetail category = (CategoryDetail)list.get(i);
			IconPane iconPane = gef.getIconPane();
			IconPane iconPane1 = gef.getIconPane();
		    Icon reservableIcon = iconPane.addIcon();
		    Icon editIcon = iconPane1.addIcon();
		    Icon deleteIcon = iconPane1.addIcon();
		  		    
		    //recupération des données de la liste
		    String id = category.getId();
		    String name = category.getName();
		    boolean bookable = category.getBookable();
			String form = category.getForm();

			arrayLine = arrayPane.addArrayLine();
			if (bookable == true)	
				reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredGreen"),resource.getString("resourcesManager.categoriereservable"),"");
			else
				reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredRed"),resource.getString("resourcesManager.categorieirreservable"),"");
			
			arrayLine.addArrayCellIconPane(iconPane);
			arrayLine.addArrayCellLink(name,"ViewResources?id="+id);
			arrayCellText2 = arrayLine.addArrayCellText(form);
		
			deleteIcon.setProperties(resource.getIcon("resourcesManager.smallDelete"), resource.getString("resourcesManager.supprimercategorie"),"javascript:deleteCategory('"+id+"','"+name+"')");
			editIcon.setProperties(resource.getIcon("resourcesManager.updateCategory"), resource.getString("resourcesManager.modifiercategorie"),"EditCategory?id="+id);
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