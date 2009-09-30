<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
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

	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath(resource.getString("whitePages.usersList"));
   	
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
	document.liste_card.userCardId.value = idCard;
	document.liste_card.submit();
}

/*****************************************************************************/
function B_SEARCH_ONCLICK(nbCard) {
	location.href = "<%=m_context%>/RpdcSearch/<%=scc.getComponentId()%>/Main";
}

</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM NAME="liste_card" METHOD="POST" ACTION="<%=routerUrl%>consultIdentity">
  <input type=hidden name=userCardId >
  <input type=hidden name=HostComponentName value="<%=Encode.javaStringToHtmlString(scc.getComponentLabel())%>">
  <input type=hidden name=HostUrl value="<%=Encode.javaStringToHtmlString(m_context + "/RwhitePages/"+scc.getComponentId()+"/Main")%>">
  <input type=hidden name=HostSpaceName value="<%=Encode.javaStringToHtmlString(scc.getSpaceLabel())%>">
  <input type=hidden name=HostPath value="<%=Encode.javaStringToHtmlString(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.consultCard"))%>">
  

<%
operationPane.addOperation(resource.getIcon("whitePages.searchCard"), resource.getString("whitePages.op.searchCard"), "javascript:onClick=B_SEARCH_ONCLICK();");

out.println(window.printBefore());
out.println(frame.printBefore());
%>
<br><br>
<%
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
				
				
				if (listCard != null) {
					Iterator i = listCard.iterator();
					while (i.hasNext()) {
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
							buffer.append( URLManager.getApplicationURL() );
							buffer.append( URLManager.getURL(null, card.getInstanceId()) );
							buffer.append( "NotifyExpert?cardId=");
							buffer.append( card.getPK().getId() );
							buffer.append( "\"><img src=\"" );
							buffer.append( resource.getIcon("whitePages.notify") );
							buffer.append( "\" border=\"0\"></a>");
							email = buffer.toString();
						} 
															
						arrayLine.addArrayCellLink(lastName, "javascript:onClick=consult('"+card.getPK().getId()+"')");
						
						arrayLine.addArrayCellText(firstName);
						
						arrayLine.addArrayCellText(email);
						
						//icones triables
						if (card.getHideStatus() == 1) {//masqué
						
							arrayLine.addArrayCellText(displayIcon(resource.getIcon("whitePages.nonvisible"), resource.getString("whitePages.cache")));
						}
						else {//visible
							arrayLine.addArrayCellText(displayIcon(resource.getIcon("whitePages.visible"), resource.getString("whitePages.visible")));
						}
						
					}
				}
				
				
		out.println(arrayPane.print());
		
%>


<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</FORM>
</BODY>
</HTML>