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
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>
<%@ page import="com.stratelia.webactiv.survey.control.FileHelper" %>
<%@ include file="checkSurvey.jsp" %>

<%!
  void displaySurveyHeader(QuestionContainerDetail surveyDetail, ResourcesWrapper resources, SurveySessionController surveyScc, JspWriter out, GraphicElementFactory gef) throws SurveyException {
    try{
          QuestionContainerHeader surveyHeader = surveyDetail.getHeader();
          String title = EncodeHelper.javaStringToHtmlString(surveyHeader.getTitle());
          String creationDate = resources.getOutputDate(new Date());
          String beginDate = "&nbsp;";
          if (surveyHeader.getBeginDate() != null)
              beginDate = resources.getOutputDate(surveyHeader.getBeginDate());
          String endDate = "&nbsp;";
          if (surveyHeader.getEndDate() != null)
              endDate = resources.getOutputDate(surveyHeader.getEndDate());
          String nbQuestions = "&nbsp;";
          if (surveyHeader.getNbQuestionsPerPage() != 0)
              nbQuestions = new Integer(surveyHeader.getNbQuestionsPerPage()).toString();
          out.println("<center>");
          Board board = gef.getBoard();
          out.println(board.printBefore());
          out.println("<table width=\"100%\">");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("GML.name")+" :</td><td align=left width=\"50%\">"+title+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationDate")+" :</td><td align=left width=\"50%\">"+creationDate+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationBeginDate")+" :</td><td align=left width=\"50%\">"+beginDate+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationEndDate")+" :</td><td align=left width=\"50%\">"+endDate+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationNbQuestionPerPage")+" :</td><td align=left width=\"50%\">"+nbQuestions+"</td></tr>");
          out.println("</table>");
           out.println("</center>");
          out.println(board.printAfter());
    }
    catch( Exception e){
        throw new  SurveyException("questionCreator_JSP.displaySurveyHeader",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_SURVEY_HEADER",e);
    }

  }
%>

<%
String nextAction = "";
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
String px =  m_context + "/util/icons/colorPix/1px.gif";

ResourceLocator surveySettings = new ResourceLocator("org.silverpeas.survey.surveySettings", surveyScc.getLanguage());

String nbMaxAnswers = surveySettings.getString("NbMaxAnswers");

Button validateButton = null;
Button cancelButton = null;
Button finishButton = null;
ButtonPane buttonPane = null;

QuestionContainerDetail survey = null;

List<FileItem> items = FileUploadUtil.parseRequest(request);
String action = FileUploadUtil.getOldParameter(items, "Action");
String question = FileUploadUtil.getOldParameter(items, "question");
String nbAnswers = FileUploadUtil.getOldParameter(items, "nbAnswers");
String style = FileUploadUtil.getOldParameter(items, "questionStyle");
//String suggestionAllowed = FileUploadUtil.getOldParameter(items, "SuggestionAllowed");
String suggestionAllowed = "";
String suggestionCheck = "";
String suggestion = FileUploadUtil.getOldParameter(items, "SuggestionAllowed");
File dir = null;
String logicalName = null;
String type = null;
String physicalName = null;

