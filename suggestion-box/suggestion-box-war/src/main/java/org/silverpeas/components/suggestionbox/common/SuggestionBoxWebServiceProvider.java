/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.suggestionbox.common;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.cache.service.CacheService;
import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.util.NotifierUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_BASE_URI;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_SUGGESTION_URI_PART;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author: Yohann Chastagnier
 */
public class SuggestionBoxWebServiceProvider {

  /**
   * Multilang
   */
  private final Map<String, ResourceLocator> multilang = new HashMap<String, ResourceLocator>();
  private final static SuggestionBoxWebServiceProvider SUGGESTION_BOX_WEB_SERVICE_PROVIDER
      = new SuggestionBoxWebServiceProvider();

  private String publishedSuggestionsCache;

  public static SuggestionBoxWebServiceProvider getWebServiceProvider() {
    return SUGGESTION_BOX_WEB_SERVICE_PROVIDER;
  }

  private SuggestionBoxWebServiceProvider() {
  }

  /**
   * Gets the list of suggestions that are in draft and which the creator is those specified.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param creator the user that must be the creator of the returned suggestions.
   * @return the aimed suggestion entities.
   * @see SuggestionBox.Suggestions#findInDraftFor(UserDetail)
   */
  public List<SuggestionEntity> getSuggestionsInDraftFor(SuggestionBox suggestionBox,
      UserDetail creator) {
    return asWebEntities(suggestionBox.getSuggestions().findInDraftFor(creator));
  }

  /**
   * Gets the list of suggestions that are out of draft and which the creator is those specified.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param creator the user that must be the creator of the returned suggestions.
   * @return the aimed suggestion entities.
   * @see SuggestionBox.Suggestions#findInDraftFor(UserDetail)
   */
  public List<SuggestionEntity> getSuggestionsOutOfDraftFor(SuggestionBox suggestionBox,
      UserDetail creator) {
    return asWebEntities(suggestionBox.getSuggestions().findOutOfDraftFor(creator));
  }

  /**
   * Gets the list of suggestions that are pending validation and which.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the aimed suggestion entities.
   * @see SuggestionBox.Suggestions#findPendingValidation()
   */
  public List<SuggestionEntity> getSuggestionsInPendingValidation(SuggestionBox suggestionBox) {
    return asWebEntities(suggestionBox.getSuggestions().findPendingValidation());
  }

  /**
   * Gets the list of suggestions that are published and that match the specified criteria.
   * The criteria are applying in web level and aren't propagated downto the business level and
   * hence the persistence level.
   * <p>
   * The user asking for the suggestions is required in the criteria as some caching is performed
   * for the given user for better performence.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param criteria the criteria the suggestions to return must match.
   * @return the published suggestion entities matching the specified criteria.
   * @see SuggestionBox.Suggestions#findPublished()
   */
  public List<SuggestionEntity> getPublishedSuggestions(SuggestionBox suggestionBox,
      final SuggestionCriteria criteria) {
    Map<String, List<SuggestionEntity>> perUserSuggestions
        = getSortedAndPublishedSuggestionsPerUser();
    List<SuggestionEntity> suggestions = perUserSuggestions.get(criteria.getCaller().getId());
    if (suggestions == null) {
      suggestions = asWebEntities(suggestionBox.getSuggestions().findPublished());
      perUserSuggestions.put(criteria.getCaller().getId(), suggestions);
    }
    SuggestionCriteriaApplier applier = new SuggestionCriteriaApplier(suggestions);
    criteria.processWith(applier);
    return applier.result();
  }

  /**
   * Gets the list of suggestions that are published.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the published suggestion entities.
   * @see SuggestionBox.Suggestions#findPublished()
   */
  public List<SuggestionEntity> getPublishedSuggestions(SuggestionBox suggestionBox) {
    return asWebEntities(suggestionBox.getSuggestions().findPublished());
  }

