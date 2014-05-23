/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.rssAgregator.web;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.rssAgregator.control.RSSServiceFactory;
import com.silverpeas.rssAgregator.model.RSSItem;
import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;

@Service
@RequestScoped
@Path("rss/{componentId}")
@Authorized
public class RSSResource extends RESTWebService {

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  /**
   * Gets the JSON representation of the specified existing channel.<br/>
   * If the channel doesn't exist, a <b>404</b> HTTP code is returned. <br/>
   * If the user isn't authentified, a <b>401</b> HTTP code is returned. <br/>
   * If the user isn't authorized to access the channel, a <b>403</b> is returned. <br/>
   * If a problem occurs when processing the request, a <b>503</b> HTTP code is returned.
   * @param agregate String option to specify if we agregate channels items sorting them by date or
   * not.<br/>
   * It means URI has the following parameter : agregate=y or agregate=n
   * @return the response to the HTTP GET request with the JSON representation of the asked channel items.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<RSSItem> getRSS(@QueryParam("agregate") String agregate) {
    boolean isAgregate = StringUtil.getBooleanValue(agregate);
    List<RSSItem> items = new ArrayList<RSSItem>();
    // Retrieve rss agregate content
    try {
      items = RSSServiceFactory.getRSSService().getApplicationItems(getComponentId(), isAgregate);
    } catch (RssAgregatorException e) {
      throw encapsulateException(e);
    }
    return items;
  }

  WebApplicationException encapsulateException(Exception ex) {
    if (ex instanceof WebApplicationException) {
      return (WebApplicationException) ex;
    }
    return new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
  }

}
