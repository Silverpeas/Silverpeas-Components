<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.whitePages.record.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%!
    String displayIcon(String source, String messageAlt)
    {
		String Html_display = "";
		Html_display = "<img src="+source+" alt="+messageAlt+" title="+messageAlt+">&nbsp;";
		return Html_display;
	}
%>

<%
	Collection listCard = (Collection) request.getAttribute("listCards");
	Boolean mailHidden = (Boolean) request.getAttribute("IsEmailHidden");

   	boolean isMailHidden = mailHidden.booleanValue();
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>

<%
   out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">

function consult(idCard) {
	document.userForm.userCardId.value = idCard;
	document.userForm.submit();
}

function notifyExpert(id) {
	document.notifForm.cardId.value = id;
	document.notifForm.submit();
}

</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
out.println(frame.printBefore());

	ArrayPane arrayPane = gef.getArrayPane("Annuaire", routerUrl+"Main", request, session);
	if (arrayPane.getColumnToSort() == 0)
    	arrayPane.setColumnToSort(2);       
	arrayPane.setVisibleLineNumber(10);
					
	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayPane.addArrayColumn(resource.getString("GML.surname"));
	arrayPane.addArrayColumn(resource.getString("GML.eMail"));
	arrayPane.addArrayColumn(resource.getString("GML.status"));
				
				
	if (listCard != null) 
	{
		Iterator i = listCard.iterator();
		while (i.hasNext()) 
		{
			Card card = (Card) i.next();
			ArrayLine arrayLine = arrayPane.addArrayLine();
						
			IconPane iconPane1 = gef.getIconPane();
			Icon debIcon = iconPane1.addIcon();
			debIcon.setProperties(resource.getIcon("whitePages.minicone"), "", "javascript:onClick=consult('"+card.getPK().getId()+"')");
			arrayLine.addArrayCellIconPane(iconPane1);	
						
			UserRecord userRecord = card.readUserRecord();
			String lastName = userRecord.getField("LastName").getValue(language);
			String firstName = userRecord.getField("FirstName").getValue(language);
						
			String email = userRecord.getField("Mail").getValue(language);
			if (isMailHidden)
			{
				StringBuffer buffer = new StringBuffer();
				buffer.append("<a href=\"");
				buffer.append( "javascript:onClick=notifyExpert('");
				buffer.append( card.getPK().getId() );
				buffer.append("')");
				buffer.append( "\"><img src=\"" );
				buffer.append( resource.getIcon("whitePages.notify") );
				buffer.append( "\" border=\"0\"></a>");
				email = buffer.toString();
			} 
															
			arrayLine.addArrayCellLink(lastName, "javascript:onClick=consult('"+card.getPK().getId()+"')");
			arrayLine.addArrayCellText(firstName);
			arrayLine.addArrayCellText(email);
						
			//icones triables
			if (card.getHideStatus() == 1) 
			{//masqué
				arrayLine.addArrayCellText(displayIcon(resource.getIcon("whitePages.nonvisible"), resource.getString("whitePages.cache")));
			}
			else 
			{//visible
				arrayLine.addArrayCellText(displayIcon(resource.getIcon("whitePages.visible"), resource.getString("whitePages.visible")));
			}
						
		}
	}
				
				
	out.println(arrayPane.print());
		
%>


<%
out.println(frame.printAfter());
%>

<form name="userForm" action="consultIdentity" Method="POST" target="MyMain">
	<input type=hidden name=userCardId >
  	<input type=hidden name=HostComponentName value="<%=Encode.javaStringToHtmlString(scc.getComponentLabel())%>">
  	<input type=hidden name=HostUrl value="<%=Encode.javaStringToHtmlString(m_context + "/RwhitePages/"+scc.getComponentId()+"/Main")%>">
  	<input type=hidden name=HostSpaceName value="<%=Encode.javaStringToHtmlString(scc.getSpaceLabel())%>">
  	<input type=hidden name=HostPath value="<%=Encode.javaStringToHtmlString(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.consultCard"))%>">
</form>

<form name="notifForm" action="NotifyExpert" Method="POST" target="MyMain">
	<input type=hidden name=cardId >
</form>

</BODY>
</HTML>