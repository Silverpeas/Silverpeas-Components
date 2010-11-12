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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.webpages.WebPagesNotifier;
import com.silverpeas.webpages.model.WebPagesException;
import com.silverpeas.webpages.model.WebPagesRuntimeException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBm;
import com.stratelia.webactiv.util.subscribe.control.SubscribeBmHome;

public class WebPagesSessionController extends AbstractComponentSessionController {

  WebPagesNotifier notifier = null;

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

    if (isXMLTemplateUsed()) {
      // register xmlForm to component
      try {
        PublicationTemplateManager.getInstance().addDynamicPublicationTemplate(getComponentId()
            + ":" + getUsedXMLTemplateShortname(), getUsedXMLTemplate());
      } catch (PublicationTemplateException e) {
        SilverTrace.error("webPages", "WebPagesSessionController()", "", "template = " +
            getUsedXMLTemplate(), e);
      }
    }
  }

  /**
   * Méthode récupérant le role le plus élevé du user
   * @return le role
   */
  public String getProfile() {
    String[] profiles = getUserRoles();
    String flag = "user";
    for (String profile : profiles) {
      // if publisher, return it, we won't find a better profile
      if (profile.equals("publisher")) {
        return profile;
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
  public synchronized void removeSubscription() {
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

  public synchronized void addSubscription() {
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

  private SubscribeBm getSubscribeBm() {
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

  /*
   * XML template management
   */
  private String getUsedXMLTemplate() {
    return getComponentParameterValue("xmlTemplate");
  }

  private String getUsedXMLTemplateShortname() {
    String xmlFormName = getUsedXMLTemplate();
    return xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
  }

  public boolean isXMLTemplateUsed() {
    return StringUtil.isDefined(getUsedXMLTemplate());
  }

  private PublicationTemplate getXMLTemplate() throws WebPagesException {
    try {
      PublicationTemplate pubTemplate =
          PublicationTemplateManager.getInstance().getPublicationTemplate(
              getComponentId() + ":" + getUsedXMLTemplateShortname());

      return pubTemplate;
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getXMLTemplate()",
          SilverpeasException.ERROR, "webPages.EX_CANT_GET_TEMPLATE", e);
    }
  }

  public DataRecord getDataRecord() throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate();

      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord("0", getLanguage());
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId("0");
        data.setLanguage(getLanguage());
      }

      return data;
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.getDataRecord()",
          SilverpeasException.ERROR, "webPages.EX_CANT_GET_DATA", e);
    }
  }

  public boolean isXMLContentDefined() throws WebPagesException {
    DataRecord data;
    try {
      PublicationTemplate pubTemplate = getXMLTemplate();

      RecordSet recordSet = pubTemplate.getRecordSet();
      data = recordSet.getRecord("0", getLanguage());
      return data != null;
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.isXMLContentDefined()",
          SilverpeasException.ERROR, "webPages.EX_CANT_GET_DATA", e);
    }
  }

  public Form getViewForm() throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate();

      return pubTemplate.getViewForm();
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getViewForm()",
          SilverpeasException.ERROR, "webPages.EX_CANT_GET_VIEWFORM", e);
    }
  }

  public Form getUpdateForm() throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate();

      return pubTemplate.getUpdateForm();
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getUpdateForm()",
          SilverpeasException.ERROR, "webPages.EX_CANT_GET_UPDATEFORM", e);
    }
  }

  public void saveDataRecord(List<FileItem> items) throws WebPagesException {
    // save data in database
    RecordSet set;
    try {
      PublicationTemplate pub = getXMLTemplate();

      set = pub.getRecordSet();
      Form form = pub.getUpdateForm();

      DataRecord data = set.getRecord("0", getLanguage());
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId("0");
        data.setLanguage(getLanguage());
      }

      PagesContext context =
          new PagesContext("useless", "0", getLanguage(), false, getComponentId(), getUserId());
      context.setEncoding("UTF-8");
      context.setObjectId("0");
      context.setContentLanguage(getLanguage());

      form.update(items, data, context);
      set.save(data);
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.saveDataRecord()",
          SilverpeasException.ERROR, "webPages.EX_CANT_SAVE_DATA", e);
    }

    // send subscriptions
    getNotifier().sendSubscriptionsNotification(getNodePK(), getUserId());

    // index updated data
    try {
      FullIndexEntry indexEntry =
          new FullIndexEntry(getComponentId(), "Component", getComponentId());
      indexEntry.setCreationDate(new Date());
      indexEntry.setCreationUser(getUserId());
      indexEntry.setTitle(getComponentLabel());
      ComponentInstLight component =
          getOrganizationController().getComponentInstLight(getComponentId());
      if (component != null) {
        indexEntry.setPreView(component.getDescription());
      }

      set.indexRecord("0", getUsedXMLTemplateShortname(), indexEntry);

      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (FormException e) {
      throw new WebPagesException("WebPagesSessionController.saveDataRecord()",
          SilverpeasException.ERROR, "webPages.EX_CANT_INDEX_DATA", e);
    }
  }

  private WebPagesNotifier getNotifier() {
    if (notifier == null) {
      notifier = new WebPagesNotifier();
    }
    return notifier;
  }
}