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
package org.silverpeas.components.whitepages.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests on the WhitePagesServiceProvider's features.
 */
@RunWith(Arquillian.class)
public class WhitePagesServiceProviderIT {

  public WhitePagesServiceProviderIT() {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(WhitePagesServiceProviderIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addAsResource("org/silverpeas/classifyEngine/ClassifyEngine.properties");
          warBuilder.addPackages(true, "org.silverpeas.components.whitepages");
        }).build();
  }

  @Before
  public void generalSetup() throws Exception {
  }

  /**
   * Tests the white pages service getting.
   */
  @Test
  public void testGetWhitePagesService() {
    WhitePagesService service = WhitePageServiceProvider.getWhitePagesService();
    assertThat(service, notNullValue());
  }

  /**
   * Tests the mixed search service getting.
   */
  @Test
  public void testGetMixedSearchService() {
    MixedSearchService service = WhitePageServiceProvider.getMixedSearchService();
    assertThat(service, notNullValue());
  }
}
