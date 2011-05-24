<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
Iterator   iter1 = null;
Collection cMedia = (Collection)request.getAttribute("Medias");
Collection cContact = (Collection)request.getAttribute("Contacts");
    
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
    document.newDelivery.action = "ViewDelivrable";
    document.newDelivery.submit();
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
    <form name="newDelivery" action="AddDelivery" method="post">
    <input type="hidden" name="deliveryId" value="<%= (String) request.getAttribute("deliveryId") %>">
      <tr> 
      	<td  nowrap align=left><span class="txtlibform"><%=(String)resources.getString("crm.deliveryDate")%>&nbsp;:&nbsp;</span></td>
        <td align=left> 
           <input type="text" name="deliveryDate" size="14" maxlength="10"  
           <% 
				s = (String)request.getAttribute("deliveryDate");
				if (!s.equals(""))
					out.print("value=\"" + formatter.format(DateUtil.stringToDate(s, resources.getLanguage()))+"\"");
		   %> > &nbsp;&nbsp;
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryElement")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="deliveryElement" value="<%= (String)request.getAttribute("deliveryElement") %>" size="25">
          &nbsp;&nbsp;
        </td>
      </tr>        
      <tr>         
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryVersion")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="deliveryVersion" value="<%= (String)request.getAttribute("deliveryVersion") %>" size="25">
          &nbsp;&nbsp;
        </td>
      </tr>        
      <tr> 
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryIntervenant")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLib" value="<%=(String)request.getAttribute("FilterLib")%>" size="25" readonly>
		  <input type="hidden" name="FilterId" value="<%=(String)request.getAttribute("FilterId")%>">          
          &nbsp;&nbsp; 
        </td>
      </tr>        
      <tr> 
      	<td  nowrap align=left><span class="txtlibform"><%=(String)resources.getString("crm.deliveryContactName")%>&nbsp;:&nbsp;</span></td>
        <td nowrap> 
          <select name="deliveryContact" size="1">
		    <%
        	iter1 = cContact.iterator();
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
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryMedia")%>&nbsp;:&nbsp;</span></td>
        <td nowrap> 
          <select name="deliveryMedia" size="1">
		    <%
        	iter1 = cMedia.iterator();
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
