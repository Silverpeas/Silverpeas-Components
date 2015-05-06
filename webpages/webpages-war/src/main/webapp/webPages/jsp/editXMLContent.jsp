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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>

<%@page import="com.silverpeas.form.DataRecord" %>
<%@page import="com.silverpeas.form.Form" %>
<%@page import="com.silverpeas.form.PagesContext" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%
  Form formUpdate = (Form) request.getAttribute("Form");
  DataRecord data = (DataRecord) request.getAttribute("Data");

  PagesContext context =
      new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, "useless");
  context.setObjectId("0");
  context.setBorderPrinted(false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <% formUpdate.displayScripts(out, context);%>
  <script type="text/javascript">
    function save() {
      if (isCorrectForm()) {
        $.progressMessage();
        document.myForm.submit();
      }
    }

    function cancel() {
      location.href = "Main";
    }
  </script>
</head>
<body class="yui-skin-sam">
<view:window>
  <view:tabs>
    <fmt:message key="webPages.preview" var="tmpLabel"/>
    <view:tab label="${tmpLabel}" action="Preview" selected="false"/>
    <fmt:message key="webPages.edit" var="tmpLabel"/>
    <view:tab label="${tmpLabel}" action="Edit" selected="true"/>
  </view:tabs>
  <view:frame>
    <table width="100%" border="0">
      <tr>
        <td id="richContent">
          <form name="myForm" method="post" action="UpdateXMLContent" enctype="multipart/form-data" accept-charset="UTF-8">
            <%
              formUpdate.display(out, context, data);
            %>
          </form>
        </td>
      </tr>
    </table>

    <view:buttonPane>
      <c:set var="saveLabel"><%=resource.getString("GML.validate")%>
      </c:set>
      <c:set var="cancelLabel"><%=resource.getString("GML.cancel")%>
      </c:set>
      <view:button label="${saveLabel}" action="javascript:onClick=save();">
        <c:set var="subscriptionManagementContext" value="${requestScope.subscriptionManagementContext}"/>
        <c:if test="${not empty subscriptionManagementContext}">
          <c:set var="formData" value="<%=data%>"/>
          <jsp:useBean id="subscriptionManagementContext" type="com.silverpeas.subscribe.util.SubscriptionManagementContext"/>
          <c:if test="${not empty formData and not formData.new
                    and subscriptionManagementContext.entityPersistenceAction.update}">
            <view:confirmComponentSubscriptionNotificationSending
                jsValidationCallbackMethodName="isCorrectForm"/>
          </c:if>
        </c:if>
      </view:button>
      <view:button label="${cancelLabel}" action="javascript:onClick=cancel();"/>
    </view:buttonPane>

  </view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>