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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@WebService
@Path(AbstractNewsResource.PATH + "/ticker")
@Authenticated
public class TickerResource extends AbstractNewsResource {

  @Override
  public String getComponentId() {
    return null;
  }

  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation) {
    super.validateUserAuthentication(
        validation.skipLastUserAccessTimeRegistering(getHttpServletRequest()));
  }

  /**
   * Gets the news to scroll in the ticker of the user behind the request. These news come from all
   * the applications the user can access.
   *
   * @param limit the maximum count of news to get; no limit if not set.
   * @return a list with the news to scroll in the ticker of the user.
   */
  @Operation(summary = "Gets the news to scroll in the ticker of the user.",
      description = "The news come from all the applications the user behind the request can " +
          "access. Asking them doesn't update the date of the last access of the user.")
  @ApiResponse(responseCode = "200", description = "The news to scroll in the ticker.",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = NewsEntity.class))))
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<NewsEntity> getTickerNews(final @QueryParam("limit") Integer limit) {
    final List<News> newsForTicker = getService().getNewsForTicker(getUser().getId());
    return asWebEntities(newsForTicker, false, limit != null ? limit : 0);
  }
}