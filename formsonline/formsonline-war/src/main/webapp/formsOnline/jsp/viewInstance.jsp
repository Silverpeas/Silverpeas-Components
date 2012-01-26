<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.formsonline.model.FormInstance"%>
<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@page import="com.silverpeas.util.StringUtil"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>


<%
	Form formView    = (Form) request.getAttribute("Form");
	DataRecord data    = (DataRecord) request.getAttribute("Data"); 
	String xmlFormName = (String) request.getAttribute("XMLFormName");
	String validationMode = (String) request.getAttribute("validationMode");
	FormInstance currentFormInstance = (FormInstance) request.getAttribute("currentFormInstance");
	String backFunction = (String) request.getAttribute("backFunction");
	String title = (String) request.getAttribute("title");
	String titleClassName = resource.getSetting("titleClassName");
	boolean backButtonAdded = false;
	
	// context creation
	PagesContext context = (PagesContext) request.getAttribute("FormContext");
	context.setFormName("newInstanceForm");
	context.setFormIndex("0");
	context.setBorderPrinted(false);
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel />
	<% formView.displayScripts(out, context); %>
	
	<script type="text/javascript">
		function validate() {
			document.validationForm.decision.value = "validate";
			document.validationForm.submit();
		}

		function refuse() {
			document.validationForm.decision.value = "refuse";
			document.validationForm.submit();
		}
	</script>
</head>
<body>

	<%=window.printBefore()%>
	<%=board.printBefore()%>

	<span class="<%=titleClassName%>"><%=title%></span>
	<form>	
	<% 
	formView.display(out, context, data); 
	%>
	</form>
	
    <%=board.printAfter()%>

	<%
	if (validationMode.equals("active")) {
		Frame validationFrame = gef.getFrame();
		Board validationBoard = gef.getBoard();
	%>
		<br/>
	    <%=validationFrame.printBefore()%>
	    <%=validationBoard.printBefore()%>
		<form name="validationForm" action="EffectiveValideForm" method="post">
			<input type="hidden" name="formInstanceId" value="<%=currentFormInstance.getId()%>"/>
			<input type="hidden" name="decision" value=""/>
			<%=resource.getString("formsOnline.receiverComments")%><br/><textarea name="comment" rows="8" cols="80" <%=(currentFormInstance.getState() != FormInstance.STATE_READ) ? "disabled" : "" %>><%=(currentFormInstance.getComments() == null)? "" : currentFormInstance.getComments()%></textarea><br/>
		</form>
	    <%=validationBoard.printAfter()%>
	    <% if (currentFormInstance.getState() == FormInstance.STATE_READ) {
			Button validate = (Button) gef.getFormButton(resource.getString("formsOnline.validateFormInstance"), "javascript:validate()", false);
			Button refuse = (Button) gef.getFormButton(resource.getString("formsOnline.refuseFormInstance"), "javascript:refuse()", false);
			Button back = (Button) gef.getFormButton(resource.getString("GML.back"), backFunction, false);
			ButtonPane validationButtonPane = gef.getButtonPane();
			validationButtonPane.addButton(validate);
			validationButtonPane.addButton(refuse);
			validationButtonPane.addButton(back);
			backButtonAdded = true;
		%>
			<br/>
			<center><%=validationButtonPane.print()%></center>
		<% 
	    	}	
	}
	%>
	
	<% if (!backButtonAdded) {
	Button back = (Button) gef.getFormButton(resource.getString("GML.back"), backFunction, false);
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(back);
	 %>
	<br/>
	<center>
	<%=buttonPane.print()%>
	</center>
	<% } %>
  	<%=window.printAfter()%>  

</body>
</html>