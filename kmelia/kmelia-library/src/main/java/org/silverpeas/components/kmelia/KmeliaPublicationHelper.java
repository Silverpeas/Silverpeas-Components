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
package org.silverpeas.components.kmelia;

import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.kernel.util.StringUtil;

public class KmeliaPublicationHelper {

  private KmeliaPublicationHelper() {
  }

  public static boolean isUserConsideredAsOwner(String instanceId, String currentUserId,
      String profile, User ownerDetail) {
    if (hasWritePrivilege(currentUserId, profile, ownerDetail)) {
      return true;
    } else if ("writer".equals(profile)) {
      // check if co-writing is enabled
      return StringUtil.getBooleanValue(
          getParameterValue(instanceId, InstanceParameters.coWriting));
    }
    return false;
  }

  public static boolean isRemovable(String instanceId, String currentUserId, String profile,
      User ownerDetail) {
    if (hasWritePrivilege(currentUserId, profile, ownerDetail)) {
      boolean removeOnlyForAdmin = StringUtil.getBooleanValue(
          getParameterValue(instanceId, InstanceParameters.suppressionOnlyForAdmin));
      return !removeOnlyForAdmin || "admin".equals(profile);
    }
    return false;
  }

  public static boolean isCanBeCut(String instanceId, String currentUserId, String profile,
      User ownerDetail) {
    return !KmeliaHelper.isKmax(instanceId) &&
        isUserConsideredAsOwner(instanceId, currentUserId, profile, ownerDetail);
  }

  public static boolean isCreationAllowed(NodePK pk, String profile) {
    boolean publicationsInTopic = !pk.isRoot() || (pk.isRoot() &&
        (isPublicationsOnRootAllowed(pk.getInstanceId()) || !isTreeEnabled(pk.getInstanceId())));
    return !SilverpeasRole.USER.isInRole(profile) && publicationsInTopic;
  }

  public static boolean isPublicationsOnRootAllowed(String instanceId) {
    String parameterValue = getParameterValue(instanceId, InstanceParameters.nbPubliOnRoot);
    if (StringUtil.isDefined(parameterValue)) {
      return Integer.parseInt(parameterValue) == 0;
    }
    return true;
  }

  public static boolean isTreeEnabled(String instanceId) {
    String param = getParameterValue(instanceId, InstanceParameters.treeEnabled);
    if (!StringUtil.isDefined(param)) {
      return true;
    }
    return "0".equals(param) || "1".equals(param);
  }

  private static String getParameterValue(String instanceId, String name) {
    return OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(instanceId, name);
  }

  private static boolean hasWritePrivilege(String currentUserId, String profile, User owner) {
    SilverpeasRole role = SilverpeasRole.fromString(profile);
    return role == SilverpeasRole.ADMIN || role == SilverpeasRole.PUBLISHER ||
        role == SilverpeasRole.SUPERVISOR ||
        (owner != null && currentUserId.equals(owner.getId()) && role == SilverpeasRole.WRITER);
  }
}
