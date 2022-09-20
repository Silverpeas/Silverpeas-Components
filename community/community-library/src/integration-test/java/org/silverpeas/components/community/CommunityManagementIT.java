/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.DbSetupRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests about the management of Community contributions.
 */
@RunWith(Arquillian.class)
public class CommunityManagementIT {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  private static final String EXPECTED_ID = "45d42847-2009-4ca8-86bb-1918909dc094";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(DATABASE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityManagementIT.class)
      .addAsResource(DATABASE_CREATION_SCRIPT.substring(1))
      .addAsResource(DATASET_SCRIPT.substring(1))
      .build();
  }

  @Test
  public void emptyTest() {
    // empty test
  }

}