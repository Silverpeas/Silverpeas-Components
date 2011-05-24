<%@ include file="checkCrm.jsp" %>

<%

%>

<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
%>

</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%

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

			}
		}

    out.println(arrayPane.print());
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</body>
</html>
