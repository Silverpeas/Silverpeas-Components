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

  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel,"listProcess");

  String canCreate = (String) request.getAttribute("canCreate");
  if (canCreate.equals("1"))
		operationPane.addOperation(resource.getIcon("processManager.add"),
									resource.getString("processManager.createProcess"),
									"createProcess");	

  String hasUserSettings = (String) request.getAttribute("hasUserSettings");
  if (hasUserSettings.equals("1"))
	operationPane.addOperation(resource.getIcon("processManager.userSettings"),
								resource.getString("processManager.userSettings"),
								"editUserSettings");
  
  Boolean isCSVExportEnabled = (Boolean) request.getAttribute("isCSVExportEnabled");
  
  if (isCSVExportEnabled != null && isCSVExportEnabled.booleanValue())
  {
  	operationPane.addLine();
  	operationPane.addOperation(resource.getIcon("processManager.csvExport"),
			resource.getString("processManager.csvExport"),
			"javaScript:exportCSV();");
  }
  
	String collapse = (String) request.getAttribute("collapse");
	if (collapse == null) 
		collapse = "true";
   	com.silverpeas.form.Form form = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

   ButtonPane buttonPane = gef.getButtonPane();
   buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:setFilter()", false));
   buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:resetFilter()", false));
   
   boolean isProcessIdVisible = ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();

   ArrayPane arrayPane = gef.getArrayPane("List", "listSomeProcess", request,session);
   arrayPane.setVisibleLineNumber(20);
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<%
   out.println(gef.getLookStyleSheet());
	form.displayScripts(out, context);
%>
<script language="JavaScript1.2">
   function collapseFilter(arg){
	   document.<%=context.getFormName()%>.collapse.value=arg;
		setFilter();
   }
	function setFilter()
	{
		if (isCorrectForm())
		{
			document.<%=context.getFormName()%>.submit();
		}
	}
	
	function resetFilter() {
			document.<%=context.getFormName()%>.reset();
	}

	function confirmURL(url, message)
	{
		if (confirm(message))
			window.location.href = url;
	}

	function exportCSV()
	{
		SP_openWindow("exportCSV", "exportWindow", "550", "350", "directories=0,menubar=0,toolbar=0,alwaysRaised");
	}
</SCRIPT>
</script>
</HEAD>
<BODY class="yui-skin-sam">
<%
   out.println(window.printBefore());
   out.println(frame.printBefore());
%>
<CENTER>
<TABLE CELLPADDING="1" CELLSPACING="0" BORDER="0" WIDTH="98%">
	<TR>
		<TD class="textePetitBold" nowrap><%=resource.getString("processManager.yourRole") %> :&nbsp;</td>
		<td>
			<table cellpadding="2" cellspacing="1" border="0" width="100%" bgcolor="000000">
				<tr>
					<form name="roleChoice" method="POST" action="changeRole">
						<td class="intfdcolor" align="center" nowrap width="100%" height="24"> 
							<select name="role" onChange="document.roleChoice.submit()">
							   <% for (int i=0; i<roles.length ; i++) { %>
								   <option <%=currentRole.equals(roles[i].name)?"selected":""%> 	value="<%=roles[i].name%>"><%=roles[i].value%></option>
								<% } %>
							</select>
						</td>
					</form>
				</tr>
			</table>
		</td>
		<td width="100%"> 
		&nbsp;
		</td>
	</tr>
</table>
<br/>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="filterProcess" ENCTYPE="multipart/form-data">
	<% out.println(board.printBefore()); %>
			<table CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
				<tr>
					<td class="intfdcolor" rowspan="2" nowrap width="100%">
						<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
						<a href="listProcess"><img border="0" src="<%=resource.getIcon("processManager.refresh")%>" alt="<%=resource.getString("processManager.refresh")%>" align="absmiddle"></a>
						<span class="txtNav">
						<%=resource.getString("processManager.filter") %>
						</span>
					</td>
					<td class="intfdcolor"><img border="0" height="10" src="<%=resource.getIcon("processManager.px") %>"></td>
					<td class="intfdcolor"><img border="0" height="10" src="<%=resource.getIcon("processManager.px") %>"></td>
				</tr>
				<tr>
					<td height="0" class="intfdcolor" align="right" valign="bottom"><img border="0" src="<%=resource.getIcon("processManager.boxAngleLeft") %>"></td>
					<td align="center" valign="bottom" nowrap>
					<%if (collapse.equals("true")) {
					out.println("<a href=\"javascript:collapseFilter('false')\"><img border=\"0\" src=\""+resource.getIcon("processManager.boxDown")+"\"></a>");
					}
					else{
					out.println("<a href=\"javascript:collapseFilter('true')\"><img border=\"0\" src=\""+resource.getIcon("processManager.boxUp")+"\"></a>");
					}
					%>
					<img border="0" height="1" width="3" src="<%=resource.getIcon("processManager.px") %>">
					</td>
				</tr>
			</table>
				<%
				if (collapse.equals("false")) 
				{
				%>
					<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
						<tr>
							<td>
						      <br><center><% form.display(out, context, data); %></center>	
									<br><center><% out.println(buttonPane.print()); %></center>				
							</td>
						</tr>
						<!-- <tr>
							<td colspan="2" align="right"><a href="javascript:collapseFilter('true')"><img border="0" src="<%=resource.getIcon("processManager.boxUp") %>"></a><img border="0" width="3" src="<%=resource.getIcon("processManager.px") %>"></td>
						</tr> -->
					</table>
				<%
				}
				else
				{
				%>
					<table border="0" cellpadding="0" cellspacing="0"><tr><td class="intfdcolor4"></td><img border="0" src="<%=resource.getIcon("processManager.px") %>"></tr></table>
				<%
				}
				%>
	<% out.println(board.printAfter()); %>
   <INPUT type="hidden" name="collapse" value="<%=collapse%>">
