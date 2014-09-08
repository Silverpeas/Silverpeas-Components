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

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@page import="com.silverpeas.thumbnail.ThumbnailSettings"%>
<%@page import="com.silverpeas.thumbnail.model.ThumbnailDetail"%>
<%@page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@page import="com.stratelia.webactiv.util.FileServerUtils" %>
<%@page import="org.silverpeas.kmelia.jstl.KmeliaDisplayHelper"%>
<%@page import="org.silverpeas.util.URLUtils"%>
<%@page import="com.stratelia.webactiv.kmelia.model.KmeliaPublication" %>

<%@include file="checkKmelia.jsp" %>
<%@include file="publicationsList.jsp.inc" %>
<%@include file="topicReport.jsp.inc" %>


<%!  //Icons
  String mandatorySrc;
  String deleteSrc;
  String alertSrc;
  String deletePubliSrc;
  String clipboardCopySrc;
  String pubDraftInSrc;
  String pubDraftOutSrc;
  String inDraftSrc;
  String outDraftSrc;
  String validateSrc;
  String refusedSrc;

// Fin des declarations
%>

<%
    String name = "";
    String description = "";
    String keywords = "";
    String content = "";
    String creationDate = "";
    String beginDate = "";
    String endDate = "";
    String updateDate = "";
    String version = "";
    String importance = "";
    String pubName = "";
    String vignette = "";
    String status = "";
    String beginHour = "";
    String endHour = "";
    String author = "";
    String targetValidatorId = "";
    String targetValidatorName = "";
    String tempId = "";
    String infoId = "0";
    String draftOutDate = "";

    String nextAction = "";

    ResourceLocator publicationSettings = new ResourceLocator("org.silverpeas.util.publication.publicationSettings", resources.getLanguage());

    KmeliaPublication kmeliaPublication = (KmeliaPublication) request.getAttribute("Publication");
    UserDetail ownerDetail = null;
    UserDetail updater = null;

    PublicationDetail pubDetail = null;

    String language = kmeliaScc.getCurrentLanguage();

    String profile = (String) request.getAttribute("Profile");
    String action = (String) request.getAttribute("Action");
    String id = (String) request.getAttribute("PubId");
    String wizard = (String) request.getAttribute("Wizard");
    String currentLang = (String) request.getAttribute("Language");
    List<NodeDetail> path = (List<NodeDetail>) request.getAttribute("Path");
    boolean draftOutTaxonomyOK = (Boolean) request.getAttribute("TaxonomyOK");
  	boolean draftOutValidatorsOK = (Boolean) request.getAttribute("ValidatorsOK");

    String resultThumbnail =  request.getParameter("resultThumbnail");
    boolean errorThumbnail = false;
	if(resultThumbnail != null && !"ok".equals(resultThumbnail)){
		errorThumbnail = true;
	}

    SilverTrace.info("kmelia", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ACTION pubManager = " + action);

    //Icons
    mandatorySrc = m_context + "/util/icons/mandatoryField.gif";
    deleteSrc = m_context + "/util/icons/delete.gif";
    alertSrc = m_context + "/util/icons/alert.gif";
    deletePubliSrc = m_context + "/util/icons/publicationDelete.gif";
    clipboardCopySrc = m_context + "/util/icons/copy.gif";
    pubDraftInSrc = m_context + "/util/icons/publicationDraftIn.gif";
    pubDraftOutSrc = m_context + "/util/icons/publicationDraftOut.gif";
    inDraftSrc = m_context + "/util/icons/masque.gif";
    outDraftSrc = m_context + "/util/icons/visible.gif";
    validateSrc = m_context + "/util/icons/ok.gif";
    refusedSrc = m_context + "/util/icons/wrong.gif";
  	String favoriteAddSrc		= m_context + "/util/icons/addFavorit.gif";

    String screenMessage = "";

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
    ThumbnailDetail thumbnail = null;

    boolean isAutomaticDraftOutEnabled = StringUtil.isDefined(resources.getSetting("cronAutomaticDraftOut"));

    String linkedPathString = displayPath(path, true, 3, language) + name;
    String pathString = displayPath(path, false, 3, language);

    Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "GoToCurrentTopic", false);
    Button validateButton = null;

