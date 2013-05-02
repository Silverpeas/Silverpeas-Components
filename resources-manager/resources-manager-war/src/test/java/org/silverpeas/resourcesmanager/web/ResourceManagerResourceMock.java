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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.resourcemanager.control.ResourcesManager;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceStatus;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.resourcesmanager.web.ResourceManagerResourceURIs
    .RESOURCE_MANAGER_BASE_URI;

/**
 * User: Yohann Chastagnier
 * Date: 16/04/13
 */
@Service
@RequestScoped
@Path(RESOURCE_MANAGER_BASE_URI + "/{componentInstanceId}")
@Authorized
public class ResourceManagerResourceMock extends ResourceManagerResource {

  private ResourcesManager resourcesManager = null;

  @Override
  protected ResourcesManager getResourceManager() {
    try {
      if (resourcesManager == null) {
        resourcesManager = mock(ResourcesManager.class);

        when(resourcesManager.getReservation(anyString(), anyLong()))
            .thenAnswer(new Answer<Reservation>() {

              @Override
              public Reservation answer(final InvocationOnMock invocation) throws Throwable {
                String instanceId = (String) invocation.getArguments()[0];
                Long reservationId = (Long) invocation.getArguments()[1];
                if (reservationId == 3L) {
                  return ReservationBuilder.getReservationBuilder()
                      .buildReservation(instanceId, reservationId,
                          ResourceStatus.STATUS_FOR_VALIDATION);
                }
                return null;
              }
            });

        when(resourcesManager.getResourcesOfReservation(anyString(), anyLong()))
            .thenAnswer(new Answer<List<Resource>>() {

              @Override
              public List<Resource> answer(final InvocationOnMock invocation) throws Throwable {
                Long reservationId = (Long) invocation.getArguments()[1];
                List<Resource> resources = new ArrayList<Resource>();
                if (reservationId == 3L) {
                  resources.add(ResourceBuilder.getResourceBuilder().buildResource(31L));
                  resources.add(ResourceBuilder.getResourceBuilder().buildResource(32L));
                }
                return resources;
              }
            });

        when(resourcesManager.getResourceOfReservationStatus(anyLong(), anyLong()))
            .thenAnswer(new Answer<String>() {

              @Override
              public String answer(final InvocationOnMock invocation) throws Throwable {
                return ResourceStatus.STATUS_FOR_VALIDATION;
              }
            });

        when(resourcesManager.getCategory(anyLong())).thenAnswer(new Answer<Category>() {

          @Override
          public Category answer(final InvocationOnMock invocation) throws Throwable {
            Long categoryId = (Long) invocation.getArguments()[0];
            if (categoryId >= 2L) {
              return CategoryBuilder.getCategoryBuilder().buildCategory(categoryId);
            }
            return null;
          }
        });

        when(resourcesManager.getResource(anyLong())).thenAnswer(new Answer<Resource>() {

          @Override
          public Resource answer(final InvocationOnMock invocation) throws Throwable {
            Long resourceId = (Long) invocation.getArguments()[0];
            if (resourceId >= 4L) {
              return ResourceBuilder.getResourceBuilder().buildResource(resourceId);
            }
            return null;
          }
        });
      }
      return resourcesManager;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
