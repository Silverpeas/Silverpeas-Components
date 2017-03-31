/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.process;

import org.silverpeas.components.gallery.GalleryContentManager;
import org.silverpeas.components.gallery.dao.MediaDAO;
import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.process.management.AbstractDataProcess;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractGalleryDataProcess extends
    AbstractDataProcess<ProcessExecutionContext> {
  private final Media media;
  private GalleryContentManager galleryContentManager;
  private OrganizationController organizationController;

  /**
   * Default constructor
   * @param media
   */
  protected AbstractGalleryDataProcess(final Media media) {
    this.media = media;
  }

  /*
   * (non-Javadoc)
   * @see SilverpeasProcess#process(org.silverpeas.process.management.
   * ProcessExecutionContext, ProcessSession)
   */
  @Override
  public final void process(final ProcessExecutionContext processExecutionContext,
      final ProcessSession session) throws Exception {
    processData(processExecutionContext, session);
  }

  /**
   * @param context
   * @param session
   * @throws Exception
   */
  abstract protected void processData(final ProcessExecutionContext context,
      final ProcessSession session) throws Exception;

  /**
   * Access to the GalleryService
   * @return
   */
  protected GalleryService getGalleryBm() {
    return MediaServiceProvider.getMediaService();
  }

  /**
   * @return the media
   */
  protected Media getMedia() {
    return media;
  }

  /**
   * Access to gallery content manager
   * @return
   */
  protected GalleryContentManager getGalleryContentManager() {
    if (galleryContentManager == null) {
      galleryContentManager = ServiceProvider.getService(GalleryContentManager.class);
    }
    return galleryContentManager;
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  protected GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   * @return an instance of PublicationTemplateManager.
   */
  protected PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  /**
   * Gets an XML form name if it exists for the media
   * @param context
   * @return
   */
  protected String getXMLFormName(final ProcessExecutionContext context) {
    String formName =
        getOrganisationController().getComponentParameterValue(context.getComponentInstanceId(),
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

  /**
   * Centralizes the media creation
   * @param albumId
   * @param context
   * @throws Exception
   */
  protected void createMedia(final String albumId, final ProcessExecutionContext context)
      throws Exception {

    // Sets technical data
    getMedia().setMediaPK(new MediaPK("unknown", context.getComponentInstanceId()));
    getMedia().setCreator(context.getUser());

    // Insert media in database
    getMedia().getMediaPK().setId(MediaDAO
        .saveMedia(OperationContext.fromUser(context.getUser()),
            getMedia()));

    // Insert path of the media
    MediaDAO.saveMediaPath(getMedia(), albumId);
  }

  /**
   * Centralizes the media update
   * @param updateTechnicalDataRequired
   * @param context
   * @throws Exception
   */
  protected void updateMedia(final boolean updateTechnicalDataRequired,
      final ProcessExecutionContext context) throws Exception {
    if (getMedia() instanceof InternalMedia) {
      if (!StringUtil.isDefined(getMedia().getTitle())) {
        getMedia().setTitle(((InternalMedia) getMedia()).getFileName());
      }
    }
    MediaDAO.saveMedia(OperationContext.fromUser(context.getUser())
        .setUpdatingInCaseOfCreation(!updateTechnicalDataRequired), getMedia());
  }

  /**
   * Access to the shared OrganizationController
   * @return
   */
  protected OrganizationController getOrganisationController() {
    if (organizationController == null) {
      organizationController = OrganizationControllerProvider.getOrganisationController();
    }
    return organizationController;
  }
}
