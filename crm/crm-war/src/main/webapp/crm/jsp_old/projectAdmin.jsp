<%@ include file="checkCrm.jsp" %>

<%

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

function openEditParticipant(c) {
    document.editParticipant.participantId.value = c;
    document.editParticipant.submit();
}

function participantDeleteConfirm(participantId, name) {
    if(window.confirm("<%=resources.getString("crm.confirmDelete")%> '" + name + "' ?")){
          document.deleteParticipants.action.value = "DeleteParticipants";
          document.deleteParticipants.participantId.value = participantId;
          document.deleteParticipants.submit();
    }
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%

	//Les opérations
	operationPane.addOperation(resources.getIcon("crm.updateClient"), resources.getString("crm.update"), "UpdateProject");
	operationPane.addLine();
	operationPane.addOperation(resources.getIcon("crm.newParticipant"), resources.getString("crm.newParticipant"), "NewParticipant");

	//Les onglets
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("crm.projet"), myURL+"ViewProject",true);
	tabbedPane.addTab(resources.getString("crm.client"), myURL+"ViewClient",false);
	tabbedPane.addTab(resources.getString("crm.delivrable"), myURL+"ViewDelivrable",false);
	tabbedPane.addTab(resources.getString("crm.journal"), myURL+"ViewJournal",false);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());

%>
<center>
  <%=boardStart%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">

      <tr>
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("crm.codeProjet")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2">
          <input type="text" name="projectCode" value="<%= (String)request.getAttribute("projectCode") %>" size="25" readonly>
          &nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td nowrap><span class=txtlibform><%=resources.getString("crm.intervenants")%>&nbsp;:&nbsp;</span></td>
      </tr>
  </table>
  <%=boardEnd%>

</center>
<br>

  <%
//Recuperation des participants
Vector participants = (Vector) request.getAttribute("listParticipants");



		  // Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
          arrayPane.setVisibleLineNumber(50);

          ArrayColumn arrayColumn0 = arrayPane.addArrayColumn(resources.getString("crm.attachment"));
		  arrayColumn0.setSortable(false);

          ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("crm.nom"));
		  arrayColumn1.setSortable(true);

          ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("crm.fonction"));
		  arrayColumn2.setSortable(true);

          ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("crm.email"));
		  arrayColumn3.setSortable(true);

          ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("crm.actif"));
		  arrayColumn4.setSortable(false);

    	  ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("GML.operation"));
		  arrayColumn5.setSortable(false);


	if (participants.size()>0)
	{
		for (int i = 0; i < participants.size(); i++)
		{
			CrmParticipant crmParticipant = (CrmParticipant) participants.elementAt(i);
			ArrayLine arrayLine = arrayPane.addArrayLine();

            Vector     attachments = crmParticipant.returnAttachments();
            StringBuffer sb = new StringBuffer();

			if (attachments.size() > 0) 
            {
                for (int j = 0; j < attachments.size(); j++)
                {
                    AttachmentDetail ad = (AttachmentDetail)(attachments.get(j));
                    sb.append("<A href=\"" + ad.getAttachmentURL() + "\" target=\"CRMWindow\">" + "<img src=\"" + resources.getIcon("crm.attachedFiles") + "\" alt=\"" + ad.getLogicalName() + "\">" + "</A>&nbsp");
                }
            }
            arrayLine.addArrayCellText(sb.toString());

            //arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(crmParticipant.getUserName()), "javascript:openEditParticipant('" + crmParticipant.getPK().getId() + "');");
			arrayLine.addArrayCellText(crmParticipant.getUserName());
			arrayLine.addArrayCellText(crmParticipant.getFunctionParticipant());
			arrayLine.addArrayCellText(crmParticipant.getEmail());

			IconPane iconPane2 = gef.getIconPane();
			Icon statusIcon = iconPane2.addIcon();
			if (crmParticipant.getActive().equals("1"))
				statusIcon.setProperties(resources.getIcon("crm.actif"), resources.getString("crm.actif"));
			else
				statusIcon.setProperties(resources.getIcon("crm.nonActif"), resources.getString("crm.nonActif"));
			arrayLine.addArrayCellIconPane(iconPane2);

            IconPane iconPane3 = gef.getIconPane();
            Icon     updateIcon = iconPane3.addIcon();
           	updateIcon.setProperties(resources.getIcon("crm.update"), resources.getString("crm.update") + " '" + Encode.javaStringToHtmlString(crmParticipant.getUserName()) + "'", "javascript:onClick=openEditParticipant('" + crmParticipant.getPK().getId() + "')");
            Icon deleteIcon = iconPane3.addIcon();
            deleteIcon.setProperties(resources.getIcon("crm.delete"), resources.getString("crm.delete") + " '" + Encode.javaStringToHtmlString(crmParticipant.getUserName()) + "'", "javascript:onClick=participantDeleteConfirm('" + crmParticipant.getPK().getId() + "', '" + Encode.javaStringToHtmlString(Encode.javaStringToJsString(crmParticipant.getUserName())) + "')");
            iconPane3.setSpacing("30px");
            arrayLine.addArrayCellIconPane(iconPane3);

			//arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"deleteParticipants\" value=\"" + crmParticipant.getPK().getId() + "\">");
		}
	}



    out.println(arrayPane.print());


  %>


<form name="deleteParticipants" action="DeleteParticipants" method="post">
	<input type="hidden" name="participantId" value="">
</form>

<form name="editParticipant" action="NewParticipant" method="post">
	<input type="hidden" name="participantId" value="">
</form>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
