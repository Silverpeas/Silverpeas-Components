<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<% com.stratelia.webactiv.kmelia.control.KmeliaSessionController kmeliaScc = (com.stratelia.webactiv.kmelia.control.KmeliaSessionController) request.getAttribute("kmelia");%>
  <% if(kmeliaScc == null ) {
    // No session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
    }
  %>
  <fmt:message var="cancelButtonLabel" key="GML.cancel"/>
  <fmt:message var="validateButtonLabel" key="GML.validate"/>
  <c:set var="node" value="${requestScope.NodeDetail}" scope="page"/>
  <%
  ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");
  String translation = (String) request.getAttribute("Translation");
  String language = (String) request.getAttribute("Language");
  NodeDetail node = (NodeDetail) request.getAttribute("NodeDetail");
  String rightsSpecificChecked = "";
  String rightsInheritedChecked = "checked";
  if (node.haveLocalRights())
  {
      rightsSpecificChecked = "checked";
      rightsInheritedChecked = "";
  }
  String name  = "";
  String description  = "";
  if (node != null) {
      name = node.getName(language);
      description = node.getDescription(language);
  }

  %>
  <html>
    <head>
      <view:looknfeel />
      <title><fmt:message key="GML.popupTitle" /></title>
      <script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js" />"></script>
      <script type="text/javascript" src="<c:url value="/util/javaScript/i18n.js" />"></script>
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
        <c:forEach items="${node.translations}" var="translation">
          <c:set var="lang" value="${translation.key}" scope="page"/>
          <%
          String lang = (String) pageContext.getAttribute("lang");
          out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(node.getName(lang))+"\";\n");
          out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(node.getDescription(lang))+"\";\n");
          %>
        </c:forEach>


            function showTranslation(lang)
            {
              showFieldTranslation('nodeName', 'name_'+lang);
              showFieldTranslation('nodeDesc', 'desc_'+lang);
            }

            function removeTranslation()
            {
              sendData();
            }
      </script>
    </head>
    <body>
      <fmt:message var="addTopicBrowseTitle" key="TopicUpdateTitle"/>
      <view:browseBar>
        <c:choose>
          <c:when test="${true eq requestScope.PopupDisplay}"><view:browseBarElt id="${addTopicBrowseTitle}" label="${addTopicBrowseTitle}" link=""/></c:when>
          <c:otherwise><view:browseBarElt id="Main" label="Main" link="${requestScope.PathLinked}"/></c:otherwise></c:choose>
      </view:browseBar>
      <view:window>
        <view:frame>
          <view:board>
            <c:if test="${requestScope.Profiles != null and !empty requestScope.Profiles}">
              <view:tabs>
                <fmt:message var="defaultTabLabel" key="Theme" />
                <view:tab label="${defaultTabLabel}" action="#" selected="true" />
                <c:forEach items="${requestScope.Profiles}" var="theProfile" >
                  <c:url var="profileAction" value="ViewTopicProfiles"><c:param name="Id" value="${theProfile.id}"/><c:param name="Role" value="${theProfile.name}"/></c:url>
                  <view:tab label="${theProfile.label}" action="${profileAction}" selected="false" />
                </c:forEach>
              </view:tabs>
            </c:if>
            <form name="topicForm" action="UpdateTopic" method="POST">
              <table CELLPADDING="5" WIDTH="100%">
                <tr>
                  <td class="txtlibform"><fmt:message key="TopicPath"/>  :</td>
                  <td valign="top"><c:out value="${requestScope.Path}" escapeXml='false'/><input type="hidden" name="ChildId" value="<c:out value="${pageScope.node.id}"/>"></td>
                </tr>
                <%=I18NHelper.getFormLine(resources, node, translation)%>
                <tr>
                  <td class="txtlibform"><fmt:message key="TopicTitle"/> :</td>
                  <td><input type="text" name="Name" id="nodeName" value="<%=EncodeHelper.javaStringToHtmlString(name)%>" size="60" maxlength="50">&nbsp;<img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
                </tr>
                <c:choose>
                  <c:when test="${true eq requestScope.IsLink}">
                    <tr>
                      <td class="txtlibform"><fmt:message key="kmelia.Path"/> :</td>
                      <td><input type="text" name="Path" value="<c:out value="${node.path}"/>" size="60" maxlength="200">&nbsp;<img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
                    </tr>
                  </c:when>
                  <c:otherwise>
                    <tr>
                      <td class="txtlibform"><fmt:message key="TopicDescription"/> :</td>
                      <td><input type="text" name="Description" id="nodeDesc" value="<%=EncodeHelper.javaStringToHtmlString(description)%>" size="60" maxlength="200"></td>
                    </tr>
                  </c:otherwise>
                </c:choose>
                <c:if test="${true eq requestScope.NotificationAllowed}">
                  <tr>
                    <td valign="top" class="txtlibform"><fmt:message key="TopicAlert" /> :</td>
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
                        <td width="201"><fmt:message key="kmelia.RightsSpecific" /></td>
                        <td width="20"><input type="radio" value="<c:out value="${node.nodePK.id}"/>" name="RightsUsed" <%=rightsSpecificChecked%>></td>
                      </tr>
                      <tr>
                        <td width="201"><fmt:message key="kmelia.RightsInherited" /></td>
                        <td width="20"><input type="radio" value="-1" name="RightsUsed" <%=rightsInheritedChecked%>></td>
                      </tr>
                    </table>
                  </td>
                </tr>
                </c:if>
                <tr>
                  <td colspan="2"><img border="0" alt="<fmt:message key="GML.requiredField" />" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField" /></td>
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
  </html>