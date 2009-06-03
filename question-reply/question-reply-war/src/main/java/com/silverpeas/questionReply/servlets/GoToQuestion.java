package com.silverpeas.questionReply.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GoToQuestion extends GoTo
{
	public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		Question question = getQuestionManager().getQuestion(new Long(objectId).longValue());
		String componentId = question.getInstanceId();

		SilverTrace.info("questionReply", "GoToQuestion.doPost", "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

		String gotoURL = URLManager.getURL(null, componentId) + question._getURL();

		return "goto=" + URLEncoder.encode(gotoURL, "UTF-8"); 
	}

	private QuestionManager getQuestionManager()
	{
		QuestionManager questionManager = null;
		if (questionManager == null)
			questionManager  = QuestionManager.getInstance();
		return questionManager;
	}
}