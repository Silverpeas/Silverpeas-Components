<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
String contactId = (String) request.getAttribute("contactId");
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
	if (document.newContact.contactName.value=="")
	{
		alert("<%= resources.getString("crm.fieldNameRequired") %>");
	}
	else if (document.newContact.contactFunction.value=="")
	{
		alert("<%= resources.getString("crm.fieldFunctionRequired") %>");
	}
	else
	{
		document.newContact.action = "ChangeContact";
		document.newContact.submit();
	}
}

function cancelForm() {
    document.newContact.action = "ViewClient";
    document.newContact.submit();
}
</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%


        out.println(window.printBefore());
        if ((contactId != null) && (contactId.length() > 0))
        {
            TabbedPane tabbedPane = gef.getTabbedPane();
            tabbedPane.addTab("Header",myURL + "NewContact?contactId=" + contactId,true);
            tabbedPane.addTab("Attachments","attachmentManager.jsp?elmtId=" + contactId + "&elmtType=CONTACT&returnAction=NewContact&returnId=contactId",false);
            out.println(tabbedPane.print());
        }
        out.println(frame.printBefore());
%>
<center>
  <%=boardStart%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="newContact" action="AddContact" method="post">
    <input type="hidden" name="contactId" value="<%=contactId%>">
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.nom")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="contactName" size=40 value="<%= (String)request.getAttribute("contactName") %>" >&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.fonction")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="contactFunction" size=40 value="<%= (String)request.getAttribute("contactFunction") %>" >&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.tel")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="contactTel" size=20 value="<%= (String)request.getAttribute("contactTel") %>" size="25">
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.email")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="contactEmail" size=40 value="<%= (String)request.getAttribute("contactEmail") %>" size="25">
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.adresse")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="contactAddress" size=40 value="<%= (String)request.getAttribute("contactAddress") %>" size="25">
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.actif")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="checkbox" name="contactActif" value="<%= (String)request.getAttribute("contactActif") %>" <%= (String)request.getAttribute("checked") %> size="25" >
          &nbsp;&nbsp;
        </td>
      </tr>
				<tr align=center>
					<td class="intfdcolor4" align=left colspan=2><span class="txt">(<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%>)</span>
					</td>
				</tr>
  </table>
  <%=boardEnd%>

<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
</center>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
<script type="text/javascript">
	document.newContact.contactName.focus();
</script>