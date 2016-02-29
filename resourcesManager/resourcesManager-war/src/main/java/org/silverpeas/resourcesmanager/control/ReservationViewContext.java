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

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.calendar.CalendarViewContext;
import org.silverpeas.resourcesmanager.web.ResourceManagerResourceURIs;

/**
 * User: Yohann Chastagnier
 * Date: 17/04/13
 */
public class ReservationViewContext extends CalendarViewContext {

  private ResourceManagerDataViewType dataViewType = ResourceManagerDataViewType.reservations;
  private UserDetail currentUser = null;
  private String selectedUserId = null;
  private Long categoryId = null;
  private Long resourceId = null;
  private boolean forValidation = false;

  /**
   * Default constructor.
   * @param componentInstanceId
   * @param language
   */
  public ReservationViewContext(final String componentInstanceId, final UserDetail currentUser,
      final String language) {
    super(componentInstanceId, language);
    this.currentUser = currentUser;
  }

  /**
   * Reset to null all filters.
   */
  @Override
  public ReservationViewContext resetFilters() {
    super.resetFilters();
    dataViewType = ResourceManagerDataViewType.reservations;
    selectedUserId = null;
    categoryId = null;
    resourceId = null;
    forValidation = false;
    return this;
  }

  public ResourceManagerDataViewType getDataViewType() {
    return dataViewType;
  }

  public void setDataViewType(final ResourceManagerDataViewType dataViewType) {
    this.dataViewType = dataViewType;
  }

  public UserDetail getCurrentUser() {
    return currentUser;
  }

  public String getCurrentUserId() {
    return currentUser.getId();
  }

  public UserDetail getSelectedUser() {
    return UserDetail.getById(selectedUserId);
  }

  public String getSelectedUserId() {
    return selectedUserId;
  }

  public void setSelectedUserId(final String userId) {
    this.selectedUserId = userId;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(final Long categoryId) {
    this.categoryId = categoryId;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public void setResourceId(final Long resourceId) {
    this.resourceId = resourceId;
  }

  public boolean isForValidation() {
    return forValidation;
  }

  public void setForValidation(final boolean forValidation) {
    this.forValidation = forValidation;
  }

  /**
   * @return the reservation event URL.
   */
  public String getReservationEventUrl() {
    StringBuilder uri = new StringBuilder(URLManager.getApplicationURL());
    uri.append("/services/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_BASE_URI);
    uri.append("/").append(getComponentInstanceId());
    uri.append("/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_RESERVATIONS_URI_PART);
    uri.append("/").append(getViewType().getPeriodeType().getName());
    uri.append("/").append(getReferenceDay().getYear());
    uri.append("/").append(getReferenceDay().getMonth() + 1);
    uri.append("/").append(getReferenceDay().getDayOfMonth());
    if (isForValidation()) {
      uri.append("/validation");
    } else {
      if (selectedUserId != null) {
        uri.append("/user/").append(selectedUserId);
      }
      if (resourceId != null || categoryId != null) {
        uri.append("/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_RESOURCES_URI_PART);
        if (resourceId != null) {
          uri.append("/").append(resourceId);
        } else {
          uri.append("/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_CATEGORIES_URI_PART);
          uri.append("/").append(categoryId);
        }
      }
    }
    return uri.toString();
  }

  /**
   * @return the category URL.
   */
  public String getCategoryUrl() {
    if (categoryId == null) {
      return "";
    }
    StringBuilder uri = new StringBuilder(URLManager.getApplicationURL());
    uri.append("/services/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_BASE_URI);
    uri.append("/").append(getComponentInstanceId());
    uri.append("/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_RESOURCES_URI_PART);
    uri.append("/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_CATEGORIES_URI_PART);
    uri.append("/").append(categoryId);
    return uri.toString();
  }

  /**
   * @return the resource URL.
   */
  public String getResourceUrl() {
    if (resourceId == null) {
      return "";
    }
    StringBuilder uri = new StringBuilder(URLManager.getApplicationURL());
    uri.append("/services/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_BASE_URI);
    uri.append("/").append(getComponentInstanceId());
    uri.append("/").append(ResourceManagerResourceURIs.RESOURCE_MANAGER_RESOURCES_URI_PART);
    uri.append("/").append(resourceId);
    return uri.toString();
  }
}
