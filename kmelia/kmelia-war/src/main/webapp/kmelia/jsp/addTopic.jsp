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
<%@page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@page import="org.silverpeas.util.i18n.I18NHelper"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="org.silverpeas.util.ResourceLocator" %>
<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<% com.stratelia.webactiv.kmelia.control.KmeliaSessionController kmeliaScc = (com.stratelia.webactiv.kmelia.control.KmeliaSessionController) request.getAttribute("kmelia");%>
<% if(kmeliaScc == null ) {
    // No session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
    }
  MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
%>
<fmt:message var="cancelButtonLabel" key="GML.cancel"/>
<fmt:message var="validateButtonLabel" key="GML.validate"/>
<html>
  <head>
    <view:looknfeel />
    <title><fmt:message key="GML.popupTitle" /></title>
    <script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js" />"></script>
    <script language="JavaScript" type="text/javascript">
      function topicGoTo(id)
      {
        location.href = "GoToTopic?Id="+id;
      }

      function sendData() {
        if (isCorrectForm()) {
          document.topicForm.submit();
        }
      }

      function cancelData()
      {
      <c:choose>
        <c:when test="${true eq requestScope.PopupDisplay}">
            window.close();
        </c:when>
        <c:otherwise>
            location.href = "GoToCurrentTopic";
        </c:otherwise>
      </c:choose>
        }

        function isCorrectForm() {
          var errorMsg = "";
          var errorNb = 0;
          var title = stripInitialWhitespace(document.topicForm.Name.value);
          if (isWhitespace(title)) {
            errorMsg+="  - '<fmt:message key="TopicTitle"/>' <fmt:message key="GML.MustBeFilled"/>\n";
            errorNb++;
          }
      <c:if test="${true eq requestScope.IsLink}">
          if (isWhitespace(stripInitialWhitespace(document.topicForm.Path.value))) {
            errorMsg+="  - '<fmt:message key="kmelia.Path"/>' <fmt:message key="GML.MustContainsText"/>\n";
            errorNb++;
          }
      </c:if>
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

  <body>
    <fmt:message var="addTopicBrowseTitle" key="TopicCreationTitle"/>
    <c:choose>
      	<c:when test="${true eq requestScope.PopupDisplay}">
      		<view:browseBar>
      			<view:browseBarElt id="${addTopicBrowseTitle}" label="${addTopicBrowseTitle}" link=""/>
      		</view:browseBar>
      	</c:when>
      	<c:otherwise>
      		<view:browseBar path="${requestScope.PathLinked}"/>
    	</c:otherwise>
    </c:choose>
    <view:window>
          <c:if test="${requestScope.Profiles != null and !empty requestScope.Profiles}">
            <view:tabs>
              <fmt:message var="defaultTabLabel" key="Theme" />
              <view:tab label="${defaultTabLabel}" action="#" selected="true" />
              <c:forEach items="${requestScope.Profiles}" var="theProfile" >
                <view:tab label="${theProfile.label}" action="#" selected="false" />
              </c:forEach>
            </view:tabs>
          </c:if>
          <view:frame>
          <view:board>
          <form name="topicForm" action="AddTopic" method="POST">
            <table cellpadding="5" width="100%">
              <tr><td class="txtlibform"><fmt:message key="TopicPath"/> :</td>
                <td valign="top"><c:out value="${requestScope.Path}" escapeXml='false'/></td>
              </tr>
              <%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
              <tr>
                <td class="txtlibform"><fmt:message key="TopicTitle"/> :</td>
                <td><input type="text" name="Name" size="60" maxlength="60"/><input type="hidden" name="ParentId" value="<c:out value="${requestScope.Parent.id}"/>"/>&nbsp;<img border="0" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
              </tr>
              <c:choose>
                <c:when test="${true eq requestScope.IsLink}">
                  <tr>
                    <td class="txtlibform"><fmt:message key="kmelia.Path" /> :</td>
                    <td><input type="text" name="Path" size="60" maxlength="200"/>&nbsp;<img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
                  </tr>
                </c:when>
                <c:otherwise>
                  <tr>
                    <td class="txtlibform"><fmt:message key="TopicDescription" /> :</td>
                    <td><input type="text" name="Description" size="60" maxlength="200"></td>
                  </tr>
                </c:otherwise>
              </c:choose>
              <c:if test="${true eq requestScope.NotificationAllowed}">
                <tr>
                  <td class="txtlibform" valign="top"><fmt:message key="TopicAlert" /> :</td>
                  <td valign="top">
                    <select name="AlertType">
                      <option value="NoAlert" selected="selected"><fmt:message key="NoAlert" /></option>
                      <option value="Publisher"><fmt:message key="OnlyPubsAlert" /></option>
                      <option value="All"><fmt:message key="AllUsersAlert" /></option>
                    </select>
                  </td>
                </tr>
              </c:if>
              <c:if test="${requestScope.Profiles != null and !empty requestScope.Profiles}">
              <tr>
                <td valign="top" class="txtlibform"><fmt:message key="kmelia.WhichTopicRightsUsed" /> :</td>
                <td valign="top">
                  <table width="235" cellpadding="0" cellspacing="0">
                    <tr>
                      <td width="201"><fmt:message key="kmelia.RightsSpecific"/></td>
                      <td width="20"><input type="radio" value="dummy" name="RightsUsed"></td>
                    </tr>
                    <tr>
                      <td width="201"><fmt:message key="kmelia.RightsInherited"/></td>
                      <td width="20"><input type="radio" value="father" name="RightsUsed" checked></td>
                    </tr>
                  </table>
                </td>
              </tr>
              </c:if>
              <tr>
                <td colspan="2">( <img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"> : <fmt:message key="GML.requiredField"/> )</td>
              </tr>
            </table>
          </form>
        </view:board>
        <br/><center>
          <view:buttonPane>
            <view:button action="javascript:onClick=sendData();" label="${validateButtonLabel}" disabled="false" />
            <view:button action="javascript:onClick=cancelData();" label="${cancelButtonLabel}" disabled="false" />
          </view:buttonPane>
        </center>
      </view:frame>
    </view:window>
  </body>
  <script language="javascript">
      document.topicForm.Name.focus();
  </script>
</html>