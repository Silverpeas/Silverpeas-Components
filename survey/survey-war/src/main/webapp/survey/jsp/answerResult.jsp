<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail" %>
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

<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkSurvey.jsp" %>

<%
Collection<String> 	users 	= (Collection<String>) request.getAttribute("Users");
QuestionContainerDetail survey = (QuestionContainerDetail) request.getAttribute("Survey");
String profile = (String) request.getAttribute("Profile");
Button close = gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
%>

<view:sp-page>
  <view:sp-head-part>
  <script type="text/javascript">
  function viewResultByUser(userId) {
    let url = "UserResult?UserId="+userId;
    const windowName = "resultByUser";
    const larg = "700";
    const haut = "500";
    const windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
    suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
    suggestions();
  }
  </script>
  </view:sp-head-part>
  <view:sp-body-part>
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
         if (profile.equals(SilverpeasRole.ADMIN.toString()) || profile.equals(SilverpeasRole.PUBLISHER.toString()) || surveyScc.getUserId().equals(userId)) {
           ArrayLine ligne = arrayPane.addArrayLine();
           String url = "<a href=\"javaScript:onclick=viewResultByUser('" + userId + "');\">" +
               WebEncodeHelper.javaStringToHtmlString(
                   user.getLastName() + " " + user.getFirstName()) + "</a>";
           cell = ligne.addArrayCellText(url);
           cell.setCompareOn(user.getLastName() + " " + user.getFirstName());
         }
        }
      }

      out.println(arrayPane.print());

      ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(close);
      out.print("<br/>"+buttonPane.print());
    %>
    </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>