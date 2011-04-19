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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>

<%@ include file="checkSurvey.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%--
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
--%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="ctxPath" value="${pageContext.request.contextPath}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<fmt:message var="surveyConfirmUpdateLabel" key="survey.confirmUpdateSurvey" />

<%!String lockSrc = "";
  String unlockSrc = "";
  String surveyDeleteSrc = "";
  String surveyUpdateSrc = "";
  String addSurveySrc = "";
  String pdcUtilizationSrc = "";
  String linkSrc = "";
  String m_context =
      GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

  ArrayPane buildSurveyArrayToAdmin(GraphicElementFactory gef, SurveySessionController surveyScc,
      int view, Collection surveys, ResourcesWrapper resources,
      javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session,
      boolean pollingStationMode) throws ParseException {

    ArrayPane arrayPane =
        gef.getArrayPane("surveysList", "surveyList.jsp?Action=View", request, session);
    arrayPane.setVisibleLineNumber(100);

    if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) ||
        (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
      arrayPane.addArrayColumn(resources.getString("GML.name"));
      arrayPane.addArrayColumn(resources.getString("SurveyClosingDate"));

      if (pollingStationMode) {
        arrayPane.addArrayColumn(resources.getString("PollingStationNbVoters"));
      } else if (surveyScc.isParticipationMultipleUsed()) {
        arrayPane.addArrayColumn(resources.getString("SurveyNbParticipations"));
      } else {
        arrayPane.addArrayColumn(resources.getString("SurveyNbVoters"));
      }
      ArrayColumn arrayColumn = arrayPane.addArrayColumn(resources.getString("GML.operation"));
      arrayColumn.setSortable(false);
    } else {
      arrayPane.addArrayColumn(resources.getString("GML.name"));
      arrayPane.addArrayColumn(resources.getString("SurveyOpeningDate"));
      arrayPane.addArrayColumn(resources.getString("SurveyClosingDate"));
      ArrayColumn arrayColumn = arrayPane.addArrayColumn(resources.getString("GML.operation"));
      arrayColumn.setSortable(false);
    }

    if (surveys != null) {
      Iterator i = surveys.iterator();
      while (i.hasNext()) {
        QuestionContainerHeader survey = (QuestionContainerHeader) i.next();
        ArrayLine arrayLine = arrayPane.addArrayLine();
        String link = "";
        if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) ||
            (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
          if (survey.getPermalink() != null) {
            link =
                "&nbsp;<a href=\"" + survey.getPermalink() + "\"><img src=\"" + linkSrc +
                "\" border=\"0\" align=\"bottom\" alt=\"" +
                resources.getString("survey.CopySurveyLink") + "\" title=\"" +
                resources.getString("survey.CopySurveyLink") + "\"></a>";
          }

          //arrayLine.addArrayCellLink(survey.getTitle()+link, "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId());
          ArrayCellText arrayCellText0 =
              arrayLine
              .addArrayCellText("<a href=\"surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId=" +
              survey.getPK().getId() + "\">" + survey.getTitle() + "</a>" + link);
          arrayCellText0.setCompareOn(survey.getTitle());

          if (survey.getEndDate() == null)
            arrayLine.addArrayCellText("&nbsp;");
          else {
            Date date = DateUtil.parse(survey.getEndDate());
            ArrayCellText arrayCellText1 =
                arrayLine.addArrayCellText(resources.getOutputDate(date));
            arrayCellText1.setCompareOn(date);
          }

          ArrayCellText arrayCellText2 =
              arrayLine.addArrayCellText(Integer.toString(survey.getNbVoters()));
          arrayCellText2.setCompareOn(Integer.valueOf(survey.getNbVoters()));

          IconPane iconPane = gef.getIconPane();
          if (view == SurveySessionController.OPENED_SURVEYS_VIEW) {
            Icon closeIcon = iconPane.addIcon();
            closeIcon.setProperties(lockSrc, resources.getString("GML.lock") + " '" +
                EncodeHelper.javaStringToHtmlString(survey.getTitle()) + "'",
                "surveyList.jsp?Action=CloseSurvey&SurveyId=" + survey.getPK().getId());
          } else {
            Icon openIcon = iconPane.addIcon();
            openIcon.setProperties(unlockSrc, resources.getString("GML.unlock") + " '" +
                EncodeHelper.javaStringToHtmlString(survey.getTitle()) + "'",
                "surveyList.jsp?Action=OpenSurvey&SurveyId=" + survey.getPK().getId());
          }
          // mise à jour
          Icon updateIcon = iconPane.addIcon();
          updateIcon.setProperties(surveyUpdateSrc, resources.getString("GML.modify") + " '" +
              EncodeHelper.javaStringToHtmlString(survey.getTitle()) + "'", "javascript:updateSurvey('" +
              survey.getPK().getId() + "','" +
              EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(survey.getTitle())) + "','" + survey.getNbVoters() + "')");
          // suppression
          Icon deleteIcon = iconPane.addIcon();
          deleteIcon.setProperties(surveyDeleteSrc, resources.getString("GML.delete") + " '" +
              EncodeHelper.javaStringToHtmlString(survey.getTitle()) + "'", "javaScript:deleteSurvey('" +
              survey.getPK().getId() + "','" +
              EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(survey.getTitle())) + "')");
          iconPane.setSpacing("30px");
          arrayLine.addArrayCellIconPane(iconPane);
        } else {
          //arrayLine.addArrayCellLink(survey.getTitle(), "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId());

          if (survey.getPermalink() != null) {
            link =
                "&nbsp;<a href=\"" + survey.getPermalink() + "\"><img src=\"" + linkSrc +
                "\" border=\"0\" align=\"bottom\" alt=\"" +
                resources.getString("survey.CopySurveyLink") + "\" title=\"" +
                resources.getString("survey.CopySurveyLink") + "\"></a>";
          }

          ArrayCellText arrayCellText =
              arrayLine
              .addArrayCellText("<a href=\"surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId=" +
              survey.getPK().getId() + "\">" + survey.getTitle() + "</a>" + link);
          arrayCellText.setCompareOn(survey.getTitle());

          if (survey.getBeginDate() == null) {
            arrayLine.addArrayCellText("&nbsp;");
          } else {
            Date date = DateUtil.parse(survey.getBeginDate());
            ArrayCellText arrayCellText0 =
                arrayLine.addArrayCellText(resources.getOutputDate(date));
            arrayCellText0.setCompareOn(date);
          }

          if (survey.getEndDate() == null) {
            arrayLine.addArrayCellText("&nbsp;");
          } else {
            Date date = DateUtil.parse(survey.getEndDate());
            ArrayCellText arrayCellText1 =
                arrayLine.addArrayCellText(resources.getOutputDate(date));
            arrayCellText1.setCompareOn(date);
          }

          IconPane iconPane = gef.getIconPane();
          Icon updateIcon = iconPane.addIcon();
          updateIcon.setProperties(surveyUpdateSrc, resources.getString("GML.modify") + " '" +
              EncodeHelper.javaStringToHtmlString(survey.getTitle()) + "'",
              "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId=" + survey.getPK().getId());
          Icon deleteIcon = iconPane.addIcon();
          deleteIcon.setProperties(surveyDeleteSrc, resources.getString("GML.delete") + " '" +
              EncodeHelper.javaStringToHtmlString(survey.getTitle()) + "'", "javaScript:deleteSurvey('" +
              survey.getPK().getId() + "','" +
              EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(survey.getTitle())) + "')");
          iconPane.setSpacing("30px");
          arrayLine.addArrayCellIconPane(iconPane);
        }
      }
    }
    return arrayPane;
  }

  ArrayPane buildSurveyArrayToUser(GraphicElementFactory gef, SurveySessionController surveyScc,
      int view, Collection surveys, ResourcesWrapper resources,
      javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session,
      boolean pollingStationMode) throws ParseException {

    ArrayPane arrayPane =
        gef.getArrayPane("surveysList", "surveyList.jsp?Action=View", request, session);
    arrayPane.setVisibleLineNumber(100);

    if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) ||
        (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
      arrayPane.addArrayColumn(resources.getString("GML.name"));
      arrayPane.addArrayColumn(resources.getString("SurveyClosingDate"));

      if (pollingStationMode) {
        arrayPane.addArrayColumn(resources.getString("PollingStationNbVoters"));
      } else if (surveyScc.isParticipationMultipleUsed()) {
        arrayPane.addArrayColumn(resources.getString("SurveyNbParticipations"));
      } else {
        arrayPane.addArrayColumn(resources.getString("SurveyNbVoters"));
      }

    } else {
      arrayPane.addArrayColumn(resources.getString("GML.name"));
      arrayPane.addArrayColumn(resources.getString("SurveyOpeningDate"));
      arrayPane.addArrayColumn(resources.getString("SurveyClosingDate"));
    }

    if (surveys != null) {
      Iterator i = surveys.iterator();
      while (i.hasNext()) {
        QuestionContainerHeader survey = (QuestionContainerHeader) i.next();
        ArrayLine arrayLine = arrayPane.addArrayLine();
        String link = "";
        if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) ||
            (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
          //arrayLine.addArrayCellLink(survey.getTitle(), "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId());

          if (survey.getPermalink() != null)
            link =
                "&nbsp;<a href=\"" + survey.getPermalink() + "\"><img src=\"" + linkSrc +
                "\" border=\"0\" align=\"bottom\" alt=\"" +
                resources.getString("survey.CopySurveyLink") + "\" title=\"" +
                resources.getString("survey.CopySurveyLink") + "\"></a>";

          ArrayCellText arrayCellText0 =
              arrayLine
              .addArrayCellText("<a href=\"surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId=" +
              survey.getPK().getId() + "\">" + survey.getTitle() + "</a>" + link);
          arrayCellText0.setCompareOn(survey.getTitle());

          if (survey.getEndDate() == null)
            arrayLine.addArrayCellText("&nbsp;");
          else {
            Date date = DateUtil.parse(survey.getEndDate());
            ArrayCellText arrayCellText1 =
                arrayLine.addArrayCellText(resources.getOutputDate(date));
            arrayCellText1.setCompareOn(date);
          }

          ArrayCellText arrayCellText2 =
              arrayLine.addArrayCellText(new Integer(survey.getNbVoters()).toString());
          arrayCellText2.setCompareOn(new Integer(survey.getNbVoters()));
        } else {
          arrayLine.addArrayCellLink(survey.getTitle(), "#");

          if (survey.getBeginDate() == null)
            arrayLine.addArrayCellText("&nbsp;");
          else {
            Date date = DateUtil.parse(survey.getBeginDate());
            ArrayCellText arrayCellText0 =
                arrayLine.addArrayCellText(resources.getOutputDate(date));
            arrayCellText0.setCompareOn(date);
          }

          if (survey.getEndDate() == null)
            arrayLine.addArrayCellText("&nbsp;");
          else {
            Date date = DateUtil.parse(survey.getEndDate());
            ArrayCellText arrayCellText1 =
                arrayLine.addArrayCellText(resources.getOutputDate(date));
            arrayCellText1.setCompareOn(date);
          }
        }
      }
    }
    return arrayPane;
  }%>

