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
package com.stratelia.webactiv.survey.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.survey.SurveyException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBm;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBmHome;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;

public class SurveySessionController extends AbstractComponentSessionController
{

	private QuestionContainerBm questionContainerBm = null;
	private QuestionResultBm questionResultBm = null;

	private QuestionContainerDetail sessionSurveyUnderConstruction = null;
	private QuestionContainerDetail sessionSurvey = null;
	private Vector sessionQuestions = null;
	private Hashtable sessionResponses = null;
	private String sessionSurveyId = null;
	private String sessionSurveyName = null;

	public final static int OPENED_SURVEYS_VIEW = 1;
	public final static int CLOSED_SURVEYS_VIEW = 2;
	public final static int INWAIT_SURVEYS_VIEW = 3;

	private int viewType = OPENED_SURVEYS_VIEW;

	private boolean pollingStationMode = false;
	private boolean participationMultipleAllowedForUser = false;
	private boolean hasAlreadyParticipated = false;
	public static String COOKIE_NAME = "surpoll"; 
	
	
	/** Creates new sessionClientController */
	public SurveySessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		super(mainSessionCtrl, componentContext, "com.stratelia.webactiv.survey.multilang.surveyBundle");
		setQuestionContainerBm();
		setQuestionResultBm();

	}

	private void setQuestionContainerBm()
	{
		if (questionContainerBm == null)
		{
			try
			{
				QuestionContainerBmHome questionContainerBmHome =
					(QuestionContainerBmHome) EJBUtilitaire.getEJBObjectRef(
						JNDINames.QUESTIONCONTAINERBM_EJBHOME,
						QuestionContainerBmHome.class);
				this.questionContainerBm = questionContainerBmHome.create();
			}
			catch (Exception e)
			{
				throw new EJBException(e.getMessage());
			}
		}
	}

	public QuestionContainerBm getQuestionContainerBm()
	{
		return questionContainerBm;
	}
	
	private void setQuestionResultBm()
	{
		if (questionResultBm == null)
		{
			try
			{
				QuestionResultBmHome questionResultBmHome =
					(QuestionResultBmHome) EJBUtilitaire.getEJBObjectRef(
						JNDINames.QUESTIONRESULTBM_EJBHOME,
						QuestionResultBmHome.class);
				this.questionResultBm = questionResultBmHome.create();
			}
			catch (Exception e)
			{
				throw new EJBException(e.getMessage());
			}
		}
	}

	public QuestionResultBm getQuestionResultBm()
	{
		return questionResultBm;
	}

	public boolean isPdcUsed()
	{
		String value = getComponentParameterValue("usePdc");
		if (value != null)
			return "yes".equals(value.toLowerCase());
		return false;
	}

	/**
	 * Return if user can participate more than one time
	 * @return true or false
	 */
	public boolean isParticipationMultipleAllowedForUser()
	{
		return participationMultipleAllowedForUser;
	}
	
	/**
	 * Set user status to know if he can participate more than one time
	 * @param state
	 */
	public void setParticipationMultipleAllowedForUser(boolean state)
	{
		this.participationMultipleAllowedForUser = state;
	}

	/**
	 * Return if participationMultiple is used
	 * @return true or false
	 */
	public boolean isParticipationMultipleUsed()
	{
		boolean participationMultipleUsed = false;
		List userMultipleRole = new ArrayList();
		userMultipleRole.add("userMultiple");
		//if we have people on userMultiple role, multiple participation is used
		if (getOrganizationController().getUsersIdsByRoleNames(getComponentId(), userMultipleRole).length > 0)
			participationMultipleUsed = true;
		return participationMultipleUsed;
	}


	/**
	 * Return if anonymous mode is authorized
	 * @return
	 */
	public boolean isAnonymousModeAuthorized()
	{
		return isAnonymousModeEnabled() && userIsAnonymous();
	}
	
	/**
	 * Return if anonymous mode is enabled
	 * @return
	 */
	private boolean isAnonymousModeEnabled()
	{
		String value = getComponentParameterValue("useAnonymousMode");
		if (value != null)
			return "yes".equals(value.toLowerCase());
		return false;
	}

	/**
	 * Return if anonymous user has already participated with this ip
	 * @return
	 */
	public boolean hasAlreadyParticipated()
	{
		return hasAlreadyParticipated;
	}

	/**
	 * Set status of anonymous user (if he has already participated with this ip)
	 * @return
	 */
	public void hasAlreadyParticipated(boolean state)
	{
		this.hasAlreadyParticipated = state;
	}


	/**
	 * Return if currentUser is anonymous
	 * @return true or false
	 */
	private boolean userIsAnonymous()
	{
		boolean userIsAnonymous = false;

		String monLook;
		try {
			monLook = new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.lookSettings", "").getString("Initial");
		} catch (Exception e) {
			//No specific look is defined, get the default one
			monLook = new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.defaultLookSettings", "").getString("Initial");
		}
		
		ResourceLocator 		settings		 	= new ResourceLocator(monLook, "");
		String 					guestId 			= settings.getString("guestId");
		
		if (guestId != null && getUserId().equals(guestId))
		{
			if (getComponentId() != null)
			{
				if (getOrganizationController().isComponentAvailable(getComponentId(), guestId))
					userIsAnonymous = true;
			}
		}
		return userIsAnonymous;
	}

	public void setViewType(int viewType)
	{
		this.viewType = viewType;
	}

	public int getViewType()
	{
		return viewType;
	}

	public Collection getSurveys() throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.getSurveys", "Survey.MSG_ENTRY_METHOD");
		if (getViewType() == OPENED_SURVEYS_VIEW)
			return getOpenedSurveys();
		if (getViewType() == CLOSED_SURVEYS_VIEW)
			return getClosedSurveys();
		if (getViewType() == INWAIT_SURVEYS_VIEW)
			return getInWaitSurveys();
		return null;
	}

	public Collection getOpenedSurveys() throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.getOpenedSurveys", "Survey.MSG_ENTRY_METHOD");
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
			return questionContainerBm.getOpenedQuestionContainers(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.getOpenedSurveys",
				SurveyException.WARNING,
				"Survey.EX_NO_OPENED_SURVEY",
				e);
		}
	}

	public Collection getClosedSurveys() throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.getClosedSurveys", "Survey.MSG_ENTRY_METHOD");
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
			return questionContainerBm.getClosedQuestionContainers(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.getClosedSurveys",
				SurveyException.WARNING,
				"Survey.EX_NO_CLOSED_SURVEY",
				e);
		}
	}

	public Collection getInWaitSurveys() throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.getInWaitSurveys", "Survey.MSG_ENTRY_METHOD");
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
			return questionContainerBm.getInWaitQuestionContainers(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.getInWaitSurveys",
				SurveyException.WARNING,
				"Survey.EX_NO_WAIT_SURVEY",
				e);
		}
	}

	public QuestionContainerDetail getSurvey(String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.getSurvey", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			QuestionContainerDetail qc = questionContainerBm.getQuestionContainer(qcPK, getUserId());
			qc.getHeader().setNbRegistered(getNbRegistered());
			return qc;
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.getSurvey",
				SurveyException.WARNING,
				"Survey.EX_NO_SURVEY",
				"id = " + surveyId,
				e);
		}
	}

	public QuestionContainerPK createSurvey(QuestionContainerDetail surveyDetail) throws SurveyException
	{
		SilverTrace.info(
			"Survey",
			"SurveySessionClientController.createSurvey",
			"Survey.MSG_ENTRY_METHOD",
			"title = " + surveyDetail.getHeader().getTitle());
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
			return questionContainerBm.createQuestionContainer(qcPK, surveyDetail, getUserId());
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.createSurvey",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_CREATE",
				"title = " + surveyDetail.getHeader().getTitle(),
				e);
		}
	}

	public void updateSurveyHeader(QuestionContainerHeader surveyHeader, String surveyId) throws SurveyException
	{
		SilverTrace.info(
			"Survey",
			"SurveySessionClientController.updateSurveyHeader",
			"Survey.MSG_ENTRY_METHOD",
			"id = " + surveyId + ", title = " + surveyHeader.getTitle());
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			surveyHeader.setPK(qcPK);
			questionContainerBm.updateQuestionContainerHeader(surveyHeader);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.updateSurveyHeader",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_UPDATE_SURVEY",
				"id = " + surveyId + ", title = " + surveyHeader.getTitle(),
				e);
		}
	}

	public void updateQuestions(Collection questions, String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.updateQuestions", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.updateQuestions(qcPK, questions);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.updateQuestions",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_UPDATE_QUESTION",
				"id = " + surveyId,
				e);
		}
	}

	public void deleteSurvey(String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.deleteSurvey", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.deleteQuestionContainer(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.deleteSurvey",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_DELETE_SURVEY",
				"id = " + surveyId,
				e);
		}
	}
	
	public void deleteVotes(String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.deleteVotes", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.deleteVotes(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.deleteSurvey",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_DELETE_SURVEY",
				"id = " + surveyId,
				e);
		}
	}
	
	public void deleteResponse(String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.deleteQuestions", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.deleteQuestionContainer(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.deleteSurvey",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_DELETE_SURVEY",
				"id = " + surveyId,
				e);
		}
	}
	
	public Collection<String> getUserByQuestion(ForeignPK questionPK) throws RemoteException
	{
		return getUserByQuestion(questionPK,true);
	}
	public Collection<String> getUserByQuestion(ForeignPK questionPK, boolean withName) throws RemoteException
	{
		Collection<QuestionResult> results = getQuestionResultBm().getQuestionResultToQuestion(questionPK);
		Collection<String> users = new LinkedHashSet<String>();
		Iterator<QuestionResult> it = results.iterator();
		while (it.hasNext())
		{
			QuestionResult result = (QuestionResult) it.next();
			
			if (result != null)
			{
				String userName = "";
				if (withName)
				{
					userName = getUserDetail(result.getUserId()).getDisplayedName();
					users.add(result.getUserId()+ "-" + userName );
				}
				else
				{
					users.add(result.getUserId());
				}
			}
		}
		return users;
	}
	
	public Collection<QuestionResult> getResultByUser(String userId, ForeignPK questionPK) throws RemoteException
	{
		return getQuestionResultBm().getUserQuestionResultsToQuestion(userId, questionPK);
	}
	
	public Collection<String> getResultByUser(String userId) throws RemoteException
	{
		Collection<QuestionResult> result = new ArrayList<QuestionResult>();
		Collection<String> resultId = new ArrayList<String>();
		QuestionContainerDetail survey = getSessionSurvey();
		Collection questions = survey.getQuestions();
		Iterator it = questions.iterator();
		while (it.hasNext())
		{
			Question question = (Question) it.next();
			Collection<QuestionResult> questionResult = getQuestionResultBm().getUserQuestionResultsToQuestion(userId, new ForeignPK(question.getPK()));
			result.addAll(questionResult);
		}
		// ne récupérer que les id des réponses
		Iterator itR = result.iterator();
		while (itR.hasNext())
		{
			QuestionResult question = (QuestionResult) itR.next();
			resultId.add(question.getAnswerPK().getId());
		}
		return resultId;
	}
	
	public Collection<String> getUserByAnswer(String answerId) throws RemoteException
	{
		return getQuestionResultBm().getUsersByAnswer(answerId);
	}

	public void closeSurvey(String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.closeSurvey", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.closeQuestionContainer(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.closeSurvey",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_CLOSE_SURVEY",
				"id = " + surveyId,
				e);
		}
	}

	public void openSurvey(String surveyId) throws SurveyException
	{
		SilverTrace.info("Survey", "SurveySessionClientController.openSurvey", "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.openQuestionContainer(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.openSurvey",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_OPEN_SURVEY",
				"id = " + surveyId,
				e);
		}
	}

	public void recordReply(String surveyId, Hashtable reply) throws SurveyException
	{
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply);
		}
		catch (Exception e)
		{
			throw new SurveyException("SurveySessionClientController.recordReply", SurveyException.WARNING,
				"Survey.EX_RECORD_REPLY_FAILED", "id = " + surveyId, e);
		}
	}
	
	public void recordReply(String surveyId, Hashtable reply, String comment, boolean isAnonymousComment) throws SurveyException
	{
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			questionContainerBm.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply, comment, isAnonymousComment);
		}
		catch (Exception e)
		{
			throw new SurveyException("SurveySessionClientController.recordReply", SurveyException.WARNING,
				"Survey.EX_RECORD_REPLY_FAILED", "id = " + surveyId, e);
		}
	}

	public Collection getSuggestions(String surveyId) throws SurveyException
	{
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
			return questionContainerBm.getSuggestions(qcPK);
		}
		catch (Exception e)
		{
			throw new SurveyException(
				"SurveySessionClientController.getSuggestions",
				SurveyException.WARNING,
				"Survey.EX_PROBLEM_TO_RETURN_SUGGESTION",
				"id = " + surveyId,
				e);
		}
	}
	
	public Collection<String> getUsersByAnswer(String answerId) throws RemoteException
	{
		return getUserByAnswer(answerId);
	}
	
	public Collection<String> getUsersBySurvey(String surveyId) throws RemoteException, SurveyException
	{
		Collection<String> users = new LinkedHashSet<String>();
		QuestionContainerDetail survey = getSurvey(surveyId);
		Collection<Question> questions = survey.getQuestions();
		Iterator<Question> it = questions.iterator();
		while (it.hasNext())
		{
			Question question = (Question) it.next();
			ForeignPK questionPK = new ForeignPK(question.getPK());
			users.addAll(getUserByQuestion(questionPK, false));
		}
		return users;
	}

	public UserDetail getUserDetail(String userId)
	{
		return getOrganizationController().getUserDetail(userId);
	}

	private int getNbRegistered()
	{
		ComponentInst component = getOrganizationController().getComponentInst(getComponentId());
		if (component.isPublic())
		{
			String[] allUserIds = getOrganizationController().getAllUsersIds();
			return allUserIds.length;
		}
		else
		{
			UserDetail[] registered = getOrganizationController().getAllUsers(getComponentId());
			return registered.length;
		}
	}

	public int getSilverObjectId(String objectId)
	{
		int silverObjectId = -1;
		try
		{
			QuestionContainerPK qcPK = new QuestionContainerPK(objectId, getSpaceId(), getComponentId());
			silverObjectId = questionContainerBm.getSilverObjectId(qcPK);
		}
		catch (Exception e)
		{
			SilverTrace.error(
				"survey",
				"SurveySessionClientController.getSilverObjectId()",
				"root.EX_CANT_GET_LANGUAGE_RESOURCE",
				"objectId=" + objectId,
				e);
		}
		return silverObjectId;
	}

	/**************** Session Objects *****************/

	public QuestionContainerDetail getSessionSurveyUnderConstruction()
	{
		return this.sessionSurveyUnderConstruction;
	}

	public void setSessionSurveyUnderConstruction(QuestionContainerDetail surveyUnderConstruction)
	{
		this.sessionSurveyUnderConstruction = surveyUnderConstruction;
	}

	public void removeSessionSurveyUnderConstruction()
	{
		setSessionSurveyUnderConstruction(null);
	}

	public QuestionContainerDetail getSessionSurvey()
	{
		return this.sessionSurvey;
	}

	public void setSessionSurvey(QuestionContainerDetail survey)
	{
		this.sessionSurvey = survey;
	}

	public void removeSessionSurvey()
	{
		setSessionSurvey(null);
	}

	public Vector getSessionQuestions()
	{
		return this.sessionQuestions;
	}

	public void setSessionQuestions(Vector questions)
	{
		this.sessionQuestions = questions;
	}

	public void removeSessionQuestions()
	{
		setSessionQuestions(null);
	}

	public Hashtable getSessionResponses()
	{
		return this.sessionResponses;
	}

	public void setSessionResponses(Hashtable responses)
	{
		this.sessionResponses = responses;
	}

	public void removeSessionResponses()
	{
		setSessionResponses(null);
	}

	public String getSessionSurveyId()
	{
		return this.sessionSurveyId;
	}

	public void setSessionSurveyId(String surveyId)
	{
		this.sessionSurveyId = surveyId;
	}

	public void removeSessionSurveyId()
	{
		setSessionSurveyId(null);
	}

	public String getSessionSurveyName()
	{
		return this.sessionSurveyName;
	}

	public void setSessionSurveyName(String surveyName)
	{
		this.sessionSurveyName = surveyName;
	}

	public void removeSessionSurveyName()
	{
		setSessionSurveyName(null);
	}
	public void close()
	{
		try
		{
			if (questionContainerBm != null)
				questionContainerBm.remove();
		}
		catch (RemoteException e)
		{
			SilverTrace.error("surveySession", "SurveySessionController.close", "", e);
		}
		catch (RemoveException e)
		{
			SilverTrace.error("surveySession", "SurveySessionController.close", "", e);
		}
	}
	
	// pour la notification
	public String initAlertUser(String surveyId) throws RemoteException, SurveyException {
        AlertUser sel = getAlertUser();
		// Initialisation de AlertUser
        sel.resetAll();
		sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
		sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra param permettant de filtrer les users ayant acces au composant)
		PairObject hostComponentName = new PairObject(getComponentLabel(),  null); // set nom du composant pour browsebar (PairObject(nom_composant, lien_vers_composant)) NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours présenté en popup => pas de lien sur nom du composant)
		sel.setHostComponentName(hostComponentName);
		SilverTrace.debug("Survey","SurveySessionController.initAlertUser()","root.MSG_GEN_PARAM_VALUE","name = "+hostComponentName+" componentId="+ getComponentId());
		sel.setNotificationMetaData(getAlertNotificationMetaData(surveyId)); // set NotificationMetaData contenant les informations à notifier
		// fin initialisation de AlertUser
		// l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
		
        return AlertUser.getAlertUserURL(); 
    }
	
	private synchronized NotificationMetaData getAlertNotificationMetaData(String surveyId) throws RemoteException, SurveyException  
	{
		QuestionContainerPK pk = new QuestionContainerPK(surveyId);
		String senderName = getUserDetail().getDisplayedName();
		QuestionContainerDetail questionDetail = getSurvey(surveyId);
		SilverTrace.debug("Survey","SurveySessionController.getAlertNotificationMetaData()","root.MSG_GEN_PARAM_VALUE","survey = "+questionDetail.toString());
      	String htmlPath = getQuestionContainerBm().getHTMLQuestionPath(questionDetail);
      	
      	ResourceLocator	message		= new ResourceLocator("com.stratelia.webactiv.survey.multilang.surveyBundle", "fr");
		ResourceLocator	message_en	= new ResourceLocator("com.stratelia.webactiv.survey.multilang.surveyBundle", "en");
      	
      	String subject = getNotificationSubject(message);
      	String body = getNotificationBody(questionDetail, htmlPath, message, senderName);
		SilverTrace.debug("Survey","SurveySessionController.getAlertNotificationMetaData()","root.MSG_GEN_PARAM_VALUE","message = "+message.toString()+" message_en = "+ message_en.toString());
		SilverTrace.debug("Survey","SurveySessionController.getAlertNotificationMetaData()","root.MSG_GEN_PARAM_VALUE","sujet = "+subject+" corps = "+ body);

      	//english notifications
      	String subject_en = getNotificationSubject(message_en);
      	String body_en = getNotificationBody(questionDetail, htmlPath, message_en, senderName);
		SilverTrace.debug("Survey","SurveySessionController.getAlertNotificationMetaData()","root.MSG_GEN_PARAM_VALUE","sujet_en = "+subject_en+" corps_en = "+ body_en);

		NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, subject, body);
		notifMetaData.addLanguage("en", subject_en, body_en);
		
		notifMetaData.setLink(getSurveyUrl(questionDetail));
		notifMetaData.setComponentId(pk.getInstanceId());
		notifMetaData.setSender(getUserId());
		
		return notifMetaData;
    }
	
	private String getNotificationSubject(ResourceLocator message)
    {
    	return message.getString("survey.notifSubject");
    }
	
	private String getNotificationBody(QuestionContainerDetail questionDetail, String htmlPath, ResourceLocator message, String senderName)
    {
    	StringBuffer messageText = new StringBuffer();
	    messageText.append(senderName).append(" ");
	    messageText.append(message.getString("survey.notifInfo")).append("\n\n");
	    messageText.append(message.getString("survey.notifName")).append(" : ").append(questionDetail.getHeader().getName()).append("\n");
	    messageText.append(message.getString("survey.notifDesc")).append(" : ").append(questionDetail.getHeader().getDescription()).append("\n");
	    messageText.append(message.getString("survey.path")).append(" : ").append(htmlPath);
	    
	    return messageText.toString();
    }
	
	private String getSurveyUrl(QuestionContainerDetail questionDetail)
	{
		return URLManager.getURL(null, getComponentId())+questionDetail.getHeader().getURL();
	}

	public boolean isPollingStationMode()
	{
		return pollingStationMode;
	}

	public void setPollingStationMode(boolean pollingStationMode)
	{
		this.pollingStationMode = pollingStationMode;
	}
	
	public List getGalleries()
	{
		List galleries = null;
    	OrganizationController orgaController = new OrganizationController();
    	String[] compoIds = orgaController.getCompoId("gallery");
    	for (int c=0; c<compoIds.length; c++)
    	{    		
    		if ("yes".equalsIgnoreCase(orgaController.getComponentParameterValue("gallery"+compoIds[c], "viewInWysiwyg")))
    		{
    			if (galleries == null)
        			galleries = new ArrayList();
    			
    			ComponentInstLight gallery = orgaController.getComponentInstLight("gallery"+compoIds[c]);
    			galleries.add(gallery);
    		}
    	}
    	return galleries;
	}
}