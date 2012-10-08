/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.delegatednews.web;
 
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.delegatednews.DelegatedNewsRuntimeException;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.web.RESTWebService;

/**
 * A REST Web resource representing a given delegated news.
 * It is a web service that provides an access to a delegated news referenced by its URL.
 */
@Service
@Scope("request")
@Path("delegatednews/{instanceId}")
@Authorized
public class DelegatedNewsResource extends RESTWebService {

  @Inject
  private DelegatedNewsService delegatednewsService;
  @PathParam("instanceId")
  private String instanceId;

  /**
   * Gets a business service on delegatedNews.
   * @return a delegatedNews service instance.
   */
  protected DelegatedNewsService getDelegatedNewsService() {
    return this.delegatednewsService;
  }

  @Override
  public String getComponentId() {
    return this.instanceId;
  }
  
  /**
   * Updates the delegatedNews Order from the JSON representation.
   * If the user isn't authentified, a 401 HTTP code is returned. 
   * If the user isn't authorized to save the delegated news, a 403 is returned. 
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param tab of ordered delegated news
   * @return the ordered list of delegated news 
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public DelegatedNewsEntity[] updateDelegatedNews(final DelegatedNewsEntity[] delegatedNewsToUpdate) {
    DelegatedNewsEntity[] tabResult = new DelegatedNewsEntity[delegatedNewsToUpdate.length];
    try {
      for(int i=0; i<delegatedNewsToUpdate.length;i++) {//the tab of DelegatedNewsEntity is in the new order
        DelegatedNewsEntity delegatedNewsEntity = delegatedNewsToUpdate[i];
        DelegatedNews delegatedNews = delegatedNewsEntity.toDelegatedNews();
        DelegatedNews delegatedNewsUpdated = getDelegatedNewsService().updateOrderDelegatedNews(delegatedNews.getPubId(), i);
        tabResult[i] = DelegatedNewsEntity.fromDelegatedNews(delegatedNewsUpdated);
      }
    } catch (DelegatedNewsRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
    return tabResult;
  }
  
  /**
   * Deletes the delegatedNews from the JSON representation.
   * If the user isn't authentified, a 401 HTTP code is returned. 
   * If the user isn't authorized to save the delegated news, a 403 is returned. 
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param the initial tab of delegated news and the tab of delegated news to delete 
   * @return the new list of delegated news without the deleted delegated news
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public DelegatedNewsEntity[] deleteDelegatedNews(final DelegatedNewsEntity[] tabDelegatedNews, final String[] tabPubIdToDelete) {
    DelegatedNewsEntity[] tabResult = null;
    Collection<DelegatedNewsEntity> listResult = new ArrayList<DelegatedNewsEntity>();
    boolean deleted = false;
    try {
      for(int i=0; i<tabDelegatedNews.length;i++) {
        deleted = false;
        DelegatedNewsEntity delegatedNewsEntity = tabDelegatedNews[i];
        for(int j=0; j<tabPubIdToDelete.length;j++) {
          String pubIdToDelete = tabPubIdToDelete[j];
          if(pubIdToDelete.equals(Integer.toString(delegatedNewsEntity.getPubId()))) {
            DelegatedNews delegatedNews = delegatedNewsEntity.toDelegatedNews();
            getDelegatedNewsService().deleteDelegatedNews(delegatedNews.getPubId());
            deleted = true;
            break;
          }
        }
        if(! deleted) {
          listResult.add(delegatedNewsEntity);
        }
      }
      tabResult = listResult.toArray(new DelegatedNewsEntity[listResult.size()]);
    } catch (DelegatedNewsRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
    return tabResult;
  }
}
