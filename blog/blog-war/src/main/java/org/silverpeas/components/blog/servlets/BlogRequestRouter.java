/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.blog.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.blog.control.BlogSessionController;
import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.mvc.util.WysiwygRouting;
import org.silverpeas.core.web.util.viewgenerator.html.monthcalendar.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static org.silverpeas.core.contribution.model.CoreContributionType.COMPONENT_INSTANCE;

public class BlogRequestRouter extends ComponentRequestRouter<BlogSessionController> {

  private static final long serialVersionUID = 6711772954612207110L;
  private static final int HOMEPAGE_LIMIT = 10;
  private static final int SAFE_LIMIT = 10000;
  private static final String VIEW_POST_FCT = "ViewPost";
  private static final String POST_BY_ARCHIVE_FCT = "PostByArchive";
  private static final String POST_BY_CATEGORY_FCT = "PostByCategory";
  private static final String VIEW_CATEGORY_FCT = "ViewCategory";
  private static final String HOMEPAGE_VIEW = "accueil.jsp";
  private static final String POSTS_ATTR = "Posts";
  private static final String POST_ID = "PostId";
  private static final String BEGIN_DATE = "BeginDate";
  private static final String END_DATE = "EndDate";
  private static final String CATEGORY_ID = "CategoryId";
  private static final String DATE_CALENDAR = "DateCalendar";

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "Blog";
  }

  @Override
  public BlogSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new BlogSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param blogSC The component Session Control, build and initialised.
   * @param request the current request.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, BlogSessionController blogSC, HttpRequest request) {
    String destination = "";

    String rootDest = "/blog/jsp/";

    // paramètres généraux
    request.setAttribute("Profile", blogSC.getFlag());

    try {
      if (function.startsWith("Main")) {
        // récupération des derniers billets par date d'évènements
        Collection<PostDetail> posts = blogSC.lastPosts(HOMEPAGE_LIMIT);
        request.setAttribute(POSTS_ATTR, posts);

        // passage des paramètres communs
        setCommonParam(blogSC, request);

        // creation d'une liste d'event par rapport aux posts du mois
        setPostsByArchiveParam(blogSC, request);

        request.setAttribute("IsUserSubscribed", blogSC.isUserSubscribed());

        // appel de la page d'accueil
        destination = rootDest + HOMEPAGE_VIEW;
      } else if ("NewPost".equals(function)) {
        //save a new post untitled, in draft mode
        String title = blogSC.getString("blog.postUntitled");
        String positions = request.getParameter("Positions");
        String categoryId = "";
        Date dateEvent = new Date();
        String postId = blogSC.createPost(title, categoryId, dateEvent, positions);

        PostDetail post = blogSC.getPost(postId);
        request.setAttribute("Post", post);
        request.setAttribute("AllCategories", blogSC.getAllCategories());

        destination = rootDest + "postManager.jsp";
      } else if ("EditPost".equals(function)) {
        blogSC.checkWriteAccessOnBlogPost();
        String postId = request.getParameter(POST_ID);
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute(POST_ID);
        }
        PostDetail post = blogSC.getPost(postId);
        request.setAttribute("Post", post);
        request.setAttribute("AllCategories", blogSC.getAllCategories());
        request.setAttribute("Updater", blogSC.getUserDetail(post.getPublication().getUpdaterId()));
        // appel de la page de modification
        destination = rootDest + "postManager.jsp";
      } else if ("EditPostContent".equals(function)) {
        blogSC.checkWriteAccessOnBlogPost();
        String postId = request.getParameter(POST_ID);
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute(POST_ID);
        }
        PostDetail post = blogSC.getPost(postId);
        String browseInfo = post.getTitle();
        WysiwygRouting routing = new WysiwygRouting();
        WysiwygRouting.WysiwygRoutingContext context =
            WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController(blogSC)
                .withBrowseInfo(browseInfo)
                .withContributionId(post.getIdentifier())
                .withComeBackUrl(
                    URLUtil.getApplicationURL() + blogSC.getComponentUrl() + "ViewPost?PostId=" +
                        post.getId())
                .withIndexation(false);
        destination = routing.getWysiwygEditorPath(context, request);
      } else if (function.startsWith("UpdatePost")) {
        String postId = request.getParameter(POST_ID);
        String title = request.getParameter("Title");
        String content = request.getParameter("editor");
        String categoryId = request.getParameter(CATEGORY_ID);
        String date = request.getParameter("DateEvent");
        String positions = request.getParameter("Positions");
        Date dateEvent;
        if (StringUtil.isDefined(date)) {
          dateEvent = DateUtil.stringToDate(date, blogSC.getLanguage());
        } else {
          dateEvent = new Date();
        }

        if ("UpdatePost".equals(function)) {
          // save post
          blogSC.updatePost(postId, title, content, categoryId, dateEvent, positions);
        } else if ("UpdatePostAndDraftOut".equals(function)) {
          // save and draft out the post
          blogSC.updatePostAndDraftOut(postId, title, content, categoryId, dateEvent, positions);
        }
        request.setAttribute(POST_ID, postId);
        destination = getDestination(VIEW_POST_FCT, blogSC, request);
      } else if ("DeletePost".equals(function)) {
        String postId = request.getParameter(POST_ID);
        blogSC.deletePost(postId);

        destination = getDestination("Main", blogSC, request);
      } else if (VIEW_POST_FCT.equals(function)) {
        // visualisation d'un billet avec les commentaires
        String postId = request.getParameter(POST_ID);
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute(POST_ID);
        }
        PostDetail post = blogSC.getPost(postId);
        request.setAttribute("Post", post);
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        setPostsByArchiveParam(blogSC, request);
        destination = rootDest + "viewPost.jsp";
      } else if (POST_BY_CATEGORY_FCT.equals(function)) {
        // récupération des paramètres
        String categoryId = request.getParameter(CATEGORY_ID);
        if (!StringUtil.isDefined(categoryId)) {
          categoryId = (String) request.getAttribute(CATEGORY_ID);
        }
        // récupération des billets par catégorie
        request.setAttribute(POSTS_ATTR, blogSC.postsByCategory(categoryId, SAFE_LIMIT));
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        setPostsByArchiveParam(blogSC, request);

        destination = rootDest + HOMEPAGE_VIEW;
      } else if (POST_BY_ARCHIVE_FCT.equals(function)) {
        // récupération des paramètres
        String beginDate = request.getParameter(BEGIN_DATE);
        if (!StringUtil.isDefined(beginDate)) {
          beginDate = (String) request.getAttribute(BEGIN_DATE);
        }
        String endDate = request.getParameter(END_DATE);
        if (!StringUtil.isDefined(endDate)) {
          endDate = (String) request.getAttribute(END_DATE);
        }

        // récupération des billets par archive
        Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate, SAFE_LIMIT);
        request.setAttribute(POSTS_ATTR, posts);
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        Collection<Event> events = toEvents(posts);
        request.setAttribute("Events", events);
        request.setAttribute(DATE_CALENDAR, blogSC.getCurrentBeginDateAsString());

        destination = rootDest + HOMEPAGE_VIEW;
      } else if ("PostByDay".equals(function)) {
        // récupération des paramètres
        String day = request.getParameter("Day");
        String date = DateUtil.date2SQLDate(day, blogSC.getLanguage());

        // récupération des billets par archive
        request.setAttribute(POSTS_ATTR, blogSC.postsByDate(date, SAFE_LIMIT));
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        setPostsByArchiveParam(blogSC, request);

        destination = rootDest + HOMEPAGE_VIEW;
      } else if ("PreviousMonth".equals(function)) {
        // modifier les dates de début et de fin de mois
        blogSC.previousMonth();
        // mettre les dates dans la request
        request.setAttribute(BEGIN_DATE, blogSC.getCurrentBeginDateAsString());
        request.setAttribute(END_DATE, blogSC.getCurrentEndDateAsString());
        destination = getDestination(POST_BY_ARCHIVE_FCT, blogSC, request);
      } else if ("NextMonth".equals(function)) {
        // modifier les dates de début et de fin de mois
        blogSC.nextMonth();
        // mettre les dates dans la request
        request.setAttribute(BEGIN_DATE, blogSC.getCurrentBeginDateAsString());
        request.setAttribute(END_DATE, blogSC.getCurrentEndDateAsString());
        destination = getDestination(POST_BY_ARCHIVE_FCT, blogSC, request);
      } else if (VIEW_CATEGORY_FCT.equals(function)) {
        Collection<NodeDetail> listCategorie = blogSC.getAllCategories();
        request.setAttribute("Categories", listCategorie);
        String listNodeJSON = blogSC.getListNodeJSON(listCategorie);
        request.setAttribute("ListCategoryJSON", listNodeJSON);
        destination = rootDest + "viewCategory.jsp";
      } else if ("CreateCategory".equals(function)) {
        // récupération des paramètres
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        NodeDetail node =
            new NodeDetail("unknown", name, description, 0, "unknown");
        Category category = new Category(node);
        blogSC.createCategory(category);

        destination = getDestination(VIEW_CATEGORY_FCT, blogSC, request);
      } else if ("UpdateCategory".equals(function)) {
        String categoryId = request.getParameter(CATEGORY_ID);
        Category category = blogSC.getCategory(categoryId);
        category.setName(request.getParameter("Name"));
        category.setDescription(request.getParameter("Description"));
        blogSC.updateCategory(category);

        destination = getDestination(VIEW_CATEGORY_FCT, blogSC, request);
      } else if ("DeleteCategory".equals(function)) {
        String categoryId = request.getParameter(CATEGORY_ID);
        blogSC.deleteCategory(categoryId);

        destination = getDestination(VIEW_CATEGORY_FCT, blogSC, request);
      } else if ("Search".equals(function)) {
        String wordSearch = request.getParameter("WordSearch");


        Collection<PostDetail> posts = blogSC.getResultSearch(wordSearch, SAFE_LIMIT);
        request.setAttribute(POSTS_ATTR, posts);

        setCommonParam(blogSC, request);
        request.setAttribute(DATE_CALENDAR, blogSC.getCurrentBeginDateAsString());

        destination = rootDest + HOMEPAGE_VIEW;
      } else if (function.startsWith("searchResult")) {
        // traiter les recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        if ("Publication".equals(type) || type.startsWith("Comment") || StringUtil.isDefined(id)) {
          // processing posts or comments or simply the aimed id
          request.setAttribute(POST_ID, id);
          destination = getDestination(VIEW_POST_FCT, blogSC, request);
        } else if ("Node".equals(type) || "Topic".equals(type)) {
          // process categories
          request.setAttribute(CATEGORY_ID, id);
          destination = getDestination(POST_BY_CATEGORY_FCT, blogSC, request);
        } else {
          destination = getDestination("Main", blogSC, request);
        }
      } else if (function.startsWith("portlet")) {
        // récupération des derniers billets
        request.setAttribute(POSTS_ATTR, blogSC.lastValidPosts());
        // appel de la page de portlet
        destination = rootDest + "portlet.jsp";
      } else if ("ManageSubscriptions".equals(function)) {
        destination = blogSC.manageSubscriptions();
      } else if ("UpdateFooter".equals(function)) {
        // mise à jour du pied de page
        WysiwygRouting routing = new WysiwygRouting();
        WysiwygRouting.WysiwygRoutingContext context =
            WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController(blogSC)
                .withContributionId(
                    ContributionIdentifier.from(blogSC.getComponentId(), blogSC.getComponentId(), COMPONENT_INSTANCE))
                .withComeBackUrl(URLUtil.getApplicationURL() +
                    URLUtil.getURL("blog", "useless", blogSC.getComponentId()) + "Main")
                .withIndexation(false);
        destination = routing.getWysiwygEditorPath(context, request);
      } else if ("DraftOutPost".equals(function)) {
        // sortir du mode brouillon
        String postId = request.getParameter(POST_ID);
        blogSC.draftOutPost(postId);
        request.setAttribute(POST_ID, postId);
        destination = getDestination(VIEW_POST_FCT, blogSC, request);
      } else if ("Customize".equals(function)) {
        String removeWallPaperFile = request.getParameter("removeWallPaperFile");
        String removeStyleSheetFile = request.getParameter("removeStyleSheetFile");
        FileItem fileWallPaper = request.getFile("wallPaper");
        FileItem fileStyleSheet = request.getFile("styleSheet");

        if (fileWallPaper != null && StringUtil.isDefined(fileWallPaper.getName())) {
          //Update
          blogSC.saveWallPaperFile(fileWallPaper);
        } else if ("yes".equals(removeWallPaperFile)) {
          //Remove
          blogSC.removeWallPaperFile();
        }
        if (fileStyleSheet != null && StringUtil.isDefined(fileStyleSheet.getName())) {
          //Update
          blogSC.saveStyleSheetFile(fileStyleSheet);
        } else if ("yes".equals(removeStyleSheetFile)) {
          //Remove
          blogSC.removeStyleSheetFile();
        }

        destination = getDestination("Main", blogSC, request);
      } else {
        destination = rootDest + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private Collection<Event> toEvents(final Collection<PostDetail> posts) {
    return posts.stream().map(post -> {
      final PublicationDetail publication = post.getPublication();
      final Date dateEvent = post.getDateEvent();
      return new Event(publication.getId(), publication.getName(), dateEvent, dateEvent, null, 0);
    }).collect(Collectors.toList());
  }

  private void setCommonParam(BlogSessionController blogSC, HttpServletRequest request) {
    request.setAttribute("Categories", blogSC.getAllCategories());
    request.setAttribute("Archives", blogSC.getAllArchives());
    request.setAttribute("Links", blogSC.getAllLinks());
    request.setAttribute("Url", blogSC.getComponentUrl());
    request.setAttribute("RSSUrl", blogSC.getRSSUrl());
    request.setAttribute("IsUsePdc", blogSC.isPdcUsed());
    request.setAttribute("WallPaper", blogSC.getWallPaper());
    request.setAttribute("StyleSheet", blogSC.getStyleSheet());
  }

  private void setPostsByArchiveParam(final BlogSessionController blogSC,
      final HttpRequest request) {
    String beginDate = blogSC.getCurrentBeginDateAsString();
    String endDate = blogSC.getCurrentEndDateAsString();
    Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate, SAFE_LIMIT);
    Collection<Event> events = toEvents(posts);
    request.setAttribute("Events", events);
    request.setAttribute(DATE_CALENDAR, beginDate);
  }
}
