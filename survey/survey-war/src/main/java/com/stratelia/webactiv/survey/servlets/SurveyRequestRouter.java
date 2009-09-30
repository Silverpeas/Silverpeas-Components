/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.survey.servlets;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.survey.control.SurveySessionController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;

public class SurveyRequestRouter extends ComponentRequestRouter {

  public String getFlag(String[] profiles) {
    String flag = "userClassic";
    for (int i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("userMultiple"))
        flag = profiles[i];
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin"))
        return profiles[i];
    }
    return flag;
  }

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    ComponentSessionController component = (ComponentSessionController) new SurveySessionController(
        mainSessionCtrl, componentContext);
    return component;
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "surveyScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
  {
      SilverTrace.info("Survey","SurveyRequestRouter.getDestination","Survey.MSG_ENTRY_METHOD");

      String flag = getFlag(componentSC.getUserRoles());
      String rootDest = "/survey/jsp/";
      SurveySessionController surveySC = (SurveySessionController) componentSC;
      if (flag.equals("userMultiple"))
    	  surveySC.setParticipationMultipleAllowedForUser(true);

      SilverTrace.info("Survey","SurveyRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE","surveyId="+surveySC.getSessionSurveyId());
      SilverTrace.info("Survey","SurveyRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE","surveyId="+request.getParameter("SurveyId"));

      surveySC.setPollingStationMode(false);
      SilverTrace.info("Survey","SurveyRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE","getComponentRootName() = "+surveySC.getComponentRootName());
	  if ("pollingStation".equals(surveySC.getComponentRootName()))
	  {
		  surveySC.setPollingStationMode(true);
	  }
	  request.setAttribute("PollingStationMode", new Boolean(surveySC.isPollingStationMode()));
	  
	  //Set status for this vote or survey
	  setAnonymousParticipationStatus(request, surveySC);
	  
      String destination = "";
      boolean profileError = false;
      if (function.startsWith("portlet"))
      {
          destination = rootDest + "portlet.jsp?Profile=" + flag;
      }
      else if (function.startsWith("Main") || function.startsWith("surveyList")) 
      {
          // the flag is the best user's profile
          destination = rootDest + "surveyList.jsp?Profile=" + flag;
      } 
      else if (function.startsWith("SurveyCreation") || function.startsWith("surveyCreator")) 
      {
    	  if (flag.equals("admin") || flag.equals("publisher")) {
    		  destination = rootDest + "surveyCreator.jsp";
    	  } else {
    		  profileError = true;
          }
      } 
      else if (function.equals("UpdateSurvey"))
      {
    	  String surveyId = request.getParameter("SurveyId");
     	  
    	  try 
    	  {
    		  // vérouiller l'enquête
        	  surveySC.closeSurvey(surveyId);
        	  
        	  // supprimer les participations 
    		  QuestionContainerDetail survey = surveySC.getSurvey(surveyId);
    		  surveySC.deleteVotes(surveyId);
     	  } 
    	  catch(Exception e)
    	  {
				SilverTrace.warn("Survey","SurveyRequestRouter.getDestination()","root.EX_USERPANEL_FAILED","function = "+function, e);
    	  }
    	  
    	  destination = rootDest + "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId=" + surveyId;
      }
/*      else if (function.startsWith("AfterSendVote")) {
          String id = request.getParameter("Id");
		  request.setAttribute("Profile", flag);
          destination = rootDest + "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+id;
      } */ 
      else if (function.equals("ViewListResult"))
      {
    	  String answerId = request.getParameter("AnswerId");
    	  Collection<String> users = new ArrayList<String>();
    	  try 
    	  {
    		  users = surveySC.getUsersByAnswer(answerId);
    	  } 
    	  catch(Exception e)
    	  {
				SilverTrace.warn("Survey","SurveyRequestRouter.getDestination()","root.EX_USERPANEL_FAILED","function = "+function, e);
    	  }	  
    	  request.setAttribute("Users", users);
    	  destination = rootDest + "answerResult.jsp";
      }
      else if (function.equals("ViewAllUsers"))
      {
    	  String surveyId = request.getParameter("SurveyId");
    	  Collection<String> users = new ArrayList<String>();
    	  try 
    	  {
    		  users = surveySC.getUsersBySurvey(surveyId);
    	  } 
    	  catch(Exception e)
    	  {
				SilverTrace.warn("Survey","SurveyRequestRouter.getDestination()","root.EX_USERPANEL_FAILED","function = "+function, e);
    	  }	  
    	  request.setAttribute("Users", users);
    	  destination = rootDest + "answerResult.jsp";
      }
      else if (function.equals("UserResult"))
      {
    	  String userId = request.getParameter("UserId");
    	  String userName = request.getParameter("UserName");
    	  Collection<String> result = new ArrayList<String>();
    	  try 
    	  {
    		  result = surveySC.getResultByUser(userId);
    	  } 
    	  catch(Exception e)
    	  {
				SilverTrace.warn("Survey","SurveyRequestRouter.getDestination()","root.EX_USERPANEL_FAILED","function = "+function, e);
    	  }	  
    	  request.setAttribute("ResultUser", result);
    	  request.setAttribute("UserName", userName);
    	  request.setAttribute("UserId", userId);
    	  request.setAttribute("Survey", surveySC.getSessionSurvey());
    	  
    	  destination = rootDest + "resultByUser.jsp";
      }
      else if (function.startsWith("searchResult")) {
          String id = request.getParameter("Id");
		  request.setAttribute("Profile", flag);
          destination = rootDest + "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId="+id;
      } 
      else if(function.equals("ToAlertUser"))
		{ 
			SilverTrace.debug("Survey","SurveyRequestRouter.getDestination()","root.MSG_GEN_PARAM_VALUE","ToAlertUser: function = "+function+" spaceId="+surveySC.getSpaceId()+" componentId="+ surveySC.getComponentId());
			String surveyId = request.getParameter("SurveyId");
			try
			{
				destination = surveySC.initAlertUser(surveyId);
			}
			catch(Exception e){
				SilverTrace.warn("Survey","SurveyRequestRouter.getDestination()","root.EX_USERPANEL_FAILED","function = "+function, e);
			}

			SilverTrace.debug("Survey","SurveyRequestRouter.getDestination()","root.MSG_GEN_PARAM_VALUE","ToAlertUser: function = "+function+"=> destination="+destination);
		}
      else {
		  request.setAttribute("Profile", flag);
          destination = rootDest + function;
      }

      if (profileError) {
          String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
          destination = sessionTimeout;
      } 
      //else {
          //destination = "/survey/jsp/" + destination;
      //}

      return destination;
  }

  /**
   * Read cookie from anonymous user and set status of anonymous user to allow
   * him to vote or not
   * 
   * @param request
   * @param surveyScc
   */
  private void setAnonymousParticipationStatus(HttpServletRequest request,
      SurveySessionController surveySC) {
    surveySC.hasAlreadyParticipated(false);
    if (request.getParameter("SurveyId") != null) {
      Cookie[] cookies = request.getCookies();
      for (int i = 0; i < cookies.length; i++) {
        Cookie currentCookie = cookies[i];
        if (currentCookie.getName().equals(
            SurveySessionController.COOKIE_NAME
                + request.getParameter("SurveyId")))
          surveySC.hasAlreadyParticipated(true);
      }
    }
  }
}