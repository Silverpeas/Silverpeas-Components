package com.silverpeas.processManager.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.processManager.ProcessManagerException;
import com.silverpeas.processManager.ProcessManagerSessionController;

/**
 * A functio handler is associated to a peas function
 * and is called by the request router when this function
 * has to be processed.
 */
public interface FunctionHandler
{
	 /**
	  * Process the request and returns the response url.
	  *
	  * @param function  the user request name
	  * @param request   the user request params
	  * @param session   the user request context
	  */
    public String getDestination(String function,
	                              ProcessManagerSessionController session,
											HttpServletRequest request)
       throws ProcessManagerException;
}
