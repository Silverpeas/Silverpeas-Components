<%--

    Copyright (C) 2000 - 2024 Silverpeas

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

<jsp:useBean id="questionsVector" scope="session" class="java.util.ArrayList" />

<%@ include file="checkQuizz.jsp" %>
<%@ page import="org.silverpeas.core.web.http.HttpRequest" %>
<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.quizz.multilang.quizz"/>
<fmt:message key="GML.mandatory" var="labelMandatory"/>
<c:url var="mandatoryIcon" value="/util/icons/mandatoryField.gif"/>

<%
  String nextAction = "";

  int nbZone = 4; // nombre de zones à controler
  List<ComponentInstLight> galleries = quizzScc.getGalleries();
  if (!galleries.isEmpty()) {
    nbZone = nbZone + 2;
  }

  SettingBundle quizzSettings = quizzScc.getSettings();

  Button validateButton = null;
  Button cancelButton = null;
  ButtonPane buttonPane = null;

  List<FileItem> items = HttpRequest.decorate(request).getFileItems();
  String action = FileUploadUtil.getOldParameter(items, "Action", "");
  String question = FileUploadUtil.getOldParameter(items, "question", "");
  String clue =  FileUploadUtil.getOldParameter(items, "clue", "");
  String penalty = FileUploadUtil.getOldParameter(items, "penalty", "");
  String nbPointsMin = FileUploadUtil.getOldParameter(items, "nbPointsMin", "");
  String nbPointsMax = FileUploadUtil.getOldParameter(items, "nbPointsMax", "");
  String nbAnswers = FileUploadUtil.getOldParameter(items, "nbAnswers", "");
  String style = FileUploadUtil.getOldParameter(items, "questionStyle", "");
  boolean file = false;
  int nb = 0;
  int attachmentSuffix = 0;
  QuestionForm form = new QuestionForm(file, attachmentSuffix);
  List<Answer> answers = QuestionHelper.extractAnswer(items, form, quizzScc.getComponentId(), quizzSettings.getString("imagesSubDirectory"));
  file = form.isFile();
  attachmentSuffix = form.getAttachmentSuffix();
  int textareaMaxlength = DBUtil.getTextAreaLength();

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<view:sp-page>
  <view:sp-head-part withFieldsetStyle="true" withCheckFormScript="true">
    <style type="text/css">
      .thumbnailPreviewAndActions {
        display: none;
      }
    </style>
    <view:script src="/util/javaScript/dateUtils.js"/>
    <view:script src="/quizz/jsp/javascript/question.js"/>
  </view:sp-head-part>
<view:sp-body-part>
<%
  if (action.equals("SendNewQuestion")) {
    List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
    int questionNb = questionsV.size() + 1;
    int penaltyInt=0;
    int nbPointsMinInt=-1000;
    int nbPointsMaxInt=1000;
    if (!penalty.equals(""))
      penaltyInt=Integer.parseInt(penalty);
    if (!nbPointsMin.equals(""))
      nbPointsMinInt=Integer.parseInt(nbPointsMin);
    if (!nbPointsMax.equals(""))
      nbPointsMaxInt=Integer.parseInt(nbPointsMax);
    Question questionObject = new Question(null, null, question, null, clue, null, 0, style,penaltyInt,0,questionNb, nbPointsMinInt, nbPointsMaxInt);

    questionObject.setAnswers(answers);
    questionsV.add(questionObject);
    session.setAttribute("questionsVector", questionsV);
  } //End if action = ViewResult
  else if (action.equals("End")) {
    QuestionContainerDetail quizzDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
    //Vector 2 Collection
    List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
    quizzDetail.setQuestions(questionsV);
  }
  if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm"))) {
    List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
    int questionNb = questionsV.size() + 1;
    cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
    buttonPane = gef.getButtonPane();
    if (action.equals("CreateQuestion")) {
      validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
      question = "";
      nbAnswers = "";
      penalty = "";
      clue = "";
      nbPointsMin ="";
      nbPointsMax ="";
      nextAction="SendQuestionForm";
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
      buttonPane.setHorizontalPosition();
    } else if (action.equals("SendQuestionForm")) {
      validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData2()", false);
      nextAction="SendNewQuestion";
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
      buttonPane.setHorizontalPosition();
    }
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(quizzScc.getSpaceLabel());
    browseBar.setComponentName(quizzScc.getComponentLabel());
    browseBar.setExtraInformation(resources.getString("QuestionAdd"));

    out.println(window.printBefore());
