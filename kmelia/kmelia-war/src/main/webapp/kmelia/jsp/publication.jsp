<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="modelUtils.jsp" %>
<%@ include file="attachmentUtils.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@page import="com.stratelia.silverpeas.versioning.model.DocumentPK"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.silverpeas.delegatednews.model.DelegatedNews"%>

<%
  ResourceLocator uploadSettings = new ResourceLocator(
      "com.stratelia.webactiv.util.uploads.uploadSettings", resources.getLanguage());
  ResourceLocator publicationSettings = new ResourceLocator(
      "com.stratelia.webactiv.util.publication.publicationSettings", resources.getLanguage());

  //Recuperation des parametres
  String profile = (String) request.getAttribute("Profile");
  String alias = (String) request.getAttribute("IsAlias");
  String action = (String) request.getAttribute("Action");
  KmeliaPublication kmeliaPublication = (KmeliaPublication) request.getAttribute(
      "Publication");
  String wizard = (String) request.getAttribute("Wizard");
  Integer rang = (Integer) request.getAttribute("Rang");
  Integer nbPublis = (Integer) request.getAttribute("NbPublis");
  String language = (String) request.getAttribute("Language");
  List languages = (List) request.getAttribute("Languages");
  String contentLanguage = (String) request.getAttribute("ContentLanguage");
  String singleFileURL = (String) request.getAttribute("SingleAttachmentURL");
  ValidationStep validation = (ValidationStep) request.getAttribute("ValidationStep");
  int validationType = ((Integer) request.getAttribute("ValidationType")).intValue();
  boolean isWriterApproval = ((Boolean) request.getAttribute("WriterApproval")).booleanValue();
  boolean notificationAllowed = ((Boolean) request.getAttribute("NotificationAllowed")).booleanValue();
  boolean attachmentsEnabled = ((Boolean) request.getAttribute("AttachmentsEnabled")).booleanValue();
  boolean isNewsManage = ((Boolean) request.getAttribute("NewsManage")).booleanValue();
  DelegatedNews delegatedNews = (DelegatedNews) request.getAttribute("DelegatedNews");
  boolean isBasket = ((Boolean) request.getAttribute("IsBasket")).booleanValue();


  if (action == null) {
    action = "View";
  }

  SilverTrace.info("kmelia", "JSPdesign", "root.MSG_GEN_PARAM_VALUE",
      "ACTION pubManager = " + action);

  CompletePublication pubComplete = kmeliaPublication.getCompleteDetail();
  PublicationDetail pubDetail = pubComplete.getPublicationDetail();
  UserDetail ownerDetail = kmeliaPublication.getCreator();
  String pubName = pubDetail.getName(language);
  String id = pubDetail.getPK().getId();

  String contextComponentId = componentId;
  //surcharge le componentId du composant courant (cas de l'alias)
  componentId = pubDetail.getPK().getInstanceId();

  TopicDetail currentTopic = null;
  String linkedPathString = kmeliaScc.getSessionPath();

  boolean debut = rang.intValue() == 0;
  boolean fin = rang.intValue() == nbPublis.intValue() - 1;

  boolean suppressionAllowed = false;

  Board boardHelp = gef.getBoard();

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

  //Vrai si le user connecte est le createur de cette publication ou si il est admin
  boolean isOwner = false;

  if (action.equals("ValidationComplete") || action.equals("ValidationInProgress") || action.equals(
      "Unvalidate") || action.equals("Suspend")) {
    if (action.equals("ValidationComplete")) {
      screenMessage = "<div class=\"inlineMessage-ok\">" + resources.getString("PubValidate") + "</div>";
    } else if (action.equals("ValidationInProgress")) {
      screenMessage = "<div class=\"inlineMessage\">" + resources.getString(
          "kmelia.PublicationValidationInProgress") + "</div>";
    } else if (action.equals("Unvalidate")) {
      screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("PublicationRefused") + "</div>";
    } else if (action.equals("Suspend")) {
      screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString(
          "kmelia.PublicationSuspended") + "</div>";
    }
    action = "ViewPublication";
  }
  if (pubDetail.isRefused()) {
    screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("PublicationRefused") + "</div>";
  }

  if (action.equals("ValidateView")) {
    kmeliaScc.setSessionOwner(true);
    action = "UpdateView";
    isOwner = true;
  } else {
    if (profile.equals("admin") || profile.equals("publisher") || profile.equals("supervisor") || (ownerDetail != null && currentUser.
        getId().equals(ownerDetail.getId()) && profile.equals("writer"))) {
      isOwner = true;

      if (!kmeliaScc.isSuppressionOnlyForAdmin() || (profile.equals("admin") && kmeliaScc.
          isSuppressionOnlyForAdmin())) {
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
      //modification pour acceder e l'onglet voir aussi
      kmeliaScc.setSessionOwner(false);
    }
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

  String creationDate = resources.getOutputDate(pubDetail.getCreationDate());

  String author = pubDetail.getAuthor();

  String creatorId = pubDetail.getCreatorId();
  String creatorName = resources.getString("kmelia.UnknownUser");
  if (creatorId != null && creatorId.length() > 0) {
    UserDetail creator = kmeliaScc.getUserDetail(creatorId);
    if (creator != null) {
      creatorName = creator.getDisplayedName();
    }
  }

  String updaterId = pubDetail.getUpdaterId();
  String updaterName = resources.getString("kmelia.UnknownUser");
  if (updaterId != null && updaterId.length() > 0) {
    UserDetail updater = kmeliaScc.getUserDetail(updaterId);
    if (updater != null) {
      updaterName = updater.getDisplayedName();
    }
  }

  boolean highlightFirst = resources.getSetting("highlightFirstOccurence", false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <%
      out.println(gef.getLookStyleSheet());
    %>
    <title></title>
    <link type="text/css" rel="stylesheet" href="<%=m_context%>/kmelia/jsp/styleSheets/pubHighlight.css" >
    <script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
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
              $( "#exportForm").submit();
              $( this ).dialog( "close" );
            },
            '<%= resources.getString("GML.cancel") %>': function() {
              $( this ).dialog( "close" );
            }
          }
        })
      });

      var refusalMotiveWindow = window;
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
        document.pubForm.PubId.value = "<%=id%>";
        url = "WantToRefusePubli?PubId="+<%=id%>;
        windowName = "refusalMotiveWindow";
        larg = "550";
        haut = "350";
        windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
        if (!refusalMotiveWindow.closed && refusalMotiveWindow.name== "refusalMotiveWindow")
          refusalMotiveWindow.close();
        refusalMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
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

      function pubDraftIn() {
        location.href = "<%=routerUrl%>DraftIn?From=ViewPublication";
      }

      function pubDraftOut() {
        if (<%=kmeliaScc.isDraftOutAllowed()%>) {
          location.href = "<%=routerUrl%>DraftOut?From=ViewPublication";
        } else {
          window.alert("<%=resources.getString("kmelia.PdcClassificationMandatory")%>");
        }
      }

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

        function addFavorite()
        {
          var name = encodeURI($("#breadCrumb").text());
          var description = encodeURI("<%=EncodeHelper.javaStringToJsString(pubDetail.getDescription(language))%>");
          var url = "<%=URLManager.getSimpleURL(URLManager.URL_PUBLI,
              pubDetail.getPK().getId())%>";
                  urlWindow = "<%=m_context%>/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
  
                  if (!favoriteWindow.closed && favoriteWindow.name== "favoriteWindow") {
                    favoriteWindow.close();
                  }
  
                  favoriteWindow = SP_openWindow(urlWindow, "favoriteWindow", "550", "250", "directories=0,menubar=0,toolbar=0,alwaysRaised");
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
        browseBar.setPath(linkedPathString);
        browseBar.setExtraInformation(pubName);
        browseBar.setI18N(languages, contentLanguage);

        OperationPane operationPane = window.getOperationPane();

        if (notificationAllowed && !currentUser.isAnonymous()) {
          operationPane.addOperation(alertSrc, resources.getString("GML.notify"),
              "javaScript:alertUsers()");
        }
        //operationPane.addOperation(exportSrc, resources.getString("kmelia.DownloadPublication"),
        //    "javaScript:zipPublication()");
        if (!toolboxMode && !exportFormats.isEmpty()) {
          operationPane.addOperation(pdfSrc, resources.getString("kmelia.ExportPublication"),
              "javascript:exportPublication()");
        }
        if (!currentUser.isAnonymous()) {
          operationPane.addOperation(favoriteAddSrc,
              resources.getString("FavoritesAddPublication") + " " + resources.getString(
              "FavoritesAdd2"),
              "javaScript:addFavorite()");
        }
        operationPane.addLine();

        if (isOwner) {
          if (!"supervisor".equals(profile)) {
            if (suppressionAllowed) {
              operationPane.addOperation(deletePubliSrc, resources.getString("GML.delete"),
                  "javaScript:pubDeleteConfirm()");
            }
            operationPane.addOperation("#", resources.getString("kmelia.AddFile"),
                "javaScript:AddAttachment()");

            if (kmeliaScc.isDraftEnabled() && !pubDetail.haveGotClone()) {
              if (pubDetail.isDraft()) {
                operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"),
                    "javaScript:pubDraftOut()");
              } else {
                operationPane.addOperation(pubDraftInSrc, resources.getString("PubDraftIn"),
                    "javaScript:pubDraftIn()");
              }
            }
            operationPane.addLine();
          }
        }
        if (!kmaxMode) {
          if (!currentUser.isAnonymous()) {
            operationPane.addOperation(resources.getIcon("kmelia.copy"), resources.getString(
                "GML.copy"),
                "javaScript:clipboardCopy()");
          }
          if (isOwner) {
            operationPane.addOperation(resources.getIcon("kmelia.cut"), resources.getString(
                "GML.cut"), "javaScript:clipboardCut()");
          }
        }
        if (!toolboxMode && isOwner) {
          if (profile.equals("admin") || profile.equals("publisher") || isWriterApproval) {
            if (pubDetail.isValidationRequired()) {
              if (validation == null) {
                operationPane.addLine();
                operationPane.addOperation(pubValidateSrc, resources.getString("PubValidate?"),
                    "javaScript:pubValidate()");
                operationPane.addOperation(pubUnvalidateSrc, resources.getString(
                    "PubUnvalidate?"),
                    "javaScript:pubUnvalidate()");
              }
            }
          }
          if (profile.equals("supervisor")) {
            operationPane.addLine();
            operationPane.addOperation(pubUnvalidateSrc, resources.getString(
                "kmelia.PubSuspend"),
                "javaScript:pubSuspend()");
          }
        }

        if (isNewsManage && isOwner && pubDetail.isValid() && delegatedNews == null && !isBasket) {
          operationPane.addLine();
          operationPane.addOperation("#", resources.getString(
              "kmelia.DelegatedNewsSuggest"),
              "javaScript:suggestDelegatedNews()");
        }

        out.println(window.printBefore());
        action = "View";
        if (isOwner) {
          displayAllOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        } else {
          displayUserOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        }
        out.println(frame.printBefore());

        if ("finish".equals(wizard)) {
          out.println(boardHelp.printBefore());
          out.println("<table border=\"0\"><tr>");
          out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\"" + resources.getIcon(
              "kmelia.info") + "\"></td>");
          out.println("<td>" + kmeliaScc.getString("kmelia.HelpPubli") + "</td>");
          out.println("</tr></table>");
          out.println(boardHelp.printAfter());
          out.println("<br/>");
        }
        if (screenMessage != null && screenMessage.length() > 0) {
          out.println("<center>" + screenMessage + "</center>");
        }

        InfoDetail infos = pubComplete.getInfoDetail();
        ModelDetail model = pubComplete.getModelDetail();
        int type = 0;
        if (kmeliaScc.isVersionControlled()) {
          type = 1; // Versioning
        }

        /*********************************************************************************************************************/
        /** Affichage du header de la publication																			**/
        /*********************************************************************************************************************/
        out.println("<table border=\"0\" width=\"98%\" align=\"center\">");
        out.println("<tr><td align=\"left\">");

        out.print("<span class=\"publiName\">");
        out.print(EncodeHelper.javaStringToHtmlString(pubDetail.getName(language)));
        out.println("</span>");

        if (!"user".equals(profile)) {
          if (pubDetail.isValidationRequired()) {
            out.println("<img src=\"" + outDraftSrc + "\" alt=\"" + resources.getString(
                "PubStateToValidate") + "\" align=\"absmiddle\" id=\"status\"/>");
          } else if (pubDetail.isDraft()) {
            out.println(
                "<img src=\"" + inDraftSrc + "\" alt=\"" + resources.getString("PubStateDraft") + "\" align=\"absmiddle\" id=\"status\"/>");
          } else if (pubDetail.isValid()) {
            out.println("<img src=\"" + validateSrc + "\" alt=\"" + resources.getString(
                "PublicationValidated") + "\" align=\"absmiddle\" id=\"status\"/>");
          } else if (pubDetail.isRefused()) {
            out.println("<img src=\"" + refusedSrc + "\" alt=\"" + resources.getString(
                "PublicationRefused") + "\" align=\"absmiddle\" id=\"status\"/>");
          }
        }

        String description = EncodeHelper.javaStringToHtmlString(pubDetail.getDescription(language));
        description = EncodeHelper.javaStringToHtmlParagraphe(description);

        out.println("<br/><span class=\"publiDesc\">" + description + "</span><br/><br/>");

        out.println("</td><td valign=\"top\" align=\"right\">");

        /*********************************************************************************************************************/
        /** Affichage des boutons de navigation (next / previous)															**/
        /*********************************************************************************************************************/
        if (nbPublis.intValue() > 1) {
      %>
      <!-- AFFICHAGE des boutons de navigation -->
      <table border="0" cellspacing="0" cellpadding="0" id="pagination">
        <tr>
          <td align="center" width="15">
            <% if (!debut) {%>
            <a href="PreviousPublication"><img src="<%=resources.getIcon("kmelia.previous")%>" align="middle" border="0" alt="<%=resources.getString("kmelia.previous")%>" title="<%=resources.getString("kmelia.previous")%>"/></a>
            <% } else {%>&nbsp;<% }%>
          </td>
          <td nowrap="nowrap" class="txtnav">
        <center><%=rang.intValue() + 1%> / <%=nbPublis.intValue()%></center>
        </td>
        <td align="center" width="15">
          <% if (!fin) {%>
          <a href="NextPublication"><img src="<%=resources.getIcon("kmelia.next")%>" align="middle" border="0" alt="<%=resources.getString("kmelia.next")%>" title="<%=resources.getString("kmelia.next")%>"/></a>
            <% } else {%>
          &nbsp;
          <% }%>
        </td>
        </tr>
      </table>
      <%
        }

        out.println("</td></tr></table>");

        /*********************************************************************************************************************/
        /** Affichage du contenu de la publication																			**/
        /*********************************************************************************************************************/
        out.println("<table border=\"0\" width=\"98%\" align=\"center\">");
        out.println("<tr><td valign=\"top\" width=\"100%\" id=\"richContent\">");
        if (WysiwygController.haveGotWysiwyg(spaceId, componentId, id)) {
          out.flush();
          getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId=" + id + "&SpaceId=" + spaceId + "&ComponentId=" + componentId + "&Language=" + language + "&axisId=" + kmeliaScc.
              getAxisIdGlossary() + "&onlyFirst=" + highlightFirst).include(request, response);
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
              xmlContext.setNodeId(kmeliaScc.getSessionTopic().getNodeDetail().getNodePK().getId());
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
        out.println("</td>");

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
          if (!"bottom".equals(resources.getSetting("attachmentPosition"))) {
            out.println("<td valign=\"top\">");
            out.println("<a name=\"attachments\"></a>");
          } else {
            out.println("</tr><tr>");
            out.println("<td valign=\"top\" align=\"left\">");
            out.println("<a name=\"attachments\"></a>");
          }
          try {
            out.flush();
            boolean indexIt = kmeliaScc.isIndexable(pubDetail);
            String pIndexIt = "0";
            if (indexIt) {
              pIndexIt = "1";
            }
            String attProfile = kmeliaScc.getProfile();
            if (kmeliaScc.isVersionControlled(componentId)) {
              if (!isOwner) {
                attProfile = "user";
              }
              getServletConfig().getServletContext().getRequestDispatcher(
                  "/versioningPeas/jsp/displayDocuments.jsp?Id=" + id + "&ComponentId=" + componentId + "&Alias=" + alias + "&Context=Images&AttachmentPosition=" + resources.
                  getSetting("attachmentPosition") + "&ShowIcon=" + showIcon + "&ShowTitle=" + showTitle + "&ShowFileSize=" + showFileSize + "&ShowDownloadEstimation=" + showDownloadEstimation + "&ShowInfo=" + showInfo + "&UpdateOfficeMode=" + kmeliaScc.
                  getUpdateOfficeMode() + "&Profile=" + attProfile + "&NodeId=" + kmeliaScc.
                  getSessionTopic().getNodePK().getId() + "&TopicRightsEnabled=" + kmeliaScc.
                  isRightsOnTopicsEnabled() + "&VersionningFileRightsMode=" + kmeliaScc.
                  getVersionningFileRightsMode() + "&CallbackUrl=" + URLManager.getURL(
                  "useless", componentId) + "ViewPublication&IndexIt=" + pIndexIt + "&ShowMenuNotif=" + true).
                  include(request, response);
            } else {
              if (!isOwner || pubDetail.haveGotClone()) {
                // Attachments can be updated in both cases only : 
                //  - on clone (if "publication always visible" is used)
                //  - if current user can modified publication
                attProfile = "user";
              }
              getServletConfig().getServletContext().getRequestDispatcher(
                  "/attachment/jsp/displayAttachments.jsp?Id=" + id + "&ComponentId=" + componentId + "&Alias=" + alias + "&Context=Images&AttachmentPosition=" + resources.
                  getSetting("attachmentPosition") + "&ShowIcon=" + showIcon + "&ShowTitle=" + showTitle + "&ShowFileSize=" + showFileSize + "&ShowDownloadEstimation=" + showDownloadEstimation + "&ShowInfo=" + showInfo + "&UpdateOfficeMode=" + kmeliaScc.
                  getUpdateOfficeMode() + "&Language=" + language + "&Profile=" + attProfile + "&CallbackUrl=" + URLManager.
                  getURL("useless", componentId) + "ViewPublication&IndexIt=" + pIndexIt + "&ShowMenuNotif=" + true).
                  include(request, response);
            }
          } catch (Exception e) {
            throw new KmeliaException(
                "JSPpublicationManager.displayUserModelAndAttachmentsView()",
                SilverpeasException.ERROR, "root.EX_DISPLAY_ATTACHMENTS_FAILED", e);
          }
          out.println("</td>");
        }
        out.println("</tr>");
        out.println("</table>");
      %>

      <span class="txtBaseline">
        <% if (kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pubDetail.getAuthor())) {%>
        <span id="authorInfo"><%=resources.getString("GML.author")%> : <%=pubDetail.getAuthor()%></span><br/>
        <% }%>
        <span id="creationInfo"><%= creatorName%> - <%=resources.getOutputDate(pubDetail.getCreationDate())%></span>
        <% if (updaterId != null) {%>
        <span id="lastModificationInfo"> | <%=resources.getString("kmelia.LastModification")%> : <%= updaterName%> - <%=resources.getOutputDate(pubDetail.getUpdateDate())%></span>
        <% }
          // Displaying all validator's name and final validation date 
          if (pubDetail.isValid() && StringUtil.isDefined(pubDetail.getValidatorId()) && pubDetail.
              getValidateDate() != null) {
            String validators = "";
            List validationSteps = pubComplete.getValidationSteps();
            if (validationSteps != null && !validationSteps.isEmpty()) {
              Collections.reverse(validationSteps); //display steps from in order of validation
              for (int v = 0; v < validationSteps.size(); v++) {
                if (v != 0) {
                  validators += ", ";
                }
                ValidationStep vStep = (ValidationStep) validationSteps.get(v);
                if (vStep != null) {
                  validators += kmeliaScc.getUserDetail(vStep.getUserId()).getDisplayedName();
                }
              }
            } else {
              validators = kmeliaScc.getUserDetail(pubDetail.getValidatorId()).getDisplayedName();
            }
        %>
        <span id="validationInfo"> | <%=resources.getString("kmelia.validation")%> : <%= validators%> - <%=resources.getOutputDate(pubDetail.getValidateDate())%></span>
        <%
          }
        %>
        <span id="statInfo"> | <%=resources.getString("kmelia.consulted")%> <%= pubDetail.getNbAccess()%> <%=resources.getString("kmelia.time")%></span>
        <% if (URLManager.displayUniversalLinks()) {
            String link = null;
            if (!pubDetail.getPK().getInstanceId().equals(contextComponentId)) {
              link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId(),
                  contextComponentId);
            } else {
              link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId());
            }%>
        <span id="permalinkInfo"> | <a href="<%=link%>"><img src="<%=resources.getIcon("kmelia.link")%>" align="absmiddle" alt="<%=Encode.convertHTMLEntities(resources.getString(
                                                                 "kmelia.CopyPublicationLink"))%>" title="<%=Encode.convertHTMLEntities(resources.getString(
                                                                     "kmelia.CopyPublicationLink"))%>"/></a></span>
            <% }%>
      </span>
      <div id="publication-export">
        <form id="exportForm" action="<c:url value='/exportPublication'/>">
          <fieldset>
            <legend><%=resources.getString("kmelia.format")%></legend>
            <%
            int i = 0;
            for(String format: exportFormats) {
              String checked = "";
              if (i++ == 0) {
                checked = "checked='checked'";
              }
            %>
            <input type="radio" name="Format" value="<%=format %>" <%=checked %> class="text ui-widget-content ui-corner-all"><%=resources.getString("kmelia.export.format." + format)%></input>
            <% } %>
            <input type="hidden" name="PubId" value="<%=id%>"/>
            <input type="hidden" name="ComponentId" value="<%=componentId%>"/>
          </fieldset>
        </form>
      </div>
      <%
        out.flush();

        out.println(frame.printAfter());
        out.println(window.printAfter());
      %>
      <form name="pubForm" action="<%=routerUrl%>publication.jsp" method="POST">
        <input type="hidden" name="Action"/>
        <input type="hidden" name="PubId"/>
        <input type="hidden" name="Profile" value="<%=profile%>"/>
      </form>
      <form name="refusalForm" action="<%=routerUrl%>Unvalidate">
        <input type="hidden" name="PubId" value="<%=id%>"/>
        <input type="hidden" name="Motive" value=""/>
      </form>
      <form name="defermentForm" action="<%=routerUrl%>SuspendPublication" method="POST">
        <input type="hidden" name="PubId" value="<%=id%>"/>
        <input type="hidden" name="Motive" value=""/>
      </form>
      <form name="toRouterForm">
        <input type="hidden" name="PubId" value="<%=id%>"/>
        <input type="hidden" name="ComponentId" value="<%=componentId%>"/>
      </form>
    </div>
  </body>
</html>
