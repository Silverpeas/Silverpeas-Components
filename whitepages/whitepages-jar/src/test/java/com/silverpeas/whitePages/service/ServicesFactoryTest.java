/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests on the ServicesFactory's features.
 */
public class ServicesFactoryTest {

  private ConfigurableApplicationContext context;

  public ServicesFactoryTest() {
  }

  @Before
  public void loadTestContext() throws Exception {
    context = new ClassPathXmlApplicationContext("/spring-whitePages-services.xml",
        "spring-whitePages-embbed-datasource.xml");
  }

  @After
  public void unloadTestContext() throws Exception {
    context.close();
  }

  /**
   * Tests the white pages service getting.
   */
  @Test
  public void testGetWhitePagesService() {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    WhitePagesService service = servicesFactory.getWhitePagesService();
    assertThat(service, notNullValue());
  }

  /**
   * Tests the mixed search service getting.
   */
  @Test
  public void testGetMixedSearchService() {
    ServicesFactory servicesFactory = ServicesFactory.getFactory();
    MixedSearchService service = servicesFactory.getMixedSearchService();
    assertThat(service, notNullValue());
  }
}