%>

  <form name="quizzForm" action="questionCreatorBis.jsp" method="post" enctype="multipart/form-data">
    <%
      if (action.equals("SendQuestionForm")) { %>

    <input type="hidden" name="questionStyle" value="<%=style%>"/>
    <fieldset id="questionFieldset" class="skinFieldset">
      <legend><fmt:message key="quizz.header.fieldset.question" /></legend>
      <div class="fields">
        <div class="field" id="questionArea">
          <label for="question" class="txtlibform"><fmt:message key="QuizzCreationQuestion" />&nbsp;<%=questionNb%></label>
          <div class="champs"><textarea id="question" name="question" cols="49" rows="3" maxlength="50" readonly="readonly"><%=WebEncodeHelper.javaStringToHtmlString(question)%></textarea>&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/></div>
        </div>
        <div class="field" id="questionStyleArea">
          <label for="questionStyle" class="txtlibform"><fmt:message key="quizz.style" /></label>
          <div class="champs"><%=resources.getString("quizz."+style) %>
          </div>
        </div>
        <div class="field" id="nbAnswersArea">
          <label for="nbAnswers" class="txtlibform"><fmt:message key="QuizzCreationNbAnswers" /></label>
          <div class="champs">
            <input type="text" id="nbAnswers" name="nbAnswers" value="<%=nbAnswers%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;&nbsp;&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/>
          </div>
        </div>

        <div class="field" id="nbPointsMinArea">
          <label for="nbPointsMin" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMin" /></label>
          <div class="champs">
            <input type="text" id="nbPointsMin" name="nbPointsMin" value="<%=nbPointsMin%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
          </div>
        </div>

        <div class="field" id="nbPointsMaxArea">
          <label for="nbPointsMax" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMax" /></label>
          <div class="champs">
            <input type="text" id="nbPointsMax" name="nbPointsMax" value="<%=nbPointsMax%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
          </div>
        </div>

        <div class="field" id="clueArea">
          <label for="clue" class="txtlibform"><fmt:message key="QuizzClue" /></label>
          <div class="champs">
            <textarea name="clue" cols="49" rows="3" readonly="readonly"><%=WebEncodeHelper.javaStringToHtmlString(clue)%></textarea>
          </div>
        </div>

        <div class="field" id="penaltyArea">
          <label for="penalty" class="txtlibform"><fmt:message key="QuizzPenalty" /></label>
          <div class="champs">
            <input type="text" id="penalty" name="penalty" value="<%=penalty%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
          </div>
        </div>

      </div>
    </fieldset>

    <fieldset id="answersFieldset" class="skinFieldset">
      <legend><fmt:message key="quizz.header.fieldset.answers" /></legend>
      <div class="fields">

        <%
          nb = Integer.parseInt(nbAnswers);
          String inputName = "";
          int j=0;
          for (int i = 0; i < nb; i++) {
            j = i + 1;
            inputName = "answer"+i;
        %>

        <div class="field">
          <label for="<%=inputName%>" class="txtlibform"><fmt:message key="QuizzCreationAnswerNb" />&nbsp;<%=(i+1)%></label>
          <div class="champs">
            <textarea name="<%=inputName%>" id="<%=inputName%>" cols="49" rows="3"></textarea>&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/>
            <div class="points">
              <input type="text" name="nbPoints<%=i%>" id="nbPoints<%=i%>" value="" size="5" maxlength="3" />&nbsp;<fmt:message key="QuizzNbPoints"/>&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/>
            </div>
          </div>
        </div>

        <div class="field">
          <label for="comment<%=i%>" class="txtlibform"><fmt:message key="QuizzCreationAnswerComment" />&nbsp;<%=(i+1)%></label>
          <div class="champs">
            <textarea name="comment<%=i%>" id="comment<%=i%>" cols="49" rows="3" maxlength="<%=textareaMaxlength%>"></textarea>
          </div>
        </div>
        <%
          if (!style.equals("list")) {
        %>
        <div class="field fieldImage">
          <label for="image<%= i %>" class="txtlibform"><fmt:message key="QuizzCreationAnswerImage" />&nbsp;<%=(i+1)%></label>
          <div class="champs">
            <div class="thumbnailPreviewAndActions" id="thumbnailPreviewAndActions<%= i %>">
              <div class="thumbnailPreview">
                <img alt="" class="thumbnail" id="thumbnail<%= i %>" src="" />
              </div>
              <div class="thumbnailActions" id="thumbnailActions<%= i %>">
                <a href="javascript:deleteImage(<%=i%>)"><img title="<fmt:message key="quizz.answer.image.delete"/>" alt="<fmt:message key="quizz.answer.image.delete"/>" src="/silverpeas/util/icons/cross.png" /> <fmt:message key="quizz.answer.image.delete"/></a>
              </div>
            </div>

            <div class="thumbnailInputs">
              <img title="<%=surveyResource.getString("survey.answer.image.select")%>" alt="<%=surveyResource.getString("survey.answer.image.select")%>" src="/silverpeas/util/icons/images.png" /> <input type="file" id="thumbnailFile" size="40" name="image<%=i%>" />
              <%if (!galleries.isEmpty()) {%>
              <span class="txtsublibform"> ou </span><input type="hidden" name="valueImageGallery<%= i %>" id="valueImageGallery<%= i %>"/>
              <select class="galleries" name="galleries" onchange="choixGallery(this, '<%= i %>');this.selectedIndex=0;">
                <option selected><%= surveyResource.getString("GML.thumbnail.galleries") %></option>
                <%
                  for (ComponentInstLight gallery : galleries) { %>
                <option value="<%= gallery.getId() %>"><%= gallery.getLabel() %></option>
                <%        }
                } %>
              </select>
            </div>
          </div>
        </div>
        <%
            }
          }
        %>
      </div>
    </fieldset>

    <%
    } //end if action = SendQuestionForm
    else { //action= CreateQuestion %>
    <fieldset id="questionFieldset" class="skinFieldset">
      <legend><fmt:message key="quizz.header.fieldset.question" /></legend>
      <div class="fields">
        <div class="field" id="questionArea">
          <label for="question" class="txtlibform"><fmt:message key="QuizzCreationQuestion" />&nbsp;<%=questionNb%></label>
          <div class="champs"><textarea id="question" name="question" cols="49" rows="3"><%=WebEncodeHelper.javaStringToHtmlString(question)%></textarea>&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/></div>
        </div>
        <div class="field" id="questionStyleArea">
          <label for="questionStyle" class="txtlibform"><fmt:message key="quizz.style" /></label>
          <div class="champs">
            <select id="questionStyle" name="questionStyle" >
              <option selected value="null"><fmt:message key="quizz.style" /></option>
              <option value="radio"><fmt:message key="quizz.radio" /></option>
              <option value="checkbox"><fmt:message key="quizz.checkbox" /></option>
              <option value="list"><fmt:message key="quizz.list" /></option>
            </select>&nbsp;&nbsp;&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/>
          </div>
        </div>
        <div class="field" id="nbAnswersArea">
          <label for="nbAnswers" class="txtlibform"><fmt:message key="QuizzCreationNbAnswers" /></label>
          <div class="champs">
            <input type="text" id="nbAnswers" name="nbAnswers" value="<%=nbAnswers%>" size="5" maxlength="3"/>&nbsp;&nbsp;&nbsp;<img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/>
          </div>
        </div>

        <div class="field" id="nbPointsMinArea">
          <label for="nbPointsMin" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMin" /></label>
          <div class="champs">
            <input type="text" name="nbPointsMin" value="<%=nbPointsMin%>" size="5" maxlength="3" />&nbsp;<%=resources.getString("QuizzNbPoints")%>
          </div>
        </div>

        <div class="field" id="nbPointsMaxArea">
          <label for="nbPointsMax" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMax" /></label>
          <div class="champs">
            <input type="text" name="nbPointsMax" value="<%=nbPointsMax%>" size="5" maxlength="3"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
          </div>
        </div>

        <div class="field" id="clueArea">
          <label for="clue" class="txtlibform"><fmt:message key="QuizzClue" /></label>
          <div class="champs">
            <textarea name="clue" id="clue" cols="49" rows="3" maxlength="<%=textareaMaxlength%>"><%=WebEncodeHelper.javaStringToHtmlString(clue)%></textarea>
          </div>
        </div>

        <div class="field" id="penaltyArea">
          <label for="penaltyField" class="txtlibform"><fmt:message key="QuizzPenalty" /></label>
          <div class="champs">
            <input type="text" id="penaltyField" name="penalty" value="<%=penalty%>" size="5" maxlength="3"/>&nbsp;<%=resources.getString("QuizzNbPoints")%><span id="optionalField">&nbsp;</span>
          </div>
        </div>

      </div>
    </fieldset>

    <%
      } //end if CreateQuestion
    %>
    <div class="legend">
      <img border="0" src="${mandatoryIcon}" width="5" height="5" alt="${labelMandatory}"/> : <fmt:message key="GML.requiredField"/>
    </div>
    <input type="hidden" name="Action" value="<%=nextAction%>"/>
  </form>
  <%
    out.println(buttonPane.print());
    out.println(window.printAfter());
  }
  %>
</view:sp-body-part>
<%
  //End if action = CreateQuestion || SendQuestionForm
  if (action.equals("SendNewQuestion")) {
%>
  <view:sp-head-part>
  <script type="application/javascript">
    function goToQuestionsUpdate() {
      document.questionForm.submit();
    }
    whenSilverpeasEntirelyLoaded(function () {
      goToQuestionsUpdate();
    });
  </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <form name="questionForm" action="questionsUpdate.jsp" method="post">
      <input type="hidden" name="Action" value="UpdateQuestions" />
      <input type="hidden" name="X-STKN" value="${param['X-STKN']}"/>
    </form>
  </view:sp-body-part>
<% } %>
</view:sp-page>
