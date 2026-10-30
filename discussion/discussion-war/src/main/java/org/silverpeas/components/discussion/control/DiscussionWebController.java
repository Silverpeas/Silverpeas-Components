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
    super.beforeRequestProcessing(context);
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
    var page = webResult(() -> conversations().historyAround(context.getComponentInstanceId(),
        context.getPathVariables().get("id")));
    prepareHistory(context, page);
    context.getRequest().setAttribute("focusedMessageId", context.getPathVariables().get("id"));
    preparePage(context);
  }

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
    participants(context);
    publications(context);
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
    webAction(() -> typingService.typing(context.getComponentInstanceId(),
        context.getRequest().getSession().getId() + ":" + tab, Boolean.parseBoolean(active)));
  }

  private void loadHistory(DiscussionWebRequestContext context, long before) {
    prepareHistory(context,
        webResult(() -> conversations().history(context.getComponentInstanceId(), before, 30)));
  }

  private void prepareHistory(DiscussionWebRequestContext context,
      org.silverpeas.components.discussion.service.MessagePage page) {
    context.getRequest().setAttribute("messagePage", page);
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
    webAction(() -> conversations().publish(context.getComponentInstanceId(),
        context.getRequest().getParameter("body")));
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
    webAction(() -> conversations().delete(context.getComponentInstanceId(),
        context.getPathVariables().get("id"), number(context, "version", -1)));
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
