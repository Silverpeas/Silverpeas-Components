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

package com.silverpeas.webpages.control;

import java.rmi.RemoteException;
import java.util.Collection;

import com.silverpeas.webpages.model.WebPagesRuntimeException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;

/**
 * @author sdevolder
 */
public class WebPagesSessionController extends AbstractComponentSessionController {
  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public WebPagesSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.webpages.multilang.webPagesBundle",
        "com.silverpeas.webpages.settings.webPagesIcons");
  }

  /**
   * Méthode récupérant le role le plus élevé du user
   * @return le role
   */
  public String getProfile() {
    String[] profiles = getUserRoles();
    String flag = "user";
    String profile = "";
    for (int i = 0; i < profiles.length; i++) {
      profile = profiles[i];
      // if admin, return it, we won't find a better profile
      if (profile.equals("admin"))
        return profile;
      if (profile.equals("publisher"))
        flag = profile;
      else if (profile.equals("writer")) {
        if (!flag.equals("publisher"))
          flag = profile;
      } else if (profile.equals("supervisor")) {
        flag = profile;
      }
    }
    return flag;
  }

  /**
   * @return vrai s'il existe un fichier wysiwyg pour l'instance de composant
   */
  public boolean haveGotWysiwygNotEmpty() {
    boolean returnValue = false;
    if (WysiwygController.haveGotWysiwyg(this.getSpaceId(), this
        .getComponentId(), this.getComponentId())) {
      try {
        String contenuWysiwyg = WysiwygController.loadFileAndAttachment(this
            .getSpaceId(), this.getComponentId(), this.getComponentId());
        if ((contenuWysiwyg != null) && (contenuWysiwyg.length() != 0)) {
          returnValue = true;
        }
      } catch (WysiwygException ex) {
        SilverTrace.error("webPages",
            "WebPagesSessionController.haveGotWysiwyg()", "root.", ex);
      }
    }
    return returnValue;
  }

  public void index() {
    try {
      String content = WysiwygController.load(getComponentId(), getComponentId(), null);
      WysiwygController.updateFileAndAttachment(content, getSpaceId(), getComponentId(),
          getComponentId(), getUserId());
    } catch (WysiwygException ex) {
    }
  }

  /**************************************************************************************/
  /* webPages - Gestion des abonnements */
  /**************************************************************************************/
  public synchronized void removeSubscription() throws RemoteException {
    SilverTrace.info("webPages", "WebPagesSessionController.removeSubscription()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getSubscribeBm().removeSubscribe(getUserId(), getNodePK());
    } catch (Exception e) {
      throw new WebPagesRuntimeException(
          "WebPagesSessionController.removeSubscription()",
          SilverpeasRuntimeException.ERROR,
          "webPages.EX_IMPOSSIBLE_DE_SUPPRIMER_ABONNEMENT", e);
    }
  }

  public synchronized void addSubscription() throws RemoteException {
    SilverTrace.info("webPages", "WebPagesSessionController.addSubscription()",
        "root.MSG_GEN_ENTER_METHOD");

    if (isSubscriber()) {
      return;
    }

    try {
      getSubscribeBm().addSubscribe(getUserId(), getNodePK());
    } catch (Exception e) {
      SilverTrace.warn("webPages",
          "WebPagesSessionController.addSubscription()",
          "webPages.EX_SUBSCRIPTION_ADD_FAILED", e);
    }
  }

  public boolean isSubscriber() {
    SilverTrace.info("webPages", "WebPagesSessionController.isSubscriber()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<NodePK> list =
          getSubscribeBm().getUserSubscribePKsByComponent(getUserId(), getComponentId());
      return (list != null && !list.isEmpty());
    } catch (Exception e) {
      throw new WebPagesRuntimeException(
          "WebPagesSessionController.isSubscriber()",
          SilverpeasRuntimeException.ERROR,
          "webPages.EX_IMPOSSIBLE_DOBTENIR_LES_ABONNEMENTS", e);
    }
  }

  private NodePK getNodePK() {
    return new NodePK("0", getSpaceId(), getComponentId());
  }

  public SubscribeBm getSubscribeBm() {
    SubscribeBm subscribeBm = null;
    try {
      SubscribeBmHome subscribeBmHome = (SubscribeBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.SUBSCRIBEBM_EJBHOME, SubscribeBmHome.class);
      subscribeBm = subscribeBmHome.create();
    } catch (Exception e) {
      throw new WebPagesRuntimeException(
          "WebPagesSessionController.getSubscribeBm()",
          SilverpeasRuntimeException.ERROR,
          "webPages.EX_IMPOSSIBLE_DE_FABRIQUER_SUBSCRIBEBM_HOME", e);
    }
    return subscribeBm;
  }

  /**
   * Return boolean if subscription is used for this instance
   * @return boolean
   */
  public boolean isSubscriptionUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("useSubscription"));
  }

}