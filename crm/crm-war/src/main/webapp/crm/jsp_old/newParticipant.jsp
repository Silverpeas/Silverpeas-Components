<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
Iterator   iter1 = null;
Collection cFunction = (Collection)request.getAttribute("Functions");
String participantId = (String) request.getAttribute("participantId");
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

function openSPWindow(fonction,windowName)
{
	fonction = fonction + "?participantId=" + newParticipant.participantId.value;
	fonction = fonction + "&participantFunction=" + newParticipant.participantFunction.value;
	fonction = fonction + "&FilterLib=" + newParticipant.FilterLib.value;
	fonction = fonction + "&FilterId=" + newParticipant.FilterId.value;
	fonction = fonction + "&participantEmail=" + newParticipant.participantEmail.value;
	fonction = fonction + "&participantActif=" + newParticipant.participantActif.value;
	SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
}


function submitForm()
{
	if (document.newParticipant.FilterLib.value=="")
	{
		alert("<%= resources.getString("crm.fieldNameRequired") %>");
	}
	else if (document.newParticipant.participantFunction.value=="")
	{
		alert("<%= resources.getString("crm.fieldFunctionRequired") %>");
	}
	else
	{
		document.newParticipant.action = "ChangeParticipant";
		document.newParticipant.submit();
	}
}

function cancelForm() {
    document.newParticipant.action = "ViewProject";
    document.newParticipant.submit();
}
</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%
        out.println(window.printBefore());
        if ((participantId != null) && (participantId.length() > 0))
        {
            TabbedPane tabbedPane = gef.getTabbedPane();
            tabbedPane.addTab("Header",myURL + "NewParticipant?participantId=" + participantId,true);
            tabbedPane.addTab("Attachments","attachmentManager.jsp?elmtId=" + participantId + "&elmtType=PARTICIPANT&returnAction=NewParticipant&returnId=participantId",false);
            out.println(tabbedPane.print());
        }
        out.println(frame.printBefore());
%>
<center>
  <%=boardStart%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="newParticipant" action="AddParticipant" method="post">
    <input type="hidden" name="participantId" value="<%=participantId%>">
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.nom")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="FilterLib" value="<%=(String)request.getAttribute("FilterLib")%>" size="25" disabled>
		  <input type="hidden" name="FilterId" value="<%=(String)request.getAttribute("FilterId")%>">
          <a href=javascript:openSPWindow('CallUserPanelParticipant','')><img src="<%=resources.getIcon("crm.userPanel")%>" align="absmiddle" alt="<%=resources.getString("crm.openUserPanelPeas")%>" border=0 title="<%=resources.getString("crm.openUserPanelPeas")%>"></a>
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.fonction")%>&nbsp;:&nbsp;</span></td>
        <td nowrap>
          <select name="participantFunction" size="1">
		    <%
        	iter1 = cFunction.iterator();
        	while (iter1.hasNext())
        	{
            	String[] item = (String[]) iter1.next();
          		out.print("<option value=" + item[0] + ">" + item[1] + "</option>");
          	}
          	%>
          </select>
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.email")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="participantEmail" value="<%= (String)request.getAttribute("participantEmail") %>" readonly size="40">
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.actif")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="checkbox" name="participantActif" value="<%= (String)request.getAttribute("participantActif") %>" <%= (String)request.getAttribute("checked") %> size="25" >
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
