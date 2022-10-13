/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.rssaggregator.web;

import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.service.RSSServiceProvider;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@WebService
@Path(RSSResource.PATH + "/{componentId}")
@Authorized
public class RSSResource extends RESTWebService {

  static final String PATH = "rss";

  @PathParam("componentId")
  protected String componentId;

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets the JSON representation of the specified existing channel.<br>
   * If the channel doesn't exist, a <b>404</b> HTTP code is returned. <br>
   * If the user isn't authentified, a <b>401</b> HTTP code is returned. <br>
   * If the user isn't authorized to access the channel, a <b>403</b> is returned. <br>
   * If a problem occurs when processing the request, a <b>503</b> HTTP code is returned.
   * @param agregate String option to specify if we agregate channels items sorting them by date or
   * not.<br>
   * It means URI has the following parameter : agregate=y or agregate=n
   * @return the response to the HTTP GET request with the JSON representation of the asked channel
   * items.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<RSSItem> getRSS(@QueryParam("agregate") String agregate) {
    boolean isAgregate = StringUtil.getBooleanValue(agregate);
    // Retrieve rss agregate content
    try {
      return
          RSSServiceProvider.getRSSService().getApplicationItems(getComponentId(), isAgregate);
    } catch (RssAgregatorException e) {
      throw encapsulateException(e);
    }
  }

  WebApplicationException encapsulateException(Exception ex) {
    if (ex instanceof WebApplicationException) {
      return (WebApplicationException) ex;
    }
    return new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
  }

}
