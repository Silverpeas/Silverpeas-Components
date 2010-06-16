<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>

<%@page import="java.util.List"%>
<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.silverpeas.formsonline.model.FormInstance"%>

<%!
	String iconsPath 			= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String iconArchived			= iconsPath+"/util/icons/tofile.gif";
	String iconDelete			= iconsPath+"/util/icons/formManager_to_del.gif";
%>
<%
	String 					filteredState 	= request.getParameter("filteredState");
	DateFormat 				formatter 		= new SimpleDateFormat(resource.getString("GML.dateFormat"));
    List 					availableForms 	= (List) request.getAttribute("availableForms");
	FormDetail 				choosenForm 	= (FormDetail) request.getAttribute("choosenForm");
	List					formInstances	= (List) request.getAttribute("formInstances");
	String 					userBestProfile = (String) request.getAttribute("userBestProfile");
	OrganizationController 	controller		= new OrganizationController();
	UserDetail 				userDetail 		= (choosenForm == null) ? null : controller.getUserDetail(choosenForm.getCreatorId());

	filteredState = (filteredState == null) ? "" : filteredState;
%>  
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
	<%=gef.getLookStyleSheet()%>

	<script type="text/javascript">
	    function removeForm() {    
	         if (window.confirm("<%=resource.getString("formsOnline.deleteFormConfirm")%>")) { 
	            document.deleteForm.submit();
	         }
	    }

	    function filterOnState(stateValue) {
	    	document.refreshForm.filteredState.value = stateValue;
	    	document.refreshForm.submit();
	    }

	    function changeForm(newFormId) {
	    	document.refreshForm.formId.value = newFormId;
	    	document.refreshForm.submit();
	    }
	</script>
</head>

<body>

<form name="refreshForm" action="InBox" >
	<input type="hidden" name="filteredState" value="<%=filteredState%>"/>
	<input type="hidden" name="formId" value="<%=(choosenForm==null) ? "" : String.valueOf(choosenForm.getId())%>"/>
</form>

<form name="deleteForm" action="DeleteFormInstances" >

<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    
    TabbedPane tabbedPane = gef.getTabbedPane(1);
    if ( (userBestProfile != null) && (userBestProfile.equals("Administrator")) ) {
	    tabbedPane.addTab(resource.getString("formsOnline.formsList"), "Main", false,1);
    }
    tabbedPane.addTab(resource.getString("formsOnline.outbox"), "OutBox", false,1);
    tabbedPane.addTab(resource.getString("formsOnline.inbox"), "InBox", true,1);
    
    if (choosenForm != null) {
		operationPane.addOperation(iconDelete,resource.getString("formsOnline.removeFormInstance"), "javascript:removeForm()");
	}
%>    

	<%=window.printBefore()%>
	<%=tabbedPane.print()%>
	<%=frame.printBefore()%>
	<%=board.printBefore()%>

	<%
	if ( (availableForms != null) && (availableForms.size()>0) )
	{
	%>

	<table class="intfdcolor4" width="100%" cellspacing="0" cellpadding="5" border="0">
		<tr>
			<td>
				<span class="txtlibform"><%=resource.getString("formsOnline.Template")%> : </span>
			</td>
            <td>
            	<select size="1" name="modele" OnChange="changeForm(this.value)">
    			<%
				Iterator it = availableForms.iterator();	
				while (it.hasNext()) {
					FormDetail form = (FormDetail) it.next();
					boolean selected = ( choosenForm.getId() == form.getId() );
					%> 
					<option <%=(selected) ? "selected":""%> value="<%=form.getId()%>"><%=form.getName()%></option> 					
					<%
				}
				%>
				</select>
			</td>
		</tr>
	  	<tr>
			<td width="30%"><span class="txtlibform"><%=resource.getString("GML.description")%> : </span></td>
	        <td colspan="4"><span class="txtlibform"><%=choosenForm.getDescription()%></span></td>
		</tr>			
<!--  	<tr>
			<td width="30%"><span class="txtlibform"><%=resource.getString("GML.date")%> : </span></td>
	        <td colspan="4"><span class="txtlibform"><%=formatter.format(choosenForm.getCreationDate())%></span></td>
		</tr>			
	   	<tr>
			<td width="30%"><span class="txtlibform"><%=resource.getString("GML.publisher")%> : </span></td>
	        <td colspan="4"><span class="txtlibform"><%=userDetail.getDisplayedName()%></span></td>
		</tr>			
 -->
	</table>
	<%
	}
	else {
	%>
	<%=resource.getString("formsOnline.noAvailableFormReceived") %>
    <%
    }
    %>
    <%=board.printAfter()%>
    <br/>
	
