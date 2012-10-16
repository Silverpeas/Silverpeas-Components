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
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.resourcemanager.model.Category"%>
<%@ page import="java.util.List" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<% 
	// Recuperation de la liste des categories
	List list = (List)request.getAttribute("categories");

	// declaration des elements du tableau
	ArrayCellText arrayCellText1;
	ArrayCellText arrayCellText2;
	ArrayLine arrayLine;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<view:looknfeel />
	<script type="text/javascript">
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
	
		operationPane.addOperationOfCreation(resource.getIcon("resourcesManager.creationOfCategory"), resource.getString("resourcesManager.creercategorie"),"NewCategory");
		
		//Definition du tableau et des colonnes
		ArrayPane arrayPane = gef.getArrayPane("categoryList", "ViewCategories", request, session);
		ArrayColumn colreservable = arrayPane.addArrayColumn(resource.getString("resourcesManager.reservable"));
		colreservable.setWidth("20");
		arrayPane.addArrayColumn(resource.getString("GML.name"));
		arrayPane.addArrayColumn(resource.getString("resourcesManager.formulaire"));
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("resourcesManager.operations"));
		columnOp.setSortable(false);
		
		for(int i=0;i<list.size();i++){ 
			Category category = (Category)list.get(i);
			IconPane iconPane = gef.getIconPane();
			IconPane iconPane1 = gef.getIconPane();
		    Icon reservableIcon = iconPane.addIcon();
		    Icon editIcon = iconPane1.addIcon();
		    Icon deleteIcon = iconPane1.addIcon();
		  		    
		    //recuperation des donnees de la liste
		    String id = category.getId();
		    String name = category.getName();
		    boolean bookable = category.isBookable();
			String form = category.getForm();

			arrayLine = arrayPane.addArrayLine();
			if (bookable) {
				reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredGreen"),resource.getString("resourcesManager.categoriereservable"),"");
            } else {
				reservableIcon.setProperties(resource.getIcon("resourcesManager.buletColoredRed"),resource.getString("resourcesManager.categorieirreservable"),"");
                		}
			arrayLine.addArrayCellIconPane(iconPane);
			arrayLine.addArrayCellLink(name,"ViewResources?id="+id);
			arrayCellText2 = arrayLine.addArrayCellText(form);
		
			deleteIcon.setProperties(resource.getIcon("resourcesManager.smallDelete"), resource.getString("resourcesManager.supprimercategorie"),"javascript:deleteCategory('"+id+"','"+name+"')");
			editIcon.setProperties(resource.getIcon("resourcesManager.updateCategory"), resource.getString("resourcesManager.modifiercategorie"),"EditCategory?id="+id);
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