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
import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.date.Period;
import org.silverpeas.servlet.FileUploadUtil;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.wysiwyg.WysiwygException;

import com.silverpeas.thumbnail.control.ThumbnailController;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.quickinfo.control.QuickInfoSessionController;
import org.silverpeas.util.DateUtil;
import com.stratelia.webactiv.publication.model.PublicationDetail;

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
    SilverpeasRole flag = quickInfo.getHighestSilverpeasUserRole();
    if (flag == null) {
      return null;
    }
    request.setAttribute("Role", flag.toString());
    request.setAttribute("AppSettings", quickInfo.getInstanceSettings());

    try {
      if ("Main".equals(function)) {
        Collection<News> infos;
        if (isContributor(flag)) {
          NewsByStatus allNews = quickInfo.getQuickInfos();
          request.setAttribute("NotVisibleNews", allNews);
          infos = allNews.getVisibles();
        } else {
          infos = quickInfo.getVisibleQuickInfos();
        }
        request.setAttribute("ListOfNews", infos);
        request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
        request.setAttribute("IsSubscriberUser", quickInfo.isSubscriberUser());
        destination = "/quickinfo/jsp/home.jsp";
      } else if (function.startsWith("portlet")) {
        List<News> infos = quickInfo.getVisibleQuickInfos();
        request.setAttribute("infos", infos);
        request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
        destination = "/portlets/jsp/quickInfos/portlet.jsp";
      } else if ("Save".equals(function)) {
        String id = saveQuickInfo(quickInfo, request, false);
        request.setAttribute("Id", id);
        destination = getDestination("View", quickInfo, request);
      } else if ("Publish".equals(function)) {
        String id = request.getParameter("Id");
        quickInfo.publish(id);
        request.setAttribute("Id", id);
        destination = getDestination("View", quickInfo, request);
      } else if ("SaveAndPublish".equals(function)) {
        String id = saveQuickInfo(quickInfo, request, true);
        request.setAttribute("Id", id);
        destination = getDestination("View", quickInfo, request);
      } else if ("View".equals(function)) {
        News news = (News) request.getAttribute("News");
        if (news == null) {
          String id = request.getParameter("Id");
          if (!StringUtil.isDefined(id)) {
            id = (String) request.getAttribute("Id");
          }
          request.setAttribute("News", quickInfo.getNews(id, true));
        }
        String anchor = request.getParameter("Anchor");
        request.setAttribute("AppSettings", quickInfo.getInstanceSettings());
        destination = "/quickinfo/jsp/news.jsp";
        if (StringUtil.isDefined(anchor)) {
          destination += "#"+anchor;
        }
      } else if ("ViewOnly".equals(function)) {
        String id = request.getParameter("Id");
        News news = quickInfo.getNewsByForeignId(id);
        request.setAttribute("News", news);
        request.setAttribute("ViewOnly", true);
        destination = getDestination("View", quickInfo, request);
      } else if ("Add".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        News news = quickInfo.createEmptyNews();
        request.setAttribute("NewOneInProgress", true);
        setCommonAttributesToAddOrUpdate(quickInfo, news, request);
        destination = "/quickinfo/jsp/quickInfoEdit.jsp";
      } else if ("Edit".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        String id = request.getParameter("Id");
        News news = quickInfo.getNews(id, false);
        setCommonAttributesToAddOrUpdate(quickInfo, news, request);
        destination = "/quickinfo/jsp/quickInfoEdit.jsp";
      } else if ("Remove".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        String id = request.getParameter("Id");
        quickInfo.remove(id);
        destination = getDestination("Main", quickInfo, request);
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        News news = null;
        if (StringUtil.isInteger(id)) {
          // from a search result
          news = quickInfo.getNewsByForeignId(id);
        } else {
          // from a comment
          news = quickInfo.getNews(id, true);
        }
        if (news.isDraft() && !isContributor(flag)) {
          throwHttpForbiddenError();
        }
        request.setAttribute("News", news);
        destination = getDestination("View", quickInfo, request);
      } else if ("Notify".equals(function)) {
        String id = request.getParameter("Id");
        destination = quickInfo.notify(id);
      } else if ("SubmitOnHomepage".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        String id = request.getParameter("Id");
        quickInfo.submitNewsOnHomepage(id);
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

  private boolean isContributor(SilverpeasRole role) {
    return role.isGreaterThanOrEquals(SilverpeasRole.publisher);
  }

  private void setCommonAttributesToAddOrUpdate(QuickInfoSessionController quickInfo, News news, HttpRequest request) {
    request.setAttribute("info", news);
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
  private String saveQuickInfo(QuickInfoSessionController quickInfo,
      HttpRequest request, boolean publish) throws Exception {

    List<FileItem> items = request.getFileItems();
    News news = requestToNews(items, quickInfo.getLanguage());

    String positions = request.getParameter("Positions");

    String id = news.getId();

    // process thumbnail first to be stored in index when publication is updated
    ThumbnailController.processThumbnail(
        new ForeignPK(news.getPublicationId(), quickInfo.getComponentId()),
        PublicationDetail.getResourceType(), items);

    quickInfo.update(id, news, positions, publish);

    return id;
  }

  private News requestToNews(List<FileItem> items, String language) throws ParseException {

    String id = FileUploadUtil.getParameter(items, "Id");
    String name = FileUploadUtil.getParameter(items, "Name");
    String description = FileUploadUtil.getParameter(items, "Description");
    String content = FileUploadUtil.getParameter(items, "Content");
    String pubId = FileUploadUtil.getParameter(items, "PubId");
    boolean important =
        StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "BroadcastImportant"));
    boolean ticker =
        StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "BroadcastTicker"));
    boolean mandatory =
        StringUtil.getBooleanValue(FileUploadUtil.getParameter(items, "BroadcastMandatory"));

    Date beginDate = null;
    String beginString = FileUploadUtil.getParameter(items, "BeginDate");
    if (StringUtil.isDefined(beginString)) {
      String hour = FileUploadUtil.getParameter(items, "BeginHour");
      beginDate = DateUtil.stringToDate(beginString, hour, language);
    }

    Date endDate = null;
    String endString = FileUploadUtil.getParameter(items, "EndDate");
    if (StringUtil.isDefined(endString)) {
      String hour = FileUploadUtil.getParameter(items, "EndHour");
      endDate = DateUtil.stringToDate(endString, hour, language);
    }

    News news =
        new News(name, description, getPeriod(beginDate, endDate), important, ticker, mandatory);
    news.setId(id);
    news.setContent(content);
    if (StringUtil.isDefined(pubId)) {
      news.setPublicationId(pubId);
    }

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
      if ("admin".equals(profiles[i])) {
        return profiles[i];
      }
      if ("publisher".equals(profiles[i])) {
        flag = profiles[i];
      }
    }
    return flag;
  }
}