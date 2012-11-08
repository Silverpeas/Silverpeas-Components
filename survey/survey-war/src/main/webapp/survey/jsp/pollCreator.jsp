<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="com.stratelia.webactiv.survey.control.FileHelper" %>
<%@ page import="java.text.ParsePosition"%>

<%@ include file="checkSurvey.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
void displayAnswer(int i, String style, ResourcesWrapper resources, List<ComponentInstLight> galleries, JspWriter out) throws IOException {
  String inputName = "answer" + i;
  
  out.println("<div class=\"field\">");
  out.println("<label for=\""+inputName+"\" class=\"txtlibform\">"+resources.getString("SurveyCreationAnswerNb") + "&nbsp;" + (i+1)+"</label>");
  out.println("<div class=\"champs\">");
  	out.println("<input type=\"text\" name=\"" + inputName + "\" value=\"\" size=\"60\" maxlength=\"" + DBUtil.getTextFieldLength() + "\"/>");
  out.println("</div>");
  out.println("</div>");
  
  if (!style.equals("list")) {
    out.println("<div class=\"field fieldImage\">");
    out.println("<label for=\"image" + i + "\" class=\"txtlibform\">"+resources.getString("SurveyCreationAnswerImage") + "&nbsp;" + (i+1)+"</label>");
  	out.println("<div class=\"champs\">");
  	out.println("<div class=\"thumbnailPreviewAndActions\" id=\"thumbnailPreviewAndActions" + i + "\">");
  		out.println("<div class=\"thumbnailPreview\">");
  		out.println("<img alt=\"\" class=\"thumbnail\" id=\"thumbnail" + i + "\" src=\"null\">");
  		out.println("</div>");
  		out.println("<div class=\"thumbnailActions\" id=\"thumbnailActions" + i + "\">");
  		out.println("<a href=\"javascript:deleteImage("+i+")\"><img title=\""+resources.getString("survey.answer.image.delete")+"\" alt=\""+resources.getString("survey.answer.image.delete")+"\" src=\"/silverpeas/util/icons/cross.png\"> "+resources.getString("survey.answer.image.delete")+"</a>");
  		out.println("</div>");
  		out.println("</div>");
	
  		out.println("<div class=\"thumbnailInputs\">");
  		out.println("<img title=\""+resources.getString("survey.answer.image.select")+"\" alt=\""+resources.getString("survey.answer.image.select")+"\" src=\"/silverpeas/util/icons/images.png\"> <input type=\"file\" id=\"thumbnailFile\" size=\"40\" name=\"image"+i+"\">");
  		out.println("<span class=\"txtsublibform\"> ou </span><input type=\"hidden\" name=\"valueImageGallery" + i + "\" id=\"valueImageGallery" + i + "\">");
  		out.println(" <select class=\"galleries\" name=\"galleries\" onchange=\"choixGallery(this, '" + i + "');this.selectedIndex=0;\"> ");
	      out.println(" <option selected>" + resources.getString("survey.galleries") + "</option> ");
	      for (int k = 0; k < galleries.size(); k++) {
	        ComponentInstLight gallery = galleries.get(k);
	        out.println(" <option value=\"" + gallery.getId() + "\">" + gallery.getLabel() + "</option> ");
	      }
	      out.println("</select>");
	      out.println("</div>");
	out.println("</div>");
	out.println("</div>");
  }
}
%>

<%
    List<FileItem> items = FileUploadUtil.parseRequest(request);
    String action = FileUploadUtil.getOldParameter(items, "Action");
    String pollId = FileUploadUtil.getOldParameter(items, "PollId");
    String title = FileUploadUtil.getOldParameter(items, "title");
    String description = FileUploadUtil.getOldParameter(items, "description");
    String creationDate = "";
    String beginDate = FileUploadUtil.getOldParameter(items, "beginDate");
    String endDate = FileUploadUtil.getOldParameter(items, "endDate");
    if(endDate == null) {
      endDate = "";
    }
    String question = FileUploadUtil.getOldParameter(items, "question");
    String nbAnswers = FileUploadUtil.getOldParameter(items, "nbAnswers");
    if(nbAnswers == null) {
      nbAnswers = "";
    }
    String answerInput = "";
    String suggestionAllowed = "";
    String suggestionCheck = "";
    String suggestion = FileUploadUtil.getOldParameter(items, "SuggestionAllowed");
    String nextAction = "";
    String style = FileUploadUtil.getOldParameter(items, "questionStyle");

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

    String anonymousAllowed = "";
    String anonymousCheck = "";
    String anonymous = FileUploadUtil.getOldParameter(items, "AnonymousAllowed");

    String positions = FileUploadUtil.getOldParameter(items, "Positions");
    
    //Mode anonyme -> force les votes à être tous anonymes
	if(surveyScc.isAnonymousModeEnabled()) {
		anonymous = "1";
	}

