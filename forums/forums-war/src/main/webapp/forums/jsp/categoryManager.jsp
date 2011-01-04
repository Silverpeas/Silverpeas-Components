<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="sessionController" value="${requestScope.forumsSessionClientController}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<%@ include file="checkForums.jsp" %>
<c:choose>
  <c:when test="${requestScope.Category != null}">
    <c:set var="action" value="UpdateCategory" />
    <c:set var="name" value="${requestScope.Category.name}" />
    <c:set var="description" value="${requestScope.Category.description}" />
    <c:set var="categoryId" value="${requestScope.Category.nodePK.id}" />
    <c:set var="creationDate"><%= resources.getOutputDate(((NodeDetail)request.getAttribute("Category")).getCreationDate()) %></c:set>
  </c:when>
  <c:otherwise>
    <c:set var="action" value="CreateCategory" />
     <c:set var="name" value="" />
     <c:set var="description" value="" />
      <c:set var="categoryId" value="" />
      <c:set var="creationDate"><%= resources.getOutputDate(new Date()) %></c:set>
  </c:otherwise>
</c:choose> 
<html>
    <head>
          <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
<%
  out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript">
        // fonctions de controle des zones du formulaire avant validation
        function sendData(creation)
        {
            if (isCorrectForm())
            {
                document.forms["categoryForm"].action = (creation ? "CreateCategory" : "UpdateCategory");
                document.forms["categoryForm"].submit();
            }
        }

        function isCorrectForm()
        {
            var errorMsg = "";
            var errorNb = 0;
            var name = stripInitialWhitespace(document.categoryForm.Name.value);
            if (name == "")
            {
              errorMsg += "  - '<fmt:message key="GML.title" />'  " + "<fmt:message key="GML.MustBeFilled"/>\n";
                errorNb++;
            }

            if (errorNb > 0)
            {
                window.alert("<fmt:message key="GML.ThisFormContains" /> " + errorNb
                    + " " + (errorNb == 1 ? "<fmt:message key="GML.error" />" : "<fmt:message key="GML.errors"/>")
                    + " : \n" + errorMsg);
                return false;
            }
            else
            {
                return true;
            }
        }
    </script>
</head>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" <%addBodyOnload(out, fsc, "document.categoryForm.Name.focus();");%>>
  <c:choose>
  <c:when test="${'CreateCategory' eq action}">
    <fmt:message var="barLabel" key="forums.addCategory" />
  </c:when>
    <c:otherwise>
      <fmt:message var="barLabel" key="forums.editCategory" />
    </c:otherwise>
  </c:choose>
  <view:browseBar>
    <view:browseBarElt label="${barLabel}" link="#" />
  </view:browseBar>
  <view:window>
    <view:frame>
      <view:board>
    <table cellpadding="5" width="100%">
      <form name="categoryForm" action="<c:out value="${pageScope.action}" />" method="POST" enctype="multipart/form-data;charset=UTF-8">
        <input type="hidden" name="CategoryId" value="<c:out value="${categoryId}" />"/>
        <input type="hidden" name="Langue" value="<%=resources.getLanguage()%>"/>
        <tr>
          <td class="txtlibform"><fmt:message key="GML.title" /> :</td>
          <td><input type="text" name="Name" size="60" maxlength="150" value="<c:out value="${name}" />">
            <img src="<%=resources.getIcon("forums.obligatoire")%>" width="5" height="5" border="0">
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="GML.description" /> :</td>
          <td><input type="text" name="Description" size="60" maxlength="150" value="<c:out value="${description}" />" ></td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="forums.creationDate" /> :</td>
          <td><c:out value="${creationDate}" />&nbsp;<span class="txtlibform"></td>
        </tr>
        <tr>
          <td colspan="2">( <img border="0" src="<%=resources.getIcon("forums.obligatoire")%>" width="5" height="5"> : Obligatoire )</td>
        </tr>
      </form>
    </table><br/>
      <center>
        <fmt:message var="validateLabel" key="GML.validate" />
        <fmt:message var="cancelLabel" key="GML.cancel" />
        <c:set var="validateAction">javascript:onclick=sendData(<c:out value="${'CreateCategory' eq action}" />)</c:set>
        <view:buttonPane>
          <view:button label="${validateLabel}" action="${validateAction}"/>
          <view:button label="${cancelLabel}" action="Main"/>
        </view:buttonPane>
      </center>
      <br>   
    </view:board>
  </view:frame>
</view:window>
</body>
</html>