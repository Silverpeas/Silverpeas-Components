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
import com.silverpeas.web.RESTWebService;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.util.NotifierUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_BASE_URI;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs
    .BOX_SUGGESTION_URI_PART;

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

  public static SuggestionBoxWebServiceProvider getWebServiceProvider() {
    return SUGGESTION_BOX_WEB_SERVICE_PROVIDER;
  }

  private SuggestionBoxWebServiceProvider() {

  }

  /**
   * Gets the list of suggestions that are not published (draft end refused status) and which
   * the creator is those specified.
   * @param suggestionBox the suggestion box the current user is working on.
   * @param creator the user that must be the creator of the returned suggestions.
   * @return the aimed suggestion entities.
   * @see SuggestionBox.Suggestions#findNotPublishedFor(UserDetail)
   */
  public List<SuggestionEntity> getNotPublishedFor(SuggestionBox suggestionBox,
      UserDetail creator) {
    return asWebEntities(suggestionBox.getSuggestions().findNotPublishedFor(creator));
  }

  /**
   * Gets the list of suggestions that are pending validation and which.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the aimed suggestion entities.
   * @see SuggestionBox.Suggestions#findPendingValidation()
   */
  public List<SuggestionEntity> getPendingValidation(SuggestionBox suggestionBox) {
    return asWebEntities(suggestionBox.getSuggestions().findPendingValidation());
  }

  /**
   * Gets the list of suggestions that are published.
   * @param suggestionBox the suggestion box the current user is working on.
   * @return the aimed suggestion entities.
   * @see SuggestionBox.Suggestions#findPublished()
   */
  public List<SuggestionEntity> getPublished(SuggestionBox suggestionBox) {
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
    if (suggestion.isDefined() && (suggestion.isInDraft() || suggestion.isRefused())) {
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
    if (suggestion.isDefined() && (suggestion.isInDraft() || suggestion.isRefused())) {
      checkAdminAccessOrUserIsCreator(fromUser, suggestion);
      suggestion.setLastUpdater(fromUser);
      Suggestion actual = suggestionBox.getSuggestions().publish(suggestion);
      UserPreferences userPreferences = fromUser.getUserPreferences();
      switch (actual.getStatus()) {
        case PENDING_VALIDATION:
          NotifierUtil.addInfo(
              getStringTranslation("suggestionBox.message.suggestion.pendingValidation",
                  userPreferences.getLanguage())
          );
          break;
        case VALIDATED:
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
    return rl.getString(key, null);
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
}
