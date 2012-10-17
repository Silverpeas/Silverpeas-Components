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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<c:set var="listCategoryJSON" value="${requestScope.ListCategoryJSON}"/>
<% 
Collection<NodeDetail>	categories	= (Collection<NodeDetail>) request.getAttribute("Categories");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<view:includePlugin name="popup"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function addCategory() {
	// force empty fields
	$("#categoryManager #CategoryId").val('');
	$("#categoryManager #Name").val('');
	$("#categoryManager #Description").val('');
	
	document.categoryForm.action = "CreateCategory";
	
	var title = "<%=resource.getString("blog.addCategory")%>";
	showDialog(title);
}

function showDialog(title) {
	$("#categoryManager").popup({
	      title: title,
	      callback: function() {
	        if (isCorrectForm()) {
	        	sendData();
	        }
	        return isCorrect;
	      }
	    });
}

function editCategory(id) {
	var name = $("#categ-"+id+" .categ-title").text();
	var desc = $("#categ-"+id+" .categ-desc").text();
	
	$("#categoryManager #CategoryId").val(id);
	$("#categoryManager #Name").val(name);
	$("#categoryManager #Description").val(desc);
	
	document.categoryForm.action = "UpdateCategory";
	
	var title = "<%=resource.getString("blog.updateCategory")%>";
	showDialog(title);
}

function isCorrectForm() {
 	var errorMsg = "";
 	var errorNb = 0;
 	var name = stripInitialWhitespace($("#categoryManager #Name").val());

 	if (name == "") { 
		errorMsg+="  - '<%=resource.getString("GML.title")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
       	errorNb++;
 	}
	   				     			     				    
   	switch(errorNb) {
       	case 0 :
           	result = true;
           	break;
       	case 1 :
           	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
           	window.alert(errorMsg);
           	result = false;
           	break;
       	default :
           	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
           	window.alert(errorMsg);
           	result = false;
           	break;
     } 
    return result;
}

function sendData() {
	document.categoryForm.submit();
}
	
function deleteConfirm(id,nom) {
	if(window.confirm("<%=resource.getString("blog.confirmDeleteCategory")%> '" + nom + "' ?")) {
		document.categoryForm.action = "DeleteCategory";
		document.categoryForm.CategoryId.value = id;
		document.categoryForm.submit();
	}
}

var listNodeJSON = ${listCategoryJSON};

$(document).ready(function() {
    $('#categoryList tbody').bind('sortupdate', function(event, ui) {
        var updatedNode = new Array(); //tableau de NodeEntity réordonnés sérialisés en JSON
        var data = $('#categoryList tbody').sortable('toArray'); //tableau de valeurs categ-{nodeId} réordonnés
        for (var i=0; i<data.length; i++)
        {
          var nodeId = data[i]; //categ-{nodeId}
          nodeId = nodeId.substring(6); //{nodeId}
          
          for (var j=0; j<listNodeJSON.length; j++)
          {
            var NodeJSON = listNodeJSON[j];
            if(nodeId == NodeJSON.attr.id) {
              updatedNode[i] = NodeJSON;
            }
          }
        }
        sortNode(updatedNode);
      });
});
  
function sortNode(updatedNodeJSON)
{
    $.ajax({
        url:"<%=m_context%>/services/nodes/<%=instanceId%>",
        type: "PUT",
        contentType: "application/json",
        dataType: "json",
        cache: false,
        data: $.toJSON(updatedNodeJSON),
        success: function (data) {
        	listNodeJSON = data;
        }
        ,
        error: function(jqXHR, textStatus, errorThrown) {
          if (onError == null)
           alert(errorThrown);
          else
           onError({
             status: jqXHR.status,
             message: errorThrown
           });
        }
    });
}  
</script>
</head>
<body id="blog">
<div class="inlineMessage"><fmt:message key="blog.homePageMessage"/></div>
<br clear="all"/>
<div id="<%=instanceId %>">
<%
   	operationPane.addOperation(resource.getIcon("blog.addCategory"), resource.getString("blog.addCategory") , "javascript:onClick=addCategory()");

	out.println(window.printBefore());
    out.println(frame.printBefore());
    
	ArrayPane arrayPane = gef.getArrayPane("categoryList", "ViewCategory", request, session);
	arrayPane.setXHTML(true);
	arrayPane.setSortableLines(true);
	ArrayColumn columnIcon = arrayPane.addArrayColumn("&nbsp;");
    columnIcon.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.title"));
	arrayPane.addArrayColumn(resource.getString("GML.description"));
	ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("GML.operation"));
	columnOp.setSortable(false);
		
	// remplissage de l'ArrayPane avec les cat�gories
	for (NodeDetail uneCategory : categories) {
		ArrayLine ligne = arrayPane.addArrayLine();
		ligne.setId("categ-"+uneCategory.getId());
			
		IconPane icon = gef.getIconPane();
		Icon categoryIcon = icon.addIcon();
   		categoryIcon.setProperties(resource.getIcon("blog.blogSmall"), "");
      	icon.setSpacing("30");
      	ligne.addArrayCellIconPane(icon);
			
		int id = uneCategory.getId();
		String nom = uneCategory.getName();
		ArrayCell cell4Name = ligne.addArrayCellText(uneCategory.getName());
		cell4Name.setStyleSheet("categ-title");
		ArrayCell cell4Desc = ligne.addArrayCellText(uneCategory.getDescription());
		cell4Desc.setStyleSheet("categ-desc");
		
		IconPane iconPane = gef.getIconPane();
		Icon updateIcon = iconPane.addIcon();
   		updateIcon.setProperties(resource.getIcon("blog.updateCategory"), resource.getString("blog.updateCategory"), "javaScript:editCategory('"+id+"')");
		Icon deleteIcon = iconPane.addIcon();
		deleteIcon.setProperties(resource.getIcon("blog.deleteCategory"), resource.getString("blog.deleteCategory"), "javaScript:deleteConfirm('"+id+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(nom))+"')");
		iconPane.setSpacing("30");
		ligne.addArrayCellIconPane(iconPane);
	}	
	out.println(arrayPane.print());
	
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</div>

<div id="categoryManager" style="display: none;">
	<form name="categoryForm" action="CreateCategory" method="post">
		<table cellpadding="5" width="100%">
			<tr>
				<td class="txtlibform"><%=resource.getString("GML.title")%> :</td>
				<td><input type="text" name="Name" id="Name" size="60" maxlength="150" value=""/>
					<img src="<%=resource.getIcon("blog.obligatoire")%>" width="5" height="5" border="0" alt="<%=resource.getString("GML.requiredField") %>"/>
					<input type="hidden" name="CategoryId" id="CategoryId" value=""/></td>
			</tr>
			<tr>
				<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
				<td><input type="text" name="Description" id="Description" size="60" maxlength="150" value=""/></td>
			</tr>
			<tr><td colspan="2"><img border="0" src="<%=resource.getIcon("blog.obligatoire")%>" width="5" height="5" alt="<%=resource.getString("GML.requiredField") %>"/> : <%=resource.getString("GML.requiredField")%></td></tr>
		</table>
	</form>
</div>

</body>
</html>