//Icons
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

//Mise a jour de l'espace
    if (action == null) {
      action = "CreatePoll";
    }

    ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings",
        surveyScc.getLanguage());
    ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.survey.surveySettings",
        surveyScc.getLanguage());

    creationDate = resources.getOutputDate(new Date());
    beginDate = resources.getInputDate(new Date());

    Button validateButton = null;
    Button cancelButton = null;
    QuestionContainerDetail poll = null;


    File dir = null;
    String logicalName = null;
    String type = null;
    boolean file = false;
    String physicalName = null;
    String mimeType = null;
    long size = 0;
    int nb = 0;
    int attachmentSuffix = 0;
    ArrayList imageList = new ArrayList();
    List<Answer> answers = new ArrayList<Answer>();
    Answer answer = null;
    Iterator<FileItem> itemIter = items.iterator();
    while (itemIter.hasNext()) {
      FileItem item = (FileItem) itemIter.next();
      if (item.isFormField()) {
        String mpName = item.getFieldName();
        if (mpName.startsWith("answer")) {
          answerInput = item.getString(FileUploadUtil.DEFAULT_ENCODING);
          answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, false, null);
          answers.add(answer);
        } else if ("suggestionLabel".equals(mpName)) {
          answerInput = item.getString(FileUploadUtil.DEFAULT_ENCODING);
          answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, true, null);
          answers.add(answer);
        } else if (mpName.startsWith("valueImageGallery")) {
          if (StringUtil.isDefined(item.getString())) {
            // traiter les images venant de la gallery si pas d'image externe
            if (!file) {
              answer.setImage(item.getString());
            }
          }
        }
      } else {
        // it's a file part
        if (FileHelper.isCorrectFile(item)) {
          // the part actually contained a file
          logicalName = FileUploadUtil.getFileName(item);
          type = logicalName.substring(logicalName.indexOf(".") + 1, logicalName.length());
          physicalName = new Long(new Date().getTime()).toString() + attachmentSuffix + "." + type;
          attachmentSuffix = attachmentSuffix + 1;
          mimeType = item.getContentType();
          dir = new File(FileRepositoryManager.getAbsolutePath(surveyScc.getComponentId()) + settings.getString(
              "imagesSubDirectory") + File.separator + physicalName);
          FileUploadUtil.saveToFile(dir, item);
          size = item.getSize();
          if (size > 0) {
            answer.setImage(physicalName);
            file = true;
          }
        } else {
          // the field did not contain a file
          file = false;
        }
        out.flush();
      }
    }

