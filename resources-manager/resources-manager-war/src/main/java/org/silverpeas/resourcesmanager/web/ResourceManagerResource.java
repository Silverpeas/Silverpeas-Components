/*
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
package org.silverpeas.resourcesmanager.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.DateUtil;
import org.silverpeas.date.Period;
import org.silverpeas.date.PeriodType;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.resourcesmanager.web.ResourceManagerResourceURIs.*;

/**
 * A REST Web resource giving reservation and resource data.
 *
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(RESOURCE_MANAGER_BASE_URI + "/{componentInstanceId}")
@Authorized
public class ResourceManagerResource extends AbstractResourceManagerResource {

  /**
   * Gets the JSON representation of a category. If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param categoryId the identifier od the aimed category.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * category.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/" + RESOURCE_MANAGER_RESOURCES_URI_PART + "/"
      + RESOURCE_MANAGER_CATEGORIES_URI_PART + "/{categoryId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ResourceCategoryEntity getCategory(@PathParam("categoryId") final long categoryId) {
    try {
      return asWebEntity(getResourceManager().getCategory(categoryId));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of a resource. If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param resourceId the identifier od the aimed resource.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * resource.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/" + RESOURCE_MANAGER_RESOURCES_URI_PART
      + "/{resourceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ResourceEntity getResource(@PathParam("resourceId") final long resourceId) {
    try {
      return asWebEntity(getResourceManager().getResource(resourceId));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of a reservation event. If it doesn't exist, a 404 HTTP code is
   * returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param reservationId the identifier od the aimed reservation.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * reservation.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{reservationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ReservationEntity getReservation(@PathParam("reservationId") final long reservationId) {
    try {
      Reservation reservation = getResourceManager().getReservation(getComponentId(), reservationId);
      return agregateReservedResourceWebEntities(asWebEntity(reservation));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of a list of resources of a reservation. If it doesn't exist, a
   * 404 HTTP code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a
   * problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param reservationId the identifier od the aimed reservation.
   * @return the response to the HTTP GET request with the JSON representation of list of resources
   * of a reservation.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{reservationId}/"
      + RESOURCE_MANAGER_RESOURCES_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservedResourceEntity> getResourcesOfReservation(
      @PathParam("reservationId") final long reservationId) {
    try {
      List<Resource> resources = getResourceManager().getResourcesOfReservation(getComponentId(),
          reservationId);
      for (Resource resource : resources) {
        resource.setStatus(getResourceManager().
            getResourceOfReservationStatus(resource.getId(), reservationId));
      }
      return asWebEntities(reservationId, resources);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of a list of reservation event. If it doesn't exist, a 404 HTTP
   * code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservations(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day) {
    return getReservations(periodType, year, month, day, null, null, null);
  }

  /**
   * Gets the JSON representation of a list of reservation event. If it doesn't exist, a 404 HTTP
   * code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @param categoryId the aimed category of resources
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(
      RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}/"
      + RESOURCE_MANAGER_RESOURCES_URI_PART + "/" + RESOURCE_MANAGER_CATEGORIES_URI_PART
      + "/{categoryId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservationsByCategory(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day,
      @PathParam("categoryId") final long categoryId) {
    return getReservations(periodType, year, month, day, null, categoryId, null);
  }

  /**
   * Gets the JSON representation of a list of reservation event. If it doesn't exist, a 404 HTTP
   * code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @param resourceId the aimed resource
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(
      RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}/"
      + RESOURCE_MANAGER_RESOURCES_URI_PART + "/{resourceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservationsByResource(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day,
      @PathParam("resourceId") final long resourceId) {
    return getReservations(periodType, year, month, day, null, null, resourceId);
  }

  /**
   * Gets the JSON representation of a list of reservation event. If it doesn't exist, a 404 HTTP
   * code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @param userId the aimed user
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}/user/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservations(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day,
      @PathParam("userId") final String userId) {
    return getReservations(periodType, year, month, day, userId, null, null);
  }

  /**
   * Gets the JSON representation of a list of reservation event. If it doesn't exist, a 404 HTTP
   * code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @param userId the aimed user
   * @param categoryId the aimed category of resources
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(
      RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}/user/{userId}/"
      + RESOURCE_MANAGER_RESOURCES_URI_PART + "/" + RESOURCE_MANAGER_CATEGORIES_URI_PART
      + "/{categoryId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservationsByCategory(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day,
      @PathParam("userId") final String userId, @PathParam("categoryId") final long categoryId) {
    return getReservations(periodType, year, month, day, userId, categoryId, null);
  }

  /**
   * Gets the JSON representation of a list of reservation event. If it doesn't exist, a 404 HTTP
   * code is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @param userId the aimed user
   * @param resourceId the aimed resource
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(
      RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}/user/{userId}/"
      + RESOURCE_MANAGER_RESOURCES_URI_PART + "/{resourceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservationsByResource(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day,
      @PathParam("userId") final String userId, @PathParam("resourceId") final long resourceId) {
    return getReservations(periodType, year, month, day, userId, null, resourceId);
  }

  /**
   * Gets the JSON representation of a list of reservation event that has to be validated. If it
   * doesn't exist, a 404 HTTP code is returned. If the user isn't authentified, a 401 HTTP code is
   * returned. If a problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @param periodType the aimed period
   * @param year the aimed year
   * @param month the aimed month
   * @param day the aimed day
   * @return the response to the HTTP GET request with the JSON representation of the asked list of
   * reservation.
   */
  @GET
  @Path(RESOURCE_MANAGER_RESERVATIONS_URI_PART + "/{periodType}/{year}/{month}/{day}/validation")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ReservationEntity> getReservationsForValidation(
      @PathParam("periodType") final PeriodType periodType, @PathParam("year") final int year,
      @PathParam("month") final int month, @PathParam("day") final int day) {
    try {

      // Compute a date with given year, month and day
      Date dateReference = computeReferenceDate(year, month, day);

      List<Reservation> reservations = getResourceManager()
          .getReservationForValidation(getComponentId(), getUserDetail().getId(),
          Period.from(dateReference, periodType, getUserPreferences().getLanguage()));

      return agregateReservedResourceWebEntities(asWebEntities(reservations));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Centralization of getting of month reservations of a user.
   *
   * @param periodType
   * @param year
   * @param month
   * @param day
   * @param userId
   * @param categoryId
   * @param resourceId
   * @return
   */
  private Collection<ReservationEntity> getReservations(final PeriodType periodType, final int year,
      final int month, final int day, final String userId, final Long categoryId,
      final Long resourceId) {
    try {

      // Compute a date with given year, month and day
      Date dateReference = computeReferenceDate(year, month, day);

      // Compute the period
      Period period = Period.from(dateReference, periodType, getUserPreferences().getLanguage());

      // User
      Integer userIdAsInteger = null;
      if (StringUtil.isInteger(userId)) {
        userIdAsInteger = Integer.valueOf(userId);
      }

      // Get reservation of the month by a user
      final List<Reservation> reservations;
      if (resourceId != null) {

        // And filtred by a given resource
        reservations = getResourceManager()
            .getReservationWithResource(getComponentId(), userIdAsInteger, period, resourceId);
      } else if (categoryId != null) {

        // And filtred by a given category
        reservations = getResourceManager()
            .getReservationWithResourcesOfCategory(getComponentId(), userIdAsInteger, period,
            categoryId);
      } else {

        // No filters
        reservations = getResourceManager().getReservationOfUser(getComponentId(), userIdAsInteger,
            period);
      }

      // Result as a list of reservation web entity
      return agregateReservedResourceWebEntities(asWebEntities(reservations));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Centralization.
   *
   * @param reservationEntity
   * @return
   */
  private ReservationEntity agregateReservedResourceWebEntities(ReservationEntity reservationEntity) {
    return reservationEntity
        .addAll(getResourcesOfReservation(reservationEntity.getReservation().getId()));
  }

  /**
   * Centralization.
   *
   * @param reservationEntities
   * @return
   */
  private Collection<ReservationEntity> agregateReservedResourceWebEntities(
      Collection<ReservationEntity> reservationEntities) {
    for (ReservationEntity reservationEntity : reservationEntities) {
      reservationEntity
          .addAll(getResourcesOfReservation(reservationEntity.getReservation().getId()));
    }
    return reservationEntities;
  }

  /**
   * Compute the reference date.
   *
   * @param year
   * @param month
   * @param day
   * @return
   */
  private Date computeReferenceDate(final int year, final int month, final int day) {
    Calendar calendar = Calendar.getInstance(I18NHelper.defaultLocale);
    calendar.setTime(DateUtil.getDate());
    calendar.set(year, (month - 1), day);
    return calendar.getTime();
  }
}
