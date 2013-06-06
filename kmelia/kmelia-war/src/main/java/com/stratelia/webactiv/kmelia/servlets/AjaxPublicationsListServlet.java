/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.kmelia.servlets;

import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.kmelia.KmeliaConstants;
import com.silverpeas.kmelia.domain.TopicSearch;
import com.silverpeas.kmelia.search.KmeliaSearchServiceFactory;
import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.ImageUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.kmelia.model.KmeliaPublicationComparator;
import com.stratelia.webactiv.kmelia.model.TopicDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.UserNameGenerator;
import com.stratelia.webactiv.util.viewGenerator.html.board.Board;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.component.kmelia.KmeliaPublicationHelper;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.viewer.ViewerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import static com.stratelia.webactiv.SilverpeasRole.*;
import static com.stratelia.webactiv.util.publication.model.PublicationDetail.*;


/**
 * @author ehugonnet
 */
public class AjaxPublicationsListServlet extends HttpServlet {

  private static final long serialVersionUID = 1003665785797438465L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    HttpSession session = req.getSession(true);

    String componentId = req.getParameter("ComponentId");
    String nodeId = req.getParameter("Id");
    String sToLink = req.getParameter("ToLink");
    String TopicToLinkId = req.getParameter("TopicToLinkId");
    // check if trying to link attachment
    String attachmentLink = req.getParameter("attachmentLink");
    boolean attachmentToLink = StringUtil.getBooleanValue(attachmentLink);

    boolean toLink = StringUtil.getBooleanValue(sToLink);

    KmeliaSessionController kmeliaSC = (KmeliaSessionController) session.getAttribute(
        "Silverpeas_kmelia_" + componentId);
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    String context = URLManager.getApplicationURL();

