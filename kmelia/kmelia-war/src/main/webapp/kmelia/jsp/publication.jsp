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
<%@page import="com.stratelia.webactiv.SilverpeasRole"%>
<%@page import="com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="modelUtils.jsp" %>
<%@ include file="topicReport.jsp.inc" %>

<%@page import="org.silverpeas.kmelia.jstl.KmeliaDisplayHelper"%>
<%@page import="com.silverpeas.kmelia.SearchContext"%>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@page import="org.silverpeas.importExport.versioning.DocumentPK"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.silverpeas.delegatednews.model.DelegatedNews"%>
<%@page import="org.silverpeas.component.kmelia.KmeliaPublicationHelper"%>
<%@ page import="org.silverpeas.rating.web.RaterRatingEntity" %>

<%
  ResourceLocator publicationSettings = new ResourceLocator("org.silverpeas.util.publication.publicationSettings", resources.getLanguage());

  //Recuperation des parametres
  String profile = (String) request.getAttribute("Profile");
  String alias = (String) request.getAttribute("IsAlias");
  String action = (String) request.getAttribute("Action");
  KmeliaPublication kmeliaPublication = (KmeliaPublication) request.getAttribute("Publication");
  String wizard = (String) request.getAttribute("Wizard");
  Integer rang = (Integer) request.getAttribute("Rang");
  Integer nbPublis = (Integer) request.getAttribute("NbPublis");
  String language = (String) request.getAttribute("Language");
  List languages = (List) request.getAttribute("Languages");
  String contentLanguage = (String) request.getAttribute("ContentLanguage");
  String singleFileURL = (String) request.getAttribute("SingleAttachmentURL");
  boolean userCanValidate = (Boolean) request.getAttribute("UserCanValidate");
  ValidationStep validation = (ValidationStep) request.getAttribute("ValidationStep");
  int validationType = (Integer) request.getAttribute("ValidationType");
  boolean isWriterApproval = (Boolean) request.getAttribute("WriterApproval");
  boolean notificationAllowed = (Boolean) request.getAttribute("NotificationAllowed");
  boolean ratingsAllowed = (Boolean) request.getAttribute("PublicationRatingsAllowed");
  boolean attachmentsEnabled = (Boolean) request.getAttribute("AttachmentsEnabled");
  boolean draftOutTaxonomyOK = (Boolean) request.getAttribute("TaxonomyOK");
  boolean validatorsOK = (Boolean) request.getAttribute("ValidatorsOK");
  int searchScope = (Integer) request.getAttribute("SearchScope");
  boolean isNewsManage = (Boolean) request.getAttribute("NewsManage");
  DelegatedNews delegatedNews = null;
  boolean isBasket = false;
  if (isNewsManage) {
    delegatedNews = (DelegatedNews) request.getAttribute("DelegatedNews");
    isBasket = (Boolean) request.getAttribute("IsBasket");
  }
  String indexIt = "0";
  if (kmeliaScc.isIndexable(kmeliaPublication.getDetail())) {
    indexIt = "1";
  }
  
  List<HistoryObjectDetail> lastAccess = (List<HistoryObjectDetail>) request.getAttribute("LastAccess");

  if (action == null) {
    action = "View";
  }

  SilverTrace.info("kmelia", "JSPdesign", "root.MSG_GEN_PARAM_VALUE",
      "ACTION pubManager = " + action);

  CompletePublication pubComplete = kmeliaPublication.getCompleteDetail();
  PublicationDetail pubDetail = kmeliaPublication.getDetail();
  UserDetail ownerDetail = kmeliaPublication.getCreator();
  String pubName = pubDetail.getName(language);
  String resourceType = pubDetail.getContributionType();
  String id = pubDetail.getPK().getId();

  String contextComponentId = componentId;
  //surcharge le componentId du composant courant (cas de l'alias)
  componentId = pubDetail.getPK().getInstanceId();

  String linkedPathString = kmeliaScc.getSessionPath();

  boolean debut = rang.intValue() == 0;
  boolean fin = rang.intValue() == nbPublis.intValue() - 1;

  boolean suppressionAllowed = false;

  //Icons
  String pubValidateSrc = m_context + "/util/icons/publicationValidate.gif";
  String pubUnvalidateSrc = m_context + "/util/icons/publicationUnvalidate.gif";
  String alertSrc = m_context + "/util/icons/alert.gif";
  String deletePubliSrc = m_context + "/util/icons/publicationDelete.gif";
  String pdfSrc = m_context + "/util/icons/publication_to_pdf.gif";
  String inDraftSrc = m_context + "/util/icons/masque.gif";
  String outDraftSrc = m_context + "/util/icons/visible.gif";
  String validateSrc = m_context + "/util/icons/ok.gif";
  String refusedSrc = m_context + "/util/icons/wrong.gif";
  String pubDraftInSrc = m_context + "/util/icons/publicationDraftIn.gif";
  String pubDraftOutSrc = m_context + "/util/icons/publicationDraftOut.gif";
  String exportSrc = m_context + "/util/icons/exportComponent.gif";
  String favoriteAddSrc = m_context + "/util/icons/addFavorit.gif";

  String screenMessage = "";
  String user_id = kmeliaScc.getUserId();
  UserDetail currentUser = kmeliaScc.getUserDetail();
  List<String> exportFormats = kmeliaScc.getSupportedFormats();
  List<String> availableFormats = kmeliaScc.getAvailableFormats();

  //Vrai si le user connecte est le createur de cette publication ou si il est admin
  boolean isOwner = false;

  // display message according to previous action
  if (action.equals("ValidationComplete") || action.equals("ValidationInProgress") || action.equals("Unvalidate") || action.equals("Suspend")) {
    if (action.equals("ValidationComplete")) {
      screenMessage = "<div class=\"inlineMessage-ok\">" + resources.getString("PubValidate") + "</div>";
    } else if (action.equals("ValidationInProgress")) {
      screenMessage = "<div class=\"inlineMessage\">" + resources.getString("kmelia.PublicationValidationInProgress") + "</div>";
    } else if (action.equals("Unvalidate")) {
      screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("PublicationRefused") + "</div>";
    } else if (action.equals("Suspend")) {
      screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("kmelia.PublicationSuspended") + "</div>";
    }
    action = "ViewPublication";
  }

  // display message according to current state of publication
  if (!StringUtil.isDefined(screenMessage)) {
    if (pubDetail.isRefused()) {
	  screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("PublicationRefused") + "</div>";
	} else if (pubDetail.isValidationRequired()) {
	  screenMessage = "<div class=\"inlineMessage\">" + resources.getString("kmelia.publication.tovalidate.state");
	  if (userCanValidate) {
	    screenMessage += " " + resources.getString("kmelia.publication.tovalidate.action")+"<br/>";
	    screenMessage += "<a href=\"javascript:onclick=pubValidate()\" class=\"button validate\"><span>"+resources.getString("PubValidate?")+"</span></a>";
	    screenMessage += "<a href=\"javascript:onclick=pubUnvalidate()\" class=\"button refuse\"><span>"+resources.getString("PubUnvalidate?")+"</span></a>";
	  }
	  screenMessage += "</div>";
	}
  }

  if (action.equals("ValidateView")) {
    kmeliaScc.setSessionOwner(true);
    action = "UpdateView";
    isOwner = true;
  } else {
    isOwner = KmeliaPublicationHelper.isUserConsideredAsOwner(contextComponentId, currentUser.getId(), profile, ownerDetail);
    suppressionAllowed = KmeliaPublicationHelper.isRemovable(contextComponentId, currentUser.getId(), profile, ownerDetail);

    //modification pour acceder e l'onglet voir aussi
    kmeliaScc.setSessionOwner(isOwner && validatorsOK);
  }

  if (isNewsManage && !kmaxMode && !toolboxMode && isOwner && delegatedNews != null) {
    if (DelegatedNews.NEWS_TO_VALIDATE.equals(delegatedNews.getStatus())) {
      screenMessage = "<div class=\"inlineMessage\">" + resources.getString(
          "kmelia.DelegatedNewsToValidate") + "</div>";
    } else if (DelegatedNews.NEWS_VALID.equals(delegatedNews.getStatus())) {
      screenMessage = "<div class=\"inlineMessage-ok\">" + resources.getString(
          "kmelia.DelegatedNewsValid") + "</div>";
    } else if (DelegatedNews.NEWS_REFUSED.equals(delegatedNews.getStatus())) {
      screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString(
          "kmelia.DelegatedNewsRefused") + "</div>";
    }
  }
  
  if (SilverpeasRole.writer.isInRole(profile) && !validatorsOK) {
    String selectUserLab = resources.getString("kmelia.SelectValidator");
    String link = "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');\">";
    link += "<img src=\""
        + resources.getIcon("kmelia.user")
        + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
        + selectUserLab + "\" align=\"absmiddle\" title=\""
        + selectUserLab + "\"></a>";
        
    screenMessage += "<div class=\"inlineMessage\" id=\"validationArea\">" + resources.getString("kmelia.publication.validators.select");
    screenMessage += "<div id=\"\"><form id=\"form-pub-validator\" action=\"SetPublicationValidator\" method=\"post\"><input type=\"text\" name=\"Valideur\" id=\"Valideur\" value=\"\" size=\"60\" readonly=\"readonly\"/><input type=\"hidden\" name=\"ValideurId\" id=\"ValideurId\" value=\"\"/>";
    screenMessage += link;
    screenMessage += "</form></div>";
    screenMessage += "<a href=\"#\" onclick=\"javascript:$('#form-pub-validator').submit();\" class=\"button\"><span>"+resources.getString("GML.validate")+"</span></a>";
    screenMessage += "<a href=\"#\" onclick=\"javascript:$('#validationArea').hide('slow');\" class=\"button\"><span>"+resources.getString("GML.close")+"</span></a>";
    screenMessage += "</div>";
    attachmentsEnabled = false;
  }

  String author = pubDetail.getAuthor();

  String updaterId = pubDetail.getUpdaterId();

  boolean highlightFirst = resources.getSetting("highlightFirstOccurence", false);

  //Attachments can be updated in both cases only :
  //  - on clone (if "publication always visible" is used)
  //  - if current user can modified publication
  boolean attachmentsUpdatable = attachmentsEnabled && isOwner && !pubDetail.haveGotClone();
