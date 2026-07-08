/**
 *
 * User: dle
 * Date: 06/07/2026
 */


$( document ).ready(function() {
  sp.i18n.load({
    bundle : 'org.silverpeas.quizz.multilang.quizz',
    language : currentUser.language,
    withGeneral: true
  });

  $('textarea[name="clue"]').on('keypress blur', function (event) {
    let textarea = $(this),
        text = textarea.val();
    if (text=="") {
      $("#penaltyField").val("");
      unmarkFieldRequired($('#optionalField'));
    }
    else {
      markFieldRequired($("#optionalField"));
    }
  });
});

function markFieldRequired(fieldObject) {
  let mandatory = '<img class="mandatory" src="'+webContext+'/util/icons/mandatoryField.gif" alt="Obligatoire" width="5" height="5"/>';
  if ($(fieldObject).find(".mandatory").length) {
    // do not add img twice
  } else {
    fieldObject.append(mandatory);
  }
}
function unmarkFieldRequired(fieldObject) {
  fieldObject.children(".mandatory").remove();
}

function getString(key) {
  return sp.i18n.get(key);
}

function confirmCancel()
{
  if (confirm(getString("ConfirmCancel")))
    self.location="Main.jsp";
}

const theFieldMessage =  getString('GML.theField') + " ";

// ---- utilitaire de collecte d'erreurs, partage par sendData() et sendData2() ----
function createErrorCollector()
{
  let messages = [];
  return {
    add : function(message) {
      messages.push(message);
    },
    count : function() {
      return messages.length;
    },
    toString : function() {
      return messages.join("");
    }
  };
}

function submitOrShowErrors(errors)
{
  let errorNb = errors.count();
  switch(errorNb) {
    case 0 :
      document.quizzForm.submit();
      break;
    case 1 :
      jQuery.popup.error(getString("GML.ThisFormContains") + " 1 " + getString("GML.error") + ":\n" + errors.toString());
      break;
    default :
      jQuery.popup.error(getString("GML.ThisFormContains") + " " + errorNb + " " + getString("GML.errors") + ":\n" + errors.toString());
  }
}

// ---- validations utilisees par sendData() ----
function validateNbAnswers(nbAnswers, questionStyleValue, errors)
{
  if (isWhitespace(nbAnswers))
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationNbAnswers") + "' " + getString("GML.MustBeFilled") + "\n");
  }
  if (questionStyleValue=="null")
  {
    //choisir au moins un style
    errors.add('  - '+theFieldMessage + "'" + getString("quizz.style") + "' " + getString("GML.MustBeFilled") +'\n');
  }
  else
  {
    if (isInteger(nbAnswers)==false)
    {
      errors.add("  - " + theFieldMessage+ "'" +getString("QuizzCreationNbAnswers") + "' " + getString("GML.MustContainsFloat")+"\n");
    }
    else if (nbAnswers <= 0)
    {
      errors.add("  - " + theFieldMessage + "'" +getString("QuizzCreationNbAnswers") + "' " + getString("MustContainsPositiveNumber")+ "\n");
    }
  }
}

function validatePenalty(penalty, clue, errors)
{
  if (isWhitespace(penalty) && !isWhitespace(clue)) {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzPenalty") + "' " +
        getString("GML.MustBeFilled") + "\n");
  }
  if (!isWhitespace(penalty))
  {
    if (isInteger(penalty)==false)
    {
      errors.add("  - " + theFieldMessage + "'" +getString("QuizzPenalty") + "' " + getString("GML.MustContainsFloat")+ "\n");
    }
    else if (penalty <= 0)
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzPenalty") + "' " + getString("MustContainsPositiveNumber") + "\n");
    }
  }
}

function validateClue(clue, penalty, errors)
{
  if (isWhitespace(clue) && !isWhitespace(penalty))
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzClue") + "' " + getString("GML.MustBeFilled") + "\n");
  }
  else
  {
    if (!isValidTextArea(document.quizzForm.clue))
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzClue") + "' " + getString("MustContainsLessCar") + " " + textAreaLength + " " + getString("Caracters") + "\n");
    }
  }
}

function validatePointsMax(nbPointsMax, errors)
{
  if (!isWhitespace(nbPointsMax))
  {
    if (isSignedInteger(nbPointsMax)==false)
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationNbPointsMax") + "' " + getString("GML.MustContainsFloat") + "\n");
    }
    else if (nbPointsMax <= 0)
    {
      errors.add("  - "+ theFieldMessage + "'" + getString("QuizzCreationNbPointsMax") + "' " + getString("MustContainsPositiveNumber") + "\n");
    }
  }
}