boolean file = false;
long size = 0;
int nb = 0;
int attachmentSuffix = 0;
ArrayList imageList = new ArrayList();
List<Answer> answers = new ArrayList<Answer>();
Answer answer = null;
Iterator<FileItem> itemIter = items.iterator();
while (itemIter.hasNext()) {
  FileItem item = (FileItem) itemIter.next();
  if (item.isFormField())
  {
    String mpName = item.getFieldName();
    if (mpName.startsWith("answer")) {
        answer = new Answer(null, null, item.getString(FileUploadUtil.DEFAULT_ENCODING), 0, 0, false, "", 0, false, null);
        answers.add(answer);
    } else if ("suggestionLabel".equals(mpName)) {
        answer = new Answer(null, null, item.getString(FileUploadUtil.DEFAULT_ENCODING), 0, 0, false, "", 0, true, null);
        answers.add(answer);
    }
    else if (mpName.startsWith("valueImageGallery"))
    {
    	if (StringUtil.isDefined(item.getString()))
    	{
    		// traiter les images venant de la gallery si pas d'image externe
    		if (!file)
    			answer.setImage(item.getString());
    	}
    }
  }
  else
  {
    // it's a file
    if (FileHelper.isCorrectFile(item)) {
      // the part actually contained a file
      logicalName = FileUploadUtil.getFileName(item);
      type = logicalName.substring(logicalName.indexOf(".")+1, logicalName.length());
      physicalName = new Long(new Date().getTime()).toString() + attachmentSuffix + "." +type;
      attachmentSuffix = attachmentSuffix + 1;
      String mimeType = item.getContentType();
      dir = new File(FileRepositoryManager.getAbsolutePath(surveyScc.getComponentId())+surveySettings.getString("imagesSubDirectory")+File.separator+physicalName);
      FileUploadUtil.saveToFile(dir, item);
      size = item.getSize();
      if (size > 0)
      {
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<%out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript1.2">
function sendData() {
    if (isCorrectForm()) {
        if (checkAnswers()) {
            if (document.surveyForm.suggestion.checked)
            {
                document.surveyForm.SuggestionAllowed.value = "1";
            }
            document.surveyForm.submit();
        }
    }
}

function checkAnswers() {
     var errorMsg = "";
     var errorNb = 0;
     var answerEmpty = false;
     var imageEmpty = false;
     var fieldsEmpty = "";
     for (var i=0; i<document.surveyForm.length; i++)
     {
        var inputName = document.surveyForm.elements[i].name;
        if (inputName) {
        	inputName = inputName.substring(0, 5);
        }
        if (inputName == "answe" ) {
            if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
                  answerEmpty = true;
            }
        }

        if (inputName == "image")
        {
            if (answerEmpty == true) {
                  if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
                        imageEmpty = true;
                  }
            }
            answerEmpty = false;
        }

        if (inputName == "value")
        {
            if (imageEmpty == true) {
                  if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
                        fieldsEmpty += (parseInt(document.surveyForm.elements[i].name.substring(17, document.surveyForm.elements[i].name.length))+1)+",";
                        errorNb++;
                  }
            }
            imageEmpty = false;
        }
      }
	 if(<%=!"0".equals(suggestion) && "SendQuestionForm".equals(action)%>){
         if (isWhitespace(stripInitialWhitespace(document.surveyForm.suggestionLabel.value))) {
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
                if (isWhitespace(stripInitialWhitespace(document.surveyForm.suggestionLabel.value))) {
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
     var question = stripInitialWhitespace(document.surveyForm.question.value);
     var nbAnswers = document.surveyForm.nbAnswers.value;
     if (isWhitespace(question))
     {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationQuestion")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }

     if (document.surveyForm.questionStyle.options[document.surveyForm.questionStyle.selectedIndex].value=="null") {
     	//choisir au moins un style
	    	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("survey.style")%>' <%=resources.getString("GML.MustBeFilled")%> \n";
	    	errorNb++;
     }
     else
     {
     	if (document.surveyForm.questionStyle.options[document.surveyForm.questionStyle.selectedIndex].value!="open") {
	          //Closed Question
	          if (isWhitespace(nbAnswers)) {
	             errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
	             errorNb++;
	          } else {
	                 if (isInteger(nbAnswers)==false) {
	                     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
	                     errorNb++;
	                 } else {
	                      if (document.surveyForm.suggestion.checked) {
	                          //nb min answers = 1
	                          if (nbAnswers <= 0) {
	                             errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberGreaterThan")%> 1\n";
	                             errorNb++;
	                          }
	                      } else {
	                          //nb min answers = 2
	                          if (nbAnswers <= 1) {
	                             errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberGreaterThan")%> 2\n";
	                             errorNb++;
	                          }
	                      }
	                      if (nbAnswers > <%=nbMaxAnswers%>) {
	                         errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberLessThan")%> <%=nbMaxAnswers%>\n";
	                         errorNb++;
	                      }
	                 }
	          }
	     } else {
	          document.surveyForm.Action.value = "SendNewQuestion";
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

function goToEnd() {
    document.surveyForm.Action.value = "End";
    document.surveyForm.submit();
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
		{
			galleryWindow.close();
		}
		galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
}

function deleteImage(idImage)
{
	document.getElementById('imageGallery'+idImage).innerHTML = "";
	document.getElementById('valueImageGallery'+idImage).value = "";
}

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

function showQuestionOptions(value)
{
  if (value != "open" && value!= "null") {
    $("#trNbQuestions").show();
    $("#trSuggestion").show();
  } else {
    $("#trNbQuestions").hide();
    $("#trSuggestion").hide();
  }
}
</script>
</head>
<%
if (action.equals("FirstQuestion")) {
      surveyScc.setSessionQuestions(new ArrayList<Question>());
      action = "CreateQuestion";
}
if (action.equals("SendNewQuestion")) {
      Question questionObject = new Question(null, null, question, "", "", null, style, 0);
      questionObject.setAnswers(answers);
      List<Question> questionsV = surveyScc.getSessionQuestions();
      questionsV.add(questionObject);
      action = "CreateQuestion";
} //End if action = ViewResult
else if (action.equals("End")) {
      out.println("<body>");
      QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
      //Vector 2 Collection
      List<Question> questionsV = surveyScc.getSessionQuestions();
      surveyDetail.setQuestions(questionsV);
      out.println("</body></html>");
}
if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm"))) {
      out.println("<body>");
      List<Question> questionsV = surveyScc.getSessionQuestions();
      int questionNb = questionsV.size() + 1;
      cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
      buttonPane = gef.getButtonPane();
      if (action.equals("CreateQuestion")) {
            validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            finishButton = (Button) gef.getFormButton(resources.getString("Finish"), "javascript:onClick=goToEnd()", false);
            question = "";
            nbAnswers = "";
            suggestion = "";
            nextAction="SendQuestionForm";
            buttonPane.addButton(validateButton);
            if (questionsV.size() != 0) {
                buttonPane.addButton(finishButton);
            }
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      } else if (action.equals("SendQuestionForm")) {
            validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            if (suggestion.equals("0"))
                suggestionCheck = "";
            else
                suggestionCheck = "checked";
            nextAction="SendNewQuestion";
            buttonPane.addButton(validateButton);
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      }

      Window window = gef.getWindow();
      Frame frame = gef.getFrame();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(surveyScc.getSpaceLabel());
      browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
      browseBar.setExtraInformation(resources.getString("SurveyCreation"));

      out.println(window.printBefore());
      out.println(frame.printBefore());
      out.println("<center>");
      displaySurveyHeader(surveyScc.getSessionSurveyUnderConstruction(), resources, surveyScc, out, gef);
      out.println("<br>");
      Board board = gef.getBoard();
      out.println(board.printBefore());
%>
      <!--DEBUT CORPS -->
      <br/>

      <form name="surveyForm" action="questionCreator.jsp" method="post" enctype="multipart/form-data">
      <table border="0" cellPadding="3" cellSpacing="0" width="100%" class="intfdcolor4">
        <tr>
          <td class="txtlibform" width="30%"><%=resources.getString("SurveyCreationQuestion")%> <%=questionNb%> :</td>
          <td width="70%"><input type="text" name="question" value="<%=EncodeHelper.javaStringToHtmlString(question)%>" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
        </tr>
        <% if (action.equals("SendQuestionForm")) {
                if (!style.equals("open"))
                {
                  // question fermée
                  %>
                    <tr>
                      <td class="txtlibform" valign="top"><%= resources.getString("survey.style")%> :</td>
                      <td><%=resources.getString("survey."+style)%>
                	    <select style="visibility: hidden;" id="questionStyle" name="questionStyle"><option selected><%=style%></option></select>
                	  </td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resources.getString("SurveyCreationNbPossibleAnswer")%> :</td><td>
                        <input type="text" name="nbAnswers" value="<%=nbAnswers%>" size="3" disabled maxlength="2">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5" />
                      </td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resources.getString("SuggestionAllowed")%> :</td>
                      <td><input type="checkbox" name="suggestion" value="" <%=suggestionCheck%> disabled></td>
                    </tr>
                    <% 
                    nb =  Integer.parseInt(nbAnswers);
                    String inputName = "";
                    int j=0;
                    for (int i = 0; i < nb; i++)
                    {
                        j = i + 1;
                        inputName = "answer"+i;
                        out.println("<tr><td colspan=2 align=center>");
                        out.println("<table cellpadding=0 cellspacing=5 width=\"100%\">");
                        out.println("<tr><td class=\"intfdcolor\"><img src=\""+px+"\" border=\"0\"></td></tr>");
                        out.println("</table></td></tr>");
                        out.println("<tr><td class=\"txtlibform\">"+resources.getString("SurveyCreationAnswerNb")+"&nbsp;"+j+" :</td><td><input type=\"text\" name=\""+inputName+"\" value=\"\" size=\"60\" maxlength=\""+DBUtil.getTextFieldLength()+"\"></td></tr>");

                        if (!style.equals("list"))
                        {
                        	// afficher les photos que si on est pas dans le choix d'une liste déroulante
                        	out.println("<tr><td class=\"txtlibform\">"+resources.getString("SurveyCreationAnswerImage")+"&nbsp;"+j+" :</td><td><input type=\"file\" name=\"image"+i+"\" size=\"60\"></td></tr>");

	                     	//zone pour le lien vers l'image
	                        out.println("<tr><td></td><td><span id=\"imageGallery"+i+"\"></span>");
	                        out.println("<input type=\"hidden\" id=\"valueImageGallery"+i+"\" name=\"valueImageGallery"+i+"\" >");

	                        List<ComponentInstLight> galleries = surveyScc.getGalleries();
	                        if (galleries.size() > 0)
	    					{
	    						out.println(" <select id=\"galleries\" name=\"galleries\" onchange=\"choixGallery(this, '"+i+"');this.selectedIndex=0;\"> ");
	    						out.println(" <option selected>"+resources.getString("survey.galleries")+"</option> ");
	   							for(int k=0; k < galleries.size(); k++ )
	   							{
	   								ComponentInstLight gallery = (ComponentInstLight) galleries.get(k);
	   								out.println(" <option value=\""+gallery.getId()+"\">"+gallery.getLabel()+"</option> ");
	   							}
	    						out.println("</select>");
	    						out.println("</td>");
	    					}
	                    	out.println("</tr>");
                        }

                    }
                    if (!suggestion.equals("0"))
                    {%>
                    <tr>
                      <td colspan="2" align="center">
                        <table cellpadding="0" cellspacing="5" width="100%">
                          <tr><td class="intfdcolor"><img src="<%=px%>" border="0"></td></tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resources.getString("OtherAnswer")%>&nbsp;:</td>
                      <td><input type="text" name="suggestionLabel" value="<%=resources.getString("SurveyCreationDefaultSuggestionLabel")%>" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>"></td>
                    </tr>
                        <%
                    }
                }
                else
                {
                	// question ouverte
                	out.println("<input type=\"hidden\" name=\"style\" value="+style+" >");
                }
                out.println("<tr><td>(<img border=0 src=\""+mandatoryField+"\" width=5 height=5>&nbsp;:&nbsp;"+generalMessage.getString("GML.requiredField")+")</td></tr>");
           }
           else
           {
                // liste déroulante des choix possible
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("survey.style")+" :</td><td>");
                out.println(" <select id=\"questionStyle\" name=\"questionStyle\" onchange=\"showQuestionOptions(this.value);\"> ");
    			out.println(" <option selected value=\"null\">"+resources.getString("survey.style")+"</option> ");
				out.println(" <option value=\"open\">"+resources.getString("survey.open")+"</option> ");
				out.println(" <option value=\"radio\">"+resources.getString("survey.radio")+"</option> ");
				out.println(" <option value=\"checkbox\">"+resources.getString("survey.checkbox")+"</option> ");
				out.println(" <option value=\"list\">"+resources.getString("survey.list")+"</option> ");
    			out.println("</select>");
                out.println("</td></tr>");

                out.println("<tr id=\"trNbQuestions\" style=\"display:none;\"><td class=\"txtlibform\">"+resources.getString("SurveyCreationNbPossibleAnswer")+" :</td><td><input type=\"text\" name=\"nbAnswers\" value=\""+nbAnswers+"\" size=\"3\"  maxlength=\"2\">&nbsp;<img border=0 src=\""+mandatoryField+"\" width=5 height=5></td></tr>");
                out.println("<tr id=\"trSuggestion\" style=\"display:none;\"><td class=\"txtlibform\">"+resources.getString("SuggestionAllowed")+" :</td><td><input type=\"checkbox\" name=\"suggestion\" value=\"\" "+suggestionCheck+"></td></tr>");

                String inputName = "answer"+0;
                out.println("<tr><td><input type=\"hidden\" name=\""+inputName+"\"></td></tr>");
                out.println("<tr><td>(<img border=0 src=\""+mandatoryField+"\" width=5 height=5>&nbsp;:&nbsp;"+generalMessage.getString("GML.requiredField")+")</td></tr>");
           }
        %>
        <tr><td><input type="hidden" name="Action" value="<%=nextAction%>">
                <input type="hidden" name="SuggestionAllowed" value="0"></td></tr>
      </table>
      </form>
       <!-- FIN CORPS -->

<%
	  out.println(board.printAfter());
      out.println(frame.printMiddle());
      out.println("<br><center>"+buttonPane.print()+"</center>");
      out.println("</center>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
      out.println("</body></html>");
 } //End if action = ViewQuestion
if (action.equals("End")) {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script language="Javascript">
    function goToSurveyPreview() {
        document.questionForm.submit();
    }
</script>
</head>
<body onload="goToSurveyPreview()">
<form name="questionForm" action="surveyDetail.jsp" method="post">
<input type="hidden" name="Action" value="PreviewSurvey">
</form>
</body>
</html>
<% } %>