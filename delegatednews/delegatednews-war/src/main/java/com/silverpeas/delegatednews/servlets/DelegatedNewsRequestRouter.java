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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.delegatednews.servlets;

import com.silverpeas.delegatednews.control.DelegatedNewsSessionController;
import com.silverpeas.delegatednews.model.DelegatedNews;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DateUtil;
import org.silverpeas.servlet.HttpRequest;

import java.util.Date;
import java.util.List;

public class DelegatedNewsRequestRouter extends ComponentRequestRouter<DelegatedNewsSessionController> {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "DelegatedNews";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public DelegatedNewsSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new DelegatedNewsSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param newsSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, DelegatedNewsSessionController newsSC,
      HttpRequest request) {

    SilverTrace.info("delegatednews", "DelegatedNewsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + newsSC.getUserId() + " Function=" + function);

    String destination = "";
    try {
      if ("Main".equals(function)) {
        List<DelegatedNews> list = newsSC.getAllAvailDelegatedNews();        
        request.setAttribute("ListNews", list);
        String listJSON = newsSC.getListDelegatedNewsJSON(list);
        request.setAttribute("ListNewsJSON", listJSON);
    	  destination = "/delegatednews/jsp/listNews.jsp";
      } 
      else if ("OpenPublication".equals(function)) {
        String pubId = request.getParameter("PubId");
        String instanceId = request.getParameter("InstanceId");
        destination = URLManager.getURL(null, instanceId)+"ViewOnly?Id="+pubId;
      } 
      else if ("ValidateDelegatedNews".equals(function)) {
        String pubId = request.getParameter("PubId");
        newsSC.validateDelegatedNews(Integer.parseInt(pubId));
        List<DelegatedNews> list = newsSC.getAllAvailDelegatedNews();        
        request.setAttribute("ListNews", list);
        String listJSON = newsSC.getListDelegatedNewsJSON(list);
        request.setAttribute("ListNewsJSON", listJSON);
        destination = "/delegatednews/jsp/listNews.jsp";
      } 
      else if (function.equals("EditRefuseReason")) {
        String pubId = request.getParameter("PubId");        
        request.setAttribute("PubId", pubId);
        destination = "/delegatednews/jsp/editRefuseReason.jsp";
      } 
      else if ("RefuseDelegatedNews".equals(function)) {
        String pubId = request.getParameter("PubId");
        String refuseReasonText = request.getParameter("RefuseReasonText");
        newsSC.refuseDelegatedNews(Integer.parseInt(pubId), refuseReasonText);
        List<DelegatedNews> list = newsSC.getAllAvailDelegatedNews();        
        request.setAttribute("ListNews", list);
        String listJSON = newsSC.getListDelegatedNewsJSON(list);
        request.setAttribute("ListNewsJSON", listJSON);
        destination = "/delegatednews/jsp/listNews.jsp";
      } 
      else if ("EditUpdateDate".equals(function)) {
        String pubId = request.getParameter("PubId");
        String beginDate = request.getParameter("BeginDate");
        String beginHour = request.getParameter("BeginHour");
        String endDate = request.getParameter("EndDate");
        String endHour = request.getParameter("EndHour");
        request.setAttribute("PubId", pubId);
        request.setAttribute("BeginDate", beginDate);
        request.setAttribute("BeginHour", beginHour);
        request.setAttribute("EndDate", endDate);
        request.setAttribute("EndHour", endHour);
        destination = "/delegatednews/jsp/editUpdateDate.jsp";
      } 
      else if ("UpdateDateDelegatedNews".equals(function)) {
        String pubId = request.getParameter("PubId");
        String beginDate = request.getParameter("BeginDate");
        String beginHour = request.getParameter("BeginHour");
        String endDate = request.getParameter("EndDate");
        String endHour = request.getParameter("EndHour");
        Date jBeginDate = null;
        Date jEndDate = null;

        if (StringUtil.isDefined(beginDate)) {
          jBeginDate = DateUtil.stringToDate(beginDate, beginHour, newsSC.getLanguage());
        }
        if (StringUtil.isDefined(endDate)) {
          jEndDate = DateUtil.stringToDate(endDate, endHour, newsSC.getLanguage());
        }
        
        newsSC.updateDateDelegatedNews(Integer.parseInt(pubId), jBeginDate, jEndDate);
        List<DelegatedNews> list = newsSC.getAllAvailDelegatedNews();        
        request.setAttribute("ListNews", list);
        String listJSON = newsSC.getListDelegatedNewsJSON(list);
        request.setAttribute("ListNewsJSON", listJSON);
        destination = "/delegatednews/jsp/listNews.jsp";
      } 
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("delegatednews", "DelegatedNewsRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

}
