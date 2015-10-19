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
import com.silverpeas.util.comparator.AbstractComplexComparator;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCollection;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.util.NotifierUtil;
import org.silverpeas.util.PaginationList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.components.suggestionbox.SuggestionBoxComponentSettings
    .getUserNotificationDisplayLiveTimeForLongMessage;
import static org.silverpeas.contribution.ContributionStatus.PENDING_VALIDATION;

/**
 * @author: Yohann Chastagnier
 */
public class SuggestionBoxWebServiceProvider {

  /**
   * Multilang
   */
  private final Map<String, ResourceLocator> multilang = new HashMap<String, ResourceLocator>();
  private final static SuggestionBoxWebServiceProvider SUGGESTION_BOX_WEB_SERVICE_PROVIDER =
      new SuggestionBoxWebServiceProvider();

  private final static List<SilverpeasRole> MODERATOR_ROLES =
      CollectionUtil.asList(SilverpeasRole.admin, SilverpeasRole.publisher);

  public static SuggestionBoxWebServiceProvider getWebServiceProvider() {
    return SUGGESTION_BOX_WEB_SERVICE_PROVIDER;
  }

  private SuggestionBoxWebServiceProvider() {
  }

  /**
   * Gets the list of suggestions that are in draft or refused and which the creator is those
   * specified.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param creator the user that must be the creator of the returned suggestions.
   * @return the aimed suggestion entities.
   * @see SuggestionCollection#findInDraftFor
   * (UserDetail)
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
   * @see SuggestionCollection#findOutOfDraftFor
   * (UserDetail)
   */
  public List<SuggestionEntity> getSuggestionsOutOfDraftFor(SuggestionBox suggestionBox,
      UserDetail creator) {
    return asWebEntities(suggestionBox.getSuggestions().findOutOfDraftFor(creator));
  }

  /**
   * Gets the list of suggestions that are published and which the creator is those specified.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param creator the user that must be the creator of the returned suggestions.
   * @return the aimed suggestion entities.
   * @see SuggestionCollection#findPublishedFor
   * (UserDetail)
   */
  public List<SuggestionEntity> getPublishedSuggestionsFor(SuggestionBox suggestionBox,
      UserDetail creator) {
    return asWebEntities(suggestionBox.getSuggestions().findPublishedFor(creator));
  }

  /**
   * Gets the list of all the suggestions in the specified suggestion box proposed by the specified
   * creator.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param creator the user that must be the creator of the returned suggestions.
   * @return the asked suggestion entities.
   * @see SuggestionCollection#findAllProposedBy(UserDetail)
   */
  public List<SuggestionEntity> getAllSuggestionsProposedBy(SuggestionBox suggestionBox,
      UserDetail creator) {
    return asWebEntities(suggestionBox.getSuggestions().findAllProposedBy(creator));
  }

  /**
   * Gets the list of suggestions that are in pending validation and which.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the aimed suggestion entities.
   * @see SuggestionCollection#findPendingValidation()
   */
  public List<SuggestionEntity> getSuggestionsInPendingValidation(SuggestionBox suggestionBox) {
    return asWebEntities(suggestionBox.getSuggestions().findPendingValidation());
  }

  /**
   * Gets the list of suggestions that are out of draft, awaiting their validation by a publisher.
   * These suggestions are made up of those in pending validation and those refused by a
   * publisher.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the aimed suggestion entities.
   */
  public List<SuggestionEntity> getSuggestionsForValidation(SuggestionBox suggestionBox) {
    return asWebEntities(suggestionBox.getSuggestions().findInStatus(PENDING_VALIDATION));
  }

  /**
   * Gets the list of suggestions that match the specified criteria.
   * The criteria are applying in web level and aren't propagated downto the business level and
   * hence the persistence level.
   * <p/>
   * The user asking for the suggestions is required in the criteria as some caching is performed
   * for the given user for better performance.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param criteria the criteria the suggestions to return must match.
   * @return the published suggestion entities matching the specified criteria.
   * @see SuggestionCollection#findPublished()
   */
  public List<SuggestionEntity> getSuggestionsByCriteria(SuggestionBox suggestionBox,
      final SuggestionCriteria criteria) {
    SuggestionFinderByCriteria suggestionsFinder = new SuggestionFinderByCriteria();
    criteria.processWith(suggestionsFinder);
    return asWebEntities(suggestionsFinder.result());
  }

  /**
   * Gets the list of suggestions that are published.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the published suggestion entities.
   * @see SuggestionCollection#findPublished()
   */
  public List<SuggestionEntity> getPublishedSuggestions(SuggestionBox suggestionBox) {
    return asWebEntities(suggestionBox.getSuggestions().findPublished());
  }

  /**
   * Gets the list of all the suggestions in the specified suggestion box that a user can see.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param user the user that requests for all suggestions.
   * @return the asked suggestion entities.
   * @see SuggestionCollection#findAllProposedBy(UserDetail)
   */
  public List<SuggestionEntity> getAllSuggestionsFor(SuggestionBox suggestionBox, UserDetail user) {
    Map<String, SuggestionEntity> uniqueSuggestionResult = new HashMap<String, SuggestionEntity>();
    // Suggestions proposed by the user
    for (SuggestionEntity entity : getAllSuggestionsProposedBy(suggestionBox, user)) {
      uniqueSuggestionResult.put(entity.getId(), entity);
    }
    // Published suggestions
    for (SuggestionEntity entity : getPublishedSuggestions(suggestionBox)) {
      uniqueSuggestionResult.put(entity.getId(), entity);
    }
    // The suggestions that are pending validation
    try {
      checkAdminAccessOrUserIsModerator(user, suggestionBox);
      for (SuggestionEntity entity : getSuggestionsInPendingValidation(suggestionBox)) {
        uniqueSuggestionResult.put(entity.getId(), entity);
      }
    } catch (Exception ignore) {
      // If the user has no admin or publisher rights, no suggestion in pending validation are
      // retrieved
    }
    // The final result
    List<SuggestionEntity> finalSuggestionResult =
        new ArrayList<SuggestionEntity>(uniqueSuggestionResult.values());
    // Sorting the result by descending last update date
    Collections.sort(finalSuggestionResult, new AbstractComplexComparator<SuggestionEntity>() {
      @Override
      protected ValueBuffer getValuesToCompare(final SuggestionEntity object) {
        return new ValueBuffer().append(object.getLastUpdateDate(), false);
      }
    });
    // Returning the result
    return finalSuggestionResult;
  }

