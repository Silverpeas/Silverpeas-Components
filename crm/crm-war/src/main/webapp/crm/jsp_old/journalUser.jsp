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


</script>
</head>


<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%

    //Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("crm.projet"), myURL+"ViewProject",false);
	tabbedPane.addTab(resources.getString("crm.client"), myURL+"ViewClient",false);
	tabbedPane.addTab(resources.getString("crm.delivrable"), myURL+"ViewDelivrable",false);
	tabbedPane.addTab(resources.getString("crm.journal"), myURL+"ViewJournal",true);			

        out.println(window.printBefore());
  		out.println(tabbedPane.print());
        out.println(frame.printBefore());
%>
<center>

<%
//Recuperation des parametres
Vector events = (Vector) request.getAttribute("listEvents");
String s1,s2;

		  // Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
          arrayPane.setVisibleLineNumber(50);

          ArrayColumn arrayColumn0 = arrayPane.addArrayColumn(resources.getString("crm.attachment"));
		  arrayColumn0.setSortable(false);

          ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("crm.date"));
		  arrayColumn1.setSortable(true);

          ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("crm.evenement"));
		  arrayColumn2.setSortable(true);

          ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("crm.action"));
		  arrayColumn3.setSortable(true);

          ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("crm.personne"));
		  arrayColumn4.setSortable(true);
		  
          ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("crm.quand"));
		  arrayColumn5.setSortable(true);

          ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resources.getString("crm.etat"));
		  arrayColumn6.setSortable(true);


	if (events.size()>0) 
	{
		for (int i = 0; i < events.size(); i++) 
		{
			CrmEvent crmEvent = (CrmEvent) events.elementAt(i);
			ArrayLine arrayLine = arrayPane.addArrayLine();
        	
            Vector     attachments = crmEvent.returnAttachments();
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

            // formatage date BD -> Client
    		try
    		{
        		s1 = DateUtil.dateToString(DateUtil.parse(crmEvent.getEventDate()), resources.getLanguage());
        		s2 = DateUtil.dateToString(DateUtil.parse(crmEvent.getActionDate()), resources.getLanguage());
    		}
    		catch (java.text.ParseException e)
    		{
        		s1 = "";
        		s2 = "";
    		}
			arrayLine.addArrayCellText(s1);
			arrayLine.addArrayCellText(crmEvent.getEventLib());
			arrayLine.addArrayCellText(crmEvent.getActionTodo());			
			arrayLine.addArrayCellText(crmEvent.getUserName());
			arrayLine.addArrayCellText(s2);
			arrayLine.addArrayCellText(crmEvent.getState());
		}
	}

    out.println(arrayPane.print());

%>

</center>


<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
