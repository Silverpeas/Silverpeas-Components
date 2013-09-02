package org.silverpeas.component.kmelia;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.node.model.NodePK;

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
    return OrganisationControllerFactory.getOrganisationController().getComponentParameterValue(
        instanceId, name);
  }
}
