/**
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcesmanager.repository;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.resourcesmanager.model.Reservation;
import org.silverpeas.resourcesmanager.model.ReservedResource;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the ReservedResourceRepository specific features.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReservedResourceRepositoryIntegrationTest {

  @Inject
  private ReservedResourceRepository repository;
  @Inject
  private ReservationRepository reservationRepository;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/resourcesmanager/services/create-database.sql")
          .loadInitialDataSetFrom(
              "/org/silverpeas/resourcesmanager/services/reservations_validation_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ReservedResourceRepositoryIntegrationTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-node");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-publication");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-formtemplate");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-calendar");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
          warBuilder.addAsResource("org/silverpeas/resourcesmanager/services");
          warBuilder.addPackages(true, "org.silverpeas.resourcesmanager");
        })
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
