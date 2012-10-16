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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="javax.ejb.FinderException, javax.ejb.NoSuchEntityException, java.rmi.RemoteException, java.sql.SQLException, javax.ejb.RemoveException, javax.ejb.CreateException, javax.naming.NamingException"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

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

<html> 
<head> 
<title><%=surveyScc.getString("GML.error")%></title>
<%=gef.getLookStyleSheet() %>
</head> 
<body>
<table>
<tr><td>
<h2><%=surveyScc.getString("GML.error")%></h2>
<h3><%=surveyScc.getString("RequestUncomplete")%></h3>
<p>
<% if (exception instanceof NamingException) {
        out.println(displayNetworkError(surveyScc));
    } else if (exception instanceof SQLException) {
            out.println(displayDatabaseError(surveyScc));
    } else if (exception instanceof CreateException) {
            out.println(displayEJBCreationError(surveyScc));
    } else if (exception instanceof RemoteException) {
            out.println(displayNetworkError(surveyScc));
    } else if (exception instanceof FinderException) {
            out.println(displayEJBFinderError(surveyScc));
    } else if (exception instanceof NoSuchEntityException) {
            out.println(displayEJBFinderError(surveyScc));
    } else {
            out.println(displayUnexpectedError(surveyScc));
    }
%>
<p>
<font color="red" size="3"><b><em><%= exception.getClass().getName() +"  "+ exception.getMessage() %></em></b></font>
<p><%=surveyScc.getString("ErrorTransmitAdmin")%> <%=surveyScc.getString("Thanks")%></p>
<p><a href="Main.jsp"><%=surveyScc.getString("BackToMainPage")%></a></p>
</td></tr>
</table>
</body>
</html>