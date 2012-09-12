/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.process;

import org.silverpeas.process.io.file.FileBasePath;
import org.silverpeas.process.management.AbstractFileProcess;
import org.silverpeas.process.management.ProcessExecutionContext;

import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractGalleryFileProcess extends
    AbstractFileProcess<GalleryProcessExecutionContext> {
  protected final static FileBasePath BASE_PATH = FileBasePath.UPLOAD_PATH;
  protected final static ResourceLocator gallerySettings = new ResourceLocator(
      "com.silverpeas.gallery.settings.gallerySettings", "");

  private final PhotoDetail photo;
  private OrganizationController organizationController;

  /**
   * Default constructor
   * @param photo
   */
  protected AbstractGalleryFileProcess(final PhotoDetail photo) {
    this.photo = photo;
  }

  /**
   * @return the photo
   */
  protected PhotoDetail getPhoto() {
    return photo;
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  protected PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  /**
   * Access to the shared OrganizationController
   * @return
   */
  protected OrganizationController getOrganizationController() {
    if (organizationController == null) {
      organizationController =
          OrganizationControllerFactory.getFactory().getOrganizationController();
    }
    return organizationController;
  }

  /**
   * Gets an XML form name if it exists for the photo
   * @return
   */
  protected String getXMLFormName(final ProcessExecutionContext context) {
    String formName =
        getOrganizationController().getComponentParameterValue(context.getComponentInstanceId(),
            "XMLFormName");
    // contrôle du formulaire et retour du nom si convenable
    if (StringUtil.isDefined(formName)) {
      try {
        final String xmlFormShortName =
            formName.substring(formName.indexOf("/") + 1, formName.indexOf("."));
        getPublicationTemplateManager().getPublicationTemplate(
            context.getComponentInstanceId() + ":" + xmlFormShortName, formName);
      } catch (final PublicationTemplateException e) {
        formName = null;
      }
    }
    return formName;
  }
}
