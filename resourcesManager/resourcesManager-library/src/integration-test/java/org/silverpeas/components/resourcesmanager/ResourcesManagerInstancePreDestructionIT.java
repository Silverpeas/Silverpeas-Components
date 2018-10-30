/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.resourcesmanager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test on the ResourcesManager instance pre-destruction process
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ResourcesManagerInstancePreDestructionIT {

  private static final String INSTANCE_ID = "resourcesManager42";

  @Inject
  private ResourcesManagerInstancePreDestruction preDestruction;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(
      "/org/silverpeas/components/resourcesmanager/service/create-database.sql")
          .loadInitialDataSetFrom(
              "/org/silverpeas/components/resourcesmanager/service" +
                  "/reservations_validation_dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(
        ResourcesManagerInstancePreDestructionIT.class).build();
  }

  @Before
  public void assertTestEnvIsOk() {
    assertThat(preDestruction, notNullValue());
  }

  @Test
  public void preDestroyAResourcesManagerInstance() throws Exception {
    Transaction.performInOne(() -> {
      preDestruction.preDestroy(INSTANCE_ID);
      return null;
    });

    try (Connection connection = DBUtil.openConnection()) {
      for (String sql : new String[] {
          "select count(*) from sc_resources_category",
          "select count(*) from sc_resources_resource",
          "select count(*) from sc_resources_reservation",
          "select count(*) from sc_resources_reservedResource",
          "select count(*) from sc_resources_managers"
      }) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
          ResultSet rs = stmt.executeQuery();
          assertThat(rs.next(), is(true));
          assertThat(rs.getLong(1), is(0l));
        }
      }
    }
  }
}
