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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.servlets.renderers;

import org.apache.commons.io.FilenameUtils;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Option;
import org.apache.ecs.wml.Alignment;
import org.apache.ecs.wml.Img;
import org.owasp.encoder.Encode;
import org.silverpeas.components.kmelia.KmeliaPublicationHelper;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.components.kmelia.model.ValidatorsList;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailSettings;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.viewer.service.ViewerProvider;
import org.silverpeas.core.web.util.viewgenerator.html.ImageTag;
import org.silverpeas.core.web.util.viewgenerator.html.UserNameGenerator;
import org.silverpeas.core.web.util.viewgenerator.html.board.Board;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;
import org.silverpeas.core.webapi.rating.RaterRatingEntity;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static org.silverpeas.components.kmelia.model.KmeliaPublicationSort.*;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;
import static org.silverpeas.core.contribution.publication.model.PublicationDetail.*;
import static org.silverpeas.core.web.selection.BasketSelectionUI.getPutIntoBasketSelectionHtmlSnippet;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.scriptContent;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * A renderer of a paginated list of publications.
 *
 * @author mmoquillon
 */
public class PublicationsRenderer implements Renderer {

  private static final String END_DIV = "</div>";
  private static final String END_SPAN = "</span>";
  private static final String NEW_LINE = "<br/>";
  private static final String SCRIPT_END = "</script>";
  private static final String SCRIPT_START = "<script>";
  private static final String KMELIA_PUBLICATION_ICON = "kmelia.publication";
  private static final String ALT = "\" alt=\"";
  private static final String TITLE_ATTR = "\" title=\"";
  private static final String ANCHOR_END = "\"/></a>";
  private static final String KMELIA_LINK_ICON = "kmelia.link";

  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");

  @Override
  public void render(Writer writer, RenderingContext ctx) throws IOException {
    var resources = ctx.getResources();
    var kmeliaScc = ctx.getSessionController();
    var gef = ctx.getGraphicElementFactory();
    String publicationSrc = resources.getIcon(KMELIA_PUBLICATION_ICON);
    boolean showNoPublisMessage = resources.getSetting("showNoPublisMessage", true);

    PublicationFragmentSettings fragmentSettings = initFragmentSettings(ctx);

    int nbPubsPerPage = kmeliaScc.getNbPublicationsPerPage();
    int firstDisplayedItemIndex = kmeliaScc.getIndexOfFirstPubToDisplay();
    var allPubs = ctx.getPublications();
    int nbPubs = allPubs.size();
    Pagination pagination = gef.getPagination(nbPubs, nbPubsPerPage, firstDisplayedItemIndex);
    List<KmeliaPublication> pubsToRender = allPubs.subList(pagination.getFirstItemIndex(),
        pagination.getLastItemIndex());

    writer.write("<form name=\"publicationsForm\" onsubmit=\"return false;\">");
    if (!pubsToRender.isEmpty()) {
      renderPublicationsPaginatedList(pubsToRender, writer, ctx, pagination, fragmentSettings);
    } else if (showNoPublisMessage) {
      String noPublications = kmeliaScc.getString("PubAucune");
      if (ctx.isSearchInProgress()) {
        noPublications = kmeliaScc.getString("NoPubFound");
      }
      writer.write("<div class=\"tableBoard\" id=\"noPublicationMessage\">");
      writer.write("<div id=\"pubsHeader\"><img src=\"" + publicationSrc + "\" border=\"0\" /> ");
      writer.write(
          "<span>" + resources.getString("GML.publications") + "</span></div>");
      writer.write("<p>" + noPublications + "</p>");
      writer.write(END_DIV);
    }
    writer.write("</form>");
    writer.write(scriptContent("sp.selection.newCheckboxMonitor('form[name=publicationsForm] " +
        "input[name=C1]')" +
        ".addEventListener('change', function(){" +
        "if(typeof showPublicationCheckedBoxes === 'function') {showPublicationCheckedBoxes();}" +
        "}, 'displayPublication');").toString());
  }

  private void renderPublicationsPaginatedList(List<KmeliaPublication> pubs, Writer writer,
      RenderingContext ctx, Pagination pagination, PublicationFragmentSettings fragmentSettings) throws IOException {
    Board board = ctx.getGraphicElementFactory().getBoard();
    writer.write(board.printBefore());

    renderPublicationsListHeader(writer, ctx, pagination);
    writer.write("<ul>");
    for (KmeliaPublication aPub : pubs) {
      renderPublication(aPub, writer, ctx, fragmentSettings);
    }
    writer.write("</ul>");

    writer.write("<div id=\"pagination\">");
    writer.write(pagination.printIndex("doPagination", true));
    writer.write(END_DIV);

    renderFilePreviewJavascript(writer, ctx);
    renderFileViewJavascript(writer, ctx);
    writer.write(board.printAfter());
  }

