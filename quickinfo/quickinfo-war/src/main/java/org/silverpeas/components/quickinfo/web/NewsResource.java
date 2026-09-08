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
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authorized;
import org.silverpeas.core.web.rs.annotation.doc.NotFound;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;

@WebService
@Path(AbstractNewsResource.PATH + "/{componentId}")
@Authorized
public class NewsResource extends AbstractNewsResource {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets all the news published in the application. A contributor gets them all whereas a reader
   * gets only those that are currently visible.
   *
   * @return a list with the news the user can see.
   */
  @Operation(summary = "Gets all the news published in the application.",
      description = "A contributor gets all the news whereas a reader gets only those that are " +
          "currently visible.")
  @ApiResponse(responseCode = "200", description = "The news the user can see.",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = NewsEntity.class))))
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<NewsEntity> getAllNews() {
    final List<News> allNews;
    if (isContributor()) {
      allNews = getService().getAllNews(componentId);
    } else {
      allNews = getService().getVisibleNews(componentId);
    }
    return asWebEntities(allNews, true, 0);
  }
  
  /**
   * Gets the specified existing news.
   *
   * @param onNewsId the unique identifier of the news.
   * @return the asked news.
   */
  @Operation(summary = "Gets the specified existing news.")
  @ApiResponse(responseCode = "200", description = "The asked news.",
      content = @Content(schema = @Schema(implementation = NewsEntity.class)))
  @NotFound
  @GET
  @Path("{newsId}")
  @Produces(MediaType.APPLICATION_JSON)
  public NewsEntity getNews(@PathParam("newsId") String onNewsId) {
    try {
      News news = getNewsById(onNewsId);
      return asWebEntity(news, true);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Acknowledges the specified news has been read by the user behind the request.
   *
   * @param onNewsId the unique identifier of the news.
   */
  @Operation(summary = "Acknowledges the given news has been read.",
      description = "The acknowledgement is recorded for the user behind the request; it is " +
          "what feeds the readership statistics of the news.")
  @ApiResponse(responseCode = "204", description = "The acknowledgement has been recorded.")
  @POST
  @Path("{newsId}/acknowledge")
  public void acknowledge(@PathParam("newsId") String onNewsId) {
    try {
      getService().acknowledgeNews(onNewsId, getUser().getId());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Deletes the specified news. Only a contributor can delete a news.
   *
   * @param onNewsId the unique identifier of the news.
   * @return the response to the HTTP DELETE request.
   */
  @Operation(summary = "Deletes the given news.",
      description = "Only a contributor of the application can delete a news.")
  @ApiResponse(responseCode = "200", description = "The news has been deleted.")
  @DELETE
  @Path("{newsId}")
  public Response deleteNews(@PathParam("newsId") String onNewsId) {
    try {
      if (!isContributor()) {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      getService().removeNews(onNewsId);
      return Response.ok().build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private News getNewsById(String id) {
    News news = getService().getNews(id);
    if (news == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return news;
  }

  private boolean isContributor() {
    return getHighestUserRole().isGreaterThanOrEquals(SilverpeasRole.PUBLISHER);
  }

}