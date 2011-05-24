<%@ include file="checkCrm.jsp" %>


<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
   
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function cancelForm() {
    document.newContact.action = "ViewClient";
    document.newContact.submit();
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
    <form name="newContact" action="AddContact" method="post">
    <input type="hidden" name="contactId" value="<%= (String) request.getAttribute("contactId") %>">
      <tr> 
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.nom")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="contactName" value="<%= (String)request.getAttribute("contactName") %>" >
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.fonction")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="contactFunction" value="<%= (String)request.getAttribute("contactFunction") %>" >
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.tel")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="contactTel" value="<%= (String)request.getAttribute("contactTel") %>" size="25">
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.email")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="contactEmail" value="<%= (String)request.getAttribute("contactEmail") %>" size="25">
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.adresse")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="contactAddress" value="<%= (String)request.getAttribute("contactAddress") %>" size="25">
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
  </table>
  <%=boardEnd%>  

<br>  

<%
    ButtonPane buttonPane = gef.getButtonPane();
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