<%
  //Retrieve parameters
  String action = (String) request.getParameter("Action");
  String profile = (String) request.getParameter("Profile");
  String iconsPath =
      GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

  //Icons
  lockSrc = iconsPath + "/util/icons/checkoutFile.gif";
  unlockSrc = iconsPath + "/util/icons/checkinFile.gif";
  surveyDeleteSrc = iconsPath + "/util/icons/delete.gif";
  surveyUpdateSrc = iconsPath + "/util/icons/update.gif";
  pdcUtilizationSrc = iconsPath + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
  linkSrc = iconsPath + "/util/icons/link.gif";
  if (pollingStationMode) {
    addSurveySrc = iconsPath + "/util/icons/polling_to_add.gif";
  } else {
    addSurveySrc = iconsPath + "/util/icons/survey_to_add.gif";
  }

  //Update space
  if (action == null) {
    action = "ViewOpenedSurveys";
  }
  if (action.equals("DeleteSurvey")) {
    String surveyId = (String) request.getParameter("SurveyId");
    surveyScc.deleteSurvey(surveyId);
    action = "View";
  } else if (action.equals("CloseSurvey")) {
    String surveyId = (String) request.getParameter("SurveyId");
    surveyScc.closeSurvey(surveyId);
    action = "ViewClosedSurveys";
  } else if (action.equals("OpenSurvey")) {
    String surveyId = (String) request.getParameter("SurveyId");
    surveyScc.openSurvey(surveyId);
    action = "ViewOpenedSurveys";
  }
  if (action.equals("ViewOpenedSurveys")) {
    surveyScc.setViewType(SurveySessionController.OPENED_SURVEYS_VIEW);
    action = "View";
  } else if (action.equals("ViewClosedSurveys")) {
    surveyScc.setViewType(SurveySessionController.CLOSED_SURVEYS_VIEW);
    action = "View";
  } else if (action.equals("ViewInWaitSurveys")) {
    surveyScc.setViewType(SurveySessionController.INWAIT_SURVEYS_VIEW);
    action = "View";
  }

  surveyScc.removeSessionSurveyUnderConstruction();
  surveyScc.removeSessionSurvey();
  surveyScc.removeSessionResponses();
