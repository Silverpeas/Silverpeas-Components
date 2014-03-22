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

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import org.silverpeas.components.suggestionbox.model.Suggestion;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_BASE_URI;
import static org.silverpeas.components.suggestionbox.web.SuggestionBoxResourceURIs.BOX_SUGGESTION_URI_PART;

import javax.ws.rs.DELETE;

/**
 * A REST Web resource giving suggestion data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(BOX_BASE_URI + "/{componentInstanceId}/{suggestionBoxId}")
@Authorized
public class SuggestionBoxResource extends AbstractSuggestionBoxResource {

  /**
   * Gets the JSON representation of an suggestion.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param suggestionId the identifier of the suggestion
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * suggestion.
   * @see com.silverpeas.web.RESTWebService.WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/{suggestionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public SuggestionEntity getSuggestion(@PathParam("suggestionId") final String suggestionId) {
    return process(new WebTreatment<SuggestionEntity>() {
      @Override
      public SuggestionEntity execute() {
        final Suggestion suggestion = getSuggestionBox().getSuggestions().get(suggestionId);
        return asWebEntity(suggestion);
      }
    }).execute();
  }

  /**
   * Deletes the suggestion identified by the specified identifier.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param suggestionId the identifier of the suggestion.
   */
  @DELETE
  @Path(BOX_SUGGESTION_URI_PART + "/{suggestionId}")
  public void deleteSuggestion(@PathParam("suggestionId") final String suggestionId) {
    process(new WebTreatment<Void>() {
      @Override
      public Void execute() {
        final Suggestion suggestion = getSuggestionBox().getSuggestions().get(suggestionId);
        assertSuggestionIsDefined(suggestion);
        getSuggestionBox().getSuggestions().remove(suggestion);
        return null;
      }
    }).execute();
  }

  /**
   * Gets the JSON representation of a list of suggestion that are not published for the user
   * behind the service call.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * list of suggestions.
   * @see org.silverpeas.components.suggestionbox.model.SuggestionBox
   * .Suggestions#findNotPublishedFor(com.stratelia.webactiv.beans.admin.UserDetail)
   * @see com.silverpeas.web.RESTWebService.WebProcess#execute()
   */
  @GET
  @Path(BOX_SUGGESTION_URI_PART + "/notPublished")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SuggestionEntity> getNotPublished() {
    return process(new WebTreatment<List<SuggestionEntity>>() {
      @Override
      public List<SuggestionEntity> execute() {
        return asWebEntities(
            getSuggestionBox().getSuggestions().findNotPublishedFor(getUserDetail()));
      }
    }).execute();
  }
}
