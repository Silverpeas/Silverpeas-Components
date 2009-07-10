<%@ include file="checkProcessManager.jsp" %>

<%
   com.silverpeas.form.Form form = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel,"listProcess");
  browseBar.setPath(resource.getString("processManager.userSettingsBB"));
  
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
		location.href = "listProcess";
	}
	
//-->
</SCRIPT>

</HEAD>
<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
   out.println(window.printBefore());
   out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveUserSettings" ENCTYPE="multipart/form-data">
<CENTER>
	<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%">
	<tr>
		<td class="intfdcolor" nowrap width="100%">
			<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
			<span class="txtNav">
           <%=resource.getString("processManager.userSettingsHeader")%>
			</span>
		</td>
	</tr>
	</table>
<%
   form.display(out, context, data);
%>
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
