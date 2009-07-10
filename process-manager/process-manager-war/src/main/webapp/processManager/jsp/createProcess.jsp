<%@ include file="checkProcessManager.jsp" %>

<%
   com.silverpeas.form.Form form
	   = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	  browseBar.setPath(resource.getString("processManager.createProcessBB"));

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
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/modal-message.css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script>
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
			displayStaticMessage();
	    	setTimeout("document.<%=context.getFormName()%>.submit();", 500);
		}
	}
	
	function B_ANNULER_ONCLICK() {
		location.href = "listProcess";
	}
	
//-->
</SCRIPT>
</HEAD>
<BODY class="yui-skin-sam">
<%
   out.println(window.printBefore());
	out.println(tabbedPane.print());
   out.println(frame.printBefore());
%>
<FORM NAME="<%=context.getFormName()%>" METHOD="POST" ACTION="saveCreation" ENCTYPE="multipart/form-data">
<CENTER>
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
<%@ include file="modalMessage.jsp.inc" %>
</BODY>