  private void renderPublication(KmeliaPublication aPub, Writer writer, RenderingContext ctx,
      PublicationFragmentSettings fragmentSettings) throws IOException {
    PublicationDetail pub = aPub.getDetail();
    String highlightClass = getHighlightClass(ctx, pub);

    var resources = ctx.getResources();
    var kmeliaScc = ctx.getSessionController();

    Pair<String, String> state = getPublicationState(aPub, ctx);
    String cssClasses = "important" + pub.getImportance();

    if (pub.isNew()) {
      cssClasses += " new-contribution";
    }

    if (ctx.isSearchInProgress()) {
      if (aPub.isRead()) {
        cssClasses += " read";
      } else {
        cssClasses += " unread";
      }
    }

    writer.write("<li class=\"");
    writer.write(cssClasses);
    writer.write("\" onmouseover=\"showPublicationOperations(this);\"");
    writer.write(" onmouseout=\"hidePublicationOperations(this);\">");

    writer.write("<div class=\"firstColumn\">");
    if (!kmeliaScc.getUserDetail().isAnonymous() && !kmeliaScc.isKmaxMode()) {
      String checked = "";
      if (ctx.getSelectedPublications() != null &&
          ctx.getSelectedPublications().contains(pub.getPK())) {
        checked = "checked=\"checked\"";
      }
      writer.write("<span class=\"selection\">");
      writer.write("<input type=\"checkbox\" name=\"C1\" value=\"" + pub.getPK().getId() + "-" +
          pub.getPK().getInstanceId() + "\" " + checked +
          " onclick=\"sendPubId(this.value, this.checked);\"/>");
      writer.write(END_SPAN);
    }
    if (!ctx.isSeeAlso()) {
      Thumbnail thumbnail = pub.getThumbnail();
      if (thumbnail != null && Boolean.parseBoolean(resources.getSetting("isVignetteVisible"))) {
        writer.write("<span class=\"thumbnail\">");
        renderPublicationThumbnail(pub, writer, ctx);
        writer.write(END_SPAN);
      }
    }
    writer.write(END_DIV);

    fragmentSettings.setPubColor(state.getSecond());
    fragmentSettings.setHighlightClass(highlightClass);
    fragmentSettings.setPubState(state.getFirst());
    fragmentSettings.setLinksAllowed(ctx.isLinksAllowed());
    fragmentSettings.setSeeAlso(ctx.isSeeAlso());
    fragmentSettings.setLinkAttachment(ctx.isAttachmentToLink());

    writer.write("<div class=\"publication\"><a name=\"" + pub.getPK().getId() + "\"></a>");
    renderPublicationFragment(aPub, writer, ctx, fragmentSettings);
    writer.write(END_DIV);
    writer.write(
        getPutIntoBasketSelectionHtmlSnippet(String.format("putPublicationInBasket('%s')",
            pub.getIdentifier().asString()), kmeliaScc.getCurrentLanguage()));
    writer.write("</li>");
  }

  private Pair<String, String> getPublicationState(KmeliaPublication aPub,
      RenderingContext ctx) {
    PublicationDetail pub = aPub.getDetail();
    var resources = ctx.getResources();
    Pair<String, String> state;
    if (pub.getStatus() != null && pub.isValid()) {
      state = computeValidPublicationState(aPub, ctx);
    } else {
      state = computePublicationState(aPub, ctx);
    }

    String pubState = state.getFirst();
    String pubColor = state.getSecond();
    if (pub.isAlias()) {
      pubState = resources.getString("kmelia.Shortcut");
    }

    return Pair.of(pubState, pubColor);
  }

  private Pair<String, String> computeValidPublicationState(KmeliaPublication aPub,
      RenderingContext ctx) {
    PublicationDetail pub = aPub.getDetail();
    String profile = ctx.getRole();
    var resources = ctx.getResources();
    var kmeliaScc = ctx.getSessionController();
    boolean targetValidationEnabled =
        kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable();
    User creator = aPub.getCreator();
    String pubColor = "";
    String pubState = null;
    var currentUserId = kmeliaScc.getUserDetail().getId();
    if (pub.haveGotClone() && CLONE_STATUS.equals(pub.getCloneStatus()) &&
        !USER.isInRole(profile)) {
      pubColor = "blue";
      pubState = resources.getString("kmelia.UpdateInProgress");
    } else if (DRAFT_STATUS.equals(pub.getCloneStatus())) {
      if (currentUserId.equals(creator.getId())) {
        pubColor = "gray";
        pubState = resources.getString("PubStateDraft");
      }
    } else if (TO_VALIDATE_STATUS.equals(pub.getCloneStatus())) {
      if (ADMIN.isInRole(profile) || PUBLISHER.isInRole(profile) ||
          currentUserId.equals(creator.getId())) {
        pubColor = "red";
        pubState = getPublicationStateByValidationStatus(aPub, resources, targetValidationEnabled);
      }
    } else {
      pubState = getPublicationStateByVisibility(pub, resources);
      if (!pub.isVisible()) {
        pubColor = "gray";
      }
    }
    return Pair.of(pubState, pubColor);
  }

  private String getPublicationStateByValidationStatus(KmeliaPublication aPub,
      MultiSilverpeasBundle resources, boolean targetValidationEnabled) {
    String pubState;
    pubState = resources.getString("kmelia.PubStateToValidate");
    if (targetValidationEnabled) {
      ValidatorsList validatorsList = aPub.getValidators();
      pubState = getTargetedValidationInfo(validatorsList, resources);
    }
    return pubState;
  }

  private static String getPublicationStateByVisibility(PublicationDetail pub,
      MultiSilverpeasBundle resources) {
    String pubState;
    if (pub.isNotYetVisible()) {
      pubState = resources.getString("kmelia.VisibleFrom") + " " +
          resources.getOutputDateAndHour(pub.getBeginDateAndHour());
    } else if (pub.isNoMoreVisible()) {
      pubState = resources.getString("kmelia.VisibleTo") + " " +
          resources.getOutputDateAndHour(pub.getEndDateAndHour());
    } else {
      pubState = null;
    }
    return pubState;
  }

