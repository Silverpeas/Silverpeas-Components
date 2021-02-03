/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.test.BasicWarBuilder;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.components.formsonline.model.FormInstance.*;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.*;
import static org.silverpeas.core.contribution.ContributionStatus.REFUSED;
import static org.silverpeas.core.contribution.ContributionStatus.VALIDATED;

/**
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class FormsOnlineServiceTestIT extends AbstractFormsOnlineIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(FormsOnlineDAOJdbcIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addPackages(true, "org.silverpeas.components.formsonline");
        }).build();
  }

  @Inject
  private FormsOnlineService service;

  @Test
  public void getAllUserRequests() throws FormsOnlineException {
    createDynamicContextOfData(2, 90,
        new String[]{
        "1", "1", "1", "1", "1", "1", "1", "1", "1",
        "2", "2", "2", "2", "2", "2", "2", "2", "2"
        },
        new Integer[] {
            STATE_DRAFT,
            STATE_UNREAD,
            STATE_READ,
            STATE_READ,
            STATE_REFUSED,
            STATE_REFUSED,
            STATE_VALIDATED,
            STATE_VALIDATED,
            STATE_ARCHIVED
        },
        new Object[] {
            null,
            null,
            null,
            new Object[][] {
                {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, false, H_DATE, "OK"}
            },
            new Object[][] {
                {VALIDATOR_ID_29, HIERARCHICAL, REFUSED, false, H_DATE, "Ha non"}
            },
            new Object[][] {
                {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, false, H_DATE, "OK"},
                {VALIDATOR_ID_31, FINAL, REFUSED, false, F_DATE, "En fait non !"}
            },
            new Object[][] {
                {VALIDATOR_ID_30, INTERMEDIATE, VALIDATED, false, I_DATE, "OK"},
                {VALIDATOR_ID_31, FINAL, VALIDATED, false, F_DATE, "OK !"}
            },
            new Object[][] {
                {VALIDATOR_ID_29, HIERARCHICAL, VALIDATED, true, H_DATE, "OK"},
                {VALIDATOR_ID_30, INTERMEDIATE, VALIDATED, true, I_DATE, "OK aussi"},
                {VALIDATOR_ID_31, FINAL, VALIDATED, false, F_DATE, "Bon ben d'accord alors"}
            },
            new Object[][] {
                {VALIDATOR_ID_30, HIERARCHICAL, VALIDATED, false, H_DATE, "OK ARCH"},
                {VALIDATOR_ID_31, FINAL, VALIDATED, false, F_DATE, "OK ! ARCH"}
            }
          });
    RequestsByStatus requestsByStatus = service
        .getAllUserRequests(DYNAMIC_DATA_INSTANCE_ID, DEFAULT_CREATOR_IDS[0], null);
    assertThat(requestsByStatus, notNullValue());
    assertThat(requestsByStatus.getAll(), hasSize(90));
    assertThat(requestsByStatus.getDraft(), hasSize(10));
    assertThat(requestsByStatus.getToValidate(), hasSize(30));
    assertThat(requestsByStatus.getValidated(), hasSize(20));
    assertThat(requestsByStatus.getDenied(), hasSize(20));
    assertThat(requestsByStatus.getArchived(), hasSize(10));
    // with pagination
    requestsByStatus = service.getAllUserRequests(DYNAMIC_DATA_INSTANCE_ID, DEFAULT_CREATOR_IDS[0],
        new PaginationPage(2, 9));
    assertThat(requestsByStatus, notNullValue());
    assertThat(requestsByStatus.getAll(), hasSize(9));
    // unknown
    requestsByStatus = service.getAllUserRequests("unknownInstanceId", DEFAULT_CREATOR_IDS[0], null);
    assertThat(requestsByStatus, notNullValue());
    assertThat(requestsByStatus.isEmpty(), is(true));
  }
}