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
<%@ page import="org.silverpeas.resourcemanager.model.Resource"%>
<%@ page import="java.util.List" %>

<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>
<% 
	String 			idcategory 	= (String) request.getAttribute("categoryId");
	List 			list 		= (List) request.getAttribute("listCategories");
	Resource details 	= (Resource) request.getAttribute("resource");
	List<UserDetail> managers  = (List<UserDetail>) request.getAttribute("Managers");
	
	Form 			formUpdate  = (Form) request.getAttribute("Form");
	DataRecord 		data    	= (DataRecord) request.getAttribute("Data"); 
	String 			xmlFormName = (String) request.getAttribute("XMLFormName");
	
	String managerIds = "";
	
	PagesContext  context = null;
	if (formUpdate != null)
	{
		context = new PagesContext("createForm", "0", resourcesManagerSC.getLanguage(), false, componentId, resourcesManagerSC.getUserId());
		
		if (details == null)
			context.setCurrentFieldIndex("5");
		else
			context.setCurrentFieldIndex("6");
	    context.setBorderPrinted(false);
	}
	
	String name = "";
	String description = "";
	boolean bookable = false;
	String resourceId = "";
	
	if (details != null){
		resourceId 		= details.getId();
		name 			= details.getName();
		bookable 		= details.isBookable();
		description 	= details.getDescription();
	}
	
	//creation des boutons Valider et Annuler
	Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javaScript:verification()", false);
	Button cancelButton = null;
	if(!"noCategory".equals(idcategory))
		cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "ViewResources?id="+idcategory,false);
	else
		cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "ViewCategories",false);
	
	%>
<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<%
	if (formUpdate != null)
	{
		//affichage du formulaire pour la saisie
		formUpdate.displayScripts(out, context);
	}
%>
<script language=JavaScript>
function isCorrectResourceForm() 
{
	var errorNb = 0;
	var errorMsg = "";
	if(document.getElementById("SPRM_name").value == 0){
		errorNb++;
		errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
	}
	switch(errorNb) 
 	{
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

function verification(){
	var xmlFormCorrect = false;
	<% if (formUpdate != null) { %>
			xmlFormCorrect = isCorrectForm();
	<% } else { %>
			xmlFormCorrect = true;
	<% } %>
	
	if (isCorrectResourceForm() && xmlFormCorrect){
		document.createForm.submit();
	}
}

function selectManagers()
{
  var url = "ToSelectManagers?ManagerIds=" + document.getElementById("managerIds").value;
  var name = "SelectUser";
  SP_openWindow(url, name, '550', '500','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</head>
<body class="yui-skin-sam">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel,"Main");
if (details == null)
	browseBar.setPath(resource.getString("resourcesManager.creerressource"));	
else
	browseBar.setPath(resource.getString("resourcesManager.modifierresource"));

operationPane.addOperation(resource.getIcon("resourcesManager.userPanel"), resource.getString("resourcesManager.SelectManagers"), "javascript:selectManagers()");

Board	board		 = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());

ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);

%>
<form NAME="createForm" method="post" enctype="multipart/form-data" action="<% if(details == null){ %>SaveResource<%}else{%>ModifyResource<%}%>">
<TABLE ALIGN="CENTER" CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.nomcategorie"));%> : </TD>
		<TD width="100%">
		<%for(int i=0;i< list.size();i++){
			Category category = (Category)list.get(i);
			String categoryId = category.getId();
		    String nameCategory = category.getName();
			if (categoryId.equals(idcategory))
			{
				%>
					<input type="hidden" name="SPRM_categoryChoice" value="<%=idcategory%>"/><%=nameCategory %>
				<%
			}
			%>
		<%} %>
		</TD>
	</tr>
	
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.name"));%> : </TD>
		<TD><input type="text" name="SPRM_name" size="60" maxlength="60" id="SPRM_name" value="<%=name%>" >&nbsp;<span id="validationNom" style="color:red"></span><IMG src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5" border="0"></TD>	
	</tr>
	
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.description"));%> : </TD>
		<TD><textarea name="SPRM_description" rows="5" cols="57" ><%=description%></textarea></TD>
	</tr>
	
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.reservable"));%> : </TD>
		<TD><input type="checkbox" name="SPRM_bookable" id="bookable" <% if((details != null) && (bookable)){out.println("checked="+"checked");}else{out.println("");}%> /> <label for="bookable"></label>&nbsp;</TD>
	</tr>

	<tr>
	   <TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.responsable"));%> : </TD>
	   
	   <TD id="managers"> 
			<%
		    StringBuilder managerNames = new StringBuilder("");
			  if (managers != null && ! managers.isEmpty()) {
			    for(UserDetail manager : managers) {
			      managerIds += manager.getId()+ ","; %>
			      <view:username userId="<%=manager.getId()%>"/><br/>
			      <%
			    }
			  } %>
			  <%=managerNames %>
      </TD>
      <input type="hidden" name="managerIds" id="managerIds" value="<%=managerIds %>"/>
  </tr>
	
	<tr>
		<td colspan="2">( <img border="0" src=<%=resource.getIcon("resourcesManager.obligatoire")%> width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</td>
	</tr>
		<!-- <input type="HIDDEN" name="idcategory" value=<%=idcategory%> > -->
		<%if (details != null){ %>
			<input type="hidden" name="SPRM_resourceId" value="<%=resourceId%>"/>
		<%}%>
			
</TABLE>
<SCRIPT>document.createForm.name.focus();</SCRIPT>
<%
out.println(board.printAfter());
%>
<br/>
<%
if (formUpdate != null)
{
	out.println(board.printBefore());
	formUpdate.display(out, context, data); 
	out.println(board.printAfter());
}
%>
</form>
<%
out.println("<BR/><center>"+buttonPane.print()+"</center><BR>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>