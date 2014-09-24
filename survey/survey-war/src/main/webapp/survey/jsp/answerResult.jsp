<%@ page import="org.silverpeas.util.EncodeHelper" %>
<%@ page import="org.silverpeas.util.GeneralPropertiesManager" %>
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
<%@ include file="checkSurvey.jsp" %>

<%
Collection<String> 	users 	= (Collection<String>) request.getAttribute("Users");
QuestionContainerDetail survey = (QuestionContainerDetail) request.getAttribute("Survey");

Button close = gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
String iconsPath = GeneralPropertiesManager.getString("ApplicationURL");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
function viewResultByUser(userId, userName) {
	url = "UserResult?UserId="+userId+"&UserName="+userName;
 	windowName = "resultByUser";
 	larg = "700";
 	haut = "500";
 	windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 	suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
 	suggestions.focus();
}
</script>
</head>
<body>
<view:browseBar extraInformations='<%=survey.getHeader().getTitle()%>'/>
<view:window popup="true">
<view:frame>
<% 
	ArrayPane arrayPane = gef.getArrayPane("SurveyParticipantsList", "ViewAllUsers?SurveyId="+survey.getId(), request, session);
	arrayPane.addArrayColumn(resources.getString("GML.name"));
	
	if (users != null) {	
	  	ArrayCellText cell = null; 
	    for (String userId : users) {
			 UserDetail user = surveyScc.getUserDetail(userId);	
			 ArrayLine ligne = arrayPane.addArrayLine();
			 String url = "<a href=\"javaScript:onclick=viewResultByUser('"+userId+"','"+
           EncodeHelper.javaStringToHtmlString(user.getDisplayedName())+"');\">"+EncodeHelper.javaStringToHtmlString(user.getLastName()+" "+user.getFirstName())+"</a>";
			 cell = ligne.addArrayCellText(url);
			 cell.setCompareOn(user.getLastName()+" "+user.getFirstName());
      	}
	}

	out.println(arrayPane.print());
	
	ButtonPane buttonPane = gef.getButtonPane();
  	buttonPane.addButton(close);
	out.print("<br/>"+buttonPane.print());
%>
</view:frame>
</view:window>
</body>
</html>