<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page import="com.silverpeas.whitePages.record.UserRecord"%>

<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkWhitePages.jsp" %>

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "javascript:goToMain();");
	Card card = (Card) request.getAttribute("card");
	
	UserRecord userRecord = card.readUserRecord();
	String lastName = userRecord.getField("LastName").getValue(language);
	String firstName = userRecord.getField("FirstName").getValue(language);
	
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.createCard") + " " + lastName + " " + firstName);
	
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"createCard", true, false);
	
	Collection<WhitePagesCard> whitePagesCards = (Collection<WhitePagesCard>) request.getAttribute("whitePagesCards");
	Form updateForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<%
   updateForm.displayScripts(out, context);
%>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript">
	function goToMain() {
		if (window.confirm("<%=resource.getString("whitePages.messageCancelCreate")%>")) {
			<% if (containerContext == null) { %>
			   location.href = "Main";
			<% } else { %>
			   location.href = "<%= m_context + containerContext.getReturnURL()%>"; 
			<% } %>
		}
	}
	function B_VALIDER_ONCLICK() {
		if (isCorrectForm()) {
			<view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
			if (errorNb > 0) {
				alert(errorMsg);
			} else {
				<view:pdcPositions setIn="document.myForm.Positions.value"/>
				document.myForm.submit();
			}
		}
	}
	
	function B_ANNULER_ONCLICK() {
		<% if (containerContext == null) { %>
		   location.href = "Main";
		<% } else { %>
		   location.href = "<%=m_context+containerContext.getReturnURL()%>"; 
		<% } %>
	}
	
	function changerChoice() {
        indexWhitePages = document.choixFiche.selectionFiche.selectedIndex;
        document.choixFiche.userCardId.value = document.choixFiche.selectionFiche.options[indexWhitePages].value;
        document.choixFiche.submit();	
	}
	
</script>
</head>
<body class="yui-skin-sam">

<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<% if (whitePagesCards != null && whitePagesCards.size() > 1) { %>
<form name="choixFiche" method="post" action="<%=routerUrl%>consultCard">
	<input type="hidden" name="userCardId"/>
<div class="inlineMessage">
		<span class="txtlibform"><%=resource.getString("whitePages.autreFiches")%> :</span>					
        <select size="1" name="selectionFiche" onchange="changerChoice()">
        <%
			for (WhitePagesCard whitePagesCard : whitePagesCards) {
				long userCardId = whitePagesCard.getUserCardId();
				String label = whitePagesCard.readInstanceLabel();
     			if (userCardId == 0) {
     				//fiche en creation
					out.println("<option selected value=\""+userCardId+"\">"+label+"</option>"); 
     			} else { 
         			out.println("<option value=\""+userCardId+"\">"+label+"</option>");
     			}
     		}
         %>
        </select>
</div>
</form>
<% } %>

<form name="myForm" method="post" action="<%=routerUrl%>effectiveCreate" enctype="multipart/form-data">
<br/>
<%
	updateForm.display(out, context, data);
%>
<br/>
<!-- PDC classification begins here -->
<input type="hidden" name="Positions"/>
<view:pdcNewContentClassification componentId="<%= componentId %>"/>
<!-- PDC classification stops here -->
</form>
<center>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
	out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>