<%@ include file="../checkProcessManager.jsp" %>

<%
	ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	com.silverpeas.form.Form form = (com.silverpeas.form.Form) request.getAttribute("form");
	PagesContext context = (PagesContext) request.getAttribute("context");
	DataRecord data = (DataRecord) request.getAttribute("data");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"adminListProcess");
	browseBar.setPath(process.getTitle(currentRole, language));

	tabbedPane.addTab(resource.getString("processManager.details"), "adminViewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	tabbedPane.addTab(resource.getString("processManager.history"), "", true, false);

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
		location.href = "adminViewProcess";
	}
	
//-->
</SCRIPT>
</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="adminDoReAssign" ENCTYPE="multipart/form-data">
<CENTER>
<%
   form.display(out, context, data);
%>
   <BR>
<%
	out.println(buttonPane.print());
%>
</CENTER>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
