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

<%@ include file="checkSurvey.jsp" %>

<%
	Collection 	users 		= (Collection) request.getAttribute("Users");
	String 		surveyId 	= (String) request.getAttribute("SurveyId");

	// d�claration des boutons
	Button close = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
	String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<html>
<head>
	<title></title>
	<% out.println(gef.getLookStyleSheet()); %>
	<script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
	<script language="JavaScript1.2">

        function viewResultByUser(userId, userName)
        {
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
<% 
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setClickable(false);
	Frame frame = gef.getFrame();
	
	out.println(window.printBefore());
	out.println(frame.printBefore());

	ArrayPane arrayPane = gef.getArrayPane("SurveyParticipantsList", "ViewAllUsers?SurveyId="+surveyId, request, session);
	arrayPane.addArrayColumn(resources.getString("GML.name"));
	
	if (users != null)
	{	
	  	ArrayCellText cell = null; 
		Iterator it = users.iterator();
		while (it.hasNext())
		{
			String userId = (String) it.next();
			UserDetail user = surveyScc.getUserDetail(userId);	
			ArrayLine ligne = arrayPane.addArrayLine();
			String url = "<a href=\"javaScript:onClick=viewResultByUser('"+userId+"','"+EncodeHelper.javaStringToHtmlString(user.getDisplayedName())+"');\">"+EncodeHelper.javaStringToHtmlString(user.getLastName()+" "+user.getFirstName())+"</a>";
			cell = ligne.addArrayCellText(url);
			cell.setCompareOn(user.getLastName()+" "+user.getFirstName());
		}
	}

	out.println(arrayPane.print());
	
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(close);
	out.print("<BR><center>"+buttonPane.print()+"</center>");
	out.print(frame.printAfter());
	out.print(window.printAfter());
%>
</body>
</html>