package com.silverpeas.resourcesmanager.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.resourcesmanager.control.ResourcesManagerSessionController;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;


public class AjaxResourcesManagerServlet extends HttpServlet {

	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{
		HttpSession session = req.getSession(true);
		
		String componentId 	= (String) req.getParameter("ComponentId");
		
		ResourcesManagerSessionController resourcesManagerSC = (ResourcesManagerSessionController) session.getAttribute("Silverpeas_" + "ResourcesManager" + "_" + componentId);
				
		if (resourcesManagerSC != null)
		{	
			try {
				String elementId = req.getParameter("ElementId");
				String reservationId = req.getParameter("reservationId");
				SilverTrace.info("resourcesManager", "AjaxResourcesManagerServlet", "root.MSG_GEN_PARAM_VALUE", "reservationId="+reservationId);
				String beginDate = req.getParameter("beginDate");
				String beginHour = req.getParameter("beginHour");
				String endDate = req.getParameter("endDate");
				String endHour = req.getParameter("endHour");
			
				List listResourceEverReserved = resourcesManagerSC.getResourcesofReservation(reservationId);
				String listResource = "";
				for (int i=0; i<listResourceEverReserved.size(); i++){
					ResourceDetail myResource = (ResourceDetail)listResourceEverReserved.get(i);
					String idResource = myResource.getId();
					if(i==0){
						listResource = idResource;
					}
					else {
						listResource = listResource+","+idResource;
					}
				}
				Date dateDebut = DateUtil.stringToDate(beginDate, beginHour, resourcesManagerSC.getLanguage());
				Date dateFin = DateUtil.stringToDate(endDate, endHour, resourcesManagerSC.getLanguage()); 
				List listResources = resourcesManagerSC.getResourcesProblemDate(listResource,dateDebut,dateFin,reservationId);
				String listResourceName="";
				for (int i=0; i<listResources.size(); i++){
					ResourceDetail myResource = (ResourceDetail)listResources.get(i);
					String resourceName = myResource.getName();
					if(i==0){
						listResourceName = resourceName;
					}
					else {
						listResourceName = listResourceName+","+resourceName;
					}
				}
				
				SilverTrace.info("resourcesManager", "AjaxResourcesManagerServlet", "root.MSG_GEN_PARAM_VALUE", " avant concaténation listResourceName= "+listResourceName);
				if(StringUtil.isDefined(listResourceName))
					listResourceName = resourcesManagerSC.getString("resourcesManager.resourceUnReservable") + listResourceName;
				/*else
					listResourceName = "";*/
				
				SilverTrace.info("resourcesManager", "AjaxResourcesManagerServlet", "root.MSG_GEN_PARAM_VALUE", "listResourceName= "+listResourceName);
				res.setContentType("text/xml");
				res.setHeader("charset","UTF-8");
				
				Writer writer = res.getWriter();
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.write("<ajax-response>");
				writer.write("<response type=\"element\" id=\""+elementId+"\">");
				writer.write(EncodeHelper.escapeXml(listResourceName));
				writer.write("</response>");
				writer.write("</ajax-response>");
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
}