package com.silverpeas.projectManager.servlets;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.projectManager.control.ProjectManagerSessionController;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;


public class AjaxProjectManagerServlet extends HttpServlet 
{
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{
        HttpSession session = req.getSession(true);
        String elementId 	= req.getParameter("ElementId");
		String componentId 	= req.getParameter("ComponentId");
		String action		= req.getParameter("Action");
	
		ProjectManagerSessionController projectManagerSC = (ProjectManagerSessionController) session.getAttribute("Silverpeas_" + "projectManager" + "_" + componentId);
		if (projectManagerSC!= null)
		{
			String output = "";
			
			if (action.equals("ProcessUserOccupation"))
			{
				// mise à jour de la charge en tenant compte de la modification des dates de début et fin
				String taskId 		= req.getParameter("TaskId");
				String userId		= req.getParameter("UserId");
				String userCharge	= req.getParameter("UserCharge");
				String sBeginDate 	= req.getParameter("BeginDate");
				String sEndDate 	= req.getParameter("EndDate");
				
				SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE", "userId = "+userId+", charge = "+userCharge);

				Date beginDate = null;
				try
				{
					beginDate = projectManagerSC.uiDate2Date(sBeginDate);
				}
				catch (ParseException e) {}
				
				Date endDate = null;
				try
				{
					endDate = projectManagerSC.uiDate2Date(sEndDate);
				}
				catch (ParseException e) {}
				
				SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE", "beginDate = "+beginDate+", endDate = "+endDate);
				
				int occupation = projectManagerSC.checkOccupation(taskId, userId, beginDate, endDate);
				
				occupation += Integer.parseInt(userCharge);
				
				if (occupation > 100)
					output = "<font color=\"red\">"+EncodeHelper.escapeXml(Integer.toString(occupation))+" %</font>";
				else
					output = "<font color=\"green\">"+EncodeHelper.escapeXml(Integer.toString(occupation))+" %</font>";
			}
			else if (action.equals("ProcessUserOccupationInit"))
			{
				// mise à jour de la charge en tenant compte de la modification des dates de début et fin
				String userId		= req.getParameter("UserId");
				String userCharge	= req.getParameter("UserCharge");
				String sBeginDate = req.getParameter("BeginDate");
				String sEndDate = req.getParameter("EndDate");
				
				Date beginDate = null;
				try
				{
					beginDate = projectManagerSC.uiDate2Date(sBeginDate);
				}
				catch (ParseException e)
				{
				}
				SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE", "userId = "+userId+", charge = "+userCharge);
				
				Date endDate = null;
				try
				{
					endDate = projectManagerSC.uiDate2Date(sEndDate);
				}
				catch (ParseException e)
				{
				}
				SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE", "beginDate = "+beginDate+", endDate = "+endDate);
				
				int occupation = projectManagerSC.checkOccupation(userId, beginDate, endDate);
				
				occupation += Integer.parseInt(userCharge);
				
				if (occupation > 100)
					output = "<font color=\"red\">"+EncodeHelper.escapeXml(Integer.toString(occupation))+" %</font>";
				else
					output = "<font color=\"green\">"+EncodeHelper.escapeXml(Integer.toString(occupation))+" %</font>";
			}
			else if (action.equals("ProcessEndDate"))
			{
				String taskId 		= req.getParameter("TaskId");
				String charge 		= req.getParameter("Charge");
				String sBeginDate 	= req.getParameter("BeginDate");
				
				Date beginDate = null;
				try
				{
					beginDate = projectManagerSC.uiDate2Date(sBeginDate);
				}
				catch (ParseException e)
				{
				}
				
				Date endDate = projectManagerSC.processEndDate(taskId, charge, beginDate);
				
				output = EncodeHelper.escapeXml(projectManagerSC.date2UIDate(endDate));
			}
			else if (action.equals("ProcessEndDateInit"))
			{
				String charge = req.getParameter("Charge");
				String sBeginDate = req.getParameter("BeginDate");
				
				Date beginDate = null;
				try
				{
					beginDate = projectManagerSC.uiDate2Date(sBeginDate);
				}
				catch (ParseException e)
				{
				}
				
				Date endDate = projectManagerSC.processEndDate(charge, beginDate, componentId);
				
				output = EncodeHelper.escapeXml(projectManagerSC.date2UIDate(endDate));
			}
			SilverTrace.info("projectManager", "AjaxProjectManagerServlet", "root.MSG_GEN_PARAM_VALUE", "output = "+output);	
			
			res.setContentType("text/xml");
			res.setHeader("charset","UTF-8");
			
			Writer writer = res.getWriter();
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.write("<ajax-response>");
			writer.write("<response type=\"element\" id=\""+elementId+"\">");
			writer.write(output);
			writer.write("</response>");
			writer.write("</ajax-response>");
        }
	}
}