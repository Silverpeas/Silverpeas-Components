<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
<%@ page import="org.silverpeas.core.contribution.template.publication.PublicationTemplate"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ include file="check.jsp" %>
<%
// Recuperation des details de l'utilsateur
  List listTemplates = (List) request.getAttribute("listTemplates");
  String name = "";
  String form = "";
  String description = "";
  boolean bookable = true;
  Long id = null;
  Category category = (Category) request.getAttribute("category");
  if (category != null) {
    id = category.getIdAsLong();
    name = category.getName();
    bookable = category.isBookable();
    form = category.getForm();
    description = category.getDescription();
  }
%>
<html>
<head>
<view:looknfeel/>
<script language=JavaScript>

function validerNom(){
	if(document.getElementById("name").value == 0)
		{
      document.getElementById('validationNom').innerHTML = "Nom obligatoire";
    }
	else
		{
      document.getElementById('validationNom').style.display = 'none';
    }
}

function verification(){
	if(document.getElementById("name").value == 0 )
		{
      jQuery.popup.error('<%=WebEncodeHelper.javaStringToJsString(resource.getString("resourcesManager.formulaireErreur")+" 1 "+ resource.getString("GML.error") +":"+ "\n" + "-" + "'" + resource.getString("GML.name")+ "'"+ " " + resource.getString("resourcesManager.renseigmentObligatoire"))%>'
    )
      ;
    }
	else
		{
      document.createForm.submit();
    }
}

</script>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel,"Main");
String chemin = "<a href=\"ViewCategories\">" + WebEncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.categorie"))+"</a>";
browseBar.setPath(chemin);
browseBar.setExtraInformation(resource.getString("resourcesManager.creercategorie"));

Board	board		 = gef.getBoard();
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());

//creation des boutons Valider et Annuler
ButtonPane buttonPane = gef.getButtonPane();
Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javaScript:verification()", false);
Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "ViewCategories",false);
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);
%>

<TABLE width="100%" cellpadding="3" border="0">

	<form NAME="createForm" method="post" action="<% if(category == null){ %>SaveCategory<%}else{%>ModifyCategory<%}%>">
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.name")%> : </TD>
		<TD width="100%"><input type="text" name="name" size="60" maxlength="50" id="name" onChange="validerNom()" value="<%=name%>" ><input type="hidden" name="responsible" value="0"/>&nbsp;<span id="validationNom" style="color:red"></span><IMG src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5" border="0"></TD>
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.description")%> : </TD>
		<TD><textarea name="description" rows="6" cols="57" ><%=description%></textarea></TD>
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><%=resource.getString("resourcesManager.reservable")%> : </TD>
		<TD><input type="checkbox" name="bookable" id="bookable" <% if((category != null) && (bookable)){out.println("checked="+"checked");}else{out.println("");}%> /> <label for="bookable"></label>&nbsp;</TD>
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><%=resource.getString("resourcesManager.formulaire")%> : </TD>
		<td>
		<select name="form">
		<%if (listTemplates != null)
		{
			%><option value=""><%=resource.getString("resourcesManager.listTemplate")%></option>
			  <option value="">-----------------</option>
			<% for(int i = 0;i<listTemplates.size();i++)
			{
				PublicationTemplate xmlForm = (PublicationTemplate) listTemplates.get(i);
				// Check if model is in the used model list
			%>
			<option value="<%=xmlForm.getFileName()%>" <%if((form != null)&& (form.equals(xmlForm.getFileName()))){%>selected="selected"<%}%>><%=xmlForm.getName()%></option>
			<%
			}
		}
		%>
		</select>
		</td>
	</tr>
	<tr>
		<td colspan="2">( <img border="0" src="<%=resource.getIcon("resourcesManager.obligatoire")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%> )</td>
	</tr>
	<%if (category != null) { %>
    <input type="HIDDEN" name="id" value="<%=id%>"/>
	<%}%>
</TABLE>
<%
out.println(board.printAfter());
out.println("<BR><center>"+buttonPane.print()+"</center><BR/>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<SCRIPT>document.createForm.name.focus();</SCRIPT>
</body>
</html>