    if (kmeliaSC == null && (toLink || attachmentToLink)) {
      MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
          MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, componentId);
      kmeliaSC = new KmeliaSessionController(mainSessionCtrl, componentContext);
      session.setAttribute("Silverpeas_kmelia_" + componentId, kmeliaSC);
    }

    if (kmeliaSC != null) {

      if (StringUtil.isDefined(nodeId)) {
        kmeliaSC.setCurrentFolderId(nodeId, true);
        kmeliaSC.getPublicationsOfCurrentFolder();
        session.setAttribute("Silverpeas_DragAndDrop_TopicId", nodeId); // used by drag n drop
      }

      if ((toLink || attachmentToLink) && StringUtil.isDefined(TopicToLinkId)) {
        TopicDetail currentTopicToLink = kmeliaSC.getTopic(TopicToLinkId, false);
        kmeliaSC.setSessionTopicToLink(currentTopicToLink);
      }

      ResourcesWrapper resources =
          new ResourcesWrapper(kmeliaSC.getMultilang(), kmeliaSC.getIcon(),
          kmeliaSC.getSettings(), kmeliaSC.getLanguage());

      String index = req.getParameter("Index");
      String sort = req.getParameter("Sort");
      String sToPortlet = req.getParameter("ToPortlet");
      String pubIdToHighlight = req.getParameter("PubIdToHighLight");
      String query = req.getParameter("Query");

      String selectedPublicationIds = req.getParameter("SelectedPubIds");
      String notSelectedPublicationIds = req.getParameter("NotSelectedPubIds");
      List<String> selectedIds =
          kmeliaSC.processSelectedPublicationIds(selectedPublicationIds, notSelectedPublicationIds);
      boolean toPortlet = StringUtil.getBooleanValue(sToPortlet);
      boolean searchInProgress = StringUtil.isDefined(query);

      if (StringUtil.isDefined(index)) {
        kmeliaSC.setIndexOfFirstPubToDisplay(index);
      }
      if (StringUtil.isDefined(sort)) {
        kmeliaSC.setSortValue(sort);
      }
      SilverTrace.info("kmelia", "AjaxPublicationsListServlet.doPost", "root.MSG_GEN_PARAM_VALUE",
          "Request parameters = " + req.getQueryString());

      boolean sortAllowed = true;
      boolean linksAllowed = true;
      boolean seeAlso = false;
      List<KmeliaPublication> publications;
      TopicDetail currentTopic = null;
      String role = kmeliaSC.getProfile();
      if (toLink) {
        currentTopic = kmeliaSC.getSessionTopicToLink();
        sortAllowed = false;
        linksAllowed = false;
        seeAlso = true;
        // get selected publication ids from session
        selectedIds = processPublicationsToLink(req);
        String currentPubId = req.getParameter("PubId");
        String currentPubComponentId = req.getParameter("PubComponentName");
        PublicationPK publicationPK = null;
        if (StringUtil.isDefined(currentPubId) && StringUtil.isDefined(currentPubComponentId)) {
          publicationPK = new PublicationPK(currentPubId, currentPubComponentId);
        }
        publications = currentTopic.getValidPublications(publicationPK);
        if (resources.getSetting("linkManagerSortByPubId", false)) {
          Collections.sort(publications, new KmeliaPublicationComparator());
        }
      } else if (toPortlet) {
        sortAllowed = false;
        publications = kmeliaSC.getSessionPublicationsList();
        role = SilverpeasRole.user.toString();
      } else if (searchInProgress) {
        // Insert this new search inside persistence layer in order to compute statistics
        saveTopicSearch(componentId, nodeId, kmeliaSC, query);
        publications = kmeliaSC.search(query);
      } else {
        publications = kmeliaSC.getSessionPublicationsList();
      }

      if (attachmentToLink) {
        sortAllowed = false;
        linksAllowed = false;
        seeAlso = false;
        searchInProgress = false;
      }

      if (KmeliaHelper.isToolbox(componentId)) {
        String profile = kmeliaSC.getUserTopicProfile(kmeliaSC.getCurrentFolderId());
        linksAllowed = !SilverpeasRole.user.isInRole(profile);
      }

      res.setContentType("text/xml");
      res.setHeader("charset", "UTF-8");

      Writer writer = res.getWriter();
      if (kmeliaSC.isRightsOnTopicsEnabled() && kmeliaSC.isUserComponentAdmin()
          && !kmeliaSC.isCurrentTopicAvailable()) {
        Board board = gef.getBoard();
        writer.write(board.printBefore());
        writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" align=\"center\">");
        writer.write("<tr>");
        writer.write("<td>" + resources.getString("GML.ForbiddenAccessContent") + "</td>");
        writer.write("</tr>");
        writer.write("</table>");
        writer.write(board.printAfter());
      } else if (NodePK.ROOT_NODE_ID.equals(kmeliaSC.getCurrentFolderId()) &&
          kmeliaSC.getNbPublicationsOnRoot() != 0 && kmeliaSC.isTreeStructure() &&
          !searchInProgress) {
        displayLastPublications(kmeliaSC, resources, gef, writer);
      } else {
        if (publications != null) {
          displayPublications(publications, sortAllowed, linksAllowed, seeAlso, searchInProgress,
              kmeliaSC, role, gef, context, resources, selectedIds, pubIdToHighlight, writer,
              attachmentToLink);
        }
      }
    }
  }

  /**
   * Save current topic search inside persistence layer
   * @param componentId the component identifier
   * @param nodeId the node identifier
   * @param kmeliaSC the KmeliaSessionController
   * @param query the topic search query keywords
   */
  private void saveTopicSearch(String componentId, String nodeId, KmeliaSessionController kmeliaSC,
      String query) {
    //Check node value
    if(!StringUtil.isDefined(nodeId)) {
      nodeId = kmeliaSC.getCurrentFolderId();
    }
    TopicSearch newTS =
        new TopicSearch(componentId, Integer.parseInt(nodeId), Integer.parseInt(kmeliaSC
            .getUserId()), kmeliaSC.getLanguage(), query.toLowerCase(), new Date());
    KmeliaSearchServiceFactory.getTopicSearchService().createTopicSearch(newTS);
  }

  /**
   * @param allPubs
   * @param sortAllowed
   * @param linksAllowed
   * @param checkboxAllowed
   * @param kmeliaScc
   * @param profile
   * @param gef
   * @param context
   * @param resources
   * @param selectedIds
   * @param pubIdToHighlight
   * @param out
   * @param linkAttachment indicates if it displays the attachment publication for the link
   * management administration
   * @throws IOException
   * @throws ThumbnailException
   * @throws NumberFormatException
   */
  private void displayPublications(List<KmeliaPublication> allPubs,
      boolean sortAllowed, boolean linksAllowed, boolean seeAlso, boolean toSearch,
      KmeliaSessionController kmeliaScc, String profile,
      GraphicElementFactory gef, String context, ResourcesWrapper resources,
      List<String> selectedIds, String pubIdToHighlight, Writer out, boolean linkAttachment)
      throws IOException {

    String publicationSrc = resources.getIcon("kmelia.publication");
    ResourceLocator publicationSettings = new ResourceLocator(
        "org.silverpeas.util.publication.publicationSettings", kmeliaScc.getLanguage());
    boolean showNoPublisMessage = resources.getSetting("showNoPublisMessage", true);

    String language = kmeliaScc.getCurrentLanguage();
    String currentUserId = kmeliaScc.getUserDetail().getId();
    String currentTopicId = kmeliaScc.getCurrentFolderId();

    // check if this instance use a custom template
    boolean specificTemplateUsed = kmeliaScc.isCustomPublicationTemplateUsed();

    PublicationFragmentSettings fragmentSettings = new PublicationFragmentSettings();
    fragmentSettings.displayLinks = URLManager.displayUniversalLinks();
    fragmentSettings.showImportance = kmeliaScc.isFieldImportanceVisible();
    fragmentSettings.fileStorageShowExtraInfoPub = resources.getSetting(
        "fileStorageShowExtraInfoPub", false);
    fragmentSettings.showTopicPathNameinSearchResult =
        resources.getSetting("showTopicPathNameinSearchResult", true);
    fragmentSettings.showDelegatedNewsInfo = kmeliaScc.isNewsManage() && !user.isInRole(profile);
    fragmentSettings.toSearch = toSearch;

    int nbPubsPerPage = kmeliaScc.getNbPublicationsPerPage();
    int firstDisplayedItemIndex = kmeliaScc.getIndexOfFirstPubToDisplay();
    int nbPubs = allPubs.size();
    Board board = gef.getBoard();
    Pagination pagination = gef.getPagination(nbPubs, nbPubsPerPage, firstDisplayedItemIndex);
    List<KmeliaPublication> pubs = allPubs.subList(pagination.getFirstItemIndex(), pagination.
        getLastItemIndex());
    out.write("<form name=\"publicationsForm\" onsubmit=\"return false;\">");
    if (!pubs.isEmpty()) {
      out.write(board.printBefore());
      displayPublicationsListHeader(nbPubs, sortAllowed, pagination, resources, kmeliaScc, out);
      out.write("<ul>");
      for (KmeliaPublication aPub : pubs) {
        PublicationDetail pub = aPub.getDetail();
        UserDetail currentUser = aPub.getCreator();

        String pubColor = "";
        String pubState = null;
        String highlightClass = "";

        if (StringUtil.isDefined(pubIdToHighlight) && pubIdToHighlight.equals(pub.getPK().getId())) {
          highlightClass = "highlight";
        }

        out.write("<!-- Publication Body -->");

        if (pub.getStatus() != null && pub.isValid()) {
          if (pub.haveGotClone() && CLONE.equals(pub.getCloneStatus()) && !user.isInRole(profile)) {
            pubColor = "blue";
            pubState = resources.getString("kmelia.UpdateInProgress");
          } else if (DRAFT.equals(pub.getCloneStatus())) {
            if (currentUserId.equals(currentUser.getId())) {
              pubColor = "gray";
              pubState = resources.getString("PubStateDraft");
            }
          } else if (TO_VALIDATE.equals(pub.getCloneStatus())) {
            if (admin.isInRole(profile) || publisher.isInRole(profile)
                || currentUserId.equals(currentUser.getId())) {
              pubColor = "red";
              pubState = resources.getString("kmelia.PubStateToValidate");
            }
          } else {
            if (pub.isNotYetVisible()) {
              pubState = resources.getString("kmelia.VisibleFrom") + " "
                  + resources.getOutputDateAndHour(pub.getBeginDateAndHour());
            } else if (pub.isNoMoreVisible()) {
              pubState = resources.getString("kmelia.VisibleTo") + " "
                  + resources.getOutputDateAndHour(pub.getEndDateAndHour());
            }
            if (!pub.isVisible()) {
              pubColor = "gray";
            }
          }
        } else {
          if (pub.getStatus() != null && pub.isDraft()) {
            // en mode brouillon, si on est en co-rédaction et si on autorise
            // le mode brouillon visible par tous,
            // les publication en mode brouillon sont visibles par tous sauf les
            // lecteurs sinon, seules les publications brouillons de l'utilisateur sont visibles
            if (currentUserId.equals(pub.getUpdaterId())
                || ((kmeliaScc.isCoWritingEnable() && kmeliaScc.isDraftVisibleWithCoWriting())
                && !user.isInRole(profile))) {
              pubColor = "gray";
              pubState = resources.getString("PubStateDraft");
            }
          } else {
            if (admin.isInRole(profile) || publisher.isInRole(profile)
                || currentUserId.equals(pub.getUpdaterId())
                || (!user.isInRole(profile) && kmeliaScc.isCoWritingEnable())) {
              // si on est en co-rédaction, on affiche toutes les publications
              // à valider (sauf pour les lecteurs)
              pubColor = "red";
              if (pub.isRefused()) {
                pubState = resources.getString("kmelia.PubStateUnvalidate");
              } else {
                pubState = resources.getString("kmelia.PubStateToValidate");
              }
            }
          }
        }

        if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
          pubState = resources.getString("kmelia.Shortcut");
        }

        String cssClass = "";
        if (toSearch) {
          if (aPub.read) {
            cssClass = " class=\"read\"";
          } else {
            cssClass = " class=\"unread\"";
          }
        }

        out.write("<li");
        out.write(cssClass);
        out.write(" onmouseover=\"showPublicationOperations(this);\"");
        out.write(" onmouseout=\"hidePublicationOperations(this);\">");

        out.write("<div class=\"firstColumn\">");
        if (!kmeliaScc.getUserDetail().isAnonymous() && !kmeliaScc.isKmaxMode) {
          String checked = "";
          if (selectedIds != null && selectedIds.contains(pub.getPK().getId())) {
            checked = "checked=\"checked\"";
          }
          out.write("<span class=\"selection\">");
          out.write("<input type=\"checkbox\" name=\"C1\" value=\""
              + pub.getPK().getId() + "/" + pub.getPK().getInstanceId() + "\" " + checked
              + " onclick=\"sendPubId(this.value, this.checked);\"/>");
          out.write("</span>");
        }
        if (!seeAlso) {
          ThumbnailDetail thumbnail = pub.getThumbnail();
          if (thumbnail != null && Boolean.valueOf(resources.getSetting("isVignetteVisible"))) {
            out.write("<span class=\"thumbnail\">");
            try {
              displayThumbnail(pub, kmeliaScc, publicationSettings, out);
            } catch (ThumbnailException e) {
              SilverTrace.info("kmelia", "AjaxPublicationsListServlet.displayPublications()",
                  "root.MSG_GEN_ENTER_METHOD", "exception = " + e);
            }
            out.write("</span>");
          } else {
            out.write("<span class=\"thumbnail\">");
            out.write("<img src=\"" + resources.getIcon("kmelia.1px") + "\" alt=\"\"/>");
            out.write("</span>");
          }
        }
        out.write("</div>");

        fragmentSettings.pubColor = pubColor;
        fragmentSettings.highlightClass = highlightClass;
        fragmentSettings.pubState = pubState;
        fragmentSettings.linksAllowed = linksAllowed;
        fragmentSettings.seeAlso = seeAlso;
        fragmentSettings.linkAttachment = linkAttachment;

        out.write("<div class=\"publication\"><a name=\"" + pub.getPK().getId() + "\"></a>");
        displayFragmentOfPublication(specificTemplateUsed, aPub, fragmentSettings, language,
            currentUserId, currentTopicId, kmeliaScc, resources, out);
        out.write("</div>");
      }
      out.write("</ul>");
      if (nbPubs > nbPubsPerPage) {
        out.write("<div id=\"pagination\">");
        out.write(pagination.printIndex("doPagination"));
        out.write("</div>");
      }
      displayFilePreviewJavascript(kmeliaScc.getComponentId(), kmeliaScc.isVersionControlled(), out);
      displayFileViewJavascript(kmeliaScc.getComponentId(), kmeliaScc.isVersionControlled(), out);
      out.write(board.printAfter());
    } else if (showNoPublisMessage
        && (toSearch || kmeliaScc.getNbPublicationsOnRoot() != 0 || !currentTopicId.equals("0"))) {
      String noPublications = kmeliaScc.getString("PubAucune");
      if (toSearch) {
        noPublications = kmeliaScc.getString("NoPubFound");
      }
      out.write("<div id=\"noPublicationMessage\">");
      out.write(board.printBefore());
      out.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" align=\"center\">");
      out.write("<tr valign=\"middle\">");
      out.write("<td width=\"80\"><img src=\"" + publicationSrc + "\" border=\"0\"/></td>");
      out.write("<td align=\"left\"><b>" + resources.getString("GML.publications")
          + "</b></td></tr>");
      out.write("<tr><td colspan=\"2\">&#160;</td></tr>");
      out.write("<tr>");
      out.write("<td>&#160;</td>");
      out.write("<td>" + noPublications + "</td>");
      out.write("</tr>");
      out.write("</table>");
      out.write(board.printAfter());
      out.write("</div>");
    }
    out.write("</form>");
  }

  void displayFilePreviewJavascript(String componentId, boolean versioned, Writer out)
      throws IOException {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">");
    sb.append("function previewFile(target, attachmentId) {");
    sb.append("$(target).preview(\"previewAttachment\", {");
    sb.append("componentInstanceId: \"").append(componentId).append("\",");
    sb.append("attachmentId: attachmentId,");
    sb.append("versioned: ").append(versioned);
    sb.append("});");
    sb.append("return false;");
    sb.append("}");
    sb.append("</script>");
    out.write(sb.toString());
  }

  void displayFileViewJavascript(String componentId, boolean versioned, Writer out)
      throws IOException {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">");
    sb.append("function viewFile(target, attachmentId) {");
    sb.append("$(target).view(\"viewAttachment\", {");
    sb.append("componentInstanceId: \"").append(componentId).append("\",");
    sb.append("attachmentId: attachmentId,");
    sb.append("versioned: ").append(versioned);
    sb.append("});");
    sb.append("return false;");
    sb.append("}");
    sb.append("</script>");
    out.write(sb.toString());
  }

  void displayFragmentOfPublication(boolean specificTemplateUsed, KmeliaPublication aPub,
      PublicationFragmentSettings fragmentSettings, String language, String userId,
      String topicId, KmeliaSessionController kmeliaScc, ResourcesWrapper resources, Writer out)
      throws IOException {

    // check if publication is draggable
    boolean canBeCut = KmeliaPublicationHelper.isCanBeCut(kmeliaScc.getComponentId(), userId,
        kmeliaScc.getUserTopicProfile(), aPub.getCreator());
    boolean alias = isAlias(kmeliaScc, aPub.getDetail());
    fragmentSettings.draggable = canBeCut && !alias && !KmeliaHelper.isToValidateFolder(topicId);

    if (specificTemplateUsed) {
      displayTemplatedFragmentOfPublication(aPub, fragmentSettings, language, userId, topicId,
          kmeliaScc, resources, out);
    } else {
      displayDefaultFragmentOfPublication(aPub, fragmentSettings, language, userId, topicId,
          kmeliaScc, resources, out);
    }
  }

  void displayTemplatedFragmentOfPublication(KmeliaPublication aPub,
      PublicationFragmentSettings fragmentSettings, String language, String userId, String topicId,
      KmeliaSessionController kmeliaScc, ResourcesWrapper resources, Writer out) throws IOException {
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
    PublicationDetail pub = aPub.getDetail();
    String name = pub.getName(language);
    String description = pub.getDescription(language);

    template.setAttribute("publication", pub);
    template.setAttribute("link", "javascript:onClick=publicationGoTo('" + pub.getId() + "')");
    template.setAttribute("name", name);
    template.setAttribute("description", EncodeHelper.javaStringToHtmlParagraphe(description));
    template.setAttribute("showDescription",
        StringUtil.isDefined(description) && !description.equals(name));
    template.setAttribute("importance", displayImportance(pub.getImportance(), resources));
    template.setAttribute("showImportance", fragmentSettings.showImportance
        && !fragmentSettings.linkAttachment);
    template.setAttribute("date", displayDate(pub, kmeliaScc, resources));
    template.setAttribute("creationDate", resources.getOutputDate(pub.getCreationDate()));
    template.setAttribute("updateDate", resources.getOutputDate(pub.getUpdateDate()));
    // check if the pub date must be display
    boolean showPubDate = true;
    if (fragmentSettings.linkAttachment) {
      showPubDate = showPubDate && fragmentSettings.fileStorageShowExtraInfoPub;
    }
    template.setAttribute("showDate", showPubDate);

    // check if user name in the list must be display
    boolean showUserName = kmeliaScc.showUserNameInList();
    if (fragmentSettings.linkAttachment) {
      showUserName = showUserName && fragmentSettings.fileStorageShowExtraInfoPub;
    }
    template.setAttribute("username", getUserName(aPub, kmeliaScc));
    template.setAttribute("showUsername", showUserName);
    template.setAttribute("permalink", displayPermalink(pub, kmeliaScc, resources));
    template.setAttribute("showPermalink", fragmentSettings.displayLinks
        && !fragmentSettings.seeAlso && !fragmentSettings.linkAttachment);
    template.setAttribute("status", fragmentSettings.pubState);
    template.setAttribute("statusColor", fragmentSettings.pubColor);
    template.setAttribute("highlightClass", fragmentSettings.highlightClass);
    template
        .setAttribute("showRef", fragmentSettings.seeAlso && resources.getSetting(
        "linkManagerShowPubId", false));
    // Show topic name only in search in topic case
    if (fragmentSettings.toSearch && fragmentSettings.showTopicPathNameinSearchResult) {
      template.setAttribute("path", displayPublicationFullPath(kmeliaScc, pub));
    }
    boolean showAuthor = kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pub.getAuthor());
    if (fragmentSettings.linkAttachment) {
      showAuthor = showAuthor && fragmentSettings.fileStorageShowExtraInfoPub;
    }
    template.setAttribute("showAuthor", showAuthor);
    template.setAttribute("author", pub.getAuthor());
    template.setAttribute("files",
        displayFiles(pub, fragmentSettings.linkAttachment, fragmentSettings.seeAlso, userId,
        topicId, kmeliaScc, resources));

    if (!pub.getInfoId().equals("0")) {
      template.setAttribute("formName", pub.getInfoId());
      template.setAttribute("form", pub.getFormValues(language));
    }

    String fragment =
        template.applyFileTemplateOnComponent("kmelia",
        kmeliaScc.getCustomPublicationTemplateName());
    out.write(fragment);
  }

  void displayDefaultFragmentOfPublication(KmeliaPublication aPub,
      PublicationFragmentSettings fragmentSettings, String language,
      String userId, String topicId, KmeliaSessionController kmeliaScc, ResourcesWrapper resources,
      Writer out) throws IOException {
    PublicationDetail pub = aPub.getDetail();
    String name = pub.getName(language);
    out.write("<div class=\"line1\">");
    out.write("<span class=\"bullet\">&#8226;</span>");
    if (fragmentSettings.linksAllowed) {
      out.write("<font color=\"");
      out.write(fragmentSettings.pubColor);
      out.write("\"><a href=\"javascript:onClick=publicationGoTo('");
      out.write(pub.getPK().getId());
      out.write("')\"><b class=\"" + fragmentSettings.highlightClass + "\">");
      if (fragmentSettings.draggable) {
        out.write("<span class=\"jstree-draggable\" id=\"pub-" + pub.getPK().getId() + "\">");
        out.write(name);
        out.write("</span>");
      } else {
        out.write(name);
      }
      out.write("</b></a></font>");
    } else {
      String ref = "";
      if (fragmentSettings.seeAlso && resources.getSetting("linkManagerShowPubId", false)) {
        ref = " [ " + pub.getPK().getId() + " ] ";
      }
      out.write("<font color=\"");
      out.write(fragmentSettings.pubColor);
      out.write("\"><b class=\"" + fragmentSettings.highlightClass + "\">");
      out.write(ref);
      out.write(name);
      out.write("</b></font>");
    }
    out.write("&#160;");
    if (StringUtil.isDefined(fragmentSettings.pubState)) {
      out.write("<span class=\"state_");
      out.write(fragmentSettings.pubState);
      out.write("\">(");
      out.write(fragmentSettings.pubState);
      out.write(")</span>");
    } else if (fragmentSettings.showImportance && !fragmentSettings.linkAttachment) {
      out.write("<span class=\"importance\"><nobr>");
      out.write(displayImportance(pub.getImportance(), resources));
      out.write("</nobr></span>");
    }

    //Gestion actualités décentralisées
    if (fragmentSettings.showDelegatedNewsInfo) {
      DelegatedNews delegatedNews = kmeliaScc.getDelegatedNews(pub.getPK().getId());
      if (delegatedNews != null) {
        out.write("<span class=\"actualite\"><nobr>");
        if (DelegatedNews.NEWS_TO_VALIDATE.equals(delegatedNews.getStatus())) {
          out.write(" (" + resources.getString("kmelia.DelegatedNewsToValidate") + ")");
        } else if (DelegatedNews.NEWS_VALID.equals(delegatedNews.getStatus())) {
          out.write(" (" + resources.getString("kmelia.DelegatedNewsValid") + ")");
        } else if (DelegatedNews.NEWS_REFUSED.equals(delegatedNews.getStatus())) {
          out.write(" (" + resources.getString("kmelia.DelegatedNewsRefused") + ")");
        }
        out.write("</nobr></span>");
      }
    }

    out.write("</div>");
    out.write("<div class=\"line2\">");
    out.write("<font color=\"");
    out.write(fragmentSettings.pubColor);
    out.write("\">");
    // Show topic name only in search in topic case
    if (fragmentSettings.toSearch && fragmentSettings.showTopicPathNameinSearchResult) {
      out.write(displayPublicationFullPath(kmeliaScc, pub));
    }
    // check if user name in the list must be display
    boolean showUserNameInList = kmeliaScc.showUserNameInList();
    if (fragmentSettings.linkAttachment) {
      showUserNameInList = showUserNameInList && fragmentSettings.fileStorageShowExtraInfoPub;
    }
    if (showUserNameInList) {
      out.write("<span class=\"user\">");
      out.write(getUserName(aPub, kmeliaScc));
      out.write(" - </span>");
    }
    // check if the pub date must be display
    boolean showPubDate = true;
    if (fragmentSettings.linkAttachment) {
      showPubDate = showPubDate && fragmentSettings.fileStorageShowExtraInfoPub;
    }
    if (showPubDate) {
      out.write("<span class=\"date\">");
      out.write(displayDate(pub, kmeliaScc, resources));
      out.write("</span>");
    }

    // check if he author name must be display
    boolean showAuthor = kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pub.getAuthor());
    if (fragmentSettings.linkAttachment) {
      showAuthor = showAuthor && fragmentSettings.fileStorageShowExtraInfoPub;
    }
    if (showAuthor) {
      out.write("<span class=\"author\">");
      out.write("&#160;-&#160;(");
      out.write(resources.getString("GML.author"));
      out.write(":&#160;");
      out.write(pub.getAuthor());
      out.write(")</span>");
    }
    // displays permalink
    if (fragmentSettings.displayLinks && !fragmentSettings.seeAlso
        && !fragmentSettings.linkAttachment) {
      out.write("<span class=\"permalink\">");
      out.write(displayPermalink(pub, kmeliaScc, resources));
      out.write("</span>");
    }
    out.write("</div>");

    String description = pub.getDescription(language);
    // displays publication description
    if (StringUtil.isDefined(description) && !description.equals(name)) {
      out.write("<div class=\"line3\">");
      out.write("<span class=\"description\">");
      out.write(EncodeHelper.javaStringToHtmlParagraphe(description));
      out.write("</span>");
      out.write("</div>");
    }

    out.write("</font>");

    out.write(displayFiles(pub, fragmentSettings.linkAttachment, fragmentSettings.seeAlso, userId,
        topicId, kmeliaScc, resources));
  }

  String displayDate(PublicationDetail pub, KmeliaSessionController kmeliaScc,
      ResourcesWrapper resources) {
    if ("5".equals(kmeliaScc.getSortValue()) || "6".equals(kmeliaScc.getSortValue())) {
      return resources.getOutputDate(pub.getCreationDate());
    } else {
      return resources.getOutputDate(pub.getUpdateDate());
    }
  }

  String displayFiles(PublicationDetail pub, boolean linkAttachment, boolean seeAlso, String userId,
      String topicId,
      KmeliaSessionController kmeliaScc, ResourcesWrapper resources) throws IOException {
    StringBuilder sb = new StringBuilder(20);
    boolean displayFiles =
        (KmeliaHelper.isToolbox(kmeliaScc.getComponentId()) || kmeliaScc.attachmentsInPubList())
        && !seeAlso || linkAttachment;
    if (displayFiles) {
      sb.append("<span class=\"files\">");
      // Can be a shortcut. Must check attachment mode according to publication source.
      boolean alias = isAlias(kmeliaScc, pub);
      if (kmeliaScc.isVersionControlled(pub.getPK().getInstanceId())) {
        sb.append(displayVersioning(pub, resources, linkAttachment, alias));
      } else {
        sb.append(displayAttachments(pub, userId, topicId, resources, linkAttachment, alias,
            kmeliaScc.getCurrentLanguage()));
      }
      sb.append("</span>");
    }
    return sb.toString();
  }

  private boolean isAlias(KmeliaSessionController kmeliaScc, PublicationDetail pub) {
    return !kmeliaScc.getComponentId().equalsIgnoreCase(pub.getPK().getInstanceId());
  }

  void displayThumbnail(PublicationDetail pub, KmeliaSessionController ksc,
      ResourceLocator publicationSettings, Writer out) throws IOException, NumberFormatException,
      ThumbnailException {
    int[] defaultSizes = ksc.getThumbnailWidthAndHeight();
    String vignette_url;
    if (pub.getImage().startsWith("/")) {
      vignette_url = pub.getImage() + "&Size=133x100";
      out.write("<img src=\"" + vignette_url + "\" alt=\"\"/>&#160;");
    } else {
      vignette_url =
          FileServerUtils.getUrl(pub.getPK().
          getComponentName(),
          "vignette", pub.getImage(), pub.getImageMimeType(),
          publicationSettings.getString("imagesSubDirectory"));
      String height = "";
      String width = "";
      if (!StringUtil.isDefined(pub.getThumbnail().getCropFileName())) {
        // thumbnail is not cropable, process sizes
        String[] size = new String[2];
        File image = getThumbnail(pub, publicationSettings);
        if (defaultSizes[0] != -1) {
          size = ImageUtil.getWidthAndHeightByWidth(image, defaultSizes[0]);
        } else if (defaultSizes[1] != -1) {
          size = ImageUtil.getWidthAndHeightByHeight(image, defaultSizes[1]);
        }
        if (StringUtil.isDefined(size[0]) && StringUtil.isDefined(size[1])) {
          width = size[0];
          height = size[1];
        }
      }
      out.write("<img src=\"" + vignette_url + "\""
          + (!StringUtil.isDefined(height) ? "" : " height=\"" + height + "\"")
          + (!StringUtil.isDefined(width) ? "" : " width=\"" + width + "\"")
          + "/ alt=\"\">&#160;");
    }
  }

  String displayPermalink(PublicationDetail pub, KmeliaSessionController kmeliaScc,
      ResourcesWrapper resources) throws IOException {
    String link;
    if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
      link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId(),
          kmeliaScc.getComponentId());
    } else {
      link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId());
    }
    return " - <a href=\""
        + link
        + "\"><img src=\""
        + resources.getIcon("kmelia.link")
        + "\" border=\"0\" align=\"absmiddle\" alt=\""
        + resources.getString("kmelia.CopyPublicationLink")
        + "\" title=\""
        + resources.getString("kmelia.CopyPublicationLink")
        + "\"/></a>";
  }

  void displaySortingListBox(ResourcesWrapper resources, KmeliaSessionController ksc, Writer out)
      throws IOException {
    out.
        write(
        "<select name=\"sortBy\" id=\"sortingList\" onChange=\"javascript:sortGoTo(this.selectedIndex);\">");
    out.write("<option>" + resources.getString("SortBy") + "</option>");
    out.write("<option>-------------------------------</option>");
    out.write("<option value=\"1\" id=\"sort1\" " + isSelectedSort(ksc, "1") + ">" + resources.
        getString("DateAsc") + "</option>");
    out.write("<option value=\"2\" id=\"sort2\" " + isSelectedSort(ksc, "2") + ">" + resources.
        getString("DateDesc") + "</option>");
    out.write("<option value=\"5\" id=\"sort5\" " + isSelectedSort(ksc, "5") + ">" + resources.
        getString("CreateDateAsc") + "</option>");
    out.write("<option value=\"6\" id=\"sort6\" " + isSelectedSort(ksc, "6") + ">" + resources.
        getString("CreateDateDesc") + "</option>");
    out.write("<option value=\"0\" id=\"sort0\" " + isSelectedSort(ksc, "0") + ">" + resources.
        getString("PubAuteur") + "</option>");
    if (ksc.isFieldImportanceVisible()) {
      out.write("<option value=\"3\" id=\"sort3\" " + isSelectedSort(ksc, "3") + ">" + resources.
          getString("PubImportance") + "</option>");
    }
    out.write("<option value=\"4\" id=\"sort4\" " + isSelectedSort(ksc, "4") + ">" + resources.
        getString("PubTitre") + "</option>");
    out.write("<option value=\"7\" id=\"sort7\" " + isSelectedSort(ksc, "7") + ">" + resources.
        getString("PubDescription") + "</option>");
    out.write("<option value=\"-1\" id=\"sort-1\" " + isSelectedSort(ksc, "-1") + ">" + resources.
        getString("kmelia.publis.sort.-1")
        + "</option>");
    out.write("</select>");
  }

  private String isSelectedSort(KmeliaSessionController ksc, String sort) {
    if (sort.equals(ksc.getSortValue())) {
      return "selected=\"selected\"";
    }
    return "";
  }

  void displayPublicationsListHeader(int nbPubs, boolean sortAllowed,
      Pagination pagination, ResourcesWrapper resources, KmeliaSessionController ksc, Writer out)
      throws IOException {
    String publicationSrc = resources.getIcon("kmelia.publication");
    out.write("<div id=\"pubsHeader\">");
    out.write("<img src=\"" + publicationSrc + "\" alt=\"\"/>");
    out.write("<span id=\"pubsCounter\">");
    out.write("<span>" + pagination.printCounter() + "</span> ");
    if (nbPubs > 1) {
      out.write(resources.getString("GML.publications"));
    } else {
      out.write(resources.getString("GML.publication"));
    }
    out.write("</span>");
    out.write("<span id=\"pubsSort\">");
    if (sortAllowed) {
      displaySortingListBox(resources, ksc, out);
    }
    out.write("</span>");
    out.write("</div>");
  }

  String getUserName(KmeliaPublication userPub, KmeliaSessionController kmeliaScc) {
    UserDetail currentUser = userPub.getCreator(); // contains creator
    PublicationDetail pub = userPub.getDetail();
    String updaterId = pub.getUpdaterId();
    UserDetail updater = null;
    if (updaterId != null && updaterId.length() > 0) {
      updater = kmeliaScc.getUserDetail(updaterId);
    }
    if (updater == null) {
      updater = currentUser;
    }
    if (updater != null && (StringUtil.isDefined(updater.getFirstName())
        || StringUtil.isDefined(updater.getLastName()))) {
      return UserNameGenerator.toString(updater, kmeliaScc.getUserId());
    }
    return kmeliaScc.getString("kmelia.UnknownUser");
  }

  private String displayImportance(int importance, ResourcesWrapper resources) {
    int maxImportance = 5;
    String fullStar = resources.getIcon("kmelia.fullStar");
    String emptyStar = resources.getIcon("kmelia.emptyStar");
    StringBuilder stars = new StringBuilder();

    // display full Stars
    for (int i = 0; i < importance; i++) {
      stars.append("<img src=\"").append(fullStar).append("\" align=\"absmiddle\" alt=\"\"/>");
    }
    // display empty stars
    for (int i = importance + 1; i <= maxImportance; i++) {
      stars.append("<img src=\"").append(emptyStar).append("\" align=\"absmiddle\" alt=\"\"/>");
    }
    return stars.toString();
  }

  @SuppressWarnings("unchecked")
  private List<String> processPublicationsToLink(HttpServletRequest request) {
    // get from session the list of publications to link with current publication
    HashSet<String> list = (HashSet<String>) request.getSession().getAttribute(
        KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
    // store the publication identifiers in an array list
    List<String> publicationsToLink = new ArrayList<String>();
    if (list != null) {
      for (String link : list) {
        StringTokenizer tokens = new StringTokenizer(link, "/");
        publicationsToLink.add(tokens.nextToken());
      }
    }
    return publicationsToLink;
  }

  private String displayVersioning(PublicationDetail pubDetail, ResourcesWrapper resources,
      boolean linkAttachment, boolean alias) {
    ForeignPK foreignPK = new ForeignPK(pubDetail.getPK());
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(foreignPK, null);
    StringBuilder result = new StringBuilder();
    boolean oneFile = false;
    for (SimpleDocument document : documents) {
      SimpleDocument version = document.getLastPublicVersion();
      if (version != null) {
        if (result.length() == 0) {
          result.append("<table border=\"0\">");
          oneFile = true;
        }
        String id = version.getPk().getId();
        String title = version.getTitle() + " v" + version.getMajorVersion();
        String info = version.getDescription();
        String icon = FileRepositoryManager.getFileIcon(FilenameUtils.getExtension(document.
            getFilename()));
        String logicalName = version.getFilename();
        String size = FileRepositoryManager.formatFileSize(version.getSize());
        String downloadTime = FileRepositoryManager.getFileDownloadTime(version.getSize());
        Date creationDate = version.getCreated();
        String permalink = URLManager.getSimpleURL(URLManager.URL_DOCUMENT, document.getId());
        String url = FileServerUtils.getApplicationContext() + version.getAttachmentURL();

        if (alias) {
          url = version.getAliasURL();
        }
        boolean previewable = ViewerFactory.isPreviewable(version.getAttachmentPath());
        boolean viewable = ViewerFactory.isViewable(version.getAttachmentPath());
        result.append(displayFile(url, title, info, icon, logicalName, size, downloadTime,
            creationDate, permalink, resources, linkAttachment, previewable, viewable, id));
      }
    }
    if (oneFile) {
      result.append("</table>");
    }
    return result.toString();
  }

  private String displayAttachments(PublicationDetail pubDetail, String userId, String nodeId,
      ResourcesWrapper resources, boolean linkAttachment, boolean alias, String language) {
    SilverTrace.info("kmelia", "AjaxPublicationsListServlet.displayAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubDetail.getPK().getId());
    StringBuilder result = new StringBuilder();

    ForeignPK foreignKey = new ForeignPK(pubDetail.getPK().getId(), pubDetail.getPK().
        getInstanceId());


    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKey(foreignKey, null);
    if (!documents.isEmpty()) {
      result.append("<table border=\"0\">");
      for (SimpleDocument document : documents) {
        String url = FileServerUtils.getApplicationContext() + document.getAttachmentURL();
        String title = document.getTitle();
        String info = document.getDescription();
        String icon = FileRepositoryManager.getFileIcon(FilenameUtils.getExtension(document.
            getFilename()));
        String logicalName = document.getFilename();
        String id = document.getId();
        String size = FileRepositoryManager.formatFileSize(document.getSize());
        String downloadTime = FileRepositoryManager.getFileDownloadTime(document.getSize());
        Date creationDate = document.getCreated();
        String permalink = URLManager.getSimpleURL(URLManager.URL_FILE, id);
        if (alias) {
          url = FileServerUtils.getAliasURL(foreignKey.getInstanceId(), document.getFilename(), id);
        }
        boolean previewable = ViewerFactory.isPreviewable(document.getAttachmentPath());
        boolean viewable = ViewerFactory.isViewable(document.getAttachmentPath());
        result.append(displayFile(url, title, info, icon, logicalName, size, downloadTime,
            creationDate, permalink, resources, linkAttachment, previewable, viewable, id));
      }
      result.append("</table>");
    }
    SilverTrace.info("kmelia", "JSPattachmentUtils.displayAttachments()",
        "root.MSG_GEN_EXIT_METHOD", "result = " + result.toString());
    return result.toString();
  }

  /**
   * @param url
   * @param title
   * @param info
   * @param icon
   * @param logicalName
   * @param size
   * @param downloadTime
   * @param creationDate
   * @param permalink
   * @param resources
   * @param attachmentLink determines if the attachments are displayed in the management interface
   * links to attachments. If it's true it formats differently the HTML rendering
   * @param viewable
   * @param out
   * @return
   * @throws IOException
   */
  private String displayFile(String url, String title, String info, String icon,
      String logicalName, String size, String downloadTime, Date creationDate, String permalink,
      ResourcesWrapper resources, boolean attachmentLink, boolean previewable, boolean viewable,
      String id) {
    SilverTrace.info("kmelia", "AjaxPublicationsListServlet.displayFile()",
        "root.MSG_GEN_ENTER_METHOD");
    StringBuilder result = new StringBuilder(1024);

    if (!attachmentLink) {
      String link = "<a href=\"" + url + "\" target=\"_blank\">";
      result.append("<tr><td valign=\"top\">");
      // Add doc type icon
      result.append(link).append("<img src=\"").append(icon).append(
          "\" border=\"0\" align=\"absmiddle\"/></a>&#160;</td>");
      result.append("<td valign=\"top\">").append(link);
      if (title == null || title.length() == 0) {
        result.append(logicalName);
      } else {
        result.append(title);
      }
      result.append("</a>");

      if (StringUtil.isDefined(permalink)) {
        result.append("&#160;<a href=\"").append(permalink).append(
            "\" target=\"_blank\"><img src=\"").append(resources.getIcon("kmelia.link")).append(
            "\" border=\"0\" valign=\"absmiddle\" alt=\"").append(
            resources.getString("toolbox.CopyFileLink")).append("\" title=\"").append(
            resources.getString("toolbox.CopyFileLink")).append("\"/></a>");
      }

      result.append("<br/>");

      result.append("<i>");
      if (StringUtil.isDefined(title) && !"no".equals(resources.getSetting("showTitle"))) {
        result.append(logicalName).append(" / ");
      }
      // Add file size
      if (!"no".equals(resources.getSetting("showFileSize"))) {
        result.append(size);
      }
      // and download estimation
      if (!"no".equals(resources.getSetting("showDownloadEstimation"))) {
        result.append(" / ").append(downloadTime).append(" / ").append(
            resources.getOutputDate(creationDate));
      }
      if (previewable) {
        result.append(" <img onclick=\"javascript:previewFile(this, '").append(id)
            .append("');\" class=\"preview-file\" src=\"")
            .append(resources.getIcon("kmelia.file.preview"))
            .append("\" alt=\"").append(resources.getString("GML.preview")).append("\" title=\"")
            .append(resources.getString("GML.preview")).append("\"/>");
      }
      if (viewable) {
        result.append(" <img onclick=\"javascript:viewFile(this, '").append(id)
            .append("');\" class=\"view-file\" src=\"")
            .append(resources.getIcon("kmelia.file.view"))
            .append("\" alt=\"").append(resources.getString("GML.view")).append("\" title=\"")
            .append(resources.getString("GML.view")).append("\"/>");
      }
      result.append("</i>");

      // Add info
      if (StringUtil.isDefined(info) && !"no".equals(resources.getSetting("showInfo"))) {
        result.append("<br/>").append(EncodeHelper.javaStringToHtmlParagraphe(info));
      }
      result.append("</td></tr>");

    } else {
      // determines the label to display
      String displayedTitle;
      if (!StringUtil.isDefined(title)) {
        displayedTitle = logicalName;
      } else {
        displayedTitle = title;
      }
      // create the javascript which allows the attachment link selecting
      String javascriptFunction = "selectAttachment('" + url + "','" + icon + "','"
          + displayedTitle + "')";
      String link = "<a href=\"javascript:" + javascriptFunction + "\" >";
      result.append("<tr><td valign=\"top\">");

      // Add doc type icon
      result.append(link).append("<img src=\"").append(icon).append(
          "\" border=\"0\" align=\"absmiddle\"/></a>&#160;</td>");
      result.append("<td valign=\"top\">").append(link);

      // inserts label (attachment title or logical file name)
      result.append(displayedTitle);
      result.append("</a>");

      // inserts permalink
      if (StringUtil.isDefined(permalink)) {
        result.append("&#160;<a href=\"").append(permalink).append(
            "\" target=\"_blank\"><img src=\"").append(resources.getIcon("kmelia.link")).append(
            "\" border=\"0\" valign=\"absmiddle\" alt=\"").append(
            resources.getString("toolbox.CopyFileLink")).append("\" title=\"").append(
            resources.getString("toolbox.CopyFileLink")).append("\"/></a>");
      }
      result.append("<br/>");
      // displays extra information if parameter is true
      if (resources.getSetting("fileStorageShowExtraInfoAttachment", false)) {
        result.append("<i>");
        if (StringUtil.isDefined(title)) {
          result.append(logicalName).append(" / ");
        }
        // Add file size
        result.append(size);

        // and download estimation
        result.append(" / ").append(downloadTime).append(" / ").append(
            resources.getOutputDate(creationDate));
        result.append("</i>");
        // Add info
        if (StringUtil.isDefined(info)) {
          result.append("<br/>").append(EncodeHelper.javaStringToHtmlParagraphe(info));
        }
      }

    }
    SilverTrace.info("kmelia", "JSPattachmentUtils.displayFile()", "root.MSG_GEN_EXIT_METHOD");
    return result.toString();

  }

  private void displayLastPublications(KmeliaSessionController kmeliaScc,
      ResourcesWrapper resources, GraphicElementFactory gef, Writer writer) throws IOException {

    List<KmeliaPublication> pubs = kmeliaScc.getLatestPublications();
    boolean displayLinks = URLManager.displayUniversalLinks();
    PublicationDetail pub;
    KmeliaPublication kmeliaPub;
    String language = kmeliaScc.getCurrentLanguage();

    Iterator<KmeliaPublication> iterator = pubs.iterator();

    Board board = gef.getBoard();
    writer.write(board.printBefore());
    writer.write("<table border=\"0\" width=\"98%\" align=\"center\" id=\"latestPublications\">");
    writer.write("<tr>");
    writer.write("<td width=\"40\" align=\"left\"><img src=\"" + resources.getIcon(
        "kmelia.publication") + "\" border=0></td>");
    writer.write("<td align=\"left\" width=\"100%\"><b>" + kmeliaScc.getString("PublicationsLast")
        + "</b></td>");
    writer.write("</tr>");
    if (iterator.hasNext()) {
      writer.write("<tr><td colspan=\"2\">&nbsp;</td></tr>");
      writer.write("<!-- Publications Header End -->");
      writer.write("<tr><td colspan=\"2\"><table border=\"0\" width=\"100%\">");
      int j = 1;
      int nbCol = Integer.parseInt(resources.getSetting("HomeNbCols"));
      if (pubs.size() < nbCol) {
        nbCol = pubs.size();
      }
      String width = Integer.toString(100 / nbCol);
      boolean endRaw = false;
      String linkIcon = resources.getIcon("kmelia.link");
      while (iterator.hasNext()) {
        if (j == 1) {
          writer.write("<tr>\n");
          writer.write("<td valign=\"top\">&nbsp;</td>\n");
          endRaw = false;
        }
        if (j <= nbCol) {
          kmeliaPub = iterator.next();
          pub = kmeliaPub.getDetail();
          String shortcut;
          if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
            shortcut = " (" + resources.getString("kmelia.Shortcut") + ")";
          } else {
            shortcut = "";
          }

          writer.write("<!-- Publication Body -->");
          writer.write("<td valign=\"top\" width=\"100\">&#149; </td>");
          writer.write("<td valign=\"top\" width=\"" + width + "%\">");
          writer.write("<p><b><a href=\"javascript:onClick=publicationGoToFromMain('"
              + pub.getPK().
              getId() + "')\">" + EncodeHelper.javaStringToHtmlString(pub.getName(language))
              + "</a>" + shortcut + "</b><br/>");

          if (kmeliaScc.showUserNameInList()) {
            writer.write(getUserName(kmeliaPub, kmeliaScc) + " - ");
          }
          writer.write(resources.getOutputDate(pub.getUpdateDate()));
          if (displayLinks) {
            String link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId());
            writer.write(" - <a href=\"" + link + "\"><img src=\"" + linkIcon
                + "\" border=\"0\" align=\"absmiddle\" alt=\"" + resources.getString(
                "kmelia.CopyPublicationLink") + "\" title=\"" + resources.getString(
                "kmelia.CopyPublicationLink") + "\"></a>");
          }
          writer.write("<br/>");
          writer.write(EncodeHelper.javaStringToHtmlParagraphe(pub.getDescription(language)));
          writer.write("</p>");
          writer.write("</td>");
          writer.write("<!-- Publication Body End -->");
          j++;
        }
        if (j > nbCol) {
          writer.write("\t</tr>");
          endRaw = true;
          j = 1;
        }
      }
      if (!endRaw) {
        int nbTd = nbCol - j + 1;
        int k = 1;
        while (k <= nbTd) {
          writer.write("<td colspan=\"3\" valign=\"top\">&nbsp;</td>\n");
          k++;
        }
        writer.write("</tr>\n");
      }
      writer.write("</td></tr></table>");
    } // End if
    else {
      writer.write("<tr>");
      writer.write("<td>&nbsp;</td>");
      writer.write("<td>" + kmeliaScc.getString("PubAucune") + "</td>");
      writer.write("</tr>");
    }
    writer.write("</td>");
    writer.write("</tr>");
    writer.write("</table>");
    writer.write(board.printAfter());
  }

  private String displayPublicationFullPath(KmeliaSessionController kmelia, PublicationDetail pub) {
    // Get space and componentLabel of the publication (can be different from context)
    OrganisationController orga = kmelia.getOrganisationController();
    ComponentInstLight compoInstLight = orga.getComponentInstLight(pub.getInstanceId());
    String componentLabel = compoInstLight.getLabel(kmelia.getCurrentLanguage());
    String spaceLabel =
        orga.getSpaceInstLightById(compoInstLight.getDomainFatherId()).getName(
        kmelia.getCurrentLanguage());
    List<NodePK> nodesPK = (List<NodePK>) pub.getPublicationBm().getAllFatherPK(pub.getPK());
    if (nodesPK != null) {
      NodePK firstNodePK = nodesPK.get(0);
      String topicPathName = spaceLabel + " > " + componentLabel + " > "
          + kmelia.displayPath(kmelia.getKmeliaBm().getPath(firstNodePK.getId(),
          firstNodePK.getInstanceId()), false, 3);
      return "<div class=\"publiPath\">" + topicPathName + "</div>";
    }
    return "";
  }

  private File getThumbnail(PublicationDetail pubDetail, ResourceLocator publicationSettings) {
    if (StringUtil.isDefined(pubDetail.getImage())) {
      return new File(FileRepositoryManager.getAbsolutePath(pubDetail.getPK().getInstanceId())
          + publicationSettings.getString("imagesSubDirectory") + File.separatorChar
          + pubDetail.getImage());
    }
    return null;
  }
}
