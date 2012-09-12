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

import java.util.Date;

import org.silverpeas.process.management.AbstractDataProcess;
import org.silverpeas.process.session.Session;

import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.gallery.GalleryContentManager;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.dao.PhotoDAO;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractGalleryDataProcess extends
    AbstractDataProcess<GalleryProcessExecutionContext> {
  private final PhotoDetail photo;
  private GalleryProcessExecutionContext context;
  private GalleryContentManager galleryContentManager;
  private OrganizationController organizationController;

  /**
   * Default constructor
   * @param photo
   */
  protected AbstractGalleryDataProcess(final PhotoDetail photo) {
    this.photo = photo;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.SilverpeasProcess#process(org.silverpeas.process.management.
   * ProcessExecutionContext, org.silverpeas.process.session.Session)
   */
  @Override
  public final void process(final GalleryProcessExecutionContext context, final Session session)
      throws Exception {
    this.context = context;
    processData(context, session);
  }

  /**
   * @param context
   * @param session
   * @throws Exception
   */
  abstract protected void processData(final GalleryProcessExecutionContext context,
      final Session session) throws Exception;

  /**
   * Access to the GalleryBm
   * @return
   */
  protected GalleryBm getGalleryBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class)
          .create();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("AbstractGalleryDataProcess.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Access to the NodeBm
   * @return
   */
  public NodeBm getNodeBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class).create();
    } catch (final Exception e) {
      throw new GalleryRuntimeException("AbstractGalleryDataProcess.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
  }

  /**
   * @return the photo
   */
  protected PhotoDetail getPhoto() {
    return photo;
  }

  /**
   * Access to gallery content manager
   * @return
   */
  protected GalleryContentManager getGalleryContentManager() {
    if (galleryContentManager == null) {
      galleryContentManager = new GalleryContentManager();
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
   * Gets an XML form name if it exists for the photo
   * @return
   */
  protected String getXMLFormName() {
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

  /**
   * Centralizes the photo creation
   * @param albumId
   * @throws Exception
   */
  protected void createPhoto(final String albumId) throws Exception {

    // Sets technical data
    getPhoto().setPhotoPK(new PhotoPK("unknown", context.getComponentInstanceId()));
    getPhoto().setCreatorId(context.getUser().getId());
    getPhoto().setUpdateId(null);
    getPhoto().setCreationDate(new Date());
    getPhoto().setUpdateDate(null);

    // Insert photo in database
    getPhoto().getPhotoPK().setId(PhotoDAO.createPhoto(context.getConnection(), getPhoto()));

    // Insert path of the photo
    PhotoDAO.createPath(context.getConnection(), getPhoto(), albumId);
  }

  /**
   * Centralizes the photo update
   * @throws Exception
   */
  protected void updatePhoto() throws Exception {
    getPhoto().setUpdateDate(new Date());
    getPhoto().setUpdateId(context.getUser().getId());
    if (!StringUtil.isDefined(getPhoto().getTitle())) {
      getPhoto().setTitle(getPhoto().getImageName());
    }
    PhotoDAO.updatePhoto(context.getConnection(), getPhoto());
  }

  /**
   * Centralizes the photo path update
   * @param fromComponentInstanceId
   * @param albumId
   * @throws Exception
   */
  protected void updatePhotoPath(final String fromComponentInstanceId, final String albumId)
      throws Exception {
    PhotoDAO.deletePhotoPath(context.getConnection(), getPhoto().getId(), fromComponentInstanceId);
    PhotoDAO.addPhotoPath(context.getConnection(), getPhoto().getId(), albumId,
        context.getComponentInstanceId());
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
}
