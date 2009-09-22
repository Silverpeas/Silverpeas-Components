<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="java.text.ParseException"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader "%>

<%@ include file="checkSurvey.jsp" %>


<%!
String lockSrc 				= "";
String unlockSrc 			= "";
String surveyDeleteSrc 		= "";
String surveyUpdateSrc 		= "";
String addSurveySrc 		= "";
String pdcUtilizationSrc 	= "";
String linkSrc				= "";
String m_context 			= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

ArrayPane buildSurveyArrayToAdmin(GraphicElementFactory gef, SurveySessionController surveyScc, int view, Collection surveys, ResourcesWrapper resources, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, boolean pollingStationMode) throws ParseException {

      ArrayPane arrayPane = gef.getArrayPane("surveysList", "surveyList.jsp?Action=View", request, session);
      arrayPane.setVisibleLineNumber(100);

      if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) || (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
            arrayPane.addArrayColumn(resources.getString("GML.name"));
            arrayPane.addArrayColumn(resources.getString("SurveyClosingDate"));
            
            if (pollingStationMode)
            	arrayPane.addArrayColumn(resources.getString("PollingStationNbVoters"));
            else if (surveyScc.isParticipationMultipleUsed()) {
            	arrayPane.addArrayColumn(resources.getString("SurveyNbParticipations"));
            }
 			else           
            	arrayPane.addArrayColumn(resources.getString("SurveyNbVoters"));
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
              if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) || (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
            	  if (survey.getPermalink() != null)
            		  link = "&nbsp;<a href=\""+survey.getPermalink()+"\"><img src=\""+linkSrc+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("survey.CopySurveyLink")+"\" title=\""+resources.getString("survey.CopySurveyLink")+"\"></a>";

                  //arrayLine.addArrayCellLink(survey.getTitle()+link, "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId());
                  ArrayCellText arrayCellText0 = arrayLine.addArrayCellText("<a href=\"surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId()+"\">"+survey.getTitle()+"</a>"+link);
                  arrayCellText0.setCompareOn(survey.getTitle());
                  
                  if (survey.getEndDate() == null)
                      arrayLine.addArrayCellText("&nbsp;");
                  else {
                  	  Date date = DateUtil.parse(survey.getEndDate());
                      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(resources.getOutputDate(date));
                      arrayCellText1.setCompareOn(date);
                  }

                  ArrayCellText arrayCellText2 = arrayLine.addArrayCellText(new Integer(survey.getNbVoters()).toString());
                  arrayCellText2.setCompareOn(new Integer(survey.getNbVoters()));

                  IconPane iconPane = gef.getIconPane();
                  if (view == SurveySessionController.OPENED_SURVEYS_VIEW) {
                      Icon closeIcon = iconPane.addIcon();
                      closeIcon.setProperties(lockSrc, resources.getString("GML.lock")+" '"+Encode.javaStringToHtmlString(survey.getTitle())+"'" , "surveyList.jsp?Action=CloseSurvey&SurveyId="+survey.getPK().getId());
                  } else {
                      Icon openIcon = iconPane.addIcon();
                      openIcon.setProperties(unlockSrc, resources.getString("GML.unlock")+" '"+Encode.javaStringToHtmlString(survey.getTitle())+"'" , "surveyList.jsp?Action=OpenSurvey&SurveyId="+survey.getPK().getId());
                  }
                  // mise à jour
                  Icon updateIcon = iconPane.addIcon();
                  updateIcon.setProperties(surveyUpdateSrc, resources.getString("GML.modify")+" '"+Encode.javaStringToHtmlString(survey.getTitle())+"'" , "javascript:updateSurvey('"+survey.getPK().getId()+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(survey.getTitle()))+"')");
                  // suppression
                  Icon deleteIcon = iconPane.addIcon();
                  deleteIcon.setProperties(surveyDeleteSrc, resources.getString("GML.delete")+" '"+Encode.javaStringToHtmlString(survey.getTitle())+"'" , "javaScript:deleteSurvey('"+survey.getPK().getId()+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(survey.getTitle()))+"')");
                  iconPane.setSpacing("30px");
                  arrayLine.addArrayCellIconPane(iconPane);
              } else {
                  //arrayLine.addArrayCellLink(survey.getTitle(), "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId());
            	  
            	  if (survey.getPermalink() != null)
            		  link = "&nbsp;<a href=\""+survey.getPermalink()+"\"><img src=\""+linkSrc+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("survey.CopySurveyLink")+"\" title=\""+resources.getString("survey.CopySurveyLink")+"\"></a>";

                  ArrayCellText arrayCellText = arrayLine.addArrayCellText("<a href=\"surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId()+"\">"+survey.getTitle()+"</a>"+link);
                  arrayCellText.setCompareOn(survey.getTitle());

                  if (survey.getBeginDate() == null)
                      arrayLine.addArrayCellText("&nbsp;");
                  else {
                  	  Date date = DateUtil.parse(survey.getBeginDate());
                      ArrayCellText arrayCellText0 = arrayLine.addArrayCellText(resources.getOutputDate(date));
                      arrayCellText0.setCompareOn(date);
                  }

                  if (survey.getEndDate() == null)
                      arrayLine.addArrayCellText("&nbsp;");
                  else {
                		Date date = DateUtil.parse(survey.getEndDate());
                      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(resources.getOutputDate(date));
                      arrayCellText1.setCompareOn(date);
                  }

                  IconPane iconPane = gef.getIconPane();
                  Icon updateIcon = iconPane.addIcon();
                  updateIcon.setProperties(surveyUpdateSrc, resources.getString("GML.modify")+" '"+Encode.javaStringToHtmlString(survey.getTitle())+"'" , "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId="+survey.getPK().getId());
                  Icon deleteIcon = iconPane.addIcon();
                  deleteIcon.setProperties(surveyDeleteSrc, resources.getString("GML.delete")+" '"+Encode.javaStringToHtmlString(survey.getTitle())+"'" , "javaScript:deleteSurvey('"+survey.getPK().getId()+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(survey.getTitle()))+"')");
                  iconPane.setSpacing("30px");
                  arrayLine.addArrayCellIconPane(iconPane);
              }
           }
       }
       return arrayPane;
}

