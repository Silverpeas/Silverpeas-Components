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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.almanach.web;

import com.silverpeas.almanach.service.AlmanachServiceProvider;
import com.silverpeas.rest.RESTWebService;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import static com.stratelia.webactiv.almanach.control.DisplayableEventOccurrence.*;

/**
 * The Web resource representing an alamanch instance. It is a REST-based web service that sent back
 * the events in JSON of the underlying almanach according to some criteria.
 */
@Service
@Scope("request")
@Path("almanach/{componentId}")
public class AlmanachResource extends RESTWebService {
  
  @Inject
  private AlmanachServiceProvider almanachServiceProvider;
  
  @PathParam("componentId")
  private String componentId;
  
  /**
   * Gets the next events registered in the almanach represented by this web resource.
   * If the almanach doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the almanach, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return a JSON representation of the next event occurrences.
   */
  @GET
  @Path("nextevents")
  @Produces({MediaType.APPLICATION_JSON})
  public String getNextEventOccurrences() {
    try {
      checkUserPriviledges();
      List<EventOccurrence> occurrences = anAlmanachServiceProvider().getNextEventOccurrencesOf(
              getComponentId());
      return toJSON(decorate(occurrences));
    } catch (AlmanachException ex) {
      Logger.getLogger(AlmanachResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected String getComponentId() {
    return componentId;
  }
  
  private AlmanachServiceProvider anAlmanachServiceProvider() {
    return almanachServiceProvider;
  }
}
