package com.stratelia.webactiv.survey.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class RecordParticipation extends HttpServlet
{
	/**
	 * Method invoked when called from a form or directly by URL
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {

		//Cookie Validity
		int	cookieDuration	= 3650;
		if (request.getParameter("duration") != null)
			cookieDuration = new Integer(request.getParameter("duration")).intValue();

		String componentId 	= request.getParameter("cid");
		String surveyId		= request.getParameter("sid");

		//write cookie for this vote or survey
		Cookie cookieIp = new Cookie("surpoll"+surveyId, request.getRemoteAddr());
		cookieIp.setMaxAge(86400*cookieDuration); 
		cookieIp.setPath("/");
		response.addCookie(cookieIp);
		
        // Get the context
        String sRequestURL = request.getRequestURL().toString();
        String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

		SilverTrace.info("Survey", "RecordParticipation.doPost", "Survey.MSG_GEN_PARAM_VALUE", m_sAbsolute + URLManager.getApplicationURL() + URLManager.getURL(null, null, componentId) + "surveyDetail.jsp&action=ViewResult&SurveyId="+surveyId);
        response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + URLManager.getApplicationURL() + URLManager.getURL(null, null, componentId) + "surveyDetail.jsp?Action=ViewResult&SurveyId="+surveyId));
    }
	
	/**
	 * Method invoked when called from a form or directly by URL
	 */
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost (request, response);
    }
}