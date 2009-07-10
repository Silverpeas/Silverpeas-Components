<%@ include file="checkProcessManager.jsp" %>

<%
   com.silverpeas.form.Form form
	   = (com.silverpeas.form.Form) request.getAttribute("form");
   PagesContext context = (PagesContext) request.getAttribute("context");
   DataRecord data = (DataRecord) request.getAttribute("data");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>

<SCRIPT Language="Javascript">

function printProcess() 
{
	window.focus();
	window.print();
}

</SCRIPT>

</HEAD>
<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%   form.display(out, context, data); %>
</BODY>
