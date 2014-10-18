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

import com.silverpeas.usernotification.builder.helper.UserNotificationHelper;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.ComponentSubscription;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.AbstractNavigationContextListener;
import com.stratelia.silverpeas.peasCore.servlets.Navigation;
import com.stratelia.silverpeas.peasCore.servlets.NavigationContext;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Invokable;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeAfter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.silverpeas.peasCore.servlets.annotation.NavigationStep;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternal;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternalJsp;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToPreviousNavigationStep;
import com.stratelia.silverpeas.peasCore.servlets.annotation.WebComponentController;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import org.silverpeas.util.PairObject;
import com.stratelia.webactiv.SilverpeasRole;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.components.suggestionbox.SuggestionBoxComponentSettings;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.notification
    .SuggestionNotifyManuallyUserNotification;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static org.silverpeas.components.suggestionbox.SuggestionBoxComponentSettings
    .getUserNotificationDisplayLiveTimeForLongMessage;
import static org.silverpeas.components.suggestionbox.common.SuggestionBoxWebServiceProvider.*;

@WebComponentController(SuggestionBoxComponentSettings.COMPONENT_NAME)
public class SuggestionBoxWebController extends
    com.stratelia.silverpeas.peasCore.servlets
        .WebComponentController<SuggestionBoxWebRequestContext> {

  /**
   * A context on the viewing of suggestions. It is used to parametrize the rendering of the
   * suggestions in a JSP.
   */
  public static enum ViewContext {
    /**
     * The suggestions to render are all the suggestions in a suggestion box the user can see.
     */
    AllSuggestions,
    /**
     * The suggestions to render are the suggestions published in a suggestion box.
     */
    PublishedSuggestions,
    /**
     * The suggestions to render are the suggestion awaiting a validation. It can comprise also
     * the suggestions that were refused by a moderator (a publisher).
     */
    SuggestionsInValidation,
    /**
     * The suggestions to render are the suggestions of the current user.
     */
    MySuggestions;

    public static ViewContext fromIdentifier(String identifier) {
      try {
        return valueOf(identifier);
      } catch (Exception e) {
        return null;
      }
    }
  }

  // Some navigation step identifier definitions
  private static final String SUGGESTION_LIST_NS_ID = "suggestionListNavStepIdentifier";
  private static final String SUGGESTION_VIEW_NS_ID = "suggestionViewNavStepIdentifier";

  // Some suffix path URI definitions
  private static final String PATH_SUGGESTIONS_ALL = "suggestions/all";
  private static final String PATH_SUGGESTIONS_PUBLISHED = "suggestions/published";
  private static final String PATH_SUGGESTIONS_PENDING = "suggestions/pending";
  private static final String PATH_SUGGESTIONS_MINE = "suggestions/mine";

  // Some identifier definitions
  public static final String SUGGESTION_LIST_ARRAYPANE_IDENTIFIER = "suggestionListIdentifier";

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public SuggestionBoxWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, SuggestionBoxComponentSettings.MESSAGES_PATH,
        SuggestionBoxComponentSettings.ICONS_PATH, SuggestionBoxComponentSettings.SETTINGS_PATH);
  }

  @Override
  protected void onInstantiation(final SuggestionBoxWebRequestContext context) {
    context.getNavigationContext()
        .addListener(new AbstractNavigationContextListener<SuggestionBoxWebRequestContext>() {

          @Override
          public void navigationStepTrashed(
              final NavigationContext<SuggestionBoxWebRequestContext>.NavigationStep
                  trashedNavigationStep) {
            super.navigationStepTrashed(trashedNavigationStep);
            if (SUGGESTION_LIST_NS_ID.equals(trashedNavigationStep.getIdentifier())) {
              // If the context change, the state of the displayed list is trashed
              clearSuggestionListState(
                  trashedNavigationStep.getNavigationContext().getWebComponentRequestContext());
            }
          }

          @Override
          public void navigationStepContextIdentifierSet(
              final NavigationContext<SuggestionBoxWebRequestContext>.NavigationStep navigationStep,
              final String oldContextIdentifier) {
            super.navigationStepContextIdentifierSet(navigationStep, oldContextIdentifier);
            // Getting the new context identifier
            String newContextIdentifier = navigationStep.getContextIdentifier();
            // Performing a treatment if it is defined and if it is different as the old one
            if (StringUtil.isDefined(newContextIdentifier) &&
                !newContextIdentifier.equals(oldContextIdentifier)) {
              // Verifying given context identifier that is corresponding to one of those of
              // ViewContext
              ViewContext viewContext = ViewContext.fromIdentifier(newContextIdentifier);
              if (viewContext != null) {
                ResourceLocator multilang = context.getMultilang();
                switch (viewContext) {
                  case AllSuggestions:
                    clearSuggestionListState(
                        navigationStep.getNavigationContext().getWebComponentRequestContext());
                    navigationStep.withLabel(
                        multilang.getString("suggestionBox.browsebar.item.suggestions.all"));
                    break;
                  case PublishedSuggestions:
                    clearSuggestionListState(
                        navigationStep.getNavigationContext().getWebComponentRequestContext());
                    navigationStep.withLabel(
                        multilang.getString("suggestionBox.browsebar.item.suggestions.published"));
                    break;
                  case SuggestionsInValidation:
                    clearSuggestionListState(
                        navigationStep.getNavigationContext().getWebComponentRequestContext());
                    navigationStep.withLabel(
                        multilang.getString("suggestionBox.browsebar.item.suggestion.viewPending"));
                    break;
                  case MySuggestions:
                    clearSuggestionListState(
                        navigationStep.getNavigationContext().getWebComponentRequestContext());
                    navigationStep.withLabel(
                        multilang.getString("suggestionBox.browsebar.item.suggestion.mine"));
                    break;
                }
              }
            }
          }

          /**
           * Clears the state of the suggestion list.
           */
          private void clearSuggestionListState(SuggestionBoxWebRequestContext context) {
            setSessionAttribute(context, SUGGESTION_LIST_ARRAYPANE_IDENTIFIER, null);
          }
        });
  }

  @Override
  protected void beforeRequestProcessing(final SuggestionBoxWebRequestContext context) {
    super.beforeRequestProcessing(context);
    context.getRequest().setAttribute("webServiceProvider", getWebServiceProvider());
    context.getRequest().setAttribute("currentSuggestionBox", context.getSuggestionBox());
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
   * Prepares the rendering of the all suggestions screen.
   * @param context the context of the incoming request.
   */
  @GET
  @Path(PATH_SUGGESTIONS_ALL)
  @NavigationStep(identifier = SUGGESTION_LIST_NS_ID,
      contextIdentifier = "AllSuggestions")
  @RedirectToInternalJsp("suggestionList.jsp")
  @InvokeAfter("isEdito")
  public void listAllSuggestions(SuggestionBoxWebRequestContext context) {
    List<SuggestionEntity> suggestions = getWebServiceProvider().getAllSuggestionsFor(context.
        getSuggestionBox(), context.getUser());
    context.getRequest().setAttribute("suggestions", suggestions);
  }

  /**
   * Prepares the rendering of the published suggestions screen.
   * @param context the context of the incoming request.
   */
  @GET
  @Path(PATH_SUGGESTIONS_PUBLISHED)
  @NavigationStep(identifier = SUGGESTION_LIST_NS_ID,
      contextIdentifier = "PublishedSuggestions")
  @RedirectToInternalJsp("suggestionList.jsp")
  @InvokeAfter("isEdito")
  public void listPublishedSuggestions(SuggestionBoxWebRequestContext context) {
    List<SuggestionEntity> suggestions = getWebServiceProvider().getPublishedSuggestions(context.
        getSuggestionBox());
    context.getRequest().setAttribute("suggestions", suggestions);
  }

  /**
   * Asks for viewing the suggestions in pending validation. It renders an HTML page with all the
   * suggestions waiting for the validation from a publisher.
   * @param context the context of the incoming request.
   */
  @GET
  @Path(PATH_SUGGESTIONS_PENDING)
  @NavigationStep(identifier = SUGGESTION_LIST_NS_ID, contextIdentifier = "SuggestionsInValidation")
  @RedirectToInternalJsp("suggestionList.jsp")
  @InvokeAfter("isEdito")
  @LowestRoleAccess(SilverpeasRole.publisher)
  public void listSuggestionsInPendingValidation(SuggestionBoxWebRequestContext context) {
    List<SuggestionEntity> suggestions =
        getWebServiceProvider().getSuggestionsForValidation(context.getSuggestionBox());
    context.getRequest().setAttribute("suggestions", suggestions);
  }

  /**
   * Asks for viewing the suggestions proposed by the current user. It renders an HTML page with
   * all
   * the suggestions of the current user.
   * @param context the context of the incoming request.
   */
  @GET
  @Path(PATH_SUGGESTIONS_MINE)
  @NavigationStep(identifier = SUGGESTION_LIST_NS_ID, contextIdentifier = "MySuggestions")
  @RedirectToInternalJsp("suggestionList.jsp")
  @InvokeAfter("isEdito")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void listCurrentUserSuggestions(SuggestionBoxWebRequestContext context) {
    List<SuggestionEntity> suggestions = getWebServiceProvider()
        .getAllSuggestionsProposedBy(context.getSuggestionBox(), context.getUser());
    context.getRequest().setAttribute("suggestions", suggestions);
  }

  /**
   * Handles the incoming from a search result URL.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("searchResult")
  @RedirectToInternal("suggestions/{id}")
  public void searchResult(SuggestionBoxWebRequestContext context) {
    context.getNavigationContext().clear();
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
      SubscriptionService subscriptionService = SubscriptionServiceFactory.getFactory().
          getSubscribeService();
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
  @Path("suggestions/new")
  @RedirectToInternalJsp("suggestionEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void newSuggestion(SuggestionBoxWebRequestContext context) {
  }

  /**
   * Adds a new suggestion into the current suggestion box. The suggestion's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("suggestions/add")
  @RedirectToInternal("suggestions/{id}")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void addSuggestion(SuggestionBoxWebRequestContext context) {
    SuggestionBox suggestionBox = context.getSuggestionBox();
    String title = context.getRequest().getParameter("title");
    String content = context.getRequest().getParameter("content");
    Suggestion suggestion = new Suggestion(title);
    suggestion.setContent(content);
    suggestion.setCreator(context.getUser());
    suggestionBox.getSuggestions().add(suggestion, context.getRequest().getUploadedFiles());
    context.getMessager()
        .addSuccess(getMultilang()
            .getString("suggestionBox.message.suggestion.created")).setDisplayLiveTime(
        getUserNotificationDisplayLiveTimeForLongMessage());
    context.addRedirectVariable("id", suggestion.getId());
  }

  /**
   * Asks for viewing the suggestion identified by the identifier specified in the URL. It
   * renders an HTML page with all the information about
   * a given suggestion. If the suggestion isn't published, it can be then modified.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("suggestions/{id}")
  @NavigationStep(identifier = SUGGESTION_VIEW_NS_ID)
  @RedirectToInternalJsp("suggestionView.jsp")
  public void viewSuggestion(SuggestionBoxWebRequestContext context) {
    String suggestionId = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(suggestionId);
    if (suggestion.isDefined()) {
      SuggestionEntity entity = getWebServiceProvider().asWebEntity(suggestion);
      context.getRequest().setAttribute("suggestion", entity);
      boolean isPublishable = entity.isPublishableBy(context.getUser());
      boolean isModeratorView = entity.getValidation().isPendingValidation() && context.
          getGreaterUserRole().isGreaterThanOrEquals(SilverpeasRole.publisher);
      context.getRequest().setAttribute("isModeratorView", isModeratorView);
      context.getRequest().setAttribute("isPublishable", isPublishable);
      context.getRequest().setAttribute("isEditable", (isPublishable || isModeratorView));
    } else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    context.getNavigationContext().navigationStepFrom(SUGGESTION_VIEW_NS_ID)
        .withLabel(StringUtil.truncate(suggestion.getTitle(), 50))
        .setUriMustBeUsedByBrowseBar(false);
  }

  /**
   * Asks for editing an existing suggestion. It renders an HTML page to modify the content of the
   * suggestion.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("suggestions/{id}/edit")
  @RedirectToInternalJsp("suggestionEdit.jsp")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void editSuggestion(SuggestionBoxWebRequestContext context) {
    String suggestionId = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(suggestionId);
    if (suggestion.isDefined()) {
      if ((suggestion.getValidation().isInDraft() || suggestion.getValidation().isRefused())) {
        checkAdminAccessOrUserIsCreator(context.getUser(), suggestion);
      } else if (suggestion.getValidation().isPendingValidation()) {
        checkAdminAccessOrUserIsModerator(context.getUser(), suggestionBox);
      } else {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      SuggestionEntity entity = getWebServiceProvider().asWebEntity(suggestion);
      context.getRequest().setAttribute("suggestion", entity);
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
  @Path("suggestions/{id}")
  @RedirectToInternal("suggestions/{id}")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void updateSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    if (suggestion.isDefined()) {
      if ((suggestion.getValidation().isInDraft() || suggestion.getValidation().isRefused())) {
        checkAdminAccessOrUserIsCreator(context.getUser(), suggestion);
      } else if (suggestion.getValidation().isPendingValidation()) {
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
  @Path("suggestions/{id}/delete")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void deleteSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    getWebServiceProvider().deleteSuggestion(suggestionBox, suggestion, context.getUser());
  }

  @POST
  @Path("suggestions/{id}/publish")
  @RedirectToPreviousNavigationStep
  @LowestRoleAccess(SilverpeasRole.writer)
  public void publishSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    getWebServiceProvider().publishSuggestion(suggestionBox, suggestion, context.getUser());
  }

  @POST
  @Path("suggestions/{id}/approve")
  @RedirectToPreviousNavigationStep
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
  @Path("suggestions/{id}/refuse")
  @RedirectToPreviousNavigationStep
  @LowestRoleAccess(SilverpeasRole.publisher)
  public void refuseSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    String validationComment = context.getRequest().getParameter("comment");
    getWebServiceProvider()
        .refuseSuggestion(suggestionBox, suggestion, validationComment, context.getUser());
  }

  @GET
  @Path("suggestions/{id}/notify")
  public Navigation notifyManuallyUsersGroups(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);

    AlertUser sel = context.getUserManualNotificationForParameterization();
    sel.resetAll();

    // Browsebar settings
    sel.setHostSpaceName(context.getSpaceLabel());
    sel.setHostComponentId(context.getComponentInstanceId());
    PairObject hostComponentName = new PairObject(context.getComponentInstanceLabel(), null);
    sel.setHostComponentName(hostComponentName);

    // The notification
    sel.setNotificationMetaData(UserNotificationHelper
        .build(new SuggestionNotifyManuallyUserNotification(suggestion, context.getUser())));

    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(context.getComponentInstanceId());
    sel.setSelectionUsersGroups(sug);
    return context.redirectToNotifyManuallyUsers();
  }
}
