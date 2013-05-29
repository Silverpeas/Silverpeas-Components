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

import com.silverpeas.web.ResourceGettingTest;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.resourcemanager.model.Category;
import org.silverpeas.resourcemanager.model.Reservation;
import org.silverpeas.resourcemanager.model.Resource;
import org.silverpeas.resourcemanager.model.ResourceStatus;

import java.util.Iterator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.silverpeas.resourcesmanager.web.CategoryEntityMatcher.matches;
import static org.silverpeas.resourcesmanager.web.ReservationEntityMatcher.matches;
import static org.silverpeas.resourcesmanager.web.ReservedResourceEntityMatcher.matches;
import static org.silverpeas.resourcesmanager.web.ResourceEntityMatcher.matches;
import static org.silverpeas.resourcesmanager.web.ResourceManagerResourceURIs
    .RESOURCE_MANAGER_CATEGORIES_URI_PART;
import static org.silverpeas.resourcesmanager.web.ResourceManagerResourceURIs
    .RESOURCE_MANAGER_RESOURCES_URI_PART;
import static org.silverpeas.resourcesmanager.web.ResourceManagerTestResources.JAVA_PACKAGE;
import static org.silverpeas.resourcesmanager.web.ResourceManagerTestResources.SPRING_CONTEXT;

/**
 * Tests on the gallery album getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
public class ResourceManagerReservationGettingTest
    extends ResourceGettingTest<ResourceManagerTestResources> {

  private String sessionKey;
  private ReservationBuilder.ReservationMock expected;

  private static String INSTANCE_ID = "componentName5";

  private static Long RESERVATION_ID = 3L;

  public ResourceManagerReservationGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    sessionKey = authenticate(aUser());
    expected = ReservationBuilder.getReservationBuilder()
        .buildReservation(INSTANCE_ID, RESERVATION_ID, ResourceStatus.STATUS_FOR_VALIDATION)
        .addResource(ResourceBuilder.getResourceBuilder().buildResource(31L))
        .addResource(ResourceBuilder.getResourceBuilder().buildResource(32L));
  }

  @Test
  public void getReservation() {
    final ReservationEntity entity = getAt(aResourceURI(), ReservationEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expected));
    assertNotNull(entity.getResources());
    assertThat(entity.getResources().size(), is(2));
    Iterator<Resource> expectedResourcesIt = expected.getResources().iterator();
    for (ReservedResourceEntity resource : entity.getResources()) {
      assertThat(resource, matches(RESERVATION_ID, expectedResourcesIt.next()));
    }
  }

  @Test
  public void getResourcesOfReservation() {
    final ReservedResourceEntity[] resources =
        getAt(aResourceURI() + "/resources", ReservedResourceEntity[].class);
    assertNotNull(resources);
    assertThat(resources.length, is(2));
    Iterator<Resource> expectedResourcesIt = expected.getResources().iterator();
    for (ReservedResourceEntity resource : resources) {
      assertThat(resource, matches(RESERVATION_ID, expectedResourcesIt.next()));
    }
  }

  @Test
  public void getCategory() {
    Category expectedCategory = CategoryBuilder.getCategoryBuilder().buildCategory(2L);
    final ResourceCategoryEntity entity =
        getAt(aMonthReservationResourceBaseURI() + "/" + RESOURCE_MANAGER_RESOURCES_URI_PART + "/" +
            RESOURCE_MANAGER_CATEGORIES_URI_PART + "/2", ResourceCategoryEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expectedCategory));
  }


  @Test
  public void getResource() {
    Resource expectedResource = ResourceBuilder.getResourceBuilder().buildResource(4L);
    final ResourceEntity entity =
        getAt(aMonthReservationResourceBaseURI() + "/" + RESOURCE_MANAGER_RESOURCES_URI_PART + "/4",
            ResourceEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(expectedResource));
  }

  @Test
  public void getMonthReservationsAllUsers() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14", ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Test
  public void getMonthReservationsForValidation() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14/validation", ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Test
  public void getMonthReservationsFiltredByCategory() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14/resources/categories/1",
            ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Test
  public void getMonthReservationsFiltredByResource() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14/resources/2",
            ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Test
  public void getMonthReservationsOfUser() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14/user/10",
            ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Test
  public void getMonthReservationsFiltredByCategoryOfUser() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14/user/10/resources/categories/1",
            ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Test
  public void getMonthReservationsFiltredByResourceOfUser() {
    // Test only if the URI exists
    final ReservationEntity[] entities =
        getAt(aMonthReservationResourceBaseURI() + "/month/2013/02/14/user/10/resources/2",
            ReservationEntity[].class);
    assertNotNull(entities);
  }

  @Override
  public String aResourceURI() {
    return aResourceURI(RESERVATION_ID);
  }

  private String aResourceURI(final Long reservationId) {
    return "resourceManager/" + getExistingComponentInstances()[0] + "/reservations/" +
        reservationId;
  }

  private String aMonthReservationResourceBaseURI() {
    return "resourceManager/" + getExistingComponentInstances()[0] + "/reservations";
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI(8L);
  }

  @Override
  public Reservation aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return ReservationEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{INSTANCE_ID};
  }
}
