<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres

%>

<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function submitForm()
{
	if (document.updateClient.clientName.value=="")
	{
		alert("<%= resources.getString("crm.fieldNameRequired") %>");
	}
	else
	{
		document.updateClient.action = "ChangeClient";
		document.updateClient.submit();
	}
}

function cancelForm() {
    document.updateClient.action = "Main";
    document.updateClient.submit();
}
</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%
        out.println(window.printBefore());
        out.println(frame.printBefore());
%>
<center>
  <%=boardStart%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="updateClient" action="ChangeClient" method="post">
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.client")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="clientName" value="<%= (String)request.getAttribute("clientName") %>" size="25">
          &nbsp;&nbsp;
        </td>
      </tr>
  </table>
  <%=boardEnd%>

</center>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
<script type="text/javascript">
	document.updateClient.clientName.focus();
</script>