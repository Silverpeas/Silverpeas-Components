<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@ page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>


<%
      Form formUpdate = (Form) request.getAttribute("Form");
      DataRecord data = (DataRecord) request.getAttribute("Data");
      PublicationDetail pubDetail = (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
      String currentLang = (String) request.getAttribute("Language");
      boolean changingTemplateAllowed = ((Boolean) request.getAttribute("IsChangingTemplateAllowed")).booleanValue();
      String pubId = pubDetail.getPK().getId();
      String pubName = pubDetail.getName(currentLang);

      PagesContext context = new PagesContext("myForm", "2", resources.getLanguage(), false, componentId, kmeliaScc.getUserId());
      context.setObjectId(pubId);
      if (data != null) {
        context.setCreation(data.isNew());
      }

      if (kmeliaMode) {
        context.setNodeId(kmeliaScc.getCurrentFolderId());
      }
      context.setBorderPrinted(false);
      context.setContentLanguage(currentLang);
      context.setRequest(request);

      String linkedPathString = kmeliaScc.getSessionPath();

      boolean isOwner = kmeliaScc.getSessionOwner();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  	<title></title>
    <view:looknfeel/>
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
        location.href = "ViewPublication";
      }

      function showTranslation(lang) {
        location.href="ToPubliContent?SwitchLanguage="+lang;
      }
      
      function changeTemplate() {
    	  $("#dialog-confirm").dialog("open");  
      }
      
      $(function() {
  		$("#dialog-confirm").dialog({
  			autoOpen: false,
  			height: 200,
  			width: 350,
  			modal: true,
  			buttons: {
  				"<%=resources.getString("kmelia.template.change.confirm")%>": function() {
  					location.href="ChangeTemplate";
  				},
  				"<%=resources.getString("GML.cancel")%>": function() {
  					$( this ).dialog( "close" );
  				}
  			}
  		});
  	});
    </script>
  </head>
  <body class="yui-skin-sam">
    <%
          Window window = gef.getWindow();
          Frame frame = gef.getFrame();
          Board board = gef.getBoard();

          BrowseBar browseBar = window.getBrowseBar();
          browseBar.setDomainName(spaceLabel);
          browseBar.setComponentName(componentLabel, "Main");
          browseBar.setPath(linkedPathString);
          browseBar.setExtraInformation(pubName);
          browseBar.setI18N(pubDetail, currentLang);
          
          if (changingTemplateAllowed) {
          	OperationPane operations = window.getOperationPane();
          	operations.addOperation("useless", resources.getString("kmelia.template.change"), "javascript:onclick=changeTemplate();");
          }

          out.println(window.printBefore());

          if (isOwner) {
            KmeliaDisplayHelper.displayAllOperations(pubId, kmeliaScc, gef, "ModelUpdateView",
                  resources, out, kmaxMode);
          } else {
            KmeliaDisplayHelper.displayUserOperations(kmeliaScc, out);
          }

          out.println(frame.printBefore());
          out.println(board.printBefore());
    %>
    <form name="myForm" method="post" action="UpdateXMLForm" enctype="multipart/form-data" accept-charset="UTF-8">
      <%
            formUpdate.display(out, context, data);
      %>
      <input type="hidden" name="KmeliaPubFormName" value="<%=formUpdate.getFormName()%>"/>
    </form>
    <%
          out.println(board.printAfter());
    %>
          <view:buttonPane>
            <c:set var="saveLabel"><%=resources.getString("GML.validate")%></c:set>
            <c:set var="cancelLabel"><%=resources.getString("GML.cancel")%></c:set>
            <view:button label="${saveLabel}" action="javascript:onClick=B_VALIDER_ONCLICK();">
              <c:set var="contributionManagementContext" value="${requestScope.contributionManagementContext}"/>
              <c:if test="${not empty contributionManagementContext}">
                <c:set var="formData" value="<%=data%>"/>
                <jsp:useBean id="contributionManagementContext" type="org.silverpeas.core.contribution.util.ContributionManagementContext"/>
                <c:if test="${not empty formData and not formData.new
                              and contributionManagementContext.entityStatusBeforePersistAction.validated
                              and contributionManagementContext.entityStatusAfterPersistAction.validated
                              and contributionManagementContext.entityPersistenceAction.update}">
                  <view:handleContributionManagementContext
                      contributionId="${contributionManagementContext.contributionId}"
                      jsValidationCallbackMethodName="isCorrectForm"
                      subscriptionResourceType="${contributionManagementContext.linkedSubscriptionResource.type}"
                      subscriptionResourceId="${contributionManagementContext.linkedSubscriptionResource.id}"
                      contributionIndexable="<%=pubDetail.isIndexable()%>"
                      location="${contributionManagementContext.location}"/>

                </c:if>
              </c:if>
            </view:button>
            <view:button label="${cancelLabel}" action="javascript:onClick=B_ANNULER_ONCLICK();"/>
          </view:buttonPane>
    <%
          out.println(frame.printAfter());
          out.println(window.printAfter());%>
	<script type="text/javascript">
    	document.myForm.elements[1].focus();
  	</script>
  <div id="dialog-confirm" title="<%=resources.getString("kmelia.template.change")%> ?">
	<p><%=resources.getString("kmelia.template.change.info") %></p>
  </div>
  <view:progressMessage/>
  
  </body>
</html>