  private Pair<String, String> computePublicationState(KmeliaPublication aPub,
      RenderingContext ctx) {
    PublicationDetail pub = aPub.getDetail();
    String profile = ctx.getRole();
    var resources = ctx.getResources();
    var kmeliaScc = ctx.getSessionController();
    boolean targetValidationEnabled =
        kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable();
    String currentUserId = kmeliaScc.getUserDetail().getId();
    boolean hasModificationAccess = ADMIN.isInRole(profile) || PUBLISHER.isInRole(profile) ||
        pub.isPublicationEditor(currentUserId) ||
        (!USER.isInRole(profile) && kmeliaScc.isCoWritingEnable());
    Pair<String, String> state = null;
    if (pub.getStatus() != null && pub.isDraft()) {
      state = getDraftPublicationState(aPub, ctx);
    } else if (pub.getStatus() != null && pub.isRefused()) {
      if (ADMIN.isInRole(profile) || PUBLISHER.isInRole(profile) ||
          (WRITER.isInRole(profile) &&
              (pub.isPublicationEditor(currentUserId) || kmeliaScc.isCoWritingEnable()))) {
        state = Pair.of(resources.getString("PublicationRefused"), "red");
      }
    } else if (hasModificationAccess) {
      // in co-redaction, all the publications to be validated are rendered (except for readers)
      String pubColor = "red";
      String pubState = getDefaultRWPublicationState(aPub, ctx, targetValidationEnabled);
      state = Pair.of(pubState, pubColor);
    }
    return state == null ? Pair.of(null, "") : state;
  }

  private String getDefaultRWPublicationState(KmeliaPublication aPub,
      RenderingContext ctx, boolean targetValidationEnabled) {
    PublicationDetail pub = aPub.getDetail();
    String pubState;
    var resources = ctx.getResources();
    if (pub.isRefused()) {
      pubState = resources.getString("kmelia.PubStateUnvalidate");
    } else {
      pubState = resources.getString("kmelia.PubStateToValidate");
      if (targetValidationEnabled) {
        ValidatorsList validatorsList = aPub.getValidators();
        pubState = getTargetedValidationInfo(validatorsList, resources);
      }
    }
    return pubState;
  }

  private Pair<String, String> getDraftPublicationState(KmeliaPublication aPub,
      RenderingContext ctx) {
    // in draft mode, in the case of a co-redaction or a draft mode visible to everyone, the
    // draft publications are visible for everyone except the readers. Otherwise, the
    // draft publications are only visible to their owner (creator)
    PublicationDetail pub = aPub.getDetail();
    String profile = ctx.getRole();
    var kmeliaScc = ctx.getSessionController();
    String currentUserId = kmeliaScc.getUserDetail().getId();
    if (pub.isPublicationEditor(currentUserId) ||
        ((kmeliaScc.isCoWritingEnable() && kmeliaScc.isDraftVisibleWithCoWriting()) &&
            !USER.isInRole(profile))) {
      return Pair.of(ctx.getResources().getString("PubStateDraft"), "gray");
    }
    return Pair.of(null, "");
  }

  private static String getHighlightClass(RenderingContext ctx, PublicationDetail pub) {
    String pubIdToHighlight = ctx.getPublicationToHighlight();
    String highlightClass = "";
    if (StringUtil.isDefined(pubIdToHighlight) &&
        pubIdToHighlight.equals(pub.getPK().getId())) {
      highlightClass = "highlight";
    }
    return highlightClass;
  }

  private static PublicationFragmentSettings initFragmentSettings(RenderingContext ctx) {
    var kmeliaScc = ctx.getSessionController();
    var resources = ctx.getResources();
    PublicationFragmentSettings fragmentSettings = new PublicationFragmentSettings();
    fragmentSettings.setDisplayLinks(URLUtil.displayUniversalLinks());
    fragmentSettings.setShowImportance(kmeliaScc.isFieldImportanceVisible());
    fragmentSettings.setFileStorageShowExtraInfoPub(resources.getSetting(
        "fileStorageShowExtraInfoPub", false));
    fragmentSettings.setShowTopicPathNameinSearchResult(resources.getSetting(
        "showTopicPathNameinSearchResult", true));
    fragmentSettings.setToSearch(ctx.isSearchInProgress());
    fragmentSettings.setRateable(kmeliaScc.isPublicationRatingAllowed());
    return fragmentSettings;
  }

  void renderFilePreviewJavascript(Writer out, RenderingContext ctx)
      throws IOException {
    String sb = SCRIPT_START +
        "function previewFile(target, attachmentId) {" +
        "  $(target).preview(\"document\", {" +
        "    documentType: 'attachment'," +
        "    documentId: attachmentId," +
        "    lang: '" + ctx.getSessionController().getCurrentLanguage() + "'" +
        "  });" +
        "  return false;" +
        "}" +
        SCRIPT_END;
    out.write(sb);
  }

  void renderFileViewJavascript(Writer out, RenderingContext ctx)
      throws IOException {
    String sb = SCRIPT_START +
        "function viewFile(target, attachmentId) {" +
        "  $(target).view(\"document\", {" +
        "    documentType: 'attachment'," +
        "    documentId: attachmentId," +
        "    lang: '" + ctx.getSessionController().getCurrentLanguage() + "'" +
        "  });" +
        "  return false;" +
        "}" +
        SCRIPT_END;
    out.write(sb);
  }