function validatePointsMin(nbPointsMin, nbPointsMax, errors)
{
  if (!isWhitespace(nbPointsMin))
  {
    if (isSignedInteger(nbPointsMin)==false)
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationNbPointsMin") + "' " +  getString("GML.MustContainsFloat") + "\n");
    }
    else if (Number.parseInt(nbPointsMin, 10) >= Number.parseInt(nbPointsMax, 10))
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationNbPointsMin") + "' " + getString("MustContainsStrictlyInfNumber") + " " + getString("QuizzCreationNbPointsMax") + "\n");
    }
  }
}

function validateQuestion(question, errors)
{
  if (isWhitespace(question))
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationQuestion") + "' " + getString("GML.MustBeFilled") + "\n");
  }
}

function sendData()
{
  let errors = createErrorCollector();
  let question = stripInitialWhitespace(document.quizzForm.question.value);
  let nbAnswers = document.quizzForm.nbAnswers.value;
  let clue = document.quizzForm.clue.value;
  let penalty = document.quizzForm.penalty.value;
  let nbPointsMin = document.quizzForm.nbPointsMin.value;
  let nbPointsMax = document.quizzForm.nbPointsMax.value;
  let questionStyleValue = document.quizzForm.questionStyle.options[document.quizzForm.questionStyle.selectedIndex].value;

  validateNbAnswers(nbAnswers, questionStyleValue, errors);
  validatePenalty(penalty, clue, errors);
  validateClue(clue, penalty, errors);
  validatePointsMax(nbPointsMax, errors);
  validatePointsMin(nbPointsMin, nbPointsMax, errors);
  validateQuestion(question, errors);

  submitOrShowErrors(errors);
}

// ---- validations utilisees par sendData2() ----
function validateAnswerPoints(index, nbPointsMaxRaw, nbPointsMinRaw, nbPointsMax, nbPointsMin, errors)
{
  let nbPoints = $("#nbPoints"+index).val();

  if (isWhitespace(nbPoints))
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") + " " +String(index+1)+") " + getString("GML.MustBeFilled") + "\n");
  }
  else if (isSignedInteger(nbPoints)==false)
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") + " " +String(index+1)+") " + getString("GML.MustContainsFloat") +"\n");
  }
  else
  {
    if (nbPointsMaxRaw!='' && Number.parseInt(nbPoints, 10) > Number.parseInt(nbPointsMax, 10))
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") + " " +String(index+1)+") " + getString("MustContainsInfNumber") + " " + getString("QuizzCreationNbPointsMax") + "\n");
    }
    if (nbPointsMinRaw!='' && Number.parseInt(nbPoints, 10) < Number.parseInt(nbPointsMin, 10))
    {
      errors.add("  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") +" " + String(index+1)+") " + getString("MustContainsSupNumber") + " " + getString("QuizzCreationNbPointsMin") + "\n");
    }
  }
}

function validateAnswerText(index, errors)
{
  let answer = $("#answer"+index).val();
  if (isWhitespace(answer))
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationAnswerNb") + " " +String(index+1)+ "' " + getString("GML.MustBeFilled") + "\n");
  }
}

function validateAnswerComment(index, errors)
{
  let comment = $("#comment"+index).val();
  if ((!isWhitespace(comment)) && (!isValidTextArea($("#comment"+index))))
  {
    errors.add("  - " + theFieldMessage + "'" + getString("QuizzCreationAnswerComment") + "' " + getString("MustContainsLessCar") + " " +textAreaLength + " " + getString("Caracters") + "\n");
  }
}

function validateAnswerRow(index, nbPointsMaxRaw, nbPointsMinRaw, nbPointsMax, nbPointsMin, errors)
{
  validateAnswerPoints(index, nbPointsMaxRaw, nbPointsMinRaw, nbPointsMax, nbPointsMin, errors);
  validateAnswerText(index, errors);
  validateAnswerComment(index, errors);
}

function sendData2()
{
  let errors = createErrorCollector();
  let nb = Number(document.quizzForm.nbAnswers.value);
  let nbPointsMaxRaw = document.quizzForm.nbPointsMax.value;
  let nbPointsMinRaw = document.quizzForm.nbPointsMin.value;
  let nbPointsMax = Number(nbPointsMaxRaw);
  let nbPointsMin = Number(nbPointsMinRaw);

  for (let i = 0; i < nb; i++)
  {
    validateAnswerRow(i, nbPointsMaxRaw, nbPointsMinRaw, nbPointsMax, nbPointsMin, errors);
  }

  submitOrShowErrors(errors);
}


function goToEnd() {
  document.quizzForm.Action.value = "End";
  document.quizzForm.submit();
}

let galleryWindow = window;
let currentAnswer;

function choixGallery(liste, idAnswer)
{
  currentAnswer = idAnswer;
  let index = liste.selectedIndex;
  let componentId = liste.options[index].value;
  if (index != 0)
  {
    let url = webContext + "/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language="+currentUser.language;
    let windowName = "galleryWindow";
    let larg = "820";
    let haut = "600";
    let windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
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