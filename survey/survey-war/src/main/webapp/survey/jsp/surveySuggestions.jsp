<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>


<%@ include file="checkSurvey.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<% 
//Retrieve parameter
String questionId = (String) request.getParameter("QuestionId");

Button closeButton = (Button) gef.getFormButton(generalMessage.getString("GML.close"), "javascript:onClick=window.close();", false);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%out.println(generalMessage.getString("GML.popupTitle"));%></title>
<view:looknfeel />
</head>
<body>
<% 
Window window = gef.getWindow();
window.setPopup(true);

BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(surveyScc.getSpaceLabel());
browseBar.setComponentName(surveyScc.getComponentLabel());
browseBar.setExtraInformation(resources.getString("MoreAnswer"));
  
Frame frame = gef.getFrame();
 
out.println(window.printBefore());
out.println(frame.printBefore());

Collection<QuestionResult> suggestions = surveyScc.getSuggestions(questionId);
if (suggestions != null) {
	Board board = gef.getBoard();
    out.println(board.printBefore());
    out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" width=\"100%\">");
    Iterator<QuestionResult> it = suggestions.iterator();
    while (it.hasNext()) {
        QuestionResult suggestion = (QuestionResult) it.next();
        if (suggestion.getOpenedAnswer() != null && ! suggestion.getOpenedAnswer().isEmpty()) {
            out.println("<tr valign=top><td width=\"10%\">&nbsp;</td><td valign=top width=\"90%\">&#149; "+WebEncodeHelper.javaStringToHtmlString(suggestion.getOpenedAnswer())+"</td></tr>");
        }
    }
    out.println("</table>");
    out.println(board.printAfter());
}
   
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(closeButton);
    
out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
        
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>