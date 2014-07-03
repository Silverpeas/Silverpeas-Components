<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="instanceId" value="${requestScope.browseContext[3]}"/>

<%
	Button validateButton 	= gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
  Button cancelButton 	= gef.getFormButton(resource.getString("GML.cancel"), "javaScript:window.close()", false);
%>

<html>
<head>
	<view:looknfeel/>
	<title></title>
	<script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js"/>"></script>
	<script type="text/javascript">

function sendData()
{
	if (isCorrectForm())
	{
		document.askMediaForm.action = "SendAsk";
		document.askMediaForm.submit();
		window.close();
	}
}

function isCorrectForm()
{
   	var errorMsg = "";
   	var errorNb = 0;
   	var title = stripInitialWhitespace(document.askMediaForm.Description.value);
   	if (title == "")
   	{
         	errorMsg+="  - '<fmt:message key="gallery.request"/>' <fmt:message key="GML.MustBeFilled"/>\n";
         	errorNb++;
   	}
   	switch(errorNb)
   	{
      	case 0 :
          	result = true;
          	break;
      	case 1 :
          	errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
          	window.alert(errorMsg);
          	result = false;
          	break;
      	default :
          	errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
          	window.alert(errorMsg);
          	result = false;
          	break;
   	}
   	return result;
}
  </script>

</head>
<body id="${instanceId}" class="gallery order" onLoad="javascript:document.askMediaForm.Description.focus();">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");

	Board board	= gef.getBoard();

	out.println(window.printBefore());
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<form name="askMediaForm" method="post" action="SendAsk">
<table cellpadding="5" width="100%">
	<tr>
		<td class="txtlibform"><fmt:message key="gallery.request"/> :<br><textarea rows="5" cols="90" name="Description"></textarea></td>
    </tr>
</table>
</form>
<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(validateButton);
  buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>