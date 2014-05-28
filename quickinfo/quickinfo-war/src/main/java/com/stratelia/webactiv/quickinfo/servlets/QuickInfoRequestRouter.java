/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.quickinfo.servlets;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.CreateException;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.date.Period;
import org.silverpeas.servlet.FileUploadUtil;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.wysiwyg.WysiwygException;

import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.quickinfo.control.QuickInfoSessionController;
import com.stratelia.webactiv.util.DateUtil;

public class QuickInfoRequestRouter extends ComponentRequestRouter<QuickInfoSessionController> {

  private static final long serialVersionUID = 2256481728385587395L;

  @Override
  public String getSessionControlBeanName() {
    return "quickinfo";
  }

  @Override
  public QuickInfoSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new QuickInfoSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param quickInfo The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, QuickInfoSessionController quickInfo,
      HttpRequest request) {
    String destination = null;
    String flag = getFlag(quickInfo.getUserRoles());
    if (flag == null) {
      return null;
    }
    request.setAttribute("Role", flag);
    request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
    
    try {
      if ("Main".equals(function)) {
        Collection<News> infos;
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          infos = quickInfo.getQuickInfos();
        } else {
          infos = quickInfo.getVisibleQuickInfos();
        }
        request.setAttribute("ListOfNews", infos);
        request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
        destination = "/quickinfo/jsp/home.jsp";
      } else if (function.startsWith("portlet")) {
        List<News> infos = quickInfo.getVisibleQuickInfos();
        request.setAttribute("infos", infos);
        request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
        destination = "/quickinfo/jsp/portlet.jsp";
      } else if ("Save".equals(function)) {
        createOrUpdateQuickInfo(quickInfo, request);
        destination = getDestination("quickInfoPublisher", quickInfo, request);
      } else if ("View".equals(function)) {
        String id = request.getParameter("Id");
        String anchor = request.getParameter("Anchor");
        request.setAttribute("News", quickInfo.getDetail(id));
        request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
        destination = "/quickinfo/jsp/news.jsp";
      } else if ("Add".equals(function) || "Edit".equals(function)) {
        if (!"publisher".equals(flag) && !"admin".equals(flag)) {
          throwHttpForbiddenError();
        }
        setCommonAttributesToAddOrUpdate(quickInfo, request);
        destination = "/quickinfo/jsp/quickInfoEdit.jsp";
      } else if ("Remove".equals(function)) {
        if (!"publisher".equals(flag) && !"admin".equals(flag)) {
          throwHttpForbiddenError();
        }
        String id = request.getParameter("Id");
        quickInfo.remove(id);
        destination = getDestination("Main", quickInfo, request);
      } else if (function.startsWith("searchResult")) {
        destination = getDestination("View", quickInfo, request);
      } else {
        destination = "/quickinfo/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }
    SilverTrace.info("quickinfo", "QuickInfoRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "destination" + destination);
    return destination;
  }
  
  private void setCommonAttributesToAddOrUpdate(QuickInfoSessionController quickInfo, HttpRequest request) {
    String id = request.getParameter("Id");
    News quickInfoDetail = null;
    if (StringUtil.isDefined(id)) {
      quickInfoDetail = quickInfo.getDetail(id);
    }
    request.setAttribute("info", quickInfoDetail);
    request.setAttribute("ThumbnailSettings", quickInfo.getThumbnailSettings());
  }

  /**
   * This method retrieve all the request parameters before creating or updating a quick info
   *
   * @param quickInfo the QuickInfoSessionController
   * @param request the HttpServletRequest
   * @param action a string representation of an action
   * @throws Exception 
   * @throws RemoteException
   * @throws CreateException
   * @throws WysiwygException
   */
  private void createOrUpdateQuickInfo(QuickInfoSessionController quickInfo,
      HttpRequest request) throws Exception {

    List<FileItem> items = request.getFileItems();
    News news = requestToNews(items, quickInfo.getLanguage());

    String id = request.getParameter("Id");
    if (StringUtil.isDefined(id)) {
      quickInfo.update(id, news);
    } else {
      String positions = request.getParameter("Positions");
      id = quickInfo.add(news, positions);
    }

    ThumbnailController.processThumbnail(new ForeignPK(id, quickInfo.getComponentId()), "News",
        items);
  }
  
  private News requestToNews(List<FileItem> items, String language) throws ParseException {
    
    String name = FileUploadUtil.getParameter(items, "Name");
    String description = FileUploadUtil.getParameter(items, "Description");
    String content = FileUploadUtil.getParameter(items, "Content");
    
    Date beginDate = null;
    String beginString = FileUploadUtil.getParameter(items, "BeginDate");
    if (StringUtil.isDefined(beginString)) {
      beginDate = DateUtil.stringToDate(beginString, language);
    }

    Date endDate = null;
    String endString = FileUploadUtil.getParameter(items, "EndDate");
    if (StringUtil.isDefined(endString)) {
      endDate = DateUtil.stringToDate(endString, language);
    }
    
    List<String> broadcastModes = FileUploadUtil.getParameterValues(items, "BroadcastMode", "UTF-8");
    int[] modes = new int[broadcastModes.size()];
    for (int i=0; i<broadcastModes.size(); i++) {
      modes[i] = Integer.parseInt(broadcastModes.get(i));
    }
    
    News news =
      new News(name, description, getPeriod(beginDate, endDate), modes);
    news.setContent(content);
    
    return news;
  }
  
  private Period getPeriod(Date begin, Date end) {
    if (begin == null) {
      begin = DateUtil.MINIMUM_DATE;
    }
    if (end == null) {
      end = DateUtil.MAXIMUM_DATE;
    }
    return Period.from(begin, end);
  }

  private String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      }
    }
    return flag;
  }
}