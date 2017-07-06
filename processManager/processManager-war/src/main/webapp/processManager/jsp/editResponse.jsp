<%@ page import="org.silverpeas.core.contribution.content.form.Form" %><%--

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

<%
	ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	Question question = (Question) request.getAttribute("question");

	Form questionForm = (Form) request.getAttribute("questionForm");
	Form responseForm = (Form) request.getAttribute("responseForm");

	PagesContext context = (PagesContext) request.getAttribute("context");

	DataRecord questionData = (DataRecord) request.getAttribute("questionData");
	DataRecord responseData = (DataRecord) request.getAttribute("responseData");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	browseBar.setPath(process.getTitle(currentRole, language));

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(
	   generalMessage.getString("GML.validate"),
		"javascript:onClick=B_VALIDER_ONCLICK();",
		false));
	buttonPane.addButton((Button) gef.getFormButton(
	   generalMessage.getString("GML.cancel"),
		"javascript:onClick=B_ANNULER_ONCLICK();",
		false));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>

<%
	responseForm.displayScripts(out, context);
%>
<script language="JavaScript">
<!--
	function B_VALIDER_ONCLICK()
	{
    ifCorrectFormExecute(function() {
			document.<%=context.getFormName()%>.submit();
		});
	}

	function B_ANNULER_ONCLICK() {
		location.href = "cancelResponse?state=<%=question.getTargetState().getName()%>&processId=<%=process.getInstanceId()%>&processManagertokenId=${currentTokenId}";
	}

//-->
</script>

</head>
<body class="yui-skin-sam">
<%
   out.println(window.printBefore());
   out.println(frame.printBefore());
%>

	<p class="txtnav">
		  <%= resource.getString("processManager.questionsToAnswer")+" "+ question.getFromUser().getFullName() %>
	</p>

<%
   questionForm.display(out, context, questionData);
%>



<form name="<%=context.getFormName()%>" method="post" action="saveResponse" enctype="multipart/form-data">
<input type="hidden" name="processManagertokenId" value="${currentTokenId}"/>


		         <p class="txtnav">
					   <%=resource.getString("processManager.yourAnswer") %>
		         </p>
				
<%
   responseForm.display(out, context, responseData);
%>

   <input type="hidden" name="questionId" value="<%=question.getId()%>" />
   <br />
<%
	out.println(buttonPane.print());
%>

</form>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</body>
