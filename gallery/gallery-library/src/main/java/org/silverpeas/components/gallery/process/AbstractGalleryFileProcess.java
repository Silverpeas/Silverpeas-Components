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

import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.process.management.AbstractFileProcess;
import org.silverpeas.core.process.management.ProcessExecutionContext;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractGalleryFileProcess
    extends AbstractFileProcess<ProcessExecutionContext> {

  private final Media media;
  private OrganizationController organizationController;

  /**
   * Default constructor
   * @param media
   */
  protected AbstractGalleryFileProcess(final Media media) {
    this.media = media;
  }

  /**
   * @return the media
   */
  protected Media getMedia() {
    return media;
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
