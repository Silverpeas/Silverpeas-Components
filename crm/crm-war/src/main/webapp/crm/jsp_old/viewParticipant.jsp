<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
	Iterator iter1 = null;
	Collection cFunction = (Collection)request.getAttribute("Functions");
%>

<html>
<head>
	<title><%=resources.getString("GML.popupTitle")%></title><%

	out.println(gef.getLookStyleSheet());
%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function cancelForm() {
		    document.newParticipant.action = "ViewProject";
		    document.newParticipant.submit();
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
    <form name="newParticipant" action="AddParticipant" method="post">
    <input type="hidden" name="participantId" value="<%= (String) request.getAttribute("participantId") %>">
      <tr> 
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.nom")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLib" value="<%=(String)request.getAttribute("FilterLib")%>" size="25" disabled>
		  <input type="hidden" name="FilterId" value="<%=(String)request.getAttribute("FilterId")%>">          
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
          <input type="text" name="participantEmail" value="<%= (String)request.getAttribute("participantEmail") %>" readonly size="25">
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.actif")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">           
          <input type="checkbox" name="participantActif" value="<%= (String)request.getAttribute("participantActif") %>" <%= (String)request.getAttribute("checked") %> size="25" readonly >
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