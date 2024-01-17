/*
 * Copyright (C) 2000 - 2024 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.resourcesmanager.repository;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.Reservation;
import org.silverpeas.components.resourcesmanager.model.ReservedResource;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests on the ReservedResourceRepository specific features.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReservedResourceRepositoryIT {

  @Inject
  private ReservedResourceRepository repository;
  @Inject
  private ReservationRepository reservationRepository;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(
      "/org/silverpeas/components/resourcesmanager/service/create-database.sql")
          .loadInitialDataSetFrom(
              "/org/silverpeas/components/resourcesmanager/service" +
                  "/reservations_validation_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(
        ReservedResourceRepositoryIT.class)
        .build();
  }

  @Before
  public void assertTestEnvIsOk() {
    assertThat(repository, notNullValue());
    assertThat(reservationRepository, notNullValue());
  }

  @Test
  public void deleteAllInAGivenComponentInstance() {
    final String instanceId = "resourcesManager42";
    final long reservedResourcesCount = 6l;
    final List<Reservation> reservations = reservationRepository.findAllReservations(instanceId);

    long deletionCount =
        Transaction.performInOne(() -> repository.deleteByComponentInstanceId(instanceId));
    assertThat(deletionCount, is(reservedResourcesCount));

    for (Reservation reservation : reservations) {
      List<ReservedResource> reservedResources =
          repository.findAllReservedResourcesOfReservation(reservation.getIdAsLong());
      assertThat(reservedResources.isEmpty(), is(true));
    }
  }
}