<%
if ( choosenForm != null )
{
	ArrayPane arrayPane = gef.getArrayPane("myForms", "InBox?filteredState="+filteredState+"&formId="+choosenForm.getId(), request, session);
	arrayPane.setSortable(true);
	arrayPane.setVisibleLineNumber(10);
	ArrayColumn column = arrayPane.addArrayColumn(resource.getString("formsOnline.sendDate"));
	column.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("formsOnline.sender"));
	arrayPane.addArrayColumn(resource.getString("GML.status"));
	arrayPane.addArrayColumn("&nbsp;");

	Iterator itInstances = formInstances.iterator();
    while (itInstances.hasNext()) {
	    FormInstance instance = (FormInstance) itInstances.next();
	    UserDetail sender = controller.getUserDetail(instance.getCreatorId());

	    ArrayLine arrayLine = arrayPane.addArrayLine();
	    arrayLine.addArrayCellLink(formatter.format(instance.getCreationDate()), "ValidFormInstance?formInstanceId="+instance.getId());
	    arrayLine.addArrayCellText(sender.getDisplayedName());
	    
	    switch (instance.getState() ) {
	    	case FormInstance.STATE_READ:
	    		arrayLine.addArrayCellText(resource.getString("formsOnline.stateRead"));
	    		break;

	    	case FormInstance.STATE_VALIDATED:
	    		arrayLine.addArrayCellText(resource.getString("formsOnline.stateValidated"));
	    		break;

	    	case FormInstance.STATE_REFUSED:
	    		arrayLine.addArrayCellText(resource.getString("formsOnline.stateRefused"));
	    		break;

	    	case FormInstance.STATE_ARCHIVED:
	    		arrayLine.addArrayCellText(resource.getString("formsOnline.stateArchived"));
	    		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"suppInst\" value=\""+instance.getId()+"\">");
	    		break;

	    	default:
	    		arrayLine.addArrayCellText(resource.getString("formsOnline.stateUnread"));
	    		break;
	    }
    }
    %>
    <div align="right" style="padding-right: 10px; padding-bottom: 10px;">
	    <b><%=resource.getString("formsOnline.filterOnState")%> : </b>
	    <select name="stateFilter" onchange="filterOnState(this.value)">
	    	<option value=""><%=resource.getString("formsOnline.noFilter")%></option>
	    	<option <%=(String.valueOf(FormInstance.STATE_UNREAD).equals(filteredState)) ? "selected" : ""%> value="<%=FormInstance.STATE_UNREAD %>"><%=resource.getString("formsOnline.stateUnread")%></option>
	    	<option <%=(String.valueOf(FormInstance.STATE_READ).equals(filteredState)) ? "selected" : ""%> value="<%=FormInstance.STATE_READ %>"><%=resource.getString("formsOnline.stateRead")%></option>
	    	<option <%=(String.valueOf(FormInstance.STATE_VALIDATED).equals(filteredState)) ? "selected" : ""%> value="<%=FormInstance.STATE_VALIDATED %>"><%=resource.getString("formsOnline.stateValidated")%></option>
	    	<option <%=(String.valueOf(FormInstance.STATE_REFUSED).equals(filteredState)) ? "selected" : ""%> value="<%=FormInstance.STATE_REFUSED %>"><%=resource.getString("formsOnline.stateRefused")%></option>
	    	<option <%=(String.valueOf(FormInstance.STATE_ARCHIVED).equals(filteredState)) ? "selected" : ""%> value="<%=FormInstance.STATE_ARCHIVED %>"><%=resource.getString("formsOnline.stateArchived")%></option>
	    </select>
	</div>
    <%=arrayPane.print()%>
    <%
}
%>
    
    <%=frame.printAfter()%>
    <%=window.printAfter()%>
</form>   
</body>
</html>