</FORM>	
<%
	ArrayColumn arrayColumn;

	if (isProcessIdVisible)
		arrayColumn = arrayPane.addArrayColumn("#");

	arrayColumn = arrayPane.addArrayColumn("<>");
	
	for (int i=0; i<headers.length; i++)
	{
		arrayColumn = arrayPane.addArrayColumn(headers[i].getLabel(language));
		arrayColumn.setSortable(true);
	}
	
	if ("supervisor".equalsIgnoreCase(currentRole))
	{
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
		if (instance.isInError())
		{
			if (isProcessIdVisible)
				cellId = arrayLine.addArrayCellText(instance.getId());
			arrayLine.addArrayCellText("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.inError") + "\" src=\""  + resource.getIcon("processManager.inError") + "\">");
			arrayLine.addArrayCellText(instance.getField(0).getValue(language));
		}
		else if (instance.isLockedByAdmin())
		{
			if (isProcessIdVisible)
				cellId = arrayLine.addArrayCellText(instance.getId());
			arrayLine.addArrayCellText("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.lockedByAdmin") + "\" src=\""  + resource.getIcon("processManager.locked") + "\">");
			arrayLine.addArrayCellText(instance.getField(0).getValue(language));
		}
		
		else if (instance.isInTimeout())
		{
			if (isProcessIdVisible)
				cellId = arrayLine.addArrayCellText("<a href=\"viewProcess?processId="+instance.getId()+"\">"+instance.getId()+"</a>");
			arrayLine.addArrayCellText("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.timeout") + "\" src=\""  + resource.getIcon("processManager.timeout") + "\">");
			arrayLine.addArrayCellLink(instance.getField(0).getValue(language), "viewProcess?processId=" + instance.getId());
		}
		else
		{
			if (isProcessIdVisible)
				cellId = arrayLine.addArrayCellText("<a href=\"viewProcess?processId="+instance.getId()+"\">"+instance.getId()+"</a>");
			arrayLine.addArrayCellText("");
			arrayLine.addArrayCellLink(instance.getField(0).getValue(language), "viewProcess?processId="+instance.getId());
		}
		if (isProcessIdVisible)
			cellId.setCompareOn(new Integer(instance.getId()));

		Field field = null;
		for (int j=1; j<headers.length; j++)
		{
			field = instance.getField(j);
			String fieldString = field.getValue(language);
			if (fieldString != null && fieldString.length() > 0 && field.getTypeName().equals(DateField.TYPE))
			{
				ArrayCellText arrayCellDate = arrayLine.addArrayCellText(fieldString);
				arrayCellDate.setCompareOn(field.getValue());
			}
			else
			{
				String fieldName = headers[j].getFieldName();
				Item item = getItem(items, fieldName);
				if (item != null)
				{
					Hashtable keyValuePairs = item.getKeyValuePairs();
					if (keyValuePairs != null && keyValuePairs.size() > 0)
					{
						String newValue = "";
						if (fieldString.indexOf("##") != -1)
						{
							//Try to display a checkbox list
							StringTokenizer tokenizer = new StringTokenizer(fieldString, "##");
							String t = null;
							while (tokenizer.hasMoreTokens())
							{
								t = tokenizer.nextToken();
								
								t = (String) keyValuePairs.get(t);
								newValue += t;
								
								if (tokenizer.hasMoreTokens())
									newValue += ", ";
							}
						}
						else if (fieldString != null && fieldString.length() > 0)
						{
							newValue = (String) keyValuePairs.get(fieldString);
						}
						fieldString = newValue;
					}
				}
				arrayLine.addArrayCellText(fieldString);
			}
		}
		
		if ("supervisor".equalsIgnoreCase(currentRole))
		{
			arrayLine.addArrayCellLink("<img border=\"0\" width=\"15\" height=\"15\" alt=\"" + resource.getString("processManager.delete") + "\" src=\""  + resource.getIcon("processManager.small_remove") + "\">", "javascript:confirmURL('adminRemoveProcess?processId=" + processList[i].getId() + "', '"+ resource.getString("processManager.confirmDelete") +"')");
		}
	}
	out.println(arrayPane.print());
%>
</CENTER>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
