<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@ page isErrorPage="true" %>

<%@ include file="checkSurvey.jsp" %>

<%!
private String displayNetworkError(SurveySessionController surveyScc) {
  return surveyScc.getString("ErrorNetwork");
}
private String displayDatabaseError(SurveySessionController surveyScc) {
  return surveyScc.getString("ErrorDB");
}
private String displayEJBFinderError(SurveySessionController surveyScc) {
  return surveyScc.getString("ErrorEJBFinder");
}
private String displayEJBCreationError(SurveySessionController surveyScc) {
  return surveyScc.getString("ErrorEJBCreation");
}
private String displayUnexpectedError(SurveySessionController surveyScc) {
  return surveyScc.getString("ErrorUnknown");
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head> 
<title><%=surveyScc.getString("GML.error")%></title>
<view:looknfeel/>
</head> 
<body>
<table>
<tr><td>
<h2><%=surveyScc.getString("GML.error")%></h2>
<h3><%=surveyScc.getString("RequestUncomplete")%></h3>
<p>
<%
  out.println(displayUnexpectedError(surveyScc));
%>
<p>
<font color="red" size="3"><b><em><%= exception.getClass().getName() +"  "+ exception.getMessage() %></em></b></font>
<p><%=surveyScc.getString("ErrorTransmitAdmin")%> <%=surveyScc.getString("Thanks")%></p>
<p><a href="Main.jsp"><%=surveyScc.getString("BackToMainPage")%></a></p>
</td></tr>
</table>
</body>
</html>