  /**
   * Deletes a suggestion.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param suggestion the suggestion to delete.
   * @param fromUser the current user.
   * @see SuggestionCollection#remove(Object)
   */
  public void deleteSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      UserDetail fromUser) {
    SilverpeasRole highestRole = getHighestUserRoleFrom(fromUser, suggestionBox);
    if (suggestion.isDefined() &&
        (suggestion.getValidation().isInDraft() || suggestion.getValidation().isRefused() ||
            ((suggestion.getValidation().isPendingValidation() ||
                suggestion.getValidation().isValidated()) &&
                (fromUser.isAccessAdmin() || SilverpeasRole.admin == highestRole)))) {
      checkAdminAccessOrAdminRoleOrUserIsCreator(fromUser, suggestion);
      boolean removed = suggestionBox.getSuggestions().remove(suggestion);
      UserPreferences userPreferences = fromUser.getUserPreferences();
      if (removed) {
        NotifierUtil.addSuccess(getStringTranslation("suggestionBox.message.suggestion.removed",
            userPreferences.getLanguage()));
      } else {
        NotifierUtil.addWarning(getStringTranslation("suggestionBox.message.suggestion.notRemoved",
            userPreferences.getLanguage()));
      }
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
   * @see SuggestionCollection#publish(Suggestion)
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
          NotifierUtil.addSuccess(getStringTranslation("suggestionBox.message.suggestion.published",
              userPreferences.getLanguage()))
              .setDisplayLiveTime(getUserNotificationDisplayLiveTimeForLongMessage());
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
   * @see SuggestionCollection#validate(Suggestion,
   * ContributionValidation)
   */
  private SuggestionEntity validateSuggestion(SuggestionBox suggestionBox, Suggestion suggestion,
      ContributionStatus newStatus, String validationComment, UserDetail fromUser) {
    if (suggestion.isDefined() && suggestion.getValidation().isPendingValidation()) {
      checkAdminAccessOrUserIsModerator(fromUser, suggestionBox);
      UserPreferences userPreferences = fromUser.getUserPreferences();
      if (newStatus.isRefused() && StringUtil.isNotDefined(validationComment)) {
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }
      ContributionValidation validation =
          new ContributionValidation(newStatus, fromUser, new Date(), validationComment);
      suggestion.setLastUpdater(fromUser);
      Suggestion actual = suggestionBox.getSuggestions().validate(suggestion, validation);
      switch (actual.getValidation().getStatus()) {
        case REFUSED:
          NotifierUtil.addInfo(MessageFormat.format(
              getStringTranslation("suggestionBox.message.suggestion.refused",
                  userPreferences.getLanguage()), suggestion.getTitle()
          )).setDisplayLiveTime(getUserNotificationDisplayLiveTimeForLongMessage());
          break;
        case VALIDATED:
          NotifierUtil.addSuccess(MessageFormat.format(
              getStringTranslation("suggestionBox.message.suggestion.validated",
                  userPreferences.getLanguage()), suggestion.getTitle()
          )).setDisplayLiveTime(getUserNotificationDisplayLiveTimeForLongMessage());
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
   * Centralization of checking if the specified user is the creator of the specified suggestion.
   * @param user the user to verify.
   * @param suggestion the suggestion to check.
   */
  public static void checkAdminAccessOrAdminRoleOrUserIsCreator(UserDetail user,
      Suggestion suggestion) {
    assertSuggestionIsDefined(suggestion);
    if (!user.isAccessAdmin() && !user.equals(suggestion.getCreator())) {
      SilverpeasRole highestRole = getHighestUserRoleFrom(user, suggestion.getSuggestionBox());
      if (SilverpeasRole.admin != highestRole) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
    }
  }

  /**
   * Centralization of checking if the specified user is a moderator of the specified suggestion.
   * @param user the user to verify.
   * @param suggestionBox the suggestion box the user is working on.
   */
  public static void checkAdminAccessOrUserIsModerator(UserDetail user,
      SuggestionBox suggestionBox) {
    SilverpeasRole highestRole = getHighestUserRoleFrom(user, suggestionBox);
    if (!user.isAccessAdmin() && !MODERATOR_ROLES.contains(highestRole)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the highest role the given user has on the given suggestion box.
   * @param user the user for which the highest role is requested.
   * @param suggestionBox the suggestion box from which the roles must be searched for.
   * @return a {@link SilverpeasRole} that represents the highest role the user has on the
   * suggestion box.
   */
  private static SilverpeasRole getHighestUserRoleFrom(UserDetail user,
      SuggestionBox suggestionBox) {
    return SilverpeasRole.getGreatestFrom(SilverpeasRole.from(
        OrganisationControllerFactory.getOrganisationController()
            .getUserProfiles(user.getId(), suggestionBox.getComponentInstanceId())));
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
    return (suggestions instanceof PaginationList ?
        PaginationList.from(entities, ((PaginationList) suggestions).maxSize()) : entities);
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
    return SuggestionEntity.fromSuggestion(suggestion);
  }
}
