<%@ include file="check.jsp" %>

<% 
Collection	categories	= (Collection) request.getAttribute("Categories");
String 		profile 	= (String) request.getAttribute("Profile");

%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
var categoryWindow = window;

function addCategory() {
	url = "NewCategory";
    windowName = "categoryWindow";
	larg = "570";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!categoryWindow.closed && categoryWindow.name== "categoryWindow")
        categoryWindow.close();
    categoryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function editCategory(id) {
    url = "EditCategory?CategoryId="+id;
    windowName = "categoryWindow";
	larg = "550";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!categoryWindow.closed && categoryWindow.name== "categoryWindow")
        categoryWindow.close();
    categoryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}
	
function deleteConfirm(id,nom) 
{
	if(window.confirm("<%=resource.getString("blog.confirmDeleteCategory")%> '" + nom + "' ?"))
	{
		document.categoryForm.action = "DeleteCategory";
		document.categoryForm.CategoryId.value = id;
		document.categoryForm.submit();
	}
}

</script>
</head>
<body id="blog">
<div id="<%=instanceId %>">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	
   	operationPane.addOperation(resource.getIcon("blog.addCategory"), resource.getString("blog.addCategory") , "javascript:onClick=addCategory()");

	out.println(window.printBefore());
    out.println(frame.printBefore());
    
	ArrayPane arrayPane = gef.getArrayPane("categoryList", "ViewCategory", request, session);
	ArrayColumn columnIcon = arrayPane.addArrayColumn("&nbsp;");
    columnIcon.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.title"));
	arrayPane.addArrayColumn(resource.getString("GML.description"));
	ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("GML.operation"));
	columnOp.setSortable(false);
		
	// remplissage de l'ArrayPane avec les catégories
	Iterator it = (Iterator) categories.iterator();
	while (it.hasNext()) 
	{
		ArrayLine ligne = arrayPane.addArrayLine();
			
		IconPane icon = gef.getIconPane();
		Icon categoryIcon = icon.addIcon();
   		categoryIcon.setProperties(resource.getIcon("blog.blogSmall"), "");
      	icon.setSpacing("30px");
      	ligne.addArrayCellIconPane(icon);
			
		NodeDetail uneCategory = (NodeDetail) it.next();
		int id = uneCategory.getId();
		String nom = uneCategory.getName();
		ligne.addArrayCellText(uneCategory.getName());
		ligne.addArrayCellText(uneCategory.getDescription());
		// création de la colonne des icônes
		IconPane iconPane = gef.getIconPane();
		// icône "modifier"
		Icon updateIcon = iconPane.addIcon();
   		updateIcon.setProperties(resource.getIcon("blog.updateCategory"), resource.getString("blog.updateCategory"), "javaScript:editCategory('"+id+"')");
		// icône "supprimer"
		Icon deleteIcon = iconPane.addIcon();
		deleteIcon.setProperties(resource.getIcon("blog.deleteCategory"), resource.getString("blog.deleteCategory"), "javaScript:deleteConfirm('"+id+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(nom))+"')");
		iconPane.setSpacing("30px");
		ligne.addArrayCellIconPane(iconPane);
	}	
	out.println(arrayPane.print());
	
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="categoryForm" action="" Method="POST">
	<input type="hidden" name="CategoryId">
	<input type="hidden" name="Name">
	<input type="hidden" name="Description">
</form>
</div>
</body>
</html>