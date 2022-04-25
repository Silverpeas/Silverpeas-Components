/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.servlets;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Option;
import org.apache.ecs.wml.Alignment;
import org.apache.ecs.wml.Img;
import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.KmeliaConstants;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.KmeliaPublicationComparator;
import org.silverpeas.components.kmelia.model.TopicDetail;
import org.silverpeas.components.kmelia.model.ValidatorsList;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.date.TemporalFormatter;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailSettings;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.viewer.service.ViewerProvider;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.ImageTag;
import org.silverpeas.core.web.util.viewgenerator.html.UserNameGenerator;
import org.silverpeas.core.web.util.viewgenerator.html.board.Board;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;
import org.silverpeas.core.webapi.rating.RaterRatingEntity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.silverpeas.components.kmelia.model.KmeliaPublicationSort.*;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;
import static org.silverpeas.core.contribution.publication.model.PublicationDetail.*;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.web.selection.BasketSelectionUI.getPutIntoBasketSelectionHtmlSnippet;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.scriptContent;

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
  public void doPost(HttpServletRequest request, HttpServletResponse res)
      throws IOException {
    final HttpRequest req = HttpRequest.decorate(request);
    final HttpSession session = req.getSession(true);

    String componentId = req.getParameter("ComponentId");
    String nodeId = req.getParameter("Id");
    String sToLink = req.getParameter("ToLink");
    String topicToLinkId = req.getParameter("TopicToLinkId");
    // check if trying to link attachment
    boolean attachmentToLink = req.getParameterAsBoolean("attachmentLink");

    boolean toLink = StringUtil.getBooleanValue(sToLink);

    KmeliaSessionController kmeliaSC =
        (KmeliaSessionController) session.getAttribute("Silverpeas_kmelia_" + componentId);
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    if (kmeliaSC == null && (toLink || attachmentToLink)) {
      MainSessionController mainSessionCtrl = (MainSessionController) session
          .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, componentId);
      kmeliaSC = new KmeliaSessionController(mainSessionCtrl, componentContext);
      session.setAttribute("Silverpeas_kmelia_" + componentId, kmeliaSC);
    }

    if (kmeliaSC != null) {

      if (StringUtil.isDefined(nodeId)) {
        kmeliaSC.setCurrentFolderId(nodeId, true);
        kmeliaSC.loadPublicationsOfCurrentFolder();
        // used by drag n drop
        session.setAttribute("Silverpeas_DragAndDrop_TopicId", nodeId);
      }

      if ((toLink || attachmentToLink) && StringUtil.isDefined(topicToLinkId)) {
        TopicDetail currentTopicToLink = kmeliaSC.getTopic(topicToLinkId, false);
        kmeliaSC.setSessionTopicToLink(currentTopicToLink);
      }

      MultiSilverpeasBundle resources =
          new MultiSilverpeasBundle(kmeliaSC.getMultilang(), kmeliaSC.getIcon(), kmeliaSC.getSettings(),
              kmeliaSC.getLanguage());

      Integer index = req.getParameterAsInteger("Index");
      Integer nbItemsPerPage = req.getParameterAsInteger("NbItemsPerPage");
      Integer sort = req.getParameterAsInteger("Sort");
      boolean resetManualSort = StringUtil.getBooleanValue(req.getParameter("ResetManualSort"));
      boolean toPortlet = req.getParameterAsBoolean("ToPortlet");
      String pubIdToHighlight = req.getParameter("PubIdToHighLight");
      String query = req.getParameter("Query");
      boolean searchRequest = req.getParameterAsBoolean("SearchRequest");

      QueryDescription queryDescription = new QueryDescription(query);
      PagesContext formContext = new PagesContext();
      initSearch(queryDescription, formContext, req, kmeliaSC);

      String selectedPublicationIds = req.getParameter("SelectedPubIds");
      String notSelectedPublicationIds = req.getParameter("NotSelectedPubIds");
      List<PublicationPK> selectedIds =
          kmeliaSC.processSelectedPublicationIds(selectedPublicationIds, notSelectedPublicationIds);
      boolean searchContextExists = kmeliaSC.getSearchContext() != null;
      boolean newSearchInProgress = searchRequest && !queryDescription.isEmpty();

      if (index != null) {
        kmeliaSC.setIndexOfFirstPubToDisplay(index);
      }
      if (nbItemsPerPage != null) {
        kmeliaSC.setNbPublicationsPerPage(nbItemsPerPage);
      }
      if (sort != null) {
        kmeliaSC.setSortValue(sort);
        final String contentLanguage = kmeliaSC.getCurrentLanguage();
        ofNullable(kmeliaSC.getSearchContext()).ifPresent(c -> c.applySort(sort, contentLanguage));
      } else if (resetManualSort) {
        kmeliaSC.resetPublicationsOrder();
      }

      if (!newSearchInProgress && searchContextExists && (searchRequest || index == null) &&
          nbItemsPerPage == null && sort == null) {
        kmeliaSC.setSearchContext(null);
        searchContextExists = false;
        kmeliaSC.loadPublicationsOfCurrentFolder();
      }

      boolean sortAllowed = true;
      boolean linksAllowed = true;
      boolean seeAlso = false;
      List<KmeliaPublication> publications;
      TopicDetail currentTopic;
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
          publications.sort(new KmeliaPublicationComparator());
        }
      } else if (toPortlet) {
        sortAllowed = false;
        publications = kmeliaSC.getSessionPublicationsList();
        role = SilverpeasRole.USER.toString();
      } else if (newSearchInProgress) {
        publications = kmeliaSC.search(queryDescription, formContext);
      } else if (searchContextExists) {
        publications = kmeliaSC.getSearchContext().getResults();
        if (index != null) {
          kmeliaSC.getSearchContext().setPaginationIndex(index);
        }
      } else {
        publications = kmeliaSC.getSessionPublicationsList();
      }

      if (attachmentToLink) {
        sortAllowed = false;
        linksAllowed = false;
        seeAlso = false;
        kmeliaSC.setSearchContext(null);
        searchContextExists = false;
      }

      if (KmeliaHelper.isToolbox(componentId)) {
        String profile = kmeliaSC.getUserTopicProfile(kmeliaSC.getCurrentFolderId());
        linksAllowed = !SilverpeasRole.USER.isInRole(profile);
      }

      res.setContentType("text/xml");
      res.setCharacterEncoding("UTF-8");

      Writer writer = res.getWriter();
      final boolean searchInProgress = newSearchInProgress || searchContextExists;
      if (kmeliaSC.isRightsOnTopicsEnabled() && !kmeliaSC.isCurrentTopicAvailable()) {
        writer.write("<div class=\"inlineMessage-nok\">");
        writer.write(resources.getString("GML.ForbiddenAccessContent"));
        writer.write("</div>");
      } else if (NodePK.ROOT_NODE_ID.equals(kmeliaSC.getCurrentFolderId()) && kmeliaSC.
          getNbPublicationsOnRoot() != 0 && kmeliaSC.isTreeStructure() && !searchInProgress) {
        try {
          displayLastPublications(kmeliaSC, resources, writer);
        } catch (IOException e) {
          SilverLogger.getLogger(this).error(e);
        }
      } else {
        if (publications != null) {
          displayPublications(publications, sortAllowed, linksAllowed, seeAlso, searchInProgress,
              kmeliaSC, role, gef, resources, selectedIds, pubIdToHighlight, writer,
              attachmentToLink);
        }
      }
    }
  }

  private void displayPublications(List<KmeliaPublication> allPubs, boolean sortAllowed,
      boolean linksAllowed, boolean seeAlso, boolean toSearch, KmeliaSessionController kmeliaScc,
      String profile, GraphicElementFactory gef, MultiSilverpeasBundle resources,
      List<PublicationPK> selectedIds, String pubIdToHighlight, Writer out, boolean linkAttachment)
      throws IOException {

    String publicationSrc = resources.getIcon("kmelia.publication");
    SettingBundle publicationSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");
    boolean showNoPublisMessage = resources.getSetting("showNoPublisMessage", true);
    boolean targetValidationEnabled =
        kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable();

    String language = kmeliaScc.getCurrentLanguage();
    String currentUserId = kmeliaScc.getUserDetail().getId();
    String currentTopicId = kmeliaScc.getCurrentFolderId();

    // check if this instance use a custom template
    boolean specificTemplateUsed = kmeliaScc.isCustomPublicationTemplateUsed();

    PublicationFragmentSettings fragmentSettings = new PublicationFragmentSettings();
    fragmentSettings.displayLinks = URLUtil.displayUniversalLinks();
    fragmentSettings.showImportance = kmeliaScc.isFieldImportanceVisible();
    fragmentSettings.fileStorageShowExtraInfoPub =
        resources.getSetting("fileStorageShowExtraInfoPub", false);
    fragmentSettings.showTopicPathNameinSearchResult =
        resources.getSetting("showTopicPathNameinSearchResult", true);
    fragmentSettings.toSearch = toSearch;
    fragmentSettings.rateable = kmeliaScc.isPublicationRatingAllowed();

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
      displayPublicationsListHeader(allPubs, sortAllowed, pagination, resources, kmeliaScc, out);
      out.write("<ul>");
      for (KmeliaPublication aPub : pubs) {
        PublicationDetail pub = aPub.getDetail();
        User currentUser = aPub.getCreator();

        String pubColor = "";
        String pubState = null;
        String highlightClass = "";

        if (StringUtil.isDefined(pubIdToHighlight) &&
            pubIdToHighlight.equals(pub.getPK().getId())) {
          highlightClass = "highlight";
        }

        if (pub.getStatus() != null && pub.isValid()) {
          if (pub.haveGotClone() && CLONE_STATUS.equals(pub.getCloneStatus()) && !USER.isInRole(profile)) {
            pubColor = "blue";
            pubState = resources.getString("kmelia.UpdateInProgress");
          } else if (DRAFT_STATUS.equals(pub.getCloneStatus())) {
            if (currentUserId.equals(currentUser.getId())) {
              pubColor = "gray";
              pubState = resources.getString("PubStateDraft");
            }
          } else if (TO_VALIDATE_STATUS.equals(pub.getCloneStatus())) {
            if (ADMIN.isInRole(profile) || PUBLISHER.isInRole(profile) ||
                currentUserId.equals(currentUser.getId())) {
              pubColor = "red";
              pubState = resources.getString("kmelia.PubStateToValidate");
              if (targetValidationEnabled) {
                ValidatorsList validatorsList = aPub.getValidators();
                pubState = getTargetedValidationInfo(validatorsList, resources);
              }
            }
          } else {
            if (pub.isNotYetVisible()) {
              pubState = resources.getString("kmelia.VisibleFrom") + " " +
                  resources.getOutputDateAndHour(pub.getBeginDateAndHour());
            } else if (pub.isNoMoreVisible()) {
              pubState = resources.getString("kmelia.VisibleTo") + " " +
                  resources.getOutputDateAndHour(pub.getEndDateAndHour());
            }
            if (!pub.isVisible()) {
              pubColor = "gray";
            }
          }
        } else {
          boolean hasModificationAccess = ADMIN.isInRole(profile) || PUBLISHER.isInRole(profile) ||
              pub.isPublicationEditor(currentUserId) ||
              (!USER.isInRole(profile) && kmeliaScc.isCoWritingEnable());
          if (pub.getStatus() != null && pub.isDraft()) {
            // en mode brouillon, si on est en co-rédaction et si on autorise
            // le mode brouillon visible par tous,
            // les publication en mode brouillon sont visibles par tous sauf les
            // lecteurs sinon, seules les publications brouillons de l'utilisateur sont visibles
            if (pub.isPublicationEditor(currentUserId) ||
                ((kmeliaScc.isCoWritingEnable() && kmeliaScc.isDraftVisibleWithCoWriting()) &&
                    !USER.isInRole(profile))) {
              pubColor = "gray";
              pubState = resources.getString("PubStateDraft");
            }
          } else if (pub.getStatus() != null && pub.isRefused()) {
            if (ADMIN.isInRole(profile) || PUBLISHER.isInRole(profile) ||
                (WRITER.isInRole(profile) &&
                    (pub.isPublicationEditor(currentUserId) || kmeliaScc.isCoWritingEnable()))) {
              pubColor = "red";
              pubState = resources.getString("PublicationRefused");
            }
          } else if (hasModificationAccess) {
            // si on est en co-rédaction, on affiche toutes les publications
            // à valider (sauf pour les lecteurs)
            pubColor = "red";
            if (pub.isRefused()) {
              pubState = resources.getString("kmelia.PubStateUnvalidate");
            } else {
              pubState = resources.getString("kmelia.PubStateToValidate");
              if (targetValidationEnabled) {
                ValidatorsList validatorsList = aPub.getValidators();
                pubState = getTargetedValidationInfo(validatorsList, resources);
              }
            }
          }
        }

        if (pub.isAlias()) {
          pubState = resources.getString("kmelia.Shortcut");
        }

        String cssClasses = "important" + pub.getImportance();

        if (pub.isNew()) {
          cssClasses += " new-contribution";
        }

        if (toSearch) {
          if (aPub.isRead()) {
            cssClasses += " read";
          } else {
            cssClasses += " unread";
          }
        }

        out.write("<li class=\"");
        out.write(cssClasses);
        out.write("\" onmouseover=\"showPublicationOperations(this);\"");
        out.write(" onmouseout=\"hidePublicationOperations(this);\">");

        out.write("<div class=\"firstColumn\">");
        if (!kmeliaScc.getUserDetail().isAnonymous() && !kmeliaScc.isKmaxMode()) {
          String checked = "";
          if (selectedIds != null && selectedIds.contains(pub.getPK())) {
            checked = "checked=\"checked\"";
          }
          out.write("<span class=\"selection\">");
          out.write("<input type=\"checkbox\" name=\"C1\" value=\"" + pub.getPK().getId() + "-" +
              pub.getPK().getInstanceId() + "\" " + checked +
              " onclick=\"sendPubId(this.value, this.checked);\"/>");
          out.write("</span>");
        }
        if (!seeAlso) {
          Thumbnail thumbnail = pub.getThumbnail();
          if (thumbnail != null && Boolean.valueOf(resources.getSetting("isVignetteVisible"))) {
            out.write("<span class=\"thumbnail\">");
            displayThumbnail(pub, kmeliaScc, publicationSettings, out);
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
        out.write(getPutIntoBasketSelectionHtmlSnippet(
            String.format("putPublicationInBasket('%s')", pub.getIdentifier().asString()),
            language));
        out.write("</li>");
      }
      out.write("</ul>");

      out.write("<div id=\"pagination\">");
      out.write(pagination.printIndex("doPagination", true));
      out.write("</div>");

      displayFilePreviewJavascript(kmeliaScc.getComponentId(), language, out);
      displayFileViewJavascript(kmeliaScc.getComponentId(), language, out);
      out.write(board.printAfter());
    } else if (showNoPublisMessage) {
      String noPublications = kmeliaScc.getString("PubAucune");
      if (toSearch) {
        noPublications = kmeliaScc.getString("NoPubFound");
      }
      out.write("<div class=\"tableBoard\" id=\"noPublicationMessage\">");
      out.write("<div id=\"pubsHeader\"><img src=\"" + publicationSrc + "\" border=\"0\" /> ");
      out.write(
          "<span>" + resources.getString("GML.publications") + "</span></div>");
      out.write("<p>" + noPublications + "</p>");
      out.write("</div>");
    }
    out.write("</form>");
    out.write(scriptContent("sp.selection.newCheckboxMonitor('form[name=publicationsForm] input[name=C1]')" +
        ".addEventListener('change', function(){" +
        "if(typeof showPublicationCheckedBoxes === 'function') {showPublicationCheckedBoxes();}" +
        "}, 'displayPublication');").toString());
  }

  void displayFilePreviewJavascript(String componentId, final String contentLanguage, Writer out)
      throws IOException {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">");
    sb.append("function previewFile(target, attachmentId) {");
    sb.append("$(target).preview(\"previewAttachment\", {");
    sb.append("componentInstanceId: \"").append(componentId).append("\",");
    sb.append("attachmentId: attachmentId,");
    sb.append("lang: '" + contentLanguage + "'");
    sb.append("});");
    sb.append("return false;");
    sb.append("}");
    sb.append("</script>");
    out.write(sb.toString());
  }

  void displayFileViewJavascript(String componentId, final String contentLanguage, Writer out)
      throws IOException {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">");
    sb.append("function viewFile(target, attachmentId) {");
    sb.append("$(target).view(\"viewAttachment\", {");
    sb.append("componentInstanceId: \"").append(componentId).append("\",");
    sb.append("attachmentId: attachmentId,");
    sb.append("lang: '" + contentLanguage + "'");
    sb.append("});");
    sb.append("return false;");
    sb.append("}");
    sb.append("</script>");
    out.write(sb.toString());
  }

  void displayFragmentOfPublication(boolean specificTemplateUsed, KmeliaPublication aPub,
      PublicationFragmentSettings fragmentSettings, String language, String userId, String topicId,
      KmeliaSessionController kmeliaScc, MultiSilverpeasBundle resources, Writer out)
      throws IOException {

    // check if publication is draggable
    boolean canBeCut = KmeliaPublicationHelper
        .isCanBeCut(kmeliaScc.getComponentId(), userId, kmeliaScc.getUserTopicProfile(),
            aPub.getCreator());
    boolean alias = aPub.isAlias();
    fragmentSettings.draggable = canBeCut && !alias && !KmeliaHelper.isToValidateFolder(topicId);

    if (specificTemplateUsed) {
      displayTemplatedFragmentOfPublication(aPub, fragmentSettings, language, kmeliaScc, resources,
          out);
    } else {
      displayDefaultFragmentOfPublication(aPub, fragmentSettings, language, kmeliaScc, resources,
          out);
    }
  }

  void displayTemplatedFragmentOfPublication(KmeliaPublication aPub,
      PublicationFragmentSettings fragmentSettings, String language,
      KmeliaSessionController kmeliaScc, MultiSilverpeasBundle resources, Writer out)
      throws IOException {
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
    PublicationDetail pub = aPub.getDetail();
    String name = pub.getName(language);
    String description = pub.getDescription(language);

    template.setAttribute("publication", pub);
    template.setAttribute("link", "javascript:onClick=publicationGoTo('" + pub.getId() + "')");
    template.setAttribute("name", Encode.forHtml(name));
    template.setAttribute("description", WebEncodeHelper.convertBlanksForHtml(Encode.
        forHtml(description)));
    template.setAttribute("showDescription",
        StringUtil.isDefined(description) && !description.equals(name));
    template.setAttribute("importance", pub.getImportance());
    template.setAttribute("showImportance",
        fragmentSettings.showImportance && !fragmentSettings.linkAttachment);
    template.setAttribute("date", displayDate(pub, kmeliaScc, resources));
    template.setAttribute("creationDate", resources.getOutputDate(pub.getCreationDate()));
    template.setAttribute("updateDate", resources.getOutputDate(pub.getLastUpdateDate()));
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
    template.setAttribute("showPermalink",
        fragmentSettings.displayLinks && !fragmentSettings.seeAlso &&
            !fragmentSettings.linkAttachment);
    template.setAttribute("status", fragmentSettings.pubState);
    template.setAttribute("statusColor", fragmentSettings.pubColor);
    template.setAttribute("highlightClass", fragmentSettings.highlightClass);
    template.setAttribute("showRef",
        fragmentSettings.seeAlso && resources.getSetting("linkManagerShowPubId", false));
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
        displayFiles(pub, fragmentSettings.linkAttachment, fragmentSettings.seeAlso, kmeliaScc,
            resources));

    if (!"0".equals(pub.getInfoId())) {
      template.setAttribute("formName", pub.getInfoId());
      template.setAttribute("form", pub.getFormValues(language));
    }

    String fragment = template
        .applyFileTemplateOnComponent("kmelia", kmeliaScc.getCustomPublicationTemplateName());
    out.write(fragment);
  }

  void displayDefaultFragmentOfPublication(KmeliaPublication aPub,
      PublicationFragmentSettings fragmentSettings, String language,
      KmeliaSessionController kmeliaScc, MultiSilverpeasBundle resources, Writer out)
      throws IOException {
    PublicationDetail pub = aPub.getDetail();
    String name = Encode.forHtml(pub.getName(language));
    out.write("<div class=\"publication-name line1\">");
    if (fragmentSettings.linksAllowed) {
      out.write("<div class=\"");
      out.write(fragmentSettings.pubColor);
      out.write("\"><a href=\"javascript:onClick=publicationGoTo('");
      out.write(pub.getPK().getId());
      out.write("')\"><span class=\"" + fragmentSettings.highlightClass + "\">");
      if (fragmentSettings.draggable) {
        out.write("<span class=\"jstree-draggable\" id=\"pub-" + pub.getPK().getId() + "\">");
        out.write(name);
        out.write("</span>");
      } else {
        out.write(name);
      }
      out.write("</span></a></div>");
    } else {
      String ref = "";
      if (fragmentSettings.seeAlso && resources.getSetting("linkManagerShowPubId", false)) {
        ref = " [ " + pub.getPK().getId() + " ] ";
      }
       out.write("<div class=\"");
      out.write(fragmentSettings.pubColor);
      out.write("\"><span class=\"" + fragmentSettings.highlightClass + "\">");
      out.write(ref);
      out.write(name);
      out.write("</span></div>");
    }
    out.write("&#160;");
    if (StringUtil.isDefined(fragmentSettings.pubState)) {
      out.write("<span class=\"state ");
      out.write(fragmentSettings.pubColor);
      out.write("\">(");
      out.write(fragmentSettings.pubState);
      out.write(")</span>");
    }

    if (fragmentSettings.rateable) {
      RaterRatingEntity raterRatingEntity = RaterRatingEntity.fromRateable(pub);
      out.write(raterRatingEntity
          .toJSonScript("raterRatingEntity_" + raterRatingEntity.getContributionId()));
      out.write(
          "<div silverpeas-rating readonly=\"true\" shownbraterratings=\"false\" " +
              "starsize=\"small\" " +
              "raterrating=\"raterRatingEntity_" + raterRatingEntity.getContributionId() +
              "\"></div>");
    }

    out.write("</div>");
    out.write("<div class=\"line2 ");
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
    if (fragmentSettings.displayLinks && !fragmentSettings.seeAlso &&
        !fragmentSettings.linkAttachment) {
      out.write("<span class=\"permalink\">");
      out.write(displayPermalink(pub, kmeliaScc, resources));
      out.write("</span>");
    }
    out.write("</div>");

    String description = pub.getDescription(language);
    // displays publication description
    if (StringUtil.isDefined(description) && !description.equals(name)) {
      out.write("<p class=\"description line3\">");
      out.write(WebEncodeHelper.convertBlanksForHtml(Encode.forHtml(description)));
      out.write("</p>");
    }

    out.write(
        displayFiles(pub, fragmentSettings.linkAttachment, fragmentSettings.seeAlso, kmeliaScc,
            resources));
  }

  String displayDate(PublicationDetail pub, KmeliaSessionController kmeliaScc,
      MultiSilverpeasBundle resources) {
    if ("5".equals(kmeliaScc.getSortValue()) || "6".equals(kmeliaScc.getSortValue())) {
      return resources.getOutputDate(pub.getCreationDate());
    } else {
      return resources.getOutputDate(pub.getLastUpdateDate());
    }
  }

  String displayFiles(PublicationDetail pub, boolean linkAttachment, boolean seeAlso,
      KmeliaSessionController kmeliaScc, MultiSilverpeasBundle resources) {
    StringBuilder sb = new StringBuilder(1024);
    boolean displayFiles =
        (KmeliaHelper.isToolbox(kmeliaScc.getComponentId()) || kmeliaScc.attachmentsInPubList()) &&
            !seeAlso || linkAttachment;
    if (displayFiles) {
      sb.append("<span class=\"files\">");
      sb.append(displayAttachments(kmeliaScc, pub, resources, linkAttachment));

      sb.append("</span>");
    }
    return sb.toString();
  }

  private void displayThumbnail(PublicationDetail pub, KmeliaSessionController ksc,
      SettingBundle publicationSettings, Writer out) throws IOException {
    ThumbnailSettings thumbnailSettings = ksc.getThumbnailSettings();
    String vignetteUrl;
    String width = String.valueOf(thumbnailSettings.getWidth());
    String height = String.valueOf(thumbnailSettings.getHeight());

    if (pub.getImage().startsWith("/")) {
      vignetteUrl = pub.getImage();
    } else {
      vignetteUrl = FileServerUtils.getUrl(pub.getPK().
              getComponentName(), "vignette", pub.getImage(), pub.getImageMimeType(),
          publicationSettings.getString("imagesSubDirectory"));
      if (pub.getThumbnail().isCropped()) {
        // thumbnail is cropped, no resize
        width = null;
        height = null;
      }
    }

    ImageTag imageTag = new ImageTag();
    imageTag.setSrc(vignetteUrl);
    imageTag.setType("vignette");
    String size = defaultStringIfNotDefined(width) + "x" + defaultStringIfNotDefined(height);
    if (!"x".equals(size)) {
      imageTag.setSize(size);
    }
    out.write(imageTag.toString());
  }

  String displayPermalink(PublicationDetail pub, KmeliaSessionController kmeliaScc,
      MultiSilverpeasBundle resources) {
    String link;
    if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
      link = URLUtil
          .getSimpleURL(URLUtil.URL_PUBLI, pub.getPK().getId(), kmeliaScc.getComponentId());
    } else {
      link = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pub.getPK().getId());
    }
    return " - <a class=\"sp-permalink\" href=\"" + link + "\"><img src=\"" + resources.getIcon("kmelia.link") +
        "\" border=\"0\" align=\"absmiddle\" alt=\"" +
        resources.getString("kmelia.CopyPublicationLink") + "\" title=\"" +
        resources.getString("kmelia.CopyPublicationLink") + "\"/></a>";
  }

  void displaySortingListBox(MultiSilverpeasBundle resources, KmeliaSessionController ksc, Writer out)
      throws IOException {

    boolean manualSort = ksc.getSort().getCurrentSort() == SORT_MANUAL;

    out.
        write(
            "<select name=\"sortBy\" id=\"sortingList\" onChange=\"javascript:sortGoTo(this" +
                ".selectedIndex);\">");
    out.write("<option>" + resources.getString("SortBy") + "</option>");
    out.write("<option>-------------------------------</option>");
    out.write(getSortingListBoxEntry(SORT_UPDATE_ASC, resources.getString("DateAsc"), ksc));
    out.write(getSortingListBoxEntry(SORT_UPDATE_DESC, resources.getString("DateDesc"), ksc));
    out.write(getSortingListBoxEntry(SORT_CREATION_ASC, resources.getString("CreateDateAsc"), ksc));
    out.write(getSortingListBoxEntry(SORT_CREATION_DESC, resources.getString("CreateDateDesc"), ksc));
    out.write(getSortingListBoxEntry(SORT_CREATOR_ASC, resources.getString("PubAuteur"), ksc));
    if (ksc.isFieldImportanceVisible()) {
      out.write(getSortingListBoxEntry(SORT_IMPORTANCE_ASC, resources.getString("PubImportance"), ksc));
    }
    out.write(getSortingListBoxEntry(SORT_TITLE_ASC, resources.getString("PubTitre"), ksc));
    out.write(getSortingListBoxEntry(SORT_DESCRIPTION_ASC, resources.getString("PubDescription"), ksc));

    if (manualSort) {
      out.write(
          getSortingListBoxEntry(SORT_MANUAL, resources.getString("kmelia.sort.manual"), ksc));
    }
    out.write("</select>");

    if (manualSort && SilverpeasRole.ADMIN == ksc.getHighestSilverpeasUserRole()) {
      // Display link to reset manual sort
      Img img = new Img();
      img.setSrc(resources.getIcon("kmelia.delete"));

      A resetSort = new A();
      resetSort.setHref("#");
      resetSort.setOnClick("resetSort()");
      resetSort.setID("resetSort");
      resetSort.addElement(img);
      resetSort.setTitle(resources.getString("kmelia.sort.manual.reset"));

      out.write(resetSort.toString());
    }
  }

  private String getSortingListBoxEntry(int value, String label, KmeliaSessionController ksc) {
    Option option = new Option(label, value);
    option.setID("sort"+value);
    option.setSelected(isSelectedSort(ksc, value));
    return option.toString();
  }

  private boolean isSelectedSort(KmeliaSessionController ksc, int sort) {
    final int currentSort = ksc.getSearchContext() != null ?
        ksc.getSearchContext().getSortValue() :
        ksc.getSortValue();
    return sort == currentSort;
  }

  private void displayPublicationsListHeader(List<KmeliaPublication> allPubs, boolean sortAllowed,
      Pagination pagination, MultiSilverpeasBundle resources, KmeliaSessionController ksc,
      Writer out) throws IOException {
    String publicationSrc = resources.getIcon("kmelia.publication");
    out.write("<div id=\"pubsHeader\">");
    Img img = new Img(publicationSrc).setAlt("");
    out.write(img.toString());
    out.write("<span id=\"pubsCounter\">");
    out.write("<span>" + pagination.printCounter() + "</span> ");
    if (allPubs.size() > 1) {
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
    User currentUser = userPub.getCreator();
    PublicationDetail pub = userPub.getDetail();
    String updaterId = pub.getUpdaterId();
    User updater = null;
    if (updaterId != null && updaterId.length() > 0) {
      updater = kmeliaScc.getUserDetail(updaterId);
    }
    if (updater == null) {
      updater = currentUser;
    }
    if (updater != null && (StringUtil.isDefined(updater.getFirstName()) ||
        StringUtil.isDefined(updater.getLastName()))) {
      return UserNameGenerator.toString(updater, kmeliaScc.getUserId());
    }
    return kmeliaScc.getString("kmelia.UnknownUser");
  }

  @SuppressWarnings("unchecked")
  private List<PublicationPK> processPublicationsToLink(HttpServletRequest request) {
    // get from session the list of publications to link with current publication
    HashSet<String> list = (HashSet<String>) request.getSession()
        .getAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
    // store the publication identifiers in an array list
    List<PublicationPK> publicationsToLink = new ArrayList<>();
    if (list != null) {
      for (String link : list) {
        String[] tokens = StringUtil.splitByWholeSeparator(link, "-");
        publicationsToLink.add(new PublicationPK(tokens[0], tokens[1]));
      }
    }
    return publicationsToLink;
  }

  private String displayAttachments(final KmeliaSessionController kmeliaScc,
      PublicationDetail pubDetail, MultiSilverpeasBundle resources, boolean linkAttachment) {
    ResourceReference resourceReference = new ResourceReference(pubDetail.getPK());
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(resourceReference, kmeliaScc.getCurrentLanguage());
    StringBuilder result = new StringBuilder(documents.size() * 256);
    boolean hasDisplayableAttachments = false;
    for (SimpleDocument document : documents) {
      SimpleDocument attachment = document.getLastPublicVersion();
      if (attachment != null) {
        if (!hasDisplayableAttachments) {
          result.append("<table border=\"0\">");
          hasDisplayableAttachments = true;
        }
        String logicalName = attachment.getFilename();
        String title = attachment.getTitle();
        if (!StringUtil.isDefined(attachment.getTitle())) {
          title = logicalName;
          // do not display filename twice
          logicalName = null;
        }
        if (attachment.isVersioned()) {
          title += " v" + attachment.getMajorVersion();
        }
        String icon = FileRepositoryManager.getFileIcon(FilenameUtils.getExtension(attachment.
            getFilename()));
        String size = FileRepositoryManager.formatFileSize(attachment.getSize());
        String downloadTime = FileRepositoryManager.getFileDownloadTime(attachment.getSize());
        String permalink = URLUtil.getSimpleURL(URLUtil.URL_FILE, document.getId());
        String url = FileServerUtils.getApplicationContext() + attachment.getAttachmentURL();
        boolean previewable = ViewerProvider.isPreviewable(attachment.getAttachmentPath());
        boolean viewable = ViewerProvider.isViewable(attachment.getAttachmentPath());
        result.append(
            displayFile(url, title, Encode.forHtml(attachment.getDescription()), icon, logicalName,
                size, downloadTime, attachment.getCreationDate(), permalink, resources, linkAttachment,
                previewable, viewable, attachment.isDownloadAllowedForReaders(),
                attachment.isDownloadAllowedForRolesFrom(kmeliaScc.getUserDetail()),
                attachment.getPk().getId()));
      }
    }
    if (hasDisplayableAttachments) {
      result.append("</table>");
    }
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
   * @param isDownloadAllowedForReaders
   * @param isUserAllowedToDownloadFile
   * @param id
   * @return
   */
  private String displayFile(String url, String title, String info, String icon, String logicalName,
      String size, String downloadTime, Date creationDate, String permalink,
      MultiSilverpeasBundle resources, boolean attachmentLink, boolean previewable, boolean viewable,
      final boolean isDownloadAllowedForReaders, final boolean isUserAllowedToDownloadFile,
      String id) {
    StringBuilder result = new StringBuilder(1024);

    if (!attachmentLink) {
      String link = isUserAllowedToDownloadFile ? "<a href=\"" + url + "\" target=\"_blank\">" :
          "<span class=\"forbidden-download\">";
      result.append("<tr><td valign=\"top\">");
      // Add doc type icon
      Img iconImg = new Img(icon).setAlignment(Alignment.MIDDLE);
      result.append(link).append(iconImg.toString()).append("</a>&#160;</td>");
      result.append("<td valign=\"top\">").append(link);
      boolean showTitle = resources.getSetting("showTitle", true);
      String fileTitle = StringUtil.isDefined(title) ? title : logicalName;
      if (StringUtil.isDefined(fileTitle) && showTitle) {
        result.append(fileTitle);
      }
      result.append(isUserAllowedToDownloadFile ? "</a>" : "</span>");

      if (StringUtil.isDefined(permalink) && isUserAllowedToDownloadFile) {
        result.append("&#160;<a href=\"").append(permalink)
            .append("\" target=\"_blank\"><img src=\"").append(resources.getIcon("kmelia.link"))
            .append("\" border=\"0\" valign=\"absmiddle\" alt=\"").append("\" title=\"")
            .append("\"/></a>");
      }

      result.append("<br/>");

      result.append("<i>");
      if (StringUtil.isDefined(logicalName) && (!logicalName.equals(fileTitle) || !showTitle)) {
        result.append(logicalName).append(" / ");
      }
      // Add file size
      if (resources.getSetting("showFileSize", true)) {
        result.append(size);
      }
      // and download estimation
      if (resources.getSetting("showDownloadEstimation", false)) {
        result.append(" / ").append(downloadTime).append(" / ")
            .append(resources.getOutputDate(creationDate));
      }
      if (previewable) {
        result.append(" <img onclick=\"javascript:previewFile(this, '").append(id)
            .append("');\" class=\"preview-file\" src=\"")
            .append(resources.getIcon("kmelia.file.preview")).append("\" alt=\"")
            .append(resources.getString("GML.preview.file")).append("\" title=\"")
            .append(resources.getString("GML.preview.file")).append("\"/>");
      }
      if (viewable) {
        result.append(" <img onclick=\"javascript:viewFile(this, '").append(id)
            .append("');\" class=\"view-file\" src=\"")
            .append(resources.getIcon("kmelia.file.view")).append("\" alt=\"")
            .append(resources.getString("GML.view.file")).append("\" title=\"")
            .append(resources.getString("GML.view.file")).append("\"/>");
      }
      if (!isDownloadAllowedForReaders) {
        String forbiddenDownloadHelp =
            isUserAllowedToDownloadFile ? resources.getString("GML.download.forbidden.readers") :
                resources.getString("GML.download.forbidden");
        result.append(" <img class=\"forbidden-download-file\" src=\"")
            .append(resources.getIcon("kmelia.file.forbidden-download")).append("\" alt=\"")
            .append(forbiddenDownloadHelp).append("\" title=\"").append(forbiddenDownloadHelp)
            .append("\"/>");
      }
      result.append("</i>");

      // Add info
      if (StringUtil.isDefined(info) && resources.getSetting("showInfo", true)) {
        result.append("<br/>").append(WebEncodeHelper.convertBlanksForHtml(info));
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
      String javascriptFunction =
          "selectAttachment('" + url + "','" + icon + "','" + displayedTitle + "')";
      String link = "<a href=\"javascript:" + javascriptFunction + "\" >";
      result.append("<tr><td valign=\"top\">");

      // Add doc type icon
      result.append(link).append("<img src=\"").append(icon)
          .append("\" border=\"0\" align=\"absmiddle\"/></a>&#160;</td>");
      result.append("<td valign=\"top\">").append(link);

      // inserts label (attachment title or logical file name)
      result.append(displayedTitle);
      result.append("</a>");

      // inserts permalink
      if (StringUtil.isDefined(permalink)) {
        result.append("&#160;<a href=\"").append(permalink)
            .append("\" target=\"_blank\"><img src=\"").append(resources.getIcon("kmelia.link"))
            .append("\" border=\"0\" valign=\"absmiddle\" alt=\"")
            .append(resources.getString("kmelia.CopyFileLink")).append("\" title=\"")
            .append(resources.getString("kmelia.CopyFileLink")).append("\"/></a>");
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
        result.append(" / ").append(downloadTime).append(" / ")
            .append(resources.getOutputDate(creationDate));
        result.append("</i>");
        // Add info
        if (StringUtil.isDefined(info)) {
          result.append("<br/>").append(WebEncodeHelper.javaStringToHtmlParagraphe(info));
        }
      }

    }

    return result.toString();

  }

  private void displayLastPublications(KmeliaSessionController kmeliaScc,
      MultiSilverpeasBundle resources, Writer writer) throws IOException {

    List<KmeliaPublication> pubs = kmeliaScc.getLatestPublications();
    boolean displayLinks = URLUtil.displayUniversalLinks();
    String language = kmeliaScc.getCurrentLanguage();

    Iterator<KmeliaPublication> iterator = pubs.iterator();

    writer.write("<div class=\"tableBoard\" id=\"latestPublications\">");

    Img img = new Img(resources.getIcon("kmelia.publication"));
    writer.write("<div id=\"pubsHeader\">"+img.toString());
    writer.write("<b>" + kmeliaScc.getString("PublicationsLast") +
        "</b></div>");

    if (iterator.hasNext()) {
      writer.write("<ul class=\"list-publication-home\">");
      String linkIcon = resources.getIcon("kmelia.link");
      while (iterator.hasNext()) {
        KmeliaPublication kmeliaPub = iterator.next();
        PublicationDetail pub = kmeliaPub.getDetail();
        String shortcut;
        if (pub.isAlias()) {
          shortcut = " (" + resources.getString("kmelia.Shortcut") + ")";
        } else {
          shortcut = "";
        }

        final String liClass = Optional.of(" class=\"new-contribution\"")
            .filter(s -> pub.isNew())
            .orElse(EMPTY);
        writer.write("<li" + liClass + ">");
        writer.write("<div class=\"publication-name line1\"><a class=\"sp-permalink\"" +
            " href=\"" + pub.getPermalink() +"\">" + Encode.forHtml(pub.getName(language)) + "</a>" + shortcut + "</div>");

        if (kmeliaScc.showUserNameInList()) {
          writer.write("<span class=\"publication-user\">");
          writer.write(getUserName(kmeliaPub, kmeliaScc));
          writer.write("</span>");
        }
        writer.write("<span class=\"publication-date\">"+ TemporalFormatter.toLocalizedDate(pub.getVisibility().getPeriod().getStartDate(), kmeliaScc.getZoneId(), kmeliaScc.getLanguage()) +"</span>");
        if (displayLinks) {
          String link = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pub.getPK().getId());
          writer.write("<a class=\"sp-permalink publication-hyperlink\" href=\"" + link + "\"><img src=\"" + linkIcon +
              "\"  alt=\"" +
              resources.getString("kmelia.CopyPublicationLink") + "\" title=\"" +
              resources.getString("kmelia.CopyPublicationLink") + "\" /></a>");
        }
        writer.write("<p class=\"publication-description\">"+WebEncodeHelper.convertBlanksForHtml(Encode.forHtml(pub.
        getDescription(language))));
        writer.write("</p>");
        writer.write("</li>");
      }
      
      writer.write("</ul>");
    }

    writer.write("</div>");
  }

  private String displayPublicationFullPath(KmeliaSessionController kmelia, PublicationDetail pub) {
    // Get space and componentLabel of the publication (can be different from context)
    OrganizationController orga = kmelia.getOrganisationController();
    ComponentInstLight compoInstLight = orga.getComponentInstLight(pub.getInstanceId());
    String componentLabel = compoInstLight.getLabel(kmelia.getCurrentLanguage());
    String spaceLabel = Encode.forHtml(
        orga.getSpaceInstLightById(compoInstLight.getDomainFatherId())
            .getName(kmelia.getCurrentLanguage()));
    return PublicationService.get().getMainLocation(pub.getPK())
        .map(m -> {
          String topicPathName = spaceLabel + " > " + componentLabel + " > " + kmelia.displayPath(
              kmelia.getKmeliaService().getPath(m.getId(), m.getInstanceId()), false, 3);
          return "<div class=\"publiPath\">" + topicPathName + "</div>";
        })
        .orElse(StringUtil.EMPTY);
  }

  private String getTargetedValidationInfo(ValidatorsList validatorsList,
      MultiSilverpeasBundle resources) {
    if (validatorsList.isAtLeastOnceValidatorActive()) {
      return resources.getStringWithParams("kmelia.PubStateToValidateBy",
          validatorsList.getValidatorNames());
    } else {
      return resources.getString("kmelia.publication.validators.nomore");
    }
  }

  private void initSearch(QueryDescription queryDescription, PagesContext formContext,
      HttpServletRequest req, KmeliaSessionController kmeliaSC) {
    Form searchForm = kmeliaSC.getXmlFormSearchForPublications();
    if (searchForm != null) {
      List<FieldTemplate> fields = searchForm.getFieldTemplates();
      for(FieldTemplate field : fields) {
        String fieldName = field.getFieldName();
        String[] fieldValues = req.getParameterValues(fieldName);
        if (ArrayUtil.isNotEmpty(fieldValues)) {

          String fieldValue = fieldValues[0];
          if (fieldValues.length > 1) {
            String operator = req.getParameter(fieldName + "Operator");
            formContext.setSearchOperator(fieldName, operator);
            fieldValue = StringUtils.join(fieldValues, " "+operator+" ");
          }

          queryDescription.addFieldQuery(
              new FieldDescription(searchForm.getFormName() + "$$" + fieldName,
                  fieldValue, kmeliaSC.getLanguage()));
        }
      }
    }
  }
  
}
