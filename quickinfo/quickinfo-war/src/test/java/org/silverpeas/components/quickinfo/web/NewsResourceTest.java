/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access to the news of a given QuickInfo instance through the REST API: the
 * news of another instance must be neither served nor removed.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class NewsResourceTest {

  private static final String QUICKINFO_ID = "quickinfo1";
  private static final String NEWS_ID = "news_1";

  @TestManagedMock
  private OrganizationController organizationController;
  @TestManagedMock
  private QuickInfoService quickInfoService;
  @TestedBean
  private NewsResource resource;

  private NewsResource rest;

  @BeforeEach
  void setUp() {
    resource.componentId = QUICKINFO_ID;
    rest = spy(resource);
    doReturn(SilverpeasRole.ADMIN).when(rest).getHighestUserRole();
  }

  @Test
  void getANewsOfAnotherInstanceIsNotFound() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.getNews(NEWS_ID));

    assertThat(ex.getResponse().getStatus(), is(404));
    verify(quickInfoService).getNews(new ResourceReference(NEWS_ID, QUICKINFO_ID));
  }

  @Test
  void deleteANewsOfTheInstance() {
    Response response = rest.deleteNews(NEWS_ID);

    assertThat(response.getStatus(), is(200));
    verify(quickInfoService).removeNews(new ResourceReference(NEWS_ID, QUICKINFO_ID));
  }

  @Test
  void deleteANewsOfAnotherInstanceIsNotFound() {
    ResourceReference newsRef = new ResourceReference(NEWS_ID, QUICKINFO_ID);
    doThrow(new NotFoundException("No such news")).when(quickInfoService).removeNews(newsRef);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> rest.deleteNews(NEWS_ID));

    assertThat(ex.getResponse().getStatus(), is(404));
  }
}
