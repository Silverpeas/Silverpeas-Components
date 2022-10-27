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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@page import="org.silverpeas.components.kmelia.model.KmeliaPublication"%>
<%@page import="org.silverpeas.core.admin.user.model.User"%>
<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.io.media.image.thumbnail.ThumbnailSettings" %>
<%@ page import="org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail" %>
<%@ page import="org.silverpeas.core.notification.user.NotificationContext" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="org.silverpeas.core.contribution.model.Thumbnail" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>

<c:set var="attachmentsEnabled" value="${requestScope['AttachmentsEnabled']}"/>

<%@include file="checkKmelia.jsp" %>
<%@include file="topicReport.jsp" %>

<%
    String name = "";
    String description = "";
    String keywords = "";
    String creationDate = "";
    String beginDate = "";
    String endDate = "";
    String updateDate = "";
    String version = "";
    String importance = "";
    String status = "";
    String beginHour = "";
    String endHour = "";
    String author = "";
    String targetValidatorId = "";
    String targetValidatorName = "";
    String tempId = "";
    String infoId = "0";

    String nextAction = "";

    KmeliaPublication kmeliaPublication = (KmeliaPublication) request.getAttribute("Publication");
    User ownerDetail = null;
    User updater = null;

    PublicationDetail pubDetail = null;

    String language = kmeliaScc.getCurrentLanguage();

    String profile = (String) request.getAttribute("Profile");
    String action = (String) request.getAttribute("Action");
    String id = (String) request.getAttribute("PubId");
    String currentLang = (String) request.getAttribute("Language");
    List<NodeDetail> path = (List<NodeDetail>) request.getAttribute("Path");
    boolean draftOutTaxonomyOK = (Boolean) request.getAttribute("TaxonomyOK");
  	boolean draftOutValidatorsOK = (Boolean) request.getAttribute("ValidatorsOK");

    String resultThumbnail =  request.getParameter("resultThumbnail");
    boolean errorThumbnail = false;
    if(resultThumbnail != null && !"ok".equals(resultThumbnail)){
      errorThumbnail = true;
    }

    PublicationDetail volatilePublication = (PublicationDetail) request.getAttribute("VolatilePublication");
    Form extraForm = (Form) request.getAttribute("ExtraForm");
    PagesContext extraFormPageContext = (PagesContext) request.getAttribute("ExtraFormPageContext");
    if (extraFormPageContext != null) {
      extraFormPageContext.setFormName("pubForm");
    }

    //Icons
    String mandatorySrc = m_context + "/util/icons/mandatoryField.gif";
    String alertSrc = m_context + "/util/icons/alert.gif";
    String deletePubliSrc = m_context + "/util/icons/publicationDelete.gif";
    String pubDraftInSrc = m_context + "/util/icons/publicationDraftIn.gif";
    String pubDraftOutSrc = m_context + "/util/icons/publicationDraftOut.gif";
    String inDraftSrc = m_context + "/util/icons/masque.gif";
    String outDraftSrc = m_context + "/util/icons/visible.gif";
    String validateSrc = m_context + "/util/icons/ok.gif";
    String refusedSrc = m_context + "/util/icons/wrong.gif";
  	String favoriteAddSrc		= m_context + "/util/icons/addFavorit.gif";

    //Vrai si le user connecte est le createur de cette publication ou si il est admin
    boolean isOwner = false;

    boolean suppressionAllowed = false;

    boolean isFieldDescriptionVisible = kmeliaScc.isFieldDescriptionVisible();
    boolean isFieldDescriptionMandatory = kmeliaScc.isFieldDescriptionMandatory();
    boolean isFieldKeywordsVisible = kmeliaScc.isFieldKeywordsVisible();
    boolean isFieldImportanceVisible = kmeliaScc.isFieldImportanceVisible();
    boolean isFieldVersionVisible = kmeliaScc.isFieldVersionVisible();
    boolean isNotificationAllowed = kmeliaScc.isNotificationAllowed();
    ThumbnailSettings thumbnailSettings = kmeliaScc.getThumbnailSettings();
    Thumbnail thumbnail = null;

    String linkedPathString = displayPath(path, true, 3, language) + name;
    String pathString = displayPath(path, false, 3, language);

    //Action = View, New, Add, UpdateView, Update, Delete, LinkAuthorView, SameSubjectView ou SameTopicView
    if (action.equals("UpdateView") || action.equals("ValidateView")) {

      id = kmeliaPublication.getId();
      
      pubDetail = kmeliaPublication.getDetail();
      thumbnail = pubDetail.getThumbnail();
      ownerDetail = kmeliaPublication.getCreator();

      if (action.equals("ValidateView")) {
        kmeliaScc.setSessionOwner(true);
        action = "UpdateView";
        isOwner = true;
      } else {
        if (profile.equals("admin") || profile.equals("publisher") || profile.equals("supervisor") || (ownerDetail != null && kmeliaScc.getUserDetail().getId().equals(ownerDetail.getId()) && profile.equals("writer"))) {
          isOwner = true;

          if (!kmeliaScc.isSuppressionOnlyForAdmin() || (profile.equals("admin") && kmeliaScc.isSuppressionOnlyForAdmin())) {
            // suppressionAllowed = true car si c'est un redacteur, c'est le proprietaire de la publication
            suppressionAllowed = true;
          }
        } else if (!profile.equals("user") && kmeliaScc.isCoWritingEnable()) {
          // si publication en co-redaction, considerer qu'elle appartient aux co-redacteur au meme titre qu'au proprietaire
          // mais suppressionAllowed = false pour que le co-redacteur ne puisse pas supprimer la publication
          isOwner = true;
          suppressionAllowed = false;
        }

        if (isOwner) {
          kmeliaScc.setSessionOwner(true);
        } else {
          //modification pour acceder a l'onglet voir aussi
          kmeliaScc.setSessionOwner(false);
        }
      }

      name = WebEncodeHelper.javaStringToHtmlString(pubDetail.getName(language));
      description = WebEncodeHelper.javaStringToHtmlString(StringUtil.defaultIfBlank(pubDetail.getDescription(language), ""));
      creationDate = resources.getOutputDate(pubDetail.getCreationDate());
      if (pubDetail.getBeginDate() != null) {
        beginDate = resources.getInputDate(pubDetail.getBeginDate());
      } else {
        beginDate = "";
      }
      if (pubDetail.getEndDate() != null) {
        if (resources.getDBDate(pubDetail.getEndDate()).equals("1000/01/01")) {
          endDate = "";
        } else {
          endDate = resources.getInputDate(pubDetail.getEndDate());
        }
      } else {
        if (action.equals("View")) {
          endDate = "&nbsp;";
        } else {
          endDate = "";
        }
      }
      if (pubDetail.getLastUpdateDate() != null) {
        updateDate = resources.getOutputDate(pubDetail.getLastUpdateDate());
        updater = kmeliaScc.getUserDetail(pubDetail.getUpdaterId());
      } else {
        updateDate = "";
      }
      version = pubDetail.getVersion();
      importance = Integer.toString(pubDetail.getImportance());
      keywords = StringUtil.defaultIfBlank(pubDetail.getKeywords(language), "");
      status = pubDetail.getStatus();
      if (beginDate == null || beginDate.length() == 0) {
        beginHour = "";
      } else {
        beginHour = pubDetail.getBeginHour();
      }
      if (endDate == null || endDate.length() == 0) {
        endHour = "";
      } else {
        endHour = pubDetail.getEndHour();
      }

      if (beginHour == null) {
        beginHour = "";
      }
      if (endHour == null) {
        endHour = "";
      }

      author = StringUtil.defaultIfBlank(pubDetail.getAuthor(), "");
      targetValidatorId = pubDetail.getTargetValidatorId();

      if (StringUtil.isDefined(targetValidatorId)) {
        StringTokenizer tokenizer = new StringTokenizer(targetValidatorId, ",");
        while (tokenizer.hasMoreTokens()) {
          targetValidatorName += kmeliaScc.getUserDetail(tokenizer.nextToken()).getDisplayedName();
          if (tokenizer.hasMoreTokens()) {
            targetValidatorName += "\n";
          }
        }
      } else {
        targetValidatorId = "";
      }

      tempId = pubDetail.getCloneId();
      infoId = pubDetail.getInfoId();

      nextAction = "UpdatePublication";

    } else if ("New".equals(action)) {
      creationDate = resources.getOutputDate(new Date());
      beginDate = resources.getInputDate(new Date());
      tempId = "-1";

      nextAction = "AddPublication";
	}

	String backUrl = URLUtil.getFullApplicationURL(request) + URLUtil.getURL("kmelia", null, componentId) + "ToUpdatePublicationHeader";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title></title>
    <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
    <view:includePlugin name="datepicker"/>
    <view:includePlugin name="popup"/>
    <view:includePlugin name="wysiwyg"/>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>

    <% if (extraForm != null) { %>
      <% extraForm.displayScripts(out, extraFormPageContext);%>
    <% } %>

    <script type="text/javascript">
      function topicGoTo(id) {
        location.href="GoToTopic?Id="+id;
      }

      <% if (action.equals("UpdateView")) {%>

  	  function clipboardCopy() {
        top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy.jsp?Id=<%=id%>';
      }

      function clipboardCut() {
        top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut.jsp?Id=<%=id%>';
      }

      function pubDeleteConfirm(id) {
        var label = "<%=resources.getString("ConfirmDeletePub")%>";
        jQuery.popup.confirm(label, function() {
          document.toRouterForm.action = "<%=routerUrl%>DeletePublication";
          document.toRouterForm.PubId.value = id;
          document.toRouterForm.submit();
        });
      }

      function deleteCloneConfirm() {
        var label = "<%=WebEncodeHelper.javaStringToJsString(resources.getString("kmelia.ConfirmDeleteClone"))%>";
        jQuery.popup.confirm(label, function() {
          document.toRouterForm.action = "<%=routerUrl%>DeleteClone";
          document.toRouterForm.submit();
        });
      }

      function pubDraftIn() {
        location.href = "<%=routerUrl%>DraftIn";
      }

      function pubDraftOut() {
	      if (<%= draftOutTaxonomyOK && draftOutValidatorsOK %>) {
	        location.href = "<%=routerUrl%>DraftOut";
	      } else {
	    	$("#publication-draftout").dialog('open');
	      }
      }

      function alertUsers() {
      <% if (!"Valid".equals(pubDetail.getStatus())) { %>
          var label = "<%=WebEncodeHelper.javaStringToJsString(resources.getString("kmelia.AlertButPubNotValid"))%>";
          jQuery.popup.confirm(label, function() {
            sp.messager.open('<%= componentId %>', {
              nodeId: '<%= kmeliaScc.getCurrentFolderId()%>',
             <%= NotificationContext.PUBLICATION_ID %>: '<%= id %>'
            });
          });
      <% } else { %>
          sp.messager.open('<%= componentId %>', {
            nodeId: '<%= kmeliaScc.getCurrentFolderId()%>',
            <%= NotificationContext.PUBLICATION_ID %>: '<%= id %>'
        });
      <% } %>
      }

      <% }%>

      function sendData() {
        <% if (extraForm != null) { %>
          ifCorrectFormExecute(function() {
            if (isCorrectHeaderForm()) {
              reallySendData();
            }
          });
        <% } else { %>
          if (isCorrectHeaderForm()) {
            reallySendData();
          }
        <% } %>
      }

      function reallySendData() {
        <% if(!kmaxMode && "New".equals(action)) { %>
          <view:pdcPositions setIn="document.pubForm.KmeliaPubPositions.value"/>
        <% } %>
        document.pubForm.action = "<%=nextAction%>";
        setTimeout(function() {
          $.progressMessage();
          document.pubForm.submit();
        }, 0);
      }

      function closeWindows() {
        if (window.publicationWindow != null)
          window.publicationWindow.close();
        if (window.publicVersionsWindow != null)
          window.publicVersionsWindow.close();
      }

      function isCorrectHeaderForm() {
        var errorMsg = "";
        var errorNb = 0;
        var title = stripInitialWhitespace(document.pubForm.KmeliaPubName.value);

        if (isWhitespace(title)) {
          errorMsg+=" - '<%=resources.getString("PubTitre")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
          errorNb++;
        }

      <% if (isFieldDescriptionVisible) {%>
        var description = document.pubForm.KmeliaPubDescription;
      <% if (isFieldDescriptionMandatory) {%>
            if (isWhitespace(description.value)) {
              errorMsg+=" - '<%=resources.getString("PubDescription")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
              errorNb++;
            }
      <% }%>
            if (!isValidTextArea(description)) {
              errorMsg+=" - '<%=resources.getString("GML.description")%>' <%=resources.getString("kmelia.containsTooLargeText") + resources.getString("kmelia.nbMaxTextArea") + resources.getString("kmelia.characters")%>\n";
              errorNb++;
            }
      <% }%>

      <% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {%>
          var validatorId = stripInitialWhitespace(document.pubForm.KmeliaPubValideurId.value);
          if (isWhitespace(validatorId)) {
            errorMsg+=" - '<%=resources.getString("kmelia.Valideur")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
            errorNb++;
          }
      <% }%>

        var beginDate = {dateId : 'beginDate', hourId : 'beginHour'};
        var endDate = {dateId : 'endDate', hourId : 'endHour', defaultDateHour : '23:59'};
        var dateErrors = isPeriodEndingInFuture(beginDate, endDate);
        $(dateErrors).each(function(index, error) {
          errorMsg += " - " + error.message + "\n";
          errorNb++;
        });

        dateErrors = isDateFuture({dateId : 'dateReminder', canBeEqualToAnother: false});
        $(dateErrors).each(function(index, error) {
          errorMsg += " - " + error.message + "\n";
          errorNb++;
        });

        <% if(!kmaxMode && "New".equals(action)) { %>
        <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
        <% } %>
        
        var error = {
          msg: errorMsg, 
          nb: errorNb
        };
        
        <% if (kmeliaMode && settings.getBoolean("isVignetteVisible", true)) {%>
        checkThumbnail(error);
        <% } %>

        switch(error.nb) {
          case 0 :
            break;
          case 1 :
            error.msg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + error.msg;
            jQuery.popup.error(error.msg);
            break;
          default :
            error.msg = "<%=resources.getString("GML.ThisFormContains")%> " + error.nb + " <%=resources.getString("GML.errors")%> :\n" + error.msg;
            jQuery.popup.error(error.msg);
        }

        return (error.nb == 0);
      }

      <%
        if (pubDetail != null) {
          for(final String lang : pubDetail.getTranslations().keySet()){
            out.println("var name_" + lang + " = \"" + WebEncodeHelper.javaStringToJsString(pubDetail.getName(lang)) + "\";\n");
            out.println("var desc_" + lang + " = \"" + WebEncodeHelper.javaStringToJsString(pubDetail.getDescription(lang)) + "\";\n");
            out.println("var keys_" + lang + " = \"" + WebEncodeHelper.javaStringToJsString(pubDetail.getKeywords(lang)) + "\";\n");
          }
        }
      %>

        function showTranslation(lang)
        {
          showFieldTranslation('pubName', 'name_'+lang);
          try
          {
            showFieldTranslation('pubDesc', 'desc_'+lang);
          }
          catch (e)
          {
            //field description is not displayed
          }
          try
          {
            showFieldTranslation('pubKeys', 'keys_'+lang);
          }
          catch (e)
          {
            //field keywords is not displayed
          }
        }

        function removeTranslation()
        {
          document.pubForm.action = "UpdatePublication";
          document.pubForm.submit();
        }

        function addFavorite(name,description,url)
        {
          postNewLink(name, url, description);
        }

        $(document).ready(function(){
          	document.pubForm.KmeliaPubName.focus();

	        $("#publication-draftout").dialog({
	            autoOpen: false,
	            title: "<%=resources.getString("PubDraftOut")%>",
	            modal: true,
	            minWidth: 500,
	            resizable : false,
	            buttons: {
	              'OK': function() {
	                $(this).dialog("close");
	              }
	            }
	          });
        });
    </script>
  </head>
  <body id="<%=componentId%>" class="publicationManager" onunload="closeWindows()">
