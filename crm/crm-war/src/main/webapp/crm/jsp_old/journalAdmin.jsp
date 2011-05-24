<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkCrm.jsp" %>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<html>
<head>
	<title><%=resources.getString("GML.popupTitle")%></title><%
	
	   out.println(gef.getLookStyleSheet());
	%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function openEditEvent(c) {
		    document.editEvent.eventId.value = c;
		    document.editEvent.submit();
		}
		
		function eventDeleteConfirm(eventId, name) {
		    if (window.confirm("<%=resources.getString("crm.confirmDelete")%> '" + name + "' ?")) {
		          document.deleteEvents.action.value = "DeleteEvents";
		          document.deleteEvents.eventId.value = eventId;
		          document.deleteEvents.submit();
		    }
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<fmt:message key="crm.newEvent" var="newEventLabel"/>
	<fmt:message key="crm.projet" var="projetLabel"/>
	<fmt:message key="crm.client" var="clientLabel"/>
	<fmt:message key="crm.delivrable" var="delivrableLabel"/>
	<fmt:message key="crm.journal" var="journalLabel"/>

	<view:operationPane>
		<fmt:message key="crm.newEvent" var="newEventIcon" bundle="${icons}" />
		<c:url var="newEventIconUrl" value="${newEventIcon}" />
		<view:operation altText="${newEventLabel}" icon="${newEventIconUrl}" action="NewEvent" />
	</view:operationPane>
	
	<view:window>
		<view:tabs>
			<view:tab label="${projetLabel}" selected="" action="${myComponentURL}ViewProject"></view:tab>
			<view:tab label="${clientLabel}" selected="" action="${myComponentURL}ViewClient"></view:tab>
			<view:tab label="${delivrableLabel}" selected="" action="${myComponentURL}ViewDelivrable"></view:tab>
			<view:tab label="${journalLabel}" selected="true" action="${myComponentURL}ViewJournal"></view:tab>
		</view:tabs>
		<view:frame>
		
		</view:frame>
	</view:window>
<%
    //Les opérations
	operationPane.addOperation(resources.getIcon("crm.newEvent"), resources.getString("crm.newEvent"), "NewEvent");	

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

    	  ArrayColumn arrayColumn7 = arrayPane.addArrayColumn(resources.getString("GML.operation"));
		  arrayColumn7.setSortable(false);

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
			
            IconPane iconPane3 = gef.getIconPane();
            Icon     updateIcon = iconPane3.addIcon();
           	updateIcon.setProperties(resources.getIcon("crm.update"), resources.getString("crm.update") + " '" + Encode.javaStringToHtmlString(crmEvent.getUserName()) + "'", "javascript:onClick=openEditEvent('" + crmEvent.getPK().getId() + "')");
            Icon deleteIcon = iconPane3.addIcon();
            deleteIcon.setProperties(resources.getIcon("crm.delete"), resources.getString("crm.delete") + " '" + Encode.javaStringToHtmlString(crmEvent.getUserName()) + "'", "javascript:onClick=eventDeleteConfirm('" + crmEvent.getPK().getId() + "', '" + Encode.javaStringToHtmlString(Encode.javaStringToJsString(crmEvent.getEventLib())) + "')");
            iconPane3.setSpacing("30px");
            arrayLine.addArrayCellIconPane(iconPane3);
		}
	}

    out.println(arrayPane.print());

%>
</center>

<form name="deleteEvents" action="DeleteEvents" method="post">
	<input type="hidden" name="eventId" value="">
</form>

<form name="editEvent" action="NewEvent" method="post">
	<input type="hidden" name="eventId" value="">
</form>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
