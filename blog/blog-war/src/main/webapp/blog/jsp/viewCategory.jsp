<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %><%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ include file="check.jsp" %>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message key="blog.viewCategory" var="viewCategoryLabel"/>
<fmt:message key="blog.addCategory" var="addCategoryLabel"/>
<fmt:message key="blog.addCategory" var="addCategoryIcon" bundle="${icons}"/>
<c:url var="addCategoryIconUrl" value="${addCategoryIcon}"/>

<c:set var="instanceId" value="<%=instanceId%>"/>

<% 
Collection<NodeDetail>	categories	= (Collection<NodeDetail>) request.getAttribute("Categories");
%>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
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
	        return sendData();
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

function sendData() {
 	var errorMsg = "";
 	var errorNb = 0;
 	var name = stripInitialWhitespace($("#categoryManager #Name").val());

 	if (name == "") { 
		errorMsg+="  - '<%=resource.getString("GML.title")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
       	errorNb++;
 	}
	   				     			     				    
   	switch(errorNb) {
       	case 0 :
            document.categoryForm.submit();
           	break;
       	case 1 :
           	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            jQuery.popup.error(errorMsg);
           	break;
       	default :
           	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
     }
     return false;
}
	
function deleteConfirm(id,nom) {
  var label = "<%=resource.getString("blog.confirmDeleteCategory")%> '" + nom + "' ?";
  jQuery.popup.confirm(label, function() {
		document.categoryForm.action = "DeleteCategory";
		document.categoryForm.CategoryId.value = id;
		document.categoryForm.submit();
	});
}

var listNodeJSON = ${requestScope.ListCategoryJSON};

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
        url: webContext + "/services/nodes/${instanceId}",
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
          if (window.onError) {
            window.onError({
              status : jqXHR.status, message : errorThrown
            });
          } else {
            notyError(errorThrown);
          }
        }
    });
}
 
</script>
</view:sp-head-part>
<view:sp-body-part id="blog">
  <view:browseBar componentId="${instanceId}" path="${viewCategoryLabel}"/>
  <view:operationPane>
    <view:operationOfCreation action="javascript:onClick=addCategory()" icon="${addCategoryIconUrl}" altText="${addCategoryLabel}"/>
  </view:operationPane>
<div id="${instanceId}">
  <view:window>
    <view:frame>
      <view:areaOfOperationOfCreation/>
<div class="inlineMessage"><fmt:message key="blog.homePageMessage"/></div>
<br />
<%  
	ArrayPane arrayPane = gef.getArrayPane("categoryList", "ViewCategory", request, session);
	arrayPane.setSortable(false);
	arrayPane.setMovableLines(true);
	arrayPane.addArrayColumn("&nbsp;");
	arrayPane.addArrayColumn(resource.getString("GML.title"));
	arrayPane.addArrayColumn(resource.getString("GML.description"));
	arrayPane.addArrayColumn(resource.getString("GML.operation"));
		
	// remplissage de l'ArrayPane avec les cat�gories
	for (NodeDetail uneCategory : categories) {
		ArrayLine ligne = arrayPane.addArrayLine();
		ligne.setId("categ-"+uneCategory.getId());
			
		IconPane icon = gef.getIconPane();
		Icon categoryIcon = icon.addIcon();
   		categoryIcon.setProperties(resource.getIcon("blog.blogSmall"), "");
      	icon.setSpacing("30");
      	ligne.addArrayCellIconPane(icon);
			
		String id = uneCategory.getId();
		String nom = uneCategory.getName();
		ArrayCell cell4Name = ligne.addArrayCellText(uneCategory.getName());
		cell4Name.setStyleSheet("categ-title");
		ArrayCell cell4Desc = ligne.addArrayCellText(uneCategory.getDescription());
		cell4Desc.setStyleSheet("categ-desc");
		
		IconPane iconPane = gef.getIconPane();
		Icon updateIcon = iconPane.addIcon();
   		updateIcon.setProperties(resource.getIcon("blog.updateCategory"), resource.getString("blog.updateCategory"), "javaScript:editCategory('"+id+"')");
		Icon deleteIcon = iconPane.addIcon();
		deleteIcon.setProperties(resource.getIcon("blog.deleteCategory"), resource.getString("blog.deleteCategory"), "javaScript:deleteConfirm('"+id+"','"+WebEncodeHelper.javaStringToHtmlString(
        WebEncodeHelper.javaStringToJsString(nom))+"')");
		iconPane.setSpacing("30");
		ligne.addArrayCellIconPane(iconPane);
	}	
	out.println(arrayPane.print());
%>
    </view:frame>
  </view:window>
</div>

<div id="categoryManager" style="display: none;">
	<form name="categoryForm" action="CreateCategory" method="post">
		<table cellpadding="5" width="100%">
			<caption></caption>
			<th id="categoryFormHeader"></th>
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

</view:sp-body-part>
</view:sp-page>