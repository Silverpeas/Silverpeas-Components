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
import org.silverpeas.resourcesmanager.model.Resource;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the specific features of ResourceValidatorRepository.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ResourceValidatorRepositoryIntegrationTest {

  @Inject
  private ResourceValidatorRepository repository;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/resourcesmanager/services/create-database.sql")
          .loadInitialDataSetFrom(
              "/org/silverpeas/resourcesmanager/services/reservations_validation_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ResourceValidatorRepositoryIntegrationTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:pdc");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:node");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:tagcloud");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:publication");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:formtemplate");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:calendar");
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
  }

  @Test
  public void deleteAllInAGivenComponentInstance() {
    final String instanceId = "resourcesManager42";
    final long managedResourcesCount = 7l;

    long deletionCount =
        Transaction.performInOne(() -> repository.deleteByComponentInstanceId(instanceId));
    assertThat(deletionCount, is(managedResourcesCount));
  }
}
