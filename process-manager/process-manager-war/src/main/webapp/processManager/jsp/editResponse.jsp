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

<%
	ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	Question question = (Question) request.getAttribute("question");

	com.silverpeas.form.Form questionForm = (com.silverpeas.form.Form) request.getAttribute("questionForm");
	com.silverpeas.form.Form responseForm = (com.silverpeas.form.Form) request.getAttribute("responseForm");

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

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<%
   out.println(gef.getLookStyleSheet());
	responseForm.displayScripts(out, context);
%>
<SCRIPT language="JavaScript">
<!--
	function B_VALIDER_ONCLICK()
	{
		if (isCorrectForm())
		{
			document.<%=context.getFormName()%>.submit();
		}
	}
	
	function B_ANNULER_ONCLICK() {
		location.href = "cancelResponse?state=<%=question.getTargetState().getName()%>&processId=<%=process.getInstanceId()%>";
	}
	
//-->
</SCRIPT>

</HEAD>
<BODY class="yui-skin-sam">
<%
   out.println(window.printBefore());
   out.println(frame.printBefore());
%>

<CENTER>
<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%">
<tr>
	<td class="intfdcolor" nowrap width="100%">
		<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
		<span class="txtNav">
			  <%= resource.getString("processManager.questionsToAnswer")+" "+ question.getFromUser().getFullName() %>
		</span>
	</td>
</tr>
</table>
<%
   questionForm.display(out, context, questionData);
%>
   <BR>
</CENTER>

<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveResponse" ENCTYPE="multipart/form-data">
<CENTER>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
				<tr>
					<td class="intfdcolor" rowspan="2" nowrap width="100%">
		         <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
		         <span class="txtNav">
					   <%=resource.getString("processManager.yourAnswer") %>
		         </span>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<%
   responseForm.display(out, context, responseData);
%>

   <INPUT type="hidden" name="questionId" value="<%=question.getId()%>">
   <BR>
<%
	out.println(buttonPane.print());
%>
</CENTER>
</FORM>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
