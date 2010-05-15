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
package com.silverpeas.webpages;

import java.sql.Connection;
import java.util.Collection;
import java.util.Vector;

import com.silverpeas.webpages.model.WebPagesRuntimeException;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;

/**
 * @author dlesimple
 */
public class WebPagesCallBack extends CallBack {

  private OrganizationController oc;

  public WebPagesCallBack() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.CallBack#doInvoke(int, int,
   * java.lang.String, java.lang.Object)
   */
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
        if (oc == null)
          oc = new OrganizationController();

        // If parameter useSubscription is used
        if ("yes".equals(oc.getComponentParameterValue(componentId,
            "useSubscription"))) {
          if (isWebPageModified(pubId, action))
            externalElementsOfWebPagesHaveChanged(componentId, sUserId);
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
  public void subscribe() {
    CallBackManager.subscribeAction(CallBackManager.ACTION_ON_WYSIWYG, this);
  }

  private boolean isWebPageModified(String pubId, int action) {
    if (!pubId.startsWith("Node")
        && (action == CallBackManager.ACTION_ON_WYSIWYG))
      return true;
    else
      return false;
  }

  public void externalElementsOfWebPagesHaveChanged(String componentId,
      String userId) {
    NodePK nodePK = new NodePK("0", componentId);
    sendSubscriptionsNotification(nodePK, userId);
  }

  private void sendSubscriptionsNotification(NodePK nodePK, String userId) {
    // send email alerts
    try {
      Collection subscriberIds = getSubscribeBm().getNodeSubscriberDetails(
          nodePK);

      if (subscriberIds != null && subscriberIds.size() > 0) {

        ResourceLocator message = new ResourceLocator(
            "com.silverpeas.webpages.multilang.webPagesBundle", "fr");
        ResourceLocator message_en = new ResourceLocator(
            "com.silverpeas.webpages.multilang.webPagesBundle", "en");

        // french notifications
        String subject = getSubscriptionsNotificationSubject(message);
        String body = getSubscriptionsNotificationBody(message, nodePK, userId);

        // english notifications
        String subject_en = getSubscriptionsNotificationSubject(message_en);
        String body_en = getSubscriptionsNotificationBody(message_en, nodePK,
            userId);

        NotificationMetaData notifMetaData = new NotificationMetaData(
            NotificationParameters.NORMAL, subject, body);
        notifMetaData.addLanguage("en", subject_en, body_en);

        notifMetaData.setUserRecipients(new Vector(subscriberIds));
        notifMetaData.setLink(URLManager.getURL(null, null, nodePK
            .getInstanceId())
            + "Main");
        notifMetaData.setComponentId(nodePK.getInstanceId());
        notifyUsers(notifMetaData, userId);
      }
    } catch (Exception e) {
      SilverTrace.warn("webPages",
          "WebPagesCallback.sendSubscriptionsNotification()",
          "webPages.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "nodeId = "
          + nodePK.getId(), e);
    }
  }

  private String getSubscriptionsNotificationBody(ResourceLocator message,
      NodePK nodePK, String userId) {
    StringBuffer messageText = new StringBuffer();
    if (oc == null)
      oc = new OrganizationController();

    messageText.append(message.getString("webPages.body")).append(" ").append(
        oc.getUserDetail(userId).getDisplayedName()).append(".\n");

    return messageText.toString();
  }

  private String getSubscriptionsNotificationSubject(ResourceLocator message) {
    return message.getString("webPages.subscription");
  }

  private void notifyUsers(NotificationMetaData notifMetaData, String senderId) {
    Connection con = null;
    try {
      con = getConnection();
      notifMetaData.setConnection(con);
      if (notifMetaData.getSender() == null
          || notifMetaData.getSender().length() == 0)
        notifMetaData.setSender(senderId);
      getNotificationSender(notifMetaData.getComponentId()).notifyUser(
          notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("webPages", "WebPagesCallback.notifyUsers()",
          "webPages.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", e);
    } finally {
      freeConnection(con);
    }
  }

  private NotificationSender getNotificationSender(String componentId) {
    // must return a new instance each time
    // This is to resolve Serializable problems
    NotificationSender notifSender = new NotificationSender(componentId);
    return notifSender;
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new WebPagesRuntimeException("WebPagesCallback.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("webPages", "WebPagesCallback.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public SubscribeBm getSubscribeBm() {
    SubscribeBm subscribeBm = null;
    try {
      SubscribeBmHome subscribeBmHome = (SubscribeBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.SUBSCRIBEBM_EJBHOME, SubscribeBmHome.class);
      subscribeBm = subscribeBmHome.create();
    } catch (Exception e) {
      throw new WebPagesRuntimeException("WebPagesCallback.getSubscribeBm()",
          SilverpeasRuntimeException.ERROR,
          "webPages.EX_IMPOSSIBLE_DE_FABRIQUER_SUBSCRIBEBM_HOME", e);
    }
    return subscribeBm;
  }

}