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
