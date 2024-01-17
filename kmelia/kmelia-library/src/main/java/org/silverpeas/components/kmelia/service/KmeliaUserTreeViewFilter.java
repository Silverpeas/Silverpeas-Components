/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ArrayUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * This class handles by one method call the following stuffs:
 * <ul>
 * <li>the computing of best user role on each node of a tree</li>
 * <li>the filtering of nodes which the user can't access (not accessible nodes are removed
 * from the tree)</li>
 * </ul>
 * @author Yohann Chastagnier
 */
class KmeliaUserTreeViewFilter {
  private static final String NODE_TO_EXCLUDE = "@@@NODE_TO_EXCLUDE@@@";

  private final OrganizationController orga = OrganizationController.get();

  private final String userId;
  private final String instanceId;
  private final NodePK initialNodeIdentifier;
  private final String bestUserComponentInstanceRole;
  private final boolean isRightsOnTopicsUsed;


  private Map<String, List<String>> nodeUserRoles = null;

  /**
   * Initializing the instance.
   * @param userId the identifier of a user
   * @param instanceId the identifier of a component instance.
   * @param initialNodeIdentifier the node root identifier.
   * @param bestUserComponentInstanceRole the best role the user has on the component instance.
   * @param isRightsOnTopicsUsed true of rights are handled at a node level.
   * @return an initialized instance.
   */
  public static KmeliaUserTreeViewFilter from(final String userId, final String instanceId,
      final NodePK initialNodeIdentifier, final String bestUserComponentInstanceRole,
      final boolean isRightsOnTopicsUsed) {
    return new KmeliaUserTreeViewFilter(userId, instanceId, initialNodeIdentifier,
        bestUserComponentInstanceRole, isRightsOnTopicsUsed);
  }

  /**
   * Hidden constructor.
   */
  private KmeliaUserTreeViewFilter(final String userId, final String instanceId,
      final NodePK initialNodeIdentifier, final String bestUserComponentInstanceRole,
      final boolean isRightsOnTopicsUsed) {
    this.userId = userId;
    this.instanceId = instanceId;
    this.initialNodeIdentifier = initialNodeIdentifier;
    this.bestUserComponentInstanceRole = bestUserComponentInstanceRole;
    this.isRightsOnTopicsUsed = isRightsOnTopicsUsed;
  }

  /**
   * Computes and sets the best user role on each node of the given tree and filters it by
   * excluding from the given tree the nodes that the user can't access.
   * @param tree the tree to perform.
   */
  void setBestUserRoleAndFilter(List<NodeDetail> tree) {
    if (isRightsOnTopicsUsed) {
      tree.removeIf(node -> !setBestUserNodeRole(node, bestUserComponentInstanceRole));
    } else {
      for (NodeDetail node : tree) {
        node.setUserRole(bestUserComponentInstanceRole);
      }
    }

    if (!tree.isEmpty()) {
      NodeDetail root = tree.get(0);
      if (root.getNodePK().isRoot()) {
        // Case of root.
        // Check if publications on root are allowed
        String sNB = defaultStringIfNotDefined(
            orga.getComponentParameterValue(initialNodeIdentifier.getInstanceId(), "nbPubliOnRoot"),
            "0");
        int nbPublisOnRoot = Integer.parseInt(sNB);
        if (nbPublisOnRoot != 0) {
          root.setUserRole("user");
        }
      }
    }
  }

  /**
   * Sets the best node profile on the given node and its children one.
   * @param node the node to perform.
   * @param bestParentNodeUserRole the best user role of the parent node.
   * @return true if the user can access the node because of its role or because it can access a sub
   * node.
   */
  private boolean setBestUserNodeRole(final NodeDetail node, final String bestParentNodeUserRole) {
    final String nodeUserRole = node.getUserRole();
    if (nodeUserRole != null) {
      return !NODE_TO_EXCLUDE.equals(nodeUserRole);
    }

    String bestNodeUserRole = bestParentNodeUserRole;
    boolean hasUserNodeAccess = false;
    if (node.haveRights()) {
      String rightsDependsOn = node.getRightsDependsOn();
      String[] profiles = getNodeUserRoles(rightsDependsOn);
      bestNodeUserRole = ArrayUtil.isEmpty(profiles) ? null : KmeliaHelper.getProfile(profiles);
    }
    if (bestNodeUserRole != null) {
      node.setUserRole(bestNodeUserRole);
      hasUserNodeAccess = true;
    }
    Iterator<NodeDetail> nodeChildIterator = node.getChildrenDetails().iterator();
    while (nodeChildIterator.hasNext()) {
      NodeDetail child = nodeChildIterator.next();
      boolean hasUserChildNodeAccess = setBestUserNodeRole(child, bestNodeUserRole);
      if (!hasUserChildNodeAccess) {
        nodeChildIterator.remove();
      }
      hasUserNodeAccess = hasUserNodeAccess || hasUserChildNodeAccess;
    }
    if (!hasUserNodeAccess) {
      node.setUserRole(NODE_TO_EXCLUDE);
    }
    return hasUserNodeAccess;
  }

  /**
   * Gets the roles of the user on the node represented by the given identifier.<br>
   * Data are loaded only one time.
   * @param nodeId a node identifier.
   * @return a list of role.
   */
  private String[] getNodeUserRoles(String nodeId) {
    if (nodeUserRoles == null) {
      nodeUserRoles = orga.getUserObjectProfiles(userId, instanceId, ProfiledObjectType.NODE);
    }
    List<String> roles = nodeUserRoles.get(nodeId);
    return (roles != null) ? roles.toArray(new String[0]) : new String[0];
  }
}
