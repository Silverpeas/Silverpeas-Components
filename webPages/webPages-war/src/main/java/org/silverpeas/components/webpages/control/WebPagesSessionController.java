/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.webpages.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.webpages.model.WebPagesException;
import org.silverpeas.components.webpages.notification.WebPagesUserNotifier;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.subscription.SubscriptionContext;

import java.util.Date;
import java.util.List;

public class WebPagesSessionController extends AbstractComponentSessionController {

  private String usedTemplate = null;

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public WebPagesSessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
            "org.silverpeas.webpages.multilang.webPagesBundle",
            "org.silverpeas.webpages.settings.webPagesIcons");

    registerXMLForm();
  }

  /**
   * Méthode récupérant le role le plus élevé du user
   * @return le role
   */
  public String getProfile() {
    final SilverpeasRole highestRole = getHighestSilverpeasUserRole();
    final SilverpeasRole normalizedRole = highestRole.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER)
        ? highestRole
        : SilverpeasRole.USER;
    return normalizedRole.getName();
  }

  /**
   * @return vrai s'il existe un fichier wysiwyg pour l'instance de composant
   */
  public boolean haveGotWysiwygNotEmpty() {
    return WysiwygController.haveGotWysiwyg(getComponentId(), getComponentId(),
        I18NHelper.defaultLanguage);
  }

  public String manageSubscriptions() {
    SubscriptionContext subscriptionContext = getSubscriptionContext();
    subscriptionContext.initialize(ComponentSubscriptionResource.from(getComponentId()));
    return subscriptionContext.getDestinationUrl();
  }

  private NodePK getNodePK() {
    return new NodePK(NodePK.ROOT_NODE_ID, getSpaceId(), getComponentId());
  }

  /**
   * Return boolean if subscription is used for this instance
   * @return boolean
   */
  public boolean isSubscriptionUsed() {
    return StringUtil.getBooleanValue(
        getComponentParameterValue(ResourceSubscriptionService.Constants.SUBSCRIPTION_PARAMETER));
  }

  /*
   * XML template management
   */
  private String getUsedXMLTemplate() {
    return getComponentParameterValue("xmlTemplate");
  }

  private String getUsedXMLTemplateShortname() {
    String xmlFormName = getUsedXMLTemplate();
    return xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
  }

  public boolean isXMLTemplateUsed() {
    return StringUtil.isDefined(getUsedXMLTemplate());
  }

  private PublicationTemplate getXMLTemplate() throws WebPagesException {
    if (isTemplateChanged()) {
      registerXMLForm();
    }
    try {
      return PublicationTemplateManager.getInstance().
              getPublicationTemplate(getComponentId() + ":" + getUsedXMLTemplateShortname());
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getXMLTemplate()",
              SilverpeasException.ERROR, "webPages.EX_CANT_GET_TEMPLATE", e);
    }
  }

  public DataRecord getDataRecord() throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate();

      RecordSet recordSet = pubTemplate.getRecordSet();
      DataRecord data = recordSet.getRecord("0");
      if (data == null) {
        data = recordSet.getEmptyRecord();
        data.setId("0");
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
      data = recordSet.getRecord("0");
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
      DataRecord data = set.getRecord("0");
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId("0");
      }

      PagesContext context = new PagesContext("useless", "0", getLanguage(), false, getComponentId(),
              getUserId());
      context.setEncoding("UTF-8");
      context.setObjectId("0");
      context.setContentLanguage(I18NHelper.defaultLanguage);

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
        indexEntry.setPreview(component.getDescription());
      }

      recordSet.indexRecord("0", getUsedXMLTemplateShortname(), indexEntry);

      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (FormException e) {
      throw new WebPagesException("WebPagesSessionController.indexForm()",
              SilverpeasException.ERROR, "webPages.EX_CANT_INDEX_DATA", e);
    }
  }

  private boolean isTemplateChanged() {
    return isXMLTemplateUsed() && !getUsedXMLTemplate().equals(usedTemplate);
  }

  private void registerXMLForm() {
    if (isXMLTemplateUsed()) {
      // register xmlForm to component
      try {
        PublicationTemplateManager.getInstance()
            .addDynamicPublicationTemplate(getComponentId() + ":" + getUsedXMLTemplateShortname(),
                getUsedXMLTemplate());
        usedTemplate = getUsedXMLTemplate();
      } catch (PublicationTemplateException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}