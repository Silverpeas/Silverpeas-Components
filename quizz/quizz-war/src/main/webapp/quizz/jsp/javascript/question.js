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
    var textarea = $(this),
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
  var mandatory = '<img class=\"mandatory\" src=\"'+webContext+'/util/icons/mandatoryField.gif\" alt=\"Obligatoire\" width=\"5\" height=\"5\"/>';
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

function sendData()
{

  let errorMsg = "";
  let errorNb = 0;
  let question = stripInitialWhitespace(document.quizzForm.question.value);
  let nbAnswers = document.quizzForm.nbAnswers.value;
  let clue = document.quizzForm.clue.value;
  let penalty = document.quizzForm.penalty.value;
  let nbPointsMin = document.quizzForm.nbPointsMin.value;
  let nbPointsMax = document.quizzForm.nbPointsMax.value;

  if (isWhitespace(nbAnswers))
  {
    errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationNbAnswers") + "' " + getString("GML.MustBeFilled") + "\n";
    errorNb++;
  }
  if (document.quizzForm.questionStyle.options[document.quizzForm.questionStyle.selectedIndex].value=="null") {
    //choisir au moins un style
    errorMsg+='  - '+theFieldMessage + "'" + getString("quizz.style") + "' " + getString("GML.MustBeFilled") +'\n';
    errorNb++;
  }
  else
  {
    if (isInteger(nbAnswers)==false)
    {
      errorMsg+="  - " + theFieldMessage+ "'" +getString("QuizzCreationNbAnswers") + "' " + getString("GML.MustContainsFloat")+"\n";
      errorNb++;
    }
    else
    {
      if (nbAnswers <= 0)
      {
        errorMsg+="  - " + theFieldMessage + "'" +getString("QuizzCreationNbAnswers") + "' " + getString("MustContainsPositiveNumber")+ "\n";
        errorNb++;
      }
    }
  }
  if (!isWhitespace(penalty))
  {
    if (isInteger(penalty)==false)
    {
      errorMsg+="  - " + theFieldMessage + "'" +getString("QuizzPenalty") + "' " + getString("GML.MustContainsFloat")+ "\n";
      errorNb++;
    }
    else
    {
      if (penalty <= 0)
      {
        errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzPenalty") + "' " + getString("MustContainsPositiveNumber") + "\n";
        errorNb++;
      }
    }
    if (isWhitespace(clue))
    {
      errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzClue") + "' " + getString("GML.MustBeFilled") + "\n";
      errorNb++;
    }
  }
  if (!isWhitespace(clue))
  {
    if (!isValidTextArea(document.quizzForm.clue))
    {
      errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzClue") + "' " + getString("MustContainsLessCar") + " " + textAreaLength + " " + getString("Caracters") + "\n";
      errorNb++;
    }
    if (isWhitespace(penalty))
    {
      errorMsg+="  - "+ theFieldMessage  + "'" + getString("QuizzPenalty") + "' " + getString("GML.MustBeFilled") +"\n";
      errorNb++;
    }
  }

  if (!isWhitespace(nbPointsMax))
  {
    if (isSignedInteger(nbPointsMax)==false)
    {
      errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationNbPointsMax") + "' " + getString("GML.MustContainsFloat") + "\n";
      errorNb++;
    }
    else
    {
      if (nbPointsMax <= 0)
      {
        errorMsg+="  - "+ theFieldMessage + "'" + getString("QuizzCreationNbPointsMax") + "' " + getString("MustContainsPositiveNumber") + "\n";
        errorNb++;
      }
    }
  }
  if (!isWhitespace(nbPointsMin))
  {
    if (isSignedInteger(nbPointsMin)==false)
    {
      errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationNbPointsMin") + "' " +  getString("GML.MustContainsFloat") + "\n";
      errorNb++;
    }
    else
    {
      if (parseInt(nbPointsMin, 10) >= parseInt(nbPointsMax, 10))
      {
        errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationNbPointsMin") + "' " + getString("MustContainsStrictlyInfNumber") + " " + getString("QuizzCreationNbPointsMax") + "\n";
        errorNb++;
      }
    }
  }

  if (isWhitespace(question)) {
    errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationQuestion") + "' " + getString("GML.MustBeFilled") + "\n";
    errorNb++;
  }
  switch(errorNb) {
    case 0 :
      document.quizzForm.submit();
      break;
    case 1 :
      errorMsg = getString("GML.ThisFormContains") + " 1 " + getString("GML.error") +":\n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = getString("GML.ThisFormContains") + " " + errorNb + " " + getString("GML.errors") + ":\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
}

function sendData2()
{
  let errorMsg = "";
  let errorNb = 0;
  let nb = Number(document.quizzForm.nbAnswers.value);
  let nbPointsMax = Number(document.quizzForm.nbPointsMax.value);
  let nbPointsMin = Number(document.quizzForm.nbPointsMin.value);

  for (let i = 0; i < nb; i++)
  {
    let answer = $("#answer"+i).val(); // document.quizzForm.elements[<%=nbZone%>*i+7].value
    let nbPoints = $("#nbPoints"+i).val(); //document.quizzForm.elements[<%=nbZone%>*i+8].value;
    let comment = $("#comment"+i).val(); //document.quizzForm.elements[<%=nbZone%>*i+9].value;

    if (isWhitespace(nbPoints))
    {
      errorMsg +="  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") + " " +String(i+1)+") " + getString("GML.MustBeFilled") + "\n";
      errorNb++;
    }
    else
    {
      if (isSignedInteger(nbPoints)==false)
      {
        errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") + " " +String(i+1)+") " + getString("GML.MustContainsFloat") +"\n";
        errorNb++;
      }
      else
      {
        if((document.quizzForm.nbPointsMax.value!='')&&(parseInt(nbPoints, 10) > parseInt(nbPointsMax, 10)))
        {
          errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") + " " +String(i+1)+") " + getString("MustContainsInfNumber") + " " + getString("QuizzCreationNbPointsMax") + "\n";
          errorNb++;
        }
        else
        {
          if((document.quizzForm.nbPointsMin.value!='')&&(parseInt(nbPoints, 10) < parseInt(nbPointsMin, 10)))
          {
            errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzNbPoints") + "' (" + getString("QuizzCreationAnswerNb") +" " + String(i+1)+") " + getString("MustContainsSupNumber") + " " + getString("QuizzCreationNbPointsMin") + "\n";
            errorNb++;
          }
        }
      }
    }
    if (isWhitespace(answer)) {
      errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationAnswerNb") + " " +String(i+1)+ "' " + getString("GML.MustBeFilled") + "\n";
      errorNb++;
    }
    if ((!isWhitespace(comment)) && (!isValidTextArea($("#comment"+i)))){
      errorMsg+="  - " + theFieldMessage + "'" + getString("QuizzCreationAnswerComment") + "' " + getString("MustContainsLessCar") + " " +textAreaLength + " " + getString("Caracters") + "\n";
      errorNb++;
    }
  }

  switch(errorNb) {
    case 0 :
      document.quizzForm.submit();
      break;
    case 1 :
      errorMsg = getString("GML.ThisFormContains") + " 1 " + getString("GML.error") +":\n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = getString("GML.ThisFormContains") + " " + errorNb + " " + getString("GML.errors") + ":\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
}


function goToEnd() {
  document.quizzForm.Action.value = "End";
  document.quizzForm.submit();
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
    url = webContext + "/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language="+currentUser.language;
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