%>
<html>
  <head>
    <title></title>
    <link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
    <view:looknfeel/>
    <style type="text/css">
      .thumbnailPreviewAndActions {
        display: none;
      }
    </style>
    <view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
  function sendData()
  {
    if (isCorrectForm()) {
      if (checkAnswers()) {
        if (window.document.pollForm.suggestion.checked) {
          window.document.pollForm.SuggestionAllowed.value = "1";
        }
        window.document.pollForm.anonymous.disabled = false;
        if (window.document.pollForm.anonymous.checked) {
            window.document.pollForm.AnonymousAllowed.value = "1";
        }
<% if ("SendPollForm".equals(action)) { %>
        <view:pdcPositions setIn="document.pollForm.Positions.value"/>;
<% } %>
        window.document.pollForm.submit();
      }
    }
  }

  function checkAnswers()
  {
    var errorMsg = "";
    var errorNb = 0;
    var answerEmpty = false;
    var imageEmpty = false;
    var fieldsEmpty = "";
    for (var i=0; i<document.pollForm.length; i++)
    {
      inputName = document.pollForm.elements[i].name.substring(0, 5);
      if (inputName == "answe" ) {
        if (isWhitespace(stripInitialWhitespace(document.pollForm.elements[i].value))) {
          answerEmpty = true;
        }
      }

      if (inputName == "image")
      {
        if (answerEmpty == true) {
          if (isWhitespace(stripInitialWhitespace(document.pollForm.elements[i].value))) {
            imageEmpty = true;
          }
        }
        answerEmpty = false;
      }

      if (inputName == "value")
      {
        if (imageEmpty == true) {
          if (isWhitespace(stripInitialWhitespace(document.pollForm.elements[i].value))) {
            fieldsEmpty += (parseInt(document.pollForm.elements[i].name.substring(17, document.pollForm.elements[i].name.length))+1)+",";
            errorNb++;
          }
        }
        imageEmpty = false;
      }
    }
    if(<%=!"0".equals(suggestion) && "SendQuestionForm".equals(action)%>){
      if (isWhitespace(stripInitialWhitespace(document.pollForm.suggestionLabel.value))) {
        errorNb++;
      }
    }
    switch(errorNb) {
      case 0 :
        result = true;
        break;
      default :
        fields = fieldsEmpty.split(",");
        for (var i=0; i < fields.length-1; i++) {
          errorMsg += "<%=resources.getString("SurveyCreationAnswerNb")%> "+fields[i]+" \n";
        }
        if(<%=!"0".equals(suggestion) && "SendQuestionForm".equals(action)%>){
          if (isWhitespace(stripInitialWhitespace(document.pollForm.suggestionLabel.value))) {
            errorMsg += "<%=resources.getString("OtherAnswer")%> \n";
          }
        }
        window.alert("<%=resources.getString("EmptyAnswerNotAllowed")%> \n" + errorMsg);
        result = false;
        break;
      }
      return result;
    }

    function isCorrectForm() {
      var errorMsg = "";
      var errorNb = 0;
      var title = stripInitialWhitespace(window.document.pollForm.title.value);
      var question = stripInitialWhitespace(window.document.pollForm.question.value);
      var nbAnswers = window.document.pollForm.nbAnswers.value;
      var beginDate = window.document.pollForm.beginDate.value;
      var endDate = window.document.pollForm.endDate.value;
      var beginDateOK = true;

      if (isWhitespace(title)) {
        errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
        errorNb++;
      }
      <% if ("SendQuestionForm".equals(action)) { %>
      if (window.document.pollForm.questionStyle.options[window.document.pollForm.questionStyle.selectedIndex].value=="null") {
        //choisir au moins un style
        errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("survey.style")%>' <%=resources.getString("GML.MustBeFilled")%> \n";
        errorNb++;
      }
      <% } %>
      if (!isWhitespace(beginDate)) {
    	if (!isDateOK(beginDate, '<%=resources.getLanguage()%>')) {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
          errorNb++;
          beginDateOK = false;
        }
      }
      if (!isWhitespace(endDate)) {
        if (!isDateOK(endDate, '<%=resources.getLanguage()%>')) {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
          errorNb++;
        } else {
            if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
              if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, '<%=resources.getLanguage()%>')) {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("MustContainsPostDateToBeginDate")%>\n";
                errorNb++;
              }
            } else {
              if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
                if (!isFuture(endDate, '<%=resources.getLanguage()%>')) {
                  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("MustContainsPostDate")%>\n";
                  errorNb++;
                }
              }
            }
        }
      }
      if (isWhitespace(question)) {
        errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationQuestion")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
        errorNb++;
      }
      if (isWhitespace(nbAnswers)) {
        errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
        errorNb++;
      } else {
        if (isInteger(nbAnswers)==false) {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
          errorNb++;
        } else {
          if (window.document.pollForm.suggestion.checked) {
            //nb min answers = 1
            if (nbAnswers <= 0) {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
              errorNb++;
            }
          } else {
            //nb min answers = 2
            if (nbAnswers <= 1) {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberGreaterThan2")%>\n";
              errorNb++;
            }
          }
        }
      }
   	  
      <% if ("SendQuestionForm".equals(action)) { %>
      	<view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
      <% } %>
   
   	  switch(errorNb) {
        case 0 :
          result = true;
          break;
        case 1 :
          errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
        default :
          errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
        }
        return result;
      }

      var galleryWindow = window;
      var currentAnswer;

      function choixGallery(liste, idAnswer)
      {
        currentAnswer = idAnswer;
        index = liste.selectedIndex;
        var componentId = liste.options[index].value;
        if (index != 0)
        {
          url = "<%=m_context%>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=<%=surveyScc.getLanguage()%>";
          windowName = "galleryWindow";
          larg = "820";
          haut = "600";
          windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
          if (!galleryWindow.closed && galleryWindow.name=="galleryWindow")
            galleryWindow.close();
          galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
        }
      }

      function deleteImage(idImage) {
        $("#thumbnailPreviewAndActions"+idImage).css("display", "none");
        $("#valueImageGallery"+idImage).attr("value", "");
      }
      
      function choixImageInGallery(url) {
        $("#thumbnailPreviewAndActions"+currentAnswer).css("display", "block");
        $("#thumbnailActions"+currentAnswer).css("display", "block");
        $("#thumbnail"+currentAnswer).attr("src", url);
        $("#valueImageGallery"+currentAnswer).attr("value", url);
      }

