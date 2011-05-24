<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
Iterator   iter1 = null;
Collection cState = (Collection)request.getAttribute("States");
%>

<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());

%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function cancelForm() {
    document.newEvent.action = "ViewJournal";
    document.newEvent.submit();
}
</script>

</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%
String s;
    
        out.println(window.printBefore());  		
        out.println(frame.printBefore());
%>
<center>
  <%=boardStart%> 
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="newEvent" action="AddEvent" method="post">
    <input type="hidden" name="eventId" value="<%= (String) request.getAttribute("eventId") %>" >
      <tr> 
      	<td nowrap align=left><span class="txtlibform"><%=(String)resources.getString("crm.eventDate")%>&nbsp;:&nbsp;</span></td>
        <td align=left> 
           <input type="text" name="eventDate" size="14" maxlength="10"  
           <% 
				s = (String)request.getAttribute("eventDate");
				if (!s.equals(""))
					out.print("value=\"" + formatter.format(DateUtil.stringToDate(s, resources.getLanguage()))+"\"");
		   %> readonly> &nbsp;&nbsp;
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.eventLib")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="eventLib" value="<%= (String)request.getAttribute("eventLib") %>" size="25" readonly>
          &nbsp;&nbsp;
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.actionTodo")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="actionTodo" value="<%= (String)request.getAttribute("actionTodo") %>" size="25" readonly>
          &nbsp;&nbsp;
        </td>
      </tr>        
      <tr> 
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.personne")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLib" value="<%=(String)request.getAttribute("FilterLib")%>" size="25" readonly>
		  <input type="hidden" name="FilterId" value="<%=(String)request.getAttribute("FilterId")%>">
          
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr> 
      	<td  nowrap align=left><span class="txtlibform"><%=(String)resources.getString("crm.actionDate")%>&nbsp;:&nbsp;</span></td>
        <td align=left> 
           <input type="text" name="actionDate" size="14" maxlength="10"  
           <% 
				s = (String)request.getAttribute("actionDate");
				if (!s.equals(""))
					out.print("value=\"" + formatter.format(DateUtil.stringToDate(s, resources.getLanguage())) +"\"");
		   %> readonly > &nbsp;&nbsp;
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.eventState")%>&nbsp;:&nbsp;</span></td>
        <td nowrap> 
          <select name="eventState" size="1">
		    <%
        	iter1 = cState.iterator();
        	while (iter1.hasNext())
        	{
            	String[] item = (String[]) iter1.next();
          		out.print("<option value=" + item[0] + ">" + item[1] + "</option>");
          	}
          	%>
          </select>
          &nbsp;&nbsp; 
        </td>
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
