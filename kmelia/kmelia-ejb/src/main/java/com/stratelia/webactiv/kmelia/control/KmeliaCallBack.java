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

/*
 * Created on 4 avr. 2005
 *
 */
package com.stratelia.webactiv.kmelia.control;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * @author neysseri
 */
public class KmeliaCallBack implements CallBack {

  private KmeliaBmHome kmeliaHome = null;

  public KmeliaCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void doInvoke(int action, int iParam, String componentId,
      Object extraParam) {
    SilverTrace.info("kmelia", "KmeliaCallback.doInvoke()",
        "root.MSG_GEN_ENTER_METHOD", "action = " + action + ", iParam = "
        + iParam + ", componentId = " + componentId + ", extraParam = "
        + extraParam.toString());

    if (iParam == -1) {
      SilverTrace.info("kmelia", "KmeliaCallback.doInvoke()",
          "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped ! action = " + action
          + ", componentId = " + componentId + ", extraParam = "
          + extraParam.toString());
      return;
    }

    if (componentId != null
        && (componentId.startsWith("kmelia")
        || componentId.startsWith("toolbox") || componentId
        .startsWith("kmax"))) {
      try {
        // extraction userId
        String sUserId = Integer.toString(iParam);

        if (action == CallBackManager.ACTION_CUTANDPASTE) {
          if (extraParam instanceof PublicationPK) {
            PublicationPK pubPK = (PublicationPK) extraParam;

            // Remove publication which has been cut and paste
            getKmeliaBm().deletePublication(pubPK);
          } else if (extraParam instanceof NodePK) {
            NodePK nodePK = (NodePK) extraParam;

            // Remove topic which has been cut and paste
            getKmeliaBm().deleteTopic(nodePK);
          }
        } else if (extraParam instanceof String) {
          String pubId = (String) extraParam;

          if (isPublicationModified(pubId, action)) {
            getKmeliaBm().externalElementsOfPublicationHaveChanged(
                new PublicationPK(pubId, componentId), sUserId);
          }
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaCallback.doInvoke()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
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
    callBackManager
        .subscribeAction(CallBackManager.ACTION_ATTACHMENT_ADD, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_ATTACHMENT_UPDATE,
        this);
    callBackManager.subscribeAction(CallBackManager.ACTION_ATTACHMENT_REMOVE,
        this);
    callBackManager
        .subscribeAction(CallBackManager.ACTION_VERSIONING_ADD, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_VERSIONING_UPDATE,
        this);
    callBackManager.subscribeAction(CallBackManager.ACTION_VERSIONING_REMOVE,
        this);
    callBackManager.subscribeAction(CallBackManager.ACTION_CUTANDPASTE, this);
  }

  private boolean isPublicationModified(String pubId, int action) {
    if (!pubId.startsWith("Node")
        &&
        (action == CallBackManager.ACTION_ON_WYSIWYG
            || action == CallBackManager.ACTION_ATTACHMENT_ADD
            || action == CallBackManager.ACTION_ATTACHMENT_UPDATE
            || action == CallBackManager.ACTION_ATTACHMENT_REMOVE
            || action == CallBackManager.ACTION_VERSIONING_ADD
            || action == CallBackManager.ACTION_VERSIONING_UPDATE || action == CallBackManager.ACTION_VERSIONING_REMOVE))
      return true;
    else
      return false;
  }

  private KmeliaBm getKmeliaBm() {
    KmeliaBm kmeliaBm = null;
    try {
      kmeliaBm = getKmeliaHome().create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.setKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return kmeliaBm;
  }

  private KmeliaBmHome getKmeliaHome() {
    if (kmeliaHome == null) {
      try {
        kmeliaHome = (KmeliaBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.KMELIABM_EJBHOME, KmeliaBmHome.class);
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaCallback.getKmeliaHome()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            e);
      }
    }
    return kmeliaHome;
  }
}