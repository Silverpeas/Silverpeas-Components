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
<%@page import="com.stratelia.webactiv.forums.control.helpers.ForumListHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.forums.multilang.forumsBundle"/>

<%
  int messageId = getIntParameter(request, "params");
  Message message = fsc.getMessage(messageId);
  int forumId = message.getForumId();
  String backUrl = ActionUrl.getUrl("viewMessage", "viewForum", 1, messageId, forumId);

  //Icons
  String mandatoryField = context + "/util/icons/mandatoryField.gif";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
  <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
  <script type="text/javascript">
    function validateMessage() {
      if (!$('textarea').val().trim()) {
        window.alert("'<fmt:message key="RefusalMotive" /> <fmt:message key="GML.MustBeFilled" />'");
      } else {
        document.refusalForm.action = "RefuseMessage";
        document.refusalForm.submit();
      }
    }
  </script>
</head>
<body>
<view:browseBar path="<%=ForumListHelper.navigationBar(forumId, resource, fsc)%>"/>
<view:window>
  <view:frame>
    <view:board>
      <form name="refusalForm" action="#" method="post">
        <table cellpadding="5" cellspacing="0" border="0" width="100%">
          <tr>
            <td></td>
            <td valign="top"><%=EncodeHelper.javaStringToHtmlString(message.getTitle())%>
            </td>
          </tr>
          <tr>
            <td class="txtlibform" valign=top><%=resource.getString("RefusalMotive")%> :</td>
            <td>
              <textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
              <input type="hidden" name="params" value="<%=messageId%>"/>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <img border="0" src="<%=mandatoryField%>" width="5" height="5"/> : <%=resources
                .getString("GML.requiredField")%>
            </td>
          </tr>
        </table>
      </form>
    </view:board>
    <br/>
    <view:buttonPane>
      <fmt:message key="valider" var="tmpLabel"/>
      <view:button action="javascript:onClick=validateMessage();" label="${tmpLabel}"/>
      <fmt:message key="annuler" var="tmpLabel"/>
      <view:button action="<%=backUrl%>" label="${tmpLabel}"/>
    </view:buttonPane>
  </view:frame>
</view:window>
</body>
</html>