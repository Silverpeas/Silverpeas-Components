<%@ page import="org.silverpeas.util.*" %>
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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkSurvey.jsp" %>
<%@ include file="surveyUtils.jsp" %>

<%
  QuestionContainerDetail survey  = (QuestionContainerDetail) request.getAttribute("Survey");
  String profile          = (String) request.getAttribute("Profile");
  Collection resultsByUser    = (Collection) request.getAttribute("ResultUser");
  String userName         = (String) request.getAttribute("UserName");
  String userId         = (String) request.getAttribute("UserId");
  
  SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.survey.surveySettings");
  String m_context = GeneralPropertiesManager.getString("ApplicationURL");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
function viewUsers(id) {
    url = "ViewListResult?AnswerId="+id;
    windowName = "users";
    larg = "550";
    haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
    suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
    suggestions.focus();
}           
</script>
</head>
<body>
<%     
    String surveyPart = displaySurveyResultOfUser(userName, userId, resultsByUser, survey, gef, m_context, surveyScc, resources, settings, profile);
    out.println(surveyPart);
%>
</body>
</html>