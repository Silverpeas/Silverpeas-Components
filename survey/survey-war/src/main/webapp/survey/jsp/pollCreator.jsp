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
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="com.silverpeas.util.web.servlet.FileUploadUtil" %>
<%@ page import="com.stratelia.webactiv.survey.control.FileHelper" %>
<%@ page import="java.text.ParsePosition"%>

<%@ include file="checkSurvey.jsp" %>

<%
    List items = FileUploadUtil.parseRequest(request);
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
    ArrayList answers = new ArrayList();
    Answer answer = null;
    Iterator itemIter = items.iterator();
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
<HTML>
  <HEAD>
    <TITLE></TITLE>
    <%
        out.println(gef.getLookStyleSheet());
    %>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script language="JavaScript1.2">
      function sendData()
      {
        if (isCorrectForm()) {
          if (checkAnswers()) {
            if (window.document.pollForm.suggestion.checked)
              window.document.pollForm.SuggestionAllowed.value = "1";
            window.document.pollForm.anonymous.disabled = false;
            if (window.document.pollForm.anonymous.checked)
                window.document.pollForm.AnonymousAllowed.value = "1";
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

          var re = /(\d\d\/\d\d\/\d\d\d\d)/i;

          var beginDate = window.document.pollForm.beginDate.value;
          var yearBegin = extractYear(window.document.pollForm.beginDate.value, '<%=surveyScc.getLanguage()%>');
          var monthBegin = extractMonth(window.document.pollForm.beginDate.value, '<%=surveyScc.getLanguage()%>');
          var dayBegin = extractDay(window.document.pollForm.beginDate.value, '<%=surveyScc.getLanguage()%>');

          var endDate = window.document.pollForm.endDate.value;
          var yearEnd = extractYear(window.document.pollForm.endDate.value, '<%=surveyScc.getLanguage()%>');
          var monthEnd = extractMonth(window.document.pollForm.endDate.value, '<%=surveyScc.getLanguage()%>');
          var dayEnd = extractDay(window.document.pollForm.endDate.value, '<%=surveyScc.getLanguage()%>');

          var beginDateOK = true;

          if (isWhitespace(title)) {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
            errorNb++;
          }
          if (window.document.pollForm.questionStyle.options[window.document.pollForm.questionStyle.selectedIndex].value=="null") {
            //choisir au moins un style
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("survey.style")%>' <%=resources.getString("GML.MustBeFilled")%> \n";
            errorNb++;
          }
          if (isWhitespace(beginDate)) {
          } else {
            if (beginDate.replace(re, "OK") != "OK") {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
              errorNb++;
              beginDateOK = false;
            } else {
              if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                errorNb++;
                beginDateOK = false;
              }
            }
          }
          if (isWhitespace(endDate)) {
          } else {
            if (endDate.replace(re, "OK") != "OK") {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
              errorNb++;
            } else {
              if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                errorNb++;
              } else {
                if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
                  if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
                    errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("MustContainsPostDateToBeginDate")%>\n";
                    errorNb++;
                  }
                } else {
                  if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) {
                    //window.alert("ici");
                    if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) {
                      errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("MustContainsPostDate")%>\n";
                      errorNb++;
                    }
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

          function deleteImage(idImage)
          {
            document.getElementById('imageGallery'+idImage).innerHTML = "";
            document.getElementById('valueImageGallery'+idImage).value = "";
          }

          //function choixImageInGallery(url)
          //{
          //document.getElementById('imageGallery'+currentAnswer).innerHTML = "<a href=\""+url+"\" target=\"_blank\"><%=resources.getString("survey.imageGallery")%></a> <a href=\"javascript:deleteImage('"+currentAnswer+"')\"><img src=\"icons/questionDelete.gif\" border=\"0\" align=\"absmiddle\" alt=\"<%=resources.getString("GML.delete")%>\" title=\"<%=resources.getString("GML.delete")%>\"></a>";
          //document.getElementById('valueImageGallery'+currentAnswer).value = url;
          //}

          function choixImageInGallery(url)
          {
            var newLink = document.createElement("a");
            newLink.setAttribute("href", url);
            newLink.setAttribute("target", "_blank");

            var newLabel = document.createTextNode("<%=resources.getString("survey.imageGallery")%>");
            newLink.appendChild(newLabel);

            var removeLink =  document.createElement("a");
            removeLink.setAttribute("href", "javascript:deleteImage('"+currentAnswer+"')");
            var removeIcon = document.createElement("img");
            removeIcon.setAttribute("src", "icons/questionDelete.gif");
            removeIcon.setAttribute("border", "0");
            removeIcon.setAttribute("align", "absmiddle");
            removeIcon.setAttribute("alt", "<%=resources.getString("GML.delete")%>");
            removeIcon.setAttribute("title", "<%=resources.getString("GML.delete")%>");

            removeLink.appendChild(removeIcon);

            document.getElementById('imageGallery'+currentAnswer).appendChild(newLink);
            document.getElementById('imageGallery'+currentAnswer).appendChild(removeLink);

            document.getElementById('valueImageGallery'+currentAnswer).value = url;
          }

    </script>
  </HEAD>
  <BODY>
    <%
        if ((action.equals("CreatePoll")) || (action.equals("SendPollForm"))) {
          cancelButton = (Button) gef.getFormButton(generalMessage.getString("GML.cancel"), "Main.jsp", false);
          if (action.equals("CreatePoll")) {
            validateButton = (Button) gef.getFormButton(generalMessage.getString("GML.validate"),
                "javascript:onClick=sendData()", false);
            nextAction = "SendPollForm";
          } else if (action.equals("SendPollForm")) {
            validateButton = (Button) gef.getFormButton(generalMessage.getString("GML.validate"),
                "javascript:onClick=sendData()", false);
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
          Board board = gef.getBoard();

          BrowseBar browseBar = window.getBrowseBar();
          browseBar.setDomainName(surveyScc.getSpaceLabel());
          browseBar.setComponentName(surveyScc.getComponentLabel(), "Main.jsp");
          browseBar.setExtraInformation(surveyScc.getString("PollNewPoll"));

          out.println(window.printBefore());
          out.println(frame.printBefore());
          out.println(board.printBefore());
    %>
    <!--DEBUT CORPS -->
    <%
String disabledValue = "";
if (action.equals("SendPollForm")) {
disabledValue = "disabled";
}
    %>
    <table border=0 cellspacing=0 cellpadding=5 width="98%" align=center>
      <form name="pollForm" Action="pollCreator.jsp" method="POST" ENCTYPE="multipart/form-data">
        <tr><td class="txtlibform"><%=resources.getString("GML.name")%> :</td><td><input type="text" name="title" size="60" maxlength="60" value="<%=EncodeHelper.javaStringToHtmlString(title)%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"> </td></tr>
        <tr><td class="txtlibform"><%=resources.getString("SurveyCreationDate")%> :</td><td><%=creationDate%></td></tr>
        <tr><td class="txtlibform"><%=resources.getString("SurveyCreationBeginDate")%> :</td><td><input type="text" class="dateToPick" name="beginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/></td></tr>
        <tr><td class="txtlibform"><%=resources.getString("SurveyCreationEndDate")%> :</td><td><input type="text" class="dateToPick" name="endDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/></td></tr>
        <tr><td class="txtlibform"><%=resources.getString("SurveyCreationQuestion")%> :</td><td><input type="text" name="question" value="<%=Encode.javaStringToHtmlString(question)%>" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"> </td></tr>
            <%if (disabledValue != "disabled") {%>
        <!--  type de question -->
        <tr><td class="txtlibform" valign=top><%=resources.getString("survey.style")%> :</td><td>
            <select id="questionStyle" name="questionStyle" onchange="showQuestionOptions(this.value);">
              <option selected value="null"><%=resources.getString("survey.style")%></option>
              <option value="radio"><%=resources.getString("survey.radio")%></option>
              <option value="checkbox"><%=resources.getString("survey.checkbox")%></option>
              <option value="list"><%=resources.getString("survey.list")%></option>
            </select>
          </td></tr>
          <% } else {%>
        <select style="visibility: hidden;" id="questionStyle" name="questionStyle" value="<%=style%>"><option selected><%=style%></option></select>
        <%}%>

        <tr><td class="txtlibform"><%=resources.getString("SurveyCreationNbPossibleAnswer")%> :</td><td><input type="text" name="nbAnswers" value="<%=nbAnswers%>" size="3" maxlength="2" <%=disabledValue%>>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"> </td></tr>
        <tr><td class="txtlibform"><%=resources.getString("SuggestionAllowed")%> :</td><td><input type="checkbox" name="suggestion" value="" <%=suggestionCheck%> <%=disabledValue%>></td></tr>

        <%
        //Mode anonyme -> force les votes à être tous anonymes
        String anonymousDisabled = "";
        if(surveyScc.isAnonymousModeEnabled()) {
			anonymousCheck = "checked";
			anonymousDisabled = "disabled";
		}
		%>

        <tr><td class="txtlibform"><%=resources.getString("survey.pollAnonymous")%> :</td><td><input type="checkbox" name="anonymous" value="" <%=anonymousCheck%> <%=disabledValue%> <%=anonymousDisabled%>></td></tr>

        <% if ("SendPollForm".equals(action)) {
nb = new Integer(nbAnswers).intValue();
String inputName = "";
int j = 0;
for (int i = 0; i < nb; i++) {
  j = i + 1;
  inputName = "answer" + i;
  out.println(
      "<tr><td class=\"txtlibform\">" + resources.getString("SurveyCreationAnswerNb") + "&nbsp;" + j + " :</td><td><input type=\"text\" name=\"" + inputName + "\" value=\"\" size=\"60\" maxlength=\"" + DBUtil.getTextFieldLength() + "\"></td></tr>");
  if (!style.equals("list")) {
    out.println(
        "<tr><td class=\"txtlibform\">" + resources.getString("SurveyCreationAnswerImage") + "&nbsp;" + j + " :</td><td><input type=\"file\" name=\"image" + i + "\" size=\"60\"></td></tr>");

    //zone pour le lien vers l'image
    out.println("<tr><td></td><td><span id=\"imageGallery" + i + "\"></span>");
    out.println("<input type=\"hidden\" id=\"valueImageGallery" + i + "\" name=\"valueImageGallery" + i + "\" >");

    List galleries = surveyScc.getGalleries();
    if (galleries != null) {
      out.println(
          " <select id=\"galleries\" name=\"galleries\" onchange=\"choixGallery(this, '" + i + "');this.selectedIndex=0;\"> ");
      out.println(" <option selected>" + resources.getString("survey.galleries") + "</option> ");
      for (int k = 0; k < galleries.size(); k++) {
        ComponentInstLight gallery = (ComponentInstLight) galleries.get(k);
        out.println(" <option value=\"" + gallery.getId() + "\">" + gallery.getLabel() + "</option> ");
      }
      out.println("</select>");
      out.println("");
      out.println("</td>");
    }
    out.println("</tr>");
  }
}
if (!"0".equals(suggestion)) {
  out.println("<tr><td class=\"txtlibform\">" + resources.getString("OtherAnswer") + "&nbsp;:</td><td><input type=\"text\" name=\"suggestionLabel\" value=\"" + resources.
      getString("SurveyCreationDefaultSuggestionLabel") + "\" size=\"60\" maxlength=\"50\"></td></tr>");
    }

  }%>
        <tr><td>(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%>)</td></tr>
        <tr><td><input type="hidden" name="Action" value="<%=nextAction%>">
            <input type="hidden" name="SuggestionAllowed" value="0">
             <input type="hidden" name="AnonymousAllowed" value="0">
         </td></tr>
      </form>
    </table>

    <!-- FIN CORPS -->
    <%
          out.println(board.printAfter());
          out.println(frame.printMiddle());
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(validateButton);
          buttonPane.addButton(cancelButton);
          buttonPane.setHorizontalPosition();
          out.println("<BR><center>" + buttonPane.print() + "</center><br>");
          out.println(frame.printAfter());
          out.println(window.printAfter());
        } //End if action = ViewQuestion
        if (action.equals("SendNewPoll")) {
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
          ArrayList questions = new ArrayList();
          questionObject.setAnswers(answers);
          questions.add(questionObject);
          QuestionContainerDetail surveyDetail = new QuestionContainerDetail(surveyHeader, questions, null, null);
          surveyDetail.setHeader(surveyHeader);
          surveyDetail.setQuestions(questions);
          surveyScc.createSurvey(surveyDetail);
          surveyScc.setSessionSurveyUnderConstruction(surveyDetail);
    %>
  <HTML>
    <HEAD>
      <script language="Javascript">
            function goToList() {
              document.questionForm.submit();
            }
      </script>
    </HEAD>
    <BODY onLoad="goToList()">
      <Form name="questionForm" Action="surveyList.jsp" Method="POST">
        <input type="hidden" name="Action" value="View">
      </Form>
    </BODY>
  </HTML>
  <% }%>
