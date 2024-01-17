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
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.mvc.util.WysiwygRouting;
import org.silverpeas.core.web.util.viewgenerator.html.monthcalendar.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static org.silverpeas.core.contribution.model.CoreContributionType.COMPONENT_INSTANCE;

public class BlogRequestRouter extends ComponentRequestRouter<BlogSessionController> {

  private static final long serialVersionUID = 6711772954612207110L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "Blog";
  }

  /**
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  @Override
  public BlogSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new BlogSessionController(mainSessionCtrl, componentContext);
  }

  // recherche du profile de l'utilisateur
  public String getFlag(String[] profiles) {
    String flag = SilverpeasRole.USER.toString();
    for (String profile : profiles) {
      if (SilverpeasRole.ADMIN.isInRole(profile)) {
        return profile;
      }
      if (SilverpeasRole.PUBLISHER.isInRole(profile)) {
        flag = profile;
      }
    }
    return flag;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param blogSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, BlogSessionController blogSC, HttpRequest request) {
    String destination = "";

    String rootDest = "/blog/jsp/";

    // paramètres généraux
    String flag = getFlag(blogSC.getUserRoles());
    request.setAttribute("Profile", flag);

    try {
      if (function.startsWith("Main")) {
        // récupération des derniers billets par date d'évènements
        Collection<PostDetail> posts = blogSC.lastPosts();
        request.setAttribute("Posts", posts);

        // passage des paramètres communs
        setCommonParam(blogSC, request);

        // creation d'une liste d'event par rapport aux posts du mois
        String beginDate = blogSC.getCurrentBeginDateAsString();
        String endDate = blogSC.getCurrentEndDateAsString();
        Collection<PostDetail> postsByMonth = blogSC.postsByArchive(beginDate, endDate);
        Collection<Event> events = getEvents(blogSC, postsByMonth);
        request.setAttribute("Events", events);

        request.setAttribute("IsUserSubscribed", blogSC.isUserSubscribed());

        request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10));

        // appel de la page d'accueil
        destination = rootDest + "accueil.jsp";
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
        String postId = request.getParameter("PostId");
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute("PostId");
        }
        PostDetail post = blogSC.getPost(postId);
        request.setAttribute("Post", post);
        request.setAttribute("AllCategories", blogSC.getAllCategories());
        request.setAttribute("Updater", blogSC.getUserDetail(post.getPublication().getUpdaterId()));
        // appel de la page de modification
        destination = rootDest + "postManager.jsp";
      } else if ("EditPostContent".equals(function)) {
        blogSC.checkWriteAccessOnBlogPost();
        String postId = request.getParameter("PostId");
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute("PostId");
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
        String postId = request.getParameter("PostId");
        String title = request.getParameter("Title");
        String content = request.getParameter("editor");
        String categoryId = request.getParameter("CategoryId");
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
        request.setAttribute("PostId", postId);
        destination = getDestination("ViewPost", blogSC, request);
      } else if ("DeletePost".equals(function)) {
        String postId = request.getParameter("PostId");
        blogSC.deletePost(postId);

        destination = getDestination("Main", blogSC, request);
      } else if ("ViewPost".equals(function)) {
        // visualisation d'un billet avec les commentaires
        String postId = request.getParameter("PostId");
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute("PostId");
        }
        PostDetail post = blogSC.getPost(postId);
        request.setAttribute("Post", post);
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        String beginDate = blogSC.getCurrentBeginDateAsString();
        String endDate = blogSC.getCurrentEndDateAsString();
        Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
        Collection<Event> events = getEvents(blogSC, posts);
        request.setAttribute("Events", events);
        request.setAttribute("DateCalendar", beginDate);
        destination = rootDest + "viewPost.jsp";
      } else if ("PostByCategory".equals(function)) {
        // récupération des paramètres
        String categoryId = request.getParameter("CategoryId");
        if (!StringUtil.isDefined(categoryId)) {
          categoryId = (String) request.getAttribute("CategoryId");
        }
        // récupération des billets par catégorie
        request.setAttribute("Posts", blogSC.postsByCategory(categoryId));
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        String beginDate = blogSC.getCurrentBeginDateAsString();
        String endDate = blogSC.getCurrentEndDateAsString();
        Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
        Collection<Event> events = getEvents(blogSC, posts);
        request.setAttribute("Events", events);
        request.setAttribute("DateCalendar", beginDate);
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10000));

        destination = rootDest + "accueil.jsp";
      } else if ("PostByArchive".equals(function)) {
        // récupération des paramètres
        String beginDate = request.getParameter("BeginDate");
        if (!StringUtil.isDefined(beginDate)) {
          beginDate = (String) request.getAttribute("BeginDate");
        }
        String endDate = request.getParameter("EndDate");
        if (!StringUtil.isDefined(endDate)) {
          endDate = (String) request.getAttribute("EndDate");
        }

        // récupération des billets par archive
        Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
        request.setAttribute("Posts", posts);
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        Collection<Event> events = getEvents(blogSC, posts);
        request.setAttribute("Events", events);
        request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10000));

        destination = rootDest + "accueil.jsp";
      } else if ("PostByDay".equals(function)) {
        // récupération des paramètres
        String day = request.getParameter("Day");
        String date = DateUtil.date2SQLDate(day, blogSC.getLanguage());

        // récupération des billets par archive
        request.setAttribute("Posts", blogSC.postsByDate(date));
        setCommonParam(blogSC, request);
        // creation d'une liste d'event par rapport à posts
        String beginDate = blogSC.getCurrentBeginDateAsString();
        String endDate = blogSC.getCurrentEndDateAsString();
        Collection<PostDetail> posts = blogSC.postsByArchive(beginDate, endDate);
        Collection<Event> events = getEvents(blogSC, posts);
        request.setAttribute("Events", events);
        request.setAttribute("DateCalendar", beginDate);
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10000));

        destination = rootDest + "accueil.jsp";
      } else if ("PreviousMonth".equals(function)) {
        // modifier les dates de début et de fin de mois
        blogSC.previousMonth();
        // mettre les dates dans la request
        request.setAttribute("BeginDate", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("EndDate", blogSC.getCurrentEndDateAsString());
        destination = getDestination("PostByArchive", blogSC, request);
      } else if ("NextMonth".equals(function)) {
        // modifier les dates de début et de fin de mois
        blogSC.nextMonth();
        // mettre les dates dans la request
        request.setAttribute("BeginDate", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("EndDate", blogSC.getCurrentEndDateAsString());
        destination = getDestination("PostByArchive", blogSC, request);
      } else if ("ViewCategory".equals(function)) {
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

        destination = getDestination("ViewCategory", blogSC, request);
      } else if ("UpdateCategory".equals(function)) {
        String categoryId = request.getParameter("CategoryId");
        Category category = blogSC.getCategory(categoryId);
        category.setName(request.getParameter("Name"));
        category.setDescription(request.getParameter("Description"));
        blogSC.updateCategory(category);

        destination = getDestination("ViewCategory", blogSC, request);
      } else if ("DeleteCategory".equals(function)) {
        String categoryId = request.getParameter("CategoryId");
        blogSC.deleteCategory(categoryId);

        destination = getDestination("ViewCategory", blogSC, request);
      } else if ("Search".equals(function)) {
        String wordSearch = request.getParameter("WordSearch");


        Collection<PostDetail> posts = blogSC.getResultSearch(wordSearch);
        request.setAttribute("Posts", posts);

        setCommonParam(blogSC, request);
        request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10000));

        destination = rootDest + "accueil.jsp";
      } else if (function.startsWith("searchResult")) {
        // traiter les recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");


        if ("Publication".equals(type)) {
          // traitement des billets

          request.setAttribute("PostId", id);
          destination = getDestination("ViewPost", blogSC, request);
        } else if ("Node".equals(type) || "Topic".equals(type)) {
          // traitement des catégories
          request.setAttribute("CategoryId", id);
          destination = getDestination("PostByCategory", blogSC, request);
        } else if (type.startsWith("Comment")) {
          // traitement des commentaires
          request.setAttribute("PostId", id);
          destination = getDestination("ViewPost", blogSC, request);
        } else if (StringUtil.isDefined(id)) {
          // no type, so given id is surely a post id
          request.setAttribute("PostId", id);
          destination = getDestination("ViewPost", blogSC, request);
        } else {
          destination = getDestination("Main", blogSC, request);
        }
      } else if (function.startsWith("portlet")) {
        // récupération des derniers billets
        request.setAttribute("Posts", blogSC.lastValidPosts());
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
        String postId = request.getParameter("PostId");
        blogSC.draftOutPost(postId);
        request.setAttribute("PostId", postId);
        destination = getDestination("ViewPost", blogSC, request);
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

  private Collection<Event> getEvents(BlogSessionController blogSC, Collection<PostDetail> posts) {
    Collection<Event> events = new ArrayList<>();
    Date dateEvent;
    for (PostDetail post : posts) {
      // chercher la date de l'évènement
      String pubId = post.getPublication().getPK().getId();
        dateEvent = blogSC.getDateEvent(pubId);
      Event event =
          new Event(post.getPublication().getPK().getId(), post.getPublication().getName(),
              dateEvent, dateEvent, null, 0);
      events.add(event);
    }

    return events;
  }

  private void setCommonParam(BlogSessionController blogSC, HttpServletRequest request) {
    request.setAttribute("Categories", blogSC.getAllCategories());
    request.setAttribute("Archives", blogSC.getAllArchives());
    request.setAttribute("Links", blogSC.getAllLinks());
    request.setAttribute("Url", blogSC.getComponentUrl());
    request.setAttribute("RSSUrl", blogSC.getRSSUrl());
    request.setAttribute("IsUsePdc", blogSC.isPdcUsed());
    request.setAttribute("IsDraftVisible", blogSC.isDraftVisible());
    request.setAttribute("WallPaper", blogSC.getWallPaper());
    request.setAttribute("StyleSheet", blogSC.getStyleSheet());
  }
}
