/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.component.kmelia;

import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.StringUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.node.model.NodePK;

public class KmeliaPublicationHelper {

  public static boolean isUserConsideredAsOwner(String instanceId, String currentUserId,
      String profile, UserDetail ownerDetail) {
    if ("admin".equals(profile) || "publisher".equals(profile) || "supervisor".equals(profile)
        || (ownerDetail != null && currentUserId.equals(ownerDetail.getId()) && "writer".equals(
        profile))) {
      return true;
    } else if ("writer".equals(profile)) {
      // check if co-writing is enabled
      return StringUtil.getBooleanValue(getParameterValue(instanceId, InstanceParameters.coWriting));
    }
    return false;
  }

  public static boolean isRemovable(String instanceId, String currentUserId, String profile,
      UserDetail ownerDetail) {
    if ("admin".equals(profile) || "publisher".equals(profile) || "supervisor".equals(profile)
        || (ownerDetail != null && currentUserId.equals(ownerDetail.getId()) && "writer".equals(
        profile))) {
      boolean removeOnlyForAdmin =
          StringUtil.getBooleanValue(getParameterValue(instanceId,
          InstanceParameters.suppressionOnlyForAdmin));
      if (!removeOnlyForAdmin || ("admin".equals(profile) && removeOnlyForAdmin)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isCanBeCut(String instanceId, String currentUserId, String profile,
      UserDetail ownerDetail) {
    return !KmeliaHelper.isKmax(instanceId) && isUserConsideredAsOwner(instanceId, currentUserId,
        profile, ownerDetail);
  }

  public static boolean isCreationAllowed(NodePK pk, String profile) {
    boolean publicationsInTopic =
        !pk.isRoot() || (pk.isRoot() && (isPublicationsOnRootAllowed(pk.getInstanceId())
        || !isTreeEnabled(pk
        .getInstanceId())));
    return !SilverpeasRole.user.isInRole(profile) && publicationsInTopic;
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
    return OrganizationControllerProvider.getOrganisationController().getComponentParameterValue(
        instanceId, name);
  }
}
