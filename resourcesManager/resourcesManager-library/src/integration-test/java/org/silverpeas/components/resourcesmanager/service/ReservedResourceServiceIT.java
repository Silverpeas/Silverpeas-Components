/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.resourcesmanager.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.ReservedResource;
import org.silverpeas.components.resourcesmanager.model.ResourceStatus;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class ReservedResourceServiceIT {

  public ReservedResourceServiceIT() {
  }

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("reservations_validation_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(ReservedResourceServiceIT.class).build();
  }

  private ReservedResourceService service;

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(ReservedResourceService.class);
  }

  /**
   * Test of getReservedResource method, of class ReservedResourceService.
   */
  @Test
  public void testGetReservedResource() {
    int resourceId = 3;
    int reservationId = 3;
    ReservedResource reservedResource = service.getReservedResource(resourceId, reservationId);
    assertThat(reservedResource, is(notNullValue()));
    assertThat(reservedResource.getStatus(), is("test"));
  }

  /**
   * Test of update method, of class ReservedResourceService.
   */
  @Test
  public void testUpdate() {
    int resourceId = 3;
    int reservationId = 3;
    ReservedResource reservedResource = service.getReservedResource(resourceId, reservationId);
    assertThat(reservedResource, is(notNullValue()));
    assertThat(reservedResource.getStatus(), is("test"));
    reservedResource.setStatus(ResourceStatus.STATUS_VALIDATE);
    service.update(reservedResource);
    reservedResource = service.getReservedResource(resourceId, reservationId);
    assertThat(reservedResource, is(notNullValue()));
    assertThat(reservedResource.getStatus(), is(ResourceStatus.STATUS_VALIDATE));
  }

  /**
   * Test of findAllReservedResourcesWithProblem method, of class ReservedResourceService.
   */
  @Test
  public void testFindAllReservedResourcesOfReservation() {
    int currentReservationId = 3;
    List<ReservedResource> result =
        service.findAllReservedResourcesOfReservation(currentReservationId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    ReservedResource resource1 = new ReservedResource();
    resource1.setReservedResourceId("1", "3");
    resource1.setStatus("V");
    ReservedResource resource2 = new ReservedResource();
    resource2.setReservedResourceId("3", "3");
    resource2.setStatus("test");
    ReservedResource resource3 = new ReservedResource();
    resource3.setReservedResourceId("2", "3");
    resource3.setStatus("R");
    assertThat(result, containsInAnyOrder(resource1, resource2, resource3));
  }

  /**
   * Test of findAllReservedResourcesWithProblem method, of class ReservedResourceService.
   */
  @Test
  public void testFindAllReservedResourcesWithProblem() {
    int currentReservationId = -1;
    List<Long> futureReservedResourceIds = Arrays.asList(1L, 2L, 3L, 5L, 8L);
    String startPeriod = "1320134400000";
    String endPeriod = "1320163200000";
    List<ReservedResource> result = service
        .findAllReservedResourcesWithProblem(currentReservationId, futureReservedResourceIds,
            startPeriod, endPeriod);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    ReservedResource resource1 = new ReservedResource();
    resource1.setReservedResourceId("1", "3");
    resource1.setStatus("V");
    ReservedResource resource2 = new ReservedResource();
    resource2.setReservedResourceId("3", "3");
    resource2.setStatus("test");
    assertThat(result, containsInAnyOrder(resource1, resource2));
  }
}