//Action = View, New, Add, UpdateView, Update, Delete, LinkAuthorView, SameSubjectView ou SameTopicView
    if (action.equals("UpdateView") || action.equals("ValidateView")) {

      id = kmeliaPublication.getId();
      
      pubDetail = kmeliaPublication.getDetail();
      pubName = pubDetail.getName(language);
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

      name = EncodeHelper.javaStringToHtmlString(pubDetail.getName(language));
      description = EncodeHelper.javaStringToHtmlString(StringUtil.defaultIfBlank(pubDetail.getDescription(language), ""));
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
      if (pubDetail.getUpdateDate() != null) {
        updateDate = resources.getOutputDate(pubDetail.getUpdateDate());
        updater = kmeliaScc.getUserDetail(pubDetail.getUpdaterId());
      } else {
        updateDate = "";
      }
      version = pubDetail.getVersion();
      importance = Integer.toString(pubDetail.getImportance());
      keywords = StringUtil.defaultIfBlank(pubDetail.getKeywords(language), "");
      content = pubDetail.getContent();
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

      if (pubDetail.getDraftOutDate() != null) {
        draftOutDate = resources.getInputDate(pubDetail.getDraftOutDate());
      }

      nextAction = "UpdatePublication";

    } else if ("New".equals(action)) {
      creationDate = resources.getOutputDate(new Date());
      beginDate = resources.getInputDate(new Date());
      tempId = "-1";

      nextAction = "AddPublication";
	}

    validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendPublicationDataToRouter('" + nextAction + "');", false);

	String backUrl = URLManager.getFullApplicationURL(request) + URLManager.getURL("kmelia", null, componentId) + "ToUpdatePublicationHeader";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title></title>
    <view:looknfeel/>
    <view:includePlugin name="datepicker"/>
    <view:includePlugin name="popup"/>
    <link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
    <script type="text/javascript">
      var favoriteWindow = window;

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
        if(window.confirm("<%=resources.getString("ConfirmDeletePub")%>")){
          document.toRouterForm.action = "<%=routerUrl%>DeletePublication";
          document.toRouterForm.PubId.value = id;
          document.toRouterForm.submit();
        }
      }

      function deleteCloneConfirm() {
        if(window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString("kmelia.ConfirmDeleteClone"))%>")){
          document.toRouterForm.action = "<%=routerUrl%>DeleteClone";
          document.toRouterForm.submit();
        }
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

      function alertUsers()
      {
      <%
         if (!"Valid".equals(pubDetail.getStatus())) {
      %>
          if (window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString("kmelia.AlertButPubNotValid"))%>"    ))
              {
                goToOperationInAnotherWindow('ToAlertUser', '<%=id%>', 'ViewAlert');
              }
      <%
              } else {
      %>
              goToOperationInAnotherWindow('ToAlertUser', '<%=id%>', 'ViewAlert');
      <%
             }
      %>
        }

      <% }%>

      function sendPublicationDataToRouter(func) {
        if (isCorrectForm()) {
          <% if(!kmaxMode && "New".equals(action)) { %>
                <view:pdcPositions setIn="document.pubForm.Positions.value"/>
          <% } %>
          document.pubForm.action = func;
          document.pubForm.submit();
        }
      }

      function closeWindows() {
        if (window.publicationWindow != null)
          window.publicationWindow.close();
        if (window.publicVersionsWindow != null)
          window.publicVersionsWindow.close();
      }
      
      function getExtension(filename) {
    	  var indexPoint = filename.lastIndexOf(".");
    	  // on verifie qu il existe une extension au nom du fichier
    	  if (indexPoint != -1) {
    	    // le fichier contient une extension. On recupere l extension
    	    var ext = filename.substring(indexPoint + 1);
    	    return ext;
    	  }
    	  return null;
    	}

      function isCorrectForm() {
        var errorMsg = "";
        var errorNb = 0;
        var title = stripInitialWhitespace(document.pubForm.Name.value);

        if (isWhitespace(title)) {
          errorMsg+=" - '<%=resources.getString("PubTitre")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
          errorNb++;
        }

      <% if (isFieldDescriptionVisible) {%>
        var description = document.pubForm.Description;
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
          var validatorId = stripInitialWhitespace(document.pubForm.ValideurId.value);
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

        <% if(!kmaxMode && "New".equals(action)) { %>
        <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
        <% } %>
        
        var error = {
          msg: errorMsg, 
          nb: errorNb
        };
        checkThumbnail(error);

        var result = false;
        switch(error.nb) {
          case 0 :
            result = true;
            break;
          case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            break;
          default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            break;
        }
        return result;
      }

      <%
        if (pubDetail != null) {
          for(final String lang : pubDetail.getTranslations().keySet()){
            out.println("var name_" + lang + " = \"" + EncodeHelper.javaStringToJsString(pubDetail.getName(lang)) + "\";\n");
            out.println("var desc_" + lang + " = \"" + EncodeHelper.javaStringToJsString(pubDetail.getDescription(lang)) + "\";\n");
            out.println("var keys_" + lang + " = \"" + EncodeHelper.javaStringToJsString(pubDetail.getKeywords(lang)) + "\";\n");
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
          	document.pubForm.Name.focus();

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
        Board board = gef.getBoard();

        // added by LBE : importance field can be hidden (depends on settings file)
        boolean showImportance = !"no".equalsIgnoreCase(resources.getSetting("showImportance"));

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "Main");
        browseBar.setPath(linkedPathString);
        browseBar.setExtraInformation(name);

        if ("UpdateView".equals(action)) {
          if (kmeliaScc.getSessionClone() == null && isNotificationAllowed) {
            operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:alertUsers();");
          }
          String urlPublication = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId());
	      pathString = EncodeHelper.javaStringToHtmlString(pubDetail.getName(language));
	      String namePath = spaceLabel + " > " + componentLabel;
	      if (!pathString.equals("")) {
	      	namePath = namePath + " > " + pathString;
	      }
		  operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAddPublication")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+EncodeHelper.javaStringToJsString(namePath)+"','"+pubDetail.getDescription(language)+"','"+urlPublication+"')");
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
            if ("Draft".equals(pubDetail.getStatus())) {
              operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"), "javaScript:pubDraftOut()");
            } else {
              operationPane.addOperation(pubDraftInSrc, resources.getString("PubDraftIn"), "javaScript:pubDraftIn()");
            }
          }
        }
        out.println(window.printBefore());
        if (isOwner) {
          KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, action, resources, out,
                kmaxMode);
        } else {
          KmeliaDisplayHelper.displayOnNewOperations(kmeliaScc, gef, action, out);
        }

        out.println(frame.printBefore());
        if ("finish".equals(wizard)) {
          // cadre d'aide
%>
			<div class="inlineMessage">
				<img border="0" src="<%=resources.getIcon("kmelia.info") %>"/>
				<%=resources.getString("kmelia.HelpView") %>
			</div>
			<br clear="all"/>
<%        }
  %>
  <div id="header">
  <form name="pubForm" action="#" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
  	<input type="hidden" name="Action"/>
    <input type="hidden" name="Positions"/>
  	<input type="hidden" name="PubId" value="<%=id%>"/>
  	<input type="hidden" name="Status" value="<%=status%>"/>
  	<input type="hidden" name="TempId" value="<%=tempId%>"/>
  	<input type="hidden" name="InfoId" value="<%=infoId%>"/>

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

				<% if (I18NHelper.isI18N) { %>
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
						<input type="text" name="Name" id="pubName" value="<%=name%>" size="68" maxlength="150" />&nbsp;<img src="<%=mandatorySrc%>" width="5" height="5" border="0"/>
					</div>
				</div>

				<% if (isFieldDescriptionVisible) {%>
				<div class="field" id="descriptionArea">
					<label for="pubDesc" class="txtlibform"><%=resources.getString("PubDescription")%></label>
					<div class="champs">
						<textarea rows="4" cols="65" name="Description" id="pubDesc"><%=description%></textarea>
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
						<input type="text" name="Keywords" id="pubKeys" value="<%=keywords%>" size="68" maxlength="1000" />
					</div>
				</div>
				<% } %>
				<% if (kmeliaScc.isAuthorUsed()) {%>
				<div class="field" id="authorArea">
					<label for="author" class="txtlibform"><%=resources.getString("GML.author")%></label>
					<div class="champs">
						<input type="text" id="author" name="Author" value="<%=author%>" size="68" maxlength="50" />
					</div>
				</div>
				<% } %>

				<% if (isFieldVersionVisible) { %>
				<div class="field" id="versionArea">
					<label for="version" class="txtlibform"><%=resources.getString("PubVersion")%></label>
					<div class="champs">
						<input type="text" id="version" name="Version" value="<%=EncodeHelper.javaStringToHtmlString(version)%>" size="5" maxlength="30" />
					</div>
				</div>
				<% } %>

				<% if (isFieldImportanceVisible) { %>
				<div class="field" id="importanceArea">
					<label for="importance" class="txtlibform"><%=resources.getString("PubImportance")%></label>
					<div class="champs">
						<select id="importance" name="Importance">
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
      				<input type="hidden" name="Importance" value="1" />
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
          				<input type="hidden" name="ValideurId" id="ValideurId" value="<%=targetValidatorId%>"/><%=link%>&nbsp;<img src="<%=mandatorySrc%>" width="5" height="5" border="0"/>
					</div>
				</div>
				<% } %>

			    <% if (kmeliaPublication != null) { %>
				<div class="field" id="creationArea">
					<label class="txtlibform"><%=resources.getString("kmelia.header.contributors") %></label>
					<% if (StringUtil.isDefined(updateDate) && updater != null) {%>
					<div class="champs">
						<%=resources.getString("PubDateUpdate")%> <br /><b><%=updateDate%></b> <%=resources.getString("kmelia.By")%> <view:username userId="<%=kmeliaPublication.getLastModifier().getId()%>"/>
						<div class="profilPhoto"><view:image src="<%=kmeliaPublication.getLastModifier().getAvatar() %>" type="avatar" css="defaultAvatar"/></div>
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
					<% if (pubDetail != null && isAutomaticDraftOutEnabled && !"-1".equals(pubDetail.getCloneId())) { %>
					<div class="field" id="draftOutArea">
						<label for="draftOutDate" class="txtlibform"><%=resources.getString("kmelia.automaticDraftOutDate")%></label>
						<div class="champs">
							<input id="draftOutDate" type="text" class="dateToPick" name="DraftOutDate" value="<%=draftOutDate%>" size="12" maxlength="10"/>
						</div>
					</div>
					<% } %>
					<div class="field" id="beginArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("PubDateDebut")%></label>
						<div class="champs">
							<input id="beginDate" type="text" class="dateToPick" name="BeginDate" value="<%=beginDate%>" size="12" maxlength="10"/>
							<span class="txtsublibform">&nbsp;<%=resources.getString("ToHour")%>&nbsp;</span>
							<input id="beginHour" class="inputHour" type="text" name="BeginHour" value="<%=beginHour%>" size="5" maxlength="5" /> <i>(hh:mm)</i>
						</div>
					</div>
					<div class="field" id="endArea">
						<label for="endDate" class="txtlibform"><%=resources.getString("PubDateFin")%></label>
						<div class="champs">
							<input id="endDate" type="text" class="dateToPick" name="EndDate" value="<%=endDate %>" size="12" maxlength="10"/>
							<span class="txtsublibform">&nbsp;<%=resources.getString("ToHour")%>&nbsp;</span>
							<input id="endHour" class="inputHour" type="text" name="EndHour" value="<%=endHour %>" size="5" maxlength="5" /> <i>(hh:mm)</i>
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
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(validateButton);
          buttonPane.addButton(cancelButton);
          buttonPane.setHorizontalPosition();
          out.println("<br/><center>" + buttonPane.print() + "</center><br/>");
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
</body>
</html>