ArrayPane buildSurveyArrayToUser(GraphicElementFactory gef, SurveySessionController surveyScc, int view, Collection surveys, ResourcesWrapper resources, javax.servlet.ServletRequest request, javax.servlet.http.HttpSession session, boolean pollingStationMode) throws ParseException {

      ArrayPane arrayPane = gef.getArrayPane("surveysList", "surveyList.jsp?Action=View", request, session);
      arrayPane.setVisibleLineNumber(100);

      if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) || (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
            arrayPane.addArrayColumn(resources.getString("GML.name"));
            arrayPane.addArrayColumn(resources.getString("SurveyClosingDate"));

            if (pollingStationMode)
            	arrayPane.addArrayColumn(resources.getString("PollingStationNbVoters"));
            else if (surveyScc.isParticipationMultipleUsed()) {
            	arrayPane.addArrayColumn(resources.getString("SurveyNbParticipations"));
            }
 						else           
            	arrayPane.addArrayColumn(resources.getString("SurveyNbVoters"));

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
              if ((view == SurveySessionController.OPENED_SURVEYS_VIEW) || (view == SurveySessionController.CLOSED_SURVEYS_VIEW)) {
                  //arrayLine.addArrayCellLink(survey.getTitle(), "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId());
            	  
            	  if (survey.getPermalink() != null)
            		  link = "&nbsp;<a href=\""+survey.getPermalink()+"\"><img src=\""+linkSrc+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("survey.CopySurveyLink")+"\" title=\""+resources.getString("survey.CopySurveyLink")+"\"></a>";

                  ArrayCellText arrayCellText0 = arrayLine.addArrayCellText("<a href=\"surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+survey.getPK().getId()+"\">"+survey.getTitle()+"</a>"+link);
                  arrayCellText0.setCompareOn(survey.getTitle());

                  if (survey.getEndDate() == null)
                      arrayLine.addArrayCellText("&nbsp;");
                  else {
                      Date date = DateUtil.parse(survey.getEndDate());
                      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(resources.getOutputDate(date));
                      arrayCellText1.setCompareOn(date);
                  }

                  ArrayCellText arrayCellText2 = arrayLine.addArrayCellText(new Integer(survey.getNbVoters()).toString());
                  arrayCellText2.setCompareOn(new Integer(survey.getNbVoters()));
              } else {
                  arrayLine.addArrayCellLink(survey.getTitle(), "#");

                  if (survey.getBeginDate() == null)
                      arrayLine.addArrayCellText("&nbsp;");
                  else {
                      Date date = DateUtil.parse(survey.getBeginDate());
                      ArrayCellText arrayCellText0 = arrayLine.addArrayCellText(resources.getOutputDate(date));
                      arrayCellText0.setCompareOn(date);
                  }

                  if (survey.getEndDate() == null)
                      arrayLine.addArrayCellText("&nbsp;");
                  else {
                      Date date = DateUtil.parse(survey.getEndDate());
                      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(resources.getOutputDate(date));
                      arrayCellText1.setCompareOn(date);
                  }
              }
           }
       }
       return arrayPane;
}
%>