%>

<c:set var="publication" value="<%=pubDetail%>"/>
<c:set var="publicationRaterRatingEntity" value="<%=RaterRatingEntity.fromRateable(pubDetail)%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.kmelia">
  <head>
    <view:looknfeel/>
    <title></title>
    <link type="text/css" rel="stylesheet" href='<c:url value="/kmelia/jsp/styleSheets/pubHighlight.css" />'/>
    <link type="text/css" rel="stylesheet" href='<c:url value="/kmelia/jsp/styleSheets/kmelia-print.css" />' media="print"/>
    <view:includePlugin name="wysiwyg"/>
    <view:includePlugin name="popup"/>
    <view:includePlugin name="rating" />
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/kmelia/jsp/javaScript/glossaryHighlight.js"></script>
    <script type="text/javascript">

      $(function() {
        $( "#publication-export" ).dialog({
          autoOpen: false,
          title: "<%=resources.getString("kmelia.chooseFormat")%>",
          modal: true,
          minWidth: 350,
          buttons: {
            '<%= resources.getString("kmelia.export") %>': function() {
              $("#exportForm").submit();
              $( this ).dialog( "close" );
            },
            '<%= resources.getString("GML.cancel") %>': function() {
              $( this ).dialog( "close" );
            }
          }
        });

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

        $("#publication-refusal-form").dialog({
            autoOpen: false,
            title: "<%=resources.getString("PubUnvalidate?")%>",
            modal: true,
            minWidth: 500,
            resizable : false,
            buttons: {
              'OK': function() {
            	  if (!$.trim($("#refusal-motive").val())) {
            		  window.alert("'<%=kmeliaScc.getString("RefusalMotive")%>' <%=resources.getString("GML.MustBeFilled")%>");
            		  return true;
            	  } else {
			  		document.refusalForm.submit();
            	  }
              },
              '<%= resources.getString("GML.cancel") %>': function() {
                  $(this).dialog("close");
              }
            }
          });

      });

      var publicVersionsWindow = window;
      var suspendMotiveWindow = window;
      var attachmentWindow = window;
      var favoriteWindow = window;

      function exportPublication() {
        $( '#publication-export' ).dialog('open');
      }

      function clipboardCopy() {
        top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy.jsp?Id=<%=id%>';
      }

      function clipboardCut() {
        top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut.jsp?Id=<%=id%>';
      }

      function compileResult(fileName) {
        SP_openWindow(fileName, "PdfGeneration","770", "550", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
      }

      function pubDeleteConfirm() {
        closeWindows();
        if(window.confirm("<%=resources.getString("ConfirmDeletePub")%>")){
          document.toRouterForm.action = "<%=routerUrl%>DeletePublication";
          document.toRouterForm.PubId.value = "<%=id%>";
          document.toRouterForm.submit();
        }
      }

      function pubValidate() {
        document.toRouterForm.action = "<%=routerUrl%>ValidatePublication";
        document.toRouterForm.submit();
      }

      function pubUnvalidate() {
	    $('#publication-refusal-form').dialog('open');
      }

      function pubSuspend() {
        document.pubForm.PubId.value = "<%=id%>";
        url = "WantToSuspendPubli?PubId="+<%=id%>;
        windowName = "suspendMotiveWindow";
        larg = "550";
        haut = "350";
        windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
        if (!suspendMotiveWindow.closed && suspendMotiveWindow.name== "suspendMotiveWindow")
          suspendMotiveWindow.close();
        suspendMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
      }

      <% if (!kmeliaPublication.isAlias()) { %>
	      function pubDraftIn() {
	        location.href = "<%=routerUrl%>DraftIn?From=ViewPublication";
	      }

	      function pubDraftOut() {
	        if (<%= draftOutTaxonomyOK %>) {
	          location.href = "<%=routerUrl%>DraftOut?From=ViewPublication";
	        } else {
	        	$("#publication-draftout").dialog('open');
	        }
	      }
	  <% } %>

      function topicGoTo(id) {
        closeWindows();
        location.href="GoToTopic?Id="+id;
      }

      function closeWindows() {
        if (window.publicationWindow != null)
          window.publicationWindow.close();
        if (window.publicVersionsWindow != null)
          window.publicVersionsWindow.close();
      }

      function alertUsers()
      {
      <% if (!pubDetail.isValid()) {%>
          if (window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString(
              "kmelia.AlertButPubNotValid"))%>"))
                  {
                    goToOperationInAnotherWindow('ToAlertUser', '<%=id%>', 'ViewAlert');
                  }
      <% } else {%>
          goToOperationInAnotherWindow('ToAlertUser', '<%=id%>', 'ViewAlert');
      <% }%>
        }

        function alertUsersAttachment(attachmentId)
        {
      <% if (!pubDetail.isValid()) {%>
          if (window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString(
              "kmelia.AlertButPubNotValid"))%>"))
                  {
                    goToOperationInAnotherWindow('ToAlertUserAttachment', '<%=id%>', attachmentId, 'ViewAlert');
                  }
      <% } else {%>
          goToOperationInAnotherWindow('ToAlertUserAttachment', '<%=id%>', attachmentId, 'ViewAlert');
      <% }%>
        }

        function alertUsersDocument(documentId)
        {
      <% if (!pubDetail.isValid()) {%>
          if (window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString(
              "kmelia.AlertButPubNotValid"))%>"))
                  {
                    goToOperationInAnotherWindow('ToAlertUserDocument', '<%=id%>', documentId, 'ViewAlert');
                  }
      <% } else {%>
          goToOperationInAnotherWindow('ToAlertUserDocument', '<%=id%>', documentId, 'ViewAlert');
      <% }%>
        }

        function openSingleAttachment() {
      <% if (StringUtil.isDefined(singleFileURL)) {
          out.print("url = \"" + EncodeHelper.javaStringToJsString(singleFileURL) + "\";");
      %>
          windowName = "attachmentWindow";
          windowParams = "directories=1,menubar=1,toolbar=1,location=1,resizable=1,scrollbars=1,status=1,alwaysRaised";
          if (!attachmentWindow.closed && attachmentWindow.name== "attachmentWindow")
            attachmentWindow.close();

          attachmentWindow = SP_openWindow(url, windowName, "600", "400", windowParams);
      <% }%>
        }

        function showTranslation(lang)
        {
          location.href="ViewPublication?SwitchLanguage="+lang;
        }

        function reloadPage() {
          location.href= "<%=routerUrl%>ViewPublication";
        }

        function addFavorite() {
          var name = $("#breadCrumb").text() + " > " + $(".publiName").text();
          var description = "<%=EncodeHelper.javaStringToJsString(pubDetail.getDescription(language))%>";
          var url = "<%=URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId())%>";
          postNewLink(name, url, description);
        }

        function suggestDelegatedNews() {
          location.href= "<%=routerUrl%>SuggestDelegatedNews";
        }
    </script>
  </head>
  <body class="yui-skin-sam" onunload="closeWindows()" onload="openSingleAttachment()" id="<%=componentId%>">

    <div id="preview">
      <%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        if (kmaxMode) {
          browseBar.setComponentName(componentLabel, "KmaxSearchResult");
        } else {
          browseBar.setComponentName(componentLabel, "Main");
        }
        if (searchScope == SearchContext.LOCAL) { 
          	if (StringUtil.isDefined(linkedPathString)) {
          		linkedPathString += " > ";
          	}
          	linkedPathString += "<a href=\"GoBackToResults\">"+resources.getString("kmelia.publication.breadcrumb.results")+"</a>";
        }
        browseBar.setPath(linkedPathString);
        browseBar.setExtraInformation(pubName);
        browseBar.setI18N(languages, contentLanguage);

        OperationPane operationPane = window.getOperationPane();

        operationPane.addOperation("useless", resources.getString("GML.print"), "javaScript:print();");
        if (notificationAllowed && !currentUser.isAnonymous()) {
          operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:alertUsers()");
        }

        if (!toolboxMode && !exportFormats.isEmpty()) {
          operationPane.addOperation(pdfSrc, resources.getString("kmelia.ExportPublication"), "javascript:exportPublication()");
        }
        if (!currentUser.isAnonymous()) {
          operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAddPublication") + " " + resources.getString("FavoritesAdd2"), "javascript:addFavorite()");
        }
        operationPane.addLine();

        if (isOwner && validatorsOK) {
          if (!"supervisor".equals(profile)) {
            if (attachmentsUpdatable) {
            	operationPane.addOperation("#", resources.getString("kmelia.AddFile"), "javascript:addAttachment('" +pubDetail.getId() + "')");
            }

            if (kmeliaScc.isDraftEnabled() && !pubDetail.haveGotClone()) {
              if (pubDetail.isDraft()) {
                operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"), "javascript:pubDraftOut()");
              } else {
                operationPane.addOperation(pubDraftInSrc, resources.getString("PubDraftIn"), "javascript:pubDraftIn()");
              }
            }

            if (suppressionAllowed) {
              operationPane.addOperation(deletePubliSrc, resources.getString("GML.delete"), "javascript:pubDeleteConfirm()");
            }
            operationPane.addLine();
          }
        }
        if (!kmaxMode) {
          if (!currentUser.isAnonymous()) {
            operationPane.addOperation(resources.getIcon("kmelia.copy"), resources.getString("GML.copy"), "javascript:clipboardCopy()");
          }
          if (isOwner) {
            operationPane.addOperation(resources.getIcon("kmelia.cut"), resources.getString("GML.cut"), "javascript:clipboardCut()");
          }
        }
        if (!toolboxMode && isOwner) {
          if (userCanValidate) {
            // if clone exists, only the clone can be validated or refused
            operationPane.addLine();
            operationPane.addOperation(pubValidateSrc, resources.getString("PubValidate?"), "javaScript:pubValidate()");
            operationPane.addOperation(pubUnvalidateSrc, resources.getString("PubUnvalidate?"), "javaScript:pubUnvalidate()");
          }
          if (profile.equals("supervisor")) {
            operationPane.addLine();
            operationPane.addOperation(pubUnvalidateSrc, resources.getString("kmelia.PubSuspend"), "javaScript:pubSuspend()");
          }
        }

        if (isNewsManage && isOwner && pubDetail.isValid() && delegatedNews == null && !isBasket) {
          operationPane.addLine();
          operationPane.addOperation("#", resources.getString("kmelia.DelegatedNewsSuggest"), "javaScript:suggestDelegatedNews()");
        }

        out.println(window.printBefore());
        action = "View";
        if (isOwner && validatorsOK) {
          KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        } else {
          KmeliaDisplayHelper.displayUserOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        }
        out.println(frame.printBefore());

        InfoDetail infos = pubComplete.getInfoDetail();
        ModelDetail model = pubComplete.getModelDetail();
        int type = 0;
        if (kmeliaScc.isVersionControlled()) {
          type = 1; // Versioning
        }
        /*********************************************************************************************************************/
        /** Affichage des boutons de navigation (next / previous)															**/
        /*********************************************************************************************************************/
        if (nbPublis.intValue() > 1) {
      %>
      <!-- AFFICHAGE des boutons de navigation -->
      <div id="pagination">
		<% if (!debut) {%>
        	<a href="PreviousPublication" title="<%=resources.getString("kmelia.previous")%>"><img src="<%=resources.getIcon("kmelia.previous")%>" alt="<%=resources.getString("kmelia.previous")%>" /></a>
        <% } else {%>
        	<img src="<%=resources.getIcon("kmelia.previousOff")%>" alt="" />
        <% } %>

        <span class="txtnav"><span class="currentPage"><%=rang.intValue() + 1%></span> / <%=nbPublis.intValue()%></span>

        <% if (!fin) {%>
        	<a href="NextPublication" title="<%=resources.getString("kmelia.next")%>"><img src="<%=resources.getIcon("kmelia.next")%>"  alt="<%=resources.getString("kmelia.next")%>" /></a>
        <% } else {%>
        	<img src="<%=resources.getIcon("kmelia.nextOff")%>" alt="" />
        <% }%>
      </div>
      <% } %>

	  <%
	    	String backURL = "GoToCurrentTopic";
	  		String backLabel = resources.getString("kmelia.publication.link.folder");
	    	if (searchScope == SearchContext.GLOBAL) {
	    	  backURL = m_context+"/RpdcSearch/jsp/LastResults";
	    	  backLabel = resources.getString("kmelia.publication.link.results");
	    	} else if (searchScope == SearchContext.LOCAL){
	    	  backURL = "GoBackToResults";
	    	  backLabel = resources.getString("kmelia.publication.link.results");
	    	}
	  %>
	    <!-- button to go back to search results or current folder -->
	  	<div id="backToSearch">
	  	 <a href="<%=backURL%>" class="button"><span><%=backLabel%></span></a>
	  	</div>

      <div class="rightContent">
      	<div id="statPublication" class="bgDegradeGris">
      		<p id="statInfo">
      			<b><%= kmeliaPublication.getNbAccess()%> <%=resources.getString("GML.stats.views") %></b>
      			<% if (ratingsAllowed) { %>
            		<viewTags:displayContributionRating raterRating="${publicationRaterRatingEntity}"/>
				<% } %>
			</p>

		    <% if (URLManager.displayUniversalLinks()) {
		            String link = null;
		            if (!pubDetail.getPK().getInstanceId().equals(contextComponentId)) {
		              link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId(),
		                  contextComponentId);
		            } else {
		              link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId());
		            }%>
		        <p id="permalinkInfo">
		        	<a href="<%=link%>" title="<%=Encode.convertHTMLEntities(resources.getString("kmelia.CopyPublicationLink"))%>"><img src="<%=resources.getIcon("kmelia.link")%>" alt="<%=Encode.convertHTMLEntities(resources.getString("kmelia.CopyPublicationLink"))%>" /></a> <%=resources.getString("GML.permalink")%> <br />
		            <input type="text" onfocus="select();" onmouseup="return false" value="<%=URLManager.getServerURL(request)+link%>" />
		        </p>
	            <% }%>
      	</div>
      <%

        /*********************************************************************************************************************/
        /** Colonne de droite																									**/
        /*********************************************************************************************************************/
			        if (attachmentsEnabled) {
			          /*********************************************************************************************************************/
			          /** Affichage des fichiers joints																					**/
			          /*********************************************************************************************************************/
			          boolean showTitle = resources.getSetting("showTitle", true);
			          boolean showFileSize = resources.getSetting("showFileSize", true);
			          boolean showDownloadEstimation = resources.getSetting("showDownloadEstimation",
			              true);
			          boolean showInfo = resources.getSetting("showInfo", true);
			          boolean showIcon = true;

			          /** Qu'est-ce qu'on fait de ça ? est-ce que c'est encore utilité	**/
			          if (!"bottom".equals(resources.getSetting("attachmentPosition"))) {

			            out.println("<a name=\"attachments\"></a>");
			          } else {

			            out.println("<a name=\"attachments\"></a>");
			          }
			          try {
			            out.flush();
			            String attProfile = kmeliaScc.getProfile();
			            if (kmeliaScc.isVersionControlled(componentId)) {
			              if (!isOwner) {
			                attProfile = "user";
			              }
			            } else {
				              if (!attachmentsUpdatable) {
				                attProfile = "user";
				              }
			            }
                  getServletConfig().getServletContext().getRequestDispatcher(
                      "/attachment/jsp/displayAttachedFiles.jsp?Id=" + id + "&ComponentId=" + componentId + "&Alias=" + alias + "&Context=attachment&AttachmentPosition=" + resources.
                          getSetting("attachmentPosition") + "&ShowIcon=" + showIcon + "&ShowTitle=" + showTitle + "&ShowFileSize=" + showFileSize + "&ShowDownloadEstimation=" + showDownloadEstimation + "&ShowInfo=" + showInfo +
                          "&Language=" + language + "&Profile=" + attProfile + "&CallbackUrl=" + URLManager.
                          getURL("useless", componentId) + "ViewPublication&IndexIt=" + indexIt + "&ShowMenuNotif=" + true).
                      include(request, response);
			          } catch (Exception e) {
			            throw new KmeliaException(
			                "JSPpublicationManager.displayUserModelAndAttachmentsView()",
			                SilverpeasException.ERROR, "root.EX_DISPLAY_ATTACHMENTS_FAILED", e);
			          }

			        }


			           /*********************************************************************************************************************/
			          /** Affichage des Info de publication																		**/
			          /*********************************************************************************************************************/
			        %>
			         <div id="infoPublication" class="bgDegradeGris">

			         			<% if (kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pubDetail.getAuthor())) { %>
									<p id="authorInfo"><%=resources.getString("GML.author")%> : <b><%=pubDetail.getAuthor()%></b></p>
								<% }%>

			         			<% if (updaterId != null) {%>
								  	<div id="lastModificationInfo" class="paragraphe">
								  		<%=resources.getString("PubDateUpdate")%>  <br />
                                        <b><%=resources.getOutputDate(pubDetail.getUpdateDate())%></b> <%=resources.getString("GML.by")%> <view:username userId="<%=kmeliaPublication.getLastModifier().getId()%>"/>
								  		<div class="profilPhoto"><img src="<%=m_context %><%=kmeliaPublication.getLastModifier().getAvatar() %>" alt="" class="defaultAvatar"/></div>
							  		</div>
							  	 <% }%>
								<c:if test="${view:isDefined(requestScope['Publication'].creator) && view:isDefined(requestScope['Publication'].creator.id)}">
                    <div id="creationInfo" class="paragraphe">
                     <%=resources.getString("PubDateCreation")%> <br/>
                     <b><%=resources.getOutputDate(pubDetail.getCreationDate())%></b> <%=resources.getString("GML.by")%> <view:username userId="${requestScope['Publication'].creator.id}"/>
                     <div class="profilPhoto"><img src='<c:url value="${requestScope['Publication'].creator.avatar}" />' alt="" class="defaultAvatar"/></div>
                   </div>
                </c:if>
							  	  <%
						          // Displaying all validator's name and final validation date
						          if (pubDetail.isValid() && StringUtil.isDefined(pubDetail.getValidatorId()) && pubDetail.getValidateDate() != null) { %>
						            <p id="validationInfo"><%=resources.getString("kmelia.validation")%> <br/>
						            	<b><%=resources.getOutputDate(pubDetail.getValidateDate())%></b> <%=resources.getString("GML.by")%>
						            <% List<ValidationStep> validationSteps = pubComplete.getValidationSteps();
						            if (validationSteps != null && !validationSteps.isEmpty()) {
						              Collections.reverse(validationSteps); //display steps from in order of validation
						              for (int v = 0; v < validationSteps.size(); v++) {
						                if (v != 0) { %>
						                  ,
						                <% }
						                ValidationStep vStep = validationSteps.get(v);
						                if (vStep != null) { %>
						                	<view:username userId="<%=vStep.getUserId()%>"/>
						                <% }
						              }
						            } else { %>
						            	<view:username userId="<%=pubDetail.getValidatorId()%>"/>
						            <% }
						        %>
   								 	</p>
							    <%
							      }
							    %>
					</div>
					<!-- consultation -->
                    <div id="lastReader" class="bgDegradeGris">
                   		<div class="bgDegradeGris header">
                        	<h4 class="clean"><%=resources.getString("kmelia.publication.lastvisitors") %></h4>
                      	</div>
                      	<% if (lastAccess.isEmpty()) { %>
                      		<div class="paragraphe"><%=resources.getString("kmelia.publication.lastvisitors.none") %></div>
                      	<% } else { %>
	                      	<ul id="lastReaderList">
	                      	<%
	                      		for (int i=0; i<lastAccess.size() && i<4; i++) {
									HistoryObjectDetail access = lastAccess.get(i);
							%>
									<li>
	                          			<div class="profilPhoto"><img src="<%=URLManager.getApplicationURL() %><%=UserDetail.getById(access.getUserId()).getAvatar() %>" alt="" class="defaultAvatar"/></div>
	                              		<view:username userId="<%=access.getUserId() %>" /> <span class="consultationDate"><%=resources.getString("kmelia.publication.lastvisitors.on") %> <%=resources.getOutputDate(access.getDate()) %></span>
	                          		</li>
							<% } %>
                      		</ul>
                      	<% } %>
                      	<% if (isOwner && kmeliaScc.getInvisibleTabs().indexOf(KmeliaSessionController.TAB_READER_LIST) == -1) { %>
                      		<a id="readingControlLink" href="ReadingControl">&gt;&gt; <%=resources.getString("PubGererControlesLecture") %></a>
                      	<% } else { %>
                      		<br clear="all" />
                      	<% } %>
                    </div>
                    <!-- /consultation -->
                    <%
                    /*********************************************************************************************************************/
        /** Affichage de la classification de la publication sur le PdC														**/

        /*********************************************************************************************************************/
        if(!kmaxMode) {
        %>
          <view:pdcClassificationPreview componentId="<%= componentId %>" contentId="<%= id %>" />
        <%
        } %>
      </div>
      <%

        /*********************************************************************************************************************/
        /** Colonne Pricipale																							    **/
        /*********************************************************************************************************************/
    	 out.println("<div class=\"principalContent\">");
      	 if ("finish".equals(wizard)) {
       %>
        	<div class="inlineMessage">
        		<img border="0" src="<%=resources.getIcon("kmelia.info") %>"/>
        		<%=resources.getString("kmelia.HelpPubli") %>
        	</div>
       <%
         }
         if (screenMessage != null && screenMessage.length() > 0) {
           out.println(screenMessage);
         }
				        /*********************************************************************************************************************/
				        /** Affichage du header de la publication																			**/
				        /*********************************************************************************************************************/
				        out.print("<h2 class=\"publiName\">");

						   out.print(EncodeHelper.javaStringToHtmlString(pubDetail.getName(language)));

				     		   if (!"user".equals(profile)) {
						          if (pubDetail.isValidationRequired()) {
						            out.println(" <img src=\"" + outDraftSrc + "\" alt=\"" + resources.getString(
						                "PubStateToValidate") + "\"  id=\"status\"/>");
						          } else if (pubDetail.isDraft()) {
						            out.println(" <img src=\"" + inDraftSrc + "\" alt=\"" + resources.getString("PubStateDraft") + "\"  id=\"status\"/>");
						          } else if (pubDetail.isValid()) {
						            out.println(" <img src=\"" + validateSrc + "\" alt=\"" + resources.getString("PublicationValidated") + "\"  id=\"status\"/>");
						          } else if (pubDetail.isRefused()) {
						            out.println(" <img src=\"" + refusedSrc + "\" alt=\"" + resources.getString("PublicationRefused") + "\"  id=\"status\"/>");
						          }
						        }

				        out.println("</h2>");

				        String description = EncodeHelper.convertWhiteSpacesForHTMLDisplay(EncodeHelper.javaStringToHtmlString(pubDetail.getDescription(language)));
				        if (StringUtil.isDefined(description)) {
				        	out.println("<p class=\"publiDesc text2\">" + description + "</p>");
				        }

				        /*********************************************************************************************************************/
				        /** Affichage du contenu de la publication																			**/
				        /*********************************************************************************************************************/

				        out.println("<div id=\"richContent\">");
				        if (WysiwygController.haveGotWysiwygToDisplay(componentId, id, language)) {%>
                <view:displayWysiwyg objectId="<%=id%>" componentId="<%=componentId%>" language="<%=language%>" axisId="<%=kmeliaScc.getAxisIdGlossary()%>" highlightFirst="<%=String.valueOf(highlightFirst)%>" />
                <%
                  } else if (infos != null && model != null) {
                  displayViewInfoModel(out, model, infos, resources, publicationSettings, m_context);

				        } else {
				          Form xmlForm = (Form) request.getAttribute("XMLForm");
				          DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");
				          if (xmlForm != null) {
				            PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(),
				                false, componentId, kmeliaScc.getUserId());
				            xmlContext.setObjectId(id);
				            if (kmeliaMode) {
				              xmlContext.setNodeId(kmeliaScc.getCurrentFolderId());
				            }
				            xmlContext.setBorderPrinted(false);
				            xmlContext.setContentLanguage(language);
				      %>
              <view:highlight axisId="<%=kmeliaScc.getAxisIdGlossary()%>" className="highlight-silver" language="<%=language%>" onlyFirst="<%=highlightFirst%>">
                <%
				          xmlForm.display(out, xmlContext, xmlData);
				        %>
				      </view:highlight>
				      <%
				          }
				        }
				        out.println("</div>");

                        if (kmeliaScc.getInvisibleTabs().indexOf(kmeliaScc.TAB_COMMENT) == -1 && !kmaxMode)	 {
			      %>
      <view:comments	userId="<%= user_id%>" componentId="<%= componentId %>"
      					resourceType="<%= resourceType %>" resourceId="<%= id %>" indexed="<%= indexIt %>"/>
      <% } %>
	  </div>

      <div id="publication-export" style="display: none;">
        <form id="exportForm" action="<c:url value='/exportPublication'/>" target="_blank">
          <input type="hidden" name="PubId" value="<%=id%>"/>
          <input type="hidden" name="ComponentId" value="<%=componentId%>"/>
          <fieldset>
            <legend><%=resources.getString("kmelia.format")%></legend>
            <%
            boolean selectedFormat = false;
            for(String format: availableFormats) {
              String checked = "";
              String disabled = "";
              if (!exportFormats.contains(format)) {
                disabled = "disabled=\"disabled\"";
              }
              if (!selectedFormat && disabled.isEmpty()) {
                checked = "checked=\"checked\"";
                selectedFormat = true;
              }
            %>
            <input type="radio" name="Format" value="<%=format %>" <%=checked %> <%=disabled %>/><%=resources.getString("kmelia.export.format." + format)%>
            <% } %>
          </fieldset>
        </form>
      </div>

      <div id="publication-draftout" style="display: none;">
        <%=resources.getString("kmelia.publication.draftout.impossible")%>
      	<ul>
      	<% if(!draftOutTaxonomyOK) { %>
      		<li><%=resources.getString("kmelia.PdcClassificationMandatory")%></li>
      	<% } %>
      	</ul>
      </div>
      <div id="publication-refusal-form" style="display: none;">
      	<form name="refusalForm" action="Unvalidate" method="post">
        <table>
        	<tr>
        		<td class="txtlibform" valign="top"><%=kmeliaScc.getString("RefusalMotive")%></td>
        		<td><textarea name="Motive" id="refusal-motive" rows="10" cols="60"></textarea>&nbsp;<img border="0" src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5"/></td>
        	</tr>
        </table>
        </form>
      </div>
      <%
        out.flush();
        out.println(frame.printAfter());
        out.println(window.printAfter());
      %>
      <form name="pubForm" action="<%=routerUrl%>publication.jsp" method="post">
        <input type="hidden" name="Action"/>
        <input type="hidden" name="PubId"/>
        <input type="hidden" name="Profile" value="<%=profile%>"/>
      </form>
      <form name="defermentForm" action="<%=routerUrl%>SuspendPublication" method="post">
        <input type="hidden" name="PubId" value="<%=id%>"/>
        <input type="hidden" name="Motive" value=""/>
      </form>
      <form name="toRouterForm" action="#">
        <input type="hidden" name="PubId" value="<%=id%>"/>
        <input type="hidden" name="ComponentId" value="<%=componentId%>"/>
      </form>
    </div>
 <script type="text/javascript">
 /* declare the module myapp and its dependencies (here in the silverpeas module) */
 var myapp = angular.module('silverpeas.kmelia', ['silverpeas.services', 'silverpeas.directives']);
 </script>
  </body>
</html>