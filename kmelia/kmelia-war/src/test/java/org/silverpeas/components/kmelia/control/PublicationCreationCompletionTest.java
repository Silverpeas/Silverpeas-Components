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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the creation of a publication from the web pages of Kmelia. What remains to do on
 * the publication once created, like setting its thumbnail, has to be done before the supervisors
 * and the subscribers are notified about the creation: this completion is then passed to the
 * Kmelia service, that notifies them. In the Kmax mode, nobody is notified about the creation of a
 * publication: it is then just completed once created.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PublicationCreationCompletionTest {

  private static final String COMPONENT_ID = "kmelia42";
  private static final String AUTHOR = "1";
  private static final String PUBLICATION_ID = "23";
  private static final NodePK CURRENT_FOLDER = new NodePK("4", COMPONENT_ID);

  private final KmeliaService kmeliaService = mock(KmeliaService.class);
  private PublicationDetail publication;
  @SuppressWarnings("unchecked")
  private final Consumer<PublicationDetail> completion = mock(Consumer.class);
  private KmeliaSessionController controller;

  @BeforeEach
  void setUpTheController(@TestManagedMock I18n i18n) {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
    publication = PublicationDetail.builder()
        .setPk(new PublicationPK("unknown", COMPONENT_ID))
        .setNameAndDescription("A publication", "")
        .build();

    // the controller initializes itself from a lot of settings of the application: only its
    // behavior at the creation of a publication is under test here
    controller = mock(KmeliaSessionController.class, CALLS_REAL_METHODS);
    doReturn(kmeliaService).when(controller).getKmeliaService();
    doReturn(AUTHOR).when(controller).getUserId();
    doReturn(CURRENT_FOLDER).when(controller).getCurrentFolderPK();

    when(kmeliaService.createPublicationIntoTopic(eq(publication), eq(CURRENT_FOLDER),
        any(Consumer.class))).thenReturn(PUBLICATION_ID);
    when(kmeliaService.createPublicationIntoTopic(eq(publication), eq(CURRENT_FOLDER),
        any(PdcClassification.class), any(Consumer.class))).thenReturn(PUBLICATION_ID);
    when(kmeliaService.createKmaxPublication(publication)).thenReturn(PUBLICATION_ID);
  }

  @Test
  void theCompletionOfAPublicationWithoutClassificationIsPassedToTheService() {
    final String publicationId = controller.createPublication(publication, null, completion);

    assertThat(publicationId, is(PUBLICATION_ID));
    verify(kmeliaService).createPublicationIntoTopic(publication, CURRENT_FOLDER, completion);
    verify(completion, never()).accept(any());
  }

  @Test
  void theCompletionOfAPublicationWithAnUndefinedClassificationIsPassedToTheService() {
    controller.createPublication(publication, PdcClassificationEntity.undefinedClassification(),
        completion);

    verify(kmeliaService).createPublicationIntoTopic(publication, CURRENT_FOLDER, completion);
    verify(completion, never()).accept(any());
  }

  @Test
  void theCompletionOfAClassifiedPublicationIsPassedToTheService() {
    final PdcClassificationEntity classification = mock(PdcClassificationEntity.class);
    when(classification.getPdcPositions()).thenReturn(List.of());

    final String publicationId =
        controller.createPublication(publication, classification, completion);

    assertThat(publicationId, is(PUBLICATION_ID));
    verify(kmeliaService).createPublicationIntoTopic(eq(publication), eq(CURRENT_FOLDER),
        any(PdcClassification.class), eq(completion));
    verify(completion, never()).accept(any());
  }

  @Test
  void aPublicationInKmaxModeIsCompletedOnceCreated() {
    controller.setKmaxMode(true);

    final String publicationId = controller.createPublication(publication, null, completion);

    assertThat(publicationId, is(PUBLICATION_ID));
    final InOrder order = inOrder(kmeliaService, completion);
    order.verify(kmeliaService).createKmaxPublication(publication);
    order.verify(completion).accept(publication);
  }

  @Test
  void theCurrentUserIsTheCreatorOfThePublication() {
    controller.createPublication(publication, null, completion);

    assertThat(publication.getCreatorId(), is(AUTHOR));
    assertThat(publication.getCreationDate(), is(notNullValue()));
  }
}