</script>
  </head>
  <body id="creation-page" class="pollingStation">
    <%
        if (("CreatePoll".equals(action)) || ("SendPollForm".equals(action))) {
          cancelButton = gef.getFormButton(generalMessage.getString("GML.cancel"), "Main.jsp", false);
          if (action.equals("CreatePoll")) {
            validateButton = gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=sendData()", false);
            nextAction = "SendPollForm";
          } else if ("SendPollForm".equals(action)) {
            validateButton = gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=sendData()", false);
            suggestionCheck = "";
            if (! "0".equals(suggestion)) {
              suggestionCheck = "checked";
            }
            anonymousCheck = "";
            if (! "0".equals(anonymous)) {
              anonymousCheck = "checked";
            }
            nextAction = "SendNewPoll";
          }

          Window window = gef.getWindow();
          Frame frame = gef.getFrame();

          BrowseBar browseBar = window.getBrowseBar();
          browseBar.setDomainName(surveyScc.getSpaceLabel());
          browseBar.setComponentName(surveyScc.getComponentLabel(), "Main.jsp");
          browseBar.setExtraInformation(surveyScc.getString("PollNewPoll"));

          out.println(window.printBefore());
          out.println(frame.printBefore());
    %>
    <!--DEBUT CORPS -->
    <form name="pollForm" action="pollCreator.jsp" method="post" enctype="multipart/form-data">
    	<fieldset id="info" class="skinFieldset">
			<legend><%=resources.getString("survey.header.fieldset.info") %></legend>
			<div class="fields">
				<div class="field" id="nameArea">
					<label class="txtlibform"><%=resources.getString("GML.name")%></label>
					<div class="champs">
						<input type="text" name="title" size="60" maxlength="60" value="<%=EncodeHelper.javaStringToHtmlString(title)%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
					</div>
				</div>
				<div class="field" id="questionArea">
					<label class="txtlibform"><%=resources.getString("SurveyCreationQuestion")%></label>
					<div class="champs">
						<input type="text" name="question" value="<%=EncodeHelper.javaStringToHtmlString(question)%>" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
					</div>
				</div>
				<div class="field" id="typeArea">
					<label class="txtlibform"><%=resources.getString("survey.style")%></label>
					<div class="champs">
						<% if (!"SendPollForm".equals(action)) {  %>
						<select id="questionStyle" name="questionStyle" onchange="showQuestionOptions(this.value);">
			              <option value="null"><%=resources.getString("survey.style")%></option>
			              <option <%="radio".equals(style) ? "selected=\"selected\"" : ""%> value="radio"><%=resources.getString("survey.radio")%></option>
			              <option <%="checkbox".equals(style) ? "selected=\"selected\"" : ""%> value="checkbox"><%=resources.getString("survey.checkbox")%></option>
			              <option <%="list".equals(style) ? "selected=\"selected\"" : ""%> value="list"><%=resources.getString("survey.list")%></option>
			            </select>
			            <% } else { %>
			            	<%=resources.getString("survey."+style)%><input type="hidden" name="questionStyle" value="<%=style%>"/>
			            <% } %>
					</div>
				</div>
				<%
				String disabledValue = "";
				if ("SendPollForm".equals(action)) {
					disabledValue = "disabled=\"disabled\"";
				} 
				%>
				<div class="field" id="nbAnswersArea">
					<label class="txtlibform"><%=resources.getString("SurveyCreationNbPossibleAnswer")%></label>
					<div class="champs">
						<input type="text" name="nbAnswers" value="<%=nbAnswers%>" size="3" maxlength="2" <%=disabledValue%>>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
					</div>
				</div>
				<div class="field" id="suggestionAllowedArea">
					<label class="txtlibform"><%=resources.getString("SuggestionAllowed")%></label>
					<div class="champs">
						<input type="checkbox" name="suggestion" value="" <%=suggestionCheck%> <%=disabledValue%>/>
					</div>
				</div>
				<div class="field" id="anonymousArea">
					<%
				        //Mode anonyme -> force les votes à être tous anonymes
				        String anonymousDisabled = "";
				        if(surveyScc.isAnonymousModeEnabled()) {
				          anonymousCheck = "checked=\"checked\"";
				          anonymousDisabled = "disabled=\"disabled\"";
				        }
					%>
					<label class="txtlibform"><%=resources.getString("survey.pollAnonymous")%></label>
					<div class="champs">
						<input type="checkbox" name="anonymous" value="" <%=anonymousCheck%> <%=disabledValue%> <%=anonymousDisabled%>/>
					</div>
				</div>
				<input type="hidden" name="Action" value="<%=nextAction%>"/>
            	<input type="hidden" name="SuggestionAllowed" value="0"/>
	            <input type="hidden" name="AnonymousAllowed" value="0"/>
			</div>
		</fieldset>
		
		<fieldset id="dates" class="skinFieldset">
				<legend><%=resources.getString("survey.header.fieldset.period") %></legend>
				<div class="fields">
					<div class="field" id="beginArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("SurveyCreationBeginDate")%></label>
						<div class="champs">
							<input type="text" class="dateToPick" name="beginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
						</div>
					</div>
					<div class="field" id="endArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("SurveyCreationEndDate")%></label>
						<div class="champs">
							<input type="text" class="dateToPick" name="endDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
						</div>
					</div>
				</div>
		</fieldset>
		
		<% if ("SendPollForm".equals(action)) { %>
		
		<fieldset id="answers" class="skinFieldset">
				<legend><%=resources.getString("survey.header.fieldset.answers") %></legend>
				<div class="fields">
				<%
					nb = Integer.parseInt(nbAnswers);
		  			List<ComponentInstLight> galleries = surveyScc.getGalleries();
		  			for (int i = 0; i < nb; i++) {
		    			displayAnswer(i, style, resources, galleries, out);
		  			}
		  		%>
		  			<% if (!"0".equals(suggestion)) { %>
					<div class="field" id="otherAnswerArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("OtherAnswer")%></label>
						<div class="champs">
							<input type="text" name="suggestionLabel" value="<%=resources.getString("SurveyCreationDefaultSuggestionLabel")%>" size="60" maxlength="50"/>
						</div>
					</div>
					<% } %>
				</div>
		</fieldset>
		
		<input type="hidden" name="Positions" />
      	<view:pdcNewContentClassification componentId="<%=componentId%>" />
		
		<% } %>

	<div class="legend">
		<img src="<%=mandatoryField%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%>
	</div>
    </form>

    <!-- FIN CORPS -->
    <%
          out.println(frame.printMiddle());
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(validateButton);
          buttonPane.addButton(cancelButton);
          buttonPane.setHorizontalPosition();
          out.println("<br/><center>" + buttonPane.print() + "</center><br/>");
          out.println(frame.printAfter());
          out.println(window.printAfter());
        } //End if action = ViewQuestion
        if ("SendNewPoll".equals(action)) {
          if (beginDate != null) {
            if (beginDate.length() > 0) {
              beginDate = resources.getDBDate(beginDate);
            }
          }
          if (endDate != null) {
            if (endDate.length() > 0) {
              endDate = resources.getDBDate(endDate);
            }
          }

          // création du vote
          boolean anonymousB = false;
          if (anonymous.equals("1")) {
            anonymousB = true;
          }
          QuestionContainerHeader surveyHeader = new QuestionContainerHeader(null, title, description, null, creationDate, beginDate, endDate, false, 0, 1, anonymousB);
          Question questionObject = new Question(null, null, question, "", "", null, style, 0);
          List<Question> questions = new ArrayList<Question>();
          questionObject.setAnswers(answers);
          questions.add(questionObject);
          QuestionContainerDetail surveyDetail = new QuestionContainerDetail(surveyHeader, questions, null, null);
          surveyDetail.setHeader(surveyHeader);
          surveyDetail.setQuestions(questions);
          surveyScc.setNewSurveyPositionsFromJSON(positions);
          surveyScc.createSurvey(surveyDetail);
          surveyScc.setSessionSurveyUnderConstruction(surveyDetail);
    %>
  <html>
    <head>
      <script language="Javascript">
            function goToList() {
              document.questionForm.submit();
            }
      </script>
    </head>
    <body onLoad="goToList()">
      <form name="questionForm" action="surveyList.jsp" method="post">
        <input type="hidden" name="Action" value="View">
      </form>
    </body>
  </html>
  <% }%>