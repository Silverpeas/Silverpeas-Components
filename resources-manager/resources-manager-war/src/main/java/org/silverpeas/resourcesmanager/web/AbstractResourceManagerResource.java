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

import org.silverpeas.util.comparator.AbstractComplexComparator;
import com.silverpeas.web.RESTWebService;
import org.silverpeas.resourcemanager.ResourcesManagerFactory;
import org.silverpeas.resourcemanager.control.ResourcesManager;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.resourcesmanager.web.ResourceManagerResourceURIs.*;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractResourceManagerResource extends RESTWebService {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Converts the resources into its corresponding web entity list.
   * @param reservationId the identifier of the linked reservation.
   * @param resources the resources to convert.
   * @return the corresponding list of resource entity.
   */
  protected Collection<ReservedResourceEntity> asWebEntities(Long reservationId,
      List<Resource> resources) {
    checkNotFoundStatus(resources);

    // Sort
    Collections.sort(resources, new AbstractComplexComparator<Resource>() {
      @Override
      protected ValueBuffer getValuesToCompare(final Resource resource) {
        return new ValueBuffer().append(resource.getCategoryId()).append(resource.getName());
      }
    });

    // Entities
    Collection<ReservedResourceEntity> resourceEntities = new ArrayList<ReservedResourceEntity>();
    for (Resource resource : resources) {
      resourceEntities.add(asWebEntity(reservationId, resource));
    }
    return resourceEntities;
  }

  /**
   * Converts the reserved resource into its corresponding web entity.
   * @param reservationId the identifier of the linked reservation.
   * @param resource the resource to convert.
   * @return the corresponding resource entity.
   */
  protected ReservedResourceEntity asWebEntity(Long reservationId, Resource resource) {
    checkNotFoundStatus(resource);
    return ReservedResourceEntity.createFrom(resource)
        .withReservationURI(buildReservationURI(reservationId))
        .withCategoryURI(buildResourceCategoryURI(resource.getCategory()))
        .withURI(buildResourceURI(resource));
  }

  /**
   * Converts the resource into its corresponding web entity.
   * @param resource the resource to convert.
   * @return the corresponding resource entity.
   */
  protected ResourceEntity asWebEntity(Resource resource) {
    checkNotFoundStatus(resource);
    return ResourceEntity.createFrom(resource)
        .withCategoryURI(buildResourceCategoryURI(resource.getCategory()))
        .withURI(buildResourceURI(resource));
  }

  /**
   * Converts the reservations into its corresponding web entity list.
   * @param reservations the reservation events to convert.
   * @return the corresponding list of reservation entity.
   */
  protected Collection<ReservationEntity> asWebEntities(List<Reservation> reservations) {
    checkNotFoundStatus(reservations);

    // Sort
    Collections.sort(reservations, new AbstractComplexComparator<Reservation>() {
      @Override
      protected ValueBuffer getValuesToCompare(final Reservation reservation) {
        return new ValueBuffer().append(reservation.getBeginDate()).append(reservation.getEndDate())
            .append(reservation.getEvent());
      }
    });

    // Entities
    Collection<ReservationEntity> reservationEntities = new ArrayList<ReservationEntity>();
    for (Reservation reservation : reservations) {
      reservationEntities.add(asWebEntity(reservation));
    }
    return reservationEntities;
  }

  /**
   * Converts the reservation into its corresponding web entity.
   * @param reservation the reservation event to convert.
   * @return the corresponding reservation entity.
   */
  protected ReservationEntity asWebEntity(Reservation reservation) {
    checkNotFoundStatus(reservation);
    return ReservationEntity.createFrom(getComponentId(), reservation)
        .withURI(buildReservationURI(reservation))
        .withResourceURI(buildResourceReservationURI(reservation));
  }

  /**
   * Converts the category into its corresponding web entity.
   * @param category the category to convert.
   * @return the corresponding category entity.
   */
  protected ResourceCategoryEntity asWebEntity(Category category) {
    checkNotFoundStatus(category);
    return ResourceCategoryEntity.createFrom(category).withURI(buildResourceCategoryURI(category));
  }

  /**
   * Centralized build of reservation URI.
   * @param reservation
   * @return reservation URI
   */
  protected URI buildReservationURI(Reservation reservation) {
    if (reservation == null) {
      return null;
    }
    return buildReservationURI(reservation.getId());
  }

  /**
   * Centralized build of reservation URI.
   * @param reservationId
   * @return reservation URI
   */
  private URI buildReservationURI(Long reservationId) {
    return getUriInfo().getBaseUriBuilder().path(RESOURCE_MANAGER_BASE_URI).path(getComponentId())
        .path(RESOURCE_MANAGER_RESERVATIONS_URI_PART).path(reservationId.toString()).build();
  }

  /**
   * Centralized build of resource reservation URI.
   * @param reservation
   * @return reservation URI
   */
  protected URI buildResourceReservationURI(Reservation reservation) {
    if (reservation == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(RESOURCE_MANAGER_BASE_URI).path(getComponentId())
        .path(RESOURCE_MANAGER_RESERVATIONS_URI_PART).path(reservation.getIdAsString())
        .path(RESOURCE_MANAGER_RESOURCES_URI_PART).build();
  }

  /**
   * Centralized build of resource reservation URI.
   * @param category
   * @return reservation URI
   */
  protected URI buildResourceCategoryURI(Category category) {
    if (category == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(RESOURCE_MANAGER_BASE_URI).path(getComponentId())
        .path(RESOURCE_MANAGER_RESOURCES_URI_PART).path(RESOURCE_MANAGER_CATEGORIES_URI_PART)
        .path(category.getIdAsString()).build();
  }

  /**
   * Centralized build of resource reservation URI.
   * @param resource
   * @return reservation URI
   */
  protected URI buildResourceURI(Resource resource) {
    if (resource == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(RESOURCE_MANAGER_BASE_URI).path(getComponentId())
        .path(RESOURCE_MANAGER_RESOURCES_URI_PART).path(resource.getIdAsString()).build();
  }

  /**
   * Centralization
   * @param object any object
   */
  private void checkNotFoundStatus(Object object) {
    if (object == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Gets Gallery EJB.
   * @return
   */
  protected ResourcesManager getResourceManager() {
    return ResourcesManagerFactory.getResourcesManager();
  }
}
