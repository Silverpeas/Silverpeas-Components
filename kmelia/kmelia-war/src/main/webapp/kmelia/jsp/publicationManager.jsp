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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="modelUtils.jsp" %>
<%@ include file="attachmentUtils.jsp" %>
<%@ include file="publicationsList.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%@page import="com.silverpeas.thumbnail.model.ThumbnailDetail"%>
<%!  //Icons
  String folderSrc;
  String publicationSrc;
  String pubValidateSrc;
  String pubUnvalidateSrc;
  String fullStarSrc;
  String emptyStarSrc;
  String mandatorySrc;
  String deleteSrc;
  String seeAlsoSrc;
  String seeAlsoDeleteSrc;
  String alertSrc;
  String deletePubliSrc;
  String clipboardCopySrc;
  String hLineSrc;
  String pdfSrc;
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
    String creatorName = "";
    String creationDate = "";
    String beginDate = "";
    String endDate = "";
    String updateDate = "";
    String updaterName = "";
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

    String nextAction = "";

    ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", kmeliaScc.getLanguage());
    ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", kmeliaScc.getLanguage());
    ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", kmeliaScc.getLanguage());

    UserCompletePublication userPubComplete = null;
    UserDetail ownerDetail = null;

    CompletePublication pubComplete = null;
    PublicationDetail pubDetail = null;
    InfoDetail infos = null;
    ModelDetail model = null;

    String language = kmeliaScc.getCurrentLanguage();

//Recuperation des parametres

    String vignette_url = null;
   
    String profile = request.getParameter("Profile");
    String action = request.getParameter("Action");
    String id = request.getParameter("PubId");
    String checkPath = request.getParameter("CheckPath");
    String wizard = (String) request.getAttribute("Wizard");
    String currentLang = (String) request.getAttribute("Language");

    String resultThumbnail = (String)request.getParameter("resultThumbnail");
    boolean errorThumbnail = false;
	if(resultThumbnail != null && !"ok".equals(resultThumbnail)){
		errorThumbnail = true;
	}
    
    SilverTrace.info("kmelia", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ACTION pubManager = " + action);

    //Icons
    folderSrc = m_context + "/util/icons/component/kmeliaSmall.gif";
    publicationSrc = m_context + "/util/icons/publication.gif";
    pubValidateSrc = m_context + "/util/icons/publicationValidate.gif";
    pubUnvalidateSrc = m_context + "/util/icons/publicationUnvalidate.gif";
    fullStarSrc = m_context + "/util/icons/starFilled.gif";
    emptyStarSrc = m_context + "/util/icons/starEmpty.gif";
    mandatorySrc = m_context + "/util/icons/mandatoryField.gif";
    deleteSrc = m_context + "/util/icons/delete.gif";
    seeAlsoSrc = "icons/linkedAdd.gif";
    seeAlsoDeleteSrc = "icons/linkedDel.gif";
    alertSrc = m_context + "/util/icons/alert.gif";
    deletePubliSrc = m_context + "/util/icons/publicationDelete.gif";
    clipboardCopySrc = m_context + "/util/icons/copy.gif";
    pdfSrc = m_context + "/util/icons/publication_to_pdf.gif";
    hLineSrc = m_context + "/util/icons/colorPix/1px.gif";
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
    TopicDetail currentTopic = null;

    boolean suppressionAllowed = false;

    boolean isFieldDescriptionVisible = kmeliaScc.isFieldDescriptionVisible();
    boolean isFieldDescriptionMandatory = kmeliaScc.isFieldDescriptionMandatory();
    boolean isFieldKeywordsVisible = kmeliaScc.isFieldKeywordsVisible();
    boolean isFieldImportanceVisible = kmeliaScc.isFieldImportanceVisible();
    boolean isFieldVersionVisible = kmeliaScc.isFieldVersionVisible();
    boolean isNotificationAllowed = kmeliaScc.isNotificationAllowed();

    boolean isThumbnailField = false;
    boolean isThumbnailMandatory = kmeliaScc.isThumbnailMandatory();

    String linkedPathString = kmeliaScc.getSessionPath();
    String pathString = "";

    Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "GoToCurrentTopic", false);
    Button validateButton = null;

    if ("1".equals(checkPath)) {
      //Calcul du chemin de la publication
      currentTopic = kmeliaScc.getPublicationTopic(id);
      kmeliaScc.setSessionTopic(currentTopic);
      Collection pathColl = currentTopic.getPath();
      linkedPathString = displayPath(pathColl, true, 3, language) + name;
      pathString = displayPath(pathColl, false, 3, language);
      kmeliaScc.setSessionPath(linkedPathString);
      kmeliaScc.setSessionPathString(pathString);
    }
