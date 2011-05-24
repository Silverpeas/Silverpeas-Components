<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
Iterator   iter1 = null;
Collection cMedia = (Collection)request.getAttribute("Medias");
Collection cContact = (Collection)request.getAttribute("Contacts");
String deliveryId = (String)request.getAttribute("deliveryId");
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

function openSPWindow(url, windowName) {
	url += "?deliveryId=" + newDelivery.deliveryId.value
		+ "&deliveryDate=" + newDelivery.deliveryDate.value
		+ "&deliveryElement=" + newDelivery.deliveryElement.value
		+ "&deliveryVersion=" + newDelivery.deliveryVersion.value
		+ "&FilterLib=" + newDelivery.FilterLib.value
		+ "&FilterId=" + newDelivery.FilterId.value
		+ "&deliveryContact=" + newDelivery.deliveryContact.value
		"&deliveryMedia=" + newDelivery.deliveryMedia.value;
	SP_openWindow(url, windowName, '750', '550', 'scrollbars=yes, menubar=yes, resizable, alwaysRaised');
}


function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var result = false;

     var deliveryElement = stripInitialWhitespace(document.newDelivery.deliveryElement.value);
     var deliveryVersion = stripInitialWhitespace(document.newDelivery.deliveryVersion.value);
     var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
     var beginDate = document.newDelivery.deliveryDate.value;

     var yearBegin = extractYear(beginDate, '<%=resources.getLanguage()%>');
     var monthBegin = extractMonth(beginDate, '<%=resources.getLanguage()%>');
     var dayBegin = extractDay(beginDate, '<%=resources.getLanguage()%>');

     var beginDateOK = true;

	if (document.newDelivery.deliveryContact.value=="") {
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryContactName")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
	}

	if (document.newDelivery.FilterLib.value=="") {
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryIntervenant")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
	}

	if (isWhitespace(deliveryElement)) {
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryElement")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
    }
    if (isWhitespace(deliveryVersion)) {
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryVersion")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
    }

     if (isWhitespace(beginDate)) {
           errorMsg +="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryDate")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
     } else {
             if (beginDate.replace(re, "OK") != "OK") {
                 errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryDate")%>' <%=resources.getString("crm.MustContainCorrectDate")%>\n";
                 errorNb++;
                 beginDateOK = false;
             } else {
                 if (!isCorrectDate(yearBegin, monthBegin, dayBegin)) {
                   errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.deliveryDate")%>' <%=resources.getString("crm.MustContainCorrectDate")%>\n";
                   errorNb++;
                   beginDateOK = true;
                 }
             }
     }

     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("crm.ThisFormContains")%> 1 <%=resources.getString("crm.Error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("crm.ThisFormContains")%> " + errorNb + " <%=resources.getString("crm.Errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}


function submitForm() {
	if (isCorrectForm()) {
		document.newDelivery.action = "ChangeDelivery";
		document.newDelivery.submit();
	}
}

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

        if ((deliveryId != null) && (deliveryId.length() > 0))
        {
            TabbedPane tabbedPane = gef.getTabbedPane();
            tabbedPane.addTab("Header",myURL + "NewDelivery?deliveryId=" + deliveryId,true);
            tabbedPane.addTab("Attachments","attachmentManager.jsp?elmtId=" + deliveryId + "&elmtType=DELIVERY&returnAction=NewDelivery&returnId=deliveryId",false);
            out.println(tabbedPane.print());
        }
        out.println(frame.printBefore());
%>
<center>
  <%=boardStart%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="newDelivery" action="AddDelivery" method="post">
    <input type="hidden" name="deliveryId" value="<%=deliveryId%>">
      <tr>
      	<td  nowrap align=left><span class="txtlibform"><%=(String)resources.getString("crm.deliveryDate")%>&nbsp;:&nbsp;</span></td>
        <td align=left>
           <input type="text" name="deliveryDate" size="14" maxlength="10"
           <%
				s = (String)request.getAttribute("deliveryDate");
				if (!s.equals(""))
					out.print("value=\"" + formatter.format(DateUtil.stringToDate(s, resources.getLanguage()))+"\"");
		   %> > &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">&nbsp;<span class="txtnote">(<%=resources.getString("crm.dateFormat")%>)</span>
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryElement")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="deliveryElement" value="<%= (String)request.getAttribute("deliveryElement") %>" size="25">
          &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryVersion")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="deliveryVersion" value="<%= (String)request.getAttribute("deliveryVersion") %>" size="25">
          &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.deliveryIntervenant")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="FilterLib" value="<%=(String)request.getAttribute("FilterLib")%>" size="25" readonly>
		  <input type="hidden" name="FilterId" value="<%=(String)request.getAttribute("FilterId")%>">
          <a href=javascript:openSPWindow('CallUserPanelDelivery','')><img src="<%=resources.getIcon("crm.userPanel")%>" align="absmiddle" alt="<%=resources.getString("crm.openUserPanelPeas")%>" border=0 title="<%=resources.getString("crm.openUserPanelPeas")%>"></a>
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
	document.newDelivery.deliveryDate.focus();
</script>
