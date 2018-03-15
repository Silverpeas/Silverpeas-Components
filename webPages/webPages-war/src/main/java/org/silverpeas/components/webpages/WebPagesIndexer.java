/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.components.webpages;

import org.silverpeas.components.webpages.model.WebPagesException;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.index.components.ComponentIndexation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author sdevolder
 */
@Singleton
@Named("webPages" + ComponentIndexation.QUALIFIER_SUFFIX)
public class WebPagesIndexer implements ComponentIndexation {

  private static String XML_TEMPLATE_PARAM = "xmlTemplate";

  @Inject
  private Administration admin;
  @Inject
  private PublicationTemplateManager templateManager;

  @Override
  public void index(SilverpeasComponentInstance componentInst) throws Exception {
    FullIndexEntry indexEntry = getFullIndexEntry((ComponentInstLight) componentInst);
    if (isXMLTemplateUsed(componentInst.getId())) {
      indexForm(componentInst.getId(), indexEntry);
    } else {
      ResourceReference
          resourceReference = new ResourceReference(componentInst.getId(), componentInst.getId());
      WysiwygController.addToIndex(indexEntry, resourceReference, null);
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void indexForm(String componentId, FullIndexEntry indexEntry) throws WebPagesException {
    RecordSet recordSet;
    try {
      PublicationTemplate pub = templateManager.getPublicationTemplate(
          componentId + ":" + getShortNameOfXMLTemplateUsedFor(componentId));
      recordSet = pub.getRecordSet();
      recordSet.indexRecord("0", getShortNameOfXMLTemplateUsedFor(componentId), indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private FullIndexEntry getFullIndexEntry(ComponentInstLight component) {
    FullIndexEntry indexEntry =
        new FullIndexEntry(component.getId(), "Component", component.getId());
    indexEntry.setCreationDate(component.getCreateDate());
    int createdBy = component.getCreatedBy();
    if (createdBy != -1) {
      indexEntry.setCreationUser(String.valueOf(createdBy));
    }
    indexEntry.setLastModificationDate(component.getUpdateDate());
    int updatedBy = component.getUpdatedBy();
    if (updatedBy != -1) {
      indexEntry.setLastModificationUser(String.valueOf(createdBy));
    }
    indexEntry.setTitle(component.getLabel());
    indexEntry.setPreview(component.getDescription());
    return indexEntry;
  }

  private String getXMLTemplateUsedFor(String componentId) {
    return admin.getComponentParameterValue(componentId, XML_TEMPLATE_PARAM);
  }

  private boolean isXMLTemplateUsed(String componentId) {
    return StringUtil.isDefined(getXMLTemplateUsedFor(componentId));
  }

  private String getShortNameOfXMLTemplateUsedFor(String componentId) {
    String xmlFormName = getXMLTemplateUsedFor(componentId);
    return xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
  }

}