  void renderPublicationFragment(KmeliaPublication aPub,
      Writer writer, RenderingContext ctx, PublicationFragmentSettings fragmentSettings)
      throws IOException {
    var kmeliaScc = ctx.getSessionController();
    var userId = kmeliaScc.getUserDetail().getId();
    String topicId = kmeliaScc.getCurrentFolderId();
    // check if this instance use a custom template
    boolean specificTemplateUsed = kmeliaScc.isCustomPublicationTemplateUsed();
    // check if publication is draggable
    boolean canBeCut = KmeliaPublicationHelper
        .isCanBeCut(kmeliaScc.getComponentId(), userId, kmeliaScc.getUserTopicProfile(),
            aPub.getCreator());
    boolean alias = aPub.isAlias();
    fragmentSettings.setDraggable(canBeCut && !alias && !KmeliaHelper.isToValidateFolder(topicId));

    if (specificTemplateUsed) {
      renderPublicationTemplatedFragment(aPub, writer, ctx, fragmentSettings);
    } else {
      renderPublicationDefaultFragment(aPub, writer, ctx, fragmentSettings);
    }
  }

  void renderPublicationTemplatedFragment(KmeliaPublication aPub,
      Writer out, RenderingContext ctx, PublicationFragmentSettings fragmentSettings)
      throws IOException {
    var kmeliaScc = ctx.getSessionController();
    var resources = ctx.getResources();
    String language = kmeliaScc.getCurrentLanguage();
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents();
    PublicationDetail pub = aPub.getDetail();
    String name = pub.getName(language);
    String description = pub.getDescription(language);

    template.setAttribute("publication", pub);
    template.setAttribute("link", "javascript:onClick=publicationGoTo('" + pub.getId() + "')");
    template.setAttribute("name", Encode.forHtml(name));
    template.setAttribute("description",
        WebEncodeHelper.convertBlanksForHtml(Encode.forHtml(description)));
    template.setAttribute("showDescription",
        StringUtil.isDefined(description) && !description.equals(name));
    template.setAttribute("importance", pub.getImportance());
    template.setAttribute("showImportance",
        fragmentSettings.isShowImportance() && !fragmentSettings.isLinkAttachment());
    template.setAttribute("date", displayDate(pub, kmeliaScc, resources));
    template.setAttribute("creationDate", resources.getOutputDate(pub.getCreationDate()));
    template.setAttribute("updateDate", resources.getOutputDate(pub.getLastUpdateDate()));
    // check if the pub date must be display
    boolean showPubDate = true;
    if (fragmentSettings.isLinkAttachment()) {
      showPubDate = fragmentSettings.isFileStorageShowExtraInfoPub();
    }
    template.setAttribute("showDate", showPubDate);

    // check if user name in the list must be display
    boolean showUserName = kmeliaScc.showUserNameInList();
    if (fragmentSettings.isLinkAttachment()) {
      showUserName = showUserName && fragmentSettings.isFileStorageShowExtraInfoPub();
    }
    template.setAttribute("username", getUserName(aPub, kmeliaScc));
    template.setAttribute("showUsername", showUserName);
    template.setAttribute("permalink", getHTMLPublicationPermalink(pub, ctx));
    template.setAttribute("showPermalink",
        fragmentSettings.isDisplayLinks() && !fragmentSettings.isSeeAlso() &&
            !fragmentSettings.isLinkAttachment());
    template.setAttribute("status", fragmentSettings.getPubState());
    template.setAttribute("statusColor", fragmentSettings.getPubColor());
    template.setAttribute("highlightClass", fragmentSettings.getHighlightClass());
    template.setAttribute("showRef",
        fragmentSettings.isSeeAlso() && resources.getSetting("linkManagerShowPubId", false));
    // Show topic name only in search in topic case
    if (fragmentSettings.isToSearch() && fragmentSettings.isShowTopicPathNameinSearchResult()) {
      template.setAttribute("path", getHTMLPublicationFullPath(pub, ctx));
    }
    boolean showAuthor = kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pub.getAuthor());
    if (fragmentSettings.isLinkAttachment()) {
      showAuthor = showAuthor && fragmentSettings.isFileStorageShowExtraInfoPub();
    }
    template.setAttribute("showAuthor", showAuthor);
    template.setAttribute("author", pub.getAuthor());
    template.setAttribute("files",
        getHTMLBlockOfFiles(pub, ctx, fragmentSettings));

    if (!"0".equals(pub.getInfoId())) {
      template.setAttribute("formName", pub.getInfoId());
      template.setAttribute("form", pub.getFormValues(language));
    }

    String fragment = template
        .applyFileTemplateOnComponent("kmelia", kmeliaScc.getCustomPublicationTemplateName());
    out.write(fragment);
  }

  void renderPublicationDefaultFragment(KmeliaPublication aPub,
      Writer writer, RenderingContext ctx, PublicationFragmentSettings fragmentSettings)
      throws IOException {
    var kmeliaScc = ctx.getSessionController();
    var resources = ctx.getResources();
    PublicationDetail pub = aPub.getDetail();
    String language = kmeliaScc.getCurrentLanguage();
    String name = Encode.forHtml(pub.getName(language));

    writer.write("<div class=\"publication-name line1\">");
    if (fragmentSettings.isLinksAllowed()) {
      renderPublicationLink(pub, name, writer, fragmentSettings);
    } else {
      renderPublicationRef(pub, name, writer, fragmentSettings, resources);
    }
    writer.write("&#160;");
    if (StringUtil.isDefined(fragmentSettings.getPubState())) {
      renderPublicationState(writer, fragmentSettings);
    }
    if (fragmentSettings.isRateable()) {
      renderPublicationRating(writer, pub);
    }
    writer.write(END_DIV);

    renderPublicationAdditionalInfo(aPub, writer, ctx, fragmentSettings);

    String description = pub.getDescription(language);
    // displays publication description
    if (StringUtil.isDefined(description) && !description.equals(name)) {
      writer.write("<p class=\"description line3\">");
      writer.write(WebEncodeHelper.convertBlanksForHtml(Encode.forHtml(description)));
      writer.write("</p>");
    }

    writer.write(
        getHTMLBlockOfFiles(pub, ctx, fragmentSettings));

    writer.write(SCRIPT_START);
    writer.write("whenSilverpeasReady(function() {" +
        "  document.querySelectorAll('.publication-name a').forEach(function(a) {\n" +
        "    a.addEventListener('click', function(e) {\n" +
        "      e.preventDefault();\n" +
        "      e.stopPropagation();\n" +
        "      const pubId = this.querySelector('span.jstree-draggable').getAttribute('id').trim().slice(4);\n" +
        "      publicationGoTo(pubId);\n" +
        "    });" +
        "  });" +
        "});");
    writer.write(SCRIPT_END);
  }

  private void renderPublicationAdditionalInfo(KmeliaPublication aPub, Writer writer,
      RenderingContext ctx, PublicationFragmentSettings fragmentSettings) throws IOException {
    var kmeliaScc = ctx.getSessionController();
    var resources = ctx.getResources();
    PublicationDetail pub = aPub.getDetail();

    writer.write("<div class=\"line2 ");
    writer.write(fragmentSettings.getPubColor());
    writer.write("\">");
    // Show topic name only in search in topic case
    if (fragmentSettings.isToSearch() && fragmentSettings.isShowTopicPathNameinSearchResult()) {
      writer.write(getHTMLPublicationFullPath(pub, ctx));
    }
    // check if user name in the list must be display
    boolean showUserNameInList = kmeliaScc.showUserNameInList();
    if (fragmentSettings.isLinkAttachment()) {
      showUserNameInList = showUserNameInList && fragmentSettings.isFileStorageShowExtraInfoPub();
    }
    if (showUserNameInList) {
      writer.write("<span class=\"user\">");
      writer.write(getUserName(aPub, kmeliaScc));
      writer.write(" - </span>");
    }
    // check if the pub date must be display
    boolean showPubDate;
    if (fragmentSettings.isLinkAttachment()) {
      showPubDate = fragmentSettings.isFileStorageShowExtraInfoPub();
    } else {
      showPubDate = true;
    }
    if (showPubDate) {
      writer.write("<span class=\"date\">");
      writer.write(displayDate(pub, kmeliaScc, resources));
      writer.write(END_SPAN);
    }

    // check if he author name must be display
    boolean showAuthor = kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pub.getAuthor());
    if (fragmentSettings.isLinkAttachment()) {
      showAuthor = showAuthor && fragmentSettings.isFileStorageShowExtraInfoPub();
    }
    if (showAuthor) {
      writer.write("<span class=\"author\">");
      writer.write("&#160;-&#160;(");
      writer.write(resources.getString("GML.author"));
      writer.write(":&#160;");
      writer.write(pub.getAuthor());
      writer.write(")</span>");
    }
    // displays permalink
    if (fragmentSettings.isDisplayLinks() && !fragmentSettings.isSeeAlso() &&
        !fragmentSettings.isLinkAttachment()) {
      writer.write("<span class=\"permalink\">");
      writer.write(getHTMLPublicationPermalink(pub, ctx));
      writer.write(END_SPAN);
    }
    writer.write(END_DIV);
  }

  private static void renderPublicationRating(Writer writer, PublicationDetail pub) throws IOException {
    RaterRatingEntity raterRatingEntity = RaterRatingEntity.fromRateable(pub);
    writer.write(raterRatingEntity
        .toJSonScript("raterRatingEntity_" + raterRatingEntity.getContributionId()));
    writer.write(
        "<div silverpeas-rating readonly=\"true\" shownbraterratings=\"false\" " +
            "starsize=\"small\" " +
            "raterrating=\"raterRatingEntity_" + raterRatingEntity.getContributionId() +
            "\"></div>");
  }

  private static void renderPublicationState(Writer writer,
      PublicationFragmentSettings fragmentSettings) throws IOException {
    writer.write("<span class=\"state ");
    writer.write(fragmentSettings.getPubColor());
    writer.write("\">(");
    writer.write(fragmentSettings.getPubState());
    writer.write(")</span>");
  }

  private static void renderPublicationRef(PublicationDetail pub, String name, Writer writer,
      PublicationFragmentSettings fragmentSettings, MultiSilverpeasBundle resources) throws IOException {
    String ref = "";
    if (fragmentSettings.isSeeAlso() && resources.getSetting("linkManagerShowPubId", false)) {
      ref = " [ " + pub.getPK().getId() + " ] ";
    }
    writer.write("<div class=\"");
    writer.write(fragmentSettings.getPubColor());
    writer.write("\"><span class=\"" + fragmentSettings.getHighlightClass() + "\">");
    writer.write(ref);
    writer.write(name);
    writer.write("</span></div>");
  }

  private static void renderPublicationLink(PublicationDetail pub, String name, Writer writer,
      PublicationFragmentSettings fragmentSettings) throws IOException {
    writer.write("<div class=\"");
    writer.write(fragmentSettings.getPubColor());
    writer.write("\"><a href=\"#\"><span class=\"" + fragmentSettings.getHighlightClass() + "\">");
    if (fragmentSettings.isDraggable()) {
      writer.write("<span class=\"jstree-draggable\" id=\"pub-" + pub.getPK().getId() + "\">");
      writer.write(name);
      writer.write(END_SPAN);
    } else {
      writer.write(name);
    }
    writer.write("</span></a></div>");
  }

  String displayDate(PublicationDetail pub, KmeliaSessionController kmeliaScc,
      MultiSilverpeasBundle resources) {
    if (5 == kmeliaScc.getSortValue() || 6 == kmeliaScc.getSortValue()) {
      return resources.getOutputDate(pub.getCreationDate());
    } else {
      return resources.getOutputDate(pub.getLastUpdateDate());
    }
  }

  String getHTMLBlockOfFiles(PublicationDetail pub, RenderingContext ctx,
      PublicationFragmentSettings fragmentSettings) {
    var kmeliaScc = ctx.getSessionController();
    StringBuilder sb = new StringBuilder(1024);
    boolean displayFiles =
        (KmeliaHelper.isToolbox(kmeliaScc.getComponentId()) || kmeliaScc.attachmentsInPubList()) &&
            !fragmentSettings.isSeeAlso() || fragmentSettings.isLinkAttachment();
    if (displayFiles) {
      sb.append("<span class=\"files\">");
      sb.append(getHTMLBlockOfAttachments(pub, ctx));

      sb.append(END_SPAN);
    }
    return sb.toString();
  }

  private void renderPublicationThumbnail(PublicationDetail pub, Writer writer,
      RenderingContext ctx) throws IOException {
    ThumbnailSettings thumbnailSettings = ctx.getSessionController().getThumbnailSettings();
    String vignetteUrl;
    String width = String.valueOf(thumbnailSettings.getWidth());
    String height = String.valueOf(thumbnailSettings.getHeight());

    if (pub.getImage().startsWith("/")) {
      vignetteUrl = pub.getImage();
    } else {
      vignetteUrl = FileServerUtils.getUrl(pub.getPK().
              getComponentName(), "vignette", pub.getImage(), pub.getImageMimeType(),
          settings.getString("imagesSubDirectory"));
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
    writer.write(imageTag.toString());
  }

  String getHTMLPublicationPermalink(PublicationDetail pub, RenderingContext ctx) {
    String link;
    var kmeliaScc = ctx.getSessionController();
    var resources = ctx.getResources();
    if (!pub.getPK().getInstanceId().equals(kmeliaScc.getComponentId())) {
      link = URLUtil
          .getSimpleURL(URLUtil.URL_PUBLI, pub.getPK().getId(), kmeliaScc.getComponentId());
    } else {
      link = URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pub.getPK().getId());
    }
    return " - <a class=\"sp-permalink\" href=\"" + link + "\"><img src=\"" +
        resources.getIcon(KMELIA_LINK_ICON) +
        "\" border=\"0\" align=\"absmiddle\" alt=\"" +
        resources.getString("kmelia.CopyPublicationLink") + TITLE_ATTR +
        resources.getString("kmelia.CopyPublicationLink") + ANCHOR_END;
  }

  void renderSortingListBox(Writer writer, RenderingContext ctx)
      throws IOException {
    var ksc = ctx.getSessionController();
    var resources = ctx.getResources();
    boolean manualSort = ksc.getSort().getCurrentSort() == SORT_MANUAL;

    writer.write(
        "<select name=\"sortBy\" id=\"sortingList\" onChange=\"javascript:sortGoTo(this" +
            ".selectedIndex);\">");
    writer.write("<option>" + resources.getString("SortBy") + "</option>");
    writer.write("<option>-------------------------------</option>");
    writer.write(getSortingListBoxEntry(SORT_UPDATE_ASC, resources.getString("DateAsc"), ksc));
    writer.write(getSortingListBoxEntry(SORT_UPDATE_DESC, resources.getString("DateDesc"), ksc));
    writer.write(getSortingListBoxEntry(SORT_CREATION_ASC, resources.getString("CreateDateAsc"),
        ksc));
    writer.write(getSortingListBoxEntry(SORT_CREATION_DESC, resources.getString("CreateDateDesc"),
        ksc));
    writer.write(getSortingListBoxEntry(SORT_CREATOR_ASC, resources.getString("PubAuteur"), ksc));
    if (ksc.isFieldImportanceVisible()) {
      writer.write(getSortingListBoxEntry(SORT_IMPORTANCE_ASC, resources.getString("PubImportance"),
          ksc));
    }
    writer.write(getSortingListBoxEntry(SORT_TITLE_ASC, resources.getString("PubTitre"), ksc));
    writer.write(getSortingListBoxEntry(SORT_DESCRIPTION_ASC, resources.getString("PubDescription"),
        ksc));

    if (manualSort) {
      writer.write(
          getSortingListBoxEntry(SORT_MANUAL, resources.getString("kmelia.sort.manual"), ksc));
    }
    writer.write("</select>");
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

      writer.write(resetSort.toString());
    }
  }

  private String getSortingListBoxEntry(int value, String label, KmeliaSessionController ksc) {
    Option option = new Option(label, value);
    option.setID("sort" + value);
    option.setSelected(isSelectedSort(ksc, value));
    return option.toString();
  }

  private boolean isSelectedSort(KmeliaSessionController ksc, int sort) {
    final int currentSort = ksc.getSearchContext() != null ?
        ksc.getSearchContext().getSortValue() :
        ksc.getSortValue();
    return sort == currentSort;
  }

  private void renderPublicationsListHeader(Writer writer, RenderingContext ctx,
      Pagination pagination) throws IOException {
    var resources = ctx.getResources();
    String publicationSrc = resources.getIcon(KMELIA_PUBLICATION_ICON);
    writer.write("<div id=\"pubsHeader\">");
    Img img = new Img(publicationSrc).setAlt("");
    writer.write(img.toString());
    writer.write("<span id=\"pubsCounter\">");
    writer.write("<span>" + pagination.printCounter() + "</span> ");
    if (ctx.getPublications().size() > 1) {
      writer.write(resources.getString("GML.publications"));
    } else {
      writer.write(resources.getString("GML.publication"));
    }
    writer.write(END_SPAN);
    writer.write("<span id=\"pubsSort\">");
    if (ctx.isSortAllowed()) {
      renderSortingListBox(writer, ctx);
    }
    writer.write(END_SPAN);
    writer.write(END_DIV);
  }

  private String getUserName(KmeliaPublication userPub, KmeliaSessionController kmeliaScc) {
    User currentUser = userPub.getCreator();
    PublicationDetail pub = userPub.getDetail();
    String updaterId = pub.getUpdaterId();
    User updater = null;
    if (updaterId != null && !updaterId.isEmpty()) {
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

  private String getHTMLBlockOfAttachments(PublicationDetail pubDetail, RenderingContext ctx) {
    var kmeliaScc = ctx.getSessionController();
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

        result.append(getHTMLOfFile(document.getId(), attachment, ctx));
      }
    }
    if (hasDisplayableAttachments) {
      result.append("</table>");
    }
    return result.toString();
  }

  private String getHTMLOfFile(String docId, SimpleDocument attachment, RenderingContext ctx) {
    if (!ctx.isAttachmentToLink()) {
      return buildNonLinkedAttachment(docId, attachment, ctx);
    } else {
      return buildLinkedAttachment(docId, attachment, ctx);
    }
  }

  private static String buildLinkedAttachment(String docId, SimpleDocument attachment,
      RenderingContext ctx) {
    String icon = FileRepositoryManager.getFileIcon(FilenameUtils.getExtension(attachment.
        getFilename()));
    String size = FileRepositoryManager.formatFileSize(attachment.getSize());
    String info = Encode.forHtml(attachment.getDescription());
    String downloadTime = FileRepositoryManager.getFileDownloadTime(attachment.getSize());
    String permalink = URLUtil.getSimpleURL(URLUtil.URL_FILE, docId);
    String url = FileServerUtils.getApplicationContext() + attachment.getAttachmentURL();
    // determines the label to display
    Pair<String, String> titleAndLogicalName = getTitleAndLogicalName(attachment);

    StringBuilder result = new StringBuilder(1024);

    var resources = ctx.getResources();

    String displayedTitle;
    if (StringUtil.isNotDefined(titleAndLogicalName.getFirst())) {
      displayedTitle = titleAndLogicalName.getSecond();
    } else {
      displayedTitle = titleAndLogicalName.getFirst();
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
          .append("\" target=\"_blank\"><img src=\"").append(resources.getIcon(KMELIA_LINK_ICON))
          .append("\" border=\"0\" valign=\"absmiddle\" alt=\"")
          .append(resources.getString("kmelia.CopyFileLink")).append(TITLE_ATTR)
          .append(resources.getString("kmelia.CopyFileLink")).append(ANCHOR_END);
    }
    result.append(NEW_LINE);
    // displays extra information if parameter is true
    if (resources.getSetting("fileStorageShowExtraInfoAttachment", false)) {
      result.append("<i>");
      if (StringUtil.isDefined(titleAndLogicalName.getFirst())) {
        result.append(titleAndLogicalName.getSecond()).append(" / ");
      }
      // Add file size
      result.append(size);

      // and download estimation
      result.append(" / ").append(downloadTime).append(" / ")
          .append(resources.getOutputDate(attachment.getCreationDate()));
      result.append("</i>");
      // Add info
      if (StringUtil.isDefined(info)) {
        result.append(NEW_LINE).append(WebEncodeHelper.javaStringToHtmlParagraphe(info));
      }
    }
    return result.toString();
  }

  private static String buildNonLinkedAttachment(String docId, SimpleDocument attachment,
      RenderingContext ctx) {
    String id = attachment.getPk().getId();
    Pair<String, String> titleAndLogicalName = getTitleAndLogicalName(attachment);
    String info = Encode.forHtml(attachment.getDescription());
    String size = FileRepositoryManager.formatFileSize(attachment.getSize());
    String downloadTime = FileRepositoryManager.getFileDownloadTime(attachment.getSize());
    boolean previewable = ViewerProvider.isPreviewable(attachment.getAttachmentPath());
    boolean viewable = ViewerProvider.isViewable(attachment.getAttachmentPath());

    StringBuilder result = new StringBuilder(1024);

    var resources = ctx.getResources();
    boolean isUserAllowedToDownloadFile =
        attachment.isDownloadAllowedForRolesFrom(ctx.getSessionController().getUserDetail());
    String fileTitle = StringUtil.isDefined(titleAndLogicalName.getFirst()) ?
        titleAndLogicalName.getFirst() : titleAndLogicalName.getSecond();

    buildAttachmentInfo(result, docId, attachment, ctx, fileTitle);

    result.append(NEW_LINE);

    result.append("<i>");
    boolean showTitle = resources.getSetting("showTitle", true);
    if (StringUtil.isDefined(titleAndLogicalName.getSecond()) &&
        (!titleAndLogicalName.getSecond().equals(fileTitle) || !showTitle)) {
      result.append(titleAndLogicalName.getSecond()).append(" / ");
    }
    // Add file size
    if (resources.getSetting("showFileSize", true)) {
      result.append(size);
    }
    // and download estimation
    if (resources.getSetting("showDownloadEstimation", false)) {
      result.append(" / ").append(downloadTime).append(" / ")
          .append(resources.getOutputDate(attachment.getCreationDate()));
    }
    if (previewable) {
      buildPreviewableBlock(result, id, resources);
    }
    if (viewable) {
      buildViewableBlock(result, id, resources);
    }
    if (!attachment.isDownloadAllowedForReaders()) {
      String forbiddenDownloadHelp =
          isUserAllowedToDownloadFile ? resources.getString("GML.download.forbidden.readers") :
              resources.getString("GML.download.forbidden");
      result.append(" <img class=\"forbidden-download-file\" src=\"")
          .append(resources.getIcon("kmelia.file.forbidden-download")).append(ALT)
          .append(forbiddenDownloadHelp).append(TITLE_ATTR).append(forbiddenDownloadHelp)
          .append("\"/>");
    }
    result.append("</i>");

    // Add info
    if (StringUtil.isDefined(info) && resources.getSetting("showInfo", true)) {
      result.append(NEW_LINE).append(WebEncodeHelper.convertBlanksForHtml(info));
    }
    result.append("</td></tr>");
    return result.toString();
  }

  private static void buildAttachmentInfo(StringBuilder result, String docId,
      SimpleDocument attachment, RenderingContext ctx, String fileTitle) {
    String icon = FileRepositoryManager.getFileIcon(FilenameUtils.getExtension(attachment.
        getFilename()));
    String permalink = URLUtil.getSimpleURL(URLUtil.URL_FILE, docId);
    String url = FileServerUtils.getApplicationContext() + attachment.getAttachmentURL();

    var resources = ctx.getResources();
    boolean isUserAllowedToDownloadFile =
        attachment.isDownloadAllowedForRolesFrom(ctx.getSessionController().getUserDetail());

    String link = isUserAllowedToDownloadFile ? "<a href=\"" + url + "\" target=\"_blank\">" :
        "<span class=\"forbidden-download\">";
    result.append("<tr><td valign=\"top\">");
    // Add doc type icon
    Img iconImg = new Img(icon).setAlignment(Alignment.MIDDLE);
    result.append(link).append(iconImg.toString()).append("</a>&#160;</td>");
    result.append("<td valign=\"top\">").append(link);
    boolean showTitle = resources.getSetting("showTitle", true);
    if (StringUtil.isDefined(fileTitle) && showTitle) {
      result.append(Encode.forHtml(fileTitle));
    }
    result.append(isUserAllowedToDownloadFile ? "</a>" : END_SPAN);

    if (StringUtil.isDefined(permalink) && isUserAllowedToDownloadFile) {
      buildPermalinkBlock(result, permalink, resources);
    }
  }

  private static void buildPermalinkBlock(StringBuilder result, String permalink, MultiSilverpeasBundle resources) {
    result.append("&#160;<a href=\"").append(permalink)
        .append("\" target=\"_blank\"><img src=\"").append(resources.getIcon(KMELIA_LINK_ICON))
        .append("\" border=\"0\" valign=\"absmiddle\" alt=\"").append(TITLE_ATTR)
        .append(ANCHOR_END);
  }

  private static void buildViewableBlock(StringBuilder result, String id, MultiSilverpeasBundle resources) {
    result.append(" <img onclick=\"javascript:viewFile(this, '").append(id)
        .append("');\" class=\"view-file\" src=\"")
        .append(resources.getIcon("kmelia.file.view")).append(ALT)
        .append(resources.getString("GML.view.file")).append(TITLE_ATTR)
        .append(resources.getString("GML.view.file")).append("\"/>");
  }

  private static void buildPreviewableBlock(StringBuilder result, String id, MultiSilverpeasBundle resources) {
    result.append(" <img onclick=\"javascript:previewFile(this, '").append(id)
        .append("');\" class=\"preview-file\" src=\"")
        .append(resources.getIcon("kmelia.file.preview")).append(ALT)
        .append(resources.getString("GML.preview.file")).append(TITLE_ATTR)
        .append(resources.getString("GML.preview.file")).append("\"/>");
  }

  private static Pair<String, String> getTitleAndLogicalName(SimpleDocument attachment) {
    String logicalName = Encode.forHtml(attachment.getFilename());
    String title = Encode.forHtml(attachment.getTitle());
    if (!StringUtil.isDefined(attachment.getTitle())) {
      title = logicalName;
      // do not display filename twice
      logicalName = null;
    }
    if (attachment.isVersioned()) {
      title += " v" + attachment.getMajorVersion();
    }
    return Pair.of(title, logicalName);
  }

  private String getHTMLPublicationFullPath(PublicationDetail pub, RenderingContext ctx) {
    // Get space and componentLabel of the publication (can be different from context)
    var kmelia = ctx.getSessionController();
    OrganizationController controller = kmelia.getOrganisationController();
    ComponentInstLight compoInstLight = controller.getComponentInstLight(pub.getInstanceId());
    String componentLabel = compoInstLight.getLabel(kmelia.getCurrentLanguage());
    String spaceLabel = Encode.forHtml(
        controller.getSpaceInstLightById(compoInstLight.getDomainFatherId())
            .getName(kmelia.getCurrentLanguage()));
    return PublicationService.get().getMainLocation(pub.getPK())
        .map(m -> {
          String topicPathName = spaceLabel + " > " + componentLabel + " > " + kmelia.displayPath(
              kmelia.getKmeliaService().getPath(m.getId(), m.getInstanceId()), false, 3);
          return "<div class=\"publiPath\">" + topicPathName + END_DIV;
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
}
  