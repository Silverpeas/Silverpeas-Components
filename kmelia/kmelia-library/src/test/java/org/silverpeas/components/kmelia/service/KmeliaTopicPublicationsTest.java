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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.kmelia.KmeliaContentManager;
import org.silverpeas.components.kmelia.KmeliaCopyDetail;
import org.silverpeas.components.kmelia.KmeliaPasteDetail;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.coordinates.service.CoordinatesService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the operations of {@link DefaultKmeliaService} applying on all the publications of
 * a topic: whatever their visibility period, all of them have to be taken into account.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class KmeliaTopicPublicationsTest {

  private static final String INSTANCE_ID = "kmelia1";
  private static final NodePK TOPIC_PK = new NodePK("10", INSTANCE_ID);

  @TestManagedMock
  private OrganizationController organizationController;
  @TestManagedMock
  private NodeService nodeService;
  @TestManagedMock
  private PublicationService publicationService;
  @TestManagedMock
  private StatisticService statisticService;
  @TestManagedMock
  private PdcManager pdcManager;
  @TestManagedMock
  private CoordinatesService coordinatesService;
  @TestManagedMock
  private CommentService commentService;
  @TestManagedMock
  private AdminController adminController;
  @TestManagedMock
  private SilverpeasCalendar calendar;
  @TestManagedMock
  private PdcClassificationService pdcClassificationService;
  @TestManagedMock
  private KmeliaContentManager kmeliaContentManager;
  @TestManagedMock
  private PublicationAccessControl publicationAccessControl;
  @TestManagedMock
  private NodeAccessControl nodeAccessControl;
  @TestManagedMock
  private ComponentAccessControl componentAccessControl;
  @TestManagedMock
  private AttachmentService attachmentService;
  @TestedBean
  private DefaultKmeliaService testedService;

  private DefaultKmeliaService kmeliaService;
  private PublicationDetail visiblePublication;
  private PublicationDetail notYetVisiblePublication;

  @BeforeEach
  void setup() {
    // spying the tested bean to stub the treatments applied on each publication of the topic
    kmeliaService = spy(testedService);
    // no topic is in the basket
    when(nodeService.getPath(any(NodePK.class))).thenReturn(new NodePath());

    visiblePublication = aPublicationInTopic("1");
    notYetVisiblePublication = aPublicationInTopic("2");
    // without any explicit filtering parameter, the publications out of their visibility
    // period are filtered
    when(publicationService.getDetailsByFatherPK(TOPIC_PK)).thenReturn(
        singletonList(visiblePublication));
    when(publicationService.getDetailsByFatherPK(eq(TOPIC_PK), any())).thenReturn(
        singletonList(visiblePublication));
    when(publicationService.getDetailsByFatherPK(eq(TOPIC_PK), any(), eq(true))).thenReturn(
        singletonList(visiblePublication));
    when(publicationService.getDetailsByFatherPK(eq(TOPIC_PK), any(), eq(false))).thenReturn(
        List.of(visiblePublication, notYetVisiblePublication));
  }

  @Test
  void sendingATopicToTheBasketRemovesAllItsPublications() {
    final NodeDetail topic = new NodeDetail();
    topic.setNodePK(TOPIC_PK);
    topic.setFatherPK(new NodePK(NodePK.ROOT_NODE_ID, INSTANCE_ID));
    when(nodeService.getHeader(TOPIC_PK)).thenReturn(topic);
    doReturn(SilverpeasRole.ADMIN.getName()).when(kmeliaService)
        .getUserTopicProfile(any(NodePK.class), anyString());

    kmeliaService.deleteTopic(TOPIC_PK, "0");

    verify(publicationService).removePublication(visiblePublication.getPK());
    verify(publicationService).removePublication(notYetVisiblePublication.getPK());
    verify(nodeService).moveNode(TOPIC_PK, new NodePK(NodePK.BIN_NODE_ID, INSTANCE_ID));
  }

  @Test
  void deletingATopicInTheBasketDeletesAllItsPublications() {
    when(nodeService.getDescendantPKs(TOPIC_PK)).thenReturn(new ArrayList<>());
    doNothing().when(kmeliaService).deletePublication(any(PublicationPK.class));

    kmeliaService.deleteTopic(TOPIC_PK);

    verify(kmeliaService).deletePublication(visiblePublication.getPK());
    verify(kmeliaService).deletePublication(notYetVisiblePublication.getPK());
    verify(nodeService).deleteNode(TOPIC_PK);
  }

  @Test
  void movingATopicMovesAllItsPublications() {
    final NodePK target = new NodePK("20", INSTANCE_ID);
    final KmeliaPasteDetail pasteContext = new KmeliaPasteDetail(target);
    final NodeDetail topic = new NodeDetail();
    topic.setNodePK(TOPIC_PK);
    when(nodeService.getSubTree(TOPIC_PK)).thenReturn(singletonList(topic));
    doNothing().when(kmeliaService)
        .movePublication(any(PublicationPK.class), any(NodePK.class), any());

    kmeliaService.moveNode(new NodePK(TOPIC_PK.getId(), INSTANCE_ID), target, pasteContext);

    verify(kmeliaService).movePublication(eq(visiblePublication.getPK()), any(),
        eq(pasteContext));
    verify(kmeliaService).movePublication(eq(notYetVisiblePublication.getPK()), any(),
        eq(pasteContext));
  }

  @Test
  void copyingTheContentOfATopicCopiesAllItsPublications() {
    final KmeliaCopyDetail copyDetail = new KmeliaCopyDetail("0");
    copyDetail.setFromNodePK(TOPIC_PK);
    copyDetail.setToNodePK(new NodePK("20", INSTANCE_ID));
    doReturn(null).when(kmeliaService)
        .copyPublication(any(PublicationDetail.class), any(KmeliaCopyDetail.class));

    kmeliaService.copyPublications(copyDetail);

    verify(kmeliaService).copyPublication(visiblePublication, copyDetail);
    verify(kmeliaService).copyPublication(notYetVisiblePublication, copyDetail);
  }

  private PublicationDetail aPublicationInTopic(final String id) {
    final PublicationPK pk = new PublicationPK(id, INSTANCE_ID);
    final PublicationDetail publication = PublicationDetail.builder().setPk(pk).build();
    when(publicationService.getAllLocations(pk)).thenReturn(
        singletonList(new Location(TOPIC_PK.getId(), INSTANCE_ID)));
    return publication;
  }
}