<%
        Window window = gef.getWindow();
        OperationPane operationPane = window.getOperationPane();

        Frame frame = gef.getFrame();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setPath(linkedPathString);
        browseBar.setExtraInformation(name);

        if ("UpdateView".equals(action)) {
          if (kmeliaScc.getSessionClone() == null && isNotificationAllowed) {
            operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:alertUsers();");
          }
          String urlPublication = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pubDetail.getPK().getId());
	      pathString = WebEncodeHelper.javaStringToHtmlString(pubDetail.getName(language));
	      String namePath = spaceLabel + " > " + componentLabel;
	      if (!pathString.equals("")) {
	      	namePath = namePath + " > " + pathString;
	      }
		  operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAddPublication")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+WebEncodeHelper.javaStringToJsString(namePath)+"','"+pubDetail.getDescription(language)+"','"+urlPublication+"')");
          operationPane.addLine();

          if (!"supervisor".equals(profile)) {
            if (kmeliaScc.getSessionClone() == null) {
              operationPane.addOperation(resources.getIcon("kmelia.copy"), resources.getString("GML.copy"), "javaScript:clipboardCopy()");

              if (suppressionAllowed) {
                // les boutons de suppression ne sont accessible qu'au redacteur de la publication
                operationPane.addOperation(resources.getIcon("kmelia.cut"), resources.getString("GML.cut"), "javaScript:clipboardCut()");
                operationPane.addLine();
                operationPane.addOperation(deletePubliSrc, resources.getString("GML.delete"), "javaScript:pubDeleteConfirm('" + id + "')");
              }
            } else {
              operationPane.addOperation(deletePubliSrc, resources.getString("kmelia.DeleteClone"), "javaScript:deleteCloneConfirm();");
            }
          }

          if (kmeliaScc.isDraftEnabled() && !"supervisor".equals(profile)) {
            operationPane.addLine();
            if (pubDetail.isDraft()) {
              operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"), "javaScript:pubDraftOut()");
            } else if (pubDetail.isRefused() || pubDetail.isValid()) {
              operationPane.addOperation(pubDraftInSrc, resources.getString("PubDraftIn"), "javaScript:pubDraftIn()");
            }
          }
        }
        out.println(window.printBefore());
        if (isOwner) {
          KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, action, resources, out,
                kmaxMode);
        } else {
          KmeliaDisplayHelper.displayOnNewOperations(kmeliaScc, out);
        }

        out.println(frame.printBefore());