%>
<html>
<head>
<title></title>
<view:looknfeel />
<script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
<script language="javascript1.2">
function viewOpenedSurveys() {
  document.surveysForm.Action.value = "ViewOpenedSurveys";
  document.surveysForm.submit();
}

function viewClosedSurveys() {
  document.surveysForm.Action.value = "ViewClosedSurveys";
  document.surveysForm.submit();
}

function viewInWaitSurveys() {
  document.surveysForm.Action.value = "ViewInWaitSurveys";
  document.surveysForm.submit();
}

function createSurvey() {
  document.newSurveyForm.Action.value = "CreateSurvey";
  document.newSurveyForm.submit();
}
function createPollingStation() {
	  document.newPollingStationForm.Action.value = "CreatePoll";
	  document.newPollingStationForm.submit();
	}

function deleteSurvey(surveyId, name) {
  if(window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString("ConfirmDeleteSurvey"))%> '" + name + "' ?")){
      document.surveysForm.Action.value = "DeleteSurvey";
      document.surveysForm.SurveyId.value = surveyId;
      document.surveysForm.submit();
  }
}

function updateSurvey(surveyId, name, nbVotes)
{
  var voteNumbers = parseInt(nbVotes);
  if(voteNumbers == 0 || window.confirm("<view:encodeJs string="${surveyConfirmUpdateLabel}" /> '" + name + "' ?")) {
    document.updateForm.action = "UpdateSurvey";
    document.updateForm.Action.value = "UpdateSurveyHeader";
    document.updateForm.SurveyId.value = surveyId;
    document.updateForm.submit();
  }
}

