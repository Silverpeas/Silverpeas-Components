/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.blog.servlets;

import com.silverpeas.blog.control.BlogSessionController;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class BlogRequestRouter extends ComponentRequestRouter<BlogSessionController> {

  private static final long serialVersionUID = 1L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "Blog";
  }

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public BlogSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new BlogSessionController(mainSessionCtrl, componentContext);
  }

  // recherche du profile de l'utilisateur
  public String getFlag(String[] profiles) {
    String flag = SilverpeasRole.user.toString();
    for (String profile : profiles) {
      if (SilverpeasRole.admin.isInRole(profile)) {
        return profile;
      }
      if (SilverpeasRole.publisher.isInRole(profile)) {
        flag = profile;
      }
    }
    return flag;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function    The entering request function (ex : "Main.jsp")
   * @param blogSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, BlogSessionController blogSC, HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "User=" + blogSC.getUserId() + " Function=" + function);
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

        request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10));
        request.setAttribute("WallPaperName", blogSC.getNameWallPaper());
        request.setAttribute("WallPaperURL", blogSC.getURLWallPaper());
        request.setAttribute("WallPaperSize", blogSC.getSizeWallPaper());
        request.setAttribute("StyleSheetName", blogSC.getNameStyleSheet());
        request.setAttribute("StyleSheetURL", blogSC.getURLStyleSheet());
        request.setAttribute("StyleSheetSize", blogSC.getSizeStyleSheet());
        request.setAttribute("StyleSheetContent", blogSC.getContentStyleSheet());

        // appel de la page d'accueil
        destination = rootDest + "accueil.jsp";
      } else if (function.equals("NewPost")) {
        request.setAttribute("AllCategories", blogSC.getAllCategories());
        
        // appel de la page de création
        destination = rootDest + "postManager.jsp";
      } else if (function.equals("CreatePost")) {
        // récupération des paramètres pour création
        String title = request.getParameter("Title");
        String categoryId = request.getParameter("CategoryId");
        String date = request.getParameter("DateEvent");
        Date dateEvent = null;
        if (StringUtil.isDefined(date)) {
          dateEvent = DateUtil.stringToDate(date, blogSC.getLanguage());
        } else {
          dateEvent = new Date();
        }
        
        // Classification
        String positions = request.getParameter("Positions");
        PdcClassificationEntity classification =
            PdcClassificationEntity.undefinedClassification();
        if (StringUtil.isDefined(positions)) {
          classification = PdcClassificationEntity.fromJSON(positions);
        }
  
        String postId = blogSC.createPost(title, categoryId, dateEvent, classification);

        // appel de la page pour saisir le contenu du billet
        request.setAttribute("PostId", postId);
        destination = getDestination("ViewContent", blogSC, request);
      } else if (function.equals("EditPost")) {
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
      } else if (function.equals("UpdatePost")) {
        String postId = request.getParameter("PostId");
        String title = request.getParameter("Title");
        String categoryId = request.getParameter("CategoryId");
        String date = request.getParameter("DateEvent");
        Date dateEvent = null;
        if (StringUtil.isDefined(date)) {
          dateEvent = DateUtil.stringToDate(date, blogSC.getLanguage());
        } else {
          dateEvent = new Date();
        }

        // MAJ base
        blogSC.updatePost(postId, title, categoryId, dateEvent);

        destination = getDestination("ViewContent", blogSC, request);
      } else if (function.equals("DeletePost")) {
        String postId = request.getParameter("PostId");
        blogSC.deletePost(postId);

        destination = getDestination("Main", blogSC, request);
      } else if (function.equals("ViewContent")) {
        String postId = request.getParameter("PostId");
        if (!StringUtil.isDefined(postId)) {
          postId = (String) request.getAttribute("PostId");
        }

        PostDetail post = blogSC.getPost(postId);
        PublicationDetail pub = post.getPublication();
        request.setAttribute("CurrentPublicationDetail", pub);
        request.setAttribute("UserId", blogSC.getUserId());
        // visualisation et modification du contenu Wysiwyg du billet
        destination = rootDest + "toWysiwyg.jsp";
      } else if (function.equals("FromWysiwyg")) {
        destination = getDestination("ViewPost", blogSC, request);
      } else if (function.equals("ViewPost")) {
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
      } else if (function.equals("PostByCategory")) {
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
      } else if (function.equals("PostByArchive")) {
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
      } else if (function.equals("PostByDay")) {
        // récupération des paramètres
        String day = request.getParameter("Day");
        String date = DateUtil.date2SQLDate(day, blogSC.getLanguage());
        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "date =" + date);
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
      } else if (function.equals("PreviousMonth")) {
        // modifier les dates de début et de fin de mois
        blogSC.previousMonth();
        // mettre les dates dans la request
        request.setAttribute("BeginDate", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("EndDate", blogSC.getCurrentEndDateAsString());
        destination = getDestination("PostByArchive", blogSC, request);
      } else if (function.equals("NextMonth")) {
        // modifier les dates de début et de fin de mois
        blogSC.nextMonth();
        // mettre les dates dans la request
        request.setAttribute("BeginDate", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("EndDate", blogSC.getCurrentEndDateAsString());
        destination = getDestination("PostByArchive", blogSC, request);
      } else if (function.equals("ViewCategory")) {
        Collection<NodeDetail> listCategorie = blogSC.getAllCategories();
        request.setAttribute("Categories", listCategorie);
        String listNodeJSON = blogSC.getListNodeJSON(listCategorie);
        request.setAttribute("ListCategoryJSON", listNodeJSON);
        destination = rootDest + "viewCategory.jsp";
      } else if (function.equals("CreateCategory")) {
        // récupération des paramètres
        String name = request.getParameter("Name");
        String description = request.getParameter("Description");
        NodeDetail node =
            new NodeDetail("unknown", name, description, null, null, null, "0", "unknown");
        Category category = new Category(node);
        blogSC.createCategory(category);

        destination = getDestination("ViewCategory", blogSC, request);
      } else if (function.equals("UpdateCategory")) {
        String categoryId = request.getParameter("CategoryId");
        Category category = blogSC.getCategory(categoryId);
        category.setName(request.getParameter("Name"));
        category.setDescription(request.getParameter("Description"));
        blogSC.updateCategory(category);

        destination = getDestination("ViewCategory", blogSC, request);
      } else if (function.equals("DeleteCategory")) {
        String categoryId = request.getParameter("CategoryId");
        blogSC.deleteCategory(categoryId);

        destination = getDestination("ViewCategory", blogSC, request);
      } else if (function.equals("ToAlertUser")) {
        String postId = request.getParameter("PostId");
        try {
          destination = blogSC.initAlertUser(postId);
        } catch (Exception e) {
          SilverTrace.warn("blog", "BlogRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if (function.equals("Search")) {
        String wordSearch = request.getParameter("WordSearch");
        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "word =" + wordSearch);

        Collection<PostDetail> posts = blogSC.getResultSearch(wordSearch);
        request.setAttribute("Posts", posts);
        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "posts =" + posts);
        setCommonParam(blogSC, request);
        request.setAttribute("DateCalendar", blogSC.getCurrentBeginDateAsString());
        request.setAttribute("NbPostDisplayed", Integer.valueOf(10000));

        destination = rootDest + "accueil.jsp";
      } else if (function.startsWith("searchResult")) {
        // traiter les recherches
        String id = request.getParameter("Id");
        String type = request.getParameter("Type");
        SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
            "Type=" + type);

        if (type.equals("Publication")) {
          // traitement des billets
          SilverTrace.info("blog", "BlogRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "Id=" + id);
          request.setAttribute("PostId", id);
          destination = getDestination("ViewPost", blogSC, request);
        } else if (type.equals("Node") || type.equals("Topic")) {
          // traitement des catégories
          request.setAttribute("CategoryId", id);
          destination = getDestination("PostByCategory", blogSC, request);
        } else if (type.startsWith("Comment")) {
          // traitement des commentaires
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
      } else if (function.equals("AddSubscription")) {
        // ajout aux abonnements
        blogSC.addSubscription("0");
        destination = getDestination("Main", blogSC, request);
      } else if (function.equals("UpdateFooter")) {
        // mise à jour du pied de page
        destination = rootDest + "footer.jsp";
      } else if (function.equals("DraftOutPost")) {
        // sortir du mode brouillon 
        String postId = request.getParameter("PostId");
        blogSC.draftOutPost(postId);
        request.setAttribute("PostId", postId);
        destination = getDestination("ViewPost", blogSC, request);
      } else if (function.equals("Customize")) {
        List<FileItem> items = FileUploadUtil.parseRequest(request);
        String removeWallPaperFile = FileUploadUtil.getParameter(items, "removeWallPaperFile");
        String removeStyleSheetFile = FileUploadUtil.getParameter(items, "removeStyleSheetFile");
        FileItem fileWallPaper = FileUploadUtil.getFile(items, "wallPaper");
        FileItem fileStyleSheet = FileUploadUtil.getFile(items, "styleSheet");
        
        if (fileWallPaper != null && StringUtil.isDefined(fileWallPaper.getName())) {//Update
          blogSC.saveWallPaperFile(fileWallPaper);
        } else if ("yes".equals(removeWallPaperFile)) {//Remove
          blogSC.removeWallPaperFile();
        }
        if (fileStyleSheet != null && StringUtil.isDefined(fileStyleSheet.getName())) {//Update
          blogSC.saveStyleSheetFile(fileStyleSheet);
        } else if ("yes".equals(removeStyleSheetFile)) {//Remove
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

    SilverTrace.info("blog", "BlogRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "Destination=" + destination);
    return destination;
  }

  private Collection<Event> getEvents(BlogSessionController blogSC, Collection<PostDetail> posts) {
    Collection<Event> events = new ArrayList<Event>();
    Date dateEvent;
    for (PostDetail post : posts) {
      // chercher la date de l'évènement
      String pubId = post.getPublication().getPK().getId();
      try {
        dateEvent = blogSC.getDateEvent(pubId);
      } catch (RemoteException e) {
        dateEvent = post.getPublication().getCreationDate();
      }
      Event event = new Event(post.getPublication().getPK().getId(), post.getPublication().getName(),
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
  }
}