<% 
//Récupération des paramètres
String action			= (String) request.getParameter("Action");
String profile			= (String) request.getParameter("Profile");
String iconsPath		= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
lockSrc = iconsPath + "/util/icons/checkoutFile.gif";
unlockSrc = iconsPath + "/util/icons/checkinFile.gif";
surveyDeleteSrc = iconsPath + "/util/icons/delete.gif";
surveyUpdateSrc = iconsPath + "/util/icons/update.gif";
pdcUtilizationSrc	= iconsPath + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
linkSrc = iconsPath + "/util/icons/link.gif";
if (pollingStationMode)
	addSurveySrc = iconsPath + "/util/icons/polling_to_add.gif";
else
	addSurveySrc = iconsPath + "/util/icons/survey_to_add.gif";


//Mise a jour de l'espace
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
<HTML>
<HEAD>
<TITLE></TITLE>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
<SCRIPT Language="JavaScript1.2">
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
  if(window.confirm("<%=Encode.javaStringToJsString(resources.getString("ConfirmDeleteSurvey"))%> '" + name + "' ?")){
      document.surveysForm.Action.value = "DeleteSurvey";
      document.surveysForm.SurveyId.value = surveyId;
      document.surveysForm.submit();
  }
}

function updateSurvey(surveyId, name)
{
	if(window.confirm("<%=Encode.javaStringToJsString(resources.getString("survey.confirmUpdateSurvey"))%> '" + name + "' ?"))
	{
		document.updateForm.action = "UpdateSurvey";
      	document.updateForm.Action.value = "UpdateSurveyHeader";
      	document.updateForm.SurveyId.value = surveyId;
      	document.updateForm.submit();
    }
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</SCRIPT>
</HEAD>
<BODY>
<% 
          Window window = gef.getWindow();

          BrowseBar browseBar = window.getBrowseBar();
          browseBar.setDomainName(surveyScc.getSpaceLabel());
          browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp");

		  if (profile.equals("admin") || profile.equals("publisher")) {
              OperationPane operationPane = window.getOperationPane();
			  if (profile.equals("admin") && surveyScc.isPdcUsed())
			  {
				  operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDC"), "javascript:openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+surveyScc.getComponentId()+"','utilizationPdc1')");
				  operationPane.addLine();
			  }
			  if (pollingStationMode)
				  operationPane.addOperation(addSurveySrc, resources.getString("PollingStationNewVote"), "javaScript:createPollingStation()");
			  else
				  operationPane.addOperation(addSurveySrc, resources.getString("SurveyNewSurvey"), "javaScript:createSurvey()");
          }
          
          String bodyPart = "";

          int view = surveyScc.getViewType();
          Collection surveys = surveyScc.getSurveys();

          TabbedPane tabbedPane = gef.getTabbedPane();
          tabbedPane.addTab(resources.getString("SurveyOpened"), "javaScript:onClick=viewOpenedSurveys()", (view == SurveySessionController.OPENED_SURVEYS_VIEW));
          tabbedPane.addTab(resources.getString("SurveyClosed"), "javaScript:onClick=viewClosedSurveys()", (view == SurveySessionController.CLOSED_SURVEYS_VIEW));
          tabbedPane.addTab(resources.getString("SurveyInWait"), "javaScript:onClick=viewInWaitSurveys()", (view == SurveySessionController.INWAIT_SURVEYS_VIEW));

          bodyPart += tabbedPane.print();
                    
		  Frame frame = gef.getFrame();

          ArrayPane arrayPane = null;
          if (profile.equals("admin") || profile.equals("publisher"))
              arrayPane = buildSurveyArrayToAdmin(gef, surveyScc, view, surveys, resources, request, session, pollingStationMode);
          else
              arrayPane = buildSurveyArrayToUser(gef, surveyScc, view, surveys, resources, request, session, pollingStationMode);
          
          //Récupération du tableau dans le haut du cadre          
		  frame.addTop("<center><table cellpadding=0 cellspacing=0 border=0 width='98%'><tr><td>"+arrayPane.print()+"</td></tr></table></center>");

		  bodyPart += frame.print();

          window.addBody(bodyPart);
          out.println(window.print());
      %>

<FORM NAME="surveysForm" ACTION="surveyList.jsp" METHOD="POST">
<input type="hidden" name="Action" value="">
<input type="hidden" name="SurveyId" value="">
</FORM>

<FORM NAME="updateForm" ACTION="UpdateSurvey" METHOD="POST">
<input type="hidden" name="Action" value="">
<input type="hidden" name="SurveyId" value="">
</FORM>

<FORM NAME="newSurveyForm" ACTION="surveyCreator.jsp" METHOD="POST">
<input type="hidden" name="Action" value="">
</FORM>

<FORM NAME="newPollingStationForm" ACTION="pollCreator.jsp" METHOD="POST" ENCTYPE="multipart/form-data">
<input type="hidden" name="Action" value="">
</FORM>

</BODY>
</HTML>