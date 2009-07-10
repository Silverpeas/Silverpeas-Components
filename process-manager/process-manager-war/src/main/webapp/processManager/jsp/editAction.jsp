<%@ include file="checkProcessManager.jsp" %>

<%
   ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	State state = (State) request.getAttribute("state");
	Action action = (Action) request.getAttribute("action");

   com.silverpeas.form.Form form
	   = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");
   
   boolean 		isProcessIdVisible 		= ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));

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
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/modal-message.css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>

<%
	if (form != null) form.displayScripts(out, context);
%>

<SCRIPT language="JavaScript">
	function B_VALIDER_ONCLICK()
	{
	<% if (form != null) { %>
		if (isCorrectForm())
		{
			displayStaticMessage();
	    	setTimeout("document.<%=context.getFormName()%>.submit();", 500);
		}
	<% } else { %>
		displayStaticMessage();
		setTimeout("document.<%=context.getFormName()%>.submit();", 500);
	<% } %>
	}
	
	function B_ANNULER_ONCLICK() {
		location.href = "cancelAction?state=<%=state.getName()%>";
	}
</SCRIPT>

</HEAD>
<BODY class="yui-skin-sam">
<%
   out.println(window.printBefore());
	out.println(tabbedPane.print());
   out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveAction" ENCTYPE="multipart/form-data">
<CENTER>
<%
   if (form != null) form.display(out, context, data);
	else
	{ %>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
         <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor4><tr><td>&nbsp;</td></tr><tr>
			   <td width="80%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
					<span class="textePetitBold">
	               <%=action.getLabel(currentRole, language)%>
			      </span>
				</td>
			</tr><tr><td>&nbsp;</td></tr></table>
		</td>
	</tr>	
</table>
<% } %>
   <INPUT type="hidden" name="state" value="<%=state.getName()%>">
   <INPUT type="hidden" name="action" value="<%=action.getName()%>">
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
<%@ include file="modalMessage.jsp.inc" %>
</BODY>