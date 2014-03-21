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
package org.silverpeas.components.suggestionbox.web;

import com.silverpeas.web.RESTWebService;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_BASE_URI;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs
    .BOX_SUGGESTION_URI_PART;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractSuggestionBoxResource extends RESTWebService {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @PathParam("suggestionBoxId")
  private String suggestionBoxId;

  private SuggestionBox suggestionBox;

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Gets the suggestion box associated to the current request.
   * @return
   */
  public SuggestionBox getSuggestionBox() {
    if (suggestionBox == null) {
      suggestionBox = SuggestionBox.getByComponentInstanceId(getComponentId());
    }
    return suggestionBox;
  }

  /**
   * Converts the list of suggestion into list of suggestion web entities.
   * @param suggestions the suggestions to convert.
   * @return the suggestion web entities.
   */
  protected List<SuggestionEntity> asWebEntities(Collection<Suggestion> suggestions) {
    List<SuggestionEntity> entities = new ArrayList<SuggestionEntity>(suggestions.size());
    for (Suggestion suggestion : suggestions) {
      entities.add(asWebEntity(suggestion));
    }
    return entities;
  }

  /**
   * Converts the suggestion into its corresponding web entity.
   * @param suggestion the suggestion to convert.
   * @return the corresponding suggestion entity.
   */
  protected SuggestionEntity asWebEntity(Suggestion suggestion) {
    checkNotFoundStatus(suggestion);
    return SuggestionEntity.fromSuggestion(suggestion).withURI(buildSuggestionURI(suggestion));
  }

  /**
   * Centralized the build of a suggestion URI.
   * @param suggestion the aimed suggestion.
   * @return the URI of specified suggestion.
   */
  protected URI buildSuggestionURI(Suggestion suggestion) {
    if (suggestion == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(BOX_BASE_URI).path(getComponentId())
        .path(suggestion.getSuggestionBox().getId()).path(BOX_SUGGESTION_URI_PART)
        .path(suggestion.getId()).build();
  }

  /**
   * Centralization
   * @param object any object
   */
  private void checkNotFoundStatus(Object object) {
    if (object == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }
}