  /**
   * Deletes a suggestion.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param suggestion the suggestion to delete.
   * @param fromUser the current user.
   * @see SuggestionBox.Suggestions#remove(Suggestion)
   */
  public void deleteSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      UserDetail fromUser) {
    if (suggestion.isDefined() && (suggestion.getValidation().isInDraft() || suggestion.
        getValidation().isRefused())) {
      checkAdminAccessOrUserIsCreator(fromUser, suggestion);
      suggestionBox.getSuggestions().remove(suggestion);
      UserPreferences userPreferences = fromUser.getUserPreferences();
      NotifierUtil.addSuccess(getStringTranslation("suggestionBox.message.suggestion.removed",
          userPreferences.getLanguage()));
    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Publishes a suggestion.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param suggestion the suggestion to publish.
   * @param fromUser the current user.
   * @return the suggestion entity.
   * @see SuggestionBox.Suggestions#publish(Suggestion)
   */
  public SuggestionEntity publishSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      UserDetail fromUser) {
    if (suggestion.isDefined() && (suggestion.getValidation().isInDraft() || suggestion.
        getValidation().isRefused())) {
      checkAdminAccessOrUserIsCreator(fromUser, suggestion);
      suggestion.setLastUpdater(fromUser);
      Suggestion actual = suggestionBox.getSuggestions().publish(suggestion);
      UserPreferences userPreferences = fromUser.getUserPreferences();
      switch (actual.getValidation().getStatus()) {
        case PENDING_VALIDATION:
          NotifierUtil.addInfo(
              getStringTranslation("suggestionBox.message.suggestion.pendingValidation",
                  userPreferences.getLanguage())
          );
          break;
        case VALIDATED:
          getSortedAndPublishedSuggestionsPerUser().clear();
          NotifierUtil.addSuccess(getStringTranslation("suggestionBox.message.suggestion.published",
              userPreferences.getLanguage()));
          break;
      }
      return asWebEntity(actual);
    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Approves a suggestion.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param suggestion the suggestion to approve.
   * @param validationComment the comment associated to the approval.
   * @param fromUser the current user.
   * @return the suggestion entity.
   * @see #validateSuggestion(SuggestionBox, Suggestion, ContributionStatus, String, UserDetail)
   */
  public SuggestionEntity approveSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      String validationComment, UserDetail fromUser) {
    return validateSuggestion(suggestionBox, suggestion, ContributionStatus.VALIDATED,
        validationComment, fromUser);
  }

  /**
   * Refuses a suggestion.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param suggestion the suggestion to refuse.
   * @param validationComment the comment associated to the refusal.
   * @param fromUser the current user.
   * @return the suggestion entity.
   * @see #validateSuggestion(SuggestionBox, Suggestion, ContributionStatus, String, UserDetail)
   */
  public SuggestionEntity refuseSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      String validationComment, UserDetail fromUser) {
    return validateSuggestion(suggestionBox, suggestion, ContributionStatus.REFUSED,
        validationComment, fromUser);
  }

  /**
   * Validate a suggestion.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param suggestion the suggestion to validate.
   * @param newStatus the new status of approval or refusal.
   * @param validationComment the optional comment related to the approval or refusal.
   * @param fromUser the current user.
   * @return the suggestion entity.
   * @see SuggestionBox.Suggestions#validate(Suggestion, ContributionValidation)
   */
  private SuggestionEntity validateSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      ContributionStatus newStatus, String validationComment, UserDetail fromUser) {
    if (suggestion.isDefined() && suggestion.getValidation().isPendingValidation()) {
      checkAdminAccessOrUserIsModerator(fromUser, suggestionBox);
      UserPreferences userPreferences = fromUser.getUserPreferences();
      if (newStatus.isRefused() && StringUtil.isNotDefined(validationComment)) {
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }
      ContributionValidation validation
          = new ContributionValidation(newStatus, fromUser, new Date(), validationComment);
      suggestion.setLastUpdater(fromUser);
      Suggestion actual = suggestionBox.getSuggestions().validate(suggestion, validation);
      switch (actual.getValidation().getStatus()) {
        case REFUSED:
          getSortedAndPublishedSuggestionsPerUser().clear();
          NotifierUtil.addInfo(MessageFormat.format(
              getStringTranslation("suggestionBox.message.suggestion.refused",
                  userPreferences.getLanguage()), suggestion.getTitle()
          ));
          break;
        case VALIDATED:
          getSortedAndPublishedSuggestionsPerUser().clear();
          NotifierUtil.addSuccess(MessageFormat.format(
              getStringTranslation("suggestionBox.message.suggestion.validated",
                  userPreferences.getLanguage()), suggestion.getTitle()
          ));
          break;
      }
      return asWebEntity(actual);
    } else {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Asserts the specified suggestion is well defined, otherwise an HTTP 404 error is sent back.
   * @param suggestion the suggestion to check.
   */
  public static void assertSuggestionIsDefined(final Suggestion suggestion) {
    if (suggestion.isNotDefined()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Centralization of checking if the specified user is the creator of the specified suggestion.
   * @param user the user to verify.
   * @param suggestion the suggestion to check.
   */
  public static void checkAdminAccessOrUserIsCreator(UserDetail user, Suggestion suggestion) {
    assertSuggestionIsDefined(suggestion);
    if (!user.isAccessAdmin() && !user.equals(suggestion.getCreator())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Centralization of checking if the specified user is a moderator of the specified suggestion.
   * @param user the user to verify.
   * @param suggestionBox the suggestion box the user is working on.
   */
  public static void checkAdminAccessOrUserIsModerator(UserDetail user,
      SuggestionBox suggestionBox) {
    Set<String> moderatorIds = CollectionUtil.asSet(
        OrganisationControllerFactory.getOrganisationController()
        .getUsersIdsByRoleNames(suggestionBox.getComponentInstanceId(),
            CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name()))
    );
    if (!user.isAccessAdmin() && !moderatorIds.contains(user.getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the translation of an element
   * @param key
   * @param language
   * @return
   */
  private String getStringTranslation(final String key, final String language) {
    ResourceLocator rl = multilang.get(language);
    if (rl == null) {
      rl = new ResourceLocator(
          "org.silverpeas.components.suggestionbox.multilang.SuggestionBoxBundle", language);
      multilang.put(language, rl);
    }
    return rl.getResourceBundle().getString(key);
  }

  /**
   * Converts the list of suggestion into list of suggestion web entities.
   * @param suggestions the suggestions to convert.
   * @return the suggestion web entities.
   */
  public List<SuggestionEntity> asWebEntities(Collection<Suggestion> suggestions) {
    List<SuggestionEntity> entities = new ArrayList<SuggestionEntity>(suggestions.size());
    for (Suggestion suggestion : suggestions) {
      entities.add(asWebEntity(suggestion));
    }
    return entities;
  }

  /**
   * Converts the suggestion into its corresponding web entity. If the specified suggestion isn't
   * defined, then an HTTP 404 error is sent back instead of the entity representation of the
   * suggestion.
   * @param suggestion the suggestion to convert.
   * @return the corresponding suggestion entity.
   */
  public SuggestionEntity asWebEntity(Suggestion suggestion) {
    assertSuggestionIsDefined(suggestion);
    return SuggestionEntity.fromSuggestion(suggestion).withURI(buildSuggestionURI(suggestion));
  }

  /**
   * Centralized the build of a suggestion URI.
   * @param suggestion the aimed suggestion.
   * @return the URI of specified suggestion.
   */
  protected URI buildSuggestionURI(Suggestion suggestion) {
    if (suggestion == null || suggestion.getSuggestionBox() == null) {
      return null;
    }
    return UriBuilder.fromUri(URLManager.getApplicationURL())
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(BOX_BASE_URI)
        .path(suggestion.getSuggestionBox().getComponentInstanceId())
        .path(suggestion.getSuggestionBox().getId()).path(BOX_SUGGESTION_URI_PART)
        .path(suggestion.getId()).build();
  }

  private Map<String, List<SuggestionEntity>> getSortedAndPublishedSuggestionsPerUser() {
    CacheService cache = CacheServiceFactory.getApplicationCacheService();
    Map<String, List<SuggestionEntity>> perUserSuggestions = cache.get(publishedSuggestionsCache,
        Map.class);
    if (perUserSuggestions == null) {
      initSortedAndPublishedSuggestionsPerUserIn(cache);
      perUserSuggestions = cache.get(publishedSuggestionsCache, Map.class);
    }
    return perUserSuggestions;
  }

  private void initSortedAndPublishedSuggestionsPerUserIn(CacheService cache) {
    Map<String, List<SuggestionEntity>> perUserSuggestions
        = new ConcurrentHashMap<String, List<SuggestionEntity>>();
    publishedSuggestionsCache = cache.add(perUserSuggestions, 10800); // 3 hours of live
  }
}
