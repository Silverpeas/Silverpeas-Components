/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.webpages;

import com.silverpeas.webpages.model.WebPagesRuntimeException;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * @author dlesimple
 */
public class WebPagesCallBack implements CallBack {

  private OrganizationController oc;

  public WebPagesCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int iParam, String sParam, Object extraParam) {
    SilverTrace.info("webPages", "WebPagesCallback.doInvoke()",
        "root.MSG_GEN_ENTER_METHOD", "action = " + action + ", iParam = "
            + iParam + ", sParam = " + sParam + ", extraParam = "
            + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("webPages", "WebPagesCallback.doInvoke()",
          "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped ! action = " + action
              + ", sParam = " + sParam + ", extraParam = "
              + extraParam.toString());
      return;
    }

    try {
      if (sParam.startsWith("webPages")) {
        // extraction userId
        String sUserId = Integer.toString(iParam);
        String componentId = sParam;
        String pubId = (String) extraParam;
        if (oc == null) {
          oc = new OrganizationController();
        }

        // If parameter useSubscription is used
        if ("yes".equals(oc.getComponentParameterValue(componentId,
            "useSubscription"))) {
          if (isWebPageModified(pubId, action)) {
            externalElementsOfWebPagesHaveChanged(componentId, sUserId);
          }
        }
      }
    } catch (Exception e) {
      throw new WebPagesRuntimeException("WebPagesCallback.doInvoke()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#subscribe()
   */
  @Override
  public void subscribe() {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.subscribeAction(CallBackManager.ACTION_ON_WYSIWYG, this);
  }

  private boolean isWebPageModified(String pubId, int action) {
    return (!pubId.startsWith("Node") && (action == CallBackManager.ACTION_ON_WYSIWYG));
  }

  public void externalElementsOfWebPagesHaveChanged(String componentId,
      String userId) {
    NodePK nodePK = new NodePK("0", componentId);
    WebPagesNotifier notifier = new WebPagesNotifier();
    notifier.sendSubscriptionsNotification(nodePK, userId);
  }

}