//Action = View, New, Add, UpdateView, Update, Delete, LinkAuthorView, SameSubjectView ou SameTopicView
    if (action.equals("UpdateView") || action.equals("ValidateView")) {

      //Recuperation des parametres de la publication
      userPubComplete = kmeliaScc.getUserCompletePublication(id);

      if (kmeliaScc.getSessionClone() != null) {
        userPubComplete = kmeliaScc.getSessionClone();
        id = userPubComplete.getId();
      }

      pubComplete = userPubComplete.getPublication();
      pubDetail = pubComplete.getPublicationDetail();
      pubName = pubDetail.getName(language);
      if (pubDetail.getImage() != null) {
          vignette_url = FileServer.getUrl("useless", pubDetail.getPK().getComponentName(), "vignette", pubDetail.getImage(), pubDetail.getImageMimeType(), publicationSettings.getString("imagesSubDirectory"));
      }
      ownerDetail = userPubComplete.getOwner();

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

      name = pubDetail.getName(language);
      description = pubDetail.getDescription(language);
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

        UserDetail updater = kmeliaScc.getUserDetail(pubDetail.getUpdaterId());
        if (updater != null) {
          updaterName = updater.getDisplayedName();
        }
      } else {
        updateDate = "";
      }
      if (ownerDetail != null) {
        creatorName = ownerDetail.getDisplayedName();
      } else {
        creatorName = kmeliaScc.getString("UnknownAuthor");
      }
      version = pubDetail.getVersion();
      importance = new Integer(pubDetail.getImportance()).toString();
      keywords = pubDetail.getKeywords(language);
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

      author = pubDetail.getAuthor();
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

    } else if (action.equals("New")) {
      creationDate = resources.getOutputDate(new Date());
      beginDate = resources.getInputDate(new Date());
      creatorName = kmeliaScc.getUserDetail().getDisplayedName();
      tempId = "-1";

      if (!kmaxMode) {
        currentTopic = kmeliaScc.getSessionTopic();
        Collection pathColl = currentTopic.getPath();
        linkedPathString = displayPath(pathColl, true, 3, language);
        kmeliaScc.setSessionPath(linkedPathString);
        pathString = displayPath(pathColl, false, 3, language);
        kmeliaScc.setSessionPathString(pathString);
      }
      nextAction = "AddPublication";
      isThumbnailField = true;
} 

    validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendPublicationDataToRouter('" + nextAction + "');", false);

    String sRequestURL = request.getRequestURL().toString();
    String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
    ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
    //Example: http://myserver
    String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);
%>

