/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.resourcemanager.services;

import org.silverpeas.resourcemanager.model.ResourceStatus;
import com.stratelia.webactiv.util.DBUtil;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-resource-manager-datasource.xml",
  "/spring-resource-manager.xml"})
@Transactional
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class ReservedResourceServiceTest {

  public ReservedResourceServiceTest() {
  }
  private static ReplacementDataSet dataSet;
  @Inject
  private ReservedResourceService service;
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @BeforeClass
  public static void prepareDataset() throws Exception {
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(ReservedResourceServiceTest.class.getClassLoader().
        getResourceAsStream(
        "org/silverpeas/resourcemanager/services/reservations_validation_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Before
  public void generalSetUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
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
    List<ReservedResource> result = service.findAllReservedResourcesOfReservation(
        currentReservationId);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(3));
    ReservedResource resource1 = new ReservedResource();
    resource1.setReservationId(3);
    resource1.setResourceId(1);
    resource1.setStatus("V");
    ReservedResource resource2 = new ReservedResource();
    resource2.setReservationId(3);
    resource2.setResourceId(3);
    resource2.setStatus("test");
    ReservedResource resource3 = new ReservedResource();
    resource3.setReservationId(3);
    resource3.setResourceId(2);
    resource3.setStatus("R");
    assertThat(result, containsInAnyOrder(resource1, resource2, resource3));
  }

  /**
   * Test of findAllReservedResourcesWithProblem method, of class ReservedResourceService.
   */
  @Test
  public void testFindAllReservedResourcesWithProblem() {
    int currentReservationId = -1;
    List<Long> futureReservedResourceIds = Arrays.asList(new Long[]{1L, 2L, 3L, 5L, 8L});
    String startPeriod = "1320134400000";
    String endPeriod = "1320163200000";
    List<ReservedResource> result = service.findAllReservedResourcesWithProblem(currentReservationId,
        futureReservedResourceIds, startPeriod, endPeriod);
    assertThat(result, is(notNullValue()));
    assertThat(result, hasSize(2));
    ReservedResource resource1 = new ReservedResource();
    resource1.setReservationId(3);
    resource1.setResourceId(1);
    resource1.setStatus("V");
    ReservedResource resource2 = new ReservedResource();
    resource2.setReservationId(3);
    resource2.setResourceId(3);
    resource2.setStatus("test");
    assertThat(result, containsInAnyOrder(resource1, resource2));
  }
}
