/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package com.stratelia.webactiv.almanach.servlets;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.export.Exporter;
import com.silverpeas.export.ExporterFactory;
import com.stratelia.webactiv.almanach.control.CalendarEventEncoder;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.CreateException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import static com.silverpeas.export.ExportDescriptor.*;

/**
 * A producer of an ICS resource from a given almanach.
 */
@Path("almanach/ics/{almanachId}")
@Service
@Scope("request")
public class AlmanachICSProducer {

  @QueryParam("userId")
  private String userId;
  @QueryParam("login")
  private String login;
  @QueryParam("password")
  private String password;

  /**
   * Gets the almanach content specified by its identifier in the ICS format.
   * The credence parameters of the user tempting to access the almanach are passed as query
   * parameters in the almanach URL.
   * If the user has not enough right to acccess the almanach, then a 403 error is returned
   * (access denied). If the almanach getting failed, then a 503 error is returned (service
   * unavailable).
   * @param almanachId the unique identifier of the almanach to get.
   * @return the iCal almanach representation
   */
  @GET
  @Produces("text/calendar")
  public String getICS(@PathParam("almanachId") String almanachId) {
    StringWriter writer = new StringWriter();

    // Check login/pwd must be an identified user
    AdminController adminController = new AdminController(null);
    UserFull user = adminController.getUserFull(userId);
    if (user != null && user.getLogin().equals(login)
        && user.getPassword().equals(password) && adminController.isComponentAvailable(almanachId,
        userId)) {
      CalendarEventEncoder encoder = new CalendarEventEncoder();
      try {
        List<EventDetail> allEventDetails = getAllEvents(almanachId);
        List<CalendarEvent> allEvents = encoder.encode(allEventDetails);

        ExporterFactory exporterFactory = ExporterFactory.getFactory();
        Exporter<CalendarEvent> iCalExporter = exporterFactory.getICalExporter();
        iCalExporter.export(withWriter(writer), allEvents);
      } catch (Exception ex) {
        throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
      }
    } else {
      throw new WebApplicationException(Status.FORBIDDEN);
    }

    return writer.toString();
  }

  /**
   * Gets all events of the underlying almanach.
   * @param almanachId
   * @return a list with the details of the events registered in the almanach.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws RemoteException if the communication with the remote business object fails.
   * @throws UtilException if a reference to the remote EJB cannot be fetched.
   * @throws CreateException if the EJB cannot be created.
   */
  public List<EventDetail> getAllEvents(final String almanachId) throws AlmanachException,
      RemoteException, UtilException, CreateException {
    EventPK pk = new EventPK("", null, almanachId);
    AlmanachBm almanachBm = ((AlmanachBmHome) EJBUtilitaire.getEJBObjectRef(
        JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class)).create();
    return new ArrayList<EventDetail>(almanachBm.getAllEvents(pk));
  }
}
