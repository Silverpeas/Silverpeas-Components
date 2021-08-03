<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="checkKmelia.jsp" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>


<%
      Form formUpdate = (Form) request.getAttribute("Form");
      String currentLang = (String) request.getAttribute("Language");
      int nbPublis = (Integer) request.getAttribute("NumberOfSelectedPublications");

      PagesContext context = new PagesContext("myForm", "2", resources.getLanguage(), false, componentId, kmeliaScc.getUserId());
      context.setBorderPrinted(false);
      context.setContentLanguage(currentLang);
      context.setRequest(request);
      context.setUseBlankFields(true);
      context.setIgnoreDefaultValues(true);
      context.setUseMandatory(false);

      String linkedPathString = kmeliaScc.getSessionPath();
%>
<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="wysiwyg"/>
    <% formUpdate.displayScripts(out, context);%>
    <script type="text/javascript">
      function topicGoTo(id) {
        location.href="GoToTopic?Id="+id;
      }

      function isCorrectForm() {
        var result = false;
        ifCorrectFormExecute(function() {
          result = true;
        });
        return result;
      }

      function B_VALIDER_ONCLICK() {
        if (isCorrectForm()) {
          $.progressMessage();
          document.myForm.submit();
        }
      }

      function B_ANNULER_ONCLICK() {
        location.href = "GoToCurrentTopic";
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="yui-skin-sam">
    <view:browseBar componentId="<%=componentId%>" path="<%=linkedPathString%>" />
    <view:window>
      <view:frame>
        <div class="inlineMessage">
          <%=resources.getStringWithParams("kmelia.publications.batch.update", Integer.toString(nbPublis))%>
        </div>
      <view:board>
      <form name="myForm" method="post" action="UpdatePublications" enctype="multipart/form-data" accept-charset="UTF-8">
        <% formUpdate.display(out, context);%>
        <input type="hidden" name="KmeliaPubFormName" value="<%=formUpdate.getFormName()%>"/>
      </form>
      </view:board>
      <view:buttonPane>
        <c:set var="saveLabel"><%=resources.getString("GML.validate")%></c:set>
        <c:set var="cancelLabel"><%=resources.getString("GML.cancel")%></c:set>
        <view:button label="${saveLabel}" action="javascript:onClick=B_VALIDER_ONCLICK();">
          <c:set var="contributionBatchManagementContext" value="${requestScope.contributionBatchManagementContext}"/>
          <c:if test="${not empty contributionBatchManagementContext}">
            <jsp:useBean id="contributionBatchManagementContext" type="org.silverpeas.core.contribution.util.ContributionBatchManagementContext"/>
            <c:if test="${contributionBatchManagementContext.entityPersistenceAction.update}">
              <view:handleContributionBatchManagementContext
                  context="${contributionBatchManagementContext}"
                  jsValidationCallbackMethodName="isCorrectForm"/>
            </c:if>
          </c:if>
        </view:button>
        <view:button label="${cancelLabel}" action="javascript:onClick=B_ANNULER_ONCLICK();"/>
      </view:buttonPane>
      </view:frame>
    </view:window>
    <script type="text/javascript">
      document.myForm.elements[1].focus();
    </script>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>
