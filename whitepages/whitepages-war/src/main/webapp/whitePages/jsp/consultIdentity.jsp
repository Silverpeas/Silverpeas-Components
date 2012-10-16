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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
		
	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.consultCard"));
	
	Card card = (Card) request.getAttribute("card");
	String userCardId = card.getPK().getId();

	boolean isAdmin = ( (Boolean) request.getAttribute("isAdmin")).booleanValue();

	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"consultIdentity", true, false);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"consultCard?userCardId="+userCardId, false, true);
	
	Form userForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 
	
	if (! card.readReadOnly()) {
		operationPane.addOperation(resource.getIcon("whitePages.editCard"), resource.getString("whitePages.op.editUser"), "javascript:onClick=B_UPDATE_ONCLICK('"+userCardId+"');");
		operationPane.addLine();
		
		if (isAdmin) {
			operationPane.addOperation(resource.getIcon("whitePages.delCard"), resource.getString("whitePages.op.deleteUser"), "javascript:onClick=B_DELETE_ONCLICK('"+userCardId+"');");
			operationPane.addLine();
			
			if (card.getHideStatus() == 0) {//Visible
				operationPane.addOperation(resource.getIcon("whitePages.hideCard"), resource.getString("whitePages.op.hideCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");	
			}
			else {//Masque
				operationPane.addOperation(resource.getIcon("whitePages.showCard"), resource.getString("whitePages.op.showCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");
			}
		}
	}
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
	
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>


<!-- JAVASCRIPT LANGUAGE -->
<script language="JavaScript">
function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</script>


<script language="JavaScript">
<!--	
	function B_UPDATE_ONCLICK(idCard) {
		 theURL = "<%=routerUrl%>updateCard?userCardId="+idCard;
         winName = "updateCard";
         larg ="600";
         haut = "400";
         windowParams = "scrollbars=yes, resizable, alwaysRaised";
         dico = SP_openWindow(theURL, winName, larg, haut, windowParams);
	}

/*****************************************************************************/
	function B_DELETE_ONCLICK(idCard) {
		if (window.confirm("<%=resource.getString("whitePages.messageSuppression")%>")) { 
			location.href = "<%=routerUrl%>delete?checkedCard="+idCard;
		}
	}	
	
/*****************************************************************************/
	function B_REVERSEHIDE_ONCLICK(idCard) {
		location.href = "<%=routerUrl%>reverseHide?returnPage=consultIdentity&userCardId="+idCard;
	}	

//-->
</script>	
</HEAD>

<BODY class="yui-skin-sam">
<FORM NAME="myForm" METHOD="POST" ACTION="#">
<%
//location.href = routerUrl+"reverseHide?returnPage=m_context/whitePages/jsp/consultIdentity.jsp&userCardId="+idCard;
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>

<center>
<%
	userForm.display(out, context, data);
%>
<br>
<%=buttonPane.print() %>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</FORM>
</BODY>
</HTML>