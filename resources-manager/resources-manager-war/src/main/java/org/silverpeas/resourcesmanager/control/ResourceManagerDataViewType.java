/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcesmanager.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * It defines the type of view of data displayed in resource manager calendar.
 */
public enum ResourceManagerDataViewType {

  reservations,
  resources,
  reservationListing;

  /**
   * Is this view data type is a reservations one.
   * @return true if this view data type is for a reservations one, false otherwise.
   */
  public boolean isReservationsDataView() {
    return this == reservations;
  }

  /**
   * Is this view data type is a resources one.
   * @return true if this view data type is for a resources one, false otherwise.
   */
  public boolean isResourcesDataView() {
    return this == resources;
  }

  /**
   * Is this view data type is a reservation listing one.
   * @return true if this view data type is for a reservation listing one, false otherwise.
   */
  public boolean isReservationListingDataView() {
    return this == reservationListing;
  }

  /**
   * Gets the name of this enum.
   * @return the enum name.
   * @see ResourceManagerDataViewType#name()
   */
  @JsonValue
  public String getName() {
    return name();
  }

  @JsonCreator
  public static ResourceManagerDataViewType from(String name) {
    if (name != null) {
      for (ResourceManagerDataViewType viewDataType : ResourceManagerDataViewType.values()) {
        if (name.equalsIgnoreCase(viewDataType.name())) {
          return viewDataType;
        }
      }
    }
    return null;
  }
}
