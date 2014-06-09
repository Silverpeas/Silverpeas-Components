<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
SPChannel channel = (SPChannel) request.getAttribute("Channel");
String displayImageChecked = "";
if (channel.getDisplayImage() == 1)
	displayImageChecked = " checked";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
<!-- LANGUAGE JAVASCRIPT -->
<script language="javascript">
	function isCorrectForm() {
		var errorMsg = "";
		var errorNb = 0;
		var url 	= document.channel.Url.value;
		var nbItems = document.channel.NbItems.value;
		var refresh = document.channel.RefreshRate.value;
		if (isWhitespace(url)) {
			errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("rss.url")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
			errorNb++; 
		}
		if (isWhitespace(refresh) || !isNumericField(refresh)) {
			errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("rss.refreshRate")%>' <%=resource.getString("GML.MustContainsNumber")%>\n";
			errorNb++; 
		}
		if (!isWhitespace(nbItems) && !isNumericField(nbItems)) {
			errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("rss.nbDisplayedItems")%>' <%=resource.getString("GML.MustContainsNumber")%>\n";
			errorNb++;
		}
		switch(errorNb)
		{
			case 0 :
				result = true;
				break;
			case 1 :
				errorMsg = "<%=resource.getString("GML.ThisFormContains")%>1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
				window.alert(errorMsg);
				result = false;
				break;
			default :
				errorMsg = "<%=resource.getString("GML.ThisFormContains")%>" + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
				window.alert(errorMsg);
				result = false;
				break;
		}
 		return result;
	}
	
	// envoi les donn�es entr�es par l'utilisateur
	function sendData(){
		if (isCorrectForm()) {
			document.channel.submit();
		}
	}
</script>
</head>
<body bgcolor="#ffffff">
<form name="channel" action="UpdateChannel" method="post">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	
	Board board = gef.getBoard();
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<input type="hidden" name="Id" value="<%=channel.getPK().getId()%>"/>
<table width="100%" border="0" cellspacing="0" cellpadding="4">
	<tr>
		<td class="txtlibform"><%=resource.getString("rss.url")%> :</td>
		<td><input type="text" name="Url" maxlength="1000" size="60" value="<%=channel.getUrl()%>"/>&nbsp;<img src="<%=resource.getIcon("rss.mandatoryField")%>" width=5 align="absmiddle"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("rss.refreshRate")%> :</td>
		<td><input type="text" name="RefreshRate" maxlength="10" size="3" value="<%=channel.getRefreshRate()%>"/>&nbsp;(<%=resource.getString("rss.minutes")%>)&nbsp;<img src="<%=resource.getIcon("rss.mandatoryField")%>" width=5 align="absmiddle"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("rss.nbDisplayedItems")%> :</td>
		<td><input type="text" name="NbItems" maxlength="10" size="3" value="<%=channel.getNbDisplayedItems()%>"/></td>
	</tr>
	<tr>
	<td class="txtlibform"><%=resource.getString("rss.displayImage")%> :</td>
		<td><input type="checkbox" name="DisplayImage" <%=displayImageChecked%>/></td>
	</tr>
	<tr> 
        <td colspan="2" valign="top">( <img src="<%=resource.getIcon("rss.mandatoryField")%>" width=5 align="absmiddle"/>&nbsp;: <%=resource.getString("GML.requiredField")%> )</td>
  </tr>
</table>
<%	 
	out.println(board.printAfter()+"<br/>");
	
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));

	out.println("<center>");
    out.println(buttonPane.print());
	out.println("</center>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</form>
</body>
</html>