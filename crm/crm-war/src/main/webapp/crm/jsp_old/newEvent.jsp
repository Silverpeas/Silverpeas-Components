<%@ include file="checkCrm.jsp" %>

<%
//Recuperation des parametres
Iterator   iter1 = null;
Collection cState = (Collection)request.getAttribute("States");
String eventId = (String) request.getAttribute("eventId");

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

function openSPWindow(fonction,windowName)
{
	fonction = fonction + "?eventId=" + newEvent.eventId.value;
	fonction = fonction + "&eventState=" + newEvent.eventState.value;
	fonction = fonction + "&FilterLib=" + newEvent.FilterLib.value;
	fonction = fonction + "&FilterId=" + newEvent.FilterId.value;
	fonction = fonction + "&eventLib=" + newEvent.eventLib.value;
	fonction = fonction + "&eventDate=" + newEvent.eventDate.value;
	fonction = fonction + "&actionTodo=" + newEvent.actionTodo.value;
	fonction = fonction + "&actionDate=" + newEvent.actionDate.value;
	SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
}


function isCorrectForm()
{
     var errorMsg = "";
     var errorNb = 0;
     var eventLib = stripInitialWhitespace(document.newEvent.eventLib.value);
     var actionTodo = stripInitialWhitespace(document.newEvent.actionTodo.value);
     var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
     var beginDate = document.newEvent.eventDate.value;
     var endDate = document.newEvent.actionDate.value;
     var yearBegin = extractYear(beginDate, '<%=resources.getLanguage()%>');
     var monthBegin = extractMonth(beginDate, '<%=resources.getLanguage()%>');
     var dayBegin = extractDay(beginDate, '<%=resources.getLanguage()%>');
     var yearEnd = extractYear(endDate, '<%=resources.getLanguage()%>');
     var monthEnd = extractMonth(endDate, '<%=resources.getLanguage()%>');
     var dayEnd = extractDay(endDate, '<%=resources.getLanguage()%>');

     var beginDateOK = true;

	if (document.newEvent.FilterLib.value=="")
	{
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.personne")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
	}

     if (isWhitespace(eventLib)) {
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.eventLib")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
     }

     if (isWhitespace(actionTodo)) {
           errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.actionTodo")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
     }

     if (isWhitespace(beginDate)) {
           errorMsg +="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.eventDate")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
     } else {
             if (beginDate.replace(re, "OK") != "OK") {
                 errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.eventDate")%>' <%=resources.getString("crm.MustContainCorrectDate")%>\n";
                 errorNb++;
                 beginDateOK = false;
             } else {
                 if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
                   errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.eventDate")%>' <%=resources.getString("crm.MustContainCorrectDate")%>\n";
                   errorNb++;
                   beginDateOK = true;
                 }
             }
     }
     if (isWhitespace(endDate)) {
           errorMsg +="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.actionDate")%>' <%=resources.getString("crm.MustContainText")%>\n";
           errorNb++;
     } else {
           if (endDate.replace(re, "OK") != "OK") {
                    errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.actionDate")%>' <%=resources.getString("crm.MustContainCorrectDate")%>\n";
                    errorNb++;
           } else {
                 if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                     errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.actionDate")%>' <%=resources.getString("crm.MustContainCorrectDate")%>\n";
                     errorNb++;
                 } else {
                     if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
                           if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
                                  errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.actionDate")%>' <%=resources.getString("crm.MustContainPostDateToBeginDate")%>\n";
                                  errorNb++;
                          }
                     } else {
                           if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) {
                               if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) {
                                      errorMsg+="  - <%=resources.getString("crm.TheField")%> '<%=resources.getString("crm.actionDate")%>' <%=resources.getString("crm.MustContainPostDate")%>\n";
                                      errorNb++;
                               }
                           }
                     }
                 }
           }
     }

     switch(errorNb)
     {
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


function submitForm()
{
	if (isCorrectForm())
	{
		document.newEvent.action = "ChangeEvent";
		document.newEvent.submit();
	}
}

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
        if ((eventId != null) && (eventId.length() > 0))
        {
            TabbedPane tabbedPane = gef.getTabbedPane();
            tabbedPane.addTab("Header",myURL + "NewEvent?eventId=" + eventId,true);
            tabbedPane.addTab("Attachments","attachmentManager.jsp?elmtId=" + eventId + "&elmtType=EVENT&returnAction=NewEvent&returnId=eventId",false);
            out.println(tabbedPane.print());
        }
        out.println(frame.printBefore());
%>
<center>
  <%=boardStart%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="newEvent" action="AddEvent" method="post">
    <input type="hidden" name="eventId" value="<%=eventId%>">
      <tr>
      	<td  nowrap align=left><span class="txtlibform"><%=(String)resources.getString("crm.eventDate")%>&nbsp;:&nbsp;</span></td>
        <td align=left>
           <input type="text" name="eventDate" size="14" maxlength="10"
           <%
				s = (String)request.getAttribute("eventDate");
				if (!s.equals(""))
					out.print("value=\"" + formatter.format(DateUtil.stringToDate(s, resources.getLanguage()))+"\"");
		   %> > &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">&nbsp;<span class="txtnote">(<%=resources.getString("crm.dateFormat")%>)</span>
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.eventLib")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="eventLib" value="<%= (String)request.getAttribute("eventLib") %>" size="40">
          &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.actionTodo")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="actionTodo" value="<%= (String)request.getAttribute("actionTodo") %>" size="40">
          &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">
        </td>
      </tr>
      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=(String)resources.getString("crm.personne")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="FilterLib" value="<%=(String)request.getAttribute("FilterLib")%>" size="25" disabled>
		  <input type="hidden" name="FilterId" value="<%=(String)request.getAttribute("FilterId")%>">
          <a href=javascript:openSPWindow('CallUserPanelEvent','')><img src="<%=resources.getIcon("crm.userPanel")%>" align="absmiddle" alt="<%=resources.getString("crm.openUserPanelPeas")%>" border=0 title="<%=resources.getString("crm.openUserPanelPeas")%>"></a>
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
		   %> > &nbsp;&nbsp;<img src="<%=resources.getIcon("crm.mandatory")%>" width="5" height="5">&nbsp;<span class="txtnote">(<%=resources.getString("crm.dateFormat")%>)</span>
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
	document.newEvent.eventDate.focus();
</script>