function clipboardPaste() {     
	  top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=RSurvey&SpaceFrom=<%=spaceId%>&ComponentFrom=<%=componentId%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("surveyList", "UTF-8"))%>&TargetFrame=MyMain&message=REFRESH');
	}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</head>

<body>
<%
  Window window = gef.getWindow();

  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(surveyScc.getSpaceLabel());
  browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp");

  if (profile.equals("admin") || profile.equals("publisher")) {
    OperationPane operationPane = window.getOperationPane();
    if (profile.equals("admin") && surveyScc.isPdcUsed()) {
      operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDC"),
          "javascript:openSPWindow('" + m_context + "/RpdcUtilization/jsp/Main?ComponentId=" +
          surveyScc.getComponentId() + "','utilizationPdc1')");
      operationPane.addLine();
    }
    if (pollingStationMode) {
      operationPane.addOperation(addSurveySrc, resources.getString("PollingStationNewVote"),
          "javaScript:createPollingStation()");
    } else {
      operationPane.addOperation(addSurveySrc, resources.getString("SurveyNewSurvey"),
          "javaScript:createSurvey()");
    }
    operationPane.addOperation(resources.getIcon("survey.paste"), resources
        .getString("GML.paste"), "javascript:onClick=clipboardPaste()");
  }

  String bodyPart = "";

  int view = surveyScc.getViewType();
  Collection surveys = surveyScc.getSurveys();

  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resources.getString("SurveyOpened"),
      "javaScript:onClick=viewOpenedSurveys()",
      (view == SurveySessionController.OPENED_SURVEYS_VIEW));
  tabbedPane.addTab(resources.getString("SurveyClosed"),
      "javaScript:onClick=viewClosedSurveys()",
      (view == SurveySessionController.CLOSED_SURVEYS_VIEW));
  tabbedPane.addTab(resources.getString("SurveyInWait"),
      "javaScript:onClick=viewInWaitSurveys()",
      (view == SurveySessionController.INWAIT_SURVEYS_VIEW));

  bodyPart += tabbedPane.print();

  Frame frame = gef.getFrame();

  ArrayPane arrayPane = null;
  if (profile.equals("admin") || profile.equals("publisher")) {
    arrayPane =
        buildSurveyArrayToAdmin(gef, surveyScc, view, surveys, resources, request, session, pollingStationMode);
  } else {
    arrayPane =
        buildSurveyArrayToUser(gef, surveyScc, view, surveys, resources, request, session, pollingStationMode);
  }

  //Retrieve array in the top corner           
  frame.addTop("<center><table cellpadding=0 cellspacing=0 border=0 width='98%'><tr><td>" +
      arrayPane.print() + "</td></tr></table></center>");

  bodyPart += frame.print();

  window.addBody(bodyPart);
  out.println(window.print());
%>

<form name="surveysForm" action="surveyList.jsp" method="post">
  <input type="hidden" name="Action" value=""> 
  <input type="hidden" name="SurveyId" value="">
</form>

<form name="updateForm" action="UpdateSurvey" method="post">
  <input type="hidden" name="Action" value=""> 
  <input type="hidden" name="SurveyId" value="">
</form>

<form name="newSurveyForm" action="surveyCreator.jsp" method="post">
  <input type="hidden" name="Action" value="">
</form>

<form name="newPollingStationForm" action="pollCreator.jsp" method="post" enctype="multipart/form-data">
  <input type="hidden" name="Action" value="">
</form>

</body>
</html>