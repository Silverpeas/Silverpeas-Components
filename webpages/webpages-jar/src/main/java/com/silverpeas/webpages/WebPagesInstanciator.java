/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.webpages;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;

import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class WebPagesInstanciator implements ComponentsInstanciatorIntf {

  public WebPagesInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("webPages", "WebPagesInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "componentId = " + componentId + ", userId =" + userId);
    removeWysiwygContent(componentId);
    removeXMLContent(componentId);
    SilverTrace.info("webPages", "WebPagesInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private void removeWysiwygContent(String componentId) throws InstanciationException {
    try {
      WysiwygController.deleteFileAndAttachment(componentId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("WebPagesInstanciator.delete",
          SilverpeasException.ERROR, "webPages.WYSIWYG_DELETION_FAILED", e);
    }
  }

  private void removeXMLContent(String componentId) throws InstanciationException {
    String xmlFormName = new OrganizationController().getComponentParameterValue(componentId,
        "xmlTemplate");
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf(
          '.'));
      try {
        PublicationTemplateManager.getInstance().removePublicationTemplate(
            componentId + ":" + xmlShortName);
      } catch (PublicationTemplateException e) {
        throw new InstanciationException("WebPagesInstanciator.removeXMLContent",
            SilverpeasException.ERROR, "webPages.XMLCONTENT_DELETION_FAILED", e);
      }
    }
  }
}
