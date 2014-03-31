/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.control;

import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.Navigation;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Invokable;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeAfter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternal;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternalJsp;
import com.stratelia.silverpeas.peasCore.servlets.annotation.WebComponentController;
import com.stratelia.webactiv.SilverpeasRole;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.components.suggestionbox.common.SuggestionBoxWebServiceProvider.*;

@WebComponentController("SuggestionBox")
public class SuggestionBoxWebController extends
    com.stratelia.silverpeas.peasCore.servlets
        .WebComponentController<SuggestionBoxWebRequestContext> {

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public SuggestionBoxWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.components.suggestionbox.multilang.SuggestionBoxBundle",
        "org.silverpeas.components.suggestionbox.settings.SuggestionBoxIcons",
        "org.silverpeas.components.suggestionbox.settings.SuggestionBoxSettings");
  }

  @Override
  protected void beforeRequestProcessing(final SuggestionBoxWebRequestContext context) {
    super.beforeRequestProcessing(context);
    context.getRequest().setAttribute("webServiceProvider", getWebServiceProvider());
    context.getRequest().setAttribute("suggestionBox", context.getSuggestionBox());
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("suggestionBox.jsp")
  @InvokeAfter({"isEdito", "isUserSubscribed"})
  public void home(SuggestionBoxWebRequestContext context) {
    // Nothing to do for now...
  }

  /**
   * Handles the incoming from a search result URL.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("searchResult")
  @RedirectToInternal("suggestion/{id}")
  public void searchResult(SuggestionBoxWebRequestContext context) {
    context.addRedirectVariable("id", context.getRequest().getParameter("Id"));
  }

  /**
   * Asks for the modification of the edito of the current suggestion box. The modification itself
   * is redirected to the WYSIWYG editor.
   * @param context the context of the incoming request.
   * @return the navigation information within which the next resource to which the control will be
   * passed is indicated.
   */
  @GET
  @Path("edito/modify")
  @LowestRoleAccess(SilverpeasRole.admin)
  public Navigation modifyEdito(SuggestionBoxWebRequestContext context) {
    return context.redirectToHtmlEditor(context.getSuggestionBox().getId(), "Main", false);
  }

  /**
   * Sets into request attributes the isEdito constant.
   * @param context the context of the incoming request.
   */
  @Invokable("isEdito")
  public void setIsEditoIntoRequest(SuggestionBoxWebRequestContext context) {
    if (WysiwygController
        .haveGotWysiwyg(context.getComponentInstanceId(), context.getSuggestionBox().getId(),
            null)) {
      context.getRequest().setAttribute("isEdito", true);
    }
  }

  /**
   * Sets into request attributes the isUserSubscribed constant.
   * @param context the context of the incoming request.
   */
  @Invokable("isUserSubscribed")
  public void setIsUserSubscribed(SuggestionBoxWebRequestContext context) {
    if (!getUserDetail().isAccessGuest()) {
      SubscriptionService subscriptionService =
          SubscriptionServiceFactory.getFactory().getSubscribeService();
      boolean isUserSubscribed = subscriptionService.existsSubscription(
          new ComponentSubscription(context.getUser().getId(), context.getComponentInstanceId()));
      context.getRequest().setAttribute("isUserSubscribed", isUserSubscribed);
    }
  }

  /**
   * Asks for purposing a new suggestion. It renders an HTML page to input the content of a new
   * suggestion.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("suggestion/new")
  @RedirectToInternalJsp("suggestion.jsp")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void newSuggestion(SuggestionBoxWebRequestContext context) {
  }

  /**
   * Adds a new suggestion into the current suggestion box. The suggestion's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("suggestion/add")
  @RedirectToInternal("suggestion/{id}")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void addSuggestion(SuggestionBoxWebRequestContext context) {
    SuggestionBox suggestionBox = context.getSuggestionBox();
    String title = context.getRequest().getParameter("title");
    String content = context.getRequest().getParameter("content");
    Suggestion suggestion = new Suggestion(title);
    suggestion.setContent(content);
    suggestion.setCreator(context.getUser());
    suggestionBox.getSuggestions().add(suggestion);
    context.getMessager()
        .addSuccess(getMultilang().getString("suggestionBox.message.suggestion.created"));
    context.addRedirectVariable("id", suggestion.getId());
  }

  /**
   * Asks for editing an existing suggestion. It renders an HTML page to modify the content of the
   * suggestion.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("suggestion/{id}")
  @RedirectToInternalJsp("suggestion.jsp")
  public void viewSuggestion(SuggestionBoxWebRequestContext context) {
    String suggestionId = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(suggestionId);
    if (suggestion.isDefined()) {
      SuggestionEntity entity = getWebServiceProvider().asWebEntity(suggestion);
      context.getRequest().setAttribute("suggestion", entity);
    } else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Asks for editing an existing suggestion. It renders an HTML page to modify the content of the
   * suggestion.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("suggestion/{id}/edit")
  @RedirectToInternalJsp("suggestion.jsp")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void editSuggestion(SuggestionBoxWebRequestContext context) {
    String suggestionId = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(suggestionId);
    if (suggestion.isDefined()) {
      if ((suggestion.isInDraft() || suggestion.isRefused())) {
        checkAdminAccessOrUserIsCreator(context.getUser(), suggestion);
      } else if (suggestion.isPendingValidation()) {
        checkAdminAccessOrUserIsModerator(context.getUser(), suggestionBox);
      } else {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      SuggestionEntity entity = getWebServiceProvider().asWebEntity(suggestion);
      context.getRequest().setAttribute("suggestion", entity);
      context.getRequest().setAttribute("edit", "edit");
    } else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Updates the specified suggestion in the current suggestion box. The suggestion's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("suggestion/{id}")
  @RedirectToInternal("suggestion/{id}")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void updateSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    if (suggestion.isDefined()) {
      if ((suggestion.isInDraft() || suggestion.isRefused())) {
        checkAdminAccessOrUserIsCreator(context.getUser(), suggestion);
      } else if (suggestion.isPendingValidation()) {
        checkAdminAccessOrUserIsModerator(context.getUser(), suggestionBox);
      } else {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      suggestion.setTitle(context.getRequest().getParameter("title"));
      suggestion.setContent(context.getRequest().getParameter("content"));
      suggestion.setLastUpdater(context.getUser());
      suggestion.save();
      context.getMessager()
          .addSuccess(getMultilang().getString("suggestionBox.message.suggestion.modified"));
    } else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  @POST
  @Path("suggestion/{id}/delete")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void deleteSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    getWebServiceProvider().deleteSuggestion(suggestionBox, suggestion, context.getUser());
  }

  @POST
  @Path("suggestion/{id}/publish")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void publishSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    getWebServiceProvider().publishSuggestion(suggestionBox, suggestion, context.getUser());
  }

  @POST
  @Path("suggestion/{id}/approve")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.publisher)
  public void approveSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    String validationComment = context.getRequest().getParameter("comment");
    getWebServiceProvider()
        .approveSuggestion(suggestionBox, suggestion, validationComment, context.getUser());
  }

  @POST
  @Path("suggestion/{id}/refuse")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.publisher)
  public void refuseSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    String validationComment = context.getRequest().getParameter("comment");
    getWebServiceProvider()
        .refuseSuggestion(suggestionBox, suggestion, validationComment, context.getUser());
  }
}