<HTML>
  <HEAD>
    <TITLE></TITLE>
    <%
        out.println(gef.getLookStyleSheet());
    %>
    <style type="text/css">
      #thumbnailPreview {
        <% if (vignette_url == null) {%>
        display: none;
        <% }%>
      }
    </style>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/ui.thickbox.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/jquery-thickbox.css">
    <script language="javascript">
      var favoriteWindow = window;

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
        if (<%=kmeliaScc.isDraftOutAllowed()%>)
        {
          location.href = "<%=routerUrl%>DraftOut";
        }
        else
        {
          window.alert("<%=kmeliaScc.getString("kmelia.PdcClassificationMandatory")%>");
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

      function topicGoTo(id) {
        location.href="GoToTopic?Id="+id;
      }

      function publicationGoTo(id, action){
        document.pubForm.Action.value = "ViewPublication";
        document.pubForm.CheckPath.value = "1";
        document.pubForm.PubId.value = id;
        document.pubForm.submit();
      }

      function sendOperation(operation) {
        document.pubForm.Action.value = operation;
        document.pubForm.submit();
      }

      function sendPublicationData(operation) {
        if (isCorrectForm()) {
          document.pubForm.Action.value = operation;
          document.pubForm.submit();
        }
      }

      function sendPublicationDataToRouter(func) {
        if (isCorrectForm()) {
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

      function isCorrectForm() {
        var errorMsg = "";
        var errorNb = 0;
        var title = stripInitialWhitespace(document.pubForm.Name.value);
     
        var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
        var beginDate = document.pubForm.BeginDate.value;
        var endDate = document.pubForm.EndDate.value;

        var yearBegin = extractYear(beginDate, '<%=kmeliaScc.getLanguage()%>');
        var monthBegin = extractMonth(beginDate, '<%=kmeliaScc.getLanguage()%>');
        var dayBegin = extractDay(beginDate, '<%=kmeliaScc.getLanguage()%>');
        var yearEnd = extractYear(endDate, '<%=kmeliaScc.getLanguage()%>');
        var monthEnd = extractMonth(endDate, '<%=kmeliaScc.getLanguage()%>');
        var dayEnd = extractDay(endDate, '<%=kmeliaScc.getLanguage()%>');
        var beginHour = document.pubForm.BeginHour.value;
        var endHour = document.pubForm.EndHour.value;

        var beginDateOK = true;

    	if (isWhitespace(title)) {
          errorMsg+="  - '<%=resources.getString("PubTitre")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
          errorNb++;
        }
         
      <% if (isFieldDescriptionVisible) {%>
             var description = document.pubForm.Description;
      <% if (isFieldDescriptionMandatory) {%>
                if (isWhitespace(description.value)) {
                  errorMsg+="  - '<%=resources.getString("PubDescription")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
                  errorNb++;
                }
      <% }%>
                if (!isValidTextArea(description)) {
                  errorMsg+="  - '<%=resources.getString("GML.description")%>' <%=resources.getString("kmelia.containsTooLargeText") + resources.getString("kmelia.nbMaxTextArea") + resources.getString("kmelia.characters")%>\n";
                  errorNb++;
                }
      <% }%>
     
      <% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {%>
             var validatorId = stripInitialWhitespace(document.pubForm.ValideurId.value);
             if (isWhitespace(validatorId)) {
               errorMsg+="  - '<%=resources.getString("kmelia.Valideur")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
               errorNb++;
             }
      <% }%>
             if (!isWhitespace(beginDate)) {
           if (beginDate.replace(re, "OK") !=     "OK") {
                 errorMsg+="  - '<%=resources.getString("PubDateDebut")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                 errorNb++;
                 beginDateOK = false;
               } else {
             if (isCorrectDate(yearBegin, monthBegin, dayBegin)    ==false) {
                   errorMsg+="  - '<%=resources.getString("PubDateDebut")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                   errorNb++;
                   beginDateOK = false;
                 }
               }
             }
             if (!checkHour(beginHour))
             {
               errorMsg+="  - '<%=resources.getString("ToHour")%>' <%=resources.getString("GML.MustContainsCorrectHour")%>\n";
               errorNb++;
             }
             if (!isWhitespace(endDate)) {
           if (endDate.replace(re,     "OK") != "OK") {
                 errorMsg+="  - '<%=resources.getString("PubDateFin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                 errorNb++;
               } else {
             if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                   errorMsg+="  - '<%=resources.getString("PubDateFin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                   errorNb++;
                 } else {
                   if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
                 if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin,     monthBegin, dayBegin) == false) {
                       errorMsg+="  - '<%=resources.getString("PubDateFin")%>' <%=resources.getString("GML.MustContainsPostOrEqualDateTo")%> "+beginDate+"\n";
                       errorNb++;
                     }
                   } else {
                     if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) {
                   if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) {
                         errorMsg+="  - '<%=resources.getString("PubDateFin")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
                         errorNb++;
                       }
                     }
                   }
                 }
               }
             }
             if (!checkHour(endHour))
             {
               errorMsg+="  - '<%=resources.getString("ToHour")%>' <%=resources.getString("GML.MustContainsCorrectHour")%>\n";
               errorNb++;
             }
             if (<%=isThumbnailMandatory%> && <%=isThumbnailField%>) {
                 if ($('#thumbnailFile').val() == '' && $('#thumbnail').attr("src") == 'null') {
                	 errorMsg+=" - '<%=resources.getString("Thumbnail")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
                     errorNb++;
                 }
				 if($('#thumbnail').attr("src") == 'null'){
					var logicalName = $('#thumbnailFile').val();
					var type = logicalName.substring(logicalName.lastIndexOf(".") + 1, logicalName.length);
                 	if (type == 'gif' || type == 'jpg'
                         || type == 'jpeg' || type == 'png') {
                 	}else{
                 		errorMsg+=" - '<%=resources.getString("Thumbnail")%>' <%=resources.getString("kmelia.EX_MSG_WRONG_TYPE_ERROR")%>\n";
                        errorNb++;
                    }
                 }
                 
             }
             switch(errorNb) {
               case 0 :
                 result = true;
             break;
               case 1 :
                 errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
                 window.alert(errorMsg);
                 result = false;
             break    ;
               default :
                 errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
                 window.alert(errorMsg);
                 result = false;
                 break;
               }
               return result;
             }

             function init() {
               document.pubForm.Name.focus();
             }

      <%
        if (pubDetail != null) {
          String lang = "";
          Iterator codes = pubDetail.getTranslations().keySet().iterator();
          while (codes.hasNext()) {
            lang = (String) codes.next();
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

        var galleryWindow = window;

        function choixGallery(liste)
        {
          index = liste.selectedIndex;
          var componentId = liste.options[index].value;
      if (index != 0)
          {
            url = "<%=m_context%>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=<%=kmeliaScc.getLanguage()%>";
            windowName = "galleryWindow";
            larg = "820";
            haut = "600";
            windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
            if (!galleryWindow.closed && galleryWindow.name=="galleryWindow")
            {
              galleryWindow.close();
            }
            galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
          }
        }

        function choixImageInGallery(url)
        {
          $("#thumbnailPreview").css("display", "block");
          $("#thumbnail").attr("src", url);
          $("#valueImageGallery").attr("value", url);
        }

        function addFavorite(name,description,url)
        {
        	urlWindow = "<%=m_context%>/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
            windowName = "favoriteWindow";
        	larg = "550";
        	haut = "250";
            windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
            if (!favoriteWindow.closed && favoriteWindow.name== "favoriteWindow")
                favoriteWindow.close();
            favoriteWindow = SP_openWindow(urlWindow, windowName, larg, haut, windowParams);
        }
                
    </script>
  </HEAD>
  <BODY id="<%=componentId%>" onload="init()" onunload="closeWindows()">
<%
        Window window = gef.getWindow();
        OperationPane operationPane = window.getOperationPane();

        Frame frame = gef.getFrame();
        Board board = gef.getBoard();
        Board boardHelp = gef.getBoard();

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
	      pathString = pubDetail.getName(language);
	      String namePath = spaceLabel + " > " + componentLabel;
	      if (!pathString.equals("")) {
	      	namePath = namePath + " > " + pathString;
	      }
		  operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAddPublication")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(namePath))+"','"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(pubDetail.getDescription(language)))+"','"+urlPublication+"')");
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
          displayAllOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        } else {
          displayOnNewOperations(kmeliaScc, gef, action, out);
        }

        out.println(frame.printBefore());
        if ("finish".equals(wizard)) {
          // cadre d'aide
          out.println(boardHelp.printBefore());
          out.println("<table border=\"0\"><tr>");
          out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\"" + resources.getIcon("kmelia.info") + "\"></td>");
          out.println("<td>" + resources.getString("kmelia.HelpView") + "</td>");
          out.println("</tr></table>");
          out.println(boardHelp.printAfter());
          out.println("<BR>");
        }
        out.println(board.printBefore());

  %>
  <div id="header">
  <TABLE CELLPADDING="5" WIDTH="100%">
    <FORM Name="pubForm" Action="publicationManager.jsp" Method="POST" enctype="multipart/form-data" accept-charset="UTF-8">
      <% if (kmeliaMode && "UpdateView".equals(action)) {%>
      <TR id="stateArea"><TD class="txtlibform"><%=resources.getString("PubState")%></TD>
        <TD>
          <%
             if (profile != null && !profile.equals("user")) {
               if ("ToValidate".equals(status)) {
                 out.println("<img src=\"" + outDraftSrc + "\" alt=\"" + resources.getString("PubStateToValidate") + "\">");
               } else if ("Draft".equals(status)) {
                 out.println("<img src=\"" + inDraftSrc + "\" alt=\"" + resources.getString("PubStateDraft") + "\">");
               } else if ("Valid".equals(status)) {
                 out.println("<img src=\"" + validateSrc + "\" alt=\"" + resources.getString("PublicationValidated") + "\">");
               } else if ("UnValidate".equals(status)) {
                 out.println("<img src=\"" + refusedSrc + "\" alt=\"" + resources.getString("PublicationRefused") + "\">");
               }
             }
          %>
        </TD>
      </TR>
      <% }%>
      <% if (kmeliaScc.isPublicationIdDisplayed() && action.equals("UpdateView")) {%>
      <TR><TD class="txtlibform"><%=resources.getString("kmelia.codification")%></TD>
        <TD><%=pubDetail.getPK().getId()%></TD></TR>
        <% }%>
        <%=I18NHelper.getFormLine(resources, pubDetail, language)%>
      <TR><TD class="txtlibform"><%=resources.getString("PubTitre")%></TD>
        <TD><input type="text" name="Name" id="pubName" value="<%=EncodeHelper.javaStringToHtmlString(name)%>" size="68" maxlength="150">&nbsp;<IMG src="<%=mandatorySrc%>" width="5" height="5" border="0"></TD></TR>

      <!-- DESCRIPTION -->
      <% if (isFieldDescriptionVisible) {%>
      <TR id="descriptionArea"><TD class="txtlibform" valign="top"><%=resources.getString("PubDescription")%></TD>
        <TD><TEXTAREA rows="6" cols="65" name="Description" id="pubDesc"><%=EncodeHelper.javaStringToHtmlString(description)%></TEXTAREA>
          <% if (isFieldDescriptionMandatory) {%>
          <IMG src="<%=mandatorySrc%>" width="5" height="5" border="0"/>
          <% }%>
        </TD></TR>
        <% }%>

      <% if (isFieldKeywordsVisible) {%>
      <TR id="keywordsArea"><TD class="txtlibform"><%=resources.getString("PubMotsCles")%></TD>
        <TD><input type="text" name="Keywords" id="pubKeys" value="<%=EncodeHelper.javaStringToHtmlString(keywords)%>" size="68" maxlength="100"></TD></TR>
          <% }%>

      <!-- Author -->
      <% if (kmeliaScc.isAuthorUsed()) {%>
      <TR id="authorArea">
        <TD class="txtlibform"><%=resources.getString("GML.author")%></TD>
        <TD><input type="text" name="Author" value="<%=EncodeHelper.javaStringToHtmlString(author)%>" size="68" maxlength="50"></TD>
      </TR>
      <% }%>
      <!-- Importance -->
      <% if (isFieldImportanceVisible) {%>
      <TR id="importanceArea"><TD class="txtlibform"><%=resources.getString("PubImportance")%></TD>
        <TD><select name="Importance">
            <% if (importance.equals("")) {
                 importance = "1";
               }
               int importanceInt = new Integer(importance).intValue();
               for (int i = 1; i <= 5; i++) {
                 if (i == importanceInt) {
                   out.println("<option selected value=\"" + i + "\">" + i + "</option>");
                 } else {
                   out.println("<option value=\"" + i + "\">" + i + "</option>");
                 }
               }
            %>
          </select>
        </TD></TR>
        <% } else {%>
      <input type="hidden" name="Importance" value="1">
      <% }%>
      <% if (isFieldVersionVisible) {%>
      <TR id="versionArea"><TD class="txtlibform"><%=resources.getString("PubVersion")%></TD>
        <TD><input type="text" name="Version" value="<%=EncodeHelper.javaStringToHtmlString(version)%>" size="5" maxlength="30"></TD></TR>
          <% }%>
      <TR id="creationArea"><TD class="txtlibform"><%=resources.getString("PubDateCreation")%></TD>
        <TD><%=creationDate%>&nbsp;<span class="txtsublibform"><%=resources.getString("kmelia.By")%></span>&nbsp;<%=creatorName%></TD></TR>

      <% if (updateDate != null && updateDate.length() > 0 && updaterName != null && updaterName.length() > 0) {%>
      <TR id="updateArea"><TD class="txtlibform"><%=resources.getString("PubDateUpdate")%></TD>
        <TD><%=updateDate%>&nbsp;<span class="txtsublibform"><%=resources.getString("kmelia.By")%></span>&nbsp;<%=updaterName%></TD></TR>
        <% }%>

      <% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {
           String selectUserLab = resources.getString("kmelia.SelectValidator");
           String link = "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');\">";
           link += "<img src=\""
               + resources.getIcon("kmelia.user")
               + "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
               + selectUserLab + "\" align=\"absmiddle\" title=\""
               + selectUserLab + "\"></a>";

      %>
      <TR><TD class="txtlibform"><%=resources.getString("kmelia.Valideur")%></TD>
        <TD>
          <% if (kmeliaScc.isTargetValidationEnable()) {%>
          <input type="text" name="Valideur" value="<%=targetValidatorName%>" size="60" readonly>
          <% } else {%>
          <textarea name="Valideur" value="<%=targetValidatorName%>" rows="4" cols="40" readonly><%=targetValidatorName%></textarea>
          <% }%>
          <input type="hidden" name="ValideurId" value="<%=targetValidatorId%>"><%=link%>&nbsp;<img src="<%=mandatorySrc%>" align="absmiddle" width="5" height="5" border="0"></TD></TR>
          <% }%>

      <TR id="beginArea"><TD class="txtlibform"><%=resources.getString("PubDateDebut")%></TD>
        <TD><input type="text" class="dateToPick" name="BeginDate" value="<%=beginDate%>" size="12" maxlength="10"/>
          <span class="txtsublibform">&nbsp;<%=resources.getString("ToHour")%>&nbsp;</span><input type="text" name="BeginHour" value="<%=beginHour%>" size="5" maxlength="5"> <i>(hh:mm)</i></TD></TR>
      <TR id="endArea"><TD class="txtlibform"><%=resources.getString("PubDateFin")%></TD>
        <TD><input type="text" class="dateToPick" name="EndDate" value="<%=endDate%>" size="12" maxlength="10"/>
          <span class="txtsublibform">&nbsp;<%=resources.getString("ToHour")%>&nbsp;</span><input type="text" name="EndHour" value="<%=endHour%>" size="5" maxlength="5"> <i>(hh:mm)</i></TD></TR>
          <% if (kmeliaMode && new Boolean(settings.getString("isVignetteVisible")).booleanValue()) {%>
          <TR>
          	<TD class="txtlibform"><%=resources.getString("Thumbnail")%></TD>
          	<TD>
		  <% if(isThumbnailField){
		  // mode création (-> champ de formulaire)
		  %>
				<div id="thumbnailPreview">
            	<%
               	out.println("<IMG SRC=\"" + vignette_url + "\" id=\"thumbnail\"/>");
               	out.println("<br/>");
            	%>
          		</div>
          	  	<input type="file" name="WAIMGVAR0" size="40" id="thumbnailFile"/>
	          <% // liste pour choisir une galerie
	             List galleries = kmeliaScc.getGalleries();
	             if (galleries != null) {
	               //zone pour le lien vers l'image
	               out.println("<span class=\"txtsublibform\"> ou </span><input type=\"hidden\" id=\"valueImageGallery\" name=\"valueImageGallery\"/>");
	
	               out.println(" <select id=\"galleries\" name=\"galleries\" onchange=\"choixGallery(this);this.selectedIndex=0;\"> ");
	               out.println(" <option selected>" + resources.getString("kmelia.galleries") + "</option> ");
	               for (int k = 0; k < galleries.size(); k++) {
	                 ComponentInstLight gallery = (ComponentInstLight) galleries.get(k);
	                 out.println(" <option value=\"" + gallery.getId() + "\">" + gallery.getLabel() + "</option> ");
	               }
	               out.println("</select>");
	             }
	          %>
	          <% if (isThumbnailMandatory) { %>
	          		<img src="<%=mandatorySrc%>" width="5" height="5" border="0" alt=""/>
	          <% } %>
          <%}else{
				String objectId = "";
				if(pubDetail != null)
					objectId =  pubDetail.getPK().getId();
					
				String requestURL = request.getRequestURL().toString();
				String applicationURL = URLManager.getApplicationURL();
				String prefixURL = requestURL.substring(0, requestURL.indexOf(applicationURL));
				String backUrl = prefixURL + applicationURL + "/Rkmelia/" + componentId + "/publicationManager.jsp?Action=UpdateView&PubId=" + objectId + "&Profile=" + kmeliaScc.getProfile();
					
				String standardParamaters = "&ComponentId=" + componentId +  
					                        "&ObjectId=" + objectId +
					                        "&BackUrl=" + URLEncoder.encode(backUrl) +
				                            "&ObjectType=" + ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE +
				                            "&height=380&width=800";
					
				String standardParamatersForAddOnly = "&ComponentId=" + componentId +  
                "&ObjectId=" + objectId +
                "&BackUrl=" + URLEncoder.encode(backUrl) +
                "&ObjectType=" + ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE +
                "&height=80&width=800";
				
				int[] thumbnailSize = kmeliaScc.getThumbnailWidthAndHeight();
				
				    if ( vignette_url != null )
					{
				    	// definition size of thumbnail selector
				    	String thumbnailWidth = "";
						String thumbnailHeight = "";
						String vignetteSizeParametersForUpdateFile = "";
						if(thumbnailSize[0] != -1){
							thumbnailWidth = String.valueOf(thumbnailSize[0]);
							vignetteSizeParametersForUpdateFile += "&ThumbnailWidth=" + String.valueOf(thumbnailSize[0]);
				        }else if(thumbnailSize[1] != -1){
							// square id one selected
				          	thumbnailWidth = String.valueOf(thumbnailSize[1]);
				        }else{
							thumbnailWidth = kmeliaScc.getThumbnailDefaultJcropWidth();	
						}
						if(thumbnailSize[1] != -1){
							thumbnailHeight = String.valueOf(thumbnailSize[1]);
							vignetteSizeParametersForUpdateFile += "&ThumbnailHeight=" + String.valueOf(thumbnailSize[1]);
						}else if(thumbnailSize[0] != -1){
						  // square id one selected
		          			thumbnailHeight = String.valueOf(thumbnailSize[0]);
		        		}else{
							thumbnailHeight = kmeliaScc.getThumbnailDefaultJcropHeight();
						}
						
				    	// mode modification
			    		out.println("<div id=\"thumbnailPreview\">");
				    	out.println("<IMG SRC=" + vignette_url + " id=\"thumbnail\">");
						if(isThumbnailMandatory){
							// vignette obligatoire -> 2 boutons modifier image et modifier fichier
							out.println("</div>");
							out.println("<BR>");
							out.println("<input alt=\"" + httpServerBase + m_context + "/Thumbnail/jsp/thumbnailManager.jsp?Action=UpdateFile" +
									standardParamatersForAddOnly + vignetteSizeParametersForUpdateFile +
									"&modal=true\" title=\"" + resources.getString("ThumbnailUpdateFile") + "\" class=\"thickbox\" type=\"button\" value=\"" + resources.getString("ThumbnailUpdateFile") + "\"/>");
						}else{
							// bouton suppression actif
							out.println("<a href=\"" + httpServerBase + m_context + "/Thumbnail/jsp/thumbnailManager.jsp?Action=Delete"+
									standardParamaters +
								    "\")><img border=\"0\" src=\""+deleteSrc+"\" alt=\""+resources.getString("ThumbnailDelete")+"\" title=\""+resources.getString("ThumbnailDelete")+"\"></a>");
							out.println("<BR>");
							out.println("</div>");
						}
						String vignetteSizeParameters = "&ThumbnailWidth=" + thumbnailWidth + "&ThumbnailHeight=" + thumbnailHeight;
				    	out.println("<input alt=\"" + httpServerBase + m_context + "/Thumbnail/jsp/thumbnailManager.jsp?Action=Update" +
								standardParamaters + vignetteSizeParameters +
								"&modal=true\" title=\"" + resources.getString("ThumbnailUpdate") + "\" class=\"thickbox\" type=\"button\" value=\"" + resources.getString("ThumbnailUpdate") + "\"/>");
						if (isThumbnailMandatory) { %>
		          			<img src="<%=mandatorySrc%>" width="5" height="5" border="0" alt=""/>
		          			<% }
						out.println("<BR>");
						
					} else {
						String vignetteSizeParameters = "";
						if(thumbnailSize[0] != -1){
							vignetteSizeParameters += "&ThumbnailWidth=" + String.valueOf(thumbnailSize[0]);
						}
						if(thumbnailSize[1] != -1){
							vignetteSizeParameters += "&ThumbnailHeight=" + String.valueOf(thumbnailSize[1]);
						}
						
						// mode création
						out.println("<input alt=\"" + httpServerBase + m_context + "/Thumbnail/jsp/thumbnailManager.jsp?Action=Add" +
								standardParamatersForAddOnly + vignetteSizeParameters +
					    		"&modal=true\" title=\"" + resources.getString("ThumbnailAdd") + "\" class=\"thickbox\" type=\"button\" value=\"" + resources.getString("ThumbnailAdd") + "\"/>");
						if (isThumbnailMandatory) { %>
		          			<img src="<%=mandatorySrc%>" width="5" height="5" border="0" alt=""/>
		          		        <% }
						out.println("<BR>");
					}
				%>
				<%
				if(errorThumbnail){
			    // message d'erreur
					%>
						<br/>
						<div style="font-style: italic;color:red;"><%=resources.getString("kmelia." + resultThumbnail)%></div>
						<br/>
					<%
				}
			}
          %>
        </TD>
      </TR> <%
     }%>
      <TR><TD><input type="hidden" name="Action"><input type="hidden" name="PubId" value="<%=id%>"><input type="hidden" name="Status" value="<%=status%>"><input type="hidden" name="TempId" value="<%=tempId%>"><input type="hidden" name="InfoId" value="<%=infoId%>"></TD></TR>
      <TR><TD colspan="2">( <img border="0" src="<%=mandatorySrc%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD></TR>
    </FORM>
  </TABLE>
  </div>
  <%
        out.println(board.printAfter());
        out.println(frame.printMiddle());
        if(isOwner || !"user".equalsIgnoreCase(profile)) {
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(validateButton);
          buttonPane.addButton(cancelButton);
          buttonPane.setHorizontalPosition();
          out.println("<BR><center>" + buttonPane.print() + "</center><BR>");
        }
        out.println(frame.printAfter());
        out.println(window.printAfter());
  %>
  <FORM name="toRouterForm">
    <input type="hidden" name="PubId" value="<%=id%>">
  </FORM>
</BODY>
</HTML>