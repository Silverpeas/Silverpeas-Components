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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.components.kmelia.service.KmeliaServiceContext.*;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
class KmeliaServiceContextTest {

  private PublicationDetail publication;
  private PublicationDetail publicationA;
  private PublicationDetail publicationB;

  @BeforeEach
  public void setup() {
    CacheServiceProvider.clearAllThreadCaches();
    publication = PublicationDetail.builder()
        .setPk(new PublicationPK("id", "instanceId"))
        .build();
    publicationA = PublicationDetail.builder()
        .setPk(new PublicationPK("otherId", "instanceId"))
        .build();
    publicationB = PublicationDetail.builder()
        .setPk(new PublicationPK("id", "otherInstanceId"))
        .build();
    assertClearedContext();
  }

  @AfterEach
  public void tearDown() {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    assertClearedContext();
  }

  private void assertClearedContext() {
    assertThat(hasPublicationBeenCreatedFromRequestContext(publication), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publication), is(false));
    assertThat(hasPublicationBeenCreatedFromRequestContext(publicationA), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publicationA), is(false));
    assertThat(hasPublicationBeenCreatedFromRequestContext(publicationB), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publicationB), is(false));
  }

  @Test
  void publicationCreatedIntoSameRequestContext() {
    createdIntoRequestContext(publication);
    assertThat(hasPublicationBeenCreatedFromRequestContext(publication), is(true));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publication), is(false));
    assertThat(hasPublicationBeenCreatedFromRequestContext(publicationA), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publicationA), is(false));
    assertThat(hasPublicationBeenCreatedFromRequestContext(publicationB), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publicationB), is(false));
  }

  @Test
  void publicationUpdatedIntoSameRequestContext() {
    updatedIntoRequestContext(publication);
    assertThat(hasPublicationBeenCreatedFromRequestContext(publication), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publication), is(true));
    assertThat(hasPublicationBeenCreatedFromRequestContext(publicationA), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publicationA), is(false));
    assertThat(hasPublicationBeenCreatedFromRequestContext(publicationB), is(false));
    assertThat(hasPublicationBeenUpdatedFromRequestContext(publicationB), is(false));
  }
}