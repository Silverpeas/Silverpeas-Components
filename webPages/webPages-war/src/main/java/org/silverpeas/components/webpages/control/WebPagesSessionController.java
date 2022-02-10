/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.util.Charsets;
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
  public static final String PARAM_MAIN_TEMPLATE = "xmlTemplate";
  public static final String PARAM_OTHER_TEMPLATE = "xmlTemplate2";

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

    registerXMLForm(PARAM_MAIN_TEMPLATE);
    registerXMLForm(PARAM_OTHER_TEMPLATE);
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
        I18NHelper.DEFAULT_LANGUAGE);
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
  private String getUsedXMLTemplate(String param) {
    return getComponentParameterValue(param);
  }

  private String getUsedXMLTemplateShortname(String param) {
    String xmlFormName = getUsedXMLTemplate(param);
    if (StringUtil.isDefined(xmlFormName)) {
      return xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
    }
    return null;
  }

  public boolean isXMLTemplateUsed(String param) {
    return StringUtil.isDefined(getUsedXMLTemplate(param));
  }

  private PublicationTemplate getXMLTemplate(String param) throws WebPagesException {
    if (!isXMLTemplateUsed(param)) {
      return null;
    }
    if (isTemplateChanged(param)) {
      registerXMLForm(param);
    }
    try {
      return PublicationTemplateManager.getInstance().
              getPublicationTemplate(getComponentId() + ":" + getUsedXMLTemplateShortname(param));
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getXMLTemplate()",
              SilverpeasException.ERROR, "webPages.EX_CANT_GET_TEMPLATE", e);
    }
  }

  public DataRecord getDataRecord(String param) throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate(param);
      if (pubTemplate != null) {
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord("0");
        if (data == null) {
          data = recordSet.getEmptyRecord();
          data.setId("0");
        }
        return data;
      }
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.getDataRecord()",
              SilverpeasException.ERROR, "webPages.EX_CANT_GET_DATA", e);
    }
    return null;
  }

  public boolean isXMLContentDefined(String param) throws WebPagesException {
    DataRecord data = null;
    try {
      PublicationTemplate pubTemplate = getXMLTemplate(param);
      if (pubTemplate != null) {
        RecordSet recordSet = pubTemplate.getRecordSet();
        data = recordSet.getRecord("0");
      }
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.isXMLContentDefined()",
              SilverpeasException.ERROR, "webPages.EX_CANT_GET_DATA", e);
    }
    return data != null;
  }

  public Form getViewForm(String param) throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate(param);
      if (pubTemplate != null) {
        Form form = pubTemplate.getViewForm();
        if (form != null) {
          form.setData(getDataRecord(param));
        }
        return form;
      }
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getViewForm()",
              SilverpeasException.ERROR, "webPages.EX_CANT_GET_VIEWFORM", e);
    }
    return null;
  }

  public Form getUpdateForm(String param) throws WebPagesException {
    try {
      PublicationTemplate pubTemplate = getXMLTemplate(param);
      if (pubTemplate != null) {
        Form form = pubTemplate.getUpdateForm();
        if (form != null) {
          form.setData(getDataRecord(param));
        }
        return form;
      }
    } catch (PublicationTemplateException e) {
      throw new WebPagesException("WebPagesSessionController.getUpdateForm()",
              SilverpeasException.ERROR, "webPages.EX_CANT_GET_UPDATEFORM", e);
    }
    return null;
  }

  public void saveDataRecord(List<FileItem> items) throws WebPagesException {
    // save data in database
    PagesContext context = new PagesContext("useless", "0", getLanguage(), false, getComponentId(),
            getUserId());
    context.setEncoding(Charsets.UTF_8.name());
    context.setObjectId("0");
    context.setContentLanguage(I18NHelper.DEFAULT_LANGUAGE);

    saveDataRecord(PARAM_MAIN_TEMPLATE, items, context);
    saveDataRecord(PARAM_OTHER_TEMPLATE, items, context);

    // send subscriptions
    WebPagesUserNotifier.notify(getNodePK(), getUserId());

    // index updated data
    indexForms();
  }

  private void indexForms() {

    // index data
    FullIndexEntry indexEntry =
            new FullIndexEntry(getComponentId(), "Component", "0");
    indexEntry.setCreationDate(new Date());
    indexEntry.setCreationUser(getUserId());
    indexEntry.setTitle(getComponentLabel());
    ComponentInstLight component = getOrganisationController().getComponentInstLight(getComponentId());
    if (component != null) {
      indexEntry.setPreview(component.getDescription());
    }

    indexFormContent(PARAM_MAIN_TEMPLATE, indexEntry);
    indexFormContent(PARAM_OTHER_TEMPLATE, indexEntry);

    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private boolean isTemplateChanged(String param) {
    return isXMLTemplateUsed(param) && !getUsedXMLTemplate(param).equals(usedTemplate);
  }

  private void registerXMLForm(String param) {
    if (isXMLTemplateUsed(param)) {
      // register xmlForm to component
      try {
        PublicationTemplateManager.getInstance()
            .addDynamicPublicationTemplate(getComponentId() + ":" + getUsedXMLTemplateShortname(param),
                getUsedXMLTemplate(param));
        usedTemplate = getUsedXMLTemplate(param);
      } catch (PublicationTemplateException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  public void saveDataRecord(String param, List<FileItem> items, PagesContext context)
      throws WebPagesException {
    // save data in database
    try {
      PublicationTemplate pub = getXMLTemplate(param);
      if (pub != null) {
        RecordSet set = pub.getRecordSet();
        Form form = pub.getUpdateForm();
        DataRecord data = set.getRecord("0");
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId("0");
        }

        form.update(items, data, context);
        set.save(data);
      }
    } catch (Exception e) {
      throw new WebPagesException("WebPagesSessionController.saveDataRecord()",
          SilverpeasException.ERROR, "webPages.EX_CANT_SAVE_DATA", e);
    }
  }

  private void indexFormContent(String param, FullIndexEntry indexEntry) {
    try {
      PublicationTemplate pub = getXMLTemplate(param);
      if (pub != null) {
        RecordSet recordSet = pub.getRecordSet();
        recordSet.indexRecord("0", getUsedXMLTemplateShortname(PARAM_MAIN_TEMPLATE), indexEntry);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }
}
