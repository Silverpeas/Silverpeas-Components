/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;

import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscription;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.webpages.model.WebPagesException;
import com.silverpeas.webpages.notification.WebPagesUserNotifier;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.attachment.AttachmentServiceProvider;
import org.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.silverpeas.util.exception.SilverpeasException;
import com.stratelia.webactiv.node.model.NodePK;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;

import java.util.Date;
import java.util.List;

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

    if (isXMLTemplateUsed()) {
      // register xmlForm to component
      try {
        PublicationTemplateManager.getInstance().addDynamicPublicationTemplate(getComponentId()
                + ":" + getUsedXMLTemplateShortname(), getUsedXMLTemplate());
      } catch (PublicationTemplateException e) {
        SilverTrace.error("webPages", "WebPagesSessionController()", "", "template = "
                + getUsedXMLTemplate(), e);
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
    return WysiwygController.haveGotWysiwyg(getComponentId(), getComponentId(),
        I18NHelper.defaultLanguage);
  }

  public void index() throws WebPagesException {
    if (isXMLTemplateUsed()) {
      indexForm(null);
    } else {
      ForeignPK foreignPK = new ForeignPK(getComponentId(), getComponentId());
      AttachmentServiceProvider.getAttachmentService().indexAllDocuments(foreignPK, null, null);
    }
  }

  public synchronized void removeSubscription() {
    SilverTrace.info("webPages", "WebPagesSessionController.unsubscribeFromNode()",
            "root.MSG_GEN_ENTER_METHOD");
    getSubscribeService().unsubscribe(new ComponentSubscription(getUserId(), getComponentId()));
  }

  public synchronized void addSubscription() {
    SilverTrace.info("webPages", "WebPagesSessionController.addSubscription()",
            "root.MSG_GEN_ENTER_METHOD");
    if (isSubscriber()) {
      return;
    }
    getSubscribeService().subscribe(new ComponentSubscription(getUserId(), getComponentId()));
  }

  public boolean isSubscriber() {
    SilverTrace.info("webPages", "WebPagesSessionController.isSubscriber()",
            "root.MSG_GEN_ENTER_METHOD");
    return getSubscribeService().existsSubscription(
        new ComponentSubscription(getUserId(), getComponentId()));
  }

  private NodePK getNodePK() {
    return new NodePK(NodePK.ROOT_NODE_ID, getSpaceId(), getComponentId());
  }

  private SubscriptionService getSubscribeService() {
    return SubscriptionServiceProvider.getSubscribeService();
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
      PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance().
              getPublicationTemplate(getComponentId() + ":" + getUsedXMLTemplateShortname());
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

      PagesContext context = new PagesContext("useless", "0", getLanguage(), false, getComponentId(),
              getUserId());
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
    WebPagesUserNotifier.notify(getNodePK(), getUserId());

    // index updated data
    indexForm(set);
  }

  private void indexForm(RecordSet recordSet) throws WebPagesException {
    try {
      if (recordSet == null) {
        PublicationTemplate pub = getXMLTemplate();
        recordSet = pub.getRecordSet();
      }
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.indexForm()",
          SilverpeasException.ERROR, "webPages.EX_CANT_GET_FORM", e);
    }
    // index data
    try {
      FullIndexEntry indexEntry =
              new FullIndexEntry(getComponentId(), "Component", getComponentId());
      indexEntry.setCreationDate(new Date());
      indexEntry.setCreationUser(getUserId());
      indexEntry.setTitle(getComponentLabel());
      ComponentInstLight component = getOrganisationController().getComponentInstLight(getComponentId());
      if (component != null) {
        indexEntry.setPreView(component.getDescription());
      }

      recordSet.indexRecord("0", getUsedXMLTemplateShortname(), indexEntry);

      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (FormException e) {
      throw new WebPagesException("WebPagesSessionController.indexForm()",
              SilverpeasException.ERROR, "webPages.EX_CANT_INDEX_DATA", e);
    }
  }
}