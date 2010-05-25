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

<%@ include file="checkProcessManager.jsp" %>

<%
   ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	String state = (String) request.getAttribute("state");
	String stepId = (String) request.getAttribute("stepId");
	HistoryStep   step  = (HistoryStep) request.getAttribute("step");

   com.silverpeas.form.Form form
	   = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

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
<script type="text/javascript" src="<%=m_context %>/util/javaScript/jquery/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
	form.displayScripts(out, context);
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
		location.href = "cancelAction?state=<%=state%>";
	}
	
//-->
</SCRIPT>

</HEAD>
<BODY class="yui-skin-sam">
<%
   out.println(window.printBefore());
   out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveQuestion" ENCTYPE="multipart/form-data">
<CENTER>
	<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%">
	<tr>
		<td class="intfdcolor" nowrap width="100%">
			<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5"><span class="txtNav">
				  <%= resource.getString("processManager.backTo")+" "+ step.getUser().getFullName()%>
			</span>
		</td>
	</tr>
	</table>
<%
   form.display(out, context, data);
%>

   <INPUT type="hidden" name="state" value="<%=state%>">
   <INPUT type="hidden" name="stepId" value="<%=stepId%>">
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
