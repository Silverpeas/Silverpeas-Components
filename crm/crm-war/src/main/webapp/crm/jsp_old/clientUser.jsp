<%@ include file="checkCrm.jsp" %>

<html>
<head>
	<title><%=resources.getString("GML.popupTitle")%></title><%

	   out.println(gef.getLookStyleSheet());
%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function openSPWindow(fonction, windowName){
			pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400', 'scrollbars=yes, resizable, alwaysRaised');
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("crm.projet"), myURL + "ViewProject", false);
	tabbedPane.addTab(resources.getString("crm.client"), myURL + "ViewClient", true);
	tabbedPane.addTab(resources.getString("crm.delivrable"), myURL + "ViewDelivrable", false);
	tabbedPane.addTab(resources.getString("crm.journal"), myURL + "ViewJournal", false);			

    out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>
	<center>
  		<%=boardStart%> 
  		<table width="100%" border="0" cellspacing="0" cellpadding="4">
      		<tr> 
        		<td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.client")%>&nbsp;:&nbsp;</span></td>
        		<td nowrap colspan="2"><input type="text" name="NomClient"
        			value="<%=(String)request.getAttribute("clientName")%>" size="25" readonly>&nbsp;&nbsp;</td>
        	</tr>
        	<tr>
        		<td nowrap><span class=txtlibform><%=resources.getString("crm.contacts")%>&nbsp;:&nbsp;</span></td>
        	</tr>
        </table>
        <%=boardEnd%>
    </center>
    <br><%

	// Recuperation des contacts
	Vector contacts = (Vector) request.getAttribute("listContacts");

	ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
	arrayPane.setVisibleLineNumber(50);
	
	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn(resources.getString("crm.attachment"));
	arrayColumn0.setSortable(false);
	
	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("crm.nom"));
	arrayColumn1.setSortable(true);
	
	ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("crm.fonction"));
	arrayColumn2.setSortable(true);
	
	ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("crm.tel"));
	arrayColumn3.setSortable(true);
	
	ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("crm.email"));
	arrayColumn4.setSortable(true);
	
	ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("crm.adresse"));
	arrayColumn5.setSortable(true);
	
	ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resources.getString("crm.actif"));
	arrayColumn6.setSortable(false);
	
	if (contacts.size() > 0) {
		for (int i = 0; i < contacts.size(); i++) {
			CrmContact crmContact = (CrmContact) contacts.elementAt(i);
			ArrayLine arrayLine = arrayPane.addArrayLine();

            Vector attachments = crmContact.returnAttachments();
            StringBuffer sb = new StringBuffer();

			if (attachments.size() > 0) {
                for (int j = 0; j < attachments.size(); j++) {
                    AttachmentDetail ad = (AttachmentDetail)(attachments.get(j));
                    sb.append("<a href=\"" + ad.getAttachmentURL() + "\" target=\"CRMWindow\">"
                        + "<img src=\"" + resources.getIcon("crm.attachedFiles") + "\" alt=\""
                        + ad.getLogicalName() + "\">" + "</a>&nbsp");
                }
            }
            arrayLine.addArrayCellText(sb.toString());
            
        	arrayLine.addArrayCellText(crmContact.getName());
			arrayLine.addArrayCellText(crmContact.getFunctionContact());
			arrayLine.addArrayCellText(crmContact.getTel());
			arrayLine.addArrayCellText(crmContact.getEmail());
			arrayLine.addArrayCellText(crmContact.getAddress());

			IconPane iconPane2 = gef.getIconPane();
			Icon statusIcon = iconPane2.addIcon();
			if (crmContact.getActive().equals("1")) {
				statusIcon.setProperties(resources.getIcon("crm.actif"), resources.getString("crm.actif"));
			} else {
				statusIcon.setProperties(resources.getIcon("crm.nonActif"), resources.getString("crm.nonActif"));
			}
			arrayLine.addArrayCellIconPane(iconPane2);
		}
	}

    out.println(arrayPane.print());

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>