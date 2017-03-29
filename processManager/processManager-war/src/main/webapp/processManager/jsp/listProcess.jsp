<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="java.util.Map" %><%--

    Copyright (C) 2000 - 2017 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkProcessManager.jsp" %>
<%!
Item getItem(Item[] items, String itemName)
{
	Item item = null;
	for(int i=0; i<items.length; i++)
	{
		item = items[i];
		if (itemName.equals(item.getName()))
			return item;
	}
	return null;
}
%>
<%
  DataRecord[] processList = (DataRecord[])request.getAttribute("processList");
  if (processList == null) processList = new DataRecord[0];

  RecordTemplate listHeaders=(RecordTemplate) request.getAttribute("listHeaders");
  FieldTemplate[] headers = listHeaders.getFieldTemplates();

  Item[] items = (Item[]) request.getAttribute("FolderItems");

  String canCreate = (String) request.getAttribute("canCreate");
  if (canCreate.equals("1")) {
		operationPane.addOperationOfCreation(resource.getIcon("processManager.add"),
									resource.getString("processManager.createProcess"),
									"createProcess");
  }

  String hasUserSettings = (String) request.getAttribute("hasUserSettings");
  if (hasUserSettings.equals("1")) {
	operationPane.addOperation(resource.getIcon("processManager.userSettings"),
								resource.getString("processManager.userSettings"),
								"editUserSettings");
  }

  Boolean isCSVExportEnabled = (Boolean) request.getAttribute("isCSVExportEnabled");

  if (isCSVExportEnabled != null && isCSVExportEnabled.booleanValue()) {
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("processManager.csvExport"),
			resource.getString("processManager.csvExport"),
			"javaScript:exportCSV();");
  }

  if ("supervisor".equalsIgnoreCase((String)request.getAttribute("currentRole"))) {
    operationPane.addLine();
	operationPane.addOperation(resource.getIcon("processManager.welcome"),
			resource.getString("processManager.operation.welcome"),
			"ToWysiwygWelcome");
  }
  String welcomeMessage = (String) request.getAttribute("WelcomeMessage");

	String collapse = (String) request.getAttribute("collapse");
	if (collapse == null) {
		collapse = "true";
	}
	Form form = (Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

   ButtonPane buttonPane = gef.getButtonPane();
   buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:setFilter()", false));
   buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:resetFilter()", false));

   boolean isProcessIdVisible = ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();

   ArrayPane arrayPane = gef.getArrayPane("List", "listSomeProcess", request,session);
   arrayPane.setVisibleLineNumber(20);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<%
	if (collapse.equals("false")) {
		form.displayScripts(out, context);
	}
%>
<script type="text/javascript">
	function collapseFilter(arg){
		document.<%=context.getFormName()%>.collapse.value=arg;
		document.<%=context.getFormName()%>.submit();
	}

	function setFilter() {
    ifCorrectFormExecute(function() {
			document.<%=context.getFormName()%>.submit();
		});
	}

	function resetFilter() {
		document.<%=context.getFormName()%>.reset();
	}

	function confirmURL(url, message) {
		if (confirm(message)) {
			window.location.href = url;
		}
	}

	function exportCSV() {
		SP_openWindow("exportCSV", "exportWindow", "550", "350", "directories=0,menubar=0,toolbar=0,alwaysRaised");
	}
</script>
</head>
<body class="yui-skin-sam processManager-main">
<%
   out.println(window.printBefore());
%>
<view:frame>
<% if (roles != null && roles.length > 1) { %>
  <div id="roles">
<form name="roleChoice" method="post" action="changeRole">
		<span class="textePetitBold"><%=resource.getString("processManager.yourRole") %> :&nbsp;</span>
    <select name="role" onchange="document.roleChoice.submit()">
       <% for (int i=0; i<roles.length ; i++) { %>
         <option <%=currentRole.equals(roles[i].name)?"selected":""%> value="<%=roles[i].name%>"><%=roles[i].value%></option>
      <% } %>
    </select>
</form>
  </div>
<br/>
<% } %>

<% if (StringUtil.isDefined(welcomeMessage)) { %>
	<span class="inlineMessage"><%=welcomeMessage %></span>
<% } %>
<view:areaOfOperationOfCreation/>
<form id="filter" name="<%=context.getFormName()%>" method="post" action="filterProcess" enctype="multipart/form-data">
  <div class="bgDegradeGris">
        <div id="filterLabel">
          <p>
            <%if (collapse.equals("true")) { %>
            <a href="javascript:collapseFilter('false')"><img border="0" src="<%=resource.getIcon("processManager.boxDown")%>"/></a>
            <% } else { %>
            <a href="javascript:collapseFilter('true')"><img border="0" src="<%=resource.getIcon("processManager.boxUp")%>"/></a>
            <% } %>
            <span class="txtNav"><%=resource.getString("processManager.filter") %></span>
          </p>

          <div id="refresh"><a href="listProcess"><img border="0" src="<%=resource.getIcon("processManager.refresh")%>" alt="<%=resource.getString("processManager.refresh")%>" align="absmiddle"/></a></div>
        </div>
				<% if (collapse.equals("false")) { %>
        <div id="filterForm">
					<% form.display(out, context, data); %>
					<% out.println(buttonPane.print()); %>
        </div>
				<% } %>
	 <input type="hidden" name="collapse" value="<%=collapse%>"/>
  </div>
</form>
<%
	ArrayColumn arrayColumn;

	if (isProcessIdVisible) {
		arrayColumn = arrayPane.addArrayColumn("#");
	}

	arrayColumn = arrayPane.addArrayColumn("<>");

	for (int i=0; i<headers.length; i++) {
		arrayColumn = arrayPane.addArrayColumn(headers[i].getLabel(language));
		arrayColumn.setSortable(true);
	}

	if ("supervisor".equalsIgnoreCase(currentRole))	{
		ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
		arrayColumn0.setSortable(false);
	}

	// Les lignes
	ProcessInstanceRowRecord instance;
	for (int i=0; i<processList.length; i++) // boucle sur tous les process
	{
		instance = (ProcessInstanceRowRecord) processList[i];
		ArrayLine arrayLine = arrayPane.addArrayLine();
		ArrayCellText cellId = null;
		if (instance.isInError()) {
			if (isProcessIdVisible) {
				cellId = arrayLine.addArrayCellText(instance.getId());
			}
			arrayLine.addArrayCellText("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.inError") + "\" src=\""  + resource.getIcon("processManager.inError") + "\"/>");
			if ("supervisor".equalsIgnoreCase(currentRole)) {
				arrayLine.addArrayCellLink(instance.getField(0).getValue(language), "viewProcess?processId=" + instance.getId());
			} else {
				arrayLine.addArrayCellText(instance.getField(0).getValue(language));
			}
		} else if (instance.isLockedByAdmin()) {
			if (isProcessIdVisible) {
				cellId = arrayLine.addArrayCellText(instance.getId());
			}
			arrayLine.addArrayCellText("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.lockedByAdmin") + "\" src=\""  + resource.getIcon("processManager.locked") + "\"/>");
			if ("supervisor".equalsIgnoreCase(currentRole)) {
				arrayLine.addArrayCellLink(instance.getField(0).getValue(language), "viewProcess?processId=" + instance.getId());
			} else {
				arrayLine.addArrayCellText(instance.getField(0).getValue(language));
			}
		} else if (instance.isInTimeout()) {
			if (isProcessIdVisible) {
				cellId = arrayLine.addArrayCellText("<a href=\"viewProcess?processId="+instance.getId()+"\">"+instance.getId()+"</a>");
			}
			arrayLine.addArrayCellText("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.timeout") + "\" src=\""  + resource.getIcon("processManager.timeout") + "\"/>");
			arrayLine.addArrayCellLink(instance.getField(0).getValue(language), "viewProcess?processId=" + instance.getId());
		} else {
			if (isProcessIdVisible) {
				cellId = arrayLine.addArrayCellText("<a href=\"viewProcess?processId="+instance.getId()+"\">"+instance.getId()+"</a>");
			}
			arrayLine.addArrayCellText("");
			arrayLine.addArrayCellLink(instance.getField(0).getValue(language), "viewProcess?processId="+instance.getId());
		}
		if (isProcessIdVisible) {
			cellId.setCompareOn(new Integer(instance.getId()));
		}

		Field field = null;
		for (int j=1; j<headers.length; j++) {
			field = instance.getField(j);
			String fieldString = field.getValue(language);
			if ("null".equals(fieldString) || fieldString == null)
				fieldString = "";
			if (fieldString != null && fieldString.length() > 0 && field.getTypeName().equals(DateField.TYPE)) {
				ArrayCellText arrayCellDate = arrayLine.addArrayCellText(fieldString);
				arrayCellDate.setCompareOn(field.getValue());
			} else {
				String fieldName = headers[j].getFieldName();
				Item item = getItem(items, fieldName);
				if (item != null) {
					Map<String, String> keyValuePairs = item.getKeyValuePairs();
					if (keyValuePairs != null && keyValuePairs.size() > 0)
					{
						String newValue = "";
						if (fieldString != null && fieldString.indexOf("##") != -1)
						{
							//Try to display a checkbox list
							StringTokenizer tokenizer = new StringTokenizer(fieldString, "##");
							String t = null;
							while (tokenizer.hasMoreTokens())
							{
								t = tokenizer.nextToken();

								t = keyValuePairs.get(t);
								newValue += t;

								if (tokenizer.hasMoreTokens())
									newValue += ", ";
							}
						}
						else if (fieldString != null && fieldString.length() > 0)
						{
							newValue = keyValuePairs.get(fieldString);
						}
						fieldString = newValue;
					}
				}
				arrayLine.addArrayCellText(fieldString);
			}
		}

		if ("supervisor".equalsIgnoreCase(currentRole)) {
			arrayLine.addArrayCellLink("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.delete") + "\" src=\""  + resource.getIcon("processManager.small_remove") + "\">", "javascript:confirmURL('adminRemoveProcess?processId=" + processList[i].getId() + "', '"+ resource.getString("processManager.confirmDelete") +"')");
		}
	}
	out.println(arrayPane.print());
%>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>