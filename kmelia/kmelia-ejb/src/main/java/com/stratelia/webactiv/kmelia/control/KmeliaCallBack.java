/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmelia.control;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.importExport.attachment.AttachmentDetail;
import org.silverpeas.importExport.versioning.Document;

import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
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
    SilverTrace.info("kmelia", "KmeliaCallback.doInvoke()", "root.MSG_GEN_ENTER_METHOD",
        "action = " + action + ", iParam = " + iParam + ", componentId = " + componentId);

    if (extraParam != null) {
      SilverTrace.info("kmelia", "KmeliaCallback.doInvoke()", "root.MSG_GEN_PARAM_VALUE",
          "extraParam = " + extraParam.toString());
    }

    if (iParam == -1) {
      SilverTrace.info("kmelia", "KmeliaCallback.doInvoke()", "root.MSG_GEN_PARAM_VALUE",
          "userId is null. Callback stopped !");
      return;
    }

    // extraction userId
    String sUserId = Integer.toString(iParam);

    if (componentId != null && (componentId.startsWith("kmelia")
        || componentId.startsWith("toolbox") || componentId.startsWith("kmax"))) {
      try {
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
        } else if (extraParam instanceof String || extraParam instanceof AttachmentDetail
            || extraParam instanceof Document || extraParam instanceof SimpleDocument) {
          String pubId = null;
          if (extraParam instanceof String) {
            pubId = (String) extraParam;
          } else if (extraParam instanceof AttachmentDetail) {
            AttachmentDetail attachment = (AttachmentDetail) extraParam;
            if (attachment.getContext().equalsIgnoreCase("images")) {
              pubId = attachment.getForeignKey().getId();
            }
          } else if (extraParam instanceof Document) {
            Document document = (Document) extraParam;
            pubId = document.getForeignKey().getId();
          } else if (extraParam instanceof SimpleDocument) {
            SimpleDocument document = (SimpleDocument) extraParam;
            pubId = document.getForeignId();
          }
          if (isPublicationModified(pubId, action)) {
            getKmeliaBm().externalElementsOfPublicationHaveChanged(new PublicationPK(pubId,
                componentId), sUserId, action);
          }
        }
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaCallback.doInvoke()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    } else {
      if (action == CallBackManager.ACTION_BEFORE_REMOVE_USER) {
        getKmeliaBm().userHaveBeenDeleted(sUserId);
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
    callBackManager.subscribeAction(CallBackManager.ACTION_ATTACHMENT_ADD, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_ATTACHMENT_UPDATE, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_VERSIONING_ADD, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_VERSIONING_UPDATE, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_CUTANDPASTE, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER, this);
  }

  private boolean isPublicationModified(String pubId, int action) {
    if (StringUtil.isDefined(pubId) && !pubId.startsWith("Node") && (action
        == CallBackManager.ACTION_ON_WYSIWYG
        || action == CallBackManager.ACTION_ATTACHMENT_ADD
        || action == CallBackManager.ACTION_ATTACHMENT_UPDATE
        || action == CallBackManager.ACTION_VERSIONING_ADD
        || action == CallBackManager.ACTION_VERSIONING_UPDATE)) {
      return true;
    } else {
      return false;
    }
  }

  private KmeliaBm getKmeliaBm() {
    KmeliaBm kmeliaBm = null;
    try {
      kmeliaBm = EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("KmeliaSessionController.setKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return kmeliaBm;
  }
}