%>

  <div id="header">
  <form name="pubForm" action="#" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
  	<input type="hidden" name="Action"/>
    <input type="hidden" name="KmeliaPubPositions"/>
  	<input type="hidden" name="KmeliaPubId" value="<%=id%>"/>
  	<input type="hidden" name="KmeliaPubStatus" value="<%=status%>"/>
  	<input type="hidden" name="KmeliaPubTempId" value="<%=tempId%>"/>
  	<input type="hidden" name="KmeliaPubInfoId" value="<%=infoId%>"/>

    <fieldset id="pubInfo" class="skinFieldset">
			<legend><%=resources.getString("kmelia.header.fieldset.main") %></legend>
			<div class="fields">
				<% if (kmeliaScc.isPublicationIdDisplayed() && action.equals("UpdateView")) {%>
        		<div class="field" id="codificationArea">
					<label class="txtlibform"><%=resources.getString("kmelia.codification")%></label>
					<div class="champs">
						<%=pubDetail.getPK().getId()%>
					</div>
				</div>
      			<% } %>
				<% if (kmeliaMode && "UpdateView".equals(action)) {%>
				<div class="field" id="stateArea">
					<label class="txtlibform"><%=resources.getString("PubState")%></label>
					<div class="champs">
						<% if ("ToValidate".equals(status)) { %>
              				<img src="<%=outDraftSrc %>" alt="<%=resources.getString("PubStateToValidate") %>"/> <%=resources.getString("PubStateToValidate") %>
            		 	<% } else if ("Draft".equals(status)) { %>
            		 		<img src="<%=inDraftSrc %>" alt="<%=resources.getString("PubStateDraft") %>"/> <%=resources.getString("PubStateDraft")%>
            			<% } else if ("Valid".equals(status)) { %>
            				<img src="<%=validateSrc %>" alt="<%=resources.getString("PublicationValidated") %>"/> <%=resources.getString("PublicationValidated") %>
            			<% } else if ("UnValidate".equals(status)) { %>
            				<img src="<%=refusedSrc %>" alt="<%=resources.getString("PublicationRefused") %>"/> <%=resources.getString("PublicationRefused") %>
            			<% } %>
					</div>
				</div>
				<% } %>

				<% if (I18NHelper.isI18nContentActivated) { %>
				<div class="field" id="languageArea">
					<label for="language" class="txtlibform"><%=resources.getString("GML.language")%></label>
					<div class="champs">
						<%=I18NHelper.getHTMLSelectObject(resources.getLanguage(), pubDetail, language) %>
					</div>
				</div>
				<% } %>

				<div class="field" id="pubNameArea">
					<label for="pubName" class="txtlibform"><%=resources.getString("PubTitre")%></label>
					<div class="champs">
						<input type="text" name="KmeliaPubName" id="pubName" value="<%=name%>" size="68" maxlength="400" />&nbsp;<img src="<%=mandatorySrc%>" width="5" height="5" border="0"/>
					</div>
				</div>

				<% if (isFieldDescriptionVisible) {%>
				<div class="field" id="descriptionArea">
					<label for="pubDesc" class="txtlibform"><%=resources.getString("PubDescription")%></label>
					<div class="champs">
						<textarea rows="4" cols="65" name="KmeliaPubDescription" id="pubDesc"><%=description%></textarea>
						<% if (isFieldDescriptionMandatory) {%>
          					<img src="<%=mandatorySrc%>" width="5" height="5" border="0"/>
          				<% }%>
					</div>
				</div>
				<% } %>

				<% if (isFieldKeywordsVisible) {%>
				<div class="field" id="keywordsArea">
					<label for="pubKeys" class="txtlibform"><%=resources.getString("PubMotsCles")%></label>
					<div class="champs">
						<input type="text" name="KmeliaPubKeywords" id="pubKeys" value="<%=keywords%>" size="68" maxlength="1000" />
					</div>
				</div>
				<% } %>
				<% if (kmeliaScc.isAuthorUsed()) {%>
				<div class="field" id="authorArea">
					<label for="author" class="txtlibform"><%=resources.getString("GML.author")%></label>
					<div class="champs">
						<input type="text" id="author" name="KmeliaPubAuthor" value="<%=author%>" size="68" maxlength="50" />
					</div>
				</div>
				<% } %>

				<% if (isFieldVersionVisible) { %>
				<div class="field" id="versionArea">
					<label for="version" class="txtlibform"><%=resources.getString("PubVersion")%></label>
					<div class="champs">
						<input type="text" id="version" name="KmeliaPubVersion" value="<%=WebEncodeHelper.javaStringToHtmlString(version)%>" size="5" maxlength="30" />
					</div>
				</div>
				<% } %>

				<% if (isFieldImportanceVisible) { %>
				<div class="field" id="importanceArea">
					<label for="importance" class="txtlibform"><%=resources.getString("PubImportance")%></label>
					<div class="champs">
						<select id="importance" name="KmeliaPubImportance">
							<% 	if (importance.equals("")) {
                 					importance = "1";
               					}
               					int importanceInt = Integer.parseInt(importance);
               					for (int i = 1; i <= 5; i++) {
	                 				if (i == importanceInt) {
	                   					out.println("<option selected=\"selected\" value=\"" + i + "\">" + i + "</option>");
	                 				} else {
	                   					out.println("<option value=\"" + i + "\">" + i + "</option>");
	                 				}
               					}
            				%>
						</select>
					</div>
				</div>
				<% } else {%>
      				<input type="hidden" name="KmeliaPubImportance" value="1" />
      			<% } %>

      			<% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {
			           String selectUserLab = resources.getString("kmelia.SelectValidator");
			           String link = "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');\">";
			           link += "<img src=\""
			               + resources.getIcon("kmelia.user")
			               + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
			               + selectUserLab + "\" align=\"absmiddle\" title=\""
			               + selectUserLab + "\"></a>";
			    %>
			    <div class="field" id="validatorArea">
					<label for="Valideur" class="txtlibform"><%=resources.getString("kmelia.Valideur")%></label>
					<div class="champs">
						<% if (kmeliaScc.isTargetValidationEnable()) {%>
          					<input type="text" name="Valideur" id="Valideur" value="<%=targetValidatorName%>" size="60" readonly="readonly"/>
          				<% } else {%>
          					<textarea name="Valideur" id="Valideur" rows="4" cols="40" readonly="readonly"><%=targetValidatorName%></textarea>
          				<% }%>
          				<input type="hidden" name="KmeliaPubValideurId" id="ValideurId" value="<%=targetValidatorId%>"/><%=link%>&nbsp;<img src="<%=mandatorySrc%>" width="5" height="5" border="0"/>
					</div>
				</div>
				<% } %>

			    <% if (kmeliaPublication != null) { %>
				<div class="field" id="creationArea">
					<label class="txtlibform"><%=resources.getString("kmelia.header.contributors") %></label>
					<% if (StringUtil.isDefined(updateDate) && updater != null) {%>
					<div class="champs">
						<%=resources.getString("PubDateUpdate")%> <br /><b><%=updateDate%></b> <%=resources.getString("kmelia.By")%> <view:username userId="<%=kmeliaPublication.getLastUpdater().getId()%>"/>
						<div class="profilPhoto"><view:image src="<%=kmeliaPublication.getLastUpdater().getAvatar() %>" type="avatar" css="defaultAvatar"/></div>
					</div>
					<% } %>
				</div>
				<div class="field" id="updateArea">
					<div class="champs">
						<%=resources.getString("PubDateCreation")%> <br /><b><%=creationDate%></b> <%=resources.getString("kmelia.By")%> <view:username userId="<%=kmeliaPublication.getCreator().getId()%>"/>
						<div class="profilPhoto"><view:image src="<%=kmeliaPublication.getCreator().getAvatar() %>" type="avatar" css="defaultAvatar"/></div>
					</div>
				</div>
				<% } %>

			</div>
		</fieldset>

		<div class="table">
		<div class="cell">
			<fieldset id="pubDates" class="skinFieldset">
				<legend><%=resources.getString("kmelia.header.period") %></legend>
				<div class="fields">
					<div class="field" id="beginArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("PubDateDebut")%></label>
						<div class="champs">
							<input id="beginDate" type="text" class="dateToPick" name="KmeliaPubBeginDate" value="<%=beginDate%>" size="12" maxlength="10"/>
							<span class="txtsublibform">&nbsp;<%=resources.getString("ToHour")%>&nbsp;</span>
							<input id="beginHour" class="inputHour" type="text" name="KmeliaPubBeginHour" value="<%=beginHour%>" size="5" maxlength="5" /> <i>(hh:mm)</i>
						</div>
					</div>
					<div class="field" id="endArea">
						<label for="endDate" class="txtlibform"><%=resources.getString("PubDateFin")%></label>
						<div class="champs">
							<input id="endDate" type="text" class="dateToPick" name="KmeliaPubEndDate" value="<%=endDate %>" size="12" maxlength="10"/>
							<span class="txtsublibform">&nbsp;<%=resources.getString("ToHour")%>&nbsp;</span>
							<input id="endHour" class="inputHour" type="text" name="KmeliaPubEndHour" value="<%=endHour %>" size="5" maxlength="5" /> <i>(hh:mm)</i>
						</div>
					</div>
				</div>
			</fieldset>
		</div>
		<% if (kmeliaMode && settings.getBoolean("isVignetteVisible", true)) {%>
		<div class="cell">
			<fieldset id="pubThumb" class="skinFieldset">
				<legend><%=resources.getString("GML.thumbnail")%></legend>
				<viewTags:displayThumbnail thumbnail="<%=thumbnail %>" mandatory="<%=thumbnailSettings.isMandatory() %>" componentId="<%=componentId %>" objectId="<%=id %>" backURL="<%=backUrl%>" objectType="<%=ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE %>" width="<%=thumbnailSettings.getWidth() %>" height="<%=thumbnailSettings.getHeight() %>"/>
			</fieldset>
		</div>
		<% } %>

	</div>

    <c:if test="${attachmentsEnabled}">
      <view:fileUpload fieldset="true" jqueryFormSelector="form[name='pubForm']" />
    </c:if>

    <% if (kmeliaScc.isReminderUsed()) {%>
    <div class="table">
      <div class="cell">
        <view:dateReminder resourceId="<%=id %>" resourceType="<%=PublicationDetail.getResourceType() %>" userId="<%= kmeliaScc.getUserId()%>" language="<%= language%>"/>
      </div>
    </div>
    <% } %>

    <% if (extraForm != null) { %>
    <fieldset id="pubExtraForm" class="skinFieldset">
      <legend><%=resources.getString("Model")%></legend>
      <input type="hidden" name="KmeliaPubFormName" value="<%=extraForm.getFormName()%>"/>
      <input type="hidden" name="KmeliaPubVolatileId" value="<%=volatilePublication.getId()%>"/>
      <%
        extraForm.display(out, extraFormPageContext);
      %>
    </fieldset>
    <% } %>

    <% if (!kmaxMode) {
        if ("New".equals(action)) { %>
          	<view:pdcNewContentClassification componentId="<%= componentId %>" nodeId="<%= kmeliaScc.getCurrentFolderId() %>"/>
    <%  } else { %>
    	<% if (!"-1".equals(pubDetail.getCloneId()) && !StringUtil.isDefined(pubDetail.getCloneStatus())) { %>
    		<!-- positions are only editable on original publication -->
    		<view:pdcClassification componentId="<%= componentId %>" contentId="<%= pubDetail.getCloneId() %>" editable="false" />
    	<% } else { %>
    		<view:pdcClassification componentId="<%= componentId %>" contentId="<%= id %>" editable="true" />
    	<% } %>
    <%  }
      } %>

	<div class="legend">
		<img src="<%=mandatorySrc%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%>
	</div>

  </form>
  </div>
  <%
        out.println(frame.printMiddle());
        if(isOwner || !"user".equalsIgnoreCase(profile)) {
          %>
          <view:buttonPane>
            <c:set var="saveLabel"><%=resources.getString("GML.validate")%></c:set>
            <c:set var="saveAction"><%="javascript:onClick=sendData();"%></c:set>
            <c:set var="cancelLabel"><%=resources.getString("GML.cancel")%></c:set>
            <view:button label="${saveLabel}" action="${saveAction}">
              <c:set var="contributionManagementContext" value="${requestScope.contributionManagementContext}"/>
              <c:if test="${not empty contributionManagementContext}">
                <jsp:useBean id="contributionManagementContext" type="org.silverpeas.core.contribution.util.ContributionManagementContext"/>
                <c:if test="${contributionManagementContext.entityStatusBeforePersistAction.validated
                      and contributionManagementContext.entityStatusAfterPersistAction.validated
                      and contributionManagementContext.entityPersistenceAction.update}">
                  <view:handleContributionManagementContext
                      contributionId="${contributionManagementContext.contributionId}"
                      jsValidationCallbackMethodName="isCorrectHeaderForm"
                      subscriptionResourceType="${contributionManagementContext.linkedSubscriptionResource.type}"
                      subscriptionResourceId="${contributionManagementContext.linkedSubscriptionResource.id}"
                      contributionIndexable="<%=pubDetail.isIndexable()%>"
                      location="${contributionManagementContext.location}"/>

                </c:if>
              </c:if>
            </view:button>
            <view:button label="${cancelLabel}" action="GoToCurrentTopic"/>
          </view:buttonPane>
          <%
        }
        out.println(frame.printAfter());
        out.println(window.printAfter());
  %>
  <form name="toRouterForm">
    <input type="hidden" name="PubId" value="<%=id%>"/>
  </form>
  <div id="publication-draftout" style="display: none;">
      	<%=resources.getString("kmelia.publication.draftout.impossible")%>
      	<ul>
      	<% if(!draftOutTaxonomyOK) { %>
      		<li><%=resources.getString("kmelia.PdcClassificationMandatory")%></li>
      	<% } %>
      	<% if(!draftOutValidatorsOK) { %>
      		<li><%=resources.getString("kmelia.publication.validators.mandatory")%></li>
      	<% } %>
      	</ul>
      </div>
  <view:progressMessage/>
</body>
</html>