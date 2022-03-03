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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
public class KmeliaUserTreeViewFilterTest {

  private static final String USER_ID = "26";
  private static final String INSTANCE_ID = "instanceId";
  private static final NodePK ROOT_NODE_PK = new NodePK(NodePK.ROOT_NODE_ID, INSTANCE_ID);
  private static final String USER_ROLE = SilverpeasRole.user.getName();
  private static final String READER_ROLE = SilverpeasRole.reader.getName();
  private static final String WRITER_ROLE = SilverpeasRole.writer.getName();
  private static final String ADMIN_ROLE = SilverpeasRole.admin.getName();

  private static final int ROOT_NODE_ID = Integer.parseInt(ROOT_NODE_PK.getId());
  private static final int NODE_A_ID = 10;
  private static final int NODE_AA_ID = 11;
  private static final int NODE_AAA_ID = 12;
  private static final int NODE_AAB_ID = 13;
  private static final int NODE_AB_ID = 14;
  private static final int NODE_ABA_ID = 15;
  private static final int NODE_B_ID = 16;
  private static final int NODE_BA_ID = 17;

  @TestManagedMock
  private OrganizationController organisationController;

  @BeforeEach
  public void setup() {
    // Verifying common data
    List<NodeDetail> commonTree = buildCommonTree();

    assertTree(commonTree, Pair.of(ROOT_NODE_ID, null), Pair.of(NODE_A_ID, null),
        Pair.of(NODE_AA_ID, null), Pair.of(NODE_AAA_ID, null), Pair.of(NODE_AAB_ID, null),
        Pair.of(NODE_AB_ID, null), Pair.of(NODE_ABA_ID, null), Pair.of(NODE_B_ID, null),
        Pair.of(NODE_BA_ID, null));

    NodeDetail node_AA = commonTree.get(2);
    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.getChildrenDetails(), hasSize(2));
  }

  @Test
  public void setBestUserRoleAndFilterOnNotExistingTree() {
    assertThrows(NullPointerException.class,
        () -> KmeliaUserTreeViewFilter.from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, false)
            .setBestUserRoleAndFilter(null));
  }

  @Test
  public void setBestUserRoleAndFilterOnEmptyTree() {
    List<NodeDetail> emptyTree = new ArrayList<NodeDetail>();
    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, false)
        .setBestUserRoleAndFilter(emptyTree);
    assertThat(emptyTree, empty());
    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(emptyTree);
    assertThat(emptyTree, empty());
  }

  @Test
  public void setBestUserRoleAndFilterOnCommonTreeWithoutNodeRightManagement() {
    List<NodeDetail> commonTree = buildCommonTree();
    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, false)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, READER_ROLE),
        Pair.of(NODE_A_ID, READER_ROLE),
        Pair.of(NODE_AA_ID, READER_ROLE),
        Pair.of(NODE_AAA_ID, READER_ROLE),
        Pair.of(NODE_AAB_ID, READER_ROLE),
        Pair.of(NODE_AB_ID, READER_ROLE),
        Pair.of(NODE_ABA_ID, READER_ROLE),
        Pair.of(NODE_B_ID, READER_ROLE),
        Pair.of(NODE_BA_ID, READER_ROLE));
  }

  @Test
  public void
  setBestUserRoleAndFilterOnCommonTreeWithoutNodeRightManagementAndDisplaying12PublisOnRootNode() {
    when(organisationController.getComponentParameterValue(INSTANCE_ID, "nbPubliOnRoot"))
        .thenReturn("12");

    List<NodeDetail> commonTree = buildCommonTree();
    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, false)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, USER_ROLE),
        Pair.of(NODE_A_ID, READER_ROLE),
        Pair.of(NODE_AA_ID, READER_ROLE),
        Pair.of(NODE_AAA_ID, READER_ROLE),
        Pair.of(NODE_AAB_ID, READER_ROLE),
        Pair.of(NODE_AB_ID, READER_ROLE),
        Pair.of(NODE_ABA_ID, READER_ROLE),
        Pair.of(NODE_B_ID, READER_ROLE),
        Pair.of(NODE_BA_ID, READER_ROLE));
  }

  @Test
  public void setBestUserRoleAndFilterOnCommonTreeWithRightHandlingOnNodeAAButNoUserRight() {
    List<NodeDetail> commonTree = buildCommonTree();

    NodeDetail node_AA = commonTree.get(2);
    node_AA.setRightsDependsOnMe();
    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.haveRights(), is(true));
    assertThat(node_AA.getChildrenDetails(), hasSize(2));

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, READER_ROLE),
        Pair.of(NODE_A_ID, READER_ROLE),
        Pair.of(NODE_AB_ID, READER_ROLE),
        Pair.of(NODE_ABA_ID, READER_ROLE),
        Pair.of(NODE_B_ID, READER_ROLE),
        Pair.of(NODE_BA_ID, READER_ROLE));

    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.haveRights(), is(true));
    assertThat(node_AA.getChildrenDetails(), empty());
  }

  @Test
  public void
  setBestUserRoleAndFilterOnCommonTreeWithRightHandlingOnNodeAAandNodeAABbutNoUserRight() {
    List<NodeDetail> commonTree = buildCommonTree();

    NodeDetail node_AA = commonTree.get(2);
    node_AA.setRightsDependsOnMe();
    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.haveRights(), is(true));
    assertThat(node_AA.getChildrenDetails(), hasSize(2));

    NodeDetail node_AAB = commonTree.get(4);
    node_AAB.setRightsDependsOnMe();
    assertThat(node_AAB.getId(), is(NODE_AAB_ID));
    assertThat(node_AAB.haveRights(), is(true));
    assertThat(node_AAB.getChildrenDetails(), empty());

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, READER_ROLE),
        Pair.of(NODE_A_ID, READER_ROLE),
        Pair.of(NODE_AB_ID, READER_ROLE),
        Pair.of(NODE_ABA_ID, READER_ROLE),
        Pair.of(NODE_B_ID, READER_ROLE),
        Pair.of(NODE_BA_ID, READER_ROLE));

    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.haveRights(), is(true));
    assertThat(node_AA.getChildrenDetails(), empty());

    assertThat(node_AAB.getId(), is(NODE_AAB_ID));
    assertThat(node_AAB.haveRights(), is(true));
    assertThat(node_AAB.getChildrenDetails(), empty());
  }

  @Test
  public void
  setBestUserRoleAndFilterOnCommonTreeWithRightHandlingOnNodeAAandNodeAABwithUserRightOnAABAndDisplaying12PublisOnRootNode() {
    when(organisationController.getComponentParameterValue(INSTANCE_ID, "nbPubliOnRoot"))
        .thenReturn("12");

    List<NodeDetail> commonTree = buildCommonTree();

    NodeDetail node_AA = commonTree.get(2);
    node_AA.setRightsDependsOnMe();
    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.haveRights(), is(true));
    assertThat(node_AA.getChildrenDetails(), hasSize(2));

    NodeDetail node_AAB = commonTree.get(4);
    node_AAB.setRightsDependsOnMe();
    assertThat(node_AAB.getId(), is(NODE_AAB_ID));
    assertThat(node_AAB.haveRights(), is(true));
    assertThat(node_AAB.getChildrenDetails(), empty());

    when(organisationController.getUserObjectProfiles(USER_ID, INSTANCE_ID, ProfiledObjectType.NODE))
        .thenReturn(UserNodeRoleMapping.from(NODE_AAB_ID, READER_ROLE, WRITER_ROLE));

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, USER_ROLE),
        Pair.of(NODE_A_ID, READER_ROLE),
        Pair.of(NODE_AA_ID, (String) null),
        Pair.of(NODE_AAB_ID, WRITER_ROLE),
        Pair.of(NODE_AB_ID, READER_ROLE),
        Pair.of(NODE_ABA_ID, READER_ROLE),
        Pair.of(NODE_B_ID, READER_ROLE),
        Pair.of(NODE_BA_ID, READER_ROLE));

    assertThat(node_AA.getId(), is(NODE_AA_ID));
    assertThat(node_AA.haveRights(), is(true));
    assertThat(node_AA.getChildrenDetails(), contains(node_AAB));

    assertThat(node_AAB.getId(), is(NODE_AAB_ID));
    assertThat(node_AAB.haveRights(), is(true));
    assertThat(node_AAB.getChildrenDetails(), empty());
  }

  @Test
  public void setBestUserRoleAndFilterOnCommonTreeWithRightHandlingOnAllNodesButNoUserRight() {
    List<NodeDetail> commonTree = buildCommonTree();

    for (NodeDetail node : commonTree) {
      node.setRightsDependsOnMe();
    }

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree);
  }

  @Test
  public void setBestUserRoleAndFilterOnCommonTreeWithRightHandlingOnAllNodesWithUserRightOnAAA() {
    List<NodeDetail> commonTree = buildCommonTree();

    for (NodeDetail node : commonTree) {
      node.setRightsDependsOnMe();
    }

    when(organisationController.getUserObjectProfiles(USER_ID, INSTANCE_ID, ProfiledObjectType.NODE))
        .thenReturn(UserNodeRoleMapping.from(NODE_AAA_ID, READER_ROLE, WRITER_ROLE));

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, (String) null),
        Pair.of(NODE_A_ID, (String) null),
        Pair.of(NODE_AA_ID, (String) null),
        Pair.of(NODE_AAA_ID, WRITER_ROLE));
  }

  @Test
  public void setBestUserRoleAndFilterOnCommonTreeWithRightHandlingOnAllNodesWithUserRightOnBA() {
    List<NodeDetail> commonTree = buildCommonTree();

    for (NodeDetail node : commonTree) {
      node.setRightsDependsOnMe();
    }

    when(organisationController.getUserObjectProfiles(USER_ID, INSTANCE_ID, ProfiledObjectType.NODE))
        .thenReturn(UserNodeRoleMapping.from(NODE_BA_ID, READER_ROLE, WRITER_ROLE));

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(ROOT_NODE_ID, (String) null),
        Pair.of(NODE_B_ID, (String) null),
        Pair.of(NODE_BA_ID, WRITER_ROLE));
  }

  @Test
  public void
  setBestUserRoleAndFilterOnPartOfCommonTreeWithoutSpecificNodeRightAndDisplaying12PublisOnRootNode() {
    when(organisationController.getComponentParameterValue(INSTANCE_ID, "nbPubliOnRoot"))
        .thenReturn("12");

    List<NodeDetail> commonTree = buildCommonTree();
    Iterator<NodeDetail> treeIt = commonTree.iterator();
    while(treeIt.hasNext()) {
      NodeDetail node = treeIt.next();
      if (!(node.getId() >= NODE_A_ID && node.getId() <= NODE_AAB_ID)) {
        treeIt.remove();
      }
    }
    assertThat(commonTree, hasSize(4));

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(NODE_A_ID, READER_ROLE),
        Pair.of(NODE_AA_ID, READER_ROLE),
        Pair.of(NODE_AAA_ID, READER_ROLE),
        Pair.of(NODE_AAB_ID, READER_ROLE));
  }

  @Test
  public void
  setBestUserRoleAndFilterOnPartOfCommonTreeWithRightHandlingOnAandABandDisplaying12PublisOnRootNode() {
    when(organisationController.getComponentParameterValue(INSTANCE_ID, "nbPubliOnRoot"))
        .thenReturn("12");

    List<NodeDetail> commonTree = buildCommonTree();
    Iterator<NodeDetail> treeIt = commonTree.iterator();
    while(treeIt.hasNext()) {
      NodeDetail node = treeIt.next();
      if (!(node.getId() >= NODE_A_ID && node.getId() <= NODE_AAB_ID)) {
        treeIt.remove();
      }
    }
    assertThat(commonTree, hasSize(4));

    for (NodeDetail node : commonTree) {
      if (node.getId() >= NODE_A_ID && node.getId() <= NODE_AA_ID) {
        node.setRightsDependsOnMe();
      }
    }

    when(organisationController.getUserObjectProfiles(USER_ID, INSTANCE_ID, ProfiledObjectType.NODE))
        .thenReturn(UserNodeRoleMapping.from(NODE_A_ID, ADMIN_ROLE, WRITER_ROLE)
            .put(NODE_AA_ID, WRITER_ROLE));

    KmeliaUserTreeViewFilter
        .from(USER_ID, INSTANCE_ID, ROOT_NODE_PK, READER_ROLE, true)
        .setBestUserRoleAndFilter(commonTree);
    assertTree(commonTree,
        Pair.of(NODE_A_ID, ADMIN_ROLE),
        Pair.of(NODE_AA_ID, WRITER_ROLE),
        Pair.of(NODE_AAA_ID, WRITER_ROLE),
        Pair.of(NODE_AAB_ID, WRITER_ROLE));
  }

  @SuppressWarnings("unchecked")
  private void assertTree(List<NodeDetail> actualTree,
      Pair<Integer, String>... expectedNodeRightPairs) {
    assertThat(actualTree, hasSize(expectedNodeRightPairs.length));
    Pair<Integer, String>[] actualNodeRightPairs = new Pair[expectedNodeRightPairs.length];
    for (int i = 0; i < actualTree.size(); i++) {
      final NodeDetail actualNode = actualTree.get(i);
      actualNodeRightPairs[i] = Pair.of(actualNode.getId(), actualNode.getUserRole());
    }
    if (actualNodeRightPairs.length > 0) {
      assertThat(actualNodeRightPairs, arrayContaining(expectedNodeRightPairs));
    }
  }

  /**
   * ROOT
   * -A (10)
   * --AA (11)
   * ----AAA (12)
   * ----AAB (13)
   * --AB (14)
   * ----ABA (15)
   * -B (16)
   * --BA (17)
   */
  private List<NodeDetail> buildCommonTree() {
    NodeDetail root = createNode(0, "");
    NodeDetail nodeA = createNode(NODE_A_ID, "A");
    NodeDetail nodeAA = createNode(NODE_AA_ID, "AA");
    NodeDetail nodeAAA = createNode(NODE_AAA_ID, "AAA");
    NodeDetail nodeAAB = createNode(NODE_AAB_ID, "AAB");
    NodeDetail nodeAB = createNode(NODE_AB_ID, "AB");
    NodeDetail nodeABA = createNode(NODE_ABA_ID, "ABA");
    NodeDetail nodeB = createNode(NODE_B_ID, "B");
    NodeDetail nodeBA = createNode(NODE_BA_ID, "BA");

    List<NodeDetail> nodes = new ArrayList<NodeDetail>();

    nodes.add(root);
    root.getChildrenDetails().add(nodeA);
    root.getChildrenDetails().add(nodeB);

    nodes.add(nodeA);
    nodeA.getChildrenDetails().add(nodeAA);
    nodeA.getChildrenDetails().add(nodeAB);

    nodes.add(nodeAA);
    nodeAA.getChildrenDetails().add(nodeAAA);
    nodeAA.getChildrenDetails().add(nodeAAB);

    nodes.add(nodeAAA);
    nodes.add(nodeAAB);

    nodes.add(nodeAB);
    nodeAB.getChildrenDetails().add(nodeABA);

    nodes.add(nodeABA);

    nodes.add(nodeB);
    nodeB.getChildrenDetails().add(nodeBA);

    nodes.add(nodeBA);
    return nodes;
  }

  private NodeDetail createNode(int id, final String name) {
    NodeDetail node = new NodeDetail();
    node.setNodePK(new NodePK(String.valueOf(id), INSTANCE_ID));
    node.setChildrenDetails(new ArrayList<NodeDetail>());
    node.setName(name);
    return node;
  }

  private static class UserNodeRoleMapping extends HashMap<Integer, List<String>> {
    private static final long serialVersionUID = -6232421131041246806L;

    static UserNodeRoleMapping from(int nodeId, String... roles) {
      UserNodeRoleMapping mapping = new UserNodeRoleMapping();
      return mapping.put(nodeId, roles);
    }

    UserNodeRoleMapping put(Integer nodeId, String... roles) {
      put(nodeId, CollectionUtil.asList(roles));
      return this;
    }
  }
}