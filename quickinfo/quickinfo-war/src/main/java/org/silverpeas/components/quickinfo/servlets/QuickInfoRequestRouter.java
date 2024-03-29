/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.components.quickinfo.control.QuickInfoSessionController;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.util.viewgenerator.html.list.ListPaneTag;

import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.time.OffsetDateTime.ofInstant;

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
   * @param function The entering request function (ex : "Main.jsp")
   * @param quickInfo The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, QuickInfoSessionController quickInfo,
      HttpRequest request) {
    String destination;
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
          request.setAttribute("isNewsToPaste", quickInfo.isNewsToPaste());
          infos = allNews.getVisibles();
        } else {
          infos = quickInfo.getVisibleQuickInfos();
        }
        request.setAttribute("ListOfNews", infos);
        request.setAttribute("IsSubscriberUser", quickInfo.isSubscriberUser());
        destination = "/quickinfo/jsp/home.jsp";
      } else if (function.startsWith("portlet")) {
        List<News> infos = quickInfo.getVisibleQuickInfos();
        request.setAttribute("infos", infos);
        if (!"portletPagination".equals(function)) {
          final String listName = ListPaneTag.class.getSimpleName() + "listOfNewsFromPortlet";
          request.getSession(false).setAttribute(listName, null);
        }
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
          String id = (String) request.getAttribute("Id");
          if (!StringUtil.isDefined(id)) {
            id = request.getParameter("Id");
          }
          news = quickInfo.getNews(id, true);
          request.setAttribute("News", news);
        }
        if (!isContributor(flag) && (news.isDraft() || !news.isVisible())) {
          if (news.isDraft() || news.isNotYetVisible()) {
            request.setAttribute("ErrorMessage", "quickinfo.news.error.notyetvisible");
          } else {
            request.setAttribute("ErrorMessage", "quickinfo.news.error.nomorevisible");
          }
          destination = getDestination("Main", quickInfo, request);
        } else {
          request.setAttribute("Index", quickInfo.getIndex());
          String anchor = request.getParameter("Anchor");
          destination = "/quickinfo/jsp/news.jsp";
          if (StringUtil.isDefined(anchor)) {
            destination += "#" + anchor;
          }
        }
      } else if ("ViewOnly".equals(function)) {
        String id = request.getParameter("Id");
        News news = quickInfo.getNewsByForeignId(id);
        request.setAttribute("News", news);
        request.setAttribute("ViewOnly", true);
        destination = getDestination("View", quickInfo, request);
      } else if ("Previous".equals(function)) {
        request.setAttribute("News", quickInfo.getPrevious());
        destination = getDestination("View", quickInfo, request);
      } else if ("Next".equals(function)) {
        request.setAttribute("News", quickInfo.getNext());
        destination = getDestination("View", quickInfo, request);
      } else if ("copy".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        final String newsId = request.getParameter("Id");
        quickInfo.addNewsToBeCopied(newsId);
        destination =
            URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        quickInfo.paste();
        destination = getDestination("Main", quickInfo, request);
      } else if ("Add".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        News news = quickInfo.prepareEmptyNews();
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
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        News news;
        if (StringUtil.isInteger(id)) {
          // from a search result
          news = quickInfo.getNewsByForeignId(id);
        } else {
          // from a comment
          news = quickInfo.getNews(id, true);
        }
        request.setAttribute("News", news);
        destination = getDestination("View", quickInfo, request);
      } else if ("SubmitOnHomepage".equals(function)) {
        if (!isContributor(flag)) {
          throwHttpForbiddenError();
        }
        String id = request.getParameter("Id");
        quickInfo.submitNewsOnHomepage(id);
        destination = getDestination("View", quickInfo, request);
      } else if ("ManageSubscriptions".equals(function)) {
        destination = quickInfo.manageSubscriptions();
      } else {
        destination = "/quickinfo/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }
    return destination;
  }

  private boolean isContributor(SilverpeasRole role) {
    return role.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER);
  }

  private void setCommonAttributesToAddOrUpdate(QuickInfoSessionController quickInfo, News news,
      HttpRequest request) {
    request.setAttribute("info", news);
    request.setAttribute("ThumbnailSettings", quickInfo.getThumbnailSettings());
  }

  /**
   * This method retrieve all the request parameters before creating or updating a quick info
   * @param quickInfo the QuickInfoSessionController
   * @param request the HttpServletRequest
   * @param publish true if publish action, false otherwise.
   * @throws Exception
   */
  private String saveQuickInfo(QuickInfoSessionController quickInfo, HttpRequest request,
      boolean publish) throws ParseException, IOException {

    List<FileItem> items = request.getFileItems();
    News news = requestToNews(items, quickInfo.getLanguage());

    String positions = request.getParameter("Positions");

    if (quickInfo.isNewsIdentifierFromMemory(news.getId())) {
      quickInfo.create(news);
    }

    String id = news.getId();

    // process thumbnail first to be stored in index when publication is updated
    ThumbnailController
        .processThumbnail(new ResourceReference(news.getPublicationId(),
                quickInfo.getComponentId()), items);

    // process files
    Collection<UploadedFile> uploadedFiles = request.getUploadedFiles();

    quickInfo.update(id, news, positions, uploadedFiles, publish);

    return id;
  }

  private News requestToNews(List<FileItem> items, String language) throws ParseException {

    String id = FileUploadUtil.getParameter(items, "Id");
    String name = FileUploadUtil.getParameter(items, "Name");
    String description = FileUploadUtil.getParameter(items, "Description");
    String keywords = FileUploadUtil.getParameter(items, "Keywords");
    String content = FileUploadUtil.getParameter(items, "editorContent");
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

    News news = News.builder()
        .setTitleAndDescription(name, description)
        .setKeywords(keywords)
        .setVisibilityPeriod(getPeriod(beginDate, endDate))
        .setImportant(important)
        .setTicker(ticker)
        .setMandatory(mandatory)
        .build();
    news.setId(id);
    news.setContentToStore(content);
    if (StringUtil.isDefined(pubId)) {
      news.setPublicationId(pubId);
    }

    return news;
  }

  private Period getPeriod(Date begin, Date end) {
    return Period.betweenNullable(
        begin != null ? ofInstant(begin.toInstant(), ZoneId.systemDefault()) : null,
        end != null ? ofInstant(end.toInstant(), ZoneId.systemDefault()) : null
    );
  }
}
