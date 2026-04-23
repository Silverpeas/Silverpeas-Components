/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.components.delegatednews.web;

import org.silverpeas.components.delegatednews.DelegatedNewsRuntimeException;
import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * A REST Web resource representing a given delegated news. It is a web service that provides access
 * to a delegated news referenced by its URL.
 */
@WebService
@Path(ListDelegatedNewsResource.PATH + "/{instanceId}")
@Authorized
public class ListDelegatedNewsResource extends RESTWebService {

  static final String PATH = "delegatednews";

  @Inject
  private DelegatedNewsService delegatednewsService;
  @PathParam("instanceId")
  private String instanceId;

  /**
   * Gets a business service on delegatedNews.
   *
   * @return a delegatedNews service instance.
   */
  protected DelegatedNewsService getDelegatedNewsService() {
    return this.delegatednewsService;
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return this.instanceId;
  }

  /**
   * Updates order or delete the delegatedNews from the JSON representation. If the user isn't
   * authenticated, a 401 HTTP code is returned. If the user isn't authorized to save the delegated
   * news, a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   *
   * @param newDelegatedNews an array of delegated news to update order or to delete
   * @return the new list of delegated news after update or delete
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<DelegatedNewsEntity> updateDelegatedNews(final DelegatedNewsEntity[] newDelegatedNews) {
    List<DelegatedNewsEntity> updatedEntities;
    List<DelegatedNews> initialListDelegatedNews = getDelegatedNewsService().getAllDelegatedNews();
    try {
      if (initialListDelegatedNews.size() == newDelegatedNews.length) {// Update Order
        updatedEntities = updateOrder(newDelegatedNews);
      } else if (initialListDelegatedNews.size() > newDelegatedNews.length) {// Delete
        deleteList(newDelegatedNews, initialListDelegatedNews);
        updatedEntities = List.of(newDelegatedNews);
      } else {
        updatedEntities = List.of();
      }
    } catch (DelegatedNewsRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
    return updatedEntities;
  }

  private void deleteList(final DelegatedNewsEntity[] newDelegatedNews,
      List<DelegatedNews> initialListDelegatedNews) {
    boolean found;
    for (DelegatedNews delegatedNews : initialListDelegatedNews) {
      String pubId = delegatedNews.getPubId();
      found = false;
      for (DelegatedNewsEntity delegatedNewsEntity : newDelegatedNews) {
        // the new tab of DelegatedNewsEntity without the delegated news deleted
        if (pubId.equals(delegatedNewsEntity.getPubId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        getDelegatedNewsService().deleteDelegatedNews(pubId); // delete the delegatedNews
      }
    }
  }

  private List<DelegatedNewsEntity> updateOrder(final DelegatedNewsEntity[] newDelegatedNews) {
    List<DelegatedNewsEntity> results = new ArrayList<>(newDelegatedNews.length);
    int order = 0;
    for (DelegatedNewsEntity delegatedNewsEntity : newDelegatedNews) {
      // the tab of DelegatedNewsEntity is in the new order
      DelegatedNews delegatedNews = delegatedNewsEntity.toDelegatedNews();
      DelegatedNews delegatedNewsUpdated =
          getDelegatedNewsService().updateOrderDelegatedNews(delegatedNews.getPubId(), order);
      results.add(DelegatedNewsEntity.fromDelegatedNews(delegatedNewsUpdated));
      order++;
    }
    return results;
  }
}
