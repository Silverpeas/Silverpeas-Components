/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.control;

import java.util.function.Supplier;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import org.silverpeas.components.discussion.service.ConversationService;
import org.silverpeas.components.discussion.service.DiscussionMentionService;
import org.silverpeas.components.discussion.web.DiscussionInstanceInfoService;
import org.silverpeas.components.discussion.web.DiscussionParticipantService;
import org.silverpeas.components.discussion.web.DiscussionPublicationService;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

/** Native session controller routed by Silverpeas' generic WebComponentRequestRouter. */
@WebComponentController("discussion")
public class DiscussionWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<DiscussionWebRequestContext> {
  private static final String BASE = "org.silverpeas.components.discussion.";
  private transient org.silverpeas.components.discussion.service.DiscussionChannelService channelService;
  private transient ConversationService conversations;
  private transient DiscussionInstanceInfoService instanceInfo;
  private transient DiscussionMentionService mentions;
  private transient DiscussionParticipantService participantService;
  private transient org.silverpeas.components.discussion.service.DiscussionTypingService typingService;
  private transient DiscussionPublicationService publicationService;

  public DiscussionWebController(MainSessionController main, ComponentContext component) {
    super(main, component, BASE + "multilang.DiscussionBundle",
        BASE + "settings.DiscussionIcons", BASE + "settings.DiscussionSettings");
  }

  @Override
  protected void onInstantiation(DiscussionWebRequestContext context) {
    // The V1 conversation needs no additional session state.
  }

  @Override
  protected void beforeRequestProcessing(DiscussionWebRequestContext context) {
    String scope = context.getPathVariables().get("channel");
    String messageId = context.getPathVariables().get("id");
    if (messageId == null) { messageId = context.getPathVariables().get("root"); }
    if (scope != null) {
      String selected = scope;
      webResult(() -> channels().select(context.getComponentInstanceId(), selected));
      context.setChannelScope(scope);
    } else if (messageId != null) {
      String target = messageId;
      var channel = webResult(() -> channels().selectMessage(context.getComponentInstanceId(), target));
      context.setChannelScope(channel.getScopeKey());
    } else {
      // Explicitly select the default on every request, including reused servlet threads.
      webResult(() -> channels().select(context.getComponentInstanceId(), "main"));
    }
    super.beforeRequestProcessing(context);
    context.getRequest().setAttribute("instanceUriBase", context.getInstanceUriBase());
    context.getResponse().setHeader("Cache-Control", "no-store");
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("discussion.jsp")
  public void home(DiscussionWebRequestContext context) {
    loadHistory(context, Long.MAX_VALUE);
    preparePage(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("messages/{id}")
  @RedirectToInternalJsp("discussion.jsp")
  public void message(DiscussionWebRequestContext context) {
    var root = webResult(() -> conversations().threadRoot(context.getComponentInstanceId(),
        context.getPathVariables().get("id")));
    String targetId = context.getPathVariables().get("id");
    if (!root.getId().equals(targetId)) {
      context.getRequest().setAttribute("initialThreadRootId", root.getId());
      context.getRequest().setAttribute("initialThreadMessageId", targetId);
    }
    // Canonical message links always render the main conversation; replies open in its panel.
    var page = webResult(() -> conversations().historyAround(context.getComponentInstanceId(), root.getId()));
    prepareHistory(context, page);
    context.getRequest().setAttribute("focusedMessageId", root.getId());
    preparePage(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}")
  @RedirectToInternalJsp("discussion.jsp")
  public void thread(DiscussionWebRequestContext context) {
    String rootId = prepareThread(context);
    String target = context.getRequest().getParameter("message");
    if (target != null && !target.isBlank() && !target.equals(rootId)) {
      var actualRoot = webResult(() -> conversations().threadRoot(context.getComponentInstanceId(), target));
      if (!rootId.equals(actualRoot.getId())) {
        throw new WebApplicationException(400);
      }
      prepareHistory(context, webResult(() -> conversations().historyAround(context.getComponentInstanceId(), target)));
      context.getRequest().setAttribute("focusedMessageId", target);
    } else {
      prepareHistory(context, webResult(() -> conversations().threadHistory(
          context.getComponentInstanceId(), rootId, Long.MAX_VALUE, 30)));
    }
    preparePage(context);
  }

  private String prepareThread(DiscussionWebRequestContext context) {
    String id = context.getPathVariables().get("root");
    var root = webResult(() -> conversations().threadRoot(context.getComponentInstanceId(), id));
    if (!root.getId().equals(id)) {
      throw new WebApplicationException(400);
    }
    context.getRequest().setAttribute("threadRoot", root);
    return id;
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}/history")
  @RedirectToInternalJsp("messages.jsp")
  public void threadHistory(DiscussionWebRequestContext context) {
    String rootId = prepareThread(context);
    prepareHistory(context, webResult(() -> conversations().threadHistory(
        context.getComponentInstanceId(), rootId, number(context, "before", Long.MAX_VALUE), 30)));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}/updates")
  @RedirectToInternalJsp("messages.jsp")
  public void threadUpdates(DiscussionWebRequestContext context) {
    String rootId = prepareThread(context);
    String ids = context.getRequest().getParameter("ids");
    if (ids != null && ids.length() > 4100) {
      throw new WebApplicationException(400);
    }
    var selected = ids == null || ids.isBlank() ? java.util.List.<String>of()
        : java.util.Arrays.asList(ids.split(","));
    prepareHistory(context, webResult(() -> conversations().threadUpdates(context.getComponentInstanceId(),
        rootId, number(context, "after", 0), selected)));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}/reply")
  @RedirectToInternal("threads/{root}")
  public void reply(DiscussionWebRequestContext context) {
    String rootId = prepareThread(context);
    webAction(() -> {
      var uploads = uploads(context);
      conversations().reply(context.getComponentInstanceId(), rootId, context.getRequest().getParameter("body"),
          uploads.images(), uploads.files());
    });
    context.addRedirectVariable("root", rootId);
  }

  private void requireThreadMessage(DiscussionWebRequestContext context) {
    String rootId = context.getPathVariables().get("root");
    var actualRoot = webResult(() -> conversations().threadRoot(context.getComponentInstanceId(),
        context.getPathVariables().get("id")));
    if (!rootId.equals(actualRoot.getId())) {
      throw new WebApplicationException(400);
    }
    context.addRedirectVariable("root", rootId);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}/messages/{id}/edit")
  @RedirectToInternal("threads/{root}")
  public void threadEdit(DiscussionWebRequestContext context) {
    requireThreadMessage(context);
    edit(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}/messages/{id}/delete")
  @RedirectToInternal("threads/{root}")
  public void threadDelete(DiscussionWebRequestContext context) {
    requireThreadMessage(context);
    delete(context);
  }

  private transient org.silverpeas.components.discussion.search.DiscussionSearchService searchService;

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("search")
  @RedirectToInternalJsp("search.jsp")
  public void search(DiscussionWebRequestContext context) {
    if (searchService == null) {
      searchService = ServiceProvider.getService(org.silverpeas.components.discussion.search.DiscussionSearchService.class);
    }
    long offset = number(context, "offset", 0);
    if (offset < 0 || offset > Integer.MAX_VALUE) {
      throw new WebApplicationException(400);
    }
    var page = webResult(() -> searchService.search(context.getComponentInstanceId(),
        context.getRequest().getParameter("q"), (int) offset,
        "true".equals(context.getRequest().getParameter("allChannels"))));
    context.getRequest().setAttribute("searchPage", page);
    context.getRequest().setAttribute("searchChannels", webResult(() -> channels().list(context.getComponentInstanceId())).stream()
        .collect(java.util.stream.Collectors.toMap(item -> item.conversation().getId(),
            item -> item.conversation().getChannelName() == null ? context.getMultilang().getString("discussion.general") : item.conversation().getChannelName())));
    context.getRequest().setAttribute("searchBodies", page.messages().stream().collect(
        java.util.stream.Collectors.toMap(org.silverpeas.components.discussion.model.DiscussionMessage::getId,
            message -> DiscussionMentionService.displayText(message.getBody()))));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("searchResult")
  @RedirectToInternal("messages/{id}")
  public void searchResult(DiscussionWebRequestContext context) {
    String id = context.getRequest().getParameter("Id");
    if (id == null || !id.matches("[a-zA-Z0-9-]{1,40}")) {
      throw new WebApplicationException(400);
    }
    context.addRedirectVariable("id", id);
  }

  private transient org.silverpeas.components.discussion.service.DiscussionNotificationSettings notificationSettings;

  private void preparePage(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("discussionInstanceId", context.getComponentInstanceId());
    if (instanceInfo == null) {
      instanceInfo = ServiceProvider.getService(DiscussionInstanceInfoService.class);
      mentions = ServiceProvider.getService(DiscussionMentionService.class);
    }
    context.getRequest().setAttribute("instanceInfo",
        webResult(() -> instanceInfo.info(context.getComponentInstanceId(), getLanguage())));
    boolean canMention = context.getHighestUserRole() != null &&
        context.getHighestUserRole().isGreaterThanOrEquals(SilverpeasRole.WRITER) &&
        !context.getUser().isAnonymous() && !context.getUser().isAccessGuest();
    context.getRequest().setAttribute("mentionCandidates", canMention
        ? webResult(() -> mentions.candidates(context.getComponentInstanceId())) : java.util.List.of());
    var available = webResult(() -> channels().list(context.getComponentInstanceId()));
    context.getRequest().setAttribute("channels", available);
    // Canonical notification links resolve the channel from the message rather than the URL.
    var current = webResult(() -> channels().select(context.getComponentInstanceId(), context.getChannelScope()));
    context.getRequest().setAttribute("currentChannel", current);
    context.getRequest().setAttribute("channelNotificationMode", webResult(() -> channels().notificationMode(context.getComponentInstanceId())));
    context.getRequest().setAttribute("canManageChannels", context.getHighestUserRole() == SilverpeasRole.ADMIN);
    if (current.isArchived()) { context.getRequest().setAttribute("canWrite", false); }
    participants(context);
    publications(context);
    pins(context);
    sharedFiles(context);
    notificationPreferences(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("messages/{id}/like")
  @RedirectToInternalJsp("like.jsp")
  public void like(DiscussionWebRequestContext context) {
    String value = context.getRequest().getParameter("liked");
    if (!"true".equals(value) && !"false".equals(value)) {
      throw new WebApplicationException(400);
    }
    context.getRequest().setAttribute("likeState", webResult(() -> conversations().setLike(
        context.getComponentInstanceId(), context.getPathVariables().get("id"), Boolean.parseBoolean(value))));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("history")
  @RedirectToInternalJsp("messages.jsp")
  public void history(DiscussionWebRequestContext context) {
    loadHistory(context, number(context, "before", Long.MAX_VALUE));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("participants")
  @RedirectToInternalJsp("participants.jsp")
  public void participants(DiscussionWebRequestContext context) {
    if (participantService == null) {
      participantService = ServiceProvider.getService(DiscussionParticipantService.class);
    }
    context.getRequest().setAttribute("participants",
        webResult(() -> participantService.list(context.getComponentInstanceId())));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("updates")
  @RedirectToInternalJsp("messages.jsp")
  public void updates(DiscussionWebRequestContext context) {
    String ids = context.getRequest().getParameter("ids");
    if (ids != null && ids.length() > 4100) {
      throw new WebApplicationException(400);
    }
    var selected = ids == null || ids.isBlank() ? java.util.List.<String>of()
        : java.util.Arrays.asList(ids.split(","));
    prepareHistory(context, webResult(() -> conversations().updates(context.getComponentInstanceId(),
        number(context, "after", 0), selected)));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("publications")
  @RedirectToInternalJsp("publications.jsp")
  public void publications(DiscussionWebRequestContext context) {
    if (publicationService == null) {
      publicationService = ServiceProvider.getService(DiscussionPublicationService.class);
    }
    context.getRequest().setAttribute("citedPublications",
        webResult(() -> publicationService.list(context.getComponentInstanceId(), getLanguage())));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("pins")
  @RedirectToInternalJsp("pins.jsp")
  public void pins(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("pinnedMessages",
        webResult(() -> conversations().pinnedMessages(context.getComponentInstanceId())));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("messages/{id}/pin")
  @RedirectToInternalJsp("pin.jsp")
  public void pin(DiscussionWebRequestContext context) {
    String value = context.getRequest().getParameter("pinned");
    if (!"true".equals(value) && !"false".equals(value)) {
      throw new WebApplicationException(400);
    }
    context.getRequest().setAttribute("pinnedMessage", webResult(() -> conversations().setPinned(
        context.getComponentInstanceId(), context.getPathVariables().get("id"), Boolean.parseBoolean(value))));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("files")
  @RedirectToInternalJsp("files.jsp")
  public void sharedFiles(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("sharedFiles", webResult(() -> conversations().sharedFiles(context.getComponentInstanceId())));
  }

  private org.silverpeas.components.discussion.service.DiscussionNotificationSettings notificationSettings() {
    if (notificationSettings == null) {
      notificationSettings = ServiceProvider.getService(org.silverpeas.components.discussion.service.DiscussionNotificationSettings.class);
    }
    return notificationSettings;
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("notification-preferences")
  @RedirectToInternalJsp("notification-preferences.jsp")
  public void notificationPreferences(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("desktopPreference", webResult(() -> notificationSettings().current(context.getComponentInstanceId())));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("notification-preferences")
  @RedirectToInternalJsp("notification-preferences.jsp")
  public void saveNotificationPreferences(DiscussionWebRequestContext context) {
    String value = context.getRequest().getParameter("enabled");
    if (!"true".equals(value) && !"false".equals(value)) {
      throw new WebApplicationException(400);
    }
    context.getRequest().setAttribute("desktopPreference", webResult(() -> notificationSettings().save(
        context.getComponentInstanceId(), Boolean.parseBoolean(value))));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("typing")
  @RedirectToInternalJsp("typing.jsp")
  public void typing(DiscussionWebRequestContext context) {
    String active = context.getRequest().getParameter("active");
    String tab = context.getRequest().getParameter("tab");
    if ((!"true".equals(active) && !"false".equals(active)) || tab == null || !tab.matches("[A-Za-z0-9_-]{1,64}")) {
      throw new WebApplicationException(400);
    }
    if (typingService == null) {
      typingService = ServiceProvider.getService(org.silverpeas.components.discussion.service.DiscussionTypingService.class);
    }
    webAction(() -> {
      String rootId = context.getRequest().getParameter("threadRootId");
      String session = context.getRequest().getSession().getId() + ":" + tab;
      if (rootId == null || rootId.isEmpty()) {
        typingService.typing(context.getComponentInstanceId(), session, Boolean.parseBoolean(active));
      } else {
        if (!rootId.matches("[a-zA-Z0-9-]{1,40}")) {
          throw new IllegalArgumentException("Invalid typing scope");
        }
        typingService.typing(context.getComponentInstanceId(), session, Boolean.parseBoolean(active), rootId);
      }
    });
  }

  private void loadHistory(DiscussionWebRequestContext context, long before) {
    prepareHistory(context,
        webResult(() -> conversations().history(context.getComponentInstanceId(), before, 30)));
  }

  private void prepareHistory(DiscussionWebRequestContext context,
      org.silverpeas.components.discussion.service.MessagePage page) {
    context.getRequest().setAttribute("messagePage", page);
    context.getRequest().setAttribute("messageFiles", webResult(() -> conversations().files(
        context.getComponentInstanceId(), page.messages().stream().map(message -> message.getId()).toList())));
    context.getRequest().setAttribute("messageImages", webResult(() -> conversations().images(
        context.getComponentInstanceId(), page.messages().stream().map(message -> message.getId()).toList())));
    context.getRequest().setAttribute("defaultAvatarPath", org.silverpeas.core.admin.user.model.User.DEFAULT_AVATAR_PATH);
    context.getRequest().setAttribute("messageLikes", webResult(() -> conversations().likeStates(
        context.getComponentInstanceId(), page.messages().stream().map(message -> message.getId()).toList())));
    context.getRequest().setAttribute("canReact", !context.getUser().isAnonymous() && !context.getUser().isAccessGuest());
    context.getRequest().setAttribute("canWrite", context.getHighestUserRole() != null &&
        context.getHighestUserRole().isGreaterThanOrEquals(SilverpeasRole.WRITER) &&
        !context.getUser().isAccessGuest() && !context.getUser().isAnonymous());
  }

  @POST
  @Path("messages/add")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @RedirectToInternal("Main")
  public void publish(DiscussionWebRequestContext context) {
    webAction(() -> {
      var request = context.getRequest();
      var uploads = uploads(context);
      if (!uploads.files().isEmpty()) {
        conversations().publish(context.getComponentInstanceId(), request.getParameter("body"),
            uploads.images(), uploads.files());
      } else if (!uploads.images().isEmpty()) {
        conversations().publish(context.getComponentInstanceId(), request.getParameter("body"), uploads.images());
      } else {
        conversations().publish(context.getComponentInstanceId(), request.getParameter("body"));
      }
    });
  }

  private record MessageUploads(
      java.util.List<org.silverpeas.components.discussion.service.DiscussionImageUpload> images,
      java.util.List<org.silverpeas.components.discussion.service.DiscussionFileUpload> files) { }

  private MessageUploads uploads(DiscussionWebRequestContext context) {
    var request = context.getRequest();
    var items = org.silverpeas.core.util.file.FileUploadUtil.isRequestMultipart(request)
        ? request.getFileItems().stream().filter(file -> !file.isFormField()
            && (file.getSize() > 0 || file.getFileName() != null && !file.getFileName().isBlank())).toList()
        : java.util.List.<org.silverpeas.core.util.file.FileItem>of();
    var images = items.stream().filter(file -> "images".equals(file.getFieldName())).toList();
    var files = items.stream().filter(file -> "files".equals(file.getFieldName())).toList();
    if (images.size() > org.silverpeas.components.discussion.service.DiscussionImageUpload.MAX_IMAGES
        || files.size() > org.silverpeas.components.discussion.service.DiscussionFileUpload.MAX_FILES) {
      throw new IllegalArgumentException("Too many attachments");
    }
    return new MessageUploads(
        images.stream().map(org.silverpeas.components.discussion.service.DiscussionImageUpload::read).toList(),
        files.stream().map(org.silverpeas.components.discussion.service.DiscussionFileUpload::read).toList());
  }

  @POST
  @Path("messages/{id}/edit")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @RedirectToInternal("Main")
  public void edit(DiscussionWebRequestContext context) {
    webAction(() -> conversations().edit(context.getComponentInstanceId(),
        context.getPathVariables().get("id"), number(context, "version", -1),
        context.getRequest().getParameter("body")));
  }

  @POST
  @Path("messages/{id}/delete")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @RedirectToInternal("Main")
  public void delete(DiscussionWebRequestContext context) {
    String deleteEvent = context.getRequest().getParameter("deleteEvent");
    if (deleteEvent != null && !"true".equals(deleteEvent) && !"false".equals(deleteEvent)) { throw new WebApplicationException(400); }
    if ("true".equals(deleteEvent)) {
      webAction(() -> conversations().delete(context.getComponentInstanceId(),
          context.getPathVariables().get("id"), number(context, "version", -1), true));
    } else {
      webAction(() -> conversations().delete(context.getComponentInstanceId(),
          context.getPathVariables().get("id"), number(context, "version", -1)));
    }
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("messages/read")
  @RedirectToInternalJsp("read.jsp")
  public void visible(DiscussionWebRequestContext context) {
    String ids = context.getRequest().getParameter("ids");
    if (ids == null || ids.length() > 4100) {
      throw new WebApplicationException(400);
    }
    context.getRequest().setAttribute("unreadCount", webResult(() -> conversations().markVisible(
        context.getComponentInstanceId(), java.util.Arrays.asList(ids.split(",")))));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("read")
  @RedirectToInternal("Main")
  public void markRead(DiscussionWebRequestContext context) {
    webAction(() -> conversations().markRead(context.getComponentInstanceId(),
        number(context, "sequence", -1)));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/Main")
    @RedirectToInternalJsp("discussion.jsp")
  public void channelHome(DiscussionWebRequestContext context) {
    home(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/messages/{id}")
  @RedirectToInternalJsp("discussion.jsp")
  public void channelMessage(DiscussionWebRequestContext context) {
    message(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}")
  @RedirectToInternalJsp("discussion.jsp")
  public void channelThread(DiscussionWebRequestContext context) {
    thread(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}/history")
  @RedirectToInternalJsp("messages.jsp")
  public void channelThreadHistory(DiscussionWebRequestContext context) {
    threadHistory(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}/updates")
  @RedirectToInternalJsp("messages.jsp")
  public void channelThreadUpdates(DiscussionWebRequestContext context) {
    threadUpdates(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}/reply")
  @RedirectToInternal("threads/{root}")
  public void channelReply(DiscussionWebRequestContext context) {
    reply(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}/messages/{id}/edit")
  @RedirectToInternal("threads/{root}")
  public void channelThreadEdit(DiscussionWebRequestContext context) {
    threadEdit(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}/messages/{id}/delete")
  @RedirectToInternal("threads/{root}")
  public void channelThreadDelete(DiscussionWebRequestContext context) {
    threadDelete(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/search")
  @RedirectToInternalJsp("search.jsp")
  public void channelSearch(DiscussionWebRequestContext context) {
    search(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/searchResult")
  @RedirectToInternal("messages/{id}")
  public void channelSearchResult(DiscussionWebRequestContext context) {
    searchResult(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/messages/{id}/like")
  @RedirectToInternalJsp("like.jsp")
  public void channelLike(DiscussionWebRequestContext context) {
    like(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/history")
  @RedirectToInternalJsp("messages.jsp")
  public void channelHistory(DiscussionWebRequestContext context) {
    history(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/participants")
  @RedirectToInternalJsp("participants.jsp")
  public void channelParticipants(DiscussionWebRequestContext context) {
    participants(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/updates")
  @RedirectToInternalJsp("messages.jsp")
  public void channelUpdates(DiscussionWebRequestContext context) {
    updates(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/publications")
  @RedirectToInternalJsp("publications.jsp")
  public void channelPublications(DiscussionWebRequestContext context) {
    publications(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/pins")
  @RedirectToInternalJsp("pins.jsp")
  public void channelPins(DiscussionWebRequestContext context) {
    pins(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/messages/{id}/pin")
  @RedirectToInternalJsp("pin.jsp")
  public void channelPin(DiscussionWebRequestContext context) {
    pin(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/files")
  @RedirectToInternalJsp("files.jsp")
  public void channelSharedFiles(DiscussionWebRequestContext context) {
    sharedFiles(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/notification-preferences")
  @RedirectToInternalJsp("notification-preferences.jsp")
  public void channelNotificationPreferences(DiscussionWebRequestContext context) {
    notificationPreferences(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/notification-preferences")
  @RedirectToInternalJsp("notification-preferences.jsp")
  public void channelSaveNotificationPreferences(DiscussionWebRequestContext context) {
    saveNotificationPreferences(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/typing")
  @RedirectToInternalJsp("typing.jsp")
  public void channelTyping(DiscussionWebRequestContext context) {
    typing(context);
  }

  @POST
  @Path("channels/{channel}/messages/add")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @RedirectToInternal("Main")
  public void channelPublish(DiscussionWebRequestContext context) {
    publish(context);
  }

  @POST
  @Path("channels/{channel}/messages/{id}/edit")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @RedirectToInternal("Main")
  public void channelEdit(DiscussionWebRequestContext context) {
    edit(context);
  }

  @POST
  @Path("channels/{channel}/messages/{id}/delete")
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @RedirectToInternal("Main")
  public void channelDelete(DiscussionWebRequestContext context) {
    delete(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/messages/read")
  @RedirectToInternalJsp("read.jsp")
  public void channelVisible(DiscussionWebRequestContext context) {
    visible(context);
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/read")
  @RedirectToInternal("Main")
  public void channelMarkRead(DiscussionWebRequestContext context) {
    markRead(context);
  }

  private org.silverpeas.components.discussion.service.DiscussionChannelService channels() {
    if (channelService == null) {
      channelService = ServiceProvider.getService(org.silverpeas.components.discussion.service.DiscussionChannelService.class);
    }
    return channelService;
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels")
  @RedirectToInternalJsp("channels.jsp")
  public void channelList(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("canManageChannels", context.getHighestUserRole() == SilverpeasRole.ADMIN);
    context.getRequest().setAttribute("channels", webResult(() -> channels().list(context.getComponentInstanceId())));
    context.getRequest().setAttribute("instanceUriBase", context.getInstanceUriBase());
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  @Path("channels/create")
  @RedirectToInternal("channels/{createdChannel}/Main")
  public void createChannel(DiscussionWebRequestContext context) {
    var channel = webResult(() -> channels().create(context.getComponentInstanceId(),
        context.getRequest().getParameter("name"), context.getRequest().getParameter("description")));
    context.addRedirectVariable("createdChannel", channel.getScopeKey());
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  @Path("channels/{channel}/settings")
  @RedirectToInternal("Main")
  public void updateChannel(DiscussionWebRequestContext context) {
    String archived = context.getRequest().getParameter("archived");
    if (!"true".equals(archived) && !"false".equals(archived)) { throw new WebApplicationException(400); }
    webAction(() -> channels().update(context.getComponentInstanceId(), context.getPathVariables().get("channel"),
        context.getRequest().getParameter("name"), context.getRequest().getParameter("description"), Boolean.parseBoolean(archived)));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/notifications")
  @RedirectToInternal("Main")
  public void channelNotifications(DiscussionWebRequestContext context) {
    webAction(() -> channels().notifications(context.getComponentInstanceId(), context.getRequest().getParameter("mode")));
  }

  private org.silverpeas.components.discussion.service.DiscussionIdeaService ideas() {
    return ServiceProvider.getService(org.silverpeas.components.discussion.service.DiscussionIdeaService.class);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("idea-options")
  @RedirectToInternalJsp("idea-options.jsp")
  public void ideaOptions(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("ideaOptions", webResult(() -> ideas().options(
        context.getComponentInstanceId(), context.getRequest().getParameter("sourceId"), getLanguage())));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/idea-options")
  @RedirectToInternalJsp("idea-options.jsp")
  public void channelIdeaOptions(DiscussionWebRequestContext context) { ideaOptions(context); }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("ideas")
  @RedirectToInternalJsp("idea-created.jsp")
  public void createIdea(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("createdIdea", webResult(() -> {
      var request = context.getRequest();
      var input = new org.silverpeas.components.discussion.service.DiscussionIdeaInput(
          request.getParameter("box"), request.getParameter("title"), request.getParameter("description"),
          request.getParameter("sourceId"));
      return ideas().create(context.getComponentInstanceId(), request.getParameter("threadRootId"), input);
    }));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/ideas")
  @RedirectToInternalJsp("idea-created.jsp")
  public void channelCreateIdea(DiscussionWebRequestContext context) { createIdea(context); }

  private transient org.silverpeas.components.discussion.service.DiscussionEventService eventService;

  private org.silverpeas.components.discussion.service.DiscussionEventService events() {
    if (eventService == null) {
      eventService = ServiceProvider.getService(org.silverpeas.components.discussion.service.DiscussionEventService.class);
    }
    return eventService;
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("event-options")
  @RedirectToInternalJsp("event-options.jsp")
  public void eventOptions(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("eventOptions", webResult(() -> events().options(
        context.getComponentInstanceId(), context.getZoneId(), getLanguage())));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/event-options")
  @RedirectToInternalJsp("event-options.jsp")
  public void channelEventOptions(DiscussionWebRequestContext context) { eventOptions(context); }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("events")
  @RedirectToInternalJsp("event-created.jsp")
  public void createEvent(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("createdEvent", webResult(() -> {
      var request = context.getRequest();
      var input = new org.silverpeas.components.discussion.service.DiscussionEventInput(
          request.getParameter("calendar"), request.getParameter("title"), request.getParameter("description"),
          new org.silverpeas.components.discussion.service.DiscussionEventInput.Timing(
              request.getParameter("start"), request.getParameter("end")));
      return events().create(context.getComponentInstanceId(), request.getParameter("threadRootId"), input, context.getZoneId());
    }));
  }

  @POST
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/events")
  @RedirectToInternalJsp("event-created.jsp")
  public void channelCreateEvent(DiscussionWebRequestContext context) { createEvent(context); }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("messages/{id}/deletion")
  @RedirectToInternalJsp("event-deletion.jsp")
  public void deletionInfo(DiscussionWebRequestContext context) {
    context.getRequest().setAttribute("eventDeletion", webResult(() -> conversations().deletion(
        context.getComponentInstanceId(), context.getPathVariables().get("id"), number(context, "version", -1))));
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/messages/{id}/deletion")
  @RedirectToInternalJsp("event-deletion.jsp")
  public void channelDeletionInfo(DiscussionWebRequestContext context) { deletionInfo(context); }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("threads/{root}/messages/{id}/deletion")
  @RedirectToInternalJsp("event-deletion.jsp")
  public void threadDeletionInfo(DiscussionWebRequestContext context) {
    requireThreadMessage(context);
    deletionInfo(context);
  }

  @GET
  @LowestRoleAccess(SilverpeasRole.WRITER)
  @Path("channels/{channel}/threads/{root}/messages/{id}/deletion")
  @RedirectToInternalJsp("event-deletion.jsp")
  public void channelThreadDeletionInfo(DiscussionWebRequestContext context) { threadDeletionInfo(context); }

  private ConversationService conversations() {
    if (conversations == null) {
      conversations = ServiceProvider.getService(ConversationService.class);
    }
    return conversations;
  }

  private void webAction(Runnable action) {
    webResult(() -> {
      action.run();
      return null;
    });
  }

  private <T> T webResult(Supplier<T> action) {
    try {
      return action.get();
    } catch (SecurityException e) {
      throw new WebApplicationException(e, 403);
    } catch (OptimisticLockException e) {
      throw new WebApplicationException(e, 409);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e, 400);
    }
  }

  private long number(DiscussionWebRequestContext context, String parameter, long fallback) {
    String value = context.getRequest().getParameter(parameter);
    try {
      return value == null ? fallback : Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new WebApplicationException(e, 400);
